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
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.util.logging.Log;

/**
  * Tests the replication mechanism with chains of NetMeshBases.
  */
public class XprisoTest8
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
        log.info( "Instantiating obj1 in mb1" );

        Transaction   tx_1     = null;
        NetMeshObject obj1_mb1 = null;

        try {
            tx_1 = mb1.createTransactionAsap();

            NetMeshBaseLifecycleManager life1 = mb1.getMeshBaseLifecycleManager();

            obj1_mb1 = life1.createMeshObject(
                    mb1.getMeshObjectIdentifierFactory().fromExternalForm( "obj1" ),
                    TestSubjectArea.AA );
            obj1_mb1.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "This is obj1." ));

        } finally {
            if( tx_1 != null ) {
                tx_1.commitTransaction();
            }
        }
        checkObject( obj1_mb1, "created object not there" );
        checkProxies( obj1_mb1, null, null, null, "obj1_mb1 has proxies" );

        //

        log.info( "Accessing obj1 at mb3 via mb2 via mb1" );

        NetMeshObject obj1_mb3 = mb3.accessLocally(
                mb3.getNetMeshObjectAccessSpecificationFactory().obtain(
                        new NetMeshBaseIdentifier [] {
                                mb2.getIdentifier(),
                                mb1.getIdentifier(),
                        },
                        obj1_mb1.getIdentifier() ));

        checkObject( obj1_mb3, "mb3 fails to access obj1." );
        checkTypesReplication(      obj1_mb1, obj1_mb3, "Types not the same" );
        checkPropertiesReplication( obj1_mb1, obj1_mb3, "Properties not the same" );
        checkNeighborsReplication(  obj1_mb1, obj1_mb3, "Neighbors not the same" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb3, new NetMeshBase[] { mb2 },  mb2,  mb2, "obj1_mb3 has wrong proxies" );

        //

        log.info( "Trying to change value from mb3 should cause DoNotHaveLock Exception" );

        obj1_mb1.setWillGiveUpLock( false );
        
        boolean exceptionThrown = false;
        Transaction tx_3 = null;
        try {
            tx_3 = mb3.createTransactionNow();

            obj1_mb3.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "wrong value" ));

        } catch( NotPermittedException ex ) {
            exceptionThrown = true;
        } finally {
            if( tx_3 != null ) {
                tx_3.commitTransaction();
            }
        }
        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION * 2L );

        checkCondition( exceptionThrown, "DoNotHaveLock exception not thrown" );
        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb3, new NetMeshBase[] { mb2 },  mb2,  mb2, "obj1_mb3 has wrong proxies" );

        //

        log.info( "Trying to change obj1 from mb1" );

        tx_1 = null;
        exceptionThrown = false;
        try {
            tx_1 = mb1.createTransactionNow();

            obj1_mb1.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "right value" ));

        } catch( NotPermittedException ex ) {
            exceptionThrown = true;
        } finally {
            if( tx_1 != null ) {
                tx_1.commitTransaction();
            }
        }
        checkCondition( !exceptionThrown, "DoNotHaveLock exception thrown" );

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION * 2L );

        checkTypesReplication(      obj1_mb1, obj1_mb3, "Types not the same" );
        checkPropertiesReplication( obj1_mb1, obj1_mb3, "Properties not the same" );
        checkNeighborsReplication(  obj1_mb1, obj1_mb3, "Neighbors not the same" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb3, new NetMeshBase[] { mb2 },  mb2,  mb2, "obj1_mb3 has wrong proxies" );

        //

        log.info( "Creating obj2 in mb2" );

        Transaction   tx_2     = null;
        NetMeshObject obj2_mb2 = null;
        try {
            tx_2 = mb2.createTransactionAsap();

            NetMeshBaseLifecycleManager life2 = mb2.getMeshBaseLifecycleManager();

            obj2_mb2 = life2.createMeshObject(
                    mb2.getMeshObjectIdentifierFactory().fromExternalForm( "object2" ),
                    TestSubjectArea.AA );
            obj2_mb2.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "This is a CPO_2." ));
            obj2_mb2.setWillGiveUpLock( false );

        } finally {
            if( tx_2 != null ) {
                tx_2.commitTransaction();
            }
        }
        checkObject( obj2_mb2, "created object not there" );
        checkProxies( obj2_mb2, null, null, null, "obj2_mb2 has wrong proxies" );

        //

        log.info( "Accessing obj2 at mb4 via mb3 via mb2" );

        NetMeshObject obj2_mb4 = mb4.accessLocally(
                mb4.getNetMeshObjectAccessSpecificationFactory().obtain(
                        new NetMeshBaseIdentifier [] {
                                mb3.getIdentifier(),
                                mb2.getIdentifier(),
                        },
                        obj2_mb2.getIdentifier() ));
        
        checkObject( obj2_mb4, "mb4 fails to access Object obj2." );

        checkTypesReplication(      obj2_mb2, obj2_mb4, "Types not the same" );
        checkPropertiesReplication( obj2_mb2, obj2_mb4, "Properties not the same" );
        checkNeighborsReplication(  obj2_mb2, obj2_mb4, "Neighbors not the same" );

        checkProxies( obj2_mb2, new NetMeshBase[] { mb3 }, null, null, "obj2_mb2 has wrong proxies" );
        checkProxies( obj2_mb4, new NetMeshBase[] { mb3 },  mb3,  mb3, "obj2_mb4 has wrong proxies" );

        //

        log.info( "Trying to change obj2 from mb4 -- should not work because mb2 does not give up lock" );

        Transaction tx_4 = null;
        exceptionThrown = false;
        try {
            tx_4 = mb4.createTransactionNow();

            obj2_mb4.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "This is obj2's change 1" ));

        } catch( NotPermittedException ex ) {
            exceptionThrown = true;
        } finally {
            if( tx_4 != null ) {
                tx_4.commitTransaction();
            }
        }
        checkCondition( exceptionThrown, "DoNotHaveLock exception not thrown" );
        checkTypesReplication(      obj2_mb4, obj2_mb2, "Types not the same" );
        checkPropertiesReplication( obj2_mb4, obj2_mb2, "Properties not the same" );
        checkNeighborsReplication(  obj2_mb4, obj2_mb2, "Neighbors not the same" );

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION * 2L );
        
        NetMeshObject obj2_mb3 = mb3.findMeshObjectByIdentifier( obj2_mb4.getIdentifier() );

        checkProxies( obj2_mb2, new NetMeshBase[] { mb3 },      null, null, "obj2_mb2 has wrong proxies" );
        checkProxies( obj2_mb3, new NetMeshBase[] { mb2, mb4 },  mb2,  mb2, "obj2_mb3 has wrong proxies" );
        checkProxies( obj2_mb4, new NetMeshBase[] { mb3 },       mb3,  mb3, "obj2_mb4 has wrong proxies" );

        //

        log.info( "setting giveUpLock to true in mb2, trying to change obj2 from mb4 -- should work now" );

        tx_2 = null;
        try {
            tx_2 = mb2.createTransactionNow();

            obj2_mb2.setWillGiveUpLock( true );

        } catch( Exception ex ) {
            reportError( "cannot set giveUpLock( TRUE ) in mb2", ex );
        } finally {
            if( tx_2 != null ) {
                tx_2.commitTransaction();
            }
        }

        checkProxies( obj2_mb2, new NetMeshBase[] { mb3 },      null, null, "obj2_mb2 has wrong proxies" );
        checkProxies( obj2_mb3, new NetMeshBase[] { mb2, mb4 },  mb2,  mb2, "obj2_mb3 has wrong proxies" );
        checkProxies( obj2_mb4, new NetMeshBase[] { mb3 },       mb3,  mb3, "obj2_mb4 has wrong proxies" );

        obj2_mb3.setWillGiveUpLock( true );
        obj2_mb4.setWillGiveUpLock( true );

        checkProxies( obj2_mb2, new NetMeshBase[] { mb3 },      null, null, "obj2_mb2 has wrong proxies" );
        checkProxies( obj2_mb3, new NetMeshBase[] { mb2, mb4 },  mb2,  mb2, "obj2_mb3 has wrong proxies" );
        checkProxies( obj2_mb4, new NetMeshBase[] { mb3 },       mb3,  mb3, "obj2_mb4 has wrong proxies" );

        tx_4            = null;
        exceptionThrown = false;
        try {
            tx_4 = mb4.createTransactionNow();

            obj2_mb4.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "This is obj2's change 1" ));

        } catch( NotPermittedException ex ) {
            exceptionThrown = true;
        } finally {
            if( tx_4 != null ) {
                tx_4.commitTransaction();
            }
        }
        checkCondition( !exceptionThrown, "DoNotHaveLock exception thrown" );

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION * 4L );
        checkPropertiesReplication( obj2_mb4, obj2_mb2, "attribute change did not propagate" );

        checkProxies( obj2_mb2, new NetMeshBase[] { mb3 },      null,  mb3, "obj2_mb2 has wrong proxies" );
        checkProxies( obj2_mb3, new NetMeshBase[] { mb2, mb4 },  mb2,  mb4, "obj2_mb3 has wrong proxies" );
        checkProxies( obj2_mb4, new NetMeshBase[] { mb3 },       mb3, null, "obj2_mb4 has wrong proxies" );

        //

        log.info( "Trying to change CPO_2 from mb2 again" );

        tx_2            = null;
        exceptionThrown = false;
        try {
            tx_2 = mb2.createTransactionNow();

            obj2_mb2.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "This is obj2's change 2" ));

        } catch( NotPermittedException ex ) {
            exceptionThrown = true;
        } finally {
            if( tx_2 != null ) {
                tx_2.commitTransaction();
            }
        }
        checkCondition( !exceptionThrown, "DoNotHaveLock exception thrown" );

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION * 4L );
        checkPropertiesReplication( obj2_mb4, obj2_mb2, "attribute change did not propagate" );
        
        checkProxies( obj2_mb2, new NetMeshBase[] { mb3 },      null, null, "obj2_mb2 has wrong proxies" );
        checkProxies( obj2_mb3, new NetMeshBase[] { mb2, mb4 },  mb2,  mb2, "obj2_mb3 has wrong proxies" );
        checkProxies( obj2_mb4, new NetMeshBase[] { mb3 },       mb3,  mb3, "obj2_mb4 has wrong proxies" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        XprisoTest8 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest8( args );
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
    public XprisoTest8(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest7_5.class );
        
        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactory.setNameServer( theNameServer );
        
        ProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create( true );

        mb1 = NetMMeshBase.create( net1, theModelBase, null, endpointFactory, proxyPolicyFactory, rootContext );
        mb2 = NetMMeshBase.create( net2, theModelBase, null, endpointFactory, proxyPolicyFactory, rootContext );
        mb3 = NetMMeshBase.create( net3, theModelBase, null, endpointFactory, proxyPolicyFactory, rootContext );
        mb4 = NetMMeshBase.create( net4, theModelBase, null, endpointFactory, proxyPolicyFactory, rootContext );

        theNameServer.put( mb1.getIdentifier(), mb1 );
        theNameServer.put( mb2.getIdentifier(), mb2 );
        theNameServer.put( mb3.getIdentifier(), mb3 );
        theNameServer.put( mb4.getIdentifier(), mb4 );
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
        mb4.die();
        
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
     * The fourth NetMeshBase.
     */
    protected NetMMeshBase mb4;

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 3 ); // I think we need three
    
    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest8.class );
}
