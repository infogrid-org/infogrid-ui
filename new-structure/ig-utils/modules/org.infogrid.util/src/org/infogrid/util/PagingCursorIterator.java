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
 * A CursorIterator that pages through a set represented by another CursorIterator.
 *
 * @param <E> the type of element to iterate over
 */
public class PagingCursorIterator<E>
    extends
        AbstractCursorIterator<E>
{
    /**
     * Factory method.
     *
     * @param start the start element
     * @param pageLength the length of one page
     * @param delegate the underlying iterator
     * @param arrayComponentType the <code>Class</code> that should be used to create arrays for return values
     * @param <E> the type of element to iterate over
     * @return the created PagingCursorIterator
     */
    public static <E> PagingCursorIterator<E> create(
            E                 start,
            int               pageLength,
            CursorIterator<E> delegate,
            Class<E>          arrayComponentType )
    {
        if( start != null ) {
            delegate.moveToBefore( start );
        } else {
            delegate.moveToBeforeFirst();
        }

        return new PagingCursorIterator<E>( start, 0, pageLength, delegate, arrayComponentType );
    }

    /**
     * Constructor.
     *
     * @param start the start element
     * @param position the current position
     * @param pageLength the length of one page
     * @param delegate the underlying iterator
     * @param arrayComponentType the <code>Class</code> that should be used to create arrays for return values
     */
    @SuppressWarnings("unchecked")
    protected PagingCursorIterator(
            E                 start,
            int               position,
            int               pageLength,
            CursorIterator<E> delegate,
            Class<E>          arrayComponentType )
    {
        super( arrayComponentType );

        theStart      = start;
        thePosition   = position;
        thePageLength = pageLength;
        theDelegate = delegate;
    }

    /**
     * Obtain the page length.
     *
     * @return the page length
     */
    public int getPageLength()
    {
        return thePageLength;
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
        if( thePosition + n > thePageLength ) {
            return false;
        }
        return theDelegate.hasNext( n );
    }

    /**
     * Returns <tt>true</tt> if the iteration has at least N more elements in the backward direction.
     *
     * @param n the number of elements for which to check
     * @return <tt>true</tt> if the iterator has at least N more elements in the backward direction.
     * @see #hasNext()
     * @see #hasPrevious()
     * @see #hasNext(int)
     */
    public boolean hasPrevious(
            int n )
    {
        if( thePosition < n ) {
            return false;
        }
        return true; // correct by construction
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     * @see #previous()
     */
    public E next()
        throws
            NoSuchElementException
    {
        if( thePosition < thePageLength ) {
            ++thePosition;
            return theDelegate.next();
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the previous element in the iteration.
     *
     * @return the previous element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     * @see #next()
     */
    public E previous()
        throws
            NoSuchElementException
    {
        if( thePosition > 0 ) {
            E ret = theDelegate.previous();
            --thePosition; // only change position after previous succeeded
            return ret;
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
        int ret;
        
        if( theStart != null ) {
            ret = theDelegate.moveToBefore( theStart );
        } else {
            ret = theDelegate.moveToBeforeFirst();
        }
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
        int steps = thePageLength - thePosition;
        if( theDelegate.hasNext( steps )) {
            theDelegate.moveBy( steps );
            thePosition = thePageLength;
            return steps;
            
        } else {
            int ret = theDelegate.moveToAfterLast();
            thePosition += ret;
            return ret;
        }
    }

    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public PagingCursorIterator<E> createCopy()
    {
        PagingCursorIterator<E> ret = new PagingCursorIterator<E>(
                theStart,
                thePosition,
                thePageLength,
                theDelegate.createCopy(),
                theArrayComponentType );

        return ret;
    }

    /**
     * Set this CursorIterator to the position represented by the provided CursorIterator.
     *
     * @param position the position to set this CursorIterator to
     * @throws IllegalArgumentException thrown if the provided CursorIterator does
     *         not work on the same CursorIterable, or the implementations were incompatible.
     */
    public void setPositionTo(
            CursorIterator<E> position )
        throws
            IllegalArgumentException
    {
        // guess we do the slow approach.
        PagingCursorIterator<E> testing = createCopy();
        testing.moveToBeforeFirst();

        E rightHere = position.peekNext();

        while( testing.hasNext() ) {
            E found = testing.next();

            if( found == rightHere ) {
                // found
                testing.previous();
                thePosition = testing.thePosition;
                theDelegate.setPositionTo( testing.theDelegate );

                return;
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * The underlying CursorIterator.
     */
    protected CursorIterator<E> theDelegate;

    /**
     * The start element.
     */
    protected E theStart;

    /**
     * The maximum number of elements to return.
     */
    protected int thePageLength;

    /**
     * The current index within a page.
     */
    protected int thePosition;
}
