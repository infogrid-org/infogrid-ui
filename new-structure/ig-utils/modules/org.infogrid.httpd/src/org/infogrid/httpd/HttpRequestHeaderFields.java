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
  * This collects the HTTP Request fields aka parameters per rfc2616.
  */
public interface HttpRequestHeaderFields
     extends
         GeneralHttpHeaderFields
{
    /** Accept. */
    public static final String ACCEPT_TAG = "Accept";

    /** Accept-Charset. */
    public static final String ACCEPT_CHARSET_TAG = "Accept-Charset";

    /** Accept-Encoding. */
    public static final String ACCEPT_ENCODING_TAG = "Accept-Encoding";

    /** Accept-Language. */
    public static final String ACCEPT_LANGUAGE_TAG = "Accept-Language";

    /** Authorization. */
    public static final String AUTHORIZATION_TAG = "Authorization";

    /** Expect. */
    public static final String EXPECT_TAG = "Expect";

    /** From. */
    public static final String FROM_TAG = "From";

    /** Host. */
    public static final String HOST_TAG = "Host";

    /** If-Match. */
    public static final String IF_MATCH_TAG = "If-Match";

    /** If-Modified-Since. */
    public static final String IF_MODIFIED_SINCE_TAG = "If-Modified-Since";

    /** If-None-Match. */
    public static final String IF_NONE_MATCH_TAG = "If-None-Match";

    /** If-Range. */
    public static final String IF_RANGE_TAG = "If-Range";

    /** If-Unmodified-Since. */
    public static final String IF_UNMODIFIED_SINCE_TAG = "If-Unmodified-Since";

    /** Max-Forwards. */
    public static final String MAX_FORWARDS_TAG = "Max-Forwards";

    /** Proxy-Authorization. */
    public static final String PROXY_AUTHORIZATION_TAG = "Proxy-Authorization";

    /** Range. */
    public static final String RANGE_TAG = "Range";

    /** TE. */
    public static final String TE_TAG = "TE";

    /** User-Agent. */
    public static final String USER_AGENT_TAG = "User-Agent";

    // Not in HTTP 1.1, but nevertheless ...

    /** Cookie. */
    public static final String COOKIE_TAG = "Cookie";
}
