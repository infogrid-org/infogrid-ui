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

import org.infogrid.lid.credential.AbstractLidCredentialType;
import org.infogrid.lid.openid.OpenIdRpSideAssociationManager;

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
     */
    protected AbstractOpenIdCredentialType(
            OpenIdRpSideAssociationManager associationManager )
    {
        theAssociationManager = associationManager;
    }

    /**
     * The association manager to use.
     */
    protected OpenIdRpSideAssociationManager theAssociationManager;

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
}
