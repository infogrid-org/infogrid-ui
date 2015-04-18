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

import java.util.HashMap;
import java.util.Map;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneUrl;

/**
 * Augments MeshObjectsToView for JEE Viewlets.
 */
public class DefaultJeeMeshObjectsToView
        extends
            AbstractJeeMeshObjectsToView
{
    /**
     * Private constructor, use factory class.
     * 
     * @param subject the subject for the Viewlet
     * @param viewletTypeName the type of Viewlet to use
     * @param viewletParameters the Viewlet parameters (eg size, zoom, ...) to use
     * @param arrivedAtPath the TraversalPath by which we arrived here, if any
     * @param traversalSpecification the TraversalSpecification to apply when viewing the subject
     * @param traversalPaths the TraversalPaths to the highlighted objects
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param state the ViewletState to to assume
     * @param transition the ViewletStateTransition to make
     * @param mimeType the requested MIME type, if any
     * @param contextPath context path of the web application
     * @param request the incoming request from which this MeshObjectsToView was parsed
     * @param c the Context
     */
    protected DefaultJeeMeshObjectsToView(
            MeshObject                subject,
            String                    viewletTypeName,
            Map<String,Object[]>      viewletParameters,
            TraversalPath             arrivedAtPath,
            TraversalSpecification    traversalSpecification,
            TraversalPathSet          traversalPaths,
            MeshBase                  mb,
            JeeViewletState           state,
            JeeViewletStateTransition transition,
            String                    mimeType,
            String                    contextPath,
            Context                   c,
            SaneUrl                   request )
    {
        super(  subject,
                viewletTypeName,
                viewletParameters,
                arrivedAtPath,
                traversalSpecification,
                traversalPaths,
                mb,
                state,
                transition,
                mimeType,
                contextPath,
                c,
                request );
    }

    /**
     * Obtain a copy of this object. This is helpful to make slight changes to a MeshObjectsToView.
     *
     * @return copy of this object
     */
    public DefaultJeeMeshObjectsToView createCopy()
    {
        HashMap<String,Object[]> paramsCopy = null;
        if( theViewletParameters != null ) {
            paramsCopy = new HashMap<String,Object[]>();
            paramsCopy.putAll( theViewletParameters );
        }

        DefaultJeeMeshObjectsToView ret = new DefaultJeeMeshObjectsToView(
                theSubject,
                theViewletTypeName,
                paramsCopy,
                theArrivedAtPath,
                theTraversalSpecification,
                theTraversalPaths,
                theMeshBase,
                theState,
                theTransition,
                theMimeType,
                theContextPath,
                getContext(),
                theRequest );

        if( getClass() != ret.getClass() ) {
            throw new UnsupportedOperationException( "Did you forget to override createCopy() in class: " + getClass().getName() );
        }
        return ret;
    }
}
