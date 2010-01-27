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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.rest.net;

import java.text.ParseException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.rest.RestfulRequest;

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
     * @return the NetMeshBaseIdentifier
     * @throws MeshObjectAccessException thrown if a MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller was not permitted to perform this operation
     * @throws ParseException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier determineRequestedProxyIdentifier()
        throws
            MeshObjectAccessException,
            NotPermittedException,
            ParseException;
    
    /**
     * Determine the requested Proxy, if any.
     * 
     * @return the Proxy
     * @throws MeshObjectAccessException thrown if a MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller was not permitted to perform this operation
     * @throws ParseException thrown if the MeshBaseIdentifier passed into the constructor could not be parsed
     */
    public Proxy determineRequestedProxy()
        throws
            MeshObjectAccessException,
            NotPermittedException,
            ParseException;
}
