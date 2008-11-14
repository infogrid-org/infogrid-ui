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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.infogrid.codegen.impl.ImplementationGenerator;
import org.infogrid.codegen.intfc.InterfaceGenerator;
import org.infogrid.codegen.modelloader.ModelLoaderGenerator;
import org.infogrid.model.primitives.SubjectArea;
import org.infogrid.model.primitives.text.SimpleModelPrimitivesStringRepresentation;
import org.infogrid.module.ModelModule;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleActivator;
import org.infogrid.module.ModuleAdvertisement;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.ModuleRequirement;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.logging.log4j.Log4jLog;
import org.infogrid.util.text.StringRepresentation;

/**
 * The InfoGrid code generator.
 */
public class CodeGenerator
{
    private static final Log log = Log.getLogInstance( CodeGenerator.class );

    /**
     * Main program for the code generator.
     *
     * @param args command-line arguments
     * @throws Exception things may go wrong, and there is no error handling on the top
     */
    public static void main(
            String [] args )
        throws
            Exception
    {
        ArrayList<String> subjectAreas = new ArrayList<String>();
        File              outputDir    = null;
        boolean           isOutput     = false;

        for( int i=0 ; i<args.length ; ++i ) {
            if( "-o".equals( args[i] )) {
                isOutput = true;
            } else if( isOutput ) {
                if( outputDir != null ) {
                    usageAndQuit();
                } else {
                    outputDir = new File( args[i] );
                    if( !outputDir.exists() || !outputDir.canRead() ) {
                        outputDir.mkdirs();
                    }
                    if( !outputDir.exists() || !outputDir.canRead() ) {
                        usageAndQuit();
                    }
                }
            } else {
                subjectAreas.add( args[i] );
            }
        }
        if( outputDir == null ) {
            usageAndQuit();
        }

        String nameOfLog4jConfigFile = "org/infogrid/codegen/Log.properties";
        try {
            Properties logProperties = new Properties();
            logProperties.load( new BufferedInputStream(
                    CodeGenerator.class.getClassLoader().getResourceAsStream( nameOfLog4jConfigFile )));

            Log4jLog.configure( logProperties );
            // which logger is being used is defined in the module dependency declaration through parameters

        } catch( Throwable ex ) {
            System.err.println( "Unexpected Exception attempting to load " + nameOfLog4jConfigFile );
            ex.printStackTrace( System.err );
        }

        ResourceHelper.initializeLogging();

        final String intfcName = "org.infogrid.modelbase.ModelBase";
        ModuleAdvertisement [] advs = theModuleRegistry.findAdvertisementsForInterface( intfcName, 1 );
        if( advs == null || advs.length == 0 ) {
            log.error( "Cannot find a Module that supports interface " + intfcName );
            System.exit( 0 );
        }
        Module modelBaseModule = theModuleRegistry.resolve( advs[0], true );

        Object base = modelBaseModule.activateRecursively(); // return value is ignored but may be helpful in debugging

        SimpleModelPrimitivesStringRepresentation commentsRepresentation = SimpleModelPrimitivesStringRepresentation.create( "Javadoc" );
        
        CodeGenerator generator         = new CodeGenerator( outputDir, commentsRepresentation );
        List<File>    moduleDirectories = theModuleRegistry.getSoftwareInstallation().getInstallModuleDirectories();

        Iterator<String> iter = subjectAreas.iterator();
        while( iter.hasNext() ) {
            String saName    = iter.next();
            String saVersion = "1_0";

            try {
                ModuleRequirement   saRequirement = ModuleRequirement.create1( saName, saVersion );
                ModuleAdvertisement saCandidate  = theModuleRegistry.determineSingleResolutionCandidate( saRequirement );

                ModelModule saModule = (ModelModule) theModuleRegistry.resolve( saCandidate, true );

                ModuleActivator activator = new CodeGeneratorModelModuleActivator(
                        saModule,
                        saName,
                        saVersion,
                        moduleDirectories );

                SubjectArea [] sas = (SubjectArea []) saModule.activateRecursively( activator );

                if( sas == null || sas.length == 0 ) {
                    log.error( "Could not obtain SubjectArea '" + saName + "', version '" + saVersion + "'" );
                    System.exit( 0 );
                }
                generator.generateForAll( sas );

            } catch( Throwable ex ) {
                System.err.println( "Unexpected Exception attempting to access SubjectArea " + saName );
                ex.printStackTrace( System.err );
            }
        }
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
     * Print usage information and quit.
     */
    private static void usageAndQuit()
    {
        System.err.println( "Usage:" );
        System.err.println( "    java " + CodeGenerator.class.getName() + " <subjectArea> ... -o <outputDir>" );
        System.exit( 0 );
    }

    /**
     * Constructor.
     *
     * @param outputDir the output directory
     * @param commentsRepresentation the StringRepresentation for generated comments
     */
    public CodeGenerator(
            File                 outputDir,
            StringRepresentation commentsRepresentation )
    {
        theOutputDirectory        = outputDir;
        theCommentsRepresentation = commentsRepresentation;
    }
    
    /**
     * Generate the code for one SubjectArea.
     *
     * @param sas the SubjectArea
     * @throws IOException thrown if an I/O error occurred
     */
    public void generateForAll(
            SubjectArea [] sas )
        throws
            IOException
    {
        InterfaceGenerator theInterfaceGenerator = new InterfaceGenerator( theOutputDirectory, theCommentsRepresentation );
        theInterfaceGenerator.generateForAll( sas );

        ImplementationGenerator theImplementationGenerator = new ImplementationGenerator( theOutputDirectory, theCommentsRepresentation );
        theImplementationGenerator.generateForAll( sas );

        ModelLoaderGenerator theLoaderGenerator = new ModelLoaderGenerator( theOutputDirectory, theCommentsRepresentation );
        theLoaderGenerator.generateForAll( sas );
    }

    /**
     * The ModuleRegistry.
     */
    protected static ModuleRegistry theModuleRegistry;
    
    /**
     * The output directory for generated artifacts.
     */
    protected File theOutputDirectory;
    
    /**
     * The StringRepresentation for generated comments.
     */
    protected StringRepresentation theCommentsRepresentation;
}
