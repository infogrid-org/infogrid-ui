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

package org.infogrid.jee.templates;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.Cookie;
import org.infogrid.jee.ProblemReporter;

/**
 * Utility interface that enables us to treat StructuredResponse and StructuredResponseSection
 * uniformly for the purposes of processing their header preferences e.g. cookies.
 */
public interface HasHeaderPreferences
        extends
            ProblemReporter
{
    /**
     * Obtain the desired MIME type.
     * 
     * @return the desired MIME type
     */
    public String getMimeType();

    /**
     * Obtain the Cookies.
     * 
     * @return the Cookies
     */
    public Collection<Cookie> getCookies();

    /**
     * Obtain the location header.
     * 
     * @return the currently set location header
     */
    public String getLocation();

    /**
     * Obtain the HTTP response code.
     * 
     * @return the HTTP response code
     */
    public int getHttpResponseCode();
    
    /**
     * Obtain the locale.
     * 
     * @return the locale
     */
    public Locale getLocale();

    /**
     * Obtain the character encoding.
     * 
     * @return the character encoding
     */
    public String getCharacterEncoding();

    /**
     * Obtain the additional headers.
     *
     * @return the headers, as Map
     */
    public Map<String,String[]> getHeaders();
}
