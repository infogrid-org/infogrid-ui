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

package org.infogrid.httpd.server;

import org.infogrid.httpd.HttpAcceptor;
import org.infogrid.httpd.HttpAccessLogger;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;

import java.io.IOException;

/**
  * This is a very simple HTTP server implementation.
  */
public class HttpServer
{
    private static final Log log = Log.getLogInstance( HttpServer.class ); // our own, private logger

    /**
     * Constructor for subclasses only.
     *
     * @param acceptPort the port at which we accept incoming requests
     * @throws IOException if the ServerSocket could not be created
     */
    public HttpServer(
            int acceptPort )
        throws
            IOException
    {
        theAcceptPort = acceptPort;

        theAcceptor = new HttpAcceptor( theAcceptPort );
    }

    /**
     * Constructor for subclasses only.
     *
     * @param acceptPort the port at which we accept incoming requests
     * @param numberThreads the number of Threads to create
     * @throws IOException if the ServerSocket could not be created
     */
    public HttpServer(
            int acceptPort,
            int numberThreads )
        throws
            IOException
    {
        theAcceptPort = acceptPort;

        theAcceptor = new HttpAcceptor( theAcceptPort, numberThreads );
    }

    /**
     * Start the server.
     *
     * @throws IOException the ServerSocket could not be created
     */
    public synchronized void start()
        throws
            IOException
    {
        if( theAcceptThread != null ) {
            throw new IllegalStateException( "HTTP server is started already, stop it first." );
        }

        log.info( "Starting " + getClass().getName() + " ..." );

        theAcceptThread = new Thread( theAcceptor );
        theAcceptThread.start();
    }

    /**
     * Set an AccessLogger.
     *
     * @param logger the new AccessLogger
     */
    public void setAccessLogger(
            HttpAccessLogger logger )
    {
        theAcceptor.setAccessLogger( logger );
    }

    /**
     * Obtain the AccessLogger.
     *
     * @return the AccessLogger
     */
    public HttpAccessLogger getAccessLogger()
    {
        return theAcceptor.getAccessLogger();
    }

    /**
     * Set a ResponseFactory.
     *
     * @param factory the new ResponseFactory
     */
    public void setResponseFactory(
            HttpResponseFactory factory )
    {
        theAcceptor.setResponseFactory( factory );
    }

    /**
     * Obtain the ResponseFactory.
     *
     * @return the ResponseFactory
     */
    public HttpResponseFactory getResponseFactory()
    {
        return theAcceptor.getResponseFactory();
    }

    /**
     * Stop the server.
     */
    public synchronized void stop()
    {
        if( theAcceptThread != null ) {
            log.info( "Stopping " + getClass().getName() + " ..." );

            theAcceptor.cancel();
            theAcceptThread.interrupt();

            theAcceptThread = null;

        } else {
            throw new IllegalStateException( "HTTP server is stopped already, cannot stop it again." );
        }
    }

    /**
     * Obtain the port at which this Server accepts incoming requests.
     *
     * @return the port number
     */
    public int getAcceptPort()
    {
        return theAcceptPort;
    }

    /**
     * The Thread that handles listening and dispatching
     */
    protected Thread theAcceptThread;

    /**
     * The Runnable run by theAcceptThread;
     */
    protected HttpAcceptor theAcceptor;

    /**
     * The actual port at which we listen.
     */
    protected int theAcceptPort;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( HttpServer.class );

    /**
      * The default port at which we listen if no Module configuration parameter has been given.
      */
    protected static final int DEFAULT_ACCEPT_PORT = theResourceHelper.getResourceIntegerOrDefault(
            "DefaultAcceptPort",
            8081 );
}
