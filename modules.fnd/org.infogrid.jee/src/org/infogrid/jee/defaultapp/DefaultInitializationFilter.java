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

package org.infogrid.jee.defaultapp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import javax.servlet.ServletException;
import org.infogrid.jee.JeeFormatter;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleException;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.ModuleRequirement;
import org.infogrid.module.SoftwareInstallation;
import org.infogrid.module.servlet.ServletBootLoader;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.SimpleStringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationDirectory;

/**
 * Configures the default InfoGridWebApp with log4j logging.
 */
public class DefaultInitializationFilter
        extends
            InitializationFilter
{
    /**
     * Public constructor.
     */
    public DefaultInitializationFilter()
    {
        // nothing right now
    }

    /**
     * Initialize the InfoGridWebApp if needed.
     *
     * @throws ServletException thrown if the InfoGridWebApp could not be initialized
     */
    @Override
    protected void initializeInfoGridWebApp()
        throws
            ServletException
    {
        InfoGridWebApp theApp = InfoGridWebApp.getSingleton();
        if( theApp == null ) {
            String className  = theFilterConfig.getInitParameter( INFOGRID_WEB_APP_CLASS_NAME_PARAMETER );
            String rootModule = theFilterConfig.getInitParameter( ROOT_MODULE_NAME_PARAMETER );

            if( ( className == null || className.length() == 0 ) && ( rootModule == null || rootModule.length() == 0 )) {
                throw new ServletException(
                        "Cannot initialize InfoGridWebApp: either parameter "
                        + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER
                        + " or parameter "
                        + ROOT_MODULE_NAME_PARAMETER
                        + " must be given in web.xml file" );
            }
            if( className != null && className.length() > 0 && rootModule != null && rootModule.length() > 0 ) {
                throw new ServletException(
                        "Cannot initialize InfoGridWebApp: only parameter "
                        + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER
                        + " or parameter "
                        + ROOT_MODULE_NAME_PARAMETER
                        + " must be given in web.xml file, not both" );
            }
            
            if( className != null && className.length() > 0 ) {
                theApp = initializeViaClassName( className );
                
            } else if( rootModule != null && rootModule.length() > 0 ) {
                theApp = initializeViaModuleName( rootModule );
                
            } else {
                throw new ServletException( "Don't know how we got there" );
            }
            try {
                InfoGridWebApp.setSingleton( theApp );

            } catch( IllegalStateException ex ) {
                // have one already, that's fine (a parallel thread was faster)
            }
            log = Log.getLogInstance( getClass() );
        }
    }

    /**
     * Initialize via the traditional subclass of InfoGridWebApp.
     * 
     * @param className the name of the class to be instantiated
     * @return the instantiated class
     * @throws ServletException thrown if the InfoGridWebApp could not be initialized
     */
    protected InfoGridWebApp initializeViaClassName(
            String className )
        throws
            ServletException
    {
        InfoGridWebApp theApp;
        
        try {                
            Class  appClass      = Class.forName( className );
            Method factoryMethod = appClass.getMethod( "create", String.class);

            theApp = (InfoGridWebApp) factoryMethod.invoke( null, theDefaultMeshBaseIdentifier );
            return theApp;

        } catch( ClassNotFoundException ex ) {
            throw new ServletException( "Cannot find class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex  );

        } catch( NoSuchMethodException ex ) {
            throw new ServletException( "Cannot find method \"create( String, String )\" in class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex );

        } catch( IllegalAccessException ex ) {
            throw new ServletException( "Cannot access method \"create( String, String )\" in class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex );

        } catch( InvocationTargetException ex ) {
            throw new ServletException( "Cannot execute method \"create( String, String )\" in class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex.getTargetException() );
        }
    }
    
    /**
     * Initialize via InfoGridWebApp and initializing a root module.
     * 
     * @param rootModule the name of the module to be activated
     * @return the instantiated class
     * @throws ServletException thrown if the InfoGridWebApp could not be initialized
     */
    protected InfoGridWebApp initializeViaModuleName(
            String rootModule )
        throws
            ServletException
    {
        Properties properties = new Properties();
        properties.put( "rootmodule", rootModule );

        Module theThisModule;
        if( SoftwareInstallation.getSoftwareInstallation() == null ) {
            theThisModule = ServletBootLoader.initialize( properties );
        } else {
            try {
                ModuleRequirement req      = ModuleRequirement.create1( rootModule );
                ModuleRegistry    registry = ServletBootLoader.getModuleRegistry();

                theThisModule = registry.resolve( registry.determineSingleResolutionCandidate( req )); // we know it is there

            } catch( ModuleException ex ) {
                throw new ServletException( "Initialization of Module " + rootModule + " failed", ex );
            }
        }

        ResourceHelper.initializeLogging();

        log = Log.getLogInstance( InfoGridWebApp.class );

        // Context
        SimpleContext rootContext = SimpleContext.createRoot( rootModule + " root context" );
        rootContext.addContextObject( theThisModule.getModuleRegistry() );
        
        initializeContextObjects( rootContext );
        
        // app
        DefaultInfoGridWebApp ret = new DefaultInfoGridWebApp( rootContext );

        return ret;        
    }

    /**
     * Initialize the context objects. This may be overridden by subclasses.
     *
     * @param rootContext the root Context
     */
    protected void initializeContextObjects(
            Context rootContext )
    {
        // Formatter
        JeeFormatter formatter = JeeFormatter.create();
        rootContext.addContextObject( formatter );

        StringRepresentationDirectory srepdir = SimpleStringRepresentationDirectory.create();
        rootContext.addContextObject( srepdir );
    }

    /**
     * Name of the Filter parameter that contains the name of root module to activate.
     */
    public static final String ROOT_MODULE_NAME_PARAMETER = "ROOTMODULE";
}
