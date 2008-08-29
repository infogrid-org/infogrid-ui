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

package org.infogrid.module.commandline;

import org.infogrid.module.ModuleAdvertisement;
import org.infogrid.module.ModuleAdvertisementXmlParser;
import org.infogrid.module.ModuleConfigurationException;
import org.infogrid.module.ModuleErrorHandler;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.SoftwareInstallation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            // Load the ModuleAdvertisements that are held in XML files.
            List<File> moduleDirectories = theInstallation.getInstallModuleDirectories();
            
            for( File currentModuleDirectory : moduleDirectories ) {
                File [] candidateModuleDirs = currentModuleDirectory.listFiles();

                for( int i=0 ; i<candidateModuleDirs.length ; ++i ) {
                    if( !candidateModuleDirs[i].isDirectory() ) {
                        continue;
                    }

                    File distDir = new File( candidateModuleDirs[i], "dist" );
                    if( !distDir.exists() || !distDir.isDirectory() ) {
                        continue;
                    }

                    File [] candidateAds = distDir.listFiles( new FileFilter() {
                        public boolean accept(
                                File f )
                        {
                            return f.getName().endsWith( ".adv" );
                        }
                    });
                    for( int j=0 ; j<candidateAds.length ; ++j ) {
                        File candidateModuleFile = candidateAds[j];
                        if( !candidateModuleFile.exists() || !candidateModuleFile.canRead() ) {

                            candidateModuleFile = new File( candidateModuleDirs[i], BUILD_DIR + File.separatorChar + MODULE_XML_FILE );
                            if( !candidateModuleFile.exists() || !candidateModuleFile.canRead() ) {
                                continue;
                            }
                        }

                        BufferedInputStream theStream = null;
                        try {
                            theStream = new BufferedInputStream( new FileInputStream( candidateModuleFile ));

                            ModuleAdvertisement ad = theParser.readAdvertisement( theStream );
                            if( !ads.contains( ad )) {
                                ads.add( ad );
                            }

                        } catch( IOException ex ) {
                            ModuleErrorHandler.warn( "Could not read ModuleAdvertisement from " + candidateModuleFile.getCanonicalPath(), ex );
                        } catch( ModuleConfigurationException ex ) {
                            ModuleErrorHandler.warn( "Could not parse ModuleAdvertisement from " + candidateModuleFile.getCanonicalPath(), ex );
                        } finally {
                            if( theStream != null )
                            {
                                try {
                                    theStream.close();
                                } catch( IOException ex2 ) {
                                    // no op
                                }
                            }
                        }
                    }
                }
            }
        }

        return new CommandlineModuleRegistry( ads, theInstallation );
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
     * Find the JAR files for this ModuleAdvertisement. These must be local Files
     * (which may require an implementation of this method to download them first).
     * This method throws an Exception if a required File could not be found.
     * It returns an empty array if no local Files are required given the specific Module.
     * It returns null if the local installation has the JAR files on the classpath already.
     *
     * @param adv the ModuleAdvertisement to consider
     * @return the locally found File containing the JARs, or null if none needed to be consid
     * @throws IOException thrown if the Files could not be loaded
     */
    @Override
    public File [] findJarFilesFor(
            ModuleAdvertisement adv )
        throws
            IOException
    {
        List<File> installDirectories = ((CommandlineSoftwareInstallation)getSoftwareInstallation()).getInstallModuleDirectories();
        String     advName            = adv.getModuleName();
        String []  jars               = adv.getProvidesJars();

        File [] ret   = new File[ jars.length ];
        int     count = 0;
        
        for( int i=0 ; i<jars.length ; ++i ) {
            File found = null;
            for( File installDir : installDirectories ) {
                String relativePath = advName + File.separatorChar + DIST_DIR + File.separatorChar;
                File   candidate    = new File( installDir, relativePath + jars[i] );
                if( candidate.exists() && candidate.isFile() ) {
                    found = candidate;
                    break;
                }
            }
            if( found != null ) {
                ret[count++] = found;
            }
        }
        if( count < ret.length ) {
            File [] temp = ret;
            ret          = new File[ count ];
            System.arraycopy( temp, 0, ret , 0, count );
        }

        return ret;
    }
    
    /**
     * Obtain the directory for a Module specified through its ModuleAdvertisement.
     * This must be a local directory or may return null.
     * 
     * @param adv the ModuleAdvertisement to consider
     * @return the locally found directory, or null
     * @throws IOException thrown if the directory could not be accessed
     */
    @Override
    public File getModuleDirectoryFor(
            ModuleAdvertisement adv )
        throws
            IOException
    {
        List<File> installDirectories = ((CommandlineSoftwareInstallation)getSoftwareInstallation()).getInstallModuleDirectories();
        String     advName            = adv.getModuleName();

        for( File installDir : installDirectories ) {
            File candidate = new File( installDir, advName );
            if( candidate.exists() && candidate.isDirectory() ) {
                File ret = candidate.getCanonicalFile();
                return ret;
            }
        }
        return null;
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
