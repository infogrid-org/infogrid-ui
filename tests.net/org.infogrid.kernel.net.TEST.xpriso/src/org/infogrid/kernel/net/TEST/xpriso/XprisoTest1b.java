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

package org.infogrid.kernel.net.TEST.xpriso;

import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.util.logging.Log;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Tests when a remote object cannot be accessed because it does not exist.
 * This tests two levels of access. Compare XprisoTest1a.
 */
public class XprisoTest1b
        extends
            AbstractXprisoTest
{
    /**
     * Run the test.
     *
     * @throws Exception thrown if an Exception occurred during the test
     */
    public void run()
        throws
            Exception
    {
        NetMeshObjectIdentifier identifier = mb3.getMeshObjectIdentifierFactory().fromExternalForm( "Missing object" );
        
        log.info( "Accessing missing object" );

        NetMeshObject missing_mb3 = mb3.accessLocally(
                NetMeshObjectAccessSpecification.create(
                        new NetMeshBaseIdentifier[] {
                            mb2.getIdentifier(),
                            mb1.getIdentifier(),
                        },
                        identifier ));

        checkCondition( missing_mb3 == null, "Object was unexpectedly found" );
    }

    /*
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        XprisoTest1b test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest1b( args );
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
      */
    public XprisoTest1b(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest1b.class );

        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( exec );
        endpointFactory.setNameServer( theNameServer );

        mb1 = NetMMeshBase.create( net1, endpointFactory, theModelBase, null, rootContext );
        mb2 = NetMMeshBase.create( net2, endpointFactory, theModelBase, null, rootContext );
        mb3 = NetMMeshBase.create( net3, endpointFactory, theModelBase, null, rootContext );
        
        theNameServer.put( mb1.getIdentifier(), mb1 );
        theNameServer.put( mb2.getIdentifier(), mb2 );
        theNameServer.put( mb3.getIdentifier(), mb3 );
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        mb1.die();
        mb2.die();
        mb3.die();
        
        exec.shutdown();
    }

    /**
     * The first NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net1 = NetMeshBaseIdentifier.createUnresolvable( "one.local" );

    /**
     * The second NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net2 = NetMeshBaseIdentifier.createUnresolvable( "two.local" );

    /**
     * The third NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net3 = NetMeshBaseIdentifier.createUnresolvable( "three.local" );

    /**
     * The first NetMeshBase.
     */
    protected NetMeshBase mb1;

    /**
     * The second NetMeshBase.
     */
    protected NetMeshBase mb2;

    /**
     * The third NetMeshBase.
     */
    protected NetMeshBase mb3;

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 2 ); // gotta have two threads

    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest1b.class );
}
