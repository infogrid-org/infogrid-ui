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

package org.infogrid.meshbase.store.net;

import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.meshbase.net.externalized.ExternalizedProxyEncoder;
import org.infogrid.meshbase.net.externalized.xml.ExternalizedProxyXmlEncoder;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyFactory;
import org.infogrid.model.primitives.externalized.DecodingException;
import org.infogrid.model.primitives.externalized.EncodingException;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.StoreValue;
import org.infogrid.store.StoreValueDecodingException;
import org.infogrid.store.StoreValueEncodingException;
import org.infogrid.util.FactoryException;
import org.infogrid.util.logging.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Knows how to map StoreProxies to and from StoreValues.
 */
public class StoreProxyEntryMapper
        implements
            StoreEntryMapper<NetMeshBaseIdentifier,Proxy>
{
    private static final Log log = Log.getLogInstance( StoreProxyEntryMapper.class ); // our own, private logger

    /**
     * Constructor.
     * 
     * @param proxyFactory the ProxyFactory to use
     */
    public StoreProxyEntryMapper(
            ProxyFactory proxyFactory )
    {
        theProxyFactory = proxyFactory;
    }

    /**
     * Set the NetMeshBase on whose bahalf the Mapper works.
     *
     * @param base the NetMeshBase
     */
    public void setMeshBase(
            NetMeshBase base )
    {
        theMeshBase = base;
    }

    /**
     * Map a key to a String value that can be used for the Store.
     *
     * @param key the key object
     * @return the corresponding String value that can be used for the Store
     */
    public String keyToString(
            NetMeshBaseIdentifier key )
    {
        String ret = key.toExternalForm();
        return ret;
    }

    /**
     * Map a String value that can be used for the Store to a key object.
     *
     * @param stringKey the key in String form
     * @return the corresponding key object
     * @throws StringRepresentationParseException thrown if a stringKey could not be converted into a valid Identifier
     */
    public NetMeshBaseIdentifier stringToKey(
            String stringKey )
        throws
            StringRepresentationParseException
    {
        NetMeshBaseIdentifier ret = theMeshBase.getMeshBaseIdentifierFactory().fromExternalForm( stringKey );
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
    public Proxy decodeValue(
            NetMeshBaseIdentifier key,
            StoreValue            value )
        throws
            StoreValueDecodingException
    {
        String encodingId = new String( value.getEncodingId() );

        ExternalizedProxyEncoder encoder = getEncoderFor( encodingId );
        if( encoder == null ) {
            log.error( "Unknown encoding ID: " + value.getEncodingId() );
            return null;
        }

        long timeCreated     = value.getTimeCreated();
        long timeUpdated     = value.getTimeUpdated();
        long timeRead        = value.getTimeRead();
        long timeExpires     = value.getTimeExpires();
        InputStream stream   = value.getDataAsStream();
        int  length          = value.getData().length;
        
        try {
            ExternalizedProxy externalized = encoder.decodeExternalizedProxy(
                    stream,
                    theMeshBase );

            Proxy ret = theProxyFactory.restoreProxy( externalized );

            return ret;
            
        } catch( FactoryException ex ) {
            throw new StoreValueDecodingException( ex );

        } catch( DecodingException ex ) {
            throw new StoreValueDecodingException( ex );

        } catch( IOException ex ) {
            throw new StoreValueDecodingException( ex );
        }
    }

    /**
     * Obtain the time a value was created.
     *
     * @param value the time a value was created.
     * @return the time created, in System.currentTimeMillis() format
     */
    public long getTimeCreated(
            Proxy value )
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
            Proxy value )
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
            Proxy value )
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
            Proxy value )
    {
        return value.getTimeExpires();
    }

    /**
     * Obtain the value as a byte array.
     *
     * @param value the value
     * @return the byte array
     * @throws StoreValueEncodingException thrown if the MeshObject could not be encoded
     */
    public byte [] asBytes(
            Proxy value )
        throws
            StoreValueEncodingException
    {
        ExternalizedProxyEncoder encoder = getEncoderFor( getPreferredEncodingId() );
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            ExternalizedProxy externalized = value.asExternalized();
            
            encoder.encodeExternalizedProxy( externalized, out );
            
            return out.toByteArray();

        } catch( EncodingException ex ) {
            throw new StoreValueEncodingException( ex );

        } catch( IOException ex ) {
            throw new StoreValueEncodingException( ex );
        }
    }


    /**
     * Obtain the preferred encodingid of this StoreMapper.
     *
     * @return the preferred encodingid
     */
    public String getPreferredEncodingId()
    {
        return NET_PREFERRED_ENCODING.getEncodingId();
    }

    /**
     * Obtain the ExternalizedProxyEncoder with a certain id.
     *
     * @param encoderId the id of the encoder
     * @return the ExternalizedProxyEncoder, or null
     */
    protected ExternalizedProxyEncoder getEncoderFor(
            String encoderId )
    {
        for( ExternalizedProxyEncoder current : theEncoders ) {
            if( encoderId.equals( current.getEncodingId() )) {
                return current;
            }
        }
        return null;
    }

    /**
     * The set of encoders / decoders currently known. This MUST be use
     * the same indices as in the superclass.
     */
    protected static final ExternalizedProxyEncoder [] theEncoders = {
            new ExternalizedProxyXmlEncoder()
    };

    /**
     * Preferred encoding type.
     */
    protected static final ExternalizedProxyEncoder NET_PREFERRED_ENCODING = theEncoders[0];
    
    /**
     * The ProxyFactory to use.
     */
    protected ProxyFactory theProxyFactory;

    /**
     * The NetMeshBase on whose behalf this mapper works.
     */
    protected NetMeshBase theMeshBase;
}
