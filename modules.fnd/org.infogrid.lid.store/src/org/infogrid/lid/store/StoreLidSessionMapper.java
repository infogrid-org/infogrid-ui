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

package org.infogrid.lid.store;

import java.io.UnsupportedEncodingException;
import org.infogrid.lid.LidSession;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.StoreValue;
import org.infogrid.store.StoreValueDecodingException;
import org.infogrid.store.StoreValueEncodingException;
import org.infogrid.util.Identifier;
import org.infogrid.util.SimpleStringIdentifier;

/**
 * Maps session cookies into the Store.
 */
public class StoreLidSessionMapper
        implements
            StoreEntryMapper<Identifier,LidSession>
{
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
     */
    public Identifier stringToKey(
            String stringKey )
    {
        return SimpleStringIdentifier.create( stringKey );
    }

    /**
     * Map a StoreValue to a value.
     *
     * @param key the key to the StoreValue
     * @param value the StoreValue
     * @return the value
     * @throws StoreValueDecodingException thrown if the StoreValue could not been decoded
     */
    public LidSession decodeValue(
            Identifier key,
            StoreValue value )
        throws
            StoreValueDecodingException
    {
        try {
            byte [] bytes = value.getData();

            String data = new String( bytes, CHARSET );

            int sep = data.indexOf( SEPARATOR );
            
            Identifier lid              = key;
            String     cookieValue      = data.substring( 0, sep );
            String     creationClientIp = data.substring( sep+1 );
            
            LidSession ret = LidSession.create(
                    lid,
                    cookieValue,
                    value.getTimeCreated(),
                    value.getTimeUpdated(),
                    value.getTimeRead(),
                    value.getTimeExpires(),
                    creationClientIp );
            
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
             LidSession value )
    {
        return value.getTimeCreated();
    }

    /**
     * Obtain the time a value was last updated.
     *
     * @param value the time a value was last updated.
     * @return the time updated, in System.currentTimeMillis() format
     */
    public long getTimeUpdated(
            LidSession value )
    {
        return value.getTimeUpdated();
    }

    /**
     * Obtain the time a value was last read.
     *
     * @param value the time a value was last read.
     * @return the time read, in System.currentTimeMillis() format
     */
    public long getTimeRead(
            LidSession value )
    {
        return value.getTimeRead();
    }

    /**
     * Obtain the time a value will expire.
     *
     * @param value the time a value will expire.
     * @return the time will expire, in System.currentTimeMillis() format
     */
    public long getTimeExpires(
            LidSession value )
    {
        return value.getTimeExpires();
    }

    /**
     * Obtain the value as a byte array.
     *
     * @param value the value
     * @return the byte array
     * @throws StoreValueEncodingException thrown if the value could not been encoded
     */
    public byte [] asBytes(
            LidSession value )
        throws
            StoreValueEncodingException
    {
        try {
            StringBuilder buf = new StringBuilder();
            buf.append( value.getCookieValue() );
            buf.append( SEPARATOR );
            buf.append( value.getCreationClientIp() );
            
            byte [] ret = buf.toString().getBytes( CHARSET );
            return ret;

        } catch( UnsupportedEncodingException ex ) {
            throw new StoreValueEncodingException( ex );
        }
    }
    
    /**
     * The encoding to use.
     */
    public static final String ENCODING = StoreLidSessionMapper.class.getName();

    /**
     * The character set to use.
     */
    public static final String CHARSET = "UTF-8";
    
    /**
     * The separator in the serialized value.
     */
    protected static final String SEPARATOR = "|";
}
