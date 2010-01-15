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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.meshbase.net.a;

import java.util.ArrayList;
import java.util.HashMap;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.meshbase.MeshObjectsNotFoundException;
import org.infogrid.meshbase.a.AMeshBase;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseAccessSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectAccessException;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyManager;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.meshbase.net.xpriso.logging.LogXprisoMessageLogger;
import org.infogrid.meshbase.net.xpriso.logging.XprisoMessageLogger;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CachingMap;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.FactoryException;
import org.infogrid.util.NameServer;
import org.infogrid.util.RemoteQueryTimeoutException;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.ReturnSynchronizerException;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * The subtype of MeshBase suitable for the AnetMeshObject implementation.
 */
public abstract class AnetMeshBase
        extends
            AMeshBase
        implements
            NetMeshBase
{
    private static final Log log = Log.getLogInstance( AnetMeshBase.class ); // our own, private logger

    /**
     * Constructor for subclasses only.
     *
     * @param identifier the NetMeshBaseIdentifier of this NetMeshBase
     * @param identifierFactory the factory for NetMeshObjectIdentifiers appropriate for this NetMeshBase
     * @param meshBaseIdentifierFactory the factory for NetMeshBaseIdentifiers
     * @param netMeshObjectAccessSpecificationFactory the factory for NetMeshObjectAccessSpecifications
     * @param setFactory the factory for MeshObjectSets appropriate for this NetMeshBase
     * @param modelBase the ModelBase containing type information
     * @param life the MeshBaseLifecycleManager to use
     * @param accessMgr the AccessManager that controls access to this NetMeshBase
     * @param cache the CachingMap that holds the NetMeshObjects in this NetMeshBase
     * @param proxyManager the ProxyManager used by this NetMeshBase
     * @param context the Context in which this NetMeshBase runs.
     */
    protected AnetMeshBase(
            NetMeshBaseIdentifier                       identifier,
            NetMeshObjectIdentifierFactory              identifierFactory,
            NetMeshBaseIdentifierFactory                meshBaseIdentifierFactory,
            NetMeshObjectAccessSpecificationFactory     netMeshObjectAccessSpecificationFactory,
            MeshObjectSetFactory                        setFactory,
            ModelBase                                   modelBase,
            AnetMeshBaseLifecycleManager                life,
            NetAccessManager                            accessMgr,
            CachingMap<MeshObjectIdentifier,MeshObject> cache,
            ProxyManager                                proxyManager,
            Context                                     context )
    {
        super( identifier, identifierFactory, setFactory, modelBase, life, accessMgr, cache, context );

        if( meshBaseIdentifierFactory == null ) {
            throw new NullPointerException();
        }
        if( netMeshObjectAccessSpecificationFactory == null ) {
            throw new NullPointerException();
        }
        if( proxyManager == null ) {
            throw new NullPointerException();
        }

        theMeshBaseIdentifierFactory               = meshBaseIdentifierFactory;
        theNetMeshObjectAccessSpecificationFactory = netMeshObjectAccessSpecificationFactory;
        theProxyManager                            = proxyManager;
        theAccessLocallySynchronizer               = AccessLocallySynchronizer.create( this );

        Log mostSpecificLog = Log.getLogInstance( getClass() );
        if( mostSpecificLog.isInfoEnabled() ) {
            theMessageLogger = LogXprisoMessageLogger.create( mostSpecificLog );
        }
    }

    /**
     * Obtain the NetMeshBaseIdentifier at which this NetMeshBase is located.
     * 
     * @return the NetMeshBaseIdentifier
     */
    @Override
    public NetMeshBaseIdentifier getIdentifier()
    {
        return (NetMeshBaseIdentifier) super.getIdentifier();
    }

    /**
     * Obtain the NetMeshBase's home object. The home object is
     * the only well-known object in a NetMeshBase. It is guaranteed to exist and
     * cannot be deleted.
     *
     * @return the NetMeshObject that is this NetMeshBase's home object
     */
    @Override
    public NetMeshObject getHomeObject()
    {
        MeshObject ret = super.getHomeObject();
        return (NetMeshObject) ret;
    }

    /**
     * <p>Find a NetMeshObject in this NetMeshBase by its identifier. Unlike
     * the {@link #accessLocally accessLocally} methods, this method purely considers NetMeshObjects in the
     * NetMeshBase, and does not attempt to obtain them if they are not in the NetMeshBase yet.</p>
     * <p>If not found, returns <code>null</code>.</p>
     * 
     * @param identifier the identifier of the NetMeshObject that shall be found
     * @return the found NetMeshObject, or null if not found
     * @see #findMeshObjectByIdentifierOrThrow
     */
    @Override
    public NetMeshObject findMeshObjectByIdentifier(
            MeshObjectIdentifier identifier )
    {
        MeshObject ret = super.findMeshObjectByIdentifier( identifier );

        return (NetMeshObject) ret;
    }

    /**
     * <p>Find a set of NetMeshObjects in this NetMeshBase by their identifiers. Unlike
     *    the {@link #accessLocally accessLocally} methods, this method purely considers NetMeshObjects in the
     *    NetMeshBase, and does not attempt to obtain them if they are not in the NetMeshBase yet.</p>
     * <p>If one or more of the NetMeshObjects could not be found, returns <code>null</code> at
     *    the respective index in the returned array.</p>
     * 
     * @param identifiers the identifiers of the NetMeshObjects that shall be found
     * @return the found NetMeshObjects, which may contain null values for NetMeshObjects that were not found
     */
    @Override
    public NetMeshObject [] findMeshObjectsByIdentifier(
            MeshObjectIdentifier [] identifiers )
    {
        NetMeshObject [] ret = new NetMeshObject[ identifiers.length ];
        
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = findMeshObjectByIdentifier( identifiers[i] );
        }
        return ret;
    }
    
    /**
     * <p>Find a NetMeshObject in this NetMeshBase by its identifier. Unlike
     * the {@link #accessLocally accessLocally} methods, this method purely considers NetMeshObjects in the
     * NetMeshBase, and does not attempt to obtain them if they are not in the NetMeshBase yet.</p>
     * <p>If not found, throws {@link MeshObjectsNotFoundException MeshObjectsNotFoundException}.</p>
     * 
     * @param identifier the identifier of the NetMeshObject that shall be found
     * @return the found NetMeshObject, or null if not found
     * @throws MeshObjectsNotFoundException thrown if the NetMeshObject was not found
     */
    @Override
    public NetMeshObject findMeshObjectByIdentifierOrThrow(
            MeshObjectIdentifier identifier )
        throws
            MeshObjectsNotFoundException
    {
        return (NetMeshObject) super.findMeshObjectByIdentifierOrThrow( identifier );
    }

    /**
     * <p>Find a set of NetMeshObjects in this NetMeshBase by their identifiers. Unlike
     *    the {@link #accessLocally accessLocally} method, this method purely considers NetMeshObjects in the
     *    NetMeshBase, and does not attempt to obtain them if they are not in the NetMeshBase yet.</p>
     * <p>If one or more of the NetMeshObjects could not be found, throws
     *    {@link MeshObjectsNotFoundException MeshObjectsNotFoundException}.</p>
     * 
     * @param identifiers the identifiers of the NetMeshObjects that shall be found
     * @return the found NetMeshObjects
     * @throws MeshObjectsNotFoundException if one or more of the NetMeshObjects were not found. Note that this Exception
     *         inherits from PartialResultException, and may carry any partial results that were available at the
     *         time the Exception was thrown
     */
    @Override
    public NetMeshObject [] findMeshObjectsByIdentifierOrThrow(
            MeshObjectIdentifier[] identifiers )
        throws
            MeshObjectsNotFoundException
    {
        // we replicate the code from our superclass, otherwise we have difficulties with
        // allocating the arrays using the right type
        NetMeshObject [] ret   = new NetMeshObject[ identifiers.length ];
        int              count = 0;
        
        for( int i=0 ; i<identifiers.length ; ++i ) {
            ret[i] = findMeshObjectByIdentifier( identifiers[i] );
            if( ret[i] == null ) {
                ++count;
            }
        }
        if( count == 0 ) {
            return ret;
        }
        NetMeshObjectIdentifier [] notFound = new NetMeshObjectIdentifier[ count ];
        for( int i=identifiers.length-1 ; i>=0 ; --i ) {
            notFound[--count] = (NetMeshObjectIdentifier) identifiers[i];
        }
        throw new MeshObjectsNotFoundException( this, ret, notFound );
        
    }

    /**
     * Obtain a MeshObject whose unique identifier is known.
     * 
     * @param identifier the identifier property of the MeshObject
     * @return the locally found MeshObject, or null if not found locally
     * @throws NetMeshObjectAccessException thrown if something went wrong accessing the MeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    @Override
    public NetMeshObject accessLocally(
            MeshObjectIdentifier identifier )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        NetMeshObject [] found = accessLocally(
                new NetMeshObjectIdentifier[] {
                        (NetMeshObjectIdentifier) identifier
                } );
        
        return found[0];
    }

    /**
     * Obtain N locally available MeshObjects whose unique identifiers are known.
     * 
     * @param identifiers the identifier properties of the MeshObjects
     * @return array of the same length as identifiers, with the locally found MeshObjects filled
     *         in at the same positions. If one or more of the MeshObjects were not found, the respective
     *         location in the array will be null.
     * @throws NetMeshObjectAccessException thrown if something went wrong accessing one or more MeshObjects
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    @Override
    public NetMeshObject [] accessLocally(
            MeshObjectIdentifier [] identifiers )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        NetMeshObjectAccessSpecification [] specs = new NetMeshObjectAccessSpecification[ identifiers.length ];
        for( int i=0 ; i<identifiers.length ; ++i ) {
            specs[i] = theNetMeshObjectAccessSpecificationFactory.obtain(
                    ((NetMeshObjectIdentifier) identifiers[i]).getNetMeshBaseIdentifier(),
                    (NetMeshObjectIdentifier) identifiers[i] );
        }
        NetMeshObject [] ret = accessLocally( specs );
        return ret;
    }

    /**
     * <p>Obtain a local replica of the home NetMeshObject held by a possibly remote NetMeshBase
     * identified by its NetMeshBaseIdentifier.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param remoteLocation the NetMeshBaseIdentifier for the location from where to obtain
     *        a local replica of the remote NetMeshObject
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject accessLocally(
            NetMeshBaseIdentifier remoteLocation )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        NetMeshObjectAccessSpecification path = theNetMeshObjectAccessSpecificationFactory.obtain(
                remoteLocation );

        return accessLocally( path );
    }

    /**
     * <p>Obtain a local replica of the home NetMeshObject held by a possibly remote NetMeshBase
     * identified by its NetMeshBaseIdentifier.
     * Specify a non-default timeout.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param remoteLocation the NetMeshBaseIdentifier for the location from where to obtain
     *        a replica of the remote MeshObject
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds. -1 means "use default".
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            long                    timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        NetMeshObjectAccessSpecification path = theNetMeshObjectAccessSpecificationFactory.obtain(
                remoteLocation );

        return accessLocally( path, timeoutInMillis );
    }
    
    /**
     * <p>Obtain a local replica of the home NetMeshObject held by a possibly remote NetMeshBase
     * identified by its NetMeshBaseIdentifier.
     * Request a non-default CoherenceSpecification.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param remoteLocation the NetMeshBaseIdentifier for the location from where to obtain
     *        a replica of the remote MeshObject
     * @param coherence the CoherenceSpecification requested by the caller
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject accessLocally(
            NetMeshBaseIdentifier  remoteLocation,
            CoherenceSpecification coherence )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        NetMeshObjectAccessSpecification path = theNetMeshObjectAccessSpecificationFactory.obtain( remoteLocation, coherence );
        return accessLocally( path, -1L );
    }
    
    /**
     * <p>Obtain a local replica of the home NetMeshObject held by a possibly remote NetMeshBase
     * identified by its NetMeshBaseIdentifier.
     * Request a non-default CoherenceSpecification.
     * This call does not obtain update rights for the obtained replica.</p>
     *
     * @param remoteLocation the NetMeshBaseIdentifier for the location from where to obtain
     *        a replica of the remote MeshObject
     * @param coherence the CoherenceSpecification requested by the caller
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds. -1 means "use default".
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject accessLocally(
            NetMeshBaseIdentifier  remoteLocation,
            CoherenceSpecification coherence,
            long                   timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        NetMeshObjectAccessSpecification path = theNetMeshObjectAccessSpecificationFactory.obtain( remoteLocation, coherence );
        return accessLocally( path, timeoutInMillis );
    }

    /**
     * <p>Obtain a local replica of a named NetMeshObject held by a possibly remote NetMeshBase
     * identified by its NetMeshBaseIdentifier.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param remoteLocation the NetMeshBaseIdentifier for the location from where to obtain
     *        a replica of the remote MeshObject
     * @param objectIdentifier the NetMeshObjectIdentifier of the remote NetMeshObject
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            NetMeshObjectIdentifier objectIdentifier )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        return accessLocally( remoteLocation, objectIdentifier, -1L );
    }

    /**
     * <p>Obtain a local replica of a named NetMeshObject held by a possibly remote NetMeshBase
     * identified by its NetMeshBaseIdentifier.
     * Specify a non-default timeout.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param remoteLocation the NetMeshBaseIdentifier for the location from where to obtain
     *        a replica of the remote MeshObject
     * @param objectIdentifier the NetMeshObjectIdentifier of the remote NetMeshObject
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds. -1 means "use default".
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            NetMeshObjectIdentifier objectIdentifier,
            long                    timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        NetMeshObjectAccessSpecification path = theNetMeshObjectAccessSpecificationFactory.obtain(
                remoteLocation,
                objectIdentifier );
        
        return accessLocally( path, timeoutInMillis );
    }

    /**
     * <p>Obtain a local replica of a named NetMeshObject held by a possibly remote NetMeshBase
     * identified by its NetMeshBaseIdentifier.
     * Request a non-default CoherenceSpecification.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param remoteLocation the NetMeshBaseIdentifier for the location from where to obtain
     *        a replica of the remote MeshObject
     * @param objectIdentifier the NetMeshObjectIdentifier of the remote NetMeshObject
     * @param coherence the CoherenceSpecification requested by the caller
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            NetMeshObjectIdentifier objectIdentifier,
            CoherenceSpecification  coherence )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        NetMeshObjectAccessSpecification path = theNetMeshObjectAccessSpecificationFactory.obtain(
                remoteLocation,
                objectIdentifier,
                coherence );
        
        return accessLocally( path, -1L );
    }
    
    /**
     * <p>Obtain a local replica of a named NetMeshObject held by a possibly remote NetMeshBase
     * identified by its NetMeshBaseIdentifier.
     * Request a non-default CoherenceSpecification.
     * Specify a non-default timeout.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param remoteLocation the NetMeshBaseIdentifier for the location from where to obtain
     *        a replica of the remote MeshObject
     * @param objectIdentifier the NetMeshObjectIdentifier of the remote NetMeshObject
     * @param coherence the CoherenceSpecification requested by the caller
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds. -1 means "use default".
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            NetMeshObjectIdentifier objectIdentifier,
            CoherenceSpecification  coherence,
            long                    timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        NetMeshObjectAccessSpecification path = theNetMeshObjectAccessSpecificationFactory.obtain(
                remoteLocation,
                objectIdentifier,
                coherence );
        
        return accessLocally( path, timeoutInMillis );
    }

    /**
     * <p>Obtain a local replica of a NetMeshObject using a NetMeshObjectAccessSpecification.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param pathToObject the NetMeshObjectAccessSpecification indicating the location and path to use to access the remote NetMeshObject
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject accessLocally(
            NetMeshObjectAccessSpecification pathToObject )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        return accessLocally( pathToObject, -1L );
    }

    /**
     * <p>Obtain a local replica of a NetMeshObject using a NetMeshObjectAccessSpecification.
     * Specify a non-default timeout.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param pathToObject the NetMeshObjectAccessSpecification indicating the location and path to use to access the remote NetMeshObject
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds. -1 means "use default".
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject accessLocally(
            NetMeshObjectAccessSpecification pathToObject,
            long                             timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        NetMeshObject [] ret = accessLocally( new NetMeshObjectAccessSpecification[] { pathToObject }, timeoutInMillis );
        return ret[0];
    }

    /**
     * <p>Obtain N local replicas of N NetMeshObjects using N NetMeshObjectAccessSpecifications.
     * This call does not obtain update rights for the obtained replicas.</p>
     * <p>There is no requirement that there is any similarity between the elements of pathToObjects;
     * the requested NetMeshObjects may reside in very different NetMeshBases. However, by offering this
     * single call, smart implementations may attempt group requests by shared paths, or execute them
     * in parallel, and thus optimize communications. There is no requirement on implementations to do
     * that, however.</p>
     * 
     * @param pathsToObjects the NetMeshObjectAccessSpecifications indicating the location and paths to use to access the remote NetMeshObjects
     * @return the locally replicated NetMeshObjects, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject [] accessLocally(
            NetMeshObjectAccessSpecification [] pathsToObjects )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        return accessLocally( pathsToObjects, -1L );
    }

    /**
     * <p>Obtain N local replicas of N NetMeshObjects using N NetMeshObjectAccessSpecifications.
     * Specify a non-default timeout.
     * This call does not obtain update rights for the obtained replicas.</p>
     * <p>There is no requirement that there is any similarity between the elements of pathToObjects;
     * the requested NetMeshObjects may reside in very different NetMeshBases. However, by offering this
     * single call, smart implementations may attempt group requests by shared paths, or execute them
     * in parallel, and thus optimize communications. There is no requirement on implementations to do
     * that, however.</p>
     * 
     * @param pathsToObjects the NetMeshObjectAccessSpecifications indicating the location and paths to use to access the remote NetMeshObjects
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds. -1 means "use default".
     * @return the locally replicated NetMeshObjects, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public NetMeshObject [] accessLocally(
            NetMeshObjectAccessSpecification [] pathsToObjects,
            long                                timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException
    {
        if( log.isDebugEnabled() ) {
            log.debug(
                    this
                    + ".accessLocally( "
                    + ArrayHelper.arrayToString( pathsToObjects )
                    + ", "
                    + timeoutInMillis
                    + " )" );
        }
        if( pathsToObjects == null ) {
            return null;
        }
        if( pathsToObjects.length == 0 ) {
            return new NetMeshObject[0];
        }

//        // You must not invoke accessLocally if this Thread has a current Transaction open
//        // FIXME: This code should be there, but then findRelatedObjects (that is invoked during transactions)
//        // cannot perform an accessLocally. This needs further investigation
//        Transaction currentTx = getCurrentTransaction();
//        if( currentTx != null && currentTx.owns( Thread.currentThread())) {
//            throw new IllegalStateException( "You must not invoke accessLocally while your Thread has a Transaction open" );
//        }

        // strip out cyclical and non-sensical items from path
        NetMeshObjectAccessSpecification [] correctRemotePaths = new NetMeshObjectAccessSpecification[ pathsToObjects.length ];
        for( int i=0 ; i<pathsToObjects.length ; ++i ) {
            correctRemotePaths[i] = correctPath( pathsToObjects[i] );
        }
        
        NetMeshObject [] ret       = new NetMeshObject[ correctRemotePaths.length ];
        boolean       [] foundRet  = new boolean[ ret.length ]; // we keep a separate array to keep track of which we found already
                                                                // (or know to be null for sure, which is the same) and which not
        boolean       [] sentQuery = new boolean[ ret.length ]; // we keep a separate array to keep track of which we sent a query already

        // first check whether we have any of them already
        int stillToGet = ret.length;
        for( int i=0 ; i<ret.length ; ++i ) {
            NetMeshObject localObject = null;

            if( correctRemotePaths[i].getNetMeshObjectIdentifier() == null ) {

                if( correctRemotePaths[i].getAccessPath().length == 0 ) {

                    ret[i]       = getHomeObject();
                    foundRet[i]  = true;
                    sentQuery[i] = true;
                    --stillToGet;
                    
                    continue;
                }
            } else {
                // else: we need to keep looking remotely, we don't know locally

                localObject = findMeshObjectByIdentifier( correctRemotePaths[i].getNetMeshObjectIdentifier() );
                if( localObject != null ) {
                    ret[i]       = localObject;
                    foundRet[i]  = true;
                    sentQuery[i] = true;
                    --stillToGet;

                } else if( correctRemotePaths[i].getAccessPath() == null || correctRemotePaths[i].getAccessPath().length == 0 ) {
                    ret[i]       = null;
                    foundRet[i]  = true; // we found it -- well, we know for sure it isn't there
                    sentQuery[i] = true;
                    --stillToGet;
                }
            }
        }

        // return if it looks like we are done
        if( stillToGet == 0 ) {
            return ret;
        }

        // make sure caller has permission
        if( theAccessManager != null ) {
            NetAccessManager realAccessManager = (NetAccessManager) theAccessManager;
            realAccessManager.checkPermittedAccessLocally( this, correctRemotePaths ); // may throw exception
        }

        // we collect all exceptions here, and the corresponding NetworkPaths
        ArrayList<Exception>                          thrownExceptions  = new ArrayList<Exception>();
        ArrayList<NetMeshObjectAccessSpecification[]> failedObjectPaths = new ArrayList<NetMeshObjectAccessSpecification[]>();

        boolean ok;
        try {
            theAccessLocallySynchronizer.beginTransaction();

            // now break down the still remaining objects into chunks, one chunk per
            // different proxy, and get them until we have everything.
            long    realTimeout = 0L; //

            int pivotIndex = 0;
            Proxy [] proxyKeeper = new Proxy[ stillToGet ]; // keep Proxies from being garbage collected while queries are ongoing
            int proxyKeeperCount = 0;

            while( stillToGet > 0 ) {
                // find the first one we have not gotten yet
                for( ; foundRet[pivotIndex] || sentQuery[pivotIndex] ; ++pivotIndex )
                {}

                int runningIndex = pivotIndex;

                // now find all that have the same first NetMeshBaseIdentifier element
                NetMeshBaseAccessSpecification pivot     = correctRemotePaths[ runningIndex ].getAccessPath()[0];
                NetMeshBaseIdentifier          pivotName = pivot.getNetMeshBaseIdentifier();
                CoherenceSpecification         pivotCalc = pivot.getCoherenceSpecification();

                // obtain a new set of object names that we still need to get
                NetMeshObjectAccessSpecification [] nextObjectPaths = new NetMeshObjectAccessSpecification[ stillToGet ]; // potentially over-allocated

                nextObjectPaths[0] = theNetMeshObjectAccessSpecificationFactory.obtain(
                        ArrayHelper.subarray( correctRemotePaths[ runningIndex ].getAccessPath(), 1, NetMeshBaseAccessSpecification.class ),
                        correctRemotePaths[ runningIndex ].getNetMeshObjectIdentifier() );

                sentQuery[ runningIndex ] = true;
                int nextObjectCount = 1;

                for( ++runningIndex ; runningIndex < correctRemotePaths.length ; ++runningIndex ) {
                    if( foundRet[runningIndex] || sentQuery[runningIndex] ) {
                        continue; // skip
                    }

                    if( ! pivotName.equals( correctRemotePaths[runningIndex].getAccessPath()[0].getNetMeshBaseIdentifier() )) {
                        continue; // has different pivot
                    }

                    nextObjectPaths[ nextObjectCount ] = theNetMeshObjectAccessSpecificationFactory.obtain(
                            ArrayHelper.subarray( correctRemotePaths[ runningIndex ].getAccessPath(), 1, NetMeshBaseAccessSpecification.class ),
                            correctRemotePaths[ runningIndex ].getNetMeshObjectIdentifier() );

                    sentQuery[ runningIndex ] = true;

                    ++nextObjectCount;
                }

                // we may have over-allocated, so reduce if necessary
                if( nextObjectCount < nextObjectPaths.length ) {
                    nextObjectPaths = ArrayHelper.subarray( nextObjectPaths, 0, nextObjectCount, NetMeshObjectAccessSpecification.class );
                }

                Proxy theProxy = null;
                try {
                    theProxy = obtainProxyFor( pivotName, pivotCalc ); // this triggers the Shadow creation in the right subclasses
                    if( theProxy != null ) {
                        long requestedTimeout = theProxy.obtainReplicas( nextObjectPaths, timeoutInMillis ); // FIXME? Should we use a different timeout here?
                        realTimeout = Math.max( realTimeout, requestedTimeout );
                    }

                } catch( FactoryException ex ) {
                    thrownExceptions.add( ex );
                    failedObjectPaths.add( withPrefix( pivot, nextObjectPaths ) );
                }
                proxyKeeper[ proxyKeeperCount++ ] = theProxy;
                stillToGet -= nextObjectPaths.length;
            }

            if( timeoutInMillis > 0 ) { // if something has been specified
                realTimeout = timeoutInMillis;
            }

            ok = theAccessLocallySynchronizer.join( realTimeout );

            if( !ok && thrownExceptions.isEmpty() ) {
                log.warn( this + ".accessLocally() timed out trying to reach " + ArrayHelper.arrayToString( pathsToObjects ) + ", timeout: " + realTimeout );
            }

            if( realTimeout < 0L ) {
                ok = true;
            }
            theAccessLocallySynchronizer.endTransaction();

        } catch( ReturnSynchronizerException ex ) {
            log.error( ex );
            ok = false;

        } catch( InterruptedException ex ) {
            log.error( ex );
            ok = false;
        }

        if( ! thrownExceptions.isEmpty() ) {
            ok = false;
        }

        // now insert the results
        boolean allFound  = true;
        boolean someFound = false;
        for( int i=0 ; i<correctRemotePaths.length ; ++i ) {
            if( foundRet[i] ) {
                someFound = true;
                continue;
            }

            NetMeshObject newlyFound = findMeshObjectByIdentifier( correctRemotePaths[i].getNetMeshObjectIdentifier() );
            if( newlyFound == null ) {
                allFound = false;

            } else {
                someFound = true;
                ret[i]    = newlyFound;
            }
        }

        if( ok ) { // all queries returned, definitive answer
            return ret;

        } else if( allFound ) { // we timed out, but we have the answer anyway
            return ret;

        } else if( thrownExceptions.isEmpty() ) { // we timed out, but have a partial result, future results still incoming
            throw new NetMeshObjectAccessException(
                    this,
                    ret,
                    correctRemotePaths,
                    new RemoteQueryTimeoutException.QueryIsOngoing( this, someFound, ret ));

        } else {
            Exception                           firstException         = thrownExceptions.get( 0 );
            NetMeshObjectAccessSpecification [] firstFailedObjectPaths = failedObjectPaths.get( 0 );

            throw new NetMeshObjectAccessException( this, ret, firstFailedObjectPaths, firstException ); // FIXME
        }
    }
    
    /**
     * This internal helper strips loops and other insecure and non-sensical things
     * out of a NetMeshObjectAccessSpecification. (FIXME: this needs to cover more cases)
     * 
     * @param raw the input NetMeshObjectAccessSpecification that shall be corrected
     * @return the corrected NetMeshObjectAccessSpecification
     */
    protected NetMeshObjectAccessSpecification correctPath(
            NetMeshObjectAccessSpecification raw )
    {
        if( raw == null ) {
            return null;
        }

        NetMeshBaseAccessSpecification [] path = raw.getAccessPath();

        if( path == null ) {
            return raw;
        }

        for( int i=path.length-1 ; i>=0 ; --i ) {
            NetMeshBaseIdentifier candidateName = path[i].getNetMeshBaseIdentifier();
            if( theMeshBaseIdentifier.equals( candidateName )) {

                return theNetMeshObjectAccessSpecificationFactory.obtain(
                       ArrayHelper.subarray( path, i+1, NetMeshBaseAccessSpecification.class ),
                       raw.getNetMeshObjectIdentifier() );
            }
        }
        return raw;
    }
    
    /**
     * <p>Obtain a manager for MeshObject lifecycles.</p>
     * 
     * @return a MeshBaseLifecycleManager that works on this MeshBase
     */
    @Override
    public AnetMeshBaseLifecycleManager getMeshBaseLifecycleManager()
    {
        return (AnetMeshBaseLifecycleManager) super.getMeshBaseLifecycleManager();
    }

    /**
     * Obtain the NetAccessManager associated with this NetMeshBase, if any.
     * The NetAccessManager controls access to the NetMeshObjects in this NetMeshBase.
     * A subtype of AccessManagerm NetAccessManager, is needed that also defines access rights related to
     * operations that exist on NetMeshBase and NetMeshObject but not on their supertypes.
     *
     * @return the NetAccessManager, if any
     */
    @Override
    public NetAccessManager getAccessManager()
    {
        return (NetAccessManager) super.getAccessManager();
    }

    /**
     * Obtain a factory for NetMeshObjectIdentifiers that is appropriate for this NetMeshBase.
     *
     * @return the factory for NetMeshObjectIdentifiers
     */
    @Override
    public NetMeshObjectIdentifierFactory getMeshObjectIdentifierFactory()
    {
        return (NetMeshObjectIdentifierFactory) super.getMeshObjectIdentifierFactory();
    }

    /**
     * Obtain a factory for NetMeshBaseIdentifiers that is appropriate for this NetMeshBase.
     * 
     * @return the factory for NetMeshBaseIdentifiers
     */
    public NetMeshBaseIdentifierFactory getMeshBaseIdentifierFactory()
    {
        return theMeshBaseIdentifierFactory;
    }
    
    /**
     * Obtain a factory for NetMeshObjectAccessSpecifications that is appropriate for this NetMeshBase.
     *
     * @return the factory for NetMeshObjectAccessSpecifications
     */
    public NetMeshObjectAccessSpecificationFactory getNetMeshObjectAccessSpecificationFactory()
    {
        return theNetMeshObjectAccessSpecificationFactory;
    }
    
    /**
     * Set the default value for a new NetMeshObject's willGiveUpLock property if not otherwise specified.
     *
     * @param newValue the new value
     * @see #getDefaultWillGiveUpLock
     */
    public void setDefaultWillGiveUpLock(
            boolean newValue )
    {
        theDefaultWillGiveUpLock = newValue;
    }
    
    /**
     * Obtain the default value for a new NetMeshObject's willGiveUpLock property if not otherwise specified.
     *
     * @return the default value
     * @see #setDefaultWillGiveUpLock
     */
    public boolean getDefaultWillGiveUpLock()
    {
        return theDefaultWillGiveUpLock;
    }

    /**
     * Set the default value for a new NetMeshObject's willGiveUpHomeReplica property if not otherwise specified.
     *
     * @param newValue the new value
     * @see #getDefaultWillGiveUpHomeReplica
     */
    public void setDefaultWillGiveUpHomeReplica(
            boolean newValue )
    {
        theDefaultWillGiveUpHomeReplica = newValue;
    }
    
    /**
     * Obtain the default value for a new NetMeshObject's willGiveUpHomeReplica property if not otherwise specified.
     *
     * @return the default value
     * @see #setDefaultWillGiveUpHomeReplica
     */
    public boolean getDefaultWillGiveUpHomeReplica()
    {
        return theDefaultWillGiveUpHomeReplica;
    }

     /**
     * Obtain or obtain a Proxy for communication with a NetMeshBase at the specified NetMeshBaseIdentifier.
     * 
     * @param networkIdentifier the NetMeshBaseIdentifier
     * @param coherence the CoherenceSpecification to use, if any
     * @return the Proxy
     * @throws FactoryException thrown if the Proxy could not be created
     * @see #getProxyFor
     */
    public Proxy obtainProxyFor(
            NetMeshBaseIdentifier  networkIdentifier,
            CoherenceSpecification coherence )
        throws
            FactoryException
    {
        return theProxyManager.obtainFor( networkIdentifier, coherence );
    }

    /**
     * Obtain an existing Proxy to the specified NetMeshBaseIdentifier. Return null if no such
     * Proxy exists. Do not attempt to obtain one.
     *
     * @param networkIdentifier the NetMeshBaseIdentifier
     * @return the Proxy
     * @see #obtainProxyFor
     */
    public Proxy getProxyFor(
            NetMeshBaseIdentifier  networkIdentifier )
    {
        return theProxyManager.get( networkIdentifier );
    }

    /**
     * <p>Obtain an Iterator over the set of currently active Proxies.</p>
     *
     * @return the CursorIterator over the Proxies
     */
    public CursorIterator<Proxy> proxies()
    {
        checkDead();
        
        return theProxyManager.proxies();
    }

    /**
     * Obtain this NetMeshBase as a NameServer for its Proxies, keyed by the NetMeshBaseIdentifiers
     * of the partner NetMeshBases.
     * 
     * @return the NameServer mapping NetMeshBaseIdentifiers to Proxies.
     */
    public NameServer<NetMeshBaseIdentifier,Proxy> getAsProxyNameServer()
    {
        return theProxyManager;
    }
    
    /**
     * Determine the Proxy, if any, that originated the current Thread.
     *
     * @return the Proxy
     */
    public Proxy determineIncomingProxy()
    {
        synchronized( theThreadProxyTable ) {
            Proxy ret = theThreadProxyTable.get( Thread.currentThread() );

            return ret;
        }
    }

    /**
     * Set the incoming Proxy for this Thread. To be called only by Proxies
     * about themselves.
     *
     * @param incomingProxy the incoming Proxy for this Thread
     */
    public void registerIncomingProxy(
            Proxy incomingProxy )
    {
        checkDead();

        synchronized( theThreadProxyTable ) {
            Proxy found = theThreadProxyTable.put( Thread.currentThread(), incomingProxy );
            if( found != null ) {
                log.error( this + ": This thread has a proxy already: " + found + ", incoming is " + incomingProxy );
            }
        }
    }

    /**
     * Unregister the incoming Proxy for this Thread. To be called only be Proxies
     * about themselves.
     */
    public void unregisterIncomingProxy()
    {
        synchronized( theThreadProxyTable ) {
            Proxy found = theThreadProxyTable.remove( Thread.currentThread() );

            if( found == null ) {
                log.error( "This thread had no proxy" );
            }
        }
    }

    /**
     * Obtain the underlying AccessLocallySynchronizer for Xpriso communication.
     * Not to be called by the application programmer.
     *
     * @return the underlying AccessLocallySynchronizer
     */
    public final AccessLocallySynchronizer getAccessLocallySynchronizer()
    {
        return theAccessLocallySynchronizer;
    }

    /**
     * <p>Attempt to update the set of NetMeshObjects to their most current state. Usually, InfoGrid manages
     * this automatically and on its own schedule. However, under some circumstances the user may
     * want to request an immediate update; that is what this method is for.</p>
     *
     * <p>In many cases, this method will do nothing as the local replicas are already most current.</p>
     *
     * @param toFreshen the set of NetMeshObjects to freshen
     */
    public void freshenNow(
            NetMeshObject [] toFreshen )
    {
        freshenNow( toFreshen, -1L );
    }

    /**
     * <p>Attempt to update the set of NetMeshObjects to their most current state. Usually, InfoGrid manages
     * this automatically and on its own schedule. However, under some circumstances the user may
     * want to request an immediate update; that is what this method is for.</p>
     *
     * <p>In many cases, this method will do nothing as the local replicas are already most current.</p>
     *
     * @param toFreshen the set of NetMeshObjects to freshen
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     */
    public void freshenNow(
            NetMeshObject [] toFreshen,
            long             duration )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "freshenNow", toFreshen, duration );
        }

        // we need to group by Proxy

        NetMeshObject [] workDown       = ArrayHelper.copyIntoNewArray( toFreshen, NetMeshObject.class );
        long             actualDuration = 0L;
        try {
            theAccessLocallySynchronizer.beginTransaction();

            for( int i=0 ; i<workDown.length ; ++i ) {
                if( workDown[i] == null ) {
                    continue;
                }

                Proxy towardsLock = workDown[i].getProxyTowardsLockReplica();
                if( towardsLock == null ) {
                    // nothing to do here
                    workDown[i] = null;
                    continue;
                }

                NetMeshObject [] thisChunk = new NetMeshObject[ workDown.length ]; // potentially over-allocated

                thisChunk[0] = workDown[i];
                workDown[i]  = null;
                int counter  = 1; // have first element already

                for( int j=i+1 ; j<workDown.length ; ++j ) {
                    if( workDown[j] == null ) {
                        continue;
                    }
                    Proxy towardsLock2 = workDown[j].getProxyTowardsLockReplica();
                    if( towardsLock2 == null || towardsLock2 != towardsLock ) {
                        continue;
                    }
                    thisChunk[counter++] = workDown[j];
                    workDown[j] = null;
                }
                if( counter < thisChunk.length ) {
                    thisChunk = ArrayHelper.copyIntoNewArray( thisChunk, 0, counter, NetMeshObject.class );
                }
                long requestedDuration = towardsLock.freshen( thisChunk, duration );
                actualDuration = Math.max( actualDuration, requestedDuration );

            }
            if( actualDuration > duration && duration >= 0L ) {
                actualDuration = duration;
            }
            theAccessLocallySynchronizer.join( actualDuration );

            theAccessLocallySynchronizer.endTransaction();

        } catch( ReturnSynchronizerException ex ) {
            log.error( ex );
        } catch( InterruptedException ex ) {
            log.error( ex );
        }

    }

    /**
     * Set a XprisoMessageLogger for all incoming and outgoing XprisoMessages.
     *
     * @param newValue the new value
     */
    public void setXprisoMessageLogger(
                XprisoMessageLogger newValue )
    {
        theMessageLogger = newValue;
    }

    /**
     * Obtain the currently active XprisoMessageLogger, if any.
     *
     * @return the currently active XprisoMessageLogger, if any
     */
    public XprisoMessageLogger getXprisoMessageLogger()
    {
        return theMessageLogger;
    }

    /**
      * Clean up.
      * 
      * @param isPermanent if true, this MeshBase will go away permanmently; if false, it may come alive again some time later
      */
    @Override
    protected void internalDie(
            boolean isPermanent )
    {
        theProxyManager.die( isPermanent );

        theProxyManager = null;
    }

    /**
     * Tell the Proxies once a Transaction has been committed.
     *
     * @param tx Transaction the Transaction that was committed
     */
    @Override
    protected void transactionCommittedHook(
            Transaction tx )
    {
        super.transactionCommittedHook( tx );
        
        for( Proxy current : proxies() ) {
            current.transactionCommitted( tx );
        }
    }

    /**
     * Helper method to prefix an array of NetMeshObjectAccessSpecifications with
     * the same NetMeshBaseAccessSpecification.
     * 
     * @param prefix the prefix for the NetworkPaths
     * @param paths the array of NetworkPaths that needs to be prefixed
     * @return an array with the prefixed paths in the same order
     */
    protected NetMeshObjectAccessSpecification [] withPrefix(
            NetMeshBaseAccessSpecification      prefix,
            NetMeshObjectAccessSpecification [] paths )
    {
        NetMeshObjectAccessSpecification [] ret = new NetMeshObjectAccessSpecification[ paths.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = theNetMeshObjectAccessSpecificationFactory.obtain(
                    ArrayHelper.append( prefix, paths[i].getAccessPath(), NetMeshBaseAccessSpecification.class ),
                    paths[i].getNetMeshObjectIdentifier() );
        }
        return ret;
    }

    /**
     * <p>Find an already-created ForwardReference in this NetMeshBase. Specify the NetMeshBaseIdentifier
     * of the NetMeshBase whose home object the to-be-found ForwardReference references.</p>
     * <p>If not found, returns <code>null</code>.</p>
     *
     * @param meshObjectLocation identifies the data source where the MeshObject can be found
     * @return the found ForwardReference, or null if not found
     * @see #findMeshObjectByIdentifierOrThrow
     */
    public NetMeshObject findForwardReference(
            NetMeshBaseIdentifier meshObjectLocation )
    {
        return findForwardReference( getNetMeshObjectAccessSpecificationFactory().obtain( meshObjectLocation ));
    }

    /**
     * <p>Find an already-created ForwardReference in this NetMeshBase. Specify the NetMeshBaseIdentifier
     * of the NetMeshBase which contains the NetMeshObject that the to-be-found ForwardReference references.</p>
     * <p>If not found, returns <code>null</code>.</p>
     *
     * @param meshObjectLocation identifies the data source where the MeshObject can be found
     * @param identifier the Identifier of the MeshObject into which this ForwardReference resolves
     * @return the found ForwardReference, or null if not found
     * @see #findMeshObjectByIdentifierOrThrow
     */
    public NetMeshObject findForwardReference(
            NetMeshBaseIdentifier   meshObjectLocation,
            NetMeshObjectIdentifier identifier )
    {
        return findForwardReference( getNetMeshObjectAccessSpecificationFactory().obtain( meshObjectLocation, identifier ));
    }

    /**
     * <p>Find an already-created ForwardReference in this NetMeshBase. Specify the NetMeshObjectAccessSpecification
     *    of the ForwardReference.</p>
     * <p>If not found, returns <code>null</code>.</p>
     *
     * @param pathToObject specifies where and how the MeshObject can be found
     * @return the found ForwardReference, or null if not found
     * @see #findMeshObjectByIdentifierOrThrow
     */
    public NetMeshObject findForwardReference(
            NetMeshObjectAccessSpecification pathToObject )
    {
        NetMeshObjectIdentifier identifier = pathToObject.getNetMeshObjectIdentifier();
        NetMeshObject           ret        = findMeshObjectByIdentifier( identifier );

        return ret;
    }

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( NetMeshBase.class );

    /**
     * The default value for the willGiveUpLock property of newly created NetMeshObjects.
     */
    private boolean theDefaultWillGiveUpLock = theResourceHelper.getResourceBooleanOrDefault(
            "DefaultWillGiveUpLock",
            true );

    /**
     * The default value for the willGiveUpHomeReplica property of newly created NetMeshObjects.
     */
    private boolean theDefaultWillGiveUpHomeReplica = theResourceHelper.getResourceBooleanOrDefault(
            "DefaultWillGiveUpHomeReplica",
            true );

    /**
     * The factory for MeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory;

    /**
     * The factory for NetMeshObjectAccessSpecifications.
     */
    protected NetMeshObjectAccessSpecificationFactory theNetMeshObjectAccessSpecificationFactory;
    
    /**
     * This object helps us with synchronizing results we are getting asynchronously.
     */
    protected AccessLocallySynchronizer theAccessLocallySynchronizer;
    
    /**
     * We delegate to this ProxyManager to manage our Proxies.
     */
    protected ProxyManager theProxyManager;
    
    /**
     * Table to map Threads to Proxies.
     */
    protected final HashMap<Thread,Proxy> theThreadProxyTable = new HashMap<Thread,Proxy>();

    /**
     * Logs incoming and outgoing XprisoMessages.
     */
    protected XprisoMessageLogger theMessageLogger;
}
