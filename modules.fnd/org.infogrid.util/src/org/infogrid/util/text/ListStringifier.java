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
import java.util.List;

/**
 * A Stringifier that processes lists.
 *
 * @param <T> the type of the Objects to be stringified
 */
public class ListStringifier<T extends List<?>>
        extends
             AbstractStringifier<T>
{
    /**
     * Factory method. This creates an ListStringifier that merely appends the
     * individual components after each other.
     *
     * @return the created ListStringifier
     * @param <T> the type of the Objects to be stringified
     */
    public static <T extends List<?>> ListStringifier<T> create()
    {
        return new ListStringifier<T>( null, null, null, null );
    }

    /**
     * Factory method. This creates an ListStringifier that joins the
     * individual components after each other with a string in the middle.
     * This is similar to Perl's join.
     *
     * @param middle the string to insert in the middle
     * @return the created ListStringifier
     * @param <T> the type of the Objects to be stringified
     */
    public static <T extends List<?>> ListStringifier<T> create(
            String         middle )
    {
        return new ListStringifier<T>( null, middle, null, null );
    }

    /**
     * Factory method. This creates an ListStringifier that joins the
     * individual components after each other with a string in the middle,
     * prepends a start and appends an end.
     *
     * @param start the string to insert at the beginning
     * @param middle the string to insert in the middle
     * @param end the string to append at the end
     * @return the created ListStringifier
     * @param <T> the type of the Objects to be stringified
     */
    public static <T extends List<?>> ListStringifier<T> create(
            String         start,
            String         middle,
            String         end )
    {
        return new ListStringifier<T>( start, middle, end, null );
    }

    /**
     * Factory method. This creates an ListStringifier that joins the
     * individual components after each other with a string in the middle,
     * prepends a start and appends an end, or uses a special empty String if
     * the array is empty.
     *
     * @param start the string to insert at the beginning
     * @param middle the string to insert in the middle
     * @param end the string to append at the end
     * @param empty what to emit instead if the array is empty
     * @return the created ListStringifier
     * @param <T> the type of the Objects to be stringified
     */
    public static <T extends List<?>> ListStringifier<T> create(
            String         start,
            String         middle,
            String         end,
            String         empty )
    {
        return new ListStringifier<T>( start, middle, end, empty );
    }

    /**
     * Constructor.
     *
     * @param start the string to insert at the beginning
     * @param middle the string to insert in the middle
     * @param end the string to append at the end
     * @param empty what to emit instead if the array is empty
     */
    protected ListStringifier(
            String         start,
            String         middle,
            String         end,
            String         empty )
    {
        theStart       = start;
        theMiddle      = middle;
        theEnd         = end;
        theEmptyString = empty;
    }

    /**
     * Obtain the start String, if any.
     *
     * @return the start String, if any
     */
    public String getStart()
    {
        return theStart;
    }

    /**
     * Obtain the middle String, if any.
     *
     * @return the middle String, if any
     */
    public String getMiddle()
    {
        return theMiddle;
    }

    /**
     * Obtain the end String, if any.
     *
     * @return the end String, if any
     */
    public String getEnd()
    {
        return theEnd;
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
            T                              arg,
            StringRepresentationParameters pars )
    {
        if( arg == null || arg.isEmpty() ) {
            if( theEmptyString != null ) {
                return theEmptyString;
            } else if( theStart != null ) {
                if( theEnd != null ) {
                    return theStart+theEnd;
                } else {
                    return theStart;
                }
            } else if( theEnd != null ) {
                return theEnd;
            } else {
                return "";
            }
        }

        StringBuilder ret  = new StringBuilder();
        String        sep  = theStart;

        for( Object current : arg ) {
            if( sep != null ) {
                ret.append( sep );
            }
            String childInput = String.valueOf( current );
            if( childInput != null ) {
                ret.append( childInput );
            }
            sep = theMiddle;
        }
        if( theEnd != null ) {
            ret.append( theEnd );
        }

        return potentiallyShorten( ret.toString(), pars );
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
    @SuppressWarnings("unchecked")
    public String attemptFormat(
            String                         soFar,
            Object                         arg,
            StringRepresentationParameters pars )
        throws
            ClassCastException
    {
        return format( soFar, (T) arg, pars );
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public T unformat(
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
     * FIXME: This is not implemented right now.
     *
     * @param rawString the String to parse
     * @param startIndex the position at which to parse rawString
     * @param endIndex the position at which to end parsing rawString
     * @param max the maximum number of choices to be returned by the Iterator.
     * @param matchAll if true, only return those matches that match the entire String from startIndex to endIndex.
     *                 If false, return other matches that only match the beginning of the String.
     * @return the Iterator
     */
    public Iterator<StringifierParsingChoice<T>> parsingChoiceIterator(
            String  rawString,
            int     startIndex,
            int     endIndex,
            int     max,
            boolean matchAll )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * The String to insert at the beginning. May be null.
     */
    protected String theStart;

    /**
     * The String to insert when joining two elements. May be null.
     */
    protected String theMiddle;

    /**
     * The String to append at the end. May be null.
     */
    protected String theEnd;

    /**
     * The String to emit if the array if empty. May be null, in which case it is assumed to the theStart+theEnd.
     */
    protected String theEmptyString;
}
