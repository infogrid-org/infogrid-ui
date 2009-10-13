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

package org.infogrid.httpd;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import org.infogrid.httpd.util.NameValueList;
import org.infogrid.util.ArrayMap;
import org.infogrid.util.Base64;
import org.infogrid.util.StreamUtils;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
  * This represents an incoming HTTP Request.
  */
public class HttpRequest
        implements
            CanBeDumped
{
    private static Log log = Log.getLogInstance( HttpRequest.class ); // our own, private logger

    /**
      * Factory method, by reading from an InputStream. The reading stops as soon
      * as the end of the header has been reached.
      *
      * @param protocol the protocol, such as http or https
      * @param port the port of the incoming Request
      * @param theInStream the InputStream
      * @return the created Request
      * @throws IOException thrown if a read error occurred
      * @throws MalformedHttpHeaderException thrown if the HTTP header found was malformed
      */
    public static HttpRequest readHeader(
            String      protocol,
            int         port,
            InputStream theInStream )
        throws
            IOException,
            MalformedHttpHeaderException
    {
        BufferedInputStream theBufferedInStream = new BufferedInputStream( theInStream );

        String        method;
        String        relativeFullUri;
        String        httpVersion = "HTTP/1.0"; // default
        NameValueList parameters  = new NameValueList();

        String firstLine = readLine( theBufferedInStream );
        int index1 = firstLine.indexOf( ' ' );
        if( index1 < 0 ) {
            throw new MalformedHttpHeaderException();
        }
        method = firstLine.substring( 0, index1 );

        int index2 = firstLine.indexOf( ' ', index1+1 );
        if( index2 > 0 ) {
            relativeFullUri = firstLine.substring( index1+1, index2 );
            httpVersion     = firstLine.substring( index2+1 );
        } else {
            relativeFullUri = firstLine.substring( index1+1 );
        }

        while( true ) {
            String currentLine = readLine( theBufferedInStream );
            if( currentLine.length() == 0 ) {
                break;
            }

            int index = currentLine.indexOf( ": " ); // separator of keyword and value
            if( index < 0 ) {
                throw new MalformedHttpHeaderException();
            }
            
            String key   = currentLine.substring( 0, index );
            String value = currentLine.substring( index+2 ); // +2 because we are looking for 2 characters

            parameters.add( key.toLowerCase(), value ); // this doesn't imply that existing keys are replaced
        }
        String host   = parameters.get( "Host".toLowerCase() );

        byte [] postData = null;
        if( "POST".equalsIgnoreCase( method ) ) {
            String length = parameters.get( "Content-Length".toLowerCase() );
            if( length != null && length.length() > 0 ) {
                postData = StreamUtils.slurp( theBufferedInStream, Integer.parseInt( length ));
            } else {
                postData = StreamUtils.slurp( theBufferedInStream ); // slurp through end of stream
            }
        }
        return new HttpRequest( protocol, method, host, port, relativeFullUri, httpVersion, parameters, postData );
    }

    /**
      * Private constructor, use factory method.
      *
      * @param protocol the protocol, such as http or https
      * @param method the HTTP method of the Request
      * @param host the HTTP Host value from HTTP 1.1
      * @param port the port at which the Request has arrived
      * @param relativeFullUri the Uri of the Request relative to the web server's document root
      * @param httpVersion the version of the HTTP protocol
      * @param parameters the name-value pairs found in the header
      * @param postData the HTTP post data, if any
      */
    protected HttpRequest(
            String        protocol,
            String        method,
            String        host,
            int           port,
            String        relativeFullUri,
            String        httpVersion,
            NameValueList parameters,
            byte []       postData )
    {
        theProtocol        = protocol;
        theMethod          = method;
        theHttpHost        = host;
        thePort            = port;
        theRelativeFullUri = relativeFullUri;
        theHttpVersion     = httpVersion;
        theParameters      = parameters;
        thePostData        = postData;

        int colon = theHttpHost.indexOf( ':' );
        if( colon >= 0 ) {
            theHttpHostOnly = theHttpHost.substring( 0, colon );
        } else {
            theHttpHostOnly = theHttpHost;
        }

        theArguments = new NameValueList();

        int q = relativeFullUri.indexOf( '?' );
        if( q < 0 ) {
            theRelativeBaseUri = relativeFullUri;

        } else {
            theRelativeBaseUri = relativeFullUri.substring( 0, q );
            String argsString  = relativeFullUri.substring( q+1 );

            StringTokenizer token = new StringTokenizer( argsString, "&", false );
            while( token.hasMoreTokens() ) {
                String pair = token.nextToken();
                int eq = pair.indexOf( '=' );

                String name;
                String value;

                if( eq < 0 ) {
                    name  = HTTP.decodeUrlArgument( pair );
                    value = null;
                } else {
                    name  = pair.substring( 0, eq );
                    value = pair.substring( eq+1 );

                    name  = HTTP.decodeUrlArgument( name );
                    value = HTTP.decodeUrlArgument( value );
                }
                theArguments.add( name, value );
            }
        }
    }

    /**
     * Helper method to read a line from an InputStream.
     *
     * @param s the Stream from where to read
     * @return the line
     * @throws IOException an I/O error occurred
     */
    protected static String readLine(
            BufferedInputStream s )
        throws
            IOException
    {
        StringBuffer ret = new StringBuffer( 80 );

        boolean marked = false;
        int c;
        while( ( c = s.read()) > 0 ) {
            if( c == '\r' ) {
                s.mark( 1 );
                marked = true;
            } else if( c == '\n' ) {
                break;
            } else if( marked ) {
                s.reset();
                break;
            } else {
                ret.append( (char) c );
            }
        }
        return ret.toString();
    }

    /**
      * Determine the HTTP method (such as GET).
      *
      * @return the HTTP method
      */
    public String getMethod()
    {
        return theMethod;
    }

    /**
     * Obtain the requested root URI.
     *
     * @return the requested root URI
     */
    public String getRootUri()
    {
        if( theRootUri == null ) {
            StringBuffer buf = new StringBuffer();
            buf.append( theProtocol );
            buf.append( "://" );
            buf.append( getHttpHostOnly() );

            int port = getPort();
            if( "http".equals( theProtocol )) {
                if( port != 80 ) {
                    buf.append( ':' ).append( port );
                }
            } else if( "https".equals( theProtocol )) {
                if( port != 443 ) {
                    buf.append( ':' ).append( port );
                }
            }

            theRootUri = buf.toString();
        }
        return theRootUri;
    }

    /**
     * Determine the requested, relative base URI.
     *
     * @return the requested, relative base URI
     */
    public String getRelativeBaseUri()
    {
        return theRelativeBaseUri;
    }

    /**
     * Determine the requested, absolute base URI.
     *
     * @return the requested, absolute base URI
     */
    public String getAbsoluteBaseUri()
    {
        if( theAbsoluteBaseUri == null ) {
            theAbsoluteBaseUri = getRootUri() + getRelativeBaseUri();
        }
        return theAbsoluteBaseUri;
    }

    /**
     * Determine the requested, relative full URI.
     *
     * @return the requested, relative full URI with parameters
     */
    public String getRelativeFullUri()
    {
        return theRelativeFullUri;
    }

    /**
     * Determine the requested, absolute full URI.
     *
     * @return the requested, absolute full URI
     */
    public String getAbsoluteFullUri()
    {
        if( theAbsoluteFullUri == null ) {
            theAbsoluteFullUri = getRootUri() + getRelativeFullUri();
        }
        return theAbsoluteFullUri;
    }

    /**
     * Obtain the arguments of this Request, such as x=y in case of http://example.com/abc/def?x=y.
     *
     * @return the arguments of this Request
     */
    public NameValueList getArguments()
    {
        return theArguments;
    }

    /**
     * Obtain the value of a named argument, or null.
     *
     * @param name the name of the argument
     * @return value of the named argument, or null
     */
    public String getArgument(
            String name )
    {
        return theArguments.get( name );
    }

    /**
     * Obtain the argument string, if any, i.e. all characters verbatim after
     * (but not including) the ?
     *
     * @return the characters verbatim after the question mark in the URL, or null if there's no question mark
     */
    public String getArgumentString()
    {
        String ret      = getRelativeFullUri();
        int    question = ret.indexOf( '?' );

        if( question > 0 ) {
            ret = ret.substring( question+1 );
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Obtain the HTTP parameters of this Request, such as Authorization=xyz.
     *
     * @return the TTP parameters of this Request
     */
    public NameValueList getHttpParameters()
    {
        return theParameters;
    }

    /**
     * Find the basic authorization String.
     *
     * @return the basic authorization String, if any
     */
    public String getBasicAuthorizationString()
    {
        String encoded = theParameters.get( HttpRequestHeaderFields.AUTHORIZATION_TAG );
        if( encoded == null ) {
            return null;
        }
        if( !encoded.startsWith( BASIC_AUTHENTICATION_TAG )) {
            return null;
        }
        encoded = encoded.substring( BASIC_AUTHENTICATION_TAG.length() );

        byte [] decoded = Base64.base64decode( encoded );
        String decodedString = new String( decoded );

        return decodedString;
    }

    /**
     * Find the username provided as part of the authorization field Basic authorization.
     *
     * @return the username provided
     */
    public String getBasicAuthorizationUserName()
    {
        // FIXME this can be made much more efficient

        String decodedString = getBasicAuthorizationString();
        if( decodedString == null ) {
            return null;
        }

        int colon = decodedString.indexOf( ":" );
        if( colon < 0 ) {
            return null;
        }

        String user = decodedString.substring( 0, colon );

        return user;
    }

    /**
     * Find the password provided as part of the authorization field's Basic authorization.
     *
     * @return the password provided
     */
    public String getBasicAuthorizationPassword()
    {
        // FIXME this can be made much more efficient

        String decodedString = getBasicAuthorizationString();
        if( decodedString == null ) {
            return null;
        }

        int colon = decodedString.indexOf( ":" );
        if( colon < 0 ) {
            return null;
        }

        String pass = decodedString.substring( colon+1 );

        return pass;
    }

    /**
     * Obtain the String specifying the byte range.
     *
     * @return the String specifying the byte range
     */
    public String getByteRangeString()
    {
        String range = getHttpParameters().get( HttpRequestHeaderFields.RANGE_TAG );
        if( range == null ) {
            return null;
        }
        if( !range.startsWith( HttpEntityHeaderFields.CONTENT_RANGE_BYTES_TAG )) {
            return null;
        }
        range = range.substring( HttpEntityHeaderFields.CONTENT_RANGE_BYTES_TAG.length() );

        if( range.indexOf( ',' ) < 0 ) {
            return null; // we don't support complex ranges
        }
        return range;
    }

    /**
     * Obtain the index of the first byte to obtain.
     *
     * @return the index of the first byte to obtain
     */
    public int getByteRangeStartIndex()
    {
        // FIXME, this could be done a lot more efficiently
        String range = getByteRangeString();
        if( range == null ) {
            return 0;
        }

        int dash = range.indexOf( '-' );
        if( dash < 0 ) {
            return 0;
        }

        String minString = range.substring( 0, dash );
        String maxString = range.substring( dash+1 );

        int min;
        int max;

        if( minString.length() > 0 ) {
            min = Integer.parseInt( minString );
        } else {
            min = 0;
        }
        if( maxString.length() > 0 ) {
            max = Integer.parseInt( maxString );
        } else {
            max = -1;
        }

        if( max != -1 && min > max ) {
            return 0;
        }

        return min;
    }

    /**
     * Obtain the index of the last byte to obtain. If this is -1, it means "until the end".
     *
     * @return the index of the last byte to obtain, or -1 for "until the end"
     */
    public int getByteRangeEndIndex()
    {
        // FIXME, this could be done a lot more efficiently
        String range = getByteRangeString();
        if( range == null ) {
            return -1;
        }

        int dash = range.indexOf( '-' );
        if( dash < 0 ) {
            return -1;
        }

        String minString = range.substring( 0, dash );
        String maxString = range.substring( dash+1 );

        int min;
        int max;

        if( minString.length() > 0 ) {
            min = Integer.parseInt( minString );
        } else {
            min = 0;
        }
        if( maxString.length() > 0 ) {
            max = Integer.parseInt( maxString );
        } else {
            max = -1;
        }
        if( max != -1 && min > max ) {
            return -1;
        }

        return max;
    }

    /**
     * Get the value of the HTTP 1.1 host name field, which may include the port.
     *
     * @return the HTTP 1.1 Host name field
     */
    public String getHttpHost()
    {
        return theHttpHost;
    }

    /**
     * Get the value of the HTTP 1.1 host name field, but without the port.
     *
     * @return the HTTP 1.1 host name field, but without the port
     */
    public String getHttpHostOnly()
    {
        return theHttpHostOnly;
    }

    /**
     * Get the port at which this Request arrived.
     *
     * @return the port
     */
    public int getPort()
    {
        return thePort;
    }

    /**
     * Get the protocol, i.e. http or https.
     *
     * @return String the protocol
     */
    public String getProtocol()
    {
        return theProtocol;
    }

    /**
     * Obtain the cookies that were sent as part of this request.
     *
     * @return the cookies
     */
    public HttpCookie [] getCookies()
    {
        // this is not efficient in a multi-threaded environment, but works correctly
        if( theCookies != null ) {
            return theCookies;
        }

        // support multiple lines, with multiple cookies each
        String [] lines = theParameters.getAll( HttpRequestHeaderFields.COOKIE_TAG.toLowerCase() );

        ArrayList<HttpCookie> ret = new ArrayList<HttpCookie>();
        for( int i=0 ; i<lines.length ; ++i ) {
            StringTokenizer token = new StringTokenizer( lines[i], "; " );
            while( token.hasMoreTokens() ) {
                String pair = token.nextToken();
                String name    = null;
                String value   = null;

                int equals = pair.indexOf( '=' );
                if( equals < 0 ) {
                    continue;
                }
                name  = pair.substring( 0, equals );
                value = pair.substring( equals+1 );

                name  = HTTP.decodeUrlArgument( name );
                value = HTTP.decodeUrlArgument( value );

                ret.add( new HttpCookie( name, value ));
            }
        }

        theCookies = new HttpCookie[ ret.size() ];
        ret.toArray( theCookies );
        return theCookies;
    }

    /**
     * Obtain a named Cookie, or null if not present.
     *
     * @param name the name of the Cookie that we are looking for
     * @return Cookie or null
     */
    public HttpCookie getCookie(
            String name )
    {
        HttpCookie [] coo = getCookies();
        for( int i=0 ; i<coo.length ; ++i ) {
            String cookieName = coo[i].getName();
            if( name.equals( cookieName )) {
                return coo[i];
            }
        }
        return null;
    }

    /**
     * Obtain the value of a named cookie, or null if not present.
     *
     * @param name the name of the cookie
     * @return the value of the named cookie, or null
     */
    public String getCookieValue(
            String name )
    {
        HttpCookie cook = getCookie( name );
        if( cook != null ) {
            return cook.getValue();
        } else {
            return null;
        }
    }

    /**
     * Obtain the hostname for cookies based on this Request.
     *
     * @return the recommended cookie host for this Request
     */
    public String recommendCookieHost()
    {
        return theHttpHostOnly;
    }

    /**
     * Obtain the content that was posted.
     *
     * @return the content that was posted, or null
     */
    public byte [] getPostData()
    {
        return thePostData;
    }

    /**
     * Obtain the content that was posted as a Map.
     *
     * @return Map of name-value pairs
     */
    public Map<String,String> getPostArguments()
    {
        if( "GET".equals( theMethod )) {
            return null;
        }

        if( thePostParameters == null ) {
            thePostParameters = new ArrayMap<String,String>();

            String          postData = new String( thePostData );
            StringTokenizer token    = new StringTokenizer( postData, "&" );

            while( token.hasMoreTokens() ) {
                String next = token.nextToken();

                int equals = next.indexOf( '=' );
                if( equals >=0 ) {
                    String name  = next.substring( 0, equals );
                    String value = next.substring( equals+1 );

                    name  = HTTP.decodeUrlArgument( name );
                    value = HTTP.decodeUrlArgument( value );

                    thePostParameters.put( name, value );
                } else {
                    thePostParameters.put( next, "" );
                }
            }
        }
        return thePostParameters;
    }

    /**
     * Obtain the value of a post argument.
     *
     * @param name the name of the post argument
     * @return the value, or null
     */
    public String getPostArgument(
            String name )
    {
        Map<String,String> args = getPostArguments();
        if( args == null ) {
            return null;
        }
        String ret = args.get( name );
        return ret;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String [] {
                    "method",
                    "relativeBaseUri",
                    "httpVersion",
                    "parameters"
                },
                new Object [] {
                    theMethod,
                    theRelativeBaseUri,
                    theHttpVersion,
                    theParameters
                } );
    }

    /**
     * The protocol that was used, such as http or https.
     */
    protected String theProtocol;

    /**
      * The request method, such as GET.
      */
    protected String theMethod;

    /**
     * The http host, potentially with port.
     */
    protected String theHttpHost;

    /**
     * The http host, without the port.
     */
    protected String theHttpHostOnly;

    /**
     * The port at which the request arrived.
     */
    protected int thePort;

    /**
     * The cookies once we determined them.
     */
    protected HttpCookie [] theCookies;

    /**
     * The request root URI, such as http://example.com
     * in case of http://example.com/abc/def/?x=y
     */
    protected String theRootUri;

    /**
      * The requested relative base URI, such as /abc/def
      * in case of http://example.com/abc/def/?x=y
      */
    protected String theRelativeBaseUri;

    /**
      * The requested absolute base URI, such as http://example.com/abc/def
      * in case of http://example.com/abc/def/?x=y
      */
    protected String theAbsoluteBaseUri;

    /**
      * The requested relative full URI, such as http://example.com/abc/def
      * in case of http://example.com/abc/def/?x=y
      */
    protected String theRelativeFullUri;

    /**
      * The requested absolute full URI, such as http://example.com/abc/def/?x=y
      * in case of http://example.com/abc/def/?x=y
      */
    protected String theAbsoluteFullUri;

    /**
     * The data that was posted, if any
     */
    protected byte [] thePostData;

    /**
     * The arguments to the relative base URI, such as x=y in case of http://example.com/abc/def/?x=y
     */
    protected NameValueList theArguments;

    /**
     * The HTTP version used. This is actual String, rather than something decoded.
     */
    protected String theHttpVersion;

    /**
      * The parameters of the request.
      */
    protected NameValueList theParameters;

    /**
     * The POST'd parameters of the request.
     */
    protected Map<String,String> thePostParameters;

    /**
     * The tag with which the Basic authorization mechanism starts.
     */
    private static final String BASIC_AUTHENTICATION_TAG = "Basic ";
}


