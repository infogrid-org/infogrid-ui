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

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.store.net.NetStoreMeshBase;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.logging.Log;

/**
 * Makes sure that replication info is written correctly into the Store.
 */
public class StoreNetMeshBaseTest2
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
// first section: Make sure things work while in memory
// second section: Make sure things get restored correctly from Store
        
        log.info( "Setting up entities" );

        MeshObjectIdentifier obj1Name = mb1.getMeshObjectIdentifierFactory().fromExternalForm( "obj1" );
        
        Transaction tx = mb1.createTransactionAsap();

        NetMeshBaseLifecycleManager life1 = mb1.getMeshBaseLifecycleManager();

        NetMeshObject obj1_mb1 = life1.createMeshObject( obj1Name );

        tx.commitTransaction();
        tx = null;

        obj1Name = obj1_mb1.getIdentifier();

        checkProxies( obj1_mb1, null, null, null, "obj1_mb1 has proxies" );
        checkEquals( mb1MeshStore.size(),  2, "Wrong number of entries in mb1MeshStore" );
        checkEquals( mb1ProxyStore.size(), 0, "Wrong number of entries in mb1ProxyStore" );
        checkEquals( mb2MeshStore.size(),  1, "Wrong number of entries in mb2MeshStore" );
        checkEquals( mb2ProxyStore.size(), 0, "Wrong number of entries in mb2ProxyStore" );
        
        //

        log.info( "replicating and checking" );
        NetMeshObject obj1_mb2 = mb2.accessLocally(
                mb1.getIdentifier(),
                obj1_mb1.getIdentifier() );

        checkObject( obj1_mb2, "accessLocally() did not work" );
        checkTypesReplication(      obj1_mb1, obj1_mb2, "accessLocally() types replication didn't work" );
        checkPropertiesReplication( obj1_mb1, obj1_mb2, "accessLocally() properties replication didn't work" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkEquals( mb1MeshStore.size(),  2, "Wrong number of entries in mb1MeshStore" );
        checkEquals( mb1ProxyStore.size(), 1, "Wrong number of entries in mb1ProxyStore" );
        checkEquals( mb2MeshStore.size(),  2, "Wrong number of entries in mb2MeshStore" );
        checkEquals( mb2ProxyStore.size(), 1, "Wrong number of entries in mb2ProxyStore" );

        //
        
        log.info( "now erasing cache" );

        WeakReference<Proxy> p1Ref = new WeakReference<Proxy>( mb1.proxies().next() );
        checkCondition( p1Ref.get() != null, "no mb1 proxy found" );
        WeakReference<Proxy> p2Ref = new WeakReference<Proxy>( mb2.proxies().next() );
        checkCondition( p2Ref.get() != null, "no mb2 proxy found" );

        WeakReference<MeshObject> obj1_mb1Ref = new WeakReference<MeshObject>( obj1_mb1 );
        WeakReference<MeshObject> obj1_mb2Ref = new WeakReference<MeshObject>( obj1_mb2 );

        obj1_mb1 = null;
        obj1_mb2 = null;
        
        collectGarbage();

        checkCondition( p1Ref.get() == null, "mb1 proxy found" );
        checkCondition( p2Ref.get() == null, "mb2 proxy found" );
        
        checkCondition( obj1_mb1Ref.get() == null, "obj1_mb1 found" );
        checkCondition( obj1_mb2Ref.get() == null, "obj1_mb2 found" );

        checkEquals( mb1MeshStore.size(),  2, "Wrong number of entries in mb1MeshStore" );
        checkEquals( mb1ProxyStore.size(), 1, "Wrong number of entries in mb1ProxyStore" );
        checkEquals( mb2MeshStore.size(),  2, "Wrong number of entries in mb2MeshStore" );
        checkEquals( mb2ProxyStore.size(), 1, "Wrong number of entries in mb2ProxyStore" );

        //
        
        log.info( "Recreating and checking objects" );
        
        obj1_mb1 = mb1.findMeshObjectByIdentifier( obj1Name );
        obj1_mb2 = mb2.findMeshObjectByIdentifier( obj1Name );
        
        checkObject( obj1_mb1, "obj1_mb1 not found" );
        checkObject( obj1_mb2, "obj1_mb2 not found" );

        //
        
        log.info( "checking proxies" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkEquals( mb1MeshStore.size(),  2, "Wrong number of entries in mb1MeshStore" );
        checkEquals( mb1ProxyStore.size(), 1, "Wrong number of entries in mb1ProxyStore" );
        checkEquals( mb2MeshStore.size(),  2, "Wrong number of entries in mb2MeshStore" );
        checkEquals( mb2ProxyStore.size(), 1, "Wrong number of entries in mb2ProxyStore" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreNetMeshBaseTest2 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreNetMeshBaseTest2( args );
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
    public StoreNetMeshBaseTest2(
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
    private static Log log = Log.getLogInstance( StoreNetMeshBaseTest2.class );
}
