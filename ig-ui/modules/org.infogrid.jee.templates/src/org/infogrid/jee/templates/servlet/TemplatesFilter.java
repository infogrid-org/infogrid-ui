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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.ProblemReporter;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.servlet.BufferedServletResponse;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseTemplate;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.ContextDirectory;
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
        StructuredResponse  structured    = createStructuredResponse( saneRequest, realResponse );

        request.setAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME, structured );

        Context c          = app.getApplicationContext();
        String  reqContext = saneRequest.getUrlArgument( LID_APPLICATION_CONTEXT_PARAMETER_NAME );

        if( reqContext != null ) {
            ContextDirectory dir   = app.getContextDirectory();
            Context          found = dir.getContext( reqContext );
            if( found != null ) {
                c = found;

                request.setAttribute( LID_APPLICATION_CONTEXT_PARAMETER_NAME, found );
            }
        }

        BufferedServletResponse bufferedResponse = new BufferedServletResponse( realResponse );
        Throwable               lastException    = null;
        try {
            chain.doFilter( request, bufferedResponse );

        } catch( Throwable ex ) {
            lastException = ex;

            Log l = getLog();
            if( l != null ) { // catastrophic errors sometimes even prevent logging from being initialized
                l.error( ex );
            } else {
                ex.printStackTrace();
            }

        } finally {
            request.removeAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
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
                getLog().warn( "Have both responses: " + structured + " vs. " + bufferedResponse );
                // will ignore bufferedResponse and only process structuredResponse
            }

            for( String name : bufferedResponse.getHttpHeaderKeySet() ) {
                String [] values = bufferedResponse.getHttpHeaderValues( name );
                for( String current : values ) {
                    realResponse.addHeader( name, current );
                }
            }
            for( Cookie current : bufferedResponse.getCookies() ) {
                realResponse.addCookie( current );
            }

            try {
                StructuredResponseTemplateFactory templateFactory = c.findContextObjectOrThrow( StructuredResponseTemplateFactory.class );
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
     * @param request the incoming request
     * @param realResponse the underlying HttpServletResponse
     * @return the created StructuredResponse
     */
    protected StructuredResponse createStructuredResponse(
            SaneRequest         request,
            HttpServletResponse realResponse )
    {
        ServletContext     servletContext = theFilterConfig.getServletContext();
        StructuredResponse ret            = StructuredResponse.create( realResponse, servletContext );

        request.setAttribute( ProblemReporter.PROBLEM_REPORTER_ATTRIBUTE_NAME, ret );

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
     * Initialize and get the log.
     *
     * @return the log
     */
    private static Log getLog()
    {
        if( log == null ) {
            log = Log.getLogInstance( TemplatesFilter.class ); // our own, private logger
        }
        return log;
    }

    /**
     * The Filter configuration object.
     */
    protected FilterConfig theFilterConfig;

    /**
     * Name of the LID application context parameter. For simplicity, this is used for two purposes:
     * <ol>
     *  <li>URL argument, to indicate a String representation of a context</li>
     *  <li>request parameter, to capture the Context that was determined by resolving the String representation
     * </ol>
     */
    public static final String LID_APPLICATION_CONTEXT_PARAMETER_NAME = "lid-appcontext";
}
