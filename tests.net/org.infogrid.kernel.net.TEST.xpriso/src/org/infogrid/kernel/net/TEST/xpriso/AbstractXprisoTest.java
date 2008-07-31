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

package org.infogrid.kernel.net.TEST.xpriso;

import org.infogrid.context.Context;
import org.infogrid.context.SimpleContext;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.set.MeshObjectSelector;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.MNameServer;
import org.infogrid.util.WritableNameServer;

/**
 * Superclass for all tests in this package.
 */
public abstract class AbstractXprisoTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     */
    protected AbstractXprisoTest(
            Class testClass )
        throws
            MeshTypeNotFoundException
    {
        super( localFile( testClass, "/ResourceHelper" ),
               localFile( testClass, "/Log.properties" ));

        theNameServer = MNameServer.create();
    }

    /**
     * Report an error if the properties are not the same in both replicas.
     *
     * @param one first replica to compare
     * @param two second replica to compare
     * @param msg message to print when replicas' properties aren't equal
     * @return true if check passed
     */
    protected boolean checkPropertiesReplication(
            NetMeshObject one,
            NetMeshObject two,
            String        msg )
        throws
            Exception
    {
        boolean ret = true;
        
        PropertyType [] oneTypes = one.getAllPropertyTypes();
        PropertyType [] twoTypes = two.getAllPropertyTypes();

        ret &= checkEqualsOutOfSequence( oneTypes, twoTypes, msg + " not the same PropertyTypes" );
        for( int i=0 ; i<oneTypes.length ; ++i ) {
            PropertyValue oneValue = one.getPropertyValue( oneTypes[i] );
            PropertyValue twoValue = two.getPropertyValue( oneTypes[i] );
            ret &= checkEquals( oneValue, twoValue, "different values for PropertyType " + oneTypes[i] );
        }
        return ret;
    }

    /**
     * Report an error if the types are not the same in both replicas.
     *
     * @param one first replica to compare
     * @param two second replica to compare
     * @param msg message to print when replicas' types aren't equal
     * @return true if check passed
     */
    protected boolean checkTypesReplication(
            NetMeshObject one,
            NetMeshObject two,
            String        msg )
    {
        boolean ret = true;
        
        EntityType [] oneTypes = one.getTypes();
        EntityType [] twoTypes = two.getTypes();

        ret &= checkEqualsOutOfSequence( oneTypes, twoTypes, msg + " not the same EntityTypes" );

        return ret;
    }

    /**
     * Report an error if the neighbors and the relationship types are not the same in both replicas.
     *
     * @param one first replica to compare
     * @param two second replica to compare
     * @param msg message to print when neighbors aren't equal
     * @return true if check passed
     */
    protected boolean checkNeighborsReplication(
            NetMeshObject one,
            NetMeshObject two,
            String        msg )
        throws
            NotRelatedException
    {
        boolean ret = true;
        
        MeshObjectSet oneNeighbors = one.traverseToNeighborMeshObjects( false );
        MeshObjectSet twoNeighbors = two.traverseToNeighborMeshObjects( false );

        ret &= checkEqualsOutOfSequence( oneNeighbors.getMeshObjects(), twoNeighbors.getMeshObjects(), msg + " not the same neighbors for " + one.getIdentifier() );

        for( final MeshObject currentOne : oneNeighbors ) {
            MeshObject currentTwo = twoNeighbors.find( new MeshObjectSelector() {
                public boolean accepts(
                        MeshObject candidate )
                {
                    return currentOne.getIdentifier().equals( candidate.getIdentifier() );
                }
            });
            
            RoleType [] relatedOne = one.getRoleTypes( currentOne );
            RoleType [] relatedTwo = two.getRoleTypes( currentTwo );
            
            ret &= checkEqualsOutOfSequence( relatedOne, relatedTwo, msg + " not the same RoleTypes for " + one.getIdentifier() + " related to " + currentOne.getIdentifier() );
        }
        return ret;
    }

    /**
     * Check the position of the Proxies
     *
     * @param obj the NetMeshObject whose proxies are checked
     * @param proxiesTowards the NetMeshBases to which the proxies are supposed to be pointing
     * @param proxyTowardHome the NetMeshBase towards which the proxyTowardHome is supposed to be pointing, or null
     * @param proxyTowardLock the NetMeshBase towards which the proxyTowardsLock is supposed to be pointing, or null
     * @param msg the message to print when the proxies are not correct
     * @return true if check passed
     */
    protected boolean checkProxies(
            NetMeshObject  obj,
            NetMeshBase [] proxiesTowards,
            NetMeshBase    proxyTowardHome,
            NetMeshBase    proxyTowardLock,
            String         msg )
    {
        boolean ret = true;
        
        if( obj == null ) {
            reportError( "Cannot check proxies of null object" );
            return false;
        }
        
        Proxy [] proxies = obj.getAllProxies();

        if( proxies == null || proxies.length == 0 ) {
            if( !( proxiesTowards == null || proxiesTowards.length == 0 )) {
                reportError( msg + ": object has no proxies, should have " + proxiesTowards.length + ": " + obj.getIdentifier().toExternalForm() );
                return false;
            } else {
                return true; // no proxies is correct
            }
        } else if( proxiesTowards == null || proxiesTowards.length == 0 ) {
            reportError( msg + ": object has " + proxies.length + " proxies, should have none: " + obj.getIdentifier().toExternalForm() );
            return false;
        }
        if( proxies.length != proxiesTowards.length ) {
            reportError( msg + ": object has wrong number of proxies. Should have " + proxiesTowards.length + ", does have " + proxies.length );
            ret = false;
        }
        
        NetMeshBaseIdentifier [] proxiesIdentifiers        = new NetMeshBaseIdentifier[ proxies.length ];
        NetMeshBaseIdentifier [] proxiesTowardsIdentifiers = new NetMeshBaseIdentifier[ proxiesTowards.length ];
        for( int i=0 ; i<proxies.length ; ++i ) {
            proxiesIdentifiers[i] = proxies[i].getPartnerMeshBaseIdentifier();
        }
        for( int i=0 ; i<proxiesTowards.length ; ++i ) {
            proxiesTowardsIdentifiers[i] = proxiesTowards[i].getIdentifier();
        }
        if( !checkEqualsOutOfSequence( proxiesIdentifiers, proxiesTowardsIdentifiers, null )) {
            StringBuilder buf = new StringBuilder();
            buf.append( msg ).append( ": not the same content: " );
            String sep = "{ ";
            for( NetMeshBaseIdentifier current : proxiesIdentifiers ) {
                buf.append( sep );
                buf.append( current.toExternalForm() );
                sep = ", ";
            }
            sep = " } vs. { ";
            for( NetMeshBaseIdentifier current : proxiesTowardsIdentifiers ) {
                buf.append( sep );
                buf.append( current.toExternalForm() );
                sep = ", ";
            }
            buf.append( " }" );
            reportError( buf.toString() );
        }

        if( proxyTowardLock == null ) {
            if( obj.getProxyTowardsLockReplica() != null ) {
                reportError( msg + ": has proxyTowardsLock but should not: " + obj.getIdentifier().toExternalForm() );
                ret = false;
            }

        } else if( obj.getProxyTowardsLockReplica() == null ) {
            reportError( msg + ": does not have proxyTowardsLock but should: " + obj.getIdentifier().toExternalForm() );
            ret = false;

        } else {
            ret &= checkEquals( proxyTowardLock.getIdentifier(), obj.getProxyTowardsLockReplica().getPartnerMeshBaseIdentifier(), msg + ": wrong proxyTowardLock" );
        }
        if( proxyTowardHome == null ) {
            if( obj.getProxyTowardsHomeReplica() != null ) {
                reportError( msg + ": has proxyTowardHome but should not: " + obj.getIdentifier().toExternalForm() );
                ret = false;
            }

        } else if( obj.getProxyTowardsHomeReplica() == null ) {
            reportError( msg + ": does not have proxyTowardHome but should: " + obj.getIdentifier().toExternalForm() );
            ret = false;

        } else {
            ret &= checkEquals( proxyTowardHome.getIdentifier(), obj.getProxyTowardsHomeReplica().getPartnerMeshBaseIdentifier(), msg + ": wrong proxyTowardLock" );
        }
        return ret;
    }

    /**
     * The ModelBase.
     */
    protected ModelBase theModelBase = ModelBaseSingleton.getSingleton();

    /**
     * The name server.
     */
    protected WritableNameServer<NetMeshBaseIdentifier, NetMeshBase> theNameServer;

    /**
     * The root context for these tests.
     */
    protected static final Context rootContext = SimpleContext.createRoot( "root-context" );
}
