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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.probe.TEST;

import java.net.URISyntaxException;
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
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.manager.ScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.logging.Log;

/**
 * Tests the starting and stopping of Shadows.
 */
public class ShadowTest9
        extends
            AbstractShadowTest
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
        long period = 3000L;
        long safety = 1000L;

        ScheduledExecutorProbeManager probeMgr = (ScheduledExecutorProbeManager) base.getProbeManager();

        log.info( "Accessing Probe first time" );

        MeshObject home = base.accessLocally( TEST_URL, new CoherenceSpecification.Periodic( period, true ));

        checkObject( home, "a1 not there" );

        ShadowMeshBase shadow = base.getShadowMeshBaseFor( TEST_URL );

        checkEquals( home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 1L ), "wrong number of Probe runs" );
        checkObject( home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_NEXTPROBERUN ), "No next probe run scheduled" );

        startClock();

        //

        log.info( "Checking for second Probe run" );

        sleepUntil( period + safety );

        checkEquals( home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 2L ), "wrong number of Probe runs" );
        checkObject( home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_NEXTPROBERUN ), "No next probe run scheduled" );

        //

        log.info( "Stopping updates" );

        probeMgr.disableFutureUpdates( shadow );

        sleepUntil( period + period + safety );

        checkEquals( home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 2L ), "wrong number of Probe runs" );
        checkNotObject( home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_NEXTPROBERUN ), "Next probe run falsely scheduled" );

        //

        log.info( "Starting updates" );

        startClock();

        probeMgr.doUpdateNow( shadow );

        sleepUntil( safety );

        checkEquals( home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 3L ), "wrong number of Probe runs" );
        checkObject( home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_NEXTPROBERUN ), "No next probe run scheduled" );


        //

        log.info(  "Checking that automatic running has been re-enabled" );

        sleepUntil( period + safety );

        checkEquals( home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_PROBERUNCOUNTER ), IntegerValue.create( 4L ), "wrong number of Probe runs" );
        checkObject( home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_NEXTPROBERUN ), "No next probe run scheduled" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ShadowTest9 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <test file>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ShadowTest9( args );
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
     * @throws Exception all kinds of things may happen in a test
     */
    public ShadowTest9(
            String [] args )
        throws
            Exception
    {
        super( ShadowTest9.class );

        theProbeDirectory.addExactUrlMatch(
                new ProbeDirectory.ExactMatchDescriptor(
                        TEST_URL.toExternalForm(),
                        TestApiProbe.class ));
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        super.cleanup();

        exec.shutdown();
    }

    // Our Logger
    private static Log log = Log.getLogInstance( ShadowTest9.class );

    /**
     * The URL that we are accessing.
     */
    private static NetMeshBaseIdentifier TEST_URL;

    static {
        try {
            TEST_URL = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://myhost.local/remainder" );

        } catch( Exception ex ) {
            log.error( ex );

            TEST_URL = null; // make compiler happy
        }
    }

    /**
     * The test Probe.
     */
    public static class TestApiProbe
            implements
                ApiProbe
    {
        public void readFromApi(
                NetMeshBaseIdentifier  dataSourceIdentifier,
                CoherenceSpecification coherenceSpecification,
                StagingMeshBase        mb )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                EntityNotBlessedException,
                IllegalPropertyTypeException,
                IllegalPropertyValueException,
                MeshObjectIdentifierNotUniqueException,
                NotPermittedException,
                NotRelatedException,
                RelatedAlreadyException,
                TransactionException,
                TransactionException,
                URISyntaxException,
                RoleTypeBlessedAlreadyException
        {
            MeshObject home = mb.getHomeObject();
            home.bless( TestSubjectArea.AA );
        }
    }
}
