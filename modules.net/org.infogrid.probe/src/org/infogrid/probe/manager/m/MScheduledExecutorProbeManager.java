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

package org.infogrid.probe.manager.m;

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

import org.infogrid.probe.manager.ScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;

import org.infogrid.util.CachingMap;
import org.infogrid.util.MCachingHashMap;

/**
 * A ScheduledExecutorProbeManager that keeps Shadows in Memory.
 */
public class MScheduledExecutorProbeManager
        extends
            ScheduledExecutorProbeManager
{
    /**
     * Factory method.
     *
     * @param delegateFactory the delegate ShadowMeshBaseFactory that knows how to instantiate ShadowMeshBases
     * @return the created MPassiveProbeManager
     */
    public static MScheduledExecutorProbeManager create(
            ShadowMeshBaseFactory delegateFactory )
    {
        CachingMap<NetMeshBaseIdentifier,ShadowMeshBase> storage = MCachingHashMap.create();

        MScheduledExecutorProbeManager ret = new MScheduledExecutorProbeManager( delegateFactory, storage );
        
        return ret;
    }

    /**
     * Constructor.
     * 
     * @param delegateFactory the delegate ShadowMeshBaseFactory that knows how to instantiate ShadowMeshBases
     * @param storage the storage to use
     */
    protected MScheduledExecutorProbeManager(
            ShadowMeshBaseFactory                            delegateFactory,
            CachingMap<NetMeshBaseIdentifier,ShadowMeshBase> storage )
    {
        super( delegateFactory, storage );
    }
}
