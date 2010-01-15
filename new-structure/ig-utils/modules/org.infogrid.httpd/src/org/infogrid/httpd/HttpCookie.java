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

package org.infogrid.httpd;

import org.infogrid.util.ArrayHelper;

import java.util.Date;

/**
 * Represents an HTTP cookie, regardless whether the cookie is on the first or second leg
 * of an HTTP interaction.
 */
public class HttpCookie
{
    /**
     * Constructor. This is typically used with Cookies sent by a server.
     *
     * @param name the name of the cookie
     * @param value the value of the cookie
     * @param domain the domain of the cookie
     * @param path the path of the cookie
     * @param expires the time of expiration
     */
    public HttpCookie(
            String name,
            String value,
            String domain,
            String path,
            Date   expires ) // FIXME: data type?
    {
        theName    = name;
        theValue   = value;
        theDomain  = domain;
        thePath    = path;
        theExpires = expires;
    }

    /**
     * Constructor. This is typically used with Cookies received by a server.
     *
     * @param name the name of the cookie
     * @param value the value of the cookie
     */
    public HttpCookie(
            String name,
            String value )
    {
        theName    = name;
        theValue   = value;
        theDomain  = null;
        thePath    = null;
        theExpires = null;
    }

    /**
     * Obtain the name of the cookie.
     *
     * @return the name of the cookie
     */
    public String getName()
    {
        return theName;
    }

    /**
     * Obtain the value of the cookie.
     *
     * @return the value of the cookie
     */
    public String getValue()
    {
        return theValue;
    }

    /**
     * Obtain the domain of the cookie.
     *
     * @return the domain of the cookie
     */
    public String getDomain()
    {
        return theDomain;
    }

    /**
     * Obtain the path of the cookie.
     *
     * @return the path of the cookie
     */
    public String getPath()
    {
        return thePath;
    }

    /**
     * Obtain the expiration time of the cookie.
     *
     * @return the expiration time of the cookie
     */
    public Date getExpires()
    {
        return theExpires;
    }

    /**
     * Equals method.
     *
     * @param other the Object to compare against
     * @return true if other is equal to this Object
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( other instanceof HttpCookie ) {
            HttpCookie realOther = (HttpCookie) other;

            boolean ret =
                       ArrayHelper.equals( theName,    realOther.theName )
                    && ArrayHelper.equals( theValue,   realOther.theValue )
                    && ArrayHelper.equals( theDomain,  realOther.theDomain )
                    && ArrayHelper.equals( thePath,    realOther.thePath )
                    && ArrayHelper.equals( theExpires, realOther.theExpires );
            
            return ret;
        }
        return false;
    }

    /**
     * Hash code.
     * 
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        // NetBeans-generated
        int hash = 7;
        hash = 83 * hash + ( theName != null   ? theName.hashCode()   : 0 );
        hash = 83 * hash + ( theDomain != null ? theDomain.hashCode() : 0 );
        hash = 83 * hash + ( thePath != null   ? thePath.hashCode()   : 0 );
        return hash;
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
     * Path of the cookie.
     */
    protected String thePath;

    /**
     * Time of expiration.
     */
    protected Date theExpires;
}
