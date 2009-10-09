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

package org.infogrid.util.http;

/**
 * Factors out functionality common to SaneCookie implementations.
 */
public abstract class AbstractSaneCookie
        implements
            SaneCookie
{
    /**
     * Returns the character at the specified index. From CharSequence.
     *
     * @param index the index
     * @return the character
     */
    public char charAt(
            int index )
    {
        return getValue().charAt( index );
    }

    /**
     * Returns the length of this character sequence. From CharSequence.
     *
     * @return length
     */
    public int length()
    {
        return getValue().length();
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     *
     * @param start start index
     * @param end end index
     * @return substring
     */
    public CharSequence subSequence(
            int start,
            int end )
    {
        return getValue().subSequence( start, end );
    }

    /**
     * Return the value of the cookie. Note that this is not just for debugging.
     *
     * @return the value of the cookie
     */
    @Override
    public String toString()
    {
        return getValue();
    }
}
