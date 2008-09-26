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
import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.util.http.SaneRequest;

/**
 * Encapsulates parameter parsing according to InfoGrid REST conventions.
 */
public interface RestfulRequest
{
    /**
     * Obtain the context path of the application in the manner JEE does it,
     * ie as a relative URL.
     *
     * @return the context path
     * @see #getAbsoluteContextPath
     */
    public String getContextPath();

    /**
     * Obtain the fully-qualified context path of the application.
     * 
     * @return the context path
     * @see #getContextPath()
     */
    public String getAbsoluteContextPath();
    
    /**
     * Obtain the underlying SaneRequest.
     *
     * @return the SaneRequest
     */
    public SaneRequest getSaneRequest();

    /**
     * Determine the identifier of the requested MeshBase.
     * 
     * @return the MeshBaseIdentifier
     * @throws URISyntaxException thrown if the request URI could not be parsed
     */
    public MeshBaseIdentifier determineRequestedMeshBaseIdentifier()
            throws
                URISyntaxException;

    /**
     * Determine the identifier of the requested MeshObject.
     * 
     * @return the MeshObjectIdentifier
     * @throws URISyntaxException thrown if the request URI could not be parsed
     */
    public MeshObjectIdentifier determineRequestedMeshObjectIdentifier()
            throws
                URISyntaxException;

    /**
     * Determine the requested MeshObject.
     * 
     * @return the MeshObject, or null if not found
     * @throws MeshObjectAccessException thrown if the requested MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller did not have the permission to perform this operation
     * @throws URISyntaxException thrown if the request URI could not be parsed
     */
    public MeshObject determineRequestedMeshObject()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                URISyntaxException;

    /**
     * Determine the requested traversal, if any.
     * 
     * @return the traversal
     */
    public String getRequestedTraversal();
    
    /**
     * Obtain the name of the requested Viewlet, if any.
     *
     * @return class name of the requested Viewlet
     */
    public String getRequestedViewletClassName();
    
    /**
     * Obtain the requested MIME type, if any.
     * 
     * @return the requuested MIME type, if any
     */
    public String getRequestedMimeType();

    /**
     * Determine the underlying HttpServletRequest.
     * 
     * @return the delegate
     */
    public HttpServletRequest getDelegate();

    /**
     * Name of the request attribute that contains an instance of this type.
     */
    public static final String RESTFUL_REQUEST_ATTRIBUTE_NAME
            = SaneServletRequest.classToAttributeName( RestfulRequest.class );

    /**
     * Name of the LID format parameter.
     */
    public static final String LID_FORMAT_PARAMETER_NAME = "lid-format";

    /**
     * The prefix in the lid-format string that indicates the name of a viewlet.
     */
    public static final String VIEWLET_PREFIX = "viewlet:";
    
    /**
     * The prefix in the lid-format string that indicates the name of a MIME type.
     */
    public static final String MIME_PREFIX = "mime:";

    /**
     * Name of the LID traversal parameter.
     */
    public static final String LID_TRAVERSAL_PARAMETER_NAME = "lid-traversal";
    
    /**
     * The prefix in the lid-traversal string that indicates an Xpath.
     */
    public static final String XPATH_PREFIX = "xpath:";
}
