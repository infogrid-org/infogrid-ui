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
  * This collects the HTTP status codes per rfc2616.
  */
public interface HttpStatusCodes
{
// Informational 1xx

    /** HTTP code 100 Continue. */
    public static final String CONTINUE_CODE = "100 Continue";

    /** HTTP code 101 Switching Protocols. */
    public static final String SWITCHING_PROTOCOLS_CODE = "101 Switching Protocols";

// Successful 2xx

    /** HTTP code 200 OK. */
    public static final String OK_CODE = "200 OK";

    /** HTTP code 201 Created. */
    public static final String CREATED_CODE = "201 Created";

    /** HTTP code 202 Accepted. */
    public static final String ACCEPTED_CODE = "202 Accepted";

    /** HTTP code 203 Non-Authoritative Information. */
    public static final String NON_AUTHORITATIVE_INFORMATION_CODE = "203 Non-Authoritative Information";

    /** HTTP code 204 No Content. */
    public static final String NO_CONTENT_CODE = "204 No Content";

    /** HTTP code 205 Reset Content. */
    public static final String RESET_CONTENT_CODE = "205 Reset Content";

    /** HTTP code 206 Partial Content. */
    public static final String PARTIAL_CONTENT_CODE = "206 Partial Content";

// Redirection 3xx

    /** HTTP code 300 Multiple Choices. */
    public static final String MULTIPLE_CHOICES_CODE = "300 Multiple Choices";

    /** HTTP code 301 Moved Permanently. */
    public static final String MOVED_PERMANENTLY_CODE = "301 Moved Permanently";

    /** HTTP code 302 Found. */
    public static final String FOUND_CODE = "302 Found";

    /** HTTP code 303 See Other. */
    public static final String SEE_OTHER_CODE = "303 See Other";

    /** HTTP code 304 Not Modified. */
    public static final String NOT_MODIFIED_CODE = "304 Not Modified";

    /** HTTP code 305 Use Proxy. */
    public static final String USE_PROXY_CODE = "305 Use Proxy";

    /** HTTP code 306 Unused. */
    public static final String UNUSED_CODE = "306 Unused";

    /** HTTP code 307 Temporary Redirect. */
    public static final String TEMPORARY_REDIRECT_CODE = "307 Temporary Redirect";

// Client Error 4xx

    /** HTTP code 400 Bad Request. */
    public static final String BAD_REQUEST_CODE = "400 Bad Request";

    /** HTTP code 401 Unauthorized. */
    public static final String UNAUTHORIZED_CODE = "401 Unauthorized";

    /** HTTP code 402 Payment Required. */
    public static final String PAYMENT_REQUIRED_CODE = "402 Payment Required";

    /** HTTP code 403 Forbidden. */
    public static final String FORBIDDEN_CODE = "403 Forbidden";

    /** HTTP code 404 Not Found. */
    public static final String NOT_FOUND_CODE = "404 Not Found";

    /** HTTP code 405 Method Not Allowed. */
    public static final String METHOD_NOT_ALLOWED_CODE = "405 Method Not Allowed";

    /** HTTP code 406 Not Acceptable. */
    public static final String NOT_ACCEPTABLE_CODE = "406 Not Acceptable";

    /** HTTP code 407 Proxy Authentication Required. */
    public static final String PROXY_AUTHENTICATION_REQUIRED_CODE = "407 Proxy Authentication Required";

    /** HTTP code 408 Request Timeout. */
    public static final String REQUEST_TIMEOUT_CODE = "408 Request Timeout";

    /** HTTP code 409 Conflict. */
    public static final String CONFLICT_CODE = "409 Conflict";

    /** HTTP code 410 Gone. */
    public static final String GONE_CODE = "410 Gone";

    /** HTTP code 411 Length Required. */
    public static final String LENGTH_REQUIRED_CODE = "411 Length Required";

    /** HTTP code 412 Precondition Failed. */
    public static final String PRECONDITION_FAILED_CODE = "412 Precondition Failed";

    /** HTTP code 413 Request Entity Too Large. */
    public static final String REQUEST_ENTITY_TOO_LARGE_CODE = "413 Request Entity Too Large";

    /** HTTP code 414 Request-URI Too Long. */
    public static final String REQUEST_URI_TOO_LONG_CODE = "414 Request-URI Too Long";

    /** HTTP code 415 Unsupported Media Type. */
    public static final String UNSUPPORTED_MEDIA_TYPE_CODE = "415 Unsupported Media Type";

    /** HTTP code 416 Requested Range Not Satisfiable. */
    public static final String REQUESTED_RANGE_NOT_SATISFIABLE_CODE = "416 Requested Range Not Satisfiable";

    /** HTTP code 417 Expectation Failed. */
    public static final String EXPECTATION_FAILED_CODE = "417 Expectation Failed";

// Server Error 5xx

    /** HTTP code 500 Internal Server Error. */
    public static final String INTERNAL_SERVER_ERROR_CODE = "500 Internal Server Error";

    /** HTTP code 501 Not Implemented. */
    public static final String NOT_IMPLEMENTED_CODE = "501 Not Implemented";

    /** HTTP code 502 Bad Gateway. */
    public static final String BAD_GATEWAY_CODE = "502 Bad Gateway";

    /** HTTP code 503 Service Unavailable. */
    public static final String SERVICE_UNAVAIALBLE_CODE = "503 Service Unavailable";

    /** HTTP code 504 Gateway Timeout. */
    public static final String GATEWAY_TIMEOUT_CODE = "504 Gateway Timeout";

    /** HTTP code 505 HTTP Version Not Supported. */
    public static final String HTTP_VERSION_NOT_SUPPORTED_CODE = "505 HTTP Version Not Supported";
}
