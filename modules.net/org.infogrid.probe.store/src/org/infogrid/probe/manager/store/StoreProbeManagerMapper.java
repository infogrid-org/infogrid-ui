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

package org.infogrid.probe.manager.store;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.model.primitives.externalized.DecodingException;
import org.infogrid.model.primitives.externalized.EncodingException;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.externalized.ExternalizedShadowMeshBase;
import org.infogrid.probe.shadow.externalized.xml.ExternalizedShadowMeshBaseXmlEncoder;
import org.infogrid.probe.shadow.store.StoreShadowMeshBase;
import org.infogrid.probe.shadow.store.StoreShadowMeshBaseFactory;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.StoreValue;
import org.infogrid.store.StoreValueDecodingException;
import org.infogrid.store.StoreValueEncodingException;

/**
 * Maps ShadowMeshBases to and from Stores.
 */
public class StoreProbeManagerMapper
        implements
            StoreEntryMapper<NetMeshBaseIdentifier,ShadowMeshBase>
{
    /**
     * Constructor.
     * 
     * @param factory the factory to recreate StoreMeshBases
     */
    public StoreProbeManagerMapper(
            StoreShadowMeshBaseFactory factory )
    {
        theFactory = factory;
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
     * @throws URISyntaxException thrown if a stringKey could not be converted into a valid Identifier
     */
    public NetMeshBaseIdentifier stringToKey(
            String stringKey )
        throws
             URISyntaxException
    {
        NetMeshBaseIdentifier ret = theFactory.getNetMeshBaseIdentifierFactory().fromExternalForm( stringKey );
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
    public ShadowMeshBase decodeValue(
            NetMeshBaseIdentifier key,
            StoreValue            value )
        throws
            StoreValueDecodingException
    {
        // we don't need a transaction here
        StoreShadowMeshBase ret = null;
        try {
            
            ret = theFactory.createEmptyForRestore( key );
            
            ExternalizedShadowMeshBase externalized = theEncoder.decodeShadowMeshBase(
                    value.getDataAsStream(),
                    ret );
            
            ret.restoreTo( externalized );

        } catch( DecodingException ex ) {
            throw new StoreValueDecodingException( ex );

        } catch( IOException ex ) {
            throw new StoreValueDecodingException( ex );

        }
        return ret;
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
     * Obtain the time a value was created.
     *
     * @param value the time a value was created.
     * @return the time created, in System.currentTimeMillis() format
     */
    public long getTimeCreated(
            ShadowMeshBase value )
    {
        long ret = value.getTimeCreated();
        return ret;
    }

    /**
     * Obtain the time a value was last updated.
     *
     * @param value the time a value was last updated.
     * @return the time updated, in System.currentTimeMillis() format
     */
    public long getTimeUpdated(
            ShadowMeshBase value )
    {
        long ret = value.getLastSuccessfulUpdateStartedTime();
        return ret;
    }

    /**
     * Obtain the time a value was last read.
     *
     * @param value the time a value was last read.
     * @return the time read, in System.currentTimeMillis() format
     */
    public long getTimeRead(
            ShadowMeshBase value )
    {
        return getTimeUpdated( value );
    }

    /**
     * Obtain the time a value will expire.
     *
     * @param value the time a value will expire.
     * @return the time will expire, in System.currentTimeMillis() format
     */
    public long getTimeExpires(
            ShadowMeshBase value )
    {
        long ret = System.currentTimeMillis() + value.getDelayUntilNextUpdate(); // FIXME can this be done better?
        return ret;
    }

    /**
     * Obtain the value as a byte array.
     *
     * @return the byte array
     * @throws StoreValueEncodingException thrown if the value could not been encoded
     */
    public byte [] asBytes(
            ShadowMeshBase value )
        throws
            StoreValueEncodingException
    {
        StoreShadowMeshBase realBase = (StoreShadowMeshBase) value;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            theEncoder.encodeShadowMeshBase( realBase.asExternalized(), false, out ); // don't encode Proxies here

            byte [] data = out.toByteArray();

            return data;

        } catch( IOException ex ) {
            throw new StoreValueEncodingException( ex );

        } catch( EncodingException ex ) {
            throw new StoreValueEncodingException( ex );
        }
    }

    /**
     * Factory to use to re-obtain StoreShadowMeshBases.
     */
    protected StoreShadowMeshBaseFactory theFactory;

    /**
     * Encoder for the content of the StoreShadowMeshBase.
     */
    protected ExternalizedShadowMeshBaseXmlEncoder theEncoder = new ExternalizedShadowMeshBaseXmlEncoder();
}
