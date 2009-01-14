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
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.transaction.CannotApplyChangeException;
import org.infogrid.meshbase.transaction.Change;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.CreateWhenNeeded;

/**
 * Extends Change to cover the NetMeshBase use cases, and return the more specific
 * subtypes.
 * 
 * @param <S> the type of the event source
 * @param <SID> the type of the identifier of the event source
 * @param <V> the type of the value
 * @param <VID> the type of the identifier of the value
 */
public interface NetChange<S,SID,V,VID>
        extends
            Change<S,SID,V,VID>
{
    /**
     * Obtain the NetMeshObject affected by this Change.
     *
     * @return obtain the NetMeshObject affected by this Change
     */
    public abstract NetMeshObject getAffectedMeshObject();

    /**
     * Obtain the identifier of the NetMeshObject affected by this Change.
     *
     * @return the identifier of the NetMeshObject affected by this Change
     */
    public NetMeshObjectIdentifier getAffectedMeshObjectIdentifier();

    /**
     * <p>Apply this NetChange to a NetMeshObject in this MeshBase. This method
     *    is intended to make it easy to replicate NetChanges that were made to a
     *    replica of one NetMeshObject in one NetMeshBase to another replica
     *    of the NetMeshObject in another NetMeshBase.</p>
     *
     * <p>This method will attempt to create a Transaction if none is present on the
     * current Thread.</p>
     *
     * @param base the NetMeshBase in which to apply the NetChange
     * @param perhapsTx the Transaction to use, if any
     * @param incomingProxy the Proxy through which this NetChange was received
     * @return the NetMeshObject to which the NetChange was applied
     * @throws CannotApplyChangeException thrown if the NetChange could not be applied, e.g because
     *         the affected NetMeshObject did not exist in MeshBase base
     * @throws TransactionException thrown if a Transaction didn't exist on this Thread and
     *         could not be created
     */
    public NetMeshObject potentiallyApplyToReplicaIn(
            NetMeshBase                   base,
            CreateWhenNeeded<Transaction> perhapsTx,
            Proxy                         incomingProxy )
        throws
            CannotApplyChangeException,
            TransactionException;

    /**
     * Obtain the NetMeshBaseIdentifier, if any, from where this NetChange originated.
     * 
     * @return the NetMeshBaseIdentifier, if any
     */
    public abstract NetMeshBaseIdentifier getOriginNetworkIdentifier();

    /**
     * Determine whether this NetChange should be forwarded through the given, outgoing Proxy.
     * If specified, {@link #getOriginNetworkIdentifier} specifies where the NetChange came from.
     *
     * @param outgoingProxy the potential outgoing Proxy
     * @return true if the NetChange should be forwarded torwards the outgoingProxy
     */
    public abstract boolean shouldBeSent(
            Proxy outgoingProxy );
}
