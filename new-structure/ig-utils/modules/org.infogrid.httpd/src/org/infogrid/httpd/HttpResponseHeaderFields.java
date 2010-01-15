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

package org.infogrid.httpd;

/**
  * This collects the HTTP Response fields aka parameters per rfc2616.
  */
public interface HttpResponseHeaderFields
    extends
        GeneralHttpHeaderFields
{
    /** Accept-Ranges. */
    public static final String ACCEPT_RANGES_TAG = "Accept-Ranges";

    /** Accept-Ranges bytes. */
    public static final String ACCEPT_RANGES_TAG_BYTES = "bytes";

    /** Age. */
    public static final String AGE_TAG = "Age";

    /** ETag. */
    public static final String ETAG_TAG = "ETag";

    /** Location. */
    public static final String LOCATION_TAG = "Location";

    /** Proxy-Authenticate. */
    public static final String PROXY_AUTHENTICATE_TAG = "Proxy-Authenticate";

    /** Retry-After. */
    public static final String RETRY_AFTER_TAG = "Retry-After";

    /** Server. */
    public static final String SERVER_TAG = "Server";

    /** Vary. */
    public static final String VARY_TAG = "Vary";

    /** WWW-Authenticate. */
    public static final String WWW_AUTHENTICATE_TAG = "WWW-Authenticate";

    // Not in HTTP 1.1, but nevertheless ...

    /** Set-Cookie. */
    public static final String SET_COOKIE_TAG = "Set-Cookie";

    /** Cookie expiration. */
    public static final String COOKIE_EXPIRES = "expires";

    /** Cookie domain. */
    public static final String COOKIE_DOMAIN = "domain";

    /** Cookie path. */
    public static final String COOKIE_PATH = "path";
}
