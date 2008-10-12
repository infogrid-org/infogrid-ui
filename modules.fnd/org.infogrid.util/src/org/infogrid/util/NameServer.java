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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents a NameServer, i.e. an object that can return objects based on a key.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 * @see org.infogrid.util.WritableNameServer
 */
public interface NameServer<K,V>
{
    /**
     * Obtain an already existing value for a provided key.
     * 
     * @param key the key for which we want to obtain a value
     * @return the already existing value, or null
     */
    public abstract V get(
            K key );

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
     * Obtain a set of the currently known keys. This follows the pattern in
     * java.util.HashMap.
     *
     * @return a set of the currently known keys
     */
    public abstract Set<K> keySet();

    /**
     * Obtain a Collection of the currently known values. This follows the pattern in
     * java.util.HashMap.
     *
     * @return a set of the currently known values
     */
    public abstract Collection<V> values();

    /**
     * Determine whether the number of key-value pairs in this SmartFactory is zero.
     *
     * @return true if there are no key-value paris in this SmartFactory
     */
    public boolean isEmpty();

    /**
     * The number of known key-value pairs in this SmartFactory at this time.
     *
     * @return the number of known key-value pairs
     */
    public abstract int size();
}
