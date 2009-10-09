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

import org.infogrid.util.FactoryCreatedObject;
import org.infogrid.util.Identifier;

/**
 * <p>Captures the information in a LID session.</p>
 * <p>The parameters are as follows:</p>
 * <ul>
 *  <li><code>String</code>;: the session token</li>
 *  <li><code>LidSession</code>: the session</li>
 *  <li><code>LidSessionManagerArguments</code>: other information related to the session, such as site and client</li>
 * </ul>
 */
public interface LidSession
        extends
            FactoryCreatedObject<String,LidSession,LidSessionManagerArguments>
{
    /**
     * Obtain the Identifier of the client whose session it is.
     *
     * @return the client Identifier
     */
    public Identifier getClientIdentifier();

    /**
     * Obtain the Identifier of the site where the session takes place.
     *
     * @return the site Identifier
     */
    public Identifier getSiteIdentifier();

    /**
     * Obtain the time the LidSession was created.
     *
     * @return the time the LidSession was created, in System.currentTimeMillis() format
     */
    public long getTimeCreated();

    /**
     * Obtain the time the LidSession was last updated, e.g. its token was updated.
     *
     * @return the time the LidSession was last updated, in System.currentTimeMillis() format
     */
    public long getTimeUpdated();

    /**
     * Obtain the time the LidSession was last used.
     *
     * @return the time the LidSession was last used, in System.currentTimeMillis() format
     */
    public long getTimeRead();

    /**
     * Obtain the time the LidSession expires, or has expired.
     *
     * @return the time the LidSession expires, or has expired, in System.currentTimeMillis() format
     */
    public long getTimeExpires();

    /**
     * Obtain the time the LidSession was last authenticated with something stronger than a session cookie.
     *
     * @return the time the LidSession was last authenticated, in System.currentTimeMillis() format
     */
    public long getTimeLastAuthenticated();

    /**
     * Obtain the time the LidSession was last used successfully.
     *
     * @return the time the LidSession was last used successfully, in System.currentTimeMillis() format
     */
    public long getTimeLastUsedSuccessfully();

    /**
     * Obtain the time when the LidSession will or has become invalid.
     *
     * @return the time the LidSession will or has become invalid, in System.currentTimeMillis() format
     */
    public long getTimeValidUntil();

    /**
     * Obtain the session token.
     * 
     * @return the session token
     */
    public String getSessionToken();
    
    /**
     * Obtain the IP address of the client that created this session.
     * 
     * @return the IP address
     */
    public String getCreationClientIp();

    /**
     * Convenience method to determine whether this token is still valid.
     * 
     * @return true if it is still valid.
     */
    public boolean isStillValid();

    /**
     * Notify the session that it was used successfully.
     */
    public void useSuccessfully();

    /**
     * Renew the session.
     *
     * @param duration the duration, in milliseconds, from now
     */
    public void renew(
            long duration );

    /**
     * Invalidate this session.
     */
    public void cancel();
}
