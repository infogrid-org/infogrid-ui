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

package org.infogrid.probe.TEST.active;

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
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.mesh.set.active.ActiveMeshObjectSet;
import org.infogrid.mesh.set.active.ActiveMeshObjectSetListener;
import org.infogrid.mesh.set.active.MeshObjectAddedEvent;
import org.infogrid.mesh.set.active.MeshObjectRemovedEvent;
import org.infogrid.mesh.set.active.OrderedActiveMeshObjectSetReorderedEvent;
import org.infogrid.mesh.set.active.m.ActiveMMeshObjectSetFactory;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.logging.Log;

/**
 * Tests that an ActiveMeshObjectSet gets updated correctly based on changes in data source read by a Probe.
 */
public class ShadowEventTest1
        extends
            AbstractShadowEventTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things can go wrong in a test
     */
    public void run()
        throws
            Exception
    {
        NetMeshBaseIdentifier here           = theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local/" ); // this is not going to work for communications
        MProbeDirectory       probeDirectory = MProbeDirectory.create();

        probeDirectory.addExactUrlMatch(
                new ProbeDirectory.ExactMatchDescriptor(
                        TEST1_URL.toExternalForm(),
                        TestApiProbe.class ));
        
        LocalNetMMeshBase base = LocalNetMMeshBase.create(
                here,
                theModelBase,
                null,
                probeDirectory,
                exec,
                rootContext );
        
        MeshObjectSetFactory passiveFactory = base.getMeshObjectSetFactory();
        MeshObjectSetFactory activeFactory  = ActiveMMeshObjectSetFactory.create( NetMeshObject.class, NetMeshObjectIdentifier.class );
        
        //

        log.info( "Running for probeRunCounter: " + probeRunCounter );

        MeshObject home = base.accessLocally( TEST1_URL );

        MeshObjectSet       destPassiveSet = home.traverse( TestSubjectArea.RR.getSource() );
        
        base.setMeshObjectSetFactory( activeFactory );
        
        ActiveMeshObjectSet destActiveSet  = (ActiveMeshObjectSet) home.traverse( TestSubjectArea.RR.getSource() );

        meshObjectAddedCount   = 1; // we have one element already
        meshObjectRemovedCount = 0;
        ActiveMeshObjectSetListener listener = new ActiveMeshObjectSetListener() {
            public void meshObjectAdded(
                    MeshObjectAddedEvent event )
            {
                ++meshObjectAddedCount;
            }
            public void meshObjectRemoved(
                    MeshObjectRemovedEvent event )
            {
                ++meshObjectRemovedCount;
            }
            public void orderedMeshObjectSetReordered(
                    OrderedActiveMeshObjectSetReorderedEvent event )
            {
                // noop
            }
        };
        destActiveSet.addWeakActiveMeshObjectSetListener( listener );

        log.debug( "Found " + destPassiveSet.size() + " objects in passive set" );
        log.debug( "Found " + destActiveSet.size() + " objects in active set" );

        checkEquals( destPassiveSet.size(),  probeRunCounter, "wrong number of ProjectComponents" );
        checkEquals( destActiveSet.size(),   probeRunCounter, "wrong number of ProjectComponents" );
        checkEquals( meshObjectAddedCount,   probeRunCounter, "wrong number of 'added' events" );
        checkEquals( meshObjectRemovedCount, 0,               "wrong number of 'removed' events" );

        //

        ShadowMeshBase shadow = base.getShadowMeshBaseFor( TEST1_URL );

        while( probeRunCounter < nProbeRuns ) {

            log.debug( "Entering loop (" + probeRunCounter + "/" + nProbeRuns + ")" );

            shadow.doUpdateNow();
            
            Thread.sleep( 2500L ); // wait for ping-pong to do its magic

            destPassiveSet = home.traverse( TestSubjectArea.RR.getSource() );

            log.debug( "Found " + destPassiveSet.size() + " objects in passive set" );
            log.debug( "Found " + destActiveSet.size() + " objects in active set" );

            checkEquals( destPassiveSet.size(),  probeRunCounter, "wrong number of ProjectComponents" );
            checkEquals( destActiveSet.size(),   probeRunCounter, "wrong number of ProjectComponents" );
            checkEquals( meshObjectAddedCount,   probeRunCounter, "wrong number of 'added' events" );
            checkEquals( meshObjectRemovedCount, 0,               "wrong number of 'removed' events" );
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
        ShadowEventTest1 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <no args" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ShadowEventTest1( args );
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
     * @throws Exception all sorts of things can go wrong in a test
     */
    public ShadowEventTest1(
            String [] args )
        throws
            Exception
    {
        super( ShadowEventTest1.class );
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
    private static Log log = Log.getLogInstance( ShadowEventTest1.class);

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );
    
    /**
     * The test protocol. In the real world this would be something like "jdbc".
     */
    private static final String PROTOCOL_NAME = "ShadowEventTest1Protocol";

    /**
     * Factory for NetMeshBaseIdentifiers.
     */
    private static final DefaultNetMeshBaseIdentifierFactory theIdentifierFactory
            = DefaultNetMeshBaseIdentifierFactory.create(
                    new DefaultNetMeshBaseIdentifierFactory.Protocol [] {
                            new DefaultNetMeshBaseIdentifierFactory.Protocol(
                                    PROTOCOL_NAME, false )
            } );

    /**
     * The first URL that we are accessing.
     */
    private static NetMeshBaseIdentifier TEST1_URL;

    static {
        try {
            TEST1_URL = theIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://myhost.local/remainder" );

        } catch( Exception ex ) {
            log.error( ex );
            
            TEST1_URL = null; // make compiler happy
        }
    }

    /**
     * The number of probe runs.
     */
    static final int nProbeRuns = 5;

    /**
     * A counter that is incremented every time the Probe is run.
     */
    static volatile int probeRunCounter = 0;

    /**
      * Number of added events received.
      * Counters need to be declared here so inner classes can share it.
      */
    protected int meshObjectAddedCount = 0;

    /**
      * Number of removed events received.
      * Counters need to be declared here so inner classes can share it.
      */
    protected int meshObjectRemovedCount = 0;

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
            log.debug( "Running probe for " + probeRunCounter );

            MeshObject home = mb.getHomeObject();
            home.bless( TestSubjectArea.AA );

            ++probeRunCounter;
            for( int i=0 ; i<probeRunCounter ; ++i ) {

                MeshObject current = mb.getMeshBaseLifecycleManager().createMeshObject(
                        mb.getMeshObjectIdentifierFactory().fromExternalForm( "typeB-" + i ),
                        TestSubjectArea.B );
                home.relateAndBless( TestSubjectArea.RR.getSource(), current );
            }
        }
    }
}
