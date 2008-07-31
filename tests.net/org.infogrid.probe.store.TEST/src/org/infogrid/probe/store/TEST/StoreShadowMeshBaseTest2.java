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

import java.io.IOException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.manager.PassiveProbeManager;
import org.infogrid.probe.manager.store.StorePassiveProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.store.StoreShadowMeshBaseFactory;
import org.infogrid.store.prefixing.IterablePrefixingStore;
import org.infogrid.util.logging.Log;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ProbeException;

/**
 * Tests ProbeManager restore of StoreShadowMeshBase with a Periodic CoherenceSpecification.
 */
public class StoreShadowMeshBaseTest2
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

        log.info( "accessing test file with meshBase" );
        
        ShadowMeshBase meshBase1 = theProbeManager1.obtainFor( TEST_NETWORK_IDENTIFIER, new CoherenceSpecification.Periodic( 20000L ));
        checkObject( meshBase1, "MeshBase1 not created" );
        
        MeshObject home1 = meshBase1.getHomeObject();
        checkObject( home1, "no home object found" );
        checkCondition( home1.isBlessedBy( TestSubjectArea.AA ), "Home object not blessed" );
        checkEquals( home1.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 1 ), "Wrong number of probe runs" );
        
        MeshObject other1 = home1.traverseToNeighborMeshObjects().getSingleMember();
        checkObject( other1, "no other object found" );
        checkCondition( other1.isBlessedBy( TestSubjectArea.B ), "Other object not blessed" );

        checkEquals( probeRunCounter, 1, "Probe run wrong number of times" );
        
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
        
        log.info( "Checking that the Shadow gets transparently re-created" );
        
        ShadowMeshBase meshBase2 = theProbeManager1.get( TEST_NETWORK_IDENTIFIER );
        checkObject( meshBase2, "MeshBase2 not re-created" );
        
        MeshObject home2 = meshBase2.getHomeObject();
        checkObject( home2, "no home object found" );
        checkCondition( home2.isBlessedBy( TestSubjectArea.AA ), "Home object not blessed" );
        checkEquals( home2.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 1 ), "Wrong number of probe runs" );
        
        MeshObject other2 = home2.traverseToNeighborMeshObjects().getSingleMember();
        checkObject( other2, "no other object found" );
        checkCondition( other2.isBlessedBy( TestSubjectArea.B ), "Other object not blessed" );
        
        checkEquals( probeRunCounter, 1, "Probe run wrong number of times" ); // this proves that it was recreated from disk
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreShadowMeshBaseTest2 test = null;
        try {
            if( args.length < 1 ) {
                System.err.println( "Synopsis: <none>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreShadowMeshBaseTest2( args );
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
      */
    public StoreShadowMeshBaseTest2(
            String [] args )
        throws
            Exception
    {
        super( StoreShadowMeshBaseTest2.class );

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                TEST_NETWORK_IDENTIFIER.toExternalForm(),
                TestApiProbe.class ));

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
        
        theProbeManager1 = StorePassiveProbeManager.create( theShadowFactory, theShadowStore );
        shadowEndpointFactory.setNameServer( theProbeManager1.getNetMeshBaseNameServer() );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( StoreShadowMeshBaseTest1.class );

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

    /**
     * The NetMeshBaseIdentifier identifying this Probe.
     */
    protected static final NetMeshBaseIdentifier TEST_NETWORK_IDENTIFIER;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = NetMeshBaseIdentifier.createUnresolvable( "TEST://example.local" );

        } catch( Throwable t ) {
            log.error( t );
        }
        TEST_NETWORK_IDENTIFIER = temp;
    }

    /**
     * The ProbeManager that we use for the first Probe.
     */
    protected PassiveProbeManager theProbeManager1;

    /**
     * Counter for probe runs.
     */
    protected static int probeRunCounter = 0;
    
    /**
     * The test Probe.
     */
    public static class TestApiProbe
            implements
                ApiProbe
    {
        /**
         * Read from the API and instantiate corresponding MeshObjects.
         * 
         * @param networkId the NetMeshBaseIdentifier that is being accessed
         * @param coherence the type of data coherence that is requested by the application. Probe
         *         implementors may ignore this parameter, letting the Probe framework choose its own policy.
         *         If the Probe chooses to define its own policy (considering or ignoring this parameter), the
         *         Probe must bless the Probe's HomeObject with a subtype of ProbeUpdateSpecification (defined
         *         in the <code>org.infogrid.model.Probe</code>) that reflects the policy.
         * @param mb the StagingMeshBase in which the corresponding MeshObjects are instantiated by the Probe
         * @throws IdeMeshObjectIdentifierNotUniqueExceptionrown if the Probe developer incorrectly
         *         assigned duplicate Identifiers to created MeshObjects
         * @throws RelatedAlreadyException thrown if the Probe developer incorrectly attempted to
         *         relate two already-related MeshObjects
         * @throws TransactionException this Exception is declared to make programming easier,
         *         although actually throwing it would be a programming error
         * @throws NotPermittedException thrown if an operation performed by the Probe was not permitted
         * @throws ProbeException a Probe error occurred per the possible subclasses defined in ProbeException
         * @throws IOException an input/output error occurred during execution of the Probe
         * @throws ModuleException thrown if a Module required by the Probe could not be loaded
         */
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
            ++probeRunCounter;

            MeshObject home = mb.getHomeObject();
            home.bless( TestSubjectArea.AA );
            home.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "A_X" ));
            
            MeshObject other = mb.getMeshBaseLifecycleManager().createMeshObject(
                    mb.getMeshObjectIdentifierFactory().fromExternalForm( "other" ),
                    TestSubjectArea.B );
            other.setPropertyValue( TestSubjectArea.B_U, StringValue.create( "B_U" ));
            
            home.relateAndBless( TestSubjectArea.R.getSource(), other );
        }
    }
}