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

package org.infogrid.httpd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import org.infogrid.httpd.util.TraceableInputStream;
import org.infogrid.httpd.util.TraceableOutputStream;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;

/**
  * A Runnable that accepts socket connections on the ServerSocket.
  */
public class HttpAcceptor
    implements
        Runnable
{
    private static final Log log = Log.getLogInstance( HttpAcceptor.class ); // our own, private logger

    /**
      * Constructor.
      *
      * @param portNumber the port number at which we accept connections
      * @throws IOException thrown if the ServerSocket could not be created for this Thread
      */
    public HttpAcceptor(
            int portNumber )
        throws
            IOException
    {
        this( portNumber, DEFAULT_NUMBER_THREADS );
    }

    /**
      * Constructor.
      *
      * @param portNumber the port number at which we accept connections
      * @param numberThreads the number of Threads that may be spawned to respond. If 0,
      *                      all responses will be handled on this Thread itself
      * @throws IOException thrown if the ServerSocket could not be created for this Thread
      */
    public HttpAcceptor(
            int portNumber,
            int numberThreads )
        throws
            IOException
    {
        try {
            thePort         = portNumber;
            theServerSocket = new ServerSocket( portNumber );
            theThreads      = new Thread[ numberThreads ];

            theServerSocket.setSoTimeout( 1000 ); // so we can ever stop this again

        } catch( IOException ex ) {
            throw ex; // for easier debugging
        }
    }

    /**
     * Set an AccessLogger.
     *
     * @param newLogger the new logger, or null if none
     */
    public void setAccessLogger(
            HttpAccessLogger newLogger )
    {
        theLogger = newLogger;
    }

    /**
     * Obtain the AccessLogger.
     *
     * @return the logger, or null if none
     */
    public HttpAccessLogger getAccessLogger()
    {
        return theLogger;
    }

    /**
     * Set a ResponseFactory.
     *
     * @param factory the new ResponseFactory
     * @throws NullPointerException thrown if the provided ResponseFactory is null
     */
    public void setResponseFactory(
            HttpResponseFactory factory )
    {
        if( factory == null ) {
            throw new NullPointerException( "Cannot set a null ResponseFactory for an AcceptThread" );
        }

        theResponseFactory = factory;
    }

    /**
     * Obtain the HttpResponseFactory.
     *
     * @return the HttpResponseFactory
     */
    public HttpResponseFactory getResponseFactory()
    {
        return theResponseFactory;
    }

    /**
      * Run method of this Thread: wait for incoming connection, dispatch, do again.
      */
    public void run()
    {
        theIsActive = true;

        for( int i=0 ; i<theThreads.length ; ++i ) {
            theThreads[i] = new Thread( new Worker(), "AcceptThread / WorkerThread " + i );
            theThreads[i].start();
        }

        while( theIsActive ) {
            Socket newSocket = null;
            try {
                newSocket = theServerSocket.accept();

            } catch( SocketTimeoutException ex ) {
                // that's fine, do nothing, go right back
            } catch( SocketException ex ) {
                // probably too much load, wait a tiny bit
                try {
                    Thread.sleep( 10L );
                    System.gc();
                } catch( Throwable t ) {
                    ex.printStackTrace();
                }
            } catch( IOException ex ) {
                ex.printStackTrace();
            }
            
            if( theIsActive && newSocket != null ) {
                if( theThreads.length == 0 ) {
                    // no worker threads, we execute synchronously
                    try {
                        dispatch( newSocket );
                    } catch( Throwable ex ) {
                        ex.printStackTrace();
                    }
                } else {
                    synchronized( theWorkQueue ) {
                        theWorkQueue.addFirst( newSocket );
                        theWorkQueue.notify();
                    }
                }
            }
        }
        for( int i=0 ; i<theThreads.length ; ++i ) {
            theThreads[i].interrupt();
        }
        try {
            theServerSocket.close();
        } catch( IOException ex ) {
            ex.printStackTrace();
        }
        theServerSocket = null;
    }

    /**
     * Stop this Thread.
     */
    public void cancel()
    {
        theIsActive = false;

        synchronized( theWorkQueue ) {
            theWorkQueue.notifyAll();
        }
        try {
            Thread.sleep( 1000L );
        } catch( Exception ex ) {
            // noop
        }
    }

    /**
      * Dispatches an incoming connection. This does not currently do HTTP 1.1 keepalives.
      * This executes everything synchronously on whatever Thread invoked this method.
      *
      * @param newSocket the Socket with the incoming connection
      */
    protected void dispatch(
            Socket newSocket )
    {
        if( log.isInfoEnabled() ) {
            log.info( this + "Dispatching incoming request from " + newSocket + " - " + ( newSocket.isClosed() ? "closed" : "not closed" ));
        }
        OutputStream inputDebugStream;
        OutputStream outputDebugStream;

        if( log.isDebugEnabled() ) {
            inputDebugStream  = new ByteArrayOutputStream();
            outputDebugStream = new ByteArrayOutputStream();
        } else {
            inputDebugStream  = null;
            outputDebugStream = null;
        }

        boolean      success      = false;
        HttpResponse theResponse  = null;
        InputStream  theInStream  = null;
        OutputStream theOutStream = null;
        try {
            theInStream  = new TraceableInputStream(  new BufferedInputStream(  newSocket.getInputStream()  ), inputDebugStream  );
            theOutStream = new TraceableOutputStream( new BufferedOutputStream( newSocket.getOutputStream() ), outputDebugStream );
//            theInStream  = newSocket.getInputStream();
//            theOutStream = newSocket.getOutputStream();

            HttpRequest theRequest = HttpRequest.readHeader( ONLY_PROTOCOL, thePort, theInStream );

            HttpResponseFactory factory = theResponseFactory; // this trick prevents us having to synchronize
            if( factory != null ) {
                theResponse = factory.createResponse( theRequest );
            } else {
                theResponse = HttpErrorResponse.createUnconfigured( theRequest, HttpStatusCodes.NOT_IMPLEMENTED_CODE );
            }
            theResponse.writeTo( theOutStream );

            success = true;

        } catch( Exception ex ) {
            log.error( ex );

        } finally {

            try {
                if( theOutStream != null ) {
                    theOutStream.flush();
                }
            } catch( Exception ex ) {
                log.error( ex );
            }
            try {
                if( newSocket != null && ! newSocket.isClosed() ) {
                    newSocket.close();
                }
            } catch( Exception ex ) {
                log.error( ex );
            }


            if( theLogger != null ) {
                if( success ) {
                    theLogger.logComplete( theResponse );
                } else {
                    theLogger.logIncomplete( theResponse );
                }
            }

            if( inputDebugStream != null ) {
                log.debug( this + " Read input:----(begin)----\n" + inputDebugStream.toString() + "\n---- (end) ----" );
            }
            if( outputDebugStream != null ) {
                log.debug( this + "Read output:----(begin)----\n" + outputDebugStream.toString() + "\n---- (end) ----" );
            }

            if( log.isInfoEnabled() ) {
                log.info( this + "Done dispatching incoming request from " + newSocket + " - " + ( newSocket.isClosed() ? "closed" : "not closed" ));
            }
        }
    }

    /**
     * The currently only protocol that we support.
     */
    protected static final String ONLY_PROTOCOL = "http";

    /**
     * The port whose incoming requests we accept.
     */
    protected int thePort;

    /**
      * The ServerSocket from which we accept and dispatch connections.
      */
    protected ServerSocket theServerSocket;

    /**
      * The factory for our Responses.
      */
    protected HttpResponseFactory theResponseFactory;

    /**
     * The worker Threads that are spawned.
     */
    protected Thread [] theThreads;

    /**
     * The work queue.
     */
    private final LinkedList<Socket> theWorkQueue = new LinkedList<Socket>();

    /**
     * The logger, if any.
     */
    protected HttpAccessLogger theLogger;

    /**
     * This is true for as long as this Acceptor is supposed to run.
     */
    private volatile boolean theIsActive = false;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( HttpAcceptor.class );

    /**
     * The default number of working Threads started if no other value is provided in the constructor.
     */
    public static final int DEFAULT_NUMBER_THREADS = theResourceHelper.getResourceIntegerOrDefault( "DefaultNumberThreads", 0 );

    /**
     * The Worker Runnable.
     */
    private class Worker
        implements
            Runnable
    {
        /**
         * Runnable's run method.
         */
        public void run()
        {
            while( theIsActive ) {
                Socket theSocket = null;
                synchronized( theWorkQueue ) {
                    while( theSocket == null ) {
                        if( theWorkQueue.isEmpty() ) {
                            try {
                                theWorkQueue.wait();
                            } catch( InterruptedException ex ) {
                                if( !theIsActive ) {
                                    return;
                                }
                            }
                        } else {
                            theSocket = theWorkQueue.removeLast();
                        }
                    }
                }
                dispatch( theSocket );
            }
        }
    }
}

