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
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.StagingMeshBaseLifecycleManager;
import org.infogrid.util.logging.Log;

/**
 * Tests that all the right events are being generated when relationships to both unresolved and
 * resolved ForwardReferences change.
 */
public class ForwardReferenceTest6
        extends
            AbstractForwardReferenceTest
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
        log.info( "accessing outer probe" );

        MeshObject abc = base.accessLocally( OUTER_URL, CoherenceSpecification.ONE_TIME_ONLY );

        checkObject( abc, "abc not found" );

        //

        int neighborNumber = 1;

        log.info( "Finding ForwardReference" );

        NetMeshObject fwdReference = (NetMeshObject) abc.traverseToNeighborMeshObjects().getSingleMember();
        checkObject( fwdReference, "fwdReference not found" );

        MeshObjectSet fwdReferenceNeighbors = fwdReference.traverseToNeighborMeshObjects();
        checkEquals( fwdReferenceNeighbors.size(), neighborNumber, "Wrong number of neighbors of ForwardReference prior to resolution" );

        //

        log.info( "Waiting for ForwardReference resolution" );

        sleepFor( PINGPONG_ROUNDTRIP_DURATION * 3L );

        MeshObjectSet fwdReferenceNeighbors2 = fwdReference.traverseToNeighborMeshObjects();
        checkEquals( fwdReferenceNeighbors2.size(), neighborNumber, "Wrong number of neighbors of ForwardReference after resolution" );

        //

        if( true ) {
            log.info( "Now run the inner probe again" );

            base.getShadowMeshBaseFor( INNER_URL ).doUpdateNow();

            ++neighborNumber;
            sleepFor( PINGPONG_ROUNDTRIP_DURATION * 3L ); // longer than needed, but this is a test so let's be safe

            MeshObjectSet fwdReferenceNeighbors3 = fwdReference.traverseToNeighborMeshObjects();
            checkEquals( fwdReferenceNeighbors3.size(), neighborNumber, "Wrong number of neighbors of ForwardReference after inner re-run (1)" );

            base.getShadowMeshBaseFor( INNER_URL ).doUpdateNow();

            ++neighborNumber;
            sleepFor( PINGPONG_ROUNDTRIP_DURATION * 3L ); // longer than needed, but this is a test so let's be safe

            MeshObjectSet fwdReferenceNeighbors4 = fwdReference.traverseToNeighborMeshObjects();
            checkEquals( fwdReferenceNeighbors4.size(), neighborNumber, "Wrong number of neighbors of ForwardReference after inner re-run (2)" );
        }

        //

        log.info( "Now run the outer probe again" );

        base.getShadowMeshBaseFor( OUTER_URL ).doUpdateNow();

        ++neighborNumber;
        sleepFor( PINGPONG_ROUNDTRIP_DURATION * 3L ); // longer than needed, but this is a test so let's be safe

        MeshObjectSet fwdReferenceNeighbors5 = fwdReference.traverseToNeighborMeshObjects();
        checkEquals( fwdReferenceNeighbors5.size(), neighborNumber, "Wrong number of neighbors of ForwardReference after outer re-run (1)" );

        base.getShadowMeshBaseFor( OUTER_URL ).doUpdateNow();

        ++neighborNumber;
        sleepFor( PINGPONG_ROUNDTRIP_DURATION * 3L ); // longer than needed, but this is a test so let's be safe

        MeshObjectSet fwdReferenceNeighbors6 = fwdReference.traverseToNeighborMeshObjects();
        checkEquals( fwdReferenceNeighbors6.size(), neighborNumber, "Wrong number of neighbors of ForwardReference after outer re-run (2)" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ForwardReferenceTest6 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no args>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ForwardReferenceTest6( args );
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
     * @throws Exception all sorts of things can go wrong during a test
     */
    public ForwardReferenceTest6(
            String [] args )
        throws
            Exception
    {
        super( ForwardReferenceTest6.class );

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                OUTER_URL.toExternalForm(),
                OuterTestApiProbe.class ));

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                INNER_URL.toExternalForm(),
                InnerTestApiProbe.class ));
    }

    // Our Logger
    private static Log log = Log.getLogInstance( ForwardReferenceTest6.class);

    /**
     * URL for the outer Probe.
     */
    public static final NetMeshBaseIdentifier OUTER_URL;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://some.example.com/outer" );
            // temp = NetMeshBaseIdentifier.create( "=foo" );

        } catch( URISyntaxException ex ) {
            log.error( ex );
        }
        OUTER_URL = temp;
    }

    /**
     * URL for the inner Probe.
     */
    public static final NetMeshBaseIdentifier INNER_URL;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://some.example.com/inner" );

        } catch( URISyntaxException ex ) {
            log.error( ex );
        }
        INNER_URL = temp;
    }

    /**
     * A counter that is incremented every time the OuterTestApiProbe is run.
     */
    static int outerProbeRunCounter = 0;

    /**
     * A counter that is incremented every time the InnerTestApiProbe is run.
     */
    static int innerProbeRunCounter = 0;

    /**
     * The Probe to the outer data feed.
     */
    public static class OuterTestApiProbe
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
            StagingMeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();

            MeshObject home = mb.getHomeObject();

            home.bless( TestSubjectArea.AA );

            MeshObject fwdRef = life.createForwardReference( INNER_URL, TestSubjectArea.A );
            fwdRef.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "forwardreference" ));

            home.relateAndBless( TestSubjectArea.AR1A.getSource(), fwdRef );

            for( int i=0 ; i<outerProbeRunCounter ; ++i ) {
                MeshObject other = life.createMeshObject( mb.getMeshObjectIdentifierFactory().fromExternalForm( "#outer-" + i ), TestSubjectArea.AA );

                fwdRef.relateAndBless( TestSubjectArea.AR1A.getSource(), other );
            }
            ++outerProbeRunCounter;
        }
    }

    /**
     * The Probe to the inner data feed.
     */
    public static class InnerTestApiProbe
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
            StagingMeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();

            MeshObject home = mb.getHomeObject();

            home.bless( TestSubjectArea.AA );
            home.bless( TestSubjectArea.B );

            home.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "resolved" ) );

            for( int i=0 ; i<innerProbeRunCounter ; ++i ) {
                MeshObject other = life.createMeshObject( mb.getMeshObjectIdentifierFactory().fromExternalForm( "#inner-" + i ), TestSubjectArea.AA );

                home.relateAndBless( TestSubjectArea.AR1A.getSource(), other );
            }
            ++innerProbeRunCounter;
        }
    }
}
