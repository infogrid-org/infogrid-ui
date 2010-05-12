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

package org.infogrid.jee.viewlet;

import java.util.Deque;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.viewlet.MeshObjectsToView;

/**
 * Augments MeshObjectsToView for JEE Viewlets.
 */
public interface JeeMeshObjectsToView
        extends
            MeshObjectsToView
{
    /**
     * Obtain the desired JeeViewletState.
     *
     * @return the desired state
     */
    public JeeViewletState getViewletState();

    /**
     * Set the desired JeeViewletState.
     *
     * @param newValue the new value
     */
    public void setViewletState(
            JeeViewletState newValue );

    /**
     * Obtain the desired JeeViewletStateTransition.
     *
     * @return the desired transition
     */
    public JeeViewletStateTransition getViewletStateTransition();

    /**
     * The desired JeeViewletStateTransition.
     *
     * @param newValue the new value
     */
    public void setViewletStateTransition(
            JeeViewletStateTransition newValue );

    /**
     * Obtain the requested MIME type.
     *
     * @return the MIME type
     */
    public String getMimeType();

    /**
     * Set the requested MIME type.
     *
     * @param newValue the new value
     */
    public void setMimeType(
            String newValue );

    /**
     * Obtain the context path of the web application.
     *
     * @return the incoming request
     */
    public String getContextPath();

    /**
     * Obtain this MeshObjectsToView as a URL, i.e. obtain the URL that, when entered into a browser,
     * will recreate this MeshObjectsToView and render it.
     * If given, use the childArrivedAt argument instead of our current TraversalPath.
     *
     * @param childArrivedAt the TraversalPath, if any, to reach any child of this
     * @return the URL
     */
    public String getAsUrl(
            TraversalPath childArrivedAt );

    /**
     * Obtain this MeshObjectsToView as a URL, i.e. obtain the URL that, when entered into a browser,
     * will recreated this MeshObjectsToView and render it as a child of the JeeViewedMeshObjects
     * given.
     * 
     * @param viewedMeshObjectsStack the Stack of ViewedMeshObjects of the parent Viewlets, if any
     * @return the URL
     */
    public String getAsUrl(
            Deque<JeeViewedMeshObjects> viewedMeshObjectsStack );

    /**
     * Obtain a copy of this object. This is helpful to make slight changes to a MeshObjectsToView.
     *
     * @return copy of this object
     */
    @Override
    public JeeMeshObjectsToView createCopy();

    /**
     * Name of the LID format URL argument in the incoming request.
     */
    public static final String LID_FORMAT_ARGUMENT_NAME = "lid-format";

    /**
     * The prefix in the lid-format string that indicates the name of a viewlet.
     */
    public static final String VIEWLET_PREFIX = "viewlet:";

    /**
     * The prefix in the lid-format string that indicates the name of a MIME type.
     */
    public static final String MIME_PREFIX = "mime:";

    /**
     * Name of the LID traversal URL argument in the incoming request.
     */
    public static final String LID_TRAVERSAL_ARGUMENT_NAME = "lid-traversal";
}
