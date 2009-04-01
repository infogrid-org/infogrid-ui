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

package org.infogrid.util.text;

import java.util.Iterator;
import org.infogrid.util.OneElementIterator;
import org.infogrid.util.ZeroElementCursorIterator;

/**
 * Stringifies a single Long.
 */
public class LongStringifier
        extends
            NumberStringifier
        implements
            Stringifier<Long>
{
    /**
     * Factory method.
     * 
     * @return the created LongStringifier
     */
    public static LongStringifier create()
    {
        return new LongStringifier( -1 );
    }

    /**
     * Factory method for an LongStringifier that attempts to display N digits, inserting leading zeros if needed.
     * 
     * @param digits the number of digits to display
     * @return the created LongStringifier
     */
    public static LongStringifier create(
            int digits )
    {
        return new LongStringifier( digits );
    }

    /**
     * Constructor. Use factory method.
     *
     * @param digits the number of digits to display
     */
    protected LongStringifier(
            int digits )
    {
        super( digits );
    }

    /**
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @return the formatted String
     */
    public String format(
            String  soFar,
            Long    arg,
            int     maxLength,
            boolean colloquial )
    {
        return super.format( soFar, arg.longValue(), maxLength, colloquial );
    }
    
    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public Long unformat(
            String rawString )
        throws
            StringifierParseException
    {
        try {
            Long ret = Long.parseLong( rawString );

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
    public Iterator<StringifierParsingChoice<Long>> parsingChoiceIterator(
            final String  rawString,
            final int     startIndex,
            final int     endIndex,
            final int     max,
            final boolean matchAll )
    {
        if( matchAll ) {
            for( int i = 0 ; i < endIndex-startIndex ; ++i ) {
                if( !validChar( i+startIndex, startIndex, endIndex, rawString ) ) {
                    return ZeroElementCursorIterator.<StringifierParsingChoice<Long>>create();
                }
            }
            return OneElementIterator.<StringifierParsingChoice<Long>>create(
                    new StringifierValueParsingChoice<Long>( startIndex, endIndex, Long.parseLong( rawString.substring( startIndex, endIndex ))));

        } else {
            return new Iterator<StringifierParsingChoice<Long>>() {
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
                @SuppressWarnings("fallthrough")
                public StringifierParsingChoice<Long> next()
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
                    return new StringifierValueParsingChoice<Long>( startIndex, currentEnd, isNegative ? (-currentValue) : currentValue );
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
                protected long currentValue = 0;
                
                /**
                 * Counts the number of iterations returned already.
                 */
                protected int counter      = 0;
            };
        }
    }
    
}
