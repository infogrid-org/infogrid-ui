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

package org.infogrid.probe.shadow.store;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.mesh.net.a.AnetMeshObject;

import org.infogrid.meshbase.a.AMeshObjectEquivalenceSetComparator;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;

import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;

import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.ModelBase;

import org.infogrid.probe.shadow.a.AStagingMeshBaseLifecycleManager;

import org.infogrid.util.ArrayHelper;
import org.infogrid.util.FactoryException;
import org.infogrid.util.logging.Log;

import java.util.HashMap;

/**
 * A MeshBaseLifecycleManager specifically for Store implementations of ShadowMeshBase.
 */
public class StoreShadowMeshBaseLifecycleManager
        extends
            AStagingMeshBaseLifecycleManager
        
{
    private static final Log log = Log.getLogInstance( StoreShadowMeshBaseLifecycleManager.class ); // our own, private logger
    
    /**
     * Factory method. The application developer should not call this or a subclass constructor; use
     * MeshBase.getMeshObjectLifecycleManager() instead.
     * 
     * @return the created StoreShadowMeshBaseLifecycleManager
     */
    public static StoreShadowMeshBaseLifecycleManager create()
    {
        StoreShadowMeshBaseLifecycleManager ret = new StoreShadowMeshBaseLifecycleManager();
        return ret;
    }

    /**
     * Constructor. The application developer should not call this or a subclass constructor; use
     * MeshBase.getMeshObjectLifecycleManager() instead.
     */
    protected StoreShadowMeshBaseLifecycleManager()
    {
        super();
    }
    
    /**
     * Recreate a NetMeshObject previously created here but swapped out on disk.
     * 
     * @param externalized the externalized form of the NetMeshObject
     * @return the recreated NetMeshObject
     * @throws FactoryException thrown if the NetMeshObject could not be recreated
     */
    public NetMeshObject restore(
            ExternalizedNetMeshObject externalized )
        throws
            FactoryException
    {
        MeshObject existing = findInStore( externalized.getIdentifier() );
        if( existing != null ) {
            throw new FactoryException( "MeshObject exists already: " + existing );
        }
        
        StoreShadowMeshBase realBase  = (StoreShadowMeshBase) theMeshBase;
        ModelBase           modelBase = theMeshBase.getModelBase();

        NetMeshBaseIdentifier [] proxyNames = externalized.getProxyIdentifiers();
        Proxy []             proxies    = new Proxy[ proxyNames.length ];

        int proxyTowardsHomeIndex = -1;
        int proxyTowardsLockIndex = -1;

        for( int i=0 ; i<proxies.length ; ++i ) {
            proxies[i] = realBase.getProxyFor( proxyNames[i] );
            if( proxies[i] == null ) {
                log.error( "Could not find proxy for " + proxyNames[i] );
                return null;
            }
            if( proxyNames[i].equals( externalized.getProxyTowardsHomeNetworkIdentifier() )) {
                proxyTowardsHomeIndex = i;
            }
            if( proxyNames[i].equals( externalized.getProxyTowardsLockNetworkIdentifier() )) {
                proxyTowardsLockIndex = i;
            }
        }
        
        NetMeshObjectIdentifier identifier = externalized.getIdentifier();
        
        int typeCounter = 0;
        EntityType [] types;
        if( externalized.getExternalTypeIdentifiers() != null && externalized.getExternalTypeIdentifiers().length > 0 ) {
            types = new EntityType[ externalized.getExternalTypeIdentifiers().length ];
            for( int i=0 ; i<externalized.getExternalTypeIdentifiers().length ; ++i ) {
                try {
                    types[typeCounter] = (EntityType) modelBase.findMeshTypeByIdentifier( externalized.getExternalTypeIdentifiers()[i] );
                    typeCounter++; // make sure we do the increment after an exception might have been thrown
                    
                } catch( MeshTypeNotFoundException ex ) {
                    log.error( ex );
                }
            }
            if( typeCounter < types.length ) {
                types = ArrayHelper.copyIntoNewArray( types, 0, typeCounter, EntityType.class );
            }
        } else {
            types = null;
        }

        HashMap<PropertyType,PropertyValue> localProperties;
        if( externalized.getPropertyTypes() != null && externalized.getPropertyTypes().length > 0 ) {

            localProperties = new HashMap<PropertyType,PropertyValue>( externalized.getPropertyTypes().length );
            for( int i=externalized.getPropertyTypes().length-1 ; i>=0 ; --i ) {
                
                try {
                    PropertyType type = (PropertyType) modelBase.findMeshTypeByIdentifier( externalized.getPropertyTypes()[i] );             
                    localProperties.put( type, externalized.getPropertyValues()[i] );
                } catch( MeshTypeNotFoundException ex ) {
                    log.error( ex );
                }
            }
        } else {
            localProperties = null;
        }

        MeshObjectIdentifier []    otherSides = externalized.getNeighbors();
        RoleType [][]              roleTypes  = null;
        NetMeshObjectIdentifier [] realOtherSides;
        
        if( otherSides != null && otherSides.length > 0 ) {
            roleTypes      = new RoleType[ otherSides.length ][];
            realOtherSides = new NetMeshObjectIdentifier[ otherSides.length ];

            for( int i=0 ; i<otherSides.length ; ++i ) {
                realOtherSides[i] = (NetMeshObjectIdentifier) otherSides[i];

                MeshTypeIdentifier [] currentRoleTypes = externalized.getRoleTypesFor( otherSides[i] );

                roleTypes[i] = new RoleType[ currentRoleTypes.length ];
                typeCounter = 0;

                for( int j=0 ; j<currentRoleTypes.length ; ++j ) {
                    try {
                        roleTypes[i][typeCounter] = modelBase.findRoleTypeByIdentifier( currentRoleTypes[j] );
                        typeCounter++; // make sure we do the increment after an exception might have been thrown
                    } catch( Exception ex ) {
                        log.warn( ex );
                    }
                    if( typeCounter < roleTypes[i].length ) {
                        roleTypes[i] = ArrayHelper.copyIntoNewArray( roleTypes[i], 0, typeCounter, RoleType.class );
                    }
                }
            }
        } else {
            realOtherSides = null;
        }

        NetMeshObjectIdentifier [] equivalents = externalized.getEquivalents();
        
        NetMeshObjectIdentifier [] leftRight
                = (NetMeshObjectIdentifier []) AMeshObjectEquivalenceSetComparator.SINGLETON.findLeftAndRightEquivalents(
                        externalized.getIdentifier(),
                        equivalents );

        AnetMeshObject ret = new AnetMeshObject(
                identifier,
                realBase,
                externalized.getTimeCreated(),
                externalized.getTimeUpdated(),
                externalized.getTimeRead(),
                externalized.getTimeExpires(),
                localProperties,
                types,
                leftRight,
                realOtherSides,
                roleTypes,
                externalized.getGiveUpHomeReplica(),
                externalized.getGiveUpLock(),
                proxies,
                proxyTowardsHomeIndex,
                proxyTowardsLockIndex );

        putIntoStore( ret );

        // no transaction
        return ret;        
    }
}
