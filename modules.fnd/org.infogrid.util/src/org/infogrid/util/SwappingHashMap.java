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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.infogrid.util.logging.Log;

/**
 * This class is basically the opposite of the JDK's <code>java.util.WeakHashMap</code>. While
 * WeakHashMap deallocates entries when the key is not referenced any more, this
 * class references all of its values via References, and thus may lose the
 * value (which in turn prompts the removal of the key). This class contains
 * hooks that may be overridden by subclasses in order to "swap in", from some
 * other place (outside of the scope of this class) a previously deallocated value.
 * In the comments to this class, and in some of the method calls, this other place
 * is called "storage".
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public abstract class SwappingHashMap<K,V>
        extends
            AbstractCachingMap<K,V>
{
    private static final Log log = Log.getLogInstance( SwappingHashMap.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param initialSize the initial size of the cache
     */
    protected SwappingHashMap(
            int initialSize )
    {
        theDelegate = new HashMap<K,Reference<V>>( initialSize );
    }

    /**
     * Factory method for a subclass of Reference.
     *
     * @param key the key
     * @param value the value
     * @return the Reference to the value
     */
    protected abstract Reference<V> createReference(
            K key,
            V value );

    /**
     * Clean up deleted references.
     */
    @SuppressWarnings(value={"unchecked"})
    protected void cleanup()
    {
        while( true ) {
            Reference<? extends V> current = theQueue.poll();
            if( current == null ) {
                break;
            }

            // we know that this queue only contains things that implement this interface, so this cast is safe
            EntryReference<K> realCurrent = (EntryReference<K>) current;
            K key = realCurrent.getKey();
            theDelegate.remove( key );

            if( log.isDebugEnabled() ) {
                log.debug( this + ".cleanup() of object with key " + key );
            }
            
            fireValueCleanedUp( key );
        }
    }

    /**
     * Returns the number of key-value mappings in this SwappingHashMap. This class returns the
     * size of the cache because it does not have a store; however, subclasses might
     * and they are encouraged to return the size of the entire HashMap, not just
     * the size of the cache.
     *
     * @return the number of key-value mappings in this SwappingHashMap.
     */
    public synchronized int size()
    {
        cleanup();
        return theDelegate.size();
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return true if this is empty
     */
    public synchronized boolean isEmpty()
    {
        cleanup();
        return theDelegate.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this SwappingHashMap contains a mapping for the specified
     * key.
     *
     * @param key the key
     * @return true if this SwappingHashMap contains a mapping for this key
     */
    @SuppressWarnings(value={"unchecked"})
    public synchronized boolean containsKey(
            Object key )
    {
        cleanup();

        Reference<V> ref = theDelegate.get( key );
        V found = ref != null ? ref.get() : null;

        boolean ret = found != null;
        if( found == null ) {
            found = loadValueFromStorage( key );
            theSwappingListeners.fireEvent( new Pair<K,V>( (K) key, found ), 0 );
            ret = found != null;
        }
        return ret;
    }

    /**
     * This method may be overridden by subclasses, to swap in a value that currently
     * is not contained in the local cache.
     *
     * @param key the key whose value should be loaded
     * @return the value that was loaded, or null if none.
     */
    protected V loadValueFromStorage(
            Object key )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "loadValueFromStorage", key );
        }
        return null; // on this level
    }

    /**
     * Returns <tt>true</tt> if this SwappingHashMap maps one or more keys to the
     * specified value.
     * 
     * @param value the value to check for
     * @return true if there is at least one key that maps to this value
     * @throws UnsupportedOperationException this is currently not implemented (FIXME)
     */
    public boolean containsValue(
            Object value )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the value to which this SwappingHashMap maps the specified key.
     *
     * @param key the key
     * @return the value, if any
     */
    @SuppressWarnings(value={"unchecked"})
    public synchronized V get(
            Object key )
    {
        cleanup();
        Reference<V> found = theDelegate.get( key );
        V ret = found != null ? found.get() : null;
        
        if( ret == null ) {
            ret = loadValueFromStorage( key );
            if( ret != null ) {
                theDelegate.put( (K) key, createReference( (K) key, ret ));
            }
            theSwappingListeners.fireEvent( new Pair<K,V>( (K) key, ret ), 0 ); // this is here, not in the method, in order to allow for easy subclassing
        }
        return ret;
    }

    /**
     * Associates the specified value with the specified key in this SwappingHashMap.
     *
     * @param key the key for the value
     * @param value the new value for the key
     * @return the old value for the key, if any
     */
    public V put(
            K key,
            V value )
    {
        V ret = internalPut( key, value, true );

        return ret;
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
        V old = internalPut( key, value, false );
    }

    /**
     * Helper method to avoid having almost the same code in two places.
     *
     * @param key the key for the value
     * @param value the new value for the key
     * @param attemptLoad if true, attempt to load the old value
     * @return the old value for the key, if any
     */
    protected synchronized V internalPut(
            K       key,
            V       value,
            boolean attemptLoad )
    {
        cleanup();
        Reference<V> found = theDelegate.put( key, createReference( key, value ));
        V ret = found != null ? found.get() : null;

        if( attemptLoad && ret == null ) {
            ret = loadValueFromStorage( key );
        }

        saveValueToStorage( key, value );
        theSwappingListeners.fireEvent( new Pair<K,V>( key, value ), 1 ); // this is here, not in the method, in order to allow for easy subclassing

        if( ret != null ) {
            fireValueRemoved( key );
        }
        if( value != null ) {
            fireValueAdded( key, value );
        }
        return ret;
    }

    /**
     * This method may be overridden by subclasses, to save a value to storage.
     *
     * @param key the key whose value was updated
     * @param newValue the new value
     */
    protected void saveValueToStorage(
            K key,
            V newValue )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "saveValueToStorage", key, newValue );
        }
        // no op on this level
    }

    /**
     * Removes the mapping for this key from this map if it is present
     * (optional operation).
     *
     * @param key the key for the mapping to remove
     * @return the old value for the key, if any
     */
    @SuppressWarnings(value={"unchecked"})
    public synchronized V remove(
            Object key )
    {
        cleanup();
        Reference<V> found = theDelegate.remove( key );
        V ret = found != null ? found.get() : null;
        
        if( ret == null ) {
            ret = loadValueFromStorage( key );
            theSwappingListeners.fireEvent( new Pair<K,V>( (K) key, null ), 0 ); // this is here, not in the method, in order to allow for easy subclassing
        }
        if( ret != null ) {
            removeValueFromStorage( key );
            theSwappingListeners.fireEvent( new Pair<K,V>( (K) key, null ), 2 ); // this is here, not in the method, in order to allow for easy subclassing
        }
        if( ret != null ) {
            fireValueRemoved( (K) key );
        }
        return ret;
    }
    
    /**
     * This method may be overridden by subclasses, to remove a value from storage.
     *
     * @param key the key whose value is to be removed
     */
    protected void removeValueFromStorage(
            Object key )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "removeValueFromStorage", key );
        }
        // no op on this level.
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * (optional operation).
     *
     * @param t the Map whose content is to be added
     * @throws UnsupportedOperationException always thrown as this is currently not implemented (FIXME)
     */
    public void putAll(
            Map<? extends K, ? extends V> t )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all mappings from this map (optional operation).
     */
    public synchronized void clear()
    {
        theDelegate.clear();
        clearStorage();
    }

    /**
     * Removes only the locally cached mappings from this map. This keeps
     * the content of the Map and only affects the cache.
     */
    public synchronized void clearLocalCache()
    {
        theDelegate.clear();
    }

    /**
     * Determine whether this CachingMap is persistent.
     *
     * @return true if it is persistent
     */
    public boolean isPersistent()
    {
        return true; // we are backed by a Store
    }

    /**
     * This method may be overridden by subclasses, to be notified when all
     * elements have been removed from this Map.
     */
    protected void clearStorage()
    {
        // no op on this level
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "clearStorage" );
        }
    }

    /**
     * Returns a set view of the keys contained in this map.
     *
     * @return the Set of keys
     */
    public synchronized Set<K> keySet()
    {
        cleanup();
        return theDelegate.keySet();
    }

    /**
     * Returns a collection view of the values contained in this map. 
     *
     * @return the collection of values
     */
    public synchronized Collection<V> values()
    {
        if( theValueCollection == null ) {
            theValueCollection = new MyValueCollection<K,V>( this );
        }
        return theValueCollection;
    }

    /**
     * Returns a set view of the mappings contained in this map.
     *
     * @return a set of key mappings
     */
    public Set<Map.Entry<K,V>> entrySet()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Obtain the underlying cache.
     *
     * @return the underlying cache
     */
    protected HashMap<K,Reference<V>> getUnderlyingCache()
    {
        return theDelegate;
    }

    /**
     * Add a listener for SwappingHashMap events.
     *
     * @param newListener the new listener
     */
    public void addDirectSwappingHashMapListener(
            SwappingHashMapListener<K,V> newListener )
    {
        theSwappingListeners.addDirect( newListener );
    }

    /**
     * Add a listener for SwappingHashMap events.
     *
     * @param newListener the new listener
     */
    public void addWeakSwappingHashMapListener(
            SwappingHashMapListener<K,V> newListener )
    {
        theSwappingListeners.addWeak( newListener );
    }

    /**
     * Add a listener for SwappingHashMap events.
     *
     * @param newListener the new listener
     */
    public void addSoftSwappingHashMapListener(
            SwappingHashMapListener<K,V> newListener )
    {
        theSwappingListeners.addSoft( newListener );
    }

    /**
     * Remove a listener for SwappingHashMap events.
     *
     * @param oldListener the old listener
     */
    public void removeSwappingHashMapListener(
            SwappingHashMapListener<K,V> oldListener )
    {
        theSwappingListeners.remove( oldListener );
    }

    /**
     * The underlying store.
     */
    protected HashMap<K,Reference<V>> theDelegate;

    /**
     * A "projection" of the referenced values in the collection.
     */
    protected MyValueCollection<K,V> theValueCollection;

    /**
     * The queue that into which deleted References are inserted.
     */
    protected ReferenceQueue<V> theQueue = new ReferenceQueue<V>();

    /**
     * Listeners of this SwappingHashMap.
     */
    private FlexibleListenerSet<SwappingHashMapListener<K,V>,Pair<K,V>,Integer> theSwappingListeners = new FlexibleListenerSet<SwappingHashMapListener<K,V>,Pair<K,V>,Integer>() {
            /**
             * Fire the event to one contained object.
             *
             * @param listener the receiver of this event
             * @param event the sent event
             * @param parameter dispatch parameter
             */
            protected void fireEventToListener(
                    SwappingHashMapListener<K,V> listener,
                    Pair<K,V>                    event,
                    Integer                      parameter )
            {
                switch( parameter ) {
                    case 0:
                        listener.loadedFromStorage( SwappingHashMap.this, event.getName(), event.getValue() );
                        break;
                    case 1:
                        listener.savedToStorage( SwappingHashMap.this, event.getName(), event.getValue() );
                        break;
                    case 2:
                        listener.removedFromStorage( SwappingHashMap.this, event.getName() );
                        break;
                    default:
                        log.error( "should not be here: " + parameter );
                }
            }
    };

    /**
     * Common for SoftEntryReference and WeakEntryReference.
     * 
     * @param <K> the type of key
     */
    static interface EntryReference<K>
    {
        /**
         * Obtain the key.
         *
         * @return the key
         */
        public K getKey();
    }

    /**
     * Override SoftReference to also hold the key.
     * 
     * @param <K> the type of key
     * @param <V> the type of value
     */
    protected static class SoftEntryReference<K,V>
            extends
                SoftReference<V>
            implements
                EntryReference<K>
    {
        /**
         * Constructor.
         *
         * @param key the key
         * @param value the value
         * @param queue the ReferenceQueue into which the cleared Reference will be inserted
         */
        public SoftEntryReference(
                K                 key,
                V                 value,
                ReferenceQueue<V> queue )
        {
            super( value, queue );
            
            theKey = key;
        }
        
        /**
         * Obtain the key.
         *
         * @return the key
         */
        public K getKey()
        {
            return theKey;
        }
        
        /**
         * The key.
         */
        protected K theKey;
    }

    /**
     * Override WeakReference to also hold the key.
     * 
     * @param <K> the type of key
     * @param <V> the type of value
     */
    protected static class WeakEntryReference<K,V>
            extends
                WeakReference<V>
            implements
                EntryReference<K>
    {
        /**
         * Constructor.
         *
         * @param key the key
         * @param value the value
         * @param queue the ReferenceQueue into which the cleared Reference will be inserted
         */
        public WeakEntryReference(
                K                 key,
                V                 value,
                ReferenceQueue<V> queue )
        {
            super( value, queue );
            
            theKey = key;
        }
        
        /**
         * Obtain the key.
         *
         * @return the key
         */
        public K getKey()
        {
            return theKey;
        }

        /**
         * The key.
         */
        protected K theKey;
    }
    
    /**
     * This class is instantiated to create a "projection" of the values in the MyReferenceMap.
     * 
     * @param <K> the type of key
     * @param <V> the type of value
     */
    protected static class MyValueCollection<K,V>
            extends
                AbstractCollection<V>
    {
        /**
         * Constructor.
         *
         * @param map the underlying map
         */
        public MyValueCollection(
                SwappingHashMap<K,V> map )
        {
            theDelegate = map;
        }
        
        /**
         * Returns an Iterator over the elements contained in this collection.
         *
         * @return an Iterator over the elements contained in this collection.
         */
        public Iterator<V> iterator()
        {
            return new MyValueIterator<K,V>( theDelegate );
        }

        /**
         * Returns the number of elements in this collection.
         *
         * @return the number of elements in this collection.
         */
        public int size()
        {
            return theDelegate.size();
        }

        /**
         * The underlying SwappingHashMap.
         */
        protected SwappingHashMap<K,V> theDelegate;
    }
    
    /**
     * Iterator over the MyValueCollection.
     * 
     * @param <K> the type of key
     * @param <V> the type of value
     */
    static class MyValueIterator<K,V>
            implements
                Iterator<V>
    {
        /**
         * Constructor.
         *
         * @param map the SwappingHashMap over whose values we iterate
         */
        public MyValueIterator(
                SwappingHashMap<K,V> map )
        {
            theDelegate = map.getUnderlyingCache().values().iterator();
            
            goNext();
        }
        
        /**
         * Advance to the next element.
         */
        protected void goNext()
        {
            while( theDelegate.hasNext() ) {
                Reference<V> currentRef = theDelegate.next();
                V            current    = currentRef.get();
                if( current != null ) {
                    theNextReturn = current;
                    return;
                }
            }
            theNextReturn = null;
        }

        /**
         * Are there more elements?
         *
         * @return true if there are
         */
        public boolean hasNext()
        {
            return theNextReturn != null;
        }
        
        /**
         * Obtain the next element.
         *
         * @return the next element
         */
        public V next()
        {
            if( theNextReturn == null ) {
                return null;
            }
            V ret = theNextReturn;
            goNext();
            return ret;
        }

        /**
         * Remove the current element
         *
         * @throws UnsupportedOperationException
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * The underlying Iterator.
         */
        protected Iterator<Reference<V>> theDelegate;
        
        /**
         * The next object to return, if any.
         */
        protected V theNextReturn;
    }
}
