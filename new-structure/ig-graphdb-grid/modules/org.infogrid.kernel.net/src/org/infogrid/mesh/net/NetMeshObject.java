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

package org.infogrid.mesh.net;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.net.externalized.SimpleExternalizedNetMeshObject;
import org.infogrid.mesh.net.proxy.ReplicaProxyInterface;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.RemoteQueryTimeoutException;

/**
 * The subtype of MeshObject used in NetMeshBases.
 */
public interface NetMeshObject
        extends
            MeshObject,
            ReplicaProxyInterface
{
    /**
     * Obtain the globally unique identifier of this NetMeshObject.
     *
     * @return the globally unique identifier of this NetMeshObject
     */
    public abstract NetMeshObjectIdentifier getIdentifier();

    /**
     * Obtain the NetMeshBase that contains this NetMeshObject. This is immutable for the
     * lifetime of this instance.
     *
     * @return the MeshBase that contains this MeshObject.
     */
    public abstract NetMeshBase getMeshBase();

    /**
     * Obtain the NetMeshObjectIdentifiers of the neighbors of this MeshObject. This is sometimes a
     * more efficient operation than to traverse to the neighbors and determine the
     * NetMeshObjectIdentifiers from there.
     *
     * @return the NetMeshObjectIdentifiers of the neighbors, if any
     */
    public abstract NetMeshObjectIdentifier [] getNeighborMeshObjectIdentifiers();

    /**
     * Obtain the NetMeshObjectIdentifiers of the neighbors of this MeshObject, as conveyed by
     * a given Proxy.
     * 
     * @param p the Proxy
     * @return the NetMeshObjectIdentifiers of the neighbors, if any, according to the Proxy
     */
    public abstract NetMeshObjectIdentifier [] getNeighborMeshObjectIdentifiersAccordingTo(
            Proxy p );

    /**
      * Determine whether this replica has update rights.
      *
      * @return returns true if this is replica has the update rights
      */
    public abstract boolean hasLock();

    /**
     * Attempt to obtain update rights.
     *
     * @return returns true if we have update rights, or we were successful obtaining them.
     * @throws RemoteQueryTimeoutException thrown if the replica that has the lock could not be contacted or did not reply in the time alloted
     */
    public abstract boolean tryToObtainLock()
        throws
            RemoteQueryTimeoutException;

    /**
     * Attempt to obtain update rights. Specify a timeout in milliseconds.
     *
     * @param timeout the timeout in milliseconds
     * @return returns true if we have update rights, or we were successful obtaining them.
     * @throws RemoteQueryTimeoutException thrown if the replica that has the lock could not be contacted or did not reply in the time alloted
     */
    public abstract boolean tryToObtainLock(
            long timeout )
        throws
            RemoteQueryTimeoutException;

    /**
     * Attempt to move update rights to the NetMeshBase that can be found through the
     * specified Proxy. This requires this NetMeshObject to have the update rights.
     * 
     * @param outgoingProxy the Proxy
     * @return returns true if the update rights were moved
     * @throws DoNotHaveLockException thrown if this NetMeshObject does not have update rights
     * @throws RemoteQueryTimeoutException thrown if the NetMeshBase could not be contacted or did not reply in the time alloted
     */
    public abstract boolean tryToPushLock(
            Proxy outgoingProxy )
        throws
            DoNotHaveLockException,
            RemoteQueryTimeoutException;
            
    /**
     * Attempt to move update rights to the NetMeshBase that can be found through the
     * specified Proxy. Specify a timeout in milliseconds. This requires this NetMeshObject to have the update rights.
     * 
     * @param outgoingProxy the Proxy
     * @param timeout the timeout in milliseconds
     * @return returns true if the update rights were moved
     * @throws DoNotHaveLockException thrown if this NetMeshObject does not have update rights
     * @throws RemoteQueryTimeoutException thrown if the NetMeshBase could not be contacted or did not reply in the time alloted
     */
    public abstract boolean tryToPushLock(
            Proxy outgoingProxy,
            long  timeout )
        throws
            DoNotHaveLockException,
            RemoteQueryTimeoutException;

    /**
     * Forced recovery of the lock. This must only be invoked on the home replica.
     * 
     * @throws NotHomeReplicaException thrown if this method is invoked on a replica other than the home replica
     */
    public abstract void forceLockRecovery()
            throws
                NotHomeReplicaException;
    
    /**
      * Determine whether this replica is going to give up update rights if it has them,
      * in case someone asks. This only says "if this replica has update rights, it will
      * give them up when asked". This call makes no statement about whether this replica
      * currently does or does not have update rights.
      *
      * @return if true, this replica will give up update rights when asked
      * @see #getWillGiveUpLock
      */
    public abstract boolean getWillGiveUpLock();

    /**
      * Set whether this replica will allow update rights to be given up or not.
      * However, if this is not the home replica and a lease for the replica expires, the
      * home replica will still reclaim the lock. Setting this value will not
      * prevent that.
      *
      * @param yesNo if true, this replica will give update rights when asked
      * @see #getWillGiveUpLock
      */
    public abstract void setWillGiveUpLock(
            boolean yesNo );

    /**
     * Obtain the Proxy in the direction of the update rights for this replica.
     * This may return null, indicating that this replica has the update rights.
     *
     * @return the Proxy in the direction of the update rights
     */
    public abstract Proxy getProxyTowardsLockReplica();
    
    /**
     * Determine whether this the home replica.
     *
     * @return returns true if this is the home replica
     */
    public abstract boolean isHomeReplica();

    /**
     * Attempt to obtain the home replica status.
     *
     * @return returns true if we have home replica status, or we were successful obtaining it.
     * @throws RemoteQueryTimeoutException thrown if the replica that has home replica status could not be contacted or did not reply in the time alloted
     */
    public abstract boolean tryToObtainHomeReplica()
        throws
            RemoteQueryTimeoutException;

    /**
     * Attempt to obtain the home replica status. Specify a timeout in milliseconds.
     *
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @return returns true if we have home replica status, or we were successful obtaining it.
     * @throws RemoteQueryTimeoutException thrown if the replica that has home replica status could not be contacted or did not reply in the time alloted
     */
    public abstract boolean tryToObtainHomeReplica(
            long duration )
        throws
            RemoteQueryTimeoutException;

    /**
     * Attempt to move the home replica status to the NetMeshBase that can be found through the
     * specified Proxy. This requires this NetMeshObject to have home replica status.
     * 
     * @param outgoingProxy the Proxy
     * @return returns true if the home replica status was moved
     * @throws NotHomeReplicaException thrown if this NetMeshObject does not have home replica status
     * @throws RemoteQueryTimeoutException thrown if the NetMeshBase could not be contacted or did not reply in the time alloted
     */
    public abstract boolean tryToPushHomeReplica(
            Proxy outgoingProxy )
        throws
            NotHomeReplicaException,
            RemoteQueryTimeoutException;
            
    /**
     * Attempt to move the home replica status to the NetMeshBase that can be found through the
     * specified Proxy. Specify a timeout in milliseconds. This requires this NetMeshObject to have home replica status.
     * 
     * @param outgoingProxy the Proxy
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @return returns true if the home replica status was moved
     * @throws NotHomeReplicaException thrown if this NetMeshObject does not have home replica status
     * @throws RemoteQueryTimeoutException thrown if the NetMeshBase could not be contacted or did not reply in the time alloted
     */
    public abstract boolean tryToPushHomeReplica(
            Proxy outgoingProxy,
            long  duration )
        throws
            NotHomeReplicaException,
            RemoteQueryTimeoutException;
    
    /**
     * Determine whether this replica is going to give up home replica status if it has it,
     * in case someone asks. This only says "if this replica is the home replica, it
     * will give it up when asked". This call makes no statement about whether this replica
     * currently does or does not have home replica status.
     * 
     * @return if true, this replica will give up home replica status when asked
     * @see #setWillGiveUpHomeReplica
     */
    public abstract boolean getWillGiveUpHomeReplica();

    /**
     * Set whether this replica will allow home replica status to be given up or not.
     * 
     * @param yesNo if true, this replica will give up home replica status when asked
     * @see #getWillGiveUpHomeReplica
     */
    public abstract void setWillGiveUpHomeReplica(
            boolean yesNo );

    /**
     * Obtain the Proxy in the direction of the home replica.
     * This may return null, indicating that this replica is the home replica.
     *
     * @return the Proxy in the direction of the home replica
     */
    public abstract Proxy getProxyTowardsHomeReplica();
    
    /**
     * Obtain all Proxies applicable to this replica.
     *
     * @return all Proxies. This may return null for efficiency reasons.
     */
    public abstract Proxy [] getAllProxies();

    /**
     * Obtain an Iterator over all Proxies applicable to this replica.
     *
     * @return the CursorIterator
     */
    public abstract CursorIterator<Proxy> proxyIterator();

    /**
     * Find a Proxy towards a partner NetMeshBase with a particular NetMeshBaseIdentifier. If such a
     * Proxy does not exist, return null.
     * 
     * @param partnerIdentifier the NetMeshBaseIdentifier of the partner NetMeshBase
     * @return the found Proxy, or null
     */
    public abstract Proxy findProxyTowards(
            NetMeshBaseIdentifier partnerIdentifier );

    /**
     * Obtain all relationship Proxies applicable to this replica.
     *
     * @return all relationship Proxies. This may return null for efficiency reasons.
     */
    public abstract Proxy [] getAllRelationshipProxies();

    /**
     * Obtain the set of relationship Proxies for the relationship between this NetMeshObject
     * and the provided neighbor NetMeshObject.
     *
     * @param neighborIdentifier identifier of the neighbor NetMeshObject, identifying the relationship
     * @return the found relationship Proxies
     * @throws NotRelatedException thrown if the two NetMeshObjects are not related
     */
    public abstract Proxy [] getRelationshipProxiesFor(
            NetMeshObjectIdentifier neighborIdentifier )
        throws
            NotRelatedException;

    /**
     * Determine the path to the NetMeshObject's home replica, to the extent it is known here.
     *
     * @return the path to the NetMeshObject's home 4replica
     */
    public abstract NetMeshObjectAccessSpecification getPathToHomeReplica();
    
    /**
     * Obtain the same NetMeshObject as SimpleExternalizedNetMeshObject so it can be easily serialized.
     * 
     * @param captureProxies if true, the SimpleExternalizedNetMeshObject contain entries for the
     *        Proxies as held in this replica. If false, that information will be left out.
     * @return this NetMeshObject as SimpleExternalizedNetMeshObject
     */
    public abstract SimpleExternalizedNetMeshObject asExternalized(
            boolean captureProxies );
}
