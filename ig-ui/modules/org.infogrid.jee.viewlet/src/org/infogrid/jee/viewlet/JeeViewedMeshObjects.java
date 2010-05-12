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

import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.viewlet.ViewedMeshObjects;

/**
 * Extends ViewedMeshObjects with JEE information.
 */
public interface JeeViewedMeshObjects
        extends
            ViewedMeshObjects
{
    /**
     * Obtain the MIME type of the current response.
     *
     * @return the MIME type
     */
    public String getMimeType();

    /**
     * The current JeeViewletState.
     *
     * @return the current state
     */
    public JeeViewletState getViewletState();

    /**
     * Obtain this ViewedMeshObjects as a URL, i.e. obtain the URL that, when entered into a browser,
     * will recreate this ViewedMeshObjects and render it.
     * If given, use the childArrivedAt argument instead of our current TraversalPath.
     *
     * @param childArrivedAt the TraversalPath, if any, to reach any child of this
     * @return the URL
     */
    public String getAsUrl(
            TraversalPath childArrivedAt );

    /**
     * Obtain the MeshObjectsToView object that was received by the Viewlet, leading to this
     * ViewedMeshObjects.
     *
     * @return the MeshObjectsToView
     */
    public JeeMeshObjectsToView getMeshObjectsToView();
}
