//
// This file is part of InfoGrid(tm). You may not use this file except in
// compliance with the InfoGrid license. The InfoGrid license and important
// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
// or you do not consent to all aspects of the license and the disclaimers,
// no license is granted; do not use this file.
// 
// For more information about InfoGrid go to http://infogrid.org/
//
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.lid.openid;

import java.util.Map;
import java.util.StringTokenizer;
import org.infogrid.lid.AbstractLidClientSignature;
import org.infogrid.lid.LidNonceManager;
import org.infogrid.util.Base64;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * An OpenId-based ClientSignature.
 */
public class OpenIdClientSignature
        extends
            AbstractLidClientSignature
{
    private static final Log log = Log.getLogInstance( OpenIdClientSignature.class ); // our own, private logger
    
    /**
     * Constructor.
     * 
     * @param request the incoming request
     * @param rpUrl the Relying Party's URL without identity parameters
     * @param identifier the provided identifier
     * @param credential the provided credential
     * @param lidCookieString the identifier held by the LID identifier cookie, if any
     * @param sessionId the content of the LID session cookie, if any
     * @param target the user's destination URL, if any
     * @param realm the realm of the trust request, if any
     * @param nonce the nonce in the request, if any
     * @param nonceManager the LidNonceManager to use to validate the nonce
     * @param openIdMode the provided OpenID mode parameter
     * @param openIdAssocHandle the provided association handle
     * @param openIdSigned the signed OpenID fields
     * @param openIdFields the fields as name-value pairs
     * @param associationManager the OpenID RelyingPartySideAssociationManager to use
     */
    public OpenIdClientSignature(
            SaneRequest                        request,
            String                             rpUrl,
            String                             identifier,
            String                             credential,
            String                             lidCookieString,
            String                             sessionId,
            String                             target,
            String                             realm,
            String                             nonce,
            LidNonceManager                    nonceManager,
            String                             openIdMode,
            String                             openIdAssocHandle,
            String                             openIdSigned,
            Map<String,String>                 openIdFields,
            RelyingPartySideAssociationManager associationManager )
    {
        super( request, rpUrl, identifier, credential, lidCookieString, sessionId, target, realm, nonce, nonceManager );

        theOpenIdMode              = openIdMode;
        theOpenIdAssociationHandle = openIdAssocHandle;
        theOpenIdSigned            = openIdSigned;
        theOpenIdFields            = openIdFields;
        theAssociationManager      = associationManager;
    }

    /**
     * Obtain the credential type provided by the client, if any.
     *
     * @return the credential type provided by the client, or null
     */
    public String getCredentialType()
    {
        return OPENID_CREDENTIAL_TYPE;
    }

    /**
     * Is this Request signed. Does not imply that the signature was valid.
     *
     * @return true if the request is signed
     */
    @Override
    public boolean isSignedRequest()
    {
        return theCredential != null && "id_res".equals( theOpenIdMode );
    }

    /**
     * The internal method that determines whether or not a request was signed, and if so,
     * whether the signature is any good.
     *
     * @return true if the Request is signed and the signature is good, false otherwise
     */
    protected boolean determineSignedGoodRequest()
    {
        if( theAssociation == null ) {
            // this is not going to be multi-threaded
            theAssociation = theAssociationManager.get( theOpenIdAssociationHandle );
        }

        if( theAssociation == null ) {
            return false; // we don't do dumb mode
        }

        StringBuilder toSign1 = new StringBuilder( 256 );

        StringTokenizer tokenizer = new StringTokenizer( theOpenIdSigned, "," );
        while( tokenizer.hasMoreTokens() ) {
            String field = tokenizer.nextToken();
            String value = theOpenIdFields.get( "openid." + field );

            toSign1.append( field ).append( ":" ).append( value );
            toSign1.append( "\n" );
        }
        String toSign1String = toSign1.toString();

        byte [] hmacSha1      = CryptUtils.calculateHmacSha1( theAssociation.getSharedSecret(), toSign1String.getBytes() );
        String  locallySigned = Base64.base64encodeNoCr( hmacSha1 );

        if( locallySigned.equals( theCredential )) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Obtain the URL of the identity provider's endpoint.
     * 
     * @return the identifier of the identity provider's endpoint, if any
     */
    public String getIdpEndpointIdentifier()
    {
        if( theAssociation == null ) {
            // this is not going to be multi-threaded
            theAssociation = theAssociationManager.get( theOpenIdAssociationHandle );
        }
        String ret = theAssociation.getServerUrl();
        return ret;
    }

    /**
     * Value of the OpenIdSignature mode parameter.
     */
    protected String theOpenIdMode;

    /**
     * Value of the OpenIdSignature Association Handle parameter.
     */
    protected String theOpenIdAssociationHandle;

    /**
     * Value of the OpenIdSignature signed parameter.
     */
    protected String theOpenIdSigned;

    /**
     * Map of OpenIdSignature fields as they came in, in order to make signature validation easier.
     */
    protected Map<String,String> theOpenIdFields;

    /**
     * The RelyingPartyAssociationManager to use.
     */
    protected RelyingPartySideAssociationManager theAssociationManager;

    /**
     * The actual association, once it has been retried.
     */
    protected RelyingPartySideAssociation theAssociation;

    /**
     * The name of the default OpenIdSignature credential type.
     */
    public static final String OPENID_CREDENTIAL_TYPE = "net.openid.credentialtype.HMAC-SHA1";
}
