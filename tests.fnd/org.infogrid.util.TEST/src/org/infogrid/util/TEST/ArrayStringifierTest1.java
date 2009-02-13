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

package org.infogrid.util.TEST;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.ArrayFacade;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.ArrayStringifier;
import org.infogrid.util.text.LongStringifier;

/**
 * Tests ArrayStringifier.
 */
public class ArrayStringifierTest1
        extends
            AbstractTest
{
    /**
     * Run the test.
     * 
     * @throws Exception all sorts of things may happen in a test
     */
//    @SuppressWarnings(value={"unchecked"})
    public void run()
        throws
            Exception
    {
        //

        log.info( "ArrayStringifier" );

        ArrayStringifier<Long> str = ArrayStringifier.create( LongStringifier.create(), "aaa", "bbb", "ccc" );
        Long [] data = { 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 0L };
        
        String result = str.format( null, ArrayFacade.create( data ));
        
        checkEquals( result.length(), str.getStart().length() + str.getEnd().length() + ( data.length-1 ) * str.getMiddle().length() + data.length, "wrong length of result string" );
        checkCondition( result.startsWith( str.getStart() ), "does not start right" );
        checkCondition( result.endsWith( str.getEnd() ), "does not end right" );

        int count = 0;
        int pos   = -1;
        while( ( pos = result.indexOf( str.getMiddle(), pos+1 )) > 0 ) {
            ++count;
        }
        checkEquals( count, data.length-1, "wrong number of middles" );
        
        log.debug( "Found result '" + result + "'." );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        ArrayStringifierTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ArrayStringifierTest1( args );
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
    public ArrayStringifierTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( ArrayStringifierTest1.class ); // our own, private logger
}
