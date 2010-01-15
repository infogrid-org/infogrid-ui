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

package org.infogrid.mesh.net.proxy;

import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.EquivalentAlreadyException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.mesh.RoleTypeNotBlessedException;
import org.infogrid.mesh.RoleTypeRequiresEntityTypeException;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;

/**
 * The methods exposed by NetMeshObject towards the Proxy. These methods should
 * never be invoked by anybody other than the Proxy. In particular, they should never
 * be invoked by the application programmer.
 */
public interface ReplicaProxyInterface
{
    /**
      * Surrender update rights when invoked. This shall not be called by the application
      * programmer. This is called only by Proxies that identify themselves to this call.
      *
      * @param theProxy the Proxy invoking this method
      * @return true if successful, false otherwise.
      */
    public abstract boolean proxyOnlySurrenderLock(
            Proxy theProxy );

    /**
      * Push update rights to this replica. This shall not be called by the application
      * programmer. This is called only by Proxies that identify themselves to this call.
      *
      * @param theProxy the Proxy invoking this method
      */
    public abstract void proxyOnlyPushLock(
            Proxy theProxy );

    /**
     * Surrender home replica status when invoked.  This shall not be called by the application
     * programmer. This is called only by Proxies that identify themselves to this call.
     *
     * @param theProxy the Proxy invoking this method
     * @return true if successful, false otherwise.
     */
    public abstract boolean proxyOnlySurrenderHomeReplica(
            Proxy theProxy );
    
    /**
     * Push home replica status to this replica. This shall not be called by the application
     * programmer. This is called only by Proxies that identify themselves to this call.
     * 
     * @param theProxy the Proxy invoking this method
     */
    public abstract void proxyOnlyPushHomeReplica(
            Proxy theProxy );

    /**
     * Tell the NetMeshObject to make a note of the fact that a new replica of the
     * NetMeshObject is being created in the direction of the provided Proxy.
     * This shall not be called by the application
     * programmer. This is called only by Proxies that identify themselves to this call.
     *
     * @param theProxy the Proxy invoking this method
     * @throws IllegalArgumentException thrown if the proxy had been registered before
     */
    public abstract void proxyOnlyRegisterReplicationTowards(
            Proxy theProxy )
        throws
            IllegalArgumentException;

    /**
      * Tell the NetMeshObject to remove the note of the fact that a replica of the
      * NetMeshObject exists in the direction of the provided Proxy.
      * This shall not be called by the application
      * programmer. This is called only by Proxies that identify themselves to this call.
      *
      * @param theProxy the Proxy invoking this method
      */
    public abstract void proxyOnlyUnregisterReplicationTowards(
            Proxy theProxy );

    /**
     * Resynchronize a replica.
     * 
     * @param timeCreated the timeCreated to use
     * @param timeUpdated the timeUpdated to use
     * @param timeRead the timeRead to use
     * @param timeExpires the timeExpires to use
     * @param correctProxies the Proxies to use
     * @param correctHomeProxyIndex the index into the correctProxies that identifies the home proxy, or -1 if none
     * @param correctProxyTowardsLockIndex the index into the correctProxies that identifies the proxy towards lock, or -1 if none
     */
    public void proxyOnlyResynchronizeReplica(
            long     timeCreated,
            long     timeUpdated,
            long     timeRead,
            long     timeExpires,
            Proxy [] correctProxies,
            int      correctHomeProxyIndex,
            int      correctProxyTowardsLockIndex );

    /**
     * Bless a replica NetMeshObject, as a consequence of the blessing of a master replica.
     *
     * @param types the to-be-blessed EntityTypes
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws EntityBlessedAlreadyException thrown if this MeshObject is already blessed with one or more of the EntityTypes
     * @throws IsAbstractException thrown if one or more of the EntityTypes were abstract and could not be instantiated
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract void rippleBless(
            EntityType [] types,
            long          timeUpdated )
        throws
            EntityBlessedAlreadyException,
            IsAbstractException,
            TransactionException,
            NotPermittedException;

    /**
     * Unbless a replica NetMeshObject, as a consequence of the unblessing of a master replica.
     *
     * @param types the to-be-unblessed EntityTypes
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws RoleTypeRequiresEntityTypeException thrown if this MeshObject plays one or more roles that requires the MeshObject to remain being blessed with at least one of the EntityTypes
     * @throws EntityNotBlessedException thrown if this MeshObject does not support at least one of the given EntityTypes
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract void rippleUnbless(
            EntityType [] types,
            long          timeUpdated )
        throws
            RoleTypeRequiresEntityTypeException,
            EntityNotBlessedException,
            TransactionException,
            NotPermittedException;

    /**
     * Relate two replica NetMeshObjects, as a consequence of relating other replicas.
     * 
     * @param newNeighborIdentifier the identifier of the NetMeshObject to relate to
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws RelatedAlreadyException thrown to indicate that this MeshObject is already related
     *         to the newNeighbor
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     */
    public abstract void rippleRelate(
            NetMeshObjectIdentifier newNeighborIdentifier,
            long                    timeUpdated )
        throws
            RelatedAlreadyException,
            TransactionException;
    
    /**
     * Unrelate two replica NetMeshObjects, as a consequence of unrelating other replicas.
     * 
     * @param neighborIdentifier the identifier of the NetMeshObject to unrelate from
     * @param mb the MeshBase that this MeshObject does or used to belong to
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws NotRelatedException thrown if this MeshObject is not related to the neighbor
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract void rippleUnrelate(
            NetMeshObjectIdentifier neighborIdentifier,
            NetMeshBase             mb,
            long                    timeUpdated )
        throws
            NotRelatedException,
            TransactionException,
            NotPermittedException;

    /**
     * Bless the relationship of two replica NetMeshObjects, as a consequence of blessing the relationship
     * of two other replicas.
     * 
     * @param theTypes the RoleTypes to use for blessing
     * @param neighborIdentifier the identifier of the NetMeshObject that
     *        identifies the relationship that shall be blessed
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws RoleTypeBlessedAlreadyException thrown if the relationship to the other MeshObject is blessed
     *         already with one ore more of the given RoleTypes
     * @throws EntityNotBlessedException thrown if this MeshObject is not blessed by a requisite EntityType
     * @throws NotRelatedException thrown if this MeshObject is not currently related to otherObject
     * @throws IsAbstractException thrown if one of the RoleTypes belong to an abstract RelationshipType
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract void rippleBless(
            RoleType []             theTypes,
            NetMeshObjectIdentifier neighborIdentifier,
            long                    timeUpdated )
        throws
            RoleTypeBlessedAlreadyException,
            EntityNotBlessedException,
            NotRelatedException,
            IsAbstractException,
            TransactionException,
            NotPermittedException;

    /**
     * Unbless the relationship of two replica NetMeshObjects, as a consequence of unblessing the relationship
     * of two other replicas.
     * 
     * @param theTypes the RoleTypes to use for unblessing
     * @param neighborIdentifier the identifier of the NetMeshObject that
     *        identifies the relationship that shall be unblessed
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws RoleTypeNotBlessedException thrown if the relationship to the other MeshObject does not support the RoleType
     * @throws NotRelatedException thrown if this MeshObject is not currently related to otherObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract void rippleUnbless(
            RoleType []             theTypes,
            NetMeshObjectIdentifier neighborIdentifier,
            long                    timeUpdated )
        throws
            RoleTypeNotBlessedException,
            NotRelatedException,
            TransactionException,
            NotPermittedException;
    
    /**
     * Add a replica NetMeshObject as an equivalent, as a consequence of adding a different replica
     * as equivalent.
     * 
     * @param identifierOfEquivalent the Identifier of the replica NetMeshObject
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws EquivalentAlreadyException thrown if the provided MeshObject is already an equivalent of this MeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract void rippleAddAsEquivalent(
            NetMeshObjectIdentifier identifierOfEquivalent,
            long                    timeUpdated )
        throws
            EquivalentAlreadyException,
            TransactionException,
            NotPermittedException;
    
    /**
     * Remove this replica NetMeshObject as an equivalent from the current set of equivalents, as a consequence of removing
     * a different replica as equivalent.
     * 
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract void rippleRemoveAsEquivalent(
            long timeUpdated )
        throws
            TransactionException,
            NotPermittedException;

    /**
     * Change the values of Properties on a replica NetMeshObject, as a consequence of changing the values of the properties
     * in another replica.
     *
     * @param types the PropertyTypes
     * @param values the new values, in the same sequence as the PropertyTypes
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws IllegalPropertyTypeException thrown if one PropertyType does not exist on this MeshObject
     *         because the MeshObject has not been blessed with a MeshType that provides this PropertyType
     * @throws IllegalPropertyValueException thrown if the new value is an illegal value for this Property
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract void rippleSetPropertyValues(
            PropertyType []  types,
            PropertyValue [] values,
            long             timeUpdated )
        throws
            IllegalPropertyTypeException,
            IllegalPropertyValueException,
            NotPermittedException,
            TransactionException;

    /**
     * Delete a replica NetMeshObject as a consequence of deleting another replica.
     * 
     * @param mb the MeshBase that this MeshObject does or used to belong to
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract void rippleDelete(
            NetMeshBase mb,
            long        timeUpdated )
        throws
            TransactionException,
            NotPermittedException;    
}