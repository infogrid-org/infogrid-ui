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

import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseAccessSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.ScopeSpecification;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;

import java.util.ArrayList;
import org.infogrid.meshbase.net.DefaultNetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.proxy.DefaultProxyFactory;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;

/**
 * Tests NetMeshObjectAccessSpecification serialization and deserialization.
 */
public class NetSerializerTest2
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
        NetMeshBaseIdentifier nmbid        = theFactory.fromExternalForm( "http://here.local/" );
        DefaultProxyFactory   proxyFactory = DefaultProxyFactory.create( null, NiceAndTrustingProxyPolicyFactory.create() );

        NetMMeshBase mb = NetMMeshBase.create(
                nmbid,
                DefaultNetMeshObjectAccessSpecificationFactory.create( nmbid, theFactory ),
                null,
                null,
                proxyFactory,
                SimpleContext.createRoot( "root" ));
        
        NetMeshBaseIdentifier [] testData = new NetMeshBaseIdentifier [] {
                theFactory.fromExternalForm( "http://www.r-objects.com/" ),
                // NetMeshBaseIdentifier.obtain( "=testing" ),  // FIXME XRI's don't currently work
                // NetMeshBaseIdentifier.obtain( "@testing@abc" ),
        };
        NetMeshObjectIdentifier [] testIdentifiers = new NetMeshObjectIdentifier[] {
                null,
                mb.getMeshObjectIdentifierFactory().fromExternalForm( "test://def.org/" ),
                mb.getMeshObjectIdentifierFactory().fromExternalForm( "http://abc.com/" ),
                mb.getMeshObjectIdentifierFactory().fromExternalForm( "http://abc.com/#def" ),
        };
        
        ScopeSpecification [] testScopes = new ScopeSpecification[] {
                null,
                new ScopeSpecification.SimpleStep( 1 ),
                new ScopeSpecification.SimpleStep( 123 ),
        };
        CoherenceSpecification [] testCoherences = new CoherenceSpecification[] {
                null,
                CoherenceSpecification.ONE_TIME_ONLY,
                new CoherenceSpecification.Periodic( 12345L ),
                new CoherenceSpecification.AdaptivePeriodic( 123L, 456L, 78.9 )
        };
        
        for( int i=1 ; i< ( 1<< testData.length ) ; ++i ) {
            log.info( "Test " + i );

            ArrayList<NetMeshBaseIdentifier> temp = new ArrayList<NetMeshBaseIdentifier>();
            for( int j=0 ; j<testData.length ; ++j ) { // start with one
                if( ( i & ( 1<<j )) != 0 ) {
                    temp.add( testData[j] );
                }
            }
            
            NetMeshBaseIdentifier [] test = ArrayHelper.copyIntoNewArray( temp, NetMeshBaseIdentifier.class );

            for( NetMeshObjectIdentifier identifier : testIdentifiers ) {
                for( ScopeSpecification scope : testScopes ) {
                    for( CoherenceSpecification coherence : testCoherences ) {

                        NetMeshBaseAccessSpecification [] meshBaseAccess = new NetMeshBaseAccessSpecification[ test.length ];
                        for( int j=0 ; j<meshBaseAccess.length ; ++j ) {
                            meshBaseAccess[j] = mb.getNetMeshObjectAccessSpecificationFactory().getNetMeshBaseAccessSpecificationFactory().obtain( test[j], scope, coherence );
                        }
                        
                        NetMeshObjectAccessSpecification original = mb.getNetMeshObjectAccessSpecificationFactory().obtain( meshBaseAccess );
                        NetMeshObjectAccessSpecification decoded  = null;
                        String      encoded  = null;

                        try {
                            encoded = original.toExternalForm();

                            log.info( "value: \"" + original + "\", serialized: \"" + encoded + "\"" );

                            decoded = mb.getNetMeshObjectAccessSpecificationFactory().fromExternalForm( encoded );

                            checkEqualsInSequence( original.getAccessPath(), decoded.getAccessPath(), "incorrect paths in deserialization" );
                            checkEquals( original.getNetMeshObjectIdentifier(), decoded.getNetMeshObjectIdentifier(), "incorrect external name in deserialization" );

                        } catch( Throwable ex ) {
                            ++errorCount;
                            if( encoded == null ) {
                                reportError( "ERROR: element " + i + " of type " + original.getClass() + " threw exception of "
                                        + ex.getClass() + " during "
                                        + "encoding of "
                                        + original,
                                        ex );
                            } else {
                                reportError( "ERROR: element " + i + " of type " + original.getClass() + " threw exception of "
                                        + ex.getClass() + " during "
                                        + "decoding of "
                                        + original
                                        + " from "
                                        + encoded,
                                        ex );
                            }
                            checkEquals( original, decoded, "what we received" );
                        }                        
                    }
                }
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
        NetSerializerTest2 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new NetSerializerTest2( args );
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
    public NetSerializerTest2(
            String [] args )
        throws
            Exception
    {
        super( NetSerializerTest2.class  );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( NetSerializerTest2.class  );
    
}
