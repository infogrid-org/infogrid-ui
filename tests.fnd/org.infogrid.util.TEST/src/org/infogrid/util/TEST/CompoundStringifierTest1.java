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
import org.infogrid.util.text.AnyMessageStringifier;
import org.infogrid.util.text.IntegerStringifier;
import org.infogrid.util.text.MessageStringifier;
import org.infogrid.util.text.StringStringifier;
import org.infogrid.util.text.Stringifier;
import org.infogrid.util.text.StringifierParsingChoice;

import java.util.HashMap;
import java.util.Iterator;

/**
 * CompoundStringifier tests. There are many "unchecked cast" exceptions, but somehow I can't figure it out better right now.
 */
public class CompoundStringifierTest1
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
        for( int i=0 ; i<datasets.length ; ++i ) {
            Dataset current = datasets[i];
            
            log.info( "Now running data set " + current );

            HashMap<String,Stringifier<? extends Object>> map1 = new HashMap<String,Stringifier<? extends Object>>();
            map1.put( "int",    IntegerStringifier.create() );
            map1.put( "string", StringStringifier.create() );

            MessageStringifier<Object> str1 = AnyMessageStringifier.create( current.theFormatString, map1 );

            checkEquals( str1.getMessageComponents().length, current.theCorrectComponents, "Wrong number of child stringifiers" );

            //

            log.debug( "Now formatting" );

            ArrayFacade<Object> temp = new ArrayFacade<Object>( current.theData );

            String result1a = str1.format( temp );
            checkEquals( result1a, current.theCorrectString, "wrong formatting result" );

            //

            log.debug( "Iterating over parse results" );

            Iterator<StringifierParsingChoice<ArrayFacade<Object>>> iter = str1.parsingChoiceIterator( result1a, 0, result1a.length(), result1a.length(), false );
            while( iter.hasNext() ) {
                StringifierParsingChoice<ArrayFacade<Object>> childChoice = iter.next();
                log.debug( "found: " + childChoice );
                ArrayFacade<? extends Object> array = childChoice.unformat();
                Object [] choices = array.getArray();
                for( int j=0 ; j<choices.length ; ++j ) {
                    log.debug( "  " + j + ": " + choices[j] );
                }
            }

            //

            log.debug( "Now parsing" );

            Object [] result1b = str1.unformat( result1a ).getArray();
            checkEqualsInSequence( current.theData, result1b, "wrong parsing result" );
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
        CompoundStringifierTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new CompoundStringifierTest1( args );
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
    public CompoundStringifierTest1(
            String [] args )
        throws
            Exception
    {
        super( thisPackage( CompoundStringifierTest1.class, "Log.properties" ));
    }

    private static final Log log = Log.getLogInstance( CompoundStringifierTest1.class ); // our own, private logger
    
    /**
     * Data sets.
     */
    static class Dataset
    {
        public Dataset(
                String name,
                String formatString,
                Object [] data,
                int    correctComponents,
                String correctString )
        {
            theName              = name;
            theFormatString      = formatString;
            theData              = data;
            theCorrectComponents = correctComponents;
            theCorrectString     = correctString;
        }

        @Override
        public String toString()
        {
            return "Dataset " + theName;
        }
        protected String    theName;
        protected String    theFormatString;
        protected Object [] theData;
        protected int       theCorrectComponents;
        protected String    theCorrectString;
    }
    
    static Dataset [] datasets = {
            new Dataset(
                    "One",
                    "Abc {0,int}",
                    new Object[] { 12 },
                    2,
                    "Abc 12" ),
            new Dataset(
                    "Two",
                    "Abc {0,int} def{1,string}",
                    new Object[] { 12, "XXX-X" },
                    4,
                    "Abc 12 defXXX-X" ),
            new Dataset(
                    "Three",
                    "Abc {0,int} def",
                    new Object[] { 987 },
                    3,
                    "Abc 987 def" ),
            new Dataset(
                    "Four",
                    "Abc {0,int} def{2,string}  ghi kl{0,int}mno {1,int}",
                    new Object[] { 0, 111, "222" },
                    8,
                    "Abc 0 def222  ghi kl0mno 111" ),
    };
}
