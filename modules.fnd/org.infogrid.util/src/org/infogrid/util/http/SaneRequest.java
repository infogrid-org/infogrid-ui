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

package org.infogrid.util.http;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * A saner API for incoming HTTP requests than the JDK's HttpServletRequest.
 */
public abstract class SaneRequest
{
    /**
     * Private constructor, for subclasses only.
     */
    protected SaneRequest()
    {
        // nothing
    }

    /**
     * Determine the HTTP method (such as GET).
     *
     * @return the HTTP method
     */
    public abstract String getMethod();

    /**
     * Obtain the requested root URI.
     * In a Request to URL <code>http://example.com:123/foo/bar?abc=def</code>
     * that would be <code>http://example.com:123</code> (no trailing slash).
     *
     * @return the requested root URI
     */
    public String getRootUri()
    {
        if( theRootUri == null ) {
            StringBuffer buf = new StringBuffer( 64 );
            buf.append( getProtocol());
            buf.append( "://" );
            buf.append( getHttpHost());
            theRootUri = buf.toString();
        }
        return theRootUri;
    }

    /**
     * Determine the requested, relative base URI.
     * In a Request to URL <code>http://example.com:123/foo/bar?abc=def</code>
     * that would be <code>/foo/bar</code>.
     *
     * @return the requested base URI
     */
    public abstract String getRelativeBaseUri();

    /**
     * Determine the requested, absolute base URI.
     * In a Request to URL <code>http://example.com:123/foo/bar?abc=def</code>
     * that would be <code>http://example.com:123/foo/bar</code>.
     *
     * @return the requested absolute base URI
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
     * In a Request to URL <code>http://example.com:123/foo/bar?abc=def</code>
     * that would be <code>/foo/bar?abc=def</code>.
     *
     * @return the requested relative full URI
     */
    public abstract String getRelativeFullUri();

    /**
     * Determine the requested, absolute full URI.
     * In a Request to URL <code>http://example.com:123/foo/bar?abc=def</code>
     * that would be <code>http://example.com:123/foo/bar?abc=def</code>.
     *
     * @return the requested absolute full URI
     */
    public String getAbsoluteFullUri()
    {
        if( theAbsoluteFullUri == null ) {
            theAbsoluteFullUri = getRootUri() + getRelativeFullUri();
        }
        return theAbsoluteFullUri;
    }

    /**
     * Get the name of the server.
     *
     * @return the name of the server
     */
    public abstract String getServer();

    /**
     * Get the value of the HTTP 1.1 host name field, which may include the port.
     *
     * @return the HTTP 1.1 Host name field
     */
    public abstract String getHttpHost();

    /**
     * Get the value of the HTTP 1.1 host name field, but without the port.
     *
     * @return the HTTP 1.1 host name field, but without the port
     */
    public abstract String getHttpHostOnly();

    /**
     * Get the port at which this Request arrived.
     *
     * @return the port at which this Request arrived
     */
    public abstract int getPort();

    /**
     * Get the protocol, i.e. http or https.
     *
     * @return http or https
     */
    public abstract String getProtocol();    
    
    /**
     * Obtain all values of a multi-valued argument
     *
     * @param argName name of the argument
     * @return value.
     */
    public abstract String [] getMultivaluedArgument(
            String argName );

    /**
     * Obtain the value of a named argument, or null. This considers both URL arguments
     * and POST arguments. If more than one argument is given by this name,
     * this will throw an IllegalStateException.
     *
     * @param name the name of the argument
     * @return the value of the argument with name name
     */
    public final String getArgument(
            String name )
    {
        String [] almost = getMultivaluedArgument( name );
        if( almost == null || almost.length == 0 ) {
            return null;
        } else if( almost.length == 1 ) {
            return almost[0];
        } else {
            throw new IllegalStateException( "Argument " + name + " has " + almost.length + " values" );
        }
    }
    
    /**
     * Obtain all arguments of this Request.
     *
     * @return a Map of name to value mappings for all arguments
     */
    public abstract Map<String,String[]> getArguments();

    /**
     * Determine whether a named argument has the given value. This method is useful
     * in case several arguments have been given with the same name.
     * 
     * @param name the name of the argument
     * @param value the desired value of the argument
     * @return true if the request contains an argument with this name and value
     */
    public boolean matchArgument(
            String name,
            String value )
    {
        String [] found = getMultivaluedArgument( name );
        if( found == null ) {
            return false;
        }
        for( String current : found ) {
            if( value.equals( current )) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtain a POST'd argument. If more than one argument is given by this name,
     * this will throw an IllegalStateException.
     *
     * @param argName name of the argument
     * @return value.
     */
    public final String getPostArgument(
            String argName )
    {
        String [] almost = getMultivaluedPostArgument( argName );
        if( almost == null || almost.length == 0 ) {
            return null;
        } else if( almost.length == 1 ) {
            return almost[0];
        } else {
            throw new IllegalStateException();
        }        
    }

    /**
     * Obtain all values of a multi-valued POST'd argument
     *
     * @param argName name of the argument
     * @return value.
     */
    public abstract String [] getMultivaluedPostArgument(
            String argName );

    /**
     * Obtain all POST'd arguments of this Request.
     *
     * @return a Map of name to value mappings for all POST'd arguments
     */
    public abstract Map<String,String[]> getPostArguments();

    /**
     * Obtain the cookies that were sent as part of this Request.
     *
     * @return the cookies that were sent as part of this Request.
     */
    public abstract SaneCookie [] getCookies();

    /**
     * Obtain a named cookie, or null if not present.
     *
     * @param name the name of the cookie
     * @return the named cookie, or null
     */
    public SaneCookie getCookie(
            String name )
    {
        SaneCookie [] cookies = getCookies();
        if( cookies != null ) {
            for( int i=0 ; i<cookies.length ; ++i ) {
                if( cookies[i].getName().equals( name )) {
                    return cookies[i];
                }
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
        SaneCookie cook = getCookie( name );
        if( cook != null ) {
            return cook.getValue();
        } else {
            return null;
        }
    }

    /**
     * Helper method to parse a URL given in String format, and return the value of the named argument.
     *
     * @param url the to-be-parsed URL
     * @param argName the name of the argument
     * @return the value of the argument in the URL, if any
     */
    public static String getUrlArgument(
            String url,
            String argName )
    {
        int pos = url.indexOf( '?' );
        if( pos == 0 ) {
            return null;
        }

        while( pos < url.length() ) {
            int pos2 = url.indexOf( '&', pos+1 );
            if( pos2 < 0 ) {
                pos2 = url.length();
            }
            String [] temp = url.substring( pos+1, pos2 ).split( "=" );
            String name  = temp[0];
            String value = temp.length > 1 ? temp[1] : "";

            name = HTTP.decodeUrlArgument( name );
            if( argName.equals( name )) {
                String ret = HTTP.decodeUrlArgument( value );
                return ret;
            }
            pos = pos2;
        }
        return null;
    }

    /**
     * Obtain the query string, if any.
     * 
     * @return the query string
     */
    public abstract String getQueryString();
    
    /**
     * Obtain the content of the request, e.g. HTTP POST data.
     *
     * @return the content of the request, or null
     */
    public abstract String getPostData();

    /**
     * Obtain an Iterator over the user's language preferences, in order of preference.
     * This Iterator takes into account a cookie that might be set by the application,
     * followed by the value of the Accept-Language header in the HTTP request.
     *
     * @return Iterator
     */
    public abstract Iterator<Locale> acceptLanguageIterator();

    /**
     * Obtain an Iterator over the requested MIME types, if any. Return the higher-priority
     * MIME types first.
     *
     * @return Iterator over the requested MIME types, if any.
     */
    public abstract Iterator<String> requestedMimeTypesIterator();

    /**
     * The root URI of the Request.
     */
    private String theRootUri;

    /**
     * The absolute base URI of the Request.
     */
    private String theAbsoluteBaseUri;

    /**
     * The absolute full URI of the Request.
     */
    private String theAbsoluteFullUri;
    
    /**
     * Name of the cookie that might contain Accept-Language information.
     */
    public static final String ACCEPT_LANGUAGE_COOKIE_NAME = "Accept-Language";
    
    /**
     * Name of the HTTP Header that specifies the acceptable MIME types.
     */
    protected static final String ACCEPT_HEADER = "Accept";
}
