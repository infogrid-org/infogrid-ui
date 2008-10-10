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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObject;
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

/**
 * Tests that shadows that are not currently in memory are still being updated.
 */
public class StoreShadowMeshBaseTest4
        extends
            AbstractStoreProbeTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may go wrong in tests
     */
    public void run()
        throws
            Exception
    {
        copyFile( testFile1a, testFile1 );

        //

        log.info( "accessing test files with meshBase: " + testFile1Id.toExternalForm() );
        
        ShadowMeshBase meshBase1 = theProbeManager1.obtainFor(
                    testFile1Id,
                    // CoherenceSpecification.ONE_TIME_ONLY );
                    new CoherenceSpecification.Periodic( 3000L ));
        checkObject( meshBase1, "MeshBase1 not created" );
        
        MeshObject home1 = meshBase1.getHomeObject();
        checkObject( home1, "no home object found" );
        checkCondition( !home1.isBlessedBy( TestSubjectArea.AA ), "Home object 1 incorrectly blessed" );
        checkEquals( home1.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 1 ), "Wrong number of probe runs" );
        
        //
        
        log.info( "Checking that Shadow goes away when not referenced" );

        WeakReference<ShadowMeshBase> meshBase1Ref = new WeakReference<ShadowMeshBase>( meshBase1 );

        meshBase1 = null;
        home1     = null;
        
        Thread.sleep( 4000L );
        collectGarbage();

        checkCondition( meshBase1Ref.get() == null, "ShadowMeshBase1 still here, should have been garbage collected" );
        
        copyFile( testFile1b, testFile1 );

        Thread.sleep( 4000L );

        //
        
        log.info( "Checking that update has been performed" );
        
        ShadowMeshBase meshBase2 = theProbeManager1.get( testFile1Id );
        checkObject( meshBase2, "MeshBase1 not recovered" );
        
        MeshObject home2 = meshBase2.getHomeObject();
        checkObject( home2, "no home object found" );
        checkCondition( home2.isBlessedBy( TestSubjectArea.AA ), "Home object 1 incorrectly not blessed" );
        checkEquals( home2.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 4 ), "Wrong number of probe runs" );
    }
        
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreShadowMeshBaseTest4 test = null;
        try {
            if( args.length != 3 ) {
                System.err.println( "Synopsis: <main test file> <test file a> <test file b>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreShadowMeshBaseTest4( args );
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
     * @throws Exception all sorts of things may go wrong in tests
     */
    public StoreShadowMeshBaseTest4(
            String [] args )
        throws
            Exception
    {
        super( StoreShadowMeshBaseTest4.class );

        testFile1    = args[0];
        testFile1a   = args[1];
        testFile1b   = args[2];

        testFile1Id    = NetMeshBaseIdentifier.create( new File( testFile1 ) );

        //
        
        log.info( "Deleting old database and creating new database" );

        theSqlStore.initializeHard();

        theSqlStore.addDirectStoreListener( theTestStoreListener );
        
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
                100000L, // a long time
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
    private static Log log = Log.getLogInstance( StoreShadowMeshBaseTest4.class);

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

    /**
     * File name of the first test file.
     */
    protected String testFile1;

    /**
     * File name of the first test file.
     */
    protected String testFile1a;

    /**
     * File name of the first test file.
     */
    protected String testFile1b;

    /**
     * The NetworkIdentifer of the first test file.
     */
    protected NetMeshBaseIdentifier testFile1Id;

    /**
     * The ProbeManager.
     */
    protected StoreScheduledExecutorProbeManager theProbeManager1;
    
    /**
     * StoreListener for debugging purposes.
     */
    protected TestStoreListener theTestStoreListener = new TestStoreListener();
}
