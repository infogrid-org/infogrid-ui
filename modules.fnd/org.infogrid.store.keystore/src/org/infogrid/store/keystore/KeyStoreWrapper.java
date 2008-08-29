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

package org.infogrid.store.keystore;

import org.infogrid.store.Store;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreValue;
import org.infogrid.util.logging.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

/**
 * Given the less-than-optimal API of the Java <code>java.security.KeyStore</code> class, this class
 * wraps around it, and makes sure that the KeyStore is initialized by reading
 * from a Store, and updated contents are written back to the Store. As a result,
 * clients can use the KeyStore API while the actual content of the keystore is held
 * by a Store.
 */
public class KeyStoreWrapper
{
    private static final Log log = Log.getLogInstance( KeyStoreWrapper.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param store the Store with the KeyStore content
     * @param keyIntoStore the key for the KeyStore
     * @param keyStorePassword the password for the KeyStore
     * @return the created KeyStoreWrapper
     */
    public static KeyStoreWrapper create(
            Store  store,
            String keyIntoStore,
            String keyStorePassword )
    {
        return new KeyStoreWrapper( store, keyIntoStore, keyStorePassword );
    }

    /**
     * Constructor.
     *
     * @param store the Store with the KeyStore content
     * @param keyIntoStore the key for the KeyStore
     * @param keyStorePassword the password for the KeyStore
     */
    protected KeyStoreWrapper(
            Store  store,
            String keyIntoStore,
            String keyStorePassword )
    {
        theStore            = store;
        theKeyIntoStore     = keyIntoStore;
        theKeyStorePassword = keyStorePassword;
    }
    
    /**
     * Load data into this key store.
     *
     * @param inStream the data
     * @throws IOException thrown if an I/O error occurred
     * @throws GeneralSecurityException thrown if the client was not permitted to invoke this operation
     */
    public void load(
            InputStream inStream )
        throws
            IOException,
            GeneralSecurityException
    {
        KeyStore keyStore = getKeyStore();
        
        try {
            keyStore.load( inStream, theKeyStorePassword.toCharArray() );
        
            internalSave( theStore, keyStore, theKeyStorePassword );

        } catch( GeneralSecurityException ex ) {
            log.error( ex );
        }
    }

    /**
     * Obtain a Certificate with a certain name.
     *
     * @param alias the name of the Certificate
     * @return the Certificate, or null
     * @throws KeyStoreException thrown if the Certificate could not be obtained from the KeyStore
     * @throws GeneralSecurityException thrown if the client was not permitted to invoke this operation
     */
    public Certificate getCertificate(
            String alias )
        throws
            KeyStoreException,
            GeneralSecurityException
    {
        KeyStore keyStore = getKeyStore();

        Certificate ret = keyStore.getCertificate( alias );
        return ret;
    }

    /**
     * Obtain a Key with a certain name.
     *
     * @param alias the name
     * @return the Key
     * @throws GeneralSecurityException thrown if the client was not permitted to invoke this operation
     */
    public Key getKey(
            String alias )
        throws
            GeneralSecurityException
    {
        return getKey( alias, theKeyStorePassword );
    }

    /**
     * Obtain a Key with a certain name.
     *
     * @param alias the name
     * @param password the password on the Key
     * @return the Key
     * @throws GeneralSecurityException thrown if the client was not permitted to invoke this operation
     */
    public Key getKey(
            String alias,
            String password )
        throws
            GeneralSecurityException
    {
        KeyStore keyStore = getKeyStore();

        Key ret = keyStore.getKey( alias, password.toCharArray() );
        return ret;
    }

    /**
     * Internal helper to create or obtain the KeyStore.
     *
     * @return the created or obtained KeyStore
     * @throws GeneralSecurityException thrown if the client was not permitted to invoke this operation
     */
    protected KeyStore getKeyStore()
        throws
            GeneralSecurityException
    {
        if( theKeyStore == null ) {
            
            long now = System.currentTimeMillis();
            theTimeCreated = theTimeUpdated = theTimeRead = now;
            theTimeExpires = -1L;

            try {
                theKeyStore = KeyStore.getInstance( "JKS" );

                StoreValue v = theStore.get( theKeyIntoStore );
                
                theKeyStore.load( v.getDataAsStream(), theKeyStorePassword.toCharArray() );

                theTimeCreated = v.getTimeCreated();
                // all others are right
                
            } catch( StoreKeyDoesNotExistException ex ) {
                // we don't have it yet, first-time invocation
                log.info( ex );

            } catch( IOException ex ) {
                log.error( ex );
            }
        }
        return theKeyStore;
    }

    /**
     * Save the content of the KeyStore back into the Store. FIXME do we need this?
     *
     * @throws IOException thrown if an I/O error occurred
     */
    public void saveToStore()
        throws
            IOException
    {
        KeyStore keyStore = theKeyStore;
        
        if( keyStore == null ) {
            return; // nothing to save here
        }

        internalSave( theStore, keyStore, theKeyStorePassword );
    }
    
    /**
     * Export the KeyStore's data to this stream.
     *
     * @param outStream the stream to write to
     * @throws IOException thrown if an I/O error occurred
     */
    public void store(
            OutputStream outStream )
        throws
            IOException
    {
        try {
            KeyStore keyStore = getKeyStore();
            keyStore.store( outStream, theKeyStorePassword.toCharArray() );

        } catch( GeneralSecurityException ex ) {
            log.error( ex );
        }
    }

    /**
     * Factors out saving functionality.
     *
     * @param store the Store to save to
     * @param keyStore the KeyStore to store
     * @param password the KeyStore password
     * @throws IOException thrown if an I/O error occurred
     */
    protected void internalSave(
            Store    store,
            KeyStore keyStore,
            String   password )
        throws
            IOException
    {
        long now = System.currentTimeMillis();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            keyStore.store( outputStream, password.toCharArray() );

            byte [] data = outputStream.toByteArray();
            
            theTimeUpdated = now;

            store.putOrUpdate(
                    theKeyIntoStore,
                    getClass().getName(),
                    theTimeCreated,
                    theTimeUpdated,
                    theTimeRead,
                    theTimeExpires,
                    data );

        } catch( GeneralSecurityException ex ) {
            log.error( ex );
        }
    }
    
    /**
     * The Store in which the key materials are stored.
     */
    protected Store theStore;
    
    /**
     * The KeyStore we use.
     */
    protected KeyStore theKeyStore;
    
    /**
     * The key into the Store.
     */
    protected String theKeyIntoStore;
    
    /**
     * The password on the KeyStore.
     */
    protected String theKeyStorePassword;

    /**
     * The time the KeyStore was created.
     */
    protected long theTimeCreated;

    /**
     * The time the KeyStore was last updated.
     */
    protected long theTimeUpdated;

    /**
     * The time the KeyStore was last read.
     */
    protected long theTimeRead;

    /**
     * The time the KeyStore will expire.
     */
    protected long theTimeExpires;

    /**
     * Alias for the site's private key.
     */
    public static final String SITE_PRIVATE_KEY = "site-private-key";
}
