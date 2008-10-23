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

package org.infogrid.jee.rest.net;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.AbstractRestfulRequest;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseNameServer;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;

/**
 * Default implementation of Net RestfulRequest.
 */
public class DefaultNetRestfulRequest
        extends
            AbstractRestfulRequest
        implements
            NetRestfulRequest
{
    private static final Log log = Log.getLogInstance( DefaultNetRestfulRequest.class ); // our own, private logger

    /**
     * Factory method.
     * 
     * @param lidRequest the incoming request
     * @param contextPath the application's JEE context path
     * @param defaultMeshBaseIdentifier the identifier, in String form, of the default MeshBase
     * @return the created DefaultNetRestfulRequest
     */
    public static DefaultNetRestfulRequest create(
            SaneServletRequest lidRequest,
            String             contextPath,
            String             defaultMeshBaseIdentifier )
    {
        NetMeshBaseIdentifierFactory meshBaseIdentifierFactory
                = InfoGridWebApp.getSingleton().getApplicationContext().findContextObject( NetMeshBaseIdentifierFactory.class );
        
        if( meshBaseIdentifierFactory == null ) {
            meshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.create();
        }
        return new DefaultNetRestfulRequest( lidRequest, contextPath, defaultMeshBaseIdentifier, meshBaseIdentifierFactory );
    }

    /**
     * Constructor.
     * 
     * @param lidRequest the incoming request
     * @param contextPath the application's JEE context path
     * @param defaultMeshBaseIdentifier the identifier, in String form, of the default MeshBase
     * @param meshBaseIdentifierFactory the factory for MeshBaseIdentifiers if any are specified in the request
     */
    protected DefaultNetRestfulRequest(
            SaneServletRequest           lidRequest,
            String                       contextPath,
            String                       defaultMeshBaseIdentifier,
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
    {
        super( lidRequest, contextPath );
        
        theDefaultMeshBaseIdentifier = defaultMeshBaseIdentifier;
        theMeshBaseIdentifierFactory = meshBaseIdentifierFactory;
    }

    /**
     * Internal method to calculate the data.
     * 
     * @throws MeshObjectAccessException thrown if a MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller was not permitted to perform this operation
     * @throws URISyntaxException thrown if the MeshBaseIdentifier passed into the constructor could not be parsed
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
            String proxyIdentifierString;
            String meshObjectIdentifierString;
            
            Matcher m = theUrlPattern.matcher( trailer );
            if( m.matches() ) {
                meshBaseIdentifierString   = m.group( 2 );
                proxyIdentifierString      = m.group( 4 );
                meshObjectIdentifierString = m.group( 5 );

            } else {
                meshBaseIdentifierString   = null;
                proxyIdentifierString      = null;
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
            NetMeshBaseNameServer<NetMeshBaseIdentifier,NetMeshBase> meshBaseNameServer
                    = InfoGridWebApp.getSingleton().getApplicationContext().findContextObjectOrThrow( 
                            NetMeshBaseNameServer.class );

            NetMeshBase mb = meshBaseNameServer.get( (NetMeshBaseIdentifier) theRequestedMeshBaseIdentifier );

            if( mb == null ) {
                throw new URISyntaxException( meshBaseIdentifierString, "Cannot find a MeshBase with this identifier" );
            }
            
            theRequestedMeshObjectIdentifier = mb.getMeshObjectIdentifierFactory().fromExternalForm( meshObjectIdentifierString );
            theRequestedMeshObject           = mb.accessLocally( theRequestedMeshObjectIdentifier );

            if( proxyIdentifierString != null ) {
                theRequestedProxyIdentifier = theMeshBaseIdentifierFactory.guessFromExternalForm( proxyIdentifierString );
                theRequestedProxy           = mb.getProxyFor( theRequestedProxyIdentifier );
            }
            
        } else {
            throw new IllegalArgumentException( "Cannot process incoming relative URI " + relativeBaseUrl + " that is outside of context path " + theContextPath );
        }
    }

    /**
     * Determine the identifier of the requested Proxy, if any.
     * 
     * @return the NetMeshIdentifier
     * @throws MeshObjectAccessException thrown if a MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller was not permitted to perform this operation
     * @throws URISyntaxException thrown if the MeshBaseIdentifier passed into the constructor could not be parsed
     */
    public NetMeshBaseIdentifier determineRequestedProxyIdentifier()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                URISyntaxException
    {
        if( theRequestedProxyIdentifier == null ) {
            calculate();
        }
        return theRequestedProxyIdentifier;
    }
    
    /**
     * Determine the requested Proxy, if any.
     * 
     * @throws MeshObjectAccessException thrown if a MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller was not permitted to perform this operation
     * @throws URISyntaxException thrown if the MeshBaseIdentifier passed into the constructor could not be parsed
     * @return the Proxy
     */
    public Proxy determineRequestedProxy()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                URISyntaxException
    {
        if( theRequestedProxy == null ) {
            calculate();
        }
        return theRequestedProxy;
    }
    
    /**
     * The identifier of the default MeshBase.
     */
    protected String theDefaultMeshBaseIdentifier;

    /**
     * The identifier of the requested Proxy, if any.
     */
    protected NetMeshBaseIdentifier theRequestedProxyIdentifier;

    /**
     * The factory for MeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory;

    /**
     * The requested Proxy, if any.
     */
    protected Proxy theRequestedProxy;

    /**
     * The pattern for URL parsing after the context path.
     */
    protected static final Pattern theUrlPattern = Pattern.compile( "^(\\[meshbase=([^\\]]*)\\])?(\\[proxy=([^\\]]*)\\])?(.*)$" );
}
