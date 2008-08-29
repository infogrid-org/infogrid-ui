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
 * A StandardModuleActivator whose parameters (given in the StandardModuleAdvertisement)
 * have been overridden.
 */
public class OverridingStandardModuleActivator
        extends
            StandardModuleActivator
{
    /**
     * Constructor.
     *
     * @param delegate the StandardModuleActivator to override
     * @param overridingActivationClassName name of the activation class to use instead
     * @param overridingActivationMethodName name of the activation method to use instead
     * @param overridingDeactivationMethodName name of the activation method in the activation class to use instead
     * @param loader ClassLoader that knows where to find overridingActivationClass
     */
    public OverridingStandardModuleActivator(
            StandardModuleActivator delegate,
            String                  overridingActivationClassName,
            String                  overridingActivationMethodName,
            String                  overridingDeactivationMethodName,
            ClassLoader             loader )
    {
        super( delegate.getModule() );

        theDelegate                         = delegate;
        theOverridingActivationClassName    = overridingActivationClassName;
        theOverridingActivationMethodName   = overridingActivationMethodName;
        theOverridingDeactivationMethodName = overridingDeactivationMethodName;
        theLoader                           = loader;
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
        return super.activate(
                dependentModules,
                dependentContextObjects,
                theOverridingActivationClassName,
                theOverridingActivationMethodName,
                theLoader );
    }

    /**
     * Deactivate a Module.
     *
     * @param dependentModules the Modules that this Module depends on, if any
     * @throws ModuleActivationException throws if the Module could not be deactivated
     */
    public void deactivate(
            Module [] dependentModules )
        throws
            ModuleActivationException
    {
        super.deactivate(
                dependentModules,
                theOverridingActivationClassName,
                theOverridingDeactivationMethodName,
                theLoader );
    }

    /**
     * The ModuleActivator that we delegate to.
     */
    protected ModuleActivator theDelegate;

    /**
     * The activation class name.
     */
    protected String theOverridingActivationClassName;
    
    /**
     * The activation method name.
     */
    protected String theOverridingActivationMethodName;
    
    /**
     * The deactivation method name.
     */
    protected String theOverridingDeactivationMethodName;

    /**
     * The ClassLoader to use.
     */
    protected ClassLoader theLoader;
}
