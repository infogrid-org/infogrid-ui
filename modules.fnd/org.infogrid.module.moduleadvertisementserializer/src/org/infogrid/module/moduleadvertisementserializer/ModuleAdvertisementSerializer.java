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

package org.infogrid.module.moduleadvertisementserializer;

import org.infogrid.module.ModuleAdvertisement;
import org.infogrid.module.ModuleAdvertisementXmlParser;
import org.infogrid.module.ModuleConfigurationException;
import org.infogrid.module.ModuleRequirement;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;

/**
 * <p>This helper program takes XML ModuleAdvertisements and converts them into a representation
 * that takes less resources to load. Currently, the following formats are supported:</p>
 * <ul>
 *  <li>Java Serialization (<code>-ser</code> argument)</li>
 *  <li>XML Serialization (<code>-xml</code> argument)</li>
 *  <li>Java code generation (<code>-java</code> argument)</li>
 * </ul>
 * <p>In addition, some debug output options are supported.
 */
public class ModuleAdvertisementSerializer
{
    /**
      * <p>Main program.</p>
      *
      * @param args command-line arguments
      */
    public static void main(
            String [] args )
    {
        try {
            if( args.length != 3 ) {
                synopsisAndExit( args );
            }

            Properties logProperties = new Properties();
            logProperties.load( new BufferedInputStream(
                    ModuleAdvertisementSerializer.class.getClassLoader().getResourceAsStream(
                            "org/infogrid/module/moduleadvertisementserializer/log4jconfig.properties" )));

            String  mode    = args[0];
            String  listKey = "list";
            boolean doList  = false;

            if( mode.length() > listKey.length() && mode.endsWith( listKey )) {
                int    index   = mode.length() - listKey.length();
                String trailer = mode.substring( index );

                mode   = mode.substring( 0, index );
                doList = listKey.equalsIgnoreCase( trailer );
            }
            boolean doSerialize = "-ser".equalsIgnoreCase( mode );
            boolean doXml       = "-xml".equalsIgnoreCase( mode );
            boolean doJava      = "-java".equalsIgnoreCase( mode );
            boolean doDebug     = "-debug".equalsIgnoreCase( mode );

            if( !( doSerialize || doXml || doJava || doDebug )) {
                synopsisAndExit( args );
            }

            File inputFile = new File( args[1] );
            File outputDir = new File( args[2] );

            if( !inputFile.exists() || !inputFile.canRead() || inputFile.isDirectory() ) {
                throw new IOException( "Cannot read file " + inputFile.getAbsolutePath() );
            }
            if( !outputDir.exists() || !outputDir.canRead() || !outputDir.isDirectory() ) {
                throw new IOException( "Cannot access directory " + outputDir.getAbsolutePath() );
            }

            if( doList ) {
                BufferedReader listReader = new BufferedReader( new InputStreamReader( new FileInputStream( inputFile )));
                String         listLine;

                while( ( listLine = listReader.readLine()) != null ) {

                    File realInputFile = new File( inputFile, listLine );
                    if( !realInputFile.exists() || !realInputFile.canRead() || realInputFile.isDirectory() ) {
                        throw new IOException( "Cannot read file " + realInputFile.getAbsolutePath() );
                    }

                    processOne(
                            realInputFile,
                            outputDir,
                            doSerialize,
                            doXml,
                            doJava,
                            doDebug );
                }
            } else {
                processOne(
                        inputFile,
                        outputDir,
                        doSerialize,
                        doXml,
                        doJava,
                        doDebug );
            }
        } catch( Exception ex ) {
            ex.printStackTrace( System.err );
            System.exit( 2 );
        }

        System.exit( 0 );
    }

    /**
     * Process one ModuleAdvertisement.
     *
     * @param inputFile the input ModuleAdvertisement file
     * @param outputDir the directory in which to generate the output
     * @param doSerialize if true, serialize the ModuleAdvertisement
     * @param doXml if true, generate an XML representation of the ModuleAdvertisement
     * @param doJava if true, generate Java code instantiating the ModuleAdvertisement
     * @param doDebug if true, create a debug file
     * @throws IOException thrown if the input could not be read or the output not written
     * @throws ModuleConfigurationException thrown if the Module was configured incorrectly 
     */
    protected static void processOne(
            File    inputFile,
            File    outputDir,
            boolean doSerialize,
            boolean doXml,
            boolean doJava,
            boolean doDebug )
        throws
            IOException,
            ModuleConfigurationException
    {
        ModuleAdvertisementXmlParser theParser = new ModuleAdvertisementXmlParser();

        InputStream fromStream = new FileInputStream( inputFile );

        ModuleAdvertisement theAdv = theParser.readAdvertisement( fromStream );

        String moduleName    = theAdv.getModuleName();
        String moduleVersion = theAdv.getModuleVersion();

        if( moduleVersion == null ) {
            moduleVersion = "";
        }

        String fileName  = null; // make compiler happy
        String extension = "";
        if( doSerialize ) {
            extension = ".ser";
            fileName  = moduleName + ".V" + moduleVersion + extension; 
        } else if( doXml ) {
            extension = ".adv";
            fileName  = moduleName + ".V" + moduleVersion + extension; 
        } else if( doJava ) {
            extension = ".java";
            fileName  = moduleName + ".module.V" + moduleVersion;
            fileName  = fileName.replace( '.', File.separatorChar ) + extension;
        } else if( doDebug ) {
            extension = ".dbg";
            fileName  = moduleName + "." + moduleVersion + extension; 
        }

        File outputFile = new File( outputDir, fileName );
        outputFile.getParentFile().mkdirs();
        
        OutputStream toStream = null;
        if( doSerialize ) {
            toStream = new FileOutputStream( outputFile );
            writeSerializedAdvertisement( theAdv, toStream );
        } else if( doXml ) {
            toStream = new FileOutputStream( outputFile );
            writeXmlAdvertisement( theAdv, toStream );
        } else if( doJava ) {
            toStream = new FileOutputStream( outputFile );
            writeJavaAdvertisement( theAdv, toStream );
            toStream.flush();
            toStream.close();
            
            toStream = new FileOutputStream( new File( outputFile.getParentFile(), "package.html" ));
            writeJavaPackageHtmlFile( theAdv, toStream );
        } else if( doDebug ) {
            toStream = new FileOutputStream( outputFile );
            writeDebugAdvertisement( theAdv, toStream );
        }
        toStream.flush();
        toStream.close();

    }

    /**
     * Write the ModuleAdvertisement to a stream in Java serialized form.
     *
     * @param adv the ModuleAdvertisement to write
     * @param theStream the stream to which we write
     * @throws IOException a write operation was unsuccessful
     */
    public static void writeSerializedAdvertisement(
            ModuleAdvertisement adv,
            OutputStream        theStream )
        throws
            IOException
    {
        ObjectOutput theObjectStream = new ObjectOutputStream( theStream );

        theObjectStream.writeObject( adv );

        theObjectStream.flush();
    }

    /**
     * Write the ModuleAdvertisement to a stream as XML.
     *
     * @param adv the ModuleAdvertisement to write
     * @param theStream the stream to which we write
     * @throws IOException a write operation was unsuccessful
     */
    public static void writeXmlAdvertisement(
            ModuleAdvertisement adv,
            OutputStream        theStream )
        throws
            IOException
    {
        OutputStreamWriter out = new OutputStreamWriter( theStream, "UTF-8" );
        out.append( "<?xml version=\"1.0\" encoding=\"" );
        out.append( out.getEncoding() );
        out.append( "\"?>\n" );
        out.append( adv.getAsXml() );
        out.flush();
    }

    /**
     * Write the ModuleAdvertisement to a stream as Java code.
     *
     * @param adv the ModuleAdvertisement to write
     * @param theStream the stream to which we write
     * @throws IOException a write operation was unsuccessful
     */
    public static void writeJavaAdvertisement(
            ModuleAdvertisement adv,
            OutputStream        theStream )
        throws
            IOException
    {
        PrintWriter out = new PrintWriter( theStream );
        out.println( "//" );
        out.println( "// This file is part of InfoGrid(tm). You may not use this file except in" );
        out.println( "// compliance with the InfoGrid license. The InfoGrid license and important" );
        out.println( "// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should" );
        out.println( "// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt" );
        out.println( "// or you do not consent to all aspects of the license and the disclaimers," );
        out.println( "// no license is granted; do not use this file." );
        out.println( "//" );
        out.println( "// For more information about InfoGrid go to http://infogrid.org/" );
        out.println( "//" );
        out.println( "// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst" );
        out.println( "// All rights reserved." );
        out.println( "//" );
        out.println();

        out.println( "package " + adv.getModuleName() + ".module;" );
        out.println();
        out.println( "import org.infogrid.module.*;" );
        out.println( "import java.net.URL;" );
        out.println();

        String moduleVersion = adv.getModuleVersion();
        if( moduleVersion == null ) {
            moduleVersion = "";
        }

        out.println( "/**" );
        out.print(   "  * Instantiates Module " + adv.getModuleName() );
        if( adv.getModuleVersion() != null ) {
            out.print( " in version " + adv.getModuleVersion() );
        } else {
            out.print( " (no specific version given)" );
        }
        out.println( "." );
        out.println( "  */" );
        out.println( "public class V" + moduleVersion );
        out.println( "    implements" );
        out.println( "        ModuleAdvertisementInstantiator" );
        out.println( "{" );
        out.println( "    /**" );
        out.println( "      * No-op constructor." );
        out.println( "      */" );
        out.println( "    public V" + moduleVersion + "()" );
        out.println( "    {}" );
        out.println();
        out.println( "    /**" );
        out.println( "      * Create the ModuleAdvertisement that is instantiated by this class." );
        out.println( "      *" );
        out.println( "      * @return the created ModuleAdvertisement" );
        out.println( "      */" );
        out.println( "    public ModuleAdvertisement create()" );
        out.println( "        throws" );
        out.println( "            ModuleAdvertisementInstantiationException" );
        out.println( "    {" );
        out.println( "        try {" );
        out.println( "            ModuleAdvertisement ret = " + adv.getJavaConstructorString( 2 ) + ";" );
        out.println();
        out.println( "            return ret;" );
        out.println( "        } catch( Throwable ex ) {" );
        out.println( "            throw new ModuleAdvertisementInstantiationException( this, ex );" );
        out.println( "        }" );
        out.println( "    }" );
        out.println( "}" );
        out.flush();
    }

    /**
     * Write the package.html documentation file to a stream.
     * 
     * @param adv the ModuleAdvertisement to write
     * @param theStream the OutputStream to write to
     * @throws IOException thrown if an I/O error occurred
     */
    public static void writeJavaPackageHtmlFile(
             ModuleAdvertisement adv,
             OutputStream        theStream )
        throws
             IOException
    {
        PrintWriter out = new PrintWriter( theStream );

        out.println( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">" );
        out.println();
        out.println( "<html>" );
        out.println( " <head>" );
        out.println( "    <title>Package " + adv.getModuleName() + ".module</title>" );
        out.println( "  </head>" );
        out.println( "  <body>" );
        out.println( "    <p>Provides the Java form of this Module's ModuleAdvertisement.</p>" );
        out.println();
        out.println( "    <p>For more information, please refer to the documentation for the InfoGrid Module Framework.</p>" );
        out.println( "  </body>" );
        out.println( "</html>" );
        out.flush();
    }
   /**
     * Write the ModuleAdvertisement to a stream as debug output.
     *
     * @param adv the ModuleAdvertisement to write
     * @param theStream the stream to which we write
     * @throws IOException a write operation was unsuccessful
     */
     public static void writeDebugAdvertisement(
             ModuleAdvertisement adv,
             OutputStream        theStream )
        throws
             IOException
    {
        PrintWriter out = new PrintWriter( theStream );

        out.println( adv.getClass().getName() );
        out.println( "    ModuleName: " + adv.getModuleName() );
        out.println( "    ModuleVersion: " + adv.getModuleVersion() );
        out.println( "    ModuleLicense: " + adv.getModuleLicense() );
        out.println( "    ModuleRequirements (build-time):" );
        ModuleRequirement [] reqs = adv.getBuildTimeModuleRequirements();
        for( int i=0 ; i<reqs.length ; ++i )
        {
            out.println( "        " + reqs[i].getRequiredModuleName() );
            if( reqs[i].getLocalParameterValues() != null )
            {
                out.println( "        PARAMETER-VALUES:" );
                Iterator iter = reqs[i].getLocalParameterValues().keySet().iterator();
                while( iter.hasNext() )
                {
                    String key = (String) iter.next();
                    out.println( "            " + key + " -> " + reqs[i].getLocalParameterValues().get( key ));
                }
            }
            if( reqs[i].getLocalParameterDefaults() != null )
            {
                out.println( "        PARAMETER-DEFAULTS:" );
                Iterator iter = reqs[i].getLocalParameterDefaults().keySet().iterator();
                while( iter.hasNext() )
                {
                    String key = (String) iter.next();
                    out.println( "            " + key + " -> " + reqs[i].getLocalParameterDefaults().get( key ));
                }
            }
        }

        out.println( "    ModuleRequirements (run-time):" );
        reqs = adv.getRunTimeModuleRequirements();
        for( int i=0 ; i<reqs.length ; ++i )
        {
            out.println( "        " + reqs[i].getRequiredModuleName() );
            if( reqs[i].getLocalParameterValues() != null )
            {
                out.println( "        PARAMETER-VALUES:" );
                Iterator iter = reqs[i].getLocalParameterValues().keySet().iterator();
                while( iter.hasNext() )
                {
                    String key = (String) iter.next();
                    out.println( "            " + key + " -> " + reqs[i].getLocalParameterValues().get( key ));
                }
            }
            if( reqs[i].getLocalParameterDefaults() != null )
            {
                out.println( "        PARAMETER-DEFAULTS:" );
                Iterator iter = reqs[i].getLocalParameterDefaults().keySet().iterator();
                while( iter.hasNext() )
                {
                    String key = (String) iter.next();
                    out.println( "            " + key + " -> " + reqs[i].getLocalParameterDefaults().get( key ));
                }
            }
        }

        out.println( "    JARS:" );
        String [] jars = adv.getProvidesJars();
        for( int i=0 ; i<jars.length ; ++i ) {
            out.println( "        " + jars[i] );
        }

        if( adv.getLocalParameterValues() != null )
        {
            out.println( "    PARAMETER-VALUES:" );
            Iterator iter = adv.getLocalParameterValues().keySet().iterator();
            while( iter.hasNext() )
            {
                String key = (String) iter.next();
                out.println( "        " + key + " -> " + adv.getLocalParameterValues().get( key ));
            }
        }
        if( adv.getLocalParameterDefaults() != null )
        {
            out.println( "    PARAMETER-DEFAULTS:" );
            Iterator iter = adv.getLocalParameterDefaults().keySet().iterator();
            while( iter.hasNext() )
            {
                String key = (String) iter.next();
                out.println( "        " + key + " -> " + adv.getLocalParameterDefaults().get( key ));
            }
        }
        out.flush();
    }

    /**
     * Print synopsis and exit.
     *
     * @param args the arguments that were provided for the invocation
     */
    private static void synopsisAndExit(
            String [] args )
    {
        System.err.println( "Synopsys: (alternatives)" );
        System.err.println( "-ser        <XML advertisement file> <output directory>" );
        System.err.println( "-xml        <XML advertisement file> <output directory>" );
        System.err.println( "-java       <XML advertisement file> <output directory>" );
        System.err.println( "-debug      <XML advertisement file> <output directory>" );
        System.err.println( "Called with:" );
        for( int i=0 ; i<args.length ; ++i ) {
            System.err.println( "    [" + i + "]: " + args[i] );
        }
        System.err.println( "Aborting ...\n" );
        System.exit( 1 );
    }
}
