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

package org.infogrid.probe.store.TEST;

import java.io.File;
import java.io.FileInputStream;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObjectFactory;
import org.infogrid.mesh.net.externalized.SimpleExternalizedNetMeshObject;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;
import org.infogrid.modelbase.m.MMeshTypeIdentifierFactory;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.probe.shadow.externalized.ExternalizedShadowMeshBase;
import org.infogrid.probe.shadow.externalized.xml.ExternalizedShadowMeshBaseXmlEncoder;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.testharness.ModuleRegistryContext;
import org.infogrid.util.logging.Log;

/**
 * Tests ShadowMeshBase serialization.
 */
public class ShadowMeshBaseSerializationTest1
        extends
            AbstractTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may go wrong in tests
     */
    public void run()
        throws
            Exception
    {
        ModuleRegistry registry = ModuleRegistryContext.getModuleRegistry();
        File installDir = registry.getSoftwareInstallation().getInstallModuleDirectories().get( 0 );
        File moduleDir  = new File( installDir, "org.infogrid.probe.store.TEST" );
        File thisDir    = new File( moduleDir, "src/org/infogrid/probe/store/TEST" );

        TestCase [] testCases = new TestCase[] {
            new TestCase(
                    "ShadowMeshBaseSerializationTest1_1.xml",
                    nmbid1,
                    null,
                    new ExternalizedNetMeshObject[] {
                        SimpleExternalizedNetMeshObject.create(
                                theNetMeshObjectIdentifierFactory.fromExternalForm( "" ),
                                null,
                                12L,
                                34L,
                                56L,
                                78L,
                                null,
                                null,
                                null,
                                null,
                                null,
                                false,
                                false,
                                null,
                                -1,
                                -1 )
                    }
            )
        };
        
        for( int i=0 ; i<testCases.length ; ++i ) {
            runTest( thisDir, testCases[i] );
        }
    }

    /**
     *  Run one test.
     * 
     * @param parentDir the parent directory
     * @param testCase the test case to run
     * @throws Exception all sorts of things may go wrong in tests
     */
    protected void runTest(
            File     parentDir,
            TestCase testCase )
        throws
            Exception
    {
        log.debug( "Now running testcase " + testCase.theInputFile );
            
        File theFile = new File( parentDir, testCase.theInputFile );

        ExternalizedShadowMeshBaseXmlEncoder test = new ExternalizedShadowMeshBaseXmlEncoder();
        
        ExternalizedShadowMeshBase mb = test.decodeShadowMeshBase(
                new FileInputStream( theFile ),
                theExternalizedMeshObjectFactory,
                theNetMeshObjectIdentifierFactory,
                theMeshBaseIdentifierFactory,
                theMeshTypeIdentifierFactory );

        checkEquals( mb.getNetworkIdentifier(), testCase.theNetworkIdentifier, testCase.theInputFile + ": NetworkIdentifier wrong" );
        
        checkEqualsOutOfSequence( mb.getExternalizedProxies(),        testCase.theProxies, testCase.theInputFile + ": Proxies wrong" );
        checkEqualsOutOfSequence( mb.getExternalizedNetMeshObjects(), testCase.theObjects, testCase.theInputFile + ": MeshObjects wrong" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ShadowMeshBaseSerializationTest1 test = null;
        try {
            if( args.length < 1 ) {
                System.err.println( "Synopsis: <test size>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ShadowMeshBaseSerializationTest1( args );
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
     * @throws Exception all sorts of things may go wrong in tests
     */
    public ShadowMeshBaseSerializationTest1(
            String [] args )
        throws
            Exception
    {
        super(  "org/infogrid/probe/store/TEST/ResourceHelper",
                "org/infogrid/probe/store/TEST/Log.properties" );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( ShadowMeshBaseSerializationTest1.class );
    
    /**
     * Factory for NetMeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.create();
    
    /**
     * A NetMeshBaseIdentifier for the test.
     */
    protected NetMeshBaseIdentifier nmbid1 = theMeshBaseIdentifierFactory.fromExternalForm( "https://foo.exampe.com/%27" );

    /**
     * A ExternalizedNetMeshObjectFactory for the test.
     */
    protected ParserFriendlyExternalizedNetMeshObjectFactory theExternalizedMeshObjectFactory
            = new ParserFriendlyExternalizedNetMeshObjectFactory() {
                    public ParserFriendlyExternalizedNetMeshObject createParserFriendlyExternalizedMeshObject() {
                        return new ParserFriendlyExternalizedNetMeshObject();
                    }
            };
    
    /**
     * A NetMeshObjectIdentifierFactory for the test.
     */
    protected NetMeshObjectIdentifierFactory theNetMeshObjectIdentifierFactory
            = DefaultAnetMeshObjectIdentifierFactory.create( nmbid1, theMeshBaseIdentifierFactory );

    /**
     * A MeshTypeIdentifierFactory for the test.
     */
    protected MeshTypeIdentifierFactory theMeshTypeIdentifierFactory
            = MMeshTypeIdentifierFactory.create();
    
    /**
     * Represents one TestCase.
     */
    protected static class TestCase
    {
        public TestCase(
                String                       inputFile,
                NetMeshBaseIdentifier            id,
                ExternalizedProxy []         proxies,
                ExternalizedNetMeshObject [] objects )

            throws
                Exception
        {
            theInputFile         = inputFile;
            theNetworkIdentifier = id;
            
            if( proxies != null ) {
                theProxies = proxies;
            } else {
                theProxies = new ExternalizedProxy[0];
            }
            if( objects != null ) {
                theObjects = objects;
            } else {
                theObjects = new ExternalizedNetMeshObject[0];
            }
        }

        public String                      theInputFile;
        public NetMeshBaseIdentifier           theNetworkIdentifier;
        public ExternalizedProxy[]         theProxies;
        public ExternalizedNetMeshObject[] theObjects;
    }
}
