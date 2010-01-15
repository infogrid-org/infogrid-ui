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
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.util.logging.Log;

/**
 * Tests that we can create relationships between
 * MeshObjects which are held by other MeshBases, and the correct propagation.
 */
public class XprisoTest7
    extends
        AbstractXprisoTest
{
    /**
     * Run the test.
     *
     * @throws Exception all kinds of things can go wrong in tests
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
        obj1_mb1.setWillGiveUpHomeReplica( false );
        obj2_mb1.setWillGiveUpLock( false );
        obj2_mb1.setWillGiveUpHomeReplica( false );

        tx1.commitTransaction();

        //

        log.debug( "Checking proxies (1)" );

        checkProxies( obj1_mb1, null, null, null, "obj1_mb1 has proxies" );
        checkProxies( obj2_mb1, null, null, null, "obj2_mb1 has proxies" );

        //

        log.info( "Accessing obj1 at mb2 from mb1" );

        NetMeshObject obj1_mb2 = mb2.accessLocally(
                mb1.getIdentifier(),
                obj1_mb1.getIdentifier() );
        checkObject( obj1_mb2, "mb2 fails to access obj1." );

        log.debug( "Checking proxies (2)" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj2_mb1, null,                      null, null, "obj2_mb1 has proxies" );

        //

        log.info( "Accessing obj1 at mb3 from mb1" );

        NetMeshObject obj1_mb3 = mb3.accessLocally(
                mb1.getIdentifier(),
                obj1_mb1.getIdentifier() );
        checkObject( obj1_mb3, "mb3 fails to access obj1." );

        log.debug( "Checking proxies (3)" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2, mb3 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 },      mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj1_mb3, new NetMeshBase[] { mb1 },      mb1,  mb1,  "obj1_mb3 has wrong proxies" );
        checkProxies( obj2_mb1, null,                           null, null, "obj2_mb1 has proxies" );

        //

        log.info( "Accessing obj2 at mb3 via mb2 from mb1" );

        NetMeshObject obj2_mb3 = mb3.accessLocally(
                mb3.getNetMeshObjectAccessSpecificationFactory().obtain(
                        new NetMeshBaseIdentifier[] {
                                mb2.getIdentifier(),
                                mb1.getIdentifier(),
                        },
                        obj2_mb1.getIdentifier() ));
        checkObject( obj2_mb3, "mb3 fails to access obj2." );
        NetMeshObject obj2_mb2 = mb2.findMeshObjectByIdentifier( obj2_mb1.getIdentifier() );
        checkObject( obj2_mb2, "mb2 does not have obj2." );

        log.debug( "Checking proxies (4)" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2, mb3 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 },      mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj1_mb3, new NetMeshBase[] { mb1 },      mb1,  mb1,  "obj1_mb3 has wrong proxies" );
        checkProxies( obj2_mb1, new NetMeshBase[] { mb2 },      null, null, "obj2_mb1 has wrong proxies" );
        checkProxies( obj2_mb2, new NetMeshBase[] { mb1, mb3 }, mb1,  mb1,  "obj2_mb2 has wrong proxies" );
        checkProxies( obj2_mb3, new NetMeshBase[] { mb2 },      mb2,  mb2,  "obj2_mb3 has wrong proxies" );

        //

        log.info( "Creating relationship between obj1 and obj2 in mb2." );

        Transaction tx2 = mb2.createTransactionAsap();
        obj1_mb2.relateAndBless( TestSubjectArea.AR1A.getSource(), obj2_mb2 );

        tx2.commitTransaction();

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION * 5L );

        log.debug( "Checking proxies (7)" );

        // For better or worse, forwarding the relationship from mb2 to mb3 causes:
        // 1. obj1 in mb3 to cancel the lease to mb1 and create one with mb2.
        // 2. obj2 in mb3 to cancel the lease to mb2 and create one with mb1
        // This is as designed. Whether the design is right is a different question ;-)

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 },      null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1, mb3 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj1_mb3, new NetMeshBase[] { mb2 },      mb2,  mb2,  "obj1_mb3 has wrong proxies" );
        checkProxies( obj2_mb1, new NetMeshBase[] { mb2, mb3 }, null, null, "obj2_mb1 has wrong proxies" );
        checkProxies( obj2_mb2, new NetMeshBase[] { mb1 },      mb1,  mb1,  "obj2_mb2 has wrong proxies" );
        checkProxies( obj2_mb3, new NetMeshBase[] { mb1 },      mb1,  mb1,  "obj2_mb3 has wrong proxies" );

        checkRelationshipProxies( obj1_mb1, obj2_mb1, new NetMeshBase[] { mb2 },      "obj1_mb1-obj2_mb1 has wrong relationship proxies" );
        checkRelationshipProxies( obj1_mb2, obj2_mb2, null,                           "obj1_mb2-obj2_mb2 has wrong relationship proxies" );
        checkRelationshipProxies( obj1_mb3, obj2_mb3, new NetMeshBase[] { mb1, mb2 }, "obj1_mb3-obj2_mb3 has wrong relationship proxies" );

        //

        log.info( "Checking mb2 relationship." );

        MeshObjectSet neighbors1_mb2 = obj1_mb2.traverseToNeighborMeshObjects( false );
        MeshObjectSet rsReplica_mb2  = obj1_mb2.traverse( TestSubjectArea.AR1A.getSource() );

        checkEquals( neighbors1_mb2.size(), 1, "obj1 in mb2 has wrong number of neighbors" );
        checkEquals( rsReplica_mb2.size(),  1, "obj1 in mb2 has wrong number of relationships" );

        //

        log.info( "Checking mb1 relationship." );

        MeshObjectSet neighbors1_mb1 = obj1_mb1.traverseToNeighborMeshObjects( false );
        MeshObjectSet rsReplica_mb1  = obj1_mb1.traverse( TestSubjectArea.AR1A.getSource() );

        checkEquals( neighbors1_mb1.size(), 1, "obj1 in mb1 has wrong number of neighbors" );
        checkEquals( rsReplica_mb1.size(),  1, "obj1 in mb1 has wrong number of relationships" );

        //

        log.info( "Checking mb3 relationship." );

        // now do it the other way round
        MeshObjectSet rsReplica_mb3  = obj1_mb3.traverse( TestSubjectArea.AR1A.getSource() );
        MeshObjectSet neighbors1_mb3 = obj1_mb3.traverseToNeighborMeshObjects( false );

        checkEquals( rsReplica_mb3.size(),  1, "obj1 in mb3 has wrong number of relationships" );
        checkEquals( neighbors1_mb3.size(), 1, "obj1 in mb3 has wrong number of neighbors" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        XprisoTest7_5 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest7_5( args );
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
     * @throws Exception all kinds of things can go wrong in tests
     */
    public XprisoTest7(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest7.class );

        MPingPongNetMessageEndpointFactory endpointFactory1 = MPingPongNetMessageEndpointFactory.create( exec1 );
        MPingPongNetMessageEndpointFactory endpointFactory2 = MPingPongNetMessageEndpointFactory.create( exec2 );
        MPingPongNetMessageEndpointFactory endpointFactory3 = MPingPongNetMessageEndpointFactory.create( exec3 );

        endpointFactory1.setNameServer( theNameServer );
        endpointFactory2.setNameServer( theNameServer );
        endpointFactory3.setNameServer( theNameServer );

        ProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create( true );

        mb1 = NetMMeshBase.create( net1, theModelBase, null, endpointFactory1, proxyPolicyFactory, rootContext );
        mb2 = NetMMeshBase.create( net2, theModelBase, null, endpointFactory2, proxyPolicyFactory, rootContext );
        mb3 = NetMMeshBase.create( net3, theModelBase, null, endpointFactory3, proxyPolicyFactory, rootContext );

        theNameServer.put( mb1.getIdentifier(), mb1 );
        theNameServer.put( mb2.getIdentifier(), mb2 );
        theNameServer.put( mb3.getIdentifier(), mb3 );
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        mb1.die();
        mb2.die();
        mb3.die();

        exec1.shutdown();
        exec2.shutdown();
        exec3.shutdown();
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
     * The third NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net3 = theMeshBaseIdentifierFactory.fromExternalForm( "test://three.local" );

    /**
     * The fourth NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net4 = theMeshBaseIdentifierFactory.fromExternalForm( "test://four.local" );

    /**
     * The first NetMeshBase.
     */
    protected NetMMeshBase mb1;

    /**
     * The second NetMeshBase.
     */
    protected NetMMeshBase mb2;

    /**
     * The third NetMeshBase.
     */
    protected NetMMeshBase mb3;

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec1 = createThreadPool( 1 ); // I think we need three
    protected ScheduledExecutorService exec2 = createThreadPool( 1 ); // I think we need three
    protected ScheduledExecutorService exec3 = createThreadPool( 1 ); // I think we need three

    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest7_5.class );
}
