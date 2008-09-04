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

package org.infogrid.meshbase.a;

import org.infogrid.mesh.a.DefaultAMeshObjectIdentifier;
import org.infogrid.meshbase.AbstractMeshObjectIdentifierFactory;

import org.infogrid.util.text.StringRepresentation;

import org.infogrid.util.text.StringifierException;

import java.net.URISyntaxException;

/**
 * Default implementation of MeshObjectIdentifierFactory for the A implementation.
 */
public class DefaultAMeshObjectIdentifierFactory
        extends
            AbstractMeshObjectIdentifierFactory
{
    /**
     * Factory method.
     *
     * @return the created DefaultAMeshObjectIdentifierFactory
     */
    public static DefaultAMeshObjectIdentifierFactory create()
    {
        DefaultAMeshObjectIdentifierFactory ret = new DefaultAMeshObjectIdentifierFactory();
        return ret;
    }

    /**
     * Constructor.
     */
    protected DefaultAMeshObjectIdentifierFactory()
    {
        // no op
    }

    /**
     * Create an identifier for a MeshObject at held locally at this MeshBase.
     *
     * @param localId the local distinguishing String
     * @throws URISyntaxException
     */
    public DefaultAMeshObjectIdentifier fromExternalForm(
            String localId )
        throws
            URISyntaxException
    {
        return DefaultAMeshObjectIdentifier.create( localId );
    }

    /**
     * Convert this StringRepresentation back to an Identifier.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created MeshObjectIdentifier
     * @throws URISyntaxException thrown if a parsing error occurred
     */
    public DefaultAMeshObjectIdentifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            URISyntaxException
    {
        try {
            representation.parseEntry( DefaultAMeshObjectIdentifier.class, DefaultAMeshObjectIdentifier.HOME_DEFAULT_ENTRY, s );
            return fromExternalForm( "" );

        } catch( StringifierException ex ) {
            // that wasn't it ...
        }
        try {
            Object [] found = representation.parseEntry( DefaultAMeshObjectIdentifier.class, DefaultAMeshObjectIdentifier.DEFAULT_ENTRY, s );

            DefaultAMeshObjectIdentifier ret;
            switch( found.length ) {
                case 1:
                    ret = fromExternalForm( (String) found[0] );
                    break;

                default:
                    throw new URISyntaxException( s, "Cannot parse identifier" );
            }

            return ret;

        } catch( StringifierException ex ) {
            throw new URISyntaxException( s, "Cannot parse identifier" );

        } catch( ClassCastException ex ) {
            throw new URISyntaxException( s, "Cannot parse identifier" );
        }
    }
    
    /**
     * Determine the Identifier of the Home Object.
     *
     * @return the Identifier
     */
    public DefaultAMeshObjectIdentifier getHomeMeshObjectIdentifier()
    {
        return HOME_OBJECT;
    }

    /**
     * The Home Object's identifier. Subclass to avoid having to make the constructor public.
     */
    public static final DefaultAMeshObjectIdentifier HOME_OBJECT = new DefaultAMeshObjectIdentifier( null ) {};
}
