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

package org.infogrid.meshbase.net.m;

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
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyManager;
import org.infogrid.meshbase.net.a.AIterableNetMeshBase;
import org.infogrid.meshbase.net.a.AnetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.proxy.DefaultProxyFactory;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.ProxyFactory;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.util.CachingMap;
import org.infogrid.util.MCachingHashMap;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * This NetMeshBase is held only in memory. It has no persistence whatsoever.
 */
public class NetMMeshBase
        extends
            AIterableNetMeshBase
{
    private static final Log log = Log.getLogInstance(NetMMeshBase.class); // our own, private logger

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetMMeshBase
      */
    public static NetMMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            ProxyMessageEndpointFactory             endpointFactory,
            Context                                 context )
    {
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();
        
        NetMMeshBase ret = create(
                identifier,
                modelBase,
                accessMgr,
                endpointFactory,
                proxyPolicyFactory,
                context );

        return ret;
    }

    /**
     * Factory method.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param endpointFactory the factory for NetMessageEndpoints to communicate with other NetMeshBases
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetMMeshBase
      */
    public static NetMMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            ProxyMessageEndpointFactory             endpointFactory,
            ProxyPolicyFactory                      proxyPolicyFactory,
            Context                                 context )
    {
        NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory
                = DefaultNetMeshObjectAccessSpecificationFactory.create( identifier );

        NetMMeshBase ret = create(
                identifier,
                netMeshObjectAccessSpecificationFactory,
                modelBase,
                accessMgr,
                endpointFactory,
                proxyPolicyFactory,
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
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetMMeshBase
     */
    public static NetMMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            ProxyMessageEndpointFactory             endpointFactory,
            ProxyPolicyFactory                      proxyPolicyFactory,
            Context                                 context )
    {
        MCachingHashMap<MeshObjectIdentifier,MeshObject> objectStorage = MCachingHashMap.create();
        MCachingHashMap<NetMeshBaseIdentifier,Proxy>     proxyStorage  = MCachingHashMap.create();
        
        DefaultProxyFactory            proxyFactory = DefaultProxyFactory.create( endpointFactory, proxyPolicyFactory );
        ProxyManager                   proxyManager = ProxyManager.create( proxyFactory, proxyStorage );
        AnetMeshBaseLifecycleManager   life         = AnetMeshBaseLifecycleManager.create();
        ImmutableMMeshObjectSetFactory setFactory   = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );

        NetMMeshBase ret = new NetMMeshBase(
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
     * @param context the Context in which this NetMeshBase runs.
     * @return the created NetMMeshBase
     */
    public static NetMMeshBase create(
            NetMeshBaseIdentifier                   identifier,
            NetMeshObjectAccessSpecificationFactory netMeshObjectAccessSpecificationFactory,
            ModelBase                               modelBase,
            NetAccessManager                        accessMgr,
            ProxyFactory                            proxyFactory,
            Context                                 context )
    {
        MCachingHashMap<MeshObjectIdentifier,MeshObject> objectStorage = MCachingHashMap.create();
        MCachingHashMap<NetMeshBaseIdentifier,Proxy>     proxyStorage  = MCachingHashMap.create();
        
        ProxyManager                   proxyManager = ProxyManager.create( proxyFactory, proxyStorage );
        AnetMeshBaseLifecycleManager   life         = AnetMeshBaseLifecycleManager.create();
        ImmutableMMeshObjectSetFactory setFactory   = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );

        NetMMeshBase ret = new NetMMeshBase(
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
     * @param context the Context in which this NetMeshBase runs.
     */
    protected NetMMeshBase(
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
                context );
    }
}
