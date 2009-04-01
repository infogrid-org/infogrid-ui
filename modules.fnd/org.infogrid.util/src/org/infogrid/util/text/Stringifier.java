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

/**
 * <p>Interface supported by objects that know how to turn an Object into a String,
 *    and parse it again. Many implementations are possible, including:</p>
 * <ul>
 *  <li>implementations that stringify a single, simple object, such as
 *      {@link IntegerStringifier IntegerStringifier}.</li>
 *  <li>implementations that stringify a complex object, such as
 *      {@link MessageStringifier MessageStringifier}.</li>
 * </ul>
 * <p>The original motivation for this interface and its related classes and interfaces were
 *    several perceived deficiencies of the <code>java.text.MessageFormat</code> class. It is mostly
 *    meant as a more powerful and particularly more extensible replacement. For example, and unlike
 *    <code>java.text.MessageFormat</code>, this framework allows the specification of sub-formatters
 *    for specific arguments; they are not hard-coded as in case of <code>java.text.MessageFormat</code>.</p>
 * 
 * @param <T> the type of the Objects to be stringified
 */
public interface Stringifier<T>
{
    /**
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @return the formatted String
     */
    public abstract String format(
            String  soFar,
            T       arg,
            int     maxLength,
            boolean colloquial );

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
    public abstract String attemptFormat(
            String  soFar,
            Object  arg,
            int     maxLength,
            boolean colloquial )
        throws
            ClassCastException;

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public abstract T unformat(
            String rawString )
        throws
            StringifierParseException;

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
    public abstract Iterator<StringifierParsingChoice<T>> parsingChoiceIterator(
            String  rawString,
            int     startIndex,
            int     endIndex,
            int     max,
            boolean matchAll );
}
