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

package org.infogrid.probe.shadow.m;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;

import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.set.m.ImmutableMMeshObjectSetFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyManager;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.net.security.NetAccessManager;

import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.a.AStagingMeshBase;

import org.infogrid.probe.shadow.a.AStagingMeshBaseLifecycleManager;
import org.infogrid.probe.shadow.proxy.DefaultShadowProxyFactory;
import org.infogrid.util.CachingMap;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.MapCursorIterator;
import org.infogrid.util.MCachingHashMap;
import org.infogrid.util.logging.Log;

/**
 * <p>An in-memory MeshBase that is used as a staging area for Probes
 * to write their data into.</p>
 *
 * <p>This needs to inherit from NetMMeshBase, not just MMeshBase, in order to get the canonicalization
 * of Identifiers right.</p>
 */
public class MStagingMeshBase
        extends
            AStagingMeshBase
        implements
            StagingMeshBase
{
    private static final Log log = Log.getLogInstance( MStagingMeshBase.class ); // our own, private logger
    
    /**
     * Factory method.
     * 
     * @param shadow the ShadowMeshBase for which this StagingMeshBase is created
     * @return the created MStagingMeshBase
     */
    public static MStagingMeshBase create(
            ShadowMeshBase shadow )
    {
        MCachingHashMap<MeshObjectIdentifier,MeshObject> objectStorage = MCachingHashMap.create();
        MCachingHashMap<NetMeshBaseIdentifier,Proxy>     proxyStorage  = MCachingHashMap.create();

        ProxyPolicyFactory proxyPolicyFactory = shadow.getProxyPolicyFactory();

        // PlaceholderShadowProxyFactory proxyFactory = PlaceholderShadowProxyFactory.create();
        DefaultShadowProxyFactory proxyFactory = DefaultShadowProxyFactory.create( null, proxyPolicyFactory );
        ProxyManager              proxyManager = ProxyManager.create( proxyFactory, proxyStorage );

        AStagingMeshBaseLifecycleManager life      = AStagingMeshBaseLifecycleManager.create();
        NetAccessManager                 accessMgr = null;

        MStagingMeshBase ret = new MStagingMeshBase(
                life,
                accessMgr,
                objectStorage,
                proxyManager,
                shadow );

        // do not initialize home object here: Shadows behave differently
        
        if( log.isDebugEnabled() ) {
            log.debug( "created " + ret );
        }
        return ret;
    }

    /**
     * Constructor.
     *
     * @param life the MeshBaseLifecycleManager to use
     * @param accessMgr the NetAccessManager to use. This is usually null. (FIUXME?)
     * @param cache stores the MeshObjects
     * @param shadow the ShadowMeshBase to which this MStagingMeshBase belongs
     * @param placeholderProxyManager the ProxyManager for this MStagingMeshBase. It only creates PlaceholderProxies.
     */
    protected MStagingMeshBase(
            AStagingMeshBaseLifecycleManager            life,
            NetAccessManager                            accessMgr,
            CachingMap<MeshObjectIdentifier,MeshObject> cache,
            ProxyManager                                placeholderProxyManager,
            ShadowMeshBase                              shadow )
    {
        super(  shadow.getIdentifier(),
                shadow.getMeshObjectIdentifierFactory(),
                shadow.getMeshBaseIdentifierFactory(),
                shadow.getNetMeshObjectAccessSpecificationFactory(),
                ImmutableMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class ),
                shadow.getModelBase(),
                life,
                accessMgr,
                cache,
                placeholderProxyManager,
                shadow.getContext() );

        getMeshObjectSetFactory().setMeshBase( this );
        theShadowMeshBase = shadow;
    }

    /**
     * Returns a CursorIterator over the content of this MeshBase.
     * 
     * @return a CursorIterator.
     */
    public CursorIterator<MeshObject> iterator()
    {
        MapCursorIterator.Values<MeshObjectIdentifier,MeshObject> ret = MapCursorIterator.createForValues(
                theCache,
                MeshObjectIdentifier.class,
                MeshObject.class );
        return ret;
    }

    /**
     * Obtain the time when the current Probe run started.
     *
     * @return the time, in System.currentTimeMillis() format
     */
    public long getCurrentUpdateStartedTime()
    {
        return theShadowMeshBase.getCurrentUpdateStartedTime();
    }
    
    /**
     * Allow a Proxy to tell this StagingMeshBase that it performed an operation that
     * modified data in the StagingMeshBase, and the StagingMeshBase may have to be flushed to disk.
     */
    public void flushMeshBase()
    {
        // no op, we are in memory only
    }

    /**
     * The ShadowMeshBase that we work with.
     */
    protected ShadowMeshBase theShadowMeshBase;
}
