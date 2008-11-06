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

import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.DefaultNetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.shadow.AbstractShadowMeshBaseFactory;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.store.IterableStore;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.FactoryException;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * Knows how to instantiate StoreShadowMeshBaseFactory.
 */
public class StoreShadowMeshBaseFactory
        extends
            AbstractShadowMeshBaseFactory
        implements
            ShadowMeshBaseFactory
{
    private static final Log log = Log.getLogInstance( StoreShadowMeshBaseFactory.class ); // our own, private logger

    /**
     * Factory method for the StoreShadowMeshBaseFactory itself.
     * 
     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
     * @param modelBase the ModelBase containing type information to be used by all created StoreShadowMeshBases
     * @param endpointFactory factory for communications endpoints, to be used by all created StoreShadowMeshBase
     * @param probeDirectory the ProbeDirectory to use for all Probes
     * @param shadowStore the Store in which the ShadowMeshBases will be stored
     * @param shadowProxyStore the Store in which the ShadowMeshBases' Proxies will be stored
     * @param context the Context in which this all created MShadowMeshBases will run.
     * @return the created StoreShadowMeshBaseFactory
     */
    public static StoreShadowMeshBaseFactory create(
            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
            ProxyMessageEndpointFactory             endpointFactory,
            ModelBase                               modelBase,
            ProbeDirectory                          probeDirectory,
            IterableStore                           shadowStore,
            IterableStore                           shadowProxyStore,
            Context                                 context )
    {
        return new StoreShadowMeshBaseFactory(
                meshBaseIdentifierFactory,
                endpointFactory,
                modelBase,
                probeDirectory,
                shadowStore,
                shadowProxyStore,
                context );
    }

    /**
     * Constructor.
     * 
     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
     * @param modelBase the ModelBase containing type information to be used by all created StoreShadowMeshBases
     * @param endpointFactory factory for communications endpoints, to be used by all created StoreShadowMeshBase
     * @param probeDirectory the ProbeDirectory to use for all Probes
     * @param shadowStore the Store in which the ShadowMeshBases will be stored
     * @param shadowProxyStore the Store in which the ShadowMeshBases' Proxies will be stored
     * @param context the Context in which this all created MShadowMeshBases will run.
     */
    protected StoreShadowMeshBaseFactory(
            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
            ProxyMessageEndpointFactory             endpointFactory,
            ModelBase                               modelBase,
            ProbeDirectory                          probeDirectory,
            IterableStore                           shadowStore,
            IterableStore                           shadowProxyStore,
            Context                                 context )
    {
        super(  endpointFactory,
                modelBase,
                probeDirectory,
                theResourceHelper.getResourceLongOrDefault( "TimeNotNeededTillExpires", 10L * 60L * 1000L ), // 10 minutes
                context );
        
        theMeshBaseIdentifierFactory = meshBaseIdentifierFactory;
        
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
        NetMeshObjectAccessSpecificationFactory theNetMeshObjectAccessSpecificationFactory = DefaultNetMeshObjectAccessSpecificationFactory.create(
                key,
                theMeshBaseIdentifierFactory );

        IterablePrefixingStore thisProxyStore = IterablePrefixingStore.create( key.toExternalForm(), theShadowProxyStore );

        StoreShadowMeshBase ret = StoreShadowMeshBase.create(
                key,
                theMeshBaseIdentifierFactory,
                theNetMeshObjectAccessSpecificationFactory,
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
            throw new FactoryException( this, ex );
        }
        
        return ret;
    }
    
    /**
     * Factory method to create an ShadowMeshBase that later will be restored from an ExternalizedShadowMeshBase object.
     *
     * @param key the NetMeshBaseIdentifier for the ShadowMeshBase
     * @return the recreated ShadowMeshBase
     */
    public StoreShadowMeshBase createEmptyForRestore(
            NetMeshBaseIdentifier key )
    {
        NetMeshObjectAccessSpecificationFactory theNetMeshObjectAccessSpecificationFactory = DefaultNetMeshObjectAccessSpecificationFactory.create(
                key,
                theMeshBaseIdentifierFactory );

        IterablePrefixingStore thisProxyStore = IterablePrefixingStore.create( key.toExternalForm(), theShadowProxyStore );

        StoreShadowMeshBase ret = StoreShadowMeshBase.create(
                key,
                theMeshBaseIdentifierFactory,
                theNetMeshObjectAccessSpecificationFactory,
                theEndpointFactory,
                theModelBase,
                null,
                theProbeDirectory,
                theTimeNotNeededTillExpires,
                thisProxyStore,
                theMeshBaseContext );
        
        ret.setFactory( this );
        
        return ret;
    }

    /**
     * Obtain a factory for NetMeshBaseIdentifiers.
     * 
     * @return the factory
     */
    public NetMeshBaseIdentifierFactory getNetMeshBaseIdentifierFactory()
    {
        return theMeshBaseIdentifierFactory;
    }

    /**
     * Factory for MeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory;

    /**
     * The Store where ShadowMeshBases are stored (but not their Proxies).
     */
    protected IterableStore theShadowStore;
    
    /**
     * The Store in which Shadow proxy data is stored.
     */
    protected IterableStore theShadowProxyStore;
    
    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( StoreShadowMeshBaseFactory.class );
}
