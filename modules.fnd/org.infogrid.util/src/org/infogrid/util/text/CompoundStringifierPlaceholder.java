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

import org.infogrid.util.ArrayFacade;

import java.util.Iterator;

/**
 * A component in the CompoundStringifier that is a placeholder for a child Stringifier.
 * 
 * @param <T> the type of the Objects to be stringified
 */
class CompoundStringifierPlaceholder<T>
        implements
            CompoundStringifierComponent<T>
{
    /**
     * Constructor.
     *
     * @param stringifier the underlying Stringifier
     * @param placeholderIndex the index of the placeholder
     */
    public CompoundStringifierPlaceholder(
            Stringifier<? extends T> stringifier,
            int                      placeholderIndex )
    {
        theStringifier      = stringifier;
        thePlaceholderIndex = placeholderIndex;
    }

    /**
     * Format zero or one Object in the ArrayFacade.
     *
     * @param soFar the String so far, if any
     * @param args the Objects to format
     * @return the formatted String
     * @throws IllegalArgumentException thrown if this component does not support the formatting of this Object
     */
    public String format(
            String         soFar,
            ArrayFacade<T> args )
    {
        T [] realArgs = args.getArray();

        T localArg = realArgs[ thePlaceholderIndex ];
        String ret = theStringifier.attemptFormat( soFar, localArg );

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
     * @return the Iterator
     */
    public Iterator<? extends StringifierParsingChoice<? extends T>> parsingChoiceIterator(
            String  rawString,
            int     startIndex,
            int     endIndex,
            int     max,
            boolean matchAll )
    {
         Iterator<? extends StringifierParsingChoice<? extends T>> ret
                 = theStringifier.parsingChoiceIterator( rawString, startIndex, endIndex, max, matchAll );
         return ret;
    }

    /**
     * Obtain the underlying Stringifier.
     *
     * @return the underlying Stringifier
     */
    public Stringifier getStringifier()
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
    protected Stringifier<? extends T> theStringifier;

    /**
     * The index of the placeholder, e.g. 2 for {2}.
     */
    protected int thePlaceholderIndex;
}
