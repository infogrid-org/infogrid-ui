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

package org.infogrid.jee.viewlet;

import org.infogrid.util.FactoryException;
import org.infogrid.util.http.SaneUrl;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.MeshObjectsToViewFactory;

/**
 * A factory for JeeMeshObjectsToView objects.
 * @author jernst
 */
public interface JeeMeshObjectsToViewFactory
    extends
        MeshObjectsToViewFactory
{
    /**
     * Create a MeshObjectsToView that corresponds to the request(s) encloded in this SaneRequest.
     *
     * @param request the SaneUrl
     * @return the created MeshObjectsToView
     * @throws FactoryException thrown if the MeshObjectsToView could not be created
     */
    public MeshObjectsToView obtainFor(
            SaneUrl request )
        throws
            FactoryException;
}
