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

package org.infogrid.meshbase.net;

import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.Sweeper;

/**
 * Adds, to the Sweeper, methods specific to NetMeshObjects and NetMeshBases.
 */
public interface NetSweeper
        extends
            Sweeper
{
    /**
     * Determine whether this candidate NetMeshObject should be purged, according
     * to this Sweeper.
     *
     * @param candidate the NetMeshObject that is a candidate for purging
     * @return true of the NetMeshObject should be swept
     */
    public boolean shouldBePurged(
            NetMeshObject candidate );
    
    /**
     * Invoked by a background sweep process, the Sweeper will purge the
     * NetMeshObject if this NetMeshObject should be purged.
     *
     * @param candidate the NetMeshObject that is a candidate for purged
     * @return true if the MeshObject was purged
     */
    public boolean potentiallyPurge(
            NetMeshObject candidate );
}
