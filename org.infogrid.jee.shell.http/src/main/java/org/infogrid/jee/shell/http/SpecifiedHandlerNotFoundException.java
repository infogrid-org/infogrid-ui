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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.shell.http;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Thrown if a specified handler could not be found.
 */
public class SpecifiedHandlerNotFoundException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param handlerName name of the handler that could not be found
     */
    public SpecifiedHandlerNotFoundException(
            String handlerName )
    {
        theHandlerName = handlerName;
    }

    /**
     * Obtain the name of the handler.
     *
     * @return name of the handler
     */
    public String getHandlerName()
    {
        return theHandlerName;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theHandlerName };
    }

    /**
     * Name of the handler that could not be found.
     */
    protected String theHandlerName;
}
