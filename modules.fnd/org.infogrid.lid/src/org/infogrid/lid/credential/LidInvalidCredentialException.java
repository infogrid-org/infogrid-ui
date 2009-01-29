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

package org.infogrid.lid.credential;

import org.infogrid.util.AbstractLocalizedException;
import org.infogrid.util.Identifier;
import org.infogrid.util.StringHelper;

/**
 * Thrown if a credential was provided that was invalid.
 */
public class LidInvalidCredentialException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param identifier the identifier for which an invalid credential was provided
     * @param type the type of credential that was invalid
     */
    public LidInvalidCredentialException(
            Identifier        identifier,
            LidCredentialType type )
    {
        theIdentifier = identifier;
    }
    
    /**
     * Constructor.
     * 
     * @param identifier the identifier for which an invalid credential was provided
     * @param type the type of credential that was invalid
     * @param cause the underlying cause, if any
     */
    public LidInvalidCredentialException(
            Identifier        identifier,
            LidCredentialType type,
            Throwable         cause )
    {
        super( cause );

        theIdentifier = identifier;
        theType       = type;
    }
    
    /**
     * Obtain the identifier for which an invalid credential was provided.
     * 
     * @return the identifier
     */
    public Identifier getIdentifier()
    {
        return theIdentifier;
    }
    
    /**
     * Obtain the type of credential that was invalid.
     * 
     * @return the credential type
     */
    public LidCredentialType getCredentialType()
    {
        return theType;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theIdentifier, theType };
    }

    /**
     * Convert to String representation, for debugging.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                        "theIdentifier",
                        "theType"
                },
                new Object[] {
                        theIdentifier,
                        theType,
                } );
    }

    /**
     * The identifier for which an invalid credential was provided.
     */
    protected Identifier theIdentifier;
    
    /**
     * The type of credential that was invalid.
     */
    protected LidCredentialType theType;
}

