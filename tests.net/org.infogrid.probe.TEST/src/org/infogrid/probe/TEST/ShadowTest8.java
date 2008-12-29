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
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.MeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.traversal.SequentialCompoundTraversalSpecification;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.util.logging.Log;

/**
 * Tests a multi-step TraversalSpecification with a Shadow. Was reported as a bug.
 */
public class ShadowTest8
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
        NetMeshBaseIdentifier here = theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local/" ); // this is not going to work for communications
        LocalNetMMeshBase     base = LocalNetMMeshBase.create( here, theModelBase, null, theProbeDirectory, exec, rootContext );

        //
        
        log.info( "Accessing probe" );

        MeshObject home = base.accessLocally( TEST_URL, CoherenceSpecification.ONE_TIME_ONLY );

        checkObject( home, "home not there" );
        
        //
        
        log.info( "creating and accessing TraversalSpec" );
        TraversalSpecification spec = SequentialCompoundTraversalSpecification.create( new TraversalSpecification[] {
            TestSubjectArea.R.getSource(),
            TestSubjectArea.S.getDestination(),
            TestSubjectArea.R.getSource()
        } );

        MeshObjectSet set  = home.traverse( spec );
        MeshObjectSet set2 = home.traverse( spec );
        MeshObjectSet set3 = home.traverse( TestSubjectArea.R.getSource() );
        MeshObjectSet set4 = home.traverse( SequentialCompoundTraversalSpecification.create( new TraversalSpecification[] {
            TestSubjectArea.R.getSource(),
            TestSubjectArea.S.getDestination() } ));
        
        checkEquals( set.size(), N1*N2*N3, "Wrong size set" );
        checkNoDuplicates( set.iterator(), true, "Set has duplicates" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ShadowTest8 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no argument>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ShadowTest8( args );
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
    public ShadowTest8(
            String [] args )
        throws
            Exception
    {
        super( ShadowTest8.class );

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
        exec.shutdown();
    }
    
    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    // Our Logger
    private static Log log = Log.getLogInstance( ShadowTest8.class );

    /**
     * Constrants that determine the number of objects created in the probe.
     */
    protected static final int N1 = 5;
    protected static final int N2 = 1;
    protected static final int N3 = 5;
            
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
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

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
                URISyntaxException
        {
            MeshBaseLifecycleManager    life   = mb.getMeshBaseLifecycleManager();
            MeshObjectIdentifierFactory idfact = mb.getMeshObjectIdentifierFactory();

            MeshObject home = mb.getHomeObject();
            home.bless( TestSubjectArea.AA );

            for( int i=0 ; i<N1 ; ++i ) {
                MeshObject a = life.createMeshObject( idfact.fromExternalForm( "a-" + i ), TestSubjectArea.B );
                home.relateAndBless( TestSubjectArea.R.getSource(), a );
                
                for( int j=0 ; j<N2 ; ++j ) {
                    MeshObject b = life.createMeshObject( idfact.fromExternalForm( "b-" + i + "-" + j ), TestSubjectArea.AA );
                    a.relateAndBless( TestSubjectArea.S.getDestination(), b );
                    
                    for( int k=0 ; k<N3 ; ++k ) {
                        MeshObject c = life.createMeshObject( idfact.fromExternalForm( "c-" + i + "-" + j + "-" + k ), TestSubjectArea.B );
                        b.relateAndBless( TestSubjectArea.R.getSource(), c );
                    }
                }
            }
                
        }
    }
}
