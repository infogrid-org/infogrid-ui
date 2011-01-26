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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.security;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.UniqueStringGenerator;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.http.SaneRequestUtils;

/**
 * Categorizes incoming POST requests as safe or unsafe, depending on whether they contain
 * a valid form token or not.
 */
public class SafeUnsafePostFilter
        implements
            Filter
{
    /**
     * Constructor.
     */
    public SafeUnsafePostFilter()
    {
        // nothing right now
    }

    /**
     * Execute the filter.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    public void doFilter(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        boolean isSafe = true;

        if( request instanceof HttpServletRequest ) {
            HttpServletRequest realRequest = (HttpServletRequest) request;

            SaneRequest sane        = SaneServletRequest.create( realRequest );
            String      cookieValue = sane.getCookieValue( COOKIE_NAME );

            if( cookieValue == null ) {
                HttpServletResponse realResponse = (HttpServletResponse) response;
                cookieValue = theGenerator.createUniqueToken();

                realResponse.addCookie( new Cookie( COOKIE_NAME, cookieValue ));
            }
            sane.setAttribute( TOKEN_ATTRIBUTE_NAME, cookieValue );

            if( "POST".equalsIgnoreCase( realRequest.getMethod() )) {

                String relativePath = realRequest.getServletPath();
                boolean process;

                if( theExcludedPattern != null ) {
                    Matcher m = theExcludedPattern.matcher( relativePath );
                    if( m.matches() ) {
                        process = false;
                    } else {
                        process = true;
                    }
                } else {
                    process = true;
                }

                if( process ) {
                    String token = sane.getPostedArgument( INPUT_FIELD_NAME );

                    if( cookieValue == null || token == null ) {
                        isSafe = false;
                    } else {
                        isSafe = cookieValue.equals( token );
                    }
                }
            }
        }
        request.setAttribute( SAFE_UNSAFE_FLAG, isSafe );
        chain.doFilter( request, response );
    }
    
    /**
     * Initialize the Filter.
     *
     * @param filterConfig the Filter configuration object
     * @throws ServletException thrown if misconfigured
     */
    public void init(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        theFilterConfig  = filterConfig;

        String excludeRegex = filterConfig.getInitParameter( EXCLUDE_REGEX_PARAMETER );
        if( excludeRegex != null && excludeRegex.length() > 0 ) {
            theExcludedPattern = Pattern.compile( excludeRegex );
        }
    }
    
    /**
     * Destroy method for this Filter.
     */
    public void destroy()
    {
        // noop
    }
    
    /**
     * Determine whether this incoming request is a safe POST. This is a static method
     * here so it can be invoked from anywhere in the application.
     * 
     * @param request the incoming request
     * @return true if this is an HTTP POST, and the POST is safe
     */
    public static boolean isSafePost(
            HttpServletRequest request )
    {
        boolean ret = false;
        if( "POST".equalsIgnoreCase( request.getMethod() )) {
            Boolean safeUnsafe = (Boolean) request.getAttribute( SafeUnsafePostFilter.SAFE_UNSAFE_FLAG );
            if( safeUnsafe != null && safeUnsafe.booleanValue() ) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Determine whether this incoming request is a safe POST. This is a static method
     * here so it can be invoked from anywhere in the application.
     *
     * @param request the incoming request
     * @return true if this is an HTTP POST, and the POST is safe
     */
    public static boolean isSafePost(
            SaneRequest request )
    {
        boolean ret = false;
        if( "POST".equalsIgnoreCase( request.getMethod() )) {
            Boolean safeUnsafe = (Boolean) request.getAttribute( SafeUnsafePostFilter.SAFE_UNSAFE_FLAG );
            if( safeUnsafe != null && safeUnsafe.booleanValue() ) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Determine whether this incoming request is an unsafe POST. This is a static method
     * here so it can be invoked from anywhere in the application.
     * 
     * @param request the incoming request
     * @return true if this is an HTTP POST, but the POST is not safe
     */
    public static boolean isUnsafePost(
            HttpServletRequest request )
    {
        boolean ret = false;
        if( "POST".equalsIgnoreCase( request.getMethod() )) {
            Boolean safeUnsafe = (Boolean) request.getAttribute( SafeUnsafePostFilter.SAFE_UNSAFE_FLAG );
            if( safeUnsafe != null && !safeUnsafe.booleanValue() ) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Determine whether this incoming request is an unsafe POST. This is a static method
     * here so it can be invoked from anywhere in the application.
     *
     * @param request the incoming request
     * @return true if this is an HTTP POST, but the POST is not safe
     */
    public static boolean isUnsafePost(
            SaneRequest request )
    {
        boolean ret = false;
        if( "POST".equalsIgnoreCase( request.getMethod() )) {
            Boolean safeUnsafe = (Boolean) request.getAttribute( SafeUnsafePostFilter.SAFE_UNSAFE_FLAG );
            if( safeUnsafe != null && !safeUnsafe.booleanValue() ) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Determine whether this incoming request has not run through the SafeUnsafePostFilter, and thus
     * it cannot be determined whether the HTTP POST is safe or not.
     * 
     * @param request the incoming request
     * @return true if this is an HTTP POST and it is unclear whether it is safe or not
     */
    public static boolean mayBeSafeOrUnsafePost(
            HttpServletRequest request )
    {
        boolean ret = false;
        if( "POST".equalsIgnoreCase( request.getMethod() )) {
            Boolean safeUnsafe = (Boolean) request.getAttribute( SafeUnsafePostFilter.SAFE_UNSAFE_FLAG );
            if( safeUnsafe == null ) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Determine whether this incoming request has not run through the SafeUnsafePostFilter, and thus
     * it cannot be determined whether the HTTP POST is safe or not.
     *
     * @param request the incoming request
     * @return true if this is an HTTP POST and it is unclear whether it is safe or not
     */
    public static boolean mayBeSafeOrUnsafePost(
            SaneRequest request )
    {
        boolean ret = false;
        if( "POST".equalsIgnoreCase( request.getMethod() )) {
            Boolean safeUnsafe = (Boolean) request.getAttribute( SafeUnsafePostFilter.SAFE_UNSAFE_FLAG );
            if( safeUnsafe == null ) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * The filter configuration object this Filter is associated with.
     */
    protected FilterConfig theFilterConfig = null;
    
    /**
     * Regular expression that identifies excluded requests.
     */
    protected Pattern theExcludedPattern;

    /**
     * Name of the Filter configuration parameter that contains the regular expression to exclude.
     */
    public static final String EXCLUDE_REGEX_PARAMETER = "EXCLUDE_REGEX";

    /**
     * Name of the attribute in the incoming request that indicates whether this is a safe request or not.
     */
    public static final String SAFE_UNSAFE_FLAG
            = SaneRequestUtils.classToAttributeName( SafeUnsafePostFilter.class, "safeunsafe" );

    /**
     * Our ResourceHelper, so field and cookie names are configurable.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( SafeUnsafePostFilter.class );

    /**
     * Name of the hidden field in the form.
     */
    public static final String INPUT_FIELD_NAME = theResourceHelper.getResourceStringOrDefault(
            "InputFieldName",
            SafeUnsafePostFilter.class.getName().replace( '.', '-' ) + "-field" );

    /**
     * Name of the cookie.
     */
    public static final String COOKIE_NAME = theResourceHelper.getResourceStringOrDefault(
            "CookieName",
            SafeUnsafePostFilter.class.getName().replace( '.', '-' ) + "-cookie" );

    /**
     * Name of the cookie value as stored in the request attribute.
     */
    public static final String TOKEN_ATTRIBUTE_NAME
            = SafeUnsafePostFilter.class.getName().replace( '.', '-' ) + "-value";

    /**
     * The length of the token.
     */
    protected static final int TOKEN_LENGTH = theResourceHelper.getResourceIntegerOrDefault(
            "TokenLength",
            64 );

    /**
     * The underlying random generator.
     */
    protected static final UniqueStringGenerator theGenerator = UniqueStringGenerator.create( TOKEN_LENGTH );
}
