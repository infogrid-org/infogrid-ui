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
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.util.logging.Log;

/**
 * Tests initial replication in a very basic manner.
 */
public class XprisoTest1c
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
        log.info( "Setting up entities" );

        Transaction tx = mb1.createTransactionAsap();

        NetMeshBaseLifecycleManager    life1 = mb1.getMeshBaseLifecycleManager();
        NetMeshObjectIdentifierFactory idf1  = mb1.getMeshObjectIdentifierFactory();

        NetMeshObject obj1_mb1 = life1.createMeshObject( idf1.fromExternalForm( "#1" ), TestSubjectArea.AA );
        obj1_mb1.setPropertyValue( TestSubjectArea.A_X,  StringValue.create( "Object 1" ) );
        obj1_mb1.setPropertyValue( TestSubjectArea.A_XX, TestSubjectArea.A_XX_type.createBlobValue( "This is a (1) very looooong description", BlobValue.TEXT_PLAIN_MIME_TYPE ));

        NetMeshObject obj2_mb1 = life1.createMeshObject( idf1.fromExternalForm( "#2" ), TestSubjectArea.AA );
        obj2_mb1.setPropertyValue( TestSubjectArea.A_X,  StringValue.create( "Object 2" ) );
        obj2_mb1.setPropertyValue( TestSubjectArea.A_XX, TestSubjectArea.A_XX_type.createBlobValue( "This is a (2) very looooong description", BlobValue.TEXT_PLAIN_MIME_TYPE ));

        NetMeshObject obj3_mb1 = life1.createMeshObject( idf1.fromExternalForm( "#3" ), TestSubjectArea.AA );
        obj3_mb1.setPropertyValue( TestSubjectArea.A_X,  StringValue.create( "Object 3" ) );
        obj3_mb1.setPropertyValue( TestSubjectArea.A_XX, TestSubjectArea.A_XX_type.createBlobValue( "This is a (3) very looooong description", BlobValue.TEXT_PLAIN_MIME_TYPE ));

        tx.commitTransaction();

        checkProxies( obj1_mb1, null, null, null, "obj1_mb1 has proxies" );
        checkProxies( obj2_mb1, null, null, null, "obj2_mb1 has proxies" );
        checkProxies( obj3_mb1, null, null, null, "obj3_mb1 has proxies" );
        
        //

        log.info( "Relate objects 1-2, 2-3" );
        tx = mb1.createTransactionAsap();

        obj1_mb1.relateAndBless( TestSubjectArea.AR1A.getSource(), obj2_mb1 );
        obj2_mb1.relateAndBless( TestSubjectArea.AR1A.getSource(), obj3_mb1 );

        tx.commitTransaction();

        checkRelationshipProxies( obj1_mb1, obj2_mb1.getIdentifier(), null, "obj1-obj2 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj1_mb1.getIdentifier(), null, "obj2-obj1 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj3_mb1.getIdentifier(), null, "obj2-obj3 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj3_mb1, obj2_mb1.getIdentifier(), null, "obj3-obj2 has wrong relationship proxies in mb1" );

        //

        log.info( "replicating first object" );
        NetMeshObject obj1_mb2 = mb2.accessLocally(
                mb1.getIdentifier(),
                obj1_mb1.getIdentifier(),
                60000L ); // long for debugging

        log.info( "checking on first replication" );
        checkObject( obj1_mb2, "accessLocally() did not work" );
        checkTypesReplication(      obj1_mb1, obj1_mb2, "accessLocally() types replication didn't work" );
        checkPropertiesReplication( obj1_mb1, obj1_mb2, "accessLocally() properties replication didn't work" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );

        checkProxies( obj2_mb1, null, null, null, "obj1_mb1 has proxies" );
        checkProxies( obj3_mb1, null, null, null, "obj1_mb1 has proxies" );

        checkRelationshipProxies( obj1_mb1, obj2_mb1.getIdentifier(), null, "obj1-obj2 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj1_mb1.getIdentifier(), null, "obj2-obj1 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj3_mb1.getIdentifier(), null, "obj2-obj3 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj3_mb1, obj2_mb1.getIdentifier(), null, "obj3-obj2 has wrong relationship proxies in mb1" );

        checkRelationshipProxies( obj1_mb2, obj2_mb1.getIdentifier(), new NetMeshBase[] { mb1 }, "obj1-obj2 has wrong relationship proxies in mb2" );

        //

        log.info( "traverse from replicated object 1" );
        MeshObjectSet replicaSet = obj1_mb2.traverse( TestSubjectArea.AR1A.getSource() );

        checkEquals( replicaSet.size(), 1, "incorrect number of relationships found" );

        //

        log.info( "checking replicated object 2" );
        NetMeshObject obj2_mb2 = (NetMeshObject) replicaSet.getSingleMember();
        checkTypesReplication(      obj2_mb1, obj2_mb2, "destination MeshObject types replication didn't work" );
        checkPropertiesReplication( obj2_mb1, obj2_mb2, "destination MeshObject properties replication didn't work" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj2_mb1, new NetMeshBase[] { mb2 }, null, null, "obj2_mb1 has wrong proxies" );
        checkProxies( obj2_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj2_mb2 has wrong proxies" );

        checkProxies( obj3_mb1, null, null, null, "obj1_mb1 has proxies" );

        checkRelationshipProxies( obj1_mb1, obj2_mb1.getIdentifier(), null, "obj1-obj2 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj1_mb1.getIdentifier(), null, "obj2-obj1 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj3_mb1.getIdentifier(), null, "obj2-obj3 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj3_mb1, obj2_mb1.getIdentifier(), null, "obj3-obj2 has wrong relationship proxies in mb1" );

        checkRelationshipProxies( obj1_mb2, obj2_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj1-obj2 has wrong relationship proxies in mb2" );
        checkRelationshipProxies( obj2_mb2, obj1_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj2-obj1 has wrong relationship proxies in mb2" );
        checkRelationshipProxies( obj2_mb2, obj3_mb1.getIdentifier(), new NetMeshBase[] { mb1 }, "obj2-obj3 has wrong relationship proxies in mb2" );

        //

        log.info( "traverse from replicated object 2" );
        replicaSet = obj2_mb2.traverse( TestSubjectArea.AR1A.getSource() );

        checkEquals( replicaSet.size(), 1, "incorrect number of relationships found" );

        //

        log.info( "checking replicated object 3" );
        NetMeshObject obj3_mb2 = (NetMeshObject) replicaSet.getSingleMember();
        checkTypesReplication(      obj3_mb1, obj3_mb2, "destination MeshObject types replication didn't work" );
        checkPropertiesReplication( obj3_mb1, obj3_mb2, "destination MeshObject properties replication didn't work" );

        checkProxies( obj1_mb1, new NetMeshBase[] { mb2 }, null, null, "obj1_mb1 has wrong proxies" );
        checkProxies( obj1_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj1_mb2 has wrong proxies" );
        checkProxies( obj2_mb1, new NetMeshBase[] { mb2 }, null, null, "obj2_mb1 has wrong proxies" );
        checkProxies( obj2_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj2_mb2 has wrong proxies" );
        checkProxies( obj3_mb1, new NetMeshBase[] { mb2 }, null, null, "obj3_mb1 has wrong proxies" );
        checkProxies( obj3_mb2, new NetMeshBase[] { mb1 }, mb1,  mb1,  "obj3_mb2 has wrong proxies" );

        checkRelationshipProxies( obj1_mb1, obj2_mb1.getIdentifier(), null, "obj1-obj2 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj1_mb1.getIdentifier(), null, "obj2-obj1 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj2_mb1, obj3_mb1.getIdentifier(), null, "obj2-obj3 has wrong relationship proxies in mb1" );
        checkRelationshipProxies( obj3_mb1, obj2_mb1.getIdentifier(), null, "obj3-obj2 has wrong relationship proxies in mb1" );

        checkRelationshipProxies( obj1_mb2, obj2_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj1-obj2 has wrong relationship proxies in mb2" );
        checkRelationshipProxies( obj2_mb2, obj1_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj2-obj1 has wrong relationship proxies in mb2" );
        checkRelationshipProxies( obj2_mb2, obj3_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj2-obj3 has wrong relationship proxies in mb2" );
        checkRelationshipProxies( obj3_mb2, obj2_mb2.getIdentifier(), new NetMeshBase[] { mb1 }, "obj3-obj2 has wrong relationship proxies in mb2" );

        //

        log.info( "checking neighbors" );
        checkNeighborsReplication( obj1_mb1, obj1_mb2, "replica 1 RPT replication didn't work" );
        checkNeighborsReplication( obj2_mb1, obj2_mb2, "replica 2 RPT replication didn't work" );
        checkNeighborsReplication( obj3_mb1, obj3_mb2, "replica 3 RPT replication didn't work" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        XprisoTest1c test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest1c( args );
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
    public XprisoTest1c(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest1c.class );

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
    protected ScheduledExecutorService exec = createThreadPool( 1 );

    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest1c.class );
}
