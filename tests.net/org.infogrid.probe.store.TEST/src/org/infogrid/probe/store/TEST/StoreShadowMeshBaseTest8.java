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
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.store.IterableLocalNetStoreMeshBase;
import org.infogrid.meshbase.net.local.store.LocalNetStoreMeshBase;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.logging.Log;

/**
 * Tests that shadow updates survive a reboot.
 */
public class StoreShadowMeshBaseTest8
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
                true,
                rootContext );
        
        checkEquals( base.getAllShadowMeshBases().size(), 0, "Wrong number of shadows" );
        
        //
        
        log.info( "Doing AccessLocally" );
        
        NetMeshObject found = base.accessLocally( testFile1Id, new CoherenceSpecification.Periodic( 4000L ));
        
        checkObject( found, "Object not found" );
        checkCondition( !found.isBlessedBy( TestSubjectArea.AA ), "Not blessed correctly" );

        checkEquals( base.getAllShadowMeshBases().size(), 1, "Wrong number of shadows" );

        
        ShadowMeshBase shadow = base.getShadowMeshBaseFor( found.getProxyTowardsHomeReplica().getPartnerMeshBaseIdentifier() );
        checkObject( shadow, "Shadow not found" );

        MeshObjectIdentifier foundIdentifier = found.getIdentifier();

        NetMeshObject foundInShadow = shadow.findMeshObjectByIdentifier( foundIdentifier );
        checkObject( foundInShadow, "Object not found in shadow" );

        checkProxies( found,         new NetMeshBase[] { shadow }, shadow, shadow, "Wrong proxies in main NetMeshBase" );
        checkProxies( foundInShadow, new NetMeshBase[] { base },   null,   null,   "Wrong proxies in shadow" );

        //
        
        log.info( "Shutting down the MeshBase" );

        WeakReference<LocalNetStoreMeshBase> baseRef          = new WeakReference<LocalNetStoreMeshBase>( base );
        WeakReference<MeshObject>            foundRef         = new WeakReference<MeshObject>( found );
        WeakReference<MeshBase>              shadowRef        = new WeakReference<MeshBase>( shadow );
        WeakReference<MeshObject>            foundInShadowRef = new WeakReference<MeshObject>( foundInShadow );

        found         = null;
        shadow        = null;
        foundInShadow = null;
        base.die();
        base          = null;

        Thread.sleep( 4000L );
        collectGarbage();
        
        //
        
        log.info( "Checking that MeshBase is gone" );
        
        checkCondition( baseRef.get()          == null, "MeshBase still here" );
        checkCondition( foundRef.get()         == null, "MeshObject still here" );
        checkCondition( foundInShadowRef.get() == null, "MeshObject still here" );
        checkCondition( shadowRef.get()        == null, "Shadow still here" );

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
                true,
                rootContext );
        
        checkEquals( base2.size(), 2, "Wrong number of MeshObjects found in recreated MeshBase" );
        checkEquals( base2.getAllShadowMeshBases().size(), 1, "Wrong number of shadows" );
        
        NetMeshObject found2 = base2.findMeshObjectByIdentifier( foundIdentifier );
        checkObject( found2, "Object not found" );
        checkCondition( !found2.isBlessedBy( TestSubjectArea.AA ), "Not blessed correctly" );

        ShadowMeshBase shadow2 = base2.getShadowMeshBaseFor( found2.getProxyTowardsHomeReplica().getPartnerMeshBaseIdentifier() );
        checkObject( shadow2, "Shadow not found" );

        NetMeshObject foundInShadow2 = shadow2.findMeshObjectByIdentifier( foundIdentifier );
        checkObject( foundInShadow2, "Object not found in shadow" );
        checkCondition( !foundInShadow2.isBlessedBy( TestSubjectArea.AA ), "Not blessed correctly" );

        checkProxies( found2,         new NetMeshBase[] { shadow2 }, shadow2, shadow2, "Wrong proxies in main NetMeshBase" );
        checkProxies( foundInShadow2, new NetMeshBase[] { base2 },   null,    null,    "Wrong proxies in shadow" );
        
        //
        
        log.info( "Updating data source, waiting, and checking" );
                
        copyFile( testFile1b, testFile1 );
        
        Thread.sleep( 7000L );
        
        checkCondition( found2.isBlessedBy(         TestSubjectArea.AA ), "Not blessed correctly" );
        checkCondition( foundInShadow2.isBlessedBy( TestSubjectArea.AA ), "Not blessed correctly in shadow" );
    }
        
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreShadowMeshBaseTest8 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <main test file 1> <test file 1a> <test file 1b>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreShadowMeshBaseTest8( args );
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
    public StoreShadowMeshBaseTest8(
            String [] args )
        throws
            Exception
    {
        super( StoreShadowMeshBaseTest8.class );

        collectGarbage(); // if I don't put this here, running this test after StoreShadowMeshBaseTest7 will make it fail (FIXME?)

        testFile1    = args[0];
        testFile1a   = args[1];
        testFile1b   = args[2];

        testFile1Id    = NetMeshBaseIdentifier.create( new File( testFile1 ) );

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
    private static Log log = Log.getLogInstance( StoreShadowMeshBaseTest8.class);

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
