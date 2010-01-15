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

package org.infogrid.mesh.net.a;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.EquivalentAlreadyException;
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
import org.infogrid.mesh.RoleTypeNotBlessedException;
import org.infogrid.mesh.RoleTypeRequiresEntityTypeException;
import org.infogrid.mesh.a.AMeshObject;
import org.infogrid.mesh.net.DoNotHaveLockException;
import org.infogrid.mesh.net.HomeReplicaChangedEvent;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.SimpleExternalizedNetMeshObject;
import org.infogrid.mesh.net.security.CannotObtainLockException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.mesh.net.LockChangedEvent;
import org.infogrid.mesh.net.NotHomeReplicaException;
import org.infogrid.mesh.net.proxy.ReplicaProxyInterface;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.a.AnetMeshBase;
import org.infogrid.meshbase.net.transaction.NetMeshObjectBecameDeadStateEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectBecamePurgedStateEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectPropertyChangeEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeRemovedEvent;
import org.infogrid.meshbase.net.a.AccessLocallySynchronizer;
import org.infogrid.meshbase.transaction.MeshObjectStateEvent;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.util.ArrayCursorIterator;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.IsDeadException;
import org.infogrid.util.RemoteQueryTimeoutException;
import org.infogrid.util.ReturnSynchronizerException;
import org.infogrid.util.ZeroElementCursorIterator;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * <p>Subclasses AMeshObject to add information necessary for NetMeshBases according
 * to the NetMeshObject interface.</p>
 * 
 * <p>To save memory, lock operations are synchronized on theIdentifier instead of a
 * (cleaner) additional object; they cannot be synchronized on the AnetMeshObject itself
 * as this conflicts with property change operations' synchronization which may trigger
 * an attempt to obtain the lock on a different Thread.</p>
 */
public class AnetMeshObject
        extends
            AMeshObject
        implements
            NetMeshObject,
            ReplicaProxyInterface
{
    private static final Log log = Log.getLogInstance( AnetMeshObject.class ); // our own, private logger

    /**
     * Constructor for regular instantiation.
     * 
     * @param identifier the NetMeshObjectIdentifier of the NetMeshObject
     * @param meshBase the NetMeshBase that this NetMeshObject belongs to
     * @param created the time this NetMeshObject was created
     * @param updated the time this NetMeshObject was last updated
     * @param read the time this NetMeshObject was last read
     * @param expires the time this NetMeshObject will expire
     * @param giveUpHomeReplica if true, this replica will give up home replica status when asked
     * @param giveUpLock if true, this replica will give up the lock when asked
     * @param proxies the currently active set of Proxies that are interested in this NetMeshObject
     * @param homeProxyIndex identifies that Proxy in the proxies array in whose direction the home replica can be found. This may be -1, indicating "here".
     * @param proxyTowardsLockIndex identifies that Proxy in the proxies array in whose direction the lock can be found. This may be -1, indicating "here".
     */
    public AnetMeshObject(
            NetMeshObjectIdentifier identifier,
            AnetMeshBase            meshBase,
            long                    created,
            long                    updated,
            long                    read,
            long                    expires,
            boolean                 giveUpHomeReplica,
            boolean                 giveUpLock,
            Proxy []                proxies,
            int                     homeProxyIndex,
            int                     proxyTowardsLockIndex )
    {
        super( identifier, meshBase, created, updated, read, expires );

        theGiveUpHomeReplica     = giveUpHomeReplica;
        theGiveUpLock            = giveUpLock;
        theProxies               = proxies;
        theHomeProxyIndex        = homeProxyIndex;
        theProxyTowardsLockIndex = proxyTowardsLockIndex;
    }

    /**
     * Constructor for re-instantiation from external storage.
     * 
     * @param identifier the MeshObjectIdentifier of the MeshObject
     * @param meshBase the MeshBase that this MeshObject belongs to
     * @param created the time this MeshObject was created
     * @param updated the time this MeshObject was last updated
     * @param read the time this MeshObject was last read
     * @param expires the time this MeshObject will expire
     * @param properties the properties with their values of the MeshObject, if any
     * @param meshTypes the MeshTypes and facdes of the MeshObject, if any
     * @param equivalents either an array of length 2, or null. If given, contains the left and right equivalence pointers.
     * @param otherSides the current neighbors of the MeshObject, given as Identifiers
     * @param roleTypes the RoleTypes of the relationships with the various neighbors, in sequence
     * @param giveUpHomeReplica if true, this replica will give up home replica status when asked
     * @param giveUpLock if true, this replica will give up the lock when asked
     * @param proxies the currently active set of Proxies that are interested in this NetMeshObject
     * @param homeProxyIndex identifies that Proxy in the proxies array in whose direction the home replica can be found. This may be -1, indicating "here".
     * @param proxyTowardsLockIndex identifies that Proxy in the proxies array in whose direction the lock can be found. This may be -1, indicating "here".
     * @param relationshipProxies the sources of the relationship with the various neighbors, in sequence
     */
    public AnetMeshObject(
            NetMeshObjectIdentifier             identifier,
            AnetMeshBase                        meshBase,
            long                                created,
            long                                updated,
            long                                read,
            long                                expires,
            HashMap<PropertyType,PropertyValue> properties,
            EntityType []                       meshTypes,
            NetMeshObjectIdentifier []          equivalents,
            NetMeshObjectIdentifier []          otherSides,
            RoleType [][]                       roleTypes,
            boolean                             giveUpHomeReplica,
            boolean                             giveUpLock,
            Proxy []                            proxies,
            int                                 homeProxyIndex,
            int                                 proxyTowardsLockIndex,
            Proxy [][]                          relationshipProxies )
    {
        super(  identifier,
                meshBase,
                created,
                updated,
                read,
                expires,
                properties,
                meshTypes,
                equivalents,
                otherSides,
                roleTypes );

        theGiveUpHomeReplica     = giveUpHomeReplica;
        theGiveUpLock            = giveUpLock;
        theProxies               = proxies;
        theHomeProxyIndex        = homeProxyIndex;
        theProxyTowardsLockIndex = proxyTowardsLockIndex;
        theRelationshipProxies   = relationshipProxies;
    }
    
    /**
     * Obtain the globally unique identifier of this NetMeshObject.
     *
     * @return the globally unique identifier of this NetMeshObject
     */
    @Override
    public NetMeshObjectIdentifier getIdentifier()
    {
        return (NetMeshObjectIdentifier) super.getIdentifier();
    }

    /**
     * Obtain the NetMeshBase that contains this NetMeshObject. This is immutable for the
     * lifetime of this instance.
     *
     * @return the MeshBase that contains this MeshObject.
     */
    @Override
    public NetMeshBase getMeshBase()
    {
        return (NetMeshBase) theMeshBase;
    }

    /**
     * Obtain the NetMeshObjectIdentifiers of the neighbors of this MeshObject. This is sometimes a
     * more efficient operation than to traverse to the neighbors and determine the
     * NetMeshObjectIdentifiers from there.
     *
     * @return the NetMeshObjectIdentifiers of the neighbors, if any
     */
    @Override
    public NetMeshObjectIdentifier [] getNeighborMeshObjectIdentifiers()
    {
        return (NetMeshObjectIdentifier []) super.getNeighborMeshObjectIdentifiers();
    }

    /**
     * Obtain the NetMeshObjectIdentifiers of the neighbors of this MeshObject, as conveyed by
     * a given Proxy.
     *
     * @param p the Proxy
     * @return the NetMeshObjectIdentifiers of the neighbors, if any, according to the Proxy
     */
    public NetMeshObjectIdentifier [] getNeighborMeshObjectIdentifiersAccordingTo(
            Proxy p )
    {
        AnetMeshObjectNeighborManager nMgr = getNeighborManager();

        NetMeshObjectIdentifier [] ret = nMgr.getNeighborMeshObjectIdentifiersFromSource( this, p );
        return ret;
    }

    /**
      * Determine whether this replica has update rights.
      *
      * @return returns true if this is replica has the update rights
      */
    public boolean hasLock()
    {
        return theProxyTowardsLockIndex == HERE_CONSTANT;
    }

    /**
     * Attempt to obtain update rights.
     *
     * @return returns true if we have update rights, or we were successful obtaining them.
     * @throws RemoteQueryTimeoutException thrown if the replica that has the lock could not be contacted or did not reply in the time alloted
     */
    public boolean tryToObtainLock()
        throws
            RemoteQueryTimeoutException
    {
        return tryToObtainLock( -1L );
    }

    /**
     * Attempt to obtain update rights. Specify a timeout in milliseconds.
     *
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @return returns true if we have update rights, or we were successful obtaining them.
     * @throws RemoteQueryTimeoutException thrown if the replica that has the lock could not be contacted or did not reply in the time alloted
     */
    public boolean tryToObtainLock(
            long duration )
        throws
            RemoteQueryTimeoutException
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "tryToObtainLock" );
        }

        Proxy p;
        synchronized( theIdentifier ) {
            if( theProxyTowardsLockIndex == HERE_CONSTANT ) {
                return true;
            }
            p = theProxies[ theProxyTowardsLockIndex ];
        }

        AccessLocallySynchronizer synchronizer = ((NetMeshBase)theMeshBase).getAccessLocallySynchronizer();
        try {
            synchronizer.beginTransaction();

            long actualDuration = p.tryToObtainLocks( new NetMeshObject[] { this }, duration );

            synchronizer.join( actualDuration );

            synchronizer.endTransaction();

        } catch( ReturnSynchronizerException ex ) {
            log.error( ex );
        } catch( InterruptedException ex ) {
            log.error( ex );
        }
        
        if( theProxyTowardsLockIndex == HERE_CONSTANT ) {
            AnetMeshBase realBase = (AnetMeshBase) theMeshBase;                
            realBase.flushMeshObject( this );

            fireLockGainedEvent();

            return true;

        } else {
            return false;
        }
    }

    /**
     * Attempt to move update rights to the NetMeshBase that can be found through the
     * specified Proxy. This requires this NetMeshObject to have the update rights.
     * 
     * @param outgoingProxy the Proxy
     * @return returns true if the update rights were moved
     * @throws DoNotHaveLockException thrown if this NetMeshObject does not have update rights
     * @throws RemoteQueryTimeoutException thrown if the NetMeshBase could not be contacted or did not reply in the time alloted
     */
    public boolean tryToPushLock(
            Proxy outgoingProxy )
        throws
            DoNotHaveLockException,
            RemoteQueryTimeoutException
    {
        return tryToPushLock( outgoingProxy, -1L );
    }
            
    /**
     * Attempt to move update rights to the NetMeshBase that can be found through the
     * specified Proxy. Specify a timeout in milliseconds. This requires this NetMeshObject to have the update rights.
     * 
     * @param outgoingProxy the Proxy
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @return returns true if the update rights were moved
     * @throws DoNotHaveLockException thrown if this NetMeshObject does not have update rights
     * @throws RemoteQueryTimeoutException thrown if the NetMeshBase could not be contacted or did not reply in the time alloted
     */
    public boolean tryToPushLock(
            Proxy outgoingProxy,
            long  duration )
        throws
            DoNotHaveLockException,
            RemoteQueryTimeoutException
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "tryToPushLock", outgoingProxy );
        }

        boolean isNewProxy = false;
        synchronized( theIdentifier ) {
            if( theProxyTowardsLockIndex != HERE_CONSTANT ) {
                throw new DoNotHaveLockException( this );
            }
            // find the right proxy
            Proxy p = null;
            if( theProxies != null ) {
                for( int i=0 ; i<theProxies.length ; ++i ) {
                    if( theProxies[i] == outgoingProxy ) {
                        p = outgoingProxy;
                        break;
                    }
                }
            }
            if( p == null ) {
                // not currently going via this proxy
                isNewProxy = true;
                if( theProxies == null ) {
                    theProxies = new Proxy[] { outgoingProxy };
                } else {
                    theProxies = ArrayHelper.append( theProxies, outgoingProxy, Proxy.class );
                }
                theProxyTowardsLockIndex = theProxies.length - 1;

                AnetMeshBase realBase = (AnetMeshBase) theMeshBase;                
                realBase.flushMeshObject( this );
            }
        }

        AccessLocallySynchronizer synchronizer = ((NetMeshBase)theMeshBase).getAccessLocallySynchronizer();

        try {
            long actualDuration = outgoingProxy.tryToPushLocks( new NetMeshObject[] { this }, new boolean[] { isNewProxy }, duration );

            synchronizer.join( actualDuration );

        } catch( ReturnSynchronizerException ex ) {
            log.error( ex );

        } catch( InterruptedException ex ) {
            log.error( ex );
        }
        
        if( theProxyTowardsLockIndex != HERE_CONSTANT ) {
            fireLockLostEvent();

            return true;

        } else {
            return false;
        }
    }
    
    /**
     * Forced recovery of the lock by the home replica.
     * 
     * @throws NotHomeReplicaException thrown if this method is invoked on a replica other than the home replica
     */
    public void forceLockRecovery()
            throws
                NotHomeReplicaException
    {
        Proxy p;
        synchronized( theIdentifier ) {
            if( theProxyTowardsLockIndex == HERE_CONSTANT ) {
                return;
            }
            if( theHomeProxyIndex != HERE_CONSTANT ) {
                throw new NotHomeReplicaException( this );
            }
            p = theProxies[ theProxyTowardsLockIndex ];
            theProxyTowardsLockIndex = HERE_CONSTANT;
        }
        AccessLocallySynchronizer synchronizer = ((NetMeshBase)theMeshBase).getAccessLocallySynchronizer();

        try {
            p.forceObtainLocks( new NetMeshObject[] { this }, -1L ); // FIXME default?

            synchronizer.join();

        } catch( ReturnSynchronizerException ex ) {
            log.error( ex );

        } catch( InterruptedException ex ) {
            log.error( ex );
        }

        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;                
        realBase.flushMeshObject( this );

        fireLockGainedEvent();
    }
    
    /**
      * Determine whether this replica is going to give up update rights if it has them,
      * in case someone asks. This only says "if this replica has update rights, it will
      * give them up when asked". This call makes no statement about whether this replica
      * currently does or does not have update rights.
      *
      * @return if true, this replica will give up update rights when asked
      * @see #getWillGiveUpLock
      */
    public boolean getWillGiveUpLock()
    {
        return theGiveUpLock;
    }

    /**
      * Set whether this replica will allow update rights to be given up or not.
      * However, if this is not the home replica and a lease for the replica expires, the
      * home replica will still reclaim the lock. Setting this value will not
      * prevent that.
      *
      * @param yesNo if true, this replica will give update rights when asked
      * @see #getWillGiveUpLock
      */
    public void setWillGiveUpLock(
            boolean yesNo )
    {
        if( yesNo != theGiveUpLock ) {
            theGiveUpLock = yesNo; // currently does not generate events
            
            AnetMeshBase realBase = (AnetMeshBase) theMeshBase;
            realBase.flushMeshObject( this );
        }        
    }

    /**
     * Obtain the Proxy in the direction of the update rights for this replica.
     * This may return null, indicating that this replica has the update rights.
     *
     * @return the Proxy in the direction of the update rights
     */
    public Proxy getProxyTowardsLockReplica()
    {
        if( theProxyTowardsLockIndex == HERE_CONSTANT ) {
            return null;
        } else {
            return theProxies[ theProxyTowardsLockIndex ];
        }
    }
    
    /**
     * Determine whether this the home replica.
     *
     * @return returns true if this is the home replica
     */
    public boolean isHomeReplica()
    {
        return theHomeProxyIndex == HERE_CONSTANT;
    }

    /**
     * Attempt to obtain the home replica status.
     *
     * @return returns true if we have home replica status, or we were successful obtaining it.
     * @throws RemoteQueryTimeoutException thrown if the replica that has home replica status could not be contacted or did not reply in the time alloted
     */
    public boolean tryToObtainHomeReplica()
        throws
            RemoteQueryTimeoutException
    {
        return tryToObtainHomeReplica( -1L );
    }

    /**
     * Attempt to obtain the home replica status. Specify a timeout in milliseconds.
     *
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @return returns true if we have home replica status, or we were successful obtaining it.
     * @throws RemoteQueryTimeoutException thrown if the replica that has home replica status could not be contacted or did not reply in the time alloted
     */
    public boolean tryToObtainHomeReplica(
            long duration )
        throws
            RemoteQueryTimeoutException
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "tryToObtainHomeReplica" );
        }

        Proxy p;
        synchronized( theIdentifier ) {
            if( theHomeProxyIndex == HERE_CONSTANT ) {
                return true;
            }
            p = theProxies[ theHomeProxyIndex ];
        }

        AccessLocallySynchronizer synchronizer = ((NetMeshBase)theMeshBase).getAccessLocallySynchronizer();

        try {
            long actualDuration = p.tryToObtainHomeReplicas( new NetMeshObject[] { this }, duration );

            synchronizer.join( actualDuration );

        } catch( ReturnSynchronizerException ex ) {
            log.error( ex );

        } catch( InterruptedException ex ) {
            log.error( ex );
        }
        
        if( theHomeProxyIndex == HERE_CONSTANT ) {
            AnetMeshBase realBase = (AnetMeshBase) theMeshBase;                
            realBase.flushMeshObject( this );

            fireHomeReplicaGainedEvent();

            return true;

        } else {
            return false;
        }        
    }

    /**
     * Attempt to move the home replica status to the NetMeshBase that can be found through the
     * specified Proxy. This requires this NetMeshObject to have home replica status.
     * 
     * @param outgoingProxy the Proxy
     * @return returns true if the home replica status was moved
     * @throws NotHomeReplicaException thrown if this NetMeshObject does not have home replica status
     * @throws RemoteQueryTimeoutException thrown if the NetMeshBase could not be contacted or did not reply in the time alloted
     */
    public boolean tryToPushHomeReplica(
            Proxy outgoingProxy )
        throws
            NotHomeReplicaException,
            RemoteQueryTimeoutException
    {
        return tryToPushHomeReplica( outgoingProxy, -1L );
    }
            
    /**
     * Attempt to move the home replica status to the NetMeshBase that can be found through the
     * specified Proxy. Specify a timeout in milliseconds. This requires this NetMeshObject to have home replica status.
     * 
     * @param outgoingProxy the Proxy
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @return returns true if the home replica status was moved
     * @throws NotHomeReplicaException thrown if this NetMeshObject does not have home replica status
     * @throws RemoteQueryTimeoutException thrown if the NetMeshBase could not be contacted or did not reply in the time alloted
     */
    public boolean tryToPushHomeReplica(
            Proxy outgoingProxy,
            long  duration )
        throws
            NotHomeReplicaException,
            RemoteQueryTimeoutException
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "tryToPushHomeReplica", outgoingProxy );
        }

        boolean isNewProxy = false;
        synchronized( theIdentifier ) {
            if( theHomeProxyIndex != HERE_CONSTANT ) {
                throw new NotHomeReplicaException( this );
            }
            // find the right proxy
            Proxy p = null;
            if( theProxies != null ) {
                for( int i=0 ; i<theProxies.length ; ++i ) {
                    if( theProxies[i] == outgoingProxy ) {
                        break;
                    }
                }
            }
            if( p == null ) {
                // not currently going via this proxy
                isNewProxy = true;
                if( theProxies == null ) {
                    theProxies = new Proxy[] { outgoingProxy };
                } else {
                    theProxies = ArrayHelper.append( theProxies, outgoingProxy, Proxy.class );
                }
                theHomeProxyIndex = theProxies.length - 1;

                AnetMeshBase realBase = (AnetMeshBase) theMeshBase;                
                realBase.flushMeshObject( this );
            }
        }

        AccessLocallySynchronizer synchronizer = ((NetMeshBase)theMeshBase).getAccessLocallySynchronizer();

        try {
            long delta = outgoingProxy.tryToPushHomeReplicas( new NetMeshObject[] { this }, new boolean[] { isNewProxy }, duration );

            if( duration >=0L ) {
                delta = duration;
            }
            synchronizer.join( delta );

        } catch( ReturnSynchronizerException ex ) {
            log.error( ex );

        } catch( InterruptedException ex ) {
            log.error( ex );
        }

        if( theHomeProxyIndex != HERE_CONSTANT ) {
            fireHomeReplicaLostEvent();

            return true;

        } else {
            return false;
        }        
    }
    
    /**
     * Determine whether this replica is going to give up home replica status if it has it,
     * in case someone asks. This only says "if this replica is the home replica, it
     * will give it up when asked". This call makes no statement about whether this replica
     * currently does or does not have home replica status.
     * 
     * @return if true, this replica will give up home replica status when asked
     * @see #setWillGiveUpHomeReplica
     */
    public boolean getWillGiveUpHomeReplica()
    {
        return theGiveUpHomeReplica;
    }

    /**
     * Set whether this replica will allow home replica status to be given up or not.
     * 
     * @param yesNo if true, this replica will give up home replica status when asked
     * @see #getWillGiveUpHomeReplica
     */
    public void setWillGiveUpHomeReplica(
            boolean yesNo )
    {
        if( yesNo != theGiveUpHomeReplica ) {
            theGiveUpHomeReplica = yesNo; // currently does not generate events
            
            AnetMeshBase realBase = (AnetMeshBase) theMeshBase;
            realBase.flushMeshObject( this );
        }        
    }

    /**
     * Obtain the Proxy in the direction of the home replica.
     * This may return null, indicating that this replica is the home replica.
     *
     * @return the Proxy in the direction of the home replica
     */
    public Proxy getProxyTowardsHomeReplica()
    {
        if( theHomeProxyIndex == HERE_CONSTANT ) {
            return null;
        } else {
            return theProxies[ theHomeProxyIndex ];
        }
    }
    
    /**
     * Obtain all Proxies applicable to this replica.
     *
     * @return all Proxies. This may return null for efficiency reasons.
     */
    public Proxy [] getAllProxies()
    {
        return theProxies;
    }

    /**
     * Obtain an Iterator over all Proxies applicable to this replica.
     *
     * @return the CursorIterator
     */
    public CursorIterator<Proxy> proxyIterator()
    {
        Proxy [] p = theProxies; // saves us a synchronized
        if( p != null ) {
            return ArrayCursorIterator.<Proxy>create( p );
        } else {
            return ZeroElementCursorIterator.<Proxy>create();
        }
    }

    /**
     * Find a Proxy towards a partner NetMeshBase with a particular NetMeshBaseIdentifier. If such a
     * Proxy does not exist, return null.
     * 
     * @param partnerIdentifier the NetMeshBaseIdentifier of the partner NetMeshBase
     * @return the found Proxy, or null
     */
    public Proxy findProxyTowards(
            NetMeshBaseIdentifier partnerIdentifier )
    {
        Proxy [] snapshot = theProxies;
        if( snapshot == null ) {
            return null;
        }
        for( Proxy current : snapshot ) {
            if( partnerIdentifier.equals( current.getPartnerMeshBaseIdentifier() )) {
                return current;
            }
        }
        return null;
    }

    /**
     * Obtain all relationship Proxies applicable to this replica.
     *
     * @return all relationship Proxies. This may return null for efficiency reasons.
     */
    public Proxy [] getAllRelationshipProxies()
    {
        AnetMeshObjectNeighborManager nMgr = getNeighborManager();

        Proxy [][] found = nMgr.getRelationshipProxies( this );
        if( found == null || found.length == 0 ) {
            return null;
        }
        ArrayList<Proxy> almost = new ArrayList<Proxy>();
        for( int i=0 ; i<found.length ; ++i ) {
            if( found[i] == null ) {
                continue;
            }
            for( int j=0 ; j<found[i].length ; ++j ) {
                if( !almost.contains( found[i][j] )) {
                    almost.add( found[i][j] );
                }
            }
        }
        if( almost.isEmpty() ) {
            return null;
        }
        Proxy [] ret = ArrayHelper.copyIntoNewArray( almost, Proxy.class );
        return ret;
    }

    /**
     * Obtain the set of relationship Proxies for the relationship between this NetMeshObject
     * and the provided neighbor NetMeshObject.
     *
     * @param neighborIdentifier identifier of the neighbor NetMeshObject, identifying the relationship
     * @return the found relationship Proxies
     * @throws NotRelatedException thrown if the two NetMeshObjects are not related
     */
    public Proxy [] getRelationshipProxiesFor(
            NetMeshObjectIdentifier neighborIdentifier )
        throws
            NotRelatedException
    {
        AnetMeshObjectNeighborManager nMgr = getNeighborManager();

        Proxy [] ret = nMgr.getRelationshipProxiesFor( this, neighborIdentifier );
        return ret;
    }

    /**
     * Determine the path to the NetMeshObject's home replica, to the extent it is known here.
     *
     * @return the path to the NetMeshObject's home 4replica
     */
    public NetMeshObjectAccessSpecification getPathToHomeReplica()
    {
        // FIXME -- needs more data
        Proxy homeProxy = getProxyTowardsHomeReplica();
        NetMeshObjectAccessSpecification ret;

        if( homeProxy != null ) {
            ret = ((NetMeshBase)theMeshBase).getNetMeshObjectAccessSpecificationFactory().obtain( homeProxy.getPartnerMeshBaseIdentifier() );
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Obtain the same NetMeshObject as ExternalizedNetMeshObject so it can be easily serialized.
     * 
     * @return this NetMeshObject as SimpleExternalizedNetMeshObject
     */
    @Override
    public SimpleExternalizedNetMeshObject asExternalized()
    {
        return asExternalized( false );
    }

    /**
     * Obtain the same NetMeshObject as SimpleExternalizedNetMeshObject so it can be easily serialized.
     * 
     * @param captureProxies if true, the SimpleExternalizedNetMeshObject contain entries for the
     *        Proxies as held in this replica. If false, that information will be left out.
     * @return this NetMeshObject as SimpleExternalizedNetMeshObject
     */
    public SimpleExternalizedNetMeshObject asExternalized(
            boolean captureProxies )
    {
        AnetMeshObjectNeighborManager nMgr = getNeighborManager();

        MeshTypeIdentifier [] types;
        if( theMeshTypes != null && theMeshTypes.size() > 0 ) {
            types = new MeshTypeIdentifier[ theMeshTypes.size() ];

            int i=0;
            for( EntityType current : theMeshTypes.keySet() ) {
                types[i++] = current.getIdentifier();
            }
        } else {
            types = null;
        }
        
        // FIXME? Only send read-write and not read-only properties?
        MeshTypeIdentifier [] propertyTypes;
        PropertyValue  [] propertyValues;
        if( theProperties != null && theProperties.size() > 0 ) {
            propertyTypes  = new MeshTypeIdentifier[ theProperties.size() ];
            propertyValues = new PropertyValue[ propertyTypes.length ];

            int i=0;
            for( PropertyType current : theProperties.keySet() ) {
                propertyTypes[i]  = current.getIdentifier();
                propertyValues[i] = theProperties.get( current );
                ++i;
            }
        } else {
            propertyTypes  = null;
            propertyValues = null;
        }
        
        NetMeshObjectIdentifier [] neighborIdentifiers;
        MeshTypeIdentifier [][]    roleTypeIdentifiers;
        NetMeshBaseIdentifier [][] relationshipProxyNames;

        if( nMgr.hasNeighbors( this )) {
            RoleType [][] roleTypes           = nMgr.getRoleTypes( this );
            Proxy [][]    relationshipProxies = nMgr.getRelationshipProxies( this );

            neighborIdentifiers    = nMgr.getNeighborIdentifiers( this );
            roleTypeIdentifiers    = new MeshTypeIdentifier[ roleTypes.length][];
            relationshipProxyNames = new NetMeshBaseIdentifier[ relationshipProxies.length ][];

            for( int i=0 ; i<roleTypes.length ; ++i ) {
                if( roleTypes[i] != null && roleTypes[i].length > 0 ) {
                    roleTypeIdentifiers[i] = new MeshTypeIdentifier[ roleTypes[i].length ];
                    for( int j=0 ; j<roleTypes[i].length ; ++j ) {
                        roleTypeIdentifiers[i][j] = roleTypes[i][j].getIdentifier();
                    }
                }
                if( relationshipProxies[i] != null && relationshipProxies[i].length > 0 ) {
                    relationshipProxyNames[i] = new NetMeshBaseIdentifier[ relationshipProxies[i].length ];
                    for( int j=0 ; j<relationshipProxies[i].length ; ++j ) {
                        relationshipProxyNames[i][j] = relationshipProxies[i][j].getPartnerMeshBaseIdentifier();
                    }
                }
            }
        } else {
            neighborIdentifiers    = null;
            roleTypeIdentifiers    = null;
            relationshipProxyNames = null;
        }


        NetMeshBaseIdentifier [] proxyNames;
        int homeProxyIndex;
        int lockProxyIndex;
        
        if( captureProxies ) {
            if( theProxies == null ) {
                proxyNames = null;
            } else {
                proxyNames = new NetMeshBaseIdentifier[ theProxies.length ];
                for( int i=0 ; i<theProxies.length ; ++i ) {
                    proxyNames[i] = theProxies[i].getPartnerMeshBaseIdentifier();
                }
            }
            homeProxyIndex = theHomeProxyIndex;
            lockProxyIndex = theProxyTowardsLockIndex;
        } else {
            proxyNames     = null;
            homeProxyIndex = HERE_CONSTANT;
            lockProxyIndex = HERE_CONSTANT;
        }
        
        NetMeshObjectIdentifier [] equivalents;
        if( theEquivalenceSetPointers == null ) {
            equivalents = null;
        } else if( theEquivalenceSetPointers[0] == null ) {
            if( theEquivalenceSetPointers[1] == null ) {
                equivalents = null;
            } else {
                equivalents = new NetMeshObjectIdentifier[] { (NetMeshObjectIdentifier) theEquivalenceSetPointers[1] };
            }
        } else if( theEquivalenceSetPointers[1] == null ) {
            equivalents = new NetMeshObjectIdentifier[] {
                    (NetMeshObjectIdentifier) theEquivalenceSetPointers[0]
            };
        } else {
            equivalents = new NetMeshObjectIdentifier[] {
                    (NetMeshObjectIdentifier) theEquivalenceSetPointers[0],
                    (NetMeshObjectIdentifier) theEquivalenceSetPointers[1]
            };
        }
        
        SimpleExternalizedNetMeshObject ret = SimpleExternalizedNetMeshObject.create(
                getIdentifier(),
                types,
                theTimeCreated,
                theTimeUpdated,
                theTimeRead,
                theTimeExpires,
                propertyTypes,
                propertyValues,
                neighborIdentifiers,
                roleTypeIdentifiers,
                equivalents,
                theGiveUpHomeReplica,
                theGiveUpLock,
                proxyNames,
                homeProxyIndex,
                lockProxyIndex,
                relationshipProxyNames );

        return ret;
    }

// ReplicaProxyInterface    
    
    /**
      * Surrender update rights when invoked. This shall not be called by the application
      * programmer. This is called only by Proxies that identify themselves to this call.
      *
      * @param theProxy the Proxy invoking this method
      * @return true if successful, false otherwise.
      */
    public boolean proxyOnlySurrenderLock(
            Proxy theProxy )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "surrenderLock", theProxy );
        }

        synchronized( theIdentifier ) {

            if( !theGiveUpLock ) {
                return false;
            }
            if( theProxyTowardsLockIndex != HERE_CONSTANT ) {
                if( theProxies[ theProxyTowardsLockIndex ] != theProxy ) {
                    return false;
                }
            }

            boolean success = false;

            if( theProxies == null ) {
                theProxies               = new Proxy[] { theProxy };
                theProxyTowardsLockIndex = 0;
                success                  = true;

            } else {
                int index = ArrayHelper.findIn( theProxy, theProxies, false );
                
                if( index >=0 ) {
                    theProxyTowardsLockIndex = index;
                    success                  = true;
                }
            }
            
            if( success ) {
                fireLockLostEvent();
                return true;

            } else {
                return false;
            }
        }        
    }

    /**
      * Push update rights to this replica. This shall not be called by the application
      * programmer. This is called only by Proxies that identify themselves to this call.
      *
      * @param theProxy the Proxy invoking this method
      */
    public void proxyOnlyPushLock(
            Proxy theProxy )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "pushLock", theProxy );
        }

        synchronized( theIdentifier ) {

            if( theProxyTowardsLockIndex == HERE_CONSTANT ) {
                log.error( this + ": have lock already" );

            } else {
                if( theProxy != theProxies[theProxyTowardsLockIndex] ) {
                    log.error( "proxy that does not have lock cannot push lock to here" );
                }

                theProxyTowardsLockIndex = HERE_CONSTANT;

                fireLockGainedEvent();
            }
        }
    }

    /**
     * Surrender home replica status when invoked.  This shall not be called by the application
     * programmer. This is called only by Proxies that identify themselves to this call.
     *
     * @param theProxy the Proxy invoking this method
     * @return true if successful, false otherwise.
     */
    public boolean proxyOnlySurrenderHomeReplica(
            Proxy theProxy )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "surrenderHomeReplica", theProxy );
        }

        synchronized( theIdentifier ) {

            if( !theGiveUpHomeReplica ) {
                return false;
            }
            if( theHomeProxyIndex != HERE_CONSTANT ) {
                if( theProxies[ theHomeProxyIndex ] != theProxy ) {
                    return false;
                }
            }

            boolean success = false;

            if( theProxies == null ) {
                theProxies               = new Proxy[] { theProxy };
                theHomeProxyIndex        = 0;
                success                  = true;

            } else {
                int index = ArrayHelper.findIn( theProxy, theProxies, false );
                
                if( index >=0 ) {
                    theHomeProxyIndex        = index;
                    success                  = true;
                }
            }
            
            if( success ) {
                fireHomeReplicaLostEvent();
                return true;

            } else {
                return false;
            }
        }         
    }
    
    /**
     * Push home replica status to this replica. This shall not be called by the application
     * programmer. This is called only by Proxies that identify themselves to this call.
     * 
     * @param theProxy the Proxy invoking this method
     */
    public void proxyOnlyPushHomeReplica(
            Proxy theProxy )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "pushHomeReplica", theProxy );
        }

        synchronized( theIdentifier ) {

            if( theHomeProxyIndex == HERE_CONSTANT ) {
                log.error( this + ": are home replica already" );

            } else {
                if( theProxy != theProxies[theHomeProxyIndex] ) {
                    log.error( "proxy that does not have home replica cannot push home replica to here" );
                }

                theHomeProxyIndex = HERE_CONSTANT;

                fireHomeReplicaGainedEvent();
            }
        }
    }

    /**
     * Tell the NetMeshObject to make a note of the fact that a new replica of the
     * NetMeshObject is being created in the direction of the provided Proxy.
     * This shall not be called by the application
     * programmer. This is called only by Proxies that identify themselves to this call.
     *
     * @param theProxy the Proxy invoking this method
     * @throws IllegalArgumentException thrown if the proxy had been registered before
     */
    public void proxyOnlyRegisterReplicationTowards(
            Proxy theProxy )
        throws
            IllegalArgumentException
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "registerReplicationTowards", theProxy );
        }

        synchronized( theIdentifier ) {
            if( theProxies == null ) {
                theProxies = new Proxy[] { theProxy };
                theHomeProxyIndex        = HERE_CONSTANT;
                theProxyTowardsLockIndex = HERE_CONSTANT;

            } else {
                for( Proxy p : theProxies ) {
                    if( p == theProxy ) {
                        throw new IllegalArgumentException( this + " - already registered this proxy: " + theProxy );
                    }
                }
                theProxies = ArrayHelper.append( theProxies, theProxy, Proxy.class );
            }
        }
        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;
        realBase.flushMeshObject( this );
    }

    /**
      * Tell the NetMeshObject to remove the note of the fact that a replica of the
      * NetMeshObject exists in the direction of the provided Proxy.
      * This shall not be called by the application
      * programmer. This is called only by Proxies that identify themselves to this call.
      *
      * @param theProxy the Proxy invoking this method
      */
    public void proxyOnlyUnregisterReplicationTowards(
            Proxy theProxy )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "unregisterReplicationTowards", theProxy );
        }

        synchronized( theIdentifier ) {
            if( theProxies == null ) {
                log.error( this + " - no proxies: " + theProxy );
                return;
            }

            // find the proxy
            int foundIndex;
            for( foundIndex = 0 ; foundIndex < theProxies.length ; ++foundIndex ) {
                if( theProxies[foundIndex] == theProxy ) {
                    break;
                }
            }
            if( foundIndex == theProxies.length ) {
                // not found
                log.error( this + " - proxy not found: " + theProxy );
                return;
            }
            if( theProxies.length == 1 ) {
                theProxies = null;
            } else {
                for( int i=foundIndex+1 ; i<theProxies.length ; ++i ) {
                    theProxies[i-1] = theProxies[i];
                }
                theProxies = ArrayHelper.copyIntoNewArray( theProxies, 0, theProxies.length-1, Proxy.class );
            }

            if( theHomeProxyIndex != HERE_CONSTANT ) {
                if( theHomeProxyIndex == foundIndex ) {
                    theHomeProxyIndex = HERE_CONSTANT; // FIXME? Error message?
                } else if( theHomeProxyIndex > foundIndex ) {
                    --theHomeProxyIndex;
                }
            }
            if( theProxyTowardsLockIndex != HERE_CONSTANT ) {
                if( theProxyTowardsLockIndex == foundIndex ) {
                    theProxyTowardsLockIndex = HERE_CONSTANT; // FIXME? Error message?
                } else if( theProxyTowardsLockIndex > foundIndex ) {
                    --theProxyTowardsLockIndex;
                }
            }
        }
        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;
        realBase.flushMeshObject( this );
    }
    
    /**
     * Find a single neighbor MeshObject of this MeshObject that is known by its
     * MeshObjectIdentifier.
     * We pass in the MeshBase to use because this may be invoked when a MeshObject's member
     * variable has been zero'd out already.
     * This internal helper method may be overridden by subclasses.
     *
     * @param mb the MeshBase to use
     * @param neighborIdentifier the MeshObjectIdentifier of the MeshObject we are looking for
     * @return the MeshObject that we found
     */
    @Override
    protected NetMeshObject findRelatedMeshObject(
            MeshBase             mb,
            MeshObjectIdentifier neighborIdentifier )
    {
        NetMeshObject [] ret = findRelatedMeshObjects( mb, new NetMeshObjectIdentifier[] { (NetMeshObjectIdentifier) neighborIdentifier } );
        return ret[0];
    }

    /**
     * Find neighbor MeshObjects of this MeshObject that are known by their
     * MeshObjectIdentifiers.
     * We pass in the MeshBase to use because this may be invoked when a MeshObject's member
     * variable has been zero'd out already.
     * The implementation of this method attempts to be smart: if a related NetMeshObject cannot be found in the
     * local NetMeshBase, it attempts a NetMeshBase.accessLocally() in the direction of
     * the home replica.
     *
     * @param mb the MeshBase to use
     * @param identifiers the MeshObjectIdentifiers of the MeshObjects we are looking for
     * @return the MeshObjects that we found
     */
    @Override
    protected NetMeshObject [] findRelatedMeshObjects(
            MeshBase                mb,
            MeshObjectIdentifier [] identifiers )
    {
        NetMeshBase realBase  = (NetMeshBase) mb;
        Proxy       homeProxy = getProxyTowardsHomeReplica();

        NetMeshObject [] ret = realBase.findMeshObjectsByIdentifier( identifiers );

        if( homeProxy != null ) {
            // try to fill in what we don't have locally by creating an expired replica and resynchronizing them
            // as soon as possible
            NetMeshBaseLifecycleManager             life           = realBase.getMeshBaseLifecycleManager();
            NetMeshObjectAccessSpecificationFactory accessSpecFact = ((NetMeshBase)theMeshBase).getNetMeshObjectAccessSpecificationFactory();
            NetMeshObjectAccessSpecification []     toFind         = new NetMeshObjectAccessSpecification[ ret.length ];

            AnetMeshObjectNeighborManager nMgr = getNeighborManager();

            int count = 0;
            for( int i=0 ; i<ret.length ; ++i ) {
                if( ret[i] == null ) {
                    // check where the relationship came from

                    Proxy direction = homeProxy;
                    // pick any. if none found, use homeProxy
                    try {
                        Proxy [] relationshipProxies = nMgr.getRelationshipProxiesFor( this, (NetMeshObjectIdentifier) identifiers[i] );
                        if( relationshipProxies != null && relationshipProxies.length > 0 ) {
                            direction = relationshipProxies[0];
                        }
                    } catch( NotRelatedException ex ) {
                        log.error( ex );
                    }

                    toFind[count++] = accessSpecFact.obtain( direction.getPartnerMeshBaseIdentifier(), (NetMeshObjectIdentifier) identifiers[i] );
                }
            }
            if( count > 0 ) {
                if( count < toFind.length ) {
                    toFind = ArrayHelper.copyIntoNewArray( toFind, 0, count, NetMeshObjectAccessSpecification.class );
                }
                Transaction txAlready = theMeshBase.getCurrentTransaction();

                NetMeshObject [] found = null;

                if( txAlready != null ) {
                    Transaction tx = null;
                    try {
                        tx = realBase.createTransactionAsapIfNeeded();
                    
                        found = life.createForwardReferences( toFind );

                    } catch( TransactionException ex ) {
                        log.error( ex );
                    } catch( MeshObjectIdentifierNotUniqueException ex ) {
                        log.error( ex );
                    } finally {
                        if( tx != null ) {
                            tx.commitTransaction();
                        }
                    }
                } else {
                    try {
                        found = realBase.accessLocally( toFind );

                    } catch( MeshObjectAccessException ex ) {
                        MeshObject [] bestEffort = ex.getBestEffortResult();
                        found = (NetMeshObject []) ArrayHelper.copyIntoNewArray( bestEffort, NetMeshObject.class );

                    } catch( NotPermittedException ex ) {
                        log.warn( ex );
                    }
                }
                if( found != null ) {
                    count = 0;
                    for( int i=0 ; i<ret.length ; ++i ) {
                        if( count < found.length && identifiers[i].equals( toFind[count].getNetMeshObjectIdentifier() )) {
                            ret[i] = found[count];
                            ++count;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Check whether it is permitted to set this MeshObject's timeExpires to the given value.
     *
     * @param newValue the proposed new value for the timeExpires
     * @throws NotPermittedException thrown if it is not permitted
     */
    @Override
    public void checkPermittedSetTimeExpires(
            long newValue )
        throws
            NotPermittedException
    {
        try {
            if( !tryToObtainLock() ) {
                throw new CannotObtainLockException( this );
            }
        } catch( RemoteQueryTimeoutException ex ) {
            throw new CannotObtainLockException( this, ex );
        }
        super.checkPermittedSetTimeExpires( newValue );
    }

    /**
     * Check whether it is permitted to set this MeshObject's given property to the given
     * value. Subclasses may override this.
     *
     * @param thePropertyType the PropertyType identifing the property to be modified
     * @param newValue the proposed new value for the property
     * @throws NotPermittedException thrown if it is not permitted
     */
    @Override
    public void checkPermittedSetProperty(
            PropertyType  thePropertyType,
            PropertyValue newValue )
        throws
            NotPermittedException
    {
        try {
            if( !tryToObtainLock() ) {
                throw new CannotObtainLockException( this );
            }
        } catch( RemoteQueryTimeoutException ex ) {
            throw new CannotObtainLockException( this, ex );
        }
        super.checkPermittedSetProperty( thePropertyType, newValue );
    }

    /**
     * Check whether it is permitted to bless this MeshObject with the given EntityTypes. Subclasses
     * may override this.
     * 
     * @param types the EntityTypes with which to bless
     * @throws NotPermittedException thrown if it is not permitted
     */
    @Override
    public void checkPermittedBless(
            EntityType [] types )
        throws
            NotPermittedException
    {
        try {
            if( !tryToObtainLock() ) {
                throw new CannotObtainLockException( this );
            }
        } catch( RemoteQueryTimeoutException ex ) {
            throw new CannotObtainLockException( this, ex );
        }
        super.checkPermittedBless( types );
    }
    
    /**
     * Check whether it is permitted to unbless this MeshObject from the given EntityTypes. Subclasses
     * may override this.
     * 
     * @param types the EntityTypes from which to unbless
     * @throws NotPermittedException thrown if it is not permitted
     */
    @Override
    public void checkPermittedUnbless(
            EntityType [] types )
        throws
            NotPermittedException
    {
        try {
            if( !tryToObtainLock() ) {
                throw new CannotObtainLockException( this );
            }
        } catch( RemoteQueryTimeoutException ex ) {
            throw new CannotObtainLockException( this, ex );
        }
        super.checkPermittedUnbless( types );
    }

    /**
     * Check whether it is permitted to delete this MeshObject. This checks both whether the
     * MeshObject itself may be deleted, and whether the relationships it participates in may
     * be deleted (which in turn depends on whether the relationships may be unblessed).
     * Subclasses may override this.
     *
     * @throws NotPermittedException thrown if it is not permitted
     */
    @Override
    public void checkPermittedDelete()
        throws
            NotPermittedException
    {
        try {
            if( !tryToObtainLock() ) {
                throw new CannotObtainLockException( this );
            }
        } catch( RemoteQueryTimeoutException ex ) {
            throw new CannotObtainLockException( this, ex );
        }
        super.checkPermittedDelete();
    }

    /**
     * Internal helper to implement a method.
     * 
     * @param isMaster true if this is the master replica
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    @Override
    protected void internalDelete(
            boolean isMaster,
            long    timeUpdated )
        throws
            TransactionException,
            NotPermittedException
    {
        if( theMeshBase == null ) {
            // this is a loop, do nothing
            return;
        }
        theProxies = null;
        super.internalDelete( isMaster, timeUpdated );
    }

    /**
     * Allows us to bless MeshObjects that act as ForwardReferences with EntityTypes
     * that are abstract.
     * 
     * @param types the new EntityTypes to be supported by this MeshObject
     * @throws EntityBlessedAlreadyException thrown if this MeshObject is blessed already with at least one of these EntityTypes
     * @throws TransactionException thrown if invoked outside of proper transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void blessForwardReference(
            EntityType [] types )
        throws
            EntityBlessedAlreadyException,
            TransactionException,
            NotPermittedException
    {
        try {
            internalBless( types, false, false, false, 0L );
        } catch( IsAbstractException ex ) {
            log.error( ex );
        }
    }

    /**
     * Purge this MeshObject. This must only be invoked by our MeshObjectLifecycleManager
     * and thus is defined down here, not higher up in the inheritance hierarchy.
     * 
     * @throws TransactionException thrown if invoked outside of proper transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void purge()
        throws
            TransactionException,
            NotPermittedException
    {
        if( theMeshBase == null ) {
            // this is a loop, do nothing
            return;
        }
        NetMeshBase oldMeshBase = (NetMeshBase) theMeshBase;

        NetMeshObjectIdentifier identifier = getIdentifier();
        
        theMeshBase            = null; // this needs to happen rather late so the other code still works
        theProxies             = null;
        theRelationshipProxies = null;
        
        firePurged( oldMeshBase, identifier, System.currentTimeMillis() );
    }
    
    /**
      * Tell the NetMeshObject to make a note of the fact that it is a replica of the
      * NetMeshObject that exists in the direction of the provided Proxy.
      * This shall not be called by the application
      * programmer. This is called only by Proxies that identify themselves to this call.
      *
      * @param theProxy the Proxy invoking this method
      */
    public void proxyInternalMakeReplicaFrom(
            Proxy theProxy )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "makeReplicaFrom", theProxy );
        }

        synchronized( theIdentifier ) {
            if( theProxies == null ) {
                theProxies = new Proxy[] { theProxy };
                theHomeProxyIndex        = 0;
                theProxyTowardsLockIndex = 0;

            } else {
                int foundIndex = -1;
                for( int i=0 ; i<theProxies.length ; ++i ) {
                    if( theProxy == theProxies[i] ) {
                        foundIndex = i;
                        break;
                    }
                }
                if( foundIndex < 0 ) {
                    theProxies = ArrayHelper.append( theProxies, theProxy, Proxy.class );
                    foundIndex = theProxies.length-1;
                }
                theHomeProxyIndex        = foundIndex;
                theProxyTowardsLockIndex = foundIndex;
            }
        }
        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;
        realBase.flushMeshObject( this );
    }
    
    /**
     * Bless a replica NetMeshObject, as a consequence of the blessing of a master replica.
     *
     * @param types the to-be-blessed EntityTypes
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws EntityBlessedAlreadyException thrown if this MeshObject is already blessed with one or more of the EntityTypes
     * @throws IsAbstractException thrown if one or more of the EntityTypes were abstract and could not be instantiated
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void rippleBless(
            EntityType [] types,
            long          timeUpdated )
        throws
            EntityBlessedAlreadyException,
            IsAbstractException,
            TransactionException,
            NotPermittedException
    {
        try {
            internalBless( types, false, false, true, timeUpdated );         

        } catch( IsDeadException ex ) {
            if( log.isDebugEnabled()) {
                log.debug( ex );
            }
        }
    }

    /**
     * Unbless a replica NetMeshObject, as a consequence of the unblessing of a master replica.
     *
     * @param types the to-be-unblessed EntityTypes
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws RoleTypeRequiresEntityTypeException thrown if this MeshObject plays one or more roles that requires the MeshObject to remain being blessed with at least one of the EntityTypes
     * @throws EntityNotBlessedException thrown if this MeshObject does not support at least one of the given EntityTypes
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void rippleUnbless(
            EntityType [] types,
            long          timeUpdated )
        throws
            RoleTypeRequiresEntityTypeException,
            EntityNotBlessedException,
            TransactionException,
            NotPermittedException
    {
        try {
            internalUnbless( types, false, timeUpdated );

        } catch( IsDeadException ex ) {
            if( log.isDebugEnabled()) {
                log.debug( ex );
            }
        }
    }

    /**
     * Relate two replica NetMeshObjects, as a consequence of relating other replicas.
     * 
     * @param newNeighborIdentifier the identifier of the NetMeshObject to relate to
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws RelatedAlreadyException thrown to indicate that this MeshObject is already related
     *         to the newNeighbor
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     */
    public synchronized void rippleRelate(
            NetMeshObjectIdentifier newNeighborIdentifier,
            long                    timeUpdated )
        throws
            RelatedAlreadyException,
            TransactionException
    {
        AnetMeshObjectNeighborManager nMgr = getNeighborManager();

        // first see whether we have it already
        boolean found = false;

        NetMeshObjectIdentifier [] oldNeighborIdentifiers;
        if( nMgr.hasNeighbors( this )) {
            oldNeighborIdentifiers = nMgr.getNeighborIdentifiers( this );
            for( int i=0 ; i<oldNeighborIdentifiers.length ; ++i ) {
                if( newNeighborIdentifier.equals( oldNeighborIdentifiers[i] )) {
                    found = true;
                    break;
                }
            }
        } else {
            oldNeighborIdentifiers = new NetMeshObjectIdentifier[]{};
        }

        if( !found ) {
            nMgr.appendNeighbor( this, newNeighborIdentifier, null );
            fireNeighborAdded( null, oldNeighborIdentifiers, newNeighborIdentifier, nMgr.getNeighborIdentifiers( this ), theMeshBase );
        }
        
        NetMeshObject neighbor = ((NetMeshBase)theMeshBase).findMeshObjectByIdentifier( newNeighborIdentifier );
        if( neighbor != null ) {
            AnetMeshObject realNeighbor = (AnetMeshObject) neighbor;

            boolean neighborFound = false;

            NetMeshObjectIdentifier [] oldNeighborNeighborIdentifiers;
            if( nMgr.hasNeighbors( realNeighbor )) {
                oldNeighborNeighborIdentifiers = nMgr.getNeighborIdentifiers( realNeighbor );
                for( int i=0 ; i<oldNeighborNeighborIdentifiers.length ; ++i ) {
                    if( theIdentifier.equals( oldNeighborNeighborIdentifiers[i] )) {
                        neighborFound = true;
                        break;
                    }
                }
            } else {
                oldNeighborNeighborIdentifiers = new NetMeshObjectIdentifier[]{};
            }

            if( !neighborFound ) {
                nMgr.appendNeighbor( realNeighbor, theIdentifier, null );
                realNeighbor.fireNeighborAdded( null, oldNeighborNeighborIdentifiers, theIdentifier, nMgr.getNeighborIdentifiers( realNeighbor ), theMeshBase );
            }
        }
        
        updateLastUpdated( timeUpdated, theTimeUpdated );
    }
    
    /**
     * Unrelate two replica NetMeshObjects, as a consequence of unrelating other replicas.
     * 
     * @param neighborIdentifier the identifier of the NetMeshObject to unrelate from
     * @param mb the MeshBase that this MeshObject does or used to belong to
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws NotRelatedException thrown if this MeshObject is not related to the neighbor
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void rippleUnrelate(
            NetMeshObjectIdentifier neighborIdentifier,
            NetMeshBase             mb,
            long                    timeUpdated )
        throws
            NotRelatedException,
            TransactionException,
            NotPermittedException
    {
        internalUnrelate( neighborIdentifier, mb, false, timeUpdated );
    }

    /**
     * Bless the relationship of two replica NetMeshObjects, as a consequence of blessing the relationship
     * of two other replicas.
     * 
     * @param theTypes the RoleTypes to use for blessing
     * @param neighborIdentifier the identifier of the NetMeshObject that
     *        identifies the relationship that shall be blessed
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws RoleTypeBlessedAlreadyException thrown if the relationship to the other MeshObject is blessed
     *         already with one ore more of the given RoleTypes
     * @throws EntityNotBlessedException thrown if this MeshObject is not blessed by a requisite EntityType
     * @throws NotRelatedException thrown if this MeshObject is not currently related to otherObject
     * @throws IsAbstractException thrown if one of the RoleTypes belong to an abstract RelationshipType
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void rippleBless(
            RoleType []             theTypes,
            NetMeshObjectIdentifier neighborIdentifier,
            long                    timeUpdated )
        throws
            RoleTypeBlessedAlreadyException,
            EntityNotBlessedException,
            NotRelatedException,
            IsAbstractException,
            TransactionException,
            NotPermittedException
    {
        // FIXME: this does not seem to throw some of the declared exceptions, and that
        // seems rather wrong ...

        AnetMeshObjectNeighborManager nMgr = getNeighborManager();

        RoleType []         oldRoleTypesHere = nMgr.getRoleTypesFor( this, neighborIdentifier ); // throws NotRelatedException
        ArrayList<RoleType> toAddHere        = new ArrayList<RoleType>();

        if( oldRoleTypesHere != null && oldRoleTypesHere.length > 0 ) {
            boolean foundRole = false;

            for( int i=0 ; i<theTypes.length ; ++i ) {
                for( int j=0 ; j<oldRoleTypesHere.length ; ++j ) {
                    if( theTypes[i].equals( oldRoleTypesHere[j] )) {
                        foundRole = true;
                        break;
                    }
                }
                if( !foundRole ) {
                    toAddHere.add( theTypes[i] );
                }
            }

        } else {
            for( RoleType current : theTypes ) {
                toAddHere.add( current );
            }
        }

        RoleType [] addedHere = ArrayHelper.copyIntoNewArray( toAddHere, RoleType.class );
        nMgr.appendRoleTypes( this, neighborIdentifier, addedHere );

        AnetMeshObject neighbor   = (AnetMeshObject) theMeshBase.findMeshObjectByIdentifier( neighborIdentifier );
        RoleType []    addedThere = null;
        RoleType []    oldRoleTypesThere = null;

        if( neighbor != null ) {
            oldRoleTypesThere              = nMgr.getRoleTypesFor( neighbor, theIdentifier );
            ArrayList<RoleType> toAddThere = new ArrayList<RoleType>();

            if( oldRoleTypesThere != null && oldRoleTypesThere.length > 0 ) {
                boolean foundRole = false;

                for( int i=0 ; i<theTypes.length ; ++i ) {
                    for( int j=0 ; j<oldRoleTypesThere.length ; ++j ) {
                        if( theTypes[i].getInverseRoleType().equals( oldRoleTypesThere[j] )) {
                            foundRole = true;
                            break;
                        }
                    }
                    if( !foundRole ) {
                        toAddThere.add( theTypes[i].getInverseRoleType());
                    }
                }
            } else {
                for( RoleType current : theTypes ) {
                    toAddThere.add( current.getInverseRoleType() );
                }
            }

            addedThere = ArrayHelper.copyIntoNewArray( toAddThere, RoleType.class );
            nMgr.appendRoleTypes( neighbor, theIdentifier, addedThere );
        }


        if( addedHere.length > 0 || ( addedThere != null && addedThere.length > 0 )) {
            updateLastUpdated( timeUpdated, theTimeUpdated );
        }

        if( addedHere.length > 0 ) {
            fireTypesAdded( oldRoleTypesHere, addedHere, nMgr.getRoleTypesFor( this, neighborIdentifier ), neighborIdentifier, theMeshBase );
        }
        if( neighbor != null && addedThere != null && addedThere.length > 0 ) {
            neighbor.fireTypesAdded( oldRoleTypesThere, addedThere, nMgr.getRoleTypesFor( neighbor, theIdentifier ), theIdentifier, theMeshBase );
        }
    }

    /**
     * Unbless the relationship of two replica NetMeshObjects, as a consequence of unblessing the relationship
     * of two other replicas.
     * 
     * @param theTypes the RoleTypes to use for unblessing
     * @param neighborIdentifier the identifier of the NetMeshObject that
     *        identifies the relationship that shall be unblessed
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws RoleTypeNotBlessedException thrown if the relationship to the other MeshObject does not support the RoleType
     * @throws NotRelatedException thrown if this MeshObject is not currently related to otherObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void rippleUnbless(
            RoleType []             theTypes,
            NetMeshObjectIdentifier neighborIdentifier,
            long                    timeUpdated )
        throws
            RoleTypeNotBlessedException,
            NotRelatedException,
            TransactionException,
            NotPermittedException
    {
        internalUnbless( theTypes, neighborIdentifier, false, timeUpdated );
    }

    /**
     * Add a replica NetMeshObject as an equivalent, as a consequence of adding a different replica
     * as equivalent.
     * 
     * @param identifierOfEquivalent the Identifier of the replica NetMeshObject
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws EquivalentAlreadyException thrown if the provided MeshObject is already an equivalent of this MeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void rippleAddAsEquivalent(
            NetMeshObjectIdentifier identifierOfEquivalent,
            long                    timeUpdated )
        throws
            EquivalentAlreadyException,
            TransactionException,
            NotPermittedException
    {
        internalAddAsEquivalent( identifierOfEquivalent, false, timeUpdated );
    }
    
    /**
     * Remove this replica NetMeshObject as an equivalent from the current set of equivalents, as a consequence of removing
     * a different replica as equivalent.
     * 
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void rippleRemoveAsEquivalent(
            long timeUpdated )
        throws
            TransactionException,
            NotPermittedException
    {
        internalRemoveAsEquivalent( false, timeUpdated );
    }

    /**
     * Change the values of Properties on a replica NetMeshObject, as a consequence of changing the values of the properties
     * in another replica.
     *
     * @param types the PropertyTypes
     * @param values the new values, in the same sequence as the PropertyTypes
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws IllegalPropertyTypeException thrown if one PropertyType does not exist on this MeshObject
     *         because the MeshObject has not been blessed with a MeshType that provides this PropertyType
     * @throws IllegalPropertyValueException thrown if the new value is an illegal value for this Property
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void rippleSetPropertyValues(
            PropertyType []  types,
            PropertyValue [] values,
            long             timeUpdated )
        throws
            IllegalPropertyTypeException,
            NotPermittedException,
            IllegalPropertyValueException,
            TransactionException
    {
        internalSetPropertyValues( types, values, false, timeUpdated );
    }
    
    /**
     * Change the values of Properties on a replica, as a consequence of changing the value of the property
     * in another replica.
     *
     * @param map the Map of PropertyTypes to PropertyValues
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws IllegalPropertyTypeException thrown if one PropertyType does not exist on this MeshObject
     *         because the MeshObject has not been blessed with a MeshType that provides this PropertyType
     * @throws IllegalPropertyValueException thrown if the new value is an illegal value for this Property
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void rippleSetPropertyValues(
            Map<PropertyType,PropertyValue> map,
            long                            timeUpdated )
        throws
            IllegalPropertyTypeException,
            NotPermittedException,
            IllegalPropertyValueException,
            TransactionException
    {
        // FIXME not the world's most efficient algorithm
        PropertyType []  types  = new PropertyType[ map.size() ];
        PropertyValue [] values = new PropertyValue[ types.length ];
        
        int i=0;
        for( PropertyType currentType : map.keySet() ) {
            PropertyValue currentValue = map.get( currentType );
            types[i]  = currentType;
            values[i] = currentValue;
            ++i;
        }
        internalSetPropertyValues( types, values, false, timeUpdated );
    }
    
    /**
     * Delete a replica NetMeshObject as a consequence of deleting another replica.
     * 
     * @param mb the MeshBase that this MeshObject does or used to belong to
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void rippleDelete(
            NetMeshBase mb,
            long        timeUpdated )
        throws
            TransactionException,
            NotPermittedException
    {
        internalDelete( false, timeUpdated );
    }

    /**
     * Resynchronize a replica.
     * 
     * @param timeCreated the timeCreated to use
     * @param timeUpdated the timeUpdated to use
     * @param timeRead the timeRead to use
     * @param timeExpires the timeExpires to use
     * @param correctProxies the Proxies to use
     * @param correctHomeProxyIndex the index into the correctProxies that identifies the home proxy, or -1 if none
     * @param correctProxyTowardsLockIndex the index into the correctProxies that identifies the proxy towards lock, or -1 if none
     */
    public void proxyOnlyResynchronizeReplica(
            long     timeCreated,
            long     timeUpdated,
            long     timeRead,
            long     timeExpires,
            Proxy [] correctProxies,
            int      correctHomeProxyIndex,
            int      correctProxyTowardsLockIndex )
    {
        theTimeCreated = timeCreated;
        theTimeUpdated = timeUpdated;
        theTimeRead    = timeRead;
        theTimeExpires = timeExpires;

        theProxies = correctProxies;
        
        theHomeProxyIndex        = correctHomeProxyIndex;
        theProxyTowardsLockIndex = correctProxyTowardsLockIndex;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    @Override
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theIdentifier",
                    "theTimeCreated",
                    "theTimeUpdated",
                    "theTimeRead",
                    "theTimeExpires",
                    "types",
                    "neighbors",
                    "neighborRoleTypes",
                    "theMeshBase.getIdentifier()"
                },
                new Object[] {
                    theIdentifier,
                    theTimeCreated,
                    theTimeUpdated,
                    theTimeRead,
                    theTimeExpires,
                    theMeshTypes != null ? theMeshTypes.keySet() : null,
                    theNeighborIdentifiers,
                    theNeighborRoleTypes,
                    theMeshBase != null ? theMeshBase.getIdentifier().toExternalForm() : null
                });
    }
    
    /**
     * This simply invokes the superclass method. It is replicated here in order to
     * get around the restrictions of the <tt>protected</tt> keyword without
     * having to make it public.
     *
     * @param thePropertyTypes the sequence of PropertyTypes to set
     * @param newValues the sequence of PropertyValues for the PropertyTypes
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @param isMaster if true, check permissions
     * @return the old values of the Properties
     * @throws IllegalPropertyTypeException thrown if one PropertyType does not exist on this MeshObject
     *         because the MeshObject has not been blessed with a MeshType that provides this PropertyType
     * @throws IllegalPropertyValueException thrown if the new value is an illegal value for this Property
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     */
    @Override
    public PropertyValue [] internalSetPropertyValues(
            PropertyType []  thePropertyTypes,
            PropertyValue [] newValues,
            boolean          isMaster,
            long             timeUpdated )
        throws
            IllegalPropertyTypeException,
            NotPermittedException,
            IllegalPropertyValueException,
            TransactionException
    {
        return super.internalSetPropertyValues( thePropertyTypes, newValues, isMaster, timeUpdated );
    }

    /**
     * Helper method to determine the NetMeshBaseIdentifier of the incoming Proxy, if any.
     * 
     * @param mb the MeshBase to use
     * @return the NetMeshBaseIdentifier the incoming Proxy, if any
     */
    protected NetMeshBaseIdentifier determineIncomingProxyIdentifier(
            MeshBase mb )
    {
        Proxy p = ((NetMeshBase)mb).determineIncomingProxy();

        NetMeshBaseIdentifier ret;
        if( p != null ) {
             ret = p.getPartnerMeshBaseIdentifier();
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Fire an event that indicates that this replica gained update rights.
     */
    protected void fireLockGainedEvent()
    {
        LockChangedEvent.GainedLock theEvent = new LockChangedEvent.GainedLock( this, System.currentTimeMillis() );

        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;
        realBase.flushMeshObject( this );

        firePropertyChange( theEvent );
    }

    /**
     * Fire an event that indicates that this replica lost update rights.
     */
    protected void fireLockLostEvent()
    {
        LockChangedEvent.LostLock theEvent = new LockChangedEvent.LostLock( this, System.currentTimeMillis() );

        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;
        realBase.flushMeshObject( this );

        firePropertyChange( theEvent );
    }

    /**
     * Fire an event that indicates that this replica gained home replica status.
     */
    protected void fireHomeReplicaGainedEvent()
    {
        HomeReplicaChangedEvent.GainedHomeReplica theEvent
                = new HomeReplicaChangedEvent.GainedHomeReplica( this, System.currentTimeMillis() );

        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;
        realBase.flushMeshObject( this );

        firePropertyChange( theEvent );
    }

    /**
     * Fire an event that indicates that this replica lost home replica status.
     */
    protected void fireHomeReplicaLostEvent()
    {
        HomeReplicaChangedEvent.LostHomeReplica theEvent
                = new HomeReplicaChangedEvent.LostHomeReplica( this, System.currentTimeMillis() );

        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;
        realBase.flushMeshObject( this );

        firePropertyChange( theEvent );
    }
    
    /**
     * Fire an event indicating that this MeshObject was purged.
     * We pass in the MeshBase, because the member variable has already been zero'd.
     * 
     * @param oldMeshBase the MeshBase this MeshObject used to belong to
     * @param canonicalIdentifier the canonical identifier that this MeshObject used to have
     * @param timeEventOccurred the time at which the event occurred, in System.currentTimeMillis() format
     */
    protected void firePurged(
            NetMeshBase             oldMeshBase,
            NetMeshObjectIdentifier canonicalIdentifier,
            long                    timeEventOccurred )
    {
        MeshObjectStateEvent theEvent = new NetMeshObjectBecamePurgedStateEvent(
                this,
                canonicalIdentifier,
                determineIncomingProxyIdentifier( oldMeshBase ),
                timeEventOccurred );
        
        oldMeshBase.getCurrentTransaction().addChange( theEvent );
        
        firePropertyChange( theEvent );
    }

    /**
      * Fire an event indicating a change of a property of this MeshObject.
      *
      * @param thePropertyType the PropertyType whose value changed
      * @param oldValue the value of the PropertyValue prior to the change
      * @param newValue the value of the PropertyValue now, after the change
      * @param mb the MeshBase to use
      */
    @Override
    protected void firePropertyChange(
            PropertyType  thePropertyType,
            PropertyValue oldValue,
            PropertyValue newValue,
            MeshBase      mb )
    {
        NetMeshObjectPropertyChangeEvent theEvent
                = new NetMeshObjectPropertyChangeEvent(
                        this,
                        thePropertyType,
                        oldValue,
                        newValue,
                        determineIncomingProxyIdentifier( theMeshBase ),
                        theTimeUpdated );

        mb.getCurrentTransaction().addChange( theEvent );

        firePropertyChange( theEvent );
    }

    /**
     * Fire an event indicating a change in the set of neighbors of this MeshObject.
     * 
     * @param addedRoleTypes the newly added RoleTypes, if any
     * @param oldValue the MeshObjectIdentifiers of the neighbors prior to the change
     * @param added the added MeshObjectIdentifier
     * @param newValue the MeshObjectIdentifiers of the neighbors now, after the change
     * @param mb the MeshBase to use
     */
    @Override
    protected void fireNeighborAdded(
            RoleType []             addedRoleTypes,
            MeshObjectIdentifier [] oldValue,
            MeshObjectIdentifier    added,
            MeshObjectIdentifier [] newValue,
            MeshBase                mb )
    {
        NetMeshObjectNeighborAddedEvent theEvent
                = new NetMeshObjectNeighborAddedEvent(
                        this,
                        addedRoleTypes,
                        (NetMeshObjectIdentifier []) oldValue, // copyIntoNetMeshObjectIdentifierArray( oldValue ),
                        (NetMeshObjectIdentifier) added,
                        (NetMeshObjectIdentifier []) newValue, // copyIntoNetMeshObjectIdentifierArray( newValue ),
                        determineIncomingProxyIdentifier( theMeshBase ),
                        theTimeUpdated );

        mb.getCurrentTransaction().addChange( theEvent );

        firePropertyChange( theEvent );
    }

    /**
      * Fire an event indicating a change in the set of neighbors of this MeshObject.
      *
      * @param oldValue the MeshObjectIdentifier of the neighbors prior to the change
      * @param removed the removed Identifier
      * @param newValue the MeshObjectIdentifier of the neighbors now, after the change
      * @param mb the MeshBase to use
      */
    @Override
    protected void fireNeighborRemoved(
            MeshObjectIdentifier [] oldValue,
            MeshObjectIdentifier    removed,
            MeshObjectIdentifier [] newValue,
            MeshBase                mb )
    {
        NetMeshObjectNeighborRemovedEvent theEvent
                = new NetMeshObjectNeighborRemovedEvent(
                        this,
                        (NetMeshObjectIdentifier []) oldValue, // copyIntoNetMeshObjectIdentifierArray( oldValue ),
                        (NetMeshObjectIdentifier) removed,
                        (NetMeshObjectIdentifier []) newValue, // copyIntoNetMeshObjectIdentifierArray( newValue ),
                        determineIncomingProxyIdentifier( theMeshBase ),
                        theTimeUpdated );

        mb.getCurrentTransaction().addChange( theEvent );

        firePropertyChange( theEvent );
    }

    /**
     * Fire an event indicating that one or more MeshTypes have been added to this MeshObject.
     *
     * @param oldTypes the EntityTypes prior to the change
     * @param addedTypes the added MeshTypes
     * @param newTypes the EntityTypes now, after the change
     * @param mb the MeshBase to use
     */
    @Override
    protected void fireTypesAdded(
            EntityType [] oldTypes,
            EntityType [] addedTypes,
            EntityType [] newTypes,
            MeshBase      mb )
    {
        NetMeshObjectTypeAddedEvent theEvent
                = new NetMeshObjectTypeAddedEvent(
                        this,
                        oldTypes,
                        addedTypes,
                        newTypes,
                        determineIncomingProxyIdentifier( theMeshBase ),
                        theTimeUpdated );

        mb.getCurrentTransaction().addChange( theEvent );

        firePropertyChange( theEvent );
    }

    /**
     * Fire an event indicating that one or more MeshTypes have been removed from this MeshObject.
     *
     * @param oldTypes the EntityTypes prior to the change
     * @param removedTypes the removed MeshTypes
     * @param newTypes the EntityTypes now, after the change
     * @param mb the MeshBase to use
     */
    @Override
    protected void fireTypesRemoved(
            EntityType [] oldTypes,
            EntityType [] removedTypes,
            EntityType [] newTypes,
            MeshBase      mb )
    {
        NetMeshObjectTypeRemovedEvent theEvent
                = new NetMeshObjectTypeRemovedEvent(
                        this,
                        oldTypes,
                        removedTypes,
                        newTypes,
                        determineIncomingProxyIdentifier( theMeshBase ),
                        theTimeUpdated );

        mb.getCurrentTransaction().addChange( theEvent );

        firePropertyChange( theEvent );
    }

    /**
     * Fire an event indicating that one or more RoleTypes were added to the relationship of this
     * MeshObject to another MeshObject.
     *
     * @param oldRoleTypes the RoleTypes prior to the change
     * @param addedRoleTypes the RoleTypes that were added
     * @param newRoleTypes the RoleTypes now, after the change
     * @param identifierOfOtherSide the Identifier of the other side of this relationship
     * @param mb the MeshBase to use
     */
    @Override
    protected void fireTypesAdded(
            RoleType []          oldRoleTypes,
            RoleType []          addedRoleTypes,
            RoleType []          newRoleTypes,
            MeshObjectIdentifier identifierOfOtherSide,
            MeshBase             mb )
    {
        NetMeshObjectRoleAddedEvent theEvent
                = new NetMeshObjectRoleAddedEvent(
                        this,
                        oldRoleTypes,
                        addedRoleTypes,
                        newRoleTypes,
                        (NetMeshObjectIdentifier) identifierOfOtherSide,
                        determineIncomingProxyIdentifier( theMeshBase ),
                        theTimeUpdated );

        mb.getCurrentTransaction().addChange( theEvent );

        firePropertyChange( theEvent );
    }

    /**
     * Fire an event indicating that one or more RoleTypes wwere removed from the relationship of this
     * MeshObject to another MeshObject.
     *
     * @param oldRoleTypes the RoleTypes prior to the change
     * @param removedRoleTypes the RoleTypes that were removed
     * @param newRoleTypes the RoleTypes now, after the change
     * @param neighborIdentifier identifier of the other side of this relationship
     * @param mb the MeshBase to use
     */
    @Override
    protected void fireTypesRemoved(
            RoleType []          oldRoleTypes,
            RoleType []          removedRoleTypes,
            RoleType []          newRoleTypes,
            MeshObjectIdentifier neighborIdentifier,
            MeshBase             mb )
    {
        NetMeshObjectRoleRemovedEvent theEvent
                = new NetMeshObjectRoleRemovedEvent(
                        this,
                        oldRoleTypes,
                        removedRoleTypes,
                        newRoleTypes,
                        (NetMeshObjectIdentifier) neighborIdentifier,
                        determineIncomingProxyIdentifier( theMeshBase ),
                        theTimeUpdated );

        mb.getCurrentTransaction().addChange( theEvent );

        firePropertyChange( theEvent );
    }

    /**
     * Fire an event indicating that this MeshObject was deleted.
     * We pass in the MeshBase, because the member variable has already been zero'd.
     * 
     * @param oldMeshBase the MeshBase this MeshObject used to belong to
     * @param canonicalMeshObjectName the canonical Identifier that this MeshObject used to have
     * @param time the time at which this change occurred
     */
    @Override
    protected void fireDeleted(
            MeshBase             oldMeshBase,
            MeshObjectIdentifier canonicalMeshObjectName,
            long                 time )
    {
        NetMeshObjectBecameDeadStateEvent theEvent
                = new NetMeshObjectBecameDeadStateEvent(
                        this,
                        (NetMeshObjectIdentifier) canonicalMeshObjectName,
                        determineIncomingProxyIdentifier( oldMeshBase ),
                        time );
        
        // Let's not insert it into the transaction any more, there is no point of having
        // both MeshObjectBecameDeadStateEvent and MeshObjectDeletedEvent
        //
        // oldMeshBase.getCurrentTransaction().addChange( theEvent );
        
        firePropertyChange( theEvent );
    }

    /**
     * Find a manager for the MeshObject's neighbors.
     *
     * @return the AMeshObjectNeighborManager
     */
    @Override
    public AnetMeshObjectNeighborManager getNeighborManager()
    {
        return AnetMeshObjectNeighborManager.SINGLETON_NET;
    }

    /**
     * Helper method to create an array of MeshObjectIdentifier.
     * This may be overridden in subclasses.
     *
     * @param n the desired size of the array
     * @return the array
     */
    @Override
    protected NetMeshObjectIdentifier [] createMeshObjectIdentifierArray(
            int n )
    {
        return new NetMeshObjectIdentifier[ n ];
    }

    /**
     * Helper method to create an array of array of MeshObjectIdentifier.
     * This may be overridden in subclasses.
     *
     * @param n the desired size of the array
     * @return the array
     */
    @Override
    protected NetMeshObjectIdentifier [][] createMeshObjectIdentifierArrayArray(
            int n )
    {
        return new NetMeshObjectIdentifier[ n ][];
    }

    /**
     * Flag indicating our willingness to give up the lock when asked.
     */
    protected boolean theGiveUpLock = false;

    /**
     * Flag indicating our willingness to give up home replica status when asked.
     */
    protected boolean theGiveUpHomeReplica = false;

    /**
     * The Proxies to other NetworkedMeshBases that contain the replicas that are
     * closest in the replication graph. This may be null.
     */
    protected Proxy [] theProxies;

    /**
     * The index into theProxies that represents our home Proxy. If this is HERE_CONSTANT, it
     * indicates that this is the home replica.
     */
    protected int theHomeProxyIndex = HERE_CONSTANT;
    
    /**
     * The index into theProxies that represents the Proxy towards the lock. If this
     * is HERE_CONSTANT, it indicates that this replica has the lock.
     */
    protected int theProxyTowardsLockIndex = HERE_CONSTANT;

    /**
     * The table of Proxies through which we learned of relationships. This has the same
     * length as the relationship table in the superclass, and is indexed the same way.
     * If two Proxies are listed in this table for a given relationship, it means that
     * we know that the replicas in the direction of these Proxies agree that this MeshObject
     * has the given relationship.
     */
    protected Proxy [][] theRelationshipProxies;

    /** 
     * Special value indicating this replica (instead of another, reached through a Proxy).
     */
    public static final int HERE_CONSTANT = -1;
}
