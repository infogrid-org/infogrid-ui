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

package org.infogrid.testharness.tomcat;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.OutgoingSaneCookie;
import org.infogrid.util.logging.Log;

/**
 * Common functionality for tests that involve accessing an application running on Tomcat.
 */
public abstract class AbstractTomcatTest
        extends
            AbstractTest
{
    /**
     * Constructor if we don't have a special resource file.
     *
     * @param applicationUrl the URL of the application that is being tested, in String form
     */
    protected AbstractTomcatTest(
            String applicationUrl )
    {
        super();

        theApplicationUrl = applicationUrl;
    }

    /**
     * Constructor if we have a special resource file.
     *
     * @param applicationUrl the URL of the application that is being tested, in String form
     * @param nameOfResourceHelperFile the name of the resource file
     */
    protected AbstractTomcatTest(
            String applicationUrl,
            String nameOfResourceHelperFile )
    {
        super( nameOfResourceHelperFile );

        theApplicationUrl = applicationUrl;
    }

    /**
     * Check the content received from the application at a particular URL via HTTP GET.
     * 
     * @param relativeUrl the URL to perform the GET on, relative to the application URL
     * @param mimeRegex regular expression to apply to the returned MIME type, or null
     * @param contentRegex regular expression to apply to the returned content, or null
     * @param statusRegex regular expression to apply to the returned HTTP status code, or null
     * @return true if check passed
     * @param msg message to print when the check fails
     */
    protected boolean checkHttpGet(
            String relativeUrl,
            String mimeRegex,
            String contentRegex,
            String statusRegex,
            String msg )
    {
        return checkHttpGet( relativeUrl, mimeRegex, contentRegex, statusRegex, null, msg );
    }
    
    /**
     * Check the content received from the application at a particular URL via HTTP GET.
     * 
     * @param relativeUrl the URL to perform the GET on, relative to the application URL
     * @param mimeRegex regular expression to apply to the returned MIME type, or null
     * @param contentRegex regular expression to apply to the returned content, or null
     * @param statusRegex regular expression to apply to the returned HTTP status code, or null
     * @param cookiesRegexes map of cookie names to regular expressions of their values
     * @return true if check passed
     * @param msg message to print when the check fails
     */
    protected boolean checkHttpGet(
            String     relativeUrl,
            String     mimeRegex,
            String     contentRegex,
            String     statusRegex,
            String[][] cookiesRegexes,
            String     msg )
    {
        return checkHttpGet( relativeUrl, mimeRegex, DEFAULT_HTTP_REGEX_FLAGS, contentRegex, statusRegex, cookiesRegexes, msg );
        
    }
    /**
     * Check the content received from the application at a particular URL via HTTP GET.
     * 
     * @param relativeUrl the URL to perform the GET on, relative to the application URL
     * @param mimeRegex regular expression to apply to the returned MIME type, or null
     * @param flags to the Pattern, such as Pattern.MULTILINE
     * @param contentRegex regular expression to apply to the returned content, or null
     * @param statusRegex regular expression to apply to the returned HTTP status code, or null
     * @param cookiesRegexes map of cookie names to regular expressions of their values
     * @return true if check passed
     * @param msg message to print when the check fails
     */
    protected boolean checkHttpGet(
            String     relativeUrl,
            String     mimeRegex,
            int        flags,
            String     contentRegex,
            String     statusRegex,
            String[][] cookiesRegexes,
            String     msg )
    {
        String url = theApplicationUrl + relativeUrl;

        Log l = Log.getLogInstance( getClass() ); // use right subclass
        if( l.isDebugEnabled() ) {
            l.debug( "Accessing " + url );
        }
        
        boolean ret = true;
        try {            
            HTTP.Response found = HTTP.http_get( url, false );
    
            if( statusRegex != null ) {
                ret &= checkRegex( statusRegex, flags, found.getResponseCode(), msg + ": response code" );
            }
            if( mimeRegex != null ) {
                ret &= checkRegex( mimeRegex, flags, found.getContentType(), msg + ": mime type" );
            }
            if( contentRegex != null ) {
                ret &= checkRegex( contentRegex, flags, found.getContentAsString(), msg + ": content" );
            }
            if( cookiesRegexes != null ) {
                ret &= checkCookiesRegexes( found, cookiesRegexes, flags, msg + ": cookies" );
            }
            
        } catch( IOException ex ) {
            reportError( msg + ": threw", ex );
            ret = false;
        }
        return ret;
    }
    
    /**
     * Check the cookies received in the HTTP Response.
     * 
     * @param r the HTTP response
     * @param cookiesRegexes map of cookie names to regular expressions of their values
     * @return true if check passed
     * @param msg message to print when the check fails
     */
    protected boolean checkCookiesRegexes(
            HTTP.Response r,
            String[][]    cookiesRegexes,
            String        msg )
    {
        return checkCookiesRegexes( r, cookiesRegexes, DEFAULT_HTTP_REGEX_FLAGS, msg );
    }

    /**
     * Check the cookies received in the HTTP Response.
     * 
     * @param r the HTTP response
     * @param cookiesRegexes map of cookie names to regular expressions of their values
     * @param flags to the Pattern, such as Pattern.MULTILINE
     * @return true if check passed
     * @param msg message to print when the check fails
     */
    protected boolean checkCookiesRegexes(
            HTTP.Response r,
            String[][]    cookiesRegexes,
            int           flags,
            String        msg )
    {
        boolean ret = true;
        Set<OutgoingSaneCookie> cookies = r.getCookies();
        if( cookies.size() != cookiesRegexes.length ) {
            ret &= reportError( msg + ": wrong number of cookies found", cookies, cookiesRegexes );
        }
        for( String [] current : cookiesRegexes ) {
            String name      = current[0];
            String cookRegex = current[1];

            OutgoingSaneCookie cook = r.getCookie( name );
            if( cook == null ) {
                ret &= reportError( msg + ": cookie not found", name );
            } else {
                ret &= checkRegex( cookRegex, flags, cook.getValue(), msg + ": cookie " + name + " has wrong value" );
            }
        }
        return ret;
    }

    /**
     * The URL at which the application runs, in String form.
     */
    protected String theApplicationUrl;
    
    /**
     * The default regular expression flags for checking HTTP response content.
     */
    protected static final int DEFAULT_HTTP_REGEX_FLAGS = Pattern.CANON_EQ | Pattern.DOTALL | Pattern.MULTILINE;
}
