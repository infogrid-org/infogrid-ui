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
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.logging.Log;
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
    private static final Log log = Log.getLogInstance( DefaultGraphTreeNodeRenderer.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @return the created DefaultGraphTreeNodeRenderer
     */
    public static DefaultGraphTreeNodeRenderer create()
    {
        if( theSingleton == null ) {
            theSingleton = new DefaultGraphTreeNodeRenderer( null );
        }
        return theSingleton;
    }

    /**
     * Factory method.
     *
     * @param linkTraversalSpec the TraversalSpecification, if any, to use to determine the MeshObject to link to
     * @return the created DefaultGraphTreeNodeRenderer
     */
    public static DefaultGraphTreeNodeRenderer create(
            TraversalSpecification linkTraversalSpec )
    {
        // don't return the singleton
        DefaultGraphTreeNodeRenderer ret = new DefaultGraphTreeNodeRenderer( linkTraversalSpec );
        return ret;
    }

    /**
     * Constructor.
     *
     * @param linkTraversalSpec the TraversalSpecification, if any, to use to determine the MeshObject to link to
     */
    protected DefaultGraphTreeNodeRenderer(
            TraversalSpecification linkTraversalSpec )
    {
        theLinkTraversalSpec = linkTraversalSpec;
    }

    /**
     * For a given MeshObject, determine which MeshObject to link to. Returning null means
     * that no link shall be created.
     *
     * @param node the MeshObject
     * @param request the incoming request
     * @param stringRepresentation the StringRepresentation to use
     * @return the MeshObject to link to
     */
    public MeshObject determineMeshObjectToLinkTo(
            MeshObject         node,
            HttpServletRequest request,
            String             stringRepresentation )
    {
        MeshObject ret = null;
        try {
            if( theLinkTraversalSpec != null ) {
                ret = node.traverse( theLinkTraversalSpec ).getSingleMember();

            }
            if( ret == null ) {
                ret = node;
            }
        } catch( IllegalStateException ex ) {
            if( log.isDebugEnabled() ) {
                log.debug( "Cannot find single destination of " + theLinkTraversalSpec + " from " + node + ", results in " + node.traverse( theLinkTraversalSpec ));
            }
        }
        return ret;
    }

    /**
     * Determine the label to be shown to the user for a given
     * MeshObject in the GraphTreeViewlet.
     *
     * @param node the MeshObject
     * @param request the incoming request
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @return the String to be shown
     */
    public String determineCurrentLabel(
            MeshObject         node,
            HttpServletRequest request,
            String             stringRepresentation,
            int                maxLength,
            boolean            colloquial )
    {
        if( node == null ) {
            return "";
        }
        String               sanitized = JeeFormatter.determineStringRepresentationString( stringRepresentation );
        StringRepresentation rep       = StringRepresentationDirectorySingleton.getSingleton().get( sanitized );

        StringRepresentationContext context = (StringRepresentationContext) request.getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        String ret = node.toStringRepresentation( rep, context, maxLength, colloquial );
        return ret;
    }

    /**
     * The TraversalSpecification, if any, to use to determine the MeshObject to link to.
     */
    protected TraversalSpecification theLinkTraversalSpec;

    /**
     * Singleton instance of this class.
     */
    protected static DefaultGraphTreeNodeRenderer theSingleton;
}
