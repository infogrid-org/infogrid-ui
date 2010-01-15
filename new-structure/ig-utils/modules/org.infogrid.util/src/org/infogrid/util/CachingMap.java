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

import java.util.Iterator;
import java.util.Map;

/**
 * Extends the <code>Map</code> interface to add some methods suitable for <code>Maps</code> that have a local
 * cache.
 *
 * @param <K> the type of key
 * @param <V> the type of value
 */
public interface CachingMap<K,V>
    extends
        Map<K,V>
{
    /**
     * Clear the local cache.
     */
    public abstract void clearLocalCache();

    /**
     * Determine whether this CachingMap is persistent.
     *
     * @return true if it is persistent
     */
    public abstract boolean isPersistent();

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
    public abstract void putIgnorePrevious(
            K key,
            V value );

    /**
     * Obtain a CursorIterator on the keys of this Map.
     *
     * @param keyArrayComponentType the class using which arrays of keys are allocated
     * @param valueArrayComponentType the class using which arrays of values are allocated
     * @return the CursorIterator
     */
    public abstract CursorIterator<K> keysIterator(
            Class<K> keyArrayComponentType,
            Class<V> valueArrayComponentType );

    /**
     * Obtain a CursorIterator on the values of this Map.
     *
     * @param keyArrayComponentType the class using which arrays of keys are allocated
     * @param valueArrayComponentType the class using which arrays of values are allocated
     * @return the CursorIterator
     */
    public abstract CursorIterator<V> valuesIterator(
            Class<K> keyArrayComponentType,
            Class<V> valueArrayComponentType );

    /**
     * Obtain the keys for an existing value. This is the opposite operation
     * of {@link #get}. Depending on the implementation of this interface,
     * this operation may take a long time.
     * 
     * @param value the value whose keys need to be found
     * @return an Iterator over the keys
     */
    public abstract Iterator<K> reverseGet(
            V value );

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
    public abstract V remove(
            K                 key,
            Invocable<V,Void> cleanupCode );

    /**
     * Invoked only by objects held in this CachingMap, this enables
     * the held objects to indicate to the CachingMap that they have been updated.
     * Depending on the implementation of the CachingMap, that may cause the
     * CachingMap to write changes to disk, for example.
     *
     * @param key the key
     * @param value the value
     */
    public abstract void valueUpdated(
            K key,
            V value );

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
            CachingMapListener newListener );

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
            CachingMapListener newListener );

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
            CachingMapListener newListener );

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
            CachingMapListener oldListener );

    /**
     * Default capacity.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 16;
}
