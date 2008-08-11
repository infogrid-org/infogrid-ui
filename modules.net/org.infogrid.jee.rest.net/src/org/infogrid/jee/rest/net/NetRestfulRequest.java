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

package org.infogrid.jee.rest.net;

import java.net.URISyntaxException;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;

/**
 * Encapsulates parameter parsing according to InfoGrid REST conventions.
 * The extraction is only being performed when needed.
 */
public interface NetRestfulRequest
        extends
            RestfulRequest
{
    /**
     * Determine the identifier of the requested Proxy, if any.
     * 
     * @return the NetMeshIdentifier
     */
    public NetMeshBaseIdentifier determineRequestedProxyIdentifier()
        throws
            MeshObjectAccessException,
            NotPermittedException,
            URISyntaxException;
    
    /**
     * Determine the requested Proxy, if any.
     * 
     * @return the Proxy
     */
    public Proxy determineRequestedProxy()
        throws
            MeshObjectAccessException,
            NotPermittedException,
            URISyntaxException;
}
