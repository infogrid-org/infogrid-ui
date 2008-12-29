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

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectBreadthFirstIterator;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.util.logging.Log;

/**
  * Tests that all MeshObjects from the same Probe have the same timeCreated and timeUpdated values.
  */
public class ProbeTest4
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
        log.info( "accessing " + theTestUrl );

        MeshObject top = theMeshBase.accessLocally( theTestUrl );
        checkObject( top, "no object from top" );
        reportOn( "top", top );

        long topCreated = top.getTimeCreated();
        long topUpdated = top.getTimeUpdated();
        
        //

        log.info( "finding all objects -- some multiple times" );

        StringBuffer buf = new StringBuffer();
        
        MeshObjectBreadthFirstIterator iter =  MeshObjectBreadthFirstIterator.create( top, 100 );
        for( int i=0 ; iter.hasNext() ; ++i ) {
            MeshObject current = iter.next();

            log.debug( "Found object: " + current );
            
            StringValue value = (StringValue) current.getPropertyValue( TestSubjectArea.A_X );
            if( value != null ) {
                buf.append( value.getAsString() );
            }

            reportOn( String.valueOf( i ), current );

            checkEquals( topCreated, current.getTimeCreated(), "create time different for object " + current );
            checkEquals( topUpdated, current.getTimeUpdated(), "update time different for object " + current );
        }
        
        checkEquals( buf.toString(), "homeabcdefghi", "wrong constructed value from properties" );
    }

    /**
     * Helper method to report on one found object.
     * @param prefix prefix to print
     * @param obj the MeshObject we report on
     */
    protected void reportOn(
            String     prefix,
            MeshObject obj )
    {
        if( log.isInfoEnabled() ) {
            StringBuffer line = new StringBuffer( 64 );
            if( prefix != null ) {
                line.append( prefix ).append( ": " );
            }
            line.append( obj.getIdentifier() );
            line.append( ", created: " );
            line.append( obj.getTimeCreated() );
            line.append( ", updated: " );
            line.append( obj.getTimeUpdated() );

            log.info( line );
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
        ProbeTest4 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <test file>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ProbeTest4( args );
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
    public ProbeTest4(
            String [] args )
        throws
            Exception
    {
        super( ProbeTest4.class );
        
        theTestUrl = theMeshBaseIdentifierFactory.obtain( new File( args[0] ) );

        NetMeshBaseIdentifier here = theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local/" ); // this is not going to work for communications

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
     * The test URL that we access
     */
    protected NetMeshBaseIdentifier theTestUrl;
    
    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    /**
     * The MeshBase to be tested.
     */
    protected LocalNetMMeshBase theMeshBase;

    // Our Logger
    private static Log log = Log.getLogInstance( ProbeTest4.class);
}