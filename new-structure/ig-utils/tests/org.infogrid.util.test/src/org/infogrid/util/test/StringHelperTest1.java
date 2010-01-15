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

package org.infogrid.util.test;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * There is no point in invoking this test from the test scripts.
 * This is a helper to try out the correct behavior of StringHelper.objectLogString().
 */
public class StringHelperTest1
        extends
            AbstractTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may happen in a test
     */
    public void run()
        throws
            Exception
    {
        TestClass t1 = new TestClass();
        t1.theInteger = 1;
        t1.theChar    = 'a';
        t1.theFloat   = 1.1f;
        t1.theString  = "first";
        t1.theIntegerArray = new int[] { 11, 12, 13, 14 };
        t1.theCharArray    = new char[] { 'a', 'b', 'c' };
        t1.theFloatArray   = new float[] { 1.1f, 1.2f, 1.3f };
        t1.theStringArray  = new String[] { "a1", "a2", null, "a4" };
        t1.theIntegerArrayArray = new int[][] {{ 101, 102 }, { 111, 112, 113 }, null, { 121, 122, 123, 124, 125 }};
        t1.theCharArrayArray    = new char[][] { { 'a', 'b', 'c' }, { 'd', 'e' }, null, { 'f', 'g', 'h', 'i' }};
        t1.theFloatArrayArray   = new float[][] { { 1.11f, 1.12f }, { 1.21f, 1.22f, 1.23f }, null, { 1.3f }};
        t1.theStringArrayArray  = new String[][] { { "a11", "a12" }, { "a21" }, null, { "a41", "a41", "a43", "a44" }};
        t1.theDelegate = null;

        TestClass t2 = new TestClass();
        t2.theInteger = 2;
        t2.theChar    = 'b';
        t2.theFloat   = 2.1f;
        t2.theString  = "second";
        t2.theIntegerArray = new int[] { 21, 22, 23 };
        t2.theCharArray    = new char[] { 'd', 'e', 'f' };
        t2.theFloatArray   = new float[] { 2.1f, 2.2f, 2.3f };
        t2.theStringArray  = new String[] { "b1", "b2", null, "b4" };
        t2.theIntegerArrayArray = new int[][] {{ 201, 202 }, { 211, 212, 213 }, null, { 221, 222, 223, 224, 225 }};
        t2.theCharArrayArray    = new char[][] { { 'j', 'k', 'l' }, { 'm', 'n' }, null, { 'o', 'p', 'q', 'r' }};
        t2.theFloatArrayArray   = new float[][] { { 2.11f, 2.12f }, { 2.21f, 2.22f, 2.23f }, null, { 2.3f }};
        t2.theStringArrayArray  = new String[][] { { "b11", "b12" }, { "b21" }, null, { "b41", "b41", "b43", "b44" }};
        t2.theDelegate = t1;

Class c = t2.theIntegerArrayArray.getClass();
        String logString1 = t1.toString();
        String logString2 = t2.toString();

        // System.err.println( "logString1: " + logString1 );
        System.err.println( "logString2: " + logString2 );

    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        StringHelperTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StringHelperTest1( args );
            test.run();

        } catch( Throwable ex ) {
            log.error( ex );
            System.exit(1);
        }
        if( test != null ) {
            test.cleanup();
        }

        if( errorCount == 0 ) {
            log.info( "PASS" );
        } else {
            log.error( "FAIL (" + errorCount + " errors)" );
        }

        System.exit( errorCount );
    }

    /**
     * Constructor.
     *
     * @param args command-line arguments
     * @throws Exception all sorts of things may happen in a test
     */
    public StringHelperTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( StringHelperTest1.class ); // our own, private logger

    /**
     * Test class.
     */
    public static class TestClass
            implements
                CanBeDumped
    {
        public int         theInteger;
        public char        theChar;
        public float       theFloat;
        public String      theString;
        public int []      theIntegerArray;
        public char []     theCharArray;
        public float []    theFloatArray;
        public String []   theStringArray;
        public int [][]    theIntegerArrayArray;
        public char [][]   theCharArrayArray;
        public float [][]  theFloatArrayArray;
        public String [][] theStringArrayArray;
        public TestClass   theDelegate;
        public TestClass   theNullDelegate;

        /**
         * Dump this object.
         *
         * @param d the Dumper to dump to
         */
        public void dump(
                Dumper d )
        {
            d.dump( this,
                    new String[] {
//                            "theInteger",
//                            "theChar",
//                            "theFloat",
//                            "theString",
//                            "theIntegerArray",
//                            "theCharArray",
//                            "theFloatArray",
//                            "theStringArray",
                            "theIntegerArrayArray",
                            "theCharArrayArray",
                            "theFloatArrayArray",
                            "theStringArrayArray",
                            "theDelegate",
                            "theNullDelegate"
                    },
                    new Object[] {
//                            theInteger,
//                            theChar,
//                            theFloat,
//                            theString,
//                            theIntegerArray,
//                            theCharArray,
//                            theFloatArray,
//                            theStringArray,
                            theIntegerArrayArray,
                            theCharArrayArray,
                            theFloatArrayArray,
                            theStringArrayArray,
                            theDelegate,
                            theNullDelegate
                    });
        }
    }
}
