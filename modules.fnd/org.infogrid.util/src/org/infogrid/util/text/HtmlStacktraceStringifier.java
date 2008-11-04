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

/**
 * Stringifies the stack trace of a Throwable in HTML text.
 */
public class HtmlStacktraceStringifier
        extends
            StacktraceStringifier
{
    /**
     * Factory method.
     *
     * @return the created StacktraceStringifier
     */
    public static HtmlStacktraceStringifier create()
    {
        String start  = "<div class=\"stacktrace\">\n<div class=\"stacktrace-element\">";
        String middle = "</div>\n<div class=\"stacktrace-element\">";
        String end    = "</div>\n</div>";
        String empty  = "<div class=\"stacktrace stacktrace-empty\">empty</div>";
               
        return new HtmlStacktraceStringifier( start, middle, end, empty );
    }

    /**
     * Factory method.
     *
     * @param start the String to print prior to the first frame of the stack trace
     * @param middle the String to print between frames of the stack trace
     * @param end the String to print after the last frame of the stack trace
     * @param empty the String to print if the stacktrace is empty
     * @return the created StacktraceStringifier
     */
    public static HtmlStacktraceStringifier create(
            String start,
            String middle,
            String end,
            String empty )
    {
        return new HtmlStacktraceStringifier( start, middle, end, empty );
    }

    /**
     * Private constructor. Use factory method.
     * 
     * @param start the String to print prior to the first frame of the stack trace
     * @param middle the String to print between frames of the stack trace
     * @param end the String to print after the last frame of the stack trace
     * @param empty the String to print if the stacktrace is empty
     */
    protected HtmlStacktraceStringifier(
            String start,
            String middle,
            String end,
            String empty )
    {
        super( start, middle, end, empty );
    }

    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param arg the Object to format, or null
     * @return the formatted String
     */
    @Override
    public String format(
            Throwable arg )
    {
        String raw = super.format( arg );
        
        String ret = raw; // HtmlStringStringifier.stringToHtml( raw );
        
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
    public Throwable unformat(
            String rawString )
        throws
            StringifierParseException
    {
        String unescaped = HtmlStringStringifier.htmlToString( rawString );
        
        Throwable ret = super.unformat( unescaped );
        
        return ret;
    }
}
