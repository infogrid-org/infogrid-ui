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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A CursorIterator that iterates over the content produced by one or more
 * delegate CursorIterators.
 * 
 * @param <E> the type of element to iterate over
 */
public class CompositeCursorIterator<E>
        extends
            AbstractCursorIterator<E>
        implements
            CursorIterator<E>        
{
    /**
     * Factory method.
     *
     * @param delegates the underlying delegate iterators
     * @param arrayComponentType the <code>Class</code> that should be used to create arrays for return values
     * @return the created CompositeCursorIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeCursorIterator<E> create(
            List<CursorIterator<E>> delegates,
            Class<E>                arrayComponentType )
    {
        CompositeCursorIterator<E> ret = new CompositeCursorIterator<E>( delegates, 0, arrayComponentType );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param delegates the underlying delegate iterators
     * @param arrayComponentType the <code>Class</code> that should be used to create arrays for return values
     * @return the created CompositeCursorIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeCursorIterator<E> create(
            CursorIterator<E> [] delegates,
            Class<E>             arrayComponentType )
    {
        List<CursorIterator<E>> temp = new ArrayList<CursorIterator<E>>();
        for( CursorIterator<E> current : delegates ) {
            temp.add( current );
        }

        CompositeCursorIterator<E> ret = new CompositeCursorIterator<E>( temp, 0, arrayComponentType );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param delegate1 the first underlying delegate iterator
     * @param delegate2 the second underlying delegate iterator
     * @param arrayComponentType the <code>Class</code> that should be used to create arrays for return values
     * @return the created CompositeCursorIterator
     * @param <E> the type of element to iterate over
     */
    public static <E> CompositeCursorIterator<E> create(
            CursorIterator<E> delegate1,
            CursorIterator<E> delegate2,
            Class<E>          arrayComponentType )
    {
        List<CursorIterator<E>> temp = new ArrayList<CursorIterator<E>>();
        temp.add( delegate1 );
        temp.add( delegate2 );

        CompositeCursorIterator<E> ret = new CompositeCursorIterator<E>( temp, 0, arrayComponentType );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param delegates the underlying delegate iterators
     * @param delegateIndex the current index into the delegates array
     * @param arrayComponentType the <code>Class</code> that should be used to create arrays for return values
     */
    protected CompositeCursorIterator(
            List<CursorIterator<E>> delegates,
            int                     delegateIndex,
            Class<E>                arrayComponentType )
    {
        super( arrayComponentType );
        
        theDelegates     = delegates;
        theDelegateIndex = delegateIndex;
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
        while( true ) {
            if( theDelegateIndex == theDelegates.size() ) {
                throw new NoSuchElementException();
            }
            CursorIterator<? extends E> currentDelegate = theDelegates.get( theDelegateIndex );
            if( currentDelegate.hasNext() ) {
                return currentDelegate.next();
            }
            ++theDelegateIndex;
            if( theDelegateIndex < theDelegates.size() ) {
                currentDelegate = theDelegates.get( theDelegateIndex );
                currentDelegate.moveToBeforeFirst();
            }
        }
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
        while( true ) {
            if( theDelegateIndex < theDelegates.size() ) {
                CursorIterator<? extends E> currentDelegate = theDelegates.get( theDelegateIndex );
                
                if( currentDelegate.hasPrevious() ) {
                    return currentDelegate.previous();
                }
            }
            if( theDelegateIndex == 0 ) {
                throw new NoSuchElementException();
            }
            --theDelegateIndex;
            CursorIterator<? extends E> currentDelegate = theDelegates.get( theDelegateIndex );
            currentDelegate.moveToAfterLast();
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
        if( n == 0 ) {
            return true;
        }
        if( theDelegateIndex >= theDelegates.size() ) {
            return false;
        }
        
        int count = 0;
        CursorIterator<E> hereIter = theDelegates.get( theDelegateIndex ).createCopy();
        while( hereIter.hasNext() && ++count < n ) {
            hereIter.next();            
        }
        if( count == n ) {
            return true;
        }
        // go to next iterators
        for( int index = theDelegateIndex+1 ; index < theDelegates.size() ; ++index ) {
            CursorIterator<E> current = theDelegates.get( index );
            current.moveToBeforeFirst();
            while( current.hasNext() && ++count < n ) {
                current.next();
            }
            if( count == n ) {
                return true;
            }
        }
        return false;
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
        if( n == 0 ) {
            return true;
        }
        if( theDelegateIndex == 0 ) {
            return theDelegates.get( 0 ).hasPrevious( n );
        }

        int count = 0;
        if( theDelegateIndex < theDelegates.size() ) {
            // not after the last
            CursorIterator<E> hereIter = theDelegates.get( theDelegateIndex ).createCopy();
            while( hereIter.hasPrevious() && ++count < n ) {
                hereIter.previous();
            }
            if( count == n ) {
                return true;
            }
        }
        // go to next iterators
        for( int index = theDelegateIndex-1 ; index >= 0 ; --index ) {
            CursorIterator<E> current = theDelegates.get( index );
            current.moveToAfterLast();
            while( current.hasPrevious() && ++count < n ) {
                current.previous();
            }
            if( count == n ) {
                return true;
            }
        }
        return false;
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
        // need to traverse to construct return value
        int ret = 0;
        while( theDelegateIndex >= 0 ) {
            if( theDelegateIndex == theDelegates.size() ) {
                --theDelegateIndex;
                theDelegates.get( theDelegateIndex ).moveToAfterLast();
            }
            ret += theDelegates.get( theDelegateIndex ).moveToBeforeFirst();
            --theDelegateIndex;
        }
        theDelegateIndex = 0;
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
        // need to traverse to construct return value
        int ret = 0;
        while( theDelegateIndex < theDelegates.size() ) {
            ret += theDelegates.get( theDelegateIndex ).moveToAfterLast();
            ++theDelegateIndex;
            if( theDelegateIndex < theDelegates.size() ) {
                theDelegates.get( theDelegateIndex ).moveToBeforeFirst();
            }
        }
        theDelegateIndex = theDelegates.size();
        return ret;
    }

    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public CompositeCursorIterator<E> createCopy()
    {
        // need a deep copy
        
        ArrayList<CursorIterator<E>> delegatesCopy = new ArrayList<CursorIterator<E>>();
        for( CursorIterator<E> current : theDelegates ) {
            CursorIterator<E> delegateCopy = current.createCopy();
            delegatesCopy.add( delegateCopy );
        }
        return new CompositeCursorIterator<E>( delegatesCopy, theDelegateIndex, theArrayComponentType );
    }
    
    /**
     * Set this CursorIterator to the position represented by the provided CursorIterator.
     *
     * @param position the position to set this CursorIterator to
     * @throws IllegalArgumentException thrown if the provided CursorIterator does
     *         not work on the same CursorIterable, or the implementations were incompatible.
     */
    @SuppressWarnings( "unchecked" )
    public void setPositionTo(
            CursorIterator<E> position )
        throws
            IllegalArgumentException
    {
        if( !( position instanceof CompositeCursorIterator )) {
            throw new IllegalArgumentException( "Wrong type of CursorIterator: " + position );
        }
        CompositeCursorIterator<E> realPosition = (CompositeCursorIterator<E>) position;

        if( hasSameContentInOrder( theDelegates, realPosition.theDelegates )) {
            throw new IllegalArgumentException( "Not the same delegates to iterate over" );
        }
        // we know it's the same content, so all casts are safe
        
        theDelegateIndex = realPosition.theDelegateIndex;
        if( theDelegateIndex < theDelegates.size() ) {
            CursorIterator newPosition = realPosition.theDelegates.get( theDelegateIndex );
            theDelegates.get( theDelegateIndex ).setPositionTo( newPosition );
        }
    }
    
    /**
     * Helper method to determine whether two Lists have the same content in the same order.
     * 
     * @param one the first List
     * @param two the second List
     * @return true they have the same content in order
     * @param <E> the type of objects in the to-be-compared Lists
     */
    protected static <E> boolean hasSameContentInOrder(
            List<E> one,
            List<E> two )
    {
        if( one.size() != two.size() ) {
            return false;
        }
        
        Iterator<E> oneIter = one.iterator();
        Iterator<E> twoIter = two.iterator();
        
        while( oneIter.hasNext() ) {
            E foundOne = oneIter.next();
            E foundTwo = twoIter.next();
            
            if( foundOne == null ) {
                if( foundTwo != null ) {
                    return false;
                }
            } else if( !foundOne.equals( foundTwo )) {
                return false;
            }
        }
        return true;
    }

    /**
     * The underlying delegate iterators.
     */
    protected List<CursorIterator<E>> theDelegates;
    
    /**
     * Index into the delegate iterators array.
     */
    protected int theDelegateIndex;
}
