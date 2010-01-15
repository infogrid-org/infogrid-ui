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

package org.infogrid.meshbase.store.net.test;

import java.util.concurrent.ScheduledExecutorService;
import java.lang.ref.WeakReference;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.m.NetMMeshBaseNameServer;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.meshbase.store.net.NetStoreMeshBase;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.logging.Log;

/**
 * Tests recovery from disk after reboot.
 */
public class StoreNetMeshBaseTest6
        extends
            AbstractStoreNetMeshBaseTest
{
    /**
     * Run the test.
     *
     * @throws Exception thrown if an Exception occurred during the test
     */
    public void run()
        throws
            Exception
    {
        log.info( "Creating Meshbases and instantiating objects" );

        NetMMeshBaseNameServer<NetMeshBaseIdentifier,NetMeshBase> theNameServerA = NetMMeshBaseNameServer.create();
        
        MPingPongNetMessageEndpointFactory endpointFactoryA = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactoryA.setNameServer( theNameServerA );

        mb1A = NetStoreMeshBase.create( net1, theModelBase, null, endpointFactoryA, mb1MeshStore, mb1ProxyStore, rootContext );
        mb2A = NetStoreMeshBase.create( net2, theModelBase, null, endpointFactoryA, mb2MeshStore, mb2ProxyStore, rootContext );
        
        theNameServerA.put( mb1A.getIdentifier(), mb1A );
        theNameServerA.put( mb2A.getIdentifier(), mb2A );
        
        NetMeshObjectIdentifier id1 = mb1A.getMeshObjectIdentifierFactory().fromExternalForm( "one" );

        Transaction tx1A = mb1A.createTransactionNow();
        
        NetMeshObject obj1_1A = mb1A.getMeshBaseLifecycleManager().createMeshObject( id1 );
        
        tx1A.commitTransaction();
        tx1A = null;

        NetMeshObject obj1_2A = mb2A.accessLocally( mb1A.getIdentifier(), id1 );
        
        checkObject( obj1_1A, "No object obj1_1" );
        checkObject( obj1_2A, "No object obj1_2" );
        
        //
        
        log.info( "Killing off MeshBases" );

        WeakReference<NetStoreMeshBase> mb1ARef = new WeakReference<NetStoreMeshBase>( mb1A );
        WeakReference<NetStoreMeshBase> mb2ARef = new WeakReference<NetStoreMeshBase>( mb2A );
        WeakReference<NetMeshObject>    obj1_1ARef = new WeakReference<NetMeshObject>( obj1_1A );
        WeakReference<NetMeshObject>    obj1_2ARef = new WeakReference<NetMeshObject>( obj1_2A );
        
        endpointFactoryA = null;
        theNameServerA   = null;
        obj1_1A = null;
        obj1_2A = null;
        
        mb1A.die();
        mb2A.die();
        
        mb1A = null;
        mb2A = null;
        
        sleepUntilIsGone( mb1ARef, 15000L, "MB1 still here" );
        // sleepUntilIsGone( mb2ARef, 15000L, "MB2 still here" ); // FIXME: This somehow only sometimes goes away
        sleepUntilIsGone( obj1_1ARef, 1000L, "obj1_1 still here" );
        sleepUntilIsGone( obj1_2ARef, 1000L, "obj1_2 still here" );
        
        //
        
        log.info( "Recreating MeshBases" );
        
        NetMMeshBaseNameServer<NetMeshBaseIdentifier,NetMeshBase> theNameServerB = NetMMeshBaseNameServer.create();

        MPingPongNetMessageEndpointFactory endpointFactoryB = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactoryB.setNameServer( theNameServerB );

        mb1B = NetStoreMeshBase.create( net1, theModelBase, null, endpointFactoryB, mb1MeshStore, mb1ProxyStore, rootContext );
        mb2B = NetStoreMeshBase.create( net2, theModelBase, null, endpointFactoryB, mb2MeshStore, mb2ProxyStore, rootContext );
        
        theNameServerB.put( mb1B.getIdentifier(), mb1B );
        theNameServerB.put( mb2B.getIdentifier(), mb2B );
        
        NetMeshObject obj1_1B = mb1B.findMeshObjectByIdentifier( id1 );
        
        checkObject( obj1_1B, "obj1_1 not found" );
        
        //
        
        log.info( "Making change and checking whether it propagates" );
        
        Transaction tx1B = mb1B.createTransactionNow();
        
        obj1_1B.bless( TestSubjectArea.AA );
        
        tx1B.commitTransaction();
        
        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION );

        NetMeshObject obj1_2B = mb2B.findMeshObjectByIdentifier( id1 );
        checkObject( obj1_2B, "obj1_2 not found" );

        checkObject( obj1_1B, "obj1_1 not found" );
        checkObject( obj1_2B, "obj1_2 not found" );
        checkCondition( obj1_1B.isBlessedBy( TestSubjectArea.AA ), "ob1_1 not blessed" );
        checkCondition( obj1_2B.isBlessedBy( TestSubjectArea.AA ), "ob1_2 not blessed" );
     
        log.info( "done" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreNetMeshBaseTest6 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no argument>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreNetMeshBaseTest6( args );
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
     * @throws Exception anything can go wrong in a test
     */
    public StoreNetMeshBaseTest6(
            String [] args )
        throws
            Exception
    {
        super( StoreNetMeshBaseTest6.class );

        log.info( "Deleting old database and creating new database" );
        
        theSqlStore.initializeHard();

        mb1MeshStore  = IterablePrefixingStore.create( "mb1-mesh-",  theSqlStore );
        mb1ProxyStore = IterablePrefixingStore.create( "mb1-proxy-", theSqlStore );
        mb2MeshStore  = IterablePrefixingStore.create( "mb2-mesh-",  theSqlStore );
        mb2ProxyStore = IterablePrefixingStore.create( "mb2-proxy-", theSqlStore );
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        if( mb1A != null ) {
            mb1A.die();
        }
        if( mb2A != null ) {
            mb2A.die();
        }
        if( mb1B != null ) {
            mb1B.die();
        }
        if( mb2B != null ) {
            mb2B.die();
        }

        exec.shutdown();
    }

    /**
     * The first NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net1 = theMeshBaseIdentifierFactory.fromExternalForm( "http://one.local/" );

    /**
     * The second NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net2 = theMeshBaseIdentifierFactory.fromExternalForm( "http://two.local/" );

    /**
     * The Store storing NetMeshBase mb1's MeshObjects.
     */
    protected IterablePrefixingStore mb1MeshStore;
    
    /**
     * The Store storing NetMeshBase mb1's Proxies.
     */
    protected IterablePrefixingStore mb1ProxyStore;

    /**
     * The Store storing NetMeshBase mb2's MeshObjects.
     */
    protected IterablePrefixingStore mb2MeshStore;
    
    /**
     * The Store storing NetMeshBase mb2's Proxies.
     */
    protected IterablePrefixingStore mb2ProxyStore;

    /**
     * MeshBases to shut down at the end of the test.
     */
    protected NetStoreMeshBase mb1A;
    protected NetStoreMeshBase mb2A;
    protected NetStoreMeshBase mb1B;
    protected NetStoreMeshBase mb2B;

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

    // Our Logger
    private static Log log = Log.getLogInstance( StoreNetMeshBaseTest6.class );
}
