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

package org.infogrid.module;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;

/**
 * Centralizes error handling within the Module Framework. As the Module Framework
 * cannot expect any logging capabilities to be present, this is fairly minimalist.
 * All methods are statics.
 */
public abstract class ModuleErrorHandler
{
    /**
     * Allow our SoftwareInstallation to set the error output stream.
     *
     * @param stream the PrintStream to print to
     */
    public static void setDebugStream(
            PrintStream stream )
    {
        errorStream = stream;
    }

    /**
     * Print a message to the standard error channel.
     *
     * @param msg the message
     */
    public static void print(
            String msg )
    {
        if( errorStream != null ) {
            errorStream.print( "<message>" );
            errorStream.print( msg );
            errorStream.println( "</message>" );
        }
    }

    /**
     * We are told that an exception occurred, but it's informational only.
     *
     * @param t the Throwable
     */
    public static void informThrowable(
            Throwable t )
    {
        if( errorStream != null ) {
            errorStream.print( "<throwable>" );
            t.printStackTrace( errorStream );
            errorStream.println( "</throwable>" );
        }
    }

    /**
     * We are told that we are about to download a file.
     *
     * @param remoteFile the URL of the file to be downloaded
     * @param localFile the to-be-created file
     */
    public static void informDownloadFile(
            URL  remoteFile,
            File localFile )
    {
        if( errorStream != null ) {
            errorStream.print( "<download remote=\"" );
            errorStream.print( remoteFile.toExternalForm() );
            errorStream.print( "\" local=\"" );
            errorStream.print( localFile.getAbsolutePath() );
            errorStream.println( "\"/>" );
        }
    }

    /**
     * We are told that a class load attempt is about to be started.
     *
     * @param mod the Module within which the class load occurs
     * @param className the name of the Class to be loaded
     */
    public static void informLoadClassAttemptStart(
            Module mod,
            String className )
    {
        if( logClassLoading && errorStream != null ) {
            errorStream.print( "<classloading class=\"" );
            errorStream.print( className );
            errorStream.print( "\" module=\"" );
            errorStream.print(  mod.getModuleName() );
            errorStream.print( "\" version=\"" );
            errorStream.print(  mod.getModuleVersion() );
            errorStream.println( "\">" );
        }
    }

    /**
     * We are told that a class load attempt has failed.
     *
     * @param mod the Module within which the class load occurs
     * @param className the name of the Class to be loaded
     */
    public static void informLoadClassAttemptFailed(
            Module mod,
            String className )
    {
        if( logClassLoading && errorStream != null ) {
            errorStream.print( " <failed>" );
            errorStream.print( "Class load failed. Class: " );
            errorStream.print( className );
            errorStream.print( ", Module: " );
            errorStream.print( mod.getModuleName() );
            errorStream.print( ", version: " );
            errorStream.print( mod.getModuleVersion() );
            errorStream.println( " </failed>" );
            errorStream.println( "</classloading>" );
        }
    }

    /**
     * We are told that a class load attempt has succeeded
     *
     * @param mod the Module within which the class load occurs
     * @param className the name of the Class to be loaded
     */
    public static void informLoadClassAttemptSucceeded(
            Module mod,
            String className )
    {
        if( logClassLoading && errorStream != null ) {
            errorStream.print( " <succeeded>" );
            errorStream.print( "Class load succeeded. Class: " );
            errorStream.print( className );
            errorStream.print( ", Module: " );
            errorStream.print( mod.getModuleName() );
            errorStream.print( ", version: " );
            errorStream.print( mod.getModuleVersion() );
            errorStream.println( " </succeeded>" );
            errorStream.println( "</classloading>" );
        }
    }

// Modules from here

    /**
     * We are told that that a Module is about to be activated recursively.
     *
     * @param mod the affected Module
     */
    public static void informModuleActivateRecursivelyStart(
            Module mod )
    {
        if( logModuleActivateRecursively && errorStream != null ) {
            errorStream.print( "<moduleactivaterecursively module=\"" );
            errorStream.print(  mod.getModuleName() );
            errorStream.print( "\" version=\"" );
            errorStream.print(  mod.getModuleVersion() );
            errorStream.println( "\">" );
        }
    }

    /**
     * We are told that that a Module activation, recursively, has failed.
     *
     * @param mod the affected Module
     */
    public static void informModuleActivateRecursivelyFailed(
            Module mod )
    {
        if( logModuleActivateRecursively && errorStream != null ) {
            errorStream.println( " <failed/>" );
            errorStream.println( "</moduleactivaterecursively>" );
        }
    }

    /**
     * We are told that that a Module activation, recursively, has succeeded.
     *
     * @param mod the affected Module
     */
    public static void informModuleActivateRecursivelySucceeded(
            Module mod )
    {
        if( logModuleActivateRecursively && errorStream != null ) {
            errorStream.println( " <succeeded/>" );
            errorStream.println( "</moduleactivaterecursively>" );
        }
    }

    /**
     * We are told that a Module activate is about to be started.
     *
     * @param mod the affected ModelModule
     */
    public static void informModuleActivateStart(
            Module mod )
    {
        if( logModuleActivate && errorStream != null ) {
            errorStream.print( "<moduleactivate module=\"" );
            errorStream.print(  mod.getModuleName() );
            errorStream.print( "\" version=\"" );
            errorStream.print(  mod.getModuleVersion() );
            errorStream.println( "\">" );
        }
    }

    /**
     * We are told that a Module activate has failed.
     *
     * @param mod the affected Module
     * @param ex indicates the problem
     */
    public static void informModuleActivateFailed(
            Module    mod,
            Throwable ex )
    {
        if( logModuleActivate && errorStream != null ) {
            errorStream.print( " <failed>");
            errorStream.print( "Module activate failed. Module: " );
            errorStream.print( mod.getModuleName() );
            errorStream.print( ", version: " );
            errorStream.println( mod.getModuleVersion() );
            ex.printStackTrace( errorStream );
            errorStream.println( " </failed>" );
            errorStream.println( "</moduleactivate>");
        }
    }

    /**
     * We are told that a Module activate has succeeded.
     *
     * @param mod the affected Module
     */
    public static void informModuleActivateSucceeded(
            Module mod )
    {
        if( logModuleActivate && errorStream != null ) {
            errorStream.print( " <succeeded>");
            errorStream.print( "Module activate succeeded. Module: " );
            errorStream.print( mod.getModuleName() );
            errorStream.print( ", version: " );
            errorStream.print( mod.getModuleVersion() );
            errorStream.println( " </succeeded>" );
            errorStream.println( "</moduleactivate>");
        }
    }

    /**
     * We are told that a Module activate method is about to be invoked.
     *
     * @param mod the affected Module
     * @param meth the to-be-invoked Method
     */
    public static void informModuleActivate(
            Module mod,
            Method meth )
    {
        if( logModuleActivate && errorStream != null ) {
            errorStream.print( "<moduleactivateactivate module=\"" );
            errorStream.print(  mod.getModuleName() );
            errorStream.print( "\" version=\"" );
            errorStream.print(  mod.getModuleVersion() );
            errorStream.print( "\" method=\"" );
            errorStream.print(  meth );
            errorStream.println( "\"/>" );
        }
    }

    /**
     * We are told that a Module deactivate is about to be started.
     *
     * @param mod the affected Module
     */
    public static void informModuleDeactivateStart(
            Module mod )
    {
        if( logModuleActivate && errorStream != null ) {
            errorStream.print( "<moduledeactivate module=\"" );
            errorStream.print(  mod.getModuleName() );
            errorStream.print( "\" version=\"" );
            errorStream.print(  mod.getModuleVersion() );
            errorStream.println( "\">" );
        }
    }

    /**
     * We are told that a Module deactivate has failed.
     *
     * @param mod the affected Module
     * @param ex indicates the problem
     */
    public static void informModuleDeactivateFailed(
            Module    mod,
            Throwable ex )
    {
        if( logModuleActivate && errorStream != null ) {
            errorStream.print( " <failed>" );
            errorStream.print( "Module deactivate failed. Module: " );
            errorStream.print( mod.getModuleName() );
            errorStream.print( ", version: " );
            errorStream.println( mod.getModuleVersion() );
            ex.printStackTrace( errorStream );
            errorStream.println( " </failed>" );
            errorStream.println( "</moduledeactivate>" );
        }
    }

    /**
     * We are told that a Module deactivate has succeeded.
     *
     * @param mod the affected Module
     */
    public static void informModuleDeactivateSucceeded(
            Module mod )
    {
        if( logModuleActivate && errorStream != null ) {
            errorStream.print( " <succeeded>" );
            errorStream.print( "Module deactivate succeeded. Module: " );
            errorStream.print( mod.getModuleName() );
            errorStream.print( ", version: " );
            errorStream.print( mod.getModuleVersion() );
            errorStream.println( " </succeeded>" );
            errorStream.println( "</moduledeactivate>" );
        }
    }

    /**
     * We are told that a Module deactivate method is about to be invoked.
     *
     * @param mod the affected Module
     * @param meth the to-be-invoked Method
     */
    public static void informModuleDeactivate(
            Module mod,
            Method meth )
    {
        if( logModuleActivate && errorStream != null ) {
            errorStream.print( "<moduleactivatedeactivate module=\"" );
            errorStream.print(  mod.getModuleName() );
            errorStream.print( "\" version=\"" );
            errorStream.print(  mod.getModuleVersion() );
            errorStream.print( "\" method=\"" );
            errorStream.print(  meth );
            errorStream.println( "\"/>" );
        }
    }

    /**
     * We are told that a StandardModule configure is about to be started.
     *
     * @param mod the affected StandardModule
     */
    public static void informModuleConfigureStart(
            StandardModule mod )
    {
        if( logStandardModuleConfiguration && errorStream != null ) {
            errorStream.print( "<moduleconfigure module=\"" );
            errorStream.print(  mod.getModuleName() );
            errorStream.print( "\" version=\"" );
            errorStream.print(  mod.getModuleVersion() );
            errorStream.println( "\">" );
        }
    }

    /**
     * We are told that a StandardModule configure has failed.
     *
     * @param mod the affected StandardModule
     * @param ex indicates the problem
     */
    public static void informModuleConfigureFailed(
            StandardModule mod,
            Throwable       ex )
    {
        if( logStandardModuleConfiguration && errorStream != null ) {
            errorStream.print( " <failed>" );
            errorStream.print( "StandardModule configure failed. StandardModule: " );
            errorStream.print( mod.getModuleName() );
            errorStream.print( ", version: " );
            errorStream.println( mod.getModuleVersion() );
            ex.printStackTrace( errorStream );
            errorStream.println( " </failed>" );
            errorStream.println( "</moduleconfigure>" );
        }
    }

    /**
     * We are told that a StandardModule configure has succeeded.
     *
     * @param mod the affected StandardModule
     */
    public static void informModuleConfigureSucceeded(
            StandardModule mod )
    {
        if( logStandardModuleConfiguration && errorStream != null ) {
            errorStream.print( " <succeeded>" );
            errorStream.print( "StandardModule configure succeeded. StandardModule: " );
            errorStream.print( mod.getModuleName() );
            errorStream.print( ", version: " );
            errorStream.print( mod.getModuleVersion() );
            errorStream.println( " </succeeded>" );
            errorStream.println( "</moduleconfigure>" );
        }
    }

    /**
     * We are told that a StandardModule configure method is about to be invoked.
     *
     * @param mod the affected StandardModule
     * @param meth the to-be-invoked Method
     */
    public static void informModuleConfigure(
            StandardModule mod,
            Method         meth )
    {
        if( logStandardModuleConfiguration && errorStream != null ) {
            errorStream.print( "<moduleconfigureconfigure module=\"" );
            errorStream.print(  mod.getModuleName() );
            errorStream.print( "\" version=\"" );
            errorStream.print(  mod.getModuleVersion() );
            errorStream.print( "\" method=\"" );
            errorStream.print(  meth );
            errorStream.println( "\"/>" );
        }
    }

    /**
     * We are told that a StandardModule run is about to be started.
     *
     * @param mod the affected StandardModule
     */
    public static void informModuleRunStart(
            StandardModule mod )
    {
        if( logStandardModuleRun && errorStream != null ) {
            errorStream.print( "<modulerun module=\"" );
            errorStream.print(  mod.getModuleName() );
            errorStream.print( "\" version=\"" );
            errorStream.print(  mod.getModuleVersion() );
            errorStream.println( "\">" );
        }
    }

    /**
     * We are told that a StandardModule run has failed.
     *
     * @param mod the affected StandardModule
     * @param ex indicates the problem
     */
    public static void informModuleRunFailed(
            StandardModule mod,
            Throwable       ex )
    {
        if( logStandardModuleRun && errorStream != null ) {
            errorStream.print( " <failed>" );
            errorStream.print( "StandardModule configure failed. StandardModule: " );
            errorStream.print( mod.getModuleName() );
            errorStream.print( ", version: " );
            errorStream.print( mod.getModuleVersion() );
            errorStream.println( " </failed>" );
            errorStream.println( "</modulerun>" );
        }
    }

    /**
     * We are told that a StandardModule run has succeeded.
     *
     * @param mod the affected StandardModule
     */
    public static void informModuleRunSucceeded(
            StandardModule mod )
    {
        if( logStandardModuleRun && errorStream != null ) {
            errorStream.print( " <succeeded>" );
            errorStream.print( "StandardModule configure succeeded. StandardModule: " );
            errorStream.print( mod.getModuleName() );
            errorStream.print( ", version: " );
            errorStream.print( mod.getModuleVersion() );
            errorStream.println( " </succeeded>" );
            errorStream.println( "</modulerun>" );
        }
    }

    /**
     * We are told that a StandardModule run method is about to be invoked.
     *
     * @param mod the affected StandardModule
     * @param meth the to-be-invoked Method
     */
    public static void informModuleRun(
            StandardModule mod,
            Method         meth )
    {
        if( logStandardModuleRun && errorStream != null ) {
            errorStream.print( "<modulerunrun module=\"" );
            errorStream.print(  mod.getModuleName() );
            errorStream.print( "\" version=\"" );
            errorStream.print(  mod.getModuleVersion() );
            errorStream.print( "\" method=\"" );
            errorStream.print(  meth );
            errorStream.println( "\"/>" );
        }
    }

    /**
     * Dump the content of the ModuleRegistry.
     *
     * @param reg the ModuleRegistry to dump.
     */
    public static void informModuleRegistry(
            ModuleRegistry reg )
    {
        PrintStream out = errorStream != null ? errorStream : System.err;

        out.println( "ModuleRegistry knows about the following Modules:" );
        Iterator iter = reg.advertisementIterator();
        for( int i=0 ; iter.hasNext() ; ++i ) {
            ModuleAdvertisement adv = (ModuleAdvertisement) iter.next();
            out.printf( "    %3d", i );
            out.print( ": Name: '" );
            out.print( adv.getModuleName() );
            if( adv.getModuleVersion() != null ) {
                out.print( "', Version: '" );
                out.print( adv.getModuleVersion() );
            }
            out.println( "'" );
        }
    }

// Error reporting

    /**
     * Report an unexpected Throwable.
     *
     * @param ex the unexpected Throwable
     */
    public static void error(
            Throwable ex )
    {
        if( errorStream != null ) {
            errorStream.print( "ERROR: unexpected Throwable: " );
            ex.printStackTrace( errorStream );
        }
        if( errorStream != System.err ) {
            System.err.print( "ERROR: unexpected Throwable: " );
            ex.printStackTrace( System.err );
        }
   }

    /**
     * Report an error.
     *
     * @param msg the error
     */
    public static void error(
            String msg )
    {
        if( errorStream != null ) {
            errorStream.print( "ERROR: " );
            errorStream.println( msg );
        }
        if( errorStream != System.err ) {
            errorStream.print( "ERROR: " );
            errorStream.println( msg );
        }
    }

    /**
     * Report a warning
     *
     * @param msg the warning
     */
    public static void warn(
            String msg )
    {
        if( errorStream != null ) {
            errorStream.print( "WARN: " );
            errorStream.println( msg );
        }
        if( errorStream != System.err ) {
            System.err.print( "WARN: " );
            System.err.println( msg );
        }
    }

    /**
     * Report a warning
     *
     * @param msg the warning message, if any
     * @param ex the Exception, if any
     */
    public static void warn(
            String    msg,
            Exception ex )
    {
        if( errorStream != null ) {
            errorStream.print( "WARN: " );
            errorStream.println( msg );
            ex.printStackTrace( errorStream );
        }
        if( errorStream != System.err ) {
            System.err.print( "WARN: " );
            System.err.println( msg );
            ex.printStackTrace( System.err );
        }
    }

    /**
     * Report an unexpected Exception that indicates an internal error
     *
     * @param ex the unexpected Exception
     */
    public static void internalError(
            Exception ex )
    {
        if( errorStream != null ) {
            errorStream.print( "INTERNAL ERROR: unexpected Exception: " );
            ex.printStackTrace( errorStream );
        }
        if( errorStream != System.err ) {
            System.err.print( "INTERNAL ERROR: unexpected Exception: " );
            ex.printStackTrace( System.err );
        }
    }

    /**
     * Report a fatal error.
     *
     * @param msg the error message, if any
     * @param ex the thrown Throwable, if any
     * @param theInstallation the SoftwareInstallation, if any
     */
    public static void fatal(
            String               msg,
            Throwable            ex,
            SoftwareInstallation theInstallation )
    {
        String      postfix           = "This is a fatal error and the application needs to quit.";
        boolean     showAllExceptions = true; // false
        String      userMessage;

        // Turns out that JDK 1.4 and later prints not only the stack trace of the Exception
        // but also the stack traces of all the Exceptions via the delegated Exception facility
        // (getCause()) method.
        // We need to do that manually for earlier JDK versions.
        // In order to distinguish, we look for the existence of the getCause() method on Throwable.
        // (somehow Sun does not seem to allow us to override the printStackTrace() method
        // to not print out the stack trace).

        if( msg != null ) {
            userMessage = msg;
        } else {
            userMessage = "";
        }

        if( ex != null ) {
            boolean printDelegatedExceptions = true;
            try {
                /* Method m = */ Throwable.class.getDeclaredMethod( "getCause", (Class []) null );

                printDelegatedExceptions = false;
            } catch( Throwable t ) {
                // no op
            }

            if( !printDelegatedExceptions ) {
                if( errorStream != null ) {
                    ex.printStackTrace( errorStream );
                }
                if( errorStream != System.err ) {
                    ex.printStackTrace( System.err );
                }
            } else {
                outer:
                while( true ) {
                    if( showAllExceptions ) {
                        if( errorStream != null ) {
                            ex.printStackTrace( errorStream );
                        }
                        if( errorStream != System.err ) {
                            ex.printStackTrace( System.err );
                        }
                    }

                    for( Class<?> c = ex.getClass() ; c != Object.class ; c = c.getSuperclass() ) {

                        Method m = null;

                        try {
                            m = c.getDeclaredMethod( "getCause", (Class []) null );
                        } catch( Throwable ex2 ) {
                            // do nothing, keep trying
                        }

                        if( m == null ) {
                            try {
                                m = c.getDeclaredMethod( "getTargetException", (Class []) null );
                            } catch( Throwable ex2 ) {
                                // do nothing, keep trying
                            }
                        }

                        if( m != null ) {
                            try {
                                Throwable delegate = (Throwable) m.invoke( ex, (Object []) null );
                                if( delegate != null && delegate != ex ) {
                                    ex = delegate;
                                    continue outer;
                                } else {
                                    break outer;
                                }

                            } catch( Throwable ex2 ) {
                                // do nothing, keep trying
                            }
                        }
                    }
                    // here we are at the end of the stick
                    break;
                }
            }
            if( ex.getMessage() != null && ex.getMessage().length() > 0 ) {
                userMessage += "ERROR MESSAGE: " + ex.getMessage();
            } else {
                userMessage += "ERROR CLASS: " + ex.getClass().getName();
            }

        } else {
            if( userMessage.length() > 0 ) {
                userMessage += "\n";
            }
            userMessage += "Unknown condition";
        }

        if( errorStream != null ) {
            errorStream.println( userMessage );
            errorStream.println( postfix );
        }
        if( errorStream != System.err ) {
            System.err.println( userMessage );
            System.err.println( postfix );
        }

        if( theInstallation != null && ! theInstallation.isErrorTextOnly() ) {
            // try Swing first, then AWT
            try {
                Class<?> optionPaneClass = Class.forName( "javax.swing.JOptionPane" );
                Method showMessageDialogMessage = optionPaneClass.getMethod(
                        "showMessageDialog",
                        new Class[] {
                            Class.forName( "java.awt.Component" ),
                            Object.class,
                            String.class,
                            Integer.TYPE } );

                showMessageDialogMessage.invoke(
                        null,
                        new Object[] {
                                null,
                                userMessage,
                                "Fatal error",
                                new Integer( 0 ) // JOptionPane.ERROR_MESSAGE
                        } );
            } catch( Exception ex2 ) {
                System.err.println( "Exception2: " );
                ex2.printStackTrace();
                // FIXME: add AWT error message
            }
        }
    }

    /**
     * Indicates whether we attempt to log class loading.
     */
    private static boolean logClassLoading = false; // false;

    /**
     * Indicates whether we attempt to log recursive Module activation.
     */
    private static boolean logModuleActivateRecursively = false; // false;

    /**
     * Indicates whether we attempt to log Module activation.
     */
    private static boolean logModuleActivate = false; // false;

    /**
     * Indicates whether we attempt to log Module deactivation.
     */
    private static boolean logModuleDeactivate = false; // false;

    /**
     * Indicates whether we attempt to log StandardModule run.
     */
    private static boolean logStandardModuleRun = false; // false;

    /**
     * Indicates whether we attempt to log StandardModule configuration.
     */
    private static boolean logStandardModuleConfiguration = false; // false;

    /**
     * The PrintStream to which we print errors. Default is standard error.
     */
    private static PrintStream errorStream = System.err;
}
