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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.testharness.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * A TomcatProxy that is configured through a properties file.
 */
public class TomcatProxyConfiguredByProperties
        extends
            TomcatProxy
{
    /**
     * Factory method.
     *
     * @param propertiesFile the File containing the properties
     * @return the created TomcatProxyConfiguredByProperties
     * @throws IOException thrown if the properties could not be read
     */
    public static TomcatProxyConfiguredByProperties create(
            File propertiesFile )
        throws
            IOException
    {
        FileInputStream propertiesStream = null;
        try {
            propertiesStream = new FileInputStream( propertiesFile );
        
            TomcatProxyConfiguredByProperties ret = create( propertiesStream );
            return ret;
            
        } finally {
            if( propertiesStream != null ) {
                propertiesStream.close();            
            }
        }
     }
        
    /**
     * Factory method.
     *
     * @param propertiesStream the InputStream containing the properties
     * @return the created TomcatProxyConfiguredByProperties
     * @throws IOException thrown if the properties could not be read
     */
    public static TomcatProxyConfiguredByProperties create(
            InputStream propertiesStream )
        throws
            IOException
    {
        Properties props = new Properties();
        props.load( propertiesStream );
        
        String topServer = props.getProperty( SERVER_URL_PROPERTY_NAME );  // mandatory
        String manager   = props.getProperty( MANAGER_URL_PROPERTY_NAME ); // optional

        URL    topServerUrl    = new URL( topServer );
        URL    managerUrl      = manager != null ? new URL( manager ) : null;
        String applicationPath = props.getProperty( APPLICATION_PATH_PROPERTY_NAME, null );
        String adminUser       = props.getProperty( ADMIN_USER_PROPERTY_NAME, null );
        String adminPassword   = props.getProperty( ADMIN_PASSWORD_PROPERTY_NAME, null );
        String timeout         = props.getProperty( TIMEOUT_PROPERTY_NAME, "-1" );
        
        TomcatProxyConfiguredByProperties ret = new TomcatProxyConfiguredByProperties(
                topServerUrl,
                managerUrl,
                applicationPath,
                adminUser,
                adminPassword,
                Integer.parseInt( timeout ));
        return ret;
    }

    /**
     * Constructor for subclasses only.
     * 
     * @param topServerUrl the URL for the server relative to which applications are deployed
     * @param managerUrl the URL for the Tomcat manager application
     * @param applicationPath the path for the to-be-tested application
     * @param adminUser the admin user name
     * @param adminPassword the administrator password
     * @param timeout the time to spend attempting to connect to Tomcat. Negative means use default.
     */
    protected TomcatProxyConfiguredByProperties(
            URL    topServerUrl,
            URL    managerUrl,
            String applicationPath,
            String adminUser,
            String adminPassword,
            int    timeout )
    {
        super( topServerUrl, managerUrl, applicationPath, adminUser, adminPassword, timeout );
    }

    /**
     * Name of the property containing Tomcat's top-level, server URL.
     */
    public static final String SERVER_URL_PROPERTY_NAME = "tomcat.server.url";

    /**
     * Name of the property containing Tomcat's manager application URL.
     */
    public static final String MANAGER_URL_PROPERTY_NAME = "tomcat.manager.url";
    
    /**
     * Name of the property containing the path for the to-be-tested application.
     */
    public static final String APPLICATION_PATH_PROPERTY_NAME = "webapp.path";
    
    /**
     * Name of the property containing the name of Tomcat's admin user.
     */
    public static final String ADMIN_USER_PROPERTY_NAME = "tomcat.manager.user";
    
    /**
     * Name of the property containing the password of Tomcat's admin user.
     */
    public static final String ADMIN_PASSWORD_PROPERTY_NAME = "tomcat.manager.password";
    
    /**
     * Name of the property containing the timeout for connection attempts.
     */
    public static final String TIMEOUT_PROPERTY_NAME = "tomcat.manager.timeout";
}
