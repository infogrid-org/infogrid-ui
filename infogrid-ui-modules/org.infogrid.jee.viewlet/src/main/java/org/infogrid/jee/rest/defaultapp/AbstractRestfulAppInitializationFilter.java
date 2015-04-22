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

package org.infogrid.jee.rest.defaultapp;

import java.text.ParseException;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.infogrid.jee.templates.DefaultStructuredResponseTemplateFactory;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.jee.templates.defaultapp.AbstractAppInitializationFilter;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.model.primitives.text.ModelPrimitivesStringRepresentationDirectorySingleton;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationDirectorySingleton;

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
     * Initialize the Filter.
     *
     * @param filterConfig the Filter configuration object
     * @throws ServletException thrown if misconfigured
     */
    @Override
    public void internalInit(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        theDefaultMeshBaseIdentifier = filterConfig.getInitParameter( DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER_NAME );
    }

    /**
     * Initialize the initial content of the MeshBase.
     *
     * @param incomingRequest the incoming request
     * @param mb the MeshBase to initialize
     */
    protected void populateMeshBase(
            SaneRequest incomingRequest,
            MeshBase    mb )
    {
        // nothing on this level
    }

    /**
     * Initialize the context objects. This may be overridden by subclasses.
     *
     * @param incomingRequest the incoming request
     * @param rootContext the root Context
     * @throws Exception initialization may fail
     */
    protected void initializeContextObjects(
            SaneRequest incomingRequest,
            Context     rootContext )
        throws
            Exception
    {
        ModelPrimitivesStringRepresentationDirectorySingleton.initialize();

        StringRepresentationDirectory srepdir = StringRepresentationDirectorySingleton.getSingleton();
        rootContext.addContextObject( srepdir );

        // StructuredResponseTemplateFactory
        StructuredResponseTemplateFactory tmplFactory = DefaultStructuredResponseTemplateFactory.create( getInfoGridWebApp() );
        rootContext.addContextObject( tmplFactory );
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

        } catch( ParseException ex ) {
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
