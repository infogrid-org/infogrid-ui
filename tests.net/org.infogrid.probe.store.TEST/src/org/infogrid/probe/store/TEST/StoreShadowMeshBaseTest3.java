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

package org.infogrid.probe.store.TEST;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.probe.manager.store.StoreScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.store.StoreShadowMeshBaseFactory;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.logging.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Tests that ProbeManagers resume Shadows correctly with manual trigger
 * after having been swapped out.
 */
public class StoreShadowMeshBaseTest3
        extends
            AbstractStoreProbeTest
            
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
        copyFile( testFile1, testFile0 );

        //

        log.info( "accessing test file with meshBase" );
        
        ShadowMeshBase meshBase1 = theProbeManager1.obtainFor( testFile0Id, CoherenceSpecification.ONE_TIME_ONLY );
        checkObject( meshBase1, "MeshBase1 not created" );
        
        MeshObject home1 = meshBase1.getHomeObject();
        checkObject( home1, "no home object found" );
        checkCondition( home1.isBlessedBy( TestSubjectArea.AA ), "Home object not blessed" );
        checkEquals( home1.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 1 ), "Wrong number of probe runs" );
        
        MeshObject other1 = home1.traverseToNeighborMeshObjects().getSingleMember();
        checkObject( other1, "no other object found" );
        checkCondition( other1.isBlessedBy( TestSubjectArea.B ), "Other object not blessed" );

        //
        
        log.info( "Checking that Shadow goes away when not referenced" );

        WeakReference<ShadowMeshBase> meshBase1Ref = new WeakReference<ShadowMeshBase>( meshBase1 );
        meshBase1 = null;
        home1     = null;
        other1    = null;
        
        Thread.sleep( 12000L );
        collectGarbage();
        
        checkCondition( meshBase1Ref.get() == null, "ShadowMeshBase still here, should have been garbage collected" );
        
        //
        
        log.info( "updating file and recreating MeshBase" );

        copyFile( testFile2, testFile0 );
        
        ShadowMeshBase meshBase2 = theProbeManager1.obtainFor( testFile0Id, CoherenceSpecification.ONE_TIME_ONLY );
        meshBase2.doUpdateNow();

        //
        
        log.info( "Check that changes have propagated" );
        
        MeshObject home2 = meshBase2.getHomeObject();
        checkObject( home2, "no home object found" );
        checkCondition( home2.isBlessedBy( TestSubjectArea.B ), "Home object not blessed" );
        checkEquals( home2.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 2 ), "Wrong number of probe runs" );
        
        MeshObjectSet others2 = home2.traverseToNeighborMeshObjects();
        checkEquals( others2.size(), 2, "wrong number of neighbors found" );
    }
        
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreShadowMeshBaseTest3 test = null;
        try {
            if( args.length != 3 ) {
                System.err.println( "Synopsis: <main test file> <test file 1> <test file 2>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreShadowMeshBaseTest3( args );
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
     * @param args the command-line arguments
     */
    public StoreShadowMeshBaseTest3(
            String [] args )
        throws
            Exception
    {
        super( StoreShadowMeshBaseTest3.class );

        testFile0    = args[0];
        testFile1    = args[1];
        testFile2    = args[2];

        testFile0Id    = NetMeshBaseIdentifier.create( new File( testFile0 ) );
        testFile1Id    = NetMeshBaseIdentifier.create( new File( testFile1 ) );
        testFile2Id    = NetMeshBaseIdentifier.create( new File( testFile2 ) );

        //
        
        log.info( "Deleting old database and creating new database" );
        
        theSqlStore.deleteStore();
        theSqlStore.initialize();

        IterablePrefixingStore theShadowStore      = IterablePrefixingStore.create( "Shadow",      theSqlStore );
        IterablePrefixingStore theShadowProxyStore = IterablePrefixingStore.create( "ShadowProxy", theSqlStore );
        
        // 

        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( exec );

        StoreShadowMeshBaseFactory theShadowFactory = StoreShadowMeshBaseFactory.create(
                theModelBase,
                shadowEndpointFactory,
                theProbeDirectory,
                theShadowStore,
                theShadowProxyStore,
                5500L,
                rootContext );

        theProbeManager1 = StoreScheduledExecutorProbeManager.create( theShadowFactory, theSqlStore );
        shadowEndpointFactory.setNameServer( theProbeManager1.getNetMeshBaseNameServer() );

        theProbeManager1.start( exec );
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        theProbeManager1 = null;

        exec.shutdown();
    }

    // Our Logger
    private static Log log = Log.getLogInstance( StoreShadowMeshBaseTest3.class);

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

    /**
     * File name of the test file in the read position.
     */
    protected String testFile0;

    /**
     * File name of the first test file.
     */
    protected String testFile1;

    /**
     * File name of the second test file.
     */
    protected String testFile2;

    /**
     * The NetworkIdentifer of the test file in the read position.
     */
    protected NetMeshBaseIdentifier testFile0Id;
    
    /**
     * The NetworkIdentifer of the first test file.
     */
    protected NetMeshBaseIdentifier testFile1Id;

    /**
     * The NetworkIdentifer of the second test file.
     */
    protected NetMeshBaseIdentifier testFile2Id;

    /**
     * The ProbeManager.
     */
    protected StoreScheduledExecutorProbeManager theProbeManager1;
}
