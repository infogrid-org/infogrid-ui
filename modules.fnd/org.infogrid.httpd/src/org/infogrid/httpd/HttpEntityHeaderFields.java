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
 * This collects the HTTP Entity Header fields aka parameters per rfc2616.
 */
public class HttpEntityHeaderFields
{
    /** Allow. */
    public static final String ALLOW_TAG = "Allow";

    /** Content-Encoding. */
    public static final String CONTENT_ENCODING_TAG = "Content-Encoding";

    /** Content-Language. */
    public static final String CONTENT_LANGUAGE_TAG = "Content-Language";

    /** Content-Length. */
    public static final String CONTENT_LENGTH_TAG = "Content-Length";

    /** Content-Location. */
    public static final String CONTENT_LOCATION_TAG = "Content-Location";

    /** Content-MD5. */
    public static final String CONTENT_MD5_TAG = "Content-MD5";

    /** Content-Range. */
    public static final String CONTENT_RANGE_TAG = "Content-Range";

    /** Content-Range bytes. */
    public static final String CONTENT_RANGE_BYTES_TAG = "bytes=";

    /** Content-Type. */
    public static final String CONTENT_TYPE_TAG = "Content-Type";

    /** Expires. */
    public static final String EXPIRES_TAG = "Expires";

    /** Last-Modified. */
    public static final String LAST_MODIFIED_TAG = "Last-Modified";
}
