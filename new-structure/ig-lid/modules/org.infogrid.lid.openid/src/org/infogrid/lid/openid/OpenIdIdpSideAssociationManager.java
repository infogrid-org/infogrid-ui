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

package org.infogrid.lid.openid;

import org.infogrid.util.WritableNameServer;

/**
 * Knows how to manage Identity-Provider-side OpenID associations. This does not
 * distinguish between smart and dumb associations; to do so, instantiate two
 * objects of this type.
 */
public interface OpenIdIdpSideAssociationManager
    extends
        WritableNameServer<String,OpenIdIdpSideAssociation>
{
    /**
     * Create a new association.
     *
     * @param sessionType the session type
     * @return the created association
     */
    public OpenIdIdpSideAssociation create(
            String sessionType );
}
