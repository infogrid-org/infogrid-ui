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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleMeta;
import org.infogrid.module.ModuleNotFoundException;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.ModuleRequirement;
import org.infogrid.module.ModuleResolutionCandidateNotUniqueException;
import org.infogrid.module.ModuleResolutionException;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.Base64;
import org.infogrid.util.StreamUtils;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;

/**
 * The place from where to interact with a Tomcat instance. It contains methods
 * that allow us to operate on that Tomcat instance without all the hassle.
 */
public abstract class TomcatProxy
{
    private static final Log log = Log.getLogInstance( TomcatProxy.class ); // our own, private logger

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
    protected TomcatProxy(
            URL    topServerUrl,
            URL    managerUrl,
            String applicationPath,
            String adminUser,
            String adminPassword,
            int    timeout )
    {
        theTopServerUrl    = topServerUrl;
        theManagerUrl      = managerUrl;
        theApplicationPath = applicationPath;
        theAdminUser       = adminUser;
        theAdminPassword   = adminPassword;
        theTimeout         = timeout;
    }
    
    /**
     * Deploy a module to Tomcat.
     * 
     * @param moduleReq captures the informatation needed to identity the Module to deploy to Tomcat.
     * @return the URL to which the Module was deployed
     * @throws ModuleNotFoundException the to-be-deployed Module could not be found
     * @throws ModuleResolutionException the to-be-deployed Module could not be resolved
     * @throws ModuleResolutionCandidateNotUniqueException thrown if the ModuleRequirement could not be met unambiguously
     * @throws IOException a file could not be read, or the HTTP PUT failed
     */
    public URL deployModule(
            ModuleRequirement moduleReq )
        throws
            ModuleNotFoundException,
            ModuleResolutionException,
            ModuleResolutionCandidateNotUniqueException,
            IOException
    {
        String path = theApplicationPath;
        if( path == null ) {
            path = "/" + moduleReq.getRequiredModuleName();
        }
        return deployModule( path, moduleReq );
    }

    /**
     * Deploy a module to Tomcat.
     * 
     * @param path at which to deploy the Module
     * @param moduleReq captures the informatation needed to identity the Module to deploy to Tomcat.
     * @return the URL to which the Module was deployed
     * @throws ModuleNotFoundException the to-be-deployed Module could not be found
     * @throws ModuleResolutionException the to-be-deployed Module could not be resolved
     * @throws ModuleResolutionCandidateNotUniqueException thrown if the ModuleRequirement could not be met unambiguously
     * @throws IOException a file could not be read, or the HTTP PUT failed
     */
    public URL deployModule(
            String            path,
            ModuleRequirement moduleReq )
        throws
            ModuleNotFoundException,
            ModuleResolutionException,
            ModuleResolutionCandidateNotUniqueException,
            IOException
    {
        if( path == null ) {
            throw new NullPointerException( "Must not provide null path" );
        }
        if( theManagerUrl != null ) {
            Module         thisModule = Init.getModule();
            ModuleRegistry registry   = thisModule.getModuleRegistry();
            ModuleMeta     candidate  = registry.determineSingleResolutionCandidate( moduleReq );
            Module         toDeploy   = registry.resolve( candidate );

            File jar = candidate.getProvidesJar();

            byte [] content = StreamUtils.slurp( jar );

            StringBuilder buf = new StringBuilder();
            buf.append( theManagerUrl.toExternalForm() );
            buf.append( "/deploy?update=true" );
            buf.append( "&path=" ).append( HTTP.encodeToValidUrlArgument( path ));

            URL endpoint = new URL( buf.toString() );

            doHttpPut( endpoint, "application/octet-stream", content );
        } else {
            log.warn( "No Tomcat Manager URL defined: skip automatic deployment" ); 
        }
        URL ret = new URL( theTopServerUrl, path );
        
        return ret;
    }

    /**
     * Perform an HTTP put.
     * 
     * @param url the URL to which to put the payload
     * @param mime the content type of the payload
     * @param payload the payload
     * @throws IOException thrown if an I/O error occurred
     */
    protected void doHttpPut(
            URL     url,
            String  mime,
            byte [] payload )
        throws
            IOException
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput( true );
        conn.setRequestMethod("PUT");
        conn.setRequestProperty( "Content-type", mime );
        // conn.setRequestProperty( "Content-length", String.valueOf( payload.length ));
        conn.setAllowUserInteraction( false);
        conn.setDoInput( true );
        conn.setUseCaches( false );
        conn.setRequestProperty( "User-Agent", "Catalina-Ant-Task/1.0" );

        if( theTimeout >= 0L ) {
            conn.setConnectTimeout( theTimeout );
        }
        if( theAdminUser != null || theAdminPassword != null ) {
            String encodedCredentials = Base64.base64encode(
                    (   ( theAdminUser != null ? theAdminUser : "" )
                      + ":"
                      + ( theAdminPassword != null ? theAdminPassword : "" )).getBytes( "UTF-8" ) );
            conn.setRequestProperty( "Authorization", "Basic " + encodedCredentials );
        }

        InputStream  ips = null;
        OutputStream ops = null;
        try {
            ops = conn.getOutputStream();
            ops.write( payload );
            ops.flush();

            ips = conn.getInputStream();
            int    responseCode    = conn.getResponseCode();
            String responseContent = new String( StreamUtils.slurp( ips ));
            
            if( responseCode != 200 || responseContent.startsWith( "FAIL" )) {
                // apparently Tomcat does not respond with proper HTTP error codes
                throw new IOException( "Could not deploy: " + responseCode + ", " + new String( responseContent ));
            }
        } catch( ConnectException ex ) {
            log.error( "Failed to connect to URL " + url.toExternalForm() );
            throw ex;

        } finally {
            if( ops != null ) {
                ops.close();
            }
            conn.disconnect();
        }
    }

    /**
     * The top URL of this Tomcat instance.
     */
    protected URL theTopServerUrl;
    
    /**
     * The URL of the manager application of this Tomcat instance.
     */
    protected URL theManagerUrl;

    /**
     * The path for the to-be-tested application.
     */
    protected String theApplicationPath;

    /**
     * The administrator user name.
     */
    protected String theAdminUser;
    
    /**
     * The administrator password.
     */
    protected String theAdminPassword;
    
    /**
     * The timeout for the connection to Tomcat, in milliseconds. Negative number means "don't set".
     */
    protected int theTimeout;
}
