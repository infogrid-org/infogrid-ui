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
 * A {@link CursorIterator} that iterates over a subset of another <code>CursorIterator</code>.
 * 
 * @param <E> the type of element to iterate over
 */
public class SubsettingCursorIterator<E>
        extends
            AbstractCursorIterator<E>
{
    /**
     * Factory method.
     *
     * @param min the minimum index in the array that we return (inclusive).
     * @param max the maximum index in the array that we return (exclusive).
     * @param delegate the underlying iterator
     * @param arrayComponentType the component type for to-be-created arrays
     * @param <E> the type of element to iterate over
     * @return the created SubsettingCursorIterator
     */
    public static <E> SubsettingCursorIterator<E> create(
            E                 min,
            E                 max,
            CursorIterator<E> delegate,
            Class<E>          arrayComponentType )
    {
        return new SubsettingCursorIterator<E>( min, max, delegate, arrayComponentType );
    }

    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param min the minimum index in the array that we return (inclusive).
     * @param max the maximum index in the array that we return (exclusive).
     * @param delegate the underlying iterator
     * @param arrayComponentType the component type for to-be-created arrays
     */
    protected SubsettingCursorIterator(
            E                 min,
            E                 max,
            CursorIterator<E> delegate,
            Class<E>          arrayComponentType )
    {
        super( arrayComponentType );

        theMin                = min;
        theMax                = max;
        theDelegate           = delegate;

        if( theMin != null ) {
            theDelegate.moveToBefore( theMin );
        } else {
            theDelegate.moveToBeforeFirst();
        }
    }

    /**
     * Obtain the minimum index that we return (inclusive).
     *
     * @return the minimum index, or null
     */
    public E getMin()
    {
        return theMin;
    }

    /**
     * Obtain the maximum index that we return (exclusive).
     *
     * @return the maximum index, or null
     */
    public E getMax()
    {
        return theMax;
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
        E ret = theDelegate.peekNext();
        if( ret != theMax ) {
            return ret;
        } else {
            throw new NoSuchElementException();
        }
    }
    
    /**
     * Obtain the previous element, without iterating backwards.
     *
     * @return the previous element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    @Override
    public E peekPrevious()
    {
        E ret = theDelegate.peekPrevious();
        if( ret != theMin ) {
            return ret;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns <tt>true</tt> if the iteration has at least N more elements in the forward direction.
     *
     * @param n the number of elements for which to check
     * @return <tt>true</tt> if the iterator has at least N more elements in the forward direction.
     * @see #hasNext()
     * @see #hasPrevious()
     * @see #hasPrevious(int)
     */
    public boolean hasNext(
            int n )
    {
        // we don't have any choice other than trying it
        CursorIterator<E> delegateTrial = theDelegate.createCopy();
        
        E [] found = delegateTrial.next( n );
        if( found.length < n ) {
            return false;
        }
        for( int i=0 ; i<found.length ; ++i ) {
            if( theMax == found[i] ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <tt>true</tt> if the iteration has at least N more elements in the backwards direction.
     *
     * @param n the number of elements for which to check
     * @return <tt>true</tt> if the iterator has at least N more elements in the backwards direction.
     * @see #hasNext()
     * @see #hasPrevious()
     * @see #hasNext(int)
     */
    public boolean hasPrevious(
            int n )
    {
        // we don't have any choice other than trying it
        CursorIterator<E> delegateTrial = theDelegate.createCopy();
        
        E [] found = delegateTrial.previous( n );
        if( found.length < n ) {
            return false;
        }
        for( int i=0 ; i<found.length ; ++i ) {
            if( theMin == found[i] ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public E next()
    {
        E ret = theDelegate.next();
        if( ret == theMax ) {
            theDelegate.previous();
            throw new NoSuchElementException();
        }
        return ret;
    }

    /**
     * <p>Obtain the next N elements. If fewer than N elements are available, return
     * as many elements are available in a shorter array.</p>
     * 
     * @return the next no more than N elements
     * @param n a (possibly 0-sized) array of the type that shall be returned in this class
     * @see #previous(int)
     */
    @Override
    public E [] next(
            int n )
    {
        E [] almost = theDelegate.next( n );
        
        for( int i=0 ; i<almost.length ; ++i ) {
            if( theMin == almost[i] ) {
                // not enough element in subset
                E [] ret = ArrayHelper.copyIntoNewArray( almost, 0, i, theArrayComponentType );
                
                theDelegate.moveBy( i - almost.length );
                
                return ret;
            }
        }
        return almost;
    }

    /**
     * Returns the previous element in the iteration.
     *
     * @return the previous element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     * @see #next()
     */
    public E previous()
    {
        E ret = theDelegate.previous();
        if( ret == theMin ) {
            theDelegate.next();
            throw new NoSuchElementException();
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
     * sequence in which the underlying Iterable stores them.</p>
     *
     * @return the previous no more than N elements
     * @param n a (possibly 0-sized) array of the type that shall be returned in this class
     * @see #next(int)
     */
    @Override
    public E [] previous(
            int n )
    {
        E [] almost = theDelegate.previous( n );
        
        for( int i=0 ; i<almost.length ; ++i ) {
            if( theMin == almost[i] ) {
                // not enough element in subset
                E [] ret = ArrayHelper.copyIntoNewArray( almost, 0, i, theArrayComponentType );
                
                theDelegate.moveBy( almost.length-i );
                
                return ret;
            }
        }
        return almost;
    }


    /**
     * Move the cursor by N positions. Positive numbers indicate forward movemement;
     * negative numbers indicate backward movement. This can move all the way forward
     * to the position "past last" and all the way backward to the position "before first".
     *
     * @param n the number of positions to move
     * @throws NoSuchElementException thrown if the position does not exist
     */
    @Override
    public void moveBy(
            int n )
        throws
            NoSuchElementException
    {
        CursorIterator<E> delegateTrial = theDelegate.createCopy();
        
        for( int i=0 ; i<n ; ++i ) {
            try {
                E found = delegateTrial.next();

                if( found == theMax ) { // FIXME?
                    // not enough elements in subset
                    throw new NoSuchElementException();
                }
            } catch( NoSuchElementException ex ) {
                // not enough elements in delegate iterator
                throw new NoSuchElementException();
            }
        }
        // did work, move
        theDelegate = delegateTrial;
    }

    /**
     * Move the cursor to this element, i.e. return this element when {@link #next} is invoked
     * right afterwards.
     *
     * @param pos the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    @Override
    public int moveToBefore(
            E pos )
        throws
            NoSuchElementException
    {
        // FIXME? Is this right?
        CursorIterator<E> delegateTrial1 = theDelegate.createCopy();
        int steps;
        try {
            steps = delegateTrial1.moveToBefore( pos );
        } catch( NoSuchElementException ex ) {
            // delegate does not have enough elements
            throw new NoSuchElementException();
        }
        
        CursorIterator<E> delegateTrial2 = theDelegate.createCopy();
        E [] found;
        E    limit;
        if( steps >= 0 ) {
            found = delegateTrial2.next( steps );
            limit = theMax;
        } else {
            found = delegateTrial2.previous( -steps );
            limit = theMin;
        }
        
        for( int i=0 ; i<found.length ; ++i ) {
            if( limit == found[i] ) {
                throw new NoSuchElementException();
            }
        }
        theDelegate = delegateTrial1;
        return steps;
    }
    
    /**
     * Move the cursor to this element, i.e. return this element when {@link #previous} is invoked
     * right afterwards.
     *
     * @param pos the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    @Override
    public int moveToAfter(
            E pos )
        throws
            NoSuchElementException
    {
        // FIXME? Is this right?
        CursorIterator<E> delegateTrial1 = theDelegate.createCopy();
        int steps;
        try {
            steps = delegateTrial1.moveToAfter( pos );
        } catch( NoSuchElementException ex ) {
            // delegate does not have enough elements
            throw new NoSuchElementException();
        }
        
        CursorIterator<E> delegateTrial2 = theDelegate.createCopy();
        E [] found;
        E    limit;
        if( steps >= 0 ) {
            found = delegateTrial2.next( steps );
            limit = theMax;
        } else {
            found = delegateTrial2.previous( -steps );
            limit = theMin;
        }
        
        for( int i=0 ; i<found.length ; ++i ) {
            if( limit == found[i] ) {
                throw new NoSuchElementException();
            }
        }
        theDelegate = delegateTrial1;
        return steps;
    }
    
    /**
     * 
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation). This is the same as the current element.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt>
     *		  operation is not supported by this Iterator.

     * @throws IllegalStateException if the <tt>next</tt> method has not
     *		  yet been called, or the <tt>remove</tt> method has already
     *		  been called after the last call to the <tt>next</tt>
     *		  method.
     */
    @Override
    public void remove()
    {
        theDelegate.remove();
    }

    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public SubsettingCursorIterator<E> createCopy()
    {
        return new SubsettingCursorIterator<E>( theMin, theMax, theDelegate.createCopy(), theArrayComponentType );
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
        if( !( position instanceof SubsettingCursorIterator )) {
            throw new IllegalArgumentException( "Wrong type of CursorIterator: " + position );
        }
        SubsettingCursorIterator<E> realPosition = (SubsettingCursorIterator<E>) position;

        if( theMin != realPosition.theMin ) {
            throw new IllegalArgumentException( "Not the same lower bound" );
        }
        if( theMax != realPosition.theMax ) {
            throw new IllegalArgumentException( "Not the same upper bound" );
        }
        
        theDelegate.setPositionTo( realPosition.theDelegate );
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
        return theDelegate.moveToBefore( theMin );
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
        return theDelegate.moveToAfter( theMax );
    }

    /**
     * The delegate iterator.
     */
    protected CursorIterator<E> theDelegate;

    /**
     * The minimum element to return (inclusive).
     */
    protected E theMin;
    
    /**
     * The maximum element to return (exclusive).
     */
    protected E theMax;
}
