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

package org.infogrid.lid;

import org.infogrid.util.AbstractLocalizedException;

/**
 * An invalidIdentifier was entered that was invalid.
 */
public class LidInvalidIdentifierException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param invalidIdentifier the invalid invalidIdentifier
     */
    public LidInvalidIdentifierException(
            String invalidIdentifier  )
    {
        theInvalidIdentifier = invalidIdentifier;
    }
    
    /**
     * Constructor.
     * 
     * @param invalidIdentifier the invalid invalidIdentifier
     * @param cause the underlying cause, if any
     */
    public LidInvalidIdentifierException(
            String    invalidIdentifier,
            Throwable cause )
    {
        super( cause );

        theInvalidIdentifier = invalidIdentifier;
    }
    
    /**
     * Obtain the invalid invalidIdentifier.
     * 
     * @return the invalidIdentifier
     */
    public String getIdentifier()
    {
        return theInvalidIdentifier;
    }
    
    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theInvalidIdentifier };
    }

    /**
     * The invalid invalidIdentifier.
     */
    protected String theInvalidIdentifier;
}
