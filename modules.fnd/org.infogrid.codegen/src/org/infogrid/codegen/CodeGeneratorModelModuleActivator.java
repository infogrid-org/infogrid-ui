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

package org.infogrid.codegen;

import org.infogrid.module.ModelModule;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleActivationException;
import org.infogrid.module.ModuleActivator;
import org.infogrid.module.OverridingModelModuleActivator;
import org.infogrid.module.StandardModule;
import org.infogrid.util.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A ModelModuleActivator specifically for the purposes of the code generator. It cannot, for example,
 * attempt to load generated class files because they aren't there yet.
 */
public class CodeGeneratorModelModuleActivator
        extends
            OverridingModelModuleActivator
{
    private static final Log log = Log.getLogInstance( CodeGeneratorModelModuleActivator.class );

    /**
     * Constructor.
     *
     * @param mod the ModelModule that we activate
     * @param name the Subject Area name
     * @param version the Subject Area version
     * @param moduleDirectories the Module directories
     * @throws IOException thrown if an I/O problem occurred
     */
    public CodeGeneratorModelModuleActivator(
            ModelModule mod,
            String      name,
            String      version,
            List<File>  moduleDirectories )
        throws
            IOException
    {
        super( mod.getDefaultModuleActivator(), null, null, createStreamFor( name, version, moduleDirectories ), mod.getClassLoader() );
        
        theModuleDirectories = moduleDirectories;
    }

    /**
     * Create a stream from the XML Model file based on the ModelModule's name, version, and the moduleDirectory.
     *
     * @param name the name of the ModelModule
     * @param version the version of the ModelModule
     * @param moduleDirectories the list of ModuleDirectories
     * @return the created InputStream
     * @throws IOException thrown if an I/O problem occurred
     */
    static InputStream createStreamFor(
            String     name,
            String     version,
            List<File> moduleDirectories )
        throws
            IOException
    {
        for( File current : moduleDirectories ) {
            File thisModuleDir = new File( current, name );
            File modelFile     = new File( thisModuleDir, "infogrid-models/model.xml" );

            if( modelFile.exists() && modelFile.canRead() && !modelFile.isDirectory() ) {
                InputStream stream = new FileInputStream( modelFile );
                return stream;
            }
        }
        throw new IOException( "Model file not found with name " + name );
    }


    /**
     * Obtain a ModuleActivator that is responsible for activating a dependent Module.
     *
     * @param dependentModule the dependent Module to activate
     * @return the ModuleActivator for the dependent Module
     * @throws ModuleActivationException thrown if the dependent Module could not be activated
     */
    @Override
    public ModuleActivator dependentModuleActivator(
            Module dependentModule )
        throws
            ModuleActivationException
    {
        try {
            ModuleActivator ret;
            if( dependentModule instanceof StandardModule ) {
                ret = new CodeGeneratorStandardModuleActivator(
                        (StandardModule) dependentModule,
                        theModuleDirectories );
            } else {
                ret = new CodeGeneratorModelModuleActivator(
                        (ModelModule) dependentModule,
                        dependentModule.getModuleName(),
                        dependentModule.getModuleVersion(),
                        theModuleDirectories );
            }
            return ret;

        } catch( IOException ex ) {
            throw new ModuleActivationException( dependentModule.getModuleAdvertisement(), ex );
        }
    }

    /**
     * The SubjectArea name.
     */
    protected String theName;
    
    /**
     * The SubjectArea version.
     */
    protected String theVersion;
    
    /**
     * The Module directories.
     */
    protected List<File> theModuleDirectories;
}
