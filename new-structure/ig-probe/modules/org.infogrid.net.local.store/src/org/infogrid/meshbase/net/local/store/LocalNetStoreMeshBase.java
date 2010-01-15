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

package org.infogrid.meshbase.net.local.store;

import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.mesh.set.m.ImmutableMMeshObjectSetFactory;
import org.infogrid.meshbase.net.DefaultNetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.a.AnetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.local.a.LocalAnetMeshBase;
import org.infogrid.meshbase.net.local.LocalNetMeshBase;
import org.infogrid.meshbase.net.proxy.DefaultProxyFactory;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyFactory;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.meshbase.store.net.StoreProxyEntryMapper;
import org.infogrid.meshbase.store.net.StoreProxyManager;
import org.infogrid.meshbase.store.net.NetStoreMeshBaseEntryMapper;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.manager.ProbeManager;
import org.infogrid.probe.manager.store.StoreScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.store.StoreShadowMeshBaseFactory;
import org.infogrid.store.IterableStore;
import org.infogrid.store.Store;
import org.infogrid.store.util.IterableStoreBackedSwappingHashMap;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * A NetStoreMeshBase that uses local (collocated, in this address space) ShadowMeshBases
 * that are also persistent.
 */
public class LocalNetStoreMeshBase
        extends
            LocalAnetMeshBase
        implements
            LocalNetMeshBase
{
    private static final Log log = Log.getLogInstance( LocalNetStoreMeshBase.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param shadowStore the Store in which to store the managed ShadowMeshBases
     * @param shadowProxyStore the Store in which to store the proxies of the managed ShadowMeshBases
     * @param probeDirectory the ProbeDirectory to use
     * @param exec the ScheduledExecutorService to use
     * @param doStart if true, start Probe processing. If false, processing needs to be started manually through the ProbeManager
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetStoreMeshBase
      */
    public static LocalNetStoreMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            Store                                   meshObjectStore,
            IterableStore                           proxyStore,
            IterableStore                           shadowStore,
            IterableStore                           shadowProxyStore,
            ProbeDirectory                          probeDirectory,
            ScheduledExecutorService                exec,
            boolean                                 doStart,
            Context                                 context )
    {
        NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory
                = DefaultNetMeshObjectAccessSpecificationFactory.create( identifier );

        return create(
                identifier,
                netMeshObjectAccessSpecificationFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                shadowStore,
                shadowProxyStore,
                probeDirectory,
                exec,
                doStart,
                context );
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param shadowStore the Store in which to store the managed ShadowMeshBases
     * @param shadowProxyStore the Store in which to store the proxies of the managed ShadowMeshBases
     * @param probeDirectory the ProbeDirectory to use
     * @param exec the ScheduledExecutorService to use
     * @param doStart if true, start Probe processing. If false, processing needs to be started manually through the ProbeManager
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetStoreMeshBase
      */
    public static LocalNetStoreMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            Store                                   meshObjectStore,
            IterableStore                           proxyStore,
            IterableStore                           shadowStore,
            IterableStore                           shadowProxyStore,
            ProbeDirectory                          probeDirectory,
            ScheduledExecutorService                exec,
            boolean                                 doStart,
            Context                                 context )
    {
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();
        
        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( exec );

        StoreShadowMeshBaseFactory delegate = StoreShadowMeshBaseFactory.create(
                netMeshObjectAccessSpecificationFactory.getNetMeshBaseIdentifierFactory(),
                shadowEndpointFactory,
                modelBase,
                probeDirectory,
                shadowStore,
                shadowProxyStore,
                context );

        StoreScheduledExecutorProbeManager probeManager = StoreScheduledExecutorProbeManager.create( delegate, shadowStore );
        shadowEndpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );

        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );
        
        LocalNetStoreMeshBase ret = create(
                identifier,
                netMeshObjectAccessSpecificationFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                endpointFactory,
                proxyPolicyFactory,
                probeManager,
                context );

        probeManager.setMainNetMeshBase( ret );
        if( doStart ) {
            probeManager.start( exec );
        }

        return ret;
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetStoreMeshBase
      */
    public static LocalNetStoreMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            Store                                   meshObjectStore,
            IterableStore                           proxyStore,
            ProxyMessageEndpointFactory             endpointFactory,
            ProbeManager                            probeManager,
            Context                                 context )
    {
        NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory
                = DefaultNetMeshObjectAccessSpecificationFactory.create( identifier );

        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();
        
        LocalNetStoreMeshBase ret = create(
                identifier,
                netMeshObjectAccessSpecificationFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                endpointFactory,
                proxyPolicyFactory,
                probeManager,
                context );

        return ret;
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetMMeshBase
      */
    public static LocalNetStoreMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            Store                                   meshObjectStore,
            IterableStore                           proxyStore,
            ProxyMessageEndpointFactory             endpointFactory,
            ProbeManager                            probeManager,
            Context                                 context )
    {
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();
        
        LocalNetStoreMeshBase ret = create(
                identifier,
                netMeshObjectAccessSpecificationFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                endpointFactory,
                proxyPolicyFactory,
                probeManager,
                context );

        return ret;
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetMMeshBase
     */
    public static LocalNetStoreMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            Store                                   meshObjectStore,
            IterableStore                           proxyStore,
            ProxyMessageEndpointFactory             endpointFactory,
            ProxyPolicyFactory                      proxyPolicyFactory,
            ProbeManager                            probeManager,
            Context                                 context )
    {
        DefaultProxyFactory proxyFactory = DefaultProxyFactory.create( endpointFactory, proxyPolicyFactory );

        NetStoreMeshBaseEntryMapper objectMapper = new NetStoreMeshBaseEntryMapper();
        StoreProxyEntryMapper       proxyMapper  = new StoreProxyEntryMapper( proxyFactory );

        StoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject>     objectStorage = StoreBackedSwappingHashMap.createWeak( objectMapper, meshObjectStore );
        IterableStoreBackedSwappingHashMap<NetMeshBaseIdentifier,Proxy> proxyStorage  = IterableStoreBackedSwappingHashMap.createWeak( proxyMapper, proxyStore );
        
        StoreProxyManager              proxyManager = StoreProxyManager.create( proxyFactory, proxyStorage );
        AnetMeshBaseLifecycleManager   life         = AnetMeshBaseLifecycleManager.create();
        ImmutableMMeshObjectSetFactory setFactory   = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );

        LocalNetStoreMeshBase ret = new LocalNetStoreMeshBase(
                identifier,
                netMeshObjectAccessSpecificationFactory.getNetMeshObjectIdentifierFactory(),
                netMeshObjectAccessSpecificationFactory.getNetMeshBaseIdentifierFactory(),
                netMeshObjectAccessSpecificationFactory,
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
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param proxyFactory factory for Proxies
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param context the Context in which this NetMeshBase runs
     * @return the created LocalNetStoreMeshBase
     */
    public static LocalNetStoreMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
            MeshObjectSetFactory                    setFactory,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            Store                                   meshObjectStore,
            IterableStore                           proxyStore,
            ProxyFactory                            proxyFactory,
            ProbeManager                            probeManager,
            Context                                 context )
    {
        NetStoreMeshBaseEntryMapper objectMapper = new NetStoreMeshBaseEntryMapper();
        StoreProxyEntryMapper       proxyMapper  = new StoreProxyEntryMapper( proxyFactory );

        StoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject>     objectStorage = StoreBackedSwappingHashMap.createWeak( objectMapper, meshObjectStore );
        IterableStoreBackedSwappingHashMap<NetMeshBaseIdentifier,Proxy> proxyStorage  = IterableStoreBackedSwappingHashMap.createWeak( proxyMapper, proxyStore );

        StoreProxyManager            proxyManager = StoreProxyManager.create( proxyFactory, proxyStorage );
        AnetMeshBaseLifecycleManager life         = AnetMeshBaseLifecycleManager.create();

        LocalNetStoreMeshBase ret = new LocalNetStoreMeshBase(
                identifier,
                netMeshObjectAccessSpecificationFactory.getNetMeshObjectIdentifierFactory(),
                netMeshObjectAccessSpecificationFactory.getNetMeshBaseIdentifierFactory(),
                netMeshObjectAccessSpecificationFactory,
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
     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param life the MeshBaseLifecycleManager to use
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param cache the CachingMap that holds the NetMeshObjects in this NetMeshBase
     * @param proxyManager the ProxyManager used by this NetMeshBase
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param context the Context in which this NetMeshBase runs.
     */
    protected LocalNetStoreMeshBase(
            NetMeshBaseIdentifier                                       identifier,
            NetMeshObjectIdentifierFactory                              identifierFactory,
            NetMeshBaseIdentifierFactory                                meshBaseIdentifierFactory,
            NetMeshObjectAccessSpecificationFactory                     netMeshObjectAccessSpecificationFactory,
            MeshObjectSetFactory                                        setFactory,
            ModelBase                                                   modelBase,
            AnetMeshBaseLifecycleManager                                life,
            NetAccessManager                                            accessMgr,
            StoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject> cache,
            StoreProxyManager                                           proxyManager,
            ProbeManager                                                probeManager,
            Context                                                     context )
    {
        super(  identifier,
                identifierFactory,
                meshBaseIdentifierFactory,
                netMeshObjectAccessSpecificationFactory,
                setFactory,
                modelBase,
                life,
                accessMgr,
                cache,
                proxyManager,
                probeManager,
                context );
    }

//    /**
//     * Our ResourceHelper.
//     */
//    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( LocalNetStoreMeshBase.class );
//
//    /**
//     * The default for the time, in milliseconds, that all created ShadowMeshBases will continue operating
//     * even if none of their MeshObjects are replicated to another NetMeshBase. 
//     */
//    public static final long DEFAULT_TIME_NOT_NEEDED_TILL_EXPIRES
//            = theResourceHelper.getResourceLongOrDefault( "ShadowTimeNotNeededTillExpires", 300000L ); // 5 min
}
