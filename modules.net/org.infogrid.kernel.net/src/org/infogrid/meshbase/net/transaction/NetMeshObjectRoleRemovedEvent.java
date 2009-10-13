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

package org.infogrid.meshbase.net.transaction;

import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.transaction.CannotApplyChangeException;
import org.infogrid.meshbase.transaction.MeshObjectRoleRemovedEvent;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.MeshTypeUtils;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.util.CreateWhenNeeded;
import org.infogrid.util.event.UnresolvedException;

/**
 * <p>This event indicates that a relationship between the NetMeshObject and
 * another NetMeshObject was unblessed.</p>
 */
public class NetMeshObjectRoleRemovedEvent
        extends
            MeshObjectRoleRemovedEvent
        implements
            NetMeshObjectRoleChangeEvent
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param source the NetMeshObject that is the source of the event
     * @param oldValues the old values of the RoleType, prior to the event
     * @param deltaValues the RoleTypes that changed
     * @param newValues the new values of the RoleType, after the event
     * @param neighborIdentifier identifier of the MeshObject that identifies the other end of the affected relationship
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public NetMeshObjectRoleRemovedEvent(
            NetMeshObject           source,
            RoleType []             oldValues,
            RoleType []             deltaValues,
            RoleType []             newValues,
            NetMeshObjectIdentifier neighborIdentifier,
            NetMeshBaseIdentifier   originIdentifier,
            long                    timeEventOccurred )
    {
        this(   source,
                source.getIdentifier(),
                oldValues,
                MeshTypeUtils.meshTypeIdentifiersOrNull( oldValues ),
                deltaValues,
                MeshTypeUtils.meshTypeIdentifiersOrNull( deltaValues ),
                newValues,
                MeshTypeUtils.meshTypeIdentifiersOrNull( newValues ),
                null,
                neighborIdentifier,
                originIdentifier,
                timeEventOccurred,
                source.getMeshBase() );
    }

    /**
     * Constructor for the case where we don't have old and new values, only the delta.
     * This perhaps should trigger some exception if it is attempted to read old or
     * new values later. (FIXME?)
     * 
     * @param meshObjectIdentifier identifier of the MeshObject that is the source of the event
     * @param roleTypeIdentifiers identifiers of the RoleTypes affected on the source MeshObject, with respect to the deltaNeighbors
     * @param deltaNeighborIdentifier the identifier of the neighbor MeshObject affected by this event
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     * @param resolver the MeshBase against which the MeshObjectIdentifiers are currently resolved, if any
     */
    public NetMeshObjectRoleRemovedEvent(
            NetMeshObjectIdentifier meshObjectIdentifier,
            MeshTypeIdentifier []   roleTypeIdentifiers,
            NetMeshObjectIdentifier deltaNeighborIdentifier,
            NetMeshBaseIdentifier   originIdentifier,
            long                    timeEventOccurred,
            NetMeshBase             resolver )
    {
        this(   null,
                meshObjectIdentifier,
                null,
                null,
                null,
                roleTypeIdentifiers,
                null,
                null,
                null,
                deltaNeighborIdentifier,
                originIdentifier,
                timeEventOccurred,
                resolver );
    }

    /**
     * Pass-through constructor for subclasses.
     * 
     * @param source the MeshObject that is the source of the event
     * @param sourceIdentifier the identifier representing the source MeshObject of the event
     * @param oldValues the old values of the RoleType, prior to the event
     * @param oldValueIdentifiers the identifier representing the old values of the RoleType, prior to the event
     * @param deltaValues the RoleTypes that changed
     * @param deltaValueIdentifiers the identifiers of the RoleTypes that changed
     * @param newValues the new values of the RoleType, after the event
     * @param newValueIdentifiers the identifier representing the new values of the RoleType, after the event
     * @param neighbor the MeshObject that identifies the other end of the affected relationship
     * @param neighborIdentifier the identifier representing the MeshObject that identifies the other end of the affected relationship
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     * @param resolver the MeshBase against which the MeshObjectIdentifiers are currently resolved, if any
     */
    protected NetMeshObjectRoleRemovedEvent(            
            NetMeshObject           source,
            NetMeshObjectIdentifier sourceIdentifier,
            RoleType []             oldValues,
            MeshTypeIdentifier []   oldValueIdentifiers,
            RoleType []             deltaValues,
            MeshTypeIdentifier []   deltaValueIdentifiers,
            RoleType []             newValues,
            MeshTypeIdentifier []   newValueIdentifiers,
            NetMeshObject           neighbor,
            NetMeshObjectIdentifier neighborIdentifier,
            NetMeshBaseIdentifier   originIdentifier,
            long                    timeEventOccurred,
            NetMeshBase             resolver )
    {
        super(  source,
                sourceIdentifier,
                oldValues,
                oldValueIdentifiers,
                deltaValues,
                deltaValueIdentifiers,
                newValues,
                newValueIdentifiers,
                neighbor,
                neighborIdentifier,
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
     * Obtain the neighbor that changed.
     *
     * @return the neighbor MeshObject
     */
    @Override
    public NetMeshObject getNeighborMeshObject()
    {
        return (NetMeshObject) super.getNeighborMeshObject();
    }

    /**
     * Obtain the Identifier of the neighbor that changed.
     *
     * @return the Identifier of the neighbor MeshObject
     */
    @Override
    public NetMeshObjectIdentifier getNeighborMeshObjectIdentifier()
    {
        return (NetMeshObjectIdentifier) super.getNeighborMeshObjectIdentifier();
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

        NetMeshObject otherObject = null;
        try {
            otherObject = (NetMeshObject) getSource();
            if( otherObject != null ) { // don't check for the lock here -- relationships can be unblessed without having the lock
                perhapsTx.obtain(); // can ignore return value

                NetMeshObjectIdentifier relatedOtherObject = getNeighborMeshObjectIdentifier();
                RoleType []             roleTypes          = getDeltaValue();

                otherObject.rippleUnbless( roleTypes, relatedOtherObject, getTimeEventOccurred() );
            }
            return otherObject;

        } catch( TransactionException ex ) {
            throw ex;

        } catch ( UnresolvedException ex ) {
            throw new CannotApplyChangeException.ExceptionOccurred( base, ex );

        } catch( NotRelatedException ex ) {
            // this is can be ignored: in case of a Shadow, for example, neither the neighbor nor the
            // relationship may even be known here
            return otherObject; // never null -- UnresolvedException caught earlier

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
        return Utils.hasReplicaInDirection( this, outgoingProxy, theOriginNetworkIdentifier );
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
