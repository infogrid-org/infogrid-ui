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

package org.infogrid.mesh.net.security;

import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

/**
 * Thrown if a NetMeshObject is attempted to be created in a NetMeshBase that
 * has a different NetMeshBaseIdentifier.
 */
public class CannotCreateNonLocalMeshObjectException
        extends
            NotPermittedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param mb the MeshBase in which this Exception was created
     * @param identifier the MeshObjectIdentifier for the MeshObject on which the illegal operation was attempted
     */
    public CannotCreateNonLocalMeshObjectException(
            NetMeshBase             mb,
            NetMeshObjectIdentifier identifier )
    {
        super( mb, mb.getIdentifier(), null, identifier );
    }

    /**
     * Constructor.
     *
     * @param mb the MeshBase in which this Exception was created
     * @param originatingMeshBaseIdentifier the MeshBaseIdentifier of the MeshBase in which this Exception was created
     * @param identifier the MeshObjectIdentifier for the MeshObject on which the illegal operation was attempted
     */
    public CannotCreateNonLocalMeshObjectException(
            NetMeshBase             mb,
            NetMeshBaseIdentifier   originatingMeshBaseIdentifier,
            NetMeshObjectIdentifier identifier )
    {
        super( mb, originatingMeshBaseIdentifier, null, identifier );
    }
}
