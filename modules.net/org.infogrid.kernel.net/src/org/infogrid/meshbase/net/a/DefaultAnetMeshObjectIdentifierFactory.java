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

package org.infogrid.meshbase.net.a;

import java.io.File;
import java.net.URI;
import java.net.URL;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.a.DefaultAnetMeshObjectIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.a.DefaultAMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParseException;

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
        theMeshBaseIdentifier        = meshBaseIdentifier;
        theMeshBaseIdentifierFactory = meshBaseIdentifierFactory;

        NET_HOME_OBJECT = new HomeObject( this, theMeshBaseIdentifier );
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
     * @throws StringRepresentationParseException a parsing error occurred
     */
    @Override
    public DefaultAnetMeshObjectIdentifier fromExternalForm(
            String raw )
        throws
            StringRepresentationParseException
    {
        DefaultAnetMeshObjectIdentifier ret = obtain( theMeshBaseIdentifier, raw, false );
        return ret;
    }

    /**
     * Re-construct a DefaultAnetMeshObjectIdentifier from an external form.
     *
     * @param contextIdentifier identifier of the NetMeshBase relative to which the external form is to be evaluated
     * @param raw the external form of the DefaultAnetMeshObjectIdentifier
     * @return the created DefaultAnetMeshObjectIdentifier
     * @throws StringRepresentationParseException thrown if a syntax error was encountered during parsing
     */
    public DefaultAnetMeshObjectIdentifier fromExternalForm(
            NetMeshBaseIdentifier contextIdentifier,
            String                raw )
        throws
            StringRepresentationParseException
    {
        return obtain( contextIdentifier, raw, false );
    }

    /**
     * Re-construct a DefaultAnetMeshObjectIdentifier from an external form.
     *
     * @param contextIdentifier identifier of the NetMeshBase relative to which the external form is to be evaluated
     * @param raw the external form of the DefaultAnetMeshObjectIdentifier
     * @param guess if true, attempt to guess the protocol if none was given
     * @return the created DefaultAnetMeshObjectIdentifier
     * @throws StringRepresentationParseException thrown if a syntax error was encountered during parsing
     */
    protected DefaultAnetMeshObjectIdentifier obtain(
            NetMeshBaseIdentifier contextIdentifier,
            String                raw,
            boolean               guess )
        throws
            StringRepresentationParseException
    {
        if( raw == null ) {
            return new HomeObject( this, contextIdentifier );
        }
        
        NetMeshBaseIdentifier meshBase;
        String                local;
        
        DefaultAnetMeshObjectIdentifier ret;
        
        int hash = raw.indexOf( DefaultAnetMeshObjectIdentifier.SEPARATOR );
        if( hash == 0 ) {
            meshBase = contextIdentifier;
            local    = raw.substring( hash+1 );
        } else if( hash > 0 ) {
            if( guess ) {
                meshBase = theMeshBaseIdentifierFactory.guessFromExternalForm( raw.substring( 0, hash ));
            } else {
                meshBase = theMeshBaseIdentifierFactory.fromExternalForm( raw.substring( 0, hash ));
            }
            local = raw.substring( hash+1 );
        } else if( treatAsGlobalIdentifier( raw )) {
            if( guess ) {
                meshBase = theMeshBaseIdentifierFactory.guessFromExternalForm( raw );
            } else {
                meshBase = theMeshBaseIdentifierFactory.fromExternalForm( raw );
            }
            local = null;
        } else {
            meshBase = contextIdentifier;
            local    = raw;
        }
        ret = DefaultAnetMeshObjectIdentifier.create(
                this,
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
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    @Override
    public DefaultAnetMeshObjectIdentifier guessFromExternalForm(
            String raw )
        throws
            StringRepresentationParseException
    {
        return obtain( theMeshBaseIdentifier, raw, true );
    }
    
    /**
     * Factory method.
     *
     * @param file the local File whose NetMeshObjectIdentifier we obtain
     * @return the created NetMeshObjectIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public DefaultAnetMeshObjectIdentifier obtain(
            File file )
        throws
            StringRepresentationParseException
    {
        return obtain( file.toURI() );
    }

    /**
     * Factory method.
     *
     * @param url the URL whose NetMeshObjectIdentifier we obtain
     * @return the created NetMeshObjectIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public DefaultAnetMeshObjectIdentifier obtain(
            URL url )
        throws
            StringRepresentationParseException
    {
        return obtain( theMeshBaseIdentifier, url.toExternalForm(), false );
    }

    /**
     * Factory method.
     *
     * @param uri the URI whose NetMeshObjectIdentifier we obtain
     * @return the created NetMeshObjectIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public DefaultAnetMeshObjectIdentifier obtain(
            URI uri )
        throws
            StringRepresentationParseException
    {
        return obtain( theMeshBaseIdentifier, uri.toASCIIString(), false );
    }

    /**
     * Determine whether a given String is to be treated as a global identifier. This
     * method encodes our policy of the String is ambiguous.
     *
     * @param raw the String
     * @return true if the String is to be treated as a global identifier
     */
    public boolean treatAsGlobalIdentifier(
            String raw )
    {
        if( raw.indexOf( '.' ) >= 0 || raw.indexOf( "localhost" ) >= 0 ) {
            return true;
        }
        try {
            MeshBaseIdentifier found = theMeshBaseIdentifierFactory.fromExternalForm( raw );
            return true;

        } catch( StringRepresentationParseException ex ) {
            // ignore
        }
        return false;
    }

    /**
     * Convert this StringRepresentation back to an Identifier.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created MeshObjectIdentifier
     * @throws StringRepresentationParseException a parsing error occurred
     */
    @Override
    public DefaultAnetMeshObjectIdentifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            StringRepresentationParseException
    {
        String [] entriesToTry1 = {
                DefaultAnetMeshObjectIdentifier.DEFAULT_MESH_BASE_HOME_ENTRY,
                DefaultAnetMeshObjectIdentifier.NON_DEFAULT_MESH_BASE_HOME_ENTRY };

        Throwable firstException = null;

        for( String entry : entriesToTry1 ) {

            try {
                representation.parseEntry( DefaultAnetMeshObjectIdentifier.class, entry, s, this );
                return fromExternalForm( "" );

            } catch( StringRepresentationParseException ex ) {
                // that wasn't it ...
                if( firstException == null ) {
                    firstException = ex;
                }
            }
        }

        String [] entriesToTry2 = {
                DefaultAnetMeshObjectIdentifier.DEFAULT_MESH_BASE_ENTRY,
                DefaultAnetMeshObjectIdentifier.NON_DEFAULT_MESH_BASE_ENTRY };

        for( String entry : entriesToTry2 ) {
            try {
                Object [] found = representation.parseEntry( DefaultAnetMeshObjectIdentifier.class, entry, s, this );

                if( found.length == 1 ) {
                    DefaultAnetMeshObjectIdentifier ret = obtain( theMeshBaseIdentifier, (String) found[0], true );
                    return ret;
                }

            } catch( StringRepresentationParseException ex ) {
                // that wasn't it ...
                if( firstException == null ) {
                    firstException = ex;
                }

            } catch( ClassCastException ex ) {
                // that wasn't it ...
                if( firstException == null ) {
                    firstException = ex;
                }
            }
        }
        throw new StringRepresentationParseException( s, null, firstException );
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
     * Determine the Identifier of the Home Object in a NetMeshBase with the given
     * NetMeshBaseIdentifier.
     *
     * @param mbIdentifier the NetMeshBaseIdentifier of the NetMeshBase
     * @return the Identifier
     */
    public NetMeshObjectIdentifier getHomeMeshObjectIdentifierFor(
            NetMeshBaseIdentifier mbIdentifier )
    {
        return new HomeObject( this, mbIdentifier );
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
         * @param factory the DefaultAnetMeshObjectIdentifierFactory that created this identifier
         * @param meshBaseIdentifier the NetMeshBaseIdentifier of the owning NetMeshBase
         */
        public HomeObject(
                DefaultAnetMeshObjectIdentifierFactory factory,
                NetMeshBaseIdentifier                  meshBaseIdentifier )
        {
            super( factory, meshBaseIdentifier, null );
        }
    }
}
