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
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.probe.manager.store.StoreScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.store.StoreShadowMeshBaseFactory;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.logging.Log;

/**
 * Tests that shadows that are not currently in memory are still being updated, in the right sequence.
 */
public class StoreShadowMeshBaseTest5
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
        copyFile( testFile2a, testFile2 );

        //

        log.info( "accessing test files with meshBase: " + testFile1Id.toExternalForm() + ", " + testFile2Id.toExternalForm() );
        
        ShadowMeshBase meshBase1 = theProbeManager1.obtainFor( testFile1Id, new CoherenceSpecification.Periodic( 8000L ));
        checkObject( meshBase1, "MeshBase1 not created" );
        
        MeshObject home1 = meshBase1.getHomeObject();
        checkObject( home1, "no home object found" );
        checkCondition( !home1.isBlessedBy( TestSubjectArea.AA ), "Home object 1 incorrectly blessed" );
        
        ShadowMeshBase meshBase2 = theProbeManager1.obtainFor( testFile2Id, new CoherenceSpecification.Periodic( 12000L ));
        checkObject( meshBase2, "MeshBase2 not created" );
        
        MeshObject home2 = meshBase2.getHomeObject();
        checkObject( home2, "no home object found" );
        checkCondition( !home2.isBlessedBy( TestSubjectArea.AA ), "Home object 2 incorrectly blessed" );
        
        startClock();

        //
        
        log.info( "Checking that Shadow goes away when not referenced" );

        WeakReference<ShadowMeshBase> meshBase1Ref = new WeakReference<ShadowMeshBase>( meshBase1 );
        WeakReference<ShadowMeshBase> meshBase2Ref = new WeakReference<ShadowMeshBase>( meshBase2 );

        meshBase1 = null;
        home1     = null;
        meshBase2 = null;
        home2     = null;
        
        sleepUntilIsGone( meshBase1Ref, 12000L, "ShadowMeshBase1 still here, should have been garbage collected" );
        sleepUntilIsGone( meshBase2Ref, 12000L, "ShadowMeshBase2 still here, should have been garbage collected" );
        
        copyFile( testFile1b, testFile1 );
        copyFile( testFile2b, testFile2 );

        //

        log.info( "Sleeping and checking that update has been performed on meshBase1, but not meshBase2" );

        sleepUntil( 10000L );
        
        meshBase1 = theProbeManager1.get( testFile1Id );
        checkObject( meshBase1, "MeshBase1 not created" );
        
        home1 = meshBase1.getHomeObject();
        checkObject( home1, "no home object found" );
        checkCondition( home1.isBlessedBy( TestSubjectArea.AA ), "Home object 1 incorrectly not blessed" );

        meshBase2 = theProbeManager1.get( testFile2Id );
        checkObject( meshBase2, "MeshBase2 not created" );
        
        home2 = meshBase2.getHomeObject();
        checkObject( home2, "no home object found" );
        checkCondition( !home2.isBlessedBy( TestSubjectArea.AA ), "Home object 2 incorrectly blessed" );
        
        meshBase1 = null;
        home1     = null;
        meshBase2 = null;
        home2     = null;
        
        //

        log.info( "Sleeping and checking that update has been performed on meshBase2 as well" );

        sleepUntil( 14000L );

        meshBase1 = theProbeManager1.get( testFile1Id );
        checkObject( meshBase1, "MeshBase1 not created" );
        
        home1 = meshBase1.getHomeObject();
        checkObject( home1, "no home object found" );
        checkCondition( home1.isBlessedBy( TestSubjectArea.AA ), "Home object 1 incorrectly not blessed" );

        meshBase2 = theProbeManager1.get( testFile2Id );
        checkObject( meshBase2, "MeshBase2 not created" );
        
        home2 = meshBase2.getHomeObject();
        checkObject( home2, "no home object found" );
        checkCondition( home2.isBlessedBy( TestSubjectArea.AA ), "Home object 2 incorrectly not blessed" );
    }
        
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreShadowMeshBaseTest5 test = null;
        try {
            if( args.length != 6 ) {
                System.err.println( "Synopsis: <main test file 1> <main test file 2> <test file 1a> <test file 1b> <test file 2a> <test file 2b>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreShadowMeshBaseTest5( args );
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
    public StoreShadowMeshBaseTest5(
            String [] args )
        throws
            Exception
    {
        super( StoreShadowMeshBaseTest5.class );

        testFile1    = args[0];
        testFile2    = args[1];
        testFile1a   = args[2];
        testFile2a   = args[3];
        testFile1b   = args[4];
        testFile2b   = args[5];

        testFile1Id    = theMeshBaseIdentifierFactory.obtain( new File( testFile1 ) );
        testFile2Id    = theMeshBaseIdentifierFactory.obtain( new File( testFile2 ) );

        //
        
        log.info( "Deleting old database and creating new database" );

        theSqlStore.initializeHard();
        
        IterablePrefixingStore theShadowStore      = IterablePrefixingStore.create( "Shadow",      theSqlStore );
        IterablePrefixingStore theShadowProxyStore = IterablePrefixingStore.create( "ShadowProxy", theSqlStore );
        
        // 

        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( exec );

        StoreShadowMeshBaseFactory theShadowFactory = StoreShadowMeshBaseFactory.create(
                theMeshBaseIdentifierFactory,
                shadowEndpointFactory,
                theModelBase,
                theProbeDirectory,
                theShadowStore,
                theShadowProxyStore,
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
    private static Log log = Log.getLogInstance( StoreShadowMeshBaseTest5.class);

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

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
     * File name of the first test file.
     */
    protected String testFile2;

    /**
     * File name of the second test file.
     */
    protected String testFile2a;

    /**
     * File name of the second test file.
     */
    protected String testFile2b;

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
