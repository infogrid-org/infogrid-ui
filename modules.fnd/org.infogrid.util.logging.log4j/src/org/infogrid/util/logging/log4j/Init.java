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

package org.infogrid.util.logging.log4j;

import java.io.BufferedInputStream;
import java.util.Map;
import java.util.Properties;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleConfigurationException;
import org.infogrid.util.logging.Log;
import org.infogrid.util.logging.LogFactory;

/**
 * Initializer class for the Module Framework.
 */
public abstract class Init
{
    /**
     * The Module Framework's BootLoader activates this Module by calling this method.
     *
     * @param dependentModules the Modules that this Module depends on, if any
     * @param dependentContextObjects the context objects of the Modules that this Module depends on, if any, in same sequence as dependentModules
     * @param thisModule reference to the Module that is currently being initialized and to which we belong
     * @return a context object that is Module-specific, or null if none
     * @throws Exception may an Exception indicating that the Module could not be activated
     */
    public static Object activate(
            Module [] dependentModules,
            Object [] dependentContextObjects,
            Module    thisModule )
        throws
            Exception
    {
        LogFactory factory = new Log4jLogFactory();
        Log.setLogFactory( factory );

        return factory;
    }

    /**
     * Configure this Module.
     *
     * @param parameters the parameters for initialization
     * @param whereParametersSpecifiedMap maps which Modules specified each parameter
     * @param thisModule the Module to be configured
     * @throws Exception may throw a range of Exceptions
     */
    public static void configure(
            Map<String,Object> parameters,
            Map<String,Module> whereParametersSpecifiedMap,
            Module             thisModule )
        throws
            Exception
    {
        if( parameters == null ) {
            return;
        }

        String configFile  = (String) parameters.get( CONFIG_PROPERTIES_FILE_PARAMETER_NAME );
        if( configFile == null ) {
            throw new ModuleConfigurationException(
                    thisModule.getModuleAdvertisement(),
                    "Missing configuration parameter " + CONFIG_PROPERTIES_FILE_PARAMETER_NAME );
        }

        Module whereParametersSpecified = whereParametersSpecifiedMap.get( CONFIG_PROPERTIES_FILE_PARAMETER_NAME );
        if( whereParametersSpecified == null ) {
            whereParametersSpecified = thisModule; // reasonable default
        }

        Properties logProperties = new Properties();
        try {
            logProperties.load( new BufferedInputStream(
                    whereParametersSpecified.getClassLoader().getResourceAsStream( configFile )));

        } catch( Throwable ex ) {
            throw new ModuleConfigurationException(
                    thisModule.getModuleAdvertisement(),
                    "Log4j configuration file " + configFile + " could not be loaded using ClassLoader " + whereParametersSpecified.getClassLoader() );
        }
        try {
            Log4jLog.configure( logProperties );
            // which logger is being used is defined in the module dependency declaration through parameters
        } catch( Throwable ex ) {
            // This can happen, for example, when a file could not be written
            throw new ModuleConfigurationException(
                    thisModule.getModuleAdvertisement(),
                    "Log4j configuration failed", ex );
        }
    }

    /**
     * Name of the parameter in the ModuleAdvertisement that holds the name of the Log4j
     * properties file. This must be given in a format so it can loaded with
     * <tt>ClassLoader.getResourceAsStream</tt>.
     */
    public static final String CONFIG_PROPERTIES_FILE_PARAMETER_NAME = "org.infogrid.util.logging.log4j.ConfigPropertiesFile";
}
