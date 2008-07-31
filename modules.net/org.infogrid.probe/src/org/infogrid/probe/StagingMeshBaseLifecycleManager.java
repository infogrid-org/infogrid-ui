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

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;

import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.transaction.TransactionException;

import org.infogrid.model.primitives.EntityType;

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
    /**
     * Obtain the MeshBase that this MeshBaseLifecycleManager works on.
     * 
     * @return the MeshBase that this MMeshBaseLifecycleManagerworks on
     */
    public abstract NetMeshBase getMeshBase();

    /**
     * <p>Create a ForwardReference without a type.</p>
     *
     * @param meshObjectLocation identifies the data source where the MeshObject can be found
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshBaseIdentifier meshObjectLocation )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create a ForwardReference with a type. This type may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     *
     * @param meshObjectLocation identifies the data source where the MeshObject can be found
     * @param type the EntityType with which the MeshObject will be blessed
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshBaseIdentifier meshObjectLocation,
            EntityType            type )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create a ForwardReference with zero or more types. These types may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     *
     * @param meshObjectLocation identifies the data source where the MeshObject can be found
     * @param types the EntityTypes with which the MeshObject will be blessed
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshBaseIdentifier meshObjectLocation,
            EntityType []         types )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create a ForwardReference without a type.</p>
     * 
     * @param meshObjectLocation identifies the data source where the MeshObject can be found
     * @param identifier the Identifier of the MeshObject into which this ForwardReference resolves.
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshBaseIdentifier   meshObjectLocation,
            NetMeshObjectIdentifier identifier )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create a ForwardReference with a type. This type may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     * 
     * @param meshObjectLocation identifies the data source where the MeshObject can be found
     * @param identifier the Identifier of the to-be-created MeshObject.
     * @param type the EntityType with which the MeshObject will be blessed
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshBaseIdentifier   meshObjectLocation,
            NetMeshObjectIdentifier identifier,
            EntityType              type )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create a ForwardReference with zero or more types. Each type may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     * 
     * @param meshObjectLocation identifies the data source where the MeshObject can be found
     * @param identifier the Identifier of the to-be-created MeshObject.
     * @param types the EntityTypes with which the MeshObject will be blessed
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshBaseIdentifier   meshObjectLocation,
            NetMeshObjectIdentifier identifier,
            EntityType []           types )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create a ForwardReference without a type.</p>
     * 
     * @param pathToObject specifies where and how the MeshObject can be found
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshObjectAccessSpecification pathToObject )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create a ForwardReference with a type. This type may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     * 
     * @param pathToObject specifies where and how the MeshObject can be found
     * @param type the EntityType with which the MeshObject will be blessed
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshObjectAccessSpecification pathToObject,
            EntityType                       type )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create a ForwardReference with zero or more types. Each type may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     * 
     * @param pathToObject specifies where and how the MeshObject can be found
     * @param types the EntityTypes with which the MeshObject will be blessed
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshObjectAccessSpecification pathToObject,
            EntityType []                    types )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;
}
