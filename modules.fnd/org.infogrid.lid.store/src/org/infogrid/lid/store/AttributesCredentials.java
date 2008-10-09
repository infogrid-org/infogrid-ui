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

package org.infogrid.lid.store;

import java.util.HashMap;
import java.util.Map;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.util.logging.Log;

/**
 * Helper class to package attributes and credentials into the same instance.
 */
public class AttributesCredentials
{
    private static final Log log = Log.getLogInstance( AttributesCredentials.class ); // our own, private logger

    /**
     * Constructor.
     */
    public AttributesCredentials()
    {
        theAttributes  = new HashMap<String,String>();
        theCredentials = new HashMap<LidCredentialType,String>();
    }

    /**
     * Constructor.
     * 
     * @param attributes the attributes
     * @param credentials the credentials
     */
    public AttributesCredentials(
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
    {
        theAttributes  = attributes;
        theCredentials = credentials;
    }

    /**
     * Obtain the attributes of the persona.
     * 
     * @return the attributes
     */
    public Map<String,String> getAttributes()
    {
        return theAttributes;
    }
    
    /**
     * Obtain the credentials of the persona.
     * 
     * @return the credentials
     */
    public Map<LidCredentialType,String> getCredentials()
    {
        return theCredentials;
    }
    
    /**
     * Add an attribute.
     * 
     * @param name the name of the attribute
     * @param value the value of the attribute
     */
    protected void addAttribute(
            String name,
            String value )
    {
        String ret = theAttributes.put( name, value );
        if( ret != null ) {
            log.error( "Overwriting attribute " + name + " with new value " + value + ", was " + ret );
        }
    }

    /**
     * Add a credential.
     * 
     * @param credentialType the credential type
     * @param value the value of the attribute
     */
    protected void addCredential(
            LidCredentialType credentialType,
            String            value )
    {
        String ret = theCredentials.put( credentialType, value );
        if( ret != null ) {
            log.error( "Overwriting credentialType " + credentialType + " with new value " + value + ", was " + ret );
        }
    }

    /**
     * Attributes of the persona.
     */
    protected Map<String,String> theAttributes;

    /**
     * Credentials of the persona, keyed by credential type.
     */
    protected Map<LidCredentialType,String> theCredentials;
}
