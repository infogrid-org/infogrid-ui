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

package org.infogrid.util;

import java.util.NoSuchElementException;

/**
 * An interator for arrays. It supports the <code>java.util.Enumeration</code>,
 * <code>java.util.Iterator</code> and {@link CursorIterator} interfaces.
 * 
 * @param <E> the type of element to iterate over
 */
public class ArrayCursorIterator<E>
        extends
            AbstractCursorIterator<E>
        implements
            CursorIterator<E>
{
    /**
     * Factory method. Position cursor at the beginning of the array. Iterate over the
     * entire array.
     *
     * @param array the array to iterate over
     * @return the created ArrayCursorIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> ArrayCursorIterator<E> create(
            E [] array )
    {
        return new ArrayCursorIterator<E>( array, 0, 0, array.length );
    }

    /**
     * Factory method. Position cursor at a given position of the array. Iterate over the
     * entire array.
     * 
     * @param array the array to iterate over
     * @param startPosition the start position
     * @return the created ArrayCursorIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> ArrayCursorIterator<E> create(
            E [] array,
            int  startPosition )
    {
        return new ArrayCursorIterator<E>( array, startPosition, 0, array.length );
    }

    /**
     *  Factory method. Position cursor at a given position of the array. Iterate over the
     * slice of the array defined by lowerBound (inclusive) and upperBound (exclusive).
     * 
     * @param array the array to iterate over
     * @param startPosition the start position
     * @param lowerBound the lowest index in the array to return (inclusive)
     * @param upperBound the highest index in the array to return (exclusive)
     * @return the created ArrayCursorIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> ArrayCursorIterator<E> create(
            E [] array,
            int  startPosition,
            int  lowerBound,
            int  upperBound )
    {
        return new ArrayCursorIterator<E>( array, startPosition, lowerBound, upperBound );
    }

    /**
     * Constructor. Position cursor at a given position of the array. Iterate over the
     * slice of the array defined by lowerBound (inclusive) and upperBound (exclusive).
     * 
     * @param array the array to iterate over
     * @param startPosition the start position
     * @param lowerBound the lowest index in the array to return (inclusive)
     * @param upperBound the highest index in the array to return (exclusive)
     */
    @SuppressWarnings(value={"unchecked"})
    protected ArrayCursorIterator(
            E [] array,
            int  startPosition,
            int  lowerBound,
            int  upperBound )
    {
        super( (Class<E>) array.getClass().getComponentType() );

        if( upperBound > array.length ) {
            throw new IllegalArgumentException( "Upperbound higher than length of array" );
        }
        if( lowerBound < 0 ) {
            throw new IllegalArgumentException( "Lowerbound cannot be negative" );
        }
        if( lowerBound > upperBound ) {
            // they can be the same, in which case this turns into a ZeroElementIterator
            throw new IllegalArgumentException( "Upperbound must be higher than lowerbound" );
        }
        if( startPosition < lowerBound ) {
            throw new IllegalArgumentException( "Start position cannot be lower than lowerbound" );
        }
        if( startPosition > upperBound ) {
            throw new IllegalArgumentException( "Start position cannot be higher than upperbound" );
        }
        
        theArray      = array;
        thePosition   = startPosition;
        theLowerBound = lowerBound;
        theUpperBound = upperBound;
    }

    /**
     * Obtain the next element, without iterating forward.
     *
     * @return the next element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    @Override
    public E peekNext()
    {
        if( thePosition >= theLowerBound && thePosition < theUpperBound ) {
            return theArray[ thePosition ];
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Obtain the previous element, without iterating backward.
     *
     * @return the previous element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    @Override
    public E peekPrevious()
    {
        if( thePosition > theLowerBound && thePosition <= theUpperBound ) {
            return theArray[ thePosition-1 ];
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements in the forward direction.
     *
     * @return <tt>true</tt> if the iterator has more elements in the forward direction.
     * @see #hasPrevious()
     * @see #hasPrevious(int)
     * @see #hasNext(int)
     */
    public boolean hasNext(
            int n )
    {
        boolean ret = thePosition + n <= theUpperBound; // in position 0, next(1) should return true if theArray.length is 1
        return ret;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements in the backward direction.
     *
     * @return <tt>true</tt> if the iterator has more elements in the backward direction.
     * @see #hasNext()
     * @see #hasPrevious(int)
     * @see #hasNext(int)
     */
    public boolean hasPrevious(
            int n )
    {
        boolean ret = thePosition - n >= theLowerBound;
        return ret;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @exception NoSuchElementException iteration has no more elements.
     */
    public E next()
    {
        if( thePosition >= theUpperBound ) {
            throw new NoSuchElementException();
        }
        E ret = theArray[ thePosition ];
        ++thePosition;        
        return ret;
    }

    /**
     * Returns the previous element in the iteration.
     *
     * @return the previous element in the iteration.
     * @exception NoSuchElementException iteration has no more elements.
     * @see #next()
     */
    public E previous()
    {
        if( thePosition <= theLowerBound ) {
            throw new NoSuchElementException();
        }
        --thePosition;

        E ret = theArray[ thePosition ];
        return ret;
    }

    /**
     * Obtain the current position, starting the count with 0 for the first element.
     *
     * @return the current position
     */
    public int getPosition()
    {
        return thePosition;
    }

    /**
     * Set the new position. Throws NoSuchElementException if the position does not exist.
     *
     * @param n the new position
     * @exception NoSuchElementException
     */
    public void setPosition(
            int n )
        throws
            NoSuchElementException
    {
        if( thePosition + n >= theUpperBound || thePosition - n < theLowerBound ) {
            throw new NoSuchElementException();
        }
        thePosition -= n;
    }

    /**
     * Move the cursor by N positions. Positive numbers indicate forward movemement;
     * negative numbers indicate backwards movement.
     * Throws NoSuchElementException if the position does not exist.
     *
     * @param n the number of positions to move
     * @exception NoSuchElementException
     */
    @Override
    public void moveBy(
            int n )
        throws
            NoSuchElementException
    {
        int newPosition = thePosition + n;
        if( newPosition >= theUpperBound || newPosition < theLowerBound ) {
            throw new NoSuchElementException();
        }
        thePosition = newPosition;
    }
    
    /**
     * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
     * right afterwards.
     *
     * @param pos the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    public int moveTo(
            E pos )
        throws
            NoSuchElementException
    {
        for( int i=0 ; i<theArray.length ; ++i ) {
            if( pos == theArray[i] ) {
                int ret = i - thePosition;
                thePosition = i;
                return ret;
            }
        }
        throw new NoSuchElementException();
    }
    
    /**
     * Move the cursor to just before the first element, i.e. return the first element when
     * {@link #next next} is invoked right afterwards.
     *
     * @return the number of steps that were taken to move. Positive number means
     *         forward, negative backward
     */
    public int moveToBeforeFirst()
    {
        int ret = -thePosition;
        thePosition = 0;
        
        return ret;
    }

    /**
     * Move the cursor to just after the last element, i.e. return the last element when
     * {@link #previous previous} is invoked right afterwards.
     *
     * @return the number of steps that were taken to move. Positive number means
     *         forward, negative backward
     */
    public int moveToAfterLast()
    {
        int ret = theArray.length - thePosition;
        thePosition = theArray.length;
        
        return ret;
        
    }

    /**
     * Reset the Iterator to the first position.
     */
    public void reset()
    {
        thePosition = 0;
    }

    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public ArrayCursorIterator<E> createCopy()
    {
        // keep position
        return new ArrayCursorIterator<E>( theArray, thePosition, theLowerBound, theUpperBound );
    }

    /**
     * Set this CursorIterator to the position represented by the provided CursorIterator.
     *
     * @param position the position to set this CursorIterator to
     * @throws IllegalArgumentException thrown if the provided CursorIterator did not work on the same CursorIterable,
     *         or the implementations were incompatible.
     */
    public void setPositionTo(
            CursorIterator<E> position )
        throws
            IllegalArgumentException
    {
        if( !( position instanceof ArrayCursorIterator )) {
            throw new IllegalArgumentException( "Wrong type of CursorIterator: " + position );
        }
        ArrayCursorIterator<E> realPosition = (ArrayCursorIterator<E>) position;

        if( theArray != realPosition.theArray ) {
            throw new IllegalArgumentException( "Not the same instance of array to iterate over" );
        }
        if( theLowerBound != realPosition.theLowerBound ) {
            throw new IllegalArgumentException( "Not the same lower bound" );
        }
        if( theUpperBound != realPosition.theUpperBound ) {
            throw new IllegalArgumentException( "Not the same upper bound" );
        }
        
        thePosition = realPosition.thePosition;
    }

    /**
      * The array that we are iterating over.
      */
    protected E [] theArray;

    /**
      * The current position in the array.
      */
    protected int thePosition;
    
    /**
     * The index of the lowest position in the array that we return (inclusive).
     */
    protected int theLowerBound;
    
    /**
     * The index of the highest position in the array that we return (exclusive).
     */
    protected int theUpperBound;
}
