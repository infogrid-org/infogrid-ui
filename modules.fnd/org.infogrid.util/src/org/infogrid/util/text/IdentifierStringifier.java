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
import org.infogrid.util.Identifier;

/**
 * Stringifies an Identifier.
 */
public class IdentifierStringifier
        implements
            Stringifier<Identifier>
{
    /**
     * Factory method without prefix or postfix.
     *
     * @return the created IdentifierStringifier
     */
    public static IdentifierStringifier create()
    {
        return new IdentifierStringifier( null, null );
    }

    /**
     * Factory method with prefix or postfix.
     *
     * @param prefix the prefix for the identifier, if any
     * @param postfix the postfix for the identifier, if any
     * @return the created IdentifierStringifier
     */
    public static IdentifierStringifier create(
            String prefix,
            String postfix )
    {
        return new IdentifierStringifier( prefix, postfix );
    }

    /**
     * No-op constructor. Use factory method.
     *
     * @param prefix the prefix for the identifier, if any
     * @param postfix the postfix for the identifier, if any
     */
    protected IdentifierStringifier(
            String prefix,
            String postfix )
    {
        thePrefix  = prefix;
        thePostfix = postfix;
    }

    /**
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the formatted String
     */
    public String format(
            String     soFar,
            Identifier arg,
            int        maxLength )
    {
        String ext = escape( arg.toExternalForm() );

        if( thePrefix == null && thePostfix == null ) {
            return ext;

        } else {
            StringBuilder buf = new StringBuilder();
            if( thePrefix != null ) {
                buf.append( thePrefix );
            }
            buf.append( ext );
            if( thePostfix != null ) {
                buf.append( thePrefix );
            }
            return buf.toString();
        }
    }

    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the formatted String
     * @throws ClassCastException thrown if this Stringifier could not format the provided Object
     *         because the provided Object was not of a type supported by this Stringifier
     */
    public String attemptFormat(
            String soFar,
            Object arg,
            int    maxLength )
        throws
            ClassCastException
    {
        return format( soFar, (Identifier) arg, maxLength );
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public Identifier unformat(
            String rawString )
        throws
            StringifierParseException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Overridable method to possibly escape a String first.
     *
     * @param s the String to be escaped
     * @return the escaped String
     */
    protected String escape(
            String s )
    {
        return s;
    }

    /**
     * Overridable method to possibly unescape a String first.
     *
     * @param s the String to be unescaped
     * @return the unescaped String
     */
    protected String unescape(
            String s )
    {
        return s;
    }

    /**
     * Obtain an iterator that iterates through all the choices that exist for this Stringifier to
     * parse the String. The iterator returns zero elements if the String could not be parsed
     * by this Stringifier.
     * FIXME: This doesn't work correctly for escaped Strings.
     *
     * @param rawString the String to parse
     * @param startIndex the position at which to parse rawString
     * @param endIndex the position at which to end parsing rawString
     * @param max the maximum number of choices to be returned by the Iterator.
     * @param matchAll if true, only return those matches that match the entire String from startIndex to endIndex.
     *                 If false, return other matches that only match the beginning of the String.
     * @return the Iterator
     */
    public Iterator<StringifierParsingChoice<Identifier>> parsingChoiceIterator(
            final String  rawString,
            final int     startIndex,
            final int     endIndex,
            final int     max,
            final boolean matchAll )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * The prefix, if any.
     */
    protected String thePrefix;

    /**
     * The postfix, if any.
     */
    protected String thePostfix;
}
