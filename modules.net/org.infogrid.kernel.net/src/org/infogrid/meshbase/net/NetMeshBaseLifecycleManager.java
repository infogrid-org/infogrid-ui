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

package org.infogrid.meshbase.net;

import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObjectFactory;
import org.infogrid.mesh.security.MustNotDeleteHomeObjectException;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.EntityType;

/**
 * This MeshBaseLifecycleManager, appropriate for a NetMeshBase, specifies
 * additional methods that only apply in a networked environment.
 */
public interface NetMeshBaseLifecycleManager
        extends
            MeshBaseLifecycleManager,
            ParserFriendlyExternalizedNetMeshObjectFactory
{
    /**
     * <p>Obtain the NetMeshBase that this NetMeshBaseLifecycleManager works on.</p>
     * <p>We list this here again because this type returns a more concrete type than our supertype does.</p>
     * 
     * @return the NetMeshBase that this NetMeshBaseLifecycleManager works on
     */
    public abstract NetMeshBase getMeshBase();

    /**
     * <p>Create a new NetMeshObject without a type
     * and an automatically created NetMeshObjectIdentifier.
     * This call is a "semantic create" which means that a new, semantically distinct object
     * is created.</p>
     *
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     *
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject()
        throws
            TransactionException,
            NotPermittedException;

    /**
     * <p>This is a convenience method to create a NetMeshObject with exactly one EntityType
     * and an automatically created NetMeshObjectIdentifier.
     * This call is a "semantic create" which means that a new, semantically distinct object
     * is created.</p>
     *
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     *
     * @param type the EntityType with which the NetMeshObject will be blessed
     * @return the created NetMeshObject
     * @throws IsAbstractException thrown if the EntityType is abstract and cannot be instantiated
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject(
            EntityType type )
        throws
            IsAbstractException,
            TransactionException,
            NotPermittedException;

    /**
     * <p>This is a convenience method to create a NetMeshObject with zero or more EntityTypes
     * and an automatically created NetMeshObjectIdentifier.
     * This call is a "semantic create" which means that a new, semantically distinct object
     * is created.</p>
     *
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     *
     * @param types the EntityTypes with which the NetMeshObject will be blessed
     * @return the created NetMeshObject
     * @throws IsAbstractException thrown if one or more of the EntityTypes are abstract and cannot be instantiated
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject(
            EntityType [] types )
        throws
            IsAbstractException,
            TransactionException,
            NotPermittedException;

    /**
     * <p>Create a new NetMeshObject without a type
     * and with a provided NetMeshObjectIdentifier.
     * This call is a "semantic create" which means that a new, semantically distinct object
     * is to be created.</p>
     * 
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     * 
     * @param identifier the identifier of the to-be-created NetMeshObject. If this is null,
     *                        automatically create a suitable NetMeshObjectIdentifier.
     * @return the created NetMeshObject
     * @throws MeshObjectIdentifierNotUniqueException a NetMeshObject exists already in this NetMeshBase with the specified identifier
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject(
            MeshObjectIdentifier identifier )
        throws
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException;

    /**
     * <p>This is a convenience method to create a NetMeshObject with exactly one EntityType
     * and a provided NetMeshObjectIdentifier.
     * This call is a "semantic create" which means that a new, semantically distinct object
     * is created.</p>
     * 
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     * 
     * @param identifier the identifier of the to-be-created NetMeshObject. If this is null,
     *                        automatically create a suitable NetMeshObjectIdentifier.
     * @param type the EntityType with which the NetMeshObject will be blessed
     * @return the created NetMeshObject
     * @throws IsAbstractException thrown if the EntityType is abstract and cannot be instantiated
     * @throws MeshObjectIdentifierNotUniqueException a NetMeshObject exists already in this NetMeshBase with the specified identifier
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject(
            MeshObjectIdentifier identifier,
            EntityType           type )
        throws
            IsAbstractException,
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException;

    /**
     * <p>This is a convenience method to create a NetMeshObject with zero or more EntityTypes
     * and a provided NetMeshObjectIdentifier.
     * This call is a "semantic create" which means that a new, semantically distinct object
     * is created.</p>
     * 
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     * 
     * @param identifier the identifier of the to-be-created NetMeshObject. If this is null,
     *                        automatically create a suitable NetMeshObjectIdentifier.
     * @param types the EntityTypes with which the NetMeshObject will be blessed
     * @return the created NetMeshObject
     * @throws IsAbstractException thrown if one or more of the EntityTypes are abstract and cannot be instantiated
     * @throws MeshObjectIdentifierNotUniqueException a NetMeshObject exists already in this NetMeshBase with the specified identifier
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject(
            MeshObjectIdentifier identifier,
            EntityType []        types )
        throws
            IsAbstractException,
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException;

    /**
     * <p>Create a new NetMeshObject without a type, but with provided time stamps
     * and a provided NetMeshObjectIdentifier.
     * This call is a "semantic create" which means that a new, semantically distinct object
     * is to be created.</p>
     * 
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     * 
     * @param identifier the identifier of the to-be-created NetMeshObject. This must not be null.
     * @param timeCreated the time when this NetMeshObject was semantically created, in System.currentTimeMillis() format
     * @param timeUpdated the time when this NetMeshObject was last updated, in System.currentTimeMillis() format
     * @param timeRead the time when this NetMeshObject was last read, in System.currentTimeMillis() format
     * @param timeExpires the time this NetMeshObject will expire, in System.currentTimeMillis() format
     * @return the created NetMeshObject
     * @throws MeshObjectIdentifierNotUniqueException a NetMeshObject exists already in this NEtMeshBase with the specified identifier
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject(
            MeshObjectIdentifier identifier,
            long                 timeCreated,
            long                 timeUpdated,
            long                 timeRead,
            long                 timeExpires )
        throws
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException;

    /**
     * <p>This is a convenience method to create a NetMeshObject with exactly one EntityType,
     * with provided time stamps
     * and a provided NetMeshObjectIdentifier.
     * This call is a "semantic create" which means that a new, semantically distinct object
     * is created.</p>
     * 
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     * 
     * @param identifier the identifier of the to-be-created NetMeshObject. If this is null,
     *                        automatically create a suitable NetMeshObjectIdentifier.
     * @param type the EntityType with which the NetMeshObject will be blessed
     * @param timeCreated the time when this NetMeshObject was semantically created, in System.currentTimeMillis() format
     * @param timeUpdated the time when this NetMeshObject was last updated, in System.currentTimeMillis() format
     * @param timeRead the time when this NetMeshObject was last read, in System.currentTimeMillis() format
     * @param timeExpires the time this NetMeshObject will expire, in System.currentTimeMillis() format
     * @return the created NetMeshObject
     * @throws IsAbstractException thrown if the EntityType is abstract and cannot be instantiated
     * @throws MeshObjectIdentifierNotUniqueException a NetMeshObject exists already in this NetMeshBase with the specified identifier
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject(
            MeshObjectIdentifier identifier,
            EntityType           type,
            long                 timeCreated,
            long                 timeUpdated,
            long                 timeRead,
            long                 timeExpires )
        throws
            IsAbstractException,
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException;

    /**
     * <p>This is a convenience method to create a NetMeshObject with zero or more EntityTypes,
     * with provided time stamps
     * and a provided NetMeshObjectIdentifier.
     * This call is a "semantic create" which means that a new, semantically distinct object
     * is created.</p>
     * 
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     * 
     * @param identifier the identifier of the to-be-created NetMeshObject. If this is null,
     *                        automatically create a suitable NetMeshObjectIdentifier.
     * @param types the EntityTypes with which the NetMeshObject will be blessed
     * @param timeCreated the time when this NetMeshObject was semantically created, in System.currentTimeMillis() format
     * @param timeUpdated the time when this NetMeshObject was last updated, in System.currentTimeMillis() format
     * @param timeRead the time when this NetMeshObject was last read, in System.currentTimeMillis() format
     * @param timeExpires the time this NetMeshObject will expire, in System.currentTimeMillis() format
     * @return the created NetMeshObject
     * @throws IsAbstractException thrown if one or more of the EntityTypes are abstract and cannot be instantiated
     * @throws MeshObjectIdentifierNotUniqueException a NetMeshObject exists already in this NetMeshBase with the specified identifier
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject(
            MeshObjectIdentifier identifier,
            EntityType []        types,
            long                 timeCreated,
            long                 timeUpdated,
            long                 timeRead,
            long                 timeExpires )
        throws
            IsAbstractException,
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException;

    /**
     * <p>Convenience method to create a new replica of an existing NetMeshObject with zero or more types,
     * with the provided time stamps, the provided NetMeshObjectIdentifier, flags whether to give up the
     * lock or home replica status should this replica ever acquire either, and the Proxy in which both
     * homeReplica and lock are to be found. 
     * This call creates a replica of an existing MeshObject, so it is not a "semantic create".</p>
     * 
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     * 
     * @param identifier the identifier of the to-be-created replica. This must not be null.
     * @param types the EntityTypes with which the NetMeshObject will be blessed
     * @param timeCreated the time when this NetMeshObject was semantically created, in System.currentTimeMillis() format
     * @param timeUpdated the time when this NetMeshObject was last updated, in System.currentTimeMillis() format
     * @param timeRead the time when this NetMeshObject was last read, in System.currentTimeMillis() format
     * @param timeExpires the time this NetMeshObject will expire, in System.currentTimeMillis() format
     * @param giveUpHomeReplica if true, this replica will give up home replica status when asked should it ever acquire it
     * @param giveUpLock if true, this replica will give up the lock when asked should it ever acquire it
     * @param proxyTowardsHomeAndLock the Proxy in whose direction the home replica and the updateable replica can be found
     * @return the created replica
     * @throws IsAbstractException thrown if one or more of the EntityTypes are abstract and cannot be instantiated
     * @throws MeshObjectIdentifierNotUniqueException a NetMeshObject exists already in this NetMeshBase with the specified identifier
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject(
            NetMeshObjectIdentifier identifier,
            EntityType []           types,
            long                    timeCreated,
            long                    timeUpdated,
            long                    timeRead,
            long                    timeExpires,
            boolean                 giveUpHomeReplica,
            boolean                 giveUpLock,
            Proxy                   proxyTowardsHomeAndLock )
        throws
            IsAbstractException,
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException;

    /**
     * <p>Create a new replica of an existing NetMeshObject with zero or more types,
     * with the provided time stamps, the provided NetMeshObjectIdentifier, flags whether to give up the
     * lock or home replica status should this replica ever acquire either, and all Proxies and their roles.
     * This call creates a replica of an existing MeshObject, so it is not a "semantic create".</p>
     * 
     * <p>Before this operation can be successfully invoked, a Transaction must be active
     * on this Thread.>/p>
     * 
     * @param identifier the identifier of the to-be-created replica. This must not be null.
     * @param types the EntityTypes with which the NetMeshObject will be blessed
     * @param timeCreated the time when this NetMeshObject was semantically created, in System.currentTimeMillis() format
     * @param timeUpdated the time when this NetMeshObject was last updated, in System.currentTimeMillis() format
     * @param timeRead the time when this NetMeshObject was last read, in System.currentTimeMillis() format
     * @param timeExpires the time this NetMeshObject will expire, in System.currentTimeMillis() format
     * @param giveUpHomeReplica if true, this replica will give up home replica status when asked should it ever acquire it
     * @param giveUpLock if true, this replica will give up the lock when asked should it ever acquire it
     * @param proxies all Proxies that are affected by this replica
     * @param homeProxyIndex the index, into proxies, of the Proxy in whose direction the home replica can be found. This
     *            replica is the home replica if this value is -1.
     * @param proxyTowardsLockIndex the index, into proxies, of the Proxy in whose direction the lock can be found. This
     *            replica is the replica with the lock if this value is -1.
     * @return the created replica
     * @throws IsAbstractException thrown if one or more of the EntityTypes are abstract and cannot be instantiated
     * @throws MeshObjectIdentifierNotUniqueException a NetMeshObject exists already in this NetMeshBase with the specified identifier
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject createMeshObject(
            NetMeshObjectIdentifier identifier,
            EntityType []           types,
            long                    timeCreated,
            long                    timeUpdated,
            long                    timeRead,
            long                    timeExpires,
            boolean                 giveUpHomeReplica,
            boolean                 giveUpLock,
            Proxy []                proxies,
            int                     homeProxyIndex,
            int                     proxyTowardsLockIndex )
        throws
            IsAbstractException,
            TransactionException,
            MeshObjectIdentifierNotUniqueException,
            NotPermittedException;

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

    /**
     * <p>Create a ForwardReference without a type.</p>
     *
     * @param pathToObject specifies where and how the MeshObject can be found
     * @param timeCreated the time when this NetMeshObject was semantically created, in System.currentTimeMillis() format
     * @param timeUpdated the time when this NetMeshObject was last updated, in System.currentTimeMillis() format
     * @param timeRead the time when this NetMeshObject was last read, in System.currentTimeMillis() format
     * @param timeExpires the time this NetMeshObject will expire, in System.currentTimeMillis() format
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshObjectAccessSpecification pathToObject,
            long                             timeCreated,
            long                             timeUpdated,
            long                             timeRead,
            long                             timeExpires )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create a ForwardReference with a type. This type may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     *
     * @param pathToObject specifies where and how the MeshObject can be found
     * @param type the EntityType with which the MeshObject will be blessed
     * @param timeCreated the time when this NetMeshObject was semantically created, in System.currentTimeMillis() format
     * @param timeUpdated the time when this NetMeshObject was last updated, in System.currentTimeMillis() format
     * @param timeRead the time when this NetMeshObject was last read, in System.currentTimeMillis() format
     * @param timeExpires the time this NetMeshObject will expire, in System.currentTimeMillis() format
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshObjectAccessSpecification pathToObject,
            EntityType                       type,
            long                             timeCreated,
            long                             timeUpdated,
            long                             timeRead,
            long                             timeExpires )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create a ForwardReference with zero or more types. Each type may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     *
     * @param pathToObject specifies where and how the MeshObject can be found
     * @param types the EntityTypes with which the MeshObject will be blessed
     * @param timeCreated the time when this NetMeshObject was semantically created, in System.currentTimeMillis() format
     * @param timeUpdated the time when this NetMeshObject was last updated, in System.currentTimeMillis() format
     * @param timeRead the time when this NetMeshObject was last read, in System.currentTimeMillis() format
     * @param timeExpires the time this NetMeshObject will expire, in System.currentTimeMillis() format
     * @return the created NetMeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject createForwardReference(
            NetMeshObjectAccessSpecification pathToObject,
            EntityType []                    types,
            long                             timeCreated,
            long                             timeUpdated,
            long                             timeRead,
            long                             timeExpires )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create several ForwardReferences without a type.</p>
     *
     * @param pathToObjects specifies where and how the MeshObjects can be found
     * @return the created NetMeshObjects
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject [] createForwardReferences(
            NetMeshObjectAccessSpecification [] pathToObjects )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create several ForwardReferences with a type. This type may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     *
     * @param pathToObjects specifies where and how the MeshObjects can be found
     * @param types the EntityTypes with which the MeshObjects will be blessed, in same sequence as pathToObjects
     * @return the created NetMeshObjects
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject [] createForwardReferences(
            NetMeshObjectAccessSpecification [] pathToObjects,
            EntityType []                       types )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * <p>Create several ForwardReferences with zero or more types. Each type may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     *
     * @param pathToObjects specifies where and how the MeshObjects can be found
     * @param types the EntityTypes with which the MeshObjects will be blessed, in same sequence as pathToObjects
     * @return the created NetMeshObjects
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if the specified NetMeshBaseIdentifier was taken already
     */
    public abstract NetMeshObject [] createForwardReferences(
            NetMeshObjectAccessSpecification [] pathToObjects,
            EntityType [][]                     types )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException;

    /**
     * Purge a replica. This method can be applied on all NetMeshObjects in this NetMeshBase,
     * except the home object of this NetMeshBase.
     *
     * @param replica the non-home replica
     * @throws TransactionException thrown if invoked outside proper Transaction boundaries
     * @throws MustNotDeleteHomeObjectException thrown if applied to the home object of this NetMeshBase
     */
    public abstract void purgeReplica(
            NetMeshObject replica )
        throws
            TransactionException,
            MustNotDeleteHomeObjectException;
    
    /**
     * Purge several replicas. This method can be applied on all NetMeshObjects in this NetMeshBase,
     * except the home object of this NetMeshBase.
     *
     * @param replicas the non-home replicas
     * @throws TransactionException thrown if invoked outside proper Transaction boundaries
     * @throws MustNotDeleteHomeObjectException thrown if applied to the home object of this NetMeshBase
     */
    public abstract void purgeReplicas(
            NetMeshObject [] replicas )
        throws
            TransactionException,
            MustNotDeleteHomeObjectException;

    /**
     * Instantiate a replica NetMeshObject in this NetMeshBase. Proxy information is provided
     * as separate parameters, as a ProxyPolicy may choose to use different Proxies than offered
     * by the NetMeshObject replica being replicated.
     *
     * @param externalized the external form of the NetMeshObject that is being replicated locally
     * @param proxies the Proxies to use
     * @param proxyTowardsHomeIndex the index, in the proxies, that holds the Proxy towards the home replica. -1 if none.
     * @param proxyTowardsLockIndex the index, in the proxies, that holds the Proxy towards the replica with the lock. -1 if none.
     * @return the created or resynchronized NetMeshObject
     * @throws MeshObjectIdentifierNotUniqueException thrown if a NetMeshObject with this identifier exists locally already
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     */
    public NetMeshObject rippleCreate(
            ExternalizedNetMeshObject externalized,
            Proxy []                  proxies,
            int                       proxyTowardsHomeIndex,
            int                       proxyTowardsLockIndex )
        throws
            MeshObjectIdentifierNotUniqueException,
            TransactionException;

    /**
     * Resynchronize an existing replica NetMeshObject in this NetMeshBase with the provided information.
     * Proxy information is provided
     * as separate parameters, as a ProxyPolicy may choose to use different Proxies than offered
     * by the NetMeshObject replica being replicated.
     * 
     * @param externalized the external form of the NetMeshObject that is being replicated locally
     * @param proxies the Proxies to use
     * @param proxyTowardsHomeIndex the index, in the proxies, that holds the Proxy towards the home replica. -1 if none.
     * @param proxyTowardsLockIndex the index, in the proxies, that holds the Proxy towards the replica with the lock. -1 if none.
     * @return the created or resynchronized NetMeshObject
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the called did not have sufficient privileges for this operation
     */
    public NetMeshObject rippleResynchronize(
            ExternalizedNetMeshObject externalized,
            Proxy []                  proxies,
            int                       proxyTowardsHomeIndex,
            int                       proxyTowardsLockIndex )
        throws
            TransactionException,
            NotPermittedException;

    /**
     * Delete a replica NetMeshObject in this NetMeshBase, thereby removing a
     * branch in the replication graph.
     * 
     * @param identifier the identifier of the NetMeshObject whose replica is to be deleted
     * @param proxyIdentifier the NetMeshBaseIdentifier that selects the Proxy that conveyed this command
     * @return the deleted NetMeshObject
     * @param timeEventOccurred the time at which the delete command occurred
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject rippleDelete(
            NetMeshObjectIdentifier identifier,
            NetMeshBaseIdentifier   proxyIdentifier,
            long                    timeEventOccurred )
        throws
            TransactionException,
            NotPermittedException;
}
