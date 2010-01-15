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
 * Collects functionality common to Stringifier implementations.
 *
 * @param <T> the type of the Objects to be stringified
 */
public abstract class AbstractStringifier<T>
        implements
            Stringifier<T>
{
    /**
     * Helper method to potentially shorten output based on a parameter
     * potentially contained in the StringRepresentationParameters.
     *
     * @param s the String potentially to be shortened
     * @param pars the StringRepresentationParameters, if any
     * @return the potentially shortened String
     */
    protected String potentiallyShorten(
            String                         s,
            StringRepresentationParameters pars )
    {
        if( pars == null ) {
            return s;
        }
        Number n = (Number) pars.get( StringRepresentationParameters.MAX_LENGTH );
        if( n == null ) {
            return s;
        }
        String ret = StringHelper.potentiallyShorten( s, n.intValue() );
        return ret;
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     * This default implementation simply throws an UnsupportedOperationException.
     *
     * @param rawString the String to parse
     * @param factory the factory needed to create the parsed values, if any
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public T unformat(
            String                     rawString,
            StringifierUnformatFactory factory )
        throws
            StringifierParseException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Obtain an iterator that iterates through all the choices that exist for this Stringifier to
     * parse the String. The iterator returns zero elements if the String could not be parsed
     * by this Stringifier.
     * This default implementation simply throws an UnsupportedOperationException.
     *
     * @param rawString the String to parse
     * @param startIndex the position at which to parse rawString
     * @param endIndex the position at which to end parsing rawString
     * @param max the maximum number of choices to be returned by the Iterator.
     * @param matchAll if true, only return those matches that match the entire String from startIndex to endIndex.
     *                 If false, return other matches that only match the beginning of the String.
     * @param factory the factory needed to create the parsed values, if any
     * @return the Iterator
     */
    public Iterator<StringifierParsingChoice<T>> parsingChoiceIterator(
            String                     rawString,
            int                        startIndex,
            int                        endIndex,
            int                        max,
            boolean                    matchAll,
            StringifierUnformatFactory factory )
    {
        throw new UnsupportedOperationException();
    }
}
