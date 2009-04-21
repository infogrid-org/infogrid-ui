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

package org.infogrid.jee.viewlet.servlet.net;

import java.util.HashMap;
import java.util.Map;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.jee.rest.net.DefaultNetRestfulRequest;
import org.infogrid.jee.rest.net.NetRestfulRequest;
import org.infogrid.jee.viewlet.servlet.ViewletDispatcherServlet;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.model.traversal.TraversalDictionary;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.text.StringRepresentationParseException;

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
     * Determine the Viewlet parameters for the MeshObjectsToView.
     * Factored out to make overriding easier in subclasses.
     *
     * @param restful the incoming RESTful request
     * @param traversalDict the TraversalDictionary to use
     * @return the created Map, or null
     * @throws MeshObjectAccessException thrown if one or more MeshObjects could not be accessed
     * @throws StringRepresentationParseException thrown if a URI parsing error occurred
     * @throws NotPermittedException thrown if an attempted operation was not permitted
     */
    @Override
    protected Map<String,Object> determineViewletParameters(
            RestfulRequest       restful,
            TraversalDictionary  traversalDict )
        throws
            MeshObjectAccessException,
            StringRepresentationParseException,
            NotPermittedException
    {
        Map<String,Object> viewletPars = super.determineViewletParameters( restful, traversalDict );

        NetRestfulRequest realRestful = (NetRestfulRequest) restful;

        if( realRestful.determineRequestedProxyIdentifier() != null ) {
            if( viewletPars == null ) {
                viewletPars = new HashMap<String,Object>();
            }
            viewletPars.put( PROXY_IDENTIFIER_NAME, realRestful.determineRequestedProxyIdentifier() );
            viewletPars.put( PROXY_NAME,            realRestful.determineRequestedProxy() );
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
