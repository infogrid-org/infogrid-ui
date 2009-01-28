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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A degenerate implementation of {@link CachingMap} that uses a memory-only <code>HashMap</code>.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public class MCachingHashMap<K,V>
        extends
            HashMap<K,V>
        implements
            CachingMap<K,V>
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Factory method.
     * 
     * @return the created MCachingHashMap
     * @param <K> the type of key
     * @param <V> the type of value
     */
    public static <K,V> MCachingHashMap<K, V> create()
    {
        return new MCachingHashMap<K,V>();
    }

    /**
     * Factory method.
     * 
     * @param delegate the Map whose mappings are to be placed in this map.
     * @return the created MCachingHashMap
     * @param <K> the type of key
     * @param <V> the type of value
     */
    public static <K,V> MCachingHashMap<K, V> create(
            HashMap<? extends K, ? extends V> delegate )
    {
        return new MCachingHashMap<K,V>( delegate );
    }

    /**
     * Factory method.
     * 
     * @param initialCapacity the initial capacity of the CachingHashMap
     * @return the created MCachingHashMap
     * @param <K> the type of key
     * @param <V> the type of value
     */
    public static <K,V> MCachingHashMap<K, V> create(
            int initialCapacity )
    {
        return new MCachingHashMap<K,V>( initialCapacity );
    }

    /**
     * Factory method.
     * 
     * @param initialCapacity the initial capacity of the CachingHashMap
     * @param loadFactor the load factor
     * @return the created MCachingHashMap
     * @param <K> the type of key
     * @param <V> the type of value
     */
    public static <K,V> MCachingHashMap<K, V> create(
            int   initialCapacity,
            float loadFactor )
    {
        return new MCachingHashMap<K,V>( initialCapacity, loadFactor );
    }

    /**
     * Constructor.
     */
    protected MCachingHashMap()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param delegate the Map whose mappings are to be placed in this map.
     */
    protected MCachingHashMap(
            Map<? extends K, ?extends V> delegate )
    {
        super( delegate );
    }

    /**
     * Constructor.
     *
     * @param initialCapacity the initial capacity of the CachingHashMap
     */
    protected MCachingHashMap(
            int initialCapacity )
    {
        super( initialCapacity );
    }

    /**
     * Constructor.
     *
     * @param initialCapacity the initial capacity of the CachingHashMap
     * @param loadFactor the load factor
     */
    protected MCachingHashMap(
            int   initialCapacity,
            float loadFactor )
    {
        super( initialCapacity, loadFactor );
    }

    /**
     * Add a value.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the HashMap previously associated
     *	       <tt>null</tt> with the specified key.
     */
    @SuppressWarnings(value={"unchecked"})
    @Override
    public V put(
            K key,
            V value )
    {
        V ret = super.put( key, value );

        if( ret != null ) {
            theListeners.fireEvent( new CachingMapEvent.Removed( this, key ), 1 );
        }
        theListeners.fireEvent( new CachingMapEvent.Added( this, key, value ), 0 );
        
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
        V old = put( key, value );
    }

    /**
     * Remove a value.
     *
     * @param  key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the map previously associated <tt>null</tt>
     *	       with the specified key.
     */
    @SuppressWarnings(value={"unchecked"})
    @Override
    public V remove(
            Object key )
    {
        V ret = super.remove( key );
        
        if( ret != null ) {
            theListeners.fireEvent( new CachingMapEvent.Removed( this, key ), 1 );
        }
        return ret;
    }

    /**
     * Remove a key-value pair that was previously created. This does not affect
     * values that are currently still being constructed. The semantics of
     * &quot;remove&quot; for a SmartFactory imply &quot;deletion&quot; of the
     * object as well. The provided cleanupCode can be used to implement those
     * semantics, e.g. in order to invoke the die() method.
     *
     * @param key the key of the key-value pair to be removed
     * @param cleanupCode the cleanup code to run, if any
     * @return the value of the key-value pair to be removed, if found
     */
    @SuppressWarnings( "unchecked" )
    public V remove(
            K                 key,
            Invocable<V,Void> cleanupCode )
    {
        V ret = super.remove( key );

        if( cleanupCode != null && ret != null ) {
            cleanupCode.invoke( ret );
        }
        
        if( ret != null ) {
            theListeners.fireEvent( new CachingMapEvent.Removed( this, key ), 1 );
        }
        return ret;
    }

    /**
     * Clear the local cache.
     */
    public void clearLocalCache()
    {
        // do nothing, we have memory only
    }

    /**
     * Determine whether this CachingMap is persistent.
     *
     * @return true if it is persistent
     */
    public boolean isPersistent()
    {
        return false;
    }

    /**
     * Obtain a CursorIterator on the keys of this Map.
     *
     * @return the CursorIterator
     */
    public CursorIterator<K> keysIterator(
            Class<K> keyArrayComponentType,
            Class<V> valueArrayComponentType )
    {
        CursorIterator<K> ret = MapCursorIterator.<K,V>createForKeys( this, keyArrayComponentType, valueArrayComponentType );
        return ret;
    }

    /**
     * Obtain a CursorIterator on the values of this Map.
     *
     * @return the CursorIterator
     */
    public CursorIterator<V> valuesIterator(
            Class<K> keyArrayComponentType,
            Class<V> valueArrayComponentType )
    {
        CursorIterator<V> ret = MapCursorIterator.<K,V>createForValues( this, keyArrayComponentType, valueArrayComponentType );
        return ret;
    }

    /**
     * Obtain the keys for an existing value. This is the opposite operation
     * of {@link #get}. Depending on the implementation of this interface,
     * this operation may take a long time.
     * 
     * @param value the value whose keys need to be found
     * @return an Iterator over the keys
     */
    public Iterator<K> reverseGet(
            V value )
    {
        ArrayList<K> ret = new ArrayList<K>();

        for( K key : keySet() ) {
            V found = get( key );
            if( value == found ) {
                ret.add( key );
            }
        }
        return ret.iterator();
    }

    /**
     * Invoked only by objects held in this CachingMap, this enables
     * the held objects to indicate to the CachingMap that they have been updated.
     * Depending on the implementation of the CachingMap, that may cause the
     * CachingMap to write changes to disk, for example.
     *
     * @param key the key
     * @param value the value
     */
    public void valueUpdated(
            K key,
            V value )
    {
        // no op
    }

    /**
      * Add a listener.
      * This listener is added directly to the listener list, which prevents the
      * listener from being garbage-collected before this Object is being garbage-collected.
      *
      * @param newListener the to-be-added listener
      * @see #addSoftCachingMapListener
      * @see #addWeakCachingMapListener
      * @see #removeCachingMapListener
      */
    public void addDirectCachingMapListener(
            CachingMapListener newListener )
    {
        theListeners.addDirect( newListener );
    }

    /**
      * Add a listener.
      * This listener is added to the listener list using a <code>java.lang.ref.SoftReference</code>,
      * which allows the listener to be garbage-collected before this Object is being garbage-collected
      * according to the semantics of Java references.
      *
      * @param newListener the to-be-added listener
      * @see #addDirectCachingMapListener
      * @see #addWeakCachingMapListener
      * @see #removeCachingMapListener
      */
    public void addSoftCachingMapListener(
            CachingMapListener newListener )
    {
        theListeners.addSoft( newListener );
    }

    /**
      * Add a listener.
      * This listener is added to the listener list using a <code>java.lang.ref.WeakReference</code>,
      * which allows the listener to be garbage-collected before this Object is being garbage-collected
      * according to the semantics of Java references.
      *
      * @param newListener the to-be-added listener
      * @see #addDirectCachingMapListener
      * @see #addSoftCachingMapListener
      * @see #removeCachingMapListener
      */
    public void addWeakCachingMapListener(
            CachingMapListener newListener )
    {
        theListeners.addWeak( newListener );        
    }

    /**
      * Remove a listener.
      * This method is the same regardless how the listener was subscribed to events.
      * 
      * @param oldListener the to-be-removed listener
      * @see #addDirectCachingMapListener
      * @see #addSoftCachingMapListener
      * @see #addWeakCachingMapListener
      */
    public void removeCachingMapListener(
            CachingMapListener oldListener )
    {
        theListeners.remove( oldListener );
    }

    /**
      * The listeners (if any).
      */
    private FlexibleListenerSet<CachingMapListener, CachingMapEvent, Integer> theListeners
            = new FlexibleListenerSet<CachingMapListener,CachingMapEvent,Integer>() {
                    protected void fireEventToListener(
                            CachingMapListener l,
                            CachingMapEvent    e,
                            Integer            p )
                    {
                        switch( p.intValue() ) {
                            case 0:
                                l.mapElementAdded( (CachingMapEvent.Added) e );
                                break;

                            case 1:
                                l.mapElementRemoved( (CachingMapEvent.Removed) e );
                                break;
                                
                            // there is no Expired case, this map never expires anything

                        }
                    }
    };
}

