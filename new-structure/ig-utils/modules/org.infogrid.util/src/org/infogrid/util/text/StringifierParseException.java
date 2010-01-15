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

package org.infogrid.util.text;

/**
 * Thrown if parsing a String by a Stringifier failed.
 */
public class StringifierParseException
        extends
            StringifierException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param source the Stringifier that raised this exception
     * @param s the String that could not be parsed
     */
    public StringifierParseException(
            Stringifier source,
            String      s )
    {
        super( source );
        
        theString = s;
    }

    /**
     * Constructor.
     *
     * @param source the Stringifier that raised this exception
     * @param s the String that could not be parsed
     * @param cause the underlying cause of the Exception
     */
    public StringifierParseException(
            Stringifier source,
            String      s,
            Throwable   cause )
    {
        super( source, cause );
        
        theString = s;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theSource, theString };
    }
    
    /**
     * The String that could not be parsed.
     */
    protected String theString;
}
