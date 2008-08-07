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

package org.infogrid.lid;

import org.infogrid.util.ResourceHelper;

/**
 * Defines the names of the LID cookies. This class must not be instantiated.
 */
public abstract class LidCookies
{
    /**
     * Private constructor to make instantiation impossible.
     */
    private LidCookies()
    {
        // nothing
    }

    /**
     * The ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( LidCookies.class  );
    
    /**
     * Name of the LID identifier cookie.
     */
    public static final String LID_IDENTIFIER_COOKIE_NAME = theResourceHelper.getResourceStringOrDefault(
            "LidIdentifierCookieName",
            "org.netmesh.lid.lid" );

    /**
     * Name of the LID session cookie.
     */
    public static final String LID_SESSION_COOKIE_NAME = theResourceHelper.getResourceStringOrDefault(
            "LidSessionCookieName",
            "org.netmesh.lid.session" );

}
