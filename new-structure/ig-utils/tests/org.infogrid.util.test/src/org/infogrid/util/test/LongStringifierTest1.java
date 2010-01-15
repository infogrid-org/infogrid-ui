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
import org.infogrid.util.text.LongStringifier;
import org.infogrid.util.text.StringifierParsingChoice;

/**
 * Tests LongStringifier.
 */
public class LongStringifierTest1
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
        //

        log.info( "LongStringifier" );
        
        long            data1a = 15243L;
        LongStringifier str1   = LongStringifier.create( );
        String          res1a  = str1.format( null, data1a, null );
        
        checkEquals( String.valueOf( data1a ), res1a, "not the same" );

        Long temp = str1.unformat( res1a, null );
        checkEquals( temp.intValue(), data1a, "Wrong found value" );

        Iterator<StringifierParsingChoice<Long>> iter1    = str1.parsingChoiceIterator( res1a, 0, res1a.length(), Integer.MAX_VALUE, true, null );
        StringifierParsingChoice<Long> []        choices1 = (StringifierParsingChoice<Long> []) ArrayHelper.copyIntoNewArray( iter1, StringifierParsingChoice.class );
        
        checkEquals( choices1.length, 1, "Wrong number of choices" );
        checkEquals( choices1[0].getStartIndex(), 0,              "Wrong start index" );
        checkEquals( choices1[0].getEndIndex(),   res1a.length(), "Wrong end index" );

        String res1b = "123"; // something entirely different
        temp = str1.unformat( res1b, null );
        checkEquals( temp.toString(), res1b, "Wrong found value" );
        
        //
        
        int             data2a = -2347561;
        LongStringifier str2   = LongStringifier.create( );
        String          res2a  = String.valueOf( data2a );

        Iterator<StringifierParsingChoice<Long>> iter2    = str2.parsingChoiceIterator( res2a, 0, res2a.length(), Integer.MAX_VALUE, false, null );
        StringifierParsingChoice<Long> []        choices2 = (StringifierParsingChoice<Long> []) ArrayHelper.copyIntoNewArray( iter2, StringifierParsingChoice.class );
        checkEquals( choices2.length, res2a.length()-1, "Wrong number of choices" ); // -1 because of the minus
        for( int i=0 ; i<choices2.length ; ++i ) {
            
            log.debug( "Found " + i + ": " + choices2[i] );

            checkEquals( choices2[i].getStartIndex(), 0,   "Wrong start index" );
            checkEquals( choices2[i].getEndIndex(),   i+2, "Wrong end index" );

            checkCondition( choices2[i].unformat() instanceof Long, "Wrong result type at index " + i );
            checkEquals( choices2[i].unformat().toString(), res2a.substring( 0, i+2 ), "Wrong result value at index " + i );
        }

        final int MAX = 4;
        iter2 = str2.parsingChoiceIterator( res2a, 0, res2a.length(), MAX, false, null );
        choices2 = ArrayHelper.copyIntoNewArray( iter2, StringifierParsingChoice.class );
        checkEquals( choices2.length, MAX, "Wrong number of choices" );
        for( int i=0 ; i<choices2.length ; ++i ) {
            
            log.debug( "Found " + i + ": " + choices2[i] );
            
            checkEquals( choices2[i].getStartIndex(), 0,   "Wrong start index" );
            checkEquals( choices2[i].getEndIndex(),   i+2, "Wrong end index" );

            checkCondition( choices2[i].unformat() instanceof Long, "Wrong result type at index " + i );
            checkEquals( choices2[i].unformat().toString(), res2a.substring( 0, i+2 ), "Wrong result at index " + i );
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
        IntegerStringifierTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new IntegerStringifierTest1( args );
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
    public LongStringifierTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( IntegerStringifierTest1.class ); // our own, private logger
}
