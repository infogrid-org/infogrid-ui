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

/**
 * Stringifies the stack trace of a Throwable in plain text.
 */
public class StacktraceStringifier
        extends
            AbstractStringifier<Throwable>
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
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param pars collects parameters that may influence the String representation
     * @return the formatted String
     */
    public String format(
            String                         soFar,
            Throwable                      arg,
            StringRepresentationParameters pars )
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

        String ret = potentiallyShorten( buf.toString(), pars );
        return ret;
    }
    
    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param pars collects parameters that may influence the String representation
     * @return the formatted String
     * @throws ClassCastException thrown if this Stringifier could not format the provided Object
     *         because the provided Object was not of a type supported by this Stringifier
     */
    public String attemptFormat(
            String                         soFar,
            Object                         arg,
            StringRepresentationParameters pars )
        throws
            ClassCastException
    {
        return format( soFar, (Throwable) arg, pars );
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
