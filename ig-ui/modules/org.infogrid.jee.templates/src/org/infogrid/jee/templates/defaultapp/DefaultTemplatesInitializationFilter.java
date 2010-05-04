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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.templates.defaultapp;

import org.infogrid.jee.JeeFormatter;
import org.infogrid.jee.defaultapp.DefaultInitializationFilter;
import org.infogrid.jee.templates.DefaultStructuredResponseTemplateFactory;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationDirectorySingleton;

/**
 * Configures the default InfoGridWebApp with the template framework.
 */
public class DefaultTemplatesInitializationFilter
        extends
            DefaultInitializationFilter
{
    /**
     * Public constructor.
     */
    public DefaultTemplatesInitializationFilter()
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
        super.initializeContextObjects( incomingRequest, rootContext );

        StringRepresentationDirectory srepdir = rootContext.findContextObject( StringRepresentationDirectory.class );
        if( srepdir == null ) {
            srepdir = StringRepresentationDirectorySingleton.getSingleton();
            rootContext.addContextObject( srepdir );
        }

        // Formatter
        JeeFormatter formatter = rootContext.findContextObject( JeeFormatter.class );
        if( formatter == null ) {
            formatter = JeeFormatter.create( srepdir );
            rootContext.addContextObject( formatter );
        }

        // StructuredResponseTemplateFactory
        StructuredResponseTemplateFactory tmplFactory = rootContext.findContextObject( StructuredResponseTemplateFactory.class );
        if( tmplFactory == null ) {
            tmplFactory = DefaultStructuredResponseTemplateFactory.create();
            rootContext.addContextObject( tmplFactory );
        }
    }
}
