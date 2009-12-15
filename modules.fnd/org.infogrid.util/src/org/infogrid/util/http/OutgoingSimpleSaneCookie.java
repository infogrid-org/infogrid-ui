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

package org.infogrid.util.http;

import java.util.Date;

/**
 * A simple, outgoing cookie.
 */
public class OutgoingSimpleSaneCookie
        extends
            AbstractSimpleSaneCookie
        implements
            OutgoingSaneCookie
{
    /**
     * Factory method.
     *
     * @param name the name of the cookie
     * @param value the value of the cookie
     * @param domain the domain of the cookie
     * @param path the path of the cookie
     * @param expires expiration date of the cookie
     * @return the created OutgoingSimpleSaneCookie
     */
    public static OutgoingSimpleSaneCookie create(
            String name,
            String value,
            String domain,
            String path,
            Date   expires )
    {
        return new OutgoingSimpleSaneCookie( name, value, domain, path, expires );
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
    protected OutgoingSimpleSaneCookie(
            String name,
            String value,
            String domain,
            String path,
            Date   expires )
    {
        super( name, value );

        theDomain  = domain;
        thePath    = path;
        theExpires = expires;
        theRemoved = false;

        if( "localhost".equalsIgnoreCase( theDomain )) {
            // weird special case rules for cookie settings, otherwise browser will ignore them
            theDomain = null;
        }
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
     * Set whether this Cookie should be sent only over a secure protocol.
     * 
     * @param newValue the new value
     */
    public void setSecure(
            boolean newValue )
    {
        theSecure = newValue;
    }

    /**
     * Determine whether this Cookie should be sent only over a secure protocol.
     *
     * @return true if the Cookie should only be sent over a secure protocol
     */
    public boolean getSecure()
    {
        return theSecure;
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
     * Convert into a String according to the HTTP spec.
     *
     * @return the String
     */
    public String getAsHttpValue()
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "Set-Cookie: " );
        buf.append( HTTP.encodeCookieName( theName ));
        buf.append( '=' );
        buf.append( HTTP.encodeToQuotedString( theValue ));
        if( thePath != null ) {
            buf.append(  "; path=" );
            buf.append( thePath );
        }
        if( theDomain != null && !"localhost".equalsIgnoreCase( theDomain )) {
            // can't mention "localhost", browser won't take it
            buf.append( "; domain=" );
            buf.append( theDomain );
        }
        if( theExpires != null ) {
            buf.append( "; max-age=" );
            long delta = theExpires.getTime() - System.currentTimeMillis();
            if( delta > 0 ) {
                buf.append( String.valueOf( delta / 1000L ));
            } else {
                buf.append( "0" );
            }
        }
        if( theSecure ) {
            buf.append( "; secure" );
        }


        return buf.toString();
    }

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

    /**
     * True if this Cookie shall only be sent over a secure protocol.
     */
    protected boolean theSecure;
}
