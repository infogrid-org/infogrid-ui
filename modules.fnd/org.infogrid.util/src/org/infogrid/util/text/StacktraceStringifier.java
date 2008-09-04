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

import java.util.Iterator;

/**
 * Stringifies the stack trace of a Throwable in plain text.
 */
public class StacktraceStringifier
        implements
            Stringifier<Throwable>
{
    /**
     * Factory method.
     *
     * @return the created StacktraceStringifier
     */
    public static StacktraceStringifier create()
    {
        String start  = "";
        String middle = "\n";
        String end    = "";
        String empty  = "";
               
        return new StacktraceStringifier( start, middle, end, empty );
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
    public static StacktraceStringifier create(
            String start,
            String middle,
            String end,
            String empty )
    {
        return new StacktraceStringifier( start, middle, end, empty );
    }

    /**
     * Private constructor. Use factory method.
     * 
     * @param start the String to print prior to the first frame of the stack trace
     * @param middle the String to print between frames of the stack trace
     * @param end the String to print after the last frame of the stack trace
     * @param empty the String to print if the stacktrace is empty
     */
    protected StacktraceStringifier(
            String start,
            String middle,
            String end,
            String empty )
    {
        theStart  = start;
        theMiddle = middle;
        theEnd    = end;
        theEmpty  = empty;
    }

    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param arg the Object to format, or null
     * @return the formatted String
     */
    public String format(
            Throwable arg )
    {
        StackTraceElement [] elements = arg.getStackTrace();

        StringBuilder buf = new StringBuilder();
        
        if( elements == null || elements.length == 0 ) {
            buf.append( theEmpty );

        } else {
            String sep = theStart;
            
            for( StackTraceElement current : elements ) {
                buf.append( sep );
                buf.append( current.toString() );
                sep = theMiddle;
            }
            buf.append( theEnd );
        }
        
        return buf.toString();
    }
    
    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param arg the Object to format, or null
     * @return the formatted String
     * @throws ClassCastException thrown if this Stringifier could not format the provided Object
     *         because the provided Object was not of a type supported by this Stringifier
     */
    public String attemptFormat(
            Object arg )
        throws
            ClassCastException
    {
        return format( (Throwable) arg );
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public Throwable unformat(
            String rawString )
        throws
            StringifierParseException
    {
        throw new UnsupportedOperationException();
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
    public Iterator<StringifierParsingChoice<Throwable>> parsingChoiceIterator(
            final String  rawString,
            final int     startIndex,
            final int     endIndex,
            final int     max,
            final boolean matchAll )
    {
        throw new UnsupportedOperationException();        
    }
    
    /**
     * The String to print prior to the stack trace.
     */
    protected String theStart;
    
    /**
     * The String to print between stack trace elements.
     */
    protected String theMiddle;

    /**
     * The String to print after the stack trace.
     */
    protected String theEnd;
    
    /**
     * The String to print if the stack trace is empty.
     */
    protected String theEmpty;
}