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
 * <p>Interface to HTTP Cookies. This is an interface so we can implement a delegation
 * model to the Java servlet cookies. This interface can be used both for incoming
 * and outgoing cookies.</p>
 * <p>Making this a subtype of CharSequence allows us to also treat it as a String,
 * which is its value. This simplifies the API.</p>
 */
public interface SaneCookie
        extends
            CharSequence
{
    /**
     * Obtain the name of the Cookie.
     *
     * @return the name of the Cookie
     */
    public String getName();

    /**
     * Obtain the value of the Cookie.
     *
     * @return the value of the Cookie
     */
    public String getValue();

    /**
     * Obtain the domain of the Cookie, if any.
     *
     * @return the domain of the Cookie
     */
    public String getDomain();

    /**
     * Obtain the path of the Cookie, if any.
     *
     * @return the path of the Cookie
     */
    public String getPath();

    /**
     * Obtain the expiration time of the Cookie, if any.
     *
     * @return the name of the Cookie
     */
    public Date getExpires();

    /**
     * Set this cookie to "please remove this cookie".
     */
    public void setRemoved();
    
    /**
     * Determine whether this cookie is supposed to be removed.
     * 
     * @return true if this cookie is removed or expired
     */
    public boolean getIsRemovedOrExpired();
}
