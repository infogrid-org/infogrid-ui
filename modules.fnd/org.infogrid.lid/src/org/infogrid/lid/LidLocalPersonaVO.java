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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.util.StringHelper;

/**
 * A value object that represents a LidLocalPersona.
 */
public class LidLocalPersonaVO
        implements
            LidLocalPersona,
            Serializable
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Factory method.
     *
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param attributes attributes of the persona, e.g. first name
     * @param credentials credentials of the persona, e.g. password
     * @return the created LidLocalPersonaVO
     */
    public static LidLocalPersonaVO create(
            String                        identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
    {
        if( attributes == null ) {
            attributes = new HashMap<String,String>();
        }
        LidLocalPersonaVO ret = new LidLocalPersonaVO( identifier, attributes, credentials );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param persona the LidPersona from where to copy the attributes
     * @return the created LidLocalPersonaVO
     */
    public static LidLocalPersonaVO create(
            String          identifier,
            LidLocalPersona persona )
    {
        HashMap<String,String>            attributes = new HashMap<String,String>();
        HashMap<LidCredentialType,String> credentials = new HashMap<LidCredentialType,String>();

        for( String key : persona.getAttributeKeys() ) {
            String value = persona.getAttribute( key );
            
            attributes.put( key, value );
        }
        for( LidCredentialType key : persona.getCredentialTypes() ) {
            String value = persona.getCredentialFor( key );
            
            credentials.put(  key, value );
        }
        
        LidLocalPersonaVO ret = new LidLocalPersonaVO( identifier, attributes, credentials );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param attributes attributes of the persona, e.g. first name
     * @param credentials credentials of the persona, e.g. password
     */
    protected LidLocalPersonaVO(
            String                        identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
    {
        theIdentifier  = identifier;
        theAttributes  = attributes;
        theCredentials = credentials;
    }
    
    /**
     * Obtain the persona's unique identifier.
     * 
     * @return the unique identifier
     */
    public String getIdentifier()
    {
        return theIdentifier;
    }

    /**
     * Determine whether this LidPersona is hosted locally or remotely.
     * 
     * @return true if the LidPersona is hosted locally
     */
    public boolean isHostedLocally()
    {
        return true;
    }

    /**
     * Obtain an attribute of the persona.
     * 
     * @param key the name of the attribute
     * @return the value of the attribute, or null
     */
    public String getAttribute(
            String key )
    {
        String ret = theAttributes.get( key );
        return ret;
    }
    
    /**
     * Set an attribute of the persona.
     * 
     * @param key the name of the attribute
     * @param value the value of the attribute
     */
    public void setAttribute(
            String key,
            String value )
    {
        theAttributes.put(  key, value );
    }

    /**
     * Get the set of keys into the set of attributes.
     * 
     * @return the keys into the set of attributes
     */
    public Set<String> getAttributeKeys()
    {
        return theAttributes.keySet();
    }
    
    /**
     * Obtain the credential for a given credential type.
     * 
     * @param type the credential type
     * @return the credential, if any
     */
    public String getCredentialFor(
            LidCredentialType type )
    {
        String ret = theCredentials.get( type );
        return ret;
    }

    /**
     * Set the credential for a given credential type.
     * 
     * @param type the credential type
     * @param credential the new value for the credential
     */
    public void setCredentialFor(
            LidCredentialType type,
            String            credential )
    {
        theCredentials.put( type, credential );
    }

    /**
     * Obtain the credential types available.
     * 
     * @return the credential types
     */
    public Set<LidCredentialType> getCredentialTypes()
    {
        return theCredentials.keySet();
    }

    /**
     * Translate to String form, for debugging.
     * 
     * @return String form
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "identifier",
                    "attributes"
                },
                new Object[] {
                    theIdentifier,
                    theAttributes
                });
    }

    /**
     * The unique identifier of the persona.
     */
    protected String theIdentifier;
    
    /**
     * Attributes of the persona.
     */
    protected Map<String,String> theAttributes;

    /**
     * Credentials of the persona, keyed by credential type.
     */
    protected Map<LidCredentialType,String> theCredentials;    
}
