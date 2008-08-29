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
 * Stringifies a single Integer.
 */
public class IntegerStringifier
        extends
            NumberStringifier
        implements
            Stringifier<Integer>
{
    /**
     * Factory method.
     * 
     * @return the created IntegerStringifier
     */
    public static IntegerStringifier create()
    {
        return new IntegerStringifier( -1 );
    }

    /**
     * Factory method for an IntegerStringifier that attempts to display N digits, inserting leading zeros if needed.
     * 
     * @param digits the number of digits to display
     * @return the created IntegerStringifier
     */
    public static IntegerStringifier create(
            int digits )
    {
        return new IntegerStringifier( digits );
    }

    /**
     * Constructor. Use factory method.
     *
     * @param digits the number of digits to display
     */
    protected IntegerStringifier(
            int digits )
    {
        super( digits );
    }

    /**
     * Format an Object using this Stringifier.
     *
     * @param arg the Object to format, or null
     * @return the formatted String
     */
    public String format(
            Integer arg )
    {
        return super.format( arg.longValue() );
    }
    
    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public Integer unformat(
            String rawString )
        throws
            StringifierParseException
    {
        try {
            Integer ret = Integer.parseInt( rawString );

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
    public Iterator<StringifierParsingChoice<Integer>> parsingChoiceIterator(
            final String  rawString,
            final int     startIndex,
            final int     endIndex,
            final int     max,
            final boolean matchAll )
    {
        if( matchAll ) {
            for( int i = 0 ; i < endIndex-startIndex ; ++i ) {
                if( !validChar( i+startIndex, startIndex, endIndex, rawString ) ) {
                    return ZeroElementIterator.<StringifierParsingChoice<Integer>>create();
                }
            }
            return OneElementIterator.<StringifierParsingChoice<Integer>>create(
                    new StringifierValueParsingChoice<Integer>( startIndex, endIndex, Integer.parseInt( rawString.substring( startIndex, endIndex ))));

        } else {
            return new Iterator<StringifierParsingChoice<Integer>>() {
                /** Constructor */
                {
                    currentEnd = startIndex;
                }
                /**
                 * Does the iterator have a next element?
                 *
                 * @return true if the iterator has a next element
                 */
                public boolean hasNext()
                {
                    if( counter >= max ) {
                        return false;
                    }
                    if( currentEnd >= rawString.length()) {
                        return false;
                    }
                    if( !validChar( currentEnd, startIndex, endIndex, rawString ) ) {
                        return false;
                    }
                    return true;
                }

                /**
                 * Obtain the next element in the iteration.
                 *
                 * @return the next element
                 */
                @SuppressWarnings( "fallthrough" )
                public StringifierParsingChoice<Integer> next()
                {
                    char c = rawString.charAt( currentEnd++ );
                    switch( c ) {
                        // sequence is significant here
                        case '-':
                            isNegative = true;
                            // no break

                        case '+':
                            c = rawString.charAt( currentEnd++ );
                            // no break

                        default:
                            currentValue = currentValue*10 + Character.digit( c, 10 );
                    }
                    ++counter;
                    return new StringifierValueParsingChoice<Integer>( startIndex, currentEnd, isNegative ? (-currentValue) : currentValue );
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
                 * Capture whether this is a negative number.
                 */
                protected boolean isNegative = false;

                /**
                 * The current end, incremented every iteration.
                 */
                protected int currentEnd;
                
                /**
                 * The current number, without the negative number.
                 */
                protected int currentValue = 0;
                
                /**
                 * Counts the number of iterations returned already.
                 */
                protected int counter      = 0;
            };
        }
    }
}
