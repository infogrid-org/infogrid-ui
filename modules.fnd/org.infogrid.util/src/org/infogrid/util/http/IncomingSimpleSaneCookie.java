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
 * A simple, incoming cookie.
 */
public class IncomingSimpleSaneCookie
        extends
            AbstractSimpleSaneCookie
        implements
            IncomingSaneCookie
{
    /**
     * Factory method.
     *
     * @param name the name of the cookie
     * @param value the value of the cookie
     * @return the created IncomingSimpleSaneCookie
     */
    public static IncomingSimpleSaneCookie create(
            String name,
            String value )
    {
        return new IncomingSimpleSaneCookie( name, value );
    }

    /**
     * Constructor.
     *
     * @param name the name of the cookie
     * @param value the value of the cookie
     */
    protected IncomingSimpleSaneCookie(
            String name,
            String value )
    {
        super( name, value );
    }
}
