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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.shell.http;

import org.infogrid.util.http.SaneRequest;

/**
 * Thrown if an operation is invoked that requires a particular argument but it was not given.
 */
public class MissingArgumentException
        extends
            InconsistentArgumentsException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param argName name of the missing argument
     * @param request the incoming request
     */
    public MissingArgumentException(
            String      argName,
            SaneRequest request )
    {
        super( request );

        theArgumentName = argName;
    }

    /**
     * Obtain the name of the missing argument.
     *
     * @return name of the argument
     */
    public String getArgumentName()
    {
        return theArgumentName;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    @Override
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theArgumentName, theRequest, theRequest.getAbsoluteFullUri() };
    }

    /**
     * Name of the argument that was missing.
     */
    protected String theArgumentName;
}
