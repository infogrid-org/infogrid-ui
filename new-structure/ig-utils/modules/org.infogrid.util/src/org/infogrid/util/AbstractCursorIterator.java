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

import java.util.NoSuchElementException;

/**
 * Factors out common behaviors of {@link CursorIterator}s. The default implementation
 * is rather inefficient, but works for all CursorIterators with little required work for
 * new implementations. Subclasses may want to override for performance reasons.
 * 
 * @param <E> the type of element to iterate over
 */
public abstract class AbstractCursorIterator<E>
        implements
            CursorIterator<E>
{
    /**
     * Constructor.
     * 
     * @param arrayComponentType the <code>Class</code> that should be used to create arrays for return values
     */
    protected AbstractCursorIterator(
            Class<E> arrayComponentType )
    {
        if( arrayComponentType == null ) {
            throw new IllegalArgumentException( "arrayComponentType must not be null" );
        }
        theArrayComponentType = arrayComponentType;
    }

    /**
     * Obtain the next element, without iterating forward.
     *
     * @return the next element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public E peekNext()
        throws
            NoSuchElementException
    {
        CursorIterator<E> clone = createCopy();
        E                 ret   = clone.next();
        
        return ret;
    }

    /**
     * Obtain the previous element, without iterating backward.
     *
     * @return the previous element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public E peekPrevious()
        throws
            NoSuchElementException
    {
        CursorIterator<E> clone = createCopy();
        E                 ret   = clone.previous();
        
        return ret;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements in the forward direction.
     *
     * @return <tt>true</tt> if the iterator has more elements in the forward direction.
     * @see #hasPrevious()
     * @see #hasPrevious(int)
     * @see #hasNext(int)
     */
    public boolean hasNext()
    {
        return hasNext( 1 );
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements in the backward direction.
     *
     * @return <tt>true</tt> if the iterator has more elements in the backward direction.
     * @see #hasNext()
     * @see #hasPrevious(int)
     * @see #hasNext(int)
     */
    public boolean hasPrevious()
    {
        return hasPrevious( 1 );
    }

    /**
     * <p>Obtain the next N elements. If fewer than N elements are available, return
     * as many elements are available in a shorter array.</p>
     * 
     * @param n the number of elements to return
     * @return the next no more than N elements
     * @see #previous(int)
     */
    public E [] next(
            int  n )
    {
        E [] ret = ArrayHelper.createArray( theArrayComponentType, n );
        
        int count = 0;
        while( count < n && hasNext() ) {
            ret[ count++ ] = next();
        }
        if( count < ret.length ) {
            ret = ArrayHelper.copyIntoNewArray( ret, 0, count, theArrayComponentType );
        }
        return ret;
    }

    /**
     * <p>Obtain the previous N elements. If fewer than N elements are available, return
     * as many elements are available in a shorter array.</p>
     * 
     * <p>Note that the elements
     * will be ordered in the opposite direction as you might expect: they are
     * returned in the sequence in which the CursorIterator visits them, not in the
     * sequence in which the underlying {@link CursorIterable} stores them.</p>
     *
     * @param n the number of elements to return
     * @return the previous no more than N elements
     * @see #next(int)
     */
    public E [] previous(
            int  n )
    {
        E [] ret = ArrayHelper.createArray( theArrayComponentType, n );
        
        int count = 0;
        while( count < n && hasPrevious() ) {
            ret[ count++ ] = previous();
        }
        if( count < ret.length ) {
            ret = ArrayHelper.copyIntoNewArray( ret, 0, count, theArrayComponentType );
        }
        return ret;
    }

    /**
     * Move the cursor by N positions. Positive numbers indicate forward movemement;
     * negative numbers indicate backward movement. This can move all the way forward
     * to the position "past last" and all the way backward to the position "before first".
     *
     * @param n the number of positions to move
     * @throws NoSuchElementException thrown if the position does not exist
     */
    public void moveBy(
            int n )
        throws
            NoSuchElementException
    {
        if( n > 0 ) {
            E [] temp = next( n );
            if( temp.length < n-1 ) {
                throw new NoSuchElementException();
            }
        } else if( n < 0 ) {
            E [] temp = previous( -n );
            if( temp.length < -n-1 ) {
                throw new NoSuchElementException();
            }
        } // else nothing
    }

    /**
     * Move the cursor to just before this element, i.e. return this element when
     * {@link #next next} is invoked right afterwards.
     *
     * @param pos the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means
     *         forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part
     *         of the underlying {@link CursorIterable}
     */
    public int moveToBefore(
            E pos )
        throws
            NoSuchElementException
    {
        // by default, we have to search.

        CursorIterator<E> currentPosition = createCopy();

        int count = 0;
        while( hasNext() ) {
            E found = peekNext();
            if( pos.equals( found )) {
                return count;
            }
            ++count;
            next();
        }
        
        setPositionTo( currentPosition );

        count = 0;
        while( hasPrevious() ) {
            E found = previous();
            --count;
            if( pos.equals( found )) {
                return count;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Move the cursor to just after this element, i.e. return this element when
     * {@link #previous previous} is invoked right afterwards.
     *
     * @param pos the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means
     *         forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part
     *         of the underlying {@link CursorIterable}
     */
    public int moveToAfter(
            E pos )
        throws
            NoSuchElementException
    {
        // by default, we have to search.

        CursorIterator<E> currentPosition = createCopy();

        int count = 0;
        while( hasNext() ) {
            E found = next();
            ++count;
            if( pos.equals( found )) {
                return count;
            }
        }
        
        setPositionTo( currentPosition );

        count = 0;
        while( hasPrevious() ) {
            E found = peekPrevious();
            if( pos.equals( found )) {
                return count;
            }
            --count;
            previous();
        }
        throw new NoSuchElementException();
    }

    /**
      * Do we have more elements?
      *
      * @return true if we have more elements
      */
    public final boolean hasMoreElements()
    {
        return hasNext();
    }

    /**
      * Return next element and iterate.
      *
      * @return the next element
      */
    public final E nextElement()
    {
        return next();
    }

    /**
     * We don't know how to remove on this level.
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Obtain a {@link CursorIterator} instead of a <code>java.util.Iterator</code>.
     *
     * @return the <code>AbstractCursorIterator</code>
     */
    public AbstractCursorIterator<E> iterator()
    {
        return this;
    }

    /**
     * Obtain a {@link CursorIterator} instead of a <code>java.util.Iterator</code>.
     * This performs the exact same operation as {@link #iterator iterator}, but is
     * friendlier towards JSPs and other software that likes to use JavaBeans conventions.
     *
     * @return the <code>AbstractCursorIterator</code>
     */
    public final AbstractCursorIterator<E> getIterator()
    {
        return iterator();
    }

    /**
     * The array component type for returned values.
     */
    protected Class<E> theArrayComponentType;
}
