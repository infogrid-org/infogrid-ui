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

package org.infogrid.lid.gpg;

import java.io.IOException;
import org.infogrid.lid.AbstractLidClientSignature;
import org.infogrid.lid.LidNonceManager;
import org.infogrid.util.FactoryException;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * A LID GPG-based ClientSignature.
 */
public class LidGpgClientSignature
        extends
            AbstractLidClientSignature
{
    private static final Log log = Log.getLogInstance( LidGpgClientSignature.class ); // our own, private logger
    
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
     * @param publicKeyManager the LidGpgPublicKeyManager to use to validate the signature
     */
    public LidGpgClientSignature(
            SaneRequest            request,
            String                 rpUrl,
            String                 identifier,
            String                 credential,
            String                 lidCookieString,
            String                 sessionId,
            String                 target,
            String                 realm,
            String                 nonce,
            LidNonceManager        nonceManager,
            LidGpgPublicKeyManager publicKeyManager )
    {
        super( request, rpUrl, identifier, credential, lidCookieString, sessionId, target, realm, nonce, nonceManager );
        
        thePublicKeyManager = publicKeyManager;
    }

    /**
     * Obtain the credential type provided by the client, if any.
     *
     * @return the credential type provided by the client, or null
     */
    public String getCredentialType()
    {
        return GPG_CREDENTIAL_TYPE;
    }

    /**
     * The internal method that determines whether or not a request was signed, and if so,
     * whether the signature is any good.
     *
     * @return true if the Request is signed and the signature is good, false otherwise
     */
    protected boolean determineSignedGoodRequest()
    {
        LidGpg theGpg       = LidGpg.create( theNonceManager );
        String thePublicKey = null;
        try {
            thePublicKey = thePublicKeyManager.obtainFor( theIdentifier );

        } catch( FactoryException ex ) {
            log.warn( ex );
        }
        if( thePublicKey == null ) {
            // can't do anything here
            return false;
        }

        boolean ret = false;
        try {
            theGpg.importPublicKey( thePublicKey );

            String fullUri    = theRequest.getAbsoluteFullUri();
            String postString = theRequest.getPostData();

            String signedText = theGpg.reconstructSignedMessage( fullUri, postString, theCredential );

            ret = theGpg.validateSignedText( theIdentifier, signedText );

        } catch( IOException ex ) {
            log.warn( ex );
        }
         return ret;
    }

    /**
     * Obtain the URL of the identity provider's endpoint.
     * 
     * @return the identifier of the identity provider's endpoint, if any
     */
    public String getIdpEndpointIdentifier()
    {
        return theIdentifier; // is this the best we can do?
    }

    /**
     * The manager of public keys to use.
     */
    protected LidGpgPublicKeyManager thePublicKeyManager;

    /**
     * The name of the GPG credential type.
     */
    public static final String GPG_CREDENTIAL_TYPE = "gpg --clearsign";
}
    