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

package org.infogrid.probe;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.IterableNetMeshBaseDifferencer;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.transaction.ForwardReferenceCreatedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectCreatedEvent;
import org.infogrid.probe.shadow.ShadowMeshBase;

/**
 * A MeshBaseDifferencer specifically for Probes. It knows about ForwardReferences.
 */
public class ProbeDifferencer
        extends
            IterableNetMeshBaseDifferencer
{
    /**
     * Constructor.
     *
     * @param baseline the baseline ShadowMeshBase against which we compare.
     */
    public ProbeDifferencer(
            ShadowMeshBase baseline )
    {
        super( baseline );
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     *
     * @param obj the MeshObject that was created
     * @param time the time at which the creation occurred
     * @return the MeshObjectCreatedEvent or subclass
     */
    @Override
    protected NetMeshObjectCreatedEvent createMeshObjectCreatedEvent(
            MeshObject             obj,
            long                   time )
    {
        NetMeshObject realObj = (NetMeshObject) obj;

        NetMeshObjectCreatedEvent ret;

        if( realObj.getProxyTowardsHomeReplica() != null ) {
            ret = new ForwardReferenceCreatedEvent(
                    (NetMeshBase) obj.getMeshBase(),
                    (NetMeshBaseIdentifier) obj.getMeshBase().getIdentifier(),
                    ((NetMeshObject) obj).getPathToHomeReplica(),
                    (NetMeshObject) obj,
                    null,
                    time );

        } else {
            ret = new NetMeshObjectCreatedEvent(
                    (NetMeshBase) obj.getMeshBase(),
                    (NetMeshBaseIdentifier) obj.getMeshBase().getIdentifier(),
                    (NetMeshObject) obj,
                    null,
                    time );
        }

        return ret;
    }
}
