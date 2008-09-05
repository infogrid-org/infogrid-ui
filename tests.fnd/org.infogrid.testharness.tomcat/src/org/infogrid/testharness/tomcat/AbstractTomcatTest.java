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

package org.infogrid.testharness.tomcat;

import java.io.IOException;
import java.util.regex.Pattern;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.http.HTTP;

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
     * @param nameOfLog4jConfigFile the name of the log4j config file
     */
    protected AbstractTomcatTest(
            String applicationUrl,
            String nameOfLog4jConfigFile )
    {
        super( nameOfLog4jConfigFile );

        theApplicationUrl = applicationUrl;
    }

    /**
     * Constructor if we have a special resource file.
     *
     * @param applicationUrl the URL of the application that is being tested, in String form
     * @param nameOfResourceHelperFile the name of the resource file
     * @param nameOfLog4jConfigFile the name of the log4j config file
     */
    protected AbstractTomcatTest(
            String applicationUrl,
            String nameOfResourceHelperFile,
            String nameOfLog4jConfigFile )
    {
        super( nameOfResourceHelperFile, nameOfLog4jConfigFile );

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
        final int flags = Pattern.CANON_EQ | Pattern.DOTALL | Pattern.MULTILINE;
        
        return checkHttpGet( relativeUrl, mimeRegex, flags, contentRegex, statusRegex, msg );
    }
    
    /**
     * Check the content received from the application at a particular URL via HTTP GET.
     * 
     * @param relativeUrl the URL to perform the GET on, relative to the application URL
     * @param mimeRegex regular expression to apply to the returned MIME type, or null
     * @param flags to the Pattern, such as Pattern.MULTILINE
     * @param contentRegex regular expression to apply to the returned content, or null
     * @param statusRegex regular expression to apply to the returned HTTP status code, or null
     * @return true if check passed
     * @param msg message to print when the check fails
     */
    protected boolean checkHttpGet(
            String relativeUrl,
            String mimeRegex,
            int    flags,
            String contentRegex,
            String statusRegex,
            String msg )
    {
        boolean ret = true;
        try {
            String url = theApplicationUrl + relativeUrl;
            
            HTTP.Response found = HTTP.http_get( url );
    
            if( statusRegex != null ) {
                ret &= checkRegex( statusRegex, flags, found.getResponseCode(), msg + ": response code" );
            }
            if( mimeRegex != null ) {
                ret &= checkRegex( mimeRegex, flags, found.getContentType(), msg + ": mime type" );
            }
            if( contentRegex != null ) {
                ret &= checkRegex( contentRegex, flags, found.getContentAsString(), msg + ": content" );
            }
            
        } catch( IOException ex ) {
            reportError( msg + ": threw ", ex );
            ret = false;
        }
        return ret;
    }

    /**
     * The URL at which the application runs, in String form.
     */
    protected String theApplicationUrl;
}
 