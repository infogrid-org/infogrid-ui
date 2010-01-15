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

package org.infogrid.meshbase.net;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Default implementation of NetMeshBaseIdentifierFactory.
 */
public class DefaultNetMeshBaseIdentifierFactory
        implements
            NetMeshBaseIdentifierFactory
{
    /**
     * Factory method for default protocols.
     * 
     * @return the created DefaultNetMeshBaseIdentifierFactory
     */
    public static DefaultNetMeshBaseIdentifierFactory create()
    {
        DefaultNetMeshBaseIdentifierFactory ret = new DefaultNetMeshBaseIdentifierFactory( DEFAULT_PROTOCOLS );
        return ret;
    }
    
    /**
     * Factory method for a specified set of protocols.
     * 
     * @param protocols the supported protocols
     * @return the created DefaultNetMeshBaseIdentifierFactory
     */
    public static DefaultNetMeshBaseIdentifierFactory create(
            Protocol [] protocols )
    {
        DefaultNetMeshBaseIdentifierFactory ret = new DefaultNetMeshBaseIdentifierFactory( protocols );
        return ret;
    }
    
    /**
     * Constructor.
     * 
     * @param protocols the supported protocols
     */
    protected DefaultNetMeshBaseIdentifierFactory(
            Protocol [] protocols )
    {
        theSupportedProtocols = protocols;
    }

    /**
     * Factory method to obtain a NetMeshBaseIdentifier that is resolvable into a stream,
     * e.g. http URL. This method attempts to guess the protocol if none has been provided.
     * 
     * @param string the (potentially incomplete) String form of this NetMeshBaseIdentifier
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier guessFromExternalForm(
            String string )
        throws
            StringRepresentationParseException
    {
        return obtain( null, string, true );
    }

    /**
     * Factory method to obtain a NetMeshBaseIdentifier specified in relative form in the
     * context of another NetMeshBaseIdentifier.
     * 
     * @param context the NetMeshBaseIdentifier that forms the context
     * @param string the (potentially incomplete) String form of this NetMeshBaseIdentifier
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier guessFromExternalForm(
            NetMeshBaseIdentifier context,
            String                string )
        throws
            StringRepresentationParseException
    {
        return obtain( context, string, true );
    }

    /**
     * Fully-specified factory method.
     * 
     * @param context the NetMeshBaseIdentifier that forms the context
     * @param string the (potentially incomplete) String form of this NetMeshBaseIdentifier
     * @param guess if true, attempt to guess the protocol if none was given
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    protected NetMeshBaseIdentifier obtain(
            NetMeshBaseIdentifier context,
            String                string,
            boolean               guess )
        throws
            StringRepresentationParseException
    {
        if( string == null ) {
            throw new NullPointerException();
        }
        string = string.trim();
        if( string != null ) {
            Matcher m = thePort80Pattern.matcher( string );
            if( m.matches() ) {
                String zapped = m.group( 1 ) + m.group( 2 );

                string = zapped;
            } else {
                m = thePort443Pattern.matcher( string );
                if( m.matches() ) {
                    String zapped = m.group( 1 ) + m.group( 2 );

                    string = zapped;
                }
            }
        }

        if( string.length() == 0 ) {
            throw new StringRepresentationParseException( string, null, null );
        }

        if( isXriGlobalContextSymbol( string.charAt( 0 ))) {
            try {
                return new NetMeshBaseIdentifier( this, string, new URI( theXriResolverPrefix + string ), true );
                
            } catch( URISyntaxException ex ) {
                throw new StringRepresentationParseException( string, null, ex );
            }
        }
        if( string.startsWith( theXriResolverPrefix )) {
            string = string.substring( theXriResolverPrefix.length() );
            try {
                return new NetMeshBaseIdentifier( this, string, new URI( theXriResolverPrefix + string ), true );

            } catch( URISyntaxException ex ) {
                throw new StringRepresentationParseException( string, null, ex );
            }
        }

        String lower = string.toLowerCase();
        
        if( guess ) {
            if( lower.indexOf( "://" ) < 0 ) {
                String prefix = null;
                if( context != null ) {
                    String contextString = context.toExternalForm();
                    int lastSlash = contextString.lastIndexOf( '/' );
                    if( lastSlash > 0 ) {
                        prefix = contextString.substring( 0, lastSlash );
                    }
                }
                if( prefix != null ) {
                    string = prefix + "/" + string;
                } else if( string.contains( "/" )) {
                    string = "http://" + string;
                } else {
                    string = "http://" + string + "/"; // example "cnn.com" without trailing slash
                }

                lower = string.toLowerCase();
            }
        }
        
        for( Protocol p : theSupportedProtocols ) {
            if( lower.startsWith( p.getName() + ":" )) {
                try {
                    return new NetMeshBaseIdentifier( this, string, new URI( string ), p.getIsRestfullyResolvable() );

                } catch( URISyntaxException ex ) {
                    throw new StringRepresentationParseException( string, null, ex );
                }
            }
        }
        throw new StringRepresentationParseException(
                string,
                null,
                new IllegalArgumentException(
                        "canonical identifier uses unknown protocol (need one of "
                        + ArrayHelper.join( theSupportedProtocols )
                        + ")" ));
    }

    /**
     * Factory method.
     * 
     * @param file the local File whose NetMeshBaseIdentifier we obtain
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier obtain(
            File file )
        throws
            StringRepresentationParseException
    {
        return obtain( file.toURI() );
    }

    /**
     * Factory method.
     * 
     * @param url the URL whose NetMeshBaseIdentifier we obtain
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier obtain(
            URL url )
        throws
            StringRepresentationParseException
    {
        return obtain( null, url.toExternalForm(), false );
    }

    /**
     * Factory method.
     * 
     * @param uri the URI whose NetMeshBaseIdentifier we obtain
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier obtain(
            URI uri )
        throws
            StringRepresentationParseException
    {
        return obtain( null, uri.toASCIIString(), false );
    }

    /**
     * Recreate a NetMeshBaseIdentifier from an external form. Be strict about syntax.
     *
     * @param raw the external form
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier fromExternalForm(
            String raw )
        throws
            StringRepresentationParseException
    {
        return obtain( null, raw, false );
    }

    /**
     * Recreate a NetMeshBaseIdentifier from an external form. Be strict about syntax.
     *
     * @param context the NetMeshBaseIdentifier that forms the context
     * @param raw the external form
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier fromExternalForm(
            NetMeshBaseIdentifier context,
            String                raw )
        throws
            StringRepresentationParseException
    {
        return obtain( context, raw, false );
    }

    /**
     * Convert this StringRepresentation back to a NetMeshBaseIdentifier.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            StringRepresentationParseException
    {
        try {
            Object [] found = representation.parseEntry( NetMeshBaseIdentifier.class, NetMeshBaseIdentifier.DEFAULT_ENTRY, s, this );

            NetMeshBaseIdentifier ret;
            switch( found.length ) {
                case 1:
                    ret = fromExternalForm( (String) found[0] );
                    break;

                default:
                    throw new StringRepresentationParseException( s, null, null );
            }

            return ret;

        // pass-through StringRepresentationParseException

        } catch( ClassCastException ex ) {
            throw new StringRepresentationParseException( s, null, null );
        }
        
    }

    /**
     * Determine whether a character is an XRI global context symbol.
     *
     * @param c the character
     * @return true if this character is an XRI global context symbol
     */
    public static boolean isXriGlobalContextSymbol(
            char c )
    {
        switch( c ) {
            case '=':
            case '@':
            case '+':
            case '$':
            case '!':
                return true;

            default:
                return false;
        }
    }

    /**
     * Helper method to remove .. and . from paths.
     *
     * @param in the input String
     * @return the output String
     */
    protected static String stripDirectoryPaths(
            String in )
    {
        return in; // FIXME
    }

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( DefaultNetMeshBaseIdentifierFactory.class );
    
    /**
     * The XRI resolver location.
     */
    private static final String theXriResolverPrefix = theResourceHelper.getResourceStringOrDefault(
            "XriResolverPrefix",
            "http://xri.net/" );

    /**
     * The supported protocols.
     */
    protected Protocol [] theSupportedProtocols;
    
    /**
     * The pattern that allows us to remove a unnecessary port 80 from a URL spec.
     */
    public static final Pattern thePort80Pattern = Pattern.compile(
            "^(http://[^/:]+):80(/.*)$" );

    /**
     * The pattern that allows us to remove a unnecessary port 443 from a URL spec.
     */
    public static final Pattern thePort443Pattern = Pattern.compile(
            "^(https://[^/:]+):443(/.*)$" );

    /**
     * The default protocols for this class.
     */
    protected static Protocol [] DEFAULT_PROTOCOLS = {
        new Protocol( "http", true ),
        new Protocol( "https", true ),
        new Protocol( "file", true )
    };

    /**
     * Captures all we need to know about protocol support here.
     */
    public static class Protocol
    {
        /**
         * Constructor.
         * 
         * @param name the protocol name in a URI
         * @param isRestful true if this is a RESTful protocol
         */
        public Protocol(
                String  name,
                boolean isRestful )
        {
            theName      = name;
            theIsRestful = isRestful;
        }
        
        /**
         * Obtain the name of this protocol.
         * 
         * @return the name
         */
        public String getName()
        {
            return theName;
        }

        /**
         * Determine whether this protocol is RESTfully resolvable.
         * 
         * @return true if it is RESTfully resolvable
         */
        public boolean getIsRestfullyResolvable()
        {
            return theIsRestful;
        }
              
        /**
         * String form for debugging.
         * 
         * @return String form
         */
        @Override
        public String toString()
        {
            return theName;
        }
        
        /**
         * The name of the protocol.
         */
        protected String theName;
        
        /**
         * True if this is a RESTful protocol.
         */
        protected boolean theIsRestful;
    }
}
