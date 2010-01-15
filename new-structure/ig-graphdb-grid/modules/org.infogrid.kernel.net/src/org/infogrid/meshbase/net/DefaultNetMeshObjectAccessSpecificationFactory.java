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

package org.infogrid.meshbase.net;

import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;

/**
 * Default implementation of NetMeshObjectAccessSpecificationFactory.
 */
public class DefaultNetMeshObjectAccessSpecificationFactory
        extends
            AbstractNetMeshObjectAccessSpecificationFactory
{
    /**
     * Factory method.
     *
     * @param localMeshBaseIdentifier the NetMeshBaseIdentifier of the NetMeshBase on behalf of which
     *        this factory will operate
     * @return the created DefaultNetMeshObjectAccessSpecificationFactory
     */
    public static DefaultNetMeshObjectAccessSpecificationFactory create(
            NetMeshBaseIdentifier localMeshBaseIdentifier )
    {
        NetMeshBaseIdentifierFactory meshBaseIdentifierFactory
                = DefaultNetMeshBaseIdentifierFactory.create();
        NetMeshObjectIdentifierFactory meshObjectIdentifierFactory
                = DefaultAnetMeshObjectIdentifierFactory.create( localMeshBaseIdentifier, meshBaseIdentifierFactory );
        NetMeshBaseAccessSpecificationFactory netMeshBaseAccessSpecificationFactory
                = DefaultNetMeshBaseAccessSpecificationFactory.create(  meshBaseIdentifierFactory );
        
        DefaultNetMeshObjectAccessSpecificationFactory ret = new DefaultNetMeshObjectAccessSpecificationFactory(
                meshObjectIdentifierFactory,
                meshBaseIdentifierFactory,
                netMeshBaseAccessSpecificationFactory );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param localMeshBaseIdentifier the NetMeshBaseIdentifier of the NetMeshBase on behalf of which
     *        this factory will operate
     * @param meshBaseIdentifierFactory the factory for MeshBaseIdentifiers
     * @return the created DefaultNetMeshObjectAccessSpecificationFactory
     */
    public static DefaultNetMeshObjectAccessSpecificationFactory create(
            NetMeshBaseIdentifier        localMeshBaseIdentifier,
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
    {
        NetMeshObjectIdentifierFactory meshObjectIdentifierFactory
                = DefaultAnetMeshObjectIdentifierFactory.create( localMeshBaseIdentifier, meshBaseIdentifierFactory );
        NetMeshBaseAccessSpecificationFactory netMeshBaseAccessSpecificationFactory
                = DefaultNetMeshBaseAccessSpecificationFactory.create(  meshBaseIdentifierFactory );
        
        DefaultNetMeshObjectAccessSpecificationFactory ret = new DefaultNetMeshObjectAccessSpecificationFactory(
                meshObjectIdentifierFactory,
                meshBaseIdentifierFactory,
                netMeshBaseAccessSpecificationFactory );
        return ret;
    }
    
    /**
     * Factory method.
     *
     * @param meshObjectIdentifierFactory the factory for MeshObjectIdentifiers
     * @param meshBaseIdentifierFactory the factory for MeshBaseIdentifiers
     * @param netMeshBaseAccessSpecificationFactory the factory for NetMeshBaseAccessSpecifications
     * @return the created DefaultNetMeshObjectAccessSpecificationFactory
     */
    public static DefaultNetMeshObjectAccessSpecificationFactory create(
            NetMeshObjectIdentifierFactory        meshObjectIdentifierFactory,
            NetMeshBaseIdentifierFactory          meshBaseIdentifierFactory,
            NetMeshBaseAccessSpecificationFactory netMeshBaseAccessSpecificationFactory )
    {
        DefaultNetMeshObjectAccessSpecificationFactory ret = new DefaultNetMeshObjectAccessSpecificationFactory(
                meshObjectIdentifierFactory,
                meshBaseIdentifierFactory,
                netMeshBaseAccessSpecificationFactory );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param meshObjectIdentifierFactory the factory for MeshObjectIdentifiers
     * @param meshBaseIdentifierFactory the factory for MeshBaseIdentifiers
     * @param netMeshBaseAccessSpecificationFactory the factory for NetMeshBaseAccessSpecifications
     */
    protected DefaultNetMeshObjectAccessSpecificationFactory(
            NetMeshObjectIdentifierFactory        meshObjectIdentifierFactory,
            NetMeshBaseIdentifierFactory          meshBaseIdentifierFactory,
            NetMeshBaseAccessSpecificationFactory netMeshBaseAccessSpecificationFactory )
    {
        super( meshObjectIdentifierFactory, meshBaseIdentifierFactory, netMeshBaseAccessSpecificationFactory );
    }
}
