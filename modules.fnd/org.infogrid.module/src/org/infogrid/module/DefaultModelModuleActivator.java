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

import java.io.*;

/**
 * The default implementation of ModelModuleActivator.
 */
public class DefaultModelModuleActivator
        extends
            ModelModuleActivator
{
    /**
     * Constructor.
     *
     * @param mod the Module to activate
     */
    public DefaultModelModuleActivator(
            ModelModule mod )
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
        Exception ex1 = null;
        Exception ex2 = null;

        try {
            ModuleErrorHandler.informModuleActivateStart( theModule );

            StringBuffer buf = new StringBuffer( 128 );
            buf.append( theModule.getModuleName() );
            buf.append( ".V" );
            String version = theModule.getModuleVersion();
            if( version != null ) {
                buf.append( version );
            }
            buf.append( ".SubjectAreaLoader" );
            String activationClassName  = buf.toString();
            String activationMethodName = "loadModelModule";

            Object ret = activateModelUsingJavaInstantiator(
                    dependentModules,
                    dependentContextObjects,
                    activationClassName,
                    activationMethodName,
                    theModule.getClassLoader() );

            ModuleErrorHandler.informModuleActivateSucceeded( theModule );
            return ret;
            
        } catch( Exception ex ) {
            ex1 = ex;
            ModuleErrorHandler.informModuleActivateFailed( theModule, ex1 );
        }
        
        try {
            ModuleErrorHandler.informModuleActivateStart( theModule );

            StringBuffer modelResource = new StringBuffer();
            modelResource.append( "infogrid-models/" );
            modelResource.append( theModule.getModuleName() );
            modelResource.append( ".V" );
            String version = theModule.getModuleVersion();
            if( version != null && version.length() > 0 ) {
                modelResource.append( version );
            }
            modelResource.append( ".xml" );

            InputStream theModelStream = theModule.getClassLoader().getResourceAsStream( modelResource.toString() );
            
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
}
