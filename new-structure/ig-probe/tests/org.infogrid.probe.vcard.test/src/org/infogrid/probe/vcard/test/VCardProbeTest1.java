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

package org.infogrid.probe.vcard.test;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.model.VCard.VCardSubjectArea;
import org.infogrid.probe.manager.PassiveProbeManager;
import org.infogrid.probe.manager.m.MPassiveProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.probe.shadow.m.MShadowMeshBaseFactory;
import org.infogrid.util.logging.Log;

/**
  * Tests the VCard Probe. FIXME: This is not a very complete test at all.
  */
public class VCardProbeTest1
    extends
        AbstractVCardProbeTest
{
    /**
     * Run the test.
     * 
     * @throws Exception all kinds of things may go wrong during a test
     */
    public void run()
        throws
            Exception
    {
        log.info( "accessing test file" );
        
        ShadowMeshBase meshBase1 = theProbeManager1.obtainFor( testFile1Id, CoherenceSpecification.ONE_TIME_ONLY );

        checkObject( meshBase1, "could not find meshBase1" );
        checkCondition( meshBase1.size() > 1, "meshBase1 is empty" );

        MeshObject home = meshBase1.getHomeObject();
        checkCondition( home.isBlessedBy( VCardSubjectArea.VCARD ), "Not blessed with a VCard" );
        
        Iterator<MeshObject> theIter = meshBase1.iterator();
        int numberObjects = 0;

        while( theIter.hasNext() ) {
            MeshObject current = theIter.next();

            ++numberObjects;
            if( log.isDebugEnabled() ) {
                log.debug( "found "+current );
            }
        }
        checkEquals( numberObjects, 6, "wrong number of MeshObjects" );
           // 1 VCard
           // 1 PhysicalAddress
           // 3 Phone numbers
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        VCardProbeTest1 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <test file>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new VCardProbeTest1( args );
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
     * @throws Exception all sorts of things may go wrong in tests
     */
    public VCardProbeTest1(
            String [] args )
        throws
            Exception
    {
        super( VCardProbeTest1.class );

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
    private static Log log = Log.getLogInstance( VCardProbeTest1.class);

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

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
}
