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

package org.infogrid.probe.TEST;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.infogrid.httpd.HttpEntity;
import org.infogrid.httpd.HttpEntityResponse;
import org.infogrid.httpd.HttpErrorResponse;
import org.infogrid.httpd.HttpRequest;
import org.infogrid.httpd.HttpResponse;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.local.LocalNetMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.logging.Log;

/**
 * Tests XRDS discovery via MIME type.
 */
public class YadisTest1
        extends
            AbstractYadisDiscoveryTest
{
    /**
     * Run one test scenario.
     *
     * @param mb the NetMeshBase to use for this scenario
     * @param mode the mode
     * @throws Exception all sorts of things may happen during a test
     */
    public void run(
            LocalNetMeshBase mb,
            boolean          mode )
        throws
            Exception
    {
        log.info( "Accessing test data source (1) - " + mode );

        theWithYadis = mode;

        NetMeshObject  shadowHome = mb.accessLocally( theIdentityIdentifier, CoherenceSpecification.ONE_TIME_ONLY );
        ShadowMeshBase shadow     = mb.getShadowMeshBaseFor( theIdentityIdentifier );
        
        if( mode ) {
            checkYadisResultsDirect( shadowHome, 2 );
        } else {
            checkNoYadisResults( shadowHome );
        }

        //

        log.info(  "Accessing test data source (2) - " + mode );

        theWithYadis = !mode;
        shadow.doUpdateNow();
        sleepFor( PINGPONG_ROUNDTRIP_DURATION );

        if( !mode ) {
            checkYadisResultsDirect( shadowHome, 2 );
        } else {
            checkNoYadisResults( shadowHome );
        }

        //

        log.info( "Accessing test data source (3) - " + mode );

        theWithYadis = mode;
        shadow.doUpdateNow();
        sleepFor( PINGPONG_ROUNDTRIP_DURATION );

        if( mode ) {
            checkYadisResultsDirect( shadowHome, 2 );
        } else {
            checkNoYadisResults( shadowHome );
        }
    }

    /**
     * Run the first test.
     *
     * @throws Exception all sorts of things may happen during a test
     */
    public void runWithoutWithWithout()
        throws
            Exception
    {
        RecordingTransactionListener listener = new RecordingTransactionListener();
        theMeshBase2.addWeakTransactionListener( listener );

        //

        log.info( "Accessing test data source without Yadis" );

        theWithYadis = false;

        NetMeshObject  shadowHome = theMeshBase2.accessLocally( theIdentityIdentifier, CoherenceSpecification.ONE_TIME_ONLY );
        ShadowMeshBase shadow     = theMeshBase2.getShadowMeshBaseFor( theIdentityIdentifier );

        checkEquals( listener.getCommittedTransactions().size(), 1, "Wrong number of Transactions" );

        checkNoYadisResults( shadowHome );

        //

        log.info(  "Accessing test data source with Yadis" );

        theWithYadis = true;
        shadow.doUpdateNow();
        sleepFor( PINGPONG_ROUNDTRIP_DURATION );

        checkEquals( listener.getCommittedTransactions().size(), 2, "Wrong number of Transactions" );

        checkYadisResultsDirect( shadowHome, 2 );

        //

        log.info( "Accessing test data source without Yadis again" );

        theWithYadis = false;
        shadow.doUpdateNow();
        sleepFor( PINGPONG_ROUNDTRIP_DURATION );

        checkEquals( listener.getCommittedTransactions().size(), 3, "Wrong number of Transactions" );

        checkNoYadisResults( shadowHome );

        //

        listener.clear();
        listener = null;
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        YadisTest1 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <delay>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new YadisTest1( args );
            test.run();

        } catch( Throwable ex ) {
            log.error( ex );
            System.exit(1);
        }
        if( test != null ) {
            test.cleanup();
        }
        if( errorCount == 0 ) {
            log.info( "PASS" );
        } else {
            log.info( "FAIL (" + errorCount + " errors)" );
        }
        System.exit( errorCount );
    }

    /**
     * Constructor.
     * 
     * @param args command-line arguments
     * @throws Exception all sorts of things may happen during a test
     */
    public YadisTest1(
            String [] args )
        throws
            Exception
    {
        super( YadisTest1.class, new MyResponseFactory( Long.valueOf( args[0] ) ));
    }

    // Our Logger
    private static Log log = Log.getLogInstance( YadisTest1.class);

    /**
      * A HttpResponseFactory that acts as the RelyingParty.
      */
    static class MyResponseFactory
        implements
            HttpResponseFactory
    {
        /**
         * Constructor.
         *
         * @param delay the delay by the web server
         */
        public MyResponseFactory(
                long delay )
        {
            theDelay = delay;
        }

        /**
          * Factory method for a HttpResponse.
          *
          * @param request the HttpRequest for which we create a HttpResponse
          * @return the created HttpResponse
          */
        public HttpResponse createResponse(
                HttpRequest request )
        {
            log.debug( "Incoming request", request );
            
            String accept = request.getHttpParameters().get( "Accept" );

            HttpResponse ret;
            if( "GET".equals( request.getMethod() ) && ("/" + IDENTITY_LOCAL_IDENTIFIER ).equals( request.getRelativeBaseUri() )) {
                HttpEntity entity;
                if( theWithYadis && accept != null && accept.indexOf( "application/xrds+xml") >= 0 ) {
                    entity = new HttpEntity() {
                            public boolean canRead() {
                                return true;
                            }
                            public InputStream getAsStream() {
                                return new ByteArrayInputStream( XRDS.getBytes() );
                            }
                            public String getMime() {
                                return "application/xrds+xml";
                            }
                    };
                
                } else {
                    entity = new HttpEntity() {
                            public boolean canRead() {
                                return true;
                            }
                            public InputStream getAsStream() {
                                return new ByteArrayInputStream( HTML.getBytes() );
                            }
                            public String getMime() {
                                return "text/html";
                            }
                    };
                
                }
                ret = HttpEntityResponse.create( request, true, entity );
            } else {
                ret = HttpErrorResponse.create( request, "500", null );
            }

            if( theDelay > 0L ) {
                try {
                    Thread.sleep( theDelay );
                } catch( Throwable t ) {
                    log.error( t );
                }
            }
            return ret;
        }

        /**
         * Captures the slowness of a web server.
         */
        protected long theDelay;
    }
}
