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

import java.util.StringTokenizer;
import org.infogrid.lid.LidInvalidNonceException;
import org.infogrid.lid.LidNonceManager;
import org.infogrid.lid.credential.AbstractLidCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.util.Base64;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.http.SaneRequest;

/**
 * Represents the OpenID authentication credential type.
 */
public class OpenIdCredentialType
        extends
            AbstractLidCredentialType
{
    /**
     * Factory method.
     *
     * @param associationManager the relying party-side association manager to use
     * @param nonceManager the LidNonceManager to use
     * @return the created OpenIdCredentialType
     */
    public static OpenIdCredentialType create(
            OpenIdRpSideAssociationManager associationManager,
            LidNonceManager                nonceManager )
    {
        OpenIdCredentialType ret = new OpenIdCredentialType( associationManager, nonceManager );
        return ret;
    }

    /**
     * Constructor.
     *
     * @param associationManager the relying party-side association manager to use
     * @param nonceManager the LidNonceManager to use
     */
    protected OpenIdCredentialType(
            OpenIdRpSideAssociationManager associationManager,
            LidNonceManager                nonceManager )
    {
        theAssociationManager = associationManager;
        theNonceManager       = nonceManager;
    }

    /**
     * Determine whether this LidCredentialType is contained in this request.
     *
     * @param request the request
     * @return true if this LidCredentialType is contained in this request
     */
    public boolean isContainedIn(
            SaneRequest request )
    {
        if( request.matchArgument( OPENID_MODE_PARAMETER_NAME, OPENID_MODE_IDRES_PARAMETER_VALUE )) {
            return true;
        }

        return false;
    }

    /**
     * Determine whether the request contains a valid LidCredentialType of this type
     * for the given subject.
     *
     * @param request the request
     * @param subject the subject
     * @throws LidInvalidCredentialException thrown if the contained LidCdedentialType is not valid for this subject
     */
    public void checkCredential(
            SaneRequest   request,
            HasIdentifier subject )
        throws
            LidInvalidCredentialException
    {
        String associationHandle = request.getArgument( OPENID_ASSOC_HANDLE_PARAMETER_NAME );
        String signed            = request.getArgument( OPENID_SIGNED_PARAMETER_NAME );
        String signature         = request.getArgument( OPENID_SIGNATURE_PARAMETER_NAME );

        OpenIdRpSideAssociation association = theAssociationManager.get( associationHandle );

        if( association == null ) {
            // we don't do dumb mode
            throw new OpenIdNoAssociationException( subject.getIdentifier(), this );
        }
        if( !association.isCurrentlyValid() ) {
            theAssociationManager.remove( associationHandle );
            throw new OpenIdAssociationExpiredException( subject.getIdentifier(), this );
        }

        try {
            theNonceManager.validateNonce( request );

        } catch( LidInvalidNonceException ex ) {
            throw new LidInvalidCredentialException( subject.getIdentifier(), this, ex );
        }

        StringBuffer toSign1 = new StringBuffer( 256 );

        StringTokenizer tokenizer = new StringTokenizer( signed, "," );
        while( tokenizer.hasMoreTokens() ) {
            String field = tokenizer.nextToken();
            String value = request.getArgument( "openid." + field );

            toSign1.append( field ).append( ":" ).append( value );
            toSign1.append( "\n" );
        }
        String toSign1String = toSign1.toString();

        byte [] hmacSha1      = CryptUtils.calculateHmacSha1( association.getSharedSecret(), toSign1String.getBytes() );
        String  locallySigned = Base64.base64encodeNoCr( hmacSha1 );

        if( !locallySigned.equals( signature )) {
            throw new OpenIdInvalidSignatureException( subject.getIdentifier(), this );
        }
    }

    /**
     * The association manager to use.
     */
    protected OpenIdRpSideAssociationManager theAssociationManager;

    /**
     * The NonceManager to use.
     */
    protected LidNonceManager theNonceManager;

    /**
     * Name of the URL parameter that indicates the OpenID mode.
     */
    public static final String OPENID_MODE_PARAMETER_NAME = "openid.mode";

    /**
     * Value of the URL parameter that indicates the OpenID credential.
     */
    public static final String OPENID_MODE_IDRES_PARAMETER_VALUE = "id_res";

    /**
     * Name of the URL parameter that holds the association handle.
     */
    public static final String OPENID_ASSOC_HANDLE_PARAMETER_NAME = "openid.asssoc_handle";

    /**
     * Name of the URL parameter that holds the list of signed fields.
     */
    public static final String OPENID_SIGNED_PARAMETER_NAME = "openid.signed";

    /**
     * Name of the URL parameter that holds the signature.
     */
    public static final String OPENID_SIGNATURE_PARAMETER_NAME = "openid.sig";
}
