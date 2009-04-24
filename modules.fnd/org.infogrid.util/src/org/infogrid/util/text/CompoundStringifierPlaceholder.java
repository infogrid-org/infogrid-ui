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

import org.infogrid.util.ArrayFacade;

import java.util.Iterator;

/**
 * A component in the CompoundStringifier that is a placeholder for a child Stringifier.
 * 
 * @param <T> the type of the Objects to be stringified
 */
class CompoundStringifierPlaceholder
        implements
            CompoundStringifierComponent
{
    /**
     * Constructor.
     *
     * @param stringifier the underlying Stringifier
     * @param placeholderIndex the index of the placeholder
     */
    public CompoundStringifierPlaceholder(
            Stringifier<? extends Object> stringifier,
            int            placeholderIndex )
    {
        theStringifier      = stringifier;
        thePlaceholderIndex = placeholderIndex;
    }

    /**
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param pars collects parameters that may influence the String representation
     * @return the formatted String
     */
    public String format(
            String                         soFar,
            ArrayFacade<Object>            arg,
            StringRepresentationParameters pars )
    {
        Object [] realArgs = arg.getArray();

        Object localArg = realArgs[ thePlaceholderIndex ];
        String ret = theStringifier.attemptFormat( soFar, localArg, pars );

        return ret;
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
     * @param factory the factory needed to create the parsed values, if any
     * @return the Iterator
     */
    public Iterator<? extends StringifierParsingChoice<?>> parsingChoiceIterator(
            String                     rawString,
            int                        startIndex,
            int                        endIndex,
            int                        max,
            boolean                    matchAll,
            StringifierUnformatFactory factory )
    {
         Iterator<? extends StringifierParsingChoice<?>> ret
                 = theStringifier.parsingChoiceIterator( rawString, startIndex, endIndex, max, matchAll, factory );
         return ret;
    }

    /**
     * Obtain the underlying Stringifier.
     *
     * @return the underlying Stringifier
     */
    public Stringifier<?> getStringifier()
    {
        return theStringifier;
    }

    /**
     * Obtain the placeholder index.
     *
     * @return the placeholder index
     */
    public int getPlaceholderIndex()
    {
        return thePlaceholderIndex;
    }

    /**
     * The underlying Stringifier.
     */
    protected Stringifier<?> theStringifier;

    /**
     * The index of the placeholder, e.g. 2 for {2}.
     */
    protected int thePlaceholderIndex;
}
