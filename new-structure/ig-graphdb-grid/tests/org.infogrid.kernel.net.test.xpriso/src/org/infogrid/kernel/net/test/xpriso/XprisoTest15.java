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

package org.infogrid.kernel.net.test.xpriso;

import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObjectGraphModificationException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.meshbase.net.xpriso.logging.LogXprisoMessageLogger;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionAction;
import org.infogrid.meshbase.transaction.TransactionActionException;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Tests concurrent execution of accessLocally to different NetMeshBases.
 */
public class XprisoTest15
    extends
        AbstractXprisoTest
{
    /**
     * Run the test.
     *
     * @throws Exception all kinds of things can go wrong in tests
     */
    public void run()
        throws
            Exception
    {
        log.info( "Instantiating objects in other MeshBases" );

        NetMeshObject [] instantiated = new NetMeshObject[ otherMbs.length ];
        for( int i=0 ; i<otherMbs.length ; ++i ) {
            instantiated[i] = otherMbs[i].executeNow( new TransactionAction<NetMeshObject>() {
                    public NetMeshObject execute(
                            Transaction tx )
                        throws
                            MeshObjectGraphModificationException,
                            TransactionException,
                            TransactionActionException
                    {
                        try {
                            return ((NetMeshBase)tx.getMeshBase()).getMeshBaseLifecycleManager().createMeshObject(
                                    tx.getMeshBase().getMeshObjectIdentifierFactory().fromExternalForm( "#obj" ));

                        } catch( StringRepresentationParseException ex ) {
                            throw new TransactionActionException.Error( ex );
                        }
                    }
            });
        }

        //

        log.info( "Accessing objects in mb1" );

        NetMeshObjectAccessSpecification [] specs = new NetMeshObjectAccessSpecification[ otherMbs.length ];
        for( int i=0 ; i<otherMbs.length ; ++i ) {
            specs[i] = mb1.getNetMeshObjectAccessSpecificationFactory().obtain( otherMbs[i].getIdentifier(), instantiated[i].getIdentifier());
        }

        startClock();

        NetMeshObject [] found = mb1.accessLocally(
                specs,
                10000L );

        long delta = super.getRelativeTime();
        log.debug( "Took " + delta + " msecs" );

        checkEquals( found.length, otherMbs.length, "wrong number of objects found" );

        for( int i=0 ; i<otherMbs.length ; ++i ) {
            checkObject( found[i], "obj2_mb2 not found in mb1" );

            checkEquals( found[i].getMeshBase(), mb1, "obj with index " + i + " not in mb1" );

            checkProxies( found[i], new NetMeshBase[] { otherMbs[i] }, otherMbs[i], otherMbs[i], "obj with index " + i + " has wrong proxies" );
        }

        checkInRange( delta, 0, PINGPONG_ROUNDTRIP_DURATION*5, "Not completed in expected time range" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        XprisoTest15 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoTest15( args );
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
     * @throws Exception all kinds of things can go wrong in tests
     */
    public XprisoTest15(
            String [] args )
        throws
            Exception
    {
        super( XprisoTest15.class );

        MPingPongNetMessageEndpointFactory endpointFactory = MPingPongNetMessageEndpointFactory.create( 1000L, PINGPONG_ROUNDTRIP_DURATION/2, 500L, 10000L, 0.f, exec );
        endpointFactory.setNameServer( theNameServer );

        mb1 = NetMMeshBase.create( net1, theModelBase, null, endpointFactory, rootContext );
        
        theNameServer.put( mb1.getIdentifier(), mb1 );

        if( log.isInfoEnabled() ) {
            mb1.setXprisoMessageLogger( LogXprisoMessageLogger.create( log ));
        }
        
        for( int i=0 ; i<otherMbs.length ; ++i ) {
            NetMeshBaseIdentifier currentId = theMeshBaseIdentifierFactory.fromExternalForm( "test://" + i + ".local" );
            otherMbs[i] = NetMMeshBase.create( currentId, theModelBase, null, endpointFactory, rootContext );

            theNameServer.put( otherMbs[i].getIdentifier(), otherMbs[i] );

            if( log.isInfoEnabled() ) {
                otherMbs[i].setXprisoMessageLogger( mb1.getXprisoMessageLogger() );
            }
        }
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        mb1.die();
        
        if( otherMbs != null ) {
            for( int i=0 ; i<otherMbs.length ; ++i ) {
                if( otherMbs[i] != null ) {
                    otherMbs[i].die();
                }
            }
        }
        exec.shutdown();
    }

    /**
     * The number of NetMeshBases to replicate from.
     */
    public static final int MAX = 20;

    /**
     * The first NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier net1 = theMeshBaseIdentifierFactory.fromExternalForm( "test://one.local" );

    /**
     * The first NetMeshBase.
     */
    protected NetMMeshBase mb1;

    /**
     * The other NetMeshBases.
     */
    protected NetMMeshBase [] otherMbs = new NetMMeshBase[MAX];

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 2 );

    // Our Logger
    private static Log log = Log.getLogInstance( XprisoTest15.class );
}
