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
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.meshbase.store.net.NetStoreMeshBase;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.logging.Log;

/**
 * Tests that PingPong resumes after recovery from disk.
 */
public class StoreNetMeshBaseTest5
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
        StringValue oldValue = StringValue.create( "old value" );
        StringValue newValue = StringValue.create( "new value" );
        
        log.info( "Setting up entities" );

        MeshObjectIdentifier obj1Name = mb1.getMeshObjectIdentifierFactory().fromExternalForm( "obj1" );
        
        Transaction tx = mb1.createTransactionAsap();

        NetMeshBaseLifecycleManager life1 = mb1.getMeshBaseLifecycleManager();

        NetMeshObject obj1_mb1 = life1.createMeshObject( obj1Name, TestSubjectArea.AA );
        obj1_mb1.setPropertyValue( TestSubjectArea.A_X, oldValue );
        obj1Name = obj1_mb1.getIdentifier();

        tx.commitTransaction();
        tx = null;

        //

        log.info( "replicating and checking" );
        NetMeshObject obj1_mb2 = mb2.accessLocally(
                mb1.getIdentifier(),
                obj1_mb1.getIdentifier() );

        Proxy mb1_p1 = mb1.getProxyFor( mb2.getIdentifier());
        Proxy mb2_p2 = mb2.getProxyFor( mb1.getIdentifier());
        
        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );

        checkEquals( mb1MeshStore.size(),  2, "Wrong number of entries in mb1MeshStore" );
        checkEquals( mb1ProxyStore.size(), 1, "Wrong number of entries in mb1ProxyStore" );
        checkEquals( mb2MeshStore.size(),  2, "Wrong number of entries in mb2MeshStore" );
        checkEquals( mb2ProxyStore.size(), 1, "Wrong number of entries in mb2ProxyStore" );

        //
        
        log.info( "now erasing cache" );
        
        obj1_mb1 = null;
        obj1_mb2 = null;
        
        collectGarbage();

        //
        
        log.info( "Recreating mb2 object and changing property" );
        
        obj1_mb2 = mb2.findMeshObjectByIdentifier( obj1Name );
        
        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION );

        tx = mb2.createTransactionAsap();
        
        obj1_mb2.setPropertyValue( TestSubjectArea.A_X, newValue );
        
        tx.commitTransaction();
        tx = null;

        //

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION );

        log.info( "checking that property propagated" );

        checkEquals( obj1_mb2.getPropertyValue( TestSubjectArea.A_X ), newValue, "obj1_mb2 property wrong" );

        obj1_mb1 = mb1.findMeshObjectByIdentifier( obj1Name );
        checkEquals( obj1_mb1.getPropertyValue( TestSubjectArea.A_X ), newValue, "obj1_mb1 property wrong" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreNetMeshBaseTest5 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreNetMeshBaseTest5( args );
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
    public StoreNetMeshBaseTest5(
            String [] args )
        throws
            Exception
    {
        super( StoreNetMeshBaseTest2.class );

        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactory.setNameServer( theNameServer );

        log.info( "Deleting old database and creating new database" );

        theSqlStore.initializeHard();

        mb1MeshStore  = IterablePrefixingStore.create( "mb1-mesh-",  theSqlStore );
        mb1ProxyStore = IterablePrefixingStore.create( "mb1-proxy-", theSqlStore );
        mb2MeshStore  = IterablePrefixingStore.create( "mb2-mesh-",  theSqlStore );
        mb2ProxyStore = IterablePrefixingStore.create( "mb2-proxy-", theSqlStore );
        
        mb1 = NetStoreMeshBase.create( net1, theModelBase, null, endpointFactory, mb1MeshStore, mb1ProxyStore, rootContext );
        mb2 = NetStoreMeshBase.create( net2, theModelBase, null, endpointFactory, mb2MeshStore, mb2ProxyStore, rootContext );
        
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
    protected NetMeshBaseIdentifier net1 = theMeshBaseIdentifierFactory.fromExternalForm( "http://one.local/" );

    /**
     * The second NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net2 = theMeshBaseIdentifierFactory.fromExternalForm( "http://two.local/" );

    /**
     * The first NetMeshBase.
     */
    protected NetStoreMeshBase mb1;

    /**
     * The second NetMeshBase.
     */
    protected NetStoreMeshBase mb2;

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
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

    // Our Logger
    private static Log log = Log.getLogInstance( StoreNetMeshBaseTest5.class );
}
