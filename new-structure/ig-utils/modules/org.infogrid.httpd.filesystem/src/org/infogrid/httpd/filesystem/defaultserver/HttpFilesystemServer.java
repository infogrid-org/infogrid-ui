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

package org.infogrid.httpd.filesystem.defaultserver;

import org.infogrid.httpd.DefaultHttpErrorHandler;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.httpd.filesystem.FilesystemHttpResponseFactory;
import org.infogrid.httpd.server.HttpServer;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
  * This is a very simple HTTPD server serving files from the file system.
  */
public class HttpFilesystemServer
    extends
        HttpServer
{
    /**
     * Constructor.
     *
     * @param documentRoot the document root directory
     * @throws IOException thrown if the documentRoot does not exist or isn't a directory
     */
    public HttpFilesystemServer(
            File documentRoot )
        throws
            IOException
    {
        this( documentRoot, DEFAULT_ACCEPT_PORT );
    }

    /**
     * Constructor.
     *
     * @param documentRoot the document root directory
     * @param acceptPort the port at which we accept incoming requests
     * @throws IOException thrown if the documentRoot does not exist or isn't a directory
     */
    public HttpFilesystemServer(
            File documentRoot,
            int  acceptPort )
        throws
            IOException
    {
        super( acceptPort );

        if( !documentRoot.exists() ) {
            throw new IOException( "Document root does not exist" );
        }
        if( !documentRoot.isDirectory() ) {
            throw new IOException( "Document root is not a directory" );
        }

        theDocumentRoot = documentRoot;

        HttpResponseFactory factory = new FilesystemHttpResponseFactory(
                documentRoot,
                new DefaultHttpErrorHandler(),
                null ); // no Default AccessManager -- everything is open

        theAcceptor.setResponseFactory( factory );
    }

    /**
     * This allows the Module framework to configure this Module prior to activating it.
     *
     * @param parameters the parameters for this Module
     * @param whereParametersSpecifiedMap maps which Modules specified each parameter
     * @param thisModule this Module
     * @throws ModuleConfigurationException thrown if the provided parameters are insufficient
     * @throws IOException thrown if the document root does not exist or is not a directory
     */
    public static void configure(
            Map<String,Object> parameters,
            Map<String,Module> whereParametersSpecifiedMap,
            Module             thisModule )
        throws
            ModuleConfigurationException,
            IOException
    {
        File    documentRoot = (File)    parameters.get( DOCUMENT_ROOT_PARAMETER_NAME );
        Integer portNumber   = (Integer) parameters.get( ACCEPT_PORT_PARAMETER_NAME );

        if( documentRoot == null ) {
            throw new ModuleConfigurationException(
                    thisModule.getModuleAdvertisement(),
                    "No " + DOCUMENT_ROOT_PARAMETER_NAME + " parameter given" );
        }

        if( portNumber != null ) {
            theSingleton = new HttpFilesystemServer( documentRoot, portNumber.intValue() );
        } else {
            theSingleton = new HttpFilesystemServer( documentRoot );
        }
    }

    /**
     * This allows the Module framework to activate this Module and thus start
     * running the HttpFilesystemServer.
     *
     * @param dependentModules the Modules that this Module depends on, if any
     * @param dependentContextObjects the context objects of the Modules that this Module depends on, if any, in same sequence as dependentModules
     * @param arguments arguments for activation
     * @param thisModule this Module
     * @return a context object that is Module-specific, or null if none
     * @throws IOException thrown if the server could not be started
     */
    public static Object activate(
            Module[] dependentModules,
            Object[] dependentContextObjects,
            Object[] arguments,
            Module   thisModule )
        throws
            IOException
    {
        theSingleton.start();

        return null;
    }

    /**
     * This allows the Module framework to deactivate this Module and thus stop
     * a running HttpFilesystemServer.
     *
     * @param dependentModules the Modules that this Module depends on, if any
     * @param thisModule this Module
     */
    public static void deactivate(
            Module[] dependentModules,
            Module   thisModule )
    {
        theSingleton.stop();
    }

    /**
     * Document root of this HttpFilesystemServer.
     */
    protected File theDocumentRoot;

    /**
     * The singleton instance of the web server. This is allocated as soon as this Module has been
     * configured.
     */
    protected static HttpFilesystemServer theSingleton = null;

    /**
     * Name of the Module configuration parameter that indicates the document root of the web server.
     * The type of this parameter must be java.io.File.
     */
    public static final String DOCUMENT_ROOT_PARAMETER_NAME = "org.infogrid.httpd.filesystem.defaultserver.DocumentRoot";

    /**
     * Name of the Module configuration parameter that indicates the port at which the web server
     * shall be run. If not given, a DEFAULT_ACCEPT_PORT will be used. The type of this parameter
     * must be java.lang.Integer.
     */
    public static final String ACCEPT_PORT_PARAMETER_NAME = "org.infogrid.httpd.filesystem.defaultserver.AcceptPort";
}
