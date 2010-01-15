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

package org.infogrid.util.test;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;

/**
 * Tests the ArrayHelper's determineDifference.
 */
public class ArrayHelperTest1
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
        String [] n = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
        String TWO = new String( "2" );

        String [] one  = new String[] { n[1], n[2],         n[3], n[4],       n[6], n[7]       };
        String [] two  = new String[] { n[1], TWO /* !! */,             n[5],       n[7], n[8] };

        //
        
        log.info( "Trying with String and equals" );
        
        String [] add1 = new String[] {                                 n[5],             n[8] };
        String [] rem1 = new String[] {                     n[3], n[4],       n[6]             };
        
        ArrayHelper.Difference<String> diff1 = ArrayHelper.determineDifference( one, two, true, String.class );
        
        checkEqualsInSequence( diff1.getAdditions(), add1, "not the same additions" );
        checkEqualsInSequence( diff1.getRemovals(),  rem1, "not the same removals" );

        //
        
        log.info( "now with ==" );
        
        String [] add2 = new String[] {       n[2],                     n[5],             n[8] };
        String [] rem2 = new String[] {       n[2],         n[3], n[4],       n[6]             };

        ArrayHelper.Difference<String> diff2 = ArrayHelper.determineDifference( one, two, false, String.class );
        
        checkEqualsInSequence( diff2.getAdditions(), add2, "not the same additions" );
        checkEqualsInSequence( diff2.getRemovals(),  rem2, "not the same removals" );
    }
    
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        ArrayHelperTest1 test = null;
        try {
            if( false && args.length != 0 ){
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ArrayHelperTest1( args );
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
    public ArrayHelperTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( ArrayHelperTest1.class ); // our own, private logger
}
