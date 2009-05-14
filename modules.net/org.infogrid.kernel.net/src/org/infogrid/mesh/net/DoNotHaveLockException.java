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

package org.infogrid.mesh.net;

import org.infogrid.mesh.MeshObjectGraphModificationException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * This Exception indicates that an attempt was made to move update rights
 * from a local NetMeshObject to one at a different NetMeshBase; however, the
 * local replica did not have update rights itself.
 */
public class DoNotHaveLockException
        extends
            MeshObjectGraphModificationException
        implements
            CanBeDumped
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param mb the NetMeshBase in which this Exception was created
     * @param originatingMeshBaseIdentifier the NetMeshBaseIdentifier of the NetMeshBase in which this Exception was created
     * @param meshObject the NetMeshObject that did not have update rights, if available
     * @param meshObjectIdentifier the NetMeshObjectIdentifier for the NetMeshObject that did not have update rights
     */
    public DoNotHaveLockException(
            NetMeshBase             mb,
            NetMeshBaseIdentifier   originatingMeshBaseIdentifier,
            NetMeshObject           meshObject,
            NetMeshObjectIdentifier meshObjectIdentifier )
    {
        super( mb, originatingMeshBaseIdentifier );
        
        theMeshObject           = meshObject;
        theMeshObjectIdentifier = meshObjectIdentifier;
    }

    /**
     * More convenient simple constructor for the most common case.
     *
     * @param meshObject the NetMeshObject that did not have update rights
     */
    public DoNotHaveLockException(
            NetMeshObject meshObject )
    {
        this(   meshObject.getMeshBase(),
                meshObject.getMeshBase().getIdentifier(),
                meshObject,
                meshObject.getIdentifier() );
    }

    /**
     * Obtain the NetMeshObject that did not have update rights.
     * 
     * @return the NetMeshObject
     * @throws MeshObjectAccessException thrown if the NetMeshObject could not be found
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     * @throws IllegalStateException thrown if no resolving MeshBase is available
     */
    public synchronized NetMeshObject getMeshObject()
        throws
            MeshObjectAccessException,
            NotPermittedException,
            IllegalStateException
    {
        if( theMeshObject == null ) {
            theMeshObject = (NetMeshObject) resolve( theMeshObjectIdentifier );
        }
        return theMeshObject;
    }

    /**
      * Obtain the NetMeshObjectIdentifier of the NetMeshObject that did not have update rights.
      *
      * @return the NetMeshObjectIdentifier
      */
    public NetMeshObjectIdentifier getMeshObjectIdentifier()
    {
        return theMeshObjectIdentifier;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theMeshObject",
                    "theMeshObjectIdentifier"
                },
                new Object[] {
                    theMeshObject,
                    theMeshObjectIdentifier
                });
    }

    /**
     * Obtain parameters for the internationalization.
     *
     * @return the parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theMeshObjectIdentifier };
    }

    /**
     * The NetMeshObject that did not have update rights.
     */
    protected transient NetMeshObject theMeshObject;

    /**
     * The NetMeshObjectIdentifier of the NetMeshObject that did not have update rights.
     */
    protected NetMeshObjectIdentifier theMeshObjectIdentifier;
}
