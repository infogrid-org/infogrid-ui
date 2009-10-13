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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.infogrid.httpd.HttpEntity;
import org.infogrid.httpd.HttpEntityResponse;
import org.infogrid.httpd.HttpErrorResponse;
import org.infogrid.httpd.HttpRequest;
import org.infogrid.httpd.HttpResponse;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.lid.model.yadis.util.YadisUtil;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.util.logging.Log;

/**
 * Tests XRDS discovery via X-XRDS header in the manner of jyte.com.
 */
public class YadisTest6
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

        NetMeshBaseIdentifier netId = theMeshBase1.getMeshBaseIdentifierFactory().fromExternalForm( "http://localhost:" + SERVER_PORT + "/" );

        NetMeshObject shadowHome = theMeshBase1.accessLocally( netId, CoherenceSpecification.ONE_TIME_ONLY );

        //

        log.info( "Checking for correct results" );

        MeshObjectSet services = YadisUtil.determineServicesFor( shadowHome );
        checkEquals( services.size(), 1, "Wrong number of services found" );

        if( log.isDebugEnabled() ) {
            log.debug( "ShadowHome: " + shadowHome );
            log.debug( "ShadowHome neighbors: " + shadowHome.traverseToNeighborMeshObjects() );
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
        YadisTest6 test = null;
        try {
            if( args.length != 2 ) {
                System.err.println( "Synopsis: <file name> <delay>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new YadisTest6( args );
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
    public YadisTest6(
            String [] args )
        throws
            Exception
    {
        super( YadisTest1.class, new MyResponseFactory( args[0], Long.valueOf( args[1] ) ));
    }

    // Our Logger
    private static Log log = Log.getLogInstance( YadisTest5.class );

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
         * @param xrdsFileName file name of the XRDS file.
         * @param delay the delay by the web server
         */
        public MyResponseFactory(
                String xrdsFileName,
                long   delay )
        {
            theXrdsFileName = xrdsFileName;
            theDelay        = delay;
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

            if( "GET".equals( request.getMethod() ) && ( "/" ).equals( request.getRelativeBaseUri() )) {
                HttpEntity entity = new HttpEntity() {
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
                ret = HttpEntityResponse.create( request, true, entity );
                ret.addHeader( "x-xrds-location", "http://localhost:" + SERVER_PORT + "/auth/xrds" );

            } else if( "GET".equals( request.getMethod() ) && ( "/auth/xrds" ).equals( request.getRelativeBaseUri() )) {
                HttpEntity entity = new HttpEntity() {
                        public boolean canRead() {
                            return true;
                        }
                        public InputStream getAsStream()
                                throws IOException
                        {
                            return new FileInputStream( theXrdsFileName );
                        }
                        public String getMime() {
                            return "application/xrds+xml";
                        }
                };
                ret = HttpEntityResponse.create( request, true, entity );

            } else if( "GET".equals( request.getMethod() ) && ( "/auth/openid_response" ).equals( request.getRelativeBaseUri() )) {
                HttpEntity entity = new HttpEntity() {
                        public boolean canRead() {
                            return true;
                        }
                        public InputStream getAsStream() {
                            return new ByteArrayInputStream( "Content of /auth/openid_response".getBytes() );
                        }
                        public String getMime() {
                            return "text/plain";
                        }
                };
                ret = HttpEntityResponse.create( request, true, entity );

            } else if( "GET".equals( request.getMethod() ) && ( "/auth/immediate_response" ).equals( request.getRelativeBaseUri() )) {
                HttpEntity entity = new HttpEntity() {
                        public boolean canRead() {
                            return true;
                        }
                        public InputStream getAsStream() {
                            return new ByteArrayInputStream( "Content of /auth/immediate_response".getBytes() );
                        }
                        public String getMime() {
                            return "text/plain";
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
         * The name of the XRDS file.
         */
        protected String theXrdsFileName;

        /**
         * Captures the slowness of a web server.
         */
        protected long theDelay;
    }
}
