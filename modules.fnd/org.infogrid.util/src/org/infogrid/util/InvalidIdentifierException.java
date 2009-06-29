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
 * Thrown if a given Identifier was invalid for a given operation.
 */
public class InvalidIdentifierException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param identifier the invalid identifier
     */
    public InvalidIdentifierException(
            Identifier identifier )
    {
        theIdentifier = identifier;
    }

    /**
     * Obtain the invalid Identifier.
     *
     * @return the invalid Identifier
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
     * The invalid Identifier.
     */
    protected Identifier theIdentifier;
}