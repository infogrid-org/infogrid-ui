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

import org.infogrid.util.text.HasStringRepresentation;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationDirectorySingleton;

/**
 * This is a supertype for Exceptions that knows how to internationalize themselves.
 * Given that Exceptions carry all their data, it is a lot easier to to
 * ask the Exception how to internationalize itself, than to write outside
 * code to do so.
 */
public abstract class AbstractLocalizedException
        extends
            Exception
        implements
            LocalizedException
{
    /**
     * Constructor.
     */
    public AbstractLocalizedException()
    {
    }

    /**
     * Constructor with a message.
     *
     * @param msg the message
     */
    public AbstractLocalizedException(
            String msg )
    {
        super( msg );
    }

    /**
     * Constructor with no message but a cause.
     *
     * @param cause the Throwable that caused this Exception
     */
    public AbstractLocalizedException(
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
    public AbstractLocalizedException(
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
        return toStringRepresentation(
                StringRepresentationDirectorySingleton.getSingleton().get( StringRepresentationDirectory.TEXT_PLAIN_NAME ),
                null,
                HasStringRepresentation.UNLIMITED_LENGTH );
    }
    
    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public abstract Object [] getLocalizationParameters();

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     * This is only a default implementation; subclasses will want to override.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation        rep,
            StringRepresentationContext context,
            int                         maxLength )
    {
        return constructStringRepresentation(
                this,
                rep,
                context,
                findResourceHelperForLocalizedMessage(),
                getLocalizationParameters(),
                findStringRepresentationParameter(),
                maxLength );
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
        return constructStringRepresentationLinkStart(
                this,
                rep,
                context,
                findResourceHelperForLocalizedMessage(),
                getLocalizationParameters(),
                findStringRepresentationLinkStartParameter() );
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
        return constructStringRepresentationLinkEnd(
                this,
                rep,
                context,
                findResourceHelperForLocalizedMessage(),
                getLocalizationParameters(),
                findStringRepresentationLinkEndParameter() );
    }

    /**
     * Factored out creation of a string representation, so several classes can reference the same code.
     *
     * @param ex the LocalizedException to be converted
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param theHelper the ResourceHelper to use
     * @param params the localization parameters to use
     * @param messageParameter the name of the message parameter to use with the ResourceHelper
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the string
     */
    public static String constructStringRepresentation(
            LocalizedException          ex,
            StringRepresentation        rep,
            StringRepresentationContext context,
            ResourceHelper              theHelper,
            Object []                   params,
            String                      messageParameter,
            int                         maxLength )
    {
        return rep.formatEntry( ex.getClass(), messageParameter, maxLength, params );
    }

    /**
     * Factored out creation of the link start of a string representation, so several classes can reference the same code.
     *
     * @param ex the LocalizedException to be converted
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param theHelper the ResourceHelper to use
     * @param params the localization parameters to use
     * @param messageParameter the name of the message parameter to use with the ResourceHelper
     * @return the string
     */
    public static String constructStringRepresentationLinkStart(
            LocalizedException          ex,
            StringRepresentation        rep,
            StringRepresentationContext context,
            ResourceHelper              theHelper,
            Object []                   params,
            String                      messageParameter )
    {
        return "";
    }

    /**
     * Factored out creation of the link end of a string representation, so several classes can reference the same code.
     *
     * @param ex the LocalizedException to be converted
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param theHelper the ResourceHelper to use
     * @param params the localization parameters to use
     * @param messageParameter the name of the message parameter to use with the ResourceHelper
     * @return the string
     */
    public static String constructStringRepresentationLinkEnd(
            LocalizedException          ex,
            StringRepresentation        rep,
            StringRepresentationContext context,
            ResourceHelper              theHelper,
            Object []                   params,
            String                      messageParameter )
    {
        return "";
    }

    /**
     * Allow subclasses to override which ResourceHelper to use.
     *
     * @return the ResourceHelper to use
     */
    protected ResourceHelper findResourceHelperForLocalizedMessage()
    {
        return ResourceHelper.getInstance( getClass() );
    }
    
    /**
     * Allow subclasses to override which ResourceHelper to use.
     *
     * @return the ResourceHelper to use
     */
    protected ResourceHelper findResourceHelperForLocalizedMessageViaEnclosingClass()
    {
        String className = getClass().getName();
        int    dollar = className.indexOf( '$' );
        if( dollar >= 0 ) {
            className = className.substring( 0, dollar );
        }
        return ResourceHelper.getInstance( className, getClass().getClassLoader() );
    }

    /**
     * Allow subclasses to override which key to use in the Resource file for the string representation.
     *
     * @return the key
     */
    protected String findStringRepresentationParameter()
    {
        return STRING_REPRESENTATION_KEY;
    }

    /**
     * Allow subclasses to override which key to use in the Resource file for the link start string representation.
     *
     * @return the key
     */
    protected String findStringRepresentationLinkStartParameter()
    {
        return STRING_REPRESENTATION_LINK_START_KEY;
    }

    /**
     * Allow subclasses to override which key to use in the Resource file for the link end string representation.
     *
     * @return the key
     */
    protected String findStringRepresentationLinkEndParameter()
    {
        return STRING_REPRESENTATION_LINK_END_KEY;
    }

    /**
     * This method can be invoked by subclasses to obtain a suitable message key
     * for the same resource file for all inner classes.
     *
     * @param key the key
     * @return the modified key
     */
    protected String findParameterViaEnclosingClass(
            String key )
    {
        String className = getClass().getName();
        String ret;
        int    dollar = className.indexOf( '$' );
        if( dollar >= 0 ) {
            ret = key + "-" + className.substring( dollar+1 );
        } else {
            ret = key;
        }
        return ret;
    }
}
