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

package org.infogrid.kernel.net.test.mesh.externalized;

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.util.logging.Log;

/**
 * Tests NetMeshBaseIdentifier serialization and deserialization.
 */
public class NetSerializerTest1
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
        for( int i=0 ; i<testData.length ; ++i ) {
            log.info( "Testing " + testData[i] );

            NetMeshBaseIdentifier original = testData[i];
            NetMeshBaseIdentifier decoded  = null;
            String            encoded  = null;

            try {
                encoded = original.toExternalForm();
                
                log.info( "value: \"" + original + "\", serialized: \"" + encoded + "\"" );

                decoded = theMeshBaseIdentifierFactory.fromExternalForm( encoded );

                checkEquals( original, decoded, "incorrect deserialization" );

            } catch( Throwable ex ) {
                ++errorCount;
                if( encoded == null ) {
                    reportError( "element " + i + " threw exception during encoding", original, ex );
                } else {
                    reportError( "element " + i + " threw exception during decoding", original, encoded, ex );
                }
                checkEquals( original, decoded, "what we received" );
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
        NetSerializerTest1 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new NetSerializerTest1( args );
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
    public NetSerializerTest1(
            String [] args )
        throws
            Exception
    {
        super( NetSerializerTest1.class  );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( NetSerializerTest1.class  );


    /**
     * The test data.
     */
    protected static NetMeshBaseIdentifier[] testData;
    static {
        try {
            testData = new NetMeshBaseIdentifier [] {
                    theMeshBaseIdentifierFactory.fromExternalForm( "http://www.r-objects.com/" ),
                    theMeshBaseIdentifierFactory.fromExternalForm( "http://foo.example.com/abc.jsp&def=ghi,,/&amp;xyz." )
            };
        } catch( Throwable t ) {
            log.error( t );
        }
    }
}
