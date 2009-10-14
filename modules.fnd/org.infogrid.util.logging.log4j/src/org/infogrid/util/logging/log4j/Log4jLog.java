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

package org.infogrid.util.logging.log4j;

import java.util.Properties;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.WriterAppender;
import org.infogrid.util.logging.BufferingDumperFactory;
import org.infogrid.util.logging.Log;

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
        //
        // This catches a lot of exceptions. They may be thrown in very restrictive environments,
        // such as Tomcat on debian.
        ClassLoader currentContextLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( Log4jLog.class.getClassLoader() );

        } catch( Throwable t ) {
            currentContextLoader = null;
            // nothing (FIXME?)
        }

        // it is essential to do this remove before the configure, otherwise we will lose System.err
        LogManager.getRootLogger().removeAppender( rootAppender );

        try {
            PropertyConfigurator.configure( newProperties );
        } catch( Throwable t ) {
        }

        try {
            Log.configure( newProperties );
        } catch( Throwable t ) {
        }

        if( currentContextLoader != null ) {
            try {
                Thread.currentThread().setContextClassLoader( currentContextLoader );
            } catch( Throwable t ) {
                // nothing (FIXME?)
            }
        }
    }

    /**
     * Private constructor, use getLogInstance to get hold of an instance.
     *
     * @param name the name of the Log object
     * @param dumperFactory the DumperFactory to use, if any
     */
    protected Log4jLog(
            String                 name,
            BufferingDumperFactory dumperFactory )
    {
        super( name, dumperFactory );
    }

    /**
     * Obtain the delegate logger.
     *
     * @return the delegate
     */
    public Logger getDelegate()
    {
        ensureDelegate();

        return theDelegate;
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
     * The method to log a traceCall message. This must be implemented by subclasses.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected void logTrace(
            String    msg,
            Throwable t )
    {
        ensureDelegate();

        theDelegate.trace( msg, t );
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
     * Determine whether logging to the trace channel is enabled.
     *
     * @return true if the trace channel is enabled
     */
    public boolean isTraceEnabled()
    {
        ensureDelegate();

        return theDelegate.isTraceEnabled();
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
