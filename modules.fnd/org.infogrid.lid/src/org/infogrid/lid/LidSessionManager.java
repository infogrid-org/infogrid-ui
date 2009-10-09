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
import org.infogrid.util.SmartFactory;

/**
 * Defines the concept of a session manager. The arguments are as follows:
 * <ul>
 *  <li><code>String</code>;: the session token</li>
 *  <li><code>LidSession</code>: the session</li>
 *  <li><code>LidSessionManagerArguments</code>: other information related to the session, such as site and client</li>
 * </ul>
 */
public interface LidSessionManager
        extends
            SmartFactory<String,LidSession,LidSessionManagerArguments>
{
    /**
     * Generate a new session token.
     *
     * @return the new session token
     */
    public String createNewSessionToken();

    /**
     * Obtain the default session duration for newly created or renewed sessions.
     *
     * @return the default session duration, in milliseconds
     */
    public long getDefaultSessionDuration();

    /**
     * The default session duration.
     */
    public static final long DEFAULT_SESSION_DURATION = ResourceHelper.getInstance( LidSessionManager.class ).getResourceLongOrDefault(
            "DefaultSessionDuration",
            8 * 60 * 60 * 1000L );
}
