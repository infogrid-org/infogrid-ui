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

import java.util.Iterator;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.DoubleStringifier;
import org.infogrid.util.text.HasStringRepresentation;
import org.infogrid.util.text.StringifierParsingChoice;

/**
 * Tests DoubleStringifier.
 */
public class DoubleStringifierTest1
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

        log.info( "DoubleStringifier" );
        
        double            data1a = 15.243;
        DoubleStringifier str1   = DoubleStringifier.create();
        String            res1a  = str1.format( null, data1a, HasStringRepresentation.UNLIMITED_LENGTH );
        
        checkEquals( String.valueOf( data1a ), res1a, "not the same" );

        Double temp = str1.unformat( res1a );
        checkEquals( temp.doubleValue(), data1a, "Wrong found value" );

        Iterator<StringifierParsingChoice<Double>> iter1    = str1.parsingChoiceIterator( res1a, 0, res1a.length(), Integer.MAX_VALUE, true );
        StringifierParsingChoice<Double> []        choices1 = (StringifierParsingChoice<Double> []) ArrayHelper.copyIntoNewArray( iter1, StringifierParsingChoice.class );
        
        checkEquals( choices1.length, 1, "Wrong number of choices" );
        checkEquals( choices1[0].getStartIndex(), 0,              "Wrong start index" );
        checkEquals( choices1[0].getEndIndex(),   res1a.length(), "Wrong end index" );

        String res1b = "1234567.1"; // something entirely different
        temp = str1.unformat( res1b );
        checkEquals( temp.toString(), res1b, "Wrong found value" );
        
        //
        
        double            data2a = -2347561.123;
        DoubleStringifier str2   = DoubleStringifier.create( );
        String            res2a  = String.valueOf( data2a );

        Iterator<StringifierParsingChoice<Double>> iter2    = str2.parsingChoiceIterator( res2a, 0, res2a.length(), Integer.MAX_VALUE, false );
        StringifierParsingChoice<Double> []        choices2 = (StringifierParsingChoice<Double> []) ArrayHelper.copyIntoNewArray( iter2, StringifierParsingChoice.class );
        checkEquals( choices2.length, res2a.length()-2, "Wrong number of choices" ); // -2 because of the minus and the decimal point
        for( int i=0 ; i<choices2.length ; ++i ) {
            
            log.debug( "Found " + i + ": " + choices2[i] );

            checkEquals( choices2[i].getStartIndex(), 0,   "Wrong start index" );
            checkEquals( choices2[i].getEndIndex(),   i+2, "Wrong end index" );

            checkCondition( choices2[i].unformat() instanceof Double, "Wrong result type at index " + i );
            checkEquals( choices2[i].unformat().toString(), appendPointZero( res2a.substring( 0, i+2 )), "Wrong result value at index " + i );
        }

        final int MAX = 4;
        iter2 = str2.parsingChoiceIterator( res2a, 0, res2a.length(), MAX, false );
        choices2 = (StringifierParsingChoice<Double> []) ArrayHelper.copyIntoNewArray( iter2, StringifierParsingChoice.class );
        checkEquals( choices2.length, MAX, "Wrong number of choices" );
        for( int i=0 ; i<choices2.length ; ++i ) {
            
            log.debug( "Found " + i + ": " + choices2[i] );
            
            checkEquals( choices2[i].getStartIndex(), 0,   "Wrong start index" );
            checkEquals( choices2[i].getEndIndex(),   i+2, "Wrong end index" );

            checkCondition( choices2[i].unformat() instanceof Double, "Wrong result type at index " + i );
            checkEquals( choices2[i].unformat().toString(), appendPointZero( res2a.substring( 0, i+2 )), "Wrong result at index " + i );
        }
    }

    /**
     * Append a ".0" to a Stringified number, if necessary.
     *
     * @param in the String to potentially append to
     * @return the appended String
     */
    String appendPointZero(
            String in )
    {
        if( in.endsWith( "." )) {
            return in + "0";
        } else if( in.indexOf( "." ) >= 0 ) {
            return in;
        } else {
            return in + ".0";
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
        DoubleStringifierTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new DoubleStringifierTest1( args );
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
    public DoubleStringifierTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( DoubleStringifierTest1.class ); // our own, private logger
}
