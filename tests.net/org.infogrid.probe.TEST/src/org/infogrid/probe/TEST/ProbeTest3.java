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

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.IterableNetMeshBaseDifferencer;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.ChangeSet;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.probe.manager.PassiveProbeManager;
import org.infogrid.probe.manager.m.MPassiveProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.probe.shadow.m.MShadowMeshBaseFactory;
import org.infogrid.util.logging.Log;

/**
 * Tests running Probes manually, tracking the changes of a data source correctly.
 */
public class ProbeTest3
        extends
            AbstractProbeTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may happen during a test
     */
    public void run()
        throws
            Exception
    {
        copyFile( testFile1, testFile0 );

        log.info( "accessing test file 1 with meshBaseA" );
        
        ShadowMeshBase meshBaseA = theProbeManagerA.obtainFor( testFile0Id, CoherenceSpecification.ONE_TIME_ONLY );

            checkObject( meshBaseA, "could not find meshBaseA" );
            checkCondition( meshBaseA.size() > 1, "meshBaseA is empty" );
            meshBaseA.addWeakShadowListener( listenerA );
            dumpMeshBase( meshBaseA, "meshBaseA", log );

        //
        
        log.info( "accessing test file 1 with meshBaseB" );
        
        ShadowMeshBase meshBaseB = theProbeManagerB.obtainFor( testFile0Id, CoherenceSpecification.ONE_TIME_ONLY );

            checkObject( meshBaseB, "could not find meshBaseB" );
            checkCondition( meshBaseB.size() > 1, "meshBaseB is empty" );
            meshBaseB.addWeakShadowListener( listenerB );
            dumpMeshBase( meshBaseB, "meshBaseB", log );

        //
        
        log.info( "diff'ing meshBaseA and meshBaseB -- should be the exact same, we read the same file" );

        IterableNetMeshBaseDifferencer diff_A_B       = new IterableNetMeshBaseDifferencer( meshBaseA );
        ChangeSet                   firstChangeSet = diff_A_B.determineChangeSet( meshBaseB );

            checkEquals( firstChangeSet.size(), 0, "not the same content" );
            if( firstChangeSet.size() > 0 ) {
                dumpChangeSet( firstChangeSet, log );
            }

        //

        copyFile( testFile2, testFile0 );

        log.info( "accessing test file 2 with meshBaseC" );

        ShadowMeshBase meshBaseC = theProbeManagerC.obtainFor( testFile0Id, CoherenceSpecification.ONE_TIME_ONLY );

            checkObject( meshBaseC, "could not find meshBaseC" );
            checkCondition( meshBaseC.size() > 1, "meshBaseC is empty" );
            meshBaseC.addWeakShadowListener( listenerC );
            dumpMeshBase( meshBaseC, "meshBaseC", log );

        //

        log.info( "updating meshBaseB" );

        meshBaseB.doUpdateNow();
            checkCondition( meshBaseB.size() > 1, "meshBaseB is empty" );
            dumpMeshBase( meshBaseB, "meshBaseB", log );

        //

        log.info( "diff'ing meshBaseB and meshBaseC -- now they should be the same again" );

        IterableNetMeshBaseDifferencer diff_B_C        = new IterableNetMeshBaseDifferencer( meshBaseB );
        ChangeSet                   secondChangeSet = diff_B_C.determineChangeSet( meshBaseB );

            checkEquals( secondChangeSet.size(), 0, "not the same content: " + secondChangeSet );
            if( secondChangeSet.size() > 0 ) {
                dumpChangeSet( secondChangeSet, log );
            }

        //

        copyFile( testFile1, testFile0 );

        log.info( "updating meshBaseC" );

        meshBaseC.doUpdateNow();
            checkCondition( meshBaseC.size() > 1, "meshBaseC is empty" );
            dumpMeshBase( meshBaseC, "meshBaseC", log );

        //

        log.info( "diff'ing meshBaseA and meshBaseC" );

        IterableNetMeshBaseDifferencer diff_A_C = new IterableNetMeshBaseDifferencer( meshBaseA );
        ChangeSet thirdChangeSet = diff_A_C.determineChangeSet( meshBaseC );

            checkEquals( thirdChangeSet.size(), 1, "not the same content (except for ProbeUpdateCounter)" );
            if( thirdChangeSet.size() > 0 ) {
                dumpChangeSet( thirdChangeSet, log );
            }

        //

        checkEquals( listenerA.size(), 0, "wrong number of events in listenerA" );
        checkEquals( listenerB.size(), 2, "wrong number of events in listenerB" );
        checkEquals( listenerC.size(), 2, "wrong number of events in listenerC" );

        if( log.isDebugEnabled() ) {

            // log.debug( listenerA.toString() );
            // log.debug( listenerB.toString() );
            // log.debug( listenerC.toString() );
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
        ProbeTest3 test = null;
        try {
            if( args.length != 3 ) {
                System.err.println( "Synopsis: <main test file> <test file 1> <test file 2>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ProbeTest3( args );
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
     * @throws Exception all sorts of things may happen during a test
     */
    public ProbeTest3(
            String [] args )
        throws
            Exception
    {
        super( ProbeTest3.class );

        testFile0   = args[0];
        testFile1   = args[1];
        testFile2   = args[2];

        testFile0Id = theMeshBaseIdentifierFactory.obtain( new File( testFile0 ) );
        testFile1Id = theMeshBaseIdentifierFactory.obtain( new File( testFile1 ) );
        testFile2Id = theMeshBaseIdentifierFactory.obtain( new File( testFile2 ) );

        MPingPongNetMessageEndpointFactory shadowEndpointFactoryA = MPingPongNetMessageEndpointFactory.create( exec );
        MPingPongNetMessageEndpointFactory shadowEndpointFactoryB = MPingPongNetMessageEndpointFactory.create( exec );
        MPingPongNetMessageEndpointFactory shadowEndpointFactoryC = MPingPongNetMessageEndpointFactory.create( exec );

        ShadowMeshBaseFactory theShadowFactoryA = MShadowMeshBaseFactory.create(
                theMeshBaseIdentifierFactory,
                shadowEndpointFactoryA,
                theModelBase,
                theProbeDirectory,
                rootContext );
        ShadowMeshBaseFactory theShadowFactoryB = MShadowMeshBaseFactory.create(
                theMeshBaseIdentifierFactory,
                shadowEndpointFactoryB,
                theModelBase,
                theProbeDirectory,
                rootContext );
        ShadowMeshBaseFactory theShadowFactoryC = MShadowMeshBaseFactory.create(
                theMeshBaseIdentifierFactory,
                shadowEndpointFactoryC,
                theModelBase,
                theProbeDirectory,
                rootContext );
        
        theProbeManagerA = MPassiveProbeManager.create( theShadowFactoryA );
        theProbeManagerB = MPassiveProbeManager.create( theShadowFactoryB );
        theProbeManagerC = MPassiveProbeManager.create( theShadowFactoryC );

        shadowEndpointFactoryA.setNameServer( theProbeManagerA.getNetMeshBaseNameServer() );
        shadowEndpointFactoryB.setNameServer( theProbeManagerB.getNetMeshBaseNameServer() );
        shadowEndpointFactoryC.setNameServer( theProbeManagerC.getNetMeshBaseNameServer() );
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        theProbeManagerA = null;
        theProbeManagerB = null;
        theProbeManagerC = null;
        
        exec.shutdown();
    }

    // Our Logger
    private static Log log = Log.getLogInstance( ProbeTest3.class);

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

    /**
     * File name of the test file in the read position.
     */
    protected String testFile0;

    /**
     * File name of the first test file.
     */
    protected String testFile1;

    /**
     * File name of the second test file.
     */
    protected String testFile2;

    /**
     * The NetworkIdentifer of the test file in the read position.
     */
    protected NetMeshBaseIdentifier testFile0Id;
    
    /**
     * The NetworkIdentifer of the first test file.
     */
    protected NetMeshBaseIdentifier testFile1Id;

    /**
     * The NetworkIdentifer of the second test file.
     */
    protected NetMeshBaseIdentifier testFile2Id;

    /**
     * The ProbeManager that we use for the first Probe.
     */
    protected PassiveProbeManager theProbeManagerA;

    /**
     * The ProbeManager that we use for the second Probe.
     */
    protected PassiveProbeManager theProbeManagerB;

    /**
     * The ProbeManager that we use for the third Probe.
     */
    protected PassiveProbeManager theProbeManagerC;
    
    /**
     * First listener.
     */
    protected ProbeTestShadowListener listenerA = new ProbeTestShadowListener( "A" );
    
    /**
     * Second listener.
     */
    protected ProbeTestShadowListener listenerB = new ProbeTestShadowListener( "B" );
    
    /**
     * Third listener.
     */
    protected ProbeTestShadowListener listenerC = new ProbeTestShadowListener( "C" );
}
