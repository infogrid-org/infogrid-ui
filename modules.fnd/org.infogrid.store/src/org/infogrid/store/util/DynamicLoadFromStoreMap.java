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

import org.infogrid.store.Store;
import org.infogrid.store.StoreKeyDoesNotExistException;

import org.infogrid.util.AbstractCachingMap;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.MapCursorIterator;
import org.infogrid.util.logging.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A <code>org.infogrid.util.CachingMap} whose cache is either entirely empty or complete,
 * and if empty, is transparently reloaded from the specified {@link org.infogrid.store.Store}.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public abstract class DynamicLoadFromStoreMap<K,V>
        extends
            AbstractCachingMap<K,V>
{
    private static final Log log = Log.getLogInstance( DynamicLoadFromStoreMap.class ); // our own, private logger
    
    /**
     * Constructor.
     *
     * @param store the underlying Store
     * @param storeEntryKey the key used to write the entire content of the Map into the Store
     */
    protected DynamicLoadFromStoreMap(
            Store  store,
            String storeEntryKey )
    {
        theStore         = store;
        theStoreEntryKey = storeEntryKey;
        theDelegate      = null; // initially empty
    }

    /**
     * Clear the local cache.
     */
    public synchronized void clearLocalCache()
    {
        saveToStore();
        theDelegate = null;
    }

    /**
     * Determine whether this CachingMap is persistent.
     *
     * @return true if it is persistent
     */
    public boolean isPersistent()
    {
        return true;
    }
    
    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size()
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        return delegate.size();
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty()
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        return delegate.isEmpty();
    }

    /**
     * Obtain a CursorIterator on the keys of this Map.
     *
     * @param keyArrayComponentType the class using which arrays of keys are allocated
     * @param valueArrayComponentType the class using which arrays of values are allocated
     * @return the CursorIterator
     */
    public CursorIterator<K> keysIterator(
            Class<K> keyArrayComponentType,
            Class<V> valueArrayComponentType )
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        
        CursorIterator<K> ret = MapCursorIterator.createForKeys( delegate, keyArrayComponentType, valueArrayComponentType );

        return ret;
    }

    /**
     * Obtain a CursorIterator on the values of this Map.
     *
     * @param keyArrayComponentType the class using which arrays of keys are allocated
     * @param valueArrayComponentType the class using which arrays of values are allocated
     * @return the CursorIterator
     */
    public CursorIterator<V> valuesIterator(
            Class<K> keyArrayComponentType,
            Class<V> valueArrayComponentType )
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        
        CursorIterator<V> ret = MapCursorIterator.createForValues( delegate, keyArrayComponentType, valueArrayComponentType );

        return ret;
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
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        return delegate.containsKey( key );
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
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        return delegate.containsValue( value );
    }

    /**
     * Returns the value to which this map maps the specified key.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *	       <tt>null</tt> if the map contains no mapping for this key.
     */
    public V get(
            Object key )
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        return delegate.get( key );
    }

    /**
     * Associates the specified value with the specified key in this map.
     * 
     * @param key the key
     * @param value the value
     * @return the old value previously stored using the same key, if any
     */
    public V put(
            K key,
            V value )
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        return delegate.put( key, value );
    }

    /**
     * Associates the specified value with the specified key in this map.
     * This is the same operation as <code>put</code>, but does not return the previous
     * value. In many cases, the return value of the put operation is ignored, but
     * providing it may incur substantial overhead (e.g. reading the old value from
     * disk); this method avoids that.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     */
    public void putIgnorePrevious(
            K key,
            V value )
    {
        V old = put( key, value );
    }

    /**
     * Removes the mapping for this key from this map if it is present.
     * 
     * @param key the key
     * @return the old value previously stored using the same key, if any
     */
    public V remove(
            Object key )
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        return delegate.remove( key );
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     *
     * @param t Mappings to be stored in this map.
     */
    public void putAll(
            Map<? extends K, ? extends V> t )
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        delegate.putAll( t );
    }

    /**
     * Removes all mappings from this map.
     */
    public synchronized void clear()
    {
        theDelegate = null;
        
        try {
            theStore.deleteAll();

        } catch( IOException ex ) {
            log.error( ex );
        }
    }

    /**
     * Returns a set view of the keys contained in this map.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set<K> keySet()
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        return delegate.keySet();
    }

    /**
     * Returns a collection view of the values contained in this map.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection<V> values()
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        return delegate.values();
    }

    /**
     * Returns a set view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     */
    public Set<Map.Entry<K, V>> entrySet()
    {
        HashMap<K,V> delegate = theDelegate; // trick to get around a synchronized statement

        if( delegate == null ) {
            delegate = ensureLoaded();
        }
        return delegate.entrySet();
    }

    /**
     * Compares the specified object with this map for equality.
     *
     * @param o object to be compared for equality with this map.
     * @return <tt>true</tt> if the specified object is equal to this map.
     */
    @Override
    public boolean equals(
            Object o )
    {
        return o == this;
    }

    /**
     * Hash code.
     * 
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return theDelegate.hashCode();
    }

    /**
     * Ensure that the DynamicLoadFromStoreMap is fully loaded. Return the delegate.
     *
     * @return the delegate
     */
    protected synchronized HashMap<K,V> ensureLoaded()
    {
        if( theDelegate == null ) {
            try {
                theDelegate = load();

            } catch( StoreKeyDoesNotExistException ex ) {
                // that's fine
                theDelegate = new HashMap<K,V>(); // empty
                
            } catch( IOException ex ) {
                throw new RuntimeException( ex );
            }
        }
        
        return theDelegate;
    }

    /**
     * Save the content of this DynamicLoadFromStoreMap to Store.
     */
    public void saveToStore()
    {
        if( theDelegate != null ) {
            try {
                save();

            } catch( IOException ex ) {
                throw new RuntimeException( ex );
            }
        }        
    }

    /**
     * Create a new delegate HashMap by loading from the Store.
     *
     * @return the new delegate HashMap
     * @throws StoreKeyDoesNotExistException thrown if no map content could be found
     * @throws IOException thrown if loading failed
     */
    protected abstract HashMap<K,V> load()
            throws
                StoreKeyDoesNotExistException,
                IOException;

    /**
     * Save the delegate HashMap to the Store.
     *
     * @throws IOException thrown if saving failed
     */
    protected abstract void save()
            throws
                IOException;

    /**
     * The underlying HashMap if currently available.
     */
    protected HashMap<K,V> theDelegate;
    
    /**
     * The Store to save from/to.
     */
    protected Store theStore;
    
    /**
     * The key of the StoreValue into which to write the entire content of this Map.
     */
    protected String theStoreEntryKey;
}
