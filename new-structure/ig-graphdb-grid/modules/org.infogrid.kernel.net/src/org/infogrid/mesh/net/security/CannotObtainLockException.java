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

package org.infogrid.mesh.net.security;

import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;

/**
 * Thrown if the acquisition of update rights failed because the caller did not have permission
 * to acquire them.
 */
public class CannotObtainLockException
        extends
            NotPermittedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param obj the NetMeshObject whose update rights could not be obtained
     */
    public CannotObtainLockException(
            NetMeshObject obj )
    {
        super( obj );
    }

    /**
     * Constructor.
     *
     * @param obj the NetMeshObject whose update rights could not be obtained
     * @param cause the underlying cause
     */
    public CannotObtainLockException(
            NetMeshObject obj,
            Throwable     cause )
    {
        super( obj, cause );
    }
}
