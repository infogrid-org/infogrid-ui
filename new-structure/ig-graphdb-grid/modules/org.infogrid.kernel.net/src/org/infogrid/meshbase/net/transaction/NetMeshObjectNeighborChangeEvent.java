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

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.meshbase.transaction.MeshObjectNeighborChangeEvent;

/**
 * Indicates that a NetMeshObject's set of neighbors has changed.
 */
public interface NetMeshObjectNeighborChangeEvent
        extends
            MeshObjectNeighborChangeEvent,
            NetMeshObjectRelationshipEvent,
            NetChange<MeshObject,MeshObjectIdentifier,MeshObject[],MeshObjectIdentifier[]>
{
    // nothing
}
