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

package org.infogrid.jee.templates.servlet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.UnsafePostException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseTemplate;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.SimpleHtmlLocalizedObjectFormatter;
import org.infogrid.util.SimplePlainLocalizedObjectFormatter;

/**
 * Captures functionality common to dispatcher implementations.
 */
public abstract class AbstractDispatcherServlet
        extends
            GenericServlet
{
    /**
     * Constructor for subclasses only.
     */
    protected AbstractDispatcherServlet()
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
    public final void service(
            ServletRequest  request,
            ServletResponse response )
        throws
            ServletException,
            IOException
    {
        InfoGridWebApp      app           = InfoGridWebApp.getSingleton();
        HttpServletResponse realResponse  = (HttpServletResponse) response;
        SaneServletRequest  saneRequest   = (SaneServletRequest)  request.getAttribute( SaneServletRequest.class.getName() );
        StructuredResponse structured     = createStructuredResponse( realResponse );

        try {
            performService( saneRequest, structured );

        } catch( Throwable ex ) {
            structured.reportProblem( ex );
        }

        // insert all error messages
        @SuppressWarnings( "unchecked" )
        List<Throwable> problems = (List<Throwable>) request.getAttribute( InfoGridWebApp.PROCESSING_PROBLEM_EXCEPTION_NAME );

        if( problems != null ) {
            for( Throwable current : problems ) {
                structured.reportProblem( current );
            }
        }
        
        try {
            StructuredResponseTemplateFactory templateFactory = app.getApplicationContext().findContextObjectOrThrow( StructuredResponseTemplateFactory.class );
            StructuredResponseTemplate        template        = templateFactory.obtainFor( saneRequest, structured );

            template.doOutput( realResponse, structured );

        } catch( FactoryException ex ) {
            throw new ServletException( ex );
        }
    }

    /**
     * Overridable method to create a structured response.
     * 
     * @param realResponse the underlying HttpServletResponse
     * @return the created StructuredResponse
     */
    protected StructuredResponse createStructuredResponse(
            HttpServletResponse realResponse )
    {
        ServletContext     servletContext = getServletContext();
        StructuredResponse ret            = StructuredResponse.create(
                realResponse,
                SimpleHtmlLocalizedObjectFormatter.create(),
                SimplePlainLocalizedObjectFormatter.create(),
                servletContext );
        return ret;
    }

    /**
     * Do the work. This may be overridden by subclasses.
     *
     * @param request the incoming request
     * @param response the outgoing structured response
     * @throws URISyntaxException thrown if a URI parsing error occurred
     * @throws UnsafePostException thrown if an unsafe POST operation was not acceptable
     * @throws ServletException thrown if an error occurred
     * @throws IOException thrown if an I/O error occurred
     */
    protected abstract void performService(
            SaneServletRequest request,
            StructuredResponse response )
        throws
            URISyntaxException,
            UnsafePostException,
            ServletException,
            IOException;
}
