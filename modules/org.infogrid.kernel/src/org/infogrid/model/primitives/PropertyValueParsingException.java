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

package org.infogrid.model.primitives;

import org.infogrid.util.AbstractLocalizedException;
import org.infogrid.util.text.StringRepresentation;

/**
 * Thrown if parsing the String representation of a PropertyValue was unsuccessful.
 */
public class PropertyValueParsingException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param type the DataType to which the PropertyValue was supposed to conform
     * @param representation the representation of the PropertyValue in the String
     * @param s the String whose parsing was unsuccessful
     */
    public PropertyValueParsingException(
            DataType             type,
            StringRepresentation representation,
            String               s )
    {
        theDataType       = type;
        theRepresentation = representation;
        theString         = s;
    }
    
    /**
     * Constructor.
     *
     * @param type the DataType to which the PropertyValue was supposed to conform
     * @param representation the representation of the PropertyValue in the String
     * @param s the String whose parsing was unsuccessful
     * @param cause the underlying exception
     */
    public PropertyValueParsingException(
            DataType             type,
            StringRepresentation representation,
            String                s,
            Throwable             cause )
    {
        super( cause );

        theDataType       = type;
        theRepresentation = representation;
        theString         = s;
    }
    
    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theDataType, theRepresentation, theString };
    }

    /**
     * Obtain the DataType into one of those instances the String was supposed to be parsed.
     *
     * @return the DataType
     */
    public final DataType getDataType()
    {
        return theDataType;
    }

    /**
     * Obtain the StringRepresentation to use.
     *
     * @return the StringRepresentation
     */
    public final StringRepresentation getStringRepresentation()
    {
        return theRepresentation;
    }
    
    /**
     * Obtain the String that failed to parse.
     *
     * @return the String
     */
    public String getString()
    {
        return theString;
    }

    /**
     * The DataType into one of whose instances the String was supposed to be parsed.
     */
    protected DataType theDataType;
    
    /**
     * The StringRepresentation of the String.
     */
    protected StringRepresentation theRepresentation;
    
    /**
     * The String that failed to parse.
     */
    protected String theString;
}
