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

package org.infogrid.kernel.net.test.xpriso;

import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.IterableNetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.meshbase.net.sweeper.UnnecessaryReplicasSweeper;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;

/**
  * <p>Tests that proxies disappear when not needed any more.
  * FIXME: This test fails today. Not sure that it is an error, however. Fine for now.
  */
public class XprisoTest11
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
        UnnecessaryReplicasSweeper sweep2 = UnnecessaryReplicasSweeper.create( 0L );
        mb2.setSweeper( sweep2 );

        //
        
        log.info( "Setting up object in mb1" );

        Transaction tx = mb1.createTransactionAsap();

        NetMeshBaseLifecycleManager life1 = mb1.getMeshBaseLifecycleManager();

        NetMeshObject obj1_mb1 = life1.createMeshObject(
                mb1.getMeshObjectIdentifierFactory().fromExternalForm( "obj1" ),
                TestSubjectArea.AA );
        obj1_mb1.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "obj1" ));
        obj1_mb1.setWillGiveUpLock( true );

        tx.commitTransaction();

        //

        log.info( "Replicating object" );
        NetMeshObject obj1_mb2 = mb2.accessLocally(
                mb1.getIdentifier(),
                obj1_mb1.getIdentifier() );

        checkTypesReplication(      obj1_mb1, obj1_mb2, "accessLocally() types replication didn't work" );
        checkPropertiesReplication( obj1_mb1, obj1_mb2, "accessLocally() properties replication didn't work" );
        
        checkEquals( obj1_mb1.getAllProxies().length, 1, "Wrong number of Proxies on obj1_mb1" );
        checkEquals( obj1_mb2.getAllProxies().length, 1, "Wrong number of Proxies on obj1_mb2" );
        checkObject( obj1_mb1.findProxyTowards( mb2.getIdentifier() ), "Proxy MB1->MB2 not found" );
        checkObject( obj1_mb2.findProxyTowards( mb1.getIdentifier() ), "Proxy MB2->MB1 not found" );

        //

        log.info( "acquiring lock for replica in mb2" );

        tx = mb2.createTransactionAsap();
        boolean lockAcquired = obj1_mb2.tryToObtainLock();
        checkCondition( lockAcquired, "failed acquiring the lock" );
        tx.commitTransaction();
        tx = null;

        //

        log.info( "counting objects, sweeping, and counting again in mb2" );

        checkEquals( mb2.size(), 2, "wrong number of objects before sweeping" ); // replicated object, plus home-object

        log.info( "Sweeping" );

        mb2.sweepAllNow();

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION );
        
        checkEquals( mb2.size(), 1, "wrong number of objects after sweeping" );

        checkEquals(    obj1_mb1.getAllProxies(), null, "Wrong number of Proxies on obj1_mb1" );
        checkCondition( obj1_mb1.hasLock(), "obj1_mb1 did not re-acquire lock" );
        checkCondition( obj1_mb2.getIsDead(), "obj1_mb2 not dead" );

        obj1_mb2 = null;
        collectGarbage();

        //

        log.info( "Make sure there are no more proxies around" );

        CursorIterator<Proxy> proxies = mb1.proxies();
        checkCondition( !proxies.hasNext(), "mb1 still has proxies" );

        if( log.isInfoEnabled() ) {
            Iterator<Proxy> piter = mb1.proxies();
            while( piter.hasNext() ) {
                Proxy current = piter.next();
                log.info( "found mb1 proxy: " + current );
            }
        }

        proxies = mb2.proxies();
        checkCondition( !proxies.hasNext(), "mb2 still has proxies" );

        if( log.isInfoEnabled() ) {
            Iterator<Proxy> piter = mb2.proxies();
            while( piter.hasNext() ) {
                Proxy current = piter.next();
                log.info( "found mb2 proxy: " + current );
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
        XprisoTest11 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest11( args );
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
    public XprisoTest11(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest10.class );
        
        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactory.setNameServer( theNameServer );

        mb1 = NetMMeshBase.create( net1, theModelBase, null, endpointFactory, rootContext );
        mb2 = NetMMeshBase.create( net2, theModelBase, null, endpointFactory, rootContext );

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
    protected IterableNetMeshBase mb1;

    /**
     * The second NetMeshBase.
     */
    protected IterableNetMeshBase mb2;

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 2 );
    
    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest11.class );
}
