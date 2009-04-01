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
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.util.text.Stringifier;
import org.infogrid.util.text.StringifierParseException;
import org.infogrid.util.text.StringifierParsingChoice;

/**
 * A Stringifier to stringify PropertyValues into Java syntax. The reverse is currently NOT supported.
 * (FIXME. But: beware of code injection attacks)
 */
public class JavaPropertyValueStringifier
        implements
            Stringifier<PropertyValue>
{
    /**
     * Factory method.
     *
     * @return the created JavaPropertyValueStringifier
     */
    public static JavaPropertyValueStringifier create()
    {
        return new JavaPropertyValueStringifier();
    }

    /**
     * Private constructor for subclasses only, use factory method.
     */
    protected JavaPropertyValueStringifier()
    {
        // no op
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
            String        soFar,
            PropertyValue arg,
            int           maxLength,
            boolean       colloquial )
    {
        String ret = arg.getJavaConstructorString( "loader", "type" );
        return ret;
    }

    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @return the formatted String
     * @throws ClassCastException thrown if this Stringifier could not format the provided Object
     *         because the provided Object was not of a type supported by this Stringifier
     */
    public String attemptFormat(
            String  soFar,
            Object  arg,
            int     maxLength,
            boolean colloquial )
        throws
            ClassCastException
    {
        if( arg == null ) {
            return "null";
        } else {
            return format( soFar, (PropertyValue) arg, maxLength, colloquial );
        }
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public PropertyValue unformat(
            String rawString )
        throws
            StringifierParseException
    {
        throw new UnsupportedOperationException();
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
    public Iterator<StringifierParsingChoice<PropertyValue>> parsingChoiceIterator(
            String  rawString,
            int     startIndex,
            int     endIndex,
            int     max,
            boolean matchAll )
    {
        throw new UnsupportedOperationException();
    }
}
