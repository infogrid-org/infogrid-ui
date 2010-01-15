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

package org.infogrid.module;

import java.net.MalformedURLException;

/**
 * This is the default implementation of StandardModuleActivator. It is used
 * unless non-standard parameters are specified in the StandardModuleAdvertisement,
 * or it is invoked differently programmatically.
 */
public class DefaultStandardModuleActivator
        extends
            StandardModuleActivator
{
    /**
     * Constructor.
     *
     * @param mod the Module to activate
     */
    public DefaultStandardModuleActivator(
            StandardModule mod )
    {
        super( mod );
    }

    /**
     * Activate a Module.
     *
     * @param dependentModules the Modules that this Module depends on, if any
     * @param dependentContextObjects the context objects of the Modules that this Module depends on, if any, in same sequence as dependentModules
     * @return a context object that is Module-specific, or null if none
     * @throws ModuleActivationException throws if the Module could not be activated
     */
    public Object activate(
            Module [] dependentModules,
            Object [] dependentContextObjects )
        throws
            ModuleActivationException
    {
        String activationClassName  = theModule.getModuleAdvertisement().getActivationClassName();
        String activationMethodName = theModule.getModuleAdvertisement().getActivationMethodName();

        if( activationClassName == null || activationClassName.length() == 0 ) {
            return null;
        }
        if( activationMethodName == null || activationMethodName.length() == 0 ) {
            return null;
        }
        try {
            return super.activate(
                    dependentModules,
                    dependentContextObjects,
                    activationClassName,
                    activationMethodName,
                    theModule.getClassLoader() );
        } catch( MalformedURLException ex ) {
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
        String activationClassName    = theModule.getModuleAdvertisement().getActivationClassName();
        String deactivationMethodName = theModule.getModuleAdvertisement().getDeactivationMethodName();

        if( activationClassName == null || activationClassName.length() == 0 ) {
            return;
        }
        if( deactivationMethodName == null || deactivationMethodName.length() == 0 ) {
            return;
        }
        try {
            super.deactivate(
                    dependentModules,
                    activationClassName,
                    deactivationMethodName,
                    theModule.getClassLoader() );
        } catch( MalformedURLException ex ) {
            throw new ModuleActivationException( theModule.getModuleAdvertisement(), ex );
        }
    }

    /**
     * Obtain a ModuleActivator that is responsible for activating a dependent Module. This
     * method exists in order to be able to override it.
     *
     * @param dependentModule the dependent Module to activate
     * @return the ModuleActivator for the dependent Module
     */
    @Override
    public ModuleActivator dependentModuleActivator(
            Module dependentModule )
        throws
            ModuleActivationException
    {
        return dependentModule.getDefaultModuleActivator();
    }
}
