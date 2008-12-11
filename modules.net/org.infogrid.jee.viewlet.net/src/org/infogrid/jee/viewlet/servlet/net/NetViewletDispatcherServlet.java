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

package org.infogrid.jee.viewlet.servlet.net;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.jee.rest.net.DefaultNetRestfulRequest;
import org.infogrid.jee.rest.net.NetRestfulRequest;
import org.infogrid.jee.viewlet.servlet.ViewletDispatcherServlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.model.traversal.TraversalDictionary;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;

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
     * @param context the context path of the application
     * @param defaultMeshBaseIdentifier String form of the identifier of the default MeshBase
     * @return the created RestfulRequest
     */
    @Override
    protected NetRestfulRequest createRestfulRequest(
            SaneRequest lidRequest,
            String      context,
            String      defaultMeshBaseIdentifier )
    {
        DefaultNetRestfulRequest ret = DefaultNetRestfulRequest.create(
                lidRequest,
                context,
                defaultMeshBaseIdentifier );
        return ret;
    }

    /**
     * Create a NetMeshObjectsToView object.
     * 
     * @param restful the incoming RESTful request
     * @param traversalDict the TraversalDictionary to use
     * @return the created MeshObjectsToView
     * @throws MeshObjectAccessException thrown if one or more MeshObjects could not be accessed
     * @throws CannotViewException thrown if a Viewlet could not view the requested MeshObjects
     * @throws URISyntaxException thrown if a URI parsing error occurred
     * @throws NotPermittedException thrown if an attempted operation was not permitted
     */
    @Override
    protected MeshObjectsToView createMeshObjectsToView(
            RestfulRequest       restful,
            TraversalDictionary  traversalDict )
        throws
            MeshObjectAccessException,
            CannotViewException,
            URISyntaxException,
            NotPermittedException
    {
        NetRestfulRequest    realRestful       = (NetRestfulRequest) restful;
        MeshObject           subject           = restful.determineRequestedMeshObject();
        MeshObjectIdentifier subjectIdentifier = restful.determineRequestedMeshObjectIdentifier();
        String               viewletClassName  = restful.getRequestedViewletClassName();
        String               traversalString   = restful.getRequestedTraversal();

        if( subject == null ) {
            throw new CannotViewException.NoSubject( subjectIdentifier );
        }

        TraversalSpecification traversal   = ( traversalDict != null ) ? traversalDict.translate( subject, traversalString ) : null;
        Map<String,Object>     viewletPars = null;
        if( realRestful.determineRequestedProxyIdentifier() != null ) {
            viewletPars = new HashMap<String,Object>();
            viewletPars.put( PROXY_IDENTIFIER_NAME, realRestful.determineRequestedProxyIdentifier() );
            viewletPars.put( PROXY_NAME,            realRestful.determineRequestedProxy() );
        }
        
        MeshObjectsToView ret = MeshObjectsToView.create(
                subject,
                null,
                viewletClassName,
                viewletPars,
                traversal );
        return ret;
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
