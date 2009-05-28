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

package org.infogrid.module;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * A normal Module containing regular code.
 */
public class StandardModule
        extends
            Module
{
    /**
     * Package-private constructor. This is only instantiated from within this package.
     *
     * @param adv the Module Advertisement describing this Module
     * @param registry the registry of Modules in which we try to find dependent Modules
     * @param moduleDirectory the Module's directory
     * @param moduleJars the JAR files belonging to this Module
     * @param parentClassLoader the class loader of our parent Module
     */
    StandardModule(
            StandardModuleAdvertisement adv,
            ModuleRegistry              registry,
            File                        moduleDirectory,
            File []                     moduleJars,
            ClassLoader                 parentClassLoader )
    {
        super( registry, moduleDirectory, moduleJars, parentClassLoader );
        
        theModuleAdvertisement = adv;
    }

    /**
      * Obtain the ModuleAdvertisement for this Module.
      *
      * @return the ModuleAdvertisement for this Module
      */
    public final StandardModuleAdvertisement getModuleAdvertisement()
    {
        return theModuleAdvertisement;
    }

    /**
     * Configure this Module. Do not call this directly, call configureRecursively() instead.
     *
     * @param parameters the set of parameters for this Module
     * @param whereParametersSpecifiedMap maps which Modules specified each parameter
     * @throws ModuleConfigurationException thrown if this Module could not be configured
     */
    protected void configure(
            Map<String,? extends Object> parameters,
            Map<String,Module>           whereParametersSpecifiedMap )
        throws
            ModuleConfigurationException
    {
        ModuleErrorHandler.informModuleConfigureStart( this );

        // look for a configure method. If none found, do nothing

        String configurationClassName  = null;
        String configurationMethodName = null;
        ClassLoader loader = null; // make compiler happy

        try {
            if( configurationClassName == null ) {
                configurationClassName = theModuleAdvertisement.getConfigurationClassName();
            }
            if( configurationMethodName == null ) {
                configurationMethodName = theModuleAdvertisement.getConfigurationMethodName();
            }
            
            if( configurationClassName != null ) {
                // invoke
                loader = getClassLoader();

                Class<?> configurationClass = Class.forName( configurationClassName, true, loader );

                Method configurationMethod = configurationClass.getMethod(
                        configurationMethodName,
                        new Class[] {
                            Map.class,
                            Map.class,
                            Module.class } );

                ModuleErrorHandler.informModuleConfigure( this, configurationMethod );

                configurationMethod.invoke(
                        null,
                        new Object[] {
                                parameters,
                                whereParametersSpecifiedMap,
                                this } ); // may throw exception
            }
            
            ModuleErrorHandler.informModuleConfigureSucceeded( this );

        } catch( InvocationTargetException ex ) {
            ModuleErrorHandler.informModuleConfigureFailed( this, ex.getTargetException() );

            // don't wrap if it is the right exception already
            if( ex.getTargetException() instanceof ModuleConfigurationException ) {
                throw (ModuleConfigurationException) ex.getTargetException();
            } else {
                throw new ModuleConfigurationException( theModuleAdvertisement, ex.getTargetException() );
            }

        } catch( Throwable ex ) {
            ModuleErrorHandler.informModuleConfigureFailed( this, ex );

            throw new ModuleConfigurationException( theModuleAdvertisement, ex );
        }

    }

    /**
     * Run this Module as a root Module.
     *
     * @param overriddenRunClassName optional name of the class to run instead of the default one specified in the ModuleAdvertisement
     * @param overriddenRunMethodName optional name of the method in the class to run instead of the default one specified in the ModuleAdvertisement
     * @param arguments arguments to run, similar to the arguments of a standard main(...) method
     * @throws ClassNotFoundException thrown if the specified run class cannot be found
     * @throws StandardModuleRunException thrown if the specified run method threw an Exception
     * @throws NoRunMethodException thrown if a suitable run method cannot be found
     * @throws InvocationTargetException thrown if the run method throws an Exception
     */
    public void run(
            String    overriddenRunClassName,
            String    overriddenRunMethodName,
            String [] arguments )
        throws
            ClassNotFoundException,
            StandardModuleRunException,
            NoRunMethodException,
            InvocationTargetException
    {
        ModuleErrorHandler.informModuleRunStart( this );

        // look for a run method. If none found, throw an exception

        String      runClassName  = overriddenRunClassName;
        String      runMethodName = overriddenRunMethodName;
        ClassLoader loader        = null;  // make compiler happy

        try {
            if( runClassName == null ) {
                runClassName = theModuleAdvertisement.getRunClassName();
            }
            if( runMethodName == null ) {
                runMethodName = theModuleAdvertisement.getRunMethodName();
            }

            if( runClassName == null ) {
                throw new NoRunMethodException( theModuleAdvertisement, runClassName, runMethodName, null );
            }
            if( runMethodName == null ) {
                throw new UnsupportedOperationException( "no run method given" );
            }

            // invoke
            loader = getClassLoader();

            Class<?> runClass = Class.forName( runClassName, true, loader );

            Method runMethod = runClass.getMethod(
                    runMethodName,
                    new Class[] {
                            String[].class } );

            ModuleErrorHandler.informModuleRun( this, runMethod );

            /* Object ret = */ runMethod.invoke(
                    null,
                    new Object[] {
                            arguments } );

           ModuleErrorHandler.informModuleRunSucceeded( this );
           
           return;

        } catch( MalformedURLException ex ) {
           ModuleErrorHandler.internalError( ex );
           ModuleErrorHandler.informModuleRunFailed( this, ex );
           return;
       } catch( InvocationTargetException ex ) {
           ModuleErrorHandler.informModuleRunFailed( this, ex );
           throw new StandardModuleRunException( theModuleAdvertisement, runClassName, runMethodName, ex );
       } catch( Throwable ex ) {
           ModuleErrorHandler.informModuleRunFailed( this, ex );
           throw new NoRunMethodException( theModuleAdvertisement, runClassName, runMethodName, ex );
       }
    }
    
    /**
     * Obtain a ModuleActivator for the Module that this ModuleAdvertisement corresponds to,
     * corresponding to the activation parameters given in the ModuleAdvertisement.
     *
     * @return the default StandardModuleActivator
     */
    public StandardModuleActivator getDefaultModuleActivator()
    {
        return new DefaultStandardModuleActivator( this );
    }

    /**
     * The ModuleAdvertisment for this Module.
     */
    protected StandardModuleAdvertisement theModuleAdvertisement;
}
