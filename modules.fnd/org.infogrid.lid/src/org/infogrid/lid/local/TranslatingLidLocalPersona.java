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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.lid.local;

import java.util.Map;
import java.util.Set;
import org.infogrid.util.AbstractHasIdentifier;
import org.infogrid.util.Identifier;

/**
 * Implementation of LidLocalPersona for this LidLocalPersonaManager.
 */
public class TranslatingLidLocalPersona
        extends
            AbstractHasIdentifier
        implements
            LidLocalPersona
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param delegate the underlying LidLocalPersona from/to which we translate
     */
    protected TranslatingLidLocalPersona(
            Identifier      identifier,
            LidLocalPersona delegate )
    {
        super( identifier );

        theDelegate = delegate;
    }

    /**
     * Obtain the delegate LidLocalPerson.
     *
     * @return the delegate
     */
    public LidLocalPersona getDelegate()
    {
        return theDelegate;
    }

    /**
     * Determine whether this LidPersona is hosted locally or remotely.
     *
     * @return true if the LidPersona is hosted locally
     */
    public boolean isHostedLocally()
    {
        return theDelegate.isHostedLocally();
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
        return theDelegate.getAttribute( key );
    }

    /**
     * Get the set of keys into the set of attributes.
     *
     * @return the keys into the set of attributes
     */
    public Set<String> getAttributeKeys()
    {
        return theDelegate.getAttributeKeys();
    }

    /**
     * Obtain the map of attributes. This breaks encapsulation, but works much better
     * for JSP pages.
     *
     * @return the map of attributes
     */
    public Map<String,String> getAttributes()
    {
        return theDelegate.getAttributes();
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
        theDelegate.setAttribute( key, value );
    }

    /**
     * The underlying LidLocalPersona from/to which we translate.
     */
    protected LidLocalPersona theDelegate;
}