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

package org.infogrid.util.text;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Thrown if a String could not be parsed by a StringRepresentation.
 */
public class StringRepresentationParseException
    extends
        AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param s the String that could not be parsed
     * @param formatString the format String that defines the syntax of the String to be parsed
     * @param cause the cause of this Exception
     */
    public StringRepresentationParseException(
            String    s,
            String    formatString,
            Throwable cause )
    {
        super( null, cause );

        theString = s;
        theFormatString = formatString;
    }

    /**
     * Obtain the String that could not be parsed.
     *
     * @return the String
     */
    public String getString()
    {
        return theString;
    }

    /**
     * Obtain the format String.
     *
     * @return the format String
     */
    public String getFormatString()
    {
        return theFormatString;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theString, theFormatString };
    }

    /**
     * The String that could not be parsed.
     */
    protected String theString;

    /**
     * The format String for the String.
     */
    protected String theFormatString;

}
