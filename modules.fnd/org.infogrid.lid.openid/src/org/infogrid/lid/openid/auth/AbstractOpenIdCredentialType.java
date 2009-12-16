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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.lid.openid.auth;

import java.util.HashSet;
import java.util.StringTokenizer;
import org.infogrid.lid.LidInvalidNonceException;
import org.infogrid.lid.LidNonceManager;
import org.infogrid.lid.credential.AbstractLidCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.lid.openid.CryptUtils;
import org.infogrid.lid.openid.OpenIdAssociationExpiredException;
import org.infogrid.lid.openid.OpenIdInvalidSignatureException;
import org.infogrid.lid.openid.OpenIdNoAssociationException;
import org.infogrid.lid.openid.OpenIdRpSideAssociation;
import org.infogrid.lid.openid.OpenIdRpSideAssociationManager;
import org.infogrid.lid.openid.OpenIdRpSideAssociationNegotiationParameters;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.Base64;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.http.SaneRequest;

/**
 * Common superclass for OpenID authentication credential types.
 */
public abstract class AbstractOpenIdCredentialType
        extends
            AbstractLidCredentialType
{
    /**
     * Constructor, for subclasses only.
     *
     * @param associationManager the relying party-side association manager to use
     * @param nonceManager the LidNonceManager to use
     */
    protected AbstractOpenIdCredentialType(
            OpenIdRpSideAssociationManager associationManager,
            LidNonceManager                nonceManager )
    {
        theAssociationManager = associationManager;
        theNonceManager       = nonceManager;
    }

    /**
     * Determine whether the request contains a valid LidCredentialType of this type
     * for the given subject.
     *
     * @param request the request
     * @param subject the subject
     * @param mandatoryFields set of fields that are mandatory
     * @param nonceParameterName name of the parameter representing the nonce
     * @throws LidInvalidCredentialException thrown if the contained LidCdedentialType is not valid for this subject
     */
    protected void checkCredential(
            SaneRequest     request,
            HasIdentifier   subject,
            HashSet<String> mandatoryFields,
            String          nonceParameterName )
        throws
            LidInvalidCredentialException
    {
        String associationHandle = request.getUrlArgument( OPENID_ASSOC_HANDLE_PARAMETER_NAME );
        String signed            = request.getUrlArgument( OPENID_SIGNED_PARAMETER_NAME );
        String signature         = request.getUrlArgument( OPENID_SIGNATURE_PARAMETER_NAME );

        if( associationHandle == null || associationHandle.length() == 0 ) {
            // we don't do dumb mode
            throw new OpenIdNoAssociationException( subject.getIdentifier(), this );
        }

        String []               endpointCandidates = determineOpenIdEndpointsFor( subject );
        OpenIdRpSideAssociation association        = null;

        for( String epCandidate : endpointCandidates ) {

            OpenIdRpSideAssociation assocCandidate = theAssociationManager.get( epCandidate );
            if( assocCandidate != null && assocCandidate.getAssociationHandle().equals( associationHandle )) {
                // found
                association = assocCandidate;
                break;
            }
        }

        if( association == null ) {
            // we don't do dumb mode
            throw new OpenIdNoAssociationException( subject.getIdentifier(), this );
        }
        if( !association.isCurrentlyValid() ) {
            theAssociationManager.remove( associationHandle );
            throw new OpenIdAssociationExpiredException( subject.getIdentifier(), this );
        }

        try {
            if( nonceParameterName != null ) {
                theNonceManager.validateNonce( request, nonceParameterName );
            } else {
                theNonceManager.validateNonce( request );
            }
        } catch( LidInvalidNonceException ex ) {
            throw new LidInvalidCredentialException( subject.getIdentifier(), this, ex );
        }

        @SuppressWarnings("unchecked")
        HashSet<String> mandatory = mandatoryFields != null ? (HashSet<String>) mandatoryFields.clone() : null;

        StringBuffer toSign1 = new StringBuffer( 256 );

        StringTokenizer tokenizer = new StringTokenizer( signed, "," );
        while( tokenizer.hasMoreTokens() ) {
            String field = tokenizer.nextToken();
            String value = request.getUrlArgument( "openid." + field );

            if( mandatory != null ) {
                mandatory.remove( field );
            }

            toSign1.append( field ).append( ":" ).append( value );
            toSign1.append( "\n" );
        }
        if( mandatory != null && !mandatory.isEmpty() ) {
            throw new OpenIdMandatorySignedFieldMissingException(
                    ArrayHelper.copyIntoNewArray( mandatory, String.class ),
                    subject.getIdentifier(),
                    this );
        }
        String toSign1String = toSign1.toString();

        byte [] hmac;
        if( OpenIdRpSideAssociationNegotiationParameters.DH_SHA256.equals( association.getSessionType() )) {
            hmac = CryptUtils.calculateHmacSha256( association.getSharedSecret(), toSign1String.getBytes() );
        } else {
            hmac = CryptUtils.calculateHmacSha1( association.getSharedSecret(), toSign1String.getBytes() );
        }
        String locallySigned = Base64.base64encodeNoCr( hmac );

        if( !locallySigned.equals( signature )) {
            throw new OpenIdInvalidSignatureException( subject.getIdentifier(), this );
        }
    }

    /**
     * Determine the endpoint URLs that support authentication for this credential type, for this subject.
     *
     * @param subject the subject
     * @return the endpoint URLs
     */
    protected abstract String [] determineOpenIdEndpointsFor(
            HasIdentifier subject );

    /**
     * The association manager to use.
     */
    protected OpenIdRpSideAssociationManager theAssociationManager;

    /**
     * The NonceManager to use.
     */
    protected LidNonceManager theNonceManager;

    /**
     * Name of the URL parameter that indicates the OpenID namespace as defined in the
     * OpenID Authentication V2 specification.
     */
    public static final String OPENID_NS_PARAMETER_NAME = "openid.ns";
    
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
    public static final String OPENID_ASSOC_HANDLE_PARAMETER_NAME = "openid.assoc_handle";

    /**
     * Name of the URL parameter that holds the list of signed fields.
     */
    public static final String OPENID_SIGNED_PARAMETER_NAME = "openid.signed";

    /**
     * Name of the URL parameter that holds the signature.
     */
    public static final String OPENID_SIGNATURE_PARAMETER_NAME = "openid.sig";

    /**
     * Name of the URL parameter that contains the OpenID V2 nonce.
     */
    public static final String OPENID_NONCE_PARAMETER_NAME = "openid.response_nonce";
}
