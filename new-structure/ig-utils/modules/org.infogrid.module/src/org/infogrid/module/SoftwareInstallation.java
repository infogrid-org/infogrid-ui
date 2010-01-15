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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A software installation, i.e. a number of Modules and activation meta-data.
 * Subclasses implement this differently, depending on the run-time environment
 * (e.g. JEE vs. JME).
 */
public abstract class SoftwareInstallation
{
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
     * @param useModuleClassLoaders if true, use ModuleClassLoaders to load the Modules
     * @param allowDefaultClassPathForRootModule if true, enable the default ClassLoader for the root Module
     * @param isDeveloper if true, run in developer mode
     * @param isDemo if true, run in demo mode
     * @param isShowModuleRegistry if true, print the content of the ModuleRegistry to the terminal
     * @param moduleDebugStream a stream for Module-related debug information (may be null)
     * @param remainingArguments the arguments on the command line not used by SoftwareInstallation itself
     * @throws SoftwareInstallationException if this software installation is incorrect, inconsistent or incomplete
     */
    protected SoftwareInstallation(
            String      platform,
            String      rootModuleName,
            String      activationClassName,
            String      activationMethodName,
            String      log4jconfigUrlName,
            String      productName,
            String      productId,
            boolean     useModuleClassLoaders,
            boolean     allowDefaultClassPathForRootModule,
            boolean     isDeveloper,
            boolean     isDemo,
            boolean     isShowModuleRegistry,
            PrintStream moduleDebugStream,
            String []   remainingArguments )
        throws
            SoftwareInstallationException
    {
        thePlatform = platform;

        theRootModuleRequirement              = ModuleRequirement.create1( rootModuleName );
        theActivationClassName                = activationClassName;
        theActivationMethodName               = activationMethodName;
        theLog4jconfigUrlName                 = log4jconfigUrlName;
        theProductName                        = productName;
        theProductId                          = productId;
        theUseModuleClassLoaders              = useModuleClassLoaders;
        theAllowDefaultClassPathForRootModule = allowDefaultClassPathForRootModule;

        theIsDeveloper          = isDeveloper;
        theIsDemo               = isDemo;
        theIsShowModuleRegistry = isShowModuleRegistry;

        if( theRootModuleRequirement == null ) {
            throw new SoftwareInstallationException( "Root Module cannot be determined" );
        }
        if( moduleDebugStream != null ) {
            ModuleErrorHandler.setDebugStream( moduleDebugStream );
        }

        theRemainingArguments = remainingArguments;

        synchronized( SoftwareInstallation.class ) {
            if( theSingleton != null ) {
                throw new IllegalStateException( "Have singleton already: " + theSingleton );
            }
            theSingleton = this;
        }
    }

    /**
     * Obtain the singleton instance of this class.
     *
     * @return the singleton instance of this class, if any
     */
    public static SoftwareInstallation getSoftwareInstallation()
    {
        return theSingleton;
    }

    /**
     * Obtain one of the enumerated values in RECOGNIZED_PLATFORMS indicating what platform we
     * are running on.
     *
     * @return the platform we are running on
     */
    public final String getPlatform()
    {
        return thePlatform;
    }

    /**
     * Obtain the properties for the log4j logger.
     *
     * @param relativeTo the ClassLoader to use when attempting the load the property file
     * @return the log4j properties
     * @throws IOException could not read the properties
     */
    public Properties getLog4jProperties(
            ClassLoader relativeTo )
        throws
            IOException
    {
        Properties ret = new Properties();

        URL log4jconfigUrl;
        try {
            log4jconfigUrl = new URL( theLog4jconfigUrlName );
        } catch( MalformedURLException ex ) {
            log4jconfigUrl = relativeTo.getResource( theLog4jconfigUrlName );
        }
        if( log4jconfigUrl != null ) {
            ret.load( new BufferedInputStream( log4jconfigUrl.openStream() ));
        }

        Enumeration theEnum = ret.propertyNames();
        while( theEnum.hasMoreElements() ) {
            String currentKey    = (String) theEnum.nextElement();
            String currentValue  = ret.getProperty( currentKey );

            String modifiedValue = replaceVariables( currentValue );

            if( (Object) currentValue != (Object) modifiedValue ) { // typecast to avoid String == comparison warning
                ret.setProperty( currentKey, modifiedValue );
            }
        }
        return ret;
    }

    /**
     * Determine whether we are running this in developer mode.
     *
     * @return if true, we are running this in developer mode
     */
    public final boolean isDeveloperMode()
    {
        return theIsDeveloper;
    }

    /**
     * Determine whether we are running this in demo mode.
     *
     * @return if true, we are running this in demo mode
     */
    public final boolean isDemo()
    {
        return theIsDemo;
    }

    /**
     * Determine whether we are supposed to dump the ModuleRegistry to the terminal.
     *
     * @return if true, dump to the terminal.
     */
    public final boolean isShowModuleRegistry()
    {
        return theIsShowModuleRegistry;
    }

    /**
     * Obtain the ModuleRequirement for the root Module as specified on the command-line.
     *
     * @return the ModuleRequirement for the root Module as specified on the command-line
     */
    public final ModuleRequirement getRootModuleRequirement()
    {
        return theRootModuleRequirement;
    }

    /**
     * Obtain the activation class specified in the command-line, if any.
     *
     * @return name of the class
     */
    public final String getOverriddenActivationClassName()
    {
        return theActivationClassName;
    }

    /**
     * Obtain the activation method specified in the command-line, if any.
     *
     * @return name of the method (without class name)
     */
    public final String getOverriddenActivationMethodName()
    {
        return theActivationMethodName;
    }

    /**
     * Obtain the deactivation method specified in the command-line, if any.
     *
     * @return name of the method (without class name)
     */
    public final String getOverriddenDeactivationMethodName()
    {
        return theDeactivationMethodName;
    }

    /**
     * Obtain the product name.
     *
     * @return the product name
     */
    public final String getProductName()
    {
        return theProductName;
    }

    /**
     * Obtain the product id.
     *
     * @return the product id
     */
    public final String getProductId()
    {
        return theProductId;
    }

    /**
     * Determine whether we are only supposed to print, to the terminal, error messages in the BootLoader.
     * This returns true by default.
     *
     * @return if true, only print to the terminal
     */
    public boolean isErrorTextOnly()
    {
        return true;
    }

    /**
     * Obtain an "About" text that describes this piece of software.
     *
     * @return the "about" text
     */
    public abstract String getAboutText();

    /**
     * Obtain the arguments passed when running the root Module.
     *
     * @return the arguments passed when running the root Module
     */
    public final String [] getRemainingArguments()
    {
        return theRemainingArguments;
    }

    /**
     * This helper method replaces installation-specific parameters in the passed-in string.
     * For example, it replaces @VERSIONID@ with the value of the version id.
     *
     * @param raw the string with variables still in it
     * @return the string with variables replaced
     */
    public String replaceVariables(
            String raw )
    {
        String ret = raw;

        if( theTokenConversionMap == null ) {
            theTokenConversionMap = getTokenConversionMap();
        }
        Iterator<String> keyIter = theTokenConversionMap.keySet().iterator();
        while( keyIter.hasNext() ) {
            String key   = keyIter.next();
            String value = theTokenConversionMap.get( key );

            int index;
            while( ( index = raw.indexOf( key )) >= 0 ) {
                ret = ret.substring( 0, index ) + value + ret.substring( index + key.length() );
            }
        }
        return ret;
    }

    /**
     * Obtain the token conversion map from our subclass. This is invoked only once
     * and then buffered by the caller.
     *
     * @return a Map of tokens in the config files that must be replaced before using their data
     */
    protected abstract Map<String,String> getTokenConversionMap();

    /**
     * Determine our host name as well as we can. Otherwise return null.
     *
     * @return the host name
     */
    public final synchronized String getHostName()
    {
        if( theHostName == null ) {
            try {
                // we first try to get the numeric address. Then we try to turn
                // it into a DNS name, but we'll use that only if we can actually
                // find the IP address again from the DNS name

                InetAddress localHost = InetAddress.getLocalHost();
                theHostName = localHost.getHostAddress(); // best guess right now

                String dnsName = localHost.getHostName();
                // if we get localhost, or we get a too-short name we just use leave it at the numeric address
                if( ! "localhost".equals( dnsName ) && dnsName.indexOf( '.' ) > 0 ) {
                    // this doesn't help us

                    InetAddress [] reverse = InetAddress.getAllByName( theHostName );
                    for( int i=0 ; i<reverse.length ; ++i ) {
                        if( localHost.equals( reverse[i] )) {
                            theHostName = dnsName;
                            break;
                        }
                    }
                }

            } catch( UnknownHostException ex ) {
                return theHostName;
            }
        }
        return theHostName;
    }

    /**
     * Determine whether or not we use ModuleClassLoaders to load code from individual Modules
     * using ModuleClassLoaders. We'd like to use ModuleClassLoaders as often as we can, but
     * two known conditions prevent that: 1) running under J2EE, where it seems to be simply
     * impossible to "insert" a ModuleClassLoader into the parent hierarchy of a ClassLoader
     * loading JSPs, and 2) running under JNLP.
     *
     * @return if true, we want to use ModuleClassLoaders
     */
    public final boolean useModuleClassLoaders()
    {
        return theUseModuleClassLoaders;
    }

    /**
     * Determine whether or not we also consult the default Java classpath for the root Module.
     * If this is true, it helps with some IDEs (NetBeans) that don't fully rebuild the current module
     * when you select Run.
     *
     * @return true if we consult the default classpath as well
     */
    public final boolean allowDefaultClassPathForRootModule()
    {
        return theAllowDefaultClassPathForRootModule;
    }

    /**
     * Create a temp file at a suitable place. This can be used by any Module.
     *
     * @param suffix suffix to use
     * @return the created temp file
     * @throws IOException if the temp file could not be created
     */
    public final File createTempFile(
            String suffix )
        throws
            IOException
    {
        return File.createTempFile( "infogrid-", suffix ); // FIXME this sounds a little too trivial
    }

    /**
     * Determine the platform on which we are running.
     *
     * @return the platform on which we are running
     */
    protected static String determinePlatform()
    {
        String ret = null;

        String osName  = System.getProperty( "os.name" );
        String lowerOs = osName.toLowerCase();

        // we check for N-1 because the Nth is the "other" category
        for( int i=0 ; i<RECOGNIZED_PLATFORMS.length-1 ; ++i ) {
            if( lowerOs.indexOf( RECOGNIZED_PLATFORMS[i] ) >= 0 ) {
                ret = RECOGNIZED_PLATFORMS[i];
                break;
            }
        }
        if( ret == null ) {
            ret = OTHER_PLATFORM;
        }
        return ret;
    }

    /**
     * Start this platform's browser. By default, this throws an Exception.
     *
     * @param u the URL we want to open in the browser
     * @throws IOException if we can't start the browser
     */
    public void openInBrowser(
            URL u )
        throws
            IOException
    {
        throw new IOException( "Browser not available in this configuration" );
    }

    /**
     * The platform -- one of the values in RECOGNIZED_PLATFORMS.
     */
    protected String thePlatform;

    /**
     * The path (fully-qualified URL or local to the class loader) of the log4j config file.
     */
    private String theLog4jconfigUrlName = "org/infogrid/module/log4jconfig.properties";

    /**
     * The ModuleRequirement for our root Module.
     */
    protected ModuleRequirement theRootModuleRequirement;

    /**
     * The name of the activation class in the root Module, if specified on the command-line.
     */
    protected String theActivationClassName;

    /**
     * the name of the activation method in the root Module, if specified on the command-line.
     */
    protected String theActivationMethodName;

    /**
     * the name of the deactivation method in the root Module, if specified on the command-line.
     */
    protected String theDeactivationMethodName;

    /**
     * Our host name, once we have determined it.
     */
    protected String theHostName;

    /**
     * The product name.
     */
    protected String theProductName;

    /**
     * The product id for this version.
     */
    protected String theProductId;

    /**
     * Do we want to use ModuleClassLoaders.
     */
    protected boolean theUseModuleClassLoaders;

    /**
     * Do we want to consult the standard Java classpath for the root Module.
     */
    protected boolean theAllowDefaultClassPathForRootModule;

    /**
     * Indicates whether we are running in developer mode.
     */
    protected boolean theIsDeveloper;

    /**
     * Indicates whether we are running in demo mode.
     */
    protected boolean theIsDemo;

    /**
     * Indicates whether we are dumping the content of the ModuleRegistry to the terminal.
     */
    protected boolean theIsShowModuleRegistry;

    /**
     * The arguments not used by SoftwareInstallation itself.
     */
    protected String [] theRemainingArguments;

    /**
     * The token conversion map.
     */
    protected Map<String,String> theTokenConversionMap;

    /**
     * The singleton instance of this class.
     */
    protected static SoftwareInstallation theSingleton;

    /**
     * The default location of the log4j configuration file.
     */
    public static final String DEFAULT_LOG4J_CONFIG_FILE = "org/infogrid/module/log4jconfig.properties";;

    /**
     * This String indicates the product id.
     */
    public static final String PRODUCTID_TOKEN = "@PRODUCTID@";

    /**
     * Use this text instead of a product id if it cannot be determined.
     */
    protected final static String UNKNOWN_PRODUCT_ID = "<unknown product id>";

    /**
     * Identifies the Mac OSX platform.
     */
    public static final String MAC_OSX_PLATFORM = "mac os x";

    /**
     * Identifies the Windows platform.
     */
    public static final String WINDOWS_PLATFORM = "windows";

    /**
     * Identifies any other platform.
     */
    public static final String OTHER_PLATFORM   = "other";

    /**
     * The list of platforms that we distinguish currently.
     */
    public static final String [] RECOGNIZED_PLATFORMS = {
            MAC_OSX_PLATFORM,
            WINDOWS_PLATFORM,
            OTHER_PLATFORM
    };

    /**
     * Name of the system property that may contain the product id.
     */
    public static final String PRODUCT_ID_PROPERTY = "product.id";

    /**
     * Name of the system property that may contain the product name.
     */
    public static final String PRODUCT_NAME_PROPERTY = "product.name";
}
