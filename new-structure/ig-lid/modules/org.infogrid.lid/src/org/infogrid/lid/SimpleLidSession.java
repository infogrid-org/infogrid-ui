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

import org.infogrid.util.AbstractFactoryCreatedObject;
import org.infogrid.util.Identifier;

/**
 * A simple implementation of LidSession.
 */
public class SimpleLidSession
        extends
            AbstractFactoryCreatedObject<String,LidSession,LidSessionManagerArguments>
        implements
            LidSession
{
    /**
     * Factory method.
     *
     * @param sessionToken the value identifying this session in a browser cookie
     * @param clientIdentifier the LID identifier of the client
     * @param siteIdentifier the identifier of the site
     * @param timeCreated the time the session was created, in System.currentTimeMillis() format
     * @param timeUpdated the time the session was last updated, in System.currentTimeMillis() format
     * @param timeRead the time the session was last read, in System.currentTimeMillis() format
     * @param timeExpires the time the session was or will expire, in System.currentTimeMillis() format
     * @param timeAuthenticated the time the session was last authenticated, in in System.currentTimeMillis() format
     * @param timeUsedSuccessfully the time the session was last used successfully, in System.currentTimeMillis() format
     * @param timeValidUntil the time has or will become invalid, in System.currentTimeMillis() format
     * @param creationClientIp the IP address of the client that created the session
     * @return the created LidSession
     */
    public static SimpleLidSession create(
            String     sessionToken,
            Identifier clientIdentifier,
            Identifier siteIdentifier,
            long       timeCreated,
            long       timeUpdated,
            long       timeRead,
            long       timeExpires,
            long       timeAuthenticated,
            long       timeUsedSuccessfully,
            long       timeValidUntil,
            String     creationClientIp )
    {
        SimpleLidSession ret = new SimpleLidSession(
                sessionToken,clientIdentifier,
                siteIdentifier,
                timeCreated,
                timeUpdated,
                timeRead,
                timeExpires,
                timeAuthenticated,
                timeUsedSuccessfully,
                timeValidUntil,
                creationClientIp );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param sessionToken the value identifying this session in a browser cookie
     * @param clientIdentifier the LID identifier of the client
     * @param siteIdentifier the identifier of the site
     * @param timeCreated the time the session was created, in System.currentTimeMillis() format
     * @param timeUpdated the time the session was last updated, in System.currentTimeMillis() format
     * @param timeRead the time the session was last read, in System.currentTimeMillis() format
     * @param timeExpires the time the session was or will expire, in System.currentTimeMillis() format
     * @param timeAuthenticated the time the session was last authenticated, in in System.currentTimeMillis() format
     * @param timeUsedSuccessfully the time the session was last used successfully, in System.currentTimeMillis() format
     * @param timeValidUntil the time has or will become invalid, in System.currentTimeMillis() format
     * @param creationClientIp the IP address of the client that created the session
     */
    protected SimpleLidSession(
            String     sessionToken,
            Identifier clientIdentifier,
            Identifier siteIdentifier,
            long       timeCreated,
            long       timeUpdated,
            long       timeRead,
            long       timeExpires,
            long       timeAuthenticated,
            long       timeUsedSuccessfully,
            long       timeValidUntil,
            String     creationClientIp )
    {
        theSessionToken         = sessionToken;
        theClientIdentifier     = clientIdentifier;
        theSiteIdentifier       = siteIdentifier;
        theTimeCreated          = timeCreated;
        theTimeUpdated          = timeUpdated;
        theTimeRead             = timeRead;
        theTimeExpires          = timeExpires;
        theTimeAuthenticated    = timeAuthenticated;
        theTimeUsedSuccessfully = timeUsedSuccessfully;
        theTimeValidUntil       = timeValidUntil;
        theCreationClientIp     = creationClientIp;
    }

    /**
     * Obtain the key for that was used to create this object by the Factory.
     *
     * @return the key
     */
    public String getFactoryKey()
    {
        return theSessionToken;
    }

    /**
     * Obtain the Identifier of the client whose session it is.
     *
     * @return the client Identifier
     */
    public Identifier getClientIdentifier()
    {
        return theClientIdentifier;
    }

    /**
     * Obtain the Identifier of the site where the session takes place.
     *
     * @return the site Identifier
     */
    public Identifier getSiteIdentifier()
    {
        return theSiteIdentifier;
    }

    /**
     * Obtain the time the LidSession was created.
     *
     * @return the time the LidSession was created, in System.currentTimeMillis() format
     */
    public long getTimeCreated()
    {
        return theTimeCreated;
    }

    /**
     * Obtain the time the LidSession was last updated, e.g. its token was updated.
     *
     * @return the time the LidSession was last updated, in System.currentTimeMillis() format
     */
    public long getTimeUpdated()
    {
        return theTimeUpdated;
    }

    /**
     * Obtain the time the LidSession was last used.
     *
     * @return the time the LidSession was last used, in System.currentTimeMillis() format
     */
    public long getTimeRead()
    {
        return theTimeRead;
    }

    /**
     * Obtain the time the LidSession expires, or has expired.
     *
     * @return the time the LidSession expires, or has expired, in System.currentTimeMillis() format
     */
    public long getTimeExpires()
    {
        return theTimeExpires;
    }

    /**
     * Obtain the time the LidSession was last authenticated with something stronger than a session cookie.
     *
     * @return the time the LidSession was last authenticated, in System.currentTimeMillis() format
     */
    public long getTimeLastAuthenticated()
    {
        return theTimeAuthenticated;
    }

    /**
     * Obtain the time the LidSession was last used successfully.
     *
     * @return the time the LidSession was last used successfully, in System.currentTimeMillis() format
     */
    public long getTimeLastUsedSuccessfully()
    {
        return theTimeUsedSuccessfully;
    }

    /**
     * Obtain the time when the LidSession will or has become invalid.
     *
     * @return the time the LidSession will or has become invalid, in System.currentTimeMillis() format
     */
    public long getTimeValidUntil()
    {
        return theTimeValidUntil;
    }

    /**
     * Obtain the session token.
     *
     * @return the session token
     */
    public String getSessionToken()
    {
        return theSessionToken;
    }

    /**
     * Obtain the IP address of the client that created this session.
     *
     * @return the IP address
     */
    public String getCreationClientIp()
    {
        return theCreationClientIp;
    }

    /**
     * Determine whether this token is still valid.
     *
     * @return true if it is still valid.
     */
    public boolean isStillValid()
    {
        long delta = theTimeExpires - System.currentTimeMillis();
        if( delta > 0 ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Notify the session that it was used successfully.
     */
    public void useSuccessfully()
    {
        long now = System.currentTimeMillis();

        theTimeRead             = now;
        theTimeUsedSuccessfully = now;

        factoryCreatedObjectUpdated();
    }

    /**
     * Renew the session.
     *
     * @param duration the duration, in milliseconds, from now
     */
    public void renew(
            long duration )
    {
        long now = System.currentTimeMillis();

        theTimeUpdated          = now;
        theTimeExpires          = now + duration;
        theTimeAuthenticated    = now;

        factoryCreatedObjectUpdated();
    }

    /**
     * Invalidate this session.
     */
    public void cancel()
    {
        theTimeExpires    = System.currentTimeMillis()-1L; // subtract one to be safe on fast machines
        theTimeValidUntil = theTimeExpires;

        factoryCreatedObjectUpdated();
    }

    /**
     * The value identifying this session.
     */
    protected String theSessionToken;

    /**
     * Identifier of the client whose session it is.
     */
    protected Identifier theClientIdentifier;

    /**
     * Identifier of the site whose session it is.
     */
    protected Identifier theSiteIdentifier;

    /**
     * The time the session was created, in System.currentTimeMillis() format.
     */
    protected long theTimeCreated;

    /**
     * The time the session was last updated, in System.currenTimeMillis() format.
     */
    protected long theTimeUpdated;

    /**
     * The time the session was last accessed, in System.currentTimeMillis() format.
     */
    protected long theTimeRead;

    /**
     * The time the session has expired or will expires, in System.currentTimeMillis() format.
     */
    protected long theTimeExpires;

    /**
     * The time the session was last authenticated, in System.currentTimeMillis() format.
     */
    protected long theTimeAuthenticated;

    /**
     * The time the session was last used successfully, in System.currentTimeMillis() format.
     */
    protected long theTimeUsedSuccessfully;

    /**
     * The time when the LidSession will or has become invalid, in System.currentTimeMillis() format.
     */
    protected long theTimeValidUntil;

    /**
     * The IP address of the client that created the session.
     */
    protected String theCreationClientIp;
}
