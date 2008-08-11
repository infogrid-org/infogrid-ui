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

package org.infogrid.meshbase.store.net;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.mesh.set.m.ImmutableMMeshObjectSetFactory;
import org.infogrid.meshbase.IterableMeshBase;
import org.infogrid.meshbase.IterableMeshBaseDifferencer;
import org.infogrid.meshbase.Sweeper;
import org.infogrid.meshbase.net.proxy.DefaultProxyFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.NetSweeper;
import org.infogrid.meshbase.net.a.AnetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.meshbase.sweeper.SweepStep;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.store.IterableStore;
import org.infogrid.store.util.IterableStoreBackedSwappingHashMap;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * A NetStoreMeshBase that is also iterable.
 */
public class IterableNetStoreMeshBase
        extends
            NetStoreMeshBase
        implements
            IterableMeshBase
{
    private static final Log log = Log.getLogInstance( IterableNetStoreMeshBase.class ); // our own, private logger

    /**
     * Factory method.
     * 
     * @param identifier the NetMeshBaseIdentifier of the to-be-created NetMeshBase
     * @param endpointFactory the MessageEndpointFactory to use for proxy communication
     * @param modelBase the ModelBase with the type definitions we use
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param context the Context in which this MeshBase will run
     * @return IterableNetStoreMeshBase the created IterableNetStoreMeshBase
     * @throws IsAbstractException thrown if the given EntityType for the home object is abstract and cannot be instantiated
     */
    public static IterableNetStoreMeshBase create(
            NetMeshBaseIdentifier     identifier,
            ProxyMessageEndpointFactory endpointFactory,
            ModelBase                 modelBase,
            NetAccessManager          accessMgr,
            IterableStore             meshObjectStore,
            IterableStore             proxyStore,
            Context                   context )
    {
        ImmutableMMeshObjectSetFactory    setFactory         = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();

        IterableNetStoreMeshBase ret = IterableNetStoreMeshBase.create(
                identifier,
                endpointFactory,
                proxyPolicyFactory,
                setFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                context );

        return ret;
    }

    /**
     * Factory method.
     * 
     * @param identifier the NetMeshBaseIdentifier of the to-be-created NetMeshBase
     * @param endpointFactory the MessageEndpointFactory to use for proxy communication
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param modelBase the ModelBase with the type definitions we use
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param context the Context in which this MeshBase will run
     * @return IterableNetStoreMeshBase the created IterableNetStoreMeshBase
     * @throws IsAbstractException thrown if the given EntityType for the home object is abstract and cannot be instantiated
     */
    public static IterableNetStoreMeshBase create(
            NetMeshBaseIdentifier     identifier,
            ProxyMessageEndpointFactory endpointFactory,
            ProxyPolicyFactory        proxyPolicyFactory,
            ModelBase                 modelBase,
            NetAccessManager          accessMgr,
            IterableStore             meshObjectStore,
            IterableStore             proxyStore,
            Context                   context )
    {
        ImmutableMMeshObjectSetFactory setFactory = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );

        IterableNetStoreMeshBase ret = IterableNetStoreMeshBase.create(
                identifier,
                endpointFactory,
                proxyPolicyFactory,
                setFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                context );

        return ret;
    }

    /**
     * Factory method.
     * 
     * @param identifier the NetMeshBaseIdentifier of the to-be-created NetMeshBase
     * @param endpointFactory the MessageEndpointFactory to use for proxy communication
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase with the type definitions we use
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param context the Context in which this MeshBase will run
     * @return IterableNetStoreMeshBase the created IterableNetStoreMeshBase
     */
    public static IterableNetStoreMeshBase create(
            NetMeshBaseIdentifier     identifier,
            ProxyMessageEndpointFactory endpointFactory,
            ProxyPolicyFactory        proxyPolicyFactory,
            MeshObjectSetFactory      setFactory,
            ModelBase                 modelBase,
            NetAccessManager          accessMgr,
            IterableStore             meshObjectStore,
            IterableStore             proxyStore,
            Context                   context )
    {
        DefaultProxyFactory proxyFactory = DefaultProxyFactory.create( endpointFactory, proxyPolicyFactory );

        NetStoreMeshBaseEntryMapper objectMapper = new NetStoreMeshBaseEntryMapper();
        StoreProxyEntryMapper       proxyMapper  = new StoreProxyEntryMapper( proxyFactory );
        
        IterableStoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject> objectStorage = IterableStoreBackedSwappingHashMap.createWeak( objectMapper, meshObjectStore );
        IterableStoreBackedSwappingHashMap<NetMeshBaseIdentifier,Proxy>     proxyStorage  = IterableStoreBackedSwappingHashMap.createWeak( proxyMapper,  proxyStore );

        NetMeshObjectIdentifierFactory identifierFactory = DefaultAnetMeshObjectIdentifierFactory.create( identifier );
        AnetMeshBaseLifecycleManager   life              = AnetMeshBaseLifecycleManager.create();

        StoreProxyManager proxyManager = StoreProxyManager.create( proxyFactory, proxyStorage );

        IterableNetStoreMeshBase ret = new IterableNetStoreMeshBase(
                identifier,
                identifierFactory,
                setFactory,
                modelBase,
                life,
                accessMgr,
                objectStorage,
                proxyManager,
                context );

        setFactory.setMeshBase( ret );
        objectMapper.setMeshBase( ret );
        proxyMapper.setMeshBase( ret );
        proxyFactory.setNetMeshBase( ret );
        ret.initializeHomeObject();
        
        if( log.isDebugEnabled() ) {
            log.debug( "created " + ret );
        }
        return ret;
    }

    /**
     * Constructor.
     * 
     * @param identifier the NetMeshBaseIdentifier of the to-be-created NetMeshBase
     * @param identifierFactory the factory for NetMeshObjectIdentifiers appropriate for this NetMeshBase
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase with the type definitions we use
     * @param life the MeshBaseLifecycleManager to use
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param cache the in-memory cache to use
     * @param proxyManager the ProxyManager used by this NetMeshBase
     * @param context the Context in which this MeshBase will run
     */
    protected IterableNetStoreMeshBase(
            NetMeshBaseIdentifier                                   identifier,
            NetMeshObjectIdentifierFactory                          identifierFactory,
            MeshObjectSetFactory                                    setFactory,
            ModelBase                                               modelBase,
            AnetMeshBaseLifecycleManager                            life,
            NetAccessManager                                        accessMgr,
            IterableStoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject> cache,
            StoreProxyManager                                       proxyManager,
            Context                                                 context )
    {
        super( identifier, identifierFactory, setFactory, modelBase, life, accessMgr, cache, proxyManager, context );
    }

    /**
     * Obtain an Iterator over all MeshObjects in the Store.
     *
     * @return the Iterator
     */
    public CursorIterator<MeshObject> iterator()
    {
        return getCachingMap().valuesIterator( null, MeshObject.class );
    }
    
    /**
     * Obtain an Iterator over all MeshObjects in the Store.
     *
     * @return the Iterator
     */
    public CursorIterator<MeshObject> getIterator()
    {
        return iterator();
    }

    /**
     * Determine the number of MeshObjects in this MeshBase.
     *
     * @return the number of MeshObjects in this MeshBase
     */
    public int size()
    {
        try {
            return getCachingMap().getStore().size();

        } catch( IOException ex ) {
            log.error( ex );
            return 0;
        }
    }

    /**
     * Factory method for a IterableMeshBaseDifferencer, with this IterableMeshBase
     * being the comparison base.
     *
     * @return the IterableMeshBaseDifferencer
     */
    public IterableMeshBaseDifferencer getDifferencer()
    {
        return new IterableMeshBaseDifferencer( this );
    }

/**
     * Continually sweep this IterableMeshBase in the background, according to
     * the configured Sweeper.
     *
     * @param scheduleVia the ScheduledExecutorService to use for scheduling
     * @throws NullPointerException thrown if no Sweeper has been set
     */
    
    public void startBackgroundSweeping(
            ScheduledExecutorService scheduleVia )
        throws
            NullPointerException
    {
        Sweeper sweep = theSweeper;
        if( sweep == null ) {
            throw new NullPointerException();
        }
        theSweeperScheduler = scheduleVia;

        scheduleSweepStep();
    }
    
    /**
     * Stop the background sweeping.
     */
    public void stopBackgroundSweeping()
    {
        SweepStep nextStep = theNextSweepStep;
        if( nextStep == null ) {
            return;
        }
        synchronized( nextStep ) {
            nextStep.cancel();
            theNextSweepStep = null;
        }
    }
    
    /**
     * Perform a sweep on every single MeshObject in this InterableMeshBase.
     * This may take a long time; using background sweeping is almost always
     * a better alternative.
     */
    public synchronized void sweepAllNow()
    {
        Sweeper sweeper = theSweeper;
        if( sweeper == null ) {
            throw new NullPointerException();
        }
        for( MeshObject candidate : this ) {
            boolean done = sweeper.potentiallyDelete( candidate );
            if( !done && ( sweeper instanceof NetSweeper )) {
                ((NetSweeper)sweeper).potentiallyPurge( (NetMeshObject) candidate );
            }
        }
    }

    /**
     * Invoked by the SweepStep, schedule the next SweepStep.
     */
    public void scheduleSweepStep()
    {
        if( theNextSweepStep != null ) {
            theNextSweepStep = theNextSweepStep.nextStep();
        } else {
            theNextSweepStep = SweepStep.create( this );
        }
        theNextSweepStep.scheduleVia( theSweeperScheduler );
    }

    /**
     * The Scheduler for the Sweeper, if any.
     */
    protected ScheduledExecutorService theSweeperScheduler;
    
    /**
     * The next background Sweep task, if any.
     */
    protected SweepStep theNextSweepStep;
}
