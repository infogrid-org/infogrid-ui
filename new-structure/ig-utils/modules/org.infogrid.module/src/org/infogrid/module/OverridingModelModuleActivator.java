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

import java.io.InputStream;

/**
 * A ModelModuleActivator that overrides one or more features of another ModelModuleActivator.
 */
public class OverridingModelModuleActivator
        extends
            ModelModuleActivator
{
    /**
     * Constructor.
     *
     * @param delegate the ModelModuleActivator to override
     * @param overridingActivationClassName name of the activation class to use instead
     * @param overridingActivationMethodName name of the activation method in the activation class to use instead
     * @param modelStream the InputStream from which to read the model
     * @param loader ClassLoader that knows where to find overridingActivationClass
     */
    public OverridingModelModuleActivator(
            ModelModuleActivator delegate,
            String               overridingActivationClassName,
            String               overridingActivationMethodName,
            InputStream          modelStream,
            ClassLoader          loader )
    {
        super( delegate.getModule() );
        
        theOverridingActivationClassName  = overridingActivationClassName;
        theOverridingActivationMethodName = overridingActivationMethodName;
        theModelStream                    = modelStream;
        theLoader                         = loader;
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
        Exception ex1 = null;
        Exception ex2 = null;

        if(    theOverridingActivationClassName  != null && theOverridingActivationClassName.length() > 0
            && theOverridingActivationMethodName != null && theOverridingActivationMethodName.length() > 0 )
        {
            ModuleErrorHandler.informModuleActivateStart( theModule );
            try {
                Object ret = activateModelUsingJavaInstantiator(
                        dependentModules,
                        dependentContextObjects,
                        theOverridingActivationClassName,
                        theOverridingActivationMethodName,
                        theModule.getClassLoader() );

                ModuleErrorHandler.informModuleActivateSucceeded( theModule );
                return ret;

            } catch( Exception ex ) {
                ex1 = ex;
                ModuleErrorHandler.informModuleActivateFailed( theModule, ex1 );
            }
        }
        
        try {
            ModuleErrorHandler.informModuleActivateStart( theModule );

            Object ret = activateModelUsingXmlModel(
                    dependentModules,
                    dependentContextObjects,
                    theModelStream,
                    theModule.getClassLoader() );

            ModuleErrorHandler.informModuleActivateSucceeded( theModule );
            return ret;

        } catch( Exception ex ) {
            ex2 = ex;
            ModuleErrorHandler.informModuleActivateFailed( theModule, ex2 );
        }
        
        if( ex2 instanceof ModuleActivationException ) {
            throw ((ModuleActivationException)ex2);
        } else {
            throw new ModuleActivationException( theModule.getModuleAdvertisement(), ex2 );
        }
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
     * The InputStream from where to read the Model XML.
     */
    protected InputStream theModelStream;

    /**
     * The ClassLoader to use.
     */
    protected ClassLoader theLoader;
}
