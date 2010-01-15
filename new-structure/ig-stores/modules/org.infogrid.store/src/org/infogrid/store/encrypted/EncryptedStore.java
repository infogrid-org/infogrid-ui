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

package org.infogrid.store.encrypted;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.infogrid.store.AbstractStore;
import org.infogrid.store.Store;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreKeyExistsAlreadyException;
import org.infogrid.store.StoreValue;
import org.infogrid.util.logging.Log;

/**
 * <p>A <code>Store</code> that encrypts its data before delegating to an
 *    underlying <code>Store</code>.</p>
 * <p>Note that the <code>encodingId</code> remains the same in this implementation; subclasses
 *    could change this.</p>
 */
public class EncryptedStore
        extends
            AbstractStore
{
    private static final Log log = Log.getLogInstance( EncryptedStore.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param transformation the name of the transformation, e.g., DES/CBC/PKCS5Padding, see <code>javax.crypto.Cipher.getInstance(String)</code>
     * @param key the secret key to be used for encryption and decryption
     * @param delegate the Store that does the actual storing
     * @return the created EncryptedStore
     * @throws InvalidKeyException thrown if the key is invalid or does not match the specified transformation
     * @throws NoSuchAlgorithmException thrown if the specified transformation is not available in the default provider package or any of the other provider packages that were searched. 
     * @throws NoSuchPaddingException thrown if transformation contains a padding scheme that is not available.
     */
    public static EncryptedStore create(
            String    transformation,
            SecretKey key,
            Store     delegate )
        throws
            InvalidKeyException,
            NoSuchPaddingException,
            NoSuchAlgorithmException
    {
        Cipher encCipher = Cipher.getInstance( transformation );
        Cipher decCipher = Cipher.getInstance( transformation );
        
        encCipher.init( Cipher.ENCRYPT_MODE, key );
        decCipher.init( Cipher.DECRYPT_MODE, key );

        return new EncryptedStore( encCipher, decCipher, delegate );
    }

    /**
     * Constructor.
     *
     * @param encCipher the Cipher to use for encryption
     * @param decCipher the Cipher to use for decryption
     * @param delegate the Store that does the actual storing
     */
    protected EncryptedStore(
            Cipher    encCipher,
            Cipher    decCipher,
            Store     delegate )
    {
        theEncCipher   = encCipher;
        theDecCipher   = decCipher;
        theDelegate    = delegate;
    }
    
    /**
     * Initialize the Store. If the Store was initialized earlier, this will delete all
     * contained information. This operation is similar to unconditionally formatting a hard drive.
     * 
     * @throws IOException thrown if an I/O error occurred
     */
    public void initializeHard()
            throws
                IOException
    {
        throw new UnsupportedOperationException( "Cannot initialize EncryptedStore; initialize underlying Store instead." );
    }
    
    /**
     * Initialize the Store if needed. If the Store was initialized earlier, this will do
     * nothing. This operation is equivalent to {@link #initializeHard} if and only if
     * the Store had not been initialized earlier.
     * 
     * @throws IOException thrown if an I/O error occurred
     */
    public void initializeIfNecessary()
            throws
                IOException
    {
        throw new UnsupportedOperationException( "Cannot initialize EncryptedStore; initialize underlying Store instead." );
    }

    /**
     * Put a data element into the Store for the first time.
     *
     * @param key the key under which the data element will be stored
     * @param encodingId the id of the encoding that was used to encode the data element. This must be 64 bytes or less.
     * @param timeCreated the time at which the data element was created originally
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @throws StoreKeyExistsAlreadyException thrown if a data element is already stored in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #update if a data element with this key exists already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    @Override
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
        try {
            byte [] encryptedData       = encrypt( data );
            String  encryptedEncodingId = constructEncodingId( encodingId );
        
            theDelegate.put( key, encryptedEncodingId, timeCreated, timeUpdated, timeRead, timeExpires, encryptedData );
        
        } finally {
            StoreValue value = new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );

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
        put(    toStore.getKey(),
                toStore.getEncodingId(),
                toStore.getTimeCreated(),
                toStore.getTimeUpdated(),
                toStore.getTimeRead(),
                toStore.getTimeExpires(),
                toStore.getData() );
    }
    
    /**
     * Update a data element that already exists in the Store, by overwriting it with a new value.
     *
     * @param key the key under which the data element is already, and will continue to be stored
     * @param encodingId the id of the encoding that was used to encode the data element
     * @param timeCreated the time at which the data element was created originally
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @throws StoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    @Override
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
        try {
            byte [] encryptedData       = encrypt( data );
            String  encryptedEncodingId = constructEncodingId( encodingId );

            theDelegate.update( key, encryptedEncodingId, timeCreated, timeUpdated, timeRead, timeExpires, encryptedData );
            
        } finally {
            StoreValue value = new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );

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
        update( toUpdate.getKey(),
                toUpdate.getEncodingId(),
                toUpdate.getTimeCreated(),
                toUpdate.getTimeUpdated(),
                toUpdate.getTimeRead(),
                toUpdate.getTimeExpires(),
                toUpdate.getData() );
    }
    
    /**
     * Put (if does not exist already) or update (if it does exist) a data element in the Store.
     *
     * @param key the key under which the data element may already, and will continue to be stored
     * @param encodingId the id of the encoding that was used to encode the data element. This must be 64 bytes or less.
     * @param timeCreated the time at which the data element was created originally
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element in the inStream was last read by some client
     * @param timeExpires the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @return true if the value was updated, false if it was put
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #update if a data element with this key exists already
     */
    @Override
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
        boolean ret = false;
        try {
            byte [] encryptedData       = encrypt( data );
            String  encryptedEncodingId = constructEncodingId( encodingId );

            ret = theDelegate.putOrUpdate( key, encryptedEncodingId, timeCreated, timeUpdated, timeRead, timeExpires, encryptedData );
        
        } finally {
            StoreValue value = new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );

            if( ret ) {
                fireUpdatePerformed( value );
            } else {
                firePutPerformed( value );
            }
        }
        return ret;
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
        return putOrUpdate(
                toStoreOrUpdate.getKey(),
                toStoreOrUpdate.getEncodingId(),
                toStoreOrUpdate.getTimeCreated(),
                toStoreOrUpdate.getTimeUpdated(),
                toStoreOrUpdate.getTimeRead(),
                toStoreOrUpdate.getTimeExpires(),
                toStoreOrUpdate.getData() );
    }

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
            IOException
    {
        StoreValue ret = null;
        try {
            ret = decryptStoreValue( theDelegate.get( key ));
            return ret;

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
     * @throws StoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     */
    public void delete(
            String key )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        try {
            theDelegate.delete( key );

        } finally {
            fireDeletePerformed( key );
        }
    }

    /**
     * Remove all data elements in this Store.
     *
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public void deleteAll()
        throws
            IOException
    {
        theDelegate.deleteAll();
    }

    /**
     * Remove all data in this Store whose keys start with this string.
     *
     * @param startsWith the String the key starts with
     * @throws IOException thrown if an I/O error occurred
     */
    public void deleteAll(
            String startsWith )
        throws
            IOException
    {
        theDelegate.deleteAll( startsWith );
    }
    
    /**
     * Encrypt a piece of data.
     *
     * @param clear the cleartext
     * @return encrypted data
     */
    protected byte [] encrypt(
            byte [] clear )
    {
        try {
            byte [] ret = theEncCipher.doFinal( clear );
            return ret;

        } catch( Throwable t ) {
            log.error( t );

             // there isn't really anything else we can do
            if( t instanceof RuntimeException ) {
                throw ((RuntimeException) t);
            } else {
                throw new RuntimeException( t );
            }
        }
    }

    /**
     * Decrypt a piece of data.
     *
     * @param encrypted the encrypted data
     * @return decrypted data
     */
    protected byte [] decrypt(
            byte [] encrypted )
    {
        try {
            byte [] ret = theDecCipher.doFinal( encrypted );
            return ret;

        } catch( Throwable t ) {
            log.error( t );

             // there isn't really anything else we can do
            if( t instanceof RuntimeException ) {
                throw ((RuntimeException) t);
            } else {
                throw new RuntimeException( t );
            }
        }
    }

    /**
     * Helper method to encrypt / translate a StoreValue.
     *
     * @param decryptedStoreValue the decrypted StoreValue
     * @return the encrypted StoreValue
     */
    protected StoreValue encryptStoreValue(
            StoreValue decryptedStoreValue )
    {
        String  encryptedEncodingId = constructEncodingId( decryptedStoreValue.getEncodingId() );
        byte [] encryptedData       = encrypt( decryptedStoreValue.getData() );

        StoreValue ret = new StoreValue(
                decryptedStoreValue.getKey(),
                encryptedEncodingId,
                decryptedStoreValue.getTimeCreated(),
                decryptedStoreValue.getTimeUpdated(),
                decryptedStoreValue.getTimeRead(),
                decryptedStoreValue.getTimeExpires(),
                encryptedData );
        return ret;
    }
    
    /**
     * Helper method to decrypt / translate a StoreValue.
     *
     * @param encryptedStoreValue the encrypted StoreValue
     * @return the decrypted StoreValue
     */
    protected StoreValue decryptStoreValue(
            StoreValue encryptedStoreValue )
    {
        String  decryptedEncodingId = reconstructEncodingId( encryptedStoreValue.getEncodingId() );
        byte [] decryptedData       = decrypt( encryptedStoreValue.getData() );

        StoreValue ret = new StoreValue(
                encryptedStoreValue.getKey(),
                decryptedEncodingId,
                encryptedStoreValue.getTimeCreated(),
                encryptedStoreValue.getTimeUpdated(),
                encryptedStoreValue.getTimeRead(),
                encryptedStoreValue.getTimeExpires(),
                decryptedData );
        return ret;
    }
    
    /**
     * Construct the encodingId to be used for the delegate. This can be
     * overridden in subclasses.
     *
     * @param original the original encodingId
     * @return the encodingId for the delegate
     */
    protected String constructEncodingId(
            String original )
    {
        return original;
    }

    /**
     * Reconstruct the encodingId from the one used by the delegate. This can be
     * overridden in subclasses.
     *
     * @param change the encodingId used by the delegate
     * @return the original encodingId
     */
    protected String reconstructEncodingId(
            String change )
    {
        return change;
    }

    /**
     * The Cipher to use for encryption.
     */
    protected Cipher theEncCipher;
    
    /**
     * The Cipher to use for decryption.
     */
    protected Cipher theDecCipher;
    
    /**
     * The underlying Store.
     */
    protected Store theDelegate;
}
