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

import org.infogrid.util.StringHelper;

/**
 * Stringifies a String using valid HTML syntax. For example, this replaces
 * <code>&gt;</code> with <code>&amp;gt;</code>.
 */
public class HtmlStringStringifier
        extends
            StringStringifier
{
    /**
     * Factory method.
     *
     * @return the created HtmlStringStringifier
     */
    public static HtmlStringStringifier create()
    {
        return new HtmlStringStringifier();
    }

    /**
     * No-op constructor. Use factory method.
     */
    protected HtmlStringStringifier()
    {
        // no op
    }
    
    /**
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param pars collects parameters that may influence the String representation
     * @return the formatted String
     */
    @Override
    public String format(
            String                         soFar,
            String                         arg,
            StringRepresentationParameters pars )
    {
        String raw = super.format( soFar, arg, pars );
        
        String ret = StringHelper.stringToHtml( raw );
        
        return ret;
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @param factory the factory needed to create the parsed values, if any
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    @Override
    public String unformat(
            String                     rawString,
            StringifierUnformatFactory factory )
        throws
            StringifierParseException
    {
        String unescaped = StringHelper.htmlToString( rawString );
        
        String ret = super.unformat( unescaped, factory );
        
        return ret;
    }
}
