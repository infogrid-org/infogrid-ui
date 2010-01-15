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
 * Representation of a ShadowMeshBase that can be easily serialized and deserialized.
 */
public interface ExternalizedShadowMeshBase
{
    /**
     * Obtain the NetMeshBaseIdentifier of the ShadowMeshBase.
     * 
     * @return the NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getNetworkIdentifier();

    /**
     * Obtain the ExternalizedProxies.
     *
     * @return the ExternalizedProxies
     */
    public ExternalizedProxy [] getExternalizedProxies();

    /**
     * Obtain the ExternalizedNetMeshObjects.
     *
     * @return the ExternalizedNetMeshObjects
     */
    public ExternalizedNetMeshObject [] getExternalizedNetMeshObjects();
}
