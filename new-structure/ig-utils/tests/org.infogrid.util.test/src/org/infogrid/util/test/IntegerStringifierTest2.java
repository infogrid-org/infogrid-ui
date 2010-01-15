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

import java.util.Iterator;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.IntegerStringifier;
import org.infogrid.util.text.StringifierParsingChoice;

/**
 * Tests IntegerStringifier.
 */
public class IntegerStringifierTest2
        extends
            AbstractTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may happen in a test
     */
    @SuppressWarnings(value={"unchecked"})
    public void run()
        throws
            Exception
    {
        int                data2a = -2347561;
        IntegerStringifier str2   = IntegerStringifier.create( );
        String             res2a  = String.valueOf( data2a );

        Iterator<StringifierParsingChoice<Integer>> iter2    = str2.parsingChoiceIterator( res2a, 0, res2a.length(), Integer.MAX_VALUE, false, null );
        StringifierParsingChoice<Integer> []        choices2 = (StringifierParsingChoice<Integer> []) ArrayHelper.copyIntoNewArray( iter2, StringifierParsingChoice.class );
        checkEquals( choices2.length, res2a.length()-1, "Wrong number of choices" ); // -1 because of the minus
        for( int i=0 ; i<choices2.length ; ++i ) {

            log.debug( "Found " + i + ": " + choices2[i] );

            checkEquals( choices2[i].getStartIndex(), 0,   "Wrong start index" );
            checkEquals( choices2[i].getEndIndex(),   i+2, "Wrong end index" );

            checkCondition( choices2[i].unformat() instanceof Integer, "Wrong result type at index " + i );
            checkEquals( choices2[i].unformat().toString(), res2a.substring( 0, i+2 ), "Wrong result value at index " + i );
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
        IntegerStringifierTest2 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new IntegerStringifierTest2( args );
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
    public IntegerStringifierTest2(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( IntegerStringifierTest2.class ); // our own, private logger
}
