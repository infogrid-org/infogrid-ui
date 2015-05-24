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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet.net;

import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.viewlet.MeshObjectsToView;

/**
 * Extends MeshObjectsToView to be able to express that a Proxy shall be viewed instead.
 */
public interface NetMeshObjectsToView
        extends
            MeshObjectsToView
{
    /**
     * Obtain the subject that the Viewlet is supposed to view.
     *
     * @return the subject
     */
    @Override
    public NetMeshObject getSubject();

    /**
     * Obtain the MeshBase from which the MeshObjects are taken.
     *
     * @return the MeshBase
     */
    @Override
    public NetMeshBase getMeshBase();

    /**
     * Obtain the Proxy that shall be viewed instead of the regular subject, if any.
     *
     * @return the Proxy
     */
    public Proxy getRequestedProxy();
}
