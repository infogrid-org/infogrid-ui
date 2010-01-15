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

import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.DefaultNetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.net.proxy.DefaultProxyFactory;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionAction;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.logging.Log;

/**
 * NetMeshObjectIdentifiers.
 */
public class NetSerializerTest4
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
        NetMeshBaseIdentifier nmbid        = theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local/" );
        DefaultProxyFactory   proxyFactory = DefaultProxyFactory.create( null, NiceAndTrustingProxyPolicyFactory.create() );

        final NetMMeshBase mb = NetMMeshBase.create(
                nmbid,
                DefaultNetMeshObjectAccessSpecificationFactory.create( nmbid, theMeshBaseIdentifierFactory ),
                theModelBase,
                null,
                proxyFactory,
                SimpleContext.createRoot( "root" ));

        NetMeshObject one = mb.getHomeObject();
        NetMeshObject two
                = mb.executeNow( new TransactionAction<NetMeshObject>() {
                    public NetMeshObject execute(
                            Transaction tx )
                        throws
                            Throwable
                    {
                        return mb.getMeshBaseLifecycleManager().createMeshObject( mb.getMeshObjectIdentifierFactory().fromExternalForm( "#1234" ));
                    }
                });

        //

        checkEquals( one.getIdentifier().toExternalForm(), "http://here.local/",      "wrong one" );
        checkEquals( two.getIdentifier().toExternalForm(), "http://here.local/#1234", "wrong two" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        NetSerializerTest4 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new NetSerializerTest4( args );
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
    public NetSerializerTest4(
            String [] args )
        throws
            Exception
    {
        super( NetSerializerTest4.class  );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( NetSerializerTest4.class  );

}
