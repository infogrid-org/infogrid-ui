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

import java.lang.ref.Reference;

/**
 * A {@link SwappingHashMap} that stores data in memory only.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 * @param <A> the type of argument
 */
public abstract class MSwappingHashMap<K,V,A>
        extends
            SwappingHashMap<K,V>
{
    /**
     * Create MSwappingHashMap that uses SoftReferences.
     * 
     * @return the created MSwappingHashMap
     * @param <K> the type of key
     * @param <V> the type of value
     * @param <A> the type of argument
     */
    public static <K,V,A> MSwappingHashMap<K, V, A> createSoft()
    {
        return createSoft( DEFAULT_INITIAL_CAPACITY );
    }
    
    /**
     * Create MSwappingHashMap that uses SoftReferences.
     * 
     * @param initialSize the initial size of the MSwappingHashMap
     * @return the created MSwappingHashMap
     * @param <K> the type of key
     * @param <V> the type of value
     * @param <A> the type of argument
     */
    public static <K,V,A> MSwappingHashMap<K, V, A> createSoft(
            int initialSize )
    {
        return new MSwappingHashMap<K,V,A>( initialSize ) {
                protected Reference<V> createReference(
                        K key,
                        V value )
                {
                    return new SoftEntryReference<K,V>( key, value, theQueue );
                }
        };
    }
    
    /**
     * Create MSwappingHashMap that uses WeakReferences.
     * 
     * @return the created MSwappingHashMap
     * @param <K> the type of key
     * @param <V> the type of value
     * @param <A> the type of argument
     */
    public static <K,V,A> MSwappingHashMap<K, V, A> createWeak()
    {
        return createWeak( DEFAULT_INITIAL_CAPACITY );
    }
    
    /**
     * Create MSwappingHashMap that uses WeakReferences.
     * 
     * @param initialSize the initial size of the MSwappingHashMap
     * @return the created MSwappingHashMap
     * @param <K> the type of key
     * @param <V> the type of value
     * @param <A> the type of argument
     */
    public static <K,V,A> MSwappingHashMap<K, V, A> createWeak(
            int initialSize )
    {
        return new MSwappingHashMap<K,V,A>( initialSize ) {
                protected Reference<V> createReference(
                        K key,
                        V value )
                {
                    return new WeakEntryReference<K,V>( key, value, theQueue );
                }
        };
    }
    
    /**
     * Constructor.
     * 
     * @param initialSize the initial size of the MSwappingHashMap
     */
    protected MSwappingHashMap(
            int initialSize )
    {
        super( initialSize );
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
        CursorIterator<K> ret = MapCursorIterator.<K,V>createForKeys( this, keyArrayComponentType, valueArrayComponentType );
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
        CursorIterator<V> ret = MapCursorIterator.<K,V>createForValues( this, keyArrayComponentType, valueArrayComponentType );
        return ret;
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
}
