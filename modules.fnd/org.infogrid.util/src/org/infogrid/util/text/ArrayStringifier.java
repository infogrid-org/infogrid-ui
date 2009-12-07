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
import org.infogrid.util.ArrayFacade;

/**
 * A Stringifier that processes arrays. Arrays must be passed as
 * {@link org.infogrid.util.ArrayFacade}, as Java generics aren't working well
 * enough to do without that intermediate class.
 * 
 * @param <T> the type of the Objects to be stringified
 */
public class ArrayStringifier<T>
        extends
             AbstractStringifier<ArrayFacade<T>>
{
    /**
     * Factory method. This creates an ArrayStringifier that merely appends the
     * individual components after each other.
     *
     * @param delegate the delegate to which all element stringifications are delegated
     * @return the created ArrayStringifier
     * @param <T> the type of the Objects to be stringified
     */
    public static <T> ArrayStringifier<T> create(
            Stringifier<T> delegate )
    {
        return new ArrayStringifier<T>( delegate, null, null, null, null );
    }

    /**
     * Factory method. This creates an ArrayStringifier that joins the
     * individual components after each other with a string in the middle.
     * This is similar to Perl's join.
     *
     * @param delegate the delegate to which all element stringifications are delegated
     * @param middle the string to insert in the middle
     * @return the created ArrayStringifier
     * @param <T> the type of the Objects to be stringified
     */
    public static <T> ArrayStringifier<T> create(
            Stringifier<T> delegate,
            String         middle )
    {
        return new ArrayStringifier<T>( delegate, null, middle, null, null );
    }

    /**
     * Factory method. This creates an ArrayStringifier that joins the
     * individual components after each other with a string in the middle,
     * prepends a start and appends an end.
     *
     * @param delegate the delegate to which all element stringifications are delegated
     * @param start the string to insert at the beginning
     * @param middle the string to insert in the middle
     * @param end the string to append at the end
     * @return the created ArrayStringifier
     * @param <T> the type of the Objects to be stringified
     */
    public static <T> ArrayStringifier<T> create(
            Stringifier<T> delegate,
            String         start,
            String         middle,
            String         end )
    {
        return new ArrayStringifier<T>( delegate, start, middle, end, null );
    }
    
    /**
     * Factory method. This creates an ArrayStringifier that joins the
     * individual components after each other with a string in the middle,
     * prepends a start and appends an end, or uses a special empty String if
     * the array is empty.
     *
     * @param delegate the delegate to which all element stringifications are delegated
     * @param start the string to insert at the beginning
     * @param middle the string to insert in the middle
     * @param end the string to append at the end
     * @param empty what to emit instead if the array is empty
     * @return the created ArrayStringifier
     * @param <T> the type of the Objects to be stringified
     */
    public static <T> ArrayStringifier<T> create(
            Stringifier<T> delegate,
            String         start,
            String         middle,
            String         end,
            String         empty )
    {
        return new ArrayStringifier<T>( delegate, start, middle, end, empty );
    }
    
    /**
     * Constructor.
     *
     * @param delegate the delegate to which all element stringifications are delegated
     * @param start the string to insert at the beginning
     * @param middle the string to insert in the middle
     * @param end the string to append at the end
     * @param empty what to emit instead if the array is empty
     */
    protected ArrayStringifier(
            Stringifier<T> delegate,
            String         start,
            String         middle,
            String         end,
            String         empty )
    {
        theDelegate    = delegate;
        theStart       = start;
        theMiddle      = middle;
        theEnd         = end;
        theEmpty       = empty;
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
     * Obtain the empty String, if any.
     *
     * @return the empty String, if any
     */
    public String getEmpty()
    {
        return theEmpty;
    }

    /**
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param pars collects parameters that may influence the String representation
     * @return the formatted String
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String format(
            String                         soFar,
            ArrayFacade<T>                 arg,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        if( arg == null || arg.getArray().length == 0 ) {
            if( theEmpty != null ) {
                return theEmpty;
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

        T []          data = arg.getArray();
        StringBuilder ret  = new StringBuilder();
        String        sep  = theStart;
        
        for( int i=0 ; i<data.length ; ++i ) {
            if( sep != null ) {
                ret.append( sep );
            }
            String childInput = theDelegate.format( soFar + ret.toString(), data[i], pars ); // presumably shorter, but we don't know
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
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     * @throws ClassCastException thrown if this Stringifier could not format the provided Object
     *         because the provided Object was not of a type supported by this Stringifier
     */
    @SuppressWarnings("unchecked")
    public String attemptFormat(
            String                         soFar,
            Object                         arg,
            StringRepresentationParameters pars )
        throws
            StringifierException,
            ClassCastException
    {
        if( arg instanceof ArrayFacade ) {
            return format( soFar, (ArrayFacade<T>) arg, pars );
        } else {
            return format( soFar, ArrayFacade.<T>create( (T []) arg ), pars );
        }
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
    public ArrayFacade<T> unformat(
            String                     rawString,
            StringifierUnformatFactory factory )
        throws
            StringifierParseException
    {
        Iterator<StringifierParsingChoice<ArrayFacade<T>>> iter  = parsingChoiceIterator( rawString, 0, rawString.length(), 1, true, factory ); // only need one
        StringifierParsingChoice<ArrayFacade<T>>           found = null;

        while( iter.hasNext() ) {
            StringifierParsingChoice<ArrayFacade<T>> choice = iter.next();
            
            if( choice.getEndIndex() == rawString.length() ) {
                // found
                found = choice;
                break;
            }
        }
        if( found == null ) {
            throw new StringifierParseException( this, rawString );
        }
        ArrayFacade<T> ret = found.unformat();
        return ret;
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
     * @param factory the factory needed to create the parsed values, if any
     * @return the Iterator
     */
    @Override
    public Iterator<StringifierParsingChoice<ArrayFacade<T>>> parsingChoiceIterator(
            String                     rawString,
            int                        startIndex,
            int                        endIndex,
            int                        max,
            boolean                    matchAll,
            StringifierUnformatFactory factory )
    {
//        if( theStart != null && !theStart.regionMatches( 0, rawString, startIndex, theStart.length() )) {
//            return new ZeroElementIterator<StringifierParsingChoice<ArrayFacade<T>>>();
//        }
//        if( theEnd != null ) {
//            if( matchAll ) {
//                if( !theEnd.regionMatches( 0, rawString, endIndex-theEnd.length(), theEnd.length() ) {
//                    return new ZeroElementIterator<StringifierParsingChoice<ArrayFacade<T>>>();
//                }
//            } else {
//                
//            }
//        }
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
     * The String to emit if the array if empty. May be null, in which case it is assumed to theStart+theEnd.
     */
    protected String theEmpty;
    
    /**
     * The delegate Stringifier to which all element stringifications are delegated.
     */
    protected Stringifier<T> theDelegate;
}
