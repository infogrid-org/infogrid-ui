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
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseAccessSpecification;
import org.infogrid.meshbase.net.NetMeshBaseAccessSpecificationFactory;
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
 * Tests multi-hop access to MeshObjects with NetMeshBases that point replicas to the ultimate source.
 * Compare XprisoTest2b.
 */
public class XprisoTest2a
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

        obj1_mb1.relate( obj2_mb1 );

        tx1.commitTransaction();

        checkProxies( obj1_mb1, null, null, null, "obj1_mb1 has proxies" );
        checkProxies( obj2_mb1, null, null, null, "obj2_mb1 has proxies" );

        checkNotObject( obj1_mb1.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj1_mb1" );
        checkNotObject( obj2_mb1.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj2_mb1" );

        //

        log.info( "Accessing obj2 at mb3 via mb2 from mb1" );

        NetMeshBaseAccessSpecificationFactory mbAccessFact = mb3.getNetMeshObjectAccessSpecificationFactory().getNetMeshBaseAccessSpecificationFactory();

        NetMeshObject obj2_mb3 = mb3.accessLocally(
                mb3.getNetMeshObjectAccessSpecificationFactory().obtain(
                        new NetMeshBaseAccessSpecification[] {
                                mbAccessFact.obtain( mb2.getIdentifier(), CoherenceSpecification.ONE_TIME_ONLY ),
                                mbAccessFact.obtain( mb1.getIdentifier(), CoherenceSpecification.ONE_TIME_ONLY )
                        },
                        obj2_mb1.getIdentifier()),
                1000000L ); // long time so we have time to debug
        checkObject( obj2_mb3, "mb3 fails to access obj2." );

        // do not wait, accessLocally is supposed to have this done for us

        log.info( "Checking in mb1 and mb3" );

        checkNotObject( obj1_mb1.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj1_mb1" );
        checkNotObject( obj2_mb1.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj2_mb1" );
        checkEquals( obj2_mb3.getAllRelationshipProxies().length, 1, "wrong relationship proxies in obj2_mb3" );
        checkEquals( obj2_mb3.getAllRelationshipProxies()[0].getPartnerMeshBaseIdentifier(), mb2.getIdentifier(), "wrong relationship proxies in obj2_mb3" );

        checkProxies( obj2_mb1, new NetMeshBase[] { mb2, mb3 }, null, null, "obj2_mb1 has wrong proxies" );
        checkProxies( obj2_mb3, new NetMeshBase[] { mb1 },      mb1,  mb1,  "obj2_mb3 has wrong proxies" );

        checkNotObject( obj1_mb1.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj1_mb1" );
        checkNotObject( obj2_mb1.getAllRelationshipProxies(), "unexpectedly found relationship proxies in obj2_mb1" );
        checkEquals( obj2_mb3.getAllRelationshipProxies().length, 1, "wrong relationship proxies in obj2_mb3" );
        checkEquals( obj2_mb3.getAllRelationshipProxies()[0].getPartnerMeshBaseIdentifier(), mb2.getIdentifier(), "wrong relationship proxies in obj2_mb3" );

        //

        log.info( "Sleeping a bit to allow the cancel lease message to reach mb2." );

        sleepFor( PINGPONG_ROUNDTRIP_DURATION );

        NetMeshObject obj2_mb2 = mb2.findMeshObjectByIdentifier( obj2_mb1.getIdentifier() );

        //

        log.info( "Checking proxies in mb2" );
        
        checkProxies( obj2_mb2, new NetMeshBase[] { mb1 },      mb1,  mb1,  "obj2_mb2 has wrong proxies" );

        checkEquals( obj2_mb2.getAllRelationshipProxies().length, 1, "wrong relationship proxies in obj2_mb2" );
        checkEquals( obj2_mb2.getAllRelationshipProxies()[0].getPartnerMeshBaseIdentifier(), mb1.getIdentifier(), "wrong relationship proxies in obj2_mb2" );

        //

        log.info( "Sleeping a tiny bit to aid in error reporting" );

        sleepFor( 1000L );

        //

        log.info( "Checking that all messages were delivered" );

        if( !theXprisoMessageLogger.allQueuesEmpty()) {
            reportError( "Message logger detects undelivered messages", theXprisoMessageLogger );
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
        XprisoTest2a test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest2a( args );
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
    public XprisoTest2a(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest2a.class );
        
        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactory.setNameServer( theNameServer );

        ProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create( false );
        
        mb1 = NetMMeshBase.create( net1, theModelBase, null, endpointFactory, proxyPolicyFactory, rootContext );
        mb2 = NetMMeshBase.create( net2, theModelBase, null, endpointFactory, proxyPolicyFactory, rootContext );
        mb3 = NetMMeshBase.create( net3, theModelBase, null, endpointFactory, proxyPolicyFactory, rootContext );

        theNameServer.put( mb1.getIdentifier(), mb1 );
        theNameServer.put( mb2.getIdentifier(), mb2 );
        theNameServer.put( mb3.getIdentifier(), mb3 );

        if( log.isDebugEnabled() ) {
            mb1.setXprisoMessageLogger( theXprisoMessageLogger );
            mb2.setXprisoMessageLogger( theXprisoMessageLogger );
            mb3.setXprisoMessageLogger( theXprisoMessageLogger );
        }
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
    protected ScheduledExecutorService exec = createThreadPool( 3 ); // I think we need three
    
    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest2a.class );
}
