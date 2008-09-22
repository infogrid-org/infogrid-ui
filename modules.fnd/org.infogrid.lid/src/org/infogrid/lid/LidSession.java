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

import java.util.Random;
import org.infogrid.util.AbstractFactoryCreatedObject;
import org.infogrid.util.ResourceHelper;

/**
 * <p>Captures the information in a LID session.</p>
 * <p>The parameters are as follows:</p>
 * <ul>
 *  <li>String: the LID identifier for the user that owns the session</li>
 *  <li>LidSession: the LidSession</li>
 *  <li>String: the user's IP address at the time the session was created</li>
 * </ul>
 */
public class LidSession
        extends
            AbstractFactoryCreatedObject<String,LidSession,String>
{
    /**
     * Factory method.
     *
     * @param lid the LID identifier of the user
     * @param creationClientIp the IP address of the client that created the session
     * @return the created LidSession
     */
    public static LidSession create(
            String lid,
            String creationClientIp )
    {
        long timeCreated = System.currentTimeMillis();
        long timeRead    = timeCreated;
        long timeExpires = timeCreated + DEFAULT_SESSION_DURATION;

        String     cookieValue = createNewCookieValue();
        LidSession ret         = new LidSession( lid, cookieValue, timeCreated, timeRead, timeExpires, creationClientIp );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param lid the LID identifier of the user
     * @param cookieValue the value identifying this session in a browser cookie
     * @param timeCreated the time the session was created, in System.currentTimeMillis() format
     * @param timeRead the time the session was last read, in System.currentTimeMillis() format
     * @param timeExpires the time the session was or will expire, in System.currentTimeMillis() format
     * @param creationClientIp the IP address of the client that created the session
     * @return the created LidSession
     */
    public static LidSession create(
            String lid,
            String cookieValue,
            long   timeCreated,
            long   timeRead,
            long   timeExpires,
            String creationClientIp )
    {
        LidSession ret = new LidSession( lid, cookieValue, timeCreated, timeRead, timeExpires, creationClientIp );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param lid the LID identifier of the user
     * @param cookieValue the value identifying this session in a browser cookie
     * @param timeCreated the time the session was created, in System.currentTimeMillis() format
     * @param timeRead the time the session was last read, in System.currentTimeMillis() format
     * @param timeExpires the time the session was or will expire, in System.currentTimeMillis() format
     * @param creationClientIp the IP address of the client that created the session
     */
    protected LidSession(
            String lid,
            String cookieValue,
            long   timeCreated,
            long   timeRead,
            long   timeExpires,
            String creationClientIp )
    {
        theLid              = lid;
        theCookieValue      = cookieValue;
        theTimeCreated      = timeCreated;
        theTimeRead         = timeRead;
        theTimeExpires      = timeExpires;
        theCreationClientIp = creationClientIp;
    }
    
    /**
     * Helper method to generate a new cookie value.
     * 
     * @return the cookie value
     */
    protected static String createNewCookieValue()
    {
        char [] buf  = new char[ COOKIE_LENGTH ];

        for( int i=0 ; i<COOKIE_LENGTH ; ++i ) {
            int  value = theGenerator.nextInt( ALLOWED_CHARS.length );
            char c     = ALLOWED_CHARS[ value ];
            
            buf[i] = c;
        }
        String cookieValue = new String( buf );
        return cookieValue;
    }

    /**
     * Obtain the key for that was used to create this object by the Factory.
     *
     * @return the key
     */
    public String getFactoryKey()
    {
        return theLid;
    }

    /**
     * Obtain the time the token was created.
     * 
     * @return the time the token was created, in System.currentTimeMillis() format
     */
    public long getTimeCreated()
    {
        return theTimeCreated;
    }

    /**
     * Obtain the time the token was last used.
     * 
     * @return the time the token was last used, in System.currentTimeMillis() format
     */
    public long getTimeRead()
    {
        return theTimeRead;
    }

    /**
     * Obtain the time the token expires.
     * 
     * @return the time the token expires, in System.currentTimeMillis() format
     */
    public long getTimeExpires()
    {
        return theTimeExpires;
    }

    /**
     * Obtain the cookie value.
     * 
     * @return the cookie value
     */
    public String getCookieValue()
    {
        return theCookieValue;
    }
    
    /**
     * Advance the cookie value to the next random number.
     */
    public void advanceCookieValue()
    {
        theCookieValue = createNewCookieValue();
        
        factoryCreatedObjectUpdated();
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
     * The LID identifier of the user
     */
    protected String theLid;
    
    /**
     * The value identifying this session in a browser cookie.
     */
    protected String theCookieValue;
    
    /**
     * The time the session was created, in System.currentTimeMillis() format.
     */
    protected long theTimeCreated;
    
    /**
     * The time the session was last accessed, in System.currentTimeMillis() format.
     */
    protected long theTimeRead;

    /**
     * The time the session has expired or will expires, in System.currentTimeMillis() format.
     */
    protected long theTimeExpires;
    
    /**
     * The IP address of the client that created the session.
     */
    protected String theCreationClientIp;
    
    /**
     * The Random generator we use.
     */
    protected static final Random theGenerator = new Random();

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( LidSession.class );
    
    /**
     * The default lifetime of a session, in milliseconds.
     */
    public static final long DEFAULT_SESSION_DURATION = theResourceHelper.getResourceLongOrDefault( 
            "DefaultSessionDuration",
            8L*60L*60L*1000L ); // 8 hours

    /**
     * The characters that are allowed in the token.
     */
    protected static final char [] ALLOWED_CHARS = theResourceHelper.getResourceStringOrDefault(
            "AllowedChars",
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_" ).toCharArray();

    /**
     * The length of the cookie.
     */
    protected static final int COOKIE_LENGTH = theResourceHelper.getResourceIntegerOrDefault(
            "CookieLength",
            64 );
}
