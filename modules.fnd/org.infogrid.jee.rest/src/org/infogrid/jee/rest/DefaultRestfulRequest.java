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

package org.infogrid.jee.rest;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.DefaultMeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneRequest;

/**
 * Default implementation of RestfulRequest.
 */
public class DefaultRestfulRequest
        extends
            AbstractRestfulRequest
{
    /**
     * Factory method.
     * 
     * @param lidRequest the underlying incoming SaneRequest
     * @param contextPath the context path of the JEE application
     * @param defaultMeshBaseIdentifier the identifier, in String form, of the default MeshBase
     * @return the created DefaultRestfulRequest
     */
    public static DefaultRestfulRequest create(
            SaneRequest lidRequest,
            String      contextPath,
            String      defaultMeshBaseIdentifier )
    {
        MeshBaseIdentifierFactory meshBaseIdentifierFactory
                = InfoGridWebApp.getSingleton().getApplicationContext().findContextObject( MeshBaseIdentifierFactory.class );
        
        if( meshBaseIdentifierFactory == null ) {
            meshBaseIdentifierFactory = DefaultMeshBaseIdentifierFactory.create();
        }
        return new DefaultRestfulRequest( lidRequest, contextPath, defaultMeshBaseIdentifier, meshBaseIdentifierFactory );
    }

    /**
     * Constructor.
     * 
     * @param lidRequest the underlying incoming SaneRequest
     * @param contextPath the context path of the JEE application
     * @param defaultMeshBaseIdentifier the identifier, in String form, of the default MeshBase
     * @param meshBaseIdentifierFactory the factory for MeshBaseIdentifiers if any are specified in the request
     */
    protected DefaultRestfulRequest(
            SaneRequest               lidRequest,
            String                    contextPath,
            String                    defaultMeshBaseIdentifier,
            MeshBaseIdentifierFactory meshBaseIdentifierFactory )
    {
        super( lidRequest, contextPath );
        
        theDefaultMeshBaseIdentifier = defaultMeshBaseIdentifier;
        theMeshBaseIdentifierFactory = meshBaseIdentifierFactory;
    }

    /**
     * Internal method to calculate the data.
     * 
     * @throws MeshObjectAccessException thrown if the requested MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller did not have the permission to perform this operation
     * @throws URISyntaxException thrown if the request URI could not be parsed
     */
    protected void calculate()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                URISyntaxException
    {
        String relativeBaseUrl = theSaneRequest.getRelativeBaseUri();
        if( relativeBaseUrl.startsWith( theContextPath )) {
            String trailer = relativeBaseUrl.substring( theContextPath.length() );
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
            if( meshBaseIdentifierString == null ) {
                meshBaseIdentifierString = theDefaultMeshBaseIdentifier;
            }
            if( meshObjectIdentifierString == null ) {
                meshObjectIdentifierString = "";
            }
            theRequestedMeshBaseIdentifier = theMeshBaseIdentifierFactory.guessFromExternalForm( meshBaseIdentifierString );
            
            @SuppressWarnings( "unchecked" )
            MeshBaseNameServer<MeshBaseIdentifier,MeshBase> meshBaseNameServer
                    = InfoGridWebApp.getSingleton().getApplicationContext().findContextObjectOrThrow( 
                            MeshBaseNameServer.class );
            
            MeshBase mb = meshBaseNameServer.get( theRequestedMeshBaseIdentifier );

            if( mb == null ) {
                throw new URISyntaxException( meshBaseIdentifierString, "Cannot find a MeshBase with this identifier" );
            }

            theRequestedMeshObjectIdentifier = mb.getMeshObjectIdentifierFactory().fromExternalForm( meshObjectIdentifierString );
            theRequestedMeshObject           = mb.accessLocally( theRequestedMeshObjectIdentifier );
            
        } else {
            throw new IllegalArgumentException( "Cannot process incoming relative URI " + relativeBaseUrl + " that is outside of context path " + theContextPath );
        }
    }
    
    /**
     * The identifier of the default MeshBase.
     */
    protected String theDefaultMeshBaseIdentifier;

    /**
     * The factory for MeshBaseIdentifiers.
     */
    protected MeshBaseIdentifierFactory theMeshBaseIdentifierFactory;

    /**
     * The pattern for URL parsing after the context path.
     */
    protected static final Pattern theUrlPattern = Pattern.compile( "^(\\[meshbase=([^\\]]*)\\])?(.*)$" );
}
