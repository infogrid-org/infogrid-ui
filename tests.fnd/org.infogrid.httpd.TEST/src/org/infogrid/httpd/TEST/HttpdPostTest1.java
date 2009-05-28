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

package org.infogrid.httpd.TEST;

import java.util.Iterator;
import org.infogrid.util.ArrayMap;
import org.infogrid.util.http.HTTP;

/**
 * Tests HTTP POSTs.
 */
public class HttpdPostTest1
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
        Thread.sleep( 1000L );

        String url = "http://localhost:" + SERVER_PORT + "/";

        ArrayMap<String,String> pars = new ArrayMap<String,String>();
        pars.put( "abc", "def" );

        int MAX = 1000;

        for( int i=0 ; i<MAX ; ++i ) {
            log.info( "Performing test " + i );

            pars.put( "index", String.valueOf( i ));

            HTTP.Response r = HTTP.http_post( url, pars, false );

            if( r != null ) {
                checkEquals( r.getResponseCode(), "200",               "Wrong response code" );
                checkEquals( r.getLocation(),     null,                "Wrong Location header" );
                checkEquals( r.getContentType(),  "text/plain",        "Wrong MIME type" );

                StringBuffer correctResponse = new StringBuffer();
                String   sep = "";
                Iterator<String> iter = pars.keySet().iterator();
                while( iter.hasNext() ) {
                    String key   = iter.next();
                    String value = pars.get( key );
                    correctResponse.append( sep ).append( key ).append( "=" ).append( value );
                    sep = "&";
                }
                if( !checkEquals( r.getContentAsString(), correctResponse.toString(), "Wrong response" )) {
                    log.info( "Pars is " + pars );
                }
            } else {
                reportError( "Null response" );
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
        HttpdPostTest1 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }
            test = new HttpdPostTest1( args );
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
    public HttpdPostTest1(
            String [] args )
        throws
            Exception
    {
        super( args );
    }
}
