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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.infogrid.httpd.HttpEntity;
import org.infogrid.httpd.HttpEntityResponse;
import org.infogrid.httpd.HttpErrorResponse;
import org.infogrid.httpd.HttpRequest;
import org.infogrid.httpd.HttpResponse;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.lid.model.yadis.YadisSubjectArea;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.util.logging.Log;

/**
 * Tests XRDS discovery via MIME type with a mylid.net example file.
 */
public class YadisTest5
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

        NetMeshObject shadowHome = theMeshBase.accessLocally( theNetworkIdentifier, CoherenceSpecification.ONE_TIME_ONLY );

        //

        log.info( "Checking for correct results" );

        MeshObjectSet services = shadowHome.traverse( YadisSubjectArea.XRDSSERVICECOLLECTION_COLLECTS_XRDSSERVICE.getSource() );
        checkEquals( services.size(), 9, "Wrong number of services found" );

        boolean found [] = new boolean[ 9 ];
        for( int i=0 ; i<found.length ; ++i ) {
            MeshObject current = services.get( i );

            checkCondition( current.isBlessedBy( YadisSubjectArea.XRDSSERVICE ), "Index " + i + " is not a Service" );

            IntegerValue priority = (IntegerValue) current.getPropertyValue( YadisSubjectArea.XRDSSERVICE_PRIORITY );
            found[ (int) priority.longValue() - 1 ] = true;
        }
        for( int i=0 ; i<found.length ; ++i ) {
            checkCondition( found[i], "Index " + i + " not found" );
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
        YadisTest5 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new YadisTest5( args );
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
    public YadisTest5(
            String [] args )
        throws
            Exception
    {
        super( YadisTest1.class, new MyResponseFactory( args[0] ) );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( YadisTest5.class);

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
         */
        public MyResponseFactory(
                String xrdsFileName )
        {
            theXrdsFileName = xrdsFileName;
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
            String accept = request.getHttpParameters().get( "Accept" );
            
            if( "GET".equals( request.getMethod() ) && "/".equals( request.getRelativeBaseUri() )) {             
                HttpEntity entity;
                if( accept != null && accept.indexOf( "application/xrds+xml") >= 0 ) {
                    entity = new HttpEntity() {
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
                HttpResponse ret = HttpEntityResponse.create( request, true, entity );
                return ret;
            } else {
                return HttpErrorResponse.create( request, "500", null );
            }
        }
        
        /**
         * The name of the XRDS file.
         */
        protected String theXrdsFileName;
    }
}
