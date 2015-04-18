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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.infogrid.util.Base64;
import org.infogrid.util.CreateWhenNeeded;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.UniqueStringGenerator;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.http.SaneRequestUtils;
import org.infogrid.util.logging.Log;

/**
 * Categorizes incoming POST requests as safe or unsafe, depending on whether they contain
 * a valid form token or not.
 *
 * By default, we generate random cookies, and a random secret for this SafeUnsafePostFilter
 * instance. When forms are created, they can ask for a form field, which is the hash of
 * the cookie and the secret. Upon post, we check for the existence of the POST field, and
 * re-calculate the hash.
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
            HttpServletRequest  realRequest  = (HttpServletRequest)  request;
            HttpServletResponse realResponse = (HttpServletResponse) response;

            SaneRequest sane        = SaneServletRequest.create( realRequest );
            String      cookieValue = sane.getCookieValue( COOKIE_NAME );

            if( cookieValue == null ) {
                cookieValue = createNewCookieValue( sane );

                Cookie cook = new Cookie( COOKIE_NAME, cookieValue );
                cook.setPath( sane.getContextPath() );
                realResponse.addCookie( cook );
            }

            final String finalCookieValue = cookieValue; // sometimes all you can do is to hate Java
            sane.setAttribute( TOKEN_ATTRIBUTE_NAME, new CreateWhenNeeded<String>() {
                    protected String instantiate()
                    {
                        return calculateFormFieldValue( finalCookieValue );
                    }
            });

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
                    String [] formValues = sane.getMultivaluedPostedArgument( INPUT_FIELD_NAME );

                    if( cookieValue == null || formValues == null || formValues.length == 0 ) {
                        isSafe = false;
                    } else {
                        String correctFormValue = calculateFormFieldValue( cookieValue );
                        isSafe = true;
                        for( String formValue : formValues ) {
                            // no harm if the same token is submitted more than once, but it better have the same value
                            if( !formValue.equals( correctFormValue )) {
                                isSafe = false;
                                break;
                            }
                        }
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
     * Generate a new cookie value.
     *
     * @param sane the incoming request
     * @return the cookie value
     */
    protected String createNewCookieValue(
            SaneRequest sane )
    {
        String cookieValue = theGenerator.createUniqueToken();
        return cookieValue;
    }

    /**
     * Calculate the form field value from the cookie value and the secret.
     *
     * @param cookieValue the value of the cookie
     * @return the value of the field in the form
     */
    protected String calculateFormFieldValue(
            String cookieValue )
    {
        try {
            MessageDigest md = MessageDigest.getInstance( DIGEST_ALGORITHM );
            md.update( cookieValue.getBytes( "UTF-8" ));

            byte hash [] = md.digest();

            String ret = Base64.base64encode( hash );
            ret = ret.replaceAll( "\\s", "" );
            return ret;

        } catch ( NoSuchAlgorithmException ex ) {
            getLog().error( ex );
        } catch ( UnsupportedEncodingException ex ) {
            getLog().error( ex );
        }
        return null;
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
     * Get the logger.
     *
     * @return the logger
     */
    private static Log getLog()
    {
        if( _log == null ) {
            _log = Log.getLogInstance( SafeUnsafePostFilter.class );
        }
        return _log;
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
            SafeUnsafePostFilter.class.getName().toLowerCase().replace( '.', '-' ) + "-cookie" );

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
            32 );

    /**
     * The underlying random generator.
     */
    protected static final UniqueStringGenerator theGenerator = UniqueStringGenerator.create( TOKEN_LENGTH );

    /**
     * The Digest algorithm to use.
     */
    public static final String DIGEST_ALGORITHM = "SHA-512";

    /**
     * Our own, private logger. Allocated as needed: can't be done statically as this is a Filter.
     */
    private static Log _log; // allocated as needed

    /**
     * The secret for this instance of SafeUnsafePostFilter. This can be overridden in the Resource in order to
     * allow easier test instrumentation.
     */
    protected String theMySecret = theResourceHelper.getResourceStringOrDefault( "MySecret", theGenerator.createUniqueToken() );
}
