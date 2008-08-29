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
 * HTTP methods.
 */
public interface HttpMethods
{
    /** Options. */
    public static final String OPTIONS_METHOD = "OPTIONS";

    /** Get. */
    public static final String GET_METHOD = "GET";

    /** Head. */
    public static final String HEAD_METHOD = "HEAD";

    /** Post. */
    public static final String POST_METHOD = "POST";

    /** Put. */
    public static final String PUT_METHOD = "PUT";

    /** Delete. */
    public static final String DELETE_METHOD = "DELETE";

    /** Trace. */
    public static final String TRACE_METHOD = "TRACE";

    /** Connect. */
    public static final String CONNECT_METHOD = "CONNECT";


    /** All methods defined in HTTP. */
    public static final String [] HTTP_METHODS = {
            OPTIONS_METHOD,
            GET_METHOD,
            HEAD_METHOD,
            POST_METHOD,
            PUT_METHOD,
            DELETE_METHOD,
            TRACE_METHOD,
            CONNECT_METHOD
    };
}
