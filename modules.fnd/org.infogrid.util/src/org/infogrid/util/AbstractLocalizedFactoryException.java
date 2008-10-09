
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

package org.infogrid.util;

/**
 * A FactoryException that is also localized.
 */
public abstract class AbstractLocalizedFactoryException
        extends
            FactoryException
        implements
            LocalizedException
{
    /**
     * Constructor.
     * 
     * @param sender the Factory that threw this exception
     */
    public AbstractLocalizedFactoryException(
            Factory<?,?,?> sender )
    {
        super( sender, null, null );
    }

    /**
     * Constructor with a message.
     *
     * @param sender the Factory that threw this exception
     * @param msg the message
     */
    public AbstractLocalizedFactoryException(
            Factory<?,?,?> sender,
            String         msg )
    {
        super( sender, msg );
    }

    /**
     * Constructor with no message but a cause.
     *
     * @param sender the Factory that threw this exception
     * @param cause the Throwable that caused this Exception
     */
    public AbstractLocalizedFactoryException(
            Factory<?,?,?> sender,
            Throwable      cause )
    {
        super( sender, cause );
    }

    /**
     * Constructor with a message and a cause.
     *
     * @param sender the Factory that threw this exception
     * @param msg the message
     * @param cause the Exception that caused this Exception
     */
    public AbstractLocalizedFactoryException(
            Factory<?,?,?> sender,
            String         msg,
            Throwable      cause )
    {
        super( sender, msg, cause );
    }

    /**
     * Determine the correct internationalized string that can be shown to the
     * user when the LocalizedException is thrown.
     *
     * @param formatter the formatter to use for data objects to be displayed as part of the message
     * @return the internationalized string
     */
    public String getLocalizedMessage(
            LocalizedObjectFormatter formatter )
    {
        return AbstractLocalizedException.constructLocalizedMessage(
                this,
                findResourceHelperForLocalizedMessage(),
                getLocalizationParameters(),
                findMessageParameter(),
                formatter );
    }

    /**
     * Obtain localized message, per JDK 1.5.
     *
     * @return localized message
     */
    @Override
    public String getLocalizedMessage()
    {
        return getLocalizedMessage( null );
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public abstract Object [] getLocalizationParameters();

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
        String key;
        int    dollar = className.indexOf( '$' );
        if( dollar >= 0 ) {
            className = className.substring( 0, dollar );
        }
        return ResourceHelper.getInstance( className, getClass().getClassLoader() );
    }
    
    /**
     * Allow subclasses to override which key to use in the Resource file for the message.
     *
     * @return the key
     */
    protected String findMessageParameter()
    {
        return MESSAGE_PARAMETER;
    }
    
    /**
     * This method can be invoked by subclasses to obtain a suitable message key
     * for the same resource file for all inner classes.
     *
     * @return the key
     */
    protected String findMessageParameterViaEnclosingClass()
    {
        String className = getClass().getName();
        String key;
        int    dollar = className.indexOf( '$' );
        if( dollar >= 0 ) {
            key = className.substring( dollar+1 ) + "-" + MESSAGE_PARAMETER;
        } else {
            key = MESSAGE_PARAMETER;
        }
        return key;
    }
}
