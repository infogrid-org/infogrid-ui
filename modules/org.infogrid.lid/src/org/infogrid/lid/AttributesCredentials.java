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

import java.util.Map;
import org.infogrid.lid.credential.LidCredentialType;

/**
 * Collects identifier, attributes and credentials into the same instance. This is a
 * helper class to make generics work.
 */
public class AttributesCredentials
{
    /**
     * Constructor.
     * 
     * @param identifier the identifier to which the attributes and credentials belong
     * @param attributes the attributes
     * @param credentials the credentials
     */
    public AttributesCredentials(
            String                        identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
    {
        theIdentifier  = identifier;
        theAttributes  = attributes;
        theCredentials = credentials;
    }

    /**
     * Obtain the key for that was used to create this object by the Factory.
     *
     * @return the key
     */
    public String getFactoryKey()
    {
        return theIdentifier;
    }

    /**
     * Obtain the identifier.
     * 
     * @return the identifier
     */
    public String getIdentifier()
    {
        return theIdentifier;
    }
    
    /**
     * Obtain the attributes.
     * 
     * @return the attributes
     */
    public Map<String,String> getAttributes()
    {
        return theAttributes;
    }
    
    /**
     * Obtain the credentials.
     * 
     * @return the credentials
     */
    public Map<LidCredentialType,String> getCredentials()
    {
        return theCredentials;
    }

    /**
     * The identifier to which the attributes and credentials belong.
     */
    protected String theIdentifier;

    /**
     * The attributes.
     */
    protected Map<String,String> theAttributes;

    /**
     * The credentials.
     */
    protected Map<LidCredentialType,String> theCredentials;
}
