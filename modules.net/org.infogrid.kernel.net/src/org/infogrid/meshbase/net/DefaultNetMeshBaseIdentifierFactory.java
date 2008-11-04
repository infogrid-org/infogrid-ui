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

package org.infogrid.meshbase.net;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.text.StringifierException;
import org.infogrid.util.text.StringRepresentation;

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

    //    /**
//     * Factory method to obtain a NetMeshBaseIdentifier that is resolvable into a stream,
//     * e.g. http URL.
//     * 
//     * @param canonicalForm the canonical form of this NetMeshBaseIdentifier
//     * @return the created NetMeshBaseIdentifier
//     * @throws URISyntaxException thrown if the syntax could not be parsed
//     */
//    public NetMeshBaseIdentifier obtain(
//            String canonicalForm )
//        throws
//            URISyntaxException
//    {
//        return obtain( null, canonicalForm, true, false );
//    }
//    
//    /**
//     * Factory method to obtain a NetMeshBaseIdentifier that cannot be resolved into a stream,
//     * e.g. jdbc.
//     * 
//     * @param canonicalForm the canonical form of this NetMeshBaseIdentifier
//     * @return the created NetMeshBaseIdentifier
//     * @throws URISyntaxException thrown if the syntax could not be parsed
//     */
//    public NetMeshBaseIdentifier obtainUnresolvable(
//            String canonicalForm )
//        throws
//            URISyntaxException
//    {
//        return obtain( null, canonicalForm, false, false );
//    }
//    
    /**
     * Factory method to obtain a NetMeshBaseIdentifier that is resolvable into a stream,
     * e.g. http URL. This method attempts to guess the protocol if none has been provided.
     * 
     * @param string the (potentially incomplete) String form of this NetMeshBaseIdentifier
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier guessFromExternalForm(
            String string )
        throws
            URISyntaxException
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
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier guessFromExternalForm(
            NetMeshBaseIdentifier context,
            String                string )
        throws
            URISyntaxException
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
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    protected NetMeshBaseIdentifier obtain(
            NetMeshBaseIdentifier context,
            String                string,
            boolean               guess )
        throws
            URISyntaxException
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
            }
        }

        if( string.length() == 0 ) {
            throw new URISyntaxException( string, "identifier cannot be empty String" );
        }

        if( isXriGlobalContextSymbol( string.charAt( 0 ))) {
            return new NetMeshBaseIdentifier( string, new URI( theXriResolverPrefix + string ), true );
        }
        if( string.startsWith( theXriResolverPrefix )) {
            string = string.substring( theXriResolverPrefix.length() );
            return new NetMeshBaseIdentifier( string, new URI( theXriResolverPrefix + string ), true );
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
                } else {
                    string = "http://" + string;
                }
                lower = string.toLowerCase();
            }
        }
        
        for( Protocol p : theSupportedProtocols ) {
            if( lower.startsWith( p.getName() + ":" )) {
                return new NetMeshBaseIdentifier( string, new URI( string ), p.getIsRestfullyResolvable() );
            }
        }
        throw new URISyntaxException(
                string,
                "canonical identifier uses unknown protocol (need one of "
                + ArrayHelper.join( theSupportedProtocols )
                + ")" );
    }

    /**
     * Factory method.
     * 
     * @param file the local File whose NetMeshBaseIdentifier we obtain
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier obtain(
            File file )
        throws
            URISyntaxException
    {
        return obtain( file.toURI() );
    }

    /**
     * Factory method.
     * 
     * @param url the URL whose NetMeshBaseIdentifier we obtain
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier obtain(
            URL url )
        throws
            URISyntaxException
    {
        return obtain( null, url.toExternalForm(), false );
    }

    /**
     * Factory method.
     * 
     * @param uri the URI whose NetMeshBaseIdentifier we obtain
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier obtain(
            URI uri )
        throws
            URISyntaxException
    {
        return obtain( null, uri.toASCIIString(), false );
    }

    /**
     * Recreate a NetMeshBaseIdentifier from an external form. Be strict about syntax.
     *
     * @param raw the external form
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier fromExternalForm(
            String raw )
        throws
            URISyntaxException
    {
        return obtain( null, raw, false );
    }

    /**
     * Convert this StringRepresentation back to a NetMeshBaseIdentifier.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            URISyntaxException
    {
        try {
            Object [] found = representation.parseEntry( NetMeshBaseIdentifier.class, NetMeshBaseIdentifier.DEFAULT_ENTRY, s );

            NetMeshBaseIdentifier ret;
            switch( found.length ) {
                case 1:
                    ret = fromExternalForm( (String) found[0] );
                    break;

                default:
                    throw new URISyntaxException( s, "Cannot parse identifier" );
            }

            return ret;

        } catch( StringifierException ex ) {
            throw new URISyntaxException( s, "Cannot parse identifier" );

        } catch( ClassCastException ex ) {
            throw new URISyntaxException( s, "Cannot parse identifier" );
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
            "^(http[s]?://[^/:]+):80(/.*)$" );

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
