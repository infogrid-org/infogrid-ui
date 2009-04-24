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

package org.infogrid.util.TEST;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.ArrayFacade;
import org.infogrid.util.text.AnyMessageStringifier;
import org.infogrid.util.text.HtmlStacktraceStringifier;
import org.infogrid.util.text.IntegerStringifier;
import org.infogrid.util.text.LongStringifier;
import org.infogrid.util.text.MessageStringifier;
import org.infogrid.util.text.StacktraceStringifier;
import org.infogrid.util.text.StringStringifier;
import org.infogrid.util.text.Stringifier;
import org.infogrid.util.text.StringifierParsingChoice;

/**
 * Factors out common elements of MessageStringifierTests.
 */
public abstract class AbstractMessageStringifierTest
        extends
            AbstractTest
{
    /**
     * Run one test.
     * 
     * @param current the Dataset for the test
     * @param doParse if true, also test parsing
     * @throws Exception all sorts of things may happen in a test
     */
    @SuppressWarnings(value={"unchecked"})
    protected void runOne(
            Dataset current,
            boolean doParse )
        throws
            Exception
    {
        getLog().info( "Now running data set " + current );

        HashMap<String,Stringifier<? extends Object>> map1 = new HashMap<String,Stringifier<? extends Object>>();
        map1.put( "int",            IntegerStringifier.create() );
        map1.put( "hex2",           IntegerStringifier.create( 2, 16 ) );
        map1.put( "longhex4",       LongStringifier.create( 4, 16 ) );
        map1.put( "string",         StringStringifier.create() );
        map1.put( "stacktrace",     StacktraceStringifier.create() );
        map1.put( "htmlstacktrace", HtmlStacktraceStringifier.create() );

        MessageStringifier str1 = AnyMessageStringifier.create( current.theFormatString, map1 );

        checkEquals( str1.getMessageComponents().length, current.theCorrectComponents, "Wrong number of child stringifiers" );

        //

        getLog().debug( "Now formatting" );

        ArrayFacade<Object> temp = new ArrayFacade<Object>( current.theData );

        String result1a = str1.format( null, temp, null );
        
        current.checkResult( this, result1a, "wrong formatting result" );

        //

        if( doParse ) {
            getLog().debug( "Iterating over parse results" );

            Iterator<StringifierParsingChoice<ArrayFacade<Object>>> iter = str1.parsingChoiceIterator( result1a, 0, result1a.length(), result1a.length(), true, null );
            while( iter.hasNext() ) {
                StringifierParsingChoice<ArrayFacade<Object>> childChoice = iter.next();
                getLog().debug( "found: " + childChoice );
                ArrayFacade<? extends Object> array = childChoice.unformat();
                Object [] choices = array.getArray();
                for( int j=0 ; j<choices.length ; ++j ) {
                    getLog().debug( "  " + j + ": " + choices[j] );
                }
            }

            //

            getLog().debug( "Now parsing" );

            Object [] result1b = str1.unformat( result1a, null ).getArray();
            checkEqualsInSequence( current.theData, result1b, "wrong parsing result" );
        }
    }

    /**
     * Constructor if we don't have a special resource file.
     */
    protected AbstractMessageStringifierTest()
    {
        super();
    }

    /**
     * Constructor if we have a special resource file.
     *
     * @param nameOfResourceHelperFile the name of the resource file
     */
    protected AbstractMessageStringifierTest(
            String nameOfResourceHelperFile )
    {
        super( nameOfResourceHelperFile );
    }

    /**
     * Captures a single data set for a MessageStringifierTest.
     */
    public static abstract class Dataset
    {
        /**
         * Constructor.
         * 
         * @param name name of the data set
         * @param formatString the format String for the MessageStringifier
         * @param data the data to be used with the format String
         * @param correctComponents the correct number of components in the format String after format String parsing
         */
        public Dataset(
                String name,
                String formatString,
                Object [] data,
                int    correctComponents )
        {
            theName              = name;
            theFormatString      = formatString;
            theData              = data;
            theCorrectComponents = correctComponents;
        }

        /**
         * Check the result.
         * 
         * @param test the invoking test
         * @param result the result to check
         * @param message the message to print if result is not valid
         * @return true if the result is valid
         */
        public abstract boolean checkResult(
                AbstractTest test,
                String       result,
                String       message );

        /**
         * Convert to String, for easier debugging.
         * 
         * @return String representation
         */
        @Override
        public String toString()
        {
            return "Dataset " + theName;
        }

        protected String    theName;
        protected String    theFormatString;
        protected Object [] theData;
        protected int       theCorrectComponents;
    }
    
    /**
     * Captures a single data set for a MessageStringifierTest in which the correct result is given as a fixed String.
     */
    static class StringDataset
            extends
                Dataset
    {
        /**
         * Constructor.
         * 
         * @param name name of the data set
         * @param formatString the format String for the MessageStringifier
         * @param data the data to be used with the format String
         * @param correctComponents the correct number of components in the format String after format String parsing
         * @param correctString the correctly formatted String after applying the format String to the data
         */
        public StringDataset(
                String name,
                String formatString,
                Object [] data,
                int    correctComponents,
                String correctString )
        {
            super( name, formatString, data, correctComponents );
            
            theCorrectString = correctString;
        }
        
        /**
         * Check the result.
         * 
         * @param test the invoking test
         * @param result the result to check
         * @param message the message to print if result is not valid
         * @return true if the result is valid
         */
        public boolean checkResult(
                AbstractTest test,
                String result,
                String message )
        {
            return test.checkEquals( result, theCorrectString, message );
        }

        protected String theCorrectString;
    }
    
    /**
     * Captures a single data set for a MessageStringifierTest in which the correct result is given as a regular expression.
     */
    static class RegexDataset
            extends
                Dataset
    {
        /**
         * Constructor.
         * 
         * @param name name of the data set
         * @param formatString the format String for the MessageStringifier
         * @param data the data to be used with the format String
         * @param correctComponents the correct number of components in the format String after format String parsing
         * @param regex regular expression for the correctly formatted String after applying the format String to the data
         */
        public RegexDataset(
                String name,
                String formatString,
                Object [] data,
                int    correctComponents,
                String regex )
        {
            super( name, formatString, data, correctComponents );
            
            theRegex = regex;
        }

        /**
         * Check the result.
         * 
         * @param test the invoking test
         * @param result the result to check
         * @param message the message to print if result is not valid
         * @return true if the result is valid
         */
        public boolean checkResult(
                AbstractTest test,
                String result,
                String message )
        {
            return test.checkRegex( theRegex, Pattern.DOTALL | Pattern.MULTILINE, result, message );
        }

        protected String theRegex;
    }
}    
