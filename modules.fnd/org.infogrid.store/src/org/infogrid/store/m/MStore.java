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

package org.infogrid.store.m;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import org.infogrid.store.AbstractIterableStore;
import org.infogrid.store.IterableStoreCursor;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreKeyExistsAlreadyException;
import org.infogrid.store.StoreValue;
import org.infogrid.util.ArrayCursorIterator;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.MapCursorIterator;
import org.infogrid.util.StringHelper;
import org.infogrid.util.logging.Log;

/**
 * A "fake" Store implementation that keeps the {@link org.infogrid.store.Store} content in memory only.
 * While there might be few production uses of this class, there are many
 * during software development and for testing.
 */
public class MStore
        extends
            AbstractIterableStore
{
    private static final Log log = Log.getLogInstance( MStore.class ); // our own, private logger
    
    /**
     * Factory method.
     *
     * @return the created MStore
     */
    public static MStore create()
    {
        return new MStore();
    }

    /**
     * Constructor.
     */
    protected MStore()
    {
    }

    /**
     * Initialize the Store. If the Store was initialized earlier, this will delete all
     * contained information. This operation is similar to unconditionally formatting a hard drive.
     * 
     * @throws IOException thrown if an I/O error occurred
     */
    public void initializeHard()
            throws
                IOException
    {
        theDelegate.clear();
    }
    
    /**
     * Initialize the Store if needed. If the Store was initialized earlier, this will do
     * nothing. This operation is equivalent to {@link #initializeHard} if and only if
     * the Store had not been initialized earlier.
     * 
     * @throws IOException thrown if an I/O error occurred
     */
    public void initializeIfNecessary()
            throws
                IOException
    {
        // no nothing
    }

    /**
     * Put a data element into the Store for the first time. Throw an Exception if a data
     * element has already been store using the same key.
     *
     * @param key the key with which the specified data element is to be associated
     * @param encodingId the id of the encoding that was used to encode the data element.
     * @param timeCreated the time at which the data element was created
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires if positive, the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @throws StoreKeyExistsAlreadyException thrown if a data element is already stored in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #update if a data element with this key exists already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    @Override
    public void put(
            String  key,
            String  encodingId,
            long    timeCreated,
            long    timeUpdated,
            long    timeRead,
            long    timeExpires,
            byte [] data )
        throws
            StoreKeyExistsAlreadyException,
            IOException
    {
        checkKey(      key );
        checkEncoding( encodingId );
        checkData(     data );

        put( new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data ));
    }

    /**
     * Put a data element into the Store for the first time. Throw an Exception if a data
     * element has already been store using the same key.
     *
     * @param toStore the StoreValue to store
     * @throws StoreKeyExistsAlreadyException thrown if a data element is already stored in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #update if a data element with this key exists already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    public synchronized void put(
            StoreValue toStore )
        throws
            StoreKeyExistsAlreadyException,
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".put( " + toStore + " )" );
        }
        try {
            StoreValue already = theDelegate.get( toStore.getKey() );
            if( already != null ) {
                throw new StoreKeyExistsAlreadyException( this, toStore.getKey() );
            }
            theDelegate.put( toStore.getKey(), toStore );

        } finally {
            firePutPerformed( toStore );
        }
    }

    /**
     * Update a data element that already exists in the Store, by overwriting it with a new value. Throw an
     * Exception if a data element with this key does not exist already.
     *
     * @param key the key with which the specified data element is already, and will continue to be stored
     * @param encodingId the id of the encoding that was used to encode the data element.
     * @param timeCreated the time at which the data element was created
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires if positive, the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @throws StoreKeyDoesNotExistException thrown if no data element exists in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    @Override
    public void update(
            String  key,
            String  encodingId,
            long    timeCreated,
            long    timeUpdated,
            long    timeRead,
            long    timeExpires,
            byte [] data )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        checkKey(      key );
        checkEncoding( encodingId );
        checkData(     data );

        update( new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data ));
    }

    /**
     * Update a data element that already exists in the Store, by overwriting it with a new value. Throw an
     * Exception if a data element with this key does not exist already.
     *
     * @param toUpdate the StoreValue to update
     * @throws StoreKeyDoesNotExistException thrown if no data element exists in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    public synchronized void update(
            StoreValue toUpdate )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".update( " + toUpdate + " )" );
        }
        try {
            StoreValue already = theDelegate.get( toUpdate.getKey() );
            if( already == null ) {
                throw new StoreKeyDoesNotExistException( this, toUpdate.getKey() );
            }
            theDelegate.put( toUpdate.getKey(), toUpdate );

        } finally {
            fireUpdatePerformed( toUpdate );
        }
    }
    
    /**
     * Put (if does not exist already) or update (if it does exist) a data element in the Store.
     *
     * @param key the key with which the specified data element is already, and will continue to be stored
     * @param encodingId the id of the encoding that was used to encode the data element.
     * @param timeCreated the time at which the data element was created
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires if positive, the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @return true if the value was updated, false if it was put
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #update if a data element with this key exists already
     */
    @Override
    public boolean putOrUpdate(
            String  key,
            String  encodingId,
            long    timeCreated,
            long    timeUpdated,
            long    timeRead,
            long    timeExpires,
            byte [] data )
        throws
            IOException
    {
        checkKey(      key );
        checkEncoding( encodingId );
        checkData(     data );

        return putOrUpdate( new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data ));
    }

    /**
     * Put (if does not exist already) or update (if it does exist) a data element in the Store.
     *
     * @param toStoreOrUpdate the StoreValue to store or update
     * @return true if the value was updated, false if it was put
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #update if a data element with this key exists already
     */
    public synchronized boolean putOrUpdate(
            StoreValue toStoreOrUpdate )
        throws
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".putOrUpdate( " + toStoreOrUpdate + " )" );
        }

        StoreValue already = theDelegate.put( toStoreOrUpdate.getKey(), toStoreOrUpdate );
        
        if( already != null ) {
            fireUpdatePerformed( toStoreOrUpdate );
            return true;

        } else {
            firePutPerformed( toStoreOrUpdate );
            return false;
        }
    }

    /**
     * Obtain a data element and associated meta-data from the Store, given a key.
     *
     * @param key the key to the data element in the Store
     * @return the StoreValue stored in the Store for this key; this encapsulates data element and meta-data
     * @throws StoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put to initially store a data element
     */
    public synchronized StoreValue get(
            String key )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        checkKey( key );

        StoreValue ret = theDelegate.get( key );

        if( log.isDebugEnabled() ) {
            log.debug( this + ".get( " + key + " ) returns " + ret );
        }
        if( ret != null ) {
            fireGetPerformed( ret );
        } else {
            fireGetFailed( key );
        }
        
        if( ret == null ) {
            throw new StoreKeyDoesNotExistException( this, key );
        }
        return ret;
    }

    /**
     * Delete the data element that is stored using this key.
     *
     * @param key the key to the data element in the Store
     * @throws StoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     */
    public void delete(
            String key )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        checkKey( key );

        if( log.isDebugEnabled() ) {
            log.debug( this + ".delete( " + key + " )" );
        }

        StoreValue already = theDelegate.remove( key );

        fireDeletePerformed( key );

        if( already == null ) {
            throw new StoreKeyDoesNotExistException( this, key );
        }
        // otherwise no harm done
    }

    /**
     * Remove all data elements in this Store.
     *
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public synchronized void deleteAll()
        throws
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".deleteAll()" );
        }
        fireDeleteAllPerformed( "" );

        theDelegate.clear();
    }

    /**
     * Remove all data in this Store whose keys start with this string.
     *
     * @param startsWith the String the key starts with
     * @throws IOException thrown if an I/O error occurred
     */
    public synchronized void deleteAll(
            String startsWith )
        throws
            IOException
    {
        checkKey( startsWith );

        if( log.isDebugEnabled() ) {
            log.debug( this + ".deleteAll( " + startsWith + " )" );
        }
        for( String key : theDelegate.keySet() ) {
            if( key.startsWith( startsWith )) {
                theDelegate.remove( key );
            }
        }
        fireDeleteAllPerformed( startsWith );
    }

    /**
     * Obtain a CursorIterable instead of an Iterator.
     *
     * @return the CursorIterable
     */
    public synchronized IterableStoreCursor iterator()
    {
        return MyIterableStoreCursor.create( theDelegate );
    }

    /**
     * Determine the number of StoreValues in this Store whose key starts with this String
     *
     * @param startsWith the String the key starts with
     * @return the number of StoreValues in this Store whose key starts with this String
     */
    public synchronized int size(
            String startsWith )
    {
        int count = 0;
        for( String key : theDelegate.keySet() ) {
            if( key.startsWith( startsWith )) {
                ++count;
            }
        }
        return count;
    }

    /**
     * Convert to String format, for debugging.
     *
     * @return String format
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "theDelegate"
                },
                new Object[] {
                    theDelegate
                } );
    }

    /**
     * The in-memory storage.
     */
    protected HashMap<String,StoreValue> theDelegate = new HashMap<String,StoreValue>();
    
    /**
     * Cursor implementation for this class.
     */
    static class MyIterableStoreCursor
            extends
                MapCursorIterator.Values<String,StoreValue>
           implements
                IterableStoreCursor
    {
        /**
         * Factory method.
         * 
         * @param delegate the HashMap that stores the content
         * @return the IterableStoreCursor
         */
        @SuppressWarnings(value={"unchecked"})
        public static MyIterableStoreCursor create(
                HashMap<String,StoreValue> delegate )
        {
            Entry<String,StoreValue> [] entries  = (Entry<String,StoreValue> []) ArrayHelper.createArray( Entry.class, delegate.size() );
            entries  = delegate.entrySet().toArray( entries );

            return new MyIterableStoreCursor( delegate, entries, ArrayCursorIterator.<Entry<String,StoreValue>>create( entries ));
        }

        /**
         * Constructor.
         * 
         * @param delegate the HashMap that stores the content
         * @param entries the entries in the HashMap, in a sequence
         * @param delegateIter the delegate iterator over the entries in a sequence
         */
        protected MyIterableStoreCursor(
                Map<String,StoreValue>                        delegate,
                Entry<String,StoreValue> []                   entries,
                ArrayCursorIterator<Entry<String,StoreValue>> delegateIter )
        {
            super( delegate, String.class, StoreValue.class, entries, delegateIter );
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #get get} is invoked
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
            int oldPos = theDelegate.getPosition();
            theDelegate.reset();
            while( theDelegate.hasNext() ) {
                String found = theDelegate.next().getKey();

                if( key.equals( found )) {
                    theDelegate.previous(); // went one too far
                    int newPos = theDelegate.getPosition();
                    return newPos - oldPos;
                }
            }
            throw new NoSuchElementException( "No such element: " + key ); 
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #get get} is invoked
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
            int oldPos = theDelegate.getPosition();
            theDelegate.reset();
            while( theDelegate.hasNext() ) {
                String found = theDelegate.next().getKey();

                if( key.equals( found )) {
                    // this is the right place, not one too far
                    int newPos = theDelegate.getPosition();
                    return newPos - oldPos;
                }
            }
            throw new NoSuchElementException( "No such element: " + key ); 
        }

        /**
         * Clone this position.
         *
         * @return identical new instance
         */
        @Override
        public MyIterableStoreCursor createCopy()
        {
            ArrayCursorIterator<Entry<String,StoreValue>> delegateIter
                    = ArrayCursorIterator.<Entry<String,StoreValue>>create( theEntries, theDelegate.getPosition() );
            return new MyIterableStoreCursor( theMap, theEntries, delegateIter );
        }
    }
}
