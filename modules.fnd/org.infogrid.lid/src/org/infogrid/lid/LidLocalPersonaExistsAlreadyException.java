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
 * Thrown if the LidLocalPersona existed already during an attempt to create it.
 */
public class LidLocalPersonaExistsAlreadyException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param persona the LidLocalPersona that existed already
     */
    public LidLocalPersonaExistsAlreadyException(
            LidLocalPersona persona )
    {
        thePersona = persona;
    }
    
    /**
     * Constructor.
     * 
     * @param persona the LidLocalPersona that existed already
     * @param cause the underlying cause
     */
    public LidLocalPersonaExistsAlreadyException(
            LidLocalPersona persona,
            Throwable       cause )
    {
        super( cause );

        thePersona = persona;
    }
    
    /**
     * Obtain the LidLocalPersona that existed already.
     * 
     * @return the LidLocalPersona
     */
    public LidLocalPersona getPersona()
    {
        return thePersona;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object[] { thePersona.getIdentifier() }; // FIXME? Perhaps more?
    }

    /**
     * The LidLocalPersona that existed already.
     */
    protected LidLocalPersona thePersona;
}
