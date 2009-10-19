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

package org.infogrid.lid;

import org.infogrid.util.ResourceHelper;

/**
 * Defines the names of the LID cookies. This class must not be instantiated.
 */
public interface LidCookies
{
    /**
     * The ResourceHelper.
     */
    static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( LidCookies.class );
    
    /**
     * Name of the LID identifier cookie.
     */
    public static final String LID_IDENTIFIER_COOKIE_NAME = theResourceHelper.getResourceStringOrDefault(
            "LidIdentifierCookieName",
            "org-netmesh-lid-lid" );

    /**
     * Name of the LID session cookie.
     */
    public static final String LID_SESSION_COOKIE_NAME = theResourceHelper.getResourceStringOrDefault(
            "LidSessionCookieName",
            "org-netmesh-lid-session" );

    /**
     * Default maximum age, in seconds, for the LID identifier cookie.
     */
    public static final int LID_IDENTIFIER_COOKIE_DEFAULT_MAX_AGE = theResourceHelper.getResourceIntegerOrDefault( 
            "LidIdentifierCookieDefaultMaxAge",
            60*60*24*365 ); // one year
    
    /**
     * Default maximum age, in seconds, for the LID session cookie. Note that the LidSessionManager may decide
     * to expire the cookie far earlier than it is indicated to the client. If this is negative, the cookie
     * will be a session cookie.
     */
    public static final int LID_SESSION_COOKIE_DEFAULT_MAX_AGE = theResourceHelper.getResourceIntegerOrDefault( 
            "LidSessionCookieDefaultMaxAge",
            -1 ); // session cookie
}
