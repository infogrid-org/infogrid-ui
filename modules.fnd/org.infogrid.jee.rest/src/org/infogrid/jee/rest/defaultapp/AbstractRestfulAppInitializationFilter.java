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

package org.infogrid.jee.rest.defaultapp;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.security.FormTokenService;
import org.infogrid.jee.security.m.MFormTokenService;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.mesh.text.MeshStringRepresentationContext;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationContext;

/**
 * Common functionality of application initialization filters that are REST-ful.
 */
public abstract class AbstractRestfulAppInitializationFilter
        implements
            Filter
{
    private static final Log log = Log.getLogInstance( AbstractRestfulAppInitializationFilter.class ); // our own, private logger

    /**
     * Constructor.
     */
    protected AbstractRestfulAppInitializationFilter()
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
        Context appContext = InfoGridWebApp.getSingleton().getApplicationContext();

        synchronized( AbstractRestfulAppInitializationFilter.class ) {
            if( !isInitialized ) {
                try {
                    initialize( request, response );
                } catch( Throwable t ) {

                    log.error( t );

                    StructuredResponse structured = (StructuredResponse) request.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
                    if( structured != null ) {
                        structured.reportProblem( t );
                    } else {
                        throw new ServletException( t );
                    }
                    // Fix whatever we can if something went wrong
                    // want some kind of FormTokenService even if initialization failed
                    if( appContext.findContextObject( FormTokenService.class ) == null ) {
                        MFormTokenService formTokenService = MFormTokenService.create();
                        appContext.addContextObject( formTokenService );
                    }
                } finally {
                    isInitialized = true;
                }
            }
        }

        StringRepresentationContext stringRepContext
                = (StringRepresentationContext) request.getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );
        MeshBase mb
                = appContext.findContextObject( MeshBase.class );

        if( stringRepContext != null && mb != null ) {
            stringRepContext.put( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY, mb );
        }

        chain.doFilter( request, response );
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

        theDefaultMeshBaseIdentifier = filterConfig.getInitParameter( DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER_NAME );
    }

    /**
     * Initialize the initial content of the MeshBase.
     *
     * @param mb the MeshBase to initialize
     */
    protected void populateMeshBase(
            MeshBase mb )
    {
        // nothing on this level
    }

    /**
     * Initialize context objects.
     *
     * @param context the Context
     */
    protected void initializeContextObjects(
            Context context )
    {
        // nothing on this level
    }

    /**
     * Destroy method for this Filter.
     */
    public void destroy()
    {
        // noop
    }

    /**
     * The filter configuration object this Filter is associated with.
     */
    protected FilterConfig theFilterConfig = null;

    /**
     * Have the Stores been successfully initialized.
     */
    protected boolean isInitialized = false;

    /**
     * Identifier of the main MeshBase.
     */
    protected String theDefaultMeshBaseIdentifier;

    /**
     * Name of the Filter parameter in web.xml that contains the identifier of the main MeshBase.
     */
    public static final String DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER_NAME = "DefaultMeshBaseIdentifier";
}
