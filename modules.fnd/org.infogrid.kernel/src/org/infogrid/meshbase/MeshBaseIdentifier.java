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

package org.infogrid.meshbase;

import org.infogrid.util.Identifier;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.text.HasStringRepresentation;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;

/**
 * Identifies a MeshBase.
 */
public class MeshBaseIdentifier
        implements
            Identifier,
            CanBeDumped
{
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
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the String representation
     */
    public String toStringRepresentation(
            StringRepresentation        rep,
            StringRepresentationContext context,
            int                         maxLength )
    {
        String externalForm = toExternalForm();

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                DEFAULT_ENTRY,
                maxLength,
                externalForm );
        return ret;
    }

    /**
     * Obtain the start part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param additionalArguments additional arguments for URLs, if any
     * @param target the HTML target, if any
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkStart(
            String                      additionalArguments,
            String                      target,
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        String contextPath  = context != null ? (String) context.get( StringRepresentationContext.WEB_CONTEXT_KEY ) : null;
        String externalForm = toExternalForm();

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                DEFAULT_LINK_START_ENTRY,
                HasStringRepresentation.UNLIMITED_LENGTH,
                contextPath,
                externalForm,
                additionalArguments,
                target );
        return ret;
    }

    /**
     * Obtain the end part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkEnd(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        String contextPath  = context != null ? (String) context.get( StringRepresentationContext.WEB_CONTEXT_KEY ) : null;
        String externalForm = toExternalForm();

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                DEFAULT_LINK_END_ENTRY,
                HasStringRepresentation.UNLIMITED_LENGTH,
                contextPath,
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
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
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
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_ENTRY = "String";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_LINK_START_ENTRY = "LinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_LINK_END_ENTRY = "LinkEndString";
}
