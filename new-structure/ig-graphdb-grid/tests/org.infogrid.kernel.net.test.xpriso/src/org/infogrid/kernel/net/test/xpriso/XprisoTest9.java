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

import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.IterableNetMeshBase;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.net.sweeper.UnnecessaryReplicasSweeper;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.util.logging.Log;

/**
  * <p>Tests the expiration part of the replication mechanism.</p>
  *
  * <p>In this test, we first replicate one object, then wait, then replicate another.
  * We check that the right things happen before any expiration, between the first and
  * the second, and after the second.</p>
  */
public class XprisoTest9
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
        
        log.info( "Setting up objects in mb1" );

        Transaction tx = mb1.createTransactionAsap();

        NetMeshBaseLifecycleManager life1 = mb1.getMeshBaseLifecycleManager();

        NetMeshObject obj1_mb1 = life1.createMeshObject(
                mb1.getMeshObjectIdentifierFactory().fromExternalForm( "obj1" ),
                TestSubjectArea.AA );
        obj1_mb1.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "obj1" ));
        obj1_mb1.setWillGiveUpLock( true );

        tx.commitTransaction();

        //

        log.info( "Replicating first object" );

        NetMeshObject obj1_mb2 = mb2.accessLocally(
                mb1.getIdentifier(),
                obj1_mb1.getIdentifier() );

        checkTypesReplication(      obj1_mb1, obj1_mb2, "accessLocally() types replication didn't work" );
        checkPropertiesReplication( obj1_mb1, obj1_mb2, "accessLocally() properties replication didn't work" );
        
        checkEquals( obj1_mb1.getAllProxies().length, 1, "Wrong number of Proxies on obj1_mb1" );
        checkEquals( obj1_mb2.getAllProxies().length, 1, "Wrong number of Proxies on obj1_mb2" );
        
        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 },  mb1,  mb1, "obj1_mb2 has wrong proxies" );

        //

        log.info( "acquiring lock and adding property change listener one replica in mb2" );

        tx = mb2.createTransactionAsap();
        boolean lockAcquired = obj1_mb2.tryToObtainLock();
        checkCondition( lockAcquired, "failed acquiring the lock" );
        
        tx.commitTransaction();

        checkCondition(  obj1_mb2.hasLock(), "does not have lock" );
        checkCondition( !obj1_mb1.hasLock(), "has lock" );
        
        //

        log.info( "counting objects, sweeping, and counting again in mb2" );

        checkEquals( mb2.size(), 2, "wrong number of objects before sweeping" ); // replicated object, plus home-object

        log.info( "Sweeping" );

        mb2.sweepAllNow();

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION );
        
        checkEquals( mb2.size(), 1, "wrong number of objects after sweeping" );

        checkEquals(    obj1_mb1.getAllProxies(), null, "Wrong number of Proxies on obj1_mb1" );
        checkCondition( obj1_mb1.hasLock(), "does not have lock" );
        checkCondition( obj1_mb2.getIsDead(), "obj1_mb2 not dead" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        XprisoTest9 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest9( args );
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
    public XprisoTest9(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest9.class );
        
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
    protected ScheduledExecutorService exec = createThreadPool( 2 ); // I think we need three
        
    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest9.class );
}
