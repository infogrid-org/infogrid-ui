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

package org.infogrid.meshbase.store.net.test;

import java.io.File;
import java.io.FileInputStream;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObjectFactory;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.meshbase.net.externalized.xml.ExternalizedProxyXmlEncoder;
import org.infogrid.meshbase.net.transaction.NetMeshObjectDeletedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectPropertyChangeEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeRemovedEvent;
import org.infogrid.meshbase.net.xpriso.SimpleXprisoMessage;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.store.net.NetStoreMeshBase;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.m.MMeshTypeIdentifierFactory;
import org.infogrid.modelbase.m.MModelBase;
import org.infogrid.store.m.MStore;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.logging.Log;

/**
 * Tests the ProxyStoreEntryMapper's parsing functionality.
 */
public class ProxySerializationTest1
        extends
            AbstractTest
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
        File thisDir = new File( "src/org/infogrid/meshbase/store/net/test" );

        TestCase [] testCases = new TestCase[] {
            new TestCase(
                    "ProxySerializationTest1_1.xml",
                    12L,
                    34L,
                    56L,
                    78L,
                    "http://one.local/",
                    "https://two.local/foo",
                    90,
                    123,
                    null,
                    null ),
            new TestCase(
                    "ProxySerializationTest1_2.xml",
                    -1L,
                    -1L,
                    -1L,
                    -1L,
                    null,
                    "=somewhere",
                    -1,
                    -1,
                    null,
                    null ),
            new TestCase(
                    "ProxySerializationTest1_3.xml",
                    -1L,
                    -1L,
                    -1L,
                    -1L,
                    null,
                    "@some.where.else",
                    -1,
                    -1,
                    new SimpleXprisoMessage[] {
                        new TestXprisoMessage(
                                135,
                                246,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                false )
                    },
                    null ),
            new TestCase(
                    "ProxySerializationTest1_4.xml",
                    12L,
                    34L,
                    56L,
                    78L,
                    "http://testing.exameple.com/some/where",
                    "http://testing.exameple.net/some/where",
                    90,
                    123,
                    new SimpleXprisoMessage[] {
                        new TestXprisoMessage(
                                135,
                                246,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                false )
                    },
                    new SimpleXprisoMessage[] {
                        new TestXprisoMessage(
                                345,
                                678,
                                "http://testing.org/some/where",
                                "http://testing.org/abcdef",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                false )
                    })
        };
        
        for( int i=0 ; i<testCases.length ; ++i ) {
            runTest( thisDir, testCases[i] );
        }
    }

    /**
     * Run one test.
     * 
     * @param parentDir the parent directory
     * @param testCase the actual test
     * @throws Exception may throw any exception, it's a test after all
     */
    protected void runTest(
            File     parentDir,
            TestCase testCase )
        throws
            Exception
    {
        log.debug( "Now running testcase " + testCase.theInputFile );
            
        File theFile = new File( parentDir, testCase.theInputFile );

        ExternalizedProxyXmlEncoder test = new ExternalizedProxyXmlEncoder();
        
        ExternalizedProxy proxy = test.decodeExternalizedProxy(
                new FileInputStream( theFile ),
                theNetMeshBase );

        checkEquals( proxy.getNetworkIdentifier(),          testCase.theNetworkIdentifier, testCase.theInputFile + ": NetworkIdentifier wrong" );
        checkEquals( proxy.getNetworkIdentifierOfPartner(), testCase.theNetworkIdentifierOfPartner, testCase.theInputFile + ": NetworkIdentifier wrong" );
        checkEquals( proxy.getLastSentToken(),     testCase.theLastSentToken,     testCase.theInputFile + ": last token sent wrong" );
        checkEquals( proxy.getLastReceivedToken(), testCase.theLastReceivedToken, testCase.theInputFile + ": last token received wrong" );
        checkEquals( proxy.getTimeCreated(),       testCase.theTimeCreated,       testCase.theInputFile + ": time created wrong" );
        checkEquals( proxy.getTimeUpdated(),       testCase.theTimeUpdated,       testCase.theInputFile + ": time updated wrong" );
        checkEquals( proxy.getTimeRead(),          testCase.theTimeRead,          testCase.theInputFile + ": time read wrong" );
        checkEquals( proxy.getTimeExpires(),       testCase.theTimeExpires,       testCase.theInputFile + ": time expires wrong" );

        checkEqualsInSequence( proxy.messagesLastSent(), testCase.theLastMessagesSent, testCase.theInputFile + ": last messages sent wrong" );
        checkEqualsInSequence( proxy.messagesToBeSent(), testCase.theMessagesToBeSent, testCase.theInputFile + ": to-be sent messages sent wrong" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ProxySerializationTest1 test = null;
        try {
            if( args.length < 1 ) {
                System.err.println( "Synopsis: <test size>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ProxySerializationTest1( args );
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
     * @throws Exception anything can go wrong in a test
     */
    public ProxySerializationTest1(
            String [] args )
        throws
            Exception
    {
        super( "org/infogrid/meshbase/store/net/test/ResourceHelper" );
        
        MStore meshObjectStore = MStore.create();
        MStore proxyStore      = MStore.create();
        
        theNetMeshBase = NetStoreMeshBase.create(
                theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local" ),
                theModelBase,
                null,
                null,
                meshObjectStore,
                proxyStore,
                rootContext );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( ProxySerializationTest1.class );
    
    /**
     * The root application context.
     */
    protected static final Context rootContext = SimpleContext.createRoot( "root" );
    
    /**
     * ModelBase.
     */
    protected static final ModelBase theModelBase = MModelBase.create();
    
    /**
     * Factory for NetMeshBaseIdentifiers.
     */
    protected static NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.create();
    
    /**
     * An ExternalizedNetMeshObjectFactory for the test.
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
            = DefaultAnetMeshObjectIdentifierFactory.create( null, theMeshBaseIdentifierFactory );

    /**
     * A MeshTypeIdentifierFactory for the test.
     */
    protected MeshTypeIdentifierFactory theMeshTypeIdentifierFactory
            = MMeshTypeIdentifierFactory.create();

    /**
     * NetMeshBase for the test.
     */
    protected NetStoreMeshBase theNetMeshBase;

    /**
     * Represents one TestCase.
     */
    protected static class TestCase
    {
        public TestCase(
                String inputFile,
                long            timeCreated,
                long            timeUpdated,
                long            timeRead,
                long            timeExpires,
                String          networkIdentifier,
                String          networkIdentifierOfPartner,
                int             lastSentToken,
                int             lastReceivedToken,
                XprisoMessage[] messagesToBeSent,
                XprisoMessage[] lastMessagesSent )
            throws
                Exception
        {
            theInputFile = inputFile;

            theTimeCreated   = timeCreated;
            theTimeUpdated   = timeUpdated;
            theTimeRead      = timeRead;
            theTimeExpires   = timeExpires;

            if( networkIdentifier != null ) {
                theNetworkIdentifier = theMeshBaseIdentifierFactory.fromExternalForm( networkIdentifier );
            }
            if( networkIdentifierOfPartner != null ) {
                theNetworkIdentifierOfPartner = theMeshBaseIdentifierFactory.fromExternalForm( networkIdentifierOfPartner );
            }
            theLastSentToken     = lastSentToken;
            theLastReceivedToken = lastReceivedToken;

            if( messagesToBeSent != null ) {
                theMessagesToBeSent = messagesToBeSent;
            } else {
                theMessagesToBeSent = new XprisoMessage[0];
            }
            if( lastMessagesSent != null ) {
                theLastMessagesSent = lastMessagesSent;
            } else {
                theLastMessagesSent = new XprisoMessage[0];
            }
        }
        
        public String theInputFile;
        public long   theTimeCreated;
        public long   theTimeUpdated;
        public long   theTimeRead;
        public long   theTimeExpires;
        public NetMeshBaseIdentifier theNetworkIdentifier;
        public NetMeshBaseIdentifier theNetworkIdentifierOfPartner;
        public int               theLastSentToken;
        public int               theLastReceivedToken;
        public XprisoMessage[]   theMessagesToBeSent;
        public XprisoMessage[]   theLastMessagesSent;
    }

    /**
     * Adds a constructor to SimpleXprisoMessage that makes it easier to use in this test.
     */
    static class TestXprisoMessage
            extends
                SimpleXprisoMessage
    {
        private static final long serialVersionUID = 1L; // helps to get rid of IDE compiler warnings
        
        public TestXprisoMessage(
                int                                  requestId,
                int                                  responseId,
                String                               senderNetworkIdentifierString,
                String                               receiverNetworkIdentifierString,
                NetMeshObjectAccessSpecification []  requestedFirstTimeObjects,
                NetMeshObjectIdentifier []           requestedCanceledObjects,
                NetMeshObjectDeletedEvent []         deleteChanges,
                long   []                            deleteUpdateTimes,
                ExternalizedNetMeshObject[]          conveyedMeshObjects,
                NetMeshObjectNeighborAddedEvent []   neighborAdditions,
                NetMeshObjectNeighborRemovedEvent [] neighborRemovals,
                NetMeshObjectPropertyChangeEvent []  propertyChanges,
                NetMeshObjectRoleAddedEvent []       roleAdditions,
                NetMeshObjectRoleRemovedEvent []     roleRemovals,
                NetMeshObjectTypeAddedEvent []       typeAdditions,
                NetMeshObjectTypeRemovedEvent []     typeRemovals,
                NetMeshObjectIdentifier []           requestedLockObjects,
                NetMeshObjectIdentifier []           pushLockObjects,
                NetMeshObjectIdentifier []           reclaimedLockObjects,
                NetMeshObjectIdentifier []           requestedResynchronizeDependentReplicas,
                boolean                              ceaseCommunications )
            throws
                Exception
        {
            super( null, null );

            setRequestId( requestId );
            setResponseId( responseId );

            if( senderNetworkIdentifierString != null ) {
                setSenderIdentifier( theMeshBaseIdentifierFactory.fromExternalForm( senderNetworkIdentifierString ));
            }
            if( receiverNetworkIdentifierString != null ) {
                setReceiverIdentifier( theMeshBaseIdentifierFactory.fromExternalForm( receiverNetworkIdentifierString ));
            }

            if( requestedFirstTimeObjects != null ) {
                setRequestedFirstTimeObjects( requestedFirstTimeObjects );
            }

            if( requestedCanceledObjects != null ) {
                setRequestedCanceledObjects( requestedCanceledObjects );
            }
            if( deleteChanges != null ) {
                setDeleteChanges( deleteChanges );
            }
            if( conveyedMeshObjects != null ) {
                super.setConveyedMeshObjects( conveyedMeshObjects );
            }
            if( neighborAdditions != null ) {
                super.setNeighborAdditions( neighborAdditions );
            }
            if( neighborRemovals != null ) {
                super.setNeighborRemovals( neighborRemovals );
            }
            if( propertyChanges != null ) {
                super.setPropertyChanges( propertyChanges );
            }
            if( roleAdditions != null ) {
                super.setRoleAdditions( roleAdditions );
            }
            if( roleRemovals != null ) {
                super.setRoleRemovals( roleRemovals );
            }
            if( typeAdditions != null ) {
                super.setTypeAdditions( typeAdditions );
            }
            if( typeRemovals != null ) {
                super.setTypeRemovals( typeRemovals );
            }

            if( requestedLockObjects != null ) {
                super.setRequestedLockObjects( requestedLockObjects );
            }
            if( pushLockObjects != null ) {
                super.setPushLockObjects( pushLockObjects );
            }
            if( reclaimedLockObjects != null ) {
                super.setReclaimedLockObjects( reclaimedLockObjects );
            }
            if( requestedResynchronizeDependentReplicas != null ) {
                super.setRequestedResynchronizeReplicas( requestedResynchronizeDependentReplicas );
            }

            super.setCeaseCommunications( ceaseCommunications );
        }
    }
}
