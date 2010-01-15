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
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

/**
 * Utility methods for this package.
 */
public abstract class Utils
{
    /**
     * Private constructor to keep this abstract.
     */
    private Utils()
    {
        // noop
    }

    /**
     * Determine whether a given Proxy points away from the source of the NetChange.
     *
     * @param change the NetChange
     * @param proxy the Proxy
     * @return true if the Proxy points away from the source of the change
     */
    public static boolean awayFromLock(
            NetChange change,
            Proxy     proxy )
    {
        NetMeshObject affectedMeshObject = change.getAffectedMeshObject();
        if( affectedMeshObject == null ) {
            // can happen if neighbors aren't replicated
            return false;
        }

        Proxy [] proxies = affectedMeshObject.getAllProxies();
        if( proxies == null ) {
            return false;
        }
        if( affectedMeshObject.getProxyTowardsLockReplica() == proxy ) {
            return false;
        }
        for( Proxy p : proxies ) {
            if( p == proxy ) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determine whether a given Proxy points away from the source of the NetChange.
     *
     * @param proxyIdentifiers the identifiers of the partner NetMeshBases that the NetMeshObject interacts with, if any
     * @param lockIdentifier the identifier of the partner NetMeshBase in whose direction the lock may be found, if any
     * @param proxy the Proxy
     * @return true if the Proxy points away from the source of the change
     */
    public static boolean awayFromLock(
            NetMeshBaseIdentifier [] proxyIdentifiers,
            NetMeshBaseIdentifier    lockIdentifier,
            Proxy                    proxy )
    {
        if( proxyIdentifiers == null ) {
            return false;
        }
        if( lockIdentifier != null && lockIdentifier.equals( proxy.getPartnerMeshBaseIdentifier() )) {
            return false;
        }
        for( NetMeshBaseIdentifier p : proxyIdentifiers ) {
            if( p.equals( proxy.getPartnerMeshBaseIdentifier() )) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether there is a replica of the affected NetMeshObject
     * in a given NetChange, in the direction of a given Proxy, exluding another
     * that is known by the NetMeshBaseIdentifier of the NetMeshBase it interacts
     * with.
     * 
     * @param change the NetChange that carries the affected NetMeshObject
     * @param direction the Proxy indicating the direction
     * @param exclude the NetMeshBaseIdentifier of the NetMeshBase that the to-be-excluded Proxy interacts with
     * @return true if there is a replica of the affected NetMeshObject in this direction
     */
    public static boolean hasReplicaInDirection(
            NetChange             change,
            Proxy                 direction,
            NetMeshBaseIdentifier exclude )
    {
        if( direction.getPartnerMeshBaseIdentifier().equals( exclude )) {
            return false;
        }

        NetMeshObject affectedMeshObject = change.getAffectedMeshObject();

        return hasReplicaInDirection( affectedMeshObject, direction );
    }
    
    /**
     * Determine whether a NetMeshObject has a replica in the direction of a given Proxy.
     * 
     * @param obj the NetMeshObject
     * @param direction the Proxy indicating the direction
     * @return true if there is a replica of the affected NetMeshObject in this direction
     */
    public static boolean hasReplicaInDirection(
            NetMeshObject obj,
            Proxy         direction )
    {
        Proxy [] proxies = obj.getAllProxies();
        if( proxies == null ) {
            return false;
        }

        for( Proxy p : proxies ) {
            if( p == direction ) {
                return true;
            }
        }
        return false;
    }
}
