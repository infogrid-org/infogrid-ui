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

package org.infogrid.kernel.net.test.xpriso;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.set.MeshObjectSelector;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.m.NetMMeshBaseNameServer;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.xpriso.logging.LogXprisoMessageLogger;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.logging.Log;

/**
 * Factors out common functionality for the tests in this package.
 */
public abstract class AbstractXprisoTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     * 
     * @param testClass the class to be tested
     */
    protected AbstractXprisoTest(
            Class testClass )
    {
        super( localFileName( testClass, "/ResourceHelper" ));

        theNameServer = NetMMeshBaseNameServer.create();

        theXprisoMessageLogger = LogXprisoMessageLogger.create( Log.getLogInstance( getClass() ));
    }

    /**
     * Report an error if the properties are not the same in both replicas.
     *
     * @param one first replica to compare
     * @param two second replica to compare
     * @param msg message to print when replicas' properties aren't equal
     * @return true if check passed
     * @throws Exception all kinds of things can go wrong in tests
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
     * @throws NotRelatedException all kinds of things can go wrong in tests
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
     * Check the position of the Proxies.
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
                reportError( msg + ": object has no proxies", proxiesTowards, obj.getIdentifier() );
                return false;
            } else {
                return true; // no proxies is correct
            }
        } else if( proxiesTowards == null || proxiesTowards.length == 0 ) {
            reportError( msg + ": object has proxies, should have none", proxies, obj.getIdentifier() );
            return false;
        }
        if( proxies.length != proxiesTowards.length ) {
            reportError( msg + ": object has wrong number of proxies.", proxiesTowards.length, proxies.length );
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
            reportError( msg + ": not the same content", proxiesIdentifiers, proxiesTowardsIdentifiers );
        }

        if( proxyTowardLock == null ) {
            if( obj.getProxyTowardsLockReplica() != null ) {
                reportError( msg + ": has proxyTowardsLock but should not", obj.getIdentifier() );
                ret = false;
            }

        } else if( obj.getProxyTowardsLockReplica() == null ) {
            reportError( msg + ": does not have proxyTowardsLock but should", obj.getIdentifier() );
            ret = false;

        } else {
            ret &= checkEquals( proxyTowardLock.getIdentifier(), obj.getProxyTowardsLockReplica().getPartnerMeshBaseIdentifier(), msg + ": wrong proxyTowardLock" );
        }
        if( proxyTowardHome == null ) {
            if( obj.getProxyTowardsHomeReplica() != null ) {
                reportError( msg + ": has proxyTowardHome but should not", obj.getIdentifier() );
                ret = false;
            }

        } else if( obj.getProxyTowardsHomeReplica() == null ) {
            reportError( msg + ": does not have proxyTowardHome but should", obj.getIdentifier() );
            ret = false;

        } else {
            ret &= checkEquals( proxyTowardHome.getIdentifier(), obj.getProxyTowardsHomeReplica().getPartnerMeshBaseIdentifier(), msg + ": wrong proxyTowardLock" );
        }
        return ret;
    }

    /**
     * Check the position of the relationship Proxies.
     *
     * @param obj1 the start object of the relationship
     * @param obj2 the destination object of the relationship
     * @param proxiesTowards the NetMeshBases to which the proxies are supposed to be pointing
     * @param msg the message to print when the proxies are not correct
     * @return true if check passed
     * @throws Exception all sorts of things may go wrong in tests
     */
    protected boolean checkRelationshipProxies(
            NetMeshObject  obj1,
            NetMeshObject  obj2,
            NetMeshBase [] proxiesTowards,
            String         msg )
        throws
            Exception
    {
        return checkRelationshipProxies( obj1, obj2.getIdentifier(), proxiesTowards, msg );
    }

    /**
     * Check the position of the relationship Proxies.
     *
     * @param obj1 the start object of the relationship
     * @param obj2Identifier identiifer of the destination object of the relationship
     * @param proxiesTowards the NetMeshBases to which the proxies are supposed to be pointing
     * @param msg the message to print when the proxies are not correct
     * @return true if check passed
     * @throws Exception all sorts of things may go wrong in tests
     */
    protected boolean checkRelationshipProxies(
            NetMeshObject           obj1,
            NetMeshObjectIdentifier obj2Identifier,
            NetMeshBase []          proxiesTowards,
            String                  msg )
        throws
            Exception
    {
        Proxy [] relationshipProxies = obj1.getRelationshipProxiesFor( obj2Identifier );

        if( relationshipProxies == null || relationshipProxies.length == 0 ) {
            if( !( proxiesTowards == null || proxiesTowards.length == 0 )) {
                reportError( msg, obj1.getIdentifier(), obj2Identifier, proxiesTowards );
                return false;
            } else {
                return true; // no proxies is correct
            }
        } else if( proxiesTowards == null || proxiesTowards.length == 0 ) {
                reportError( msg, obj1.getIdentifier(), obj2Identifier, relationshipProxies, relationshipProxies );
            return false;
        }

        boolean ret = true;
        if( relationshipProxies.length != proxiesTowards.length ) {
                reportError( msg, obj1.getIdentifier(), obj2Identifier, relationshipProxies, proxiesTowards.length, relationshipProxies );

            ret = false;

        } else {
            NetMeshBaseIdentifier [] relationshipProxiesIdentifiers = new NetMeshBaseIdentifier[ relationshipProxies.length ];
            NetMeshBaseIdentifier [] proxiesTowardsIdentifiers      = new NetMeshBaseIdentifier[ proxiesTowards.length ];
            for( int i=0 ; i<relationshipProxies.length ; ++i ) {
                relationshipProxiesIdentifiers[i] = relationshipProxies[i].getPartnerMeshBaseIdentifier();
            }
            for( int i=0 ; i<proxiesTowards.length ; ++i ) {
                proxiesTowardsIdentifiers[i] = proxiesTowards[i].getIdentifier();
            }

            if( !checkEqualsOutOfSequence( relationshipProxiesIdentifiers, proxiesTowardsIdentifiers, null )) {
                reportError( msg + ": not the same content", relationshipProxiesIdentifiers, proxiesTowardsIdentifiers );
            }
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
    protected NetMMeshBaseNameServer<NetMeshBaseIdentifier,NetMeshBase> theNameServer;

    /**
     * Factory for NetMeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.create(
            new DefaultNetMeshBaseIdentifierFactory.Protocol[] {
                    new DefaultNetMeshBaseIdentifierFactory.Protocol( "test", false ),
                    new DefaultNetMeshBaseIdentifierFactory.Protocol( "http", true )
            });
    
    /**
     * The root context for these tests.
     */
    protected static final Context rootContext = SimpleContext.createRoot( "root-context" );

    /**
     * Expected duration within which at least one ping-pong round trip can be completed.
     * Milliseconds.
     */
    protected static final long PINGPONG_ROUNDTRIP_DURATION = 500L;

    /**
     * The XprisoMessageLogger to use.
     */
    protected LogXprisoMessageLogger theXprisoMessageLogger;
}
