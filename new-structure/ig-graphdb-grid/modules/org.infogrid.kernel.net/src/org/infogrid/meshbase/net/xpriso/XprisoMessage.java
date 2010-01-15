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

package org.infogrid.meshbase.net.xpriso;

import java.io.Serializable;
import org.infogrid.comm.CarriesInvocationId;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.transaction.NetMeshObjectDeletedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectEquivalentsAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectEquivalentsRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectPropertyChangeEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeRemovedEvent;

/**
 * An XprisoMessage that captures the information exchanged between Proxies.
 * For efficiency reasons, this provides multiple implementing classes.
 */
public interface XprisoMessage
        extends
            CarriesInvocationId,
            Serializable
{
    /**
     * Obtain the NetMeshBaseIdentifier of the sender.
     * 
     * @return the sender's NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getSenderIdentifier();
    
    /**
     * Obtain the NetMeshBaseIdentifier of the receiver.
     * 
     * @return the receiver's NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getReceiverIdentifier();

    /**
     * Obtain the request ID. From CarriesInvocationId.
     *
     * @return the request ID
     */
    public long getRequestId();

    /**
     * Obtain the response ID. From CarriesInvocationId.
     *
     * @return the response ID
     */
    public long getResponseId();

    /**
     * Determine whether or not to cease communications after this message.
     *
     * @return if true, cease communications
     */
    public boolean getCeaseCommunications();

    /**
     * Obtain the externalized representation of the NetMeshObjects that are conveyed
     * by the sender to the receiver, e.g. in response to a request.
     *
     * @return the ExternalizedNetMeshObjects
     */
    public ExternalizedNetMeshObject [] getConveyedMeshObjects();

    /**
     * Obtain the NetMeshObjectAccessSpecifications to the NetMeshObjects for which the sender requests
     * a lease for the first time.
     *
     * @return the NetMeshObjectAccessSpecifications for the NetMeshObjects
     */
    public NetMeshObjectAccessSpecification [] getRequestedFirstTimeObjects();

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * that a currently valid lease be canceled.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedCanceledObjects();

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * a freshening.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedFreshenReplicas();

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender has a replica
     * that it wishes to resynchronize.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedResynchronizeReplicas();

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * the lock from the receiver (i.e. update rights).
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedLockObjects();

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender surrenders
     * the lock to the receiver (i.e. update rights).
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getPushLockObjects();

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender has forcefully
     * reclaimed the lock.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getReclaimedLockObjects();

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * home replica status.
     * 
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedHomeReplicas();

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender surrenders
     * the home replica status to the receiver.
     * 
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getPushHomeReplicas();
    
    /**
     * Obtain the type addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the type addition events
     */
    public NetMeshObjectTypeAddedEvent [] getTypeAdditions();

    /**
     * Obtain the type removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the type removal events
     */
    public NetMeshObjectTypeRemovedEvent [] getTypeRemovals();

    /**
     * Obtain the property change events that the sender needs to convey to the
     * receiver.
     *
     * @return the property change events
     */
    public NetMeshObjectPropertyChangeEvent [] getPropertyChanges();

    /**
     * Obtain the neighbor addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the neighbor addition events
     */
    public NetMeshObjectNeighborAddedEvent [] getNeighborAdditions();

    /**
     * Obtain the neighbor removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the neighbor removal events
     */
    public NetMeshObjectNeighborRemovedEvent [] getNeighborRemovals();

    /**
     * Obtain the equivalent addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the equivalent addition events
     */
    public NetMeshObjectEquivalentsAddedEvent [] getEquivalentsAdditions();

    /**
     * Obtain the equivalent removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the equivalent addition events
     */
    public NetMeshObjectEquivalentsRemovedEvent [] getEquivalentsRemovals();

    /**
     * Obtain the role addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the role addition events
     */
    public NetMeshObjectRoleAddedEvent [] getRoleAdditions();

    /**
     * Obtain the role removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the role removal events
     */
    public NetMeshObjectRoleRemovedEvent [] getRoleRemovals();

    /**
     * Obtain the deletion events for the NetMeshObjects that the sender has deleted
     * semantically and that the sender needs to convey to the receiver.
     *
     * @return the deletion events
     */
    public NetMeshObjectDeletedEvent [] getDeletions();

    /**
     * Determine whether this XprisoMessage contains any valid payload or is empty.
     *
     * @return true if it is empty
     */
    public boolean isEmpty();

    /**
     * Check that the XprisoMessage is internally correct.
     *
     * @throws IllegalStateException thrown if the XprisoMessage is not internally correct
     */
    public void check()
            throws
                IllegalStateException;
}
