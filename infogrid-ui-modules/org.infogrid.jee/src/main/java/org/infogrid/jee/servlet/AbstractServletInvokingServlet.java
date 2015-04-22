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

package org.infogrid.jee.servlet;

import java.io.IOException;
import javax.servlet.GenericServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.infogrid.util.logging.Log;

/**
 * Abstract servlet that knows how to invoke another servlet.
 */
public abstract class AbstractServletInvokingServlet
        extends
            AbstractInfoGridServlet
{
    private static final long serialVersionUID = 1L; // helps with serialization
    private static final Log  log              = Log.getLogInstance( AbstractServletInvokingServlet.class ); // our own, private logger

    /**
     * Public constructor.
     */
    public AbstractServletInvokingServlet()
    {
        // nothing right now
    }

    /**
     * Main servlet method. This may be overridden by subclasses.
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @throws ServletException thrown if an error occurred
     * @throws IOException thrown if an I/O error occurred
     */
    public void service(
            ServletRequest  request,
            ServletResponse response )
        throws
            ServletException,
            IOException
    {
        invokeServlet( request, response );
    }

    /**
     * Invoke the configured servlet.
     * 
     * @param request the incoming request
     * @param response the outgoing response
     * @throws ServletException thrown if an error occurred
     * @throws IOException thrown if an I/O error occurred
     */
    protected void invokeServlet(
            ServletRequest  request,
            ServletResponse response )
        throws
            ServletException,
            IOException
    {
        RequestDispatcher dispatcher = getServletContext().getNamedDispatcher( theServletName );
        if( dispatcher != null ) {
            dispatcher.include( request, response );
        } else {
            log.error( "Could not find RequestDispatcher (servlet) with name " + theServletName );
        }
    }

    /**
     * Initialize the Servlet.
     *
     * @throws ServletException thrown if misconfigured
     */
    @Override
    public void init()
        throws
            ServletException
    {
        super.init();

        theServletName = super.getInitParameter( SERVLET_NAME_PAR );
        if( theServletName == null || theServletName.length() == 0 ) {
            throw new ServletException( "Must configure servlet " + getClass().getName() + " with servlet parameter " + SERVLET_NAME_PAR + " in web.xml" );
        }
    }

    /**
     * Name of the web.xml parameter that contains the invoked servlet.
     */
    public static String SERVLET_NAME_PAR = "SERVLET_NAME";


    /**
     * Name of the Servlet to invoke.
     */
    protected String theServletName;
}
