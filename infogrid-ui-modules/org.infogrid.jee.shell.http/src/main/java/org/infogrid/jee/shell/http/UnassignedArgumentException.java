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

import org.infogrid.util.AbstractLocalizedException;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * Thrown if an operation is invoked with an argument that does not have a value.
 */
public class UnassignedArgumentException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor. Emit the default error message
     *
     * @param argName name of the argument without a value
     */
    public UnassignedArgumentException(
            String argName )
    {
        this( argName, null );
    }

    /**
     * Constructor. Emit a custom error message
     *
     * @param argName name of the argument without a value
     * @param msg the custom error message
     */
    public UnassignedArgumentException(
            String argName,
            String msg )
    {
        theArgumentName = argName;
        theMessage      = msg;
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
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theArgumentName };
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        if( theMessage != null ) {
            return theMessage;
        } else {
            return super.toStringRepresentation( rep, pars );
        }
    }

    /**
     * Name of the missing argument.
     */
    protected String theArgumentName;
    
    /**
     * Custom error message.
     */
    protected String theMessage;
}
