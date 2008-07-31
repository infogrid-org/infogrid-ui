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
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.store.IterableLocalNetStoreMeshBase;
import org.infogrid.meshbase.net.local.store.LocalNetStoreMeshBase;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.logging.Log;

/**
 * Tests that shadows resume running probes at the right time after a reboot.
 */
public class StoreShadowMeshBaseTest9
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
        //
        
        log.info( "Creating Stores" );

        IterablePrefixingStore theMeshStore        = IterablePrefixingStore.create( "Mesh",        theSqlStore );
        IterablePrefixingStore theProxyStore       = IterablePrefixingStore.create( "Proxy",       theSqlStore );
        IterablePrefixingStore theShadowStore      = IterablePrefixingStore.create( "Shadow",      theSqlStore );
        IterablePrefixingStore theShadowProxyStore = IterablePrefixingStore.create( "ShadowProxy", theSqlStore );
        
        //
        
        log.info( "Creating MeshBase" );
        
        NetMeshBaseIdentifier             baseIdentifier     = NetMeshBaseIdentifier.create(  "http://here.local/" );
        NiceAndTrustingProxyPolicyFactory proxyPolicyFactory = NiceAndTrustingProxyPolicyFactory.create();
        
        IterableLocalNetStoreMeshBase base = IterableLocalNetStoreMeshBase.create(
                baseIdentifier,
                proxyPolicyFactory,
                theModelBase,
                null,
                theMeshStore,
                theProxyStore,
                theShadowStore,
                theShadowProxyStore,
                theProbeDirectory,
                exec,
                100000L, // long time
                rootContext );
        
        checkEquals( base.getAllShadowMeshBases().size(), 0, "Wrong number of shadows" );
        
        startClock();
        
        //
        
        log.info( "Doing AccessLocally" );
        
        NetMeshObject found = base.accessLocally( testFile1Id, new CoherenceSpecification.Periodic( 10000L ));
        
        checkObject( found, "Object not found" );
        checkCondition( found.isBlessedBy( ProbeSubjectArea.PROBEUPDATESPECIFICATION ), "Not blessed correctly" );
        checkEquals( found.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 1 ), "Wrong ProbeRunCounter" );

        //
        
        log.info( "Shutting down the MeshBase" );

        WeakReference<LocalNetStoreMeshBase> baseRef  = new WeakReference<LocalNetStoreMeshBase>( base );
        WeakReference<MeshObject>            foundRef = new WeakReference<MeshObject>( found );

        found         = null;
        base.die();
        base          = null;

        sleepUntilIsGone( baseRef, 20000L, "base still here" );
        
        //
        
        log.info( "Checking that MeshBase is gone" );
        
        checkCondition( baseRef.get()          == null, "MeshBase still here" );
        checkCondition( foundRef.get()         == null, "MeshObject still here" );

        Thread.sleep( 3000L );

        //
        
        log.info( "Re-creating Meshbase" );

        IterableLocalNetStoreMeshBase base2 = IterableLocalNetStoreMeshBase.create(
                baseIdentifier,
                proxyPolicyFactory,
                theModelBase,
                null,
                theMeshStore,
                theProxyStore,
                theShadowStore,
                theShadowProxyStore,
                theProbeDirectory,
                exec,
                100000L, // long time
                rootContext );
        
        NetMeshObject found2 = base2.accessLocally( testFile1Id, (CoherenceSpecification) null );

        checkObject( found2, "Object not found" );

        checkCondition( found2.isBlessedBy( ProbeSubjectArea.PROBEUPDATESPECIFICATION ), "Not blessed correctly" );
        checkEquals( found2.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 1 ), "Wrong ProbeRunCounter" );

        //
        
        log.info( "Waiting a bit for second Probe run and checking again" );
        
        sleepUntil( 14000L );
        checkEquals( found2.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 2 ), "Wrong ProbeRunCounter" );
    }
        
    /*
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreShadowMeshBaseTest9 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <test file 1>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreShadowMeshBaseTest9( args );
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
     * @throws Exception tests can throw all kinds of Exceptions
     */
    public StoreShadowMeshBaseTest9(
            String [] args )
        throws
            Exception
    {
        super( StoreShadowMeshBaseTest9.class );

        collectGarbage();

        testFile1    = args[0];
        testFile1Id  = NetMeshBaseIdentifier.create( new File( testFile1 ) );

        //
        
        log.info( "Deleting old database and creating new database" );
        
        theSqlStore.deleteStore();
        theSqlStore.initialize();
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
    private static Log log = Log.getLogInstance( StoreShadowMeshBaseTest9.class);

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

    /**
     * File name of the first test file.
     */
    protected String testFile1;

    /**
     * The NetworkIdentifer of the first test file.
     */
    protected NetMeshBaseIdentifier testFile1Id;
}
