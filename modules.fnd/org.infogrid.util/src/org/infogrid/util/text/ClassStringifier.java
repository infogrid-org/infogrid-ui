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
import org.infogrid.util.StringHelper;

/**
 * Stringifies an Java Class name.
 */
public class ClassStringifier
        implements
            Stringifier<Class>
{
    /**
     * Factory method.
     *
     * @return the created ClassStringifier
     */
    public static ClassStringifier create()
    {
        return new ClassStringifier();
    }

    /**
     * No-op constructor. Use factory method.
     */
    protected ClassStringifier()
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
            String  soFar,
            Class   arg,
            int     maxLength,
            boolean colloquial )
    {
        String ret = escape( arg.getName() );
        ret = StringHelper.potentiallyShorten( ret, maxLength );

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
            return format( soFar, null, maxLength, colloquial );

        } else if( arg instanceof Class ) {
            return format( soFar, (Class) arg, maxLength, colloquial );

        } else {
            return format( soFar, arg.getClass(), maxLength, colloquial );
        }
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public Class unformat(
            String rawString )
        throws
            StringifierParseException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Overridable method to possibly escape a String first.
     *
     * @param s the String to be escaped
     * @return the escaped String
     */
    protected String escape(
            String s )
    {
        return s;
    }

    /**
     * Overridable method to possibly unescape a String first.
     *
     * @param s the String to be unescaped
     * @return the unescaped String
     */
    protected String unescape(
            String s )
    {
        return s;
    }

    /**
     * Obtain an iterator that iterates through all the choices that exist for this Stringifier to
     * parse the String. The iterator returns zero elements if the String could not be parsed
     * by this Stringifier.
     * FIXME: This doesn't work correctly for escaped Strings.
     *
     * @param rawString the String to parse
     * @param startIndex the position at which to parse rawString
     * @param endIndex the position at which to end parsing rawString
     * @param max the maximum number of choices to be returned by the Iterator.
     * @param matchAll if true, only return those matches that match the entire String from startIndex to endIndex.
     *                 If false, return other matches that only match the beginning of the String.
     * @return the Iterator
     */
    public Iterator<StringifierParsingChoice<Class>> parsingChoiceIterator(
            final String  rawString,
            final int     startIndex,
            final int     endIndex,
            final int     max,
            final boolean matchAll )
    {
        throw new UnsupportedOperationException();
    }
}
