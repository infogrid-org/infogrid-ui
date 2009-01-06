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

package org.infogrid.mesh.net.externalized;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.externalized.ExternalizedMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

/**
 * Adds Net information to ExternalizedMeshObject.
 */
public interface ExternalizedNetMeshObject
        extends
            ExternalizedMeshObject
{
    /**
     * Obtain the NetMeshObjectIdentifier of the NetMeshObject.
     *
     * @return the NetMeshObjectIdentifier of the NetMeshObject
     */
    public abstract NetMeshObjectIdentifier getIdentifier();

    /**
     * Obtain the NetMeshObjectIdentifiers of the neighbors of this NetMeshObject.
     *
     * @return the NetMeshObjectIdentifiers of the neighbors
     */
    public abstract NetMeshObjectIdentifier [] getNeighbors();

    /**
     * Obtain the NetMeshObjectIdentifiers of the NetMeshObjects that participate in an equivalence
     * set with this NetMeshObject.
     *
     * @return the NetMeshObjectIdentifiers. May be null.
     */
    public abstract NetMeshObjectIdentifier [] getEquivalents();
    
    /**
     * Obtain the GiveUpHomeReplica property.
     * 
     * @return the GiveUpHomeReplica property
     */
    public abstract boolean getGiveUpHomeReplica();

    /**
     * Obtain the GiveUpLock property.
     *
     * @return the GiveUpLock property
     */
    public abstract boolean getGiveUpLock();

    /**
     * Obtain the NetMeshBaseIdentifiers of all Proxies affected by this NetMeshObject.
     *
     * @return the NetMeshBaseIdentifiers, if any
     */
    public abstract NetMeshBaseIdentifier[] getProxyIdentifiers();

    /**
     * Obtain the NetMeshBaseIdentifier of the Proxy towards the home replica.
     * 
     * @return the NetMeshBaseIdentifier, if any
     */
    public abstract NetMeshBaseIdentifier getProxyTowardsHomeNetworkIdentifier();

    /**
     * Obtain the NetMeshBaseIdentifier of the Proxy towards the replica with the lock.
     * 
     * @return the NetMeshBaseIdentifier, if any
     */
    public abstract NetMeshBaseIdentifier getProxyTowardsLockNetworkIdentifier();

    /**
     * Obtain the NetMeshObjectIdentifiers of the partner NetMeshBases from which this
     * NetMeshObject obtained relationship information.
     *
     * @param neighbor the identifier of the neighbor
     * @return the NetMeshBaseIdentifiers of the partner NetMeshBases
     */
    public abstract NetMeshBaseIdentifier [] getRelationshipProxyIdentifiersFor(
            MeshObjectIdentifier neighbor );
}

