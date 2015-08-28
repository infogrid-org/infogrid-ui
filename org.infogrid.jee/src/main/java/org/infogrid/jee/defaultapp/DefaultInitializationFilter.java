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

package org.infogrid.jee.defaultapp;

import java.io.IOException;
import java.text.ParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.diet4j.core.Module;
import org.diet4j.core.ModuleException;
import org.diet4j.core.ModuleMeta;
import org.diet4j.core.ModuleRegistry;
import org.diet4j.core.ModuleRequirement;
import org.diet4j.inclasspath.InClasspathModuleRegistry;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.servlet.AbstractInfoGridServlet;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.util.QuitManager;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Configures the default InfoGridWebApp with log4j logging.
 */
public class DefaultInitializationFilter
        extends
            InitializationFilter
{
    private static Log log; // initialized later

    /**
     * Public constructor.
     */
    public DefaultInitializationFilter()
    {
        // nothing right now
    }

    /**
     * Find the InfoGridWebApp object.
     *
     * @return the InfoGridWebApp object
     */
    protected InfoGridWebApp getInfoGridWebApp()
    {
        InfoGridWebApp ret = (InfoGridWebApp) theFilterConfig.getServletContext().getAttribute( AbstractInfoGridServlet.INFOGRID_WEB_APP_NAME );
        return ret;
    }

    /**
     * Initialize the InfoGridWebApp if needed.
     *
     * @param incomingRequest the incoming request
     * @throws ServletException thrown if the InfoGridWebApp could not be initialized
     */
    @Override
    protected void initializeInfoGridWebApp(
            HttpServletRequest incomingRequest )
        throws
            ServletException
    {
        InfoGridWebApp theApp = getInfoGridWebApp();
        if( theApp == null ) {
            // try again, this time synchronized
            synchronized( DefaultInitializationFilter.class ) {
                theApp = getInfoGridWebApp();
                if( theApp == null ) {
                    Module thisModule = initializeModuleFramework();

                    SaneRequest lidRequest = SaneServletRequest.create( incomingRequest );

                    theApp = createInfoGridWebApp( lidRequest );

                    if( thisModule != null ) {
                        theApp.getApplicationContext().addContextObject( thisModule.getModuleRegistry() );
                    }
                    
                    try {
                        theFilterConfig.getServletContext().setAttribute( AbstractInfoGridServlet.INFOGRID_WEB_APP_NAME, theApp );

                    } catch( IllegalStateException ex ) {
                        // have one already, that's fine (a parallel thread was faster)
                    }
                }
            }
        }
    }

    /**
     * Initialize the module framework, if needed.
     *
     * @return the top Module, or null
     */
    protected Module initializeModuleFramework()
    {
        String rootModule = theFilterConfig.getInitParameter( ROOT_MODULE_NAME_PARAMETER );
        Module ret        = null;
        if( rootModule != null ) {
            ModuleRegistry registry;
            ModuleMeta     meta;
            try {
                registry = InClasspathModuleRegistry.instantiateOrGet( getClass().getClassLoader() );
                meta     = registry.determineSingleResolutionCandidate( ModuleRequirement.parse( rootModule ));

                ret = registry.resolve( meta );
                ret.activateRecursively();

            } catch( ParseException ex ) {
                getLog().error( ex );
            } catch( IOException ex ) {
                getLog().error( ex );
            } catch( ModuleException ex ) {
                getLog().error( ex );
            }
        }
        return ret;
    }

    /**
     * Create the InfoGridWebApp instance.
     *
     * @param lidRequest the incoming request
     * @return the created InfoGridWebApp
     * @throws ServletException thrown if the InfoGridWebApp could not be initialized
     */
    protected InfoGridWebApp createInfoGridWebApp(
            SaneRequest lidRequest )
        throws
            ServletException
    {
        ResourceHelper.initializeLogging();

        log = Log.getLogInstance( getClass() );

        // Context
        SimpleContext rootContext = SimpleContext.createRoot( theFilterConfig.getServletContext().getServletContextName() + " root context" );
        rootContext.addContextObject( QuitManager.create() );

        // app
        DefaultInfoGridWebApp ret = new DefaultInfoGridWebApp( lidRequest, rootContext );
        rootContext.addContextObject( ret );

        return ret;        
    }

    /**
     * Initialize and get the log.
     *
     * @return the log
     */
    private static Log getLog()
    {
        if( log == null ) {
            log = Log.getLogInstance( InitializationFilter.class ); // our own, private logger
        }
        return log;
    }

    /**
     * Name of the Filter parameter that contains the name of root module to activate.
     */
    public static final String ROOT_MODULE_NAME_PARAMETER = "ROOTMODULE";
}
