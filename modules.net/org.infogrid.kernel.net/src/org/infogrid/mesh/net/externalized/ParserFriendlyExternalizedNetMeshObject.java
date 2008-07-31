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

package org.infogrid.mesh.net.externalized;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.externalized.ParserFriendlyExternalizedMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

import org.infogrid.util.ArrayHelper;
import org.infogrid.util.StringHelper;
import org.infogrid.util.logging.Log;

import java.util.ArrayList;

/**
 * A temporary buffer for a to-be-deserialized NetMeshObject.
 */
public class ParserFriendlyExternalizedNetMeshObject
        extends
            ParserFriendlyExternalizedMeshObject
        implements
            ExternalizedNetMeshObject
{
    private static final Log log = Log.getLogInstance( ParserFriendlyExternalizedNetMeshObject.class ); // our own, private logger

    /**
     * Constructor.
     */
    public ParserFriendlyExternalizedNetMeshObject()
    {
    }

    /**
     * Obtain the Identifier of the MeshObject.
     *
     * @return the Identifier of the MeshObject
     */
    @Override
    public NetMeshObjectIdentifier getIdentifier()
    {
        return (NetMeshObjectIdentifier) super.getIdentifier();
    }

    /**
     * Set the Identifier.
     * 
     * @param newValue the new value
     */
    @Override
    public void setIdentifier(
            MeshObjectIdentifier newValue )
    {
        NetMeshObjectIdentifier realNewValue = (NetMeshObjectIdentifier) newValue;
        super.setIdentifier( realNewValue );
    }

    /**
     * Obtain the Identifiers of the neighbors of this MeshObject.
     * 
     * @return the Identifiers of the neighbors
     */
    @Override
    public NetMeshObjectIdentifier [] getNeighbors()
    {
        NetMeshObjectIdentifier [] ret = new NetMeshObjectIdentifier[ theRelationships.size() ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = (NetMeshObjectIdentifier) theRelationships.get( i ).getNeighborIdentifier();
        }
        return ret;
    }

    /**
     * Add an equivalent, using its MeshObjectIdentifier.
     * 
     * @param identifier the identifier of an equivalent
     */
    @Override
    public void addEquivalent(
            MeshObjectIdentifier identifier )
    {
        NetMeshObjectIdentifier realIdentifier = (NetMeshObjectIdentifier) identifier;
        super.addEquivalent( realIdentifier );
    }

    /**
     * Obtain the NetMeshObjectIdentifiers of the NetMeshObjects that participate in an equivalence
     * set with this NetMeshObject.
     *
     * @return the NetMeshObjectIdentifiers. May be null.
     */
    @Override
    public NetMeshObjectIdentifier [] getEquivalents()
    {
        NetMeshObjectIdentifier [] ret = theEquivalents.toArray( new NetMeshObjectIdentifier[ theEquivalents.size() ]);
        return ret;
    }

    /**
     * Set the GiveUpHomeReplica property.
     *
     * @param newValue the new value
     */
    public void setGiveUpHomeReplica(
            boolean newValue )
    {
        theGiveUpHomeReplica = newValue;
    }

    /**
     * Obtain the GiveUpHomeReplica property.
     * 
     * @return the GiveUpHomeReplica property
     */
    public boolean getGiveUpHomeReplica()
    {
        return theGiveUpHomeReplica;
    }

    /**
     * Set the GiveUpLock property.
     *
     * @param newValue the new value
     */
    public void setGiveUpLock(
            boolean newValue )
    {
        theGiveUpLock = newValue;
    }
    
    /**
     * Obtain the GiveUpLock property.
     *
     * @return the value
     */
    public boolean getGiveUpLock()
    {
        return theGiveUpLock;
    }

    /**
     * Add a NetMeshBaseIdentifier of a Proxy.
     * 
     * @param toAdd to be added
     * @param isProxyTowardsHome iof true, this identifies the Proxy in whose direction the home replica may be found
     * @param isProxyTowardsLock iof true, this identifies the Proxy in whose direction the lock may be found
     */
    public void addProxyNetworkIdentifier(
            NetMeshBaseIdentifier toAdd,
            boolean               isProxyTowardsHome,
            boolean               isProxyTowardsLock )
    {
        int last = theProxyIdentifiers.size();
        theProxyIdentifiers.add( toAdd );
        
        if( isProxyTowardsHome ) {
            if( theProxyTowardsHomeIndex != -1 ) {
                log.error( "proxyTowardsHome specified twice for " + theIdentifier );
            }
            theProxyTowardsHomeIndex = last;
        }
        if( isProxyTowardsLock ) {
            if( theProxyTowardsLockIndex != -1 ) {
                log.error( "proxyTowardsLock specified twice for " + theIdentifier );
            }
            theProxyTowardsLockIndex = last;
        }
    }
    
    /**
     * Obtain the NetMeshBaseIdentifiers of all Proxies affected by this NetMeshObject.
     *
     * @return the NetMeshBaseIdentifiers, if any
     */
    public final NetMeshBaseIdentifier[] getProxyIdentifiers()
    {
        NetMeshBaseIdentifier [] ret = ArrayHelper.copyIntoNewArray( theProxyIdentifiers, NetMeshBaseIdentifier.class );
        return ret;
    }

    /**
     * Obtain the NetMeshBaseIdentifier of the Proxy towards the home replica.
     * 
     * @return the NetMeshBaseIdentifier, if any
     */
    public final NetMeshBaseIdentifier getProxyTowardsHomeNetworkIdentifier()
    {
        if( theProxyIdentifiers == null || theProxyTowardsHomeIndex == HERE_CONSTANT ) {
            return null;
        } else {
            return theProxyIdentifiers.get( theProxyTowardsHomeIndex );
        }
    }

    /**
     * Obtain the NetMeshBaseIdentifier of the Proxy towards the replica with the lock.
     * 
     * @return the NetMeshBaseIdentifier, if any
     */
    public final NetMeshBaseIdentifier getProxyTowardsLockNetworkIdentifier()
    {
        if( theProxyIdentifiers == null || theProxyTowardsLockIndex == HERE_CONSTANT ) {
            return null;
        } else {
            return theProxyIdentifiers.get( theProxyTowardsLockIndex );
        }
    }
    
    /**
     * Convert to String representation, for debugging.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "theIdentifier",
                    "theMeshTypes",
                    "thePropertyTypes",
                    "thePropertyValues",
                    "theTimeCreated",
                    "theTimeUpdated",
                    "theTimeRead",
                    "theTimeExpires",
                    "theRelationships",
                    "theEquivalents",
                    "theGiveUpHomeReplica",
                    "theGiveUpLock",
                    "theProxyIdentifiers",
                    "theProxyTowardsHomeIndex",
                    "theProxyTowardsLockIndex"
                },
                new Object[] {
                    theIdentifier,
                    theMeshTypes,
                    thePropertyTypes,
                    thePropertyValues,
                    theTimeCreated,
                    theTimeUpdated,
                    theTimeRead,
                    theTimeExpires,
                    theRelationships,
                    theEquivalents,
                    theGiveUpHomeReplica,
                    theGiveUpLock,
                    theProxyIdentifiers,
                    theProxyTowardsHomeIndex,
                    theProxyTowardsLockIndex
                },
                StringHelper.LOG_FLAGS.SHOW_NON_NULL | StringHelper.LOG_FLAGS.SHOW_NON_ZERO | StringHelper.LOG_FLAGS.SHOW_ZERO );
    }

    /**
     * The GiveUpHomeReplica property.
     */
    protected boolean theGiveUpHomeReplica;
    
    /**
     * The GiveUpLock property.
     */
    protected boolean theGiveUpLock;
    
    /**
     * NetMeshBaseIdentifier for the Proxies to other NetworkedMeshBases that contain the replicas that are
     * closest in the replication graph.
     */
    protected ArrayList<NetMeshBaseIdentifier> theProxyIdentifiers = new ArrayList<NetMeshBaseIdentifier>();

    /**
     * The index into theProxyIdentifiers that represents our home Proxy. If this is HERE_CONSTANT, it
     * indicates that this is the home replica.
     */
    protected int theProxyTowardsHomeIndex = HERE_CONSTANT;
    
    /**
     * The index into theProxyIdentifiers that represents the Proxy towards the lock. If this
     * is HERE_CONSTANT, it indicates that this replica has the lock.
     */
    protected int theProxyTowardsLockIndex = HERE_CONSTANT;
    
    /** 
     * Special value indicating this replica (instead of another, reached through a Proxy).
     */
    protected static final int HERE_CONSTANT = -1;
}
