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

import java.io.ByteArrayInputStream;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObjectFactory;
import org.infogrid.mesh.net.externalized.SimpleExternalizedNetMeshObject;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.DefaultNetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.m.NetMMeshBase;
import org.infogrid.meshbase.net.transaction.NetMeshObjectDeletedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectPropertyChangeEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeRemovedEvent;
import org.infogrid.meshbase.net.xpriso.ParserFriendlyXprisoMessage;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.net.xpriso.xml.XprisoMessageXmlEncoder;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.TimePeriodValue;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;
import org.infogrid.modelbase.m.MMeshTypeIdentifierFactory;
import org.infogrid.util.logging.Log;

/**
 * Tests XprisoMessage serialization.
 */
public class XprisoMessageSerializationTest1
        extends
            AbstractXprisoTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may go wrong during a test
     */
    public void run()
        throws
            Exception
    {
        XprisoMessage [] testMessages = constructTestMessages();
        
        for( int i=0 ; i<testMessages.length ; ++i ) {
            runOne( i, testMessages[i] );
        }
    }

    /**
     * Run a single test.
     * 
     * @param index the index of the test
     * @param message the XprisoMessage to test
     * @throws Exception all sorts of things may go wrong during a test
     */
    protected void runOne(
            int           index,
            XprisoMessage message )
        throws
            Exception
    {
        log.info( "Now running test " + index );
        
        XprisoMessageXmlEncoder encoder = new XprisoMessageXmlEncoder();
        StringBuilder           buf     = new StringBuilder();
        
        encoder.appendXprisoMessage( message, buf );
        
        String encodedMessage = buf.toString();
        
        log.debug( "Serialized message: " + encodedMessage );
        
        ByteArrayInputStream inStream = new ByteArrayInputStream( encodedMessage.getBytes());
        
        XprisoMessage recovered = encoder.decodeXprisoMessage(
                inStream,
                theNetMeshBase );

        checkEquals( message, recovered, "Recovered XprisoMessage not the same" );
    }

    /**
     * Construct the test messages.
     * 
     * @return the constructed test messages
     * @throws Exception all sorts of things may go wrong during a test
     */
    protected XprisoMessage [] constructTestMessages()
        throws
            Exception
    {
        // some useful data setup
        
        NetMeshBaseIdentifier id1 = theMeshBaseIdentifierFactory.fromExternalForm( "http://some.where.example.com/" );
        NetMeshBaseIdentifier id2 = theMeshBaseIdentifierFactory.fromExternalForm( "http://foo.net/" ); // FIXME Make this work with plain =foo, too
        NetMeshBaseIdentifier id3 = theMeshBaseIdentifierFactory.fromExternalForm( "http://abc.example.net/?some=parameter&other=%20" );
        
        NetMeshObjectIdentifier nmo_ref1 = theNetMeshObjectIdentifierFactory.fromExternalForm( "" );
        NetMeshObjectIdentifier nmo_ref2 = theNetMeshObjectIdentifierFactory.fromExternalForm( "abc" );
        NetMeshObjectIdentifier nmo_ref3 = theNetMeshObjectIdentifierFactory.fromExternalForm( "def" );
        NetMeshObjectIdentifier nmo_ref4 = theNetMeshObjectIdentifierFactory.fromExternalForm( "http://example.net/foo/bar#abc" );
        NetMeshObjectIdentifier nmo_ref5 = theNetMeshObjectIdentifierFactory.fromExternalForm( "#abc" );
        NetMeshObjectIdentifier nmo_ref6 = theNetMeshObjectIdentifierFactory.fromExternalForm( "#123%20456" );

        MeshTypeIdentifier mt_ref1 = theMeshTypeIdentifierFactory.fromExternalForm( "org.infogrid.model.Some/Model" );
        MeshTypeIdentifier mt_ref2 = theMeshTypeIdentifierFactory.fromExternalForm( "org.infogrid.model/Some/Model" );
        MeshTypeIdentifier mt_ref3 = theMeshTypeIdentifierFactory.fromExternalForm( "http://foo.bar.example/com/123" );
        MeshTypeIdentifier mt_ref4 = theMeshTypeIdentifierFactory.fromExternalForm( "https://foobar" );
        MeshTypeIdentifier mt_ref5 = theMeshTypeIdentifierFactory.fromExternalForm( "https://foo.com/bar/12" );
        
        // Message 0
        
        ParserFriendlyXprisoMessage zero = ParserFriendlyXprisoMessage.create( id1, id2 );
        zero.setRequestId( 0 );
        zero.setResponseId( 33 );
        zero.addRequestedFirstTimeObject( theNetMeshObjectAccessSpecificationFactory.obtain( id2 ));
        zero.addRequestedFirstTimeObject( theNetMeshObjectAccessSpecificationFactory.obtain(
                new NetMeshBaseIdentifier[] { id3, id1 } ));
        
        zero.addPushLockObject( nmo_ref3 );
        zero.addPushLockObject( nmo_ref4 );
        
        zero.addRequestedCanceledObject( nmo_ref1 );
        zero.addRequestedLockObject( nmo_ref2 );
        
        // Message 1
        
        ParserFriendlyXprisoMessage one = ParserFriendlyXprisoMessage.create( null, null );
        one.setRequestId( 111 );
        one.setResponseId( 123456 );
        one.addReclaimedLockObject( nmo_ref2 );
        one.addReclaimedLockObject( nmo_ref3 );
        one.addRequestedResynchronizeReplica( nmo_ref1 );
        one.setCeaseCommunications( true );
        
        // Message 2
        
        ParserFriendlyXprisoMessage two = ParserFriendlyXprisoMessage.create( id3, id1 );
        two.setRequestId( 222 );
        two.setResponseId( 0 );
        two.addConveyedMeshObject( SimpleExternalizedNetMeshObject.create(
                nmo_ref1, // identifier
                new MeshTypeIdentifier[] {
                        mt_ref1,
                        mt_ref2 
                }, // typeNames
                12L, // timeCreated
                34L, // timeUpdated
                56L, // timeRead
                78L, // timeExpires
                new MeshTypeIdentifier[] {
                        mt_ref3,
                        mt_ref4
                }, // propertyTypes
                new PropertyValue[] {
                        StringValue.create( "test stringy" ),
                        FloatValue.create( 999.987 )
                }, // propertyValues
                new NetMeshObjectIdentifier[] {
                        nmo_ref6,
                        nmo_ref4
                }, // neighbors
                new MeshTypeIdentifier [][] {
                        new MeshTypeIdentifier [] { mt_ref5, mt_ref2, mt_ref1 },
                        null
                }, // roleTypes
                new NetMeshObjectIdentifier[] {
                        nmo_ref3,
                        nmo_ref5
                }, // equivalents
                false, // giveUpHomeReplica
                true, // giveUpLock
                new NetMeshBaseIdentifier[] {
                        id3,
                        id1
                }, // proxyNames
                1, // proxyTowardsHomeIndex
                0, // proxyTowardsLockIndex
                new NetMeshBaseIdentifier[][] {
                        new NetMeshBaseIdentifier [] { id3, id2 },
                        null
                })); // relationshipProxyNames
        
        // Message 3
        
        ParserFriendlyXprisoMessage three = ParserFriendlyXprisoMessage.create( id3, id1 );
        three.setRequestId( 333 );
        three.setResponseId( -27 );
        three.addDeleteChange( new NetMeshObjectDeletedEvent(
                null,
                id2,
                null,
                nmo_ref4,
                id3,
                null,
                5834L ));

        // Message 4
        
        ParserFriendlyXprisoMessage four = ParserFriendlyXprisoMessage.create( id2, id3 );
        four.setRequestId( 444 );
        four.setResponseId( -11111 );
        four.addConveyedMeshObject( SimpleExternalizedNetMeshObject.create(
                nmo_ref1, // identifier
                new MeshTypeIdentifier[] {
                        mt_ref2
                }, // typeNames
                112L, // timeCreated
                134L, // timeUpdated
                156L, // timeRead
                178L, // timeExpires
                new MeshTypeIdentifier[0], // propertyTypes
                new PropertyValue[0], // propertyValues
                new NetMeshObjectIdentifier[0], // neighbors
                new MeshTypeIdentifier [0][], // roleTypes
                new NetMeshObjectIdentifier[0], // equivalents
                true, // giveUpHomeReplica
                true, // giveUpLock
                new NetMeshBaseIdentifier[0], // proxyNames
                -1, // proxyTowardsHomeIndex
                -1, // proxyTowardsLockIndex
                new NetMeshBaseIdentifier[0][] )); // relationshipProxyNames

        // Message 5
        
        ParserFriendlyXprisoMessage five = ParserFriendlyXprisoMessage.create( null, null );
        five.setRequestId( 555 );
        five.addNeighborAddition( new NetMeshObjectNeighborAddedEvent(
                nmo_ref3,
                new MeshTypeIdentifier[] { mt_ref5, mt_ref1 },
                nmo_ref5,
                id3,
                17L,
                null ) );

        // Message 6

        ParserFriendlyXprisoMessage six = ParserFriendlyXprisoMessage.create( null, null );
        six.setRequestId( 666 );
        six.addNeighborRemoval( new NetMeshObjectNeighborRemovedEvent(
                nmo_ref2,
                nmo_ref3,
                id2,
                92L,
                null ) );
        
        // Message 7

        ParserFriendlyXprisoMessage seven = ParserFriendlyXprisoMessage.create( null, null );
        seven.setRequestId( 777 );
        seven.addRoleAddition( new NetMeshObjectRoleAddedEvent(
                nmo_ref4,
                new MeshTypeIdentifier[] { mt_ref4, mt_ref3 },
                nmo_ref1,
                id1,
                7777L,
                null ) );
        seven.addRoleRemoval( new NetMeshObjectRoleRemovedEvent(
                nmo_ref2,
                new MeshTypeIdentifier[] { mt_ref2, mt_ref5 },
                nmo_ref1,
                id2,
                8888L,
                null ) );
        
        // Message 8

        ParserFriendlyXprisoMessage eight = ParserFriendlyXprisoMessage.create( null, null );
        eight.setRequestId( 888 );
        eight.addTypeAddition( new NetMeshObjectTypeAddedEvent(
                nmo_ref3,
                new MeshTypeIdentifier[] { mt_ref4, mt_ref1 },
                id2,
                1L,
                null ) );
        eight.addTypeRemoval( new NetMeshObjectTypeRemovedEvent(
                nmo_ref3,
                new MeshTypeIdentifier[] { mt_ref1, mt_ref2 },
                id3,
                17L,
                null ) );

        // Message 7
        
        ParserFriendlyXprisoMessage nine = ParserFriendlyXprisoMessage.create( null, null );
        nine.setRequestId( 999 );
        nine.addPropertyChange( new NetMeshObjectPropertyChangeEvent(
                nmo_ref3,
                mt_ref4,
                TimePeriodValue.create( 12L ),
                TimePeriodValue.create( (short) 2008, (short) 1, (short) 2, (short) 3, (short) 4, 56.789f ),
                id1,
                17L,
                null ) );
        
        // Put response together
        return new XprisoMessage[] {
                zero,
                one,
                two,
                three,
                four,
                five,
                six,
                seven,
                eight,
                nine
        };
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        XprisoMessageSerializationTest1 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoMessageSerializationTest1( args );
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
     * @throws Exception all sorts of things may go wrong during a test
     */
    public XprisoMessageSerializationTest1(
            String [] args )
        throws
            Exception
    {
        super( XprisoMessageSerializationTest1.class );
        
        theNetMeshBase = NetMMeshBase.create( nmbid0, theModelBase, null, null, rootContext );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( XprisoMessageSerializationTest1.class );

    /**
     * Factory for NetMeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theNetMeshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.create();

    /**
     * A NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier nmbid0 = theNetMeshBaseIdentifierFactory.fromExternalForm( "http://here.local" );

    /**
     * A NetMeshBaseIdentifier.
     */
    protected NetMeshBaseIdentifier nmbid1 = theNetMeshBaseIdentifierFactory.fromExternalForm( "https://foo.exampe.com/%27" );

    /**
     * Factory for ExternalizedMeshObjects.
     */
    protected ParserFriendlyExternalizedNetMeshObjectFactory theExternalizedMeshObjectFactory
            = new ParserFriendlyExternalizedNetMeshObjectFactory() {
                    public ParserFriendlyExternalizedNetMeshObject createParserFriendlyExternalizedMeshObject() {
                        return new ParserFriendlyExternalizedNetMeshObject();
                    }
            };
    
    /**
     * Factory for NetMeshObjectIdentifiers.
     */
    protected NetMeshObjectIdentifierFactory theNetMeshObjectIdentifierFactory
            = DefaultAnetMeshObjectIdentifierFactory.create( nmbid1, theNetMeshBaseIdentifierFactory );

    /**
     * Factory for MeshTypeIdentifiers.
     */
    protected MeshTypeIdentifierFactory theMeshTypeIdentifierFactory
            = MMeshTypeIdentifierFactory.create();
    
    /**
     * Factory for NetMeshObjectAccessSpecifications.
     */
    protected NetMeshObjectAccessSpecificationFactory theNetMeshObjectAccessSpecificationFactory
            = DefaultNetMeshObjectAccessSpecificationFactory.create( nmbid1 );
    
    /**
     * The NetMeshBase used to decode incoming messages.
     */
    protected NetMeshBase theNetMeshBase;
}
