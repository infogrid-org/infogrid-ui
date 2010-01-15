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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.util.logging.Log;

/**
 * Tests relationship replication.
 */
public class XprisoTest6
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
        NetMeshBaseLifecycleManager life1 = mb1.getMeshBaseLifecycleManager();

        MyListener obj1_mb2_listener = new MyListener();
        
        //
        
        log.info( "Instantiating objects in mb1" );

        Transaction tx1 = mb1.createTransactionAsap();

        NetMeshObject obj1_mb1 = life1.createMeshObject(
                mb1.getMeshObjectIdentifierFactory().fromExternalForm( "obj1" ),
                TestSubjectArea.AA );
        NetMeshObject obj2_mb1 = life1.createMeshObject(
                mb1.getMeshObjectIdentifierFactory().fromExternalForm( "obj2" ),
                TestSubjectArea.B  );

        tx1.commitTransaction();

        checkProxies( obj1_mb1, null, null, null, "obj1_mb1 has proxies" );
        checkProxies( obj2_mb1, null, null, null, "obj2_mb1 has proxies" );

        //

        log.info( "Accessing objects at mb2 from mb1" );

        NetMeshObject obj1_mb2 = mb2.accessLocally(
                    mb1.getIdentifier(),
                    obj1_mb1.getIdentifier() );

        NetMeshObject obj2_mb2 = mb2.accessLocally(
                    mb1.getIdentifier(),
                    obj2_mb1.getIdentifier() );

        checkObject( obj1_mb2, "object1 not found in mb2" );
        checkObject( obj2_mb2, "object2 not found in mb2" );
        
        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj2_mb1, new NetMeshBase[] { mb2 }, null, null, "obj2_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj2_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj2_mb2 has wrong proxies" );

        checkNotObject( obj1_mb1.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj1_mb1" );
        checkNotObject( obj2_mb1.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj2_mb1" );
        checkNotObject( obj1_mb2.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj1_mb2" );
        checkNotObject( obj2_mb2.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj2_mb2" );

        obj1_mb2.addSoftPropertyChangeListener( obj1_mb2_listener );
        
        //
        
        log.info( "Relating in mb1" );
        
        tx1 = mb1.createTransactionAsap();
        
        obj1_mb1.relateAndBless( TestSubjectArea.R.getSource(), obj2_mb1 );
        
        tx1.commitTransaction();
        
        //
        
        log.info( "Sleeping a bit, then checking that events have propagated" );

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION );
        
        checkEquals( obj1_mb2_listener.theEvents.size(), 2, "Wrong number of events received" );
            // neighbor added, and role added
        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj2_mb1, new NetMeshBase[] { mb2 }, null, null, "obj2_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj2_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj2_mb2 has wrong proxies" );
        
        checkRelationshipProxies( obj1_mb1, obj2_mb1.getIdentifier(), null, "obj1-obj2 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj1_mb1.getIdentifier(), null, "obj2-obj1 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj1_mb2, obj2_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj1-obj2 has wrong relationship proxies in mb2" );
        checkRelationshipProxies( obj2_mb2, obj1_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj2-obj1 has wrong relationship proxies in mb2" );

        //
        
        log.info( "Checking mb2 relationship." );
        
        MeshObjectSet neighbors1_mb2 = obj1_mb2.traverseToNeighborMeshObjects( false );
        MeshObjectSet related1_mb2   = obj1_mb2.traverse( TestSubjectArea.R.getSource() );
        
        checkEquals( neighbors1_mb2.size(), 1, "Object1 in mb2 has wrong number of neighbors" );
        checkEquals( related1_mb2.size(), 1,   "Object1 in mb2 is not related right" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj2_mb1, new NetMeshBase[] { mb2 }, null, null, "obj2_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj2_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj2_mb2 has wrong proxies" );

        checkRelationshipProxies( obj1_mb1, obj2_mb1.getIdentifier(), null, "obj1-obj2 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj1_mb1.getIdentifier(), null, "obj2-obj1 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj1_mb2, obj2_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj1-obj2 has wrong relationship proxies in mb2" );
        checkRelationshipProxies( obj2_mb2, obj1_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj2-obj1 has wrong relationship proxies in mb2" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        XprisoTest6 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest6( args );
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
    public XprisoTest6(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest6.class );
        
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
    protected NetMeshBase mb1;

    /**
     * The second NetMeshBase.
     */
    protected NetMeshBase mb2;

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 2 ); // gotta have two threads
        

    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest6.class );
    
    /**
     * Listener implementation.
     */
    static class MyListener
            implements
                PropertyChangeListener
    {
        /**
         * A property has changed.
         *
         * @param event the vent
         */
        public void propertyChange(
                PropertyChangeEvent event )
        {
            theEvents.add( event );
        }
        
        /**
         * Clear the listener.
         */
        public void clear()
        {
            theEvents.clear();
        }

        /**
         * The collected events.
         */
        protected ArrayList<PropertyChangeEvent> theEvents = new ArrayList<PropertyChangeEvent>();
    }
}
