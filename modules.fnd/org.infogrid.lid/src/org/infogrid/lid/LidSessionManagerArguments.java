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

import org.infogrid.util.Identifier;

/**
 * Collects the arguments to the LidSessionManager factory methods.
 */
public class LidSessionManagerArguments
{
    /**
     * Factory method.
     *
     * @param sessionDuration the session duration, in milliseconds
     * @param clientIdentifier identifier of the client whose session it is
     * @param siteIdentifier identifier of the site where the session takes place
     * @param clientIp IP address of the client that created the session
     * @return the created LidSessionManagerArguments
     */
    public static LidSessionManagerArguments create(
            long       sessionDuration,
            Identifier clientIdentifier,
            Identifier siteIdentifier,
            String     clientIp )
    {
        return new LidSessionManagerArguments( sessionDuration, clientIdentifier, siteIdentifier, clientIp );
    }

    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param sessionDuration the session duration, in milliseconds
     * @param clientIdentifier identifier of the client whose session it is
     * @param siteIdentifier identifier of the site where the session takes place
     * @param clientIp IP address of the client that created the session
     */
    protected LidSessionManagerArguments(
            long       sessionDuration,
            Identifier clientIdentifier,
            Identifier siteIdentifier,
            String     clientIp )
    {
        theSessionDuration  = sessionDuration;
        theClientIdentifier = clientIdentifier;
        theSiteIdentifier   = siteIdentifier;
        theClientIp         = clientIp;
    }

    /**
     * Obtain the duration of the session.
     * 
     * @return the duration of the session, in milliseconds.
     */
    public long getSessionDuration()
    {
        return theSessionDuration;
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
     * Obtain the IP address of the client that originally created the session.
     *
     * @return the IP address
     */
    public String getClientIp()
    {
        return theClientIp;
    }

    /**
     * The duration of the session.
     */
    protected long theSessionDuration;

    /**
     * Identifier of the client whose session it is.
     */
    protected Identifier theClientIdentifier;

    /**
     * Identifier of the site whose session it is.
     */
    protected Identifier theSiteIdentifier;

    /**
     * The IP address of the client that originally created the session.
     */
    protected String theClientIp;
}
