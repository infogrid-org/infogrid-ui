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

/**
 * A value object that represents a local Persona.
 */
public class LidLocalPersona
        implements
            Serializable
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Factory method.
     *
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param attributes attributes of the persona, e.g. first name
     * @return the created LidLocalPersona
     */
    public static LidLocalPersona create(
            String             identifier,
            Map<String,String> attributes )
    {
        if( attributes == null ) {
            attributes = new HashMap<String,String>();
        }
        LidLocalPersona ret = new LidLocalPersona( identifier, attributes );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param attributes attributes of the persona, e.g. first name
     */
    protected LidLocalPersona(
            String             identifier,
            Map<String,String> attributes )
    {
        theIdentifier = identifier;
        theAttributes = attributes;
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
     * Directly get the attributes.
     * 
     * @return the attributes
     */
    public Map<String,String> getAttributes()
    {
        return theAttributes;
    }
    /**
     * The unique identifier of the persona.
     */
    protected String theIdentifier;
    
    /**
     * Attributes of the persona.
     */
    protected Map<String,String> theAttributes;
}
