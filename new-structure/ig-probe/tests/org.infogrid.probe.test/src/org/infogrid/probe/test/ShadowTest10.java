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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.probe.test;

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
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Tests that NetMeshBase.freshenNow works.
 */
public class ShadowTest10
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
        NetMeshObjectIdentifier a1Id    = base.getMeshObjectIdentifierFactory().fromExternalForm( test_URL1, "a" );
        NetMeshObjectIdentifier a2Id    = base.getMeshObjectIdentifierFactory().fromExternalForm( test_URL2, "a" );
        NetMeshObjectIdentifier a3Id    = base.getMeshObjectIdentifierFactory().fromExternalForm( test_URL3, "a" );
        NetMeshObjectIdentifier localId = base.getMeshObjectIdentifierFactory().fromExternalForm( "local" );

        //

        log.info( "Creating local object" );

        Transaction tx = base.createTransactionAsap();

        MeshObject local = base.getMeshBaseLifecycleManager().createMeshObject( localId );

        tx.commitTransaction();

        //

        log.info( "Accessing probes first time" );

        probeFlag = 100;

        NetMeshObject a1 = base.accessLocally( test_URL1, a1Id, CoherenceSpecification.ONE_TIME_ONLY );
        checkObject( a1, "a1 not there" );
        NetMeshObject a2 = base.accessLocally( test_URL2, a2Id, CoherenceSpecification.ONE_TIME_ONLY );
        checkObject( a2, "a2 not there" );
        NetMeshObject a3 = base.accessLocally( test_URL3, a3Id, CoherenceSpecification.ONE_TIME_ONLY );
        checkObject( a3, "a3 not there" );

        //

        log.info( "Relating objects and checking" );

        tx = base.createTransactionAsap();

        local.relate( a1 );
        local.relate( a2 );
        local.relate( a3 );

        tx.commitTransaction();

        checkEquals( a1.getPropertyValue( TestSubjectArea.PROPERTYTEST_WHATAINTEGERDATATYPE ), IntegerValue.create( 100 ), "Wrong flag" );
        checkEquals( a2.getPropertyValue( TestSubjectArea.PROPERTYTEST_WHATAINTEGERDATATYPE ), IntegerValue.create( 100 ), "Wrong flag" );
        checkEquals( a3.getPropertyValue( TestSubjectArea.PROPERTYTEST_WHATAINTEGERDATATYPE ), IntegerValue.create( 100 ), "Wrong flag" );

        //

        log.info( "Freshening" );

        probeFlag = 200;

        base.freshenNow( new NetMeshObject[] { a1, a3 } ); // but not a2
        // do not wait, this needs to be freshening immediately


        //

        log.info( "Checking" );

        checkEquals( a1.getPropertyValue( TestSubjectArea.PROPERTYTEST_WHATAINTEGERDATATYPE ), IntegerValue.create( 200 ), "Wrong flag" );
        checkEquals( a2.getPropertyValue( TestSubjectArea.PROPERTYTEST_WHATAINTEGERDATATYPE ), IntegerValue.create( 100 ), "Wrong flag" );
        checkEquals( a1.getPropertyValue( TestSubjectArea.PROPERTYTEST_WHATAINTEGERDATATYPE ), IntegerValue.create( 200 ), "Wrong flag" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ShadowTest10 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no argument>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ShadowTest10( args );
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
    public ShadowTest10(
            String [] args )
        throws
            Exception
    {
        super( ShadowTest10.class );

        theProbeDirectory.addExactUrlMatch(
                new ProbeDirectory.ExactMatchDescriptor(
                        test_URL1.toExternalForm(),
                        TestApiProbe.class ));
        theProbeDirectory.addExactUrlMatch(
                new ProbeDirectory.ExactMatchDescriptor(
                        test_URL2.toExternalForm(),
                        TestApiProbe.class ));
        theProbeDirectory.addExactUrlMatch(
                new ProbeDirectory.ExactMatchDescriptor(
                        test_URL3.toExternalForm(),
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
    private static Log log = Log.getLogInstance( ShadowTest4.class );

    /**
     * The URLs that we are accessing.
     */
    private static NetMeshBaseIdentifier test_URL1;
    private static NetMeshBaseIdentifier test_URL2;
    private static NetMeshBaseIdentifier test_URL3;

    static {
        try {
            test_URL1 = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://myhost.local/remainder1" );
            test_URL2 = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://myhost.local/remainder2" );
            test_URL3 = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://myhost.local/remainder3" );

        } catch( Exception ex ) {
            log.error( ex );

            test_URL1 = null; // make compiler happy
            test_URL2 = null; // make compiler happy
            test_URL3 = null; // make compiler happy
        }
    }

    /**
     * Distinguishes Probe runs.
     */
    public static int probeFlag = 0;

    /**
     * The test Probe superclass.
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
                StringRepresentationParseException
        {
            MeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();

            MeshObject home = mb.getHomeObject();

            MeshObject a = life.createMeshObject( mb.getMeshObjectIdentifierFactory().fromExternalForm( "a" ), TestSubjectArea.PROPERTYTEST );
            a.setPropertyValue( TestSubjectArea.PROPERTYTEST_WHATAINTEGERDATATYPE, IntegerValue.create( probeFlag ));

            home.relate( a );
        }
    }
}
