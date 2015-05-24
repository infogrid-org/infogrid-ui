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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet.net;

import org.infogrid.jee.viewlet.JeeMeshObjectsToViewFactory;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.util.FactoryException;
import org.infogrid.util.http.SaneUrl;

/**
 * Narrows the returned types of the created objects.
 */
public interface JeeNetMeshObjectsToViewFactory
        extends
            JeeMeshObjectsToViewFactory
{
    /**
     * Create a MeshObjectsToView that only asks for a subject.
     * Repeated here for clarity.
     *
     * @param subject the subject MeshObject
     * @return the created MeshObjectsToView
     */
    public JeeNetMeshObjectsToView obtainFor(
            MeshObject subject );

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type.
     * Repeated here for clarity.
     *
     * @param subject the subject MeshObject
     * @param viewletTypeName the name of the Viewlet type
     * @return the created MeshObjectsToView
     */
    public JeeNetMeshObjectsToView obtainFor(
            MeshObject subject,
            String     viewletTypeName );

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @return the created MeshObjectsToView
     */
    public JeeNetMeshObjectsToView obtainFor(
            TraversalPath reachedBy );

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @param viewletTypeName the name of the Viewlet type
     * @return the created MeshObjectsToView
     */
    public JeeNetMeshObjectsToView obtainFor(
            TraversalPath reachedBy,
            String        viewletTypeName );

    /**
     * Create a MeshObjectsToView that corresponds to the request(s) encloded in this SaneRequest.
     *
     * @param request the SaneUrl
     * @return the created MeshObjectsToView
     * @throws FactoryException thrown if the MeshObjectsToView could not be created
     */
    public JeeNetMeshObjectsToView obtainFor(
            SaneUrl request )
        throws
            FactoryException;

    /**
     * Create a MeshObjectsToView that only asks for a subject.
     * Repeated here for clarity.
     *
     * @param subject the subject MeshObject
     * @param request the SaneUrl of the current request
     * @return the created MeshObjectsToView
     */
    public JeeNetMeshObjectsToView obtainFor(
            MeshObject subject,
            SaneUrl    request );

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type.
     * Repeated here for clarity.
     *
     * @param subject the subject MeshObject
     * @param viewletTypeName the name of the Viewlet type
     * @param request the SaneUrl of the current request
     * @return the created MeshObjectsToView
     */
    public JeeNetMeshObjectsToView obtainFor(
            MeshObject subject,
            String     viewletTypeName,
            SaneUrl    request );

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @param request the SaneUrl of the current request
     * @return the created MeshObjectsToView
     */
    public JeeNetMeshObjectsToView obtainFor(
            TraversalPath reachedBy,
            SaneUrl       request );

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @param viewletTypeName the name of the Viewlet type
     * @param request the SaneUrl of the current request
     * @return the created MeshObjectsToView
     */
    public JeeNetMeshObjectsToView obtainFor(
            TraversalPath reachedBy,
            String        viewletTypeName,
            SaneUrl       request );
}
