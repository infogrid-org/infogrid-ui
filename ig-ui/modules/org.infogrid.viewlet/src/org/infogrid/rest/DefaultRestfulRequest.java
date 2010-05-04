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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.rest;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.DefaultMeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.util.UnknownSymbolParseException;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * Default implementation of RestfulRequest.
 */
public class DefaultRestfulRequest
        extends
            AbstractRestfulRequest
        implements
            CanBeDumped
{
    /**
     * Factory method.
     * 
     * @param lidRequest the underlying incoming SaneRequest
     * @param defaultMeshBaseIdentifier the identifier, in String form, of the default MeshBase
     * @param meshBaseNameServer the name server with which to look up MeshBases
     * @return the created DefaultRestfulRequest
     */
    public static DefaultRestfulRequest create(
            SaneRequest                                     lidRequest,
            MeshBaseIdentifier                              defaultMeshBaseIdentifier,
            MeshBaseNameServer<MeshBaseIdentifier,MeshBase> meshBaseNameServer )
    {
        return create( lidRequest, defaultMeshBaseIdentifier, null, meshBaseNameServer );
    }

    /**
     * Factory method.
     *
     * @param lidRequest the underlying incoming SaneRequest
     * @param defaultMeshBaseIdentifier the identifier, in String form, of the default MeshBase
     * @param meshBaseIdentifierFactory the factory to use to create MeshBaseIdentifiers
     * @param meshBaseNameServer the name server with which to look up MeshBases
     * @return the created DefaultRestfulRequest
     */
    public static DefaultRestfulRequest create(
            SaneRequest                                     lidRequest,
            MeshBaseIdentifier                              defaultMeshBaseIdentifier,
            MeshBaseIdentifierFactory                       meshBaseIdentifierFactory,
            MeshBaseNameServer<MeshBaseIdentifier,MeshBase> meshBaseNameServer )
    {
        if( meshBaseIdentifierFactory == null ) {
            meshBaseIdentifierFactory = DefaultMeshBaseIdentifierFactory.create();
        }
        return new DefaultRestfulRequest( lidRequest, defaultMeshBaseIdentifier, meshBaseIdentifierFactory, meshBaseNameServer );
    }

    /**
     * Constructor.
     * 
     * @param lidRequest the underlying incoming SaneRequest
     * @param defaultMeshBaseIdentifier the identifier, in String form, of the default MeshBase
     * @param meshBaseIdentifierFactory the factory for MeshBaseIdentifiers if any are specified in the request
     * @param meshBaseNameServer the name server with which to look up MeshBases
     */
    protected DefaultRestfulRequest(
            SaneRequest                                     lidRequest,
            MeshBaseIdentifier                              defaultMeshBaseIdentifier,
            MeshBaseIdentifierFactory                       meshBaseIdentifierFactory,
            MeshBaseNameServer<MeshBaseIdentifier,MeshBase> meshBaseNameServer )
    {
        super( lidRequest, defaultMeshBaseIdentifier );

        theMeshBaseIdentifierFactory = meshBaseIdentifierFactory;
        theMeshBaseNameServer        = meshBaseNameServer;
    }

    /**
     * Internal method to calculate the data.
     * 
     * @throws MeshObjectAccessException thrown if the requested MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller did not have the permission to perform this operation
     * @throws ParseException thrown if the request URI could not be parsed
     */
    protected void calculate()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                ParseException
    {
        String relativeBaseUrl = theSaneRequest.getRelativeBaseUri();
        if( relativeBaseUrl.startsWith( theSaneRequest.getContextPath() )) {
            String trailer = relativeBaseUrl.substring( theSaneRequest.getContextPath().length() );
            if( trailer.startsWith( "/" )) {
                trailer = trailer.substring( 1 );
            }
            trailer = HTTP.decodeUrl( trailer );

            String meshBaseIdentifierString;
            String meshObjectIdentifierString;
            
            Matcher m = theUrlPattern.matcher( trailer );
            if( m.matches() ) {
                meshBaseIdentifierString   = m.group( 2 );
                meshObjectIdentifierString = m.group( 3 );

            } else {
                meshBaseIdentifierString   = null;
                meshObjectIdentifierString = trailer;
            }
            if( meshObjectIdentifierString == null ) {
                meshObjectIdentifierString = "";
            }
            if( meshBaseIdentifierString != null ) {
                theRequestedMeshBaseIdentifier = theMeshBaseIdentifierFactory.guessFromExternalForm( meshBaseIdentifierString );
            } else {
                theRequestedMeshBaseIdentifier = theDefaultMeshBaseIdentifier;
            }
            
            MeshBase mb = theMeshBaseNameServer.get( theRequestedMeshBaseIdentifier );

            if( mb == null ) {
                throw new UnknownSymbolParseException( theSaneRequest.getAbsoluteFullUri(), -1, meshBaseIdentifierString );
            }

            theRequestedMeshObjectIdentifier = mb.getMeshObjectIdentifierFactory().fromExternalForm( meshObjectIdentifierString );
            theRequestedMeshObject           = mb.accessLocally( theRequestedMeshObjectIdentifier );
            
        } else {
            throw new IllegalArgumentException( "Cannot process incoming relative URI " + relativeBaseUrl + " that is outside of context path " + theSaneRequest.getContextPath() );
        }
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theRequestedMeshBaseIdentifier",
                    "theRequestedMeshObjectIdentifier",
                    "getRequestedTraversalParameters()",
                    "theRequestedViewletClass",
                    "theRequestedMimeType"
                },
                new Object[] {
                    theRequestedMeshBaseIdentifier,
                    theRequestedMeshObjectIdentifier,
                    getRequestedTraversalParameters(),
                    theRequestedViewletTypeName,
                    theRequestedMimeType
                });
    }

    /**
     * The factory for MeshBaseIdentifiers.
     */
    protected MeshBaseIdentifierFactory theMeshBaseIdentifierFactory;

    /**
     * The name server with which to look up MeshBases.
     */
    protected MeshBaseNameServer<MeshBaseIdentifier,MeshBase> theMeshBaseNameServer;

    /**
     * The pattern for URL parsing after the context path.
     */
    protected static final Pattern theUrlPattern = Pattern.compile( "^(\\[meshbase=([^\\]]*)\\])?(.*)$" );
}
