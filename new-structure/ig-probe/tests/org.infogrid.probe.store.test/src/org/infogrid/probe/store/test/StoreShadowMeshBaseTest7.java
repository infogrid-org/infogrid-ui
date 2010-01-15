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

package org.infogrid.probe.store.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.DefaultNetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.store.LocalNetStoreMeshBase;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.store.AbstractStoreListener;
import org.infogrid.store.Store;
import org.infogrid.store.StoreValue;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.logging.Log;

/**
 * Tests that LocalStoreNetMeshBase writes the data in the right tables.
 */
public class StoreShadowMeshBaseTest7
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
        log.info( "Creating Stores" );

        IterablePrefixingStore theMeshStore        = IterablePrefixingStore.create( "Mesh",        theSqlStore );
        IterablePrefixingStore theProxyStore       = IterablePrefixingStore.create( "Proxy",       theSqlStore );
        IterablePrefixingStore theShadowStore      = IterablePrefixingStore.create( "Shadow",      theSqlStore );
        IterablePrefixingStore theShadowProxyStore = IterablePrefixingStore.create( "ShadowProxy", theSqlStore );
        
        checkEquals( theMeshStore.size(),        0, "MeshStore not empty" );
        checkEquals( theProxyStore.size(),       0, "ProxyStore not empty" );
        checkEquals( theShadowStore.size(),      0, "ShadowStore not empty" );
        checkEquals( theShadowProxyStore.size(), 0, "ShadowProxyStore not empty" );
        
        AbstractStoreListener listener = new AbstractStoreListener() {
                @Override
                public void putPerformed(
                        Store      store,
                        StoreValue value )
                {
                    log.debug( "Put performed on " + store + " with key: " + value.getKey() );
                }
        };
        
        theMeshStore.addDirectStoreListener( listener );
        theProxyStore.addDirectStoreListener( listener );
        theShadowStore.addDirectStoreListener( listener );
        theShadowProxyStore.addDirectStoreListener( listener );
        
        //
        
        log.info( "Creating MeshBase" );
        
        NetMeshBaseIdentifier             baseIdentifier     = theMeshBaseIdentifierFactory.fromExternalForm(  "http://here.local/" );

        LocalNetStoreMeshBase base = LocalNetStoreMeshBase.create(
                baseIdentifier,
                DefaultNetMeshObjectAccessSpecificationFactory.create(
                        baseIdentifier,
                        theMeshBaseIdentifierFactory ),
                theModelBase,
                null,
                theMeshStore,
                theProxyStore,
                theShadowStore,
                theShadowProxyStore,
                theProbeDirectory,
                exec,
                true,
                rootContext );
        
        checkEquals( theMeshStore.size(),        1, "No home object in MeshStore" );
        checkEquals( theProxyStore.size(),       0, "ProxyStore not empty" );
        checkEquals( theShadowStore.size(),      0, "ShadowStore not empty" );
        checkEquals( theShadowProxyStore.size(), 0, "ShadowProxyStore not empty" );

        //
        
        log.info( "Doing AccessLocally" );
        
        MeshObject found = base.accessLocally( test_NETWORK_IDENTIFIER );
        
        checkObject( found, "Object not found" );
        checkCondition( found.isBlessedBy( TestSubjectArea.AA ), "Not blessed correctly" );

        //
        
        log.info( "Checking stores" );
        
        checkEquals( theMeshStore.size(),        2, "MeshStore content wrong" );
        checkEquals( theProxyStore.size(),       1, "ProxyStore content wrong" );
        checkEquals( theShadowStore.size(),      1, "ShadowStore content wrong" );
        checkEquals( theShadowProxyStore.size(), 1, "ShadowProxyStore content wrong" );
    }
        
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreShadowMeshBaseTest7 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreShadowMeshBaseTest7( args );
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
    public StoreShadowMeshBaseTest7(
            String [] args )
        throws
            Exception
    {
        super( StoreShadowMeshBaseTest7.class );

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                test_NETWORK_IDENTIFIER.toExternalForm(),
                TestApiProbe.class ));
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
    private static Log log = Log.getLogInstance( StoreShadowMeshBaseTest7.class);

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

    /**
     * The NetMeshBaseIdentifier identifying this Probe.
     */
    protected static final NetMeshBaseIdentifier test_NETWORK_IDENTIFIER;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = theMeshBaseIdentifierFactory.fromExternalForm( "test://test_NETWORK_IDENTIFIER.local" );

        } catch( Throwable t ) {
            log.error( t );
        }
        test_NETWORK_IDENTIFIER = temp;
    }

    /**
     * The test Probe.
     */
    public static class TestApiProbe
            implements
                ApiProbe
    {
        public void readFromApi(
                NetMeshBaseIdentifier  networkId,
                CoherenceSpecification coherence,
                StagingMeshBase        mb )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                EntityNotBlessedException,
                RelatedAlreadyException,
                NotRelatedException,
                RoleTypeBlessedAlreadyException,
                MeshObjectIdentifierNotUniqueException,
                IllegalPropertyTypeException,
                IllegalPropertyValueException,
                TransactionException,
                NotPermittedException,
                ProbeException,
                IOException,
                ModuleException,
                URISyntaxException
        {
            mb.getHomeObject().bless( TestSubjectArea.AA );
        }
    }
}
