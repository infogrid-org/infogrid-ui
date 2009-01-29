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

package org.infogrid.lid.openid.auth;

import java.util.HashSet;
import java.util.StringTokenizer;
import org.infogrid.lid.LidInvalidNonceException;
import org.infogrid.lid.LidNonceManager;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.lid.openid.CryptUtils;
import org.infogrid.lid.openid.OpenIdAssociationExpiredException;
import org.infogrid.lid.openid.OpenIdInvalidSignatureException;
import org.infogrid.lid.openid.OpenIdNoAssociationException;
import org.infogrid.lid.openid.OpenIdRpSideAssociation;
import org.infogrid.lid.openid.OpenIdRpSideAssociationManager;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.Base64;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.http.SaneRequest;

/**
 * Represents the OpenID authentication credential type in OpenID Authentication V2.
 */
public abstract class AbstractOpenId2CredentialType
        extends
            AbstractOpenIdCredentialType
{
    /**
     * Constructor.
     *
     * @param associationManager the relying party-side association manager to use
     * @param nonceManager the LidNonceManager to use
     */
    protected AbstractOpenId2CredentialType(
            OpenIdRpSideAssociationManager associationManager,
            LidNonceManager                nonceManager )
    {
        super( associationManager );
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
        if( !request.matchArgument( OPENID_NS_PARAMETER_NAME, OPENID_AUTHV2_VALUE )) {
            return false;
        }
        if( !request.matchArgument( OPENID_MODE_PARAMETER_NAME, OPENID_MODE_IDRES_PARAMETER_VALUE )) {
            return false;
        }

        return true;
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

        if( associationHandle == null || associationHandle.length() == 0 ) {
            // we don't do dumb mode
            throw new OpenIdNoAssociationException( subject.getIdentifier(), this );
        }

        String []               endpointCandidates = determineOpenId2EndpointsFor( subject );
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
            theNonceManager.validateNonce( request, OPENID_NONCE_PARAMETER_NAME );

        } catch( LidInvalidNonceException ex ) {
            throw new LidInvalidCredentialException( subject.getIdentifier(), this, ex );
        }

        @SuppressWarnings("unchecked")
        HashSet<String> mandatory = (HashSet<String>) MANDATORY_FIELDS.clone();

        StringBuffer toSign1 = new StringBuffer( 256 );

        StringTokenizer tokenizer = new StringTokenizer( signed, "," );
        while( tokenizer.hasMoreTokens() ) {
            String field = tokenizer.nextToken();
            String value = request.getArgument( "openid." + field );

            mandatory.remove( field );

            toSign1.append( field ).append( ":" ).append( value );
            toSign1.append( "\n" );
        }
        if( !mandatory.isEmpty() ) {
            throw new OpenIdMandatorySignedFieldMissingException(
                    ArrayHelper.copyIntoNewArray( mandatory, String.class ),
                    subject.getIdentifier(),
                    this );
        }
        String toSign1String = toSign1.toString();

        byte [] hmacSha1      = CryptUtils.calculateHmacSha1( association.getSharedSecret(), toSign1String.getBytes() );
        String  locallySigned = Base64.base64encodeNoCr( hmacSha1 );

        if( !locallySigned.equals( signature )) {
            throw new OpenIdInvalidSignatureException( subject.getIdentifier(), this );
        }
    }

    /**
     * Determine the endpoint URLs that support OpenID V2 authentication, for this subject.
     *
     * @param subject the subject
     * @return the endpoint URLs
     */
    protected abstract String [] determineOpenId2EndpointsFor(
            HasIdentifier subject );

    /**
     * The NonceManager to use.
     */
    protected LidNonceManager theNonceManager;

    /**
     * NS value that indicates OpenID Authentication V2.
     */
    public static final String OPENID_AUTHV2_VALUE = "http://specs.openid.net/auth/2.0";

    /**
     * Name of the URL parameter that contains the OpenID V2 nonce.
     */
    public static final String OPENID_NONCE_PARAMETER_NAME = "openid.response_nonce";

    /**
     * Fields that must be signed per spec.
     */
    public static final HashSet<String> MANDATORY_FIELDS = new HashSet<String>();
    static {
        MANDATORY_FIELDS.add( "op_endpoint" );
        MANDATORY_FIELDS.add( "return_to" );
        MANDATORY_FIELDS.add( "response_nonce" );
        MANDATORY_FIELDS.add( "assoc_handle" );
        MANDATORY_FIELDS.add( "claimed_id" );
        MANDATORY_FIELDS.add( "identity" );
    }
}
