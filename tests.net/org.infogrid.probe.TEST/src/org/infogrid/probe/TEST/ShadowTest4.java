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
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Relates a ShadowMeshBase-producted MeshObject A to another MeshObject B outside of the ShadowMeshBase,
 * and re-runs the Probe.
 */
public class ShadowTest4
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
        NetMeshObjectIdentifier aId     = base.getMeshObjectIdentifierFactory().fromExternalForm( TEST_URL, "a" );
        NetMeshObjectIdentifier localId = base.getMeshObjectIdentifierFactory().fromExternalForm( "local" );

        //
        
        log.info( "Creating local object" );
        
        Transaction tx = base.createTransactionAsap();
        
        MeshObject local = base.getMeshBaseLifecycleManager().createMeshObject( localId );
        
        tx.commitTransaction();

        //
        
        log.info( "Accessing probe first time" );
        

        MeshObject a1 = base.accessLocally( TEST_URL, aId, CoherenceSpecification.ONE_TIME_ONLY );

        checkObject( a1, "a1 not there" );
        
        ShadowMeshBase shadow = base.getShadowMeshBaseFor( TEST_URL );
        checkEquals( shadow.size(), 2, "Wrong number of objects in Shadow" );

        //
        
        log.info( "Relating objects and checking" );
        
        tx = base.createTransactionAsap();
        
        local.relate( a1 );
        
        tx.commitTransaction();
        
        checkEquals( local.traverseToNeighborMeshObjects().size(), 1, "neighbor of local not found" );
        checkEquals(    a1.traverseToNeighborMeshObjects().size(), 2, "neighbor of a1 not found" );
        checkEquals( shadow.size(), 2, "Wrong number of objects in Shadow" );
        
        //
        
        log.info( "Running Probe again" );
        
        base.getShadowMeshBaseFor( TEST_URL ).doUpdateNow();
        
        Thread.sleep( PINGPONG_ROUNDTRIP_DURATION );
        
        //
        
        log.info( "Checking" );
        
        checkEquals( shadow.size(), 2, "Wrong number of objects in Shadow" );
        checkEquals( local.traverseToNeighborMeshObjects().size(), 1, "neighbor of local not found" );
        checkEquals(    a1.traverseToNeighborMeshObjects().size(), 2, "neighbor of a1 not found" );
        
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ShadowTest4 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no argument>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ShadowTest4( args );
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
    public ShadowTest4(
            String [] args )
        throws
            Exception
    {
        super( ShadowTest4.class );

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
    private static Log log = Log.getLogInstance( ShadowTest4.class );

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
            home.bless( TestSubjectArea.AA );
            
            MeshObject a = life.createMeshObject( mb.getMeshObjectIdentifierFactory().fromExternalForm( "a" ), TestSubjectArea.B );

            home.relateAndBless( TestSubjectArea.RR.getSource(), a );
        }
    }
}
