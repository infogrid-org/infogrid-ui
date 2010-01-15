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

import org.infogrid.util.logging.Log;
import java.lang.reflect.Constructor;

/**
 * Methods to easily run a group of subclasses of AbstractTest.
 */
public abstract class AbstractTestGroup
{
    private static final Log log = Log.getLogInstance( AbstractTestGroup.class ); // our own, private logger

    /**
     * Run a list of tests.
     *
     * @param testSpecs the TestSpecs capturing the tests to be run
     */
    protected static void runTests(
            TestSpec [] testSpecs )
    {
        runTests( null, testSpecs );
    }

    /**
     * Run a list of tests.
     *
     * @param comment a comment to print out when running a test
     * @param testSpecs the TestSpecs capturing the tests to be run
     */
    protected static void runTests(
            String      comment,
            TestSpec [] testSpecs )
    {
        for( int i=0 ; i<testSpecs.length ; ++i ) {
            
            int errorCount = testSpecs[i].run( comment );
            if( errorCount != 0 ) {
                Log testClassLog = Log.getLogInstance( testSpecs[i].theTestClass );

                testClassLog.info( "FAIL (" + errorCount + " errors)" );

                System.exit( errorCount );
            }
        }
    }
    
    /**
     * Captures one test.
     */
    public static class TestSpec
    {
        /**
         * Constructor.
         *
         * @param testName the name of the test
         * @param testClass the class containing the test
         * @param testArgs the arguments for the test run
         */
        public TestSpec(
                String                        testName,
                Class<? extends AbstractTest> testClass,
                String ...                    testArgs )
        {
            theTestName  = testName;
            theTestClass = testClass;
            theTestArgs  = testArgs;
        }
        
        /**
         * Constructor.
         *
         * @param testClass the class containing the test
         * @param testArgs the arguments for the test run
         */
        public TestSpec(
                Class<? extends AbstractTest> testClass,
                String ...                    testArgs )
        {
            this( testClass.getName(), testClass, testArgs );
        }
        
        /**
         * Constructor for a test that does not need any arguments.
         *
         * @param testClass the class containing the test
         */
        public TestSpec(
                Class<? extends AbstractTest> testClass )
        {
            this( testClass.getName(), testClass, new String[0] );
        }
        
        /**
         * Run the test described by this TestSpec.
         *
         * @param comment a comment to print out when running a test
         * @return the number of errors that occurred during the test before it was aborted or terminated regularly
         */
        public int run(
                String comment )
        {
            AbstractTest test         = null;
            int          ret          = 0;
            Log          testClassLog = Log.getLogInstance( theTestClass );

            if( testClassLog.isDebugEnabled() ) {
                if( comment != null ) {
                    testClassLog.debug( "Attempting to activate TestSpec " + theTestName + " (" + comment + ")" );
                } else {
                    testClassLog.debug( "Attempting to activate TestSpec " + theTestName );
                }
            }

            try {
                Constructor<? extends AbstractTest> con = theTestClass.getConstructor( new Class[] { String[].class } );

                test = con.newInstance( new Object[] { theTestArgs } );

                if( comment != null ) {
                    log.info( "Running TestSpec " + theTestName + " (" + comment + ")" );
                } else {
                    log.info( "Running TestSpec " + theTestName );
                }
                test.run();
                
                ret = AbstractTest.errorCount;

            } catch( Throwable ex ) {
                log.error( ex );
                ret = 1;
            }
            if( test != null ) {
                test.cleanup();
            }
            return ret;
        }

        /**
         * The name of the test.
         */
        protected String theTestName;
        
        /**
         * The Class of the test.
         */
        protected Class<? extends AbstractTest> theTestClass;
        
        /**
         * The arguments into the test.
         */
        protected String [] theTestArgs;
    }
}
