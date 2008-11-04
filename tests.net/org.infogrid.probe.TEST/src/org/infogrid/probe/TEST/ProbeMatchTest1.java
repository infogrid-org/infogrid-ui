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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.probe.manager.PassiveProbeManager;
import org.infogrid.probe.manager.m.MPassiveProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.probe.shadow.m.MShadowMeshBaseFactory;
import org.infogrid.util.logging.Log;

/**
 * Tests exact and pattern-based URL matching.
 */
public class ProbeMatchTest1
        extends
            AbstractProbeTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things can go wrong during a test
     */
    public void run()
        throws
            Exception
    {
        log.info( "Configuring ProbeDirectory" );
        
        NetMeshBaseIdentifier id1 = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://foo.com/bar" );
        NetMeshBaseIdentifier id2 = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://foo.com/bar?abc=def" );
        NetMeshBaseIdentifier id3 = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://foo.com/bar?ghi=klm" );

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                id1.toExternalForm(),
                TestProbe1.class ));

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                id2.toExternalForm(),
                TestProbe2.class ));

        theProbeDirectory.addPatternUrlMatch( new ProbeDirectory.PatternMatchDescriptor(
                Pattern.compile( PROTOCOL_NAME + "://foo.com/bar(.*)" ),
                TestProbe3.class ));

        //
        
        log.info( "Running the Probes and checking" );

        ShadowMeshBase meshBase1 = theProbeManager1.obtainFor( id1, CoherenceSpecification.ONE_TIME_ONLY );        
        checkEquals( meshBase1.getHomeObject().getPropertyValue( TestSubjectArea.A_X ), TestProbe1.class.getName(), "Wrong probe invoked for " + id1 );

        ShadowMeshBase meshBase2 = theProbeManager1.obtainFor( id2, CoherenceSpecification.ONE_TIME_ONLY );        
        checkEquals( meshBase2.getHomeObject().getPropertyValue( TestSubjectArea.A_X ), TestProbe2.class.getName(), "Wrong probe invoked for " + id2 );

        ShadowMeshBase meshBase3 = theProbeManager1.obtainFor( id3, CoherenceSpecification.ONE_TIME_ONLY );        
        checkEquals( meshBase3.getHomeObject().getPropertyValue( TestSubjectArea.A_X ), TestProbe3.class.getName(), "Wrong probe invoked for " + id3 );        
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ProbeMatchTest1 test = null;
        try {
            if( args.length > 0) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ProbeMatchTest1( args );
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
     * @throws Exception all sorts of things can go wrong during a test
     */
    public ProbeMatchTest1(
            String [] args )
        throws
            Exception
    {
        super( ProbeMatchTest1.class );

        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( exec );

        ShadowMeshBaseFactory theShadowFactory = MShadowMeshBaseFactory.create(
                theMeshBaseIdentifierFactory,
                shadowEndpointFactory,
                theModelBase,
                theProbeDirectory,
                rootContext );
        
        theProbeManager1 = MPassiveProbeManager.create( theShadowFactory );
        shadowEndpointFactory.setNameServer( theProbeManager1.getNetMeshBaseNameServer() );
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
    private static Log log = Log.getLogInstance( ProbeMatchTest1.class);

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

    /**
     * The ProbeManager that we use for the first Probe.
     */
    protected PassiveProbeManager theProbeManager1;
    
    /**
     * The abstract test Probe. This needs to be declared public otherwise the Probe framework cannot access it.
     */
    public static abstract class AbstractTestProbe
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
            mb.getHomeObject().bless( TestSubjectArea.AA );
            mb.getHomeObject().setPropertyValue( TestSubjectArea.A_X, StringValue.create( getClass().getName() ));
        }
    }

    /**
     * Test Probe 1. This is just renamed from AbstractTestProbe.
     */
    public static class TestProbe1
            extends
                AbstractTestProbe
    {}
    
    /**
     * Test Probe 2. This is just renamed from AbstractTestProbe.
     */
    public static class TestProbe2
            extends
                AbstractTestProbe
    {}

    /**
     * Test Probe 3. This is just renamed from AbstractTestProbe.
     */
    public static class TestProbe3
            extends
                AbstractTestProbe
    {}
}
