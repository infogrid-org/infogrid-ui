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

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.a.DefaultAnetMeshObjectIdentifier;
import org.infogrid.mesh.set.m.ImmutableMMeshObjectSetFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.a.AnetMeshBase;
import org.infogrid.meshbase.net.a.AnetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.proxy.DefaultProxyFactory;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.store.IterableStore;
import org.infogrid.store.Store;
import org.infogrid.store.util.IterableStoreBackedSwappingHashMap;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * A NetMeshBase that stores its content in two Stores: one for the NetMeshObjects in
 * the NetMeshBase, and one for the Proxies associated with the NetMeshBase.
 */
public class NetStoreMeshBase
        extends
            AnetMeshBase
{
    private static final Log log = Log.getLogInstance(NetStoreMeshBase.class); // our own, private logger

    /**
     * Factory method.
     * 
     * @param identifier the NetMeshBaseIdentifier of the to-be-created NetMeshBase
     * @param modelBase the ModelBase with the type definitions we use
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param endpointFactory the MessageEndpointFactory to use for proxy communication
     * @param context the Context in which this MeshBase runs
     * @return the created NetStoreMeshBase
     */
    public static NetStoreMeshBase create(
            NetMeshBaseIdentifier     identifier,
            ModelBase                 modelBase,
            NetAccessManager          accessMgr,
            Store                     meshObjectStore,
            IterableStore             proxyStore,
            ProxyMessageEndpointFactory endpointFactory,
            Context                   context )
    {
        ImmutableMMeshObjectSetFactory    setFactory         = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();
        
        NetStoreMeshBase ret = NetStoreMeshBase.create(
                identifier,
                proxyPolicyFactory,
                setFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                endpointFactory,
                context );

        return ret;
    }

    /**
     * Factory method.
     * 
     * @param identifier the NetMeshBaseIdentifier of the to-be-created NetMeshBase
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param modelBase the ModelBase with the type definitions we use
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param endpointFactory the MessageEndpointFactory to use for proxy communication
     * @param context the Context in which this MeshBase runs
     * @return the created NetStoreMeshBase
     */
    public static NetStoreMeshBase create(
            NetMeshBaseIdentifier     identifier,
            ProxyPolicyFactory        proxyPolicyFactory,
            ModelBase                 modelBase,
            NetAccessManager          accessMgr,
            Store                     meshObjectStore,
            IterableStore             proxyStore,
            ProxyMessageEndpointFactory endpointFactory,
            Context                   context )
    {
        ImmutableMMeshObjectSetFactory setFactory = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );
        
        NetStoreMeshBase ret = NetStoreMeshBase.create(
                identifier,
                proxyPolicyFactory,
                setFactory,
                modelBase,
                accessMgr,
                meshObjectStore,
                proxyStore,
                endpointFactory,
                context );

        return ret;
    }

    /**
     * Factory method.
     * 
     * @param identifier the NetMeshBaseIdentifier of the to-be-created NetMeshBase
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase with the type definitions we use
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param meshObjectStore the Store in which to store the MeshObjects
     * @param proxyStore the Store in which to store the Proxies
     * @param endpointFactory the MessageEndpointFactory to use for proxy communication
     * @param context the Context in which this MeshBase runs
     * @return the created NetStoreMeshBase
     */
    public static NetStoreMeshBase create(
            NetMeshBaseIdentifier     identifier,
            ProxyPolicyFactory        proxyPolicyFactory,
            MeshObjectSetFactory      setFactory,
            ModelBase                 modelBase,
            NetAccessManager          accessMgr,
            Store                     meshObjectStore,
            IterableStore             proxyStore,
            ProxyMessageEndpointFactory endpointFactory,
            Context                   context )
    {
        DefaultProxyFactory proxyFactory = DefaultProxyFactory.create( endpointFactory, proxyPolicyFactory );

        NetStoreMeshBaseEntryMapper objectMapper = new NetStoreMeshBaseEntryMapper();
        StoreProxyEntryMapper       proxyMapper  = new StoreProxyEntryMapper( proxyFactory );
        
        StoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject>     objectStorage = StoreBackedSwappingHashMap.createWeak( objectMapper, meshObjectStore );
        IterableStoreBackedSwappingHashMap<NetMeshBaseIdentifier,Proxy> proxyStorage  = IterableStoreBackedSwappingHashMap.createWeak( proxyMapper,  proxyStore );

        NetMeshObjectIdentifierFactory identifierFactory = DefaultAnetMeshObjectIdentifierFactory.create( identifier );
        AnetMeshBaseLifecycleManager   life              = AnetMeshBaseLifecycleManager.create();
        
        StoreProxyManager proxyManager = StoreProxyManager.create( proxyFactory, proxyStorage );

        NetStoreMeshBase ret = new NetStoreMeshBase(
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
    * @param identifier the MeshBaseIdentifier of this MeshBase
    * @param identifierFactory the factory for NetMeshObjectIdentifiers appropriate for this NetMeshBase
    * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
    * @param modelBase the ModelBase containing type information
    * @param life the MeshBaseLifecycleManager to use
    * @param accessMgr the AccessManager that controls access to this MeshBase
    * @param cache the CachingMap that holds the MeshObjects in this MeshBase
    * @param proxyManager the ProxyManager for this NetMeshBase
    * @param context the Context in which this MeshBase runs
    */
    protected NetStoreMeshBase(
            NetMeshBaseIdentifier                           identifier,
            NetMeshObjectIdentifierFactory                  identifierFactory,
            MeshObjectSetFactory                            setFactory,
            ModelBase                                       modelBase,
            AnetMeshBaseLifecycleManager                    life,
            NetAccessManager                                accessMgr,
            StoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject> cache,
            StoreProxyManager                               proxyManager,
            Context                                         context )
    {
        super( identifier, identifierFactory, setFactory, modelBase, life, accessMgr, cache, proxyManager, context );

        HOME_OBJECT_IDENTIFIER = DefaultAnetMeshObjectIdentifier.create( identifier, "" );
    }

//    /**
//     * Create an identifier for a MeshObject at held locally at this MeshBase.
//     *
//     * @param raw the identifier String
//     */
//    public NetMeshObjectIdentifier fromExternalForm(
//            String raw )
//        throws
//            URISyntaxException
//    {
//        DefaultAnetMeshObjectIdentifier ret = DefaultAnetMeshObjectIdentifier.fromExternalForm( getIdentifier(), raw );
//        return ret;
//    }
//    
    /**
     * Determine the Identifier of the Home Object.
     *
     * @return the Identifier
     */
    public NetMeshObjectIdentifier getHomeMeshObjectIdentifier()
    {
        return HOME_OBJECT_IDENTIFIER;
    }
    
    /**
     * Helper method for typecasting to the right subtype of CachingMap.
     * 
     * @return the right subtype of CachingMap
     */
    protected IterableStoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject> getCachingMap()
    {
        return (IterableStoreBackedSwappingHashMap<MeshObjectIdentifier,MeshObject>) theCache;
    }

    /**
     * The home object identifier.
     */
    protected NetMeshObjectIdentifier HOME_OBJECT_IDENTIFIER;
}
