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

package org.infogrid.meshbase.net.externalized.xml;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.meshbase.net.externalized.ExternalizedProxyEncoder;
import org.infogrid.meshbase.net.externalized.ParserFriendlyExternalizedProxy;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.net.xpriso.xml.XprisoMessageXmlEncoder;
import org.infogrid.model.primitives.externalized.DecodingException;
import org.infogrid.model.primitives.externalized.EncodingException;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Utility methods to encode/decode Proxies to/from XML. Implements the SAX interface.
 */
public class ExternalizedProxyXmlEncoder
        extends
            XprisoMessageXmlEncoder
        implements
            ExternalizedProxyEncoder,
            ExternalizedProxyXmlTags
{
    private static final Log log = Log.getLogInstance( ExternalizedProxyXmlEncoder.class ); // our own, private logger

    /**
     * Constructor.
     */
    public ExternalizedProxyXmlEncoder()
    {
        // no op        
    }

    /**
     * Serialize an ExternalizedProxy to an OutputStream.
     * 
     * @param value the ExternalizedProxy
     * @param out the OutputStream to which to append the PropertyValue
     * @throws EncodingException thrown if a problem occurred during encoding
     * @throws IOException thrown if an I/O error occurred
     */
    public void encodeExternalizedProxy(
            ExternalizedProxy value,
            OutputStream      out )
        throws
            EncodingException,
            IOException
    {
        StringBuilder buf = new StringBuilder();

        appendExternalizedProxy( value, buf );

        OutputStreamWriter w = new OutputStreamWriter( out, ENCODING );
        w.write( buf.toString() );
        w.flush();
    }

    /**
     * Serialize an ExternalizedProxy to a StringBuilder.
     * 
     * @param value the ExternalizedProxy
     * @param buf the StringBuilder to which to append the ExternalizedProxy
     * @throws EncodingException thrown if a problem occurred during encoding
     */
    public void appendExternalizedProxy(
            ExternalizedProxy value,
            StringBuilder     buf )
        throws
            EncodingException
    {
        buf.append( "<" ).append( PROXY_INLINED_TAG );
        buf.append( " " ).append( TIME_CREATED_TAG ).append( "=\"" );
        appendLong( value.getTimeCreated(), buf );
        buf.append( "\"" );
        buf.append( " " ).append( TIME_UPDATED_TAG ).append( "=\"" );
        appendLong( value.getTimeUpdated(), buf );
        buf.append( "\"" );
        buf.append( " " ).append( TIME_READ_TAG    ).append( "=\"" );
        appendLong( value.getTimeRead(), buf );
        buf.append( "\"" );
        buf.append( " " ).append( TIME_EXPIRES_TAG ).append( "=\"" );
        appendLong( value.getTimeExpires(), buf );
        buf.append( "\"" );
        buf.append( " " ).append( HERE_TAG  ).append( "=\"" );
        appendNetworkIdentifier( value.getNetworkIdentifier(), buf );
        buf.append( "\"" );
        buf.append( " " ).append( THERE_TAG ).append( "=\"" );
        appendNetworkIdentifier( value.getNetworkIdentifierOfPartner(), buf );
        buf.append( "\"" );
        buf.append( ">\n" );

        appendExternalizedProxyEncodingHook( value, buf );

        buf.append( "  <" ).append( LAST_SENT_TOKEN_TAG     ).append( ">" );
        appendLong( value.getLastSentToken(), buf );
        buf.append( "</" ).append( LAST_SENT_TOKEN_TAG     ).append( ">\n" );
        buf.append( "  <" ).append( LAST_RECEIVED_TOKEN_TAG ).append( ">" );
        appendLong( value.getLastReceivedToken(), buf );
        buf.append( "</" ).append( LAST_RECEIVED_TOKEN_TAG ).append( ">\n" );

        if( value.getCoherenceSpecification() != null ) {
            buf.append( "  <" ).append( COHERENCE_SPECIFICATION_TAG ).append( ">" );
            buf.append( value.getCoherenceSpecification().toExternalForm() ); // this might not be the best way of encoding this?
            buf.append( "</" ).append( COHERENCE_SPECIFICATION_TAG     ).append( ">\n" );
        }
        
        buf.append( "  <" ).append( MESSAGES_TO_SEND_TAG ).append( ">\n" );
        Iterator<XprisoMessage> iter = value.messagesToBeSent().iterator();
        while( iter.hasNext() ) {
            appendXprisoMessage( iter.next(), buf );
        }
        buf.append( "  </" ).append( MESSAGES_TO_SEND_TAG ).append( ">\n" );

        buf.append( "  <" ).append( MESSAGES_LAST_SENT_TAG ).append( ">\n" );
        iter = value.messagesLastSent().iterator();
        while( iter.hasNext() ) {
            appendXprisoMessage( iter.next(), buf );
        }
        buf.append( "  </" ).append( MESSAGES_LAST_SENT_TAG ).append( ">\n" );

        buf.append( "</" ).append( PROXY_INLINED_TAG ).append( ">" );
    }        

    /**
     * Enable subclasses to add information when serializing the ExternalizedProxy.
     * 
     * @param value the ExternalizedProxy to be serialized
     * @param buf the StringBuilder to append to
     * @throws EncodingException thrown if a problem occurred during encoding
     */
    protected void appendExternalizedProxyEncodingHook(
            ExternalizedProxy value,
            StringBuilder     buf )
        throws
            EncodingException
    {
        // nothing on this level
    }

    /**
     * Deserialize a ExternalizedProxy from a stream.
     * 
     * @param contentAsStream the byte [] stream in which the ExternalizedProxy is encoded
     * @param mb the NetMeshBase on whose behalf the decoding is performed
     * @return return the just-instantiated ExternalizedProxy
     * @throws DecodingException thrown if a problem occurred during decoding
     * @throws IOException thrown if an I/O error occurred
     */
    public ExternalizedProxy decodeExternalizedProxy(
            InputStream contentAsStream,
            NetMeshBase mb )
        throws
            DecodingException,
            IOException
    {
        theMeshBase = mb;

        try {
            theParser.parse( contentAsStream, this );

            return theProxyBeingParsed;

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
    protected void startElement4(
            String     namespaceURI,
            String     localName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {
        if( PROXY_INLINED_TAG.equals( qName )) {
            theProxyBeingParsed = createParserFriendlyExternalizedProxy();

            String here  = attrs.getValue( HERE_TAG );
            String there = attrs.getValue( THERE_TAG );

            if( here != null ) {
                try {
                    theProxyBeingParsed.setNetworkIdentifier( ((NetMeshBase)theMeshBase).getMeshBaseIdentifierFactory().fromExternalForm( here ));
                } catch( StringRepresentationParseException ex ) {
                    log.warn( ex ); // we can do without this one
                }
            }
                
            if( there != null ) {
                try {
                    theProxyBeingParsed.setNetworkIdentifierOfPartner( ((NetMeshBase)theMeshBase).getMeshBaseIdentifierFactory().fromExternalForm( there ));
                } catch( StringRepresentationParseException ex ) {
                    error( ex ); // we cannot do without this one
                }
            } else {
                throw new SAXException( "No " + THERE_TAG + " attribute on node " + PROXY_INLINED_TAG );
            }

            theProxyBeingParsed.setTimeCreated( parseLong( attrs, TIME_CREATED_TAG, -1 ));
            theProxyBeingParsed.setTimeUpdated( parseLong( attrs, TIME_UPDATED_TAG, -1 ));
            theProxyBeingParsed.setTimeRead(    parseLong( attrs, TIME_READ_TAG,    -1 ));
            theProxyBeingParsed.setTimeExpires( parseLong( attrs, TIME_EXPIRES_TAG, -1 ));
            
        } else if( LAST_SENT_TOKEN_TAG.equals( qName )) {
            // no op
        } else if( LAST_RECEIVED_TOKEN_TAG.equals( qName )) {
            // no op
        } else if( COHERENCE_SPECIFICATION_TAG.equals( qName )) {
            // no op
        } else if( MESSAGES_TO_SEND_TAG.equals( qName )) {
            theMessagesBeingParsed = new ArrayList<XprisoMessage>();

        } else if( MESSAGES_LAST_SENT_TAG.equals( qName )) {
            theMessagesBeingParsed = new ArrayList<XprisoMessage>();

        } else {
            startElement5( namespaceURI, localName, qName, attrs );
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
    protected void startElement5(
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
    protected void endElement3(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        if( MESSAGE_TAG.equals( qName )) {
            theMessagesBeingParsed.add( theMessage );
        } else {
            super.endElement3( namespaceURI, localName, qName );
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
            // do nothing
            
        } else if( LAST_SENT_TOKEN_TAG.equals( qName )) {
            theProxyBeingParsed.setLastSentToken( parseLong( theCharacters.toString(), -1 ));

        } else if( LAST_RECEIVED_TOKEN_TAG.equals( qName )) {
            theProxyBeingParsed.setLastReceivedToken( parseLong( theCharacters.toString(), -1 ));
    
        } else if( COHERENCE_SPECIFICATION_TAG.equals( qName )) {
            theProxyBeingParsed.setCoherenceSpecification( CoherenceSpecification.fromExternalForm( theCharacters.toString() ));

        } else if( MESSAGES_TO_SEND_TAG.equals( qName )) {
            theProxyBeingParsed.setMessagesToSend( theMessagesBeingParsed );
            theMessagesBeingParsed = null;

        } else if( MESSAGES_LAST_SENT_TAG.equals( qName )) {
            theProxyBeingParsed.setMessagesLastSent( theMessagesBeingParsed );
            theMessagesBeingParsed = null;
            
        } else {
            endElement5( namespaceURI, localName, qName );
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
    protected void endElement5(
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
    protected ParserFriendlyExternalizedProxy createParserFriendlyExternalizedProxy()
    {
        ParserFriendlyExternalizedProxy ret = new ParserFriendlyExternalizedProxy();
        return ret;
    }

    /**
     * Reset the parser.
     */
    @Override
    public void clearState()
    {
        theProxyBeingParsed    = null;
        theMessagesBeingParsed = null;

        super.clearState();
    }

    /**
     * The ExternalizedProxy currently being parsed.
     */
    protected ParserFriendlyExternalizedProxy theProxyBeingParsed;
    
    /**
     * The list of XprisoMessages currently being parsed.
     */
    protected ArrayList<XprisoMessage> theMessagesBeingParsed;
}
