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
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.testharness.util.IteratorElementCounter;
import org.infogrid.util.logging.Log;

/**
 * Tests resolving ForwardReferences in external files.
 */
public class ForwardReferenceTest1
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
        log.info( "accessing test file 1" );
        
        MeshObject abc = base.accessLocally( testFile1Id, theCoherence );

        checkObject( abc, "abc not found" );

        checkEquals( IteratorElementCounter.countIteratorElements( base.proxies()), 1, "wrong number of proxies in main NetMeshBase" );
        // if wait, this is the proxy to the old Shadow, if !wait, the proxy to the new Shadow

        //
        
        log.info( "Finding ForwardReference" );

        NetMeshObject fwdReference = (NetMeshObject) abc.traverseToNeighborMeshObjects().getSingleMember();
        checkObject( fwdReference, "fwdReference not found" );

        if( theWait ) {
            checkCondition(  fwdReference.isBlessedBy( TestSubjectArea.A, false ), "Not blessed by right type" );
            checkCondition( !fwdReference.isBlessedBy( TestSubjectArea.B, false ), "Blessed by wrong type" );
            checkEquals( fwdReference.getPropertyValue( TestSubjectArea.A_X ), "forwardreference", "wrong property value" );
            checkEquals( fwdReference.getNeighborMeshObjectIdentifiers().length, 1, "wrong number of neighbors" );

            // wait some
        
            Thread.sleep( PINGPONG_ROUNDTRIP_DURATION*3L );
        }

        checkEquals( fwdReference.getPropertyValue( TestSubjectArea.A_X ), "resolved", "ForwardReference was not successfully resolved: " + fwdReference.getIdentifier().toExternalForm() );

        checkEquals(    fwdReference.getAllProxies().length, 1, "Wrong number of proxies on forward reference" );
        checkCondition( fwdReference.getAllProxies()[0].getPartnerMeshBaseIdentifier().toExternalForm().endsWith( "ForwardReferenceTest1_2.xml" ), "Wrong proxy on forward reference" );
        checkCondition( !fwdReference.isBlessedBy( TestSubjectArea.A,  false ), "Blessed still by old type" );
        checkCondition(  fwdReference.isBlessedBy( TestSubjectArea.AA, false ), "Not blessed by the right type (AA)" );
        checkCondition(  fwdReference.isBlessedBy( TestSubjectArea.B,  false ), "Not blessed by the right type (B)" );
        checkEquals( fwdReference.getNeighborMeshObjectIdentifiers().length, 2, "wrong number of neighbors" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ForwardReferenceTest1 test = null;
        try {
            if( args.length != 2 ) {
                System.err.println( "Synopsis: <test file> \"fast\"|\"slow\"" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ForwardReferenceTest1( args );
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
    public ForwardReferenceTest1(
            String [] args )
        throws
            Exception
    {
        super( ForwardReferenceTest1.class, args[1] );

        testFile1 = args[0];

        testFile1Id = theMeshBaseIdentifierFactory.obtain( new File( testFile1 ) );

    }

    // Our Logger
    private static Log log = Log.getLogInstance( ForwardReferenceTest1.class);

    /**
     * File name of the first test file.
     */
    protected String testFile1;

    /**
     * The NetworkIdentifer of the first test file.
     */
    protected NetMeshBaseIdentifier testFile1Id;
}
