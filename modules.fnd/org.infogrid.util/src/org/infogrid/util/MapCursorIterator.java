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

import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * Provides {@link CursorIterator}s for the keys and values of <code>java.util.HashMap</code>s.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public abstract class MapCursorIterator<K,V>
{
    /**
     * Factory method to create an iterator over the keys of a map.
     * 
     * @param map the underlying map to iterate over
     * @param keyArrayComponentType the Class to use for arrays containing keys
     * @param valueArrayComponentType the Class to use for arrays containing values
     * @return the created MapCursorIterator, iterating over the keys
     * @param <K> the type of key
     * @param <V> the type of value
     */
    @SuppressWarnings(value={"unchecked"})
    public static <K,V> Keys<K,V> createForKeys(
            Map<K,V> map,
            Class<K> keyArrayComponentType,
            Class<V> valueArrayComponentType )
    {
        Entry<K,V> [] entries  = (Entry<K,V> []) ArrayHelper.createArray( Entry.class, map.size() );
        entries  = map.entrySet().toArray( entries );

        return new Keys<K,V>( map, keyArrayComponentType, valueArrayComponentType, entries, ArrayCursorIterator.create( entries ));
    }

    /**
     * Factory method to create an iterator over the values of a map.
     * 
     * @param map the underlying map to iterate over
     * @param keyArrayComponentType the Class to use for arrays containing keys
     * @param valueArrayComponentType the Class to use for arrays containing values
     * @return the created MapCursorIterator, iterating over the values
     * @param <K> the type of key
     * @param <V> the type of value
     */
    @SuppressWarnings(value={"unchecked"})
    public static <K,V> Values<K,V> createForValues(
            Map<K,V> map,
            Class<K> keyArrayComponentType,
            Class<V> valueArrayComponentType )
    {
        Entry<K,V> [] entries  = (Entry<K,V> []) ArrayHelper.createArray( Entry.class, map.size() );
        entries  = map.entrySet().toArray( entries );

        return new Values<K,V>( map, keyArrayComponentType, valueArrayComponentType, entries, ArrayCursorIterator.create( entries ));
    }

    /**
     * Constructor.
     *
     * @param map the Map to iterate over
     * @param keyArrayComponentType the type of the keys, to be able to create arrays of this type
     * @param valueArrayComponentType the type of the values, to be able to create arrays of this type
     * @param entries the sorted list of entries in the map
     * @param delegate the iterator over the entries
     */
    protected MapCursorIterator(
            Map<K,V>                        map,
            Class<K>                        keyArrayComponentType,
            Class<V>                        valueArrayComponentType,
            Entry<K,V> []                   entries,
            ArrayCursorIterator<Entry<K,V>> delegate )
    {
        theMap                     = map;
        theKeyArrayComponentType   = keyArrayComponentType;
        theValueArrayComponentType = valueArrayComponentType;
        theEntries                 = entries;
        theDelegate                = delegate;
    }
    
    /**
     * Obtain the next element, without iterating forward.
     *
     * @return the next element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    protected Entry<K,V> getPeekNext()
    {
        return theDelegate.peekNext();
    }

    /**
     * Obtain the previous element, without iterating backward.
     *
     * @return the previous element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    protected Entry<K,V> getPeekPrevious()
    {
        return theDelegate.peekPrevious();
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
        return theDelegate.hasNext();
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
        return theDelegate.hasPrevious( n );
    }

    /**
     * Move the cursor by N positions. Positive numbers indicate forward movemement;
     * negative numbers indicate backward movement.
     *
     * @param n the number of positions to move
     * @throws NoSuchElementException if the position does not exist
     */
    public void moveBy(
            int n )
        throws
            NoSuchElementException
    {
        theDelegate.moveBy( n );
    }

    /**
     * Obtain the current position, starting the count with 0 for the first element.
     *
     * @return the current position
     */
    public int getPosition()
    {
        return theDelegate.getPosition();
    }

    /**
     * Set this CursorIterator to a particular position.
     *
     * @param n the position to set this CursorIterator to
     * @throws NoSuchElementException thrown if this position was not available
     */
    public void setPosition(
            int n )
        throws
            NoSuchElementException
    {
        theDelegate.setPosition( n );
    }


    /**
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
    public void remove()
    {
        theDelegate.remove();
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
     * Obtain the next entry.
     * 
     * @return the next entry
     */
    protected Entry<K,V> getNext()
    {
        return theDelegate.next();
    }

    /**
     * Obtain the next N entries.
     * 
     * @param n the number of entries to return
     * @return the next N entries
     */
    protected Entry<K,V> [] getNext(
            int n )
    {
        return theDelegate.next( n );
    }

    /**
     * Obtain the previous entry.
     * 
     * @return the previous entry
     */
    protected Entry<K,V> getPrevious()
    {
        return theDelegate.previous();
    }

    /**
     * Obtain the previous N entries.
     * 
     * @param n the number of entries to return
     * @return the previous N entries
     */
    protected Entry<K,V> [] getPrevious(
            int n )
    {
        return theDelegate.previous( n );
    }

    /**
     * Helper method to get the keys of an array of Entries.
     * 
     * @param entries the entries
     * @return the keys of the entries
     */
    protected K [] keysOf(
            Entry<K,V> [] entries )
    {
        K [] ret = ArrayHelper.createArray( theKeyArrayComponentType, entries.length );
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = entries[i].getKey();
        }
        return ret;
    }

    /**
     * Helper method to get the values of an array of Entries.
     * 
     * @param entries the entries
     * @return the values of the entries
     */
    protected V [] valuesOf(
            Entry<K,V> [] entries )
    {
        V [] ret = ArrayHelper.createArray( theValueArrayComponentType, entries.length );
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = entries[i].getValue();
        }
        return ret;
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
        int ret = theDelegate.moveToBeforeFirst();
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
        int ret = theDelegate.moveToAfterLast();
        return ret;
    }

    /**
     * The Map to iterate over.
     */
    protected Map<K,V> theMap;

    /**
     * The type for keys.
     */
    protected Class<K> theKeyArrayComponentType;
    
    /**
     * The type for values.
     */
    protected Class<V> theValueArrayComponentType;

    /**
     * The Entries in the Map.
     */
    protected Entry<K,V> [] theEntries;

    /**
     * CursorIterator on theEntries.
     */
    protected ArrayCursorIterator<Entry<K,V>> theDelegate;
    
    /**
     * An iterator over the keys.
     * 
     * @param <K> the type of keys
     * @param <V> the type of values
     */
    public static class Keys<K,V>
            extends
                MapCursorIterator<K, V>
            implements
                CursorIterator<K>
    {
        /**
         * Constructor.
         *
         * @param map the Map to iterate over
         * @param keyArrayComponentType the type of the keys, to be able to create arrays of this type
         * @param valueArrayComponentType the type of the values, to be able to create arrays of this type
         * @param entries the sorted list of entries in the map
         * @param delegate the iterator over the entries
         */
        public Keys(
                Map<K,V>                        map,
                Class<K>                        keyArrayComponentType,
                Class<V>                        valueArrayComponentType,
                Entry<K,V> []                   entries,
                ArrayCursorIterator<Entry<K,V>> delegate )
        {
            super( map, keyArrayComponentType, valueArrayComponentType, entries, delegate );
        }

        /**
         * Obtain the next element, without iterating forward.
         *
         * @return the next element
         * @exception NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
         */
        public K peekNext()
        {
            return getPeekNext().getKey();
        }

        /**
         * Obtain the previous element, without iterating backwards.
         *
         * @return the previous element
         * @exception NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
         */
        public K peekPrevious()
        {
            return getPeekPrevious().getKey();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @exception NoSuchElementException iteration has no more elements.
         */
        public K next()
        {
            return getNext().getKey();
        }

        /**
         * <p>Obtain the next N elements. If fewer than N elements are available, return
         * as many elements are available in a shorter array.</p>
         * 
         * @param n the number of elements to return
         * @return the next no more than N elements
         * @see #previous(int)
         */
        public K [] next(
                int n )
        {
            return keysOf( getNext( n ) );
        }

        /**
         * Returns the previous element in the iteration.
         *
         * @return the previous element in the iteration.
         * @exception NoSuchElementException iteration has no more elements.
         * @see #next()
         */
        public K previous()
        {
            return getPrevious().getKey();
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
         * @param n the number of elements to return
         * @return the previous no more than N elements
         * @see #next(int)
         */
        public K [] previous(
                int n )
        {
            return keysOf( getPrevious( n ) );
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
         * right afterwards.
         *
         * @param pos the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToBefore(
                K pos )
            throws
                NoSuchElementException
        {
            // FIXME? Is this right?
            for( int i=0 ; i<theEntries.length ; ++i ) {
                if( pos == theEntries[i].getKey() ) {

                    int ret = i - theDelegate.getPosition() - 1;
                    theDelegate.setPosition( i );
                    return ret;
                }
            }
            throw new NoSuchElementException();
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
         * right afterwards.
         *
         * @param pos the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToAfter(
                K pos )
            throws
                NoSuchElementException
        {
            // FIXME? Is this right?
            for( int i=0 ; i<theEntries.length ; ++i ) {
                if( pos == theEntries[i].getKey() ) {

                    int ret = i - theDelegate.getPosition();
                    theDelegate.setPosition( i );
                    return ret;
                }
            }
            throw new NoSuchElementException();
        }

        /**
          * Return next element and iterate.
          *
          * @return the next element
          */
        public final K nextElement()
        {
            return next();
        }
    
        /**
         * Clone this position.
         *
         * @return identical new instance
         */
        public Keys<K,V> createCopy()
        {
            return new Keys<K,V>( theMap, theKeyArrayComponentType, theValueArrayComponentType, theEntries, theDelegate.createCopy() );
        }    

        /**
         * Set this CursorIterator to the position represented by the provided CursorIterator.
         *
         * @param position the position to set this CursorIterator to
         * @throws IllegalArgumentException thrown if the provided CursorIterator did not work on the same CursorIterable,
         *         or the implementations were incompatible.
         */
        public void setPositionTo(
                CursorIterator<K> position )
            throws
                IllegalArgumentException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * Obtain a CursorIterable instead of an Iterator.
         *
         * @return the CursorIterable
         */
        public CursorIterator<K> iterator()
        {
            return this;
        }

        /**
         * Obtain a CursorIterable. This performs the exact same operation as
         * @link #iterator iterator}, but is friendlier towards JSPs and other software
         * that likes to use JavaBeans conventions.
         *
         * @return the CursorIterable
         */
        public final CursorIterator<K> getIterator()
        {
            return iterator();
        }
    }

    /**
     * An iterator over the values.
     * 
     * @param <K> the type of keys
     * @param <V> the type of values
     */
    public static class Values<K,V>
            extends
                MapCursorIterator<K, V>
            implements
                CursorIterator<V>
    {
        /**
         * Constructor.
         *
         * @param map the Map to iterate over
         * @param keyArrayComponentType the type of the keys, to be able to create arrays of this type
         * @param valueArrayComponentType the type of the values, to be able to create arrays of this type
         * @param entries the sorted list of entries in the map
         * @param delegate the iterator over the entries
         */
        protected Values(
                Map<K,V>                        map,
                Class<K>                        keyArrayComponentType,
                Class<V>                        valueArrayComponentType,
                Entry<K,V> []                   entries,
                ArrayCursorIterator<Entry<K,V>> delegate )
        {
            super( map, keyArrayComponentType, valueArrayComponentType, entries, delegate );
        }
        
        /**
         * Obtain the next element, without iterating forward.
         *
         * @return the next element
         * @exception NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
         */
        public V peekNext()
        {
            return getPeekNext().getValue();
        }

        /**
         * Obtain the previous element, without iterating backwards.
         *
         * @return the previous element
         * @exception NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
         */
        public V peekPrevious()
        {
            return getPeekPrevious().getValue();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @exception NoSuchElementException iteration has no more elements.
         */
        public V next()
        {
            return getNext().getValue();
        }

        /**
         * <p>Obtain the next N elements. If fewer than N elements are available, return
         * as many elements are available in a shorter array.</p>
         * 
         * @param n the number of elements to return
         * @return the next no more than N elements
         * @see #previous(int)
         */
        public V [] next(
                int  n )
        {
            return valuesOf( getNext( n ) );
        }

        /**
         * Returns the previous element in the iteration.
         *
         * @return the previous element in the iteration.
         * @exception NoSuchElementException iteration has no more elements.
         * @see #next()
         */
        public V previous()
        {
            return getPrevious().getValue();
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
         * @param n the number of elements to return
         * @return the previous no more than N elements
         * @see #next(int)
         */
        public V [] previous(
                int  n )
        {
            return valuesOf( getPrevious( n ) );
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
         * right afterwards.
         *
         * @param pos the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToBefore(
                V pos )
            throws
                NoSuchElementException
        {
            // FIXME? Is this right?
            for( int i=0 ; i<theEntries.length ; ++i ) {
                if( pos == theEntries[i].getValue() ) {

                    int ret = i - theDelegate.getPosition() - 1;
                    theDelegate.setPosition( i );
                    return ret;
                }
            }
            throw new NoSuchElementException();
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
         * right afterwards.
         *
         * @param pos the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToAfter(
                V pos )
            throws
                NoSuchElementException
        {
            // FIXME? Is this right?
            for( int i=0 ; i<theEntries.length ; ++i ) {
                if( pos == theEntries[i].getValue() ) {

                    int ret = i - theDelegate.getPosition();
                    theDelegate.setPosition( i );
                    return ret;
                }
            }
            throw new NoSuchElementException();
        }

        /**
          * Return next element and iterate.
          *
          * @return the next element
          */
        public final V nextElement()
        {
            return next();
        }
    
        /**
         * Clone this position.
         *
         * @return identical new instance
         */
        public Values<K, V> createCopy()
        {
            return new Values<K,V>( theMap, theKeyArrayComponentType, theValueArrayComponentType, theEntries, theDelegate.createCopy() );
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
            throw new UnsupportedOperationException();
        }

        /**
         * Obtain a CursorIterable instead of an Iterator.
         *
         * @return the CursorIterable
         */
        public CursorIterator<V> iterator()
        {
            return this;
        }

        /**
         * Obtain a CursorIterable. This performs the exact same operation as
         * @link #iterator iterator}, but is friendlier towards JSPs and other software
         * that likes to use JavaBeans conventions.
         *
         * @return the CursorIterable
         */
        public final CursorIterator<V> getIterator()
        {
            return iterator();
        }
    }
}
