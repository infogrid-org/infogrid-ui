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


package org.infogrid.lid.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.servlet.AbstractServletInvokingServlet;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.utils.JeeTemplateUtils;
import org.infogrid.lid.DefaultLidProcessingPipeline;
import org.infogrid.lid.LidProcessingPipeline;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Invokes the LidProcessingPipeline.
 */
public class LidProcessingPipelineServlet
        extends
            AbstractServletInvokingServlet
{
    private static final long serialVersionUID = 1L; // helps with serialization
    private static final Log  log              = Log.getLogInstance( LidProcessingPipelineServlet.class ); // our own, private logger

    /**
     * Constructor.
     */
    public LidProcessingPipelineServlet()
    {
        // nothing right now
    }

    /**
     * Main servlet method.
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @throws ServletException thrown if an error occurred
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public final void service(
            ServletRequest  request,
            ServletResponse response )
        throws
            ServletException,
            IOException
    {
        InfoGridWebApp     app         = InfoGridWebApp.getSingleton();
        Context            appContext  = app.getApplicationContext();
        HttpServletRequest realRequest = (HttpServletRequest) request;
        SaneServletRequest lidRequest  = SaneServletRequest.create( realRequest );
        StructuredResponse lidResponse = (StructuredResponse) request.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );

        LidProcessingPipeline pipe = obtainLidProcessingPipeline( appContext );

        try {
            pipe.processPipeline( lidRequest, lidResponse );

            invokeServlet( lidRequest, lidResponse );

        } catch( Throwable ex ) {
            handleException( lidRequest, lidResponse, ex );
        }
    }
    
    /**
     * Overridable method to create the LidProcessingPipeline.
     * 
     * @param c Context for the pipeline
     * @return the created LidProcessingPipeline
     */
    protected LidProcessingPipeline obtainLidProcessingPipeline(
            Context c )
    {
        if( thePipeline == null ) {
            thePipeline = DefaultLidProcessingPipeline.create( c );
        }
        return thePipeline;
    }
    
    /**
     * Invoke the configured servlet. Instead of doing what our superclass does, we use the template framework.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @throws ServletException thrown if an error occurred
     * @throws IOException thrown if an I/O error occurred
     */
    protected void invokeServlet(
            SaneServletRequest lidRequest,
            StructuredResponse lidResponse )
        throws
            ServletException,
            IOException
    {
        RequestDispatcher dispatcher = getServletContext().getNamedDispatcher( theServletName );

        if( dispatcher != null ) {
            JeeTemplateUtils.runRequestDispatcher(
                    dispatcher,
                    lidRequest,
                    lidResponse );
        } else {
            log.error( "Could not find RequestDispatcher (servlet) with name " + theServletName );
        }
    }
    
    /**
     * Overridable method to handle Exceptions thrown by the pipeline processing.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param t the exception
     */
    protected void handleException(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse,
            Throwable          t )
    {
        if( log.isDebugEnabled() ) {
            log.debug( t );
        }
    }

    /**
     * Cached LidProcessingPipeline.
     */
    protected LidProcessingPipeline thePipeline;
}
