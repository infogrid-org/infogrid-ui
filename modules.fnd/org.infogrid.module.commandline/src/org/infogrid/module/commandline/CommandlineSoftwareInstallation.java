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

import org.infogrid.module.ModuleErrorHandler;
import org.infogrid.module.SoftwareInstallation;
import org.infogrid.module.SoftwareInstallationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A local software installation.
 * Use this to obtain all paths, for example.
 */
public class CommandlineSoftwareInstallation
    extends
        SoftwareInstallation
{
    /**
     * Factory method to create a new instance of SoftwareInstallation from the command-line arguments passed
     * into this method.
     *
     * @param args the command-line arguments to this invocation
     * @return a newly created SoftwareInstallation
     * @throws SoftwareInstallationException if this software installation is incorrect, inconsistent or incomplete
     */
    public static CommandlineSoftwareInstallation createFromCommandLine(
            String [] args )
        throws
            SoftwareInstallationException
    {
        String            userDataDirName                    = null;
        ArrayList<String> installModuleDirNames              = new ArrayList<String>();
        String            homeObjectUrlName                  = null;
        String            rootModuleName                     = null;
        String            activationClassName                = null;
        String            activationMethodName               = null;
        String            runClassName                       = null;
        String            runMethodName                      = null;
        String            platform                           = null;
        String            log4jconfigUrlName                 = DEFAULT_LOG4J_CONFIG_FILE;
        PrintStream       moduleDebugStream                  = null;
        String []         remainingArguments                 = null;
        boolean           useModuleClassLoaders              = true;
        boolean           allowDefaultClassPathForRootModule = false;
        boolean           showSplash                         = false;
        boolean           isDeveloper                        = false;
        boolean           isDemo                             = false;
        boolean           isErrorTextOnly                    = true;
        boolean           isShowModuleRegistry               = false;
        long              sleepPeriodBeforeExit              = 0L;

        for( int i = 0; i < args.length; ++i ) {

            if( "-help".equalsIgnoreCase( args[i] )) {
                usageCommandLineExit( null );
                
            } else if ( "-userdir".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    userDataDirName = args[i];
                } else {
                    usageCommandLineThrow( "argument missing after -userdir" );
                }

            } else if ( "-homeurl".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    homeObjectUrlName = args[i];
                } else {
                    usageCommandLineThrow( "argument missing after -homeurl" );
                }

            } else if ( "-installdir".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    for( String dir : args[i].split( ":" )) {
                        installModuleDirNames.add( dir );
                    }
                } else {
                    usageCommandLineThrow( "argument missing after -installdir" );
                }

            } else if ( "-rootmodule".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    rootModuleName = args[i];
                } else {
                    usageCommandLineThrow( "argument missing after -rootmodule" );
                }

            } else if ( "-activationClass".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    activationClassName = args[i];
                } else {
                    usageCommandLineThrow( "argument missing after -activationclass" );
                }

            } else if ( "-activationMethod".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    activationMethodName = args[i];
                } else {
                    usageCommandLineThrow( "argument missing after -activationmethod" );
                }

            } else if ( "-runclass".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    runClassName = args[i];
                } else {
                    usageCommandLineThrow( "argument missing after -runclass" );
                }

            } else if ( "-runMethodName".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    runMethodName = args[i];
                } else {
                    usageCommandLineThrow( "argument missing after -runMethodName" );
                }

            } else if ( "-log4j".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    log4jconfigUrlName = args[i];
                } else {
                    usageCommandLineThrow( "argument missing after -log4j" );
                }

            } else if ( "-moduledebug".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    String moduleDebugStreamName = args[i];
                    if( "System.err".equalsIgnoreCase( moduleDebugStreamName )) {
                        moduleDebugStream = System.err;
                    } else if( "System.out".equalsIgnoreCase( moduleDebugStreamName )) {
                        moduleDebugStream = System.out;
                    } else {
                        try {
                            moduleDebugStream = new PrintStream( new FileOutputStream( moduleDebugStreamName ));
                        } catch( IOException ex ) {
                            usageCommandLineThrow( "file " + moduleDebugStreamName + " cannot be opened for writing" );
                        }
                    }
                } else {
                    usageCommandLineThrow( "argument missing after -moduledebug" );
                }

            } else if ( "-nomoduleclassloaders".equalsIgnoreCase( args[i] )) {
                useModuleClassLoaders = false;
            } else if ( "-allowdefaultclasspathforrootmodule".equalsIgnoreCase( args[i] )) {
                allowDefaultClassPathForRootModule = true;
            } else if ( "-splash".equalsIgnoreCase( args[i] )) {
                showSplash = true;
            } else if ( "-demo".equalsIgnoreCase( args[i] )) {
                isDemo = true;
            } else if ( "-developer".equalsIgnoreCase( args[i] )) {
                isDeveloper = true;
            } else if( "-showmoduleregistry".equalsIgnoreCase( args[i] )) {
                isShowModuleRegistry = true;
            } else if( "-debug".equalsIgnoreCase( args[i] )) {
                ModuleErrorHandler.setDebugStream( System.err );
            } else if( "-sleepbeforeexit".equalsIgnoreCase( args[i] )) {
                if( ++i < args.length ) {
                    sleepPeriodBeforeExit = Long.parseLong( args[i] );
                } else {
                    usageCommandLineThrow( "argument missing after -sleepbeforeexit" );
                }
                
            } else {
                // all other arguments are remaining arguments
                remainingArguments = new String[ args.length - i ];
                System.arraycopy( args, i, remainingArguments, 0, remainingArguments.length );
                break;
            }
        }
        if( rootModuleName == null ) {
            usageCommandLineThrow( "no rootmodule argument given" );
        }

        // determine product name and version
        String productName = System.getProperty( PRODUCT_NAME_PROPERTY );
        String productId   = System.getProperty( PRODUCT_ID_PROPERTY );
        if( productId == null ) {
            productId = parseProductIdFrom( installModuleDirNames );
        }
        // determine platform
        platform = determinePlatform();

        // determine directories that weren't given
        if( installModuleDirNames == null || installModuleDirNames.isEmpty() ) {
            String homeDir = System.getProperty( "user.home" );
            installModuleDirNames.add( homeDir + File.separator + installDirTable.get( platform ));
        }
        if( userDataDirName == null ) {
            String homeDir = System.getProperty( "user.home" );
            userDataDirName = homeDir + File.separator + userDataDirTable.get( platform );
        }

        // fix remaining arguments
        if( remainingArguments == null ) {
            remainingArguments = new String[0];
        }
        return new CommandlineSoftwareInstallation(
                platform,
                installModuleDirNames,
                userDataDirName,
                homeObjectUrlName,
                rootModuleName,
                activationClassName,
                activationMethodName,
                runClassName,
                runMethodName,
                log4jconfigUrlName,
                productName,
                productId,
                useModuleClassLoaders,
                allowDefaultClassPathForRootModule,
                showSplash,
                isDeveloper,
                isDemo,
                isErrorTextOnly,
                isShowModuleRegistry,
                sleepPeriodBeforeExit,
                moduleDebugStream,
                remainingArguments );
    }

    /**
     * Private constructor, use factory method.
     *
     * @param platform the platform on which we run, one of the pre-defined values in this class
     * @param installModuleDirNames names of the installation directories in which the Modules can be found
     * @param userDataDirName the user's data directory for this application
     * @param homeObjectUrlName the start object, if any
     * @param rootModuleName the name of the root Module to run
     * @param activationClassName the name of the class to invoke to activate the root Module (overrides default specified in ModuleAdvertisement)
     * @param activationMethodName the name of the method to invoke to activate the root Module (overrides default specified in ModuleAdvertisement)
     * @param runClassName the name of the class to run in the root Module (overrides default specified in ModuleAdvertisement)
     * @param runMethodName the name of the method to run in the root Module (overrides default specified in ModuleAdvertisement)
     * @param log4jconfigUrlName the URL to a log4j configuration file, if any
     * @param productName the product name
     * @param productId the product id
     * @param useModuleClassLoaders if true, use ModuleClassLoaders to load the Modules
     * @param allowDefaultClassPathForRootModule if true, allow the default ClassLoader for the the root Module (only)
     * @param showSplash if true, show splash screen
     * @param isDeveloper if true, run in developer mode
     * @param isDemo if true, run in demo mode
     * @param isErrorTextOnly if true, do not pop up dialog for module error messages
     * @param sleepPeriodBeforeExit the number of milliseconds to sleep prior to exiting the VM
     * @param isShowModuleRegistry if true, print the content of the ModuleRegistry to the terminal
     * @param moduleDebugStream a stream for Module-related debug information (may be null)
     * @param remainingArguments the arguments on the command line not used by SoftwareInstallation itself
     * @throws SoftwareInstallationException if this software installation is incorrect, inconsistent or incomplete
     */
    protected CommandlineSoftwareInstallation(
            String       platform,
            List<String> installModuleDirNames,
            String       userDataDirName,
            String       homeObjectUrlName,
            String       rootModuleName,
            String       activationClassName,
            String       activationMethodName,
            String       runClassName,
            String       runMethodName,
            String       log4jconfigUrlName,
            String       productName,
            String       productId,
            boolean      useModuleClassLoaders,
            boolean      allowDefaultClassPathForRootModule,
            boolean      showSplash,
            boolean      isDeveloper,
            boolean      isDemo,
            boolean      isErrorTextOnly,
            boolean      isShowModuleRegistry,
            long         sleepPeriodBeforeExit,
            PrintStream  moduleDebugStream,
            String []    remainingArguments )
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
                useModuleClassLoaders,
                allowDefaultClassPathForRootModule,
                isDeveloper,
                isDemo,
                isShowModuleRegistry,
                moduleDebugStream,
                remainingArguments );

        theUserDataDir       = new File( userDataDirName );
        theHomeObjectUrlName = homeObjectUrlName;

        theRunClassName      = runClassName;
        theRunMethodName     = runMethodName;

        theShowSplash        = showSplash;
        theIsErrorTextOnly   = isErrorTextOnly;

        theInstallModuleDirs = new ArrayList<File>();
        for( String installModuleDirName : installModuleDirNames ) {
            File candidate = new File( installModuleDirName );
            if( candidate.exists() && candidate.isDirectory() ) {
                theInstallModuleDirs.add( candidate );
            }
        }

        if( theUseModuleClassLoaders ) {
            if( theInstallModuleDirs.isEmpty() ) {
                throw new SoftwareInstallationException( "Installation directory/ies do not exist or are not directory/ies" );
            }
        }
        if( theUserDataDir.exists() && !theUserDataDir.isDirectory() ) {
            throw new SoftwareInstallationException( "User Data directory " + userDataDirName + " exists but isn't a directory" );
        }
        
        theSleepPeriodBeforeExit = sleepPeriodBeforeExit;
    }

    /**
     * Helper method to derive a product id from a directory. Handles the situation that
     * on the PC running under Cygwin the separator is a "/".
     *
     * @param installDirNames names of the installation directories
     * @return the found version id
     */
    static private String parseProductIdFrom(
            List<String> installDirNames )
    {
        if( installDirNames == null || installDirNames.isEmpty() ) {
            return UNKNOWN_PRODUCT_ID;
        }

        // go through the absolute path name
        String installDirName = null;
        for( String candidate : installDirNames ) {
            try {
                File installDirFile = new File( candidate );
                installDirName = installDirFile.getCanonicalPath();

                break; // if no exception
                
            } catch( IOException ex ) { // directory does not exist
            }
        }
        if( installDirName != null ) {
            return UNKNOWN_PRODUCT_ID;
        }

        String productId;
        if( installDirName.endsWith( File.separator )) {
            productId = installDirName.substring( 0, installDirName.length() - File.separator.length() );
        } else if( installDirName.endsWith( "/" )) {
            productId = installDirName.substring( 0, installDirName.length() - 1 );
        } else {
            productId = installDirName;
        }
        int lastSlash = productId.lastIndexOf( File.separator );
        if( lastSlash < 0 ) {
            lastSlash = productId.lastIndexOf( "/" );
        }
        if( lastSlash >=0 ) {
            productId = productId.substring( lastSlash+1 );
        }
        return productId;
    }

    /**
     * Obtain the installation directories in which the Modules can be found.
     *
     * @return the installation directories
     */
    @Override
    public final List<File> getInstallModuleDirectories()
    {
        return theInstallModuleDirs;
    }

    /**
     * Obtain the user data directory. This may not exist when called, but the
     * call will attempt to create it. However, that may fail.
     *
     * @return the user data directory
     * @throws SoftwareInstallationException thrown if the user data directory does not exist and cannot be created
     */
    public final File getUserDataDirectory()
        throws
            SoftwareInstallationException
    {
        if( !theUserDataDir.exists() && !theUserDataDir.mkdirs() ) {
            throw new SoftwareInstallationException( "User Data directory " + theUserDataDir + " could not be created" );
        }
        return theUserDataDir;
    }

    /**
     * Determine whether we are only supposed to print, to the terminal, error messages in the BootLoader.
     *
     * @return if true, only print to the terminal
     */
    @Override
    public final boolean isErrorTextOnly()
    {
        return theIsErrorTextOnly;
    }

    /**
     * Obtain the run class specified in the command-line, if any.
     *
     * @return name of the class
     */
    public final String getOverriddenRunClassName()
    {
        return theRunClassName;
    }

    /**
     * Obtain the run method specified in the command-line, if any.
     *
     * @return name of the method (without class name)
     */
    public final String getOverriddenRunMethodName()
    {
        return theRunMethodName;
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
     * Obtain the number of milliseconds to sleep before exiting the main method.
     * This can be very convenient for profiling, for example.
     * 
     * @return the time before exit, in milliseconds
     */
    public final long getSleepPeriodBeforeExit()
    {
        return theSleepPeriodBeforeExit;
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

        if( theInstallModuleDirs != null && ! theInstallModuleDirs.isEmpty() ) {
            StringBuilder buf = new StringBuilder();
            String        sep = "";

            for( File current : theInstallModuleDirs ) {
                try {
                    buf.append( sep );
                    buf.append( current.getCanonicalPath() );
                    sep = ", ";
                } catch( IOException ex ) {
                    ModuleErrorHandler.error( ex );
                }
            }

            ret.put( INSTALL_MODULE_DIR_TOKEN, buf.toString() );
        }

        if( theUserDataDir != null ) {
            try {
                ret.put( USERDATA_DIR_TOKEN, theUserDataDir.getCanonicalPath() );
            } catch( IOException ex ) {
                ModuleErrorHandler.error( ex );
            }
        }

        ret.put( PRODUCTID_TOKEN, getProductId() );

        return ret;
    }

    /**
     * Prints usage message to the terminal for command-line invocation.
     *
     * @param msg the message to print to the terminal
     */
    protected static void usageCommandLine(
            String msg )
    {
        StringBuffer fullMsg = new StringBuffer( 512 );
        fullMsg.append( "Command line: " );

        if( msg != null ) {
            fullMsg.append( msg ).append( '\n' );
        }
        fullMsg.append( "Usage:\n" );
        fullMsg.append( "    [ -help ]                      prints out this synopsys\n" );
        fullMsg.append( "    [ -userdir <dir> ]             directory for user data instead of the default\n" );
        fullMsg.append( "    [ -homeurl <url> ]             url of an object to be opened instead of the default\n" );
        fullMsg.append( "    [ -moduledebug <stream> ]      debug module loading to System.out or System.err\n" );
        fullMsg.append( "    [ -nomoduleclassloaders ]      do not use ModuleClassLoaders (default is on)\n" );
        fullMsg.append( "    -installdir <dir>              installation directory\n" );
        fullMsg.append( "    -rootmodule <name>             name of the root module to start\n" );
        fullMsg.append( "    [ -activationclass <class> ]   override the activation class specified for the root module\n" );
        fullMsg.append( "    [ -activationmethod <method> ] override the activation method specified for the root module\n" );
        fullMsg.append( "    [ -runclass <class> ]          override the run class specified for the root module\n" );
        fullMsg.append( "    [ -runmethod <method> ]        override the run method specified for the root module\n" );
        // fullMsg.append( "    [ -errortextonly ]             errors shall only be printed onto the terminal\n" );
        fullMsg.append( "    [ -showmoduleregistry ]        print the content of the ModuleRegistry to the terminal\n" );

        ModuleErrorHandler.print( fullMsg.toString() );
    }

    /**
     * Prints usage message to the terminal and exits.
     *
     * @param msg the message to print to the terminal
     */
    protected static void usageCommandLineExit(
            String msg )
    {
        usageCommandLine( msg );
        System.exit( 1 );
    }

    /**
     * Prints usage message to the terminal and throws Exception.
     *
     * @param msg the message to print to the terminal
     */
    protected static void usageCommandLineThrow(
            String msg )
    {
        usageCommandLine( msg );
        throw new IllegalArgumentException( msg );
    }

    /**
     * Start this platform's browser.
     *
     * Subtype this for JNLP, and run this code:
     *   theBasicService.showDocument( u );
     * FIXME
     *
     * @param u the URL we want to open in the browser
     * @throws IOException if we can't execute the browser executable
     */
    @Override
    public void openInBrowser(
            URL u )
        throws
            IOException
    {
        // FIXME implement reasonably multi-platform support
        if( getPlatform().equals( MAC_OSX_PLATFORM )) {
            // we are on the Mac
            try {
                Class mrjFileUtilsClass = Class.forName( "com.apple.mrj.MRJFileUtils" );
                Method openUrlMethod = mrjFileUtilsClass.getDeclaredMethod(
                        "openURL",
                        new Class[] { String.class } );

                openUrlMethod.invoke( null, new Object[] { u.toString() } );

            } catch( Exception ex ) {
                ex.printStackTrace( System.err );
            }

        } else if( getPlatform().equals( WINDOWS_PLATFORM )) {
            // we are on the PC.

            StringBuffer cmd = new StringBuffer();
            cmd.append("cmd /c start ");

            cmd.append( u.toString() );

            // issue command line command
            Process p = Runtime.getRuntime().exec( cmd.toString() );

        } else {
            ModuleErrorHandler.error( "Browsers not supported right now on this platform" );
            return;
        }
    }

    /**
     * Convert to String, for debugging purposes.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( super.toString() );
        buf.append( "{\n" );
        buf.append( "    thePlatform:              " ).append( thePlatform ).append( "\n" );
        buf.append( "    theRootModuleRequirement: " ).append( theRootModuleRequirement ).append( "\n" );
        buf.append( "    theActivationClassName:   " ).append( theActivationClassName ).append( "\n" );
        buf.append( "    theActivationMethodName:  " ).append( theActivationMethodName ).append( "\n" );
        buf.append( "    theHostName:              " ).append( theHostName ).append( "\n" );
        buf.append( "    theProductName:           " ).append( theProductName ).append( "\n" );
        buf.append( "    theProductId:             " ).append( theProductId ).append( "\n" );
        buf.append( "    theUseModuleClassLoaders: " ).append( theUseModuleClassLoaders ).append( "\n" );
        buf.append( "    theIsDeveloper:           " ).append( theIsDeveloper ).append( "\n" );
        buf.append( "    theIsDemo:                " ).append( theIsDemo ).append( "\n" );
        buf.append( "    theIsShowModuleRegistry:  " ).append( theIsShowModuleRegistry ).append( "\n" );
        buf.append( "    theRemainingArguments: {\n" );
        for( int i=0 ; i<theRemainingArguments.length ; ++i ) {
            buf.append( "        " ).append( theRemainingArguments[i] ).append( "\n" );
        }
        buf.append( "    }\n" );
        buf.append( "    theTokenConversionMap:" );
        if( theTokenConversionMap != null ) {
            buf.append( " {\n" );
            for( String key : theTokenConversionMap.keySet() ) {
                buf.append( "        " ).append( key ).append( " -> ").append( theTokenConversionMap.get( key )).append( "\n" );
            }
            buf.append( "    }\n" );
        } else {
            buf.append( " null\n" );
        }
        buf.append( "    thePlatform:              " ).append( thePlatform ).append( "\n" );
        buf.append( "    theInstallModuleDirs:     " );
        if( theInstallModuleDirs != null && !theInstallModuleDirs.isEmpty() ) {
            buf.append( " {\n" );
            for( File current : theInstallModuleDirs ) {
                buf.append( "        " ).append( current.getAbsolutePath() ).append( "\n" );
            }
            buf.append( "    }\n" );
        } else {
            buf.append( " null\n" );
        }
        buf.append( "    theUserDataDir:           " ).append( theUserDataDir ).append( "\n" );
        buf.append( "    theHomeObjectUrlName:     " ).append( theHomeObjectUrlName ).append( "\n" );
        buf.append( "    theRunClassName:          " ).append( theRunClassName ).append( "\n" );
        buf.append( "    theRunMethodName:         " ).append( theRunMethodName ).append( "\n" );
        buf.append( "}" );
        return buf.toString();
    }

    /**
     * The Module installation directories.
     */
    protected List<File> theInstallModuleDirs;

    /**
     * The user data directory (typically in the user's home directory).
     */
    protected File theUserDataDir;

    /**
     * The URL of the object that we want to open up first (if any).
     */
    protected String theHomeObjectUrlName;

    /**
     * The name of the run class in the root Module, if specified on the command-line.
     */
    protected String theRunClassName;

    /**
     * The name of the run method in the root Module, if specified on the command-line.
     */
    protected String theRunMethodName;

    /**
     * Indicates whether we want to show the splash screen or not.
     */
    protected boolean theShowSplash;

    /**
     * Indicates whether we should only print error messages to the console or
     * pop up dialogs.
     */
    protected boolean theIsErrorTextOnly;

    /**
     * Time, in milliseconds, to sleep prior to exiting the main method. This can be very
     * useful for profiling, for example.
     */
    protected long theSleepPeriodBeforeExit;
    
    /**
     * The about text for this version. FIXME, needs to be internationalized.
     */
    protected static final String theAboutText
            = "InfoGrid(TM)\n"
            + "Version: @PRODUCTID@\n\n"
            + "\u00A9 1998-2008 NetMesh Inc.\n"
            + "All rights reserved.\n"
            + "This product may include software developed by the\n"
            + "Apache Software Foundation (www.apache.org).\n";

    /**
      * A table of mapping platforms to user data directories.
      */
    protected static final HashMap<String,String> userDataDirTable;
    static {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put( MAC_OSX_PLATFORM, "Documents/InfoGrid/Data" );
        map.put( WINDOWS_PLATFORM, "Application Data\\InfoGrid\\Data" );
        map.put( OTHER_PLATFORM,   "InfoGrid/Data" );
        userDataDirTable = map;
    }

    /**
      * A table of mapping platforms to user-level installation directories.
      */
    protected static final HashMap<String,String> installDirTable;
    static {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put( MAC_OSX_PLATFORM, "Library/InfoGrid/Installation" );
        map.put( WINDOWS_PLATFORM, "Application Data\\InfoGrid\\Installation" );
        map.put( OTHER_PLATFORM,   "InfoGrid/Installation" );
        installDirTable = map;
    }

    /**
     * This String indicates the directory containing the Modules. Use this in resource files in connection with
     * this class's replaceVariables method.
     */
    public static final String INSTALL_MODULE_DIR_TOKEN = "@MODULEDIR@";

    /**
     * This String indicates the user data directory. Use this in resource files in connection with
     * this class's replaceVariables method.
     */
    public static final String USERDATA_DIR_TOKEN = "@USERDATADIR@";
}
