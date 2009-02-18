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

package org.infogrid.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * <p>Iterator that returns the content returned by N delegate Iterators, in sequence.</p>
 * 
 * @param <E> the type of element to iterate over
 */
public class CompositeIterator<E>
        implements
            Iterator<E>,
            Enumeration<E>
{
    /**
     * Factory method.
     *
     * @param delegates the Iterators to delegate to, in sequence
     * @return the created CompositeIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeIterator<E> createFromIterators(
            List<Iterator<E>> delegates )
    {
        return new CompositeIterator<E>( delegates );
    }

    /**
     * Factory method.
     *
     * @param delegates the Iterators to delegate to, in sequence
     * @return the created CompositeIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeIterator<E> createFromIterators(
            Iterator<E> [] delegates )
    {
        List<Iterator<E>> delegates2 = new ArrayList<Iterator<E>>( delegates.length );
        for( int i=0 ; i<delegates.length ; ++i ) {
            delegates2.add( delegates[i] );
        }
        return new CompositeIterator<E>( delegates2 );
    }

    /**
     * Factory method.
     *
     * @param delegate1 the first Iterator to delegate to
     * @param delegate2 the second Iterator to delegate to
     * @return the created CompositeIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeIterator<E> createFromIterators(
            Iterator<E> delegate1,
            Iterator<E> delegate2 )
    {
        List<Iterator<E>> delegates2 = new ArrayList<Iterator<E>>( 2 );
        delegates2.add( delegate1 );
        delegates2.add( delegate2 );

        return new CompositeIterator<E>( delegates2 );
    }

    /**
     * Factory method.
     *
     * @param delegate1 the first Iterator to delegate to
     * @param delegate2 the second Iterator to delegate to
     * @param delegate3 the third Iterator to delegate to
     * @return the created CompositeIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeIterator<E> createFromIterators(
            Iterator<E> delegate1,
            Iterator<E> delegate2,
            Iterator<E> delegate3 )
    {
        List<Iterator<E>> delegates2 = new ArrayList<Iterator<E>>( 2 );
        delegates2.add( delegate1 );
        delegates2.add( delegate2 );
        delegates2.add( delegate3 );

        return new CompositeIterator<E>( delegates2 );
    }

    /**
     * Factory method.
     *
     * @param delegates the Enumerations to delegate to, in sequence
     * @return the created CompositeIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeIterator<E> createFromEnumerations(
            List<Enumeration<E>> delegates )
    {
        return new CompositeIterator<E>( delegates );
    }

    /**
     * Factory method.
     *
     * @param delegates the Enumeration to delegate to, in sequence
     * @return the created CompositeIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeIterator<E> createFromEnumerations(
            Enumeration<E> [] delegates )
    {
        List<Enumeration<E>> delegates2 = new ArrayList<Enumeration<E>>( delegates.length );
        for( int i=0 ; i<delegates.length ; ++i ) {
            delegates2.add( delegates[i] );
        }
        return new CompositeIterator<E>( delegates2 );
    }

    /**
     * Factory method.
     *
     * @param delegate1 the first Enumeration to delegate to
     * @param delegate2 the second Enumeration to delegate to
     * @return the created CompositeIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeIterator<E> createFromEnumerations(
            Enumeration<E> delegate1,
            Enumeration<E> delegate2 )
    {
        List<Enumeration<E>> delegates2 = new ArrayList<Enumeration<E>>( 2 );
        delegates2.add( delegate1 );
        delegates2.add( delegate2 );

        return new CompositeIterator<E>( delegates2 );
    }

    /**
     * Factory method.
     *
     * @param delegate1 the first Enumeration to delegate to
     * @param delegate2 the second Enumeration to delegate to
     * @param delegate3 the third Enumeration to delegate to
     * @return the created CompositeIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeIterator<E> createFromEnumerations(
            Enumeration<E> delegate1,
            Enumeration<E> delegate2,
            Enumeration<E> delegate3 )
    {
        List<Enumeration<E>> delegates2 = new ArrayList<Enumeration<E>>( 3 );
        delegates2.add( delegate1 );
        delegates2.add( delegate2 );
        delegates2.add( delegate3 );

        return new CompositeIterator<E>( delegates2 );
    }

    /**
     * Constructor.
     *
     * @param delegates the Iterators to delegate to, in sequence
     */
    public CompositeIterator(
            List<?> delegates )
    {
        theDelegates = delegates;
        theIndex     = 0;        
    }
    
    /**
     * Returns <tt>true</tt> if the iteration has more elements.
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext()
    {
        if( theIndex >= theDelegates.size() ) {
            return false;
        } else {
            return hasNext( theDelegates.get( theIndex ) );
        }
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    @SuppressWarnings(value={"unchecked"})
    public E next()
    {
        if( theIndex >= theDelegates.size() ) {
            throw new NoSuchElementException();
        }

        Object current = theDelegates.get( theIndex );
        E      ret;

        if( current instanceof Enumeration ) {
            ret = (E) ((Enumeration) current ).nextElement();
        } else {
            ret = ((Iterator<E>) current ).next();
        }
        
        goAdvance();
        
        return ret;
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt>
     *		  operation is not supported by this Iterator.
     
     * @throws IllegalStateException if the <tt>next</tt> method has not
     *		  yet been called, or the <tt>remove</tt> method has already
     *		  been called after the last call to the <tt>next</tt>
     *		  method.
     */
    public void remove()
    {
        if( theIndex >= theDelegates.size() ) {
            throw new IllegalStateException();
        }

        Object current = theDelegates.get( theIndex );

        if( current instanceof Enumeration ) {
            throw new UnsupportedOperationException( "underlying java.util.Enumeration does not support this method" );
        } else {
            ((Iterator) current).remove();
        }
    }

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return  <code>true</code> if and only if this enumeration object
     *           contains at least one more element to provide;
     *          <code>false</code> otherwise.
     */
    public final boolean hasMoreElements()
    {
        return hasNext();
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return     the next element of this enumeration.
     * @throws  NoSuchElementException  if no more elements exist.
     */
    public final E nextElement()
    {
        return next();
    }
    
    /**
     * Advance to the next element.
     */
    protected void goAdvance()
    {
        while( true ) {
            if( theIndex >= theDelegates.size() ) {
                break; // we are done, no more
            }
            
            if( hasNext( theDelegates.get( theIndex ) ) ) {
                break; // found one
            }
            
            ++theIndex;
        }
    }

    /**
     * Determine whether this Iterator has a next element.
     *
     * @param candidate the Iterator or Enumeration to consider
     * @return true if it has a next element
     */
    protected boolean hasNext(
            Object candidate )
    {
        if( candidate instanceof Enumeration ) {
            return ((Enumeration) candidate).hasMoreElements();
        } else {
            return ((Iterator) candidate).hasNext();
        }
    }

    /**
     * The delegate Iterators from which the elements are obtained. This is stored as
     * <code>Object</code> so both Iterators and Enumerations are supported.
     */
    protected List<?> theDelegates;
    
    /**
     * Index into theDelegates array that identifies the currently active Iterator.
     */
    protected int theIndex;
}
