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

package org.infogrid.model.primitives;

import org.infogrid.mesh.MeshObject;

/**
 * This pairs up a RoleType with the other side of a relationship that plays this RoleType.
 * One can think of this as a convenience "struct".
 */
public final class Role
{
    /**
     * Constructor.
     *
     * @param type the RoleType
     * @param otherSide the MeshObject at the other side of the relationship
     */
    public Role(
            RoleType   type,
            MeshObject otherSide )
    {
        theType      = type;
        theOtherSide = otherSide;
    }
    
    /**
     * Obtain the RoleType.
     *
     * @return the RoleType
     */
    public RoleType getRoleType()
    {
        return theType;
    }
    
    /**
     * Obtain the other side.
     *
     * @return the other side
     */
    public MeshObject getOtherSide()
    {
        return theOtherSide;
    }

    /**
     * The RoleType.
     */
    protected RoleType theType;
    
    /**
     * The other side.
     */
    protected MeshObject theOtherSide;
}
