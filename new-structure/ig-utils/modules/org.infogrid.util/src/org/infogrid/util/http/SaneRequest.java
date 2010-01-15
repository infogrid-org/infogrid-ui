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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.util.http;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.infogrid.util.CursorIterator;

/**
 * A saner API for incoming HTTP requests than the JDK's HttpServletRequest.
 */
public interface SaneRequest
{
    /**
     * If this request was obtained by way of a reverse proxy, return the SaneRequest
     * that the reverse proxy received. Returns null if no reverse proxy was involved.
     *
     * @return the SaneRequest at the reverse proxy, or null if none
     */
    public abstract SaneRequest getSaneRequestAtProxy();

    /**
     * Obtain the original request as originally issued by the HTTP client. If a reverse
     * proxy was involved, return the SaneRequest that the reverse proxy received. If
     * no reverse proxy was involved, return this SaneRequest.
     *
     * @return the ultimate SaneRequest
     */
    public abstract SaneRequest getOriginalSaneRequest();

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
    public abstract String getRootUri();

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
    public abstract String getAbsoluteBaseUri();

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
    public abstract String getAbsoluteFullUri();

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
     * Obtain the value of a named argument provided in the URL, or null.
     * If more than one argument is given by this name,
     * this will throw an IllegalStateException.
     *
     * @param name the name of the argument
     * @return the value of the argument with name name
     */
    public abstract String getUrlArgument(
            String name );

    /**
     * Obtain the value of a named argument provided in the URL, or null.
     * If more than one argument is given by this name,
     * return the first one.
     *
     * @param name the name of the argument
     * @return the value of the argument with name name
     */
    public abstract String getFirstUrlArgument(
            String name );

    /**
     * Obtain all arguments of this Request provided in the URL.
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
    public abstract boolean matchUrlArgument(
            String name,
            String value );

    /**
     * Obtain a POST'd argument. If more than one argument is given by this name,
     * this will throw an IllegalStateException.
     *
     * @param argName name of the argument
     * @return value.
     */
    public abstract String getPostedArgument(
            String argName );

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
    public abstract boolean matchPostedArgument(
            String name,
            String value );

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
    public abstract String getContextPathWithSlash();

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
    public abstract String getAbsoluteContextUriWithSlash();

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
    public abstract IncomingSaneCookie getCookie(
            String name );

    /**
     * Obtain the value of a named cookie, or null if not present.
     *
     * @param name the name of the cookie
     * @return the value of the named cookie, or null
     */
    public abstract String getCookieValue(
            String name );

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
    public abstract MimePart getMimePart(
            String argName );

    /**
     * Return this absolute full URL but with all URL arguments stripped whose names meet at least
     * one of the provided Patterns.
     * For example, http://example.com/?abc=def&abcd=ef&abcde=f&x=y would become http://example.com?abc=def&x=y
     * if invoked with Pattern "^abcd.*$".
     *
     * @param patterns the Patterns
     * @return the absolute full URL without the matched URL arguments
     */
    public abstract String getAbsoluteFullUriWithoutMatchingArguments(
            Pattern [] patterns );
}
