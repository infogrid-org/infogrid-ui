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
 * Common functionality for IncomingSimpleSaneCookie and OutgoingSimpleSaneCookie.
 */
public abstract class AbstractSimpleSaneCookie
        extends
            AbstractSaneCookie
{
    /**
     * Constructor.
     * 
     * @param name the name of the cookie
     * @param value the value of the cookie
     */
    protected AbstractSimpleSaneCookie(
            String name,
            String value )
    {
        theName    = name;
        theValue   = value;
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
     * Name of the cookie.
     */
    protected String theName;
    
    /**
     * Value of the cookie.
     */
    protected String theValue;
}

