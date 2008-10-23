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

import org.infogrid.model.primitives.externalized.DecodingException;
import org.infogrid.model.primitives.externalized.EncodingException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.infogrid.meshbase.net.NetMeshBase;

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
     * @param mb the NetMeshBase on whose behalf the decoding is performed
     * @return return the just-instantiated ExternalizedProxy
     * @throws DecodingException thrown if a problem occurred during decoding
     * @throws IOException thrown if an I/O error occurred
     */
    public abstract ExternalizedProxy decodeExternalizedProxy(
            InputStream contentAsStream,
            NetMeshBase mb )
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
