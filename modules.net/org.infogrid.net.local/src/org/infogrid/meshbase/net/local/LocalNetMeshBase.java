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

package org.infogrid.meshbase.net.local;

import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.probe.shadow.ShadowMeshBase;

import org.infogrid.util.NameServer;

import java.util.Collection;

/**
 * This NetMeshBase maintains local ShadowMeshBases.
 */
public interface LocalNetMeshBase
        extends
            NetMeshBase
{
    /**
     * Obtain a ShadowMeshBase that we are operating.
     *
     * @param networkId the identifier of the data source for which the ShadowMeshBase shall be returned
     * @return the ShadowMeshBase, or null
     */
    public ShadowMeshBase getShadowMeshBaseFor(
            NetMeshBaseIdentifier networkId );

    /**
     * Obtain all ShadowMeshBases currently operating.
     *
     * @return all ShadowMeshBases
     */
    public Collection<ShadowMeshBase> getAllShadowMeshBases();

    /**
     * Obtain the NetMeshBases (this one and all ShadowMeshBases) as a NameServer.
     * 
     * @return NameServer
     */
    public NameServer<NetMeshBaseIdentifier,NetMeshBase> getLocalNameServer();
    
    /**
     * The name of the bound property we use to express "the set of all ShadowMeshBases has changed".
     */
    public static final String ALL_SHADOW_MESH_BASES_PROPERTY = "AllShadowMeshBases";
}
