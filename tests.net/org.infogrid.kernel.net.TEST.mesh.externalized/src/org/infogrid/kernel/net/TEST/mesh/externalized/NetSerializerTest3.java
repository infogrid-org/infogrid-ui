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

package org.infogrid.kernel.net.TEST.mesh.externalized;

import java.net.URISyntaxException;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.util.logging.Log;


/**
 * NetMeshBaseIdentifiers can only use protocols that have been specified in the factory.
 */
public class NetSerializerTest3
        extends
            AbstractNetSerializerTest
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
        String [] testData = {
            "https://foo.com/",
            "abc://bar.com"
        };
        
        for( int i=0 ; i<testData.length ; ++i ) {
            log.info( "Test " + i );

            NetMeshBaseIdentifier found = null;
            try {
                found = theFactory.fromExternalForm( testData[i] );
                
                reportError( "No exception thrown for " + testData[i] );

            } catch( URISyntaxException ex ) {
                // ok
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
        NetSerializerTest3 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new NetSerializerTest3( args );
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
     * @throws Exception all sorts of things may go wrong in tests
     */
    public NetSerializerTest3(
            String [] args )
        throws
            Exception
    {
        super( NetSerializerTest3.class  );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( NetSerializerTest3.class  );
    
}
