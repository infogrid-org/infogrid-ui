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

package org.infogrid.module.servlet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import org.infogrid.module.ModuleErrorHandler;
import org.infogrid.module.SoftwareInstallation;
import org.infogrid.module.SoftwareInstallationException;

/**
 * A SoftwareInstallation of a server-side J2EE installation.
 * Use this to obtain all paths, for example.
 */
public class ServletSoftwareInstallation
   extends
       SoftwareInstallation
{
    /**
     * Factory method to create a new instance of SoftwareInstallation from a default properties file.
     *
     * @return a newly created SoftwareInstallation
     * @throws SoftwareInstallationException if this software installation is incorrect, inconsistent or incomplete
     * @throws IOException if a needed file could not be loaded
     */
    public static ServletSoftwareInstallation createFromDefaultProperties()
        throws
            SoftwareInstallationException,
            IOException
    {
        ClassLoader loader     = SoftwareInstallation.class.getClassLoader();
        InputStream propStream = loader.getResourceAsStream( DEFAULT_PROPERTIES_FILE );

        return createFromProperties( propStream );
    }

    /**
     * Factory method to create a new instance of SoftwareInstallation from a properties file at a certain stream.
     *
     * @param propStream the InputStream from where to read the Properties
     * @return a newly created SoftwareInstallation
     * @throws SoftwareInstallationException if this software installation is incorrect, inconsistent or incomplete
     * @throws IOException if a needed file could not be loaded
     */
    public static ServletSoftwareInstallation createFromProperties(
            InputStream propStream )
        throws
            SoftwareInstallationException,
            IOException
    {
        if( propStream == null ) {
            throw new IOException( "Cannot read BootLoader properties from null stream" );
        }
        Properties props = new Properties();
        props.load( propStream );

        return createFromProperties( props );
    }

    /**
     * Factory method to create a new instance of SoftwareInstallation from java.util.Properties.
     *
     * @param props the Properties that we parse
     * @return a newly created SoftwareInstallation
     * @throws SoftwareInstallationException if this software installation is incorrect, inconsistent or incomplete
     */
    public static ServletSoftwareInstallation createFromProperties(
            Properties props )
        throws
            SoftwareInstallationException
    {
        String      rootModuleName             = null;
        String      activationClassName        = null;
        String      activationMethodName       = null;
        String      platform                   = null;
        String      log4jconfigUrlName         = DEFAULT_LOG4J_CONFIG_FILE;
        PrintStream moduleDebugStream          = null;
        String []   remainingArguments         = null;
        boolean     isDeveloper                = false;
        boolean     isDemo                     = false;
        boolean     isShowModuleRegistry       = false;

        Iterator iter = props.keySet().iterator();
        while( iter.hasNext() ) {
            String key   = (String) iter.next();
            String value = props.getProperty( key );

            if ( "rootmodule".equalsIgnoreCase( key )) {
                rootModuleName = value;

            } else if( "activationClassName".equalsIgnoreCase( key )) {
                activationClassName = value;

            } else if( "activationMethodName".equalsIgnoreCase( key )) {
                activationMethodName = value;

            // no run class here
            } else if ( "log4j".equalsIgnoreCase( key )) {
                log4jconfigUrlName = value;

            } else if ( "moduledebug".equalsIgnoreCase( key )) {
                String moduleDebugStreamName = value;
                if( "System.err".equalsIgnoreCase( moduleDebugStreamName )) {
                    moduleDebugStream = System.err;
                } else if( "System.out".equalsIgnoreCase( moduleDebugStreamName )) {
                    moduleDebugStream = System.out;
                } else {
                    try {
                        moduleDebugStream = new PrintStream( new FileOutputStream( moduleDebugStreamName ));
                    } catch( IOException ex ) {
                        usageJspThrow( "file " + moduleDebugStreamName + " cannot be opened for writing" );
                    }
                }
            } else if( "demo".equalsIgnoreCase( key )) {
                isDemo = true;
            } else if( "developer".equalsIgnoreCase( key )) {
                isDeveloper = true;
            } else if( "showmoduleregistry".equalsIgnoreCase( key )) {
                isShowModuleRegistry = true;
            } else if( "args".equalsIgnoreCase( key )) {
                StringTokenizer   token = new StringTokenizer( value );
                ArrayList<String> temp  = new ArrayList<String>();
                while( token.hasMoreElements() ) {
                    temp.add( token.nextToken());
                }
                // all other arguments are remaining arguments
                remainingArguments = new String[ temp.size() ];
                temp.toArray( remainingArguments );
            } else {
                usageJspThrow( "Unknown property " + key );
            }
        }

        if( rootModuleName == null ) {
            usageJspThrow( "no rootmodule argument given" );
        }

        // determine product name and version
        String productName = "unknown";
        String productId   = "unknown";

        // This does not actually occur in a typical JEE environment. It also throws
        // java.security.AccessControlException: access denied (java.util.PropertyPermission
        // product.name read). So I'm commenting it out.
//        try {
//            productName = System.getProperty( PRODUCT_NAME_PROPERTY );
//            productId   = System.getProperty( PRODUCT_ID_PROPERTY );
//
//        } catch( AccessControlException ex ) {
//            ModuleErrorHandler.informThrowable( ex );
//        }

        // determine platform
        platform = determinePlatform();

        // fix remaining arguments
        if( remainingArguments == null ) {
            remainingArguments = new String[0];
        }

        return new ServletSoftwareInstallation(
                platform,
                rootModuleName,
                activationClassName,
                activationMethodName,
                log4jconfigUrlName,
                productName,
                productId,
                isDeveloper,
                isDemo,
                isShowModuleRegistry,
                moduleDebugStream,
                remainingArguments );
    }

    /**
     * Private constructor, use factory  method.
     *
     * @param platform the platform on which we run, one of the pre-defined values in this class
     * @param rootModuleName the name of the root Module to run
     * @param activationClassName the name of the class to invoke to activate the root Module (overrides default specified in ModuleAdvertisement)
     * @param activationMethodName the name of the method to invoke to activate the root Module (overrides default specified in ModuleAdvertisement)
     * @param log4jconfigUrlName the URL to a log4j configuration file, if any
     * @param productName the product name
     * @param productId the product id
     * @param isDeveloper if true, run in developer mode
     * @param isDemo if true, run in demo mode
     * @param isShowModuleRegistry if true, print the content of the ModuleRegistry to the terminal
     * @param moduleDebugStream a stream for Module-related debug information (may be null)
     * @param remainingArguments the arguments on the command line not used by SoftwareInstallation itself
     * @throws SoftwareInstallationException if this software installation is incorrect, inconsistent or incomplete
     */
    protected ServletSoftwareInstallation(
            String      platform,
            String      rootModuleName,
            String      activationClassName,
            String      activationMethodName,
            String      log4jconfigUrlName,
            String      productName,
            String      productId,
            boolean     isDeveloper,
            boolean     isDemo,
            boolean     isShowModuleRegistry,
            PrintStream moduleDebugStream,
            String []   remainingArguments )
        throws
            SoftwareInstallationException
    {
        super(  platform,
                rootModuleName,
                activationClassName,
                activationMethodName,
                log4jconfigUrlName,
                productName,
                productId,
                false, // never use ModuleClassLoaders in J2EE mode
                false, // allowDefaultClassPathForRootModule
                isDeveloper,
                isDemo,
                isShowModuleRegistry,
                moduleDebugStream,
                remainingArguments );
    }

    /**
     * Obtain an "About" text that describes this piece of software.
     *
     * @return the "about" text
     */
    public final String getAboutText()
    {
        return theAboutText;
    }

    /**
     * Obtain the token conversion map from our subclass. This is invoked only once
     * and then buffered by the caller.
     *
     * @return a Map of tokens in the config files that must be replaced before using their data
     */
    @Override
    protected Map<String,String> getTokenConversionMap()
    {
        HashMap<String,String> ret = new HashMap<String,String>();

        ret.put( PRODUCTID_TOKEN, getProductId() );

        return ret;
    }

    /**
     * Prints usage message to the terminal for JSP invocation and throws RuntimeException.
     *
     * @param msg the message to print to the terminal
     */
    protected static void usageJspThrow(
            String msg )
    {
        StringBuffer fullMsg = new StringBuffer( 256 );

        if( msg != null ) {
            fullMsg.append( msg ).append( '\n' );
        }

        fullMsg.append( "Usage: set properties with the following names:\n" );
        fullMsg.append( "    moduledebug=<stream>                      debug module loading to System.out or System.err\n" );
        fullMsg.append( "    moduleadvertisementinstantiatorjars=<url> before running, load these ModuleAdvertisementInstantiator JARs (comma-separated)\n" );
        fullMsg.append( "    moduleadvertisementxmllistjars=<url>      before running, load the ModuleAdvertisment XMLs listed in these list files (comma-separated)\n" );
        fullMsg.append( "    rootmodule=<name>                         name of the root module to start\n" );
        fullMsg.append( "    activationclass=<class>                   override the activation class specified for the root module\n" );
        fullMsg.append( "    activationmethod=<method>                 override the activation method specified for the root module\n" );

        ModuleErrorHandler.print( fullMsg.toString() );

        throw new RuntimeException( "Incorrect configuration, check error log for more information" );
    }

    /**
     * The default location for our properties file.
     */
    public static final String DEFAULT_PROPERTIES_FILE = "BootLoader.properties";

    /**
     * The about text for this version. FIXME, needs to be internationalized.
     */
    protected static String theAboutText
            = "InfoGrid.org(tm)\n"
            + "\u00A9 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst\n"
            + "All rights reserved.\n"
            + "For more information about InfoGrid go to http://infogrid.org/";
}
