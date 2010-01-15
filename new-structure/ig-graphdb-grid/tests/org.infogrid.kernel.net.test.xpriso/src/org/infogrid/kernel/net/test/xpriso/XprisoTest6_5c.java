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

package org.infogrid.kernel.net.test.xpriso;

import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.util.logging.Log;

/**
 * Tests that newly created relationships propagate correctly.
 * Here the created relationship is between the home replicas, but one end has
 * not been replicated yet.
 */
public class XprisoTest6_5c
    extends
        AbstractXprisoTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may go wrong during a test
     */
    public void run()
        throws
            Exception
    {
        log.info( "Instantiating objects in mb1" );

        Transaction tx1 = mb1.createTransactionAsap();

        NetMeshBaseLifecycleManager life1 = mb1.getMeshBaseLifecycleManager();

        NetMeshObject obj1_mb1 = life1.createMeshObject(
                mb1.getMeshObjectIdentifierFactory().fromExternalForm( "obj1" ),
                TestSubjectArea.AA );
        obj1_mb1.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "This is a obj1." ));

        NetMeshObject obj2_mb1 = life1.createMeshObject(
                mb1.getMeshObjectIdentifierFactory().fromExternalForm( "obj2" ),
                TestSubjectArea.AA );
        obj2_mb1.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "This is a obj2." ));

        obj1_mb1.setWillGiveUpLock( false );
        obj2_mb1.setWillGiveUpLock( false );

        tx1.commitTransaction();

        log.debug( "Checking proxies (1)" );

        checkProxies( obj1_mb1, null, null, null, "obj1_mb1 has proxies" );
        checkProxies( obj2_mb1, null, null, null, "obj2_mb1 has proxies" );

        //

        log.info( "Accessing obj1 from mb2" );

        NetMeshObject obj1_mb2 = mb2.accessLocally(
                mb1.getIdentifier(),
                obj1_mb1.getIdentifier() );
        checkObject( obj1_mb2, "mb2 fails to access obj1." );

        log.debug( "Checking proxies (2)" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj2_mb1, new NetMeshBase[] {},      null, null, "obj2_mb1 has wrong proxies" );

        checkNotObject( obj1_mb1.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj1_mb1" );
        checkNotObject( obj2_mb1.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj2_mb1" );
        checkNotObject( obj1_mb2.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj1_mb2" );

        //

        log.info( "Creating relationship between obj1 and obj2 in mb1." );

        Transaction tx2 = mb1.createTransactionAsap();
        obj1_mb1.relateAndBless( TestSubjectArea.AR1A.getSource(), obj2_mb1 );

        tx2.commitTransaction();

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION ); // need to wait here as the operation is not synchronous

        //
        
        log.debug( "Checking replication" );
        
        NetMeshObject obj2_mb2 = mb2.findMeshObjectByIdentifier( obj2_mb1.getIdentifier() );
        
        checkNotObject( obj2_mb2, "obj2 in mb2 should not have been replicated" );

        //
        
        log.debug( "Checking proxies (3)" );
        
        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj2_mb1, new NetMeshBase[] {},      null, null, "obj2_mb1 has wrong proxies" );

        checkRelationshipProxies( obj1_mb1, obj2_mb1.getIdentifier(), null, "obj1-obj2 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj1_mb1.getIdentifier(), null, "obj2-obj1 has wrong relationship proxies in mb1" );

        //

        log.info( "Checking mb2 relationship." );

        MeshObjectSet neighbors1_mb2 = obj1_mb2.traverseToNeighborMeshObjects( false );
        MeshObjectSet related1_mb2   = obj1_mb2.traverse( TestSubjectArea.AR1A.getSource(), false );

        obj2_mb2 = mb2.findMeshObjectByIdentifier(  obj2_mb1.getIdentifier() ); // now it has been replicated

        MeshObjectSet neighbors2_mb2 = obj2_mb2.traverseToNeighborMeshObjects( false );
        MeshObjectSet related2_mb2   = obj2_mb2.traverse( TestSubjectArea.AR1A.getDestination(), false );
        
        checkEquals( neighbors1_mb2.size(), 1, "obj1 in mb2 has wrong number of neighbors" );
        checkEquals( related1_mb2.size(),   1, "obj1 in mb2 has wrong number of relationships" );
        checkEquals( neighbors2_mb2.size(), 1, "obj2 in mb2 has wrong number of neighbors" );
        checkEquals( related2_mb2.size(),   1, "obj2 in mb2 has wrong number of relationships" );

        // this must be later than in XprisoTest6_5b because earlier we don't have the object yet
        checkRelationshipProxies( obj1_mb2, obj2_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj1-obj2 has wrong relationship proxies in mb2" );
        checkRelationshipProxies( obj2_mb2, obj1_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj2-obj1 has wrong relationship proxies in mb2" );

        //

        log.info( "Checking mb1 relationship." );

        MeshObjectSet neighbors1_mb1 = obj1_mb1.traverseToNeighborMeshObjects( false );
        MeshObjectSet related1_mb1   = obj1_mb1.traverse( TestSubjectArea.AR1A.getSource(), false );
        MeshObjectSet neighbors2_mb1 = obj2_mb1.traverseToNeighborMeshObjects( false );
        MeshObjectSet related2_mb1   = obj2_mb1.traverse( TestSubjectArea.AR1A.getDestination(), false );
        
        checkEquals( neighbors1_mb1.size(), 1, "obj1 in mb1 has wrong number of neighbors" );
        checkEquals( related1_mb1.size(),   1, "obj1 in mb1 has wrong number of relationships" );
        checkEquals( neighbors2_mb1.size(), 1, "obj2 in mb1 has wrong number of neighbors" );
        checkEquals( related2_mb1.size(),   1, "obj2 in mb1 has wrong number of relationships" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        XprisoTest6_5c test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest6_5c( args );
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
     * @throws Exception all sorts of things may go wrong during a test
     */
    public XprisoTest6_5c(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest6_5c.class  );
        
        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactory.setNameServer( theNameServer );

        ProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create( true );

        mb1 = NetMMeshBase.create( net1, theModelBase, null, endpointFactory, proxyPolicyFactory, rootContext );
        mb2 = NetMMeshBase.create( net2, theModelBase, null, endpointFactory, proxyPolicyFactory, rootContext );

        theNameServer.put( mb1.getIdentifier(), mb1 );
        theNameServer.put( mb2.getIdentifier(), mb2 );
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        mb1.die();
        mb2.die();
        
        exec.shutdown();
    }

    /**
     * The first NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net1 = theMeshBaseIdentifierFactory.fromExternalForm( "test://one.local" );

    /**
     * The second NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net2 = theMeshBaseIdentifierFactory.fromExternalForm( "test://two.local" );

    /**
     * The first NetMeshBase.
     */
    protected NetMMeshBase mb1;

    /**
     * The second NetMeshBase.
     */
    protected NetMMeshBase mb2;

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 3 ); // I think we need three

    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest6_5c.class  );
}
