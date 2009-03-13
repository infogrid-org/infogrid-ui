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
  *       log.debug( "Finished parsing file: " + file.getName() );</code>"</li>
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
     * The method to log a fatal error. This may be overridden by subclasses.
     *
     * @param msg    the message to log
     * @param toDump an Object to dump, if any
     * @param t      the Throwable to log. This may be null.
     */
    protected void logFatal(
            String    msg,
            Object    toDump,
            Throwable t )
    {
        String newMessage = createCompoundMessage( msg, toDump );
        logFatal( newMessage, t );
    }

    /**
     * The method to log an error. This may be overridden by subclasses.
     *
     * @param msg    the message to log
     * @param toDump an Object to dump, if any
     * @param t      the Throwable to log. This may be null.
     */
    protected void logError(
            String    msg,
            Object    toDump,
            Throwable t )
    {
        String newMessage = createCompoundMessage( msg, toDump );
        logError( newMessage, t );
    }

    /**
     * The method to log a warning. This may be overridden by subclasses.
     *
     * @param msg    the message to log
     * @param toDump an Object to dump, if any
     * @param t      the Throwable to log. This may be null.
     */
    protected void logWarn(
            String    msg,
            Object    toDump,
            Throwable t )
    {
        String newMessage = createCompoundMessage( msg, toDump );
        logWarn( newMessage, t );
    }

    /**
     * The method to log an informational message. This may be overridden by subclasses.
     *
     * @param msg    the message to log
     * @param toDump an Object to dump, if any
     * @param t      the Throwable to log. This may be null.
     */
    protected void logInfo(
            String    msg,
            Object    toDump,
            Throwable t )
    {
        String newMessage = createCompoundMessage( msg, toDump );
        logInfo( newMessage, t );
    }

    /**
     * The method to log a debug message. This may be overridden by subclasses.
     *
     * @param msg    the message to log
     * @param toDump an Object to dump, if any
     * @param t      the Throwable to log. This may be null.
     */
    protected void logDebug(
            String    msg,
            Object    toDump,
            Throwable t )
    {
        String newMessage = createCompoundMessage( msg, toDump );
        logDebug( newMessage, t );
    }

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
     * If <code>assertion</code> parameter is <code>false</code>, then
     * logs <code>msg</code> as an error.
     *
     * @param assertion the assertion that is made
     * @param msg       the message to log if <code>assertion</code> is false
     */
    public final void assertLog(
            boolean assertion,
            String  msg )
    {
        if( !assertion ) {
            logError( msg, null, null );
        }
    }

    /**
     * If <code>assertion</code> parameter is <code>false</code>, then
     * logs <code>msg</code> as an error.
     *
     * @param assertion the assertion that is made
     * @param msg       the message to log if <code>assertion</code> is false
     * @param t         the Throwable to log if <code>assertion</code> is false
     */
    public final void assertLog(
            boolean   assertion,
            String    msg,
            Throwable t )
    {
        if( !assertion ) {
            logError( msg, null, t );
        }
    }

    /**
     * If <code>assertion</code> parameter is <code>null</code>, then
     * logs <code>msg</code> as an error.
     *
     * @param assertion the assertion that is made
     * @param msg       the message to log if <code>assertion</code> is null
     */
    public final void assertLog(
            Object assertion,
            String msg )
    {
        if( assertion == null ) {
            logError( msg, null, null );
        }
    }

    /**
     * If <code>assertion</code> parameter is <code>null</code>, then
     * logs <code>msg</code> as an error.
     *
     * @param assertion the assertion that we make
     * @param msg       the message to log if <code>assertion</code> is null
     * @param t         the Throwable to log if <code>assertion</code> is false
     */
    public final void assertLog(
            Object    assertion,
            String    msg,
            Throwable t )
    {
        if( assertion == null ) {
            logError( msg, null, t );
        }
    }

    /**
     * This logs a fatal error. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     */
    public final void fatal(
            Object message )
    {
        fatal( message, null );
    }

    /**
     * This logs a fatal error. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     * @param toDump  the Object to dump, if any
     */
    public final void fatal(
            Object    message,
            Object    toDump )
    {
        if( message instanceof Throwable ) {
            fatal( message, toDump, (Throwable) message );
        } else {
            fatal( message, toDump, null );
        }
    }

    /**
     * This logs a fatal error. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     * @param toDump  an Object to dump, if any
     * @param t       the Throwable to log
     */
    public final void fatal(
            Object    message,
            Object    toDump,
            Throwable t )
    {
        if( t != null ) {
            logFatal( String.valueOf( message ), toDump, t );
        } else if( logStacktraceOnError ) {
            logFatal( String.valueOf( message ), toDump, new Exception( "Logging marker" ) );
        } else {
            logFatal( String.valueOf( message ), toDump, null );
        }
    }

    /**
     * This logs an error. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     */
    public final void error(
            Object message )
    {
        error( message, null );
    }

    /**
     * This logs an error. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     * @param toDump  an Object to dump, if any
     */
    public final void error(
            Object    message,
            Object    toDump )
    {
        if( message instanceof Throwable ) {
            error( "Throwable", toDump, (Throwable) message );
        } else {
            error( message, toDump, null );
        }
    }

    /**
     * This logs an error. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     * @param toDump  an Object to dump, if any
     * @param t       the Throwable to log
     */
    public final void error(
            Object    message,
            Object    toDump,
            Throwable t )
    {
        if( t != null ) {
            logError( String.valueOf( message ), toDump, t );
        } else if( logStacktraceOnError ) {
            logError( String.valueOf( message ), toDump, new Exception( "Logging marker" ) );
        } else {
            logError( String.valueOf( message ), toDump, null );
        }
    }

    /**
     * This logs a warning. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the warning Object
     */
    public final void warn(
            Object message )
    {
        warn( message, null );
    }

    /**
     * This logs a warning. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     * @param toDump  an Object to dump, if any
     */
    public final void warn(
            Object    message,
            Object    toDump )
    {
        if( message instanceof Throwable ) {
            warn( message, toDump, (Throwable) message );
        } else {
            warn( message, toDump, null );
        }
    }

    /**
     * This logs a warning. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     * @param toDump  an Object to dump, if any
     * @param t       the Throwable to log
     */
    public final void warn(
            Object    message,
            Object    toDump,
            Throwable t )
    {
        if( t != null ) {
            logWarn( String.valueOf( message ), toDump, t );
        } else if( logStacktraceOnWarn ) {
            logWarn( String.valueOf( message ), toDump, new Exception( "Logging marker" ) );
        } else {
            logWarn( String.valueOf( message ), toDump, null );
        }
    }

    /**
     * This logs an info message. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the info Object
     */
    public final void info(
            Object message )
    {
        info( message, null );
    }

    /**
     * This logs an info message. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     * @param toDump  an Object to dump, if any
     */
    public final void info(
            Object    message,
            Object    toDump )
    {
        if( message instanceof Throwable ) {
            info( message, toDump, (Throwable) message );
        } else {
            info( message, toDump, null );
        }
    }

    /**
     * This logs an info message. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     * @param toDump  an Object to dump, if any
     * @param t       the Throwable to log
     */
    public final void info(
            Object    message,
            Object    toDump,
            Throwable t )
    {
        if( t != null ) {
            logInfo( String.valueOf( message ), toDump, t );
        } else if( logStacktraceOnInfo ) {
            logInfo( String.valueOf( message ), toDump, new Exception( "Logging marker" ) );
        } else {
            logInfo( String.valueOf( message ), toDump, null );
        }
    }

    /**
     * This logs a debug message. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the info Object
     */
    public final void debug(
            Object message )
    {
        debug( message, null );
    }

    /**
     * This logs a debug message. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the error Object
     * @param toDump  an Object to dump, if any
     */
    public final void debug(
            Object    message,
            Object    toDump )
    {
        if( message instanceof Throwable ) {
            debug( message, toDump, (Throwable) message );
        } else {
            debug( message, toDump, null );
        }
    }

    /**
     * This logs a debug message. The behavior of this method is slightly different depending
     * on the actual Class of the message.
     *
     * @param message the debug Object
     * @param toDump  an Object to dump, if any
     * @param t       the Throwable to log
     */
    public final void debug(
            Object    message,
            Object    toDump,
            Throwable t )
    {
        if( t != null ) {
            logDebug( String.valueOf( message ), toDump, t );
        } else if( logStacktraceOnDebug ) {
            logDebug( String.valueOf( message ), toDump, new Exception( "Logging marker" ) );
        } else { 
            logDebug( String.valueOf( message ), toDump, null );
        }
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

        logFatal( formattedMessage, null, t );

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

        logError( formattedMessage, null, t );

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

        logWarn( formattedMessage, null, t );

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
     * Create a compound message from a message, and an Object to dump.
     * This may be overridden by subclasses.
     *
     * @param msg    the message to log
     * @param toDump an Object to dump, if any
     * @return the compound message
     */
    protected String createCompoundMessage(
            String msg,
            Object toDump )
    {
        if( toDump == null ) {
            return msg;
        }
        if( theDumperFactory == null ) {
            return msg;
        }
        try {
            BufferingDumper dumper = theDumperFactory.obtainFor( this );
            if( dumper == null ) {
                return msg;
            }
            dumper.dump( toDump );
            String append = dumper.getBuffer();
            return msg + " - " + append;

        } catch( FactoryException ex ) {
            error( ex );
            return msg + " - cannot create Dumper";
        }
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
}
