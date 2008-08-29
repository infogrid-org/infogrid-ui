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

package org.infogrid.meshbase;

import org.infogrid.util.Identifier;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.StringHelper;
import org.infogrid.util.text.StringRepresentation;

import java.net.URISyntaxException;

/**
 * Identifies a MeshBase.
 */
public class MeshBaseIdentifier
        implements
            Identifier
{
    /**
     * Factory method.
     * 
     * @param canonicalForm the canonical representation of this identifier
     * @return the created MeshBaseIdentifier
     */
    public static MeshBaseIdentifier create(
            String canonicalForm )
        throws
            URISyntaxException
    {
        if( canonicalForm == null ) {
            throw new IllegalArgumentException( "MeshBaseIdentifier's canonical form cannot be null" );
        }
        return new MeshBaseIdentifier( canonicalForm );
    }

    /**
     * Constructor.
     * 
     * @param canonicalForm the canonical representation of this identifier
     */
    protected MeshBaseIdentifier(
            String canonicalForm )
    {
        theCanonicalForm = canonicalForm;
    }

    /**
     * Obtain the canonical form of this identifier.
     *
     * @return the canonical form
     */
    public String getCanonicalForm()
    {
        return theCanonicalForm;
    }

    /**
     * For consistency with the Java APIs, this method is provided.
     *
     * @return the external form
     */
    public String toExternalForm()
    {
        return getCanonicalForm();
    }

    /**
     * Convert this PropertyValue to its String representation, using the representation scheme.
     *
     * @param representation the representation scheme
     * @return the String representation
     */
    public String toStringRepresentation(
            StringRepresentation representation )
    {
        String externalForm = toExternalForm();

        String ret = representation.formatEntry(
                ResourceHelper.getInstance( getClass() ), // dispatch to the right subtype
                DEFAULT_ENTRY,
                externalForm );
        return ret;
    }

    /**
     * Determine equality.
     *
     * @param other the Object to compare against
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( !( other instanceof MeshBaseIdentifier )) {
            return false;
        }
        MeshBaseIdentifier realOther = (MeshBaseIdentifier) other;
        
        String here  = getCanonicalForm();
        String there = realOther.getCanonicalForm();
        
        boolean ret = here.equals( there );
        return ret;
    }

    /**
     * Calculate hash value.
     *
     * @return the hash value
     */
    @Override
    public int hashCode()
    {
        String canonical = getCanonicalForm();
        return canonical.hashCode();
    }
    
    /**
     * Obtain as String representation, for debugging only.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "canonical"
                },
                new Object[] {
                    theCanonicalForm
                } );
    }

    /**
     * The canonical form.
     */
    protected String theCanonicalForm;

    /**
     * The default entry in the resouce files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_ENTRY = "String";
}
