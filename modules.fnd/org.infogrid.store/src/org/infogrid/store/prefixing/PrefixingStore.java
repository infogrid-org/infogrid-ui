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

package org.infogrid.store.prefixing;

import org.infogrid.store.AbstractStore;
import org.infogrid.store.Store;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreKeyExistsAlreadyException;
import org.infogrid.store.StoreValue;

import org.infogrid.util.StringHelper;

import java.io.IOException;

/**
 * This {@link Store} delegates to another <code>Store</code>, but prefixes all keys with
 * a constant prefix. This is useful to build a hierarchical namespace of <code>Store</code>
 * keys.
 */
public class PrefixingStore
        extends
            AbstractStore
{
    /**
     * Factory method.
     *
     * @param prefix the prefix for all keys
     * @param delegate the Store that this PrefixingStore delegates to
     * @return the created PrefixingStore
     */
    public static PrefixingStore create(
            String prefix,
            Store  delegate )
    {
        return new PrefixingStore( prefix + DEFAULT_SEPARATOR, delegate );
    }

    /**
     * Constructor.
     *
     * @param prefixAndSeparator the prefixAndSeparator for all keys
     * @param delegate the Store that this PrefixingStore delegates to
     */
    protected PrefixingStore(
            String prefixAndSeparator,
            Store  delegate )
    {
        thePrefixAndSeparator = prefixAndSeparator;
        theDelegate           = delegate;
    }

    /**
     * Put a data element into the Store for the first time. Throw an Exception if a data
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
            IOException
    {
        String delegatedKey = constructDelegatedKey( key );
        
        StoreValue value = new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );
        try {
            theDelegate.put( delegatedKey, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );

        } catch( StoreKeyExistsAlreadyException ex ) {
            throw new PrefixingStoreKeyExistsAlreadyException( this, key, ex );

        } finally {
            firePutPerformed( value );
        }
    }

    /**
     * Put a data element into the Store for the first time. Throw an Exception if a data
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
            IOException
    {
        String delegatedKey = constructDelegatedKey( toStore.getKey() );
        
        try {
            theDelegate.put(
                    delegatedKey,
                    toStore.getEncodingId(),
                    toStore.getTimeCreated(),
                    toStore.getTimeUpdated(),
                    toStore.getTimeRead(),
                    toStore.getTimeExpires(),
                    toStore.getData() );

        } catch( StoreKeyExistsAlreadyException ex ) {
            throw new PrefixingStoreKeyExistsAlreadyException( this, toStore.getKey(), ex );

        } finally {
            firePutPerformed( toStore );
        }
    }

    /**
     * Update a data element that already exists in the Store, by overwriting it with a new value. Throw an
     * Exception if a data element with this key does not exist already.
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
            IOException
    {
        String delegatedKey = constructDelegatedKey( key );
        
        StoreValue value = new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );

        try {
            theDelegate.update( delegatedKey, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );

        } catch( StoreKeyDoesNotExistException ex ) {
            throw new PrefixingStoreKeyDoesNotExistException( this, key, ex );

        } finally {
            fireUpdatePerformed( value );
        }
    }

    /**
     * Update a data element that already exists in the Store, by overwriting it with a new value. Throw an
     * Exception if a data element with this key does not exist already.
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
            IOException
    {
        String delegatedKey = constructDelegatedKey( toUpdate.getKey() );
        
        try {
            theDelegate.update(
                    delegatedKey,
                    toUpdate.getEncodingId(),
                    toUpdate.getTimeCreated(),
                    toUpdate.getTimeUpdated(),
                    toUpdate.getTimeRead(),
                    toUpdate.getTimeExpires(),
                    toUpdate.getData() );

        } catch( StoreKeyDoesNotExistException ex ) {
            throw new StoreKeyDoesNotExistException( this, toUpdate.getKey(), ex );

        } finally {
            fireUpdatePerformed( toUpdate );
        }
    }

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
            IOException
    {
        String delegatedKey = constructDelegatedKey( key );
        
        boolean updated = theDelegate.putOrUpdate( delegatedKey, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );

        StoreValue value = new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );
        
        if( !updated ) {
            firePutPerformed( value );
        } else {
            fireUpdatePerformed( value );
        }
        
        return updated;
    }

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
            IOException
    {
        String delegatedKey = constructDelegatedKey( toStoreOrUpdate.getKey() );
        
        boolean updated = theDelegate.putOrUpdate(
                    delegatedKey,
                    toStoreOrUpdate.getEncodingId(),
                    toStoreOrUpdate.getTimeCreated(),
                    toStoreOrUpdate.getTimeUpdated(),
                    toStoreOrUpdate.getTimeRead(),
                    toStoreOrUpdate.getTimeExpires(),
                    toStoreOrUpdate.getData() );

        if( !updated ) {
            firePutPerformed( toStoreOrUpdate );
        } else {
            fireUpdatePerformed( toStoreOrUpdate );
        }
        
        return updated;
    }


    /**
     * Obtain a data element and associated meta-data from the Store, given a key.
     *
     * @param key the key to the data element in the Store
     * @return the StoreValue stored in the Store for this key; this encapsulates data element and meta-data
     * @throws PrefixingStoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put to initially store a data element
     */
    public StoreValue get(
            String key )
        throws
            PrefixingStoreKeyDoesNotExistException,
            IOException
    {
        String delegatedKey = constructDelegatedKey( key );
        
        StoreValue ret = null;
        try {
            StoreValue delegateValue = theDelegate.get( delegatedKey );

            ret = translateDelegateStoreValue( delegateValue );

            return ret;

        } catch( StoreKeyDoesNotExistException ex ) {
            throw new PrefixingStoreKeyDoesNotExistException( this, key, ex );

        } finally {
            if( ret != null ) {
                fireGetPerformed( ret );
            } else {
                fireGetFailed( key );
            }
        }
    }

    /**
     * Delete the data element that is stored using this key.
     *
     * @param key the key to the data element in the Store
     * @throws PrefixingStoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     */
    public void delete(
            String key )
        throws
            PrefixingStoreKeyDoesNotExistException,
            IOException
    {
        String delegatedKey = constructDelegatedKey( key );
        
        try {
            theDelegate.delete( delegatedKey );

        } catch( StoreKeyDoesNotExistException ex ) {
            throw new PrefixingStoreKeyDoesNotExistException( this, key, ex );

        } finally {
            fireDeletePerformed( key );
        }
    }

    /**
     * Remove all data elements in this Store.
     *
     * @throws IOException thrown if an I/O error occurred
     */
    public void deleteAll()
        throws
            IOException
    {
        theDelegate.deleteAll( thePrefixAndSeparator );
    }

    /**
     * Remove all data in this Store whose keys start with this string.
     *
     * @param prefix the String the key starts with
     * @throws IOException thrown if an I/O error occurred
     */
    public void deleteAll(
            String prefix )
        throws
            IOException
    {
        theDelegate.deleteAll( thePrefixAndSeparator + prefix );
    }

    /**
     * Convert a delegate StoreValue into a StoreValue here.
     *
     * @param delegateValue the delegate StoreValue
     * @return the local StoreValue
     */
    protected StoreValue translateDelegateStoreValue(
            StoreValue delegateValue )
    {
        String newKey = delegateValue.getKey().substring( thePrefixAndSeparator.length() );
        StoreValue ret = new StoreValue(
                newKey,
                delegateValue.getEncodingId(),
                delegateValue.getTimeCreated(),
                delegateValue.getTimeUpdated(),
                delegateValue.getTimeRead(),
                delegateValue.getTimeExpires(),
                delegateValue.getData() );
        
        return ret;
    }

    /**
     * Convert a StoreValue into a delegate StoreValue here.
     *
     * @param value the StoreValue
     * @return the delegate StoreValue
     */
    protected StoreValue translateToDelegateStoreValue(
            StoreValue value )
    {
        String newKey = constructDelegatedKey( value.getKey() );
        StoreValue ret = new StoreValue(
                newKey,
                value.getEncodingId(),
                value.getTimeCreated(),
                value.getTimeUpdated(),
                value.getTimeRead(),
                value.getTimeExpires(),
                value.getData() );
        
        return ret;
    }

    /**
     * Construct the delegate key, given this key and instance data.
     *
     * @param key in key
     * @return the delegate key
     */
    protected String constructDelegatedKey(
            String key )
    {
        StringBuilder ret = new StringBuilder();
        ret.append( thePrefixAndSeparator );
        ret.append( key );
        return ret.toString();
    }

    /**
     * Determine whether this delegate key matches one of the local keys.
     *
     * @param delegateKey the delegate key
     * @return true if it matches
     */
    protected boolean matches(
            String delegateKey )
    {
        return delegateKey.startsWith( thePrefixAndSeparator );
    }

    /**
     * Convert to String representation, for debugging.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                theDelegate,
                new String[] {
                    "prefixAndSeparator"
                },
                new Object[] {
                    thePrefixAndSeparator
                });
    }

    /**
     * The prefix and separator.
     */
    protected String thePrefixAndSeparator;

    /**
     * The delegate Store.
     */
    protected Store theDelegate;

    /**
     * The default separator between the prefix and the key.
     */
    public static final String DEFAULT_SEPARATOR = " ";
}
