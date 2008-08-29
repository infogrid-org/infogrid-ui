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

package org.infogrid.httpd.TEST;

import org.infogrid.httpd.HttpEntity;
import org.infogrid.httpd.HttpEntityResponse;
import org.infogrid.httpd.HttpRequest;
import org.infogrid.httpd.HttpResponse;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.httpd.server.HttpServer;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.logging.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.Iterator;
import java.util.Map;

/**
 * Common functionality for all Http tests.
 */
public abstract class AbstractHttpdTest
        extends
            AbstractTest
{
    /**
     * Setup.
     *
     * @param args not used
     * @throws Exception any kind of exception
     */
    public AbstractHttpdTest(
            String [] args )
        throws
            Exception
    {
        super( thisPackage( AbstractHttpdTest.class, "Log.properties" ));

        log = Log.getLogInstance( getClass() );

        // set up the server
        HttpResponseFactory factory = new MyResponseFactory();

        theServer = new HttpServer( SERVER_PORT, NUMBER_THREADS );
        theServer.setResponseFactory( factory );

        // start the server
        theServer.start();
    }

    /**
     * Clean up after test.
     */
    @Override
    public void cleanup()
    {
        if( theServer != null ) {
            theServer.stop();
        }
    }

    // Our Logger
    protected static Log log;

    /**
     * Our HTTP Server.
     */
    protected HttpServer theServer;

    /**
     * The port on which we run our server.
     */
    protected static final int SERVER_PORT = 8081;

    /**
     * The number of threads to create in the server. FIXME? Should we run all tests
     * with different numbers of threads?
     */
    protected static final int NUMBER_THREADS = 0;

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
            HttpEntity entity;
            if( "GET".equals( request.getMethod())) {
                entity = new HttpEntity() {
                    public boolean canRead() {
                        return true;
                    }
                    public InputStream getAsStream() {
                        return new ByteArrayInputStream( String.valueOf( theCounter++ ).getBytes() );
                    }
                    public String getMime() {
                        return "text/plain";
                    }
                };
            } else {
                Map      postPars = request.getPostArguments();
                Iterator iter     = postPars.keySet().iterator();

                String sep = "";
                final StringBuffer responseContent = new StringBuffer();
                while( iter.hasNext() ) {
                    String key   = (String) iter.next();
                    String value = (String) postPars.get( key );

                    responseContent.append( sep );
                    responseContent.append( key ).append( '=' ).append( value );
                    sep = "&";
                }

                entity = new HttpEntity() {
                    public boolean canRead() {
                        return true;
                    }
                    public InputStream getAsStream() {

                        return new ByteArrayInputStream( responseContent.toString().getBytes() );
                    }
                    public String getMime() {
                        return "text/plain";
                    }
                };
            }
            HttpResponse ret = HttpEntityResponse.create( request, true, entity );
            return ret;
        }

        /**
         * Request counter.
         */
        protected int theCounter = 0;
    }
}
