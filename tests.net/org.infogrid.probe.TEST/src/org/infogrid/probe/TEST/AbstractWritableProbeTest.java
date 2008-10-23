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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.logging.Log;

/**
 * Provides a test framework that makes it easier to test WritableProbes systematically.
 */
public abstract class AbstractWritableProbeTest
        extends
            AbstractProbeTest
{
    /**
     * Constructor.
     * 
     * @param testClass the Class to be tested
     */
    protected AbstractWritableProbeTest(
            Class testClass )
    {
        super( testClass );
    }

    /**
     * Run the tests.
     *
     * @param testCases the test cases to run
     * @throws Exception all sorts of things can go wrong during a test
     */
    public void run(
            WritableProbeTestCase [] testCases )
        throws
            Exception
    {
        Log myLog = Log.getLogInstance( getClass() ); // find right subclass logger

        for( int i=0 ; i<testCases.length ; ++i ) {

            myLog.info( "About to run TestCase " + i + ": " + testCases[i].theProbeClass.getName() );

            LocalNetMMeshBase base = null;
            try {
                // set up ProbeDirectory
                MProbeDirectory theProbeDirectory = MProbeDirectory.create();

                theProbeDirectory.addExactUrlMatch(
                        new ProbeDirectory.ExactMatchDescriptor(
                                TEST1_URL.toExternalForm(),
                                testCases[i].theProbeClass ));

                // create MeshBase and run Probe
                base = LocalNetMMeshBase.create( here, theModelBase, null, theProbeDirectory, exec, rootContext );

                myLog.info( "Performing accessLocally" );

                NetMeshObject shadowHomeInMain = base.accessLocally( TEST1_URL );
                checkObject( shadowHomeInMain, "could not find shadow's home object in main MeshBase" );

                ShadowMeshBase shadow = base.getShadowMeshBaseFor( TEST1_URL );
                checkObject( shadow, "could not find shadow" );
                
                testCases[i].afterFirstRun( base, shadow, shadowHomeInMain );
                
                Thread.sleep( 2500L );

                // updating
                myLog.info( "Now doing a manual update" );

                shadow.doUpdateNow();

                testCases[i].afterSecondRun( base, shadow, shadowHomeInMain );

            } catch( Throwable ex ) {
                reportError( "Test " + i + " failed", ex );
                System.exit( 1 );

            } finally {
                if( base != null ) {
                    base.die( true );
                }
            }
        }
    }
    
    /**
     * Cleanup.
     */
    @Override
    public void cleanup()
    {
        exec.shutdown();
    }

    // Our Logger
    private static Log log = Log.getLogInstance( AbstractWritableProbeTest.class  );

    /**
     * the test protocol, in the real world this would be something like "jdbc"
     */
    private static final String PROTOCOL_NAME = "AbstractWritableProbeTestProtocol";

    /**
     * The identifier of the main NetMeshBase.
     */
    protected static final NetMeshBaseIdentifier here;
    static {
        NetMeshBaseIdentifier temp;
        try {
            temp = theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local/" ); // this is not going to work for communications
        } catch( Exception ex ) {
            log.error( ex );
            temp = null; // make compiler happy
        }
        here = temp;
    }

    /**
     * The first URL that we are accessing.
     */
    protected static final NetMeshBaseIdentifier TEST1_URL;
    static {
        NetMeshBaseIdentifier temp;
        try {
            temp = theMeshBaseIdentifierFactory.obtainUnresolvable( PROTOCOL_NAME + "://shadow.some.where/one" );
        } catch( Exception ex ) {
            log.error( ex );
            temp = null; // make compiler happy
        }
        TEST1_URL = temp;
    }

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );
    
    /**
     * A TestCase for WritableProbes.
     */
    public static abstract class WritableProbeTestCase
    {
        /**
         * Constructor.
         * 
         * @param clazz the Probe class to test
         */
        public WritableProbeTestCase(
                Class<? extends ApiProbe> clazz )
        {
            theProbeClass = clazz;
        }
        
        /**
         * Invoked after the first Probe run has been completed.
         * 
         * @param mainBase the main NetMeshBase
         * @param shadow the ShadowMeshBase into which the Probe has been processed
         * @param shadowHomeInMain the ShadowMeshBase's home object, as replicated in the main MeshBase
         * @throws Exception all kinds of things can go wrong in a test
         */
        public abstract void afterFirstRun(
                NetMeshBase    mainBase,
                ShadowMeshBase shadow,
                NetMeshObject  shadowHomeInMain )
            throws
                Exception;

        /**
         * Invoked after the second Probe run has been completed.
         * 
         * @param mainBase the main NetMeshBase
         * @param shadow the ShadowMeshBase into which the Probe has been processed
         * @param shadowHomeInMain the ShadowMeshBase's home object, as replicated in the main MeshBase
         * @throws Exception all kinds of things can go wrong in a test
         */
        public abstract void afterSecondRun(
                NetMeshBase    mainBase,
                ShadowMeshBase shadow,
                NetMeshObject  shadowHomeInMain )
            throws
                Exception;

   
        /**
         * The Probe class to test.
         */
        protected Class<? extends ApiProbe> theProbeClass;
    }
}
