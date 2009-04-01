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

package org.infogrid.model.primitives.text;

import org.infogrid.util.text.HtmlStringStringifier;
import org.infogrid.util.text.StringifierParseException;

/**
 * Overrides StringStringifier to make Strings suitable for appending as arguments to URLs.
 */
public class EscapeHashHtmlStringStringifier
        extends
            HtmlStringStringifier
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
    @Override
    public String format(
            String  soFar,
            String  arg,
            int     maxLength,
            boolean colloquial )
    {
        String s   = super.format( soFar, arg, maxLength, colloquial );
        String ret = s.replaceAll( "#", "%23" );

        return ret;
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    @Override
    public String unformat(
            String rawString )
        throws
            StringifierParseException
    {
        String s   = rawString.replaceAll( "%23", "%" );
        String ret = super.unformat( s );

        return ret;
    }
}
