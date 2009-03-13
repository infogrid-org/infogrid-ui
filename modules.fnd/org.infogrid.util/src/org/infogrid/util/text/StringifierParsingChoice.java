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

import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * One alternative how to parse a String using a Stringifier.
 * 
 * @param <T> the type of the Objects to be stringified
 */
public abstract class StringifierParsingChoice<T>
        implements
            CanBeDumped
{
    /**
     * Constructor.
     *
     * @param startIndex the start index (inclusive) in the parsed String
     * @param endIndex the end index (exclusive) in the parsed String
     */
    protected StringifierParsingChoice(
            int    startIndex,
            int    endIndex )
    {
        theStartIndex = startIndex;
        theEndIndex   = endIndex;
    }

    /**
     * Obtain the position (inclusive) from which the match was accomplished.
     *
     * @return the position
     */
    public int getStartIndex()
    {
        return theStartIndex;
    }
    
    /**
     * Obtain the position (exclusive) through which the match was accomplished.
     *
     * @return the position
     */
    public int getEndIndex()
    {
        return theEndIndex;
    }
    
    /**
     * Given this StringifierParsingChoice, obtain the Object parsed. This may
     * return null, given that some Stringifiers don't accept Objects into their
     * format method either.
     *
     * @return the Object parsed
     */
    public abstract T unformat();
    
    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theStartIndex",
                    "theEndIndex"
                },
                new Object[] {
                    theStartIndex,
                    theEndIndex
                });
    }

    /**
     * The index of the first character (inclusive) of this choice.
     */
    protected int theStartIndex;
    
    /**
     * The index of the last character (exclusive) of this choice.
     */
    protected int theEndIndex;
}
