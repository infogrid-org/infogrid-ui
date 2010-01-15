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

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Instances supporting this interfaces know how to activate a ModelModule.
 */
public abstract class ModelModuleActivator
        implements
            ModuleActivator
{
    /**
     * Constructor.
     *
     * @param mod the ModelModule to activate
     */
    protected ModelModuleActivator(
            ModelModule mod )
    {
        theModule = mod;
    }

    /**
     * Obtain the Module that we can activate.
     *
     * @return the Module that we can activate
     */
    public ModelModule getModule()
    {
        return theModule;
    }

    /**
     * Activate this Module by instantiating the Model through a Java instantiator.
     *
     * @param dependentModules the set of dependent Modules
     * @param dependentContextObjects context objects obtained from activating the dependent Modules, in sequence
     * @param activationClassName name of the activation class
     * @param activationMethodName name of the activation method in the activation class
     * @param loader ClassLoader that knows where to find activationClass
     * @return a Module-specific return Object, or null
     * @throws ModuleActivationException thrown if the Module could not be activated
     */
    protected Object activateModelUsingJavaInstantiator(
            Module []   dependentModules,
            Object []   dependentContextObjects,
            String      activationClassName,
            String      activationMethodName,
            ClassLoader loader )
        throws
            ModuleActivationException
    {
        ModuleErrorHandler.informModuleActivateStart( theModule );

        try {
            // FIXME? I think this does not distinguish between ClassNotFoundExceptions that are triggered
            // by not finding the activationClass, and those triggered by those not finding a class while
            // running it.
            Class<?> activationClass = Class.forName( activationClassName, true, loader );

            Method activationMethod = activationClass.getMethod(
                    activationMethodName,
                    new Class[] {
                            Module[].class,
                            Object[].class,
                            Module.class } );

            ModuleErrorHandler.informModuleActivate( theModule, activationMethod );

            Object ret = activationMethod.invoke(
                    null,
                    new Object[] {
                            dependentModules,
                            dependentContextObjects,
                            theModule } ); // may throw activation exception

            ModuleErrorHandler.informModuleActivateSucceeded( theModule );

            return ret;

        } catch( InvocationTargetException ex ) {
            ModuleErrorHandler.informModuleActivateFailed( theModule, ex.getTargetException() );

            if( ex.getTargetException() instanceof ModuleActivationException ) {
                throw (ModuleActivationException) ex.getTargetException();
            }

            throw new ModuleActivationException( theModule.getModuleAdvertisement(), ex.getTargetException() );

        } catch( Throwable ex ) {
            ModuleErrorHandler.informModuleActivateFailed( theModule, ex );

            throw new ModuleActivationException( theModule.getModuleAdvertisement(), ex );
        }        
    }

    /**
     * Activate this Module by instantiating the Model by loading the Model XML.
     *
     * @param dependentModules the set of dependent Modules
     * @param dependentContextObjects the context objects obtained from activating the dependent Modules, in sequence
     * @param modelStream the InputStream from which to read the model
     * @param loader the ClassLoader to use
     * @return a Module-specific return Object, or null
     * @throws ModuleActivationException thrown if the Module could not be activated
     */
    protected Object activateModelUsingXmlModel(
            Module []   dependentModules,
            Object []   dependentContextObjects,
            InputStream modelStream,
            ClassLoader loader )
        throws
            ModuleActivationException
    {
        ModuleErrorHandler.informModuleActivateStart( theModule );

        // XML fallback
        try {
            Class<?> modelBaseSingleton = Class.forName( MODEL_BASE_SINGLETON_CLASS, true, loader );
            Method loadModelModuleMethod = modelBaseSingleton.getMethod(
                    MODEL_BASE_SINGLETON_METHOD,
                    new Class[] {
                            String.class,
                            InputStream.class,
                            ClassLoader.class
                    });
            
            Object ret = loadModelModuleMethod.invoke(
                    null,
                    new Object[] {
                            theModule.getModuleName(),
                            modelStream,
                            loader
                    });

            ModuleErrorHandler.informModuleActivateSucceeded( theModule );

            return ret;
            
        } catch( InvocationTargetException ex ) {
            ModuleErrorHandler.informModuleActivateFailed( theModule, ex.getTargetException() );

            if( ex.getTargetException() instanceof ModuleActivationException ) {
                throw (ModuleActivationException) ex.getTargetException();
            }

            throw new ModuleActivationException( theModule.getModuleAdvertisement(), ex.getTargetException() );

        } catch( Throwable ex ) {
            ModuleErrorHandler.informModuleActivateFailed( theModule, ex );

            throw new ModuleActivationException( theModule.getModuleAdvertisement(), ex );
        }
    }
 
    /**
     * Deactivate a Module.
     *
     * @param dependentModules the Modules that this Module depends on, if any
     * @throws ModuleActivationException throws if the Module could not be activated
     */
    public void deactivate(
            Module [] dependentModules )
        throws
            ModuleActivationException
    {
        ModuleErrorHandler.informModuleDeactivateStart( theModule );

        // FIXME: unload model?

        ModuleErrorHandler.informModuleDeactivateSucceeded( theModule );
    }

    /**
     * Obtain a ModuleActivator that is responsible for activating a dependent Module. This
     * method exists in order to be able to override it.
     *
     * @param dependentModule the dependent Module to activate
     * @return the ModuleActivator for the dependent Module
     * @throws ModuleActivationException thrown if the ModuleActivator could not be instantiated
     */
    public ModuleActivator dependentModuleActivator(
            Module dependentModule )
        throws
            ModuleActivationException
    {
        return dependentModule.getDefaultModuleActivator();
    }

    /**
     * The Module that we can activate.
     */
    protected ModelModule theModule;
    
    /**
     * Name of the ModelBaseSingleton class.
     */
    private static final String MODEL_BASE_SINGLETON_CLASS = "org.infogrid.modelbase.ModelBaseSingleton";
    
    /**
     * Name of the method on the ModelBaseSingleton class that loads the Model.
     */
    private static final String MODEL_BASE_SINGLETON_METHOD = "loadModel";
}
