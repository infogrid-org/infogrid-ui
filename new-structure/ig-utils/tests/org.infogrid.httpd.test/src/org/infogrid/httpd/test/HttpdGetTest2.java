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

package org.infogrid.httpd.test;

import org.infogrid.util.http.HTTP;

/**
 * Tests multithreaded HTTP GETs.
 */
public class HttpdGetTest2
        extends
            AbstractHttpdTest
{
    /**
     * Run the test.
     *
     * @throws Exception this code may throw any Exception
     */
    public void run()
            throws
                Exception
    {
        Thread [] t = new Thread[ THREADS ];
        for( int i=0 ; i<t.length ; ++i ) {
            t[i] = new Thread( new MyRunnable( i ));
            t[i].start();
        }

        while( true ) {
            Thread.sleep( 1000L );
            boolean cont = false;
            for( int i=0 ; i<t.length ; ++i ) {
                if( t[i].isAlive() ) {
                    cont = true;
                    break;
                }
            }
            if( !cont ) {
                break;
            }
        }
    }

    /**
      * Main program.
      *
      * @param args command-line arguments
      */
    public static void main(
             String [] args )
    {
        HttpdGetTest2 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }
            test = new HttpdGetTest2( args );
            test.run();

        } catch( Throwable ex ) {
            log.error( ex );
            ++errorCount;
        }
        if( test != null ) {
            test.cleanup();
        }

        if( errorCount == 0 ) {
            log.info( "PASS" );
        } else {
            log.error( "FAIL (" + errorCount + " errors)" );
        }

        System.exit( errorCount );
    }

    /**
     * Setup.
     *
     * @param args not used
     * @throws Exception any kind of exception
     */
    public HttpdGetTest2(
            String [] args )
        throws
            Exception
    {
        super( args );
    }

    /**
     * The number of concurrent threads.
     */
    protected static final int THREADS = 20;

    /**
     * The Runnable for the Threads.
     */
    class MyRunnable
        implements
            Runnable
    {
        public MyRunnable(
                int threadIndex )
        {
            theThreadIndex = threadIndex;
        }

        public void run()
        {
            String url = "http://localhost:" + SERVER_PORT + "/";

            for( int i=0 ; i<100 ; ++i ) {
                log.info( "Performing test " + theThreadIndex + "/" + i );

                try {
                    HTTP.Response r = HTTP.http_get( url );

                    if( r != null ) {
                        checkEquals( r.getResponseCode(), "200",        "Wrong response code" );
                        checkEquals( r.getLocation(),     null,         "Wrong Location header" );
                        checkEquals( r.getContentType(),  "text/plain", "Wrong MIME type" );
                    } else {
                        reportError( "Null response" );
                    }
                } catch( Exception ex ) {
                    reportError( "Thread threw exception", theThreadIndex, ex );
                }
            }
            log.info( "Thread " + theThreadIndex + " is done." );
        }

        protected int theThreadIndex;
    }
}
