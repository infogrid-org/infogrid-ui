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

package org.infogrid.module.servlet;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleAdvertisement;
import org.infogrid.module.ModuleErrorHandler;
import org.infogrid.module.OverridingStandardModuleActivator;
import org.infogrid.module.StandardModule;
import org.infogrid.module.StandardModuleActivator;

/**
 * The Module Framework's BootLoader for use in a Servlet environment.
 * This is an abstract class as it only provides static methods.
 */
public abstract class ServletBootLoader
{
    /**
     * Initialize. Return the activated root Module.
     *
     * @param propStream InputStream containing the configuration properties
     * @return the activated root Module
     */
    public static Module initialize(
            InputStream propStream )
    {
        try {
            theInstallation = ServletSoftwareInstallation.createFromProperties( propStream );
        } catch( Throwable ex ) {
            fatal( ex );
        }
        return initialize( theInstallation, null );
    }
    
    /**
     * Initialize. Return the activated root Module.
     *
     * @param props the configuration properties
     * @return the activated root Module
     */
    public static Module initialize(
            Properties props )
    {
        try {
            theInstallation = ServletSoftwareInstallation.createFromProperties( props );
        } catch( Throwable ex ) {
            fatal( ex );
        }
        return initialize( theInstallation, null );
    }

    /**
     * Initialize. Return the activated root Module.
     *
     * @param theInstallation the ServletSoftwareInstallation object to use
     * @param rootConfigParameters the configuration parameters
     * @return the activated root Module
     */
    public static Module initialize(
            ServletSoftwareInstallation theInstallation,
            Map<String,Object>          rootConfigParameters )
    {
        if( theInstallation == null ) {
            fatal( "Cannot determine SoftwareInstallation" );
        }

        // create ModuleRegistry
        try {
            theModuleRegistry = ServletModuleRegistry.create( theInstallation );
        } catch( Throwable ex ) {
            fatal( ex );
        }
        if( theModuleRegistry == null ) {
            fatal( "Could not create Module Registry" );
        }
        if( theInstallation.isShowModuleRegistry() ) {
            ModuleErrorHandler.informModuleRegistry( theModuleRegistry );
            return null; // we are done
        }

        // find and resolve the main module
        ModuleAdvertisement rootModuleAdv = null; // make compiler happy
        try {
            rootModuleAdv = theModuleRegistry.determineSingleResolutionCandidate( theInstallation.getRootModuleRequirement() );
        } catch( Throwable ex ) {
            fatal( ex );
        }

        StandardModule theRootModule = null;
        try {
            theRootModule = (StandardModule) theModuleRegistry.resolve( rootModuleAdv, true );
        } catch( Throwable ex ) {
            fatal( ex );
        }

        StandardModuleActivator act = theRootModule.getDefaultModuleActivator();
        if(    theInstallation.getOverriddenActivationClassName() != null
            || theInstallation.getOverriddenActivationMethodName() != null )
        {
            try {
                act = new OverridingStandardModuleActivator(
                        act,
                        theInstallation.getOverriddenActivationClassName(),
                        theInstallation.getOverriddenActivationMethodName(),
                        theInstallation.getOverriddenDeactivationMethodName(),
                        theRootModule.getClassLoader() );
            } catch( Throwable ex ) {
                fatal( ex );
            }
        }

        try {
            Object ret = theRootModule.activateRecursively( act );
                    // may throw an exception
                    // ret is only there for debugging

            theRootModule.configureRecursively( rootConfigParameters ); // FIXME

            // we don't run anything in servlet mode

        } catch( Throwable ex ) {
            fatal( ex );
        }
        return theRootModule;
    }

    /**
     * Obtain the SoftwareInstallation. If this returns an object, the ServletBootLoader
     * has been initialized.
     *
     * @return the ServletSoftwareInstallation, if any
     */
    public static ServletSoftwareInstallation getSoftwareInstallation()
    {
        return theInstallation;
    }

    /**
     * Obtain the ModuleRegistry. If this returns an object, the ServletBootLoader
     * has been initialized.
     *
     * @return the ModuleRegistry, if any
     */
    public static ServletModuleRegistry getModuleRegistry()
    {
        return theModuleRegistry;
    }

    /**
     * This is called if all hope is lost and we need to exit.
     *
     * @param ex exception, if any, that caused the problem
     * @throws RuntimeException the fatal problem
     */
    protected static void fatal(
           Throwable ex )
        throws
            RuntimeException
    {
        ModuleErrorHandler.fatal( null, ex, ServletSoftwareInstallation.getSoftwareInstallation() );
        
        if( ex instanceof RuntimeException ) {
            throw (RuntimeException) ex;
        } else {
            throw new RuntimeException( ex );
        }
    }

    /**
     * This is called if all hope is lost and we need to exit.
     *
     * @param msg error message
     */
    protected static void fatal(
            String msg )
    {
        ModuleErrorHandler.fatal( msg, null, ServletSoftwareInstallation.getSoftwareInstallation() );

        throw new RuntimeException( msg );
    }

    /**
     * This is called if all hope is lost and we need to exit.
     *
     * @param msg error message
     * @param ex exception, if any, that caused the problem
     * @throws RuntimeException throws a RuntimeException
     */
    protected static void fatal(
           String    msg,
           Throwable ex )
       throws
           RuntimeException
    {
        ModuleErrorHandler.fatal( msg, ex, ServletSoftwareInstallation.getSoftwareInstallation() );

        throw new RuntimeException( msg );
    }
    
    /**
     * The SoftwareInstallation.
     */
    protected static ServletSoftwareInstallation theInstallation = null;

    /**
     * The ModuleRegistry.
     */
    protected static ServletModuleRegistry theModuleRegistry;
    
    /**
     * Name of the Servlet parameter that contains the name of the ServletBootLoader properties.
     */
    public static final String BOOTLOADER_PROPERTIES_FILE_NAME = "org.infogrid.module.servlet.ServletBootLoader.PROPERTIES";
}
