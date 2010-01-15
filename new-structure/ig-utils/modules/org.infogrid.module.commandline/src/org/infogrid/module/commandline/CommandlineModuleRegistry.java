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

package org.infogrid.module.commandline;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.infogrid.module.ModuleAdvertisement;
import org.infogrid.module.ModuleAdvertisementXmlParser;
import org.infogrid.module.ModuleConfigurationException;
import org.infogrid.module.ModuleErrorHandler;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.SoftwareInstallation;

/**
 * A ModuleRegistry particularly appropriate for the CommandlineBootLoader.
 */
public class CommandlineModuleRegistry
    extends
        ModuleRegistry
{
    /**
      * Factory method to construct a ModuleRegistry from the information provided
      * by this SoftwareInstallation.
      *
      * @param theInstallation the SoftwareInstallation that knows all the parameters
      * @return the created ModuleRegistry
      * @throws IOException thrown when we were unsuccessful reading a specified ModuleAdvertisement file
      * @throws ClassNotFoundException thrown when we were trying to read a ModuleAdvertisement subclass that was not available locally
      */
    public static CommandlineModuleRegistry create(
            CommandlineSoftwareInstallation theInstallation )
        throws
            IOException,
            ClassNotFoundException
    {
        ArrayList<ModuleAdvertisement> ads = new ArrayList<ModuleAdvertisement>( 64 );

        ModuleAdvertisementXmlParser theParser = null;
        try {
           theParser = new ModuleAdvertisementXmlParser();
           // this will throw an exception in a non-JDK 1.4 environment where XML is not built into the JDK
        } catch( Throwable t ) {
            ModuleErrorHandler.informThrowable( t );
        }

        if( theParser != null ) {
            // Load the ModuleAdvertisements
            List<String> moduleAdPaths = theInstallation.getModuleAdvertisementPaths();
            Set<File>    moduleAdFiles = new HashSet<File>();

            for( String currentPath : moduleAdPaths ) {
                addFilesAtPath( currentPath, moduleAdFiles );
            }

            for( File candidateFile : moduleAdFiles ) {

                if( !candidateFile.exists() || !candidateFile.canRead() ) {
                    continue;
                }

                BufferedInputStream theStream = null;
                try {
                    theStream = new BufferedInputStream( new FileInputStream( candidateFile ));

                    ModuleAdvertisement ad = theParser.readAdvertisement( theStream, candidateFile.getAbsoluteFile() );
                    if( !ads.contains( ad )) {
                        ads.add( ad );
                    }

                } catch( IOException ex ) {
                    ModuleErrorHandler.warn( "Could not read ModuleAdvertisement from " + candidateFile.getCanonicalPath(), ex );
                } catch( ModuleConfigurationException ex ) {
                    ModuleErrorHandler.warn( "Could not parse ModuleAdvertisement from " + candidateFile.getCanonicalPath(), ex );
                } finally {
                    if( theStream != null ) {
                        try {
                            theStream.close();
                        } catch( IOException ex2 ) {
                            // no op
                        }
                    }
                }
            }
        }

        return new CommandlineModuleRegistry( ads, theInstallation );
    }

    /**
     * Helper method to add the files found at a path, potentially with wildcards.
     *
     * @param path path, potentially with wildcards
     * @param found the collection of Files found so far
     */
    protected static void addFilesAtPath(
            String    path,
            Set<File> found )
    {
        File start;
        if( path.startsWith( "/" )) {
            start = new File( "/" );
            path = path.substring( 1 );
        } else {
            start = new File( "." );
        }
        try {
            start = start.getCanonicalFile();
        } catch( IOException ex ) {
            ModuleErrorHandler.warn( "Could not determine location of file " + path, ex );
        }

        String [] pathComponents = path.split( "/" );

        addFilesAtPath( pathComponents, 0, start, found );
    }

    /**
     * Recursive helper method to find the files at a path, potentially with wildcards.
     *
     * @param pathComponents the components of the path
     * @param here index into the pathComponents
     * @param currentLocation the current location in the file system
     * @param found the collection of Files found so far
     */
    protected static void addFilesAtPath(
            String [] pathComponents,
            int       here,
            File      currentLocation,
            Set<File> found )
    {
        final boolean isLast = here == pathComponents.length-1;

        if( ".".equals( pathComponents[ here ] )) {
            if( !isLast ) {
                addFilesAtPath( pathComponents, here+1, currentLocation, found ); // same location
            }
        } else if( "..".equals( pathComponents[ here ] )) {
            if( !isLast ) {
                addFilesAtPath( pathComponents, here+1, currentLocation.getParentFile(), found ); // one up
            }
        } else {
            final String  regex  = pathComponents[here].replace( ".", "\\." ).replace( "*" , ".*" ).replace( "?", "." ); // good enough

            File [] foundHere = currentLocation.listFiles( new FilenameFilter() {
                    public boolean accept(
                            File   dir,
                            String name )
                    {
                        if( !Pattern.matches( regex, name )) {
                            return false;
                        }
                        if( new File( dir, name ).isDirectory() ) {
                            return !isLast; // last cannot be directory
                        } else {
                            return isLast; // non-last cannot be file
                        }
                    }
            });
            if( isLast ) {
                for( int i=0 ; i<foundHere.length ; ++i ) {
                    found.add( foundHere[i] );
                }
            } else {
                for( int i=0 ; i<foundHere.length ; ++i ) {
                    addFilesAtPath( pathComponents, here+1, foundHere[i], found );
                }
            }
        }
    }

    /**
     * Private constructor, use factory method.
     *
     * @param ads the ModuleAdvertisements found
     * @param installation the SoftwareInstallation
     */
    protected CommandlineModuleRegistry(
            ArrayList<ModuleAdvertisement> ads,
            SoftwareInstallation           installation)
    {
        super( ads, installation );
    }

    /**
     * The name of the Module XML file.
     */
    public static final String MODULE_XML_FILE = "module.adv";

    /**
     * The name of the directory in which we find any built module.adv file.
     */
    public static final String BUILD_DIR = "dist";

    /**
     * The name of the directory in which we find the JAR files.
     */
    public static final String DIST_DIR = "dist";
}
