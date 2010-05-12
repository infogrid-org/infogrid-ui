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
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.context.ObjectInContext;

/**
 * <p>A software component of an InfoGrid application's user interface.
 *    Conceptually, the user interface of an InfoGrid application consists of Viewlets.
 *    Viewlets are abstract concepts with a common API that can be implemented in a variety
 *    of technologies, including Java servlets and Java Swing components.
 * <p>A Viewlet typically has a subject, which is given as a <code>MeshObject</code>. For example,
 *    a Viewlet showing an electronic business card might have the owner of the business card
 *    as the subject.</p>
 * <p>The <code>Viewlet</code> interface is supported by all InfoGrid Viewlets. More specific
 *    subtypes are provided for specific implementation technologies, such as JSE and JEE.</p>
 */
public interface Viewlet
        extends
            ObjectInContext
{
    /**
      * Obtain a String, to be shown to the user, that identifies this Viewlet to the user.
      *
      * @return a String
      */
    public String getUserVisibleName();

    /**
     * Obtain the computable name of the Viewlet.
     * 
     * @return the Viewet's name
     */
    public abstract String getName();

    /**
      * The Viewlet is being instructed to view certain objects, which are packaged as
      * {@link MeshObjectsToView MeshObjectsToView}.
      *
      * @param toView the MeshObjects to view
      * @throws CannotViewException thrown if this Viewlet cannot view these MeshObjectsToView
      */
    public void view(
            MeshObjectsToView toView )
        throws
            CannotViewException;
    
    /**
     * Obtain the REST-ful subject. This is a convenience method as it only delegates to
     * the ViewedMeshObjects.
     *
     * @return the subject
     */
    public MeshObject getSubject();

    /**
      * Obtain the MeshObjects that this Viewlet is currently viewing, plus
      * context information. This method will return the same instance of ViewedMeshObjects
      * during the lifetime of the Viewlet.
      *
      * @return the ViewedMeshObjects
      */
    public ViewedMeshObjects getViewedMeshObjects();
}
