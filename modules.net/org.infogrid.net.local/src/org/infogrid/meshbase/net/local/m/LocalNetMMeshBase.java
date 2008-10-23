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

package org.infogrid.meshbase.net.local.m;

import java.util.HashMap;
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
import org.infogrid.meshbase.net.local.a.LocalAIterableNetMeshBase;
import org.infogrid.meshbase.net.proxy.DefaultProxyFactory;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyFactory;
import org.infogrid.meshbase.net.proxy.ProxyManager;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.manager.ProbeManager;
import org.infogrid.probe.manager.ScheduledExecutorProbeManager;
import org.infogrid.probe.manager.m.MScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.probe.shadow.m.MShadowMeshBaseFactory;
import org.infogrid.util.CachingMap;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.MCachingHashMap;
import org.infogrid.util.MapCursorIterator;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * A NetMMeshBase that uses local (collocated, in this address space) ShadowMeshBases.
 */
public class LocalNetMMeshBase
        extends
            LocalAIterableNetMeshBase
{
    private static final Log log = Log.getLogInstance( LocalNetMMeshBase.class ); // our own, private logger


//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeDirectory the ProbeDirectory to use
//     * @param context the Context in which this NetMeshBase runs
//     * @return the created LocalNetStoreMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier    identifier,
//            ModelBase                modelBase,
//            NetAccessManager         accessMgr,
//            ProbeDirectory           probeDirectory,
//            Context                  context )
//    {
//        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory        = NiceAndTrustingProxyPolicyFactory.obtain();
//        NetMeshBaseIdentifierFactory      meshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.obtain();
//
//        return obtain(
//                identifier,
//                meshBaseIdentifierFactory,
//                DefaultNetMeshObjectAccessSpecificationFactory.obtain(),
//                proxyPolicyFactory,
//                modelBase,
//                accessMgr,
//                probeDirectory,
//                DEFAULT_TIME_NOT_NEEDED_TILL_EXPIRES,
//                context );
//    }
//
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeDirectory the ProbeDirectory to use
//     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
//     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
//     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
//     *         not very useful.
//     * @param context the Context in which this NetMeshBase runs
//     * @return the created LocalNetStoreMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                   identifier,
//            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
//            ModelBase                               modelBase,
//            NetAccessManager                        accessMgr,
//            ProbeDirectory                          probeDirectory,
//            long                                    timeNotNeededTillExpires,
//            Context                                 context )
//    {
//        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.obtain();
//
//        return obtain(
//                identifier,
//                proxyPolicyFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                modelBase,
//                accessMgr,
//                probeDirectory,
//                timeNotNeededTillExpires,
//                context );
//    }
//
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeDirectory the ProbeDirectory to use
//     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
//     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
//     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
//     *         not very useful.
//     * @param context the Context in which this NetMeshBase runs
//     * @return the created LocalNetStoreMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                   identifier,
//            ProxyPolicyFactory                      proxyPolicyFactory,
//            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
//            ModelBase                               modelBase,
//            NetAccessManager                        accessMgr,
//            ProbeDirectory                          probeDirectory,
//            long                                    timeNotNeededTillExpires,
//            Context                                 context )
//    {
//        ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );
//
//        return obtain(
//                identifier,
//                proxyPolicyFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                modelBase,
//                accessMgr,
//                probeDirectory,
//                exec,
//                timeNotNeededTillExpires,
//                context );
//    }
//
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeDirectory the ProbeDirectory to use
//     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
//     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
//     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
//     *         not very useful.
//     * @param context the Context in which this NetMeshBase runs
//     * @return the created LocalNetStoreMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                   identifier,
//            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
//            MeshObjectSetFactory                    setFactory,
//            ModelBase                               modelBase,
//            NetAccessManager                        accessMgr,
//            ProbeDirectory                          probeDirectory,
//            long                                    timeNotNeededTillExpires,
//            Context                                 context )
//    {
//        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.obtain();
//
//        return obtain(
//                identifier,
//                proxyPolicyFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                setFactory,
//                modelBase,
//                accessMgr,
//                probeDirectory,
//                timeNotNeededTillExpires,
//                context );
//    }
//    
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeDirectory the ProbeDirectory to use
//     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
//     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
//     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
//     *         not very useful.
//     * @param context the Context in which this NetMeshBase runs
//     * @return the created LocalNetStoreMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                   identifier,
//            ProxyPolicyFactory                      proxyPolicyFactory,
//            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
//            MeshObjectSetFactory                    setFactory,
//            ModelBase                               modelBase,
//            NetAccessManager                        accessMgr,
//            ProbeDirectory                          probeDirectory,
//            long                                    timeNotNeededTillExpires,
//            Context                                 context )
//    {
//        ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );
//
//        return obtain(
//                identifier,
//                proxyPolicyFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                setFactory,
//                modelBase,
//                accessMgr,
//                probeDirectory,
//                exec,
//                timeNotNeededTillExpires,
//                context );
//    }
//
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeDirectory the ProbeDirectory to use
//     * @param exec the ScheduledExecutorService to use
//     * @param context the Context in which this NetMeshBase runs
//     * @return the created LocalNetStoreMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                   identifier,
//            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
//            ModelBase                               modelBase,
//            NetAccessManager                        accessMgr,
//            ProbeDirectory                          probeDirectory,
//            ScheduledExecutorService                exec,
//            Context                                 context )
//    {
//        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.obtain();
//
//        return obtain(
//                identifier,
//                proxyPolicyFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                modelBase,
//                accessMgr,
//                probeDirectory,
//                exec,
//                DEFAULT_TIME_NOT_NEEDED_TILL_EXPIRES,
//                context );
//    }
//    
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeDirectory the ProbeDirectory to use
//     * @param exec the ScheduledExecutorService to use
//     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
//     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
//     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
//     *         not very useful.
//     * @param context the Context in which this NetMeshBase runs
//     * @return the created LocalNetStoreMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                   identifier,
//            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
//            ModelBase                               modelBase,
//            NetAccessManager                        accessMgr,
//            ProbeDirectory                          probeDirectory,
//            ScheduledExecutorService                exec,
//            long                                    timeNotNeededTillExpires,
//            Context                                 context )
//    {
//        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.obtain();
//
//        return obtain(
//                identifier,
//                proxyPolicyFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                modelBase,
//                accessMgr,
//                probeDirectory,
//                exec,
//                timeNotNeededTillExpires,
//                context );
//    }
//    
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeDirectory the ProbeDirectory to use
//     * @param exec the ScheduledExecutorService to use
//     * @param timeNotNeededTillExpires the time, in milliseconds, that all created MShadowMeshBases will continue operating
//     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
//     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
//     *         not very useful.
//     * @param context the Context in which this NetMeshBase runs
//     * @return the created LocalNetStoreMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                   identifier,
//            ProxyPolicyFactory                      proxyPolicyFactory,
//            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
//            ModelBase                               modelBase,
//            NetAccessManager                        accessMgr,
//            ProbeDirectory                          probeDirectory,
//            ScheduledExecutorService                exec,
//            long                                    timeNotNeededTillExpires,
//            Context                                 context )
//    {
//        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.obtain( exec );
//
//        MShadowMeshBaseFactory delegate = MShadowMeshBaseFactory.obtain(
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                shadowEndpointFactory,
//                modelBase,
//                probeDirectory,
//                timeNotNeededTillExpires,
//                context );
//
//        MScheduledExecutorProbeManager probeManager = MScheduledExecutorProbeManager.obtain( delegate );
//        shadowEndpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );
//
//        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.obtain( exec );
//        endpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );
//        
//        LocalNetMMeshBase ret = obtain(
//                identifier,
//                endpointFactory,
//                proxyPolicyFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                modelBase,
//                accessMgr,
//                probeManager,
//                context );
//        
//        probeManager.setMainNetMeshBase( ret );
//        probeManager.start( exec ); // no if( doStart ) here -- does not make sense for MMeshBase
//
//        return ret;
//    }
//    
//   
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeDirectory the ProbeDirectory to use for all Probes
//     * @param exec the ScheduledExecutorService to schedule timed tasks
//     * @param timeNotNeededTillExpires the time, in milliseconds, that all created ShadowMeshBases will continue operating
//     *         even if none of their MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
//     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
//     *         not very useful.
//     * @param context the Context in which this NetMeshBase runs.
//     * @return the created LocalNetMMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                   identifier,
//            ProxyPolicyFactory                      proxyPolicyFactory,
//            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
//            MeshObjectSetFactory                    setFactory,
//            ModelBase                               modelBase,
//            NetAccessManager                        accessMgr,
//            ProbeDirectory                          probeDirectory,
//            ScheduledExecutorService                exec,
//            long                                    timeNotNeededTillExpires,
//            Context                                 context )
//    {
//        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.obtain( exec );
//
//        ShadowMeshBaseFactory delegate = MShadowMeshBaseFactory.obtain(
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                shadowEndpointFactory,
//                modelBase,
//                probeDirectory,
//                timeNotNeededTillExpires,
//                context );
//
//        ScheduledExecutorProbeManager probeManager = MScheduledExecutorProbeManager.obtain( delegate );
//        shadowEndpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );
//
//        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.obtain( exec );
//        endpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );
//        
//        LocalNetMMeshBase ret = obtain(
//                identifier,
//                endpointFactory,
//                proxyPolicyFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                setFactory,
//                modelBase,
//                accessMgr,
//                probeManager,
//                context );
//        
//        probeManager.setMainNetMeshBase( ret );
//        probeManager.start( exec );
//
//        return ret;
//    }
//
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
//     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeManager the ProbeManager for this LocalNetMeshBase
//     * @param context the Context in which this NetMeshBase runs.
//     * @return the created LocalNetMMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                   identifier,
//            ProxyMessageEndpointFactory             endpointFactory,
//            ProxyPolicyFactory                      proxyPolicyFactory,
//            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
//            ModelBase                               modelBase,
//            NetAccessManager                        accessMgr,
//            ProbeManager                            probeManager,
//            Context                                 context )
//    {
//        ImmutableMMeshObjectSetFactory setFactory = ImmutableMMeshObjectSetFactory.obtain( NetMeshObject.class, NetMeshObjectIdentifier.class );
//
//        LocalNetMMeshBase ret = LocalNetMMeshBase.obtain(
//                identifier,
//                endpointFactory,
//                proxyPolicyFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                setFactory,
//                modelBase,
//                accessMgr,
//                probeManager,
//                context );
//
//        return ret;
//    }
//    
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
//     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param probeManager the ProbeManager for this LocalNetMeshBase
//     * @param context the Context in which this NetMeshBase runs.
//     * @return the created LocalNetMMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                       identifier,
//            ProxyMessageEndpointFactory                 endpointFactory,
//            ProxyPolicyFactory                          proxyPolicyFactory,
//            NetMeshBaseIdentifierFactory                meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory     netMeshObjectAccessSpecificationFactory,
//            MeshObjectSetFactory                        setFactory,
//            ModelBase                                   modelBase,
//            NetAccessManager                            accessMgr,
//            ProbeManager                                probeManager,
//            Context                                     context )
//    {
//        MCachingHashMap<MeshObjectIdentifier,MeshObject> objectStorage = MCachingHashMap.obtain();
//        MCachingHashMap<NetMeshBaseIdentifier,Proxy>     proxyStorage  = MCachingHashMap.obtain();
//
//        DefaultProxyFactory proxyFactory = DefaultProxyFactory.obtain( endpointFactory, proxyPolicyFactory );
//        ProxyManager        proxyManager = ProxyManager.obtain( proxyFactory, proxyStorage );
//        
//        NetMeshObjectIdentifierFactory identifierFactory = DefaultAnetMeshObjectIdentifierFactory.obtain( identifier, meshBaseIdentifierFactory );
//        AnetMeshBaseLifecycleManager   life              = AnetMeshBaseLifecycleManager.obtain();
//
//        LocalNetMMeshBase ret = new LocalNetMMeshBase(
//                identifier,
//                identifierFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                setFactory,
//                modelBase,
//                life,
//                accessMgr,
//                objectStorage,
//                proxyManager,
//                probeManager,
//                context );
//
//        setFactory.setMeshBase( ret );
//        proxyFactory.setNetMeshBase( ret );
//        ret.initializeHomeObject();
//        
//        if( log.isDebugEnabled() ) {
//            log.debug( "created " + ret );
//        }
//        return ret;
//    }
//
//    /**
//     * Factory method.
//     *
//     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
//     * @param identifierFactory the factory for NetMeshObjectIdentifiers appropriate for this NetMeshBase
//     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
//     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
//     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
//     * @param modelBase the ModelBase containing type information
//     * @param accessMgr the AccessManager that controls access to this NetMeshBase
//     * @param proxyFactory factory for Proxies
//     * @param probeManager the ProbeManager for this LocalNetMeshBase
//     * @param context the Context in which this NetMeshBase runs.
//     * @return the created LocalNetMMeshBase
//     */
//    public static LocalNetMMeshBase obtain(
//            NetMeshBaseIdentifier                       identifier,
//            NetMeshObjectIdentifierFactory              identifierFactory,
//            NetMeshBaseIdentifierFactory                meshBaseIdentifierFactory,
//            NetMeshObjectAccessSpecificationFactory     netMeshObjectAccessSpecificationFactory,
//            MeshObjectSetFactory                        setFactory,
//            ModelBase                                   modelBase,
//            NetAccessManager                            accessMgr,
//            ProxyFactory                                proxyFactory,
//            ProbeManager                                probeManager,
//            Context                                     context )
//    {
//        MCachingHashMap<MeshObjectIdentifier,MeshObject> objectStorage = MCachingHashMap.obtain();
//        MCachingHashMap<NetMeshBaseIdentifier,Proxy>     proxyStorage  = MCachingHashMap.obtain();
//
//        ProxyManager                 proxyManager = ProxyManager.obtain( proxyFactory, proxyStorage );
//        AnetMeshBaseLifecycleManager life         = AnetMeshBaseLifecycleManager.obtain();
//
//        LocalNetMMeshBase ret = new LocalNetMMeshBase(
//                identifier,
//                identifierFactory,
//                meshBaseIdentifierFactory,
//                netMeshObjectAccessSpecificationFactory,
//                setFactory,
//                modelBase,
//                life,
//                accessMgr,
//                objectStorage,
//                proxyManager,
//                probeManager,
//                context );
//
//        setFactory.setMeshBase( ret );
//        proxyFactory.setNetMeshBase( ret );
//        ret.initializeHomeObject();
//        
//        if( log.isDebugEnabled() ) {
//            log.debug( "created " + ret );
//        }
//        return ret;
//    }
//    

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param probeDirectory the ProbeDirectory to use
     * @param exec the ScheduledExecutorService to use
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetStoreMeshBase
      */
    public static LocalNetMMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            ProbeDirectory                          probeDirectory,
            ScheduledExecutorService                exec,
            Context                                 context )
    {
        NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory
                = DefaultNetMeshObjectAccessSpecificationFactory.create( identifier );

        return create(
                identifier,
                netMeshObjectAccessSpecificationFactory,
                modelBase,
                accessMgr,
                probeDirectory,
                exec,
                context );
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param probeDirectory the ProbeDirectory to use
     * @param exec the ScheduledExecutorService to use
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetStoreMeshBase
      */
    public static LocalNetMMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            ProbeDirectory                          probeDirectory,
            ScheduledExecutorService                exec,
            Context                                 context )
    {
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();
        
        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( exec );

        ShadowMeshBaseFactory delegate = MShadowMeshBaseFactory.create(
                netMeshObjectAccessSpecificationFactory.getNetMeshBaseIdentifierFactory(),
                shadowEndpointFactory,
                modelBase,
                probeDirectory,
                context );

        ScheduledExecutorProbeManager probeManager = MScheduledExecutorProbeManager.create( delegate );
        shadowEndpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );

        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );
        
        LocalNetMMeshBase ret = create(
                identifier,
                netMeshObjectAccessSpecificationFactory,
                modelBase,
                accessMgr,
                endpointFactory,
                proxyPolicyFactory,
                probeManager,
                context );

        probeManager.setMainNetMeshBase( ret );
        probeManager.start( exec );

        return ret;
    }
    
    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetStoreMeshBase
      */
    public static LocalNetMMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            ProxyMessageEndpointFactory             endpointFactory,
            ProbeManager                            probeManager,
            Context                                 context )
    {
        NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory
                = DefaultNetMeshObjectAccessSpecificationFactory.create( identifier );

        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();
        
        LocalNetMMeshBase ret = create(
                identifier,
                netMeshObjectAccessSpecificationFactory,
                modelBase,
                accessMgr,
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
     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetMMeshBase
      */
    public static LocalNetMMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            ProxyMessageEndpointFactory             endpointFactory,
            ProbeManager                            probeManager,
            Context                                 context )
    {
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();
        
        LocalNetMMeshBase ret = create(
                identifier,
                netMeshObjectAccessSpecificationFactory,
                modelBase,
                accessMgr,
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
     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetMMeshBase
     */
    public static LocalNetMMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            ProxyMessageEndpointFactory             endpointFactory,
            ProxyPolicyFactory                      proxyPolicyFactory,
            ProbeManager                            probeManager,
            Context                                 context )
    {
        MCachingHashMap<MeshObjectIdentifier,MeshObject> objectStorage = MCachingHashMap.create();
        MCachingHashMap<NetMeshBaseIdentifier,Proxy>     proxyStorage  = MCachingHashMap.create();
        
        DefaultProxyFactory            proxyFactory = DefaultProxyFactory.create( endpointFactory, proxyPolicyFactory );
        ProxyManager                   proxyManager = ProxyManager.create( proxyFactory, proxyStorage );
        AnetMeshBaseLifecycleManager   life         = AnetMeshBaseLifecycleManager.create();
        ImmutableMMeshObjectSetFactory setFactory   = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );

        LocalNetMMeshBase ret = new LocalNetMMeshBase(
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
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param proxyFactory factory for Proxies
     * @param probeManager the ProbeManager for this LocalNetMeshBase
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetMMeshBase
     */
    public static LocalNetMMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            ProxyFactory                            proxyFactory,
            ProbeManager                            probeManager,
            Context                                 context )
    {
        MCachingHashMap<MeshObjectIdentifier,MeshObject> objectStorage = MCachingHashMap.create();
        MCachingHashMap<NetMeshBaseIdentifier,Proxy>     proxyStorage  = MCachingHashMap.create();
        
        ProxyManager                   proxyManager = ProxyManager.create( proxyFactory, proxyStorage );
        AnetMeshBaseLifecycleManager   life         = AnetMeshBaseLifecycleManager.create();
        ImmutableMMeshObjectSetFactory setFactory   = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );

        LocalNetMMeshBase ret = new LocalNetMMeshBase(
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
    protected LocalNetMMeshBase(
            NetMeshBaseIdentifier                       identifier,
            NetMeshObjectIdentifierFactory              identifierFactory,
            NetMeshBaseIdentifierFactory                meshBaseIdentifierFactory,
            NetMeshObjectAccessSpecificationFactory     netMeshObjectAccessSpecificationFactory,
            MeshObjectSetFactory                        setFactory,
            ModelBase                                   modelBase,
            AnetMeshBaseLifecycleManager                life,
            NetAccessManager                            accessMgr,
            CachingMap<MeshObjectIdentifier,MeshObject> cache,
            ProxyManager                                proxyManager,
            ProbeManager                                probeManager,
            Context                                     context )
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

    /**
     * Returns a CursorIterator over the content of this MeshBase.
     * 
     * @return a CursorIterator.
     */
    public CursorIterator<MeshObject> iterator()
    {
        // not sure why these type casts are needed, they should not be
        MapCursorIterator.Values<MeshObjectIdentifier,MeshObject> ret = MapCursorIterator.createForValues(
                (HashMap<MeshObjectIdentifier,MeshObject>) theCache,
                MeshObjectIdentifier.class,
                MeshObject.class );
        return ret;
    }
}
