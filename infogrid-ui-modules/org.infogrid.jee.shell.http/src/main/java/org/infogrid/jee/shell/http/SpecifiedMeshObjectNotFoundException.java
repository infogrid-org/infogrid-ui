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
 * Thrown if an operation requires an HTTP Shell variable to be non-null, but no value could be found.
 */
public class SpecifiedMeshObjectNotFoundException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param varName name of the variable without a value
     */
    public SpecifiedMeshObjectNotFoundException(
            String varName )
    {
        theVariableName = varName;
    }

    /**
     * Obtain the name of the null variable.
     *
     * @return name of the variable
     */
    public String getArgumentName()
    {
        return theVariableName;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theVariableName };
    }

    /**
     * Name of the variable without a value.
     */
    protected String theVariableName;
}
