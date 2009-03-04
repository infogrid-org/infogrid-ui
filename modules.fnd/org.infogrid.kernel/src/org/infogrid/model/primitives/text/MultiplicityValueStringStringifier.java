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

package org.infogrid.model.primitives.text;

import java.util.Iterator;
import org.infogrid.model.primitives.MultiplicityValue;
import org.infogrid.util.OneElementIterator;
import org.infogrid.util.ZeroElementCursorIterator;
import org.infogrid.util.text.Stringifier;
import org.infogrid.util.text.StringifierParseException;
import org.infogrid.util.text.StringifierParsingChoice;
import org.infogrid.util.text.StringifierValueParsingChoice;

/**
 * StringStringifier for multiplicity values.
 */
public class MultiplicityValueStringStringifier
        implements
            Stringifier<MultiplicityValue>
{
    /**
     * Factory method.
     *
     * @return the created StringStringifier
     */
    public static MultiplicityValueStringStringifier create()
    {
        return new MultiplicityValueStringStringifier();
    }

    /**
     * No-op constructor. Use factory method.
     */
    protected MultiplicityValueStringStringifier()
    {
        // no op
    }

    /**
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the formatted String
     */
    public String format(
            String            soFar,
            MultiplicityValue arg,
            int               maxLength )
    {
        StringBuilder ret = new StringBuilder();

        if( arg.getMinimum() == MultiplicityValue.N ) {
            ret.append( MultiplicityValue.N_SYMBOL );
        } else {
            ret.append( String.valueOf( arg.getMinimum() ));
        }
        ret.append( ".." );
        if( arg.getMaximum() == MultiplicityValue.N ) {
            ret.append( MultiplicityValue.N_SYMBOL );
        } else {
            ret.append( String.valueOf( arg.getMaximum() ));
        }
        return ret.toString();
    }

    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the formatted String
     * @throws ClassCastException thrown if this Stringifier could not format the provided Object
     *         because the provided Object was not of a type supported by this Stringifier
     */
    public String attemptFormat(
            String soFar,
            Object arg,
            int    maxLength )
        throws
            ClassCastException
    {
        if( arg instanceof MultiplicityValue ) {
            return format( soFar, (MultiplicityValue) arg, maxLength );
        } else {
            return (String) arg;
        }
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public MultiplicityValue unformat(
            String rawString )
        throws
            StringifierParseException
    {
        int dots = rawString.indexOf( ".." );
        if( dots < 0 ) {
            throw new StringifierParseException( this, rawString );
        }
        String first  = rawString.substring( 0, dots );
        String second = rawString.substring( dots+2 );

        if( first.length() == 0 || second.length() == 0 ) {
            throw new StringifierParseException( this, rawString );
        }

        int min;
        int max;

        try {
            if( MultiplicityValue.N_SYMBOL.equals( first )) {
                min = MultiplicityValue.N;
            } else {
                min = Integer.parseInt( first );
            }
            if( MultiplicityValue.N_SYMBOL.equals( second )) {
                max = MultiplicityValue.N;
            } else {
                max = Integer.parseInt( second );
            }

            MultiplicityValue ret = MultiplicityValue.create( min, max );
            return ret;

        } catch( NumberFormatException ex ) {
            throw new StringifierParseException( this, null, ex );
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
    public Iterator<StringifierParsingChoice<MultiplicityValue>> parsingChoiceIterator(
            final String  rawString,
            final int     startIndex,
            final int     endIndex,
            final int     max,
            final boolean matchAll )
    {
        try {
            MultiplicityValue found = unformat( rawString.substring( startIndex, endIndex ));

            StringifierValueParsingChoice<MultiplicityValue> choice = new StringifierValueParsingChoice<MultiplicityValue>(
                        startIndex,
                        endIndex,
                        found );

            OneElementIterator<StringifierParsingChoice<MultiplicityValue>> ret
                    = OneElementIterator.<StringifierParsingChoice<MultiplicityValue>>create( choice );

            return ret;

        } catch( StringifierParseException ex ) {
            return ZeroElementCursorIterator.create();
        }
    }
}