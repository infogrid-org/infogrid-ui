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
import org.infogrid.util.FactoryException;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;

/**
 * Factory for MShadowMeshBases.
 */
public class MShadowMeshBaseFactory
        extends
            AbstractShadowMeshBaseFactory
{
    /**
     * Factory method for the MShadowMeshBaseFactory itself.
     * 
     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
     * @param endpointFactory factory for communications endpoints, to be used by all created MShadowMeshBases
     * @param modelBase the ModelBase containing type information to be used by all created MShadowMeshBases
     * @param probeDirectory the ProbeDirectory to use for all Probes
     * @param context the Context in which this all created MShadowMeshBases will run.
     * @return the created MShadowMeshBaseFactory
     */
    public static MShadowMeshBaseFactory create(
            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
            ProxyMessageEndpointFactory             endpointFactory,
            ModelBase                               modelBase,
            ProbeDirectory                          probeDirectory,
            Context                                 context )
    {
        return new MShadowMeshBaseFactory(
                meshBaseIdentifierFactory,
                endpointFactory,
                modelBase,
                probeDirectory,
                context );
    }

    /**
     * Constructor.
     * 
     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
     * @param endpointFactory factory for communications endpoints, to be used by all created MShadowMeshBases
     * @param modelBase the ModelBase containing type information to be used by all created MShadowMeshBases
     * @param probeDirectory the ProbeDirectory to use for all Probes
     * @param context the Context in which this all created MShadowMeshBases will run.
     */
    protected MShadowMeshBaseFactory(
            NetMeshBaseIdentifierFactory            meshBaseIdentifierFactory,
            ProxyMessageEndpointFactory             endpointFactory,
            ModelBase                               modelBase,
            ProbeDirectory                          probeDirectory,
            Context                                 context )
    {
        super(  endpointFactory,
                modelBase,
                probeDirectory,
                theResourceHelper.getResourceLongOrDefault( "TimeNotNeededTillExpires", 10L * 60L * 1000L ), // 10 minutes
                context );
        
        theMeshBaseIdentifierFactory               = meshBaseIdentifierFactory;
    }

    /**
     * Factory method.
     *
     * @param key the key information required for object creation, if any
     * @param argument any information required for object creation, if any
     * @return the created object
     */
    public ShadowMeshBase obtainFor(
            NetMeshBaseIdentifier  key,
            CoherenceSpecification argument )
        throws
            FactoryException
    {
        NetMeshObjectAccessSpecificationFactory theNetMeshObjectAccessSpecificationFactory = DefaultNetMeshObjectAccessSpecificationFactory.create(
                key,
                theMeshBaseIdentifierFactory );
        
        MShadowMeshBase ret = MShadowMeshBase.create(
                key,
                theMeshBaseIdentifierFactory,
                theNetMeshObjectAccessSpecificationFactory,
                theEndpointFactory,
                theModelBase,
                null,
                theProbeDirectory,
                theTimeNotNeededTillExpires,
                theMeshBaseContext );
        
        ret.setFactory( this );

        Long next; // put out here for easier debugging
        try {
            next = ret.doUpdateNow( argument );

        } catch( Throwable ex ) {
            throw new FactoryException( this, ex );
        }
        
        return ret;
    }
    
    /**
     * Factory for MeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory;
    
    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( MShadowMeshBaseFactory.class );
}
