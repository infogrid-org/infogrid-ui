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

package org.infogrid.jee.viewlet;

import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.viewlet.DefaultViewedMeshObjects;
import org.infogrid.viewlet.MeshObjectsToView;

/**
 * Extends DefaultViewedMeshObjects with JEE information.
 */
public class DefaultJeeViewedMeshObjects
        extends
            DefaultViewedMeshObjects
        implements
            JeeViewedMeshObjects
{
    /**
     * Constructor, specifying a default TraversalSpecification.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param traversal the default TraversalSpecification
     */
    public DefaultJeeViewedMeshObjects(
            MeshBase               mb,
            TraversalSpecification traversal )
    {
        super( mb, traversal );
    }

    /**
     * Constructor. Initializes to empty content.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     */
    public DefaultJeeViewedMeshObjects(
            MeshBase mb )
    {
        super( mb );
    }

    /**
     * Through this convenience method, the Viewlet that this object belongs to updates this object.
     *
     * @param newObjectsToView the new objects accepted to be viewed by the Viewlet
     */
    @Override
    public void updateFrom(
            MeshObjectsToView newObjectsToView )
    {
        JeeMeshObjectsToView realNewObjectsToView = (JeeMeshObjectsToView) newObjectsToView;

        super.updateFrom( realNewObjectsToView );

        theMimeType = realNewObjectsToView.getMimeType();
        theState    = realNewObjectsToView.getViewletState();

        if( theState == null ) {
            theState = DefaultJeeViewletStateEnum.VIEW;
        }
        JeeViewletStateTransition newTransition = realNewObjectsToView.getViewletStateTransition();
        if( newTransition != null ) {
            performStateTransition( newTransition );
        }
    }

    /**
     * Overridable method to perform a state transition.
     *
     * @param newTransition the transition to perform
     */
    protected void performStateTransition(
            JeeViewletStateTransition newTransition )
    {
        if( newTransition.getNextState() != null ) {
            theState = newTransition.getNextState();
        }
    }

    /**
     * Obtain the MIME type of the current response.
     *
     * @return the MIME type
     */
    public String getMimeType()
    {
        return theMimeType;
    }

    /**
     * The current JeeViewletState.
     *
     * @return the current state
     */
    public JeeViewletState getViewletState()
    {
        return theState;
    }

    /**
     * Obtain this ViewedMeshObjects as a URL, i.e. obtain the URL that, when entered into a browser,
     * will recreate this ViewedMeshObjects and render it.
     * If given, use the childArrivedAt argument instead of our current TraversalPath.
     *
     * @param childArrivedAt the TraversalPath, if any, to reach any child of this
     * @return the URL
     */
    public String getAsUrl(
            TraversalPath childArrivedAt )
    {
        return ((JeeMeshObjectsToView) theMeshObjectsToView).getAsUrl( childArrivedAt );
    }

    /**
     * Obtain the MeshObjectsToView object that was received by the Viewlet, leading to this
     * ViewedMeshObjects.
     *
     * @return the MeshObjectsToView
     */
    @Override
    public JeeMeshObjectsToView getMeshObjectsToView()
    {
        return (JeeMeshObjectsToView) super.getMeshObjectsToView();
    }

    /**
     * The current MIME type.
     */
    protected String theMimeType;

    /**
     * The current viewlet state, if any.
     */
    protected JeeViewletState theState;
}
