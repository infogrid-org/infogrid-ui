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

import org.infogrid.util.AbstractFactory;
import org.infogrid.util.CachingMap;
import org.infogrid.util.Factory;
import org.infogrid.util.MSmartFactory;
import org.infogrid.util.ResourceHelper;

/**
 * Factors out common behaviors of LidSessionManagers.
 */
public abstract class AbstractLidSessionManager
        extends
            MSmartFactory<String,LidSession,String>
        implements
            LidSessionManager
{
    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param delegateFactory the underlying factory for LidSessions
     * @param storage the storage to use
     * @param sessionDuration the duration of new or renewed sessions in milli-seconds
     */
    protected AbstractLidSessionManager(
            Factory<String,LidSession,String> delegateFactory,
            CachingMap<String,LidSession>     storage,
            long                              sessionDuration )
    {
        super( delegateFactory, storage );
        
        theSessionDuration = sessionDuration;
    }
    
    /**
     * Obtain the session duration for newly created or renewed sessions.
     * 
     * @return the session duration, in milliseconds
     */
    public long getSessionDuration()
    {
        return theSessionDuration;
    }

    /**
     * The session duration, in milliseconds.
     */
    protected long theSessionDuration;
    
    /**
     * The default session duration.
     */
    protected static final long DEFAULT_SESSION_DURATION = ResourceHelper.getInstance( AbstractLidSessionManager.class ).getResourceLongOrDefault(
            "DefaultSessionDuration",
            8 * 60 * 60 * 1000L );

    /**
     * The delegate factory. This is factored out, so it can access the session duration.
     * It needs to be a static class as the LidSessionManager is only instantiated after this
     * class is.
     */
    protected static class MyDelegateFactory
            extends
                AbstractFactory<String,LidSession,String>
    {
        /**
         * Constructor.
         */
        public MyDelegateFactory()
        {
            // nothing
        }

        /**
         * Factory method.
         * 
         * @param lid the identifier of the client for whom to create a session
         * @param clientIp the IP address of the client at the time of session creation
         * @return the new LidSession
         */
        public LidSession obtainFor(
                String lid,
                String clientIp )
        {
            String cookieValue = LidSession.createNewCookieValue();
            long timeCreated   = System.currentTimeMillis();
            long timeUpdated   = timeCreated;
            long timeRead      = timeCreated;
            long timeExpires   = timeCreated + theSessionManager.getSessionDuration();
            
            LidSession ret = LidSession.create( lid, cookieValue, timeCreated, timeUpdated, timeRead, timeExpires, clientIp );

            return ret;
        }

        /**
         * Let the LidSessionManager set itself.
         * 
         * @param mgr the LidSessionManager
         */
        public void setLidSessionManager(
                LidSessionManager mgr )
        {
            theSessionManager = mgr;
        }

        /**
         * The LidSessionManager that this instance belongs to.
         */
        protected LidSessionManager theSessionManager;
    }
}

