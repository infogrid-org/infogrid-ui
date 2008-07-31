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

package org.infogrid.probe.shadow.externalized.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObjectFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.meshbase.net.externalized.xml.ExternalizedProxyXmlEncoder;
import org.infogrid.model.primitives.externalized.DecodingException;
import org.infogrid.model.primitives.externalized.EncodingException;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;
import org.infogrid.probe.shadow.externalized.ExternalizedShadowMeshBase;
import org.infogrid.probe.shadow.externalized.ExternalizedShadowProxy;
import org.infogrid.probe.shadow.externalized.ParserFriendlyExternalizedShadowMeshBase;
import org.infogrid.probe.shadow.externalized.ParserFriendlyExternalizedShadowProxy;
import org.infogrid.util.logging.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Utility methods to encode/decode the content of a ShadowMeshBase to/from XML. Implements the SAX interface.
 */
public class ExternalizedShadowMeshBaseXmlEncoder
        extends
            ExternalizedProxyXmlEncoder
        implements
            ExternalizedShadowMeshBaseXmlTags
{
    private static final Log log = Log.getLogInstance( ExternalizedShadowMeshBaseXmlEncoder.class ); // our own, private logger

    /**
     * Constructor.
     */
    public ExternalizedShadowMeshBaseXmlEncoder()
    {
        // noop
    }

    /**
     * Serialize an ExternalizedShadowMeshBase to an OutputStream.
     * 
     * @param mb the ExternalizedShadowMeshBase
     * @param includeProxies if false, do not append the Proxies
     * @param out the OutputStream to which to append the ExternalizedShadowMeshBase
     * @throws EncodingException thrown if a problem occurred during encoding
     * @throws IOException thrown if an I/O error occurred
     */
    public void encodeShadowMeshBase(
            ExternalizedShadowMeshBase mb,
            boolean                    includeProxies,
            OutputStream               out )
        throws
            IOException,
            EncodingException
    {
        StringBuilder buf = new StringBuilder();

        appendShadowMeshBase( mb, includeProxies, buf );

        OutputStreamWriter w = new OutputStreamWriter( out, ENCODING );
        w.write( buf.toString() );
        w.flush();
    }

    /**
     * Serialize an ExternalizedShadowMeshBase to a StringBuilder.
     * 
     * @param mb the ExternalizedShadowMeshBase
     * @param includeProxies if false, do not append the Proxies
     * @param buf the StringBuilder to write to
     * @throws EncodingException thrown if a problem occurred during encoding
     */
    public void appendShadowMeshBase(
            ExternalizedShadowMeshBase mb,
            boolean                    includeProxies,
            StringBuilder              buf )
        throws
            EncodingException
    {
        buf.append( "<" ).append( SHADOW_TAG );
        buf.append( " " ).append( SHADOW_IDENTIFIER_TAG ).append( "=\"" );
        appendNetworkIdentifier( mb.getNetworkIdentifier(), buf );        
        buf.append( "\">\n" );

        if( includeProxies ) {
            ExternalizedProxy [] proxies = mb.getExternalizedProxies();
            for( int i=0 ; i<proxies.length ; ++i ) {
                appendExternalizedProxy( proxies[i], buf );
            }
        }
        
        ExternalizedNetMeshObject [] objects = mb.getExternalizedNetMeshObjects();
        for( int i=0 ; i<objects.length ; ++i ) {
            appendExternalizedMeshObject( objects[i], buf );
        }
        
        buf.append( "</" ).append( SHADOW_TAG ).append( ">\n" );
    }

    /**
     * Enables subclasses to add information to an ExternaliProxy.
     * 
     * @param value the ExternalizedProxy to serialize
     * @param buf the StringBuilder to write to
     * @throws EncodingException thrown if a problem occurred during encoding
     */
    @Override
    protected void appendExternalizedProxyEncodingHook(
            ExternalizedProxy value,
            StringBuilder     buf )
        throws
            EncodingException
    {
        if( value instanceof ExternalizedShadowProxy ) {
            ExternalizedShadowProxy realValue = (ExternalizedShadowProxy) value;
            
            if( realValue.getIsPlaceholder() ) {
                buf.append( "<" ).append( PROXY_PLACEHOLDER_TAG ).append( "/>\n" );
            }
        }
    }

    /**
     * Deserialize an ExternalizedShadowMeshBase from a stream.
     * 
     * @param contentAsStream the byte [] stream in which the ExternalizedProxy is encoded
     * @param externalizedMeshObjectFactory the factory to use for ExternalizedMeshObjects
     * @param meshObjectIdentifierFactory the factory to use for MeshObjectIdentifier
     * @param meshTypeIdentifierFactory the factory to use for MeshTypes
     * @return return the just-instantiated ExternalizedShadowMeshBase
     * @throws DecodingException thrown if a problem occurred during decoding
     * @throws IOException thrown if an I/O error occurred
     */
    public ExternalizedShadowMeshBase decodeShadowMeshBase(
            InputStream                                    contentAsStream,
            ParserFriendlyExternalizedNetMeshObjectFactory externalizedMeshObjectFactory,
            NetMeshObjectIdentifierFactory                 meshObjectIdentifierFactory,
            MeshTypeIdentifierFactory                      meshTypeIdentifierFactory )
        throws
            DecodingException,
            IOException
    {
        theExternalizedMeshObjectFactory = externalizedMeshObjectFactory;
        theMeshObjectIdentifierFactory   = meshObjectIdentifierFactory;
        theMeshTypeIdentifierFactory     = meshTypeIdentifierFactory;

        try {
            theParser.parse( contentAsStream, this );

            return this.theParsedShadowMeshBase;
            
        } catch( SAXException ex ) {
            throw new DecodingException( ex );

        } finally {
            clearState();
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
    protected void startElement5(
            String     namespaceURI,
            String     localName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {        
        if( SHADOW_TAG.equals( qName )) {
            String id = attrs.getValue( SHADOW_IDENTIFIER_TAG );

            theParsedShadowMeshBase = ParserFriendlyExternalizedShadowMeshBase.create();
            
            if( id != null && id.length() > 0 ) {
                try {
                    theParsedShadowMeshBase.setNetworkIdentifier( NetMeshBaseIdentifier.fromExternalForm( id ));

                } catch( URISyntaxException ex ) {
                    error( ex );
                }
            }

        } else if( PROXY_PLACEHOLDER_TAG.equals( qName )) {
            ((ParserFriendlyExternalizedShadowProxy) theProxyBeingParsed).setIsPlaceholder( true );
            
        } else {
            startElement6( namespaceURI, localName, qName, attrs );
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
    protected void startElement6(
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
    protected void endElement1(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        if( MESHOBJECT_TAG.equals( qName )) {
            theParsedShadowMeshBase.addExternalizedNetMeshObject( (ExternalizedNetMeshObject) theMeshObjectBeingParsed );    

        } else {
            super.endElement1( namespaceURI, localName, qName );
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
    @Override
    protected void endElement4(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        if( PROXY_INLINED_TAG.equals( qName )) {
            if( theParsedShadowMeshBase != null ) {
                theParsedShadowMeshBase.addExternalizedProxies( theProxyBeingParsed );
                // FIXME: this if should not be necessary: I believe we invoke
                // ExternalizedShadowMeshBaseXmlEncoder for the encoding/decoding of
                // Shadow Proxies by themselves, and it appears we should not use this
                // class for it
            }
            
        } else {
            super.endElement4( namespaceURI, localName, qName );
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
    @Override
    protected void endElement5(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        if( SHADOW_TAG.equals( qName )) {
            // noop

        } else if( PROXY_PLACEHOLDER_TAG.equals( qName )) {
            // noop
            
        } else {
            endElement6( namespaceURI, localName, qName );
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
    protected void endElement6(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        log.error( "unknown qname " + qName );
    }

    /**
     * Factory method for ExternalizedProxy. This may be overridden by subclasses.
     * 
     * @return the ParserFriendlyExternalizedProxy
     */
    @Override
    protected ParserFriendlyExternalizedShadowProxy createParserFriendlyExternalizedProxy()
    {
        ParserFriendlyExternalizedShadowProxy ret = new ParserFriendlyExternalizedShadowProxy();
        return ret;
    }

    /**
     * Reset the parser.
     */
    @Override
    public void clearState()
    {
        theParsedShadowMeshBase = ParserFriendlyExternalizedShadowMeshBase.create();
        
        super.clearState();
    }

    /**
     * The ExternalizedShadowMeshBase being parsed.
     */
    protected ParserFriendlyExternalizedShadowMeshBase theParsedShadowMeshBase = null;
}
