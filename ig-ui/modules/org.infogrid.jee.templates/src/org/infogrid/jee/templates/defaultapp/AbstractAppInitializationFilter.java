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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.templates.defaultapp;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.util.CompoundException;
import org.infogrid.util.CompoundRuntimeException;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * Common functionality of application initialization filters.
 */
public abstract class AbstractAppInitializationFilter
        implements
            Filter
{
    private static Log log; // because this is a filter, it needs delayed initialization

    /**
     * Constructor.
     */
    protected AbstractAppInitializationFilter()
    {
        // nothing
    }

    /**
     * Execute the filter.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
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
        doFilterInitialize( request, response, chain );
        doFilterPrepare(    request, response, chain );

        chain.doFilter( request, response );
    }

    /**
     * Initialize the Filter, if needed, and handle errors appropriately.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    protected void doFilterInitialize(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        Context appContext = InfoGridWebApp.getSingleton().getApplicationContext();

        synchronized( AbstractAppInitializationFilter.class ) {
            if( !isInitialized ) {
                try {
                    initialize( request, response );

                } catch( Throwable t ) {

                    getLog().error( t );

                    StructuredResponse structured = (StructuredResponse) request.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
                    if( structured != null ) {
                        if( t instanceof AppInitializationException ) {
                            t = ((AppInitializationException)t).getCause();
                        }
                        if( t instanceof CompoundException ) {
                            structured.reportProblems( ((CompoundException)t).getThrowables() );

                        } else if( t instanceof CompoundRuntimeException ) {
                            structured.reportProblems( ((CompoundRuntimeException)t).getThrowables() );

                        } else {
                            structured.reportProblem( t );
                        }
                    } else {
                        throw new ServletException( t );
                    }
                } finally {
                    isInitialized = true;
                }
            }
        }
    }

    /**
     * <p>Perform initialization.</p>
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @throws Throwable something bad happened that cannot be fixed by re-invoking this method
     */
    protected abstract void initialize(
            ServletRequest  request,
            ServletResponse response )
        throws
            Throwable;

    /**
     * Set up the request before performing the delegation to the chain.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    protected void doFilterPrepare(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        // by default, do nothing
    }

    /**
     * Initialize the Filter.
     *
     * @param filterConfig the Filter configuration object
     * @throws ServletException thrown if misconfigured
     */
    public void init(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        theFilterConfig  = filterConfig;
    }

    /**
     * Destroy method for this Filter.
     */
    public void destroy()
    {
        // noop
    }

    /**
     * Initialize and get the log.
     *
     * @return the log
     */
    private static Log getLog()
    {
        if( log == null ) {
            log = Log.getLogInstance( AbstractAppInitializationFilter.class ); // our own, private logger
        }
        return log;
    }

    /**
     * The filter configuration object this Filter is associated with.
     */
    protected FilterConfig theFilterConfig = null;

    /**
     * Have the Stores been successfully initialized.
     */
    protected boolean isInitialized = false;
}
