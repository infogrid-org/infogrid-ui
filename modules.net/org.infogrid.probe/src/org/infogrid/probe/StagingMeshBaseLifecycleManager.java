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

import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;

/**
 * <p>Adds methods to create ForwardReferences to a MeshBaseLifecycleManager.</p>
 *
 * <p>Note: This should really not inherit from NetMeshBaseLifecycleManager, only from MeshBaseLifecycleManager.
 * However, due to a bug in the Java compiler in Java 5, that's how the inheritance hierarchy needs to look like.
 * For that reason, getNetMeshBase must return NetMeshBase, too, instead of StagingMeshBase.
 * In later versions, I hope to be able to remove this workaround.</p>
 */
public interface StagingMeshBaseLifecycleManager
        extends
            MeshBaseLifecycleManager,
            NetMeshBaseLifecycleManager
{
}
