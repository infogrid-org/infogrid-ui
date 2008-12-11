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
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.servlet.BufferedServletResponse;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseTemplate;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * <p>Creates and processes StructuredResponseTemplates according to
 *    the InfoGrid web template framework.</p>
 */
public class TemplatesFilter
        implements
            Filter
{
    private static Log log; // this requires delayed initialization

    /**
     * Constructor.
     */
    public TemplatesFilter()
    {
        if( log == null ) {
            log = Log.getLogInstance( TemplatesFilter.class ); // our own, private logger
        }
    }

    /**
     * Main filter method.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    public void doFilter(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        HttpServletRequest  realRequest  = (HttpServletRequest)  request;
        HttpServletResponse realResponse = (HttpServletResponse) response;

        InfoGridWebApp      app           = InfoGridWebApp.getSingleton();
        SaneRequest         saneRequest   = SaneServletRequest.create( realRequest );
        StructuredResponse  structured    = createStructuredResponse( realResponse );

        request.setAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME, structured );

        BufferedServletResponse bufferedResponse = new BufferedServletResponse( realResponse );
        Throwable               lastException    = null;
        try {
            chain.doFilter( request, bufferedResponse );

        } catch( Throwable ex ) {
            lastException = ex;
            log.error( ex );

        } finally {
            request.removeAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        }

        // insert all error messages
        @SuppressWarnings( "unchecked" )
        List<Throwable> problems = (List<Throwable>) request.getAttribute( InfoGridWebApp.PROCESSING_PROBLEM_EXCEPTION_NAME );

        if( problems != null ) {
            for( Throwable current : problems ) {
                structured.reportProblem( current );
            }
        }
        if( lastException != null ) {
            structured.reportProblem( lastException );
        }
        
        if( structured.isEmpty() ) {
            // traditional processing, it ignored the StructuredResponse. We simply copy.
            bufferedResponse.copyTo( realResponse );

        } else {
            // process structured response
            if( !bufferedResponse.isEmpty() ) {
                log.warn( "Have both responses: " + structured + " vs. " + bufferedResponse );
                // will ignore bufferedResponse and only process structuredResponse
            }

            try {
                StructuredResponseTemplateFactory templateFactory = app.getApplicationContext().findContextObjectOrThrow( StructuredResponseTemplateFactory.class );
                StructuredResponseTemplate        template        = templateFactory.obtainFor( saneRequest, structured );

                template.doOutput( realResponse, structured );

            } catch( FactoryException ex ) {
                throw new ServletException( ex );
            }
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
        ServletContext     servletContext = theFilterConfig.getServletContext();
        StructuredResponse ret            = StructuredResponse.create( realResponse, servletContext );

        return ret;
    }

    /**
     * Destroy method for this filter.
     */
    public void destroy()
    {
    }
    
    /**
     * Initialization method for this filter.
     * 
     * @param filterConfig the Filter configuration
     */
    public void init(
            FilterConfig filterConfig )
    {
        theFilterConfig = filterConfig;
    }

    /**
     * The Filter configuration object.
     */
    protected FilterConfig theFilterConfig;
}
