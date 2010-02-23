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

package org.infogrid.jee.viewlet.servlet.net;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.infogrid.rest.net.DefaultNetRestfulRequest;
import org.infogrid.rest.net.NetRestfulRequest;
import org.infogrid.jee.viewlet.servlet.ViewletDispatcherServlet;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseNameServer;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * Extends ViewletDispatcherServlet to also be able to render Shadows and Proxies.
 */
public class NetViewletDispatcherServlet
        extends
            ViewletDispatcherServlet
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Construct a RestfulRequest object that is suitable to the URL-to-MeshObject mapping
     * applied by this application.
     *
     * @param lidRequest the incoming request
     * @param defaultMeshBaseIdentifier the identifier of the default MeshBase
     * @param c the Context
     * @return the created RestfulRequest
     */
    @Override
    protected NetRestfulRequest createRestfulRequest(
            SaneRequest        lidRequest,
            MeshBaseIdentifier defaultMeshBaseIdentifier,
            Context            c )
    {
        @SuppressWarnings("unchecked") // this is a hack, but the inheritance / generics structure isn't quite optimal
        DefaultNetRestfulRequest ret = DefaultNetRestfulRequest.create(
                lidRequest,
                (NetMeshBaseIdentifier) defaultMeshBaseIdentifier,
                c.findContextObjectOrThrow( NetMeshBaseIdentifierFactory.class ),
                (NetMeshBaseNameServer<NetMeshBaseIdentifier,NetMeshBase>) c.findContextObjectOrThrow( NetMeshBaseNameServer.class ));
        return ret;
    }

    /**
     * Determine the Viewlet parameters for the MeshObjectsToView.
     * Factored out to make overriding easier in subclasses.
     *
     * @param restful the incoming RESTful request
     * @param traversalDict the TraversalTranslator to use
     * @return the created Map, or null
     * @throws MeshObjectAccessException thrown if one or more MeshObjects could not be accessed
     * @throws ParseException thrown if a parsing problem occurred
     * @throws NotPermittedException thrown if an attempted operation was not permitted
     */
    @Override
    protected Map<String,Object[]> determineViewletParameters(
            RestfulRequest       restful,
            TraversalTranslator  traversalDict )
        throws
            MeshObjectAccessException,
            ParseException,
            NotPermittedException
    {
        Map<String,Object[]> viewletPars = super.determineViewletParameters( restful, traversalDict );

        NetRestfulRequest realRestful = (NetRestfulRequest) restful;

        if( realRestful.determineRequestedProxyIdentifier() != null ) {
            if( viewletPars == null ) {
                viewletPars = new HashMap<String,Object[]>();
            }
            viewletPars.put( PROXY_IDENTIFIER_NAME, new NetMeshBaseIdentifier[] { realRestful.determineRequestedProxyIdentifier() } );
            viewletPars.put( PROXY_NAME,            new Proxy[] {                 realRestful.determineRequestedProxy() } );
        }
        return viewletPars;
    }
    
    /**
     * Name of the viewlet parameter that contains the Proxy identifier.
     */
    public static final String PROXY_IDENTIFIER_NAME = "proxy-identifier";
    
    /**
     * Name of the viewlet parameter that contains the Proxy.
     */
    public static final String PROXY_NAME = "proxy";
}
