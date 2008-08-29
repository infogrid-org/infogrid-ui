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

package org.infogrid.util.logging.log4j;

import org.infogrid.util.logging.Log;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.WriterAppender;

import java.util.Properties;

/**
 * Implementation of the Log concept for log4j.
 */
public class Log4jLog
    extends
        Log
{
    /**
     * This method allows us to configure the log with a set of properties.
     *
     * @param newProperties the properties
     */
    public static void configure(
            Properties newProperties )
    {
        if( newProperties == null || newProperties.isEmpty() ) {
            return;
        }

        // what a hack. Otherwise log4j will try the Thread context ClassLoader
        ClassLoader currentContextLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( Log4jLog.class.getClassLoader() );

            // it is essential to do this remove before the configure, otherwise we will lose System.err
            LogManager.getRootLogger().removeAppender( rootAppender );

            PropertyConfigurator.configure( newProperties );

            Log.configure( newProperties );

        } finally {
            Thread.currentThread().setContextClassLoader( currentContextLoader );
        }
    }

    /**
     * Constructor.
     *
     * @param name the name of the Log
     */
    public Log4jLog(
            String name )
    {
        super( name );
    }

    /**
     * The method to log a fatal error.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected void logFatal(
            String    msg,
            Throwable t )
    {
        ensureDelegate();

        theDelegate.fatal( msg, t );
    }

    /**
     * The method to log an error.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected void logError(
            String    msg,
            Throwable t )
    {
        ensureDelegate();

        theDelegate.error( msg, t );
    }

    /**
     * The method to log a warning.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected void logWarn(
            String    msg,
            Throwable t )
    {
        ensureDelegate();

        theDelegate.warn( msg, t );
    }

    /**
     * The method to log an informational message.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected void logInfo(
            String    msg,
            Throwable t )
    {
        ensureDelegate();

        theDelegate.info( msg, t );
    }

    /**
     * The method to log a debug message.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected void logDebug(
            String    msg,
            Throwable t )
    {
        ensureDelegate();

        theDelegate.debug( msg, t );
    }

    /**
     * Determine whether logging to the info channel is enabled.
     *
     * @return true if the info channel is enabled
     */
    public boolean isInfoEnabled()
    {
        ensureDelegate();

        return theDelegate.isInfoEnabled();
    }

    /**
     * Determine whether logging to the debug channel is enabled.
     *
     * @return true if the debug channel is enabled
     */
    public boolean isDebugEnabled()
    {
        ensureDelegate();

        return theDelegate.isDebugEnabled();
    }

    /**
     * Internal helper making sure that we have a delegate.
     */
    protected void ensureDelegate()
    {
        if( theDelegate == null ) {
            theDelegate = Logger.getLogger( theName );
        }
    }

    /**
     * The log4j logging delegate.
     */
    protected Logger theDelegate;

    /**
     * The root appender which is used before Log.configure() has been called.
     */
    protected static Appender rootAppender;

    /**
     * Initialize defaults.
     */
    static {
        rootAppender = new WriterAppender(
                new PatternLayout( "%-5p %d [%t] %-17c{3} (%13F:%L) - %m\n  @ %C.%M:%L\n" ),
                System.err );

        Logger root = LogManager.getRootLogger();
        root.setLevel( Level.WARN );
        root.addAppender( rootAppender );
    }
}
