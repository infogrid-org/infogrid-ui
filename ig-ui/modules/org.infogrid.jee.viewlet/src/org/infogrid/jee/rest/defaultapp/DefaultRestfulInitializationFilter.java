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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.rest.defaultapp;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.infogrid.jee.defaultapp.DefaultInitializationFilter;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.templates.DefaultStructuredResponseTemplateFactory;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.model.primitives.text.ModelPrimitivesStringRepresentationDirectorySingleton;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationDirectorySingleton;

/**
 * Configures the default InfoGridWebApp with log4j logging, the template framework, and
 * a REST-ful JeeFormatter.
 */
public class DefaultRestfulInitializationFilter
        extends
            DefaultInitializationFilter
{
    /**
     * Public constructor.
     */
    public DefaultRestfulInitializationFilter()
    {
        // nothing right now
    }

    /**
     * Initialize the context objects. This may be overridden by subclasses.
     *
     * @param incomingRequest the incoming request
     * @param rootContext the root Context
     * @throws Exception initialization may fail
     */
    @Override
    protected void initializeContextObjects(
            SaneRequest incomingRequest,
            Context     rootContext )
        throws
            Exception
    {
        // do NOT invoke super.initializeContextObjects( incomingRequest, rootContext );
        // we use better subclasses ourselves
        
        ModelPrimitivesStringRepresentationDirectorySingleton.initialize();

        StringRepresentationDirectory srepdir = StringRepresentationDirectorySingleton.getSingleton();
        rootContext.addContextObject( srepdir );

        // Formatter
        RestfulJeeFormatter formatter = RestfulJeeFormatter.create( srepdir );
        rootContext.addContextObject( formatter );

        // StructuredResponseTemplateFactory
        StructuredResponseTemplateFactory tmplFactory = DefaultStructuredResponseTemplateFactory.create( getInfoGridWebApp() );
        rootContext.addContextObject( tmplFactory );
    }

    /**
     * Initialization method for this filter.
     *
     * @param filterConfig the filter configuration
     * @throws ServletException an exception occurred
     */
    @Override
    protected void internalInit(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        super.internalInit( filterConfig );

        theDefaultMeshBaseIdentifier = theFilterConfig.getInitParameter( DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER );
    }

    /**
     * The default MeshBaseIdentifier, in String form,
     */
    protected String theDefaultMeshBaseIdentifier;

    /**
     * Name of the String in the RequestContext that contains the identifier of the default
     * MeshBase.
     */
    public static final String DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER = "DefaultMeshBaseIdentifier";

}
