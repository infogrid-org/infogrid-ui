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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.IterableMeshBase;
import org.infogrid.meshbase.IterableMeshBaseDifferencer;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.m.MMeshBase;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.meshbase.transaction.ChangeSet;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.probe.manager.PassiveProbeManager;
import org.infogrid.probe.manager.m.MPassiveProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.probe.shadow.m.MShadowMeshBaseFactory;
import org.infogrid.util.logging.Log;

/**
 * Tests reading file probetest1.xml via the probe framework.
 */
public class ProbeTest1
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
        log.info( "accessing test file with meshBase" );
        
        ShadowMeshBase meshBase1 = theProbeManager1.obtainFor( testFile1Id, CoherenceSpecification.ONE_TIME_ONLY );

        checkObject( meshBase1, "could not find meshBase1" );
        checkCondition( meshBase1.size() > 1, "meshBase1 is empty" );
        dumpMeshBase( meshBase1, "meshBase1", log );

        //

        log.info( "creating the same data independently" );
        
        IterableMeshBase meshBase2 = MMeshBase.create(
                theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://meshBase2" ),
                theModelBase,
                null,
                rootContext );
        MeshBaseLifecycleManager life2 = meshBase2.getMeshBaseLifecycleManager();

        Transaction tx2 = meshBase2.createTransactionNow();
        
        Calendar cal = GregorianCalendar.getInstance( TimeZone.getTimeZone( "GMT" ));

        cal.set( 2007, 1-1, 2, 3, 4, 5 ); // month is -1
        cal.set( Calendar.MILLISECOND, 61 );
        long ts1 = cal.getTimeInMillis();

        cal.set( 1999, 12-1, 13, 14, 15, 16 );
        cal.set( Calendar.MILLISECOND, 178 );
        long ts2 = cal.getTimeInMillis();

        cal.set( Calendar.MILLISECOND, 189 );
        long ts3 = cal.getTimeInMillis();

        cal.set( Calendar.MILLISECOND, 190 );
        long ts4 = cal.getTimeInMillis();

        cal.set( Calendar.YEAR, 2009 );
        cal.set( Calendar.MILLISECOND, 191 );
        long ts5 = cal.getTimeInMillis();
        
        MeshObject objAbc = life2.createMeshObject(
                meshBase2.getMeshObjectIdentifierFactory().fromExternalForm( testFile1Id.getCanonicalForm() + "#abc" ),
                ts1, ts1, ts1, -1L );
        MeshObject objDef = life2.createMeshObject(
                meshBase2.getMeshObjectIdentifierFactory().fromExternalForm( testFile1Id.getCanonicalForm() + "#def" ),
                TestSubjectArea.AA, ts2, ts3, ts4, ts5 );
        objDef.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "My test String." ));
        MeshObject objGhi = life2.createMeshObject(
                meshBase2.getMeshObjectIdentifierFactory().fromExternalForm( testFile1Id.getCanonicalForm() + "#ghi" ));

        objDef.relate( objGhi );
        objGhi.bless( TestSubjectArea.B );

        objDef.blessRelationship( TestSubjectArea.RR.getSource(), objGhi );

        tx2.commitTransaction();

        dumpMeshBase( meshBase2, "meshBase2", log );

        //

        log.info( "diff'ing meshBase1 and meshBase3" );

        IterableMeshBaseDifferencer diff = new IterableMeshBaseDifferencer( meshBase1 );
        ChangeSet changes = diff.determineChangeSet( meshBase2 );

        checkEquals( changes.size(), 2, "more than two changes (2 Home Objects)" );
        if( changes.size() > 1 ) {
            dumpChangeSet( changes, log );
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
        ProbeTest1 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <test file>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ProbeTest1( args );
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
     * @throws Exception all sorts of things may happen during a test
     */
    public ProbeTest1(
            String [] args )
        throws
            Exception
    {
        super( ProbeTest1.class );

        testFile1   = args[0];
        testFile1Id = theMeshBaseIdentifierFactory.obtain( new File( testFile1 ) );

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
    private static Log log = Log.getLogInstance( ProbeTest1.class);

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
    
    /**
     * The ProbeManager that we use for the first Probe.
     */
    protected PassiveProbeManager theProbeManager1;

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();
}
