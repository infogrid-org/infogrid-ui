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
import org.infogrid.util.DereferencingIterator;
import org.infogrid.util.logging.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Tests the DereferencingIterator.
 */
public class DereferencingIteratorTest1
        extends
            AbstractTest
{
    /**
     * Run the test.
     * 
     * @throws Exception all sorts of things may go wrong in a test
     */
    public void run()
        throws
            Exception
    {
        String [] testData = new String[10];
        for( int i=0 ; i<testData.length ; ++i ) {
            testData[i] = new String( String.valueOf( i )); // allocate new instance of String, so String.intern() does not get in the way of garbage collection
        }
        
        ArrayList<WeakReference<String>> testCollection = new ArrayList<WeakReference<String>>();
        for( int i=0 ; i<testData.length ; ++i ) {
            testCollection.add( new WeakReference<String>( testData[i] ));
        }
        
        //
        
        log.info( "Iterate through both" );
        
        DereferencingIterator<String> testIter1 = new DereferencingIterator<String>( testCollection.iterator() );
        for( int i=0 ; i<testData.length ; ++i ) {
            if( testIter1.hasNext() ) {
                String found = testIter1.next();
                
                checkEquals( testData[i], found, "Wrong data found at index " + i );
            } else {
                reportError( "No data found at index " + i );
            }
        }
        
        //
        
        log.info( "Unsettings a few, collecting garbage" );
        
        testData[2] = null;
        testData[3] = null;
        testData[7] = null;
        testData[9] = null;
        
        collectGarbage();
        
        //
        
        log.info( "Iterate again" );
        
        DereferencingIterator<String> testIter2 = new DereferencingIterator<String>( testCollection.iterator() );
        for( int i=0 ; i<testData.length ; ++i ) {
            if( testData[i] == null ) {
                continue;
            }
            if( testIter2.hasNext() ) {
                String found = testIter2.next();
                
                checkEquals( testData[i], found, "Wrong data found at index " + i );
            } else {
                reportError( "No data found at index " + i );
            }
        }        
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        DereferencingIteratorTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new DereferencingIteratorTest1( args );
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
     * @throws Exception all sorts of things may go wrong in a test
     */
    public DereferencingIteratorTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( DereferencingIteratorTest1.class ); // our own, private logger
}
