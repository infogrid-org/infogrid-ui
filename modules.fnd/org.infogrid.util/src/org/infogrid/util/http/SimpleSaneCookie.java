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

package org.infogrid.util.http;

import java.util.Date;

/**
 * A very simple SaneCookie implementation.
 */
public class SimpleSaneCookie
        implements
            SaneCookie
{
    /**
     * Factory method.
     * 
     * @param name the name of the cookie
     * @param value the value of the cookie
     * @param domain the domain of the cookie
     * @param path the path of the cookie
     * @param expires expiration date of the cookie
     * @return the created SimpleSaneCookie
     */
    public static SimpleSaneCookie create(
            String name,
            String value,
            String domain,
            String path,
            Date   expires )
    {
        return new SimpleSaneCookie( name, value, domain, path, expires );
    }
    
    /**
     * Constructor.
     * 
     * @param name the name of the cookie
     * @param value the value of the cookie
     * @param domain the domain of the cookie
     * @param path the path of the cookie
     * @param expires expiration date of the cookie
     */
    public SimpleSaneCookie(
            String name,
            String value,
            String domain,
            String path,
            Date   expires )
    {
        theName    = name;
        theValue   = value;
        theDomain  = domain;
        thePath    = path;
        theExpires = expires;
        theRemoved = false;
    }
    
    /**
     * Obtain the name of the Cookie.
     *
     * @return the name of the Cookie
     */
    public String getName()
    {
        return theName;
    }

    /**
     * Obtain the value of the Cookie.
     *
     * @return the value of the Cookie
     */
    public String getValue()
    {
        return theValue;
    }

    /**
     * Obtain the domain of the Cookie, if any.
     *
     * @return the domain of the Cookie
     */
    public String getDomain()
    {
        return theDomain;
    }

    /**
     * Obtain the path of the Cookie, if any.
     *
     * @return the path of the Cookie
     */
    public String getPath()
    {
        return thePath;
    }

    /**
     * Obtain the expiration time of the Cookie, if any.
     *
     * @return the name of the Cookie
     */
    public Date getExpires()
    {
        return theExpires;
    }

    /**
     * Set this cookie to "please remove this cookie".
     */
    public void setRemoved()
    {
        theRemoved = true;
    }
    
    /**
     * Determine whether this cookie is supposed to be removed.
     * 
     * @return true if this cookie is removed or expired
     */
    public boolean getIsRemovedOrExpired()
    {
        if( theRemoved ) {
            return true;
        }
        if( theExpires == null ) {
            return false;
        } else if( theExpires.after( new Date() )) {
            return false;
        } else {
            return true;
        }
    }
    /**
     * Returns the character at the specified index. From CharSequence. 
     * 
     * @param index the index 
     * @return the character
     */
    public char charAt(
            int index )
    {
        return theValue.charAt( index );
    }
          
    /**
     * Returns the length of this character sequence. From CharSequence.
     * 
     * @return length
     */
    public int length()
    {
        return theValue.length();
    }
    
    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     * 
     * @param start start index
     * @param end end index
     * @return substring
     */
    public CharSequence subSequence(
            int start,
            int end )
    {
        return theValue.subSequence( start, end );
    }
          
    /**
     * Return the value of the cookie. Note that this is not just for debugging.
     * 
     * @return the value of the cookie
     */
    @Override
    public String toString()
    {
        return theValue;
    }

    /**
     * Name of the cookie.
     */
    protected String theName;
    
    /**
     * Value of the cookie.
     */
    protected String theValue;
    
    /**
     * Domain of the cookie.
     */
    protected String theDomain;
    
    /**
     * Path of the cookie, if any.
     */
    protected String thePath;
    
    /**
     * Date when the cookie expires.
     */
    protected Date theExpires;
    
    /**
     * True if this Cookie is supposed to be, or already removed.
     */
    protected boolean theRemoved;
}

