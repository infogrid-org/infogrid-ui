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
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessException;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.testharness.util.IteratorElementCounter;
import org.infogrid.util.logging.Log;

/**
  * Tests the ApiProbe mechanism.
  */
public class ShadowTest2
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
        
        
        log.info( "Accessing probe 1" );

        MeshObject home1 = base.accessLocally( TEST1_URL );

        checkObject( home1, "home object 1 not there" );
        checkEquals( home1.getPropertyValue( TestSubjectArea.A_X ), TestApiProbe1.class.getName(), "wrong probe called" );

        Object shadow = base.getShadowMeshBaseFor( TEST1_URL );
        checkObject( shadow, "cannot find shadow" );

        int count = 0;
        for( ShadowMeshBase current : base.getShadowMeshBases() ) {
            ++count;
        }
        checkEquals( count, 1, "Wrong number of shadows" );
        checkEquals( IteratorElementCounter.countIteratorElements( base.proxies() ), 1, "wrong number of proxies" );
        
        MeshObjectSet others1 = home1.traverseToNeighborMeshObjects();

        checkEquals( others1.size(), 2, "wrong number of content objects" );

        //

        log.info( "Accessing probe 2" );

        MeshObject home2 = base.accessLocally( TEST2_URL );

        checkObject( home2, "home object 2 not there" );
        checkEquals( home2.getPropertyValue( TestSubjectArea.A_X ), TestApiProbe2.class.getName(), "wrong probe called" );

        checkEquals( IteratorElementCounter.countIteratorElements( base.proxies() ), 2, "wrong number of proxies" );
        
        MeshObjectSet others2 = home2.traverseToNeighborMeshObjects();

        checkEquals( others2.size(), 2, "wrong number of content objects" );

        //

        log.info( "accessing probe 3" );

        try {
            MeshObject home3 = base.accessLocally( TEST3_URL );

            reportError( "This should have thrown an exception", "returned object is " + home3 );

        } catch( NetMeshObjectAccessException ex ) {
            log.debug( "Has correctly thrown an exception", ex );

            Throwable cause = ex;
            while( cause.getCause() != null ) {
                cause = cause.getCause();
            }
            if( !( cause instanceof ProbeException.EmptyDataSource )) {
                reportError( "wrong type of exception thrown, was: " + cause, ex );
            }
        }
        checkEquals( IteratorElementCounter.countIteratorElements( base.proxies() ), 2, "wrong number of proxies" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ShadowTest2 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <test file>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ShadowTest2( args );
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
    public ShadowTest2(
            String [] args )
        throws
            Exception
    {
        super( ShadowTest2.class );

        theProbeDirectory.addExactUrlMatch(
                new ProbeDirectory.ExactMatchDescriptor(
                        TEST1_URL.toExternalForm(),
                        TestApiProbe1.class ));
        theProbeDirectory.addExactUrlMatch(
                new ProbeDirectory.ExactMatchDescriptor(
                        TEST2_URL.toExternalForm(),
                        TestApiProbe2.class ));
        theProbeDirectory.addExactUrlMatch(
                new ProbeDirectory.ExactMatchDescriptor(
                        TEST3_URL.toExternalForm(),
                        TestApiProbe3.class ));
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
    private static Log log = Log.getLogInstance( ShadowTest2.class );

    /**
     * The first URL that we are accessing.
     */
    private static NetMeshBaseIdentifier TEST1_URL;

    /**
     * The second URL that we are accessing
     */
    private static NetMeshBaseIdentifier TEST2_URL;

    /**
     * The third URL that we are accessing.
     */
    private static NetMeshBaseIdentifier TEST3_URL;

    static {
        try {
            TEST1_URL = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://myhost.local/remainder" );
            TEST2_URL = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://otherhost.local/remainder" );
            TEST3_URL = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://otherhost.local/empty" );

        } catch( Exception ex ) {
            log.error( ex );
            
            TEST1_URL = null; // make compiler happy
            TEST2_URL = null; // make compiler happy
            TEST3_URL = null; // make compiler happy
        }
    }

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

    /**
     * The test Probe superclass.
     */
    public static abstract class TestApiProbe
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
            MeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();

            MeshObject home = mb.getHomeObject();
            home.bless( TestSubjectArea.AA );
            home.setPropertyValue( TestSubjectArea.A_X, StringValue.create( getClass().getName() ));
            
            MeshObject one = life.createMeshObject( TestSubjectArea.B );
            MeshObject two = life.createMeshObject( TestSubjectArea.B );
            
            home.relateAndBless( TestSubjectArea.RR.getSource(), one );
            home.relateAndBless( TestSubjectArea.RR.getSource(), two );
        }
    }

    /**
     * The first test Probe.
     */
    public static class TestApiProbe1
        extends
            TestApiProbe
    {
    }

    /**
     * The second test Probe.
     */
    public static class TestApiProbe2
        extends
            TestApiProbe
    {
    }

    /**
     * The third test Probe. This one will throw EmptyDataSourceException.
     */
    public static class TestApiProbe3
        implements
            ApiProbe
    {
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
            throw new ProbeException.EmptyDataSource( networkId );
        }
    }
}
