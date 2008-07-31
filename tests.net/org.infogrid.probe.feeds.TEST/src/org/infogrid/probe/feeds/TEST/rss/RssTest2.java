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

package org.infogrid.probe.feeds.TEST.rss;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSelector;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.model.Feeds.FeedsSubjectArea;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.feeds.TEST.AbstractFeedTest;
import org.infogrid.probe.feeds.rss.RssProbe;
import org.infogrid.probe.manager.PassiveProbeManager;
import org.infogrid.probe.manager.m.MPassiveProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.probe.shadow.m.MShadowMeshBaseFactory;
import org.infogrid.util.logging.Log;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Tests the RSS Probe with InfoGrid extensions with RssTest2.xml.
 */
public class RssTest2
        extends
            AbstractFeedTest
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
        log.info( "accessing test file with meshBase" );
        
        ShadowMeshBase meshBase1 = theProbeManager1.obtainFor( testFile1Id, CoherenceSpecification.ONE_TIME_ONLY );

        checkObject( meshBase1, "could not find meshBase1" );
        checkCondition( meshBase1.size() > 1, "meshBase1 is empty" );
        dumpMeshBase( meshBase1, "meshBase1", log );

        MeshObject home1 = meshBase1.getHomeObject();

        checkEqualsOutOfSequence(
                home1.getTypes(),
                new EntityType[] {
                    FeedsSubjectArea.RSSFEED,
                    TestSubjectArea.C,
                    ProbeSubjectArea.ONETIMEONLYPROBEUPDATESPECIFICATION
                },
                "home object has wrong type" );

        checkEquals( home1.traverseToNeighborMeshObjects().size(), 2, "wrong number of neighbors for home object" );
        MeshObject def = home1.traverseToNeighborMeshObjects().find( new MeshObjectSelector() {
                public boolean accepts(
                        MeshObject candidate )
                {
                    return candidate.getIdentifier().toExternalForm().endsWith( "def" );
                }
        });
        checkObject( def, "no def object found" );
        checkEquals( def.traverseToNeighborMeshObjects().size(), 2, "wrong number of neighbors for def object" );
        
        checkEqualsOutOfSequence(
                def.getTypes(),
                new EntityType[] {
                    FeedsSubjectArea.RSSFEEDITEM,
                    TestSubjectArea.AA,
                    TestSubjectArea.B
                },
                "home object has wrong type" );
        
        checkEquals( home1.getRoleTypes( def ).length, 1, "Relationship between home and def is unexpectedly more blessed than plain Atom" );
        
        MeshObject abc = def.traverse( TestSubjectArea.RR.getSource() ).getSingleMember();
        checkObject( abc, "no def object found" );
        checkEquals( abc.traverseToNeighborMeshObjects().size(), 2, "wrong number of neighbors for abc object" );
        
        checkEqualsOutOfSequence(
                abc.getTypes(),
                new EntityType[] {
                    FeedsSubjectArea.RSSFEEDITEM,
                    TestSubjectArea.B
                },
                "home object has wrong type" );
        
        checkEquals( abc.getRoleTypes( def ).length, 1, "Relationship between abc and def is not blessed" );
        
        checkEquals( abc.getPropertyValue( TestSubjectArea.B_Z ), "Value3", "wrong property value" );
    }

    /*
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        RssTest2 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <test file>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new RssTest2( args );
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
     */
    public RssTest2(
            String [] args )
        throws
            Exception
    {
        super( RssTest2.class );

        testFile1   = args[0];
        testFile1Id = NetMeshBaseIdentifier.create( new File( testFile1 ) );

        theProbeDirectory.addXmlDomProbe( new ProbeDirectory.XmlDomProbeDescriptor( null, "rss", RssProbe.class ));
        
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

        MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( exec );


        ShadowMeshBaseFactory theShadowFactory
                = MShadowMeshBaseFactory.create( theModelBase, shadowEndpointFactory, theProbeDirectory, -1L, rootContext );
        
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
    }

    // Our Logger
    private static Log log = Log.getLogInstance( RssTest2.class);

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
