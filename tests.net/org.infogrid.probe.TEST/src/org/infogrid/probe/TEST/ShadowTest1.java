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

import java.io.File;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.IterableNetMeshBaseDifferencer;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.ChangeSet;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.testharness.util.IteratorElementCounter;
import org.infogrid.util.logging.Log;

/**
 * Reads (via the Probe framework) test1.xml into a NetMeshBase.
 */
public class ShadowTest1
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
        log.info( "accessing #abc of test file with NetMeshBase" );
        
        MeshObject abc = base.accessLocally(
                base.getNetMeshObjectAccessSpecificationFactory().obtain(
                        testFile1Id,
                        base.getMeshObjectIdentifierFactory().fromExternalForm( testFile1Id.toExternalForm() + "#abc" ),
                        CoherenceSpecification.ONE_TIME_ONLY ));

        checkObject( abc, "Object not found" );
        checkEquals( IteratorElementCounter.countIteratorElements( base.proxies()), 1, "wrong number of proxies in main NetMeshBase" );
        
        //
        
        log.info( "accessing #def of test file with NetMeshBase" );
        
        MeshObject def = base.accessLocally(
                base.getNetMeshObjectAccessSpecificationFactory().obtain(
                        testFile1Id,
                        base.getMeshObjectIdentifierFactory().fromExternalForm( testFile1Id.toExternalForm() + "#def" ),
                        CoherenceSpecification.ONE_TIME_ONLY ));
                
        checkObject( def, "Object not found" );
        checkEquals( IteratorElementCounter.countIteratorElements( base.proxies()), 1, "wrong number of proxies in main NetMeshBase" );

        //
        
        log.info( "traverse to related objects" );

        MeshObjectSet abcNeighbors = abc.traverseToNeighborMeshObjects();
        checkEquals( abcNeighbors.size(), 0, "wrong number of neighbors for abc" );

        MeshObjectSet defNeighbors = def.traverseToNeighborMeshObjects();
        checkEquals( defNeighbors.size(), 1, "wrong number of neighbors for def" );

        //
        
        log.info( "now compare main and shadow base" );
        
        ShadowMeshBase shadow = base.getShadowMeshBaseFor( testFile1Id );
        
        IterableNetMeshBaseDifferencer diff = new IterableNetMeshBaseDifferencer( base );
        ChangeSet changes = diff.determineChangeSet( shadow );

        // these two are here for better debugging
        MeshObject baseHome   = base.getHomeObject();
        MeshObject shadowHome = shadow.getHomeObject();
        
        checkEquals( changes.size(), 6, "wrong number of changes" );
        // These changes should be:
        // 1. Home Object created
        // 2. Home Object deleted
        // 3. ProbeUpdateSpecification#ProbeRunCounter changed
        // 4. ProbeUpdateSpecification#LastRunUsedWritableProbe changed
        // 5. ProbeUpdateSpecification#LastProbeRun changed
        // 6. ProbeUpdateSpecification#LastRunUsedProbeClass changed

        if( changes.size() != 6 ) {
            dumpChangeSet( changes, log );
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
        ShadowTest1 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <test file>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ShadowTest1( args );
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
    public ShadowTest1(
            String [] args )
        throws
            Exception
    {
        super( ShadowTest1.class );

        testFile1 = args[0];

        testFile1Id = theMeshBaseIdentifierFactory.obtain( new File( testFile1 ) );
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
    private static Log log = Log.getLogInstance( ShadowTest1.class);

    /**
     * File name of the first test file.
     */
    protected String testFile1;

    /**
     * The NetworkIdentifer of the first test file.
     */
    protected NetMeshBaseIdentifier testFile1Id;
}
