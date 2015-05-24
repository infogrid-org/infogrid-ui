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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.net.test;

import org.infogrid.testharness.tomcat.AbstractTomcatTest;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;

/**
 * Tests that the AllMeshObjectsViewlet renders MeshObjects in the main NetMeshBase properly.
 */
public class AllMeshObjectsTest1
        extends
            AbstractTomcatTest
{
    private static Log log = Log.getLogInstance( AllMeshObjectsTest1.class ); // our own, private logger

    /**
     * Run the test.
     *
     * @throws Exception this code may throw any Exception
     */
    public void run()
            throws
                Exception
    {
        log.info( "Looking at all the MeshObjectIdentifiers" );
        
        HTTP.Response r = HTTP.http_get( theApplicationUrl + "/" );
        checkRegex( "200",       r.getResponseCode(), "wrong response code" );
        checkRegex( "text/html", r.getContentType(),  "wrong mime type" );

        String content = r.getContentAsString();
        
        String [] objects = {
            "<a href=\"/org.infogrid.jee.net.testapp/custom%3A//example.org/%3Ffoo%3Dbar%26argl%3Dbrgl\" target=\"_self\" title=\"\"><span class=\"org-infogrid-mesh-a-net-DefaultAnetMeshObjectIdentifier\">custom://example.org/?foo=bar&amp;argl=brgl</span></a>",
            "<a href=\"/org.infogrid.jee.net.testapp/\" target=\"_self\" title=\"\"><span class=\"org-infogrid-mesh-a-net-DefaultAnetMeshObjectIdentifier\">&lt;HOME&gt;</span></a>",
            "<a href=\"/org.infogrid.jee.net.testapp/custom%3A//example.com/\" target=\"_self\" title=\"\"><span class=\"org-infogrid-mesh-a-net-DefaultAnetMeshObjectIdentifier\">custom://example.com/</span></a>",
            "<a href=\"/org.infogrid.jee.net.testapp/custom%3A//example.com/%23xxxx\" target=\"_self\" title=\"\"><span class=\"org-infogrid-mesh-a-net-DefaultAnetMeshObjectIdentifier\">custom://example.com/#xxxx</span></a>",
            "<a href=\"/org.infogrid.jee.net.testapp/custom%3A//example.org/%3Ffoo%3Dbar%26argl%3Dbrgl%23xxxx\" target=\"_self\" title=\"\"><span class=\"org-infogrid-mesh-a-net-DefaultAnetMeshObjectIdentifier\">custom://example.org/?foo=bar&amp;argl=brgl#xxxx</span></a>"
        };
        for( int i=0 ; i<objects.length ; ++i ) {
            checkCondition( content.indexOf( objects[i] ) >=0, "Not found: " + objects[i] );
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
        AllMeshObjectsTest1 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }
            test = new AllMeshObjectsTest1( args );
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
    public AllMeshObjectsTest1(
            String [] args )
        throws
            Exception
    {
        super( args[0] );

        log = Log.getLogInstance( getClass() );
    }
}
