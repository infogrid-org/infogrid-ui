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
import org.infogrid.mesh.security.CallerHasInsufficientPermissionsException;
import org.infogrid.mesh.security.PropertyReadOnlyException;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.meshbase.transaction.NotWithinTransactionBoundariesException;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.module.ModuleActivationException;
import org.infogrid.module.ModuleAdvertisementInstantiationException;
import org.infogrid.module.ModuleConfigurationException;
import org.infogrid.module.ModuleException;
import org.infogrid.module.ModuleNotFoundException;
import org.infogrid.module.ModuleResolutionException;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.util.logging.Log;

/**
  * Tests error handling from Probes.
  */
public class ProbeTest5
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
        Class [] expectedExceptionTypes = {
                CallerHasInsufficientPermissionsException.class,
                PropertyReadOnlyException.class,
                IsAbstractException.class,
                MeshObjectIdentifierNotUniqueException.class,
                RelatedAlreadyException.class,
                NotWithinTransactionBoundariesException.class,
                ProbeException.EmptyDataSource.class,
                ProbeException.ErrorInProbe.class,
                ProbeException.IncompleteData.class,
                ProbeException.Other.class,
                ProbeException.SyntaxError.class,
                IOException.class,
                NullPointerException.class,
                ClassCastException.class,
                ModuleException.class
        };                    

        // for( int i=4 ; i<5 ; ++i )
        for( int i=0 ; i<expectedExceptionTypes.length ; ++i ) {
            log.info( "Running test " + i );

            probeRunCounter = i;

            MeshObject obj           = null;
            Throwable  lastException = null; // just there for debugging
            Throwable  lastCause     = null;

            try {
                obj = theMeshBase.accessLocally( TEST_NETWORK_IDENTIFIER );

            } catch( Throwable ex ) {
                lastException = ex;

                lastCause = ex;
                while( lastCause.getCause() != null ) {
                    lastCause = lastCause.getCause();
                }
                
                if( log.isDebugEnabled() ) {
                    log.debug( "Caught exception (type " + ex.getClass() + ") with ultimate cause (type " + lastCause.getClass() + ")" );
                }
            }
            if( lastCause == null ) {
                reportError( "no exception thrown at all" );
            
            } else if( !checkType( lastCause, expectedExceptionTypes[i], "not the right type" ) ) {
                reportError( "last cause is ", lastCause );
            }
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
        ProbeTest5 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <test file>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ProbeTest5( args );
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
    public ProbeTest5(
            String [] args )
        throws
            Exception
    {
        super( ProbeTest5.class );
        
        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                TEST_NETWORK_IDENTIFIER.toExternalForm(),
                TestApiProbe.class ));

        NetMeshBaseIdentifier    here = theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local/" ); // this is not going to work for communications

        theMeshBase = LocalNetMMeshBase.create( here, theModelBase, null, theProbeDirectory, exec, rootContext );
    }

    /**
     * Cleanup.
     */
    @Override
    public void cleanup()
    {
        if( theMeshBase != null ) {
            theMeshBase.die();
        }
        exec.shutdown();
    }

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    /**
     * The NetMeshBase to be tested.
     */
    protected LocalNetMMeshBase theMeshBase;

    /**
     * A counter that is incremented every time the Probe is run.
     */
    static int probeRunCounter = 0;

    // Our Logger
    private static Log log = Log.getLogInstance(ProbeTest5.class);

    /**
     * The NetMeshBaseIdentifier identifying this Probe.
     */
    protected static final NetMeshBaseIdentifier TEST_NETWORK_IDENTIFIER;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = theMeshBaseIdentifierFactory.fromExternalForm( "test://example.local/" );

        } catch( Throwable t ) {
            log.error( t );
        }
        TEST_NETWORK_IDENTIFIER = temp;
    }

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

    /**
     * The test Probe.
     */
    public static class TestApiProbe
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
            String     message     = "Exception for case " + probeRunCounter;
            MeshObject placeholder = mb.getHomeObject();

            switch( probeRunCounter ) {
                case 0:
                    throw new CallerHasInsufficientPermissionsException( placeholder, null );
                case 1:
                    throw new PropertyReadOnlyException( placeholder, TestSubjectArea.A_READONLY );
                case 2:
                    throw new IsAbstractException( placeholder, TestSubjectArea.A );
                case 3:
                    throw new MeshObjectIdentifierNotUniqueException( placeholder );
                case 4:
                    throw new RelatedAlreadyException( placeholder, placeholder );
                case 5:
                    throw new NotWithinTransactionBoundariesException( mb );
                case 6:
                    throw new ProbeException.EmptyDataSource( networkId );
                case 7:
                    throw new ProbeException.ErrorInProbe( networkId, null );
                case 8:
                    throw new ProbeException.IncompleteData( networkId, "nothing to say" );
                case 9:
                    throw new ProbeException.Other( networkId, "nothing to say" );
                case 10:
                    throw new ProbeException.SyntaxError( networkId, message, null );
                case 11:
                    throw new IOException( message );
                case 12:
                    throw new NullPointerException( message );
                case 13:
                    throw new ClassCastException();
                case 14:
                    throw new ModuleActivationException( null, (Throwable) null );
                case 15:
                    throw new ModuleAdvertisementInstantiationException( null, null );
                case 16:
                    throw new ModuleConfigurationException( null, (Throwable) null );
                case 17:
                    throw new ModuleNotFoundException( null, null );
                case 18:
                    throw new ModuleResolutionException( null, null, null );
                default:
                    log.error( "should not be here" );
            }
        }
    }
}
