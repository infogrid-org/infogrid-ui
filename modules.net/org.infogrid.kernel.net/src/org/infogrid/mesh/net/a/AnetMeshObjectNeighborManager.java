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

package org.infogrid.mesh.net.a;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.a.AMeshObject;
import org.infogrid.mesh.a.AMeshObjectNeighborManager;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.util.ArrayHelper;

/**
 * Refines AMeshObjectNeighborManager for AnetMeshObjects.
 */
public class AnetMeshObjectNeighborManager
    extends
        AMeshObjectNeighborManager
{
    /**
     * Obtain the set of identifiers of neighbor MeshObjects.
     *
     * @param subject the MeshObject in question
     * @return the set of identifiers of neighbor MeshObjects
     */
    @Override
    public NetMeshObjectIdentifier [] getNeighborIdentifiers(
            AMeshObject subject )
    {
        NetMeshObjectIdentifier [] ret = (NetMeshObjectIdentifier []) super.getNeighborIdentifiers( subject );
        return ret;
    }

    /**
     * Overridable method to create a single-element MeshObjectIdentifier array.
     *
     * @param oneElement the single element
     * @return the created array
     */
    @Override
    protected NetMeshObjectIdentifier [] makeMeshObjectIdentifiers(
            MeshObjectIdentifier oneElement )
    {
        return new NetMeshObjectIdentifier[] { (NetMeshObjectIdentifier) oneElement };
    }

    /**
     * Overridable method to create a MeshObjectIdentifier array as a
     * concatenation between an existing array and a new element.
     *
     * @param existing the existing array
     * @param toAdd the single element to add
     * @return the created array
     */
    @Override
    protected NetMeshObjectIdentifier [] makeMeshObjectIdentifiers(
            MeshObjectIdentifier [] existing,
            MeshObjectIdentifier    toAdd )
    {
        return ArrayHelper.append(
                (NetMeshObjectIdentifier []) existing,
                (NetMeshObjectIdentifier) toAdd,
                NetMeshObjectIdentifier.class );
    }

    /**
     * Remove an element from an array of MeshObjectIdentifier.
     * This may be overridden by subclasses.
     *
     * @param content the array
     * @param indexToRemove index of the object to remove
     * @return the content without to the object to be removed
     */
    @Override
    protected NetMeshObjectIdentifier [] removeMeshObjectIdentifier(
            MeshObjectIdentifier [] content,
            int                     indexToRemove )
    {
        NetMeshObjectIdentifier [] ret = ArrayHelper.remove( (NetMeshObjectIdentifier []) content, indexToRemove, NetMeshObjectIdentifier.class );
        return ret;
    }

    /**
     * Singleton instance of this class.
     */
    public static final AnetMeshObjectNeighborManager SINGLETON_NET
            = new AnetMeshObjectNeighborManager();
}
