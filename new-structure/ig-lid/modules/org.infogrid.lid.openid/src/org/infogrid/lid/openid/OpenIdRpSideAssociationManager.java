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

import org.infogrid.util.SmartFactory;

/**
 * Knows how to manage RelyingParty-side OpenID associations.
 * <p>The parameters are as follows:</p>
 * <ul>
 *  <li>key: the identity provider endpoint URL</li>
 *  <li>value: the found or created OpenIdRpSideAssociation</li>
 *  <li>argument: negotiation parameters, e.g. which crypto to use</li>
 * </ul>
 * <p>Note that this is not keyed by association handle, but by identity provider URL.</p>
 */
public interface OpenIdRpSideAssociationManager
    extends
        SmartFactory<String,OpenIdRpSideAssociation,OpenIdRpSideAssociationNegotiationParameters>
{
}
