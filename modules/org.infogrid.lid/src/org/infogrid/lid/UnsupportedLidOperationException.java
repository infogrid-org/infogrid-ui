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
 * Thrown if a LID operation was attempted that was recognized but is not supported.
 * For example, this exception is thrown if the client attempts to retrieve a public
 * key, but the local identity does not possess a public key.
 */
public class UnsupportedLidOperationException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param source the LidService that threw this exception
     * @param unsupportedInstruction the part of the request that was not supported
     */
    public UnsupportedLidOperationException(
            LidService  source,
            String      unsupportedInstruction )
    {
        super( "Unsupported LID operation: " + unsupportedInstruction );
        
        theUnsupportedInstruction = unsupportedInstruction;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object [] { theUnsupportedInstruction };
    }
    
    /**
     * The part of the request that was unsupported.
     */
    protected String theUnsupportedInstruction;
}
