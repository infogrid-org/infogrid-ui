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

package org.infogrid.jee.viewlet.meshbase.net;

import java.io.IOException;
import javax.servlet.ServletException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.AbstractJeeViewlet;
import org.infogrid.jee.viewlet.DefaultJeeViewedMeshObjects;
import org.infogrid.jee.viewlet.DefaultJeeViewletFactoryChoice;
import org.infogrid.jee.viewlet.JeeViewedMeshObjects;
import org.infogrid.jee.viewlet.net.JeeNetMeshObjectsToView;
import org.infogrid.jee.viewlet.net.NetMeshObjectsToView;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Viewlet that displays one particular Proxy. This is not exactly REST-ful as
 * Proxies aren't nouns in InfoGrid, which is why we have to do a few tricks...
 */
public class ProxyViewlet
        extends
            AbstractJeeViewlet
{
    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static ProxyViewlet create(
            MeshBase mb,
            Context  c )
    {
        DefaultJeeViewedMeshObjects viewed = new DefaultJeeViewedMeshObjects( mb );
        ProxyViewlet                ret    = new ProxyViewlet( viewed, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param toView the MeshObjectsToView for which this is a choice
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            JeeNetMeshObjectsToView toView,
            double                  matchQuality )
    {
        return new DefaultJeeViewletFactoryChoice( toView, ProxyViewlet.class, matchQuality ) {
                public Viewlet instantiateViewlet()
                    throws
                        CannotViewException
                {
                    return create( getMeshObjectsToView().getMeshBase(), getMeshObjectsToView().getContext() );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the JeeViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected ProxyViewlet(
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( viewed, c );
    }

    /**
     * Override processing the incoming request.
     * 
     * @param request the incoming request
     * @param structured the StructuredResponse into which to write the result
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    @Override
    public void processRequest(
            SaneRequest        request,
            StructuredResponse structured )
        throws
            ServletException,
            IOException
    {
        super.processRequest( request, structured );

        NetMeshObjectsToView realToView = (NetMeshObjectsToView) getViewedMeshObjects().getMeshObjectsToView();

        Proxy pseudoSubject = realToView.getRequestedProxy();
        request.setAttribute( "Proxy", pseudoSubject );
        
    }
}
