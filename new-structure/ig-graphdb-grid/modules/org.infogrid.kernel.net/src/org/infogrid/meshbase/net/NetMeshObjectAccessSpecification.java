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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.meshbase.net;

import org.infogrid.mesh.net.NetMeshObjectIdentifier;

/**
 * <p>A path to a remote NetMeshObject, comprised of zero or more NetMeshBaseIdentifiers.
 * It can be roughly compared to a UUCP path, or a server trace in a mail message.</p>
 */
public interface NetMeshObjectAccessSpecification
{
    /**
     * Obtain the factory that created this object.
     *
     * @return the factory
     */
    public NetMeshObjectAccessSpecificationFactory getFactory();

    /**
     * Obtain the NetMeshBaseAccessSpecification path.
     * 
     * @return the path we traverse to the NetMeshObject we want to access. May be of length 0.
     */
    public NetMeshBaseAccessSpecification [] getAccessPath();

    /**
     * Obtain the Identifier of the NetMeshObject that we are looking for in the remote NetMeshBase.
     * Calculate it if it is the default.theNetMeshObjectAccessSpecification
     *
     * @return the Identifier of the NetMeshObject that we are looking for
     */
    public NetMeshObjectIdentifier getNetMeshObjectIdentifier();

    /**
     * Obtain an externalized version of this NetMeshObjectAccessSpecification.
     * 
     * @return external form of this NetMeshObjectAccessSpecification similar to URL.toExternalForm()
     */
    public String toExternalForm();

   /**
     * To save memory, this constant is allocated here and used wherever appropriate.
     */
    public static final NetMeshObjectAccessSpecification [] EMPTY_ARRAY = {};
}
