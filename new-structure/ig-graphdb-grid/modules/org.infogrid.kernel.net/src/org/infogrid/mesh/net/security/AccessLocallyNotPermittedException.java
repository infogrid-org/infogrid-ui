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
package org.infogrid.mesh.net.security;

import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;

/**
 * Indicates that accessLocally() was not permitted.
 */
public class AccessLocallyNotPermittedException
        extends
            NotPermittedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param mb the MeshBase in which this Exception was created
     * @param originatingMeshBaseIdentifier the MeshBaseIdentifier of the MeshBase in which this Exception was created
     * @param failedPaths the NetMeshObjectAccessSpecification that were attempted, but failed
     */
    public AccessLocallyNotPermittedException(
            MeshBase                            mb,
            MeshBaseIdentifier                  originatingMeshBaseIdentifier,
            NetMeshObjectAccessSpecification [] failedPaths )
    {
        super( mb, originatingMeshBaseIdentifier, null, null );
        
        theFailedPaths = failedPaths;
    }
    
     /**
     * Obtain the failed NetMeshObjectAccessSpecifications.
     *
     * @return the NetMeshObjectAccessSpecifications
     */
    public NetMeshObjectAccessSpecification [] getFailedAccessSpecifications()
    {
        return theFailedPaths;
    }
    
    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    @Override
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theOriginatingMeshBaseIdentifier, theFailedPaths };
    }

    /**
     * The failed paths.
     */
    protected NetMeshObjectAccessSpecification [] theFailedPaths;
}
