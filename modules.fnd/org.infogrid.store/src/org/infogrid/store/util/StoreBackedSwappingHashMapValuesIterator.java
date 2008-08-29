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

package org.infogrid.store.util;

import org.infogrid.store.IterableStoreCursor;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.util.AbstractCursorIterator;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CursorIterator;

import java.util.NoSuchElementException;

/**
 * Iterates over the values in a StoreBackedSwappingHashMap.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public class StoreBackedSwappingHashMapValuesIterator<K,V>
        extends
            AbstractCursorIterator<V>
{
    /**
     * Constructor.
     *
     * @param delegate the underling Iterator over the StoreValues in the Store
     * @param cache the in-memory cache in the StoreMeshBase
     * @param mapper the mapper to use
     * @param keysArrayComponentClass the Class to use when returning arrays of keys
     * @param valuesArrayComponentClass the Class to use when returning arrays of values
     */
    public StoreBackedSwappingHashMapValuesIterator(
            IterableStoreCursor   delegate,
            StoreBackedSwappingHashMap<K,V>   cache,
            StoreEntryMapper<K,V> mapper,
            Class<K>              keysArrayComponentClass,
            Class<V>              valuesArrayComponentClass )
    {
        super( valuesArrayComponentClass );
        theKeysIterator = new StoreBackedSwappingHashMapKeysIterator<K,V>( delegate, cache, mapper, keysArrayComponentClass );
    }

    /**
     * Private copy-constructor.
     * 
     * @param old the StoreBackedSwappingHashMapValuesIterator to clone
     */
    protected StoreBackedSwappingHashMapValuesIterator(
            StoreBackedSwappingHashMapValuesIterator<K,V> old  )
    {
        super( old.theArrayComponentType );

        theKeysIterator        = old.theKeysIterator.createCopy();
    }

    /**
     * Obtain the next element, without iterating forward.
     *
     * @return the next element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    @Override
    public V peekNext()
    {
        K key = theKeysIterator.peekNext();
        V ret = theKeysIterator.getMap().get( key );

        return ret;
    }
    
    /**
     * Obtain the previous element, without iterating backward.
     *
     * @return the previous element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    @Override
    public V peekPrevious()
    {
        K key = theKeysIterator.peekPrevious();
        V ret = theKeysIterator.getMap().get( key );

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
        return theKeysIterator.hasNext();
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements in the backward direction.
     *
     * @return <tt>true</tt> if the iterator has more elements in the backward direction.
     * @see #hasNext()
     * @see #hasPrevious(int)
     * @see #hasNext(int)
     */
    @Override
    public boolean hasPrevious()
    {
        return theKeysIterator.hasPrevious();
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
        return theKeysIterator.hasNext( n );
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
        return theKeysIterator.hasPrevious( n );
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @exception NoSuchElementException iteration has no more elements.
     */
    public V next()
    {
        K key = theKeysIterator.next();
        V ret = theKeysIterator.getMap().get( key );

        return ret;
    }

    /**
     * <p>Obtain the next N elements. If fewer than N elements are available, return
     * as many elements are available in a shorter array.</p>
     * 
     * @return the next no more than N elements
     * @see #previous(int)
     */
    @Override
    public V [] next(
            int n )
    {
        K [] keys = theKeysIterator.next( n );
        V [] ret = ArrayHelper.createArray( theArrayComponentType, keys.length );

        StoreBackedSwappingHashMap<K,V> map = theKeysIterator.getMap();
        
        for( int i=0 ; i<keys.length ; ++i ) {
            ret[i] = map.get( keys[i] );
        }
        return ret;
    }

    /**
     * Returns the previous element in the iteration.
     *
     * @return the previous element in the iteration.
     * @see #next()
     */
    public V previous()
    {
        K key = theKeysIterator.previous();
        V ret = theKeysIterator.getMap().get( key );

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
     * @see #next(int)
     */
    @Override
    public V [] previous(
            int  n )
    {
        K [] keys = theKeysIterator.previous( n );
        V [] ret = ArrayHelper.createArray( theArrayComponentType, keys.length );

        StoreBackedSwappingHashMap<K,V> map = theKeysIterator.getMap();
        
        for( int i=0 ; i<keys.length ; ++i ) {
            ret[i] = map.get( keys[i] );
        }
        return ret;
    }

    /**
     * Move the cursor by N positions. Positive numbers indicate forward movemement;
     * negative numbers indicate backwards movement.
     * Throws NoSuchElementException if the position does not exist.
     *
     * @param n the number of positions to move
     * @throws NoSuchElementException
     */
    @Override
    public void moveBy(
            int n )
        throws
            NoSuchElementException
    {
        theKeysIterator.moveBy( n );
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
        theKeysIterator.remove();
    }
    
    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public StoreBackedSwappingHashMapValuesIterator<K,V> createCopy()
    {
        StoreBackedSwappingHashMapValuesIterator<K,V> ret = new StoreBackedSwappingHashMapValuesIterator<K,V>( this );

        return ret;
    }
    
    /**
     * Set this CursorIterator to the position represented by the provided CursorIterator.
     *
     * @param position the position to set this CursorIterator to
     * @throws IllegalArgumentException thrown if the provided CursorIterator did not work on the same CursorIterable,
     *         or the implementations were incompatible.
     */
    public void setPositionTo(
            CursorIterator<V> position )
        throws
            IllegalArgumentException
    {
        throw new UnsupportedOperationException(); // there is a funny typecast warning in the commented-out code which I
        // don't have time to investigate right now. FIXME.
//        if( !( position instanceof StoreBackedSwappingHashMapValuesIterator )) {
//            throw new IllegalArgumentException( "Wrong type of CursorIterator: " + position );
//        }
//        StoreBackedSwappingHashMapValuesIterator<K,V> realPosition = (StoreBackedSwappingHashMapValuesIterator<K,V>) position;
//
//        theKeysIterator.setPositionTo( realPosition.theKeysIterator ); // this may throw
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
        int ret = theKeysIterator.moveToBeforeFirst();
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
        int ret = theKeysIterator.moveToAfterLast();
        return ret;
    }

    /**
     * The underlying keys iterator.
     */
    protected StoreBackedSwappingHashMapKeysIterator<K,V> theKeysIterator;
}
