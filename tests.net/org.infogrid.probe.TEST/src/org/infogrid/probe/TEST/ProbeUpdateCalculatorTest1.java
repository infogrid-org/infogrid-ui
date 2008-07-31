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

package org.infogrid.probe.TEST;

import java.io.IOException;
import java.net.URISyntaxException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.manager.ScheduledExecutorProbeManager;
import org.infogrid.probe.manager.m.MScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.probe.shadow.m.MShadowMeshBaseFactory;
import org.infogrid.util.logging.Log;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.m.MProbeDirectory;

/**
  * Tests the standard ProbeUpdateCalculator implementations.
  */
public class ProbeUpdateCalculatorTest1
    extends
        AbstractProbeTest
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
        testOne(
                new CoherenceSpecification.Periodic( 3000L ),
                ProbeSubjectArea.PERIODICPROBEUPDATESPECIFICATION,
                new long[] { 0, 3000L, 6000L, 9000L, 12000L, 15000L } );

        testOne(
                new CoherenceSpecification.AdaptivePeriodic( 3000L, 10000L, 1.0f ),
                ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION,
                new long[] { 0, 3000L, 6000L, 9000L, 12000L, 15000L } );

        testOne(
                new CoherenceSpecification.AdaptivePeriodic( 3000L, 10000, 1.1f ),
                ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION,
                new long[] {
                    0L,
                    3000L,
                    3000L + (long) (3000f*1.1f),
                    3000L + (long) (3000f*1.1f) + (long) (3000f*1.1f*1.1f),
                    3000L + (long) (3000f*1.1f) + (long) (3000f*1.1f*1.1f) + (long) (3000f*1.1f*1.1f*1.1f),
                    3000L + (long) (3000f*1.1f) + (long) (3000f*1.1f*1.1f) + (long) (3000f*1.1f*1.1f*1.1f) + (long) (3000f*1.1f*1.1f*1.1f*1.1f),
                },
                new long[] {
                    0L,
                    3000L,
                    3000L * 2,
                    3000L * 3,
                    3000L * 4,
                    3000L * 5
                } );
    }

    /**
     * Run one test, behavior is expected to be the same for the NoChange and the WithChange cases.
     */
    protected void testOne(
            CoherenceSpecification coherence,
            EntityType             homeObjectType,
            long []                points )
        throws
            Exception
    {
        testOne( coherence, homeObjectType, points, points );
    }

    /**
     * Run one test, behavior is expected to be different for the NoChange and the WithChange cases.
     */
    protected void testOne(
            CoherenceSpecification coherence,
            EntityType             homeObjectType,
            long []                noChangePoints,
            long []                changePoints )
        throws
            Exception
    {
        theInvokedAt = new ArrayList<Long>();
        
        ScheduledExecutorProbeManager noChangeProbeManager = MScheduledExecutorProbeManager.create( theShadowFactory );
        theShadowEndpointFactory.setNameServer( noChangeProbeManager.getNetMeshBaseNameServer() );
        noChangeProbeManager.start( theExec );

        log.info( "Starting NoChange test for " + coherence );
        startClock();

        ShadowMeshBase meshBase1 = noChangeProbeManager.obtainFor( theUnchangingDataSource, coherence );

        checkEqualsOutOfSequence(
                meshBase1.getHomeObject().getTypes(),
                new EntityType[] { homeObjectType },
                "wrong home object type for unchanging probe" );
        
        Thread.sleep( noChangePoints[ noChangePoints.length-1 ] + 1000L ); // a bit longer than needed
        noChangeProbeManager.remove( theUnchangingDataSource );

        checkInMarginRange( copyIntoNewLongArray( theInvokedAt ), noChangePoints, 500L, 0.1f, getStartTime(), "Out of range" );

        //
        
        theInvokedAt = new ArrayList<Long>();

        ScheduledExecutorProbeManager changeProbeManager = MScheduledExecutorProbeManager.create( theShadowFactory );
        theShadowEndpointFactory.setNameServer( changeProbeManager.getNetMeshBaseNameServer() );
        changeProbeManager.start( theExec );

        log.info( "Starting WithChange test for " + coherence );
        startClock();

        ShadowMeshBase meshBase2 = changeProbeManager.obtainFor( theChangingDataSource, coherence );

        checkEqualsOutOfSequence(
                meshBase2.getHomeObject().getTypes(),
                new EntityType[] { homeObjectType, TestSubjectArea.AA },
                "wrong home object types for changing probe" );

        Thread.sleep( changePoints[ changePoints.length-1 ] + 1000L ); // a bit longer than needed
        changeProbeManager.remove( theChangingDataSource );

        checkInMarginRange( copyIntoNewLongArray( theInvokedAt ), changePoints, 500L, 0.1f, getStartTime(), "Out of range" );

    }

    /**
     * Helper method.
     */
    protected static long [] copyIntoNewLongArray(
            ArrayList<Long> data )
    {
        long [] ret = new long[ data.size() ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = data.get( i );
        }
        return ret;
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ProbeUpdateCalculatorTest1 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <test file>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ProbeUpdateCalculatorTest1( args );
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
      * constructor
      */
    public ProbeUpdateCalculatorTest1(
            String [] args )
        throws
            Exception
    {
        super( ProbeUpdateCalculatorTest1.class );

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                theUnchangingDataSource.toExternalForm(),
                UnchangingProbe.class ));

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                theChangingDataSource.toExternalForm(),
                ChangingProbe.class ));

        theShadowFactory = MShadowMeshBaseFactory.create(
                theModelBase,
                theShadowEndpointFactory,
                theProbeDirectory,
                16000L, // this must be slightly higher, but not much higher, than the maximum test length, so our Shadows go away automatically
                rootContext );
        
    }

    // Our Logger
    private static Log log = Log.getLogInstance( ProbeUpdateCalculatorTest1.class );

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    protected ScheduledExecutorService theExec = Executors.newSingleThreadScheduledExecutor();
    
    protected MPingPongNetMessageEndpointFactory theShadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( theExec );

    protected ShadowMeshBaseFactory theShadowFactory;
    
    protected static ArrayList<Long> theInvokedAt;

    protected NetMeshBaseIdentifier theChangingDataSource   = NetMeshBaseIdentifier.createUnresolvable( "proto://here.local/change" );
    protected NetMeshBaseIdentifier theUnchangingDataSource = NetMeshBaseIdentifier.createUnresolvable( "proto://here.local/nochange" );

    /**
     * The test Probe.
     */
    public static class UnchangingProbe
            implements
                ApiProbe
    {
        /**
         * Read from the API and instantiate corresponding MeshObjects.
         * 
         * @param networkId the NetMeshBaseIdentifier that is being accessed
         * @param coherenceSpecification the type of data coherence that is requested by the application. Probe
         *         implementors may ignore this parameter, letting the Probe framework choose its own policy.
         *         If the Probe chooses to define its own policy (considering or ignoring this parameter), the
         *         Probe must bless the Probe's HomeObject with a subtype of ProbeUpdateSpecification (defined
         *         in the <code>org.infogrid.model.Probe</code>) that reflects the policy.
         * @param mb the StagingMeshBase in which the corresponding MeshObjects are instantiated by the Probe
         * @throws IdentifierNotUniqueException thrown if the Probe developer incorrectly
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
                NetMeshBaseIdentifier      networkId,
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
            long now = System.currentTimeMillis();
            if( log.isDebugEnabled() ) {
                log.debug( this + ".readFromApi() invoked " + now );
            }
            theInvokedAt.add( now );
            return; // do nothing
        }
    }

    /**
     * The test Probe.
     */
    public static class ChangingProbe
            implements
                ApiProbe
    {
        /**
         * Read from the API and instantiate corresponding MeshObjects.
         * 
         * @param networkId the NetMeshBaseIdentifier that is being accessed
         * @param coherenceSpecification the type of data coherence that is requested by the application. Probe
         *         implementors may ignore this parameter, letting the Probe framework choose its own policy.
         *         If the Probe chooses to define its own policy (considering or ignoring this parameter), the
         *         Probe must bless the Probe's HomeObject with a subtype of ProbeUpdateSpecification (defined
         *         in the <code>org.infogrid.model.Probe</code>) that reflects the policy.
         * @param mb the StagingMeshBase in which the corresponding MeshObjects are instantiated by the Probe
         * @throws IdentifierNotUniqueException thrown if the Probe developer incorrectly
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
                NetMeshBaseIdentifier      networkId,
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
            long now = System.currentTimeMillis();
            if( log.isDebugEnabled() ) {
                log.debug( this + ".readFromApi() invoked " + now );
            }

            theInvokedAt.add( now );

            mb.getHomeObject().bless( TestSubjectArea.AA );
            mb.getHomeObject().setPropertyValue( TestSubjectArea.AA_Y, FloatValue.create( System.currentTimeMillis() )); // simple thing that is always different
        }
    }
}
