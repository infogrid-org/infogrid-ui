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
import org.infogrid.mesh.MeshObject;

/**
 * Knows how to determineCurrentLabel a node in the GraphTree.
 */
public interface GraphTreeNodeRenderer
{
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
            String             stringRepresentation );

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
            boolean            colloquial );
}
