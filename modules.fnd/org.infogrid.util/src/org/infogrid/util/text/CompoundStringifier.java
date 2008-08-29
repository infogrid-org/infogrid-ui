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
import org.infogrid.util.ArrayFacade;


/**
 * Abstract superclass for compound Stringifiers that delegate to N child Stringifiers.
 * By default, this class assumes that the child Stringifiers are ordered, and that the
 * result of a {@link #format format} operation is a concatenation of the child
 * Stringifiers' results of their respective {@link #format format} operation.
 * 
 * @param T the type of the Objects to be stringified
 */
public abstract class CompoundStringifier<T>
        implements
            Stringifier<ArrayFacade<T>>
{
    /**
     * Constructor, for subclasses only. This does not invoke {@link #compile compile}.
     */
    protected CompoundStringifier()
    {
        // no op
    }    

    /**
     * Compile the compound expression into child Stringifiers.
     * 
     * @throws CompoundStringifierCompileException thrown if the compilation failed.
     */
    protected synchronized final void compile()
        throws
            CompoundStringifierCompileException
    {
        if( theComponents == null ) {
            theComponents = compileIntoComponents();
        }
    }
    
    /**
     * Determine the components of this CompoundStringifier.
     * 
     * @return the components of this CompoundStringifier
     * @throws CompoundStringifierCompileException thrown if the compilation failed.
     */
    protected abstract CompoundStringifierComponent<T> [] compileIntoComponents()
        throws
            CompoundStringifierCompileException;
    
    /**
     * Obtain the components of this CompoundStringifier.
     *
     * @return the child components
     */
    public final CompoundStringifierComponent<T> [] getMessageComponents()
    {
        return theComponents;
    }
    
    /**
     * Format an Object using this Stringifier.
     *
     * @param arg the Object to format, or null
     * @return the formatted String
     */
    public String format(
            ArrayFacade<T> arg )
    {
        StringBuffer ret = new StringBuffer();
        for( int i=0 ; i<theComponents.length ; ++i ) {
            CompoundStringifierComponent<T> current = theComponents[i];
            
            String found = current.format( arg );
            
            ret.append( found );
        }
        return ret.toString();
    }
    
    /**
     * Format an Object using this Stringifier.
     *
     * @param arg the Object to format, or null
     * @return the formatted String
     * @throws ClassCastException thrown if this Stringifier could not format the provided Object
     *         because the provided Object was not of a type supported by this Stringifier
     */
    @SuppressWarnings(value={"unchecked"})
    public String attemptFormat(
            Object arg )
        throws
            ClassCastException
    {
        return format( (ArrayFacade<T>) arg );
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    public ArrayFacade<T> unformat(
            String rawString )
        throws
            StringifierParseException
    {
        Iterator<StringifierParsingChoice<ArrayFacade<T>>> iter  = parsingChoiceIterator( rawString, 0, rawString.length(), 1, true ); // only need one
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
        
        int max = 0;
        for( int i = 0 ; i<theComponents.length ; ++i ) {
            if( theComponents[i] instanceof CompoundStringifierPlaceholder ) {
                CompoundStringifierPlaceholder pl = (CompoundStringifierPlaceholder) theComponents[i];
                if( pl.getPlaceholderIndex() > max ) {
                    max = pl.getPlaceholderIndex();
                }
            }
        }

        ArrayFacade<T> ret = ArrayFacade.<T>create( max+1 );        
        T [] values = found.unformat().getArray();
        
        for( int i = 0 ; i<theComponents.length ; ++i ) {
            if( theComponents[i] instanceof CompoundStringifierPlaceholder ) {
                CompoundStringifierPlaceholder pl = (CompoundStringifierPlaceholder) theComponents[i];
                ret.put( pl.getPlaceholderIndex(), values[i] );
            }
        }
        return ret;
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
    public Iterator<StringifierParsingChoice<ArrayFacade<T>>> parsingChoiceIterator(
            String  rawString,
            int     startIndex,
            int     endIndex,
            int     max,
            boolean matchAll )
    {
        CompoundStringifierIterator<T> ret = new CompoundStringifierIterator<T>(
                this,
                theComponents,
                rawString,
                startIndex,
                endIndex,
                max,
                matchAll );
        
        return ret;
    }

    /**
     * Factory method to instantiate an array of T, given the childrens' choices.
     *
     * @param childChoices the childrens' StringifierParsingChoices
     * @return this CompoundStringifier's choice
     */
    protected abstract ArrayFacade<T> compoundUnformat(
            StringifierParsingChoice [] childChoices );
    
    /**
     * The components in the CompoundStringifier.
     */
    protected CompoundStringifierComponent<T> [] theComponents;
}
