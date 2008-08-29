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

package org.infogrid.store.sql;

import org.infogrid.store.AbstractKeyBasedIterableStoreCursor;
import org.infogrid.store.StoreValue;
import org.infogrid.util.logging.Log;

/**
 * Iterator implementation for the StoreValues in the SqlStore.
 * FIXME: This currently does not deal very well with moving to the very beginning or the very end of the Store.
 */
class SqlStoreIterator
        extends
            AbstractKeyBasedIterableStoreCursor
{
    private static final Log log = Log.getLogInstance( SqlStoreIterator.class ); // our own, private logger

    /**
     * Constructor. Start at the beginning.
     *
     * @param store the SqlStore to iterate over
     */
    protected SqlStoreIterator(
            SqlStore store )
    {
        this( store, "" );
    }
    
    /**
     * Constructor. Start at a defined place.
     *
     * @param store the SqlStore to iterate over
     * @param position the key of the current position
     */
    protected SqlStoreIterator(
            SqlStore   store,
            String     position )
    {
        super( store, position );
    }

    /**
     * Find the next n StoreValues, including the StoreValue for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key key for the first StoreValue
     * @param n the number of StoreValues to find
     * @return the found StoreValues
     */
    protected StoreValue [] findNextIncluding(
            String key,
            int    n )
    {
        StoreValue [] ret = ((SqlStore)theStore).findNextIncluding( key, n );
        return ret;
    }
    
    /**
     * Find the next n keys, including key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key the first key
     * @param n the number of keys to find
     * @return the found keys
     */
    protected String [] findNextKeyIncluding(
            String key,
            int    n )
    {
        String [] ret = ((SqlStore)theStore).findNextKeyIncluding( key, n );
        return ret;
    }

    /**
     * Find the previous n StoreValues, excluding the StoreValue for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key key for the first StoreValue NOT to be returned
     * @param n the number of StoreValues to find
     * @return the found StoreValues
     */
    protected StoreValue [] findPreviousExcluding(
            String key,
            int    n )
    {
        StoreValue [] ret = ((SqlStore)theStore).findPreviousExcluding( key, n );
        return ret;
    }

    /**
     * Find the previous n keys, excluding the key for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key the first key NOT to be returned
     * @param n the number of keys to find
     * @return the found keys
     */
    protected String [] findPreviousKeyExcluding(
            String key,
            int    n )
    {
        String [] ret = ((SqlStore)theStore).findPreviousKeyExcluding( key, n );
        return ret;
    }
    
    /**
     * Count the number of elements following and including the one with the key.
     *
     * @param key the key
     * @return the number of elements
     */
    protected int hasNextIncluding(
            String key )
    {
        int ret = ((SqlStore)theStore).hasNextIncluding(  key );
        return ret;
    }

    /**
     * Count the number of elements preceding and excluding the one with the key.
     *
     * @param key the key
     * @return the number of elements
     */
    protected int hasPreviousExcluding(
            String key )
    {
        int ret = ((SqlStore)theStore).hasPreviousExcluding(  key );
        return ret;
    }

    /**
     * Find the key N elements up or down from the current key.
     *
     * @param key the current key
     * @param delta the number of elements up (positive) or down (negative)
     * @return the found key, or null
     */
    protected String findKeyAt(
            String key,
            int    delta )
    {
        String ret = ((SqlStore)theStore).findKeyAt( key, delta );
        return ret;
    }

    /**
     * Helper method to determine the number of elements between two keys.
     *
     * @param from the start key
     * @param to the end key
     * @return the distance
     */
    protected int determineDistance(
            String from,
            String to )
    {
        int ret = ((SqlStore)theStore).determineDistance( from, to );
        return ret;
    }
    
    /**
     * Determine the key at the very beginning.
     * 
     * @return the key
     */
    protected String getBeforeFirstPosition()
    {
        return "";
    }
    
    /**
     * Determine the key at the very end.
     * 
     * @return the key
     */
    protected String getAfterLastPosition()
    {
        return null;
    }
    
    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public SqlStoreIterator createCopy()
    {
        return new SqlStoreIterator( (SqlStore) theStore, thePosition );
    }
}
