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
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.NotSingleMemberException;
import org.infogrid.util.context.ObjectInContext;
import org.infogrid.util.logging.CanBeDumped;

/**
 * Objects supporting this interface are being used to tell a Viewlet which MeshObjects it is supposed
 * to view, in which context, with which parameters etc.
 */
public interface MeshObjectsToView
        extends
            ObjectInContext,
            CanBeDumped
{
    /**
     * Obtain the subject that the Viewlet is supposed to view.
     *
     * @return the subject
     */
    public MeshObject getSubject();

    /**
     * Obtain the name representing the Viewlet type that the Viewlet is supposed to support.
     *
     * @return the name representing the Viewlet type that the Viewlet is supposed to support
     */
    public String getViewletTypeName();

    /**
     * Set the name representing the Viewlet type that the Viewlet is supposed to support.
     *
     * @param newValue the new value
     */
    public void setViewletTypeName(
            String newValue );

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
     * Set all values of a multi-valued Viewlet parameter.
     *
     * @param name the name of the Viewlet parameter
     * @param values the values
     */
    public void setMultivaluedViewletParameter(
            String    name,
            Object [] values );

    /**
     * Obtain the parameters that the Viewlet is supposed to use.
     *
     * @return the parameters that the Viewlet is supposed to use
     */
    public Map<String,Object[]> getViewletParameters();

    /**
     * Determine how we arrived at this Viewlet, if available. This
     * is most likely a member of the TraversalPathSet of the parent Viewlet.
     *
     * @return the TraversalPath through which we arrived here
     */
    public TraversalPath getArrivedAtPath();

    /**
     * Set how we arrived at this Viewlet.
     *
     * @param newValue the new value
     */
    public void setArrivedAtPath(
            TraversalPath newValue );

    /**
     * Obtain the TraversalSpecification that the Viewlet is supposed to use.
     * 
     * @return the TraversalSpecification that the Viewlet is supposed to use
     */
    public TraversalSpecification getTraversalSpecification();

    /**
     * Set the TraversalSpecification that the Viewlet is supposed to use.
     *
     * @param newValue the new value
     */
    public void setTraversalSpecification(
            TraversalSpecification newValue );

    /**
     * Obtain the reached Objects by means of their TraversalPaths.
     *
     * @return the TraversalPaths
     */
    public TraversalPathSet getTraversalPaths();

    /**
     * Obtain the MeshBase from which the MeshObjects are taken.
     *
     * @return the MeshBase
     */
    public MeshBase getMeshBase();

    /**
     * Obtain a copy of this object. This is helpful to make slight changes to a MeshObjectsToView.
     *
     * @return copy of this object
     */
    public MeshObjectsToView createCopy();
}
