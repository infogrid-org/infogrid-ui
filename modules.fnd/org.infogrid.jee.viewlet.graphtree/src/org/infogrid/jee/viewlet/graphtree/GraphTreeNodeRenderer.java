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
 * Knows how to render a node in the GraphTree.
 */
public interface GraphTreeNodeRenderer
{
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
            String             stringRepresentation );
}
