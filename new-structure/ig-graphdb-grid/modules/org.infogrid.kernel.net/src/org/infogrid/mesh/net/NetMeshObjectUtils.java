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

package org.infogrid.mesh.net;

/**
 * Collection of utility methods for NetMeshObjects.
 */
public abstract class NetMeshObjectUtils
{
    /**
     * Private constructor, this class cannot be instantiated.
     */
    private NetMeshObjectUtils()
    {
        // noop
    }

    /**
     * Construct an array of NetMeshObjectIdentifiers from an array of NetMeshObjects.
     *
     * @param objs the NetMeshObjects
     * @return the NetMeshObjectIdentifiers of the NetMeshObjects
     */
    public static NetMeshObjectIdentifier [] netMeshObjectIdentifiers(
            NetMeshObject [] objs )
    {
        if( objs == null ) {
            return null;
        }
        NetMeshObjectIdentifier [] ret = new NetMeshObjectIdentifier[ objs.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = objs[i].getIdentifier();
        }
        return ret;
    }
}
