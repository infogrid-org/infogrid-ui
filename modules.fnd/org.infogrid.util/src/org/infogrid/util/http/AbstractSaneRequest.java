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

package org.infogrid.util.http;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;

/**
 * Factors out functionality common to many implementations of SaneRequest.
 */
public abstract class AbstractSaneRequest
        implements
            SaneRequest
{
    private static final Log log = Log.getLogInstance( AbstractSaneRequest.class ); // our own, private logger

    /**
     * Private constructor, for subclasses only.
     *
     * @param requestAtProxy the SaneRequest received by the reverse proxy, if any
     */
    protected AbstractSaneRequest(
            SaneRequest requestAtProxy )
    {
        theRequestAtProxy = requestAtProxy;
    }

    /**
     * If this request was obtained by way of a reverse proxy, return the SaneRequest
     * that the reverse proxy received. Returns null if no reverse proxy was involved.
     *
     * @return the SaneRequest at the reverse proxy, or null if none
     */
    public SaneRequest getSaneRequestAtProxy()
    {
        return theRequestAtProxy;
    }

    /**
     * Obtain the original request as originally issued by the HTTP client. If a reverse
     * proxy was involved, return the SaneRequest that the reverse proxy received. If
     * no reverse proxy was involved, return this SaneRequest.
     *
     * @return the ultimate SaneRequest
     */
    public SaneRequest getOriginalSaneRequest()
    {
        if( theRequestAtProxy == null ) {
            return this;
        } else {
            return theRequestAtProxy.getOriginalSaneRequest();
        }
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
     * Obtain all values of a multi-valued argument given in the URL.
     *
     * @param argName name of the argument
     * @return value.
     */
    public abstract String [] getMultivaluedUrlArgument(
            String argName );

    /**
     * Obtain the value of a named argument given in the URL, or null.
     * If more than one argument is given by this name,
     * this will throw an IllegalStateException.
     *
     * @param name the name of the argument
     * @return the value of the argument with name name
     */
    public final String getUrlArgument(
            String name )
    {
        String [] almost = getMultivaluedUrlArgument( name );
        if( almost == null || almost.length == 0 ) {
            return null;
        } else if( almost.length == 1 ) {
            return almost[0];
        }
        // let it pass if all of them have the same value
        boolean letPass = true;
        String firstValue = almost[0];
        for( int i=1 ; i<almost.length ; ++i ) {
            if( firstValue == null ) {
                if( almost[i] != null ) {
                    letPass = false;
                    break;
                }
            } else if( !firstValue.equals( almost[i] )) {
                letPass = false;
                break;
            }
        }

        if( !letPass ) {
            throw new IllegalStateException( "Argument " + name + " has " + almost.length + " values" );
        } else {
            log.warn( "Multiple arguments but with same value: " + name + " -> " + firstValue );
        }
        return firstValue;
    }

    /**
     * Obtain all arguments of this Request given in the URL.
     *
     * @return a Map of name to value mappings for all arguments
     */
    public abstract Map<String,String[]> getUrlArguments();

    /**
     * Determine whether a named argument provided in the URL  has the given value.
     * This method is useful in case several arguments have been given with the same name.
     *
     * @param name the name of the argument
     * @param value the desired value of the argument
     * @return true if the request contains an argument with this name and value
     */
    public boolean matchUrlArgument(
            String name,
            String value )
    {
        String [] found = getMultivaluedUrlArgument( name );
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
    public final String getPostedArgument(
            String argName )
    {
        String [] almost = getMultivaluedPostedArgument( argName );
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
    public abstract String [] getMultivaluedPostedArgument(
            String argName );

    /**
     * Obtain all POST'd arguments of this Request.
     *
     * @return a Map of name to value mappings for all POST'd arguments
     */
    public abstract Map<String,String[]> getPostedArguments();

    /**
     * Determine whether a named POST'd argument has the given value.
     * This method is useful in case several arguments have been given with the same name.
     *
     * @param name the name of the argument
     * @param value the desired value of the argument
     * @return true if the request contains an argument with this name and value
     */
    public boolean matchPostedArgument(
            String name,
            String value )
    {
        String [] found = getMultivaluedPostedArgument( name );
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
     * Obtain the relative context Uri of this application.
     *
     * @return the relative context URI
     */
    public abstract String getContextPath();

    /**
     * Obtain the relative context Uri of this application with a trailing slash.
     *
     * @return the relative context URI with a trailing slash
     */
    public String getContextPathWithSlash()
    {
        return getContextPath() + "/";
    }

    /**
     * Obtain the absolute context Uri of this application.
     *
     * @return the absolute context URI
     */
    public abstract String getAbsoluteContextUri();

    /**
     * Obtain the absolute context Uri of this application with a trailing slash.
     *
     * @return the absolute context URI with a trailing slash.
     */
    public String getAbsoluteContextUriWithSlash()
    {
        return getAbsoluteContextUri() + "/";
    }

    /**
     * Obtain the cookies that were sent as part of this Request.
     *
     * @return the cookies that were sent as part of this Request.
     */
    public abstract IncomingSaneCookie [] getCookies();

    /**
     * Obtain a named cookie, or null if not present.
     *
     * @param name the name of the cookie
     * @return the named cookie, or null
     */
    public IncomingSaneCookie getCookie(
            String name )
    {
        IncomingSaneCookie [] cookies = getCookies();
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
     * Obtain the query string, if any.
     *
     * @return the query string
     */
    public abstract String getQueryString();

    /**
     * Obtain the client IP address.
     *
     * @return the client IP address
     */
    public abstract String getClientIp();

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
     * Obtain the value of the accept header, if any.
     *
     * @return the value of the accept header
     */
    public abstract String getAcceptHeader();

    /**
     * Set a request-context attribute. The semantics are equivalent to setting
     * an attribute on an HttpServletRequest.
     *
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @see #getAttribute
     * @see #removeAttribute
     * @see #getAttributeNames
     */
    public abstract void setAttribute(
            String name,
            Object value );

    /**
     * Get a request-context attribute. The semantics are equivalent to getting
     * an attribute on an HttpServletRequest.
     *
     * @param name the name of the attribute
     * @return the value of the attribute
     * @see #setAttribute
     * @see #removeAttribute
     * @see #getAttributeNames
     */
    public abstract Object getAttribute(
            String name );

    /**
     * Remove a request-context attribute. The semantics are equivalent to removing
     * an attribute on an HttpServletRequest.
     *
     * @param name the name of the attribute
     * @see #setAttribute
     * @see #getAttribute
     * @see #getAttributeNames
     */
    public abstract void removeAttribute(
            String name );

    /**
     * Iterate over all request-context attributes currently set.
     *
     * @return an Iterator over the names of all the request-context attributes
     * @see #setAttribute
     * @see #getAttribute
     * @see #removeAttribute
     */
    public abstract Enumeration<String> getAttributeNames();

    /**
     * Obtain the names of the MimeParts conveyed.
     *
     * @return the names of the MimeParts
     */
    public abstract CursorIterator<String> getMimePartNames();

    /**
     * Obtain all MimeParts with a given name
     *
     * @param argName name of the MimePart
     * @return the values, or <code>null</code>
     */
    public abstract MimePart [] getMultivaluedMimeParts(
            String argName );

    /**
     * Obtain a MimePart that was HTTP POST'd. If more than one MimePart was posted with this name,
     * this will throw an IllegalStateException.
     *
     * @param argName name of the argument
     * @return the MimePart or null
     */
    public MimePart getMimePart(
            String argName )
    {
        MimePart [] parts = getMultivaluedMimeParts( argName );
        if( parts == null || parts.length == 0 ) {
            return null;
        } else if( parts.length == 1 ) {
            return parts[0];
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Helper method to convert a class name into a suitable attribute name.
     *
     * @param clazz the Class
     * @return the attribute name
     */
    public static String classToAttributeName(
            Class<?> clazz )
    {
        String ret = clazz.getName();
        ret = ret.replaceAll( "\\.", "_" );
        return ret;
    }

    /**
     * Helper method to convert a class name and a local fragment into a suitable attribute name.
     *
     * @param clazz the Class
     * @param fragment the fragment, or local id
     * @return the attribute name
     */
    public static String classToAttributeName(
            Class<?> clazz,
            String   fragment )
    {
        String ret = clazz.getName();
        ret = ret.replaceAll( "\\.", "_" );
        ret = ret + "__" + fragment;
        return ret;
    }

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
     * The request as it was received by the reverse proxy, if any.
     */
    protected SaneRequest theRequestAtProxy;

    /**
     * Name of the cookie that might contain Accept-Language information.
     */
    public static final String ACCEPT_LANGUAGE_COOKIE_NAME = "Accept-Language";

    /**
     * Name of the HTTP Header that specifies the acceptable MIME types.
     */
    protected static final String ACCEPT_HEADER = "Accept";
}
