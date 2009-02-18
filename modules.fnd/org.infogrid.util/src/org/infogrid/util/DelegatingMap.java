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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Map that delegates to other Maps.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class DelegatingMap<K,V>
    implements
        Map<K,V>
{
    /**
     * Factory method for any numebr of delegates.
     *
     * @param delegates the Maps to which this Map delegates
     * @return the created DelegatingMap
     * @param <K> the key type
     * @param <V> the value type
     */
    public static <K,V> DelegatingMap<K,V> create(
            List<Map<K,V>> delegates )
    {
        return new DelegatingMap<K,V>( delegates );
    }

    /**
     * Factory method for any numebr of delegates.
     *
     * @param delegates the Maps to which this Map delegates
     * @return the created DelegatingMap
     * @param <K> the key type
     * @param <V> the value type
     */
    public static <K,V> DelegatingMap<K,V> create(
            Map<K,V> [] delegates )
    {
        ArrayList<Map<K,V>> delegates2 = new ArrayList<Map<K,V>>( delegates.length );
        for( int i=0 ; i<delegates.length ; ++i ) {
            delegates2.add( delegates[i] );
        }
        return new DelegatingMap<K,V>( delegates2 );
    }

    /**
     * Factory method for two delegates.
     *
     * @param del1 the first delegate
     * @param del2 the second delegate
     * @return the created DelegatingMap
     * @param <K> the key type
     * @param <V> the value type
     */
    @SuppressWarnings("unchecked")
    public static <K,V> DelegatingMap<K,V> create(
            Map<K,V> del1,
            Map<K,V> del2 )
    {
        ArrayList<Map<K,V>> delegates2 = new ArrayList<Map<K,V>>( 2 );
        delegates2.add( del1 );
        delegates2.add( del2 );
        return new DelegatingMap<K,V>( delegates2 );
    }

    /**
     * Constructor.
     *
     * @param delegates the Maps to which this Map delegates
     */
    protected DelegatingMap(
            List<Map<K,V>> delegates )
    {
        theDelegates = delegates;
    }

    /**
     * Obtain the delegates.
     *
     * @return the delegates
     */
    public List<Map<K,V>> getDelegates()
    {
        return theDelegates;
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size()
    {
        int ret = 0;
        for( Map<K,V> current : theDelegates ) {
            ret += current.size();
        }
        return ret;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty()
    {
        for( Map<K,V> current : theDelegates ) {
            if( !current.isEmpty() ) {
                return false;
            }
        }
        return true;
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
        for( Map<K,V> current : theDelegates ) {
            if( current.containsKey( key )) {
                return true;
            }
        }
        return false;
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
        for( Map<K,V> current : theDelegates ) {
            if( current.containsValue( value )) {
                return true;
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
     */
    public V get(
            Object key )
    {
        for( Map<K,V> current : theDelegates ) {
            V ret = current.get( key );
            if( ret != null ) {
                return ret;
            }
        }
        return null;
    }

    // Modification Operations

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
     * @throws UnsupportedOperationException if the <tt>put</tt> operation is
     *	          not supported by this map.
     */
    public V put(
            K key,
            V value )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the mapping for this key from this map if it is present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *         not supported by this map.
     */
    public V remove(
            Object key )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     *
     * @param t Mappings to be stored in this map.
     *
     * @throws UnsupportedOperationException if the <tt>putAll</tt> method is
     * 		  not supported by this map.
     */
    public void putAll(
            Map<? extends K, ? extends V> t )
    {
        throw new UnsupportedOperationException();
    }


    /**
     * Removes all mappings from this map (optional operation).
     *
     * @throws UnsupportedOperationException clear is not supported by this
     * 		  map.
     */
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a set view of the keys contained in this map.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set<K> keySet()
    {
        List<Set<K>> newDelegates = new ArrayList<Set<K>>( theDelegates.size() );

        for( Map<K,V> current : theDelegates ) {
            newDelegates.add( current.keySet() );
        }
        return DelegatingSet.create( newDelegates );
    }

    /**
     * Returns a collection view of the values contained in this map.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection<V> values()
    {
        List<Collection<V>> newDelegates = new ArrayList<Collection<V>>( theDelegates.size() );

        for( Map<K,V> current : theDelegates ) {
            newDelegates.add( current.values() );
        }
        return DelegatingCollection.create( newDelegates );
    }

    /**
     * Returns a set view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     */
    public Set<Map.Entry<K,V>> entrySet()
    {
        List<Set<Map.Entry<K,V>>> newDelegates = new ArrayList<Set<Map.Entry<K,V>>>( theDelegates.size() );

        for( Map<K,V> current : theDelegates ) {
            newDelegates.add( current.entrySet() );
        }
        return DelegatingSet.create( newDelegates );
    }

    /**
     * The Maps to which this Map delegates.
     */
    protected List<Map<K,V>> theDelegates;
}
