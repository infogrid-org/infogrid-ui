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

package org.infogrid.jee.net.TEST;

import org.infogrid.testharness.tomcat.AbstractTomcatTest;
import org.infogrid.util.logging.Log;

/**
 * Tests that the AllMeshObjectsViewlet renders things properly.
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
        log.info( "Testing general site setup" );
        

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
        super( args[0], thisPackage( AllMeshObjectsTest1.class, "Log.properties"  ));

        log = Log.getLogInstance( getClass() );
    }
}
