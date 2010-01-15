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

package org.infogrid.meshbase.store.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.externalized.ExternalizedMeshObjectEncoder;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObjectEncoder;
import org.infogrid.mesh.net.externalized.xml.ExternalizedNetMeshObjectXmlEncoder;
import org.infogrid.meshbase.store.AbstractStoreMeshBaseEntryMapper;
import org.infogrid.model.primitives.externalized.EncodingException;
import org.infogrid.store.StoreValueEncodingException;

/**
 * Extends StoreMeshBaseEntryMapper for NetMeshObject data.
 */
public class NetStoreMeshBaseEntryMapper
        extends
            AbstractStoreMeshBaseEntryMapper
{
    /**
     * Constructor.
     */
    public NetStoreMeshBaseEntryMapper()
    {
        // noop
    }

    /**
     * Obtain the value as a byte array.
     *
     * @param value the value
     * @return the byte array
     * @throws StoreValueEncodingException thrown if the MeshObject could not be encoded
     */
    @Override
    public byte [] asBytes(
            MeshObject value )
        throws
            StoreValueEncodingException
    {
        NetMeshObject realValue = (NetMeshObject) value;

        ExternalizedMeshObjectEncoder encoder = getEncoderFor( getPreferredEncodingId() );
        
        try {
            ExternalizedNetMeshObject externalValue = realValue.asExternalized( true );
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            encoder.encodeExternalizedMeshObject( externalValue, out );
            
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
     * Obtain the StoreMeshBaseEncoder with a certain id.
     *
     * @return the StoreMeshBaseEncoder, or null
     */
    protected ExternalizedMeshObjectEncoder getEncoderFor(
            String encoderId )
    {
        for( ExternalizedNetMeshObjectEncoder current : theNetEncoders ) {
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
    protected static final ExternalizedNetMeshObjectEncoder[] theNetEncoders = {
            new ExternalizedNetMeshObjectXmlEncoder()
            
    };

    /**
     * Preferred encoding type.
     */
    protected static final ExternalizedNetMeshObjectEncoder NET_PREFERRED_ENCODING = theNetEncoders[0];
}
