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

package org.infogrid.probe.test;

import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.instrument.Breakpoint;
import org.infogrid.util.instrument.InstrumentedThread;
import org.infogrid.util.logging.Log;

/**
 * Factors out common functionality of the ProbeTest9xxx tests.
 */
public abstract class AbstractProbeTest9
    extends
        AbstractProbeTest
{
    /**
     * Constructor.
     *
     * @param testClass the Class to be tested
     * @param probeClass the Probe class to run
     * @throws Exception all sorts of things may happen during a test
     */
    protected AbstractProbeTest9(
            Class                     testClass,
            Class<? extends ApiProbe> probeClass )
        throws
            Exception
    {
        super( testClass );

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                test1_URL.toExternalForm(),
                probeClass ));
                NetMeshBaseIdentifier here = theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local/" ); // this is not going to work for communications

        theMeshBase = LocalNetMMeshBase.create( here, theModelBase, null, theProbeDirectory, exec, rootContext );
    }

    /**
     * This method creates a Thread for each of the passed Runnables, and returns the Threads it created.
     * It does not start the Threads.
     *
     * @param runs the Runnables for which we want to create Threads
     * @param names the names for the different Threads in the same sequence as runs
     * @return the created Threads
     */
    protected InstrumentedThread [] createThreads(
            Runnable [] runs,
            String []   names )
    {
        if( getLog().isDebugEnabled() ) {
            getLog().debug( "createThreads( " + ArrayHelper.arrayToString( runs ) + " )" );
        }

        InstrumentedThread [] ret = new InstrumentedThread[ runs.length ];
        for( int i=0 ; i<runs.length ; ++i ) {
            if( runs[i] != null ) {
                ret[i] = new InstrumentedThread( runs[i], names != null ? names[i] : null );
            } else {
                getLog().info( "Skipping start of Thread with index " + i );
            }
        }
        theThreadsToKill = ret;
        return ret;
    }

    /**
     * Clean up.
     */
    @Override
    public void cleanup()
    {
        if( theThreadsToKill != null ) {
            for( int i=0 ; i<theThreadsToKill.length ; ++i ) {
                theThreadsToKill[i].interrupt();
            }
        }
        if( theMeshBase != null ) {
            theMeshBase.die();
        }
        exec.shutdown();

    }

    /**
     * The MeshBase that we use to test.
     */
    protected LocalNetMMeshBase theMeshBase;

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    private static final Log log = Log.getLogInstance( AbstractProbeTest9.class ); // Our own, private logger.

    /**
     * The first URL that we are accessing.
     */
    protected static NetMeshBaseIdentifier test1_URL;

    static {
        try {
            test1_URL = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://test1_myhost.local/remainder" );

        } catch( Exception ex ) {
            log.error( ex );

            test1_URL = null; // make compiler happy
        }
    }

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

    /**
     * A "breakpoint" location. This has to be static as we can't provide non-static variables to our Probes.
     */
    protected static Breakpoint location1 = new Breakpoint( "location1", 60000L );

    /**
     * The result delivered by Probe1.
     */
    MeshObject foundObject1 = null;

    /**
     * The result delivered by Probe2.
     */
    MeshObject foundObject2 = null;

    /**
     * Any Exception thrown by Probe1.
     */
    Throwable throwable1 = null;

    /**
     * Any Exception thrown by Probe2.
     */
    Throwable throwable2 = null;

    /**
     * Counter how many times the Probe was invoked.
     */
    protected static int probeInvocationCounter;

    /**
     * The Threads we created and that we need to kill.
     */
    protected InstrumentedThread [] theThreadsToKill;

    /**
     * Code to be invoked by the threads.
     */
    public abstract class MyRunnable
        implements
            Runnable
    {
        public MyRunnable(
                NetMeshBaseIdentifier n )
        {
            thePath = n;
        }

        public void run()
        {
            if( getLog().isDebugEnabled() ) {
                getLog().debug( "MyRunnable starting: " + thePath.toExternalForm() );
            }
            try {
                sleepFor( 100L );


                MeshObject obj = theMeshBase.accessLocally(
                        thePath,
                        CoherenceSpecification.ONE_TIME_ONLY );
                recordResult( obj );

            } catch( Exception ex ) {
                recordException( ex );
            }
            if( getLog().isDebugEnabled() ) {
                getLog().debug( "MyRunnable ending: " + thePath.toExternalForm() );
            }
        }

        protected abstract void recordResult(
                MeshObject result );
        protected abstract void recordException(
                Throwable t );

        private NetMeshBaseIdentifier thePath;
    }
}