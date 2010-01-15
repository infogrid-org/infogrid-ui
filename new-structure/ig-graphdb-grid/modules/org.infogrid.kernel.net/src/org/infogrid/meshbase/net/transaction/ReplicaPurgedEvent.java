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

package org.infogrid.meshbase.net.transaction;

import org.infogrid.mesh.externalized.ExternalizedMeshObject;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

/**
 * Indicates that a replica has been purged. This is a subclass of MeshObjectDeletedEvent as
 * from the perspective of a non-"net"-aware application, there is no difference between
 * a replica having been purged and a semantic delete.
 */
public class ReplicaPurgedEvent
        extends
            AbstractNetMeshObjectDeletedEvent
        implements
            ReplicaEvent
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor. This must be invoked with both the NetMeshObject and the canonical NetMeshObjectIdentifier,
     * because it is not possible to construct the canonical NetMeshObjectIdentifier after the NetMeshObject is dead.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param deletedObject the NetMeshObject that was deleted
     * @param deletedObjectIdentifier the identifier of the NetMeshObject that was deleted
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param externalized the deleted MeshObject in externalized form as it was just prior to deletion
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public ReplicaPurgedEvent(
            NetMeshBase             source,
            NetMeshBaseIdentifier   sourceIdentifier,
            NetMeshObject           deletedObject,
            NetMeshObjectIdentifier deletedObjectIdentifier,
            NetMeshBaseIdentifier   originIdentifier,
            ExternalizedMeshObject  externalized,
            long                    timeEventOccurred )
    {
        super( source, sourceIdentifier, deletedObject, deletedObjectIdentifier, originIdentifier, externalized, timeEventOccurred );
    }

    /**
     * Determine whether this NetChange should be forwarded through the given, outgoing Proxy.
     * If specified, {@link #getOriginNetworkIdentifier} specifies where the NetChange came from.
     *
     * @param outgoingProxy the potential outgoing Proxy
     * @return true if the NetChange should be forwarded torwards the outgoingProxy
     */
    @Override
    public boolean shouldBeSent(
            Proxy outgoingProxy )
    {
        return true;
    }
}
