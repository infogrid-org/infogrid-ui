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

import java.util.*;

/**
 * A cache with a limited size, and a maximum age for elements.
 * 
 * @param K the type of key
 * @param V the type of value
 */
public class TimeSpaceLimitedCache<K,V>
    implements
        Map<K,V>
{
    /**
     * Constructor.
     *
     * @param maxSize the maximum number of elements in the cache
     * @param maxAge the number of milliseconds until cache elements expire
     */
    public TimeSpaceLimitedCache(
            int  maxSize,
            long maxAge )
    {
        theMaxSize = maxSize;
        theMaxAge  = maxAge;

        theMap  = new HashMap<K,CacheEntry<K,V>>( maxSize );
        theList = new ArrayList<CacheEntry<K,V>>( maxSize );
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size()
    {
        return theList.size(); // that might be faster than asking theMap
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty()
    {
        return theMap.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified
     *         key.
     */
    public boolean containsKey(
            Object key )
    {
        return get( key ) != null;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     */
    public boolean containsValue(
            Object value )
    {
        Iterator iter = theList.iterator();
        while( iter.hasNext() ) {
            CacheEntry<K,V> current = theMap.get( iter.next() );
            current = filterExpired( current );
            if( current != null ) {
                if( value == null ) {
                    if( current.getValue() == null ) {
                        return true;
                    }
                } else if( value.equals( current.getValue() )) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the value to which this map maps the specified key.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *	       <tt>null</tt> if the map contains no mapping for this key.
     *
     * @see #containsKey(Object)
     */
    public V get(
            Object key )
    {
        CacheEntry<K,V> entry = theMap.get( key );
        if( entry != null ) {
            entry = filterExpired( entry );
        }
        if( entry != null ) {
            touch( entry );
            return entry.getValue();
        } else {
            return null;
        }
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the map previously associated <tt>null</tt>
     *	       with the specified key, if the implementation supports
     *	       <tt>null</tt> values.
     */
    public V put(
            K key,
            V value )
    {
        removeExpired();

        // check that there is room
        if( size() >= theMaxSize ) {
            CacheEntry<K,V> oldest = theList.remove( theMaxSize-1 );
            theMap.remove( oldest.getKey() );
        }

        // now add
        CacheEntry<K,V> newEntry = new CacheEntry<K,V>( key, value, System.currentTimeMillis() + theMaxAge );
        CacheEntry<K,V> oldEntry = theMap.put( key, newEntry );
        if( oldEntry != null ) {
            theList.remove( oldEntry );
        }
        theList.add( 0, newEntry ); // at the beginning
        if( oldEntry != null ) {
            return oldEntry.getValue();
        } else {
            return null;
        }
    }

    /**
     * Removes the mapping for this key from this map if it is present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.
     */
    public V remove(
            Object key )
    {
        CacheEntry<K,V> removed = theMap.remove( key );
        if( removed != null ) {
            theList.remove( removed );
            return removed.getValue();
        }
        return null;
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     *
     * @param t Mappings to be stored in this map.
     */
    public void putAll(
            Map<? extends K,? extends V> t )
    {
        Iterator<? extends K> iter = t.keySet().iterator();
        while( iter.hasNext() ) {
            K key   = iter.next();
            V value = t.get( key );

            put( key, value );
        }
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear()
    {
        theMap.clear();
        theList.clear();
    }

    /**
     * Returns a set view of the keys contained in this map.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set<K> keySet()
    {
        return theMap.keySet();
    }

    /**
     * Returns a collection view of the values contained in this map.
     *
     * @return a collection view of the values contained in this map.
     * @throws UnsupportedOperationException always thrown
     */
    public Collection<V> values()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    /**
     * Returns a set view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     * @throws UnsupportedOperationException always thrown
     */
    public Set<Entry<K,V>> entrySet()
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    /**
     * Helper method that takes out this value from the cache, and all subsequent ones,
     * it is expired.
     *
     * @param candidate the CacheEntry to filter
     * @return null if the entry has been taken out, the passed-in entry otherwise
     */
    protected CacheEntry<K,V> filterExpired(
            CacheEntry<K,V> candidate )
    {
        if( !candidate.isExpired()) {
            return candidate;
        }
        removeExpired(); // this is a good time to get rid of others
        return null;
    }

    /**
     * Helper method to remove the trailing, expired end of the cache.
     */
    public void removeExpired()
    {
        int i = theList.size()-1;
        for( ; i>=0 ; --i ) {
            CacheEntry<K,V> current = theList.get( i );
            if( current.isExpired() ) {
                theMap.remove( current.getKey() );
                theList.remove( i );
            }
        }
    }

    /**
     * Helper method to update the expiration time of an entry.
     * 
     * @param current the CacheEntry to touch
     */
    protected void touch(
            CacheEntry<K,V> current )
    {
        theList.remove( current );
        current.setExpirationTime( System.currentTimeMillis() + theMaxAge );
        theList.add( 0, current );
    }

    /**
     * The maximum number of elements in this Cache at any point in time.
     */
    protected int theMaxSize;

    /**
     * The maximum age, in milliseconds, of entries in this cache.
     */
    protected long theMaxAge;

    /**
     * This maps keys to CacheEntry objects.
     */
    protected HashMap<K,CacheEntry<K,V>> theMap;

    /**
     * This is our priority queue. Because our priorities change (when stored objects
     * are being accessed), we don't use a pre-built class for this.
     */
    protected ArrayList<CacheEntry<K,V>> theList;

    /**
     * Our entry contains the object to be stored as well as a time stamp
     * when this entry was last accessed.
     * 
     * @param K the type of key
     * @param V the type of value
     */
    protected static class CacheEntry<K,V>
    {
        /**
         * Constructor.
         * 
         * @param key the key of the CacheEntry
         * @param value the value of the CacheEntry
         * @param expiration the expiration time of the CacheEntry
         */
        public CacheEntry(
                K    key,
                V    value,
                long expiration )
        {
            theKey        = key;
            theValue      = value;
            theExpiration = expiration;
        }

        /**
         * Set a new expiration time.
         *
         * @param expiration the new expiration time
         */
        public void setExpirationTime(
                long expiration )
        {
            theExpiration = expiration;
        }

        /**
         * Determine whether this entry is expired.
         *
         * @return true of this entry is expired
         */
        public boolean isExpired()
        {
           return System.currentTimeMillis() > theExpiration;
        }

        /**
         * Obtain the key Object.
         *
         * @return the key Object
         */
        public K getKey()
        {
            return theKey;
        }

        /**
         * Obtain the payload Object.
         *
         * @return the payload Object
         */
        public V getValue()
        {
            return theValue;
        }

        /**
         * Needs to have an equals method to correctly implement entry overwrite.
         *
         * @param other the Object to compare with
         * @return true if the objects are equal
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( other instanceof CacheEntry ) {

                CacheEntry realOther = (CacheEntry) other;
                if( theKey == null ) {
                    return realOther.theKey == null;
                } else {
                    return theKey.equals( realOther.theKey );
                }
            }
            return false;
        }

        /**
         * Needs to have a hashCode method to correctly implement entry overwrite.
         * 
         * @return the hashCode
         */
        @Override
        public int hashCode()
        {
            if( theKey != null ) {
                return theKey.hashCode();
            } else {
                return 0;
            }
        }

        /**
         * The key object.
         */
        protected K theKey;

        /**
         * The payload object.
         */
        protected V theValue;

        /**
         * The time, in milliseconds of the local clock, when this entry is supposed
         * to expire.
         */
        protected long theExpiration;
    }
    
    /**
     * Iterator that knows how to iterate over the values, given an iterator over a set
     * of CacheEntry.
     * 
     * @param K the type of key
     * @param V the type of value
     */
    static class ValueIterator<K,V>
            implements
                Iterator<V>
    {
        /**
         * Constructor.
         *
         * @param delegate the delegate Iterator
         */
        public ValueIterator(
                Iterator<CacheEntry<K,V>> delegate )
        {
            theDelegate = delegate;
        }

        /**
         * Is there another element?
         *
         * @return true if there is another element
         */
        public boolean hasNext()
        {
            return theDelegate.hasNext();
        }

        /**
         * Obtain the next element.
         *
         * @return the next element
         */
        public V next()
        {
            CacheEntry<K,V> almost = theDelegate.next();
            if( almost != null ) {
                return almost.getValue();
            } else {
                return null;
            }
        }
        
        /**
         * Remove the current element
         */
        public void remove()
        {
            throw new UnsupportedOperationException( "not implemented" );
        }
 
        /**
         * The underlying delegate iterator.
         */
        protected Iterator<CacheEntry<K,V>> theDelegate;
    }
}
