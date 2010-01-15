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

import org.infogrid.util.ArrayCursorIterator;
import org.infogrid.util.FilteringCursorIterator;
import org.infogrid.util.logging.Log;

/**
 * Tests the FilteringCursorIterator.
 * The underlying iterator has only one element, which the filter accepts.
 */
public class FilteringCursorIteratorTest2b
        extends
            AbstractCursorIteratorTest2
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
        String [] baseData = new String[] {
            "a+A",  //  [0]  [0]
        };
        String [] testData = new String[] {
            "a+A",  //  [0]  [0]
        };

        ArrayCursorIterator<String> baseIterator = ArrayCursorIterator.<String>create( baseData );

        FilteringCursorIterator<String> filterIterator = FilteringCursorIterator.<String>create(
                baseIterator,
                new FilteringCursorIterator.Filter<String>() {
                    public boolean accept(
                            String s )
                    {
                        boolean ret = s.indexOf( "+" ) >= 0;
                        return ret;
                    }
                },
                String.class );

        runWith( testData[0], filterIterator, log );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        FilteringCursorIteratorTest2b test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new FilteringCursorIteratorTest2b( args );
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
    public FilteringCursorIteratorTest2b(
            String [] args )
        throws
            Exception
    {
        super();
    }

    private static final Log log = Log.getLogInstance( FilteringCursorIteratorTest2b.class ); // our own, private logger
}
