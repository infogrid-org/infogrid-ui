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

package org.infogrid.meshbase.net.a;

import java.net.URISyntaxException;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.a.DefaultAnetMeshObjectIdentifier;
import org.infogrid.meshbase.a.DefaultAMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.util.text.StringifierException;
import org.infogrid.util.text.StringRepresentation;

/**
 * The default NetMeshObjectIdentifierFactory in the "A" implementation.
 */
public class DefaultAnetMeshObjectIdentifierFactory
        extends
            DefaultAMeshObjectIdentifierFactory
        implements
            NetMeshObjectIdentifierFactory
{
    /**
     * Factory method.
     *
     * @param meshBaseIdentifier the NetMeshBaseIdentifier of the owning NetMeshBase
     * @param meshBaseIdentifierFactory factory for NetMeshBaseIdentifiers
     * @return the created DefaultAMeshObjectIdentifierFactory
     */
    public static DefaultAnetMeshObjectIdentifierFactory create(
            NetMeshBaseIdentifier        meshBaseIdentifier,
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
    {
        DefaultAnetMeshObjectIdentifierFactory ret
                = new DefaultAnetMeshObjectIdentifierFactory( meshBaseIdentifier, meshBaseIdentifierFactory );
        return ret;
    }

    /**
     * Constructor.
     * 
     * @param meshBaseIdentifier the NetMeshBaseIdentifier of the owning NetMeshBase
     * @param meshBaseIdentifierFactory factory for NetMeshBaseIdentifiers
     */
    protected DefaultAnetMeshObjectIdentifierFactory(
            NetMeshBaseIdentifier        meshBaseIdentifier,
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
    {
        theMeshBaseIdentifier     = meshBaseIdentifier;
        theMeshBaseIdentifierFactory = meshBaseIdentifierFactory;

        NET_HOME_OBJECT = new HomeObject( theMeshBaseIdentifier  );
    }

    /**
     * Create a unique NetMeshObjectIdentifier.
     *
     * @return the unique NetMeshObjectIdentifier
     */
    @Override
    public NetMeshObjectIdentifier createMeshObjectIdentifier()
    {
        return (NetMeshObjectIdentifier) super.createMeshObjectIdentifier();
    }

    /**
     * Create an identifier for a MeshObject at held locally at this MeshBase.
     *
     * @param raw the identifier String
     * @return the created DefaultAnetMeshObjectIdentifier
     * @throws URISyntaxException a parsing error occurred
     */
    @Override
    public DefaultAnetMeshObjectIdentifier fromExternalForm(
            String raw )
        throws
            URISyntaxException
    {
        DefaultAnetMeshObjectIdentifier ret = fromExternalForm( theMeshBaseIdentifier, raw  );
        return ret;
    }

    /**
     * Re-construct a DefaultAnetMeshObjectIdentifier from an external form.
     *
     * @param contextIdentifier identifier of the NetMeshBase relative to which the external form is to be evaluated
     * @param raw the external form of the DefaultAnetMeshObjectIdentifier
     * @return the created DefaultAnetMeshObjectIdentifier
     * @throws URISyntaxException thrown if a syntax error was encountered during parsing
     */
    public DefaultAnetMeshObjectIdentifier fromExternalForm(
            NetMeshBaseIdentifier contextIdentifier,
            String                raw )
        throws
            URISyntaxException
    {
        if( raw == null ) {
            return null;
        }
        
        NetMeshBaseIdentifier meshBase;
        String                local;
        
        DefaultAnetMeshObjectIdentifier ret;
        
        int hash = raw.indexOf( DefaultAnetMeshObjectIdentifier.SEPARATOR );
        if( hash == 0 ) {
            meshBase = contextIdentifier;
            local    = raw.substring( hash+1 );
        } else if( hash > 0 ) {
            meshBase = theMeshBaseIdentifierFactory.fromExternalForm( raw.substring( 0, hash ));
            local    = raw.substring( hash+1 );
        } else if( raw.indexOf( '.' ) >= 0 ) {
            meshBase = theMeshBaseIdentifierFactory.fromExternalForm( raw );
            local    = null;
        } else {
            meshBase = contextIdentifier;
            local    = raw;
        }
        ret = DefaultAnetMeshObjectIdentifier.create(
                meshBase,
                local );
        return ret;
    }

    /**
     * Recreate a NetMeshObjectIdentifier from an external form. Be lenient about syntax and
     * attempt to interpret what the user meant when entering an invalid or incomplete
     * raw String.
     *
     * @param raw the external form
     * @return the created MeshObjectIdentifier
     * @throws URISyntaxException thrown if a parsing error occurred
     */
    @Override
    public NetMeshObjectIdentifier guessFromExternalForm(
            String raw )
        throws
            URISyntaxException
    {
        // FIXME? Can we make this smarter?

        DefaultAnetMeshObjectIdentifier ret = fromExternalForm( theMeshBaseIdentifier, raw  );
        return ret;
    }
    
    /**
     * Convert this StringRepresentation back to an Identifier.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created MeshObjectIdentifier
     * @throws URISyntaxException a parsing error occurred
     */
    @Override
    public DefaultAnetMeshObjectIdentifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            URISyntaxException
    {
        try {
            representation.parseEntry( DefaultAnetMeshObjectIdentifier.class, DefaultAnetMeshObjectIdentifier.HOME_DEFAULT_ENTRY, s );
            return fromExternalForm( "" );

        } catch( StringifierException ex ) {
            // that wasn't it ...
        }
        try {
            Object [] found = representation.parseEntry( DefaultAnetMeshObjectIdentifier.class, DefaultAnetMeshObjectIdentifier.DEFAULT_ENTRY, s );

            DefaultAnetMeshObjectIdentifier ret;
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
     * Determine the MeshObjectIdentifier of the Home Object.
     *
     * @return the MeshObjectIdentifier
     */
    @Override
    public DefaultAnetMeshObjectIdentifier getHomeMeshObjectIdentifier()
    {
        return NET_HOME_OBJECT;
    }
    

    /**
     * Identifies the NetMeshBase to which this factory belongs.
     */
    protected NetMeshBaseIdentifier theMeshBaseIdentifier;
    
    /**
     * Factory for NetMeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory;
    
    /**
     * The home object identifier.
     */
    public final DefaultAnetMeshObjectIdentifier NET_HOME_OBJECT;

    /**
     * This subclass of DefaultAnetMeshObjectIdentifier is only used for identifiers
     * of home objects.
     */
    private static class HomeObject
            extends
                DefaultAnetMeshObjectIdentifier
    {
        /**
         * Constructor.
         * 
         * @param meshBaseIdentifier the NetMeshBaseIdentifier of the owning NetMeshBase
         */
        public HomeObject(
                NetMeshBaseIdentifier meshBaseIdentifier )
        {
            super( meshBaseIdentifier, null );
        }
    }
}
