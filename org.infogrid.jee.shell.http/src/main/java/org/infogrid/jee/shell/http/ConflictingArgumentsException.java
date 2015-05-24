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

import org.infogrid.util.http.SaneRequest;

/**
 * Thrown if an operation is invoked with conflicting arguments.
 */
public class ConflictingArgumentsException
        extends
            InconsistentArgumentsException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param arg1Name name of the first conflicting argument
     * @param arg2Name name of the second conflicting argument
     * @param request the incoming request
     */
    public ConflictingArgumentsException(
            String      arg1Name,
            String      arg2Name,
            SaneRequest request )
    {
        super( request );

        theArgument1Name = arg1Name;
        theArgument2Name = arg2Name;
    }
    
    /**
     * Obtain the name of the first argument that was conflicting.
     *
     * @return name of the argument
     */
    public String getArgument1Name()
    {
        return theArgument1Name;
    }

    /**
     * Obtain the name of the second argument that was conflicting.
     *
     * @return name of the argument
     */
    public String getArgument2Name()
    {
        return theArgument2Name;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    @Override
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theArgument1Name, theArgument2Name, theRequest, theRequest.getAbsoluteFullUri() };
    }

    /**
     * Name of the first argument that was conflicting.
     */
    protected String theArgument1Name;

    /**
     * Name of the second argument that was conflicting.
     */
    protected String theArgument2Name;
}
