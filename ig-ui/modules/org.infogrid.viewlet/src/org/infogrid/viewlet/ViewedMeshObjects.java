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

import java.util.Map;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.NotSingleMemberException;

/**
 * A collection of MeshObjects, plus information about their context,
 * that are being viewed by a Viewlet. It is similar to {@link MeshObjectsToView MeshObjectsToView},
 * but MeshObjectsToView is a specification looking for a good Viewlet, while ViewedMeshObjects
 * captures what a chosen Viewlet currently actually does.
 */
public interface ViewedMeshObjects
{
    /**
     * Obtain the subject of the Viewlet. As long as the Viewlet displays any
     * information whatsoever, this is non-null.
     *
     * @return the subject of the Viewlet
     */
    public MeshObject getSubject();

    /**
     * Obtain the Viewlet by which these MeshObjects are viewed.
     *
     * @return the Viewlet
     */
    public Viewlet getViewlet();

    /**
     * Obtain the parameters of the viewing Viewlet.
     *
     * @return the parameters of the viewing Viewlet. This may be null.
     */
    public Map<String,Object[]> getViewletParameters();

    /**
     * Obtain the value of a named Viewlet parameter.
     *
     * @param name the name of the Viewlet parameter
     * @return the value, if any
     * @throws NotSingleMemberException if a Viewlet parameter had more than one value
     */
    public Object getViewletParameter(
            String name )
        throws
            NotSingleMemberException;

    /**
     * Obtain all values of a multi-valued Viewlet parameter.
     *
     * @param name the name of the Viewlet parameter
     * @return the values, if any
     */
    public Object [] getMultivaluedViewletParameter(
            String name );

    /**
     * Determine how we arrived at this Viewlet, if available. This
     * is most likely a member of the TraversalPathSet of the parent Viewlet.
     *
     * @return the TraversalPath through which we arrived here
     */
    public TraversalPath getArrivedAtPath();

    /**
     * Obtain the TraversalSpecification that the Viewlet currently uses.
     * 
     * @return the TraversalSpecification that the Viewlet currently uses
     */
    public TraversalSpecification getTraversalSpecification();

    /**
     * Obtain the set of TraversalPaths that the Viewlet currently uses, if any.
     *
     * @return the TraversalPathSet
     */
    public TraversalPathSet getTraversalPathSet();

    /**
     * Obtain the Objects, i.e. the MeshObjects reached by traversing from the
     * Subject via the TraversalSpecification.
     * 
     * @return the Objects, or null
     */
    public MeshObjectSet getReachedObjects();

    /**
     * Obtain the MeshBase from which the viewed MeshObjects are taken.
     *
     * @return the MeshBase
     */
    public MeshBase getMeshBase();
}
