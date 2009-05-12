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

package org.infogrid.jee.security;

import java.util.Random;
import org.infogrid.util.ResourceHelper;

/**
 * Captures all information in a form token.
 */
public class FormToken
{
    /**
     * Factory method. Use default expiration time.
     *
     * @return the created StoreFormToken
     */
    public static FormToken createNew()
    {
        return createNew( DEFAULT_EXPIRES );
    }

    /**
     * Factory method.
     *
     * @param relativeTimeExpires the time, relative to now, when the newly created token is supposed to expire
     * @return the created StoreFormToken
     */
    public static FormToken createNew(
            long relativeTimeExpires )
    {
        char [] buf  = new char[ TOKEN_LENGTH ];

        for( int i=0 ; i<TOKEN_LENGTH ; ++i ) {
            int  value = theGenerator.nextInt( ALLOWED_CHARS.length );
            char c     = ALLOWED_CHARS[ value ];
            
            buf[i]  = c;
        }
        String key = new String( buf );
        long   now = System.currentTimeMillis();

        FormToken ret = new FormToken( key, now, now + relativeTimeExpires );
        return ret;
    }

    /**
     * Factory method to recreate a previously-existing token.
     *
     * @param key the token's key
     * @param timeCreated the absolute time when the token was created
     * @param timeExpires the absolute time when the newly created token is supposed to expire
     * @return the recreated StoreFormToken
     */
    public static FormToken restore(
            String key,
            long   timeCreated,
            long   timeExpires )
    {
        FormToken ret = new FormToken( key, timeCreated, timeExpires );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param key the token's key
     * @param timeCreated the absolute time when the token was created
     * @param timeExpires the absolute time when the newly created token is supposed to expire
     */
    protected FormToken(
            String key,
            long   timeCreated,
            long   timeExpires )
    {
        theKey         = key;
        theTimeCreated = timeCreated;
        theTimeExpires = timeExpires;
    }
    
    /**
     * Obtain the key to the token. This will be given to the client.
     * 
     * @return the key
     */
    public String getKey()
    {
        return theKey;
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
     * Obtain the time the token expires.
     * 
     * @return the time the token expires, in System.currentTimeMillis() format
     */
    public long getTimeExpires()
    {
        return theTimeExpires;
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
     * The key of the token.
     */
    protected String theKey;
    
    /**
     * The time when the token was created, in System.currentTimeMillis() format.
     */
    protected long theTimeCreated;

    /**
     * The absolute time when the token expires, in System.currentTimeMillis() format.
     */
    protected long theTimeExpires;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( FormToken.class  );
    
    /**
     * The length of the token.
     */
    protected static final int TOKEN_LENGTH = theResourceHelper.getResourceIntegerOrDefault(
            "TokenLength",
            64 );
    
    /**
     * The characters that are allowed in the token.
     */
    protected static final char [] ALLOWED_CHARS = theResourceHelper.getResourceStringOrDefault(
            "AllowedChars",
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_" ).toCharArray();
    
    /**
     * The time, in milliseconds, from the time the token was created until the token expires.
     */
    public static final long DEFAULT_EXPIRES = theResourceHelper.getResourceLongOrDefault(
            "Expires",
            1000L * 60L * 60L ); // 1 hour
    
    /**
     * The Random generator we use.
     */
    protected static final Random theGenerator = new Random();
}
