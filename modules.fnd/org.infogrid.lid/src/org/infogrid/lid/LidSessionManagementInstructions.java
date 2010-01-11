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

package org.infogrid.lid;

import javax.servlet.http.HttpServletResponse;
import org.infogrid.util.Identifier;
import org.infogrid.util.http.OutgoingSaneCookie;
import org.infogrid.util.http.SaneRequest;

/**
 * Instructions what to do to manage the client's session.
 */
public interface LidSessionManagementInstructions
{
    /**
     * Obtain the current client's authentication status.
     *
     * @return the current client's authentication status
     */
    public LidClientAuthenticationStatus getClientAuthenticationStatus();

    /**
     * Obtain the LidSessions to cancel, if any.
     *
     * @return the LidSessions to cancel, if any
     */
    public LidSession [] getSessionsToCancel();

    /**
     * Obtain the LidSessions to renew, if any.
     *
     * @return the LidSessions to renew, if any
     */
    public LidSession [] getSessionsToRenew();

    /**
     * Obtain the Identifier of the client for which a new session shall be created, if any.
     *
     * @return the client Identifier
     */
    public Identifier getClientIdentifierForNewSession();

    /**
     * Obtain the Identifier of the site for which a new session shall be created, if any.
     *
     * @return the site Identifier
     */
    public Identifier getSiteIdentifierForNewSession();

    /**
     * Obtain the initial token for the to-be-created new session, if any.
     *
     * @return the initial token
     */
    public String getTokenForNewSession();

    /**
     * Obtain the duration, in milliseconds, for the to-be-created new session, if any.
     *
     * @return the duration, or -1L
     */
    public long getDurationOfNewSession();

    /**
     * Obtain the cookies that shall be removed.
     *
     * @return the cookies, if any
     */
    public OutgoingSaneCookie [] getCookiesToRemove();

    /**
     * Obtain the cookies that shall be set.
     *
     * @return the cookies, if any
     */
    public OutgoingSaneCookie [] getCookiesToSet();

    /**
     * Apply all instructions as recommended.
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @param sessionManager the LidSessionManager to use
     */
    public void applyAsRecommended(
            SaneRequest         request,
            HttpServletResponse response,
            LidSessionManager   sessionManager );
}
