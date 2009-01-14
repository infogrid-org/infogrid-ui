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
import org.infogrid.meshbase.transaction.MeshObjectTypeAddedEvent;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.util.CreateWhenNeeded;

/**
 * Event that indicates a NetMeshObject was blessed with one or more EntityTypes.
 */
public class NetMeshObjectTypeAddedEvent
        extends
            MeshObjectTypeAddedEvent
        implements
            NetMeshObjectTypeChangeEvent
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param source the NetMeshObject whose type changed
     * @param oldValues the old EntityTypes, prior to the event
     * @param deltaValues the EntityTypes that were added
     * @param newValues the new EntityTypes, after the event
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public NetMeshObjectTypeAddedEvent(
            NetMeshObject         source,
            EntityType []         oldValues,
            EntityType []         deltaValues,
            EntityType []         newValues,
            NetMeshBaseIdentifier originIdentifier,
            long                  timeEventOccurred )
    {
        super(  source,
                oldValues,
                deltaValues,
                newValues,
                timeEventOccurred );
        
        theOriginNetworkIdentifier = originIdentifier;
    }

    /**
     * Constructor for the case where we don't have old and new values, only the delta.
     * This perhaps should trigger some exception if it is attempted to read old or
     * new values later. (FIXME?)
     * 
     * @param sourceIdentifier the identifier of the MeshObject whose type changed
     * @param deltaValueIdentifiers the identifiers of the EntityTypes that were added
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     * @param resolver the MeshBase against which the MeshObjectIdentifiers are currently resolved, if any
     */
    public NetMeshObjectTypeAddedEvent(
            NetMeshObjectIdentifier sourceIdentifier,
            MeshTypeIdentifier []   deltaValueIdentifiers,
            NetMeshBaseIdentifier   originIdentifier,
            long                    timeEventOccurred,
            NetMeshBase             resolver )
    {
        super(  sourceIdentifier,
                deltaValueIdentifiers,
                timeEventOccurred,
                resolver );
        
        theOriginNetworkIdentifier = originIdentifier;
    }

    /**
     * Constructor.
     *
     * @param sourceIdentifier the identifier of the MeshObject whose type changed
     * @param oldValueIdentifiers the identifiers of the old set of EntityTypes, prior to the event
     * @param deltaValueIdentifiers the identifiers of the EntityTypes that were added
     * @param newValueIdentifiers the identifiers of the new set of EntityTypes, after the event
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     * @param resolver the MeshBase against which the MeshObjectIdentifiers are currently resolved, if any
     */
    public NetMeshObjectTypeAddedEvent(
            NetMeshObjectIdentifier sourceIdentifier,
            MeshTypeIdentifier []   oldValueIdentifiers,
            MeshTypeIdentifier []   deltaValueIdentifiers,
            MeshTypeIdentifier []   newValueIdentifiers,
            NetMeshBaseIdentifier   originIdentifier,
            long                    timeEventOccurred,
            NetMeshBase             resolver )
    {
        super(  sourceIdentifier,
                oldValueIdentifiers,
                deltaValueIdentifiers,
                newValueIdentifiers,
                timeEventOccurred,
                resolver );
        
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
            TransactionException
    {
        setResolver( base );

        try {
            NetMeshObject otherObject = (NetMeshObject) getSource();
            if( otherObject != null && incomingProxy == otherObject.getProxyTowardsLockReplica() ) {
                perhapsTx.obtain(); // can ignore return value

                EntityType [] types = getDeltaValue();
                otherObject.rippleBless( types, getTimeEventOccurred() );
            }
            return otherObject;

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
        return Utils.awayFromLock( this, outgoingProxy );
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
