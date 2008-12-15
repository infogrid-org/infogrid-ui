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

package org.infogrid.meshbase.net;

import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.MeshObjectIdentifierFactory;

import java.net.URISyntaxException;

/**
 * Specializes MeshObjectIdentifierFactory to create NetMeshObjectIdentifiers.
 */
public interface NetMeshObjectIdentifierFactory
        extends
            MeshObjectIdentifierFactory
{
    /**
     * Determine the Identifier of the Home Object.
     *
     * @return the Identifier
     */
    public abstract NetMeshObjectIdentifier getHomeMeshObjectIdentifier();

    /**
     * Create a unique Identifier for a MeshObject that can be used to create a MeshObject
     * with the associated MeshBaseLifecycleManager.
     *
     * @return the created Identifier
     */
    public abstract NetMeshObjectIdentifier createMeshObjectIdentifier();

    /**
     * Create an identifier for a MeshObject held locally at this MeshBase.
     *
     * @param raw the identifier String
     * @return the created NetMeshObjectIdentifier
     * @throws URISyntaxException a parsing error occurred
     */
    // @Override except that the compiler doesn't like it
    public NetMeshObjectIdentifier fromExternalForm(
            String raw )
        throws
            URISyntaxException;

    /**
     * Create an identifier for a MeshObject held at a different MeshBase.
     *
     * @param meshBaseIdentifier MeshBaseIdentifier of the MeshBase where the object is held
     * @param raw the identifier String
     * @return the created NetMeshObjectIdentifier
     * @throws URISyntaxException a parsing error occurred
     */
    public NetMeshObjectIdentifier fromExternalForm(
            NetMeshBaseIdentifier meshBaseIdentifier,
            String                raw )
        throws
            URISyntaxException;

    /**
     * Recreate a NetMeshObjectIdentifier from an external form. Be lenient about syntax and
     * attempt to interpret what the user meant when entering an invalid or incomplete
     * raw String.
     *
     * @param raw the external form
     * @return the created MeshObjectIdentifier
     * @throws URISyntaxException thrown if a parsing error occurred
     */
    public NetMeshObjectIdentifier guessFromExternalForm(
            String raw )
        throws
            URISyntaxException;
}
