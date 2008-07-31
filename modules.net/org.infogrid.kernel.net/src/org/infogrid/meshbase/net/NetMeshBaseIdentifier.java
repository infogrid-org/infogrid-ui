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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.util.Identifier;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.StringHelper;

/**
 * A network identifier, such as a URI, XRI or URL. It has two components
 * (which may be the same): the identifier in its canonical form, and a URL-equivalent.
 * For our purposes, we always need to have an URL-equivalent, otherwise the
 * identifier cannot be used for anything useful in InfoGrid. This class performs
 * the conversion.
 */
public class NetMeshBaseIdentifier
        extends
            MeshBaseIdentifier
        implements
            Identifier
{
    /**
     * Factory method to create a NetMeshBaseIdentifier that is resolvable into a stream,
     * e.g. http URL.
     * 
     * @param canonicalForm the canonical form of this NetMeshBaseIdentifier
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public static NetMeshBaseIdentifier create(
            String canonicalForm )
        throws
            URISyntaxException
    {
        return create( null, canonicalForm, true, false );
    }
    
    /**
     * Factory method to create a NetMeshBaseIdentifier that cannot be resolved into a stream,
     * e.g. jdbc.
     * 
     * @param canonicalForm the canonical form of this NetMeshBaseIdentifier
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public static NetMeshBaseIdentifier createUnresolvable(
            String canonicalForm )
        throws
            URISyntaxException
    {
        return create( null, canonicalForm, false, false );
    }
    
    /**
     * Factory method to create a NetMeshBaseIdentifier that is resolvable into a stream,
     * e.g. http URL. This method attempts to guess the protocol if none has been provided.
     * 
     * @param string the (potentially incomplete) String form of this NetMeshBaseIdentifier
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public static NetMeshBaseIdentifier guessAndCreate(
            String string )
        throws
            URISyntaxException
    {
        return create( null, string, true, true );
    }
    
    /**
     * Factory method to create a NetMeshBaseIdentifier specified in relative form in the
     * context of another NetMeshBaseIdentifier.
     * 
     * @param context the NetMeshBaseIdentifier that forms the context
     * @param string the (potentially incomplete) String form of this NetMeshBaseIdentifier
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public static NetMeshBaseIdentifier guessAndCreate(
            NetMeshBaseIdentifier context,
            String                string )
        throws
            URISyntaxException
    {
        return create( context, string, true, true );
    }

    /**
     * Fully-specified factory method.
     * 
     * @param context the NetMeshBaseIdentifier that forms the context
     * @param string the (potentially incomplete) String form of this NetMeshBaseIdentifier
     * @param checkForSupportedProtocol if true, check for supported protocols
     * @param guess if true, attempt to guess the protocol if none was given
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    protected static NetMeshBaseIdentifier create(
            NetMeshBaseIdentifier context,
            String                string,
            boolean               checkForSupportedProtocol,
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
        
        boolean isResolvable = false;
        for( String urlProtocol : theRestfulProtocols ) {
            if( lower.startsWith( urlProtocol + ":" )) {
                isResolvable = true;
                break;
            }
        }
        if( isResolvable ) {
            string = stripDirectoryPaths( string );
            lower = string.toLowerCase();
        }
        
        if( checkForSupportedProtocol ) {
            for( String urlProtocol : theSupportedUrlProtocols ) {
                if( lower.startsWith( urlProtocol + ":" )) {
                    return new NetMeshBaseIdentifier( string, new URI( string ), isResolvable );
                }
            }
            throw new IllegalArgumentException(
                    "canonical identifier uses unknown protocol (need one of "
                    + StringHelper.join( theSupportedUrlProtocols )
                    + "), is "
                    + string );
        } else {
            return new NetMeshBaseIdentifier( string, new URI( string ), isResolvable );
        }
    }

    /**
     * Factory method.
     * 
     * @param file the local File whose NetMeshBaseIdentifier we create
     * @return the created NetMeshBaseIdentifier
     */
    public static NetMeshBaseIdentifier create(
            File file )
    {
        return new NetMeshBaseIdentifier( "file:" + file.getAbsolutePath(), file.toURI(), true );
    }

    /**
     * Factory method.
     * 
     * @param url the URL whose NetMeshBaseIdentifier we create
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public static NetMeshBaseIdentifier create(
            URL url )
        throws
            URISyntaxException
    {
        return new NetMeshBaseIdentifier( url.toString(), url.toURI(), true );
    }

    /**
     * Factory method.
     * 
     * @param uri the URI whose NetMeshBaseIdentifier we create
     * @return the created NetMeshBaseIdentifier
     */
    public static NetMeshBaseIdentifier create(
            URI uri )
    {
        String lower = uri.toString().toLowerCase();
        
        boolean isResolvable = false;
        for( String urlProtocol : theRestfulProtocols ) {
            if( lower.startsWith( urlProtocol + ":" )) {
                isResolvable = true;
                break;
            }
        }
        return new NetMeshBaseIdentifier( uri.toString(), uri, isResolvable );
    }

    /**
     * For consistency with the Java APIs, we provide this method.
     * 
     * @param canonicalForm the canonical form of this NetMeshBaseIdentifier
     * @return the created NetMeshBaseIdentifier
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public static NetMeshBaseIdentifier fromExternalForm(
            String canonicalForm )
        throws
            URISyntaxException
    {
        return create( null, canonicalForm, false, false );
    }

    /**
     * Constructor.
     * 
     * @param canonicalForm the canonical representation of this identifier
     * @param uri URI representation of this identifier
     * @param isResolvable if true, this identifier is REST-fully resolvable
     */
    protected NetMeshBaseIdentifier(
            String  canonicalForm,
            URI     uri,
            boolean isResolvable )
    {
        super( canonicalForm );

        theUri           = uri;
        theIsResolvable  = isResolvable;
    }

    /**
     * Determine whether this NetMeshBaseIdentifier is REST-fully resolvable.
     * 
     * @return true if it is
     */
    public boolean isRestfullyResolvable()
    {
        return theIsResolvable;
    }

    /**
     * Obtain the URI form of this identifier as URI.
     * 
     * @return the URI form
     */
    public URI toUri()
    {
        return theUri;
    }

    /**
     * Obtain the URL form of this identifier as URL.
     * 
     * @return the URL form
     * @throws MalformedURLException thrown if the URI could not be turned into a URL
     */
    public URL toUrl()
        throws
           MalformedURLException
    {
        return theUri.toURL();
    }

    /**
     * Obtain the URI String form of this identifier.
     *
     * @return the URI String form
     */
    public String getUriString()
    {
        if( theUriString == null ) {
            theUriString = theUri.toString();
        }
        return theUriString;
    }

    /**
     * Obtain the protocol of the URL form.
     *
     * @return the protocol.
     * @throws MalformedURLException thrown if the URI could not be turned into a URL
     */
    public String getUrlProtocol()
        throws
           MalformedURLException
    {
        if( theUrlProtocol == null ) {
            theUrlProtocol = toUrl().getProtocol();
        }
        return theUrlProtocol;
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
     * Determine equality.
     *
     * @param other the Object to compare against
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( !( other instanceof NetMeshBaseIdentifier )) {
            return false;
        }
        NetMeshBaseIdentifier realOther = (NetMeshBaseIdentifier) other;
        
        String here  = getCanonicalForm();
        String there = realOther.getCanonicalForm();
        
        boolean ret = here.equals( there );
        return ret;
    }

    /**
     * Calculate hash value. Make NetBeans happy.
     *
     * @return the hash value
     */
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    /**
     * The URI.
     */
    protected URI theUri;

    /**
     * The URI in String form. This is stored here for efficiency reasons.
     */
    protected String theUriString;

    /**
     * The protocol part of the URL form. This is stored here for efficiency reasons.
     */
    protected String theUrlProtocol;

    /**
     * Whether this NetMeshBaseIdentifier is REST-fully resolvable.
     */
    protected boolean theIsResolvable;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( NetMeshBaseIdentifier.class );
    
    /**
     * The XRI resolver location.
     */
    private static final String theXriResolverPrefix = theResourceHelper.getResourceStringOrDefault(
            "XriResolverPrefix",
            "http://xri.net/" );

    /**
     * The XRI URI prefix.
     */
    private static final String theXriUriPrefix = "xri://";

    /**
     * The supported URL protocols.
     */
    private static final String [] theSupportedUrlProtocols = { "http", "https", "file", "ppid" };
    
    /**
     * The Restful protocols.
     */
    private static final String [] theRestfulProtocols = { "http", "https", "file" };

    /**
     * The pattern that allows us to remove a unnecessary port 80 from a URL spec.
     */
    public static final Pattern thePort80Pattern = Pattern.compile(
            "^(http[s]?://[^/:]+):80(/.*)$" );
}
