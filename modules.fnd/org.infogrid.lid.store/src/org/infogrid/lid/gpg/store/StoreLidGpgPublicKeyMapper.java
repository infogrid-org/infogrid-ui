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
import java.net.URISyntaxException;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.StoreValue;
import org.infogrid.store.StoreValueDecodingException;
import org.infogrid.store.StoreValueEncodingException;
import org.infogrid.util.Identifier;
import org.infogrid.util.IdentifierFactory;

/**
 * Maps LID public keys from and to the Store.
 */
public class StoreLidGpgPublicKeyMapper
        implements
            StoreEntryMapper<Identifier,String>
{
    /**
     * Factory method.
     *
     * @param identifierFactory the IdentifierFactory to use
     * @return the created StoreLidGpgPublicKeyMapper
     */
    public static StoreLidGpgPublicKeyMapper create(
            IdentifierFactory identifierFactory )
    {
        StoreLidGpgPublicKeyMapper ret = new StoreLidGpgPublicKeyMapper( identifierFactory );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param identifierFactory the IdentifierFactory to use
     */
    protected StoreLidGpgPublicKeyMapper(
            IdentifierFactory identifierFactory )
    {
        theIdentifierFactory = identifierFactory;
    }

    /**
     * Map a key to a String value that can be used for the Store.
     *
     * @param key the key object
     * @return the corresponding String value that can be used for the Store
     */
    public String keyToString(
            Identifier key )
    {
        return key.toExternalForm();
    }

    /**
     * Map a String value that can be used for the Store to a key object.
     *
     * @param stringKey the key in String form
     * @return the corresponding key object
     * @throws URISyntaxException thrown if a stringKey could not be converted into a valid Identifier
     */
    public Identifier stringToKey(
            String stringKey )
        throws
            URISyntaxException
    {
        Identifier ret = theIdentifierFactory.fromExternalForm( stringKey );
        return ret;
    }

    /**
     * Map a StoreValue to a value.
     *
     * @param key the key to the StoreValue
     * @param value the StoreValue
     * @return the value
     * @throws StoreValueDecodingException thrown if the StoreValue could not been decoded
     */
    public String decodeValue(
            Identifier key,
            StoreValue value )
        throws
            StoreValueDecodingException
    {
        try {
            String ret = new String( value.getData(), CHARSET );

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
            String value )
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
            String value )
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
            String value )
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
            String value )
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
            String value )
        throws
            StoreValueEncodingException
    {
        try {
            return value.getBytes( CHARSET );

        } catch( UnsupportedEncodingException ex ) {
            throw new StoreValueEncodingException( ex );
        }
    }

    /**
     * The IdentifierFactory to use.
     */
    protected IdentifierFactory theIdentifierFactory;

    /**
     * The encoding to use.
     */
    public static final String ENCODING = StoreLidGpgPublicKeyMapper.class.getName();

    /**
     * The character set to use.
     */
    public static final String CHARSET = "UTF-8";
}