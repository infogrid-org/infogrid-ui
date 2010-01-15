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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A Map that uses an array as implementation. This is slow, but it keeps the names in order.
 * 
 * @param <K> the map key type
 * @param <V> the map value type
 */
public class ArrayMap<K,V>
        implements
            Map<K,V>
{
    /**
     * Constructor with default initial size.
     */
    public ArrayMap()
    {
        theStorage = new ArrayList<Map.Entry<K,V>>();
    }
    
    /**
     * Constructor with specified initial size.
     * 
     * @param size the initial size
     */
    public ArrayMap(
            int size )
    {
        theStorage = new ArrayList<Map.Entry<K,V>>( size );
    }
    
    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size()
    {
        return theStorage.size();
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty()
    {
        return theStorage.isEmpty();
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
        synchronized( theStorage ) {
            if( key == null ) {
                for( Map.Entry<K,V> current : theStorage ) {
                    if( current.getKey() == null ) {
                        return true;
                    }
                }
            } else {
                for( Map.Entry<K,V> current : theStorage ) {
                    if( key.equals( current.getKey() )) {
                        return true;
                    }
                }
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
        synchronized( theStorage ) {
            if( value == null ) {
                for( Map.Entry<K,V> current : theStorage ) {
                    if( current.getValue() == null ) {
                        return true;
                    }
                }
            } else {
                for( Map.Entry<K,V> current : theStorage ) {
                    if( value.equals( current.getValue() )) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.  A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *	       <tt>null</tt> if the map contains no mapping for this key.
     * @see #containsKey(Object)
     */
    public V get(
            Object key )
    {
        synchronized( theStorage ) {
            if( key == null ) {
                for( Map.Entry<K,V> current : theStorage ) {
                    if( current.getKey() == null ) {
                        return current.getValue();
                    }
                }
            } else {
                for( Map.Entry<K,V> current : theStorage ) {
                    if( key.equals( current.getKey() )) {
                        return current.getValue();
                    }
                }
            }
        }
        return null;
        
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
        synchronized( theStorage ) {
            if( key == null ) {
                for( Map.Entry<K,V> current : theStorage ) {
                    if( current.getKey() == null ) {
                        V ret = current.getValue();
                        current.setValue( value );
                        return ret;
                    }
                }
            } else {
                for( Map.Entry<K,V> current : theStorage ) {
                    if( key.equals( current.getKey() )) {
                        V ret = current.getValue();
                        current.setValue( value );
                        return ret;
                    }
                }
            }
        }
        theStorage.add( new MyPair<K,V>( key, value ));
        return null;
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
        synchronized( theStorage ) {
            if( key == null ) {
                int i=0;
                for( Map.Entry<K,V> current : theStorage ) {
                    if( current.getKey() == null ) {
                        theStorage.remove( i );
                        return current.getValue();
                    }
                    ++i;
                }
            } else {
                int i=0;
                for( Map.Entry<K,V> current : theStorage ) {
                    if( key.equals( current.getKey() )) {
                        theStorage.remove( i );
                        return current.getValue();
                    }
                    ++i;
                }
            }
        }
        return null;
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     *
     * @param t Mappings to be stored in this map.
     */
    public void putAll(
            Map<? extends K, ? extends V> t )
    {
        for( K key : t.keySet() ) {
            V value = t.get( key );
            
            put( key, value );
        }
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear()
    {
        theStorage.clear();
    }

    /**
     * Returns a set view of the keys contained in this map.
     *
     * @return a set view of the keys contained in this map.
     * @see #getKeySet
     */
    public synchronized Set<K> keySet()
    {
        if( theKeySet == null ) {
            theKeySet = new MyKeySet();
        }
        return theKeySet;
    }

    /**
     * Make method easier to call from JSP.
     * 
     * @return a set view of the mappings contained in this map.
     * @see #keySet
     */
    public Set<K> getKeySet()
    {
        return keySet();
    }

    /**
     * Returns a collection view of the values contained in this map.
     *
     * @return a collection view of the values contained in this map.
     * @see #getValues
     */
    public synchronized Collection<V> values()
    {
        if( theValueCollection == null ) {
            theValueCollection = new MyValueCollection();
        }
        return theValueCollection;
    }

    /**
     * Make method easier to call from JSP.
     * 
     * @return a set view of the mappings contained in this map.
     * @see #values
     */
    public Collection<V> getValues()
    {
        return values();
    }

    /**
     * Returns a set view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     * @see #getEntrySet
     */
    public synchronized Set<Map.Entry<K,V>> entrySet()
    {
        if( theEntrySet == null ) {
            theEntrySet = new MyEntrySet();
        }
        return theEntrySet;
    }

    /**
     * Make method easier to call from JSP.
     * 
     * @return a set view of the mappings contained in this map.
     * @see #entrySet
     */
    public Set<Map.Entry<K,V>> getEntrySet()
    {
        return entrySet();
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
        if( o instanceof ArrayMap ) {
            @SuppressWarnings("unchecked")
            boolean ret = theStorage.equals( ((ArrayMap<K,V>) o).theStorage );
            return ret;
        }
        return false;
    }
    
    /**
     * Hash code. This is here to make the IDE happy that otherwise indicates a warning.
     * 
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    /**
     * Storage for the ArrayMap.
     */
    protected final ArrayList<Map.Entry<K,V>> theStorage;
    
    /**
     * View into the entries, allocated as needed.
     */
    protected MyEntrySet theEntrySet;

    /**
     * View on the keys, allocated as needed.
     */
    protected MyKeySet theKeySet;
    
    /**
     * View on the values, allocated as needed.
     */
    protected MyValueCollection theValueCollection;
    
    /**
     * Stores a name-value pair.
     * 
     * @param <K> the map key type
     * @param <V> the map value type
     */
    protected static class MyPair<K,V>
            extends
                Pair<K,V>
            implements
                Map.Entry<K,V>
    {
        /**
          * Constructor.
          *
          * @param name the name
          * @param value the value
          */
        public MyPair(
                K name,
                V value )
        {
            super( name, value );
        }
        
        /**
         * Rename accessor method.
         * 
         * @return the key
         */
        public K getKey()
        {
            return super.getName();
        }

        /**
         * Set a new value for this key.
         * 
         * @param value the new value
         * @return the old value, if any
         */
        public V setValue(
                V value )
        {
            V ret = theValue;
            theValue = value;
            return ret;
        }
    }
    
    /**
     * View into the set of entries.
     */
    protected class MyEntrySet
            extends
                AbstractSet<Map.Entry<K,V>>
    {
        /**
         * Returns an iterator over the elements contained in this collection.
         *
         * @return an iterator over the elements contained in this collection.
         */
        public Iterator<Map.Entry<K,V>> iterator()
        {
            Iterator<Map.Entry<K,V>> ret = theStorage.iterator();
            return ret;
        }

        /**
         * Determine size.
         * 
         * @return size
         */
        public int size()
        {
            return ArrayMap.this.size();
        }
    }
    
    /**
     * View into the set of keys.
     */
    protected class MyKeySet
            extends
                AbstractSet<K>
    {
        /**
         * Returns an iterator over the elements contained in this collection.
         *
         * @return an iterator over the elements contained in this collection.
         */
        public Iterator<K> iterator()
        {
            return new TranslatingIterator<K,Map.Entry<K,V>>( theStorage.iterator() ) {
                protected K translate(
                        Entry<K,V> org )
                {
                    return org.getKey();
                }
            };
        }

        /**
         * Determine size.
         * 
         * @return size
         */
        public int size()
        {
            return ArrayMap.this.size();
        }
    }
    
    /**
     * View into the collection of values.
     */
    protected class MyValueCollection
            extends
                AbstractCollection<V>
    {
        /**
         * Returns an iterator over the elements contained in this collection.
         *
         * @return an iterator over the elements contained in this collection.
         */
        public Iterator<V> iterator()
        {
            return new TranslatingIterator<V,Map.Entry<K,V>>( theStorage.iterator() ) {
                protected V translate(
                        Entry<K,V> org )
                {
                    return org.getValue();
                }
            };
        }
        
        /**
         * Determine size.
         * 
         * @return size
         */
        public int size()
        {
            return ArrayMap.this.size();
        }
    }
}
