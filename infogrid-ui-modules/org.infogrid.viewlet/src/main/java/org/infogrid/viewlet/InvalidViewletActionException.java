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

package org.infogrid.viewlet;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Thrown by a Viewlet if it is supposed to perform an action which does not exist.
 * This is a rather abstract Exception, and subclasses may be more specific.
 */
public class InvalidViewletActionException
        extends
            AbstractLocalizedException
{
    /**
     * Constructor.
     *
     * @param v the Viewlet that threw the Exception
     */
    public InvalidViewletActionException(
            Viewlet v )
    {
        theViewlet = v;
    }

    /**
     * Obtain the Viewlet that threw the Exception.
     *
     * @return the Viewlet
     */
    public Viewlet getViewlet()
    {
        return theViewlet;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] {
            theViewlet,
            theViewlet.getName(),
            theViewlet.getUserVisibleName()
        };
    }

    /**
     * The Viewlet that threw the Exception.
     */
    protected Viewlet theViewlet;
}
