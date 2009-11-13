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
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.jee.templates.defaultapp.AbstractAppInitializationFilter;
import org.infogrid.mesh.text.MeshStringRepresentationContext;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Common functionality of application initialization filters that are REST-ful.
 */
public abstract class AbstractRestfulAppInitializationFilter
        extends
            AbstractAppInitializationFilter
{
    /**
     * Constructor.
     */
    protected AbstractRestfulAppInitializationFilter()
    {
        // nothing
    }

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
    @Override
    protected void doFilterPrepare(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        Context appContext = InfoGridWebApp.getSingleton().getApplicationContext();

        StringRepresentationContext stringRepContext
                = (StringRepresentationContext) request.getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );
        MeshBase mb
                = appContext.findContextObject( MeshBase.class );

        if( stringRepContext != null && mb != null ) {
            stringRepContext.put( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY, mb );
        }
    }

    /**
     * Initialize the Filter.
     *
     * @param filterConfig the Filter configuration object
     * @throws ServletException thrown if misconfigured
     */
    @Override
    public void init(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        super.init( filterConfig );

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
     * Initialize the context objects. This may be overridden by subclasses.
     *
     * @param rootContext the root Context
     * @throws Exception initialization may fail
     */
    protected void initializeContextObjects(
            Context rootContext )
        throws
            Exception
    {
        // nothing on this level
    }

    /**
     * Overridable method to determine the MeshBaseIdentifier of the main MeshBase.
     *
     * @param request the incoming request
     * @param meshBaseIdentifierFactory the MeshBaseIdentifierFactory to use
     * @return the determined MeshBaseIdentifier
     * @throws RuntimeException thrown if a parsing problem occurred
     */
    protected MeshBaseIdentifier determineMainMeshBaseIdentifier(
            SaneRequest               request,
            MeshBaseIdentifierFactory meshBaseIdentifierFactory )
    {
        MeshBaseIdentifier mbId;

        try {
            if( theDefaultMeshBaseIdentifier != null ) {
                mbId = meshBaseIdentifierFactory.fromExternalForm( theDefaultMeshBaseIdentifier );
            } else {
                SaneRequest originalRequest = request.getOriginalSaneRequest();
                mbId = meshBaseIdentifierFactory.fromExternalForm( originalRequest.getAbsoluteContextUriWithSlash());
            }
            return mbId;

        } catch( StringRepresentationParseException ex ) {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Identifier of the main MeshBase.
     */
    protected String theDefaultMeshBaseIdentifier;

    /**
     * Name of the Filter parameter in web.xml that contains the identifier of the main MeshBase.
     */
    public static final String DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER_NAME = "DefaultMeshBaseIdentifier";
}
