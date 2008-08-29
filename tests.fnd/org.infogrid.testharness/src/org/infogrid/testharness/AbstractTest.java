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

package org.infogrid.testharness;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.infogrid.module.ModuleClassLoader;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.logging.log4j.Log4jLog;

/**
 * An abstract superclass for tests. It provides a bunch of generic test
 * harness infrastructure.
 */
public abstract class AbstractTest
{
    /**
     * Constructor if we don't have a special resource file.
     *
     * @param nameOfLog4jConfigFile the name of the log4j config file
     */
    protected AbstractTest(
            String nameOfLog4jConfigFile )
    {
        this( null, nameOfLog4jConfigFile );
    }

    /**
     * Constructor if we have a special resource file.
     *
     * @param nameOfResourceHelperFile the name of the resource file
     * @param nameOfLog4jConfigFile the name of the log4j config file
     */
    protected AbstractTest(
            String nameOfResourceHelperFile,
            String nameOfLog4jConfigFile )
    {
        // first resource helper, then logger
        if( nameOfResourceHelperFile != null ) {
            try {
                ResourceHelper.setApplicationResourceBundle( ResourceBundle.getBundle(
                        nameOfResourceHelperFile,
                        Locale.getDefault(),
                        getClass().getClassLoader() ));

            } catch( Exception ex ) {
                System.err.println( "Unexpected Exception attempting to load " + nameOfResourceHelperFile );
                ex.printStackTrace( System.err );
            }
        }

        try {
            Properties logProperties = new Properties();
            logProperties.load( new BufferedInputStream(
                    getClass().getClassLoader().getResourceAsStream( nameOfLog4jConfigFile )));

            Log4jLog.configure( logProperties );
            // which logger is being used is defined in the module dependency declaration through parameters

        } catch( Throwable ex ) {
            System.err.println( "Unexpected Exception attempting to load " + nameOfLog4jConfigFile );
            ex.printStackTrace( System.err );
        }

        ResourceHelper.initializeLogging();

        log = Log.getLogInstance( AbstractTest.class );
    }

    /**
     * Construct the fully-qualified file name of a file relative to a Class.
     * 
     * @param clazz the Class
     * @param relative the relative file name
     * @return the fully-qualified file name
     */
    protected static String thisPackage(
            Class<?> clazz,
            String   relative )
    {
        String clazzName = clazz.getName();
        int    period    = clazzName.lastIndexOf( '.' );
        
        StringBuilder ret = new StringBuilder();
        ret.append( clazzName.substring( 0, period+1 ).replace( '.', '/' ));
        ret.append( relative );
        return ret.toString();
    }

    /**
     * Run the test.
     *
     * @throws Throwable thrown if an Exception occurred during the test
     */
    public abstract void run()
        throws
            Throwable;

    /**
     * Clean up after test. Subclasses often override this.
     */
    public void cleanup()
    {
    }

    /**
     * Report error if the arguments are not equal.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments aren't equal
     * @return true if check passed
     */
    protected final boolean checkEquals(
            Object one,
            Object two,
            String msg )
    {
        if( one == null ) {
            if( two == null ) {
                return true;
            } else {
                reportError( msg, "null vs. \"" + two + "\" (class: " + two.getClass().getName() + ")" );
                return false;
            }
        }
        if( one.equals( two )) {
            return true;
        } else {
            reportError( msg, "\"" + one + "\" (class: " + one.getClass().getName() + ") vs. " + ( (two==null) ? "null" : ( "\"" + two + "\" (class: " + two.getClass().getName() + ")" )) );
            return false;
        }
    }

    /**
     * Report error if the arguments are equal.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments are equal
     * @return true if check passed
     */
    protected final boolean checkNotEquals(
            Object one,
            Object two,
            String msg )
    {
        if( one == null ) {
            if( two != null ) {
                return true;
            } else {
                reportError( msg, "null vs. \"" + two + "\" (class: " + two.getClass().getName() + ")" );
                return false;
            }
        }
        if( !one.equals( two )) {
            return true;
        } else {
            reportError( msg, "\"" + one + "\" (class: " + one.getClass().getName() + ") vs. " + ( (two==null) ? "null" : ( "\"" + two + "\" (class: " + two.getClass().getName() + ")" )) );
            return false;
        }
    }

    /**
     * Report error if the arguments are not equal.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments aren't equal
     * @return true if check passed
     */
    protected final boolean checkEquals(
            boolean one,
            boolean two,
            String msg )
    {
        if( one != two ) {
            reportError( msg, "\"" + one + "\" vs. \"" + two + "\" " );
            return false;
        }
        return true;
    }

    /**
     * Report error if the arguments are equal.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments are equal
     * @return true if check passed
     */
    protected final boolean checkNotEquals(
            boolean one,
            boolean two,
            String msg )
    {
        if( one == two ) {
            reportError( msg, "\"" + one + "\" vs. \"" + two + "\" " );
            return false;
        }
        return true;
    }

    /**
     * Report error if the arguments are not equal.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments aren't equal
     * @return true if check passed
     */
    protected final boolean checkEquals(
            long   one,
            long   two,
            String msg )
    {
        if( one != two ) {
            reportError( msg, "\"" + one + "\" vs. \"" + two + "\" " );
            return false;
        }
        return true;
    }

    /**
     * Report error if the arguments are not equal.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments aren't equal
     * @return true if check passed
     */
    protected final boolean checkEqualByteArrays(
            byte [] one,
            byte [] two,
            String msg )
    {
        if( one == null ) {
            if( two != null ) {
                reportError( msg, "null vs. non-null" );
                return false;
            } else {
                return true;
            }
        } else if( two == null ) {
            reportError( msg, "non-null vs. null" );
            return false;
        } else {
            int min = Math.min( one.length, two.length );
            for( int i=0 ; i<min ; ++i ) {
                if( one[i] != two[i] ) {
                    reportError( msg, "byte arrays differ at index " + i + ": " + Integer.toHexString( one[i] ) + " vs. " + Integer.toHexString( two[i] ));
                    return false;
                }
            }
            if( one.length > two.length ) {
                reportError( msg, "first byte[] longer than second (but a subset)" );
                return false;
            } else if( one.length < two.length ) {
                reportError( msg, "first byte[] shorter than second (but a subset)" );
                return false;
            }
        }
        return true;
    }

    /**
     * Report error if the arguments are equal.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments are equal
     * @return true if check passed
     */
    protected final boolean checkNotEquals(
            long   one,
            long   two,
            String msg )
    {
        if( one == two ) {
            reportError( msg, "\"" + one + "\" vs. \"" + two + "\" " );
            return false;
        }
        return true;
    }

    /**
     * Report error if the arguments do not have the same content in the same sequence.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments aren't equal
     * @return true if check passed
     */
    protected final boolean checkEqualsInSequence(
            Object [] one,
            Object [] two,
            String    msg )
    {
        boolean ret = true;

        int length = one.length;
        if( length != two.length ) {
            ret = false;
            reportError( msg, "different length: " + one.length + " vs. " + two.length );
            if( length > two.length ) {
                length = two.length;
            }
        }

        for( int i=0 ; i<length ; ++i ) {
            if( one[i] == null ) {
                if( two[i] != null ) {
                    ret = false;
                    reportError( msg, "element at index " + i + " is different: null vs. " + two[i] );
                }
            } else if( !one[i].equals( two[i] )) {
                ret = false;
                reportError( msg, "element at index " + i + " is different: " + one[i] + " vs. " + two[i] );
            }
        }
        return ret;
    }

    /**
     * Report error if the arguments do not have the same content in the same sequence.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments aren't equal
     * @return true if check passed
     */
    protected final boolean checkEqualsInSequence(
            List<?>   one,
            Object [] two,
            String    msg )
    {
        boolean ret = true;

        if( one == null ) {
            if( two != null ) {
                reportError( msg, "null vs. non-null: null vs. " + two );
                ret = false;
            }
        } else if( two == null ) {
            reportError( msg, "non-null vs. null: " + one + " vs. null" );
            ret = false;
        } else {
            int length = one.size();
            if( length != two.length ) {
                ret = false;
                reportError( msg, "different length: " + one.size() + " vs. " + two.length );
                if( length > two.length ) {
                    length = two.length;
                }
            }

            for( int i=0 ; i<length ; ++i ) {
                if( one.get( i ) == null ) {
                    if( two[ i ] != null ) {
                        ret = false;
                        reportError( msg, "element at index " + i + " is different: null vs. " + two[i]);
                    }
                } else if( !one.get( i ).equals( two[i] )) {
                    ret = false;
                    reportError( msg, "element at index " + i + " is different: " + one.get( i ) + " vs. " + two[i] );
                }
            }
        }
        return ret;
    }

    /**
     * Report error if the arguments do not have the same content in any sequence.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments aren't equal
     * @return true if check passed
     */
    protected final boolean checkEqualsOutOfSequence(
            Object [] one,
            Object [] two,
            String    msg )
    {
        boolean ret = ArrayHelper.hasSameContentOutOfOrder( one, two, true );
        if( !ret ) {
            reportError( msg, "not the same content" );
        }
        return ret;
    }

    /**
     * Report error if the arguments are not identical.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments aren't identical
     * @return true if check passed
     */
    protected final boolean checkIdentity(
            Object one,
            Object two,
            String msg )
    {
        if( one != two ) {
            reportError( msg, one + " vs. " + two );
            return false;
        }

        return true;
    }

    /**
     * Report error if the first argument does not start with the second argument String
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @param msg message to print when arguments aren't equal
     * @return true if check passed
     */
    protected final boolean checkStartsWith(
            String one,
            String two,
            String msg )
    {
        if( one == null ) {
            if( two == null ) {
                return true;
            } else {
                reportError( msg, "null vs. \"" + two + "\" (class: " + two.getClass().getName() + ")" );
                return false;
            }
        }
        if( one.startsWith( two )) {
            return true;
        } else {
            reportError( msg, "\"" + one + "\" (class: " + one.getClass().getName() + ") vs. " + ( (two==null) ? "null" : ( "\"" + two + "\" (class: " + two.getClass().getName() + ")" )) );
            return false;
        }
    }

    /**
     * Report error if the argument is null.
     *
     * @param one the object to test
     * @param msg message to print when the argument is null
     * @return true if check passed
     */
    protected final boolean checkObject(
            Object one,
            String msg )
    {
        if( one == null ) {
            reportError( msg );
            return false;
        }

        return true;
    }

    /**
     * Report error if the object is not of a certain type.
     *
     * @param one the object whose type we test
     * @param typeOne the type against which we test
     * @param msg message to print when the object is not an instance of type
     * @return true if check passed
     */
    protected final boolean checkType(
            Object one,
            Class  typeOne,
            String msg )
    {
        if( one == null ) {
            reportError( msg, "object is null" );
            return false;
        }
        if( !typeOne.isInstance( one )) {
            reportError( msg, "object is instance of " + one.getClass().getName() + ", not of " + typeOne.getName() );
            return false;
        }
        return true;
    }

    /**
     * Report error if the condition is false.
     *
     * @param condition the condition
     * @param msg message to print when the condition is false
     * @return true if check passed
     */
    protected final boolean checkCondition(
            boolean condition,
            String  msg )
    {
        if( ! condition ) {
            reportError( msg );
        }
        return condition;
    }

    /**
     * Report error if the tested number is outside of a certain range.
     *
     * @param test the number to test
     * @param median the median of the range against which the number is being tested
     * @param plusminus half the length of the range against which the number is being tested
     * @param msg message to print
     * @return true if check passed
     */
    protected final boolean checkInPlusMinusRange(
            long   test,
            long   median,
            long   plusminus,
            String msg )
    {
        if( test < median - plusminus ) {
            reportError( msg, String.valueOf( test ) + " is outside of " + median + " +/- " + plusminus );
            return false;
        }
        if( test > median + plusminus ) {
            reportError( msg, String.valueOf( test ) + " is outside of " + median + " +/- " + plusminus );
            return false;
        }
        return true;
    }

    /**
     * Report error if the tested numbers are outside of a certain range.
     *
     * @param tests the sequence of numbers to test
     * @param medians the sequence of medians against which the numbers are being tested
     * @param jitter the amount of jitter allowed
     * @param margin the percentage by which the test numbers may be off
     * @param msg message to print
     * @return true if check passed
     */
    protected final boolean checkInMarginRange(
            long [] tests,
            long [] medians,
            long    jitter,
            double  margin,
            String  msg )
    {
        return checkInMarginRange( tests, medians, jitter, margin, 0, msg );
    }

    /**
     * Report error if the tested numbers are outside of a certain range.
     *
     * @param tests the sequence of numbers to test
     * @param medians the sequence of medians against which the numbers are being tested
     * @param jitter the amount of jitter allowed
     * @param margin the percentage by which the test numbers may be off
     * @param offset a static offset between the units in which medians and tests are measured
     * @param msg message to print
     * @return true if check passed
     */
    protected final boolean checkInMarginRange(
            long [] tests,
            long [] medians,
            long    jitter,
            double  margin,
            long    offset,
            String  msg )
    {
        if( getLog().isDebugEnabled() ) {
            long [] adjustedTests = new long[ tests.length ];
            for( int i=0 ; i<tests.length ; ++i ) {
                adjustedTests[i] = tests[i] - offset;
            }
            StringBuilder buf = new StringBuilder();
            buf.append( "checkInMarginRange( " );
            buf.append( ArrayHelper.arrayToString( adjustedTests ));
            buf.append( ", " );
            buf.append( ArrayHelper.arrayToString( medians ));
            buf.append( ", " );
            buf.append( jitter );
            buf.append( ", " );
            buf.append( margin );
            getLog().debug( buf.toString() );
        }
        
        boolean ret = true;
        int length = tests.length;
        if( length != medians.length ) {
            ret = false;
            reportError( msg, "different length: " + tests.length + " vs. " + medians.length );
            if( length > medians.length ) {
                length = medians.length;
            }
        }
        
        for( int i=0 ; i<length ; ++i ) {
            if( tests[i] < medians[i]*(1.-margin) + offset - jitter || tests[i] > medians[i]*(1.+margin ) + offset + jitter ) {                    
                ret = false;
                reportError( msg, "element at index " + i + " is outside of range: " + (tests[i] - offset) + " vs. " + medians[i] + " +/- " + jitter + " +/- " + ( margin*100.) + "% (offset: " + offset + ", jitter: " + jitter + ")" );
            }
        }
        return ret;
    }
            
    /**
     * Report error if the tested number is outside of a certain range.
     *
     * @param test the number to test
     * @param min the lower end of the range
     * @param max the higher end of the range
     * @param msg message to print
     * @return true if check passed
     */
    protected final boolean checkInRange(
            long   test,
            long   min,
            long   max,
            String msg )
    {
        if( test < min ) {
            reportError( msg, String.valueOf( test ) + " is outside of [ " + min + ", " + max + " ] (by " + (min-test) + ")" );
            return false;
        }
        if( test > max ) {
            reportError( msg, String.valueOf( test ) + " is outside of [ " + min + ", " + max + " ] (by " + (test-max) + ")" );
            return false;
        }
        return true;
    }

    /**
     * Report error if a set contains duplicates.
     * 
     * @param iter iterator over the set
     * @param useEquals if true, use the equals method to determine equality; identity otherwise
     * @param msg message to print
     * @return true if check passed
     */
    protected final boolean checkNoDuplicates(
            Iterator<?> iter,
            boolean     useEquals,
            String      msg )
    {
        ArrayList<Object> found = new ArrayList<Object>();
        
        while( iter.hasNext() ) {
            Object current = iter.next();
            
            for( Object currentFound : found ) {
                if( useEquals ) {
                    if( currentFound == null ) {
                        if( current == null ) {
                            reportError( msg, "Null value contained at least twice" );
                            return false;
                        }
                    } else if( currentFound.equals( current )) {
                        reportError( msg, "Object " + current + " contained at least twice" );
                        return false;
                    }
                } else {
                    if( currentFound == found ) {
                        reportError( msg, "Object " + current + " contained at least twice" );
                        return false;
                    }
                }
            }
            found.add( current );
        }
        return true;
    }

    /**
     * Report error.
     *
     * @param msg message to print
     * @param explanation an explanation for this error
     * @return true if check passed
     */
    protected final boolean reportError(
            String msg,
            String explanation )
    {
        if( msg != null ) {
            getLog().error( msg + ": " + explanation );
        }
        ++errorCount;
        return false;
    }

    /**
     * Report error.
     *
     * @param msg message to print
     * @param t the Throwable indicating the error
     * @return true if check passed
     */
    protected final boolean reportError(
            String    msg,
            Throwable t )
    {
        if( msg != null ) {
            getLog().error( msg, t );
        }
        ++errorCount;
        return false;
    }

    /**
     * Report error.
     *
     * @param msg message to print
     * @return true if check passed
     */
    protected final boolean reportError(
            String msg )
    {
        if( msg != null ) {
            getLog().error( msg );
        }
        ++errorCount;
        return false;
    }

    /**
     * This starts a relative timer.
     * 
     * @return the time at which the clock was started, in System.currentTimeMillis() format
     */
    protected final long startClock()
    {
        startTime = System.currentTimeMillis();
        getLog().info( "Starting clock -- it is now " + startTime );
        
        return startTime;
    }

    /**
     * Obtain the absolute start time.
     *
     * @return the time at which the clock was started, in System.currentTimeMillis() format
     */
    public final long getStartTime()
    {
        return startTime;
    }

    /**
     * Sleep for a specified number of milliseconds.
     *
     * @param delta the number of milliseconds to wait
     * @throws InterruptedException thrown when another thread interrupts this Thread while sleeping
     */
    protected final void sleepFor(
            long delta )
        throws
            InterruptedException
    {
        Thread.sleep( delta );
    }

    /**
     * This puts the invoking Thread to sleep until a specified relative time
     * (in milli-seconds) from the time startClock() was called.
     *
     * @param relativeTimeInMillis the relative time by which this Thread is supposed to wake up
     * @throws InterruptedException thrown if the Thread was interrupted
     */
    protected final void sleepUntil(
            long relativeTimeInMillis )
        throws
            InterruptedException
    {
        long current = System.currentTimeMillis();

        getLog().info( "sleeping until relative time: " + relativeTimeInMillis + " (it is now: relative: " + (current-startTime) + " / absolute: " + current + " )" );

        long delta = relativeTimeInMillis - (current - startTime);

        try {
            Thread.sleep( delta );
        } catch( IllegalArgumentException ex ) {
            getLog().error(
                    "Wait time has passed already: current relative time: "
                    + (current-startTime)
                    + " vs. "
                    + relativeTimeInMillis
                    + " absolute: "
                    + current );
        }
    }

    /**
     * Obtain the current time relative to the time startClock() was called.
     *
     * @return the current time relative to the time startClock() was called
     */
    protected final long getRelativeTime()
    {
        long current = System.currentTimeMillis();

        return current - startTime;
    }

    /**
     * Sleep until a Reference has been cleared, or the maximum amount of time, whatever comes first.
     * 
     * @param ref the Reference that needs to be cleared
     * @param max the maximum number of milliseconds
     * @param msg the error message is the object is still around after max milliseconds
     * @throws InterruptedException thrown if the Thread was interrupted
     */
    protected void sleepUntilIsGone(
            Reference<?> ref,
            long         max,
            String       msg )
        throws
            InterruptedException
    {
        if( ref.get() == null ) {
            return;
        }

        long end = System.currentTimeMillis() + max;

        System.gc();

        while( ref.get() != null && System.currentTimeMillis() < end ) {
            Thread.sleep( 50L );
            System.gc();
        }
        if( ref.get() != null ) {
            reportError( msg );
        }
    }

    /**
     * Helper method that allows tests to use local file names for files in the JAR file.
     * 
     * @param testClass the test class to which the file is local
     * @param file the local file name
     * @return the file name relative to the project directory
     */
    public static String localFileName(
            Class<?> testClass,
            String   file )
    {
        // this is relative to the class loader
        StringBuilder ret = new StringBuilder();

        String name = testClass.getName();
        String packageName = name.substring( 0, name.lastIndexOf( '.' ));
        ret.append( packageName.replace( '.', '/' ));
        if( !file.startsWith( "/" )) {
            ret.append( "/" );
        }
        ret.append( file );
        return ret.toString();
    }

    /**
     * Helper method that allows tests to use local file names for files in the file system.
     * 
     * @param testClass the test class to which the file is local
     * @param file the local file name
     * @return the file name relative to the project directory
     */
    public static String fileSystemFileName(
            Class<?> testClass,
            String   file )
    {
        // this is relative to the class loader
        StringBuilder ret = new StringBuilder();

        File dir = determineModuleDirectoryFrom( testClass.getClassLoader() );
        if( dir != null ) {
            ret.append( dir.getAbsolutePath() );
            ret.append( File.separatorChar );
        }
        ret.append( "src/" );

        String name = testClass.getName();
        String packageName = name.substring( 0, name.lastIndexOf( '.' ));
        ret.append( packageName.replace( '.', '/' ));
        if( !file.startsWith( "/" )) {
            ret.append( "/" );
        }
        ret.append( file );
        return ret.toString();
    }

    /**
     * Helper method to determine the location of a temporary input file.
     * 
     * @param testClass the test class that needs a temporary input file
     * @param file the local file name
     * @return the file name relative to the project directory
     */
    public static String tempInputFileName(
            Class<?> testClass,
            String   file )
    {
        StringBuilder ret = new StringBuilder();
        File dir = determineModuleDirectoryFrom( testClass.getClassLoader() );
        if( dir != null ) {
            ret.append( dir.getAbsolutePath() );
            ret.append( File.separatorChar );
        }
        ret.append( "build" );
        if( !file.startsWith( "/" )) {
            ret.append( "/" );
        }
        ret.append( file );
        return ret.toString();
    }

    /**
     * Determine the directory of a Module by deriving it from the class loader of a
     * class that belongs to the Momdule.
     * 
     * @param loader the ClassLoader
     * @return the directory
     */
    public static File determineModuleDirectoryFrom(
            ClassLoader loader )
    {
        File ret;
        if( loader instanceof ModuleClassLoader ) {
            ModuleClassLoader realLoader = (ModuleClassLoader) loader;
            
            ret = realLoader.getModule().getModuleDirectory();

        } else {
            Log.getLogInstance( AbstractTest.class ).warn( "Not a ModuleClassLoader: " + loader );
            ret = null;
        }
        return ret;
    }
    /**
     * Helper smart factory method to create an XML parser.
     *
     * @return the XML DocumentBuilder
     * @throws ParserConfigurationException thrown if the parser cannot be created
     */
    protected DocumentBuilder createXmlParser()
            throws
                ParserConfigurationException
    {
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

        DocumentBuilderFactory theDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        theDocumentBuilderFactory.setNamespaceAware( true );

        theDocumentBuilderFactory.setValidating( false );
        theDocumentBuilderFactory.setIgnoringComments( true );
        theDocumentBuilderFactory.setIgnoringElementContentWhitespace( true );

        return theDocumentBuilderFactory.newDocumentBuilder();
    }

    /**
     * This converts the current stack trace into a string.
     *
     * @return the current stack trace as string
     */
    public static final String stackTraceToString()
    {
        StringWriter theStringWriter = new StringWriter();
        PrintWriter  thePrintWriter  = new PrintWriter( theStringWriter );
        new Exception().printStackTrace( thePrintWriter );
        thePrintWriter.close();
        return theStringWriter.toString();
    }

    /**
     * Invoke the garbage collector.
     */
    protected final void collectGarbage()
    {
        try {
            for( int i=0 ; i<20 ; ++i ) {
                System.gc();
                Thread.sleep( 50L );
            }
        } catch( InterruptedException ex ) {
            reportError( "unexpected interruption of thread", ex );
        }
    }

    /**
     * This helper copies files for us. It waits for one second, in order to get around
     * the one-second delay resolution for File.lastModified() -- at least on MacOSX.
     *
     * @param from file name of the source file
     * @param to   file name of the destination file
     * @throws IOException accessing one of the two files failed
     */
    protected final void copyFile(
            String from,
            String to )
        throws
            IOException
    {
        File fromFile = new File( from );
        File toFile   = new File( to );

        getLog().info( "copying " + fromFile.getCanonicalPath() + " to " + toFile.getCanonicalPath() );

        try {
            Thread.sleep( 1010L ); // we need to sleep some in order to prevent that OSX considers the file unchanged
                                   // because the time resolution on the lastModified stamp is seconds only
        } catch( InterruptedException ex ) {
            log.error( ex );
        }

        if( false ) {
            Runtime.getRuntime().exec( "cp " + fromFile.getCanonicalPath() + " " + toFile.getCanonicalPath() );

        } else {
            if( !fromFile.exists() ) {
                throw new IOException( "Cannot copy from file " + fromFile.getCanonicalPath() + ": does not exist" );
            }
            if( fromFile.isDirectory() ) {
                throw new IOException( "Cannot copy from file " + fromFile.getCanonicalPath() + ": is a directory" );
            }
            if( !fromFile.canRead() ) {
                throw new IOException( "Cannot copy from file " + fromFile.getCanonicalPath() + ": cannot read" );
            }
            if( !toFile.getParentFile().isDirectory() ) {
                throw new IOException( "Cannot copy to file " + toFile.getCanonicalPath() + ": enclosing directory does not exist" );
            }
            if( toFile.exists() ) {
                if( !toFile.canWrite() ) {
                    throw new IOException( "Cannot copy to file " + toFile.getCanonicalPath() + ": cannot write" );
                }
            } else  if( !toFile.getParentFile().canWrite() ) {
                throw new IOException( "Cannot copy to file " + toFile.getCanonicalPath() + ": cannot write to enclosing directory" );
            }
            InputStream  fromStream = new FileInputStream( fromFile );
            OutputStream toStream   = new FileOutputStream( toFile );
            int c;

            while( ( c=fromStream.read()) >= 0 ) {
                toStream.write( c );
            }
            toStream.flush();
            fromStream.close();
            toStream.close();
        }
    }

    /**
     * This checks whether the content of these two files is the same.
     *
     * @param one the first file to compare
     * @param two the second file to compare
     * @param msg the message to print if the files do not have the same content
     * @return true if the test passed
     */
    protected final boolean checkFileDiff(
            File   one,
            File   two,
            String msg )
    {
        boolean ret = true;

        if( ! one.exists() ) {
            reportError( msg, "File does not exist: " + one );
            ret = false;
        }
        if( ! two.exists() ) {
            reportError( msg, "File does not exist: " + two );
            ret = false;
        }
        if( !ret ) {
            return ret;
        }
        try {
            InputStream oneStream = new BufferedInputStream( new FileInputStream( one ));
            InputStream twoStream = new BufferedInputStream( new FileInputStream( two ));

            while( true ) {
                int v1 = oneStream.read();
                int v2 = twoStream.read();

                if( v1 != v2 ) {
                    reportError( msg, "file content is different" );
                    ret = false;
                    break;
                }
                if( v1 <= 0 ) {
                    break;
                }
            }

            oneStream.close();
            twoStream.close();

        } catch( Exception ex ) {
            getLog().error( ex );
            ret = false;
        }
        return ret;
    }

    /**
     * Obtain the Log for this subclass.
     *
     * @return the Log for this subclass
     */
    protected Log getLog()
    {
        return Log.getLogInstance( getClass() );
    }

    /**
     * Obtain a Thread pool for a test.
     * 
     * @param testName name of the test, used to label the Threads created by the ThreadFactory
     * @param nThreads the number of threads in the thread pool
     * @return the created ScheduledExecutorService
     */
    protected ScheduledExecutorService createThreadPool(
            String testName,
            int    nThreads )
    {
        TestThreadFactory factory = new TestThreadFactory( testName );

        return new ScheduledThreadPoolExecutor( nThreads, factory );
    }

    /**
     * Obtain a Thread pool for a test.
     * 
     * @param nThreads the number of threads in the thread pool
     * @return the created ScheduledExecutorService
     */
    protected ScheduledExecutorService createThreadPool(
            int nThreads )
    {
        Class<? extends AbstractTest> testClass = getClass();
        
        return createThreadPool( testClass.getName(), nThreads );
    }

    // Our Logger
    private Log log;

    /**
     * The absolute time in millis when the timer was started.
     */
    protected long startTime;

    /**
     * The number of errors we have encountered up to now.
     */
    protected static int errorCount = 0;

    /**
     * Customized ThreadFactory for better error reporting.
     */
    static class TestThreadFactory
            implements
                ThreadFactory
    {
        /**
         * Constructor.
         * 
         * @param testName name of the test, used to label the Threads created by the ThreadFactory
         */
        public TestThreadFactory(
                String testName )
        {
            thePrefix = testName + "-";
        }
        
        /**
         * Constructs a new <tt>Thread</tt>.  Implementations may also initialize
         * priority, name, daemon status, <tt>ThreadGroup</tt>, etc.
         *
         * @param r a runnable to be executed by new thread instance
         * @return constructed thread
         */
        public Thread newThread(
                Runnable r )
        {
            Thread ret = new Thread( r, thePrefix + theCounter++ );
            return ret;
        }

        /**
         * The Prefix for the name of created Threads.
         */
        protected String thePrefix;
        
        /**
         * The current counter of created Threads.
         */
        protected int theCounter = 0;
    }    
}
