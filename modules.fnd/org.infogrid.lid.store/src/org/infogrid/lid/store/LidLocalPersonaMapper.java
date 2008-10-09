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

package org.infogrid.lid.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.StoreValue;
import org.infogrid.store.StoreValueDecodingException;
import org.infogrid.store.StoreValueEncodingException;
import org.infogrid.util.XmlUtils;
import org.infogrid.util.logging.Log;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Knows how to map LidLocalPersonWithCredentials to and from the Store.
 */
public class LidLocalPersonaMapper
        extends
            DefaultHandler
        implements
            StoreEntryMapper<String,StoreLidLocalPersona>,
            LidLocalPersonaTags
{
    private static final Log log = Log.getLogInstance( LidLocalPersonaMapper.class  ); // our own, private logger

    /**
     * Constructor.
     * 
     * @param credentialTypeClassLoader the ClassLoader to use to instantiate LidCredentialTypes
     */
    public LidLocalPersonaMapper(
            ClassLoader credentialTypeClassLoader )
    {
        theCredentialTypeClassLoader = credentialTypeClassLoader;
        
        try {
            theParser = theSaxParserFactory.newSAXParser();

        } catch( Throwable t ) {
            log.error( t );
        }
    }

    /**
     * Map a key to a String value that can be used for the Store.
     *
     * @param key the key object
     * @return the corresponding String value that can be used for the Store
     */
    public String keyToString(
            String key )
    {
        return key;
    }

    /**
     * Map a String value that can be used for the Store to a key object.
     *
     * @param stringKey the key in String form
     * @return the corresponding key object
     */
    public String stringToKey(
            String stringKey )
    {
        return stringKey;
    }

    /**
     * Map a StoreValue to a value.
     *
     * @param key the key to the StoreValue
     * @param value the StoreValue
     * @return the value
     * @throws StoreValueDecodingException thrown if the StoreValue could not been decoded
     */
    public StoreLidLocalPersona decodeValue(
            String     key,
            StoreValue value )
        throws
            StoreValueDecodingException
    {
        try {
            InputStream contentAsStream = value.getDataAsStream();

            theParser.parse( contentAsStream, this );
            
            StoreLidLocalPersona ret = new StoreLidLocalPersona(
                    theCurrentIdentifier,
                    theCurrentAttCreds.getAttributes(),
                    theCurrentAttCreds.getCredentials() );
            
            return ret;

        } catch( SAXException ex ) {
            throw new StoreValueDecodingException( ex );

        } catch( IOException ex ) {
            throw new StoreValueDecodingException( ex );

        } finally {
            clearState();
        }
    }
    
    /**
     * Override locator method so we know what we are parsing.
     *
     * @param locator the new Locator object
     */
    @Override
    public void setDocumentLocator(
            Locator locator )
    {
        theLocator = locator;
    }

    /**
     * SAX found some characters.
     *
     * @param ch the character array
     * @param start the start index
     * @param length the number of characters
     */
    @Override
    public final void characters(
            char [] ch,
            int     start,
            int     length )
    {
        try {
            if( length > 0 ) {
                if( theCharacters == null ) {
                    theCharacters = new StringBuilder();
                }
                theCharacters.append( ch, start, length );
            }
        } catch( RuntimeException ex ) {
            log.error( ex ); // internal error, no need to throw SAXParseException
        }
    }

    /**
     * SAX found a new element.
     *
     * @param namespaceURI URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @param attrs the Attributes at this element
     * @throws SAXException thrown if a parsing error occurrs
     */
    @Override
    public void startElement(
            String     namespaceURI,
            String     localName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {
        if( log.isInfoEnabled() ) {
            log.info( "SAX startElement: " + namespaceURI + ", " + localName + ", " + qName );
        }
        theCharacters = null; // if we haven't processed them so far, we never will

        if( PERSONA_TAG.equals( qName )) {
            if( theCurrentIdentifier != null ) {
                throw new SAXParseException( "Repeated tag " + PERSONA_TAG, theLocator );
            }
            theCurrentIdentifier = attrs.getValue( IDENTIFIER_TAG );
            theCurrentAttCreds = new AttributesCredentials();

        } else if( ATTRIBUTE_TAG.equals( qName )) {
            if( theCurrentAttribute != null ) {
                throw new SAXParseException( "Have current " + ATTRIBUTE_TAG + " already", theLocator );
            }
            theCurrentAttribute = attrs.getValue( IDENTIFIER_TAG );
            
        } else if( CREDENTIAL_TAG.equals( qName )) {
            if( theCurrentCredentialType != null ) {
                throw new SAXParseException( "Have current " + CREDENTIAL_TAG + " already", theLocator );
            }
            theCurrentCredentialType = instantiateCredentialType( attrs.getValue( IDENTIFIER_TAG ));

        } else {
            startElement1( namespaceURI, localName, qName, attrs );
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
    protected void startElement1(
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
     * SAX says an element ends.
     *
     * @param namespaceURI the URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @throws SAXException thrown if a parsing error occurrs
     */
    @Override
    public void endElement(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        if( log.isInfoEnabled() ) {
            log.info( "SAX endElement: " + namespaceURI + ", " + localName + ", " + qName );
        }

        if( PERSONA_TAG.equals( qName )) {
            // no op

        } else if( ATTRIBUTE_TAG.equals( qName )) {
            theCurrentAttCreds.addAttribute(
                    theCurrentAttribute,
                    theCharacters != null ? XmlUtils.cdataDescape( theCharacters.toString()) : "" );
            theCurrentAttribute = null;

        } else if( CREDENTIAL_TAG.equals( qName )) {
            theCurrentAttCreds.addCredential(
                    theCurrentCredentialType,
                    theCharacters != null ? XmlUtils.cdataDescape( theCharacters.toString()) : ""  );
            theCurrentCredentialType = null;

        } else {
            endElement1( namespaceURI, localName, qName );
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
    protected void endElement1(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        log.error( "unknown qname " + qName );
    }
    
    /**
     * Reset the parser.
     */
    public void clearState()
    {
        theCharacters            = null;
        theCurrentIdentifier     = null;
        theCurrentAttCreds       = null;
        theCurrentAttribute      = null;
        theCurrentCredentialType = null;

        theStack.clear();
    }

    /**
     * Throw exception in case of an Exception indicating an error.
     *
     * @param ex the Exception
     * @throws org.xml.sax.SAXParseException thrown if a parsing error occurs
     */
    public final void error(
            Throwable ex )
        throws
            SAXParseException
    {
        if( ex instanceof SAXParseException ) {
            throw ((SAXParseException)ex);
        } else {
            throw new FixedSAXParseException( theErrorPrefix, theLocator, ex );
        }
    }
    
    /**
     * Helper method to instantiate a LidCredentialType based on its name.
     * 
     * @param className name of the Class
     * @return the LidCredentialType
     * @throws org.xml.sax.SAXParseException thrown if a prob
     */
    protected LidCredentialType instantiateCredentialType(
            String className )
        throws
            SAXParseException
    {
        try {
            Class<?> foundClass    = Class.forName( className, true, theCredentialTypeClassLoader );
            Method   factoryMethod = foundClass.getMethod( "create" );
            
            Object ret = factoryMethod.invoke( null );
            return (LidCredentialType) ret;

        } catch( ClassNotFoundException ex ) {
            throw new FixedSAXParseException( "Could not find class " + className, theLocator, ex );

        } catch( NoSuchMethodException ex ) {
            throw new FixedSAXParseException( "Could not find method 'create' in class " + className, theLocator, ex );

        } catch( IllegalAccessException ex ) {
            throw new FixedSAXParseException( "Could not access method 'create' in class " + className, theLocator, ex );

        } catch( InvocationTargetException ex ) {
            throw new FixedSAXParseException( "Could not invoke method 'create' in class " + className, theLocator, ex );
        }
    }

    /**
     * Obtain the preferred encoding id of this StoreEntryMapper.
     * 
     * @return the preferred encoding id
     */
    public String getPreferredEncodingId()
    {
        return ENCODING;
    }

    /**
     * Obtain the time a value was created.
     *
     * @param value the time a value was created.
     * @return the time created, in System.currentTimeMillis() format
     */
    public long getTimeCreated(
            StoreLidLocalPersona value )
    {
        return -1L;
    }

    /**
     * Obtain the time a value was last updated.
     *
     * @param value the time a value was last updated.
     * @return the time updated, in System.currentTimeMillis() format
     */
    public long getTimeUpdated(
            StoreLidLocalPersona value )
    {
        return -1L;
    }

    /**
     * Obtain the time a value was last read.
     *
     * @param value the time a value was last read.
     * @return the time read, in System.currentTimeMillis() format
     */
    public long getTimeRead(
            StoreLidLocalPersona value )
    {
        return -1L;
    }

    /**
     * Obtain the time a value will expire.
     *
     * @param value the time a value will expire.
     * @return the time will expire, in System.currentTimeMillis() format
     */
    public long getTimeExpires(
            StoreLidLocalPersona value )
    {
        return -1L;
    }

    /**
     * Obtain the persona as a byte array.
     *
     * @param persona the value
     * @return the byte array
     * @throws StoreValueEncodingException thrown if the value could not been encoded
     */
    public byte [] asBytes(
            StoreLidLocalPersona persona )
        throws
            StoreValueEncodingException
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "<" ).append( PERSONA_TAG );
        buf.append(  " " ).append( IDENTIFIER_TAG ).append( "=\"" ).append( XmlUtils.escape( persona.getIdentifier() )).append( "\">\n");

        for( String name : persona.getAttributeKeys() ) {
            String value = persona.getAttribute( name );
            
            buf.append( "<" ).append( ATTRIBUTE_TAG );
            buf.append(  " " ).append( IDENTIFIER_TAG ).append( "=\"" ).append( XmlUtils.escape( name )).append( "\">\n");
            buf.append( XmlUtils.escape( value ));
            buf.append( "</" ).append( ATTRIBUTE_TAG ).append( ">" );
        }
        for( LidCredentialType type : persona.getCredentialTypes() ) {
            String value = persona.getCredentialFor( type );
            
            buf.append( "<" ).append( CREDENTIAL_TAG );
            buf.append(  " " ).append( IDENTIFIER_TAG ).append( "=\"" ).append( XmlUtils.escape( type.getFullName() )).append( "\">\n");
            buf.append( XmlUtils.escape( value ));
            buf.append( "</" ).append( CREDENTIAL_TAG ).append( ">" );
        }
        
        buf.append( "</" ).append( PERSONA_TAG ).append( ">" );
        try {
            return buf.toString().getBytes( CHARSET );

        } catch( UnsupportedEncodingException ex ) {
            throw new StoreValueEncodingException( ex );
        }
    }
    
    /**
     * The encoding to use.
     */
    public static final String ENCODING = LidLocalPersonaMapper.class.getName();

    /**
     * The character set to use.
     */
    public static final String CHARSET = "UTF-8";

    /**
     * The SAX parser factory to use.
     */
    protected static final SAXParserFactory theSaxParserFactory;
    static {
        theSaxParserFactory = SAXParserFactory.newInstance();
        theSaxParserFactory.setValidating( true );
    }

    /**
     * Our SAX parser.
     */
    protected SAXParser theParser;
    
    /**
     * The ClassLoader to use to instantiate a LidCredentialType.
     */
    protected ClassLoader theCredentialTypeClassLoader;

    /**
     * The identifier of the LidLocalPersona currently being parsed.
     */
    protected String theCurrentIdentifier;
    
    /**
     * The AttributesCredentials of the LidLocalPersona currently being parsed.
     */
    protected AttributesCredentials theCurrentAttCreds;
    
    /**
     * The name of the currently being parsed attribute in theObjectBeingParsed.
     */
    protected String theCurrentAttribute;
    
    /**
     * The name of the currently being parsed credential in theObjectBeingParsed.
     */
    protected LidCredentialType theCurrentCredentialType;

    /**
     * The error message prefix in case we need it.
     */
    protected String theErrorPrefix;

    /**
     * The Locator object that tells us where we are in the parsed file.
     */
    protected Locator theLocator;

    /**
     * The parse stack.
     */
    protected Stack<Object> theStack = new Stack<Object>();

    /**
     * The character String that is currently being parsed, if any.
     */
    protected StringBuilder theCharacters = null;

    /**
     * Java FixedSAXParseException's constructor is broken, so we created this workaround class.
     */
    static class FixedSAXParseException
            extends
                SAXParseException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         * 
         * @param message the error message
         * @param locator indicates the location of the error in the stream
         * @param cause the underlying cause, if any
         */
        public FixedSAXParseException(
                String    message,
                Locator   locator,
                Throwable cause )
        {
            super( message, locator );
            
            initCause( cause );
        }
    }
}