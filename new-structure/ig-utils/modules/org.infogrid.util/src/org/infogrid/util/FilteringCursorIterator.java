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
 * A {@link CursorIterator} that iterates over a subset of elements returned by a delegate
 * CursorIterator. The position of the Iterator is assumed to be on the next element
 * to be returned going forward, and one after than the next element to be returned
 * going backwards.
 * 
 * @param <E> the type of element to iterate over
 */
public class FilteringCursorIterator<E>
        extends
            AbstractCursorIterator<E>
{
    /**
     * Factory method.
     *
     * @param delegate the delegate CursorIterator
     * @param filter the Filter to use that determines which elements from the underlying CursorIterator to select
     * @param arrayComponentType component type of the array that is allocated for return values
     * @return the created FilteringCursorIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> FilteringCursorIterator<E> create(
            CursorIterator<E> delegate,
            Filter<E>         filter,
            Class<E>          arrayComponentType )
    {
        return new FilteringCursorIterator<E>( delegate, filter, arrayComponentType );
    }

    /**
     * Constructor.
     *
     * @param delegate the delegate CursorIterator
     * @param filter the Filter to use that determines which elements from the underlying CursorIterator to select
     * @param arrayComponentType component type of the array that is allocated for return values
     */
    protected FilteringCursorIterator(
            CursorIterator<E> delegate,
            Filter<E>         filter,
            Class<E>          arrayComponentType )
    {
        super( arrayComponentType );
        
        theDelegate = delegate;
        theFilter   = filter;
        
        theIsAhead = false;
        theIsBack  = false;
 
        try {
            if( filter.accept( delegate.peekNext() )) {
                theIsAhead = true;
            }
        } catch( NoSuchElementException ex ) {
            // that's fine
            theIsAhead = true;
        }
        try {
            if( filter.accept( delegate.peekPrevious() )) {
                theIsBack = true;
            }
        } catch( NoSuchElementException ex ) {
            // that's fine
            theIsBack = true;
        }
    }

    /**
     * Constructor.
     *
     * @param delegate the delegate CursorIterator
     * @param filter the Filter to use that determines which elements from the underlying CursorIterator to select
     * @param arrayComponentType component type of the array that is allocated for return values
     * @param isAhead if true, the delegate iterator is as far ahead as possible.
     * @param isBack if true, the delegate iterator is as far back as possible.
     */
    protected FilteringCursorIterator(
            CursorIterator<E> delegate,
            Filter<E>         filter,
            Class<E>          arrayComponentType,
            boolean           isAhead,
            boolean           isBack )
    {
        super( arrayComponentType );
        
        theDelegate = delegate;
        theFilter   = filter;

        theIsAhead = isAhead;
        theIsBack  = isBack;
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
        if( !theIsAhead ) {
            moveAhead();
        }
        E ret = theDelegate.peekNext();
        return ret;
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
        if( !theIsBack ) {
            moveBack();
        }
        E ret = theDelegate.peekPrevious();
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
    @Override
    public boolean hasNext()
    {
        if( !theIsAhead ) {
            moveAhead();
        }
        return theDelegate.hasNext();
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements in the backwards direction.
     *
     * @return <tt>true</tt> if the iterator has more elements in the backwards direction.
     * @see #hasNext()
     * @see #hasPrevious(int)
     * @see #hasNext(int)
     */
    @Override
    public boolean hasPrevious()
    {
        if( !theIsBack ) {
            moveBack();
        }
        return theDelegate.hasPrevious();
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
        // gotta try it out
        if( !theIsAhead ) {
            moveAhead();
        }
        CursorIterator<E> temp = theDelegate.createCopy();
        
        int count = 0;
        while( count < n && temp.hasNext() ) {
            E current = temp.next();
            if( theFilter.accept( current )) {
                ++count;
            }
        }
        if( count == n ) {
            return true;
        } else {
            return false;
        }
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
        // gotta try it out
        if( !theIsBack ) {
            moveBack();
        }
        CursorIterator<E> temp = theDelegate.createCopy();
        
        int count = 0;
        while( count < n && temp.hasPrevious() ) {
            E current = temp.previous();
            if( theFilter.accept( current )) {
                ++count;
            }
        }
        if( count == n ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public E next()
    {
        if( !theIsAhead ) {
            moveAhead();
        }
        E ret = theDelegate.next();
        theIsAhead = false; // it might be true, but moveAhead will catch this the next time
        return ret;        
    }

    // use default implementations for next(int) and previous(int) from superclass

    /**
     * Returns the previous element in the iteration.
     *
     * @return the previous element in the iteration.
     * @see #next()
     */
    public E previous()
    {
        if( !theIsBack ) {
            moveBack();
        }
        E ret = theDelegate.previous();
        theIsBack = false; // it might be true, but moveBack will catch this the next time
        return ret;        
    }

    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public CursorIterator<E> createCopy()
    {
        return new FilteringCursorIterator<E>(
                theDelegate.createCopy(),
                theFilter,
                theArrayComponentType,
                theIsAhead,
                theIsBack );
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
        if( !( position instanceof FilteringCursorIterator )) {
            throw new IllegalArgumentException( "Wrong type of CursorIterator: " + position );
        }
        FilteringCursorIterator<E> realPosition = (FilteringCursorIterator<E>) position;

        if( theFilter != realPosition.theFilter ) {
            throw new IllegalArgumentException( "Not the same instance of Filter" );
        }
        
        theDelegate.setPositionTo( realPosition.theDelegate ); // this may throw
        
        theIsAhead = realPosition.theIsAhead;
        theIsBack  = realPosition.theIsBack;
    }

    /**
     * Move to the next valid element.
     */
    protected void moveAhead()
    {
        theIsAhead = true; // regardless what else happens
        while( theDelegate.hasNext() ) {
            if( theFilter.accept( theDelegate.peekNext() )) {
                return;
            }
            theIsBack = false;
            theDelegate.next();
        }
    }

    /**
     * Move to the next previous element.
     */
    protected void moveBack()
    {
        theIsBack = true; // regardless what else happens
        while( theDelegate.hasPrevious() ) {
            if( theFilter.accept( theDelegate.peekPrevious() )) {
                return;
            }
            theIsAhead = false;
            theDelegate.previous();
        }
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
        int ret = 0;
        while( theDelegate.hasPrevious() ) {
            E candidate = theDelegate.previous();
            if( theFilter.accept( candidate )) {
                --ret;
            }
        }
        theIsAhead = false;

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
        int ret = 0;
        while( theDelegate.hasNext() ) {
            E candidate = theDelegate.next();
            if( theFilter.accept( candidate )) {
                ++ret;
            }
        }
        theIsBack = false;
        
        return ret;
    }

    /**
     * The filter.
     */
    protected Filter<E> theFilter;
    
    /**
     * The delegate iterator.
     */
    protected CursorIterator<E> theDelegate;
    
    /**
     * If true, the delegate iterator is as far ahead as possible.
     */
    protected boolean theIsAhead;
    
    /**
     * If true, the delegate iterator is as far back as possible.
     */
    protected boolean theIsBack;

    /**
     * This inner interface is implemented by users of FilteringIterator to determine whether
     * or not we should accept a certain Object.
     * 
     * @param <E> the type of element to iterate over
     */
    public static interface Filter<E>
    {
        /**
          * Determine whether or not to accept a candidate Object.
          *
          * @param candidate the candidate Object
          * @return true if this Object shall be accepted according to this Filter
          */
        public boolean accept(
                E candidate );
    }
}
