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
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.probe.manager.store.StoreScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.store.StoreShadowMeshBaseFactory;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.logging.Log;

/**
 * Tests that Shadows resume correctly after they have been rebooted. This does not test
 * that the Shadows' Proxies resume correctly, only the Shadow/Probe updates.
 */
public class StoreShadowMeshBaseTest6
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
        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( exec );

        IterablePrefixingStore theShadowStore      = IterablePrefixingStore.create( "Shadow",      theSqlStore );
        IterablePrefixingStore theShadowProxyStore = IterablePrefixingStore.create( "ShadowProxy", theSqlStore );

        NetMeshBaseIdentifier here = theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local" );

        StoreShadowMeshBaseFactory theShadowFactory
                = StoreShadowMeshBaseFactory.create(
                        shadowEndpointFactory,
                        theModelBase,
                        theProbeDirectory,
                        theShadowStore,
                        theShadowProxyStore,
                        rootContext );
        //
        
        log.info( "Setting up ProbeManager1" );
        
        StoreScheduledExecutorProbeManager probeManager1 = StoreScheduledExecutorProbeManager.create( theShadowFactory, theSqlStore );
        shadowEndpointFactory.setNameServer( probeManager1.getNetMeshBaseNameServer() );

        probeManager1.start( exec );

        //

        log.info( "accessing test files with meshBase: " + testFile1Id.toExternalForm() );
        
        copyFile( testFile1a, testFile1 );

        ShadowMeshBase meshBase1 =
                probeManager1.obtainFor(
                    testFile1Id,
                    // CoherenceSpecification.ONE_TIME_ONLY );
                    new CoherenceSpecification.Periodic( 2000L ));
        checkObject( meshBase1, "MeshBase1 not created" );
        
        MeshObject home1 = meshBase1.getHomeObject();
        checkObject( home1, "no home object found" );
        checkCondition( !home1.isBlessedBy( TestSubjectArea.AA ), "Home object 1 incorrectly blessed" );
        
        //
        
        log.info( "Checking that ProbeManager and Shadow go away when not referenced" );

        WeakReference<StoreScheduledExecutorProbeManager> probeManager1Ref = new WeakReference<StoreScheduledExecutorProbeManager>( probeManager1 );
        WeakReference<ShadowMeshBase>                     meshBase1Ref     = new WeakReference<ShadowMeshBase>( meshBase1 );

        probeManager1 = null;
        meshBase1     = null;
        home1         = null;
        shadowEndpointFactory.setNameServer( null );
        
        Thread.sleep( 4000L );
        collectGarbage();

        checkCondition( probeManager1Ref.get() == null, "ProbeManager1 still here, should have been garbage collected" );
        checkCondition( meshBase1Ref.get() == null, "ShadowMeshBase1 still here, should have been garbage collected" );
        
        //
        
        log.info( "Creating new ProbeManager with old data and new feed" );

        copyFile( testFile1b, testFile1 );

        StoreScheduledExecutorProbeManager probeManager2 = StoreScheduledExecutorProbeManager.create( theShadowFactory, theSqlStore );
        shadowEndpointFactory.setNameServer( probeManager2.getNetMeshBaseNameServer() );
        probeManager2.start( exec );

        Thread.sleep( 2500L );

        //
        
        log.info( "Accessing old object again" );

        ShadowMeshBase meshBase2 = probeManager2.get( testFile1Id );
        checkObject( meshBase2, "MeshBase2 not created" );
        
        MeshObject home2 = meshBase2.getHomeObject();
        checkObject( home2, "no home object found" );
        checkCondition( home2.isBlessedBy( TestSubjectArea.AA ), "Home object 2 not blessed" );
    }
        
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreShadowMeshBaseTest6 test = null;
        try {
            if( args.length != 3 ) {
                System.err.println( "Synopsis: <main test file> <test file a> <test file b>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreShadowMeshBaseTest6( args );
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
    public StoreShadowMeshBaseTest6(
            String [] args )
        throws
            Exception
    {
        super( StoreShadowMeshBaseTest6.class );

        testFile1    = args[0];
        testFile1a   = args[1];
        testFile1b   = args[2];

        testFile1Id  = theMeshBaseIdentifierFactory.obtain( new File( testFile1 ) );

        //
        
        log.info( "Deleting old database and creating new database" );

        theSqlStore.initializeHard();
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        exec.shutdown();
    }

    // Our Logger
    private static Log log = Log.getLogInstance( StoreShadowMeshBaseTest6.class);

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
}
