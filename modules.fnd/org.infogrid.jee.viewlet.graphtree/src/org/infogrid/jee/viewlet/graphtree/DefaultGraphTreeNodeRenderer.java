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

package org.infogrid.jee.viewlet.graphtree;

import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.JeeFormatter;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.mesh.MeshObject;
import org.infogrid.util.text.HasStringRepresentation;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationDirectorySingleton;

/**
 * The default renderer for GraphTree nodes.
 */
public class DefaultGraphTreeNodeRenderer
    implements
        GraphTreeNodeRenderer
{
    /**
     * Factory method.
     *
     * @return the created DefaultGraphTreeNodeRenderer
     */
    public static DefaultGraphTreeNodeRenderer create()
    {
        if( theSingleton == null ) {
            theSingleton = new DefaultGraphTreeNodeRenderer();
        }
        return theSingleton;
    }

    /**
     * Render a node, represented as a MeshObject.
     *
     * @param node the MeshObject
     * @param request the incoming request
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to be shown
     */
    public String render(
            MeshObject         node,
            HttpServletRequest request,
            String             stringRepresentation )
    {
        if( node == null ) {
            return "";
        }
        String               sanitized = JeeFormatter.determineStringRepresentationString( stringRepresentation );
        StringRepresentation rep       = StringRepresentationDirectorySingleton.getSingleton().get( sanitized );

        StringRepresentationContext context = (StringRepresentationContext) request.getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        String ret = node.toStringRepresentation( rep, context, HasStringRepresentation.UNLIMITED_LENGTH );
        return ret;
    }

    /**
     * Singleton instance of this class.
     */
    protected static DefaultGraphTreeNodeRenderer theSingleton;
}
