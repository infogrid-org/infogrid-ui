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

import org.infogrid.util.AbstractLocalizedFactoryException;

/**
 * Thrown if a specified credential type is not known.
 */
public class UnknownLidCredentialTypeException
        extends
            AbstractLocalizedFactoryException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param sender the Factory that threw this exception
     * @param credentialType name of the credential type that was not known
     */
    public UnknownLidCredentialTypeException(
            LidCredentialTypeFactory sender,
            String                   credentialType )
    {
        super( sender );
        
        theCredentialType = credentialType;
    }
    
    /**
     * Obtain the name of the credential type that was not known.
     * 
     * @return the name of the credential type that was not known
     */
    public String getCredentialType()
    {
        return theCredentialType;
    }
    
    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theCredentialType };
    }

    /**
     * The credential type that was not known.
     */
    protected String theCredentialType;
}
