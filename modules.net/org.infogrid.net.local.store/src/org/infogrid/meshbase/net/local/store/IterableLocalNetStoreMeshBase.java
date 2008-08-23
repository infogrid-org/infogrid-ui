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

package org.infogrid.meshbase.net.local.store;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.mesh.set.m.ImmutableMMeshObjectSetFactory;
import org.infogrid.meshbase.Sweeper;
import org.infogrid.meshbase.net.proxy.DefaultProxyFactory;
import org.infogrid.meshbase.net.IterableNetMeshBaseDifferencer;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.a.AnetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.local.IterableLocalNetMeshBase;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.meshbase.store.net.NetStoreMeshBaseEntryMapper;
import org.infogrid.meshbase.store.net.StoreProxyEntryMapper;
import org.infogrid.meshbase.store.net.StoreProxyManager;
import org.infogrid.meshbase.sweeper.SweepStep;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.manager.ProbeManager;
import org.infogrid.probe.manager.store.StoreScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.store.StoreShadowMeshBaseFactory;
import org.infogrid.store.IterableStore;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.store.util.IterableStoreBackedSwappingHashMap;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * An IterableNetStoreMeshBase that uses local (collocated, in this address space) ShadowMeshBases.
 */
public class IterableLocalNetStoreMeshBase
        extends
            LocalNetStoreMeshBase
        implements
            IterableLocalNetMeshBase
{
    private static final Log log = Log.getLogInstance( IterableLocalNetStoreMeshBase.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param store the single Store used for all data managed by this NetMeshBase
     * @param probeDirectory the ProbeDirectory to use
     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this NetMeshBase runs
     * @return the created IterableLocalNetStoreMeshBase
     */
    public static IterableLocalNetStoreMeshBase create(
            NetMeshBaseIdentifier    identifier,
            ModelBase                modelBase,
            NetAccessManager         accessMgr,
            IterableStore            store,
            ProbeDirectory           probeDirectory,
            long                     timeNotNeededTillExpires,
            Context                  context )
    {
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();

        return create(
                identifier,
                proxyPolicyFactory,
                modelBase,
                accessMgr,
                store,
                probeDirectory,
                timeNotNeededTillExpires,
                context );
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param store the single Store used for all data managed by this NetMeshBase
     * @param probeDirectory the ProbeDirectory to use
     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this NetMeshBase runs
     * @return the created IterableLocalNetStoreMeshBase
     */
    public static IterableLocalNetStoreMeshBase create(
            NetMeshBaseIdentifier    identifier,
            ProxyPolicyFactory       proxyPolicyFactory,
            ModelBase                modelBase,
            NetAccessManager         accessMgr,
            IterableStore            store,
            ProbeDirectory           probeDirectory,
            long                     timeNotNeededTillExpires,
            Context                  context )
    {
        IterableStore meshStore        = IterablePrefixingStore.create( "mesh",        store );
        IterableStore proxyStore       = IterablePrefixingStore.create( "proxy",       store );
        IterableStore shadowStore      = IterablePrefixingStore.create( "shadowmesh",  store );
        IterableStore shadowProxyStore = IterablePrefixingStore.create( "shadowproxy", store );

        ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

        return create(
                identifier,
                proxyPolicyFactory,
                modelBase,
                accessMgr,
                meshStore,
                proxyStore,
                shadowStore,
                shadowProxyStore,
                probeDirectory,
                exec,
                timeNotNeededTillExpires,
                context );
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param store the single Store used for all data managed by this NetMeshBase
     * @param probeDirectory the ProbeDirectory to use
     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this NetMeshBase runs
     * @return the created IterableLocalNetStoreMeshBase
     */
    public static IterableLocalNetStoreMeshBase create(
            NetMeshBaseIdentifier    identifier,
            MeshObjectSetFactory     setFactory,
            ModelBase                modelBase,
            NetAccessManager         accessMgr,
            IterableStore            store,
            ProbeDirectory           probeDirectory,
            long                     timeNotNeededTillExpires,
            Context                  context )
    {
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();

        return create(
                identifier,
                proxyPolicyFactory,
                setFactory,
                modelBase,
                accessMgr,
                store,
                probeDirectory,
                timeNotNeededTillExpires,
                context );
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param store the single Store used for all data managed by this NetMeshBase
     * @param probeDirectory the ProbeDirectory to use
     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this NetMeshBase runs
     * @return the created IterableLocalNetStoreMeshBase
     */
    public static IterableLocalNetStoreMeshBase create(
            NetMeshBaseIdentifier    identifier,
            ProxyPolicyFactory       proxyPolicyFactory,
            MeshObjectSetFactory     setFactory,
            ModelBase                modelBase,
            NetAccessManager         accessMgr,
            IterableStore            store,
            ProbeDirectory           probeDirectory,
            long                     timeNotNeededTillExpires,
            Context                  context )
    {
        IterableStore meshStore        = IterablePrefixingStore.create( "mesh",        store );
        IterableStore proxyStore       = IterablePrefixingStore.create( "proxy",       store );
        IterableStore shadowStore      = IterablePrefixingStore.create( "shadowmesh",  store );
        IterableStore shadowProxyStore = IterablePrefixingStore.create( "shadowproxy", store );

        ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

        return create(
                identifier,
                proxyPolicyFactory,
                setFactory,
                modelBase,
                accessMgr,
                meshStore,
                proxyStore,
                shadowStore,
                shadowProxyStore,
                probeDirectory,
                exec,
                timeNotNeededTillExpires,
                context );
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param shadowStore the Store in which to store the managed ShadowMeshBases
     * @param shadowProxyStore the Store in which to store the proxies of the managed ShadowMeshBases
     * @param probeDirectory the ProbeDirectory to use
     * @param exec the ScheduledExecutorService to use
     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this NetMeshBase runs
     * @return the created IterableLocalNetStoreMeshBase
     */
    public static IterableLocalNetStoreMeshBase create(
            NetMeshBaseIdentifier    identifier,
            ModelBase                modelBase,
            NetAccessManager         accessMgr,
            IterableStore            meshObjectStore,
            IterableStore            proxyStore,
            IterableStore            shadowStore,
            IterableStore            shadowProxyStore,
            ProbeDirectory           probeDirectory,
            ScheduledExecutorService exec,
            long                     timeNotNeededTillExpires,
            Context                  context )
    {
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();

        return create(
                identifier,
                proxyPolicyFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                shadowStore,
                shadowProxyStore,
                probeDirectory,
                exec,
                timeNotNeededTillExpires,
                context );
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param shadowStore the Store in which to store the managed ShadowMeshBases
     * @param shadowProxyStore the Store in which to store the proxies of the managed ShadowMeshBases
     * @param probeDirectory the ProbeDirectory to use
     * @param exec the ScheduledExecutorService to use
     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this NetMeshBase runs
     * @return the created IterableLocalNetStoreMeshBase
     */
    public static IterableLocalNetStoreMeshBase create(
            NetMeshBaseIdentifier    identifier,
            ProxyPolicyFactory       proxyPolicyFactory,
            ModelBase                modelBase,
            NetAccessManager         accessMgr,
            IterableStore            meshObjectStore,
            IterableStore            proxyStore,
            IterableStore            shadowStore,
            IterableStore            shadowProxyStore,
            ProbeDirectory           probeDirectory,
            ScheduledExecutorService exec,
            long                     timeNotNeededTillExpires,
            Context                  context )
    {
        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( exec );

        StoreShadowMeshBaseFactory delegate = StoreShadowMeshBaseFactory.create(
                modelBase,
                shadowEndpointFactory,
                probeDirectory,
                shadowStore,
                shadowProxyStore,
                timeNotNeededTillExpires,
                context );

        StoreScheduledExecutorProbeManager probeManager = StoreScheduledExecutorProbeManager.create( delegate, shadowStore );
        shadowEndpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );

        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );
        
        IterableLocalNetStoreMeshBase ret = create(
                identifier,
                proxyPolicyFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                probeManager,
                endpointFactory,
                context );
        
        probeManager.setMainNetMeshBase( ret );
        probeManager.start( exec );

        return ret;
    }
    
    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param shadowStore the Store in which to store the managed ShadowMeshBases
     * @param shadowProxyStore the Store in which to store the proxies of the managed ShadowMeshBases
     * @param probeDirectory the ProbeDirectory to use
     * @param exec the ScheduledExecutorService to use
     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this NetMeshBase runs
     * @return the created IterableLocalNetStoreMeshBase
     */
    public static IterableLocalNetStoreMeshBase create(
            NetMeshBaseIdentifier    identifier,
            ProxyPolicyFactory       proxyPolicyFactory,
            MeshObjectSetFactory     setFactory,
            ModelBase                modelBase,
            NetAccessManager         accessMgr,
            IterableStore            meshObjectStore,
            IterableStore            proxyStore,
            IterableStore            shadowStore,
            IterableStore            shadowProxyStore,
            ProbeDirectory           probeDirectory,
            ScheduledExecutorService exec,
            long                     timeNotNeededTillExpires,
            Context                  context )
    {
        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( exec );

        StoreShadowMeshBaseFactory delegate = StoreShadowMeshBaseFactory.create(
                modelBase,
                shadowEndpointFactory,
                probeDirectory,
                shadowStore,
                shadowProxyStore,
                timeNotNeededTillExpires,
                context );

        StoreScheduledExecutorProbeManager probeManager = StoreScheduledExecutorProbeManager.create( delegate, shadowStore );
        shadowEndpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );

        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );
        
        IterableLocalNetStoreMeshBase ret = create(
                identifier,
                proxyPolicyFactory,
                setFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                probeManager,
                endpointFactory,
                context );
        
        probeManager.setMainNetMeshBase( ret );
        probeManager.start( exec );

        return ret;
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
     * @param context the Context in which this NetMeshBase runs
     * @return the created IterableLocalNetStoreMeshBase
     */
    public static IterableLocalNetStoreMeshBase create(
            NetMeshBaseIdentifier     identifier,
            ProxyPolicyFactory        proxyPolicyFactory,
            ModelBase                 modelBase,
            NetAccessManager          accessMgr,
            IterableStore             meshObjectStore,
            IterableStore             proxyStore,
            ProbeManager              probeManager,
            ProxyMessageEndpointFactory endpointFactory,
            Context                   context )
    {
        ImmutableMMeshObjectSetFactory setFactory = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );

        IterableLocalNetStoreMeshBase ret = IterableLocalNetStoreMeshBase.create(
                identifier,
                proxyPolicyFactory,
                setFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                probeManager,
                endpointFactory,
                context );

        return ret;
    }
    
    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
     * @param context the Context in which this NetMeshBase runs
     * @return the created IterableLocalNetStoreMeshBase
     */
    public static IterableLocalNetStoreMeshBase create(
            NetMeshBaseIdentifier     identifier,
            ProxyPolicyFactory        proxyPolicyFactory,
            MeshObjectSetFactory      setFactory,
            ModelBase                 modelBase,
            NetAccessManager          accessMgr,
            IterableStore             meshObjectStore,
            IterableStore             proxyStore,
            ProbeManager              probeManager,
            ProxyMessageEndpointFactory endpointFactory,
            Context                   context )
    {
        DefaultProxyFactory proxyFactory = DefaultProxyFactory.create( endpointFactory, proxyPolicyFactory );

        NetStoreMeshBaseEntryMapper objectMapper = new NetStoreMeshBaseEntryMapper();
        StoreProxyEntryMapper       proxyMapper  = new StoreProxyEntryMapper( proxyFactory );
        
        IterableStoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject> objectStorage = IterableStoreBackedSwappingHashMap.createWeak( objectMapper, meshObjectStore );
        IterableStoreBackedSwappingHashMap<NetMeshBaseIdentifier,Proxy>     proxyStorage  = IterableStoreBackedSwappingHashMap.createWeak( proxyMapper,  proxyStore );

        final StoreProxyManager proxyManager = StoreProxyManager.create( proxyFactory, proxyStorage );

        NetMeshObjectIdentifierFactory identifierFactory = DefaultAnetMeshObjectIdentifierFactory.create( identifier );
        AnetMeshBaseLifecycleManager   life              = AnetMeshBaseLifecycleManager.create();

        IterableLocalNetStoreMeshBase ret = new IterableLocalNetStoreMeshBase(
                identifier,
                identifierFactory,
                setFactory,
                modelBase,
                life,
                accessMgr,
                objectStorage,
                proxyManager,
                probeManager,
                context );

        setFactory.setMeshBase( ret );
        proxyFactory.setNetMeshBase( ret );
        proxyMapper.setMeshBase( ret );
        objectMapper.setMeshBase( ret );

        ret.initializeHomeObject();
        
        if( log.isDebugEnabled() ) {
            log.debug( "created " + ret );
        }
        return ret;
    }

    /**
     * Constructor.
     * 
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param identifierFactory the factory for NetMeshObjectIdentifiers appropriate for this NetMeshBase
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param life the MeshBaseLifecycleManager to use
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param cache the CachingMap that holds the NetMeshObjects in this NetMeshBase
     * @param proxyManager the ProxyManager used by this NetMeshBase
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param context the Context in which this NetMeshBase runs.
     */
    protected IterableLocalNetStoreMeshBase(
            NetMeshBaseIdentifier                           identifier,
            NetMeshObjectIdentifierFactory                  identifierFactory,
            MeshObjectSetFactory                            setFactory,
            ModelBase                                       modelBase,
            AnetMeshBaseLifecycleManager                    life,
            NetAccessManager                                accessMgr,
            StoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject> cache,
            StoreProxyManager                               proxyManager,
            ProbeManager                                    probeManager,
            Context                                         context )
    {
        super( identifier, identifierFactory, setFactory, modelBase, life, accessMgr, cache, proxyManager, probeManager, context );
    }

    /**
     * Obtain an Iterator over all MeshObjects in the Store.
     *
     * @return the Iterator
     */
    public CursorIterator<MeshObject> iterator()
    {
        return getCachingMap().valuesIterator( MeshObjectIdentifier.class, MeshObject.class );
    }
    
    /**
     * Obtain a CursorIterable. This performs the exact same operation as
     * @link #iterator iterator}, but is friendlier towards JSPs and other software
     * that likes to use JavaBeans conventions.
     *
     * @return the CursorIterable
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
            return ((IterableStore)(getCachingMap().getStore())).size();

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
    public IterableNetMeshBaseDifferencer getDifferencer()
    {
        return new IterableNetMeshBaseDifferencer( this );
    }

    /**
     * Helper method for typecasting to the right subtype of CachingMap.
     *
     * @return theCache, typecast
     */
    protected StoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject> getCachingMap()
    {
        return (StoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject>) theCache;
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
        Sweeper sweep = theSweeper;
        if( sweep == null ) {
            throw new NullPointerException();
        }
        for( MeshObject candidate : this ) {
            sweep.potentiallyDelete( candidate );
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
