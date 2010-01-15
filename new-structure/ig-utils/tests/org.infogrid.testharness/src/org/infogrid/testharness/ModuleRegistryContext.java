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

package org.infogrid.testharness;

import org.infogrid.module.Module;
import org.infogrid.module.ModuleRegistry;

/**
 * This Module's initializer class.
 */
public abstract class ModuleRegistryContext
{
    /**
     * Constructor.
     */
    private ModuleRegistryContext()
    {
        // no op
    }

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
        theModuleRegistry = thisModule.getModuleRegistry();
        return null;
    }

    /**
     * Obtain the ModuleRegistry.
     *
     * @return the ModuleRegistry
     */
    public static ModuleRegistry getModuleRegistry()
    {
        return theModuleRegistry;
    }

    /**
     * The ModuleRegistry.
     */
    protected static ModuleRegistry theModuleRegistry;
}
