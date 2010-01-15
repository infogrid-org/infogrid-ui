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

package org.infogrid.store;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;

/**
 * Collects functionality common to IterableStoreCursor implementations that work on
 * IterableStores whose stored data is organized by ordered keys.
 */
public abstract class AbstractKeyBasedIterableStoreCursor
        implements
            IterableStoreCursor
{
    private static final Log log = Log.getLogInstance( AbstractKeyBasedIterableStoreCursor.class ); // our own, private logger

    /**
     * Constructor, for subclasses only.
     * 
     * @param store    the IterableStore to iterate over.
     * @param position the key for the current position.
     */
    protected AbstractKeyBasedIterableStoreCursor(
            IterableStore store,
            String        position )
    {
        theStore    = store;
        thePosition = position;
    }

    /**
     * Obtain the next element, without iterating forward.
     *
     * @return the next element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public StoreValue peekNext()
    {
        StoreValue [] found = findNextIncluding( thePosition, 1 );
        
        if( found.length == 1 ) {
            return found[0];
        } else {
            throw new NoSuchElementException();
        }
    }
    
    /**
     * Obtain the next key, without iterating forward.
     *
     * @return the next key
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public String peekNextKey()
    {
        String [] found = findNextKeyIncluding( thePosition, 1 );
        
        if( found.length == 1 ) {
            return found[0];
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
    public StoreValue peekPrevious()
    {
        StoreValue [] found = findPreviousExcluding( thePosition, 1 );
        
        if( found.length == 1 ) {
            return found[0];
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Obtain the previous key, without iterating backwards.
     *
     * @return the previous key
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public String peekPreviousKey()
    {
        String [] found = findPreviousKeyExcluding( thePosition, 1 );
        
        if( found.length == 1 ) {
            return found[0];
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
    public boolean hasNext()
    {
        return hasNext( 1 );
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
        return hasPrevious( 1 );
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
        int found = hasNextIncluding( thePosition );
        if( found >= n ) {
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
        int found = hasPreviousExcluding( thePosition );
        if( found >= n ) {
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
    public StoreValue next()
    {
        StoreValue [] found = next( 1 );
        if( found.length == 1 ) {
            return found[0];
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the next key in the iteration.
     *
     * @return the next key in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public String nextKey()
    {
        String [] found = nextKey( 1 );
        if( found.length == 1 ) {
            return found[0];
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * <p>Obtain the next N elements. If fewer than N elements are available, return
     * as many elements are available in a shorter array.</p>
     * 
     * @param n the number of elements to obtain
     * @return the next no more than N elements
     * @see #previous(int)
     */
    public StoreValue [] next(
            int n )
    {
        // we have to go forward by one more, so we can set the new position right after what's returned here
        StoreValue [] found = findNextIncluding( thePosition, n+1 );
        StoreValue [] ret   = ArrayHelper.copyIntoNewArray( found, 0, Math.min( n, found.length ), StoreValue.class );

        if( found.length == n+1 ) {
            thePosition = found[found.length-1].getKey();
        } else {
            moveToAfterLast();
        }
        return ret;
    }

    /**
     * <p>Obtain the next N keys. If fewer than N elements are available, return
     * as many keys are available in a shorter array.</p>
     * 
     * @param n the number of keys to obtain
     * @return the next no more than N keys
     * @see #previousKey(int)
     */
    public String [] nextKey(
            int n )
    {
        // we have to go forward by one more, so we can set the new position right after what's returned here
        String [] found = findNextKeyIncluding( thePosition, n+1 );
        String [] ret   = ArrayHelper.copyIntoNewArray( found, 0, Math.min( n, found.length ), String.class );

        if( found.length == n+1 ) {
            thePosition = found[found.length-1];
        } else {
            moveToAfterLast();
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
        StoreValue [] found = previous( 1 );
        if( found.length == 1 ) {
            return found[0];
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the previous key in the iteration.
     *
     * @return the previous key in the iteration.
     * @see #nextKey()
     */
    public String previousKey()
    {
        String [] found = previousKey( 1 );
        if( found.length == 1 ) {
            return found[0];
        } else {
            throw new NoSuchElementException();
        }
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
     * @param n the number of elements to obtain
     * @return the previous no more than N elements
     * @see #next(int)
     */
    public StoreValue [] previous(
            int n )
    {
        StoreValue [] found = findPreviousExcluding( thePosition, n );
        if( found.length > 0 ) {
            thePosition = found[found.length-1].getKey();
        } else{
            moveToBeforeFirst();
        }
        return found;
    }

    /**
     * <p>Obtain the previous N keys. If fewer than N elements are available, return
     * as many keys are available in a shorter array.</p>
     * 
     * <p>Note that the keys
     * will be ordered in the opposite direction as you might expect: they are
     * returned in the sequence in which the CursorIterator visits them, not in the
     * sequence in which the underlying Iterable stores them.</p>
     *
     * @param n the number of keys to obtain
     * @return the previous no more than N keys
     * @see #next(int)
     */
    public String [] previousKey(
            int n )
    {
        String [] found = findPreviousKeyExcluding( thePosition, n );
        if( found.length > 0 ) {
            thePosition = found[found.length-1];
        } else{
            moveToBeforeFirst();
        }
        return found;
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
        if( n == 0 ) {
            return;
        }
        String newPosition = findKeyAt( thePosition, n );
        thePosition = newPosition;
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
        return moveToBefore( pos.getKey() );
    }

    /**
     * Move the cursor to just before this element, i.e. return this element when {@link #previous previous} is invoked
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
        return moveToAfter( pos.getKey() );
    }

    /**
     * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
     * right afterwards.
     *
     * @param key the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    public int moveToBefore(
            String key )
        throws
            NoSuchElementException
    {
        if( thePosition.equals( key )) {
            return 0;
        }

        // FIXME this does not look right
        int distance = determineDistance( thePosition, key );
        if( distance < 0 ) {
            distance = -determineDistance( key, thePosition );
            if( distance > 0 ) {
                throw new NoSuchElementException();
            }
        }
        thePosition = key;

        return distance;
    }

    /**
     * Move the cursor to this element, i.e. return this element when {@link #previous previous} is invoked
     * right afterwards.
     *
     * @param key the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    public int moveToAfter(
            String key )
        throws
            NoSuchElementException
    {
        int ret = moveToBefore( key );
        next();
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
        int ret;

        try {
            String newPosition = getBeforeFirstPosition();

            ret         = determineDistance( thePosition, newPosition );
            thePosition = newPosition;

        } catch( NoSuchElementException ex ) {
            // empty store
            ret = 0;
        }
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
        String newPosition = getAfterLastPosition();
        int    ret         = determineDistance( thePosition, newPosition );
        
        thePosition = newPosition;
        return ret;
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
        try {
            theStore.delete( thePosition );
        
            // we don't need to adjust the position
        } catch( StoreKeyDoesNotExistException ex ) {
            log.error( ex );
        } catch( IOException ex ) {
            log.error( ex );
        }
    }
    
    
    /**
     * Set this CursorIterator to the position represented by the provided CursorIterator.
     *
     * @param position the position to set this CursorIterator to
     * @throws ClassCastException thrown if the provided CursorIterator did not work on the same CursorIterable,
     *         or the implementations were incompatible.
     */
    public void setPositionTo(
            CursorIterator<StoreValue> position )
        throws
            ClassCastException
    {
        if( !( position instanceof AbstractKeyBasedIterableStoreCursor )) {
            throw new ClassCastException( "Wrong type of CursorIterator: " + position );
        }
        AbstractKeyBasedIterableStoreCursor realPosition = (AbstractKeyBasedIterableStoreCursor) position;

        if( theStore != realPosition.theStore ) {
            throw new IllegalArgumentException( "Not the same instance of Store" );
        }
        
        thePosition = realPosition.thePosition;
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
     * Find the next n StoreValues, including the StoreValue for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key key for the first StoreValue
     * @param n the number of StoreValues to find
     * @return the found StoreValues
     */
    protected abstract StoreValue [] findNextIncluding(
            String key,
            int    n );
    
    /**
     * Find the next n keys, including key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key the first key
     * @param n the number of keys to find
     * @return the found keys
     */
    protected abstract String [] findNextKeyIncluding(
            String key,
            int    n );
    
    /**
     * Find the previous n StoreValues, excluding the StoreValue for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key key for the first StoreValue NOT to be returned
     * @param n the number of StoreValues to find
     * @return the found StoreValues
     */
    protected abstract StoreValue [] findPreviousExcluding(
            String key,
            int    n );

    /**
     * Find the previous n keys, excluding the key for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key the first key NOT to be returned
     * @param n the number of keys to find
     * @return the found keys
     */
    protected abstract String [] findPreviousKeyExcluding(
            String key,
            int    n );

    /**
     * Count the number of elements following and including the one with the key.
     *
     * @param key the key
     * @return the number of elements
     */
    protected abstract int hasNextIncluding(
            String key );

    /**
     * Count the number of elements preceding and excluding the one with the key.
     *
     * @param key the key
     * @return the number of elements
     */
    protected abstract int hasPreviousExcluding(
            String key );

    /**
     * Find the key N elements up or down from the current key.
     *
     * @param key the current key
     * @param delta the number of elements up (positive) or down (negative)
     * @return the found key, or null
     * @throws NoSuchElementException thrown if the delta went beyond the "after last" or "before first" element
     */
    protected abstract String findKeyAt(
            String key,
            int    delta )
       throws
            NoSuchElementException;

    /**
     * Helper method to determine the number of elements between two keys.
     *
     * @param from the start key
     * @param to the end key
     * @return the distance
     */
    protected abstract int determineDistance(
            String from,
            String to );

    /**
     * Determine the key at the very beginning.
     * 
     * @return the key
     */
    protected abstract String getBeforeFirstPosition();
    
    /**
     * Determine the key at the very end.
     * 
     * @return the key
     */
    protected abstract String getAfterLastPosition();
    
    /**
     * The IterableStore to iterate over.
     */
    protected IterableStore theStore;

    /**
     * The key for the current position.
     */
    protected String thePosition;
}
