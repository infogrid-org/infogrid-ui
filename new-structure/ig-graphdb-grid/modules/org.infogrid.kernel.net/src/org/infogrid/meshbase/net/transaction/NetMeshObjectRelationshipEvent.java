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

import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.transaction.MeshObjectRelationshipEvent;

/**
 * Abstraction for neighbor and role changes.
 */
public interface NetMeshObjectRelationshipEvent
    extends
        MeshObjectRelationshipEvent
{
    /**
     * Obtain the MeshObject affected by this Change.
     *
     * @return obtain the MeshObject affected by this Change
     */
    public NetMeshObject getAffectedMeshObject();

    /**
     * Obtain the MeshObjectIdentifier of the MeshObject affected by this Change.
     *
     * @return the MeshObjectIdentifier of the NetMeshObject affected by this Change
     */
    public NetMeshObjectIdentifier getAffectedMeshObjectIdentifier();

    /**
     * Obtain the neighbor that changed.
     *
     * @return the neighbor MeshObject
     */
    public NetMeshObject getNeighborMeshObject();

    /**
     * Obtain the Identifier of the neighbor that changed.
     *
     * @return the Identifier of the neighbor MeshObject
     */
    public NetMeshObjectIdentifier getNeighborMeshObjectIdentifier();
}
