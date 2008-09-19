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

package org.infogrid.meshbase.net.a;

import java.util.HashMap;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.mesh.RoleTypeRequiresEntityTypeException;
import org.infogrid.mesh.externalized.ExternalizedMeshObject;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.a.AnetMeshObject;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObject;
import org.infogrid.mesh.net.security.CannotCreateNonLocalMeshObjectException;
import org.infogrid.mesh.net.security.CannotObtainLockException;
import org.infogrid.mesh.security.MustNotDeleteHomeObjectException;
import org.infogrid.meshbase.a.AMeshBase;
import org.infogrid.meshbase.a.AMeshBaseLifecycleManager;
import org.infogrid.meshbase.a.AMeshObjectEquivalenceSetComparator;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.transaction.NetMeshObjectCreatedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectDeletedEvent;
import org.infogrid.meshbase.net.transaction.ReplicaCreatedEvent;
import org.infogrid.meshbase.net.transaction.ReplicaPurgedEvent;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.FactoryException;
import org.infogrid.util.RemoteQueryTimeoutException;
import org.infogrid.util.logging.Log;

/**
 * A NetMeshBaseLifecycleManager for the AnetMeshObject implementation.
 */
public class AnetMeshBaseLifecycleManager
        extends
            AMeshBaseLifecycleManager
        implements
            NetMeshBaseLifecycleManager
{
    private static final Log log = Log.getLogInstance( AnetMeshBaseLifecycleManager.class ); // our own, private logger

    /**
     * Factory method. The application developer should not call this or a subclass constructor; use
     * MeshBase.getMeshObjectLifecycleManager() instead.
     * 
     * @return the created AnetMeshBaseLifecycleManager
     */
    public static AnetMeshBaseLifecycleManager create()
    {
        return new AnetMeshBaseLifecycleManager();
    }
    
    /**
     * Constructor. The application developer should not call this or a subclass constructor; use
     * MeshBase.getMeshObjectLifecycleManager() instead.
     */
    protected AnetMeshBaseLifecycleManager()
    {
        super();
    }

    /**
     * <p>Obtain the NetMeshBase that this NetMeshBaseLifecycleManager works on.</p>
     * <p>We list this here again because this type returns a more concrete type than our supertype does.</p>
     * 
     * @return the NetMeshBase that this NetMeshBaseLifecycleManager works on
     */
    @Override
    public NetMeshBase getMeshBase()
    {
        return (NetMeshBase) theMeshBase;
    }

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
    @Override
    public AnetMeshObject createMeshObject()
        throws
            TransactionException,
            NotPermittedException
    {
        MeshObject ret = super.createMeshObject();
        return (AnetMeshObject) ret;
    }

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
    @Override
    public AnetMeshObject createMeshObject(
            EntityType type )
        throws
            IsAbstractException,
            TransactionException,
            NotPermittedException
    {
        MeshObject ret = super.createMeshObject( type );
        return (AnetMeshObject) ret;
    }

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
    @Override
    public AnetMeshObject createMeshObject(
            EntityType [] types )
        throws
            IsAbstractException,
            TransactionException,
            NotPermittedException
    {
        MeshObject ret = super.createMeshObject( types );
        return (AnetMeshObject) ret;
    }

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
    @Override
    public AnetMeshObject createMeshObject(
            MeshObjectIdentifier identifier )
        throws
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException
    {
        MeshObject ret = super.createMeshObject( identifier );
        return (AnetMeshObject) ret;
    }

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
    @Override
    public AnetMeshObject createMeshObject(
            MeshObjectIdentifier identifier,
            EntityType           type )
        throws
            IsAbstractException,
            TransactionException,
            NotPermittedException,
            MeshObjectIdentifierNotUniqueException
    {
        MeshObject ret = super.createMeshObject( identifier, type );
        return (AnetMeshObject) ret;
    }

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
    @Override
    public AnetMeshObject createMeshObject(
            MeshObjectIdentifier identifier,
            EntityType []        types )
        throws
            IsAbstractException,
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException
    {
        MeshObject ret = super.createMeshObject( identifier, types );
        return (AnetMeshObject) ret;
    }

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
    @Override
    public AnetMeshObject createMeshObject(
            MeshObjectIdentifier identifier,
            long                 timeCreated,
            long                 timeUpdated,
            long                 timeRead,
            long                 timeExpires )
        throws
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException
    {
        MeshObject ret = super.createMeshObject( identifier, timeCreated, timeUpdated, timeRead, timeExpires );
        return (AnetMeshObject) ret;
    }

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
    @Override
    public AnetMeshObject createMeshObject(
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
            NotPermittedException
    {
        MeshObject ret = super.createMeshObject( identifier, type, timeCreated, timeUpdated, timeRead, timeExpires );
        return (AnetMeshObject) ret;
    }

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
    @Override
    public AnetMeshObject createMeshObject(
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
            NotPermittedException
    {
        MeshObject ret = super.createMeshObject( identifier, types, timeCreated, timeUpdated, timeRead, timeExpires );
        return (AnetMeshObject) ret;
    }

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
    public AnetMeshObject createMeshObject(
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
            NotPermittedException
    {
        return createMeshObject(
                identifier,
                types,
                timeCreated,
                timeUpdated,
                timeRead,
                timeExpires,
                giveUpHomeReplica,
                giveUpLock,
                new Proxy[] { proxyTowardsHomeAndLock },
                0,
                0 );
    }
    
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
    public AnetMeshObject createMeshObject(
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
            NotPermittedException
    {
        checkPermittedCreate( identifier );

        long now = determineCreationTime();
        if( timeCreated < 0 ) {
            timeCreated = now;
        }
        if( timeUpdated < 0 ) {
            timeUpdated = now;
        }
        if( timeRead < 0 ) {
            timeRead = now;
        }
        // not timeExpires

        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;

        Transaction tx = realBase.checkTransaction();

        AnetMeshObject ret = instantiateMeshObjectImplementation(
                identifier,
                timeCreated,
                timeUpdated,
                timeRead,
                timeExpires,
                giveUpHomeReplica,
                giveUpLock,
                proxies,
                homeProxyIndex,
                proxyTowardsLockIndex );

        if( types != null && types.length > 0 ) {
            try {
                ret.bless( types );

            } catch( EntityBlessedAlreadyException ex ) {
                log.error( ex );
            }
        }

        Proxy                 incomingProxy           = realBase.determineIncomingProxy();
        NetMeshBaseIdentifier incomingProxyIdentifier = incomingProxy != null ? incomingProxy.getPartnerMeshBaseIdentifier() : null;

        putIntoMeshBase(
                ret,
                new NetMeshObjectCreatedEvent(
                        realBase,
                        realBase.getIdentifier(),
                        ret,
                        incomingProxyIdentifier ));

        assignOwner( ret );

        return ret;
    }

    /**
     * Check whether it is permitted to create a MeshObject with the specfied parameters.
     * 
     * @param identifier the identifier of the to-be-created MeshObject. This must not be null.
     * @throws MeshObjectIdentifierNotUniqueException a MeshObject exists already in this MeshBase with the specified Identifier
     * @throws NotPermittedException thrown if the combination of arguments was not permitted
     * @throws IllegalArgumentException thrown if an illegal argument was specified
     */
    @Override
    protected void checkPermittedCreate(
            MeshObjectIdentifier identifier )
        throws
            MeshObjectIdentifierNotUniqueException,
            NotPermittedException,
            IllegalArgumentException
    {
        NetMeshObjectIdentifier realIdentifier = (NetMeshObjectIdentifier) identifier;

        super.checkPermittedCreate( identifier );

        NetMeshBaseIdentifier baseIdentifier = realIdentifier.getNetMeshBaseIdentifier();
        if( !theMeshBase.getIdentifier().equals( baseIdentifier ) ) {
            throw new CannotCreateNonLocalMeshObjectException( (NetMeshBase) theMeshBase, realIdentifier );
        }
    }

    /**
     * <p>Semantically delete several NetMeshObjects at the same time.</p>
     * 
     * <p>This call is a "semantic delete", which means that an existing
     * NetMeshObjects will go away in all its replicas. Due to time lag, the NetMeshObject
     * may still exist in certain replicas in other places for a while, but
     * the request to deleteMeshObjects all objects is in the queue and will get there
     * eventually.</p>
     * 
     * <p>This call either succeeds or fails in total: if one or more of the specified NetMeshObjects cannot be
     *    deleted for some reason, none of the other NetMeshObjects will be deleted either.</p>
     * 
     * @param theObjects the MeshObjects to be semantically deleted
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    @Override
    public synchronized void deleteMeshObjects(
            MeshObject [] theObjects )
        throws
            TransactionException,
            NotPermittedException
    {
        AMeshBase realBase = (AMeshBase) theMeshBase;
        long      now      = System.currentTimeMillis();
        
        Transaction tx = realBase.checkTransaction();

        MeshObject home = realBase.getHomeObject();

        for( int i=0 ; i<theObjects.length ; ++i ) {
            if( theObjects[i] == null ) {
                throw new NullPointerException( "MeshObject at index " + i + " is null" );
            }
            if( theObjects[i].getMeshBase() != realBase ) {
                throw new IllegalArgumentException( "cannot delete MeshObjects in a different MeshBases" );
            }
            if( theObjects[i] == home ) {
                throw new MustNotDeleteHomeObjectException( home );
            }
        }
        for( int i=0 ; i<theObjects.length ; ++i ) {
            try {
                ((AnetMeshObject)theObjects[i]).tryToObtainLock();

            } catch( RemoteQueryTimeoutException ex ) {
                log.warn( ex );
                throw new CannotObtainLockException( (NetMeshObject) theObjects[i], ex );
            }

            ((AnetMeshObject)theObjects[i]).checkPermittedDelete(); // this may throw NotPermittedException
        }
        for( int i=0 ; i<theObjects.length ; ++i ) {
            AnetMeshObject         current              = (AnetMeshObject) theObjects[i];
            MeshObjectIdentifier   currentCanonicalName = current.getIdentifier();
            ExternalizedMeshObject externalized         = current.asExternalized( true );
            
            current.delete();
            removeFromMeshBase(
                    current.getIdentifier(),
                    createDeletedEvent( current, currentCanonicalName, externalized, now ));
        }
    }

    /**
     * Helper method to instantiate the right subclass of MeshObject.
     * 
     * @param identifier the identifier of the to-be-created NetMeshObject. This must not be null.
     * @param timeCreated the time when this NetMeshObject was semantically created, in System.currentTimeMillis() format
     * @param timeUpdated the time when this NetMeshObject was last updated, in System.currentTimeMillis() format
     * @param timeRead the time when this NetMeshObject was last read, in System.currentTimeMillis() format
     * @param timeExpires the time this NetMeshObject will expire, in System.currentTimeMillis() format
     * @return the created NetMeshObject
     */
    @Override
    protected AnetMeshObject instantiateMeshObjectImplementation(
            MeshObjectIdentifier identifier,
            long                 timeCreated,
            long                 timeUpdated,
            long                 timeRead,
            long                 timeExpires )
    {
        AnetMeshObject ret = new AnetMeshObject(
                (NetMeshObjectIdentifier) identifier,
                (AnetMeshBase) theMeshBase,
                timeCreated,
                timeUpdated,
                timeRead,
                timeExpires,
                ((AnetMeshBase) theMeshBase).getDefaultWillGiveUpHomeReplica(),
                ((AnetMeshBase) theMeshBase).getDefaultWillGiveUpLock(),
                null,
                AnetMeshObject.HERE_CONSTANT,
                AnetMeshObject.HERE_CONSTANT );
        
        return ret;
    }
    
    /**
     * Helper method to instantiate the right subclass of MeshObject.
     * 
     * @param identifier the identifier of the to-be-created NetMeshObject. This must not be null.
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
     * @return the created NetMeshObject
     */
    protected AnetMeshObject instantiateMeshObjectImplementation(
            NetMeshObjectIdentifier identifier,
            long                    timeCreated,
            long                    timeUpdated,
            long                    timeRead,
            long                    timeExpires,
            boolean                 giveUpHomeReplica,
            boolean                 giveUpLock,
            Proxy []                proxies,
            int                     homeProxyIndex,
            int                     proxyTowardsLockIndex )
    {
        AnetMeshObject ret = new AnetMeshObject(
                identifier,
                (AnetMeshBase) theMeshBase,
                timeCreated,
                timeUpdated,
                timeRead,
                timeExpires,
                giveUpHomeReplica,
                giveUpLock,
                proxies,
                homeProxyIndex,
                proxyTowardsLockIndex );
        
        return ret;
    }

    /**
     * Factored out method to instantiate a recreated NetMeshObject.
     * 
     * @param identifier the identifier of the MeshObject
     * @param timeCreated the time it was created
     * @param timeUpdated the time it was last udpated
     * @param timeRead the time it was last read
     * @param timeExpires the time it will expire
     * @param properties the properties of the MeshObject
     * @param types the EntityTypes of the MeshObject
     * @param equivalents either an array of length 2, or null. If given, contains the left and right equivalence pointers.
     * @param otherSides the identifiers of the MeshObject's neighbors, if any
     * @param roleTypes the RoleTypes in which this MeshObject participates with its neighbors
     * @param theObjectBeingParsed the externalized representation of the MeshObject
     * @return the recreated AMeshObject
     */
    @Override
    protected AnetMeshObject instantiateRecreatedMeshObject(
            MeshObjectIdentifier                identifier,
            long                                timeCreated,
            long                                timeUpdated,
            long                                timeRead,
            long                                timeExpires,
            HashMap<PropertyType,PropertyValue> properties,
            EntityType []                       types,
            MeshObjectIdentifier []             equivalents,
            MeshObjectIdentifier []             otherSides,
            RoleType [][]                       roleTypes,
            ExternalizedMeshObject              theObjectBeingParsed )
    {
        AnetMeshBase              realBase              = (AnetMeshBase)              theMeshBase;
        ExternalizedNetMeshObject realObjectBeingParsed = (ExternalizedNetMeshObject) theObjectBeingParsed;
        
//        if(    identifier instanceof NetMeshObjectIdentifier ) {
//            NetMeshObjectIdentifier realIdentifier = (NetMeshObjectIdentifier) identifier;
//
//            if( realBase.getIdentifier().toExternalForm().equals( realIdentifier.getPrefix() )) {
//                identifier = MeshObjectIdentifier.create( realIdentifier.getLocalId() );
//            }
//        }
        
        boolean giveUpHomeReplica = realObjectBeingParsed.getGiveUpHomeReplica();
        boolean giveUpLock        = realObjectBeingParsed.getGiveUpLock();

        NetMeshBaseIdentifier [] proxyNetworkIdentifiers = realObjectBeingParsed.getProxyIdentifiers();

        int proxyTowardsHomeIndex = -1;
        int proxyTowardsLockIndex = -1;
        
        Proxy [] proxies = new Proxy[ proxyNetworkIdentifiers.length ];

        for( int i=0 ; i<proxies.length ; ++i ) {
            if( proxyNetworkIdentifiers[i].equals( realObjectBeingParsed.getProxyTowardsHomeNetworkIdentifier() )) {
                proxyTowardsHomeIndex = i;
            }
            if( proxyNetworkIdentifiers[i].equals( realObjectBeingParsed.getProxyTowardsLockNetworkIdentifier() )) {
                proxyTowardsLockIndex = i;
            }

            try {
                proxies[i] = realBase.obtainProxyFor( proxyNetworkIdentifiers[i], null ); // FIXME? What is the right CoherenceSpecification here?

            } catch( FactoryException ex ) {
                log.error( ex );
            }
        }
        
        AnetMeshObject ret = new AnetMeshObject(
                (NetMeshObjectIdentifier) identifier,
                realBase,
                timeCreated,
                timeUpdated,
                timeRead,
                timeExpires,
                properties,
                types,
                (NetMeshObjectIdentifier []) equivalents,
                (NetMeshObjectIdentifier []) otherSides,
                roleTypes,
                giveUpHomeReplica,
                giveUpLock,
                proxies,
                proxyTowardsHomeIndex,
                proxyTowardsLockIndex );

        return ret;
    }

    /**
     * Purge a replica. This method can be applied on all NetMeshObjects in this NetMeshBase,
     * except the home object of this NetMeshBase.
     *
     * @param replica the non-home replica
     * @throws TransactionException thrown if invoked outside proper Transaction boundaries
     * @throws MustNotDeleteHomeObjectException thrown if applied to the home object of this NetMeshBase
     */
    public void purgeReplica(
            NetMeshObject replica )
        throws
            TransactionException,
            MustNotDeleteHomeObjectException
    {
        purgeReplicas( new NetMeshObject[] { replica } );
    }
    
    /**
     * Purge several replicas. This method can be applied on all NetMeshObjects in this NetMeshBase,
     * except the home object of this NetMeshBase.
     *
     * @param replicas the non-home replicas
     * @throws TransactionException thrown if invoked outside proper Transaction boundaries
     * @throws MustNotDeleteHomeObjectException thrown if applied to the home object of this NetMeshBase
     */
    public void purgeReplicas(
            NetMeshObject [] replicas )
        throws
            TransactionException,
            MustNotDeleteHomeObjectException
    {
        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;

        Transaction tx = realBase.checkTransaction();

        long time = System.currentTimeMillis();
        
        // we don't do that here because it's the slave replica
        // theObject.checkPermittedDelete(); // this may throw NotPermittedException

        Proxy                 incomingProxy           = realBase.determineIncomingProxy();
        NetMeshBaseIdentifier incomingProxyIdentifier = incomingProxy != null ? incomingProxy.getPartnerMeshBaseIdentifier() : null;

        for( NetMeshObject current : replicas ) {
            try {
                NetMeshObjectIdentifier identifier   = current.getIdentifier();
                ExternalizedMeshObject  externalized = current.asExternalized( true );

                ((AnetMeshObject) current).purge();
                
                removeFromMeshBase(
                        identifier,
                        new ReplicaPurgedEvent(
                                realBase,
                                realBase.getIdentifier(),
                                current,
                                identifier,
                                incomingProxyIdentifier,
                                externalized,
                                time ));

            } catch( NotPermittedException ex ) {
                log.error( ex );
            }
        }
    }

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
    public AnetMeshObject rippleDelete(
            NetMeshObjectIdentifier identifier,
            NetMeshBaseIdentifier   proxyIdentifier,
            long                    timeEventOccurred )
        throws
            TransactionException,
            NotPermittedException
    {
        AnetMeshBase   realBase  = (AnetMeshBase) theMeshBase;
        AnetMeshObject theObject = (AnetMeshObject) realBase.findMeshObjectByIdentifier( identifier );
        
        if( theObject != null ) {
            MeshObjectIdentifier   realIdentifier = theObject.getIdentifier();
            ExternalizedMeshObject externalized   = theObject.asExternalized( true );

            Transaction tx = realBase.checkTransaction();

            // we don't do that here because it's the slave replica
            // theObject.checkPermittedDelete(); // this may throw NotPermittedException

            Proxy                 incomingProxy           = realBase.determineIncomingProxy();
            NetMeshBaseIdentifier incomingProxyIdentifier = incomingProxy != null ? incomingProxy.getPartnerMeshBaseIdentifier() : null;

            theObject.delete();
            removeFromMeshBase(
                    theObject.getIdentifier(),
                    new NetMeshObjectDeletedEvent(
                            realBase,
                            realBase.getIdentifier(),
                            theObject,
                            realIdentifier,
                            incomingProxyIdentifier,
                            externalized,
                            timeEventOccurred ));
        }
        return theObject;
    }
    
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
            TransactionException
    {
        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;

        Transaction tx = realBase.checkTransaction();

        NetMeshObjectIdentifier identifier = externalized.getIdentifier();
        
        AnetMeshObject ret = findInStore( identifier );
        
        if( ret != null ) {
            throw new MeshObjectIdentifierNotUniqueException( ret );
        }
        
        long now = System.currentTimeMillis();

        NetMeshObjectIdentifier [] leftRight
                = (NetMeshObjectIdentifier []) AMeshObjectEquivalenceSetComparator.SINGLETON.findLeftAndRightEquivalents(
                        identifier,
                        externalized.getEquivalents() );
        
        HashMap<PropertyType,PropertyValue> properties = propertiesFromExternalizedMeshObject( externalized );        
        EntityType []                       types      = typesFromExternalizedMeshObject( externalized );
        NetMeshObjectIdentifier []          neighbors  = externalized.getNeighbors();
        RoleType [][]                       roleTypes  = roleTypesFromExternalizedMeshObject( externalized );
        
        ret = instantiateMeshObjectImplementation(
                identifier,
                externalized.getTimeCreated(),
                externalized.getTimeUpdated(),
                externalized.getTimeRead(),
                externalized.getTimeExpires(),
                externalized.getGiveUpHomeReplica(),
                externalized.getGiveUpLock(),
                proxies,
                proxyTowardsHomeIndex,
                proxyTowardsLockIndex );

        if( types != null && types.length > 0 ) {
            try {
                ret.rippleBless( types, externalized.getTimeUpdated() );

            } catch( IsAbstractException ex ) {
                log.error( ex );
            } catch( EntityBlessedAlreadyException ex ) {
                log.error( ex );
            } catch( NotPermittedException ex ) {
                log.error( ex );
            }
        }

        if( !properties.isEmpty() ) {
            try {
                ret.rippleSetPropertyValues( properties, externalized.getTimeUpdated() );

            } catch( IllegalPropertyTypeException ex ) {
                log.error( ex );
            } catch( IllegalPropertyValueException ex ) {
                log.error( ex );
            } catch( NotPermittedException ex ) {
                log.error( ex );
            }
        }
        
        for( int i=0 ; i<neighbors.length ; ++i ) {
            try {
                ret.rippleRelate( neighbors[i], externalized.getTimeUpdated() );
            } catch( RelatedAlreadyException ex ) {
                log.error( ex );
            }
            try {
                if( roleTypes[i] != null ) {
                    ret.rippleBless( roleTypes[i], neighbors[i], externalized.getTimeUpdated() );
                }
            } catch( NotRelatedException ex ) {
                log.error( ex );
            } catch( IsAbstractException ex ) {
                log.error( ex );
            } catch( RoleTypeBlessedAlreadyException ex ) {
                log.error( ex );
            } catch( EntityNotBlessedException ex ) {
                log.error( ex );
            } catch( NotPermittedException ex ) {
                log.error( ex );
            }
        }

        Proxy                 incomingProxy           = realBase.determineIncomingProxy();
        NetMeshBaseIdentifier incomingProxyIdentifier = incomingProxy != null ? incomingProxy.getPartnerMeshBaseIdentifier() : null;

        putIntoMeshBase(
                ret,
                new ReplicaCreatedEvent(
                        realBase,
                        realBase.getIdentifier(),
                        ret,
                        incomingProxyIdentifier,
                        now ));
        
        // don't assign owner, that comes via replication

        return ret;        
    }    

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
            NotPermittedException
    {
        NetMeshObjectIdentifier identifier = externalized.getIdentifier();
        AnetMeshObject          ret        = findInStore( identifier );

        if( ret == null ) {
            throw new IllegalStateException( "Should exist: " + identifier );
        }
        
        EntityType [] correctTypes = typesFromExternalizedMeshObject( externalized );
        EntityType [] currentTypes = ret.getTypes();

        ArrayHelper.Difference<EntityType> typeDifferences = ArrayHelper.determineDifference( currentTypes, correctTypes, false, EntityType.class );
        
        for( EntityType toAdd : typeDifferences.getAdditions() ) {
            try {
                ret.rippleBless( new EntityType[] { toAdd }, externalized.getTimeUpdated() );
            } catch( IsAbstractException ex ) {
                // a real error
                log.error( ex );
            } catch( EntityBlessedAlreadyException ex ) {
                // this can happen
                if( log.isDebugEnabled() ) {
                    log.debug( ex );
                }
            }
        }
        for( EntityType toRemove : typeDifferences.getRemovals() ) {
            try {
                ret.rippleUnbless( new EntityType[] { toRemove }, externalized.getTimeUpdated() );
            } catch( RoleTypeRequiresEntityTypeException ex ) {
                // This might be a FIXME depending on what we decide to do about neighbors and roles in this method
                log.error( ex );
            } catch( EntityNotBlessedException ex ) {
                // this can happen, e.g. when downcasting
                if( log.isDebugEnabled() ) {
                    log.debug( ex );
                }
            }
        }
        
        // FIXME: equivalents

        // FIXME: neighbors
        
        // FIXME: roles
        
        // properties
        HashMap<PropertyType,PropertyValue> correctProperties = propertiesFromExternalizedMeshObject( externalized );
        PropertyType []  propertyTypes  = new PropertyType[ correctProperties.size() ];
        PropertyValue [] propertyValues = new PropertyValue[ propertyTypes.length ];
        int i = 0;
        for( PropertyType current : correctProperties.keySet() ) {
            propertyTypes[i]  = current;
            propertyValues[i] = correctProperties.get( current );
            ++i;
        }
        
        try {
            ret.internalSetPropertyValues( propertyTypes, propertyValues, false, externalized.getTimeUpdated() );
        } catch( IllegalPropertyTypeException ex ) {
            log.error( ex );
        } catch( IllegalPropertyValueException ex ) {
            log.error( ex );
        }
        
        // should the next two lines be here? FIXME?
        ret.setWillGiveUpHomeReplica( externalized.getGiveUpHomeReplica() );
        ret.setWillGiveUpLock( externalized.getGiveUpLock() );
       
        ret.proxyOnlyResynchronizeReplica(
                externalized.getTimeCreated(),
                externalized.getTimeUpdated(),
                externalized.getTimeRead(),
                externalized.getTimeExpires(),
                proxies,
                proxyTowardsHomeIndex,
                proxyTowardsLockIndex );
        
        return ret;
    }

    /**
     * Internal helper to extract Properties from an ExternalizedNetMeshObject.
     * 
     * @param externalized the ExternalizedNetMeshObject
     * @return the Properties, as PropertyType/PropertyValue pairs
     */
    protected HashMap<PropertyType,PropertyValue> propertiesFromExternalizedMeshObject(
            ExternalizedNetMeshObject externalized )
    {
        ModelBase model = theMeshBase.getModelBase();

        HashMap<PropertyType,PropertyValue> ret = new HashMap<PropertyType,PropertyValue>();
        for( int i=0 ; i<externalized.getPropertyTypes().length ; ++i ) {
            try {
                PropertyType  type  = model.findPropertyTypeByIdentifier( externalized.getPropertyTypes()[i] );
                
                if( !type.getIsReadOnly().value() ) {
                    PropertyValue value = externalized.getPropertyValues()[i];
                    ret.put(  type, value );
                }
            } catch( MeshTypeWithIdentifierNotFoundException ex ) {
                log.error( ex );
            }
        }
        return ret;
    }
        
    /**
     * Internal helper to extract EntityTypes from an ExternalizedNetMeshObject.
     * 
     * @param externalized the ExternalizedNetMeshObject
     * @return the EntityTypes
     */
    protected EntityType [] typesFromExternalizedMeshObject(
            ExternalizedMeshObject externalized )
    {
        ModelBase model = theMeshBase.getModelBase();

        EntityType [] ret = new EntityType[ externalized.getExternalTypeIdentifiers().length ];
        int count = 0;
        for( int i=0 ; i<ret.length ; ++i ) {
            try {
                ret[count] = model.findEntityTypeByIdentifier( externalized.getExternalTypeIdentifiers()[i] );
                ++count;
            } catch( MeshTypeWithIdentifierNotFoundException ex ) {
                log.error( ex );
            }                
        }
        if( count < ret.length ) {
            ret = ArrayHelper.copyIntoNewArray( ret, 0, count, EntityType.class );
        }
        return ret;
    }
    
    /**
     * Internal helper to extract RoleTypes from an ExternalizedNetMeshObject.
     * 
     * @param externalized the ExternalizedNetMeshObject
     * @return the RoleTypes, ordered in the same sequence as the neighbors
     */
    protected RoleType [][] roleTypesFromExternalizedMeshObject(
            ExternalizedMeshObject externalized )
    {
        ModelBase model = theMeshBase.getModelBase();

        MeshObjectIdentifier [] neighbors = externalized.getNeighbors();
        
        RoleType [][] roleTypes = new RoleType[ neighbors.length ][];
        for( int i=0 ; i<neighbors.length ; ++i ) {
            MeshTypeIdentifier [] row = externalized.getRoleTypesFor( neighbors[i] );
            if( row.length > 0 ) {
                roleTypes[i] = new RoleType[ row.length ];
                int count = 0;
                for( int j=0 ; j<row.length ; ++j ) {
                    try {
                        roleTypes[i][count] = model.findRoleTypeByIdentifier( row[j] );
                        ++count;
                    } catch( MeshTypeWithIdentifierNotFoundException ex ) {
                        log.error( ex );
                    }                
                }
                if( count < roleTypes[i].length ) {
                    roleTypes[i] = ArrayHelper.copyIntoNewArray( roleTypes[i], 0, count, RoleType.class );
                }
            }
        }
        return roleTypes;
    }

    /**
     * Overridable helper to create a NetMeshObjectCreatedEvent.
     *
     * @param createdObject the created MeshObject
     * @return the created event
     */
    @Override
    protected NetMeshObjectCreatedEvent createCreatedEvent(
            MeshObject createdObject )
    {
        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;

        Proxy                 incomingProxy           = realBase.determineIncomingProxy();
        NetMeshBaseIdentifier incomingProxyIdentifier = incomingProxy != null ? incomingProxy.getPartnerMeshBaseIdentifier() : null;

        NetMeshObjectCreatedEvent ret = new NetMeshObjectCreatedEvent(
                getMeshBase(),
                getMeshBase().getIdentifier(),
                (NetMeshObject) createdObject,
                incomingProxyIdentifier );
        return ret;
    } 

    /**
     * Overridable helper to create a NetMeshObjectDeletedEvent.
     * 
     * @param deletedObject the deleted MeshObject
     * @param canonicalIdentifier the canonical MeshObjectIdentifier of the deleted MeshObject.
     *        Once a MeshObject has been deleted, its canonical MeshObjectIdentifier can no longer be determined
     * @param externalized external form of the MeshObject just before it was deleted
     * @param timeEventOccurred the time when the event occurred
     * @return the created event
     */
    @Override
    protected NetMeshObjectDeletedEvent createDeletedEvent(
            MeshObject             deletedObject,
            MeshObjectIdentifier   canonicalIdentifier,
            ExternalizedMeshObject externalized,
            long                   timeEventOccurred )
    {
        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;

        Proxy                 incomingProxy           = realBase.determineIncomingProxy();
        NetMeshBaseIdentifier incomingProxyIdentifier = incomingProxy != null ? incomingProxy.getPartnerMeshBaseIdentifier() : null;

        NetMeshObjectDeletedEvent ret = new NetMeshObjectDeletedEvent(
                getMeshBase(),
                getMeshBase().getIdentifier(),
                (NetMeshObject) deletedObject,
                canonicalIdentifier,
                incomingProxyIdentifier,
                externalized,
                timeEventOccurred );
        return ret;
    }
    
    /**
     * Helper method that allows our subclasses to access the Store without having to expose it publicly.
     * 
     * @param identifier the Identifier of the MeshObject
     * @return the found MeshObject, or none.
     */
    @Override
    protected AnetMeshObject findInStore(
            MeshObjectIdentifier identifier )
    {
        return (AnetMeshObject) super.findInStore( identifier );
    }

    /**
     * Instantiate an ExternalizedMeshObject that is appropriate to capture the information held by
     * the subtype of MeshObject used by an AMeshBase. This is factored out so it can be subclassed.
     * 
     * @return the AParserFriendlyExternalizedMeshObjector subclass.
     */
    @Override
    public ParserFriendlyExternalizedNetMeshObject createParserFriendlyExternalizedMeshObject()
    {
        return new ParserFriendlyExternalizedNetMeshObject();
    }
}
