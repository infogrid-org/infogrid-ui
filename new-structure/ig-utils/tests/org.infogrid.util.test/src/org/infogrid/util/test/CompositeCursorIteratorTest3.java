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
 * Tests the CompoundCursorIterator.
 * There is no element.
 */
public class CompositeCursorIteratorTest3
        extends
            AbstractCursorIteratorTest3
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
        ArrayList<String> collection1 = new ArrayList<String>();
        ArrayList<String> collection2 = new ArrayList<String>();
        ArrayList<String> collection3 = new ArrayList<String>();

        ArrayListCursorIterator<String> iter1 = ArrayListCursorIterator.create( collection1, String.class );
        ArrayListCursorIterator<String> iter2 = ArrayListCursorIterator.create( collection2, String.class );
        ArrayListCursorIterator<String> iter3 = ArrayListCursorIterator.create( collection3, String.class );

        List<CursorIterator<String>> iterators = new ArrayList<CursorIterator<String>>();
        iterators.add( iter1 );
        iterators.add( iter2 );
        iterators.add( iter3 );

        CompositeCursorIterator<String> iter = CompositeCursorIterator.<String>create( iterators, String.class );

        runWith( iter, log );
    }


    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        CompositeCursorIteratorTest3 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new CompositeCursorIteratorTest3( args );
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
    public CompositeCursorIteratorTest3(
            String [] args )
        throws
            Exception
    {
        super();
    }

    private static final Log log = Log.getLogInstance( CompositeCursorIteratorTest3.class  ); // our own, private logger
}
