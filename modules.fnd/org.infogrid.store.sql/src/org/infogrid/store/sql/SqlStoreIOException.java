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

package org.infogrid.store.sql;

import java.sql.SQLException;
import org.infogrid.util.DelegatingIOException;
import org.infogrid.util.LocalizedException;
import org.infogrid.util.text.HasStringRepresentation;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;

/**
 * <p>An <code>IOException</code> that delegates to a <code>SQLException</code>.</p>
 */
public class SqlStoreIOException
        extends
            DelegatingIOException
        implements
            HasStringRepresentation
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param cause the cause
     */
    public SqlStoreIOException(
            SQLException cause )
    {
        super( "SQL Exception", cause );
    }
    
    /**
     * Obtain the underlying cause which we know to be a SQLException.
     * 
     * @return the cause
     */
    @Override
    public SQLException getCause()
    {
        return (SQLException) super.getCause();
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     * This is only a default implementation; subclasses will want to override.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        Throwable cause     = getCause();
        Throwable rootCause = cause;

        if( rootCause != null ) {
            while( true ) {
                Throwable t = rootCause.getCause();
                if( t == null ) {
                    break;
                }
                rootCause = t;
            }
        }
        
        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                LocalizedException.STRING_REPRESENTATION_KEY,
                getMessage(),
                getLocalizedMessage(),
                getStackTrace(),
                cause,
                cause != null ? cause.getMessage() : null,
                cause != null ? cause.getLocalizedMessage() : null,
                cause != null ? cause.getStackTrace() : null,
                rootCause,
                rootCause != null ? rootCause.getMessage() : null,
                rootCause != null ? rootCause.getLocalizedMessage() : null,
                rootCause != null ? rootCause.getStackTrace() : null );

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
        return "";
    }

    /**
     * Obtain the end part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public final String toStringRepresentationLinkEnd(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        return "";
    }
}
