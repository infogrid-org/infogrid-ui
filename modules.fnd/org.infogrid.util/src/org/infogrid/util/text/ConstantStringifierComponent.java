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
import org.infogrid.util.ArrayFacade;
import org.infogrid.util.OneElementIterator;
import org.infogrid.util.ZeroElementCursorIterator;

/**
 * The constant blocks of text inside a (compound) MessageStringifier.
 */
public class ConstantStringifierComponent
        implements
            CompoundStringifierComponent
{
    /**
     * Constructor.
     *
     * @param s the constant String
     */
    public ConstantStringifierComponent(
            String s )
    {
        theString = s;
    }

    /**
     * Format zero or one Objects in the ArrayFacade.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format
     * @param pars collects parameters that may influence the String representation
     * @return the formatted String
     */
    public String format(
            String                         soFar,
            ArrayFacade<Object>            arg,
            StringRepresentationParameters pars )
    {
        // regardless of argument, we always return the same -- this is a constant after all
        return theString;
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
    public Iterator<? extends StringifierParsingChoice<Object>> parsingChoiceIterator(
            String                     rawString,
            int                        startIndex,
            int                        endIndex,
            int                        max,
            boolean                    matchAll,
            StringifierUnformatFactory factory )
    {
        if( rawString.regionMatches( startIndex, theString, 0, theString.length() )) {
            return OneElementIterator.<StringifierParsingChoice<Object>>create(
                    new StringifierParsingChoice<Object>( startIndex, startIndex + theString.length() ) {
                        public Object unformat() {
                            // this doesn't return any value
                            return null;
                        }
                    }
                );
        } else {
            return ZeroElementCursorIterator.<StringifierParsingChoice<Object>>create();
        }
    }

    /**
     * The constant String.
     */
    protected String theString;
}
