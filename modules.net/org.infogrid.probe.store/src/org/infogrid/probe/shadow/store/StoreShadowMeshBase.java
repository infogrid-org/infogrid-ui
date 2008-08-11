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

package org.infogrid.probe.shadow.store;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.mesh.set.m.ImmutableMMeshObjectSetFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.meshbase.store.net.StoreProxyManager;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.shadow.a.AShadowMeshBase;
import org.infogrid.probe.shadow.externalized.ExternalizedShadowMeshBase;
import org.infogrid.probe.shadow.proxy.DefaultShadowProxyFactory;
import org.infogrid.probe.shadow.proxy.DefaultShadowProxyPolicyFactory;
import org.infogrid.store.IterableStore;
import org.infogrid.store.util.IterableStoreBackedSwappingHashMap;
import org.infogrid.util.CachingMap;
import org.infogrid.util.FactoryException;
import org.infogrid.util.MCachingHashMap;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * A ShadowMeshBase whose content is stored in a Store. Unlike other Store implementations
 * of MeshBase, this implementation writes the entire content into the Store in bulk.
 * This avoid unnecessary network operations and for example, makes Differencer operations faster.
 */
public class StoreShadowMeshBase
        extends
            AShadowMeshBase
{
    private static final Log log = Log.getLogInstance( StoreShadowMeshBase.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param identifier the MeshBaseIdentifier of this MeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param proxyManager the ProxyManager for this NetMeshBase
     * @param placeholderProxyManager the ProxyManager for the placeholder Proxies (for forward references only)
     * @param directory the ProbeDirectory to use
     * @param shadowStore the Store into which the ShadowMeshBase's MeshObjects are written
     * @param shadowStoreEntryKey the key that identifies the location in the ShadowStore for the bulk MeshObjects
     * @param timeNotNeededTillExpires the time, in milliseconds, that this MShadowMeshBase will continue operating
     *         even if none of its MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this MeshBase runs.
     */
    public static StoreShadowMeshBase create(
            NetMeshBaseIdentifier     identifier,
            ProxyMessageEndpointFactory endpointFactory,
            ModelBase                 modelBase,
            NetAccessManager          accessMgr,
            ProbeDirectory            directory,
            long                      timeNotNeededTillExpires,
            IterableStore             proxyStore,
            Context                   context )
    {
        DefaultShadowProxyPolicyFactory proxyPolicyFactory = DefaultShadowProxyPolicyFactory.create();

        StoreShadowMeshBase ret = create(
                identifier,
                endpointFactory,
                proxyPolicyFactory,
                modelBase,
                accessMgr,
                directory,
                timeNotNeededTillExpires,
                proxyStore,
                context );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param identifier the MeshBaseIdentifier of this MeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param proxyManager the ProxyManager for this NetMeshBase
     * @param placeholderProxyManager the ProxyManager for the placeholder Proxies (for forward references only)
     * @param directory the ProbeDirectory to use
     * @param shadowStore the Store into which the ShadowMeshBase's MeshObjects are written
     * @param shadowStoreEntryKey the key that identifies the location in the ShadowStore for the bulk MeshObjects
     * @param timeNotNeededTillExpires the time, in milliseconds, that this MShadowMeshBase will continue operating
     *         even if none of its MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this MeshBase runs.
     */
    public static StoreShadowMeshBase create(
            NetMeshBaseIdentifier     identifier,
            ProxyMessageEndpointFactory endpointFactory,
            ProxyPolicyFactory        proxyPolicyFactory,
            ModelBase                 modelBase,
            NetAccessManager          accessMgr,
            ProbeDirectory            directory,
            long                      timeNotNeededTillExpires,
            IterableStore             proxyStore,
            Context                   context )
    {
        DefaultShadowProxyFactory   proxyFactory   = DefaultShadowProxyFactory.create( endpointFactory, proxyPolicyFactory );
        ShadowStoreProxyEntryMapper theProxyMapper = new ShadowStoreProxyEntryMapper( proxyFactory );
        
        MCachingHashMap<MeshObjectIdentifier,MeshObject>    objectStorage = MCachingHashMap.create();
        IterableStoreBackedSwappingHashMap<NetMeshBaseIdentifier,Proxy> proxyStorage  = IterableStoreBackedSwappingHashMap.createWeak( theProxyMapper, proxyStore );

        StoreProxyManager proxyManager = StoreProxyManager.create( proxyFactory, proxyStorage );

        NetMeshObjectIdentifierFactory      identifierFactory = DefaultAnetMeshObjectIdentifierFactory.create( identifier );
        ImmutableMMeshObjectSetFactory      setFactory        = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );
        StoreShadowMeshBaseLifecycleManager life              = StoreShadowMeshBaseLifecycleManager.create();

        StoreShadowMeshBase ret = new StoreShadowMeshBase(
                identifier,
                identifierFactory,
                setFactory,
                modelBase,
                life,
                accessMgr,
                objectStorage,
                proxyManager,
                directory,
                System.currentTimeMillis(),
                timeNotNeededTillExpires,
                context );

        setFactory.setMeshBase( ret );
        proxyFactory.setNetMeshBase( ret );
        theProxyMapper.setMeshBase( ret );

        // do not initialize home object here -- this is a ShadowMeshBase

        if( log.isDebugEnabled() ) {
            log.debug( "created " + ret );
        }
        return ret;
    }

    /**
     * Factory method to restore.
     *
     */
    public static StoreShadowMeshBase restore(
            NetMeshBaseIdentifier      identifier,
            ExternalizedShadowMeshBase externalized,
            ProxyMessageEndpointFactory  endpointFactory,
            ProxyPolicyFactory         proxyPolicyFactory,
            ModelBase                  modelBase,
            NetAccessManager           accessMgr,
            ProbeDirectory             directory,
            long                       timeNotNeededTillExpires,
            IterableStore              proxyStore,
            Context                    c )
    {
        DefaultShadowProxyFactory   proxyFactory   = DefaultShadowProxyFactory.create( endpointFactory, proxyPolicyFactory );
        ShadowStoreProxyEntryMapper theProxyMapper = new ShadowStoreProxyEntryMapper( proxyFactory );

        MCachingHashMap<MeshObjectIdentifier,MeshObject>    objectStorage = MCachingHashMap.create();
        IterableStoreBackedSwappingHashMap<NetMeshBaseIdentifier,Proxy> proxyStorage  = IterableStoreBackedSwappingHashMap.createWeak( theProxyMapper, proxyStore );

        StoreProxyManager proxyManager = StoreProxyManager.create( proxyFactory, proxyStorage );

        NetMeshObjectIdentifierFactory      identifierFactory = DefaultAnetMeshObjectIdentifierFactory.create( identifier );
        ImmutableMMeshObjectSetFactory      setFactory        = ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );
        StoreShadowMeshBaseLifecycleManager life              = StoreShadowMeshBaseLifecycleManager.create();

        StoreShadowMeshBase ret = new StoreShadowMeshBase(
                identifier,
                identifierFactory,
                setFactory,
                modelBase,
                life,
                accessMgr,
                objectStorage,
                proxyManager,
                directory,
                System.currentTimeMillis(),
                timeNotNeededTillExpires,
                c );

        setFactory.setMeshBase( ret );
        proxyFactory.setNetMeshBase( ret );
        theProxyMapper.setMeshBase( ret );

        // cannot restore the Proxies here, we don't have the data

        for( ExternalizedNetMeshObject current : externalized.getExternalizedNetMeshObjects() ) {
            try {
                life.restore( current );

            } catch( FactoryException ex ) {
                log.error( ex );
            }
        }
        
        if( log.isDebugEnabled() ) {
            log.debug( "created " + ret );
        }
        return ret;
    }

    /**
     * Constructor. This does not initialize content.
     *
     * @param identifier the MeshBaseIdentifier of this MeshBase
     * @param identifierFactory the factory for MeshObjectIdentifiers appropriate for this MeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param cache the CachingMap that holds the MeshObjects in this MeshBase
     * @param proxyManager the ProxyManager for this NetMeshBase
     * @param directory the ProbeDirectory to use
     * @param shadowStore the Store into which the ShadowMeshBase's MeshObjects are written
     * @param shadowStoreEntryKey the key that identifies the location in the ShadowStore for the bulk MeshObjects
     * @param timeCreated the time at which the data source was created (if this is a restore operation) or -1
     * @param timeNotNeededTillExpires the time, in milliseconds, that this MShadowMeshBase will continue operating
     *         even if none of its MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this MeshBase runs.
     */
    protected StoreShadowMeshBase(
            NetMeshBaseIdentifier                       identifier,
            NetMeshObjectIdentifierFactory              identifierFactory,
            MeshObjectSetFactory                        setFactory,
            ModelBase                                   modelBase,
            StoreShadowMeshBaseLifecycleManager         life,
            NetAccessManager                            accessMgr,
            CachingMap<MeshObjectIdentifier,MeshObject> cache,
            StoreProxyManager                           proxyManager,
            ProbeDirectory                              directory,
            long                                        timeCreated,
            long                                        timeNotNeededTillExpires,    
            Context                                     context )
    {
        super(  identifier,
                identifierFactory,
                setFactory,
                modelBase,
                life,
                accessMgr,
                cache,
                proxyManager,
                directory,
                timeCreated,
                timeNotNeededTillExpires,
                context );
    }

    /**
     * <p>Obtain a manager for object lifecycles.</p>
     * 
     * @return a MeshBaseLifecycleManager that works on this MeshBase with the specified parameters
     */
    @Override
    public StoreShadowMeshBaseLifecycleManager getMeshBaseLifecycleManager()
    {
        return (StoreShadowMeshBaseLifecycleManager) theMeshBaseLifecycleManager;
    }
    
    /**
     * Allow a Proxy to tell this StagingMeshBase that it performed an operation that
     * modified data in the StagingMeshBase, and the StagingMeshBase may have to be flushed to disk.
     */
    public void flushMeshBase()
    {
        if( theProbeManager != null ) {
            theProbeManager.factoryCreatedObjectUpdated( this );
        }
    }
    /**
     * The currently only encoding ID.
     */
    protected static final String ENCODING_ID = StoreShadowMeshBase.class.getName() + "-1";
}
