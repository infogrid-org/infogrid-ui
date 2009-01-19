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

package org.infogrid.probe.store.TEST;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.store.sql.AbstractSqlStore;
import org.infogrid.store.sql.mysql.MysqlStore;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.SimpleContext;

/**
 * Factors out common functionality of StoreProbeTests.
 */
public abstract class AbstractStoreProbeTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     * 
     * @param testClass identifies the actual test to be run
     * @throws Exception all sorts of things may go wrong in tests
     */
    public AbstractStoreProbeTest(
            Class testClass )
        throws
            Exception
    {
        super( localFileName( testClass, "/ResourceHelper" ));

        theDataSource = new MysqlDataSource();
        theDataSource.setDatabaseName( TEST_DATABASE_NAME );
        
        theSqlStore = MysqlStore.create( theDataSource, TEST_TABLE_NAME );
        theSqlStore.initializeHard();
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
     * The root context for these tests.
     */
    protected static final Context rootContext = SimpleContext.createRoot( "root-context" );

    /**
     * The ModelBase.
     */
    protected static ModelBase theModelBase = ModelBaseSingleton.getSingleton();

    /**
     * The Database connection.
     */
    protected MysqlDataSource theDataSource;

    /**
     * The AbstractSqlStore to be tested.
     */
    protected AbstractSqlStore theSqlStore;

    /**
     * The ProbeDirectory.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    /**
     * The factory for NetMeshBaseIdentifiers.
     */
    protected static NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.create(
            new DefaultNetMeshBaseIdentifierFactory.Protocol [] {
                new DefaultNetMeshBaseIdentifierFactory.Protocol( "test", false ),
                new DefaultNetMeshBaseIdentifierFactory.Protocol( "http", true ),
                new DefaultNetMeshBaseIdentifierFactory.Protocol( "file", true ),
            });
    
    /**
     * The name of the database that we use to store test data.
     */
    public static final String TEST_DATABASE_NAME = "test";

    /**
     * The name of the table that we use to store test data.
     */
    public static final String TEST_TABLE_NAME = "StoreProbeTest";

    /**
     * Expected duration within which at least one ping-pong round trip can be completed.
     * Milliseconds.
     */
    protected static final long PINGPONG_ROUNDTRIP_DURATION = 100L;

    /**
     * The SQL driver.
     */
    static Object theSqlDriver;
    static {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            theSqlDriver = Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }        
    }
}
