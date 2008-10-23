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

package org.infogrid.mesh.net.externalized;

import java.io.IOException;
import java.io.InputStream;
import org.infogrid.mesh.externalized.ExternalizedMeshObjectEncoder;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.primitives.externalized.DecodingException;

/**
 * This interface is supported by classes that know how to serialize and deserialize
 * ExternalizedNetMeshObject.
 */
public interface ExternalizedNetMeshObjectEncoder
        extends
            ExternalizedMeshObjectEncoder // This extends because we only narrow the return type
{
    /**
     * Deserialize a ExternalizedNetMeshObject from a stream.
     * 
     * @param contentAsStream the byte [] stream in which the ExternalizedProxy is encoded
     * @param mb the NetMeshBase on whose behalf the decoding is performed
     * @return return the just-instantiated ExternalizedNetMeshObject
     * @throws DecodingException thrown if a problem occurred during decoding
     * @throws IOException thrown if an I/O error occurred
     */
    public ExternalizedNetMeshObject decodeExternalizedMeshObject(
            InputStream contentAsStream,
            MeshBase    mb )
        throws
            DecodingException,
            IOException;
}
