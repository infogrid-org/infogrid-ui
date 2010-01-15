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

/**
 * <p>Interface to HTTP Cookies. This is an interface so we can implement a delegation
 * model to the Java servlet cookies. Subtypes distinguish between incoming
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
}
