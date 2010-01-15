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

import java.util.ArrayList;
import java.util.List;
import org.infogrid.util.ArrayListCursorIterator;
import org.infogrid.util.CompositeCursorIterator;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;

/**
 * Tests the CompoundCursorIterator. This is the substantially the same test as
 * ArrayCursorIteratorTest1.
 */
public class CompositeCursorIteratorTest1
        extends
            AbstractCursorIteratorTest1
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
        String [] testData = new String[] {
            "a", // [0]
            "b", // [1]
            "c", // [2]
            "d", // [3]
            "e", // [4]
            "f", // [5]
            "g", // [6]
            "h"  // [7]
        };

        ArrayList<String> collection1 = new ArrayList<String>();
        ArrayList<String> collection2 = new ArrayList<String>();
        ArrayList<String> collection3 = new ArrayList<String>();
        ArrayList<String> collection4 = new ArrayList<String>();
        
        collection1.add( testData[0] );
        collection1.add( testData[1] );
        // nothing in #2
        collection3.add( testData[2] );
        collection3.add( testData[3] );
        collection3.add( testData[4] );
        collection3.add( testData[5] );
        collection3.add( testData[6] );
        collection4.add( testData[7] );

        ArrayListCursorIterator<String> iter1 = ArrayListCursorIterator.create( collection1, String.class );
        ArrayListCursorIterator<String> iter2 = ArrayListCursorIterator.create( collection2, String.class );
        ArrayListCursorIterator<String> iter3 = ArrayListCursorIterator.create( collection3, String.class );
        ArrayListCursorIterator<String> iter4 = ArrayListCursorIterator.create( collection4, String.class );
        
        List<CursorIterator<String>> iterators = new ArrayList<CursorIterator<String>>();
        iterators.add( iter1 );
        iterators.add( iter2 );
        iterators.add( iter3 );
        iterators.add( iter4 );
        
        CompositeCursorIterator<String> iter = CompositeCursorIterator.<String>create( iterators, String.class );

        runWith( testData, iter, log );
    }
    

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        CompositeCursorIteratorTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new CompositeCursorIteratorTest1( args );
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
    public CompositeCursorIteratorTest1(
            String [] args )
        throws
            Exception
    {
        super();
    }

    private static final Log log = Log.getLogInstance( CompositeCursorIteratorTest1.class  ); // our own, private logger
}
