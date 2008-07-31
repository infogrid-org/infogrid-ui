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

package org.infogrid.meshbase.net.transaction;

import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

/**
 * This event indicates that a new NetMeshObject was created. If the NetMeshObject was created
 * and blessed at the same time, only this event (and no PropertyChangeEvents reflecting the blessing)
 * will be sent.
 */
public class NetMeshObjectCreatedEvent
        extends
            AbstractNetMeshObjectCreatedEvent
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param createdObject the NetMeshObject that was created
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     */
    public NetMeshObjectCreatedEvent(
            NetMeshBase           source,
            NetMeshBaseIdentifier sourceIdentifier,
            NetMeshObject         createdObject,
            NetMeshBaseIdentifier originIdentifier )
    {
        super( source, sourceIdentifier, createdObject, originIdentifier );
    }

    /**
     * Constructor.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param createdObject the NetMeshObject that was created, in externalized form
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     */
    public NetMeshObjectCreatedEvent(
            NetMeshBase               source,
            NetMeshBaseIdentifier     sourceIdentifier,
            ExternalizedNetMeshObject createdObject,
            NetMeshBaseIdentifier     originIdentifier )
    {
        super( source, sourceIdentifier, createdObject, originIdentifier );
    }

    /**
     * Constructor.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param createdObject the NetMeshObject that was created
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public NetMeshObjectCreatedEvent(
            NetMeshBase           source,
            NetMeshBaseIdentifier sourceIdentifier,
            NetMeshObject         createdObject,
            NetMeshBaseIdentifier originIdentifier,
            long                  timeEventOccurred )
    {
        super( source, sourceIdentifier, createdObject, originIdentifier, timeEventOccurred );
    }
}
