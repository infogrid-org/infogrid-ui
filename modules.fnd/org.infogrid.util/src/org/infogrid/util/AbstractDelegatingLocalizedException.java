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

import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationParameters;

/**
 * An AbstractLocalizedException used only for those Exceptions that primarily delegate to
 * other causes.
 */
public abstract class AbstractDelegatingLocalizedException
    extends
        AbstractLocalizedException
{
    /**
     * Constructor with no message but a cause.
     *
     * @param cause the Throwable that caused this Exception
     */
    public AbstractDelegatingLocalizedException(
            Throwable cause )
    {
        super( cause );
    }

    /**
     * Constructor with a message and a cause.
     *
     * @param msg the message
     * @param cause the Exception that caused this Exception
     */
    public AbstractDelegatingLocalizedException(
            String    msg,
            Throwable cause )
    {
        super( msg, cause );
    }

    /**
     * Obtain localized message, per JDK 1.5.
     *
     * @return localized message
     */
    @Override
    public String getLocalizedMessage()
    {
        Throwable cause = getCause();
        if( cause instanceof LocalizedException ) {
            return ((LocalizedException)cause).getLocalizedMessage();
        } else {
            return super.getLocalizedMessage();
        }
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] { getCause().getLocalizedMessage() };
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     */
    @Override
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationContext    context,
            StringRepresentationParameters pars )
    {
        Throwable cause = getCause();
        if( cause instanceof LocalizedException ) {
            return ((LocalizedException)cause).toStringRepresentation( rep, context, pars );
        } else {
            return super.toStringRepresentation( rep, context, pars );
        }
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
    @Override
    public String toStringRepresentationLinkStart(
            String                      additionalArguments,
            String                      target,
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        Throwable cause = getCause();
        if( cause instanceof LocalizedException ) {
            return ((LocalizedException)cause).toStringRepresentationLinkStart( additionalArguments, target, rep, context );
        } else {
            return super.toStringRepresentationLinkStart( additionalArguments, target, rep, context );
        }
    }

    /**
     * Obtain the end part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    @Override
    public String toStringRepresentationLinkEnd(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        Throwable cause = getCause();
        if( cause instanceof LocalizedException ) {
            return ((LocalizedException)cause).toStringRepresentationLinkEnd( rep, context );
        } else {
            return super.toStringRepresentationLinkEnd( rep, context );
        }
    }
}