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

package org.infogrid.util.text;

import org.infogrid.util.OneElementIterator;
import org.infogrid.util.ZeroElementIterator;

import java.util.Iterator;

/**
 * Stringifies a single Double.
 */
public class DoubleStringifier
        implements
            Stringifier<Double>
{
    /**
     * Factory method.
     *
     * @return the created DoubleStringifier
     */
    public static DoubleStringifier create()
    {
        return new DoubleStringifier();
    }

    /**
     * Constructor. Use factory method.
     */
    protected DoubleStringifier()
    {
        // noop
    }

    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param arg the Object to format, or null
     * @return the formatted String
     */
    public String format(
            Double arg )
    {
        String ret = String.valueOf( arg );
        return ret;
    }
    
    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param arg the Object to format, or null
     * @return the formatted String
     * @throws ClassCastException thrown if this Stringifier could not format the provided Object
     *         because the provided Object was not of a type supported by this Stringifier
     */
    public String attemptFormat(
            Object arg )
        throws
            ClassCastException
    {
        if( arg instanceof Float ) {
            return format( ((Float)arg).doubleValue() );
        } else {
            return format( (Double) arg );
        }
    }
    
    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public Double unformat(
            String rawString )
        throws
            StringifierParseException
    {
        try {
            Double ret = Double.parseDouble( rawString );

            return ret;

        } catch( NumberFormatException ex ) {
            throw new StringifierParseException( this, rawString, ex );
        }
    }

    /**
     * Obtain an iterator that iterates through all the choices that exist for this Stringifier to
     * parse the String. The iterator returns zero elements if the String could not be parsed
     * by this Stringifier.
     *
     * @param rawString the String to parse
     * @param startIndex the position at which to parse rawString
     * @param endIndex the position at which to end parsing rawString
     * @param max the maximum number of choices to be returned by the Iterator.
     * @param matchAll if true, only return those matches that match the entire String from startIndex to endIndex.
     *                 If false, return other matches that only match the beginning of the String.
     * @return the Iterator
     */
    public Iterator<StringifierParsingChoice<Double>> parsingChoiceIterator(
            final String  rawString,
            final int     startIndex,
            final int     endIndex,
            final int     max,
            final boolean matchAll )
    {
        if( matchAll ) {
            try {
                Double found = Double.parseDouble( rawString.substring( startIndex, endIndex ));

                return OneElementIterator.<StringifierParsingChoice<Double>>create(
                        new StringifierValueParsingChoice<Double>( startIndex, endIndex, found ));
                
            } catch( NumberFormatException ex ) {
                return ZeroElementIterator.<StringifierParsingChoice<Double>>create();
            }
            
        } else if( startIndex == endIndex ) {
            return ZeroElementIterator.<StringifierParsingChoice<Double>>create();
            
        } else {
            char first = rawString.charAt( startIndex );
            int  startIndex2 = startIndex;
            if( first == '+' || first == '-' ) {
                if( startIndex + 1 == endIndex ) {
                    return ZeroElementIterator.<StringifierParsingChoice<Double>>create();
                }
                ++startIndex2;
            }
            return new MyIterator( this, rawString, startIndex, startIndex2, endIndex, max, matchAll );
        }
    }
    
    /**
     * Iterator implementation for the DoubleStringifier.
     */
    static class MyIterator
            implements
                Iterator<StringifierParsingChoice<Double>>
    {
        /**
         * Constructor.
         * 
         * @param stringifier the DoubleStringifier we belong to
         * @param rawString the String to parse
         * @param startIndex the start index
         * @param currentEnd the current end index
         * @param endIndex the final end index
         * @param max the maximum number of iterations to return
         * @param matchAll if true, match all chars between start and end
         */
        public MyIterator(
                DoubleStringifier stringifier,
                String            rawString,
                int               startIndex,
                int               currentEnd,
                int               endIndex,
                int               max,
                boolean           matchAll )
        {
            theStringifier = stringifier;
            theRawString   = rawString;
            theStartIndex  = startIndex;
            theCurrentEnd  = currentEnd;
            theEndIndex    = endIndex;
            theMax         = max;
            theMatchAll    = matchAll;
            
            goNext();
        }

        /**
         * Does the iterator have a next element?
         *
         * @return true if the iterator has a next element
         */
        public boolean hasNext()
        {
            if( theNext != null ) {
                return true;
            } else {
                return false;
            }
        }
        
        /**
         * Go to the next position.
         */
        protected void goNext()
        {
            if( ++theCounter > theMax ) {
                theNext = null;
                return;
            }
            if( ++theCurrentEnd >= theRawString.length()) {
                theNext = null;
                return;
            }
            try {
                theNext = Double.parseDouble( theRawString.substring( theStartIndex, theCurrentEnd ));

            } catch( NumberFormatException ex ) {
                theNext = null;
            }
        }

        /**
         * Obtain the next element in the iteration.
         *
         * @return the next element
         */
        public StringifierParsingChoice<Double> next()
        {
            Double ret         = theNext;
            int    previousEnd = theCurrentEnd;
            
            goNext();
            
            return new StringifierValueParsingChoice<Double>( theStartIndex, previousEnd, ret );
        }

        /**
         * Throws UnsupportedOperationException at all times.
         *
         * @throws UnsupportedOperationException at all times.
         */
        public void remove()
            throws
                UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * The Stringifier this iterator belongs to.
         */
        protected DoubleStringifier theStringifier;
        
        /**
         * The String to parse.
         */
        protected String theRawString;
        
        /**
         * Where to start.
         */
        protected int theStartIndex;
        
        /**
         * The current end, incremented every iteration.
         */
        protected int theCurrentEnd;
        
        /**
         * Where to end.
         */
        protected int theEndIndex;
        
        /**
         * The maximum number of iterations to return.
         */
        protected int theMax;
        
        /**
         * Should all chars between start and end be matched?
         */
        protected boolean theMatchAll;

        /**
         * Counts the number of iterations returned already.
         */
        protected int theCounter = 0;
        
        /**
         * The next double to return.
         */
        protected Double theNext;
    }
}
