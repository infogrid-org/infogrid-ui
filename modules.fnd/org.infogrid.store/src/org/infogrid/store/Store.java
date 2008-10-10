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

import java.io.IOException;

/**
 * <p>A <code>Store</code> is an abstraction for a data store that behaves like a persistent
 * Java <code>java.util.Map</code> keyed with String, and with byte arrays as values.</p>
 * 
 * <p>Classes implementing this Store interface may store information in a variety of
 * different ways, including in file systems, in databases, or even on P2P networks.
 * Application developers can usually program directly against this interface, without
 * having to be aware aware of the specific class implementing it.</p>
 * 
 * <p>When storing or retrieving data element, various meta-data values are being
 * carried around:</p>
 * <dl>
 *  <dt><code>EncodingId</code></dt>
 *  <dd>An application-defined value that represents the encoder used for storing the
 *      data. This is roughly comparable to the idea of a file system descriptor
 *      in a UNIX-like operating system, but it applies on a data element-level.</dd>
 *  <dt><code>TimeCreated</code></dt>
 *  <dd>Time the data element was created, as seen by the application. This value may
 *      be different from the time at which this data element was first inserted into
 *      the <code>Store</code>.</dd>
 *  <dt><code>TimeUpdated</code></dt>
 *  <dd>Time the data element was last updated, as seen by the application. This value may
 *      be different from the time at which this data element was lasted updated in
 *      the <code>Store</code>.</dd>
 *  <dt><code>TimeRead</code></dt>
 *  <dd>Time the data element was last read, as seen by the application. This value may
 *      be different from the time at which this data element was last retrieved from
 *      the <code>Store</code>.</dt>
 *  <dt><code>TimeInvalid</code></dt>
 *  <dd>If positive, this represents the time at which this data element will become invalid.
 *      The definition of invalid is up to the application; an application may choose
 *      to treat invalid StoreValues as if they didn't exist (which is facilitated by
 *      the property <code>ignoreInvalid</code> settable by some <code>Store</code>
 *      implementations) or any other strategy that makes sense to the application. This
 *      is a core meta-data element in order to enable administration from outside of
 *      the application (e.g. by a database administrator who can delete expired rows from
 *      a SQL implementation of <code>Store</code> outside of the application.</dd>
 * </dl>
 *
 * <p><code>StoreListeners</code> may be used to listen to Store operations, e.g. for
 * logging purposes.</p>
 * <p>This interface uses the format defined by <code>System.currentTimeMillis()</code>
 * to encode all time values.</p>
 */
public interface Store
{
    /**
     * Initialize the Store. If the Store was initialized earlier, this will delete all
     * contained information. This operation is similar to unconditionally formatting a hard drive.
     * 
     * @throws IOException thrown if an I/O error occurred
     */
    public void initializeHard()
            throws
                IOException;
    
    /**
     * Initialize the Store if needed. If the Store was initialized earlier, this will do
     * nothing. This operation is equivalent to {@see #initializeHard} if and only if
     * the Store had not been initialized earlier.
     * 
     * @throws IOException thrown if an I/O error occurred
     */
    public void initializeIfNecessary()
            throws
                IOException;
    
    /**
     * Put a data element into the Store for the first time. Throw a
     * {@link StoreKeyExistsAlreadyException} if a data
     * element has already been store using the same key.
     *
     * @param key the key with which the specified data element is to be associated
     * @param encodingId the id of the encoding that was used to encode the data element.
     * @param timeCreated the time at which the data element was created
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires if positive, the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @throws StoreKeyExistsAlreadyException thrown if a data element is already stored in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #update if a data element with this key exists already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    public void put(
            String  key,
            String  encodingId,
            long    timeCreated,
            long    timeUpdated,
            long    timeRead,
            long    timeExpires,
            byte [] data )
        throws
            StoreKeyExistsAlreadyException,
            IOException;

    /**
     * Put a data element into the Store for the first time. Throw a
     * {@link StoreKeyExistsAlreadyException} if a data
     * element has already been store using the same key.
     *
     * @param toStore the StoreValue to store
     * @throws StoreKeyExistsAlreadyException thrown if a data element is already stored in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #update if a data element with this key exists already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    public void put(
            StoreValue toStore )
        throws
            StoreKeyExistsAlreadyException,
            IOException;

    /**
     * Update a data element that already exists in the Store, by overwriting it with a new value. Throw a
     * {@link StoreKeyDoesNotExistException} if a data element with this key does not exist already.
     *
     * @param key the key with which the specified data element is already, and will continue to be stored
     * @param encodingId the id of the encoding that was used to encode the data element.
     * @param timeCreated the time at which the data element was created
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires if positive, the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @throws StoreKeyDoesNotExistException thrown if no data element exists in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    public void update(
            String  key,
            String  encodingId,
            long    timeCreated,
            long    timeUpdated,
            long    timeRead,
            long    timeExpires,
            byte [] data )
        throws
            StoreKeyDoesNotExistException,
            IOException;

    /**
     * Update a data element that already exists in the Store, by overwriting it with a new value. Throw a
     * {@link StoreKeyDoesNotExistException} if a data element with this key does not exist already.
     *
     * @param toUpdate the StoreValue to update
     * @throws StoreKeyDoesNotExistException thrown if no data element exists in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    public void update(
            StoreValue toUpdate )
        throws
            StoreKeyDoesNotExistException,
            IOException;

    /**
     * Put (if does not exist already) or update (if it does exist) a data element in the Store.
     *
     * @param key the key with which the specified data element is already, and will continue to be stored
     * @param encodingId the id of the encoding that was used to encode the data element.
     * @param timeCreated the time at which the data element was created
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires if positive, the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @return true if the data element was updated, false if it was put
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #update if a data element with this key exists already
     */
    public boolean putOrUpdate(
            String  key,
            String  encodingId,
            long    timeCreated,
            long    timeUpdated,
            long    timeRead,
            long    timeExpires,
            byte [] data )
        throws
            IOException;

    /**
     * Put (if does not exist already) or update (if it does exist) a data element in the Store.
     *
     * @param toStoreOrUpdate the StoreValue to store or update
     * @return true if the value was updated, false if it was put
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #update if a data element with this key exists already
     */
    public boolean putOrUpdate(
            StoreValue toStoreOrUpdate )
        throws
            IOException;

    /**
     * Obtain a data element and associated meta-data from the Store, given a key.
     *
     * @param key the key to the data element in the Store
     * @return the StoreValue stored in the Store for this key; this encapsulates data element and meta-data
     * @throws StoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put to initially store a data element
     */
    public StoreValue get(
            String key )
        throws
            StoreKeyDoesNotExistException,
            IOException;

    /**
     * Delete the data element that is stored using this key.
     *
     * @param key the key to the data element in the Store
     * @throws StoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     */
    public void delete(
            String key )
        throws
            StoreKeyDoesNotExistException,
            IOException;

    /**
     * Remove all data elements in this Store.
     *
     * @throws IOException thrown if an I/O error occurred
     */
    public void deleteAll()
        throws
            IOException;

    /**
     * Remove all data in this Store whose keys start with this string.
     *
     * @param startsWith the String the key starts with
     * @throws IOException thrown if an I/O error occurred
     */
    public void deleteAll(
            String startsWith )
        throws
            IOException;

    /**
      * Add a listener.
      * This listener is added directly to the listener list, which prevents the
      * listener from being garbage-collected before this Object is being garbage-collected.
      *
      * @param newListener the to-be-added listener
      * @see #addSoftStoreListener
      * @see #addWeakStoreListener
      * @see #removeStoreListener
      */
    public void addDirectStoreListener(
            StoreListener newListener );

    /**
      * Add a listener.
      * This listener is added to the listener list using a <code>java.lang.ref.SoftReference</code>,
      * which allows the listener to be garbage-collected before this Object is being garbage-collected
      * according to the semantics of Java references.
      *
      * @param newListener the to-be-added listener
      * @see #addDirectStoreListener
      * @see #addWeakStoreListener
      * @see #removeStoreListener
      */
    public void addSoftStoreListener(
            StoreListener newListener );

    /**
      * Add a listener.
      * This listener is added to the listener list using a <code>java.lang.ref.WeakReference</code>,
      * which allows the listener to be garbage-collected before this Object is being garbage-collected
      * according to the semantics of Java references.
      *
      * @param newListener the to-be-added listener
      * @see #addDirectStoreListener
      * @see #addSoftStoreListener
      * @see #removeStoreListener
      */
    public void addWeakStoreListener(
            StoreListener newListener );

    /**
      * Remove a listener.
      * This method is the same regardless how the listener was subscribed to events.
      * 
      * @param oldListener the to-be-removed listener
      * @see #addDirectStoreListener
      * @see #addSoftStoreListener
      * @see #addWeakStoreListener
      */
    public void removeStoreListener(
            StoreListener oldListener );
}
