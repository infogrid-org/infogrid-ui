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
import org.infogrid.lid.LidNonceManager;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.lid.openid.OpenIdRpSideAssociationManager;
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
        super( associationManager, nonceManager );
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
        if( !request.matchUrlArgument( OPENID_NS_PARAMETER_NAME, OPENID_AUTHV2_VALUE )) {
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
        checkCredential( request, subject, MANDATORY_FIELDS, OPENID_NONCE_PARAMETER_NAME );
    }

    /**
     * NS value that indicates OpenID Authentication V2.
     */
    public static final String OPENID_AUTHV2_VALUE = "http://specs.openid.net/auth/2.0";

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
