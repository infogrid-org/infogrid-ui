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

package org.infogrid.mesh.net.externalized.xml;

import java.io.IOException;
import java.io.InputStream;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.externalized.ExternalizedMeshObject;
import org.infogrid.mesh.externalized.xml.ExternalizedMeshObjectXmlEncoder;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObjectEncoder;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.externalized.DecodingException;
import org.infogrid.model.primitives.externalized.EncodingException;
import org.infogrid.util.XmlUtils;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Utility methods to encode/decode an ExternalizedNetMeshObject to/from XML. Implements the SAX interface.
 */
public class ExternalizedNetMeshObjectXmlEncoder
        extends
            ExternalizedMeshObjectXmlEncoder
        implements
            ExternalizedNetMeshObjectEncoder,
            ExternalizedNetMeshObjectXmlTags
{
    private static final Log log = Log.getLogInstance( ExternalizedNetMeshObjectXmlEncoder.class ); // our own, private logger

    /**
     * Constructor.
     */
    public ExternalizedNetMeshObjectXmlEncoder()
    {
        // no op
    }

    /**
     * Serialize the opening tag, to make it easy for subclasses to add to the attributes list.
     *
     * @param obj the AMeshObject to encode
     * @param meshObjectTagName the XML top-level tag to use for this ExternalizedMeshObject
     * @param buf the StringBuilder to which to append the ExternalizedMeshObject
     */
    @Override
    protected void encodeOpeningTag(
            ExternalizedMeshObject obj,
            String                 meshObjectTagName,
            StringBuilder          buf )
    {
        ExternalizedNetMeshObject realObject = (ExternalizedNetMeshObject) obj;
        
        buf.append( "<" );
        buf.append( meshObjectTagName );
        buf.append( " " );
        buf.append( IDENTIFIER_TAG );
        buf.append( "=\"" );
        appendIdentifier( obj.getIdentifier(), buf );
        buf.append( "\" " );
        buf.append( TIME_CREATED_TAG );
        buf.append( "=\"" );
        appendLong( obj.getTimeCreated(), buf );
        buf.append( "\" " );
        buf.append( TIME_UPDATED_TAG );
        buf.append( "=\"" );
        appendLong( obj.getTimeUpdated(), buf );
        buf.append( "\" " );
        buf.append( TIME_READ_TAG );
        buf.append( "=\"" );
        appendLong( obj.getTimeRead(), buf );
        buf.append( "\" " );
        buf.append( TIME_EXPIRES_TAG );
        buf.append( "=\"" );
        appendLong( obj.getTimeExpires(), buf );
        if( realObject.getGiveUpLock()) {
            buf.append( "\" " );
            buf.append( GIVE_UP_LOCK_TAG );
            buf.append( "=\"" );
            buf.append( YES_TAG );
        }
        if( realObject.getGiveUpHomeReplica()) {
            buf.append( "\" " );
            buf.append( GIVE_UP_HOME_TAG );
            buf.append( "=\"" );
            buf.append( YES_TAG );
        }
        buf.append( "\">\n" );
    }

    /**
     * Serialize the neighbors section.
     *
     * @param obj the ExternalizedMeshObject to encode
     * @param buf the StringBuilder to which to append the ExternalizedMeshObject
     * @throws EncodingException thrown if a problem occurred during encoding
     */
    @Override
    protected void encodeExternalizedMeshObjectNeighbors(
            ExternalizedMeshObject obj,
            StringBuilder          buf  )
        throws
            EncodingException
    {
        ExternalizedNetMeshObject realObj = (ExternalizedNetMeshObject) obj;

        NetMeshObjectIdentifier [] neighbors = realObj.getNeighbors();
        if( neighbors != null ) {
            for( int i=0 ; i<neighbors.length ; ++i ) {
                NetMeshObjectIdentifier  currentNeighbor            = neighbors[i];
                MeshTypeIdentifier []    currentRoleTypes           = realObj.getRoleTypesFor( currentNeighbor );
                NetMeshBaseIdentifier [] currentRelationshipProxies = realObj.getRelationshipProxyIdentifiersFor( currentNeighbor );

                buf.append( " <" );
                buf.append( RELATIONSHIP_TAG );
                buf.append( " " );
                buf.append( IDENTIFIER_TAG );
                buf.append( "=\"" );
                appendIdentifier( currentNeighbor, buf );
                buf.append( "\"" );
                if(    ( currentRoleTypes           == null || currentRoleTypes.length           == 0 )
                    && ( currentRelationshipProxies == null || currentRelationshipProxies.length == 0 ))
                {
                    buf.append( "/>\n" );

                } else {
                    buf.append( ">\n" );
                    if( currentRoleTypes != null ) {
                        for( int j=0 ; j<currentRoleTypes.length ; ++j ) {
                            buf.append( "  <" );
                            buf.append( TYPE_TAG );
                            buf.append( ">" );
                            appendIdentifier( currentRoleTypes[j], buf );
                            buf.append( "</" );
                            buf.append( TYPE_TAG );
                            buf.append( ">\n" );
                        }
                    }
                    if( currentRelationshipProxies != null ) {
                        for( int j=0 ; j<currentRelationshipProxies.length ; ++j ) {
                            buf.append( "  <" );
                            buf.append( RELATIONSHIP_PROXY_REFERENCE_TAG );
                            buf.append( " " ).append( PROXY_NETWORK_IDENTIFIER_TAG ).append( "=\"");
                            appendNetworkIdentifier( currentRelationshipProxies[j], buf );
                            buf.append( "\"/>\n" );
                        }
                    }
                    buf.append( " </" );
                    buf.append( RELATIONSHIP_TAG );
                    buf.append( ">\n" );
                }
            }
        }
    }

    /**
     * Hook to enable subclasses to add to the encoding of an ExternalizedMeshObject.
     *
     * @param obj the ExternalizedMeshObject to encode
     * @param buf the StringBuilder to which to append the ExternalizedMeshObject
     */
    @Override
    protected void appendExternalizedMeshObjectEncodingHook(
            ExternalizedMeshObject obj,
            StringBuilder          buf )
    {
        ExternalizedNetMeshObject realObject = (ExternalizedNetMeshObject) obj;

        NetMeshBaseIdentifier [] proxyNames = realObject.getProxyIdentifiers();
        NetMeshBaseIdentifier    homeProxy  = realObject.getProxyTowardsHomeNetworkIdentifier();
        NetMeshBaseIdentifier    lockProxy  = realObject.getProxyTowardsLockNetworkIdentifier();
        
        if( proxyNames != null ) {
            for( int i=0 ; i<proxyNames.length ; ++i ) {
                NetMeshBaseIdentifier current = proxyNames[i];

                buf.append( " <" ).append( PROXY_REFERENCE_TAG );
                buf.append( " " ).append( PROXY_NETWORK_IDENTIFIER_TAG ).append( "=\"");
                appendNetworkIdentifier( current, buf );
                buf.append( "\"" );

                if( current == homeProxy ) {
                    buf.append( " " ).append( PROXY_TOWARDS_HOME_TAG ).append( "=\"" ).append( YES_TAG ).append( "\"" );
                }
                if( current == lockProxy ) {
                    buf.append( " " ).append( PROXY_TOWARDS_LOCK_TAG ).append( "=\"" ).append( YES_TAG ).append( "\"" );
                }
                buf.append( "/>\n" );
            }
        }
    }
    
    /**
     * Append a NetMeshBaseIdentifier encoded as a String to a StringBuilder.
     * 
     * @param id the NetMeshBaseIdentifier
     * @param buf the StringBuilder
     */
    protected void appendNetworkIdentifier(
            NetMeshBaseIdentifier id,
            StringBuilder         buf )
    {
        if( id != null ) {
            String externalForm = id.getCanonicalForm();
            buf.append( XmlUtils.escape( externalForm ));
        }
    }

    /**
     * Append a NetMeshObjectAccessSpecification encoded as a String to a StringBuilder.
     * 
     * @param path the NetMeshObjectAccessSpecification
     * @param buf the StringBuilder
     */
    protected void appendNetMeshObjectAccessSpecification(
            NetMeshObjectAccessSpecification path,
            StringBuilder                    buf )
    {
        if( path != null ) {
            String externalForm = path.toExternalForm();
            buf.append( XmlUtils.escape( externalForm ));
        }
    }

    /**
     * Deserialize a ExternalizedMeshObject from a stream.
     * 
     * @param contentAsStream the byte [] stream in which the ExternalizedProxy is encoded
     * @param mb the NetMeshBase on whose behalf the decoding is performed
     * @return return the just-instantiated ExternalizedMeshObject
     * @throws DecodingException thrown if a problem occurred during decoding
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public ExternalizedNetMeshObject decodeExternalizedMeshObject(
            InputStream contentAsStream,
            MeshBase    mb )
        throws
            DecodingException,
            IOException
    {
        ExternalizedMeshObject ret = super.decodeExternalizedMeshObject( contentAsStream, (NetMeshBase) mb ); // cast to make sure this is invoked right
        return (ExternalizedNetMeshObject) ret;
    }
    
    /**
     * Invoked when no previous start-element parsing rule has matched. Allows subclasses to add to parsing.
     *
     * @param namespaceURI the URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @param attrs the Attributes at this element
     * @throws SAXException thrown if a parsing error occurrs
     */
    @Override
    protected final void startElement1(
            String     namespaceURI,
            String     localName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {
        super.startElement1( namespaceURI, localName, qName, attrs );

        // patch after the fact
        if( MESHOBJECT_TAG.equals( qName )) {
            String giveUpLock = attrs.getValue( GIVE_UP_LOCK_TAG );
            if( YES_TAG.equals( giveUpLock )) {
                ParserFriendlyExternalizedNetMeshObject realObjectBeingParsed = (ParserFriendlyExternalizedNetMeshObject) theMeshObjectBeingParsed;
                realObjectBeingParsed.setGiveUpLock( true );
            }
            String giveUpHome = attrs.getValue( GIVE_UP_HOME_TAG );
            if( YES_TAG.equals( giveUpHome )) {
                ParserFriendlyExternalizedNetMeshObject realObjectBeingParsed = (ParserFriendlyExternalizedNetMeshObject) theMeshObjectBeingParsed;
                realObjectBeingParsed.setGiveUpHome( true );
            }
        }
    }

    /**
     * Invoked when no previous start-element parsing rule has matched. Allows subclasses to add to parsing.
     *
     * @param namespaceURI the URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @param attrs the Attributes at this element
     * @throws SAXException thrown if a parsing error occurrs
     */
    @Override
    protected final void startElement2(
            String     namespaceURI,
            String     localName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {
        ParserFriendlyExternalizedNetMeshObject realObjectBeingParsed = (ParserFriendlyExternalizedNetMeshObject) theMeshObjectBeingParsed;

        if( PROXY_REFERENCE_TAG.equals( qName )) {
            String proxyString = attrs.getValue( PROXY_NETWORK_IDENTIFIER_TAG );
            String homeProxy   = attrs.getValue( PROXY_TOWARDS_HOME_TAG );
            String lockProxy   = attrs.getValue( PROXY_TOWARDS_LOCK_TAG );

            try {
                NetMeshBaseIdentifier proxyId = ((NetMeshBase)theMeshBase).getMeshBaseIdentifierFactory().fromExternalForm( proxyString );
                realObjectBeingParsed.addProxyNetworkIdentifier( proxyId, YES_TAG.equals( homeProxy ), YES_TAG.equals( lockProxy ));

            } catch( StringRepresentationParseException ex ) {
                error( ex );
            }

        } else if( RELATIONSHIP_PROXY_REFERENCE_TAG.equals( qName )) {

            String proxyString = attrs.getValue( PROXY_NETWORK_IDENTIFIER_TAG );
            ParserFriendlyExternalizedNetMeshObject.RelationshipWithRelationshipProxies realRelationship
                    = (ParserFriendlyExternalizedNetMeshObject.RelationshipWithRelationshipProxies) theHasTypesBeingParsed;

            try {
                NetMeshBaseIdentifier proxyId = ((NetMeshBase)theMeshBase).getMeshBaseIdentifierFactory().fromExternalForm( proxyString );
                realRelationship.addRelationshipProxyIdentifier( proxyId );

            } catch( StringRepresentationParseException ex ) {
                error( ex );
            }

        } else {
            startElement3( namespaceURI, localName, qName, attrs );
        }
    }

    /**
     * Invoked when no previous start-element parsing rule has matched. Allows subclasses to add to parsing.
     *
     * @param namespaceURI the URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @param attrs the Attributes at this element
     * @throws SAXException thrown if a parsing error occurrs
     */
    protected void startElement3(
            String     namespaceURI,
            String     localName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {
        log.error( "unknown qname " + qName );
    }

    /**
     * Invoked when no previous end-element parsing rule has matched. Allows subclasses to add to parsing.
     *
     * @param namespaceURI the URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @throws SAXException thrown if a parsing error occurrs
     */
    @Override
    protected final void endElement2(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        if( PROXY_REFERENCE_TAG.equals( qName )) {
            // no op

        } else if( RELATIONSHIP_PROXY_REFERENCE_TAG.equals( qName )) {
            // no op

        } else {
            endElement3( namespaceURI, localName, qName );
        }
    }

    /**
     * Invoked when no previous end-element parsing rule has matched. Allows subclasses to add to parsing.
     *
     * @param namespaceURI the URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @throws SAXException thrown if a parsing error occurrs
     */
    protected void endElement3(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        log.error( "unknown qname " + qName );
    }

    /**
     * Factors out the creation of Relationship instances, so subclasses can override it.
     *
     * @param identifier the MeshObjectIdentifier on this side of the relationship
     * @param neighborIdentifier the MeshObjectIdentifier on the other side of the relationship
     * @param timeUpdated the time it was last updated
     * @return the created Relationship object
     */
    @Override
    protected ParserFriendlyExternalizedNetMeshObject.RelationshipWithRelationshipProxies createRelationship(
            MeshObjectIdentifier identifier,
            MeshObjectIdentifier neighborIdentifier,
            long                 timeUpdated )
    {
        return new ParserFriendlyExternalizedNetMeshObject.RelationshipWithRelationshipProxies( identifier, neighborIdentifier, timeUpdated );
    }
}
