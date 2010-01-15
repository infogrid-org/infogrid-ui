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

package org.infogrid.util.logging;

import java.io.PrintStream;

/**
 * A very basic implementation of Log that does not require any other libraries.
 */
public class BasicLog
    extends
        Log
{
    /**
     * Constructor.
     *
     * @param name the name of the Log object
     * @param dumperFactory the DumperFactory to use, if any
     */
    public BasicLog(
            String                 name,
            BufferingDumperFactory dumperFactory )
    {
        super( name, dumperFactory );
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
        if( isFatalEnabled() ) {
            print( "FATAL: ", msg, t );
        }
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
        if( isErrorEnabled() ) {
            print( "ERROR: ", msg, t );
        }
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
        if( isWarnEnabled() ) {
            print( "WARN: ", msg, t );
        }
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
        if( isInfoEnabled() ) {
            print( "INFO: ", msg, t );
        }
    }

    /**
     * The method to log a traceMethodCallEntry message.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected void logDebug(
            String    msg,
            Throwable t )
    {
        if( isDebugEnabled() ) {
            print( "DEBUG: ", msg, t );
        }
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
        if( isTraceEnabled() ) {
            print( "TRACE: ", msg, t );
        }
    }

    /**
     * Determine whether logging to the fatal channel is enabled.
     *
     * @return true if the fatal channel is enabled
     */
    public boolean isFatalEnabled()
    {
        return theIsFatalEnabled;
    }

    /**
     * Determine whether logging to the error channel is enabled.
     *
     * @return true if the error channel is enabled
     */
    public boolean isErrorEnabled()
    {
        return theIsErrorEnabled;
    }

    /**
     * Determine whether logging to the warn channel is enabled.
     *
     * @return true if the warn channel is enabled
     */
    public boolean isWarnEnabled()
    {
        return theIsWarnEnabled;
    }

    /**
     * Determine whether logging to the info channel is enabled.
     *
     * @return true if the info channel is enabled
     */
    public boolean isInfoEnabled()
    {
        return theIsInfoEnabled;
    }

    /**
     * Determine whether logging to the traceMethodCallEntry channel is enabled.
     *
     * @return true if the traceMethodCallEntry channel is enabled
     */
    public boolean isDebugEnabled()
    {
        return theIsDebugEnabled;
    }

    /**
     * Determine whether logging to the trace channel is enabled.
     *
     * @return true if the trace channel is enabled
     */
    public boolean isTraceEnabled()
    {
        return theIsTraceEnabled;
    }

    /**
     * All logging goes through this method.
     *
     * @param level the tag indicating the logging-level (e.g. ERROR)
     * @param message the logging message, if any
     * @param t the Throwable leading to the message, if any
     */
    protected void print(
            String level,
            String message,
            Throwable t )
    {
        if( level != null ) {
            out.print( level );
        }

        if( message != null ) {
            out.println( message );
        }
        
        if( t != null ) {
            t.printStackTrace( out );
        }

        if( message == null && t == null ) {
            out.println();
        }
    }

    /**
     * Set the enabled status of the fatal channel.
     *
     * @param newValue the new value
     */
    public static void setIsFatalEnabled(
            boolean newValue )
    {
        theIsFatalEnabled = newValue;
    }

    /**
     * Set the enabled status of the error channel.
     *
     * @param newValue the new value
     */
    public static void setIsErrorEnabled(
            boolean newValue )
    {
        theIsErrorEnabled = newValue;
    }

    /**
     * Set the enabled status of the warning channel.
     *
     * @param newValue the new value
     */
    public static void setIsWarnEnabled(
            boolean newValue )
    {
        theIsWarnEnabled = newValue;
    }

    /**
     * Set the enabled status of the info channel.
     *
     * @param newValue the new value
     */
    public static void setIsInfoEnabled(
            boolean newValue )
    {
        theIsInfoEnabled = newValue;
    }

    /**
     * Set the enabled status of the traceMethodCallEntry channel.
     *
     * @param newValue the new value
     */
    public static void setIsDebugEnabled(
            boolean newValue )
    {
        theIsDebugEnabled = newValue;
    }

    /**
     * Is the fatal channel enabled?
     */
    protected static boolean theIsFatalEnabled = true;

    /**
     * Is the error channel enabled?
     */
    protected static boolean theIsErrorEnabled = true;

    /**
     * Is the warn channel enabled?
     */
    protected static boolean theIsWarnEnabled = true;

    /**
     * Is the info channel enabled?
     */
    protected static boolean theIsInfoEnabled = false;

    /**
     * Is the debug channel enabled?
     */
    protected static boolean theIsDebugEnabled = false;

    /**
     * Is the traceMethodCallEntry channel enabled?
     */
    protected static boolean theIsTraceEnabled = false;

    /**
     * The PrintStream to which we log.
     */
    protected static PrintStream out = System.err;

    /**
     * The implementor of LogFactory for the basic Logger.
     */
    public static class Factory
        extends
            LogFactory
    {
        /**
         * Obtain a Logger.
         *
         * @param name name of the Log object that we are looking for
         * @param dumperFactory the factory for dumpers
         * @return the Log object
         */
        public Log create(
                String                 name,
                BufferingDumperFactory dumperFactory )
        {
            return new BasicLog( name, dumperFactory );
        }

        /**
         * Show a message to the user interface. This requires an implementation
         * of the LogFactory that is appropriate for the user interface used by the
         * application, e.g. AWT dialogs or HTML pages.
         *
         * @param logger the logger from which this invocation is delegated
         * @param parentComponent the component to which the error message is reported, e.g.
         *        an AWT component, or an HTML page
         * @param message the error message
         * @param title the title of the dialog
         */
        public void showMessage(
                Log    logger,
                Object parentComponent,
                String message,
                String title )
        {
            ((BasicLog)logger).print( null, message, null );
        }
    }
}
