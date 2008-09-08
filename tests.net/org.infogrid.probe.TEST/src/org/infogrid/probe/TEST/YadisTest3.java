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
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.logging.Log;

/**
 * Tests XRDS discovery via HTTP-equiv.
 */
public class YadisTest3
        extends
            AbstractYadisTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may happen during a test
     */
    public void run()
        throws
            Exception
    {
        log.info( "accessing test server" );
        
        ShadowMeshBase meshBase1 = theProbeManager1.obtainFor( theNetworkIdentifier, CoherenceSpecification.ONE_TIME_ONLY );
        MeshObject     home      = meshBase1.getHomeObject();
        
        // 
        
        log.info( "Checking for correct results" );
        
        checkYadisResults( home, 2 );
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
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
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
     * @throws Exception all sorts of things may happen during a test
     */
    public YadisTest3(
            String [] args )
        throws
            Exception
    {
        super( YadisTest3.class, new MyResponseFactory() );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( YadisTest3.class);

    /**
      * A HttpResponseFactory that acts as the RelyingParty.
      */
    static class MyResponseFactory
        implements
            HttpResponseFactory
    {
        /**
          * Factory method for a HttpResponse.
          *
          * @param request the HttpRequest for which we create a HttpResponse
          * @return the created HttpResponse
          */
        public HttpResponse createResponse(
                HttpRequest request )
        {
            if( "GET".equals( request.getMethod() ) && "/".equals( request.getRelativeBaseUri() )) {             
                HttpEntity entity = new HttpEntity() {
                        public boolean canRead() {
                            return true;
                        }
                        public InputStream getAsStream() {
                            return new ByteArrayInputStream( HTML_WITH_EQUIV.getBytes() );
                        }
                        public String getMime() {
                            return "text/html";
                        }
                };                
                HttpResponse ret = HttpEntityResponse.create( request, true, entity );
                return ret;
            } else if( "GET".equals( request.getMethod() ) && "/xrds".equals( request.getRelativeBaseUri() )) {             
                HttpEntity entity = new HttpEntity() {
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
                HttpResponse ret = HttpEntityResponse.create( request, true, entity );
                return ret;
            } else {
                return HttpErrorResponse.create( request, "500", null );
            }
        }
    }
}
