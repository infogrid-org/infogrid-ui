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

package org.infogrid.viewlet;

import org.infogrid.mesh.MeshObject;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.util.Factory;

/**
 * Implemented by all objects that know how to instantiate a subclass of MeshObjectsToView.
 * This is the minimum contract; subclasses of this factory interface may have additional
 * factory methods.
 */
public interface MeshObjectsToViewFactory
        extends
            Factory<MeshObject,MeshObjectsToView,String>
{
    /**
     * Create a MeshObjectsToView that only asks for a subject.
     * Repeated here for clarity.
     *
     * @param subject the subject MeshObject
     * @return the created MeshObjectsToView
     */
    public MeshObjectsToView obtainFor(
            MeshObject subject );

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type.
     * Repeated here for clarity.
     *
     * @param subject the subject MeshObject
     * @param viewletTypeName the name of the Viewlet type
     * @return the created MeshObjectsToView
     */
    public MeshObjectsToView obtainFor(
            MeshObject subject,
            String     viewletTypeName );

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @return the created MeshObjectsToView
     */
    public MeshObjectsToView obtainFor(
            TraversalPath reachedBy );

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @param viewletTypeName the name of the Viewlet type
     * @return the created MeshObjectsToView
     */
    public MeshObjectsToView obtainFor(
            TraversalPath reachedBy,
            String        viewletTypeName );
}
