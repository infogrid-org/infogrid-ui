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

import org.infogrid.mesh.externalized.ExternalizedMeshObjectEncoder;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.model.primitives.externalized.DecodingException;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * This interface is supported by classes that know how to serialize and deserialize
 * ExternalizedNetMeshObject.
 */
public interface ExternalizedNetMeshObjectEncoder
        extends
            ExternalizedMeshObjectEncoder // This extends because we only narrow the return type
{
    /**
     * Deserialize an ExternalizedMeshObject from a byte stream.
     *
     * @param s the InputStream from which to read
     * @param externalizedMeshObjectFactory the factory for ParserFriendlyExternalizedMeshObjects
     * @param meshObjectIdentifierFactory the factory for MeshObjectIdentifiers
     * @param meshTypeIdentifierFactory the factory for MeshTypeIdentifiers
     * @return return the just-instantiated ExternalizedMeshObject
     * @throws DecodingException thrown if a problem occurred during decoding
     * @throws IOException thrown if a problem occurred during writing the output
     */
    // Compiler does not like the @Override here
    public ExternalizedNetMeshObject decodeExternalizedMeshObject(
            InputStream                                    s,
            ParserFriendlyExternalizedNetMeshObjectFactory externalizedMeshObjectFactory,
            NetMeshObjectIdentifierFactory                 meshObjectIdentifierFactory,
            MeshTypeIdentifierFactory                      meshTypeIdentifierFactory )
        throws
            DecodingException,
            IOException;
}
