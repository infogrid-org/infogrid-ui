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

package org.infogrid.meshbase.net.externalized;

import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObjectFactory;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.model.primitives.externalized.DecodingException;
import org.infogrid.model.primitives.externalized.EncodingException;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Knows how to serialize and deserialize a Proxy.
 */
public interface ExternalizedProxyEncoder
{
    /**
     * Serialize an ExternalizedProxy to an OutputStream.
     * 
     * @param p the input ExternalizedProxy
     * @param out the OutputStream to which to append the ExternalizedProxy
     * @throws EncodingException thrown if a problem occurred during encoding
     * @throws IOException thrown if an I/O error occurred
     */
    public void encodeExternalizedProxy(
            ExternalizedProxy p,
            OutputStream      out )
        throws
            EncodingException,
            IOException;

    /**
     * Deserialize a ExternalizedProxy from a stream.
     * 
     * @param contentAsStream the byte [] stream in which the ExternalizedProxy is encoded
     * @param externalizedMeshObjectFactory the factory to use for ExternalizedMeshObjects
     * @param meshObjectIdentifierFactory the factory to use for MeshObjectIdentifier
     * @param meshTypeIdentifierFactory the factory to use for MeshTypes
     * @return return the just-instantiated ExternalizedProxy
     * @throws DecodingException thrown if a problem occurred during decoding
     * @throws IOException thrown if an I/O error occurred
     */
    public abstract ExternalizedProxy decodeExternalizedProxy(
            InputStream                                    contentAsStream,
            ParserFriendlyExternalizedNetMeshObjectFactory externalizedMeshObjectFactory,
            NetMeshObjectIdentifierFactory                 meshObjectIdentifierFactory,
            MeshTypeIdentifierFactory                      meshTypeIdentifierFactory )
        throws
            DecodingException,
            IOException;

    /**
     * Obtain an encodingId that reflects this ExternalizedProxyEncoder.
     * 
     * @return the encodingId.
     */
    public String getEncodingId();
}
