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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.lid.store;

import java.io.UnsupportedEncodingException;
import org.infogrid.lid.LidSession;
import org.infogrid.lid.SimpleLidSession;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.StoreValue;
import org.infogrid.store.StoreValueDecodingException;
import org.infogrid.store.StoreValueEncodingException;
import org.infogrid.util.IdentifierFactory;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Maps session cookies into the Store.
 */
public class StoreLidSessionMapper
        implements
            StoreEntryMapper<String,LidSession>
{
    /**
     * Constructor.
     *
     * @param idFact the IdentifierFactory to use for client and site Identifiers
     */
    public StoreLidSessionMapper(
            IdentifierFactory idFact )
    {
        theIdentifierFactory = idFact;
    }

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
    public LidSession decodeValue(
            String     key,
            StoreValue value )
        throws
            StoreValueDecodingException
    {
        try {
            byte [] bytes = value.getData();

            String data = new String( bytes, CHARSET );

            String [] components = data.split( SEPARATOR );
            String sessionToken;
            String clientIdentifier;
            String siteIdentifier;
            String creationClientIp;
            long   timeLastAuthenticated;
            long   timeLastUsedSuccessfully;
            long   timeValidUntil;

            if( components.length >= 1 ) {
                sessionToken = components[0];
            } else {
                sessionToken = null;
            }
            if( components.length >= 2 ) {
                clientIdentifier = components[1];
            } else {
                clientIdentifier = null;
            }
            if( components.length >= 3 ) {
                siteIdentifier = components[2];
            } else {
                siteIdentifier = null;
            }
            if( components.length >= 4 ) {
                creationClientIp = components[3];
            } else {
                creationClientIp = null;
            }
            if( components.length >= 5 ) {
                timeLastAuthenticated = Long.parseLong( components[4] );
            } else {
                timeLastAuthenticated = -1L;
            }
            if( components.length >= 6 ) {
                timeLastUsedSuccessfully = Long.parseLong( components[5] );
            } else {
                timeLastUsedSuccessfully = -1L;
            }
            if( components.length >= 7 ) {
                timeValidUntil = Long.parseLong( components[6] );
            } else {
                timeValidUntil = -1L;
            }
            
            LidSession ret = SimpleLidSession.create(
                    sessionToken,
                    clientIdentifier != null ? theIdentifierFactory.fromExternalForm( clientIdentifier ) : null,
                    siteIdentifier != null   ? theIdentifierFactory.fromExternalForm( siteIdentifier   ) : null,
                    value.getTimeCreated(),
                    value.getTimeUpdated(),
                    value.getTimeRead(),
                    value.getTimeExpires(),
                    timeLastAuthenticated,
                    timeLastUsedSuccessfully,
                    timeValidUntil,
                    creationClientIp );
            
            return ret;

        } catch( UnsupportedEncodingException ex ) {
            throw new StoreValueDecodingException( ex );
        } catch( StringRepresentationParseException ex ) {
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
            buf.append( value.getSessionToken() );
            buf.append( SEPARATOR );
            buf.append( value.getClientIdentifier().toExternalForm() );
            buf.append( SEPARATOR );
            buf.append( value.getSiteIdentifier().toExternalForm() );
            buf.append( SEPARATOR );
            buf.append( value.getCreationClientIp() );
            buf.append( SEPARATOR );
            buf.append( value.getTimeLastAuthenticated() );
            buf.append( SEPARATOR );
            buf.append( value.getTimeLastUsedSuccessfully() );
            buf.append( SEPARATOR );
            buf.append( value.getTimeValidUntil() );
            
            byte [] ret = buf.toString().getBytes( CHARSET );
            return ret;

        } catch( UnsupportedEncodingException ex ) {
            throw new StoreValueEncodingException( ex );
        }
    }

    /**
     * Identifier Factory to use for client and site identifiers.
     */
    protected IdentifierFactory theIdentifierFactory;

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
    protected static final String SEPARATOR = " ";
}
