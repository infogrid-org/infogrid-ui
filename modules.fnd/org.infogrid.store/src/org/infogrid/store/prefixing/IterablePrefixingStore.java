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

package org.infogrid.store.prefixing;

import java.io.IOException;
import org.infogrid.store.IterableStore;
import org.infogrid.store.IterableStoreCursor;
import org.infogrid.store.StoreValue;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.FilteringCursorIterator;

import java.util.NoSuchElementException;

/**
 * A {@link PrefixingStore} that is also an {@link IterableStore}.
 */
public class IterablePrefixingStore
        extends
            PrefixingStore
        implements
            IterableStore
{
    /**
     * Factory method.
     *
     * @param prefix the prefix for all keys
     * @param delegate the IterableStore that this IterablePrefixingStore delegates to
     * @return the created IterablePrefixingStore
     */
    public static IterablePrefixingStore create(
            String        prefix,
            IterableStore delegate )
    {
        return new IterablePrefixingStore( prefix + DEFAULT_SEPARATOR, delegate );
    }

    /**
     * Factory method.
     *
     * @param prefix the prefix for all keys
     * @param separator the separator String between prefix and the key
     * @param delegate the IterableStore that this IterablePrefixingStore delegates to
     * @return the created IterablePrefixingStore
     */
    public static IterablePrefixingStore create(
            String        prefix,
            String        separator,
            IterableStore delegate )
    {
        return new IterablePrefixingStore( prefix + separator, delegate );
    }

    /**
     * Constructor.
     *
     * @param prefixAndSeparator the prefix for all keys, including any separator
     * @param delegate the IterableStore that this IterablePrefixingStore delegates to
     */
    protected IterablePrefixingStore(
            String        prefixAndSeparator,
            IterableStore delegate )
    {
        super( prefixAndSeparator, delegate );
    }

    /**
     * Obtain an Iterator over the content of this Store.
     *
     * @return the Iterator
     */
    public IterableStoreCursor iterator()
    {
        return new MyIterator( ((IterableStore)theDelegate).iterator() );
    }

    /**
     * Obtain an Iterator over the content of this Store.
     *
     * @return the Iterator
     */
    public IterableStoreCursor getIterator()
    {
        return iterator();
    }

    /**
     * Determine the number of StoreValues in this Store.
     *
     * @return the number of StoreValues in this Store
     * @throws IOException thrown if an I/O error occurred
     */
    public int size()
        throws
            IOException
    {
        return ((IterableStore)theDelegate).size( thePrefixAndSeparator );
    }

    /**
     * Determine the number of StoreValues in this Store with this prefix.
     *
     * @param prefix the prefix
     * @return the number of StoreValues in this Store with this prefix
     * @throws IOException thrown if an I/O error occurred
     */
    public int size(
            String prefix )
        throws
            IOException
    {
        return ((IterableStore)theDelegate).size( thePrefixAndSeparator + prefix );
    }

    /**
     * Determine whether this Store is empty.
     *
     * @return true if this Store is empty
     * @throws IOException thrown if an I/O error occurred
     */
    public boolean isEmpty()
        throws
            IOException
    {
        int size = ((IterableStore)theDelegate).size( thePrefixAndSeparator );
        return size == 0;
    }

    /**
     * The filter to use.
     */
    protected FilteringCursorIterator.Filter<StoreValue> myFilter = new FilteringCursorIterator.Filter<StoreValue>() {
            public boolean accept(
                    StoreValue v )
            {
                String  key = v.getKey();
                boolean ret = matches( key );

                return ret;
            }
    };

    /**
     * Our IterableStoreCursor implementation.
     */
    class MyIterator
            implements
                IterableStoreCursor
    {
        /**
         * Constructor.
         * 
         * @param delegateIter an Iterator over the underlying delegate IterableStore
         */
        public MyIterator(
                IterableStoreCursor delegateIter )
        {
            theFilterIterator = new MyFilteringIterator( delegateIter );
        }
        
        /**
         * Obtain the next element, without iterating forward.
         *
         * @return the next element
         * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
         */
        public StoreValue peekNext()
        {
            StoreValue ret = translateDelegateStoreValue( theFilterIterator.peekNext() );
            return ret;
        }

        /**
         * Obtain the previous element, without iterating backwards.
         *
         * @return the previous element
         * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
         */
        public StoreValue peekPrevious()
        {
            StoreValue ret = translateDelegateStoreValue( theFilterIterator.peekPrevious() );
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
            return theFilterIterator.hasNext();
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements in the backwards direction.
         *
         * @return <tt>true</tt> if the iterator has more elements in the backwards direction.
         * @see #hasNext()
         * @see #hasPrevious(int)
         * @see #hasNext(int)
         */
        public boolean hasPrevious()
        {
            return theFilterIterator.hasPrevious();
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
            return theFilterIterator.hasNext( n );
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
            return theFilterIterator.hasPrevious( n );
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException iteration has no more elements.
         */
        public StoreValue next()
        {
            StoreValue ret = translateDelegateStoreValue( theFilterIterator.next() );
            return ret;
        }

        /**
         * <p>Obtain the next N elements. If fewer than N elements are available, return
         * as many elements are available in a shorter array.</p>
         * 
         * @param n the number of elements to return
         * @return the next no more than N elements
         * @see #previous(int)
         */
        public StoreValue [] next(
                int n )
        {
            StoreValue [] found = theFilterIterator.next( n );
            StoreValue [] ret   = new StoreValue[ found.length ];
            for( int i=0 ; i<found.length ; ++i ) {
                ret[i] = translateDelegateStoreValue( found[i] );
            }
            return ret;
        }

        /**
         * Returns the previous element in the iteration.
         *
         * @return the previous element in the iteration.
         * @see #next()
         */
        public StoreValue previous()
        {
            StoreValue ret = translateDelegateStoreValue( theFilterIterator.previous() );
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
         * @param n the number of elements to return
         * @return the previous no more than N elements
         * @see #next(int)
         */
        public StoreValue [] previous(
                int  n )
        {
            StoreValue [] found = theFilterIterator.previous( n );
            StoreValue [] ret   = new StoreValue[ found.length ];
            for( int i=0 ; i<found.length ; ++i ) {
                ret[i] = translateDelegateStoreValue( found[i] );
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
        public void moveBy(
                int n )
            throws
                NoSuchElementException
        {
            theFilterIterator.moveBy( n );
        }

        /**
         * Move the cursor to just before this element, i.e. return this element when {@link #next next} is invoked
         * right afterwards.
         *
         * @param pos the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToBefore(
                StoreValue pos )
            throws
                NoSuchElementException
        {
            StoreValue delegatedPos = translateToDelegateStoreValue( pos );
            int        ret          = theFilterIterator.moveToBefore( delegatedPos );

            return ret;
        }

        /**
         * Move the cursor to just after this element, i.e. return this element when {@link #previous previous} is invoked
         * right afterwards.
         *
         * @param pos the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToAfter(
                StoreValue pos )
            throws
                NoSuchElementException
        {
            StoreValue delegatedPos = translateToDelegateStoreValue( pos );
            int        ret          = theFilterIterator.moveToAfter( delegatedPos );

            return ret;
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
        public void remove()
        {
            theFilterIterator.remove();
        }

        /**
         * Clone this position.
         *
         * @return identical new instance
         */
        public MyIterator createCopy()
        {
            return new MyIterator( theFilterIterator.createCopy() );
        }

        /**
         * Set this CursorIterator to the position represented by the provided CursorIterator.
         *
         * @param position the position to set this CursorIterator to
         * @throws IllegalArgumentException thrown if the provided CursorIterator did not work on the same CursorIterable,
         *         or the implementations were incompatible.
         */
        public void setPositionTo(
                CursorIterator<StoreValue> position )
            throws
                IllegalArgumentException
        {
            StoreValue delegatedPos = translateToDelegateStoreValue( position.next() );
            
            theFilterIterator.moveToAfter( delegatedPos );
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
         * right afterwards.
         *
         * @param key the key of the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToBefore(
                String key )
            throws
                NoSuchElementException
        {
            String delegatedKey = constructDelegatedKey( key );
            int    ret          = theFilterIterator.moveToBefore( delegatedKey );
            
            return ret;
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #previous previous} is invoked
         * right afterwards.
         *
         * @param key the key of the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToAfter(
                String key )
            throws
                NoSuchElementException
        {
            String delegatedKey = constructDelegatedKey( key );
            int    ret          = theFilterIterator.moveToAfter( delegatedKey );
            
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
            int ret = theFilterIterator.moveToBeforeFirst();
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
            int ret = theFilterIterator.moveToAfterLast();
            return ret;
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
        public final StoreValue nextElement()
        {
            return next();
        }

        /**
         * Obtain a CursorIterable instead of an Iterator.
         *
         * @return the CursorIterable
         */
        public CursorIterator<StoreValue> iterator()
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
        public final CursorIterator<StoreValue> getIterator()
        {
            return iterator();
        }

        /**
         * The underlying FilteringIterator.
         */
        protected MyFilteringIterator theFilterIterator;
    }
    
    /**
     * The FilteringIterator that MyIterator uses.
     */
    class MyFilteringIterator
            extends
                FilteringCursorIterator<StoreValue>
            implements
                IterableStoreCursor
    {
        /**
         * Constructor.
         *
         * @param delegateIter the Iterator over the underlying Store
         */
        public MyFilteringIterator(
                IterableStoreCursor delegateIter )
        {
            super( delegateIter, myFilter, StoreValue.class );
        }
        
        /**
         * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
         * right afterwards.
         *
         * @param pos the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToBefore(
                String pos )
            throws
                NoSuchElementException
        {
            // see moveToBefore in superclass

            CursorIterator<StoreValue> currentPosition = createCopy();

            int count = 0;
            while( hasNext() ) {
                StoreValue found = peekNext();
                if( pos.equals( found.getKey() )) {
                    return count;
                }
                ++count;
                next();
            }

            setPositionTo( currentPosition );

            count = 0;
            while( hasPrevious() ) {
                StoreValue found = previous();
                --count;
                if( pos.equals( found.getKey() )) {
                    return count;
                }
            }
            throw new NoSuchElementException();
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #previous previous} is invoked
         * right afterwards.
         *
         * @param pos the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToAfter(
                String pos )
            throws
                NoSuchElementException
        {
            // see moveToBefore in superclass

            CursorIterator<StoreValue> currentPosition = createCopy();

            int count = 0;
            while( hasNext() ) {
                StoreValue found = next();
                ++count;
                if( pos.equals( found.getKey() )) {
                    return count;
                }
            }

            setPositionTo( currentPosition );

            count = 0;
            while( hasPrevious() ) {
                StoreValue found = peekPrevious();
                if( pos.equals( found.getKey() )) {
                    return count;
                }
                --count;
                previous();
            }
            throw new NoSuchElementException();
        }

        /**
         * Clone this position.
         *
         * @return identical new instance
         */
        @Override
        public MyFilteringIterator createCopy()
        {
            return new MyFilteringIterator( (IterableStoreCursor) theDelegate.createCopy() );
        }
    }
}
