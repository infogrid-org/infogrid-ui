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
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.util.logging.Log;

/**
 * Tests property replication.
 */
public class XprisoTest4
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
        StringValue [] values = new StringValue[] {
            StringValue.create( "abc" ),
            StringValue.create( "def" ),
            StringValue.create( "ghi" ),
        };
        
        NetMeshBaseLifecycleManager    life1 = mb1.getMeshBaseLifecycleManager();
        NetMeshObjectIdentifierFactory fact1 = mb1.getMeshObjectIdentifierFactory();

        //
        
        log.info( "Instantiating object in mb1" );

        Transaction tx1 = mb1.createTransactionAsap();

        NetMeshObject obj_mb1 = life1.createMeshObject( fact1.fromExternalForm( "one" ), TestSubjectArea.AA );
        obj_mb1.setPropertyValue( TestSubjectArea.A_X, values[0] );

        tx1.commitTransaction();

        checkCondition( obj_mb1.getAllProxies() == null, "obj_mb1 has proxies" );

        //

        log.info( "Accessing object at mb2 from mb1" );

        NetMeshObject obj_mb2 = mb2.accessLocally(
                    mb1.getIdentifier(),
                    obj_mb1.getIdentifier() );

        checkObject( obj_mb2, "object not found in mb2" );
        checkEquals( obj_mb2.getPropertyValue( TestSubjectArea.A_X ), obj_mb1.getPropertyValue( TestSubjectArea.A_X ), "Wrong property value" );
        
        checkProxies( obj_mb1, new NetMeshBase[] { mb2 }, null, null, "obj_mb1 has wrong proxies" );
        checkProxies( obj_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj_mb2 has wrong proxies" );

        if( !mb1.getAccessLocallySynchronizer().areAllQueriesComplete() ) {
            reportError( "mb1 has ongoing queries", mb1.getAccessLocallySynchronizer() );
        }
        if( !mb2.getAccessLocallySynchronizer().areAllQueriesComplete() ) {
            reportError( "mb2 has ongoing queries", mb2.getAccessLocallySynchronizer() );
        }

        //
        
        log.info( "Changing property" );
        
        tx1 = mb1.createTransactionAsap();

        obj_mb1.setPropertyValue( TestSubjectArea.A_X, values[1] );

        tx1.commitTransaction();

        checkEquals( obj_mb1.getPropertyValue( TestSubjectArea.A_X ), values[1], "failed to set value" );
        checkProxies( obj_mb1, new NetMeshBase[] { mb2 }, null, null, "obj_mb1 has wrong proxies" );
        checkProxies( obj_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj_mb2 has wrong proxies" );

        //
        
        log.info( "Sleeping a bit, then checking property value has propagated" );

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION );
        
        checkEquals( obj_mb2.getPropertyValue( TestSubjectArea.A_X ), obj_mb1.getPropertyValue( TestSubjectArea.A_X ), "Wrong property value" );
        checkProxies( obj_mb1, new NetMeshBase[] { mb2 }, null, null, "obj_mb1 has wrong proxies" );
        checkProxies( obj_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj_mb2 has wrong proxies" );

        if( !mb1.getAccessLocallySynchronizer().areAllQueriesComplete() ) {
            reportError( "mb1 has ongoing queries", mb1.getAccessLocallySynchronizer() );
        }
        if( !mb2.getAccessLocallySynchronizer().areAllQueriesComplete() ) {
            reportError( "mb2 has ongoing queries", mb2.getAccessLocallySynchronizer() );
        }

        //
        
        log.info( "Now changing property value on the other end" );
        
        Transaction tx2 = mb2.createTransactionAsap();
        
        obj_mb2.setPropertyValue( TestSubjectArea.A_X, values[2] );
        
        tx2.commitTransaction();
        
        checkEquals( obj_mb2.getPropertyValue( TestSubjectArea.A_X ), values[2], "failed to set value" );

        //

        log.info( "Sleeping a bit, then checking property value has propagated" );

        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION );
        
        checkEquals( obj_mb2.getPropertyValue( TestSubjectArea.A_X ), obj_mb1.getPropertyValue( TestSubjectArea.A_X ), "Wrong property value" );
        checkProxies( obj_mb1, new NetMeshBase[] { mb2 }, null, mb2,  "obj_mb1 has wrong proxies" );
        checkProxies( obj_mb2, new NetMeshBase[] { mb1 }, mb1,  null, "obj_mb2 has wrong proxies" );

        if( !mb1.getAccessLocallySynchronizer().areAllQueriesComplete() ) {
            reportError( "mb1 has ongoing queries", mb1.getAccessLocallySynchronizer() );
        }
        if( !mb2.getAccessLocallySynchronizer().areAllQueriesComplete() ) {
            reportError( "mb2 has ongoing queries", mb2.getAccessLocallySynchronizer() );
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
        XprisoTest4 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest4( args );
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
    public XprisoTest4(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest4.class );
        
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
    protected ScheduledExecutorService exec = createThreadPool( 3 ); // gotta have two threads

    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest4.class );
}
