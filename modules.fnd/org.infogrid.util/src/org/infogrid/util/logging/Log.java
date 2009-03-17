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

import java.text.MessageFormat;
import java.util.Properties;
import org.infogrid.util.AbstractLocalizedException;
import org.infogrid.util.AbstractLocalizedRuntimeException;
import org.infogrid.util.FactoryException;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.text.HasStringRepresentation;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;

/** 
  * <p>The central class in the org.infogrid.util.logging package.</p>
  * <p>How to use it (simplest case):</p>
  * <ol>
  *  <li> import this class "<code>import org.infogrid.util.logging.Log;</code>"</li>
  *  <li> put a "<code>private static final Log log = Log.getLogInstance( &lt;myClass&gt;.class );</code>"
  *       into your class,<br>
  *       where "<code>&lt;myClass&gt;</code>" is your class </li>
  *  <li> use the logger, eg "<code>log.warn("this is a warning"); <BR>
  *       log.traceMethodCallEntry( "Finished parsing file: " + file.getName() );</code>"</li>
  * </ol>
  * <p>Unlike in previous versions, this class now defaults to basic logging-to-System.err
  * instead of logging to log4j.</p>
  */
public abstract class Log
{
    /**
     * Configure the Log class with a set of Properties.
     *
     * @param newProperties the properties
     */
    public static void configure(
            Properties newProperties )
    {
        if( newProperties == null || newProperties.isEmpty() ) {
            return;
        }

        String val = newProperties.getProperty( "StacktraceOnError" ); // default is yes
        if( val != null && ( val.startsWith( "n" ) || val.startsWith( "N" ) || val.startsWith( "F" ) || val.startsWith( "f" )) ) {
            logStacktraceOnError = false;
        } else {
            logStacktraceOnError = true;
        }

        val = newProperties.getProperty( "StacktraceOnWarn" ); // default is no
        if( val != null && ( val.startsWith( "y" ) || val.startsWith( "Y" ) || val.startsWith( "T" ) || val.startsWith( "t" )) ) {
            logStacktraceOnWarn = true;
        } else {
            logStacktraceOnWarn = false;
        }

        val = newProperties.getProperty( "StacktraceOnInfo" ); // default is no
        if( val != null && ( val.startsWith( "y" ) || val.startsWith( "Y" ) || val.startsWith( "T" ) || val.startsWith( "t" )) ) {
            logStacktraceOnInfo = true;
        } else {
            logStacktraceOnInfo = false;
        }

        val = newProperties.getProperty( "StacktraceOnDebug" ); // default is no
        if( val != null && ( val.startsWith( "y" ) || val.startsWith( "Y" ) || val.startsWith( "T" ) || val.startsWith( "t" )) ) {
            logStacktraceOnDebug = true;
        } else {
            logStacktraceOnDebug = false;
        }

        val = newProperties.getProperty( "StacktraceOnTraceCall" ); // default is no
        if( val != null && ( val.startsWith( "y" ) || val.startsWith( "Y" ) || val.startsWith( "T" ) || val.startsWith( "t" )) ) {
            logStacktraceOnTraceCall = true;
        } else {
            logStacktraceOnTraceCall = false;
        }
    }

    /**
     * Set a factory for new Log objects.
     *
     * @param newFactory the new LogFactory
     * @throws IllegalArgumentException thrown if a null is provided as a new factory
     */
    public static void setLogFactory(
            LogFactory newFactory )
    {
        if( newFactory == null ) {
            throw new NullPointerException( "Null factory not allowed" );
        }
        theFactory = newFactory;
    }

    /**
     * Obtain a Logger.
     *
     * @param clazz the Class for which we are looking for a Log object
     * @return the Log object
     */
    public static Log getLogInstance(
            Class clazz )
    {
        return theFactory.create( clazz );
    }

    /**
     * Obtain a Logger.
     *
     * @param name name of the Log object that we are looking for
     * @return the Log object
     */
    public static Log getLogInstance(
            String name )
    {
        return theFactory.create( name );
    }

    /**
     * Obtain a Logger and specify a dumper factory instead of the default.
     *
     * @param clazz the Class for which we are looking for a Log object
     * @param dumperFactory the factory for dumpers
     * @return the Log object
     */
    public static Log getLogInstance(
            Class                                    clazz,
            DumperFactory<? extends BufferingDumper> dumperFactory )
    {
        return theFactory.create( clazz, dumperFactory );
    }

    /**
     * Obtain a Logger.
     *
     * @param name name of the Log object that we are looking for
     * @param dumperFactory the factory for dumpers
     * @return the Log object
     */
    public static Log getLogInstance(
            String                                   name,
            DumperFactory<? extends BufferingDumper> dumperFactory )
    {
        return theFactory.create( name, dumperFactory );
    }

    /**
     * Private constructor, use getLogInstance to get hold of an instance.
     *
     * @param name the name of the Log object
     * @param dumperFactory the DumperFactory to use, if any
     */
    protected Log(
            String                                   name,
            DumperFactory<? extends BufferingDumper> dumperFactory )
    {
        theName          = name;
        theDumperFactory = dumperFactory;
    }

    /**
     * The method to log a fatal error. This must be implemented by subclasses.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected abstract void logFatal(
            String    msg,
            Throwable t );

    /**
     * The method to log an error. This must be implemented by subclasses.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected abstract void logError(
            String    msg,
            Throwable t );

    /**
     * The method to log a warning. This must be implemented by subclasses.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected abstract void logWarn(
            String    msg,
            Throwable t );

    /**
     * The method to log an informational message. This must be implemented by subclasses.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected abstract void logInfo(
            String    msg,
            Throwable t );

    /**
     * The method to log a debug message. This must be implemented by subclasses.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected abstract void logDebug(
            String    msg,
            Throwable t );

    /**
     * The method to log a traceCall message. This must be implemented by subclasses.
     *
     * @param msg the message to log
     * @param t   the Throwable to log. This may be null.
     */
    protected abstract void logTraceCall(
            String    msg,
            Throwable t );

    /**
     * Determine whether logging to the info channel is enabled.
     *
     * @return true if the info channel is enabled
     */
    public abstract boolean isInfoEnabled();

    /**
     * Determine whether logging to the debug channel is enabled.
     *
     * @return true if the debug channel is enabled
     */
    public abstract boolean isDebugEnabled();

    /**
     * Determine whether logging to the trace channel is enabled.
     *
     * @return true if the trace channel is enabled
     */
    public abstract boolean isTraceCallEnabled();

    /**
     * If the <code>obj</code> parameter is <code>null</code>, or
     * if the <code>obj</code> is a boolean that is false, then
     * logs <code>args</code> as an error.
     *
     * @param obj the obj to check for
     * @param args the argument list
     */
    public final void assertLog(
            Object    obj,
            Object... args )
    {
        if( obj == null ) {
            error( "Assertion failed: object null", args );
        } else if( obj instanceof Boolean && !((Boolean)obj).booleanValue() ) {
            error( "Assertion failed: flag is false", args );
        }
    }

    /**
     * This logs a fatal error.
     *
     * @param args the argument list
     */
    public final void fatal(
            Object... args )
    {
        String    message = determineMessage(   true, args );
        Throwable t       = determineThrowable( true, args );

        logFatal( message, t );
    }

    /**
     * This logs an error.
     *
     * @param args the argument list
     */
    public final void error(
            Object... args )
    {
        String    message = determineMessage(   logStacktraceOnError, args );
        Throwable t       = determineThrowable( logStacktraceOnError, args );

        logError( message, t );
    }

    /**
     * This logs a warning.
     *
     * @param args the argument list
     */
    public final void warn(
            Object... args )
    {
        String    message = determineMessage(   logStacktraceOnWarn, args );
        Throwable t       = determineThrowable( logStacktraceOnWarn, args );

        logWarn( message, t );
    }

    /**
     * This logs an info message.
     *
     * @param args the argument list
     */
    public final void info(
            Object... args )
    {
        String    message = determineMessage(   logStacktraceOnInfo, args );
        Throwable t       = determineThrowable( logStacktraceOnInfo, args );

        logInfo( message, t );
    }

    /**
     * This logs a debug message.
     *
     * @param args the argument list
     */
    public final void debug(
            Object... args )
    {
        String    message = determineMessage(   logStacktraceOnDebug, args );
        Throwable t       = determineThrowable( logStacktraceOnDebug, args );

        logDebug( message, t );
    }

    /**
     * Logs a trace message that represents a method invocation.
     *
     * @param subject the object on which the method was invoked
     * @param method the name of the method being invoked
     * @param args the arguments to the method call
     */
    public final void traceMethodCallEntry(
            Object     subject,
            String     method,
            Object ... args )
    {
        String    message = determineMessage(   logStacktraceOnTraceCall, args );
        Throwable t       = determineThrowable( logStacktraceOnTraceCall, args );

        StringBuilder buf = new StringBuilder();
        buf.append( "Entered " );
        buf.append( subject );
        buf.append( " ." );
        buf.append( method );
        if( message != null ) {
            buf.append( ":" );
            buf.append( message );
        }
        logTraceCall( buf.toString(), t );
    }

    /**
     * Logs a trace message that represents a static method invocation.
     *
     * @param subject the Class on which the method was invoked
     * @param method the name of the method being invoked
     * @param args the arguments to the method call
     */
    public final void traceMethodCallEntry(
            Class      subject,
            String     method,
            Object ... args )
    {
        String    message = determineMessage(   logStacktraceOnTraceCall, args );
        Throwable t       = determineThrowable( logStacktraceOnTraceCall, args );

        StringBuilder buf = new StringBuilder();
        buf.append( "Entered " );
        buf.append( subject );
        buf.append( " ." );
        buf.append( method );
        if( message != null ) {
            buf.append( ":" );
            buf.append( message );
        }
        logTraceCall( buf.toString(), t );
    }

    /**
     * Logs a trace message that represents a return from a method.
     *
     * @param subject the object on which the method was invoked
     * @param method the name of the method being invoked
     */
    public final void traceMethodCallExit(
            Object     subject,
            String     method )
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "Exited " );
        buf.append( subject );
        buf.append( " ." );
        buf.append( method );
        logTraceCall( buf.toString(), null );
    }

    /**
     * Logs a trace message that represents a return from a static method.
     *
     * @param subject the Class on which the method was invoked
     * @param method the name of the method being invoked
     */
    public final void traceMethodCallExit(
            Class      subject,
            String     method )
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "Exited " );
        buf.append( subject );
        buf.append( " ." );
        buf.append( method );
        logTraceCall( buf.toString(), null );
    }

    /**
     * Logs a trace message that represents a constructor invocation.
     *
     * @param subject the instance that was created by the constructor
     * @param args the arguments to the method call
     */
    public final void traceConstructor(
            Object     subject,
            Object ... args )
    {
        String    message = determineMessage(   logStacktraceOnTraceCall, args );
        Throwable t       = determineThrowable( logStacktraceOnTraceCall, args );

        StringBuilder buf = new StringBuilder();
        buf.append( "Constructed " );
        buf.append( subject );
        if( message != null ) {
            buf.append( ":" );
            buf.append( message );
        }
        logTraceCall( buf.toString(), t );
    }

    /**
     * Logs a trace message that indicates object finalization.
     *
     * @param subject the instance being finalized
     */
    public final void traceFinalization(
            Object subject )
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "Finalized " );
        buf.append( subject );
        logTraceCall( buf.toString(), null );
    }

    /**
     * Given an argument list, determine the message.
     * 
     * @param logStacktrace log the tracktrace even if no Throwable is given in the argument list
     * @param args the argument list
     * @return the message, if any
     */
    protected String determineMessage(
            boolean   logStacktrace,
            Object... args )
    {
        try {
            BufferingDumper dumper = theDumperFactory.obtainFor( this );
            if( dumper == null ) {
                return "Cannot find Dumper";
            }
            if( args.length == 1 && args[0] instanceof String ) {
                return (String) args[0]; // special case of a simple informational message
            } else if( args.length == 2 && ( args[0] instanceof String ) && ( args[1] instanceof Object[] ) && ((Object[])args[1]).length == 0 ) {
                return (String) args[0]; // special case of a method invocation with no arguments
            } else {
                dumper.dump( args );
                return dumper.getBuffer();
            }


        } catch( FactoryException ex ) {
            error( ex );
            return "Cannot create Dumper";
        }
    }

    /**
     * Given an argument list, determine the Throwable to log.
     *
     * @param logStacktrace log the tracktrace even if no Throwable is given in the argument list
     * @param args the argument list
     * @return the Throwable, if any
     */
    protected Throwable determineThrowable(
            boolean   logStacktrace,
            Object... args )
    {
        Object lastArgument;
        if( args != null && args.length > 0 ) {
            lastArgument= args[ args.length-1 ];
        } else {
            lastArgument = null;
        }
        if( lastArgument instanceof Throwable ) {
            return (Throwable) lastArgument;
        }
        if( logStacktrace ) {
            return new Exception( "Logging marker" );
        }
        return null;
    }


    /**
     * This logs a localized fatal user error. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     * 
     * @param parentComponent center a possible dialog against this parent component
     * @param t               Throwable to be logged
     * @param rep             the StringRepresentation to use
     * @param context         the StringRepresentationContext to use
     */
    public final void userFatal(
            Object                      parentComponent,
            Throwable                   t,
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        if( t instanceof AbstractLocalizedException ) {
            AbstractLocalizedException realEx = (AbstractLocalizedException) t;
            userFatal(
                    realEx.toStringRepresentation( rep, context, HasStringRepresentation.UNLIMITED_LENGTH ),
                    parentComponent,
                    realEx );

        } else if( t instanceof AbstractLocalizedRuntimeException ) {
            AbstractLocalizedRuntimeException realEx = (AbstractLocalizedRuntimeException) t;
            userFatal(
                    realEx.toStringRepresentation( rep, context, HasStringRepresentation.UNLIMITED_LENGTH ),
                    parentComponent,
                    realEx );

        } else {
            userFatal(
                    theResourceHelper.getResourceString( t.getClass().getName() ),
                    new Object[] { t.getLocalizedMessage() },
                    parentComponent,
                    t );
        }
    }

    /**
     * This logs a localized fatal user error. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param parentComponent center a possible dialog against this parent component
     */
    public final void userFatal(
            String message,
            Object parentComponent )
    {
        userFatal( message, null, parentComponent, null );
    }

    /**
     * This logs a localized fatal user error. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param parentComponent center a possible dialog against this parent component
     * @param t               Throwable to be logged
     */
    public final void userFatal(
            String    message,
            Object    parentComponent,
            Throwable t )
    {
        userFatal( message, null, parentComponent, t );
    }

    /**
     * This logs a localized fatal user error. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param params          the Objects to be substituted into the message
     * @param parentComponent center the dialog against this parent component
     */
    public final void userFatal(
            String    message,
            Object [] params,
            Object    parentComponent )
    {
        userFatal( message, params, parentComponent, null );
    }

    /**
     * This logs a localized fatal user error. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param params          the Objects to be substituted into the message
     * @param parentComponent center the dialog against this parent component
     * @param t               Throwable to be logged
     */
    public final void userFatal(
            String    message,
            Object [] params,
            Object    parentComponent,
            Throwable t )
    {
        String formattedMessage = message;

        if( params != null ) {
            try {
                formattedMessage = MessageFormat.format( message, params );

            } catch (IllegalArgumentException ex) {
                formattedMessage = message + " (error while formatting translated message)";
            }
        }

        logFatal( formattedMessage, t );

        showMessage(
                parentComponent,
                theResourceHelper.getResourceStringOrDefault( "FatalErrorMessagePrefix", "" )
                + formattedMessage
                + theResourceHelper.getResourceStringOrDefault( "FatalErrorMessagePostfix", "" ),
                theResourceHelper.getResourceString( "FatalErrorTitle" ));
    }

    /**
     * This logs a localized user error. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     * 
     * @param parentComponent center a possible dialog against this parent component
     * @param t               Throwable to be logged
     * @param rep             the StringRepresentation to use
     * @param context         the StringRepresentationContext to use
     */
    public final void userError(
            Object                      parentComponent,
            Throwable                   t,
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        if( t instanceof AbstractLocalizedException ) {
            AbstractLocalizedException realEx = (AbstractLocalizedException) t;
            userError(
                    realEx.toStringRepresentation( rep, context, HasStringRepresentation.UNLIMITED_LENGTH ),
                    parentComponent,
                    realEx );

        } else if( t instanceof AbstractLocalizedRuntimeException ) {
            AbstractLocalizedRuntimeException realEx = (AbstractLocalizedRuntimeException) t;
            userError(
                    realEx.toStringRepresentation( rep, context, HasStringRepresentation.UNLIMITED_LENGTH ),
                    parentComponent,
                    realEx );
        } else {
            userError(
                    theResourceHelper.getResourceString( t.getClass().getName() ),
                    new Object[] { t.getLocalizedMessage() },
                    parentComponent,
                    t );
        }
    }

    /**
     * This logs a localized user error. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param parentComponent center the dialog against this parent Component
     */
    public final void userError(
            String message,
            Object parentComponent )
    {
        userError( message, null, parentComponent, null );
    }

    /**
     * This logs a localized user error. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param parentComponent center the dialog against this parent component
     * @param t               Throwable to be logged
     */
    public final void userError(
            String    message,
            Object    parentComponent,
            Throwable t )
    {
        userError( message, null, parentComponent, t );
    }

    /**
     * This logs a localized user error. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param params          the Objects to be substituted into the message
     * @param parentComponent center the dialog against this parent component
     */
    public final void userError(
            String    message,
            Object [] params,
            Object    parentComponent )
    {
        userError( message, params, parentComponent, null );
    }

    /**
     * This logs a localized user error. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param params          the Objects to be substituted into the message
     * @param parentComponent center the dialog against this parent component
     * @param t               Throwable to be logged
     */
    public final void userError(
            String    message,
            Object [] params,
            Object    parentComponent,
            Throwable t )
    {
        String formattedMessage = message;

        if( params != null ) {
            try {
                formattedMessage = MessageFormat.format( message, params );

            } catch (IllegalArgumentException ex) {
                formattedMessage = message + " (error while formatting translated message)";
            }
        }

        logError( formattedMessage, t );

        showMessage(
                parentComponent,
                theResourceHelper.getResourceStringOrDefault( "ErrorMessagePrefix", "" )
                + formattedMessage
                + theResourceHelper.getResourceStringOrDefault( "ErrorMessagePostfix", "" ),
                theResourceHelper.getResourceString( "ErrorTitle" ));
    }

    /**
     * This logs a user warning. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     * 
     * @param parentComponent center a possible dialog against this parent component
     * @param t               Throwable to be logged
     * @param rep             the StringRepresentation to use
     * @param context         the StringRepresentationContext to use
     */
    public final void userWarn(
            Object                      parentComponent,
            Throwable                   t,
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        if( t instanceof AbstractLocalizedException ) {
            AbstractLocalizedException realEx = (AbstractLocalizedException) t;
            userWarn(
                    realEx.toStringRepresentation( rep, context, HasStringRepresentation.UNLIMITED_LENGTH ),
                    parentComponent,
                    realEx );

        } else if( t instanceof AbstractLocalizedRuntimeException ) {
            AbstractLocalizedRuntimeException realEx = (AbstractLocalizedRuntimeException) t;
            userWarn(
                    realEx.toStringRepresentation( rep, context, HasStringRepresentation.UNLIMITED_LENGTH ),
                    parentComponent,
                    realEx );

        } else {
            userWarn(
                    theResourceHelper.getResourceString( t.getClass().getName() ),
                    new Object[] { t.getLocalizedMessage() },
                    parentComponent,
                    t );
        }
    }

    /**
     * This logs a user warning. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param parentComponent center the dialog against this parent component
     */
    public final void userWarn(
            String message,
            Object parentComponent )
    {
        userWarn( message, null, parentComponent, null );
    }

    /**
     * This logs a user warning. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param parentComponent center the dialog against this AWT Component
     * @param t               Throwable to be logged
     */
    public final void userWarn(
            String    message,
            Object    parentComponent,
            Throwable t )
    {
        userWarn( message, null, parentComponent, t );
    }

    /**
     * This logs a user warning. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param params          the Objects to be substituted into the message
     * @param parentComponent center the dialog against this AWT Component
     */
    public final void userWarn(
            String    message,
            Object [] params,
            Object    parentComponent )
    {
        userWarn( message, params, parentComponent, null );
    }

    /**
     * This logs a user warning. This may also pop up a dialog. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param message         String to display/append
     * @param params          the Objects to be substituted into the message
     * @param parentComponent center the dialog against this parent component
     * @param t               Throwable to be logged
     */
    public final void userWarn(
            String    message,
            Object [] params,
            Object    parentComponent,
            Throwable t )
    {
        String formattedMessage = message;

        if( params != null ) {
            try {
                formattedMessage = MessageFormat.format( message, params );

            } catch (IllegalArgumentException ex) {
                formattedMessage = message + " (error while formatting translated message)";
            }
        }

        logWarn( formattedMessage, t );

        showMessage(
                parentComponent,
                theResourceHelper.getResourceStringOrDefault( "WarningMessagePrefix", "" )
                + formattedMessage
                + theResourceHelper.getResourceStringOrDefault( "WarningMessagePostfix", "" ),
                theResourceHelper.getResourceString( "WarningTitle" ));
    }

    /**
     * This internal helper may show a message to the user interface, or do nothing. As this
     * class must be independent of the underlying GUI technology, the parentComponent
     * parameter is of type Object. In an AWT context, it would pop up an AWT dialog.
     *
     * @param parentComponent the component to which the error message is reported, e.g.
     *        an AWT component, or an HTML page
     * @param message the error message
     * @param title the title of the dialog
     */
    protected void showMessage(
            Object parentComponent,
            String message,
            String title )
    {
        theFactory.showMessage( this, parentComponent, message, title );
    }

    /**
     * The name of this logger.
     */
    protected String theName;

    /**
     * The factory for creating Dumper objects to use for dumping objects, if any.
     */
    protected DumperFactory<? extends BufferingDumper> theDumperFactory;

    /**
     * The factory for new Log objects.
     */
    private static LogFactory theFactory = new BasicLog.Factory();

    /**
     * Where we get the internationalization resources from.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( Log.class );

    /**
     * This specifies whether a stack trace shall be logged with every error.
     */
    private static boolean logStacktraceOnError = false;

    /**
     * This specifies whether a stack trace shall be logged with every warning.
     */
    private static boolean logStacktraceOnWarn = false;

    /**
     * This specifies whether a stack trace shall be logged with every info.
     */
    private static boolean logStacktraceOnInfo = false;

    /**
     * This specifies whether a stack trace shall be logged with every debug.
     */
    private static boolean logStacktraceOnDebug = false;

    /**
     * This specifies whether a stack trace shall be logged with every traceCall.
     */
    private static boolean logStacktraceOnTraceCall = false;
}
