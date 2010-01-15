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

package org.infogrid.util.test;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;

/**
 * Tests URL encoding via the HTTP class.
 */
public class UrlEncodingTest1
        extends
            AbstractTest
{
    /**
     * Run the test.
     * 
     * @throws Exception all sorts of things may happen in a test
     */
    public void run()
        throws
            Exception
    {
        String [][] testCases = {
            {
                "abc def",
                "abc%20def",
                "abc+def"
            },
            {
                "abc?def",
                "abc%3Fdef",
                "abc%3Fdef"
            },
            {
                "abc/def",
                "abc/def",
                "abc/def" // do not encode "/", Tomcat does not like that at all, it considers it a security issue
            },
            {
                "abc:def",
                "abc%3Adef",
                "abc%3Adef"
            },
            {
                "abc#def",
                "abc%23def",
                "abc%23def"
            },
        };

        for( int i=0 ; i<testCases.length ; ++i ) {
            log.debug( "Now testing " + i );
            
            String input = testCases[i][0];
            String safeUrl      = HTTP.encodeToValidUrl( input );
            String safeArgument = HTTP.encodeToValidUrlArgument( input );
            
            checkEquals( testCases[i][1], safeUrl,      "SafeURL encoding failed for test case \"" + input + "\" (" + i + ")" );
            checkEquals( testCases[i][2], safeArgument, "SafeURLArgument encoding failed for test case \"" + input + "\" (" + i + ")" );
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
        UrlEncodingTest1 test = null;
        try {
            if( false && args.length != 0 ){
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new UrlEncodingTest1( args );
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
            log.error( "FAIL (" + errorCount + " errors)" );
        }

        System.exit( errorCount );
    }

    /**
     * Constructor.
     *
     * @param args command-line arguments
     * @throws Exception all sorts of things may happen in a test
     */
    public UrlEncodingTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( UrlEncodingTest1.class  ); // our own, private logger
}
