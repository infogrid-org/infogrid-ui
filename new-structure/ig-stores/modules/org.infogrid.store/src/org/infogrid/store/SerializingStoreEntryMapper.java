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

package org.infogrid.store;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * A StoreEntryMapper that uses Java Serialization, and thus can store and retrieve arbitrary
 * Java object graphs in a <code>Store</code>. It uses the key's <code>toString</code> method
 * to construct the String key needed by <code>Store</code>.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public abstract class SerializingStoreEntryMapper<K,V>
        implements
            StoreEntryMapper<K,V>
{
    private static final Log log = Log.getLogInstance( SerializingStoreEntryMapper.class ); // our own, private logger

    /**
     * Constructor. Use default class loader.
     */
    public SerializingStoreEntryMapper()
    {
        theClassLoader = null;
    }

    /**
     * Constructor.
     *
     * @param cl the ClassLoader to use
     */
    public SerializingStoreEntryMapper(
            ClassLoader cl )
    {
        theClassLoader = cl;
    }

    /**
     * Map a key to a String value that can be used for the Store.
     *
     * @param key the key object
     * @return the corresponding String value that can be used for the Store.
     */
    public String keyToString(
            K key )
    {
        String ret;
        if( key != null ) {
            ret = key.toString();
        } else {
            ret = null;
        }
        return ret;
    }
    
    /**
     * Map a String value that can be used for the Store to a key object.
     *
     * @param stringKey the key in String form
     * @return the corresponding key object
     * @throws StringRepresentationParseException thrown if a stringKey could not be converted into a valid Identifier
     */
    public abstract K stringToKey(
            String stringKey )
        throws
            StringRepresentationParseException;

    /**
     * Map a StoreValue to a value.
     *
     * @param key the key to the StoreValue
     * @param value the StoreValue
     * @return the value
     */
    @SuppressWarnings(value={"unchecked"})
    public V decodeValue(
            K          key,
            StoreValue value )
    {
        try {
            ObjectInputStream in = new ObjectInputStream( value.getDataAsStream() ) {
                    // code similar to the superclass
                    @Override
                    protected Class<?> resolveClass(
                            ObjectStreamClass desc )
                        throws
                            IOException,
                            ClassNotFoundException
                    {
                        String name = desc.getName();
                        if( theClassLoader != null ) {
                            try {
                                return Class.forName( name, false, theClassLoader );
                            } catch (ClassNotFoundException ex) {
                                // noop
                            }
                        }
                        return super.resolveClass( desc );
                    }
            };

            Object obj = in.readObject();
            return (V) obj;

        } catch( Exception ex ) {
            log.error( ex );
            return null;
        }
    }
    
    /**
     * Obtain the preferred encoding id of this StoreEntryMapper.
     * 
     * @return the preferred encoding id
     */
    public String getPreferredEncodingId()
    {
        return getClass().getName();
    }

    /**
     * Obtain the time a data element was created.
     *
     * @param value the time a data element was created.
     * @return the time created, in System.currentTimeMillis() format
     */
    public long getTimeCreated(
            V value )
    {
        return -1;
    }

    /**
     * Obtain the time a data element was last updated.
     *
     * @param value the time a data element was last updated.
     * @return the time updated, in System.currentTimeMillis() format
     */
    public long getTimeUpdated(
            V value )
    {
        return -1;
    }

    /**
     * Obtain the time a data element was last read.
     *
     * @param value the time a data element was last read.
     * @return the time read, in System.currentTimeMillis() format
     */
    public long getTimeRead(
            V value )
    {
        return -1;
    }

    /**
     * Obtain the time a data element will expire.
     *
     * @param value the time a data element will expire.
     * @return the time will auto-delete, in System.currentTimeMillis() format
     */
    public long getTimeExpires(
            V value )
    {
        return -1;
    }

    /**
     * Obtain the value as a byte array.
     *
     * @return the byte array
     */
    public byte [] asBytes(
            V value )
    {
        try {
            ByteArrayOutputStream stream    = new ByteArrayOutputStream();
            ObjectOutputStream    objStream = new ObjectOutputStream( stream );

            objStream.writeObject( value );
            objStream.flush();

            byte [] ret = stream.toByteArray();

            return ret;

        } catch( IOException ex ) {
            log.error( ex );
            return null;
        }
    }
    
    /**
     * The ClassLoader that we use, if any.
     */
    protected ClassLoader theClassLoader;
}
