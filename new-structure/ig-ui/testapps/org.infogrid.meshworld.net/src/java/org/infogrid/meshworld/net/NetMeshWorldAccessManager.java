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

package org.infogrid.meshworld.net;

import org.infogrid.meshbase.security.AccessManager;
import org.infogrid.meshbase.net.security.DelegatingNetAccessManager;
import org.infogrid.model.AclBasedSecurity.accessmanager.AclBasedAccessManager;

/**
 * An AccesssManager specifically for the NetMeshWorld application. FIXME?
 */
public class NetMeshWorldAccessManager
        extends
            DelegatingNetAccessManager
{
    /**
     * Factory method.
     * 
     * @return the created NetMeshWorldAccessManager
     */
    public static NetMeshWorldAccessManager create()
    {
        AclBasedAccessManager delegate = AclBasedAccessManager.create();
        
        return new NetMeshWorldAccessManager( delegate );
    }

    /**
     * Constructor.
     *
     * @param delegate the delegate AccessManager
     */
    protected NetMeshWorldAccessManager(
            AccessManager delegate )
    {
        super( delegate );
    }
}
