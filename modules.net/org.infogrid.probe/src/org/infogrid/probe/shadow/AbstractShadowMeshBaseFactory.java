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

package org.infogrid.probe.shadow;

import org.infogrid.context.Context;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.util.AbstractFactory;

/**
 * This type knows how to create ShadowMeshBases.
 */
public abstract class AbstractShadowMeshBaseFactory
        extends
            AbstractFactory<NetMeshBaseIdentifier,ShadowMeshBase,CoherenceSpecification>
        implements
            ShadowMeshBaseFactory
{
    /**
     * Constructor.
     * 
     * @param modelBase the ModelBase to use for created ShadowMeshBases
     * @param endpointFactory the Context to use for created ShadowMeshBases
     * @param probeDirectory the ProbeDirectory to use for the created ShadowMeshBases
     * @param timeNotNeededTillExpires the time until unneeded ShadowMeshBases disappear, in milliseconds
     * @param c the Context to use for created ShadowMeshBases
     */
    protected AbstractShadowMeshBaseFactory(
            ModelBase                 modelBase,
            ProxyMessageEndpointFactory endpointFactory,
            ProbeDirectory            probeDirectory,
            long                      timeNotNeededTillExpires,
            Context                   c )
    {
        theModelBase                = modelBase;
        theEndpointFactory          = endpointFactory;
        theProbeDirectory           = probeDirectory;
        theTimeNotNeededTillExpires = timeNotNeededTillExpires;
        theMeshBaseContext          = c;
    }
    
    /**
     * Factory for NetMessageEndpoints. This is shared by all created ShadowMeshBases.
     */
    protected ProxyMessageEndpointFactory theEndpointFactory;

    /**
     * The ModelBase which the created MeshBases will use.
     */
    protected ModelBase theModelBase;

    /**
     * The ProbeDirectory to use for the created ShadowMeshBases.
     */
    protected ProbeDirectory theProbeDirectory;

    /**
     * The time, in milliseconds, until unused ShadowMeshBases should self-destruct. -1 indicates never.
     */
    protected long theTimeNotNeededTillExpires;

    /**
     * The Context in which the created MeshBases will run.
     */
    protected Context theMeshBaseContext;    
}
