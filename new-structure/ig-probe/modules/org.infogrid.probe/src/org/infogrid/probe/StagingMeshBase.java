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

package org.infogrid.probe;

import org.infogrid.meshbase.net.IterableNetMeshBase;

/**
 * The specific subtype of MeshBase that is used for Probes to stage the information
 * that they read. In most cases, application developers do not need to directly interact
 * with this interface and classes implementing it.
 */
public interface StagingMeshBase
        extends
            IterableNetMeshBase
{
    /**
     * Enable the ProbeDispatcher to create the home object in the StagingMeshBase.
     *
     * @param timeCreated the creation date for the home object
     */
    public void initializeHomeObject(
            long timeCreated );

    /**
     * Enable the associated MeshBaseLifecycleManager to determine the start of the current update.
     * This is only invoked during an actual update.
     *
     * @return the start time of the current update
     */
    public long getCurrentUpdateStartedTime();
    
    /**
     * Obtain a MeshObjectLifecycleManager that is appropriate for StagingMeshBases.
     *
     * @return the StagingMeshBaseLifecycleManager
     */
    public StagingMeshBaseLifecycleManager getMeshBaseLifecycleManager();
    
    /**
     * Allow a Proxy to tell this StagingMeshBase that it performed an operation that
     * modified data in the StagingMeshBase, and the StagingMeshBase may have to be flushed to disk.
     */
    public void flushMeshBase();
}