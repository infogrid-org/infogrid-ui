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
import org.infogrid.util.Base64;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.http.SaneRequest;

/**
 * Represents the OpenID authentication credential type in OpenID Authentication V1.
 */
public abstract class AbstractOpenId1CredentialType
        extends
            AbstractOpenIdCredentialType
{
    /**
     * Constructor.
     *
     * @param associationManager the relying party-side association manager to use
     * @param nonceManager the LidNonceManager to use
     */
    protected AbstractOpenId1CredentialType(
            OpenIdRpSideAssociationManager associationManager,
            LidNonceManager                nonceManager )
    {
        super( associationManager );
        theNonceManager = nonceManager;
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
        if( request.getMultivaluedUrlArgument( OPENID_NS_PARAMETER_NAME ) != null ) {
            return false;
        }
        if( !request.matchUrlArgument( OPENID_MODE_PARAMETER_NAME, OPENID_MODE_IDRES_PARAMETER_VALUE )) {
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
        String associationHandle = request.getUrlArgument( OPENID_ASSOC_HANDLE_PARAMETER_NAME );
        String signed            = request.getUrlArgument( OPENID_SIGNED_PARAMETER_NAME );
        String signature         = request.getUrlArgument( OPENID_SIGNATURE_PARAMETER_NAME );

        if( associationHandle == null || associationHandle.length() == 0 ) {
            // we don't do dumb mode
            throw new OpenIdNoAssociationException( subject.getIdentifier(), this );
        }

        String []               endpointCandidates = determineOpenId1EndpointsFor( subject );
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
            theNonceManager.validateNonce( request );

        } catch( LidInvalidNonceException ex ) {
            throw new LidInvalidCredentialException( subject.getIdentifier(), this, ex );
        }

        StringBuffer toSign1 = new StringBuffer( 256 );

        StringTokenizer tokenizer = new StringTokenizer( signed, "," );
        while( tokenizer.hasMoreTokens() ) {
            String field = tokenizer.nextToken();
            String value = request.getUrlArgument( "openid." + field );

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
     * Determine the endpoint URLs that support OpenID V1 authentication, for this subject.
     *
     * @param subject the subject
     * @return the endpoint URLs
     */
    protected abstract String [] determineOpenId1EndpointsFor(
            HasIdentifier subject );

    /**
     * The NonceManager to use.
     */
    protected LidNonceManager theNonceManager;
}
