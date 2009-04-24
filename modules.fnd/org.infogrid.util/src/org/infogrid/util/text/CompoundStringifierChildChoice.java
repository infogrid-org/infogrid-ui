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
import org.infogrid.util.logging.Dumper;

/**
 * A StringifierParsingChoice for CompoundStringifiers.
 * 
 * @param T the type of the Objects to be stringified
 */
class CompoundStringifierChildChoice
        extends
            StringifierParsingChoice<ArrayFacade<Object>>
{
    /**
     * Constructor.
     *
     * @param stringifier the Stringifier this choice belongs to
     * @param componentChoices the choices of the child Stringifiers, in sequence from left to right
     * @param startIndex the startIndex of the String match (inclusive)
     * @param endIndex the endIndex of the String match (exclusive)
     */
    public CompoundStringifierChildChoice(
            CompoundStringifier              stringifier,
            StringifierParsingChoice []      componentChoices,
            int                              startIndex,
            int                              endIndex )
    {
        super( startIndex, endIndex );

        theStringifier      = stringifier;
        theComponentChoices = componentChoices;
    }

    /**
     * Given this StringifierParsingChoice, obtain the Object parsed. This may
     * return null, given that some Stringifiers don't accept Objects into their
     * format method either.
     *
     * @return the Object parsed
     */
    public ArrayFacade<Object> unformat()
    {
        return theStringifier.compoundUnformat( theComponentChoices );
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    @Override
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theStringifier",
                    "theComponentChoices"
                },
                new Object[] {
                    theStringifier,
                    theComponentChoices
                } );
    }
    
    /**
     * Pointer back to our CompoundStringifier. Somehow, the non-static inner class thing wasn't working.
     */
    protected CompoundStringifier theStringifier;

    /**
     * The choices of the children.
     */
    protected StringifierParsingChoice [] theComponentChoices;
}
