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

/**
 * A component in the CompoundStringifier. This is not quite symmetric: the <code>format</code> argument
 * takes all arguments of the CompoundStringifier (and picks out the ones that apply), while the
 * Iterator only returns this CompoundStringifierComponent's contribution.
 * 
 * @param <T> the type of the Objects to be stringified
 */
public interface CompoundStringifierComponent<T>
{
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
            ArrayFacade<T>                 arg,
            StringRepresentationParameters pars );
    
    /**
     * Obtain an iterator that goes with this CompoundStringifierComponent.
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
            boolean matchAll );
}
