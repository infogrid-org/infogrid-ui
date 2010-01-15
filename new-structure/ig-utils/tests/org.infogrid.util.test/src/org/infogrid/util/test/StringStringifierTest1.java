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
import org.infogrid.util.text.StringStringifier;
import org.infogrid.util.text.StringifierParsingChoice;

/**
 * Tests StringStringifier.
 */
public class StringStringifierTest1
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

        log.info( "StringStringifier" );
        
        String            data1a = "abc";
        StringStringifier str1   = StringStringifier.create( );
        String            res1a  = str1.format( null, data1a, null );
        
        checkEquals( data1a, res1a, "not the same" );

        String temp = str1.unformat( res1a, null );
        checkEquals( temp, data1a, "Wrong found value" );

        Iterator<StringifierParsingChoice<String>> iter1    = str1.parsingChoiceIterator( data1a, 0, data1a.length(), Integer.MAX_VALUE, true, null );
        StringifierParsingChoice<String> []        choices1 = ArrayHelper.copyIntoNewArray( iter1, StringifierParsingChoice.class );
        
        checkEquals( choices1.length, 1, "Wrong number of choices" );
        checkEquals( choices1[0].getStartIndex(), 0,               "Wrong start index" );
        checkEquals( choices1[0].getEndIndex(),   data1a.length(), "Wrong end index" );

        String data1b = "lagnraerg"; // something entirely different
        temp = str1.unformat( data1b, null );
        checkEquals( temp, data1b, "Wrong found value" );
        
        //
        
        String            data2a = "ak;wrbg;WRG";
        StringStringifier str2   = StringStringifier.create( );

        Iterator<StringifierParsingChoice<String>> iter2    = str2.parsingChoiceIterator( data2a, 0, data2a.length(), Integer.MAX_VALUE, false, null );
        StringifierParsingChoice<String> []        choices2 = ArrayHelper.copyIntoNewArray( iter2, StringifierParsingChoice.class );
        checkEquals( choices2.length, data2a.length(), "Wrong number of choices" );
        for( int i=0 ; i<choices2.length ; ++i ) {
            checkEquals( choices2[i].unformat(), data2a.substring( 0, i ), "Wrong result at index " + i );
        }

        final int MAX = 4;
        iter2 = str2.parsingChoiceIterator( data2a, 0, data2a.length(), MAX, false, null );
        choices2 = ArrayHelper.copyIntoNewArray( iter2, StringifierParsingChoice.class );
        checkEquals( choices2.length, MAX, "Wrong number of choices" );
        for( int i=0 ; i<choices2.length ; ++i ) {
            checkEquals( choices2[i].unformat(), data2a.substring( 0, i ), "Wrong result at index " + i );
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
        StringStringifierTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StringStringifierTest1( args );
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
    public StringStringifierTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( StringStringifierTest1.class ); // our own, private logger
}
