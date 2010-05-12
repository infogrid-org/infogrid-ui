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

package org.infogrid.jee.viewlet.net;

import java.util.HashMap;
import java.util.Map;
import org.infogrid.jee.viewlet.DefaultJeeMeshObjectsToView;
import org.infogrid.jee.viewlet.JeeViewletState;
import org.infogrid.jee.viewlet.JeeViewletStateTransition;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.context.Context;

/**
 * Extends DefaultJeeMeshObjectsToView to be able to express that a Proxy shall be viewed instead.
 */
public class DefaultJeeNetMeshObjectsToView
        extends
            DefaultJeeMeshObjectsToView
        implements
            JeeNetMeshObjectsToView
{
    /**
     * Private constructor, use factory class.
     *
     * @param subject the subject for the Viewlet
     * @param proxy the requested Proxy, if any
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
     * @param c the Context
     */
    protected DefaultJeeNetMeshObjectsToView(
            NetMeshObject             subject,
            Proxy                     proxy,
            String                    viewletTypeName,
            Map<String,Object[]>      viewletParameters,
            TraversalPath             arrivedAtPath,
            TraversalSpecification    traversalSpecification,
            TraversalPathSet          traversalPaths,
            NetMeshBase               mb,
            JeeViewletState           state,
            JeeViewletStateTransition transition,
            String                    mimeType,
            String                    contextPath,
            Context                   c )
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
                c );

        theProxy = proxy;
    }

    /**
     * Obtain the subject that the Viewlet is supposed to view.
     *
     * @return the subject
     */
    @Override
    public NetMeshObject getSubject()
    {
        return (NetMeshObject) super.getSubject();
    }

    /**
     * Obtain the MeshBase from which the MeshObjects are taken.
     *
     * @return the MeshBase
     */
    @Override
    public NetMeshBase getMeshBase()
    {
        return (NetMeshBase) super.getMeshBase();
    }

    /**
     * Obtain the Proxy that shall be viewed instead of the regular subject, if any.
     *
     * @return the Proxy
     */
    public Proxy getRequestedProxy()
    {
        return theProxy;
    }

    /**
     * Obtain a copy of this object. This is helpful to make slight changes to a MeshObjectsToView.
     *
     * @return copy of this object
     */
    @Override
    public DefaultJeeNetMeshObjectsToView createCopy()
    {
        HashMap<String,Object[]> paramsCopy = null;
        if( theViewletParameters != null ) {
            paramsCopy = new HashMap<String,Object[]>();
            paramsCopy.putAll( theViewletParameters );
        }

        DefaultJeeNetMeshObjectsToView ret = new DefaultJeeNetMeshObjectsToView(
                (NetMeshObject) theSubject,
                theProxy,
                theViewletTypeName,
                paramsCopy,
                theArrivedAtPath,
                theTraversalSpecification,
                theTraversalPaths,
                (NetMeshBase) theMeshBase,
                theState,
                theTransition,
                theMimeType,
                theContextPath,
                getContext() );

        if( getClass() != ret.getClass() ) {
            throw new UnsupportedOperationException( "Did you forget to override createCopy() in class: " + getClass().getName() );
        }
        return ret;
    }

    /**
     * The requested proxy, if any.
     */
    protected Proxy theProxy;
}
