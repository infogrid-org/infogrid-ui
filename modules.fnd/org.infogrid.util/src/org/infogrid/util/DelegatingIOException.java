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

import java.io.IOException;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationDirectorySingleton;

/**
 * Subclass of <code>java.io.IOException</code> that can carry a <code>Throwable</code> as a
 * payload. Unfortunately Java IOException's constructor does not allow us to specify
 * a cause directly, so this class is a workaround.
 */
public class DelegatingIOException
        extends
            IOException
        implements
            LocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param cause the cause
     */
    public DelegatingIOException(
            Throwable cause )
    {
        super.initCause( cause ); // stupid API
    }

    /**
     * Constructor.
     *
     * @param msg the message
     * @param cause the cause
     */
    public DelegatingIOException(
            String    msg,
            Throwable cause )
    {
        super( msg );

        super.initCause( cause ); // stupid API
    }

    /**
     * Determine the correct internationalized string that can be shown to the
     * user. Use a default formatter.
     *
     * @return the internationalized string
     */
    @Override
    public String getLocalizedMessage()
    {
        return toStringRepresentation(
                StringRepresentationDirectorySingleton.getSingleton().get( StringRepresentationDirectory.TEXT_PLAIN_NAME ),
                null );
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[0];
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
        return AbstractLocalizedException.constructStringRepresentation(
                this,
                rep,
                context,
                findResourceHelperForLocalizedMessage(),
                getLocalizationParameters(),
                findStringRepresentationParameter() );
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
        return AbstractLocalizedException.constructStringRepresentationLinkStart(
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
        return AbstractLocalizedException.constructStringRepresentationLinkEnd(
                this,
                rep,
                context,
                findResourceHelperForLocalizedMessage(),
                getLocalizationParameters(),
                findStringRepresentationLinkEndParameter() );
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
            ret = className.substring( dollar+1 ) + "-" + key;
        } else {
            ret = key;
        }
        return ret;
    }
}
