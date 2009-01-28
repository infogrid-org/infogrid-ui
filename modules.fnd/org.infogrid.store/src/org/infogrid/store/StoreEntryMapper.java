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

package org.infogrid.store;

import java.net.URISyntaxException;

/**
 * Classes implementing this interface know how to map key-value pairs, of a
 * parameterized type, into and from a <code>Store</code>.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public interface StoreEntryMapper<K,V>
{
    /**
     * Map a key to a String value that can be used for the Store.
     *
     * @param key the key object
     * @return the corresponding String value that can be used for the Store
     */
    public abstract String keyToString(
            K key );

    /**
     * Map to a key a String value that can be used for the Store.
     *
     * @param stringKey key the key in String form
     * @return the corresponding key
     * @throws URISyntaxException thrown if a stringKey could not be converted into a valid Identifier
     */
    public abstract K stringToKey(
            String stringKey )
        throws
            URISyntaxException;

    /**
     * Map a StoreValue to a value.
     *
     * @param key the key to the StoreValue
     * @param value the StoreValue
     * @return the value
     * @throws StoreValueDecodingException thrown if the StoreValue could not been decoded
     */
    public abstract V decodeValue(
            K          key,
            StoreValue value )
        throws
            StoreValueDecodingException;
    
    /**
     * Obtain the preferred encoding id of this StoreEntryMapper.
     * 
     * @return the preferred encoding id
     */
    public abstract String getPreferredEncodingId();

    /**
     * Obtain the time a value was created.
     *
     * @param value the time a value was created.
     * @return the time created, in System.currentTimeMillis() format
     */
    public abstract long getTimeCreated(
            V value );

    /**
     * Obtain the time a value was last updated.
     *
     * @param value the time a value was last updated.
     * @return the time updated, in System.currentTimeMillis() format
     */
    public abstract long getTimeUpdated(
            V value );

    /**
     * Obtain the time a value was last read.
     *
     * @param value the time a value was last read.
     * @return the time read, in System.currentTimeMillis() format
     */
    public abstract long getTimeRead(
            V value );

    /**
     * Obtain the time a value will expire.
     *
     * @param value the time a value will expire.
     * @return the time will expire, in System.currentTimeMillis() format
     */
    public abstract long getTimeExpires(
            V value );

    /**
     * Obtain the value as a byte array.
     *
     * @param value the value
     * @return the byte array
     * @throws StoreValueEncodingException thrown if the value could not been encoded
     */
    public abstract byte [] asBytes(
            V value )
        throws
            StoreValueEncodingException;
}
