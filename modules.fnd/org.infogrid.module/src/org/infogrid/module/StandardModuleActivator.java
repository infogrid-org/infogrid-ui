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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Instances supporting this interfaces know how to activate a StandardModule.
 */
public abstract class StandardModuleActivator
        implements
            ModuleActivator
{
    /**
     * Constructor.
     *
     * @param mod the StandardModule to activate
     */
    protected StandardModuleActivator(
            StandardModule mod )
    {
        theModule = mod;
    }

    /**
     * Obtain the Module that we can activate.
     *
     * @return the Module that we can activate
     */
    public StandardModule getModule()
    {
        return theModule;
    }

    /**
     * The method is a default implementation useful for activating StandardModules, even
     * if parameters have been overridden.
     *
     * @param dependentModules the set of dependent Modules
     * @param dependentContextObjects the context objects obtained from activating the dependent Modules, in sequence
     * @param activationClassName name of the activating class
     * @param activationMethodName name of the activating method in the activating class
     * @param loader the ClassLoader that can find the activationClass
     * @return Module-specific return Object, or null
     * @throws ModuleActivationException thrown if the Module could not be activated
     */
    protected Object activate(
            Module []   dependentModules,
            Object []   dependentContextObjects,
            String      activationClassName,
            String      activationMethodName,
            ClassLoader loader )
        throws
            ModuleActivationException
    {
        ModuleErrorHandler.informModuleActivateStart( theModule );

        Class<?> activationClass  = null; // make compiler happy
        Method   activationMethod = null;
        try {
            // FIXME? I think this does not distinguish between ClassNotFoundExceptions that are triggered
            // by not finding the activationClass, and those triggered by those not finding a class while
            // running it.
            activationClass = Class.forName( activationClassName, true, loader );

            activationMethod = activationClass.getMethod(
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
     * The method is a default implementation useful for activating StandardModules, even
     * if parameters have been overridden.
     *
     * @param dependentModules the set of dependent Modules
     * @param deactivationClassName name of the deactivating class
     * @param deactivationMethodName name of the deactivating method in the deactivating class
     * @param loader the ClassLoader that can find the deactivationClass
     * @throws ModuleActivationException thrown if the Module could not be deactivated
     */
    protected void deactivate(
            Module []   dependentModules,
            String      deactivationClassName,
            String      deactivationMethodName,
            ClassLoader loader )
        throws
            ModuleActivationException
    {
        ModuleErrorHandler.informModuleDeactivateStart( theModule );

        try {
            Class<?> deactivationClass = Class.forName( deactivationClassName, true, loader );
        
            Method deactivationMethod = deactivationClass.getMethod(
                    deactivationMethodName,
                    new Class[] {
                            Module[].class,
                            Module.class
                    } );

            ModuleErrorHandler.informModuleDeactivate( theModule, deactivationMethod );

            deactivationMethod.invoke(
                    null,
                    new Object[] {
                            dependentModules,
                            theModule
                    } );

            ModuleErrorHandler.informModuleDeactivateSucceeded( theModule );
            
            return;

        } catch( InvocationTargetException ex ) {
            ModuleErrorHandler.informModuleDeactivateFailed( theModule, ex.getTargetException() );

            if( ex.getTargetException() instanceof ModuleActivationException ) {
                throw (ModuleActivationException) ex.getTargetException();
            }

            throw new ModuleActivationException( theModule.getModuleAdvertisement(), ex.getTargetException() );

        } catch( Throwable ex ) {
            ModuleErrorHandler.informModuleDeactivateFailed( theModule, ex );

            throw new ModuleActivationException( theModule.getModuleAdvertisement(), ex );
        }
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
    protected StandardModule theModule;
}
