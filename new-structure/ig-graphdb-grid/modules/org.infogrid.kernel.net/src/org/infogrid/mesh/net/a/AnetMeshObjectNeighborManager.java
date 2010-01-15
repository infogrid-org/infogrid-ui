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

package org.infogrid.mesh.net.a;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.a.AMeshObject;
import org.infogrid.mesh.a.AMeshObjectNeighborManager;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;

/**
 * Refines AMeshObjectNeighborManager for AnetMeshObjects.
 */
public class AnetMeshObjectNeighborManager
    extends
        AMeshObjectNeighborManager
{
    private static final Log log = Log.getLogInstance( AnetMeshObjectNeighborManager.class ); // our own, private logger

    /**
     * Append a new neighbor and associated RoleTypes to a MeshObject.
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     * @param neighborRoleTypes RoleTypes, or null, for the neighbor
     * @throws RelatedAlreadyException thrown if the subject and the neighbor are related already
     */
    @Override
    public void appendNeighbor(
            AMeshObject          subject,
            MeshObjectIdentifier neighborIdentifier,
            RoleType []          neighborRoleTypes )
        throws
            RelatedAlreadyException
    {
        AnetMeshObject realSubject = (AnetMeshObject) subject;
        Proxy          incoming    = realSubject.getMeshBase().determineIncomingProxy();

        Proxy [] relationshipProxiesToAdd;
        if( incoming != null ) {
            relationshipProxiesToAdd = new Proxy[] { incoming };
        } else {
            relationshipProxiesToAdd = null;
        }

        synchronized( subject ) {
            internalAppendNeighbor( realSubject, neighborIdentifier, neighborRoleTypes, relationshipProxiesToAdd );
        }
    }

    /**
     * Helper method to append a new neighbor, associated RoleTypes and relationship proxies.
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     * @param neighborRoleTypes RoleTypes, or null, for the neighbor
     * @param relationshipProxies the relationship proxies for this relationship
     * @throws RelatedAlreadyException thrown if the subject and the neighbor are related already
     */
    protected void internalAppendNeighbor(
            AnetMeshObject       subject,
            MeshObjectIdentifier neighborIdentifier,
            RoleType []          neighborRoleTypes,
            Proxy []             relationshipProxies )
        throws
            RelatedAlreadyException
    {
        internalAppendNeighbor( subject, neighborIdentifier, neighborRoleTypes );

        if( subject.theRelationshipProxies == null ) {
            subject.theRelationshipProxies = new Proxy[][] { relationshipProxies };
        } else {
            subject.theRelationshipProxies = ArrayHelper.append( subject.theRelationshipProxies, relationshipProxies, Proxy[].class );
        }
    }

    /**
     * Helper method to remove a neighbor and associated RoleTypes, at a given index.
     *
     * @param subject the MeshObject in question
     * @param index index at which the neighbor is found
     */
    @Override
    protected void internalRemoveNeighbor(
            AMeshObject subject,
            int         index )
    {
        AnetMeshObject realSubject = (AnetMeshObject) subject;

        super.internalRemoveNeighbor( realSubject, index );

        realSubject.theRelationshipProxies = ArrayHelper.remove( realSubject.theRelationshipProxies, index, Proxy[].class );
    }

    /**
     * Helper method to append RoleTypes, at a given index.
     *
     * @param subject the MeshObject in question
     * @param index index at which the neighbor is found
     * @param toAdd the RoleTypes to add
     */
    @Override
    protected void internalAppendRoleTypes(
            AMeshObject subject,
            int         index,
            RoleType [] toAdd )
    {
        AnetMeshObject realSubject = (AnetMeshObject) subject;
        Proxy          incoming    = realSubject.getMeshBase().determineIncomingProxy();

        super.internalAppendRoleTypes( subject, index, toAdd );

        if( incoming != null ) {
            if( realSubject.theRelationshipProxies[index] == null ) {
                realSubject.theRelationshipProxies[index] = new Proxy[] { incoming };

            } else if( !ArrayHelper.isIn( incoming, realSubject.theRelationshipProxies[index], false )) {
                realSubject.theRelationshipProxies[index] = ArrayHelper.append( realSubject.theRelationshipProxies[index], incoming, Proxy.class );
            }
        }
    }

    /**
     * Obtain the set of relationship Proxies for all neighbors.
     *
     * @param subject the MeshObject in question
     * @return the relationship Proxies of neighbor MeshObjects, in the same sequence as getNeighborIdentifiers()
     */
    public Proxy [][] getRelationshipProxies(
            AnetMeshObject subject )
    {
        return subject.theRelationshipProxies;
    }

    /**
     * Obtain the sources of a particular relationship between a NetMeshObject
     * and another NetMeshObject (as given by a NetMeshObjectIdentifier).
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor MeshObject
     * @return the sources of the relationship, if any
     * @throws NotRelatedException thrown if the specified MeshObject is not actually a neighbor
     */
    public Proxy [] getRelationshipProxiesFor(
            AnetMeshObject          subject,
            NetMeshObjectIdentifier neighborIdentifier )
        throws
            NotRelatedException
    {
        synchronized( subject ) {
            int found = determineRelationshipIndex( subject, neighborIdentifier );
            if( found < 0 ) {
                throw new NotRelatedException( subject, neighborIdentifier );
            }

            return subject.theRelationshipProxies[ found ];
        }
    }

    /**
     * Obtain the set of identifiers of neighbor MeshObjects.
     *
     * @param subject the MeshObject in question
     * @return the set of identifiers of neighbor MeshObjects
     */
    @Override
    public NetMeshObjectIdentifier [] getNeighborIdentifiers(
            AMeshObject subject )
    {
        NetMeshObjectIdentifier [] ret = (NetMeshObjectIdentifier []) super.getNeighborIdentifiers( subject );
        return ret;
    }

    /**
     * Obtain the set of identifiers of neighbor MeshObjects according
     * to a particular relationship Proxy.
     *
     * @param subject the MeshObject in question
     * @param p the relationship Proxy
     * @return the set of identifiers of neighbor MeshObjects
     */
    public NetMeshObjectIdentifier [] getNeighborMeshObjectIdentifiersFromSource(
            AnetMeshObject subject,
            Proxy          p )
    {
        NetMeshObjectIdentifier [] all = getNeighborIdentifiers( subject );

        if( all == null || all.length == 0 ) {
            return new NetMeshObjectIdentifier[0];
        }

        NetMeshObjectIdentifier [] ret = new NetMeshObjectIdentifier[ all.length ];
        int count = 0;
        for( int i=0 ; i<all.length ; ++i ) {
            Proxy [] here = subject.theRelationshipProxies[ i ];
            if( here == null ) {
                continue;
            }
            for( int j=0 ; j<here.length ; ++j ) {
                if( p == here[j] ) {
                    ret[ count++ ] = all[i];
                    break;
                }
            }
        }
        if( count < ret.length ) {
            ret = ArrayHelper.copyIntoNewArray( ret, 0, count, NetMeshObjectIdentifier.class );
        }
        return ret;
    }

    /**
     * Overridable method to create a single-element MeshObjectIdentifier array.
     *
     * @param oneElement the single element
     * @return the created array
     */
    @Override
    protected NetMeshObjectIdentifier [] makeMeshObjectIdentifiers(
            MeshObjectIdentifier oneElement )
    {
        return new NetMeshObjectIdentifier[] { (NetMeshObjectIdentifier) oneElement };
    }

    /**
     * Overridable method to create a MeshObjectIdentifier array as a
     * concatenation between an existing array and a new element.
     *
     * @param existing the existing array
     * @param toAdd the single element to add
     * @return the created array
     */
    @Override
    protected NetMeshObjectIdentifier [] makeMeshObjectIdentifiers(
            MeshObjectIdentifier [] existing,
            MeshObjectIdentifier    toAdd )
    {
        return ArrayHelper.append(
                (NetMeshObjectIdentifier []) existing,
                (NetMeshObjectIdentifier) toAdd,
                NetMeshObjectIdentifier.class );
    }

    /**
     * Remove an element from an array of MeshObjectIdentifier.
     * This may be overridden by subclasses.
     *
     * @param content the array
     * @param indexToRemove index of the object to remove
     * @return the content without to the object to be removed
     */
    @Override
    protected NetMeshObjectIdentifier [] removeMeshObjectIdentifier(
            MeshObjectIdentifier [] content,
            int                     indexToRemove )
    {
        NetMeshObjectIdentifier [] ret = ArrayHelper.remove( (NetMeshObjectIdentifier []) content, indexToRemove, NetMeshObjectIdentifier.class );
        return ret;
    }

    /**
     * Singleton instance of this class.
     */
    public static final AnetMeshObjectNeighborManager SINGLETON_NET
            = new AnetMeshObjectNeighborManager();
}
