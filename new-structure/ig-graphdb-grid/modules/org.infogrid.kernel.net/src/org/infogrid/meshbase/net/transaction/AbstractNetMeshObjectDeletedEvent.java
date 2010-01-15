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

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.externalized.ExternalizedMeshObject;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.transaction.CannotApplyChangeException;
import org.infogrid.meshbase.transaction.MeshObjectDeletedEvent;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.CreateWhenNeeded;

/**
 * Abstract superclass to hold the common behavior of NetMeshObjectDeletedEvent
 * and ReplicaPurgedEvent.
 */
public abstract class AbstractNetMeshObjectDeletedEvent
        extends
            MeshObjectDeletedEvent
        implements
            NetChange<MeshBase,MeshBaseIdentifier,MeshObject,MeshObjectIdentifier>
{
    /**
     * Constructor. This must be invoked with both the NetMeshObject and the canonical NetMeshObjectIdentifier,
     * because it is not possible to construct the canonical NetMeshObjectIdentifier after the NetMeshObject is dead.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param deletedObject the NetMeshObject that was deleted
     * @param deletedObjectIdentifier the identifier of the NetMeshObject that was deleted
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param externalized the deleted MeshObject in externalized form as it was just prior to deletion
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public AbstractNetMeshObjectDeletedEvent(
            NetMeshBase            source,
            NetMeshBaseIdentifier  sourceIdentifier,
            NetMeshObject          deletedObject,
            MeshObjectIdentifier   deletedObjectIdentifier,
            NetMeshBaseIdentifier  originIdentifier,
            ExternalizedMeshObject externalized,
            long                   timeEventOccurred )
    {
        super( source, sourceIdentifier, deletedObject, deletedObjectIdentifier, externalized, timeEventOccurred );
        
        theOriginNetworkIdentifier = originIdentifier;
    }

    /**
     * Obtain the MeshObject affected by this Change.
     *
     * @return obtain the MeshObject affected by this Change
     */
    @Override
    public NetMeshObject getAffectedMeshObject()
    {
        return (NetMeshObject) super.getAffectedMeshObject();
    }

    /**
     * Obtain the MeshObjectIdentifier of the MeshObject affected by this Change.
     *
     * @return the MeshObjectIdentifier of the NetMeshObject affected by this Change
     */
    @Override
    public NetMeshObjectIdentifier getAffectedMeshObjectIdentifier()
    {
        return (NetMeshObjectIdentifier) super.getAffectedMeshObjectIdentifier();
    }

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
            TransactionException
    {
        setResolver( base );

        try {
            NetMeshObject ret = base.findMeshObjectByIdentifier( getAffectedMeshObjectIdentifier() );
            if( ret != null && incomingProxy == ret.getProxyTowardsLockReplica() ) {
                perhapsTx.obtain(); // can ignore return value

                NetMeshObjectIdentifier otherObjectIdentifier = getAffectedMeshObjectIdentifier();

                ret = base.getMeshBaseLifecycleManager().rippleDelete(
                        otherObjectIdentifier,
                        theOriginNetworkIdentifier,
                        getTimeEventOccurred() );
            }
            return ret;

        } catch( TransactionException ex ) {
            throw ex;

        } catch( Throwable ex ) {
            throw new CannotApplyChangeException.ExceptionOccurred( base, ex );
        }
    }

    /**
     * Determine whether this NetChange should be forwarded through the given, outgoing Proxy.
     * If specified, {@link #getOriginNetworkIdentifier} specifies where the NetChange came from.
     *
     * @param outgoingProxy the potential outgoing Proxy
     * @return true if the NetChange should be forwarded torwards the outgoingProxy
     */
    public boolean shouldBeSent(
            Proxy outgoingProxy )
    {
        ExternalizedNetMeshObject realExternalized = (ExternalizedNetMeshObject)theExternalizedMeshObject;

        return Utils.awayFromLock( realExternalized.getProxyIdentifiers(), realExternalized.getProxyTowardsLockNetworkIdentifier(), outgoingProxy );
    }

    /**
     * Obtain the NetMeshBaseIdentifier of the NetMeshBase from where this NetChange arrived.
     * This may or may not be the NetMeshBase where the Change originated, as it might be
     * passed through several NetMeshBases until it arrived here. This may be null if
     * the Change originated locally.
     *
     * @return the NetMeshBaseIdentifier, if any
     */
    public final NetMeshBaseIdentifier getOriginNetworkIdentifier()
    {
        return theOriginNetworkIdentifier;
    }

    /**
     * The NetMeshBaseIdentifier of the NetMeshBase from where this NetChange arrived.
     * This may or may not be the NetMeshBase where the Change originated, as it might be
     * passed through several NetMeshBases until it arrived here. This may be null if
     * the Change originated locally.
     */
    protected NetMeshBaseIdentifier theOriginNetworkIdentifier;
}
