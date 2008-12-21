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

package org.infogrid.jee.shell.http;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Thrown if an operation is invoked that is missing an essential argument.
 */
public class EssentialArgumentMissingException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization
    
    /**
     * Constructor.
     *
     * @param verb the verb representing the operation that was missing an essential argument
     * @param argName name of the argument that was missing
     */
    public EssentialArgumentMissingException(
            HttpShellVerb verb,
            String        argName )
    {
        theVerb         = verb;
        theArgumentName = argName;
    }
    
    /**
     * Obtain the verb representing the operation that was missing an essential argument.
     *
     * @return the verb
     */
    public HttpShellVerb getHttpShellVerb()
    {
        return theVerb;
    }

    /**
     * Obtain the name of the argument that was missing.
     *
     * @return name of the argument
     */
    public String getMissingArgumentName()
    {
        return theArgumentName;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theVerb, theArgumentName };
    }

    /**
     * The operation that was missing an argument.
     */
    protected HttpShellVerb theVerb;

    /**
     * Name of the argument that was missing.
     */
    protected String theArgumentName;
}
