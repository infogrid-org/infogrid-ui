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

package org.infogrid.util;

import org.infogrid.util.text.IdentifierStringifier;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;

/**
 * Trivially simple implementation of Identifier using a String.
 */
public class SimpleStringIdentifier
        implements
            Identifier
{
    /**
     * Factory method.
     *
     * @param s the underlying String
     * @return the created SimpleStringIdentifier
     */
    public static SimpleStringIdentifier create(
            String s )
    {
        return new SimpleStringIdentifier( s );
    }

    /**
     * Constructor, use factory method instead.
     *
     * @param s the underlying string
     */
    protected SimpleStringIdentifier(
            String s )
    {
        theString = s;
    }

    /**
     * Obtain an external form for this Identifier, similar to
     * <code>java.net.URL.toExternalForm()</code>.
     *
     * @return external form of this Identifier
     */
    public String toExternalForm()
    {
        return theString;
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation        rep,
            StringRepresentationContext context,
            int                         maxLength,
            boolean                     colloquial )
    {
        String ret = IdentifierStringifier.colloquialUrl( toExternalForm(), colloquial );
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
        return ""; // FIXME?
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
        return ""; // FIXME?
    }

    /**
     * Compare.
     *
     * @param other the Object to compare to
     * @return true if the Objects are equal
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( !( other instanceof Identifier )) {
            return false;
        }
        if( other == null ) {
            return false;
        }
        Identifier realOther = (Identifier) other;
        if( toExternalForm().equals( realOther.toExternalForm() )) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return toExternalForm().hashCode();
    }

    /**
     * Convert to String, for debugging.
     *
     * @return String format
     */
    @Override
    public String toString()
    {
        return toExternalForm();
    }

    /**
     * The underlying String.
     */
    protected String theString;
}
