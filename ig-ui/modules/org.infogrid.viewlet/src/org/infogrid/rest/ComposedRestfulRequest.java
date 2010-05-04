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

package org.infogrid.rest;

import java.text.ParseException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * A RestfulRequest that is explicitly composed from its components.
 */
public class ComposedRestfulRequest
        extends
            AbstractRestfulRequest
        implements
            CanBeDumped
{
    /**
     * Factory method.
     *
     * @param lidRequest the underlying incoming SaneRequest
     * @param defaultMeshBaseIdentifier the identifier of the default MeshBase
     * @param requestedMeshBaseIdentifier the identifier of the requested MeshBase
     * @param requestedMeshObject the requested MeshObject
     * @param requestedMeshObjectIdentifier identifier of the requested MeshObject
     * @param requestedMimeType requested MIME type
     * @param requestedViewletTypeName name of the requested Viewlet type
     * @return the created ComposedRestfulRequest
     */
    public static ComposedRestfulRequest create(
            SaneRequest          lidRequest,
            MeshBaseIdentifier   defaultMeshBaseIdentifier,
            MeshBaseIdentifier   requestedMeshBaseIdentifier,
            MeshObject           requestedMeshObject,
            MeshObjectIdentifier requestedMeshObjectIdentifier,
            String               requestedMimeType,
            String               requestedViewletTypeName )
    {
        return new ComposedRestfulRequest(
                lidRequest,
                defaultMeshBaseIdentifier,
                requestedMeshBaseIdentifier,
                requestedMeshObject,
                requestedMeshObjectIdentifier,
                requestedMimeType,
                requestedViewletTypeName );
    }

    /**
     * Constructor.
     *
     * @param lidRequest the underlying incoming SaneRequest
     * @param defaultMeshBaseIdentifier the identifier of the default MeshBase
     * @param requestedMeshBaseIdentifier the identifier of the requested MeshBase
     * @param requestedMeshObject the requested MeshObject
     * @param requestedMeshObjectIdentifier identifier of the requested MeshObject
     * @param requestedMimeType requested MIME type
     * @param requestedViewletTypeName name of the requested Viewlet type
     */
    protected ComposedRestfulRequest(
            SaneRequest          lidRequest,
            MeshBaseIdentifier   defaultMeshBaseIdentifier,
            MeshBaseIdentifier   requestedMeshBaseIdentifier,
            MeshObject           requestedMeshObject,
            MeshObjectIdentifier requestedMeshObjectIdentifier,
            String               requestedMimeType,
            String               requestedViewletTypeName )
    {
        super( lidRequest, defaultMeshBaseIdentifier );

        theRequestedMeshBaseIdentifier   = requestedMeshBaseIdentifier;
        theRequestedMeshObject           = requestedMeshObject;
        theRequestedMeshObjectIdentifier = requestedMeshObjectIdentifier;
        theRequestedMimeType             = requestedMimeType;
        theRequestedViewletTypeName      = requestedViewletTypeName;
    }

    /**
     * Obtain the name of the requested Viewlet type, if any.
     *
     * @return type name of the requested Viewlet
     */
    @Override
    public String getRequestedViewletTypeName()
    {
        return theRequestedViewletTypeName; // only return what was set
    }

    /**
     * Obtain the requested MIME type, if any.
     *
     * @return the requested MIME type, if any
     */
    @Override
    public String getRequestedMimeType()
    {
        return theRequestedMimeType; // only return what was set
    }

    /**
     * Internal method to calculate the data.
     *
     * @throws MeshObjectAccessException thrown if the requested MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller did not have the permission to perform this operation
     * @throws ParseException thrown if the request URI could not be parsed
     */
    protected void calculate()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                ParseException
    {
        // does nothing here
        throw new UnsupportedOperationException(); // we missed some part of the implementation
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
                    "theRequestedMeshBaseIdentifier",
                    "theRequestedMeshObjectIdentifier",
                    "getRequestedTraversalParameters()",
                    "theRequestedViewletClass",
                    "theRequestedMimeType"
                },
                new Object[] {
                    theRequestedMeshBaseIdentifier,
                    theRequestedMeshObjectIdentifier,
                    getRequestedTraversalParameters(),
                    theRequestedViewletTypeName,
                    theRequestedMimeType
                });
    }
}
