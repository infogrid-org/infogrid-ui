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
import org.infogrid.util.logging.Log;

/**
 * Tests pseudo-XRDS discovery via the OpenID link rel tags.
 */
public class YadisTest4
        extends
            AbstractYadisTest
{
    /**
     * Run the test.
     *
     * @throws Exception thrown if an Exception occurred during the test
     */
    public void run()
        throws
            Exception
    {
        log.info( "accessing test data source" );

        NetMeshObject shadowHome = theMeshBase.accessLocally( theIdentityIdentifier, CoherenceSpecification.ONE_TIME_ONLY );

        //

        log.info( "Checking for correct results" );

        checkYadisResultsDirect( shadowHome, 1 );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        YadisTest3 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <delay>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new YadisTest3( args );
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
     * @throws Exception thrown if an Exception occurred during the test
     */
    public YadisTest4(
            String [] args )
        throws
            Exception
    {
        super( YadisTest4.class, new MyResponseFactory( Long.valueOf( args[0] ) ));
    }

    // Our Logger
    private static Log log = Log.getLogInstance( YadisTest4.class);

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
            HttpResponse ret;
            if( "GET".equals( request.getMethod() ) && ( "/" + IDENTITY_LOCAL_IDENTIFIER ).equals( request.getRelativeBaseUri() )) {
                HttpEntity entity = new HttpEntity() {
                        public boolean canRead() {
                            return true;
                        }
                        public InputStream getAsStream() {
                            return new ByteArrayInputStream( HTML_WITH_OPENID_LINK_REL.getBytes() );
                        }
                        public String getMime() {
                            return "text/html";
                        }
                };                
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
