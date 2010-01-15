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

package org.infogrid.util;

/**
 * Thrown if the object that has an Identifier cannot be found.
 */
public class CannotFindHasIdentifierException
    extends
        AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param identifier the identifier that could not be resolved
     */
    public CannotFindHasIdentifierException(
            Identifier identifier )
    {
        theIdentifier = identifier;
    }

    /**
     * Constructor with a message.
     *
     * @param identifier the identifier that could not be resolved
     * @param msg the message
     */
    public CannotFindHasIdentifierException(
            Identifier identifier,
            String     msg )
    {
        super( msg );

        theIdentifier = identifier;
    }

    /**
     * Constructor with no message but a cause.
     *
     * @param identifier the identifier that could not be resolved
     * @param cause the Throwable that caused this Exception
     */
    public CannotFindHasIdentifierException(
            Identifier identifier,
            Throwable  cause )
    {
        super( cause );

        theIdentifier = identifier;
    }

    /**
     * Constructor with a message and a cause.
     *
     * @param identifier the identifier that could not be resolved
     * @param msg the message
     * @param cause the Exception that caused this Exception
     */
    public CannotFindHasIdentifierException(
            Identifier identifier,
            String     msg,
            Throwable  cause )
    {
        super( msg, cause );

        theIdentifier = identifier;
    }

    /**
     * Obtain the identifier that could not be resolved.
     *
     * @return the identifier
     */
    public Identifier getIdentifier()
    {
        return theIdentifier;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theIdentifier };
    }

    /**
     * The identifier that could not be resolved.
     */
    protected Identifier theIdentifier;
}
