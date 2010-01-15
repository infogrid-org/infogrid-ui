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

package org.infogrid.probe.shadow.externalized;

import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;

/**
 * This implementation of ExternalizedShadowMeshBase is fully initialized in the
 * factory method.
 */
public class SimpleExternalizedShadowMeshBase
        extends
            AbstractExternalizedShadowMeshBase
{
    /**
     * Factory method.
     * 
     * @param identifier the NetMeshBaseIdentifier for the ShadowMeshBase
     * @param externalizedProxies externalized representation of the Proxies of the ShadowMeshBase
     * @param externalizedNetMeshObjects externalized representation of the NetMeshObjects held by this ShadowMeshBase
     * @return the created SimpleExternalizedShadowMeshBase
     */
    public static SimpleExternalizedShadowMeshBase create(
            NetMeshBaseIdentifier        identifier,
            ExternalizedProxy []         externalizedProxies,
            ExternalizedNetMeshObject [] externalizedNetMeshObjects )
    {
        return new SimpleExternalizedShadowMeshBase(
                identifier,
                externalizedProxies,
                externalizedNetMeshObjects );
    }

    /**
     * Constructor.
     * 
     * @param identifier the NetMeshBaseIdentifier for the ShadowMeshBase
     * @param externalizedProxies externalized representation of the Proxies of the ShadowMeshBase
     * @param externalizedNetMeshObjects externalized representation of the NetMeshObjects held by this ShadowMeshBase
     */
    protected SimpleExternalizedShadowMeshBase(
            NetMeshBaseIdentifier        identifier,
            ExternalizedProxy []         externalizedProxies,
            ExternalizedNetMeshObject [] externalizedNetMeshObjects )
    {
        theNetworkIdentifier          = identifier;
        theExternalizedProxies        = externalizedProxies;
        theExternalizedNetMeshObjects = externalizedNetMeshObjects;
    }

    /**
     * Obtain the ExternalizedProxies.
     *
     * @return the ExternalizedProxies
     */
    public ExternalizedProxy [] getExternalizedProxies()
    {
        return theExternalizedProxies;
    }

    /**
     * Obtain the ExternalizedNetMeshObjects.
     *
     * @return the ExternalizedNetMeshObjects
     */
    public ExternalizedNetMeshObject [] getExternalizedNetMeshObjects()
    {
        return theExternalizedNetMeshObjects;
    }

    /**
     * The ExternalizedProxies.
     */
    protected ExternalizedProxy [] theExternalizedProxies;
    
    /**
     * The ExternalizedProxies.
     */    
    protected ExternalizedNetMeshObject [] theExternalizedNetMeshObjects;
}
