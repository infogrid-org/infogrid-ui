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

package org.infogrid.lid.gpg.store;

import java.io.UnsupportedEncodingException;
import org.infogrid.lid.gpg.LidKeyPair;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.StoreValue;
import org.infogrid.store.StoreValueDecodingException;
import org.infogrid.store.StoreValueEncodingException;

/**
 * Maps LidKeyPairs from and to the Store. FIXME: This is not a very robust implementation.
 */
public class StoreLidKeyPairMapper
        implements
            StoreEntryMapper<String,LidKeyPair>
{
    /**
     * Map a key to a String value that can be used for the Store.
     *
     * @param key the key object
     * @return the corresponding String value that can be used for the Store
     */
    public String keyToString(
            String key )
    {
        return key;
    }

    /**
     * Map a String value that can be used for the Store to a key object.
     *
     * @param stringKey the key in String form
     * @return the corresponding key object
     */
    public String stringToKey(
            String stringKey )
    {
        return stringKey;
    }

    /**
     * Map a StoreValue to a value.
     *
     * @param key the key to the StoreValue
     * @param value the StoreValue
     * @return the value
     * @throws StoreValueDecodingException thrown if the StoreValue could not been decoded
     */
    public LidKeyPair decodeValue(
            String     key,
            StoreValue value )
        throws
            StoreValueDecodingException
    {
        try {
            String buf = new String( value.getData(), CHARSET );

            int from = buf.indexOf(  "\"" )+1;
            int to   = buf.indexOf( "\"", from );

            String pub = buf.substring( from, to );

            from = buf.indexOf( "\"", to+1 );
            to   = buf.indexOf( "\"", from );

            String priv = buf.substring( from, to );

            LidKeyPair ret = new LidKeyPair( key, pub, priv );
            return ret;

        } catch( UnsupportedEncodingException ex ) {
            throw new StoreValueDecodingException( ex );
        }
    }
    
    /**
     * Obtain the preferred encoding id of this StoreEntryMapper.
     * 
     * @return the preferred encoding id
     */
    public String getPreferredEncodingId()
    {
        return ENCODING;
    }

    /**
     * Obtain the time a value was created.
     *
     * @param value the time a value was created.
     * @return the time created, in System.currentTimeMillis() format
     */
    public long getTimeCreated(
            LidKeyPair value )
    {
        return -1L;
    }

    /**
     * Obtain the time a value was last updated.
     *
     * @param value the time a value was last updated.
     * @return the time updated, in System.currentTimeMillis() format
     */
    public long getTimeUpdated(
            LidKeyPair value )
    {
        return -1L;
    }

    /**
     * Obtain the time a value was last read.
     *
     * @param value the time a value was last read.
     * @return the time read, in System.currentTimeMillis() format
     */
    public long getTimeRead(
            LidKeyPair value )
    {
        return -1L;
    }

    /**
     * Obtain the time a value will expire.
     *
     * @param value the time a value will expire.
     * @return the time will expire, in System.currentTimeMillis() format
     */
    public long getTimeExpires(
            LidKeyPair value )
    {
        return -1L;
    }

    /**
     * Obtain the value as a byte array.
     *
     * @param value the value
     * @return the byte array
     * @throws StoreValueEncodingException thrown if the value could not been encoded
     */
    public byte [] asBytes(
            LidKeyPair value )
        throws
            StoreValueEncodingException
    {
        try {
            StringBuilder almost = new StringBuilder();
            almost.append( "publicKey=\"" );
            almost.append( value.getPublicKey() );
            almost.append( "\"; privateKey=\"" );
            almost.append( value.getPrivateKey() );
            almost.append( "\"" );
            return almost.toString().getBytes( CHARSET );

        } catch( UnsupportedEncodingException ex ) {
            throw new StoreValueEncodingException( ex );
        }
    }

    
    /**
     * The encoding to use.
     */
    public static final String ENCODING = StoreLidKeyPairMapper.class.getName();

    /**
     * The character set to use.
     */
    public static final String CHARSET = "UTF-8";
}