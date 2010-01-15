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

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

/**
 * Factors out functionality common to ExternalizedShadowMeshBase implementations.
 */
public abstract class AbstractExternalizedShadowMeshBase
        implements
            ExternalizedShadowMeshBase
{
    /**
     * Constructor.
     */
    protected AbstractExternalizedShadowMeshBase()
    {
        // noop
    }
    
    /**
     * Obtain the NetMeshBaseIdentifier of the ShadowMeshBase.
     * 
     * @return the NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getNetworkIdentifier()
    {
        return theNetworkIdentifier;
    }

    /**
     * The NetMeshBaseIdentifier of the ShadowMeshBase's data source.
     */
    protected NetMeshBaseIdentifier theNetworkIdentifier;
}
