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
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet;

import org.infogrid.util.context.Context;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewedMeshObjects;

/**
 * Very simple implementation of Viewlet that provides no special features. It
 * uses a path parameter, to be given upon creation, for the content Servlet.
 */
public class SimpleJeeViewlet
        extends
            AbstractJeeViewlet
{
    /**
     * Factory method.
     *
     * @param path the path for the RequestDispatcher
     * @param c the application context
     * @return the created Viewlet
     */
    public static SimpleJeeViewlet create(
            String  path,
            Context c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects();
        SimpleJeeViewlet         ret    = new SimpleJeeViewlet( path, viewed, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected SimpleJeeViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        this( null, viewed, c );
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param path the path for the RequestDispatcher
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected SimpleJeeViewlet(
            String                    path,
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );

        thePath = path;
    }

    /**
     * Obtain the path to the Servlet for this JeeViewlet.
     * 
     * @return the Servlet path
     */
    @Override
    public String getServletPath()
    {
        if( thePath != null ) {
            return thePath;
        } else {
            return super.getServletPath();
        }
    }

    /**
     * The path to the servlet.
     */
    protected String thePath;
}
