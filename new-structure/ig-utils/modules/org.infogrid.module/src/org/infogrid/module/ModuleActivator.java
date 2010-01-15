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

package org.infogrid.module;

/**
 * Instances supporting this interfaces know how to activate a Module.
 */
public interface ModuleActivator
{
    /**
     * Obtain the Module that we can activate.
     *
     * @return the Module that we can activate
     */
    public Module getModule();

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
            ModuleActivationException;

    /**
     * Deactivate a Module.
     *
     * @param dependentModules the Modules that this Module depends on, if any
     * @throws ModuleActivationException throws if the Module could not be activated
     */
    public void deactivate(
            Module [] dependentModules )
        throws
            ModuleActivationException;

    /**
     * Obtain a ModuleActivator that is responsible for activating a dependent Module.
     *
     * @param dependentModule the dependent Module to activate
     * @return the ModuleActivator for the dependent Module
     * @throws ModuleActivationException thrown if the ModuleActivator could not be instantiated
     */
    public ModuleActivator dependentModuleActivator(
            Module dependentModule )
        throws
            ModuleActivationException;
}
