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

import org.infogrid.context.Context;

import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObjectFactory;

import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;

import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;
import org.infogrid.modelbase.ModelBase;

import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;

import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.shadow.AbstractShadowMeshBaseFactory;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.probe.shadow.externalized.ExternalizedShadowMeshBase;

import org.infogrid.probe.shadow.proxy.DefaultShadowProxyPolicyFactory;
import org.infogrid.store.IterableStore;
import org.infogrid.store.prefixing.IterablePrefixingStore;

import org.infogrid.util.FactoryException;

/**
 * Knows how to instantiate StoreShadowMeshBaseFactory.
 */
public class StoreShadowMeshBaseFactory
        extends
            AbstractShadowMeshBaseFactory
        implements
            ShadowMeshBaseFactory
{
    /**
     * Factory method.
     */
    public static StoreShadowMeshBaseFactory create(
            ModelBase                 modelBase,
            ProxyMessageEndpointFactory endpointFactory,
            ProbeDirectory            probeDirectory,
            IterableStore             shadowStore,
            IterableStore             shadowProxyStore,
            long                      timeNotNeededTillExpires,
            Context                   c )
    {
        return new StoreShadowMeshBaseFactory( modelBase, endpointFactory, probeDirectory, shadowStore, shadowProxyStore, timeNotNeededTillExpires, c );
    }

    /**
     * Constructor.
     */
    protected StoreShadowMeshBaseFactory(
            ModelBase                 modelBase,
            ProxyMessageEndpointFactory endpointFactory,
            ProbeDirectory            probeDirectory,
            IterableStore             shadowStore,
            IterableStore             shadowProxyStore,
            long                      timeNotNeededTillExpires,
            Context                   c )
    {
        super( modelBase, endpointFactory, probeDirectory, timeNotNeededTillExpires, c );
        
        theShadowStore      = shadowStore;
        theShadowProxyStore = shadowProxyStore;
    }

    /**
     * Factory method.
     *
     * @param key the key information required for object creation, if any
     * @param argument any information required for object creation, if any
     * @return the created object
     */
    public ShadowMeshBase obtainFor(
            NetMeshBaseIdentifier      key,
            CoherenceSpecification argument )
        throws
            FactoryException
    {
        IterablePrefixingStore thisProxyStore = IterablePrefixingStore.create( key.toExternalForm(), theShadowProxyStore );

        StoreShadowMeshBase ret = StoreShadowMeshBase.create(
                key,
                theEndpointFactory,
                theModelBase,
                null,
                theProbeDirectory,
                theTimeNotNeededTillExpires,
                thisProxyStore,
                theMeshBaseContext );
        
        ret.setFactory( this );

        try {
            Long next = ret.doUpdateNow( argument );

        } catch( Throwable ex ) {
            throw new FactoryException( ex );
        }
        
        return ret;
    }
    
    /**
     * Factory method to recreate a ShadowMeshBase from an ExternalizedShadowMeshBase object.
     *
     * @param key the NetMeshBaseIdentifier for the ShadowMeshBase
     * @param externalized the ExternalizedShadowMeshBase
     * @return the recreated ShadowMeshBase
     */
    public ShadowMeshBase restore(
            NetMeshBaseIdentifier      key,
            ExternalizedShadowMeshBase externalized )
        throws
            FactoryException
    {
        IterablePrefixingStore          thisProxyStore     = IterablePrefixingStore.create( key.toExternalForm(), theShadowProxyStore );
        DefaultShadowProxyPolicyFactory proxyPolicyFactory = DefaultShadowProxyPolicyFactory.create();

        ShadowMeshBase ret = StoreShadowMeshBase.restore(
                key,
                externalized,
                theEndpointFactory,
                proxyPolicyFactory,
                theModelBase,
                null,
                theProbeDirectory,
                theTimeNotNeededTillExpires,
                thisProxyStore,
                theMeshBaseContext );

        ret.setFactory( this );
        
        return ret;
    }

    public ParserFriendlyExternalizedNetMeshObjectFactory getExternalizedMeshObjectFactory()
    {
        return new ParserFriendlyExternalizedNetMeshObjectFactory() {
                /**
                 * Factory method.
                 *
                 * @return the created ParserFriendlyExternalizedMeshObject
                 */
                public ParserFriendlyExternalizedNetMeshObject createParserFriendlyExternalizedMeshObject()
                {
                    return new ParserFriendlyExternalizedNetMeshObject();
                }
        };
    }
    
    public NetMeshObjectIdentifierFactory getNetMeshObjectIdentifierFactory(
            NetMeshBaseIdentifier key )
    {
        return DefaultAnetMeshObjectIdentifierFactory.create( key );
    }
    
    public MeshTypeIdentifierFactory getMeshTypeIdentifierFactory()
    {
        return theModelBase.getMeshTypeIdentifierFactory();
    }

    /**
     * The Store where ShadowMeshBases are stored (but not their Proxies).
     */
    protected IterableStore theShadowStore;
    
    /**
     * The Store in which Shadow proxy data is stored.
     */
    protected IterableStore theShadowProxyStore;
}
