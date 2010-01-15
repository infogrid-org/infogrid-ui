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
import org.infogrid.util.ArrayListCursorIterator;
import org.infogrid.util.logging.Log;

/**
 * Tests the ArrayListCursorIterator. This is the substantially the same test as
 * ArrayCursorIteratorTest1.
 */
public class ArrayListCursorIteratorTest1
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
        
        ArrayList<String> testCollection = new ArrayList<String>();
        for( String current : testData ) {
            testCollection.add( current );
        }

        ArrayListCursorIterator<String> iter = ArrayListCursorIterator.create( testCollection, String.class );

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
        ArrayListCursorIteratorTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ArrayListCursorIteratorTest1( args );
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
    public ArrayListCursorIteratorTest1(
            String [] args )
        throws
            Exception
    {
        super();
    }

    private static final Log log = Log.getLogInstance( ArrayListCursorIteratorTest1.class ); // our own, private logger
}
