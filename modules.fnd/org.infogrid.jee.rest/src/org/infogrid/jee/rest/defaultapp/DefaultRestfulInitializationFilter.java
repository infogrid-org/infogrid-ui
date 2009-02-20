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

package org.infogrid.jee.rest.defaultapp;

import org.infogrid.jee.defaultapp.DefaultInitializationFilter;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.templates.DefaultStructuredResponseTemplateFactory;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.model.primitives.text.ModelPrimitivesStringRepresentationDirectorySingleton;
import org.infogrid.util.context.Context;
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
     * @param rootContext the root Context
     * @throws Exception initialization may fail
     */
    @Override
    protected void initializeContextObjects(
            Context rootContext )
        throws
            Exception
    {
        // do NOT invoke super.initializeContextObjects( rootContext );
        // we use better subclasses ourselves
        
        ModelPrimitivesStringRepresentationDirectorySingleton.initialize();

        StringRepresentationDirectory srepdir = StringRepresentationDirectorySingleton.getSingleton();
        rootContext.addContextObject( srepdir );

        // Formatter
        RestfulJeeFormatter formatter = RestfulJeeFormatter.create( srepdir );
        rootContext.addContextObject( formatter );

        // StructuredResponseTemplateFactory
        StructuredResponseTemplateFactory tmplFactory = DefaultStructuredResponseTemplateFactory.create();
        rootContext.addContextObject( tmplFactory );
    }
}
