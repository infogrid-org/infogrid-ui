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

package org.infogrid.meshbase.net;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshObjectsNotFoundException;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.meshbase.net.a.AccessLocallySynchronizer;
import org.infogrid.meshbase.net.xpriso.logging.XprisoMessageLogger;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.FactoryException;
import org.infogrid.util.NameServer;

/**
 * A MeshBase that is networked, i.e. that can replicate data that resides in one
 * or more different locations on the network. A number of methods defined in the
 * supertype {@link org.infogrid.meshbase.MeshBase} are repeated
 * here as they are known to return more specific subtypes.
 */
public interface NetMeshBase
        extends
            MeshBase
{
    /**
     * Obtain the NetMeshBaseIdentifier at which this NetMeshBase is located.
     * 
     * @return the NetMeshBaseIdentifier
     */
    public abstract NetMeshBaseIdentifier getIdentifier();

    /**
      * Obtain the NetMeshBase's home object. The home object is
      * the only well-known object in a NetMeshBase. It is guaranteed to exist and
      * cannot be deleted.
      *
      * @return the NetMeshObject that is this NetMeshBase's home object
      */
    public abstract NetMeshObject getHomeObject();

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
    public abstract NetMeshObject findMeshObjectByIdentifier(
            MeshObjectIdentifier identifier );

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
    public abstract NetMeshObject [] findMeshObjectsByIdentifier(
            MeshObjectIdentifier [] identifiers );

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
    public abstract NetMeshObject findMeshObjectByIdentifierOrThrow(
            MeshObjectIdentifier identifier )
        throws
            MeshObjectsNotFoundException;

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
    public abstract NetMeshObject [] findMeshObjectsByIdentifierOrThrow(
            MeshObjectIdentifier[] identifiers )
        throws
            MeshObjectsNotFoundException;

    /**
     * <p>Find an already-created ForwardReference in this NetMeshBase. Specify the NetMeshBaseIdentifier
     * of the NetMeshBase whose home object the to-be-found ForwardReference references.</p>
     * <p>If not found, returns <code>null</code>.</p>
     *
     * @param meshObjectLocation identifies the data source where the NetMeshBase can be found
     * @return the found ForwardReference, or null if not found
     * @see #findMeshObjectByIdentifierOrThrow
     */
    public abstract NetMeshObject findForwardReference(
            NetMeshBaseIdentifier meshObjectLocation );

    /**
     * <p>Find an already-created ForwardReference in this NetMeshBase. Specify the NetMeshBaseIdentifier
     * of the NetMeshBase which contains the NetMeshObject that the to-be-found ForwardReference references.</p>
     * <p>If not found, returns <code>null</code>.</p>
     *
     * @param meshObjectLocation identifies the data source where the NetMeshBase can be found
     * @param identifier the Identifier of the MeshObject into which this ForwardReference resolves
     * @return the found ForwardReference, or null if not found
     * @see #findMeshObjectByIdentifierOrThrow
     */
    public abstract NetMeshObject findForwardReference(
            NetMeshBaseIdentifier   meshObjectLocation,
            NetMeshObjectIdentifier identifier );

    /**
     * <p>Find an already-created ForwardReference in this NetMeshBase. Specify the NetMeshObjectAccessSpecification
     *    of the ForwardReference.</p>
     * <p>If not found, returns <code>null</code>.</p>
     *
     * @param pathToObject specifies where and how the NetMeshBase can be found
     * @return the found ForwardReference, or null if not found
     * @see #findMeshObjectByIdentifierOrThrow
     */
    public abstract NetMeshObject findForwardReference(
            NetMeshObjectAccessSpecification pathToObject );

    /**
     * Obtain a MeshObject whose unique identifier is known.
     * 
     * @param identifier the identifier property of the MeshObject
     * @return the locally found MeshObject, or null if not found locally
     * @throws NetMeshObjectAccessException thrown if something went wrong accessing the MeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject accessLocally(
            MeshObjectIdentifier identifier )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

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
    public abstract NetMeshObject [] accessLocally(
            MeshObjectIdentifier [] identifiers )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

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
    public abstract NetMeshObject accessLocally(
            NetMeshBaseIdentifier remoteLocation )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

    /**
     * <p>Obtain a local replica of the home NetMeshObject held by a possibly remote NetMeshBase
     * identified by its NetMeshBaseIdentifier.
     * Specify a non-default timeout.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param remoteLocation the NetMeshBaseIdentifier for the location from where to obtain
     *        a replica of the remote MeshObject
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            long                    timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

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
    public abstract NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            CoherenceSpecification  coherence )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

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
    public abstract NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            NetMeshObjectIdentifier objectIdentifier )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

    /**
     * <p>Obtain a local replica of a named NetMeshObject held by a possibly remote NetMeshBase
     * identified by its NetMeshBaseIdentifier.
     * Specify a non-default timeout.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param remoteLocation the NetMeshBaseIdentifier for the location from where to obtain
     *        a replica of the remote MeshObject
     * @param objectIdentifier the NetMeshObjectIdentifier of the remote NetMeshObject
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            NetMeshObjectIdentifier objectIdentifier,
            long                    timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

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
    public abstract NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            NetMeshObjectIdentifier objectIdentifier,
            CoherenceSpecification  coherence )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

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
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject accessLocally(
            NetMeshBaseIdentifier   remoteLocation,
            NetMeshObjectIdentifier objectIdentifier,
            CoherenceSpecification  coherence,
            long                    timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

    /**
     * <p>Obtain a local replica of a NetMeshObject using a NetMeshObjectAccessSpecification.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param pathToObject the NetMeshObjectAccessSpecification indicating the location and path to use to access the remote NetMeshObject
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject accessLocally(
            NetMeshObjectAccessSpecification pathToObject )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

    /**
     * <p>Obtain a local replica of a NetMeshObject using a NetMeshObjectAccessSpecification.
     * Specify a non-default timeout.
     * This call does not obtain update rights for the obtained replica.</p>
     * 
     * @param pathToObject the NetMeshObjectAccessSpecification indicating the location and path to use to access the remote NetMeshObject
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds
     * @return the locally replicated NetMeshObject, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject accessLocally(
            NetMeshObjectAccessSpecification pathToObject,
            long                             timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

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
    public abstract NetMeshObject [] accessLocally(
            NetMeshObjectAccessSpecification [] pathsToObjects )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

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
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds
     * @return the locally replicated NetMeshObjects, or null if not found
     * @throws NetMeshObjectAccessException thrown if something went wrong attempting to access the NetMeshObject
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public abstract NetMeshObject [] accessLocally(
            NetMeshObjectAccessSpecification [] pathsToObjects,
            long                                timeoutInMillis )
        throws
            NetMeshObjectAccessException,
            NotPermittedException;

    /**
     * <p>Obtain a manager for NetMeshObject lifecycles.</p>
     * 
     * @return a NetMeshBaseLifecycleManager that works on this MeshBase
     */
    public abstract NetMeshBaseLifecycleManager getMeshBaseLifecycleManager();

    /**
     * Obtain the NetAccessManager associated with this NetMeshBase, if any.
     * The NetAccessManager controls access to the NetMeshObjects in this NetMeshBase.
     * A subtype of AccessManagerm NetAccessManager, is needed that also defines access rights related to
     * operations that exist on NetMeshBase and NetMeshObject but not on their supertypes.
     *
     * @return the NetAccessManager, if any
     */
    public abstract NetAccessManager getAccessManager();

    /**
     * Obtain a factory for NetMeshObjectIdentifiers that is appropriate for this NetMeshBase.
     *
     * @return the factory for NetMeshObjectIdentifiers
     */
    public abstract NetMeshObjectIdentifierFactory getMeshObjectIdentifierFactory();

    /**
     * Obtain a factory for NetMeshBaseIdentifiers that is appropriate for this NetMeshBase.
     * 
     * @return the factory for NetMeshBaseIdentifiers
     */
    public abstract NetMeshBaseIdentifierFactory getMeshBaseIdentifierFactory();
    
    /**
     * Obtain a factory for NetMeshObjectAccessSpecifications that is appropriate for this NetMeshBase.
     *
     * @return the factory for NetMeshObjectAccessSpecifications
     */
    public abstract NetMeshObjectAccessSpecificationFactory getNetMeshObjectAccessSpecificationFactory();
    
    /**
     * Obtain or create a Proxy for communication with a NetMeshBase at the specified NetMeshBaseIdentifier.
     * 
     * @param networkIdentifier the NetMeshBaseIdentifier
     * @param coherence the CoherenceSpecification to use, if any
     * @return the Proxy
     * @throws FactoryException thrown if the Proxy could not be created
     * @see #getProxyFor
     */
    public abstract Proxy obtainProxyFor(
            NetMeshBaseIdentifier  networkIdentifier,
            CoherenceSpecification coherence )
         throws
            FactoryException;

    /**
     * Obtain an existing Proxy to the specified NetMeshBaseIdentifier. Return null if no such
     * Proxy exists. Do not attempt to create one.
     *
     * @param networkIdentifier the NetMeshBaseIdentifier
     * @return the Proxy
     * @see #obtainProxyFor
     */
    public abstract Proxy getProxyFor(
            NetMeshBaseIdentifier networkIdentifier );

    /**
     * <p>Obtain an Iterator over the set of currently active Proxies.</p>
     *
     * @return the CursorIterator over the Proxies
     */
    public abstract CursorIterator<Proxy> proxies();
    
    /**
     * Obtain this NetMeshBase as a NameServer for its Proxies, keyed by the NetMeshBaseIdentifiers
     * of the partner NetMeshBases.
     * 
     * @return the NameServer mapping NetMeshBaseIdentifiers to Proxies.
     */
    public abstract NameServer<NetMeshBaseIdentifier,Proxy> getAsProxyNameServer();

    /**
     * Determine the Proxy, if any, that originated the current Thread.
     *
     * @return the Proxy
     */
    public abstract Proxy determineIncomingProxy();

    /**
     * Set the incoming Proxy for this Thread. To be called only by Proxies
     * about themselves.
     *
     * @param incomingProxy the incoming Proxy for this Thread
     */
    public abstract void registerIncomingProxy(
            Proxy incomingProxy );
    
    /**
     * Unregister the incoming Proxy for this Thread. To be called only be Proxies
     * about themselves.
     */
    public abstract void unregisterIncomingProxy();

    /**
     * Obtain the underlying AccessLocallySynchronizer for synchronizing the return of parallel
     * accessLocally invocation.
     * Not to be called by the application programmer.
     *
     * @return the underlying AccessLocallySynchronizer
     */
    public abstract AccessLocallySynchronizer getAccessLocallySynchronizer();

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
            NetMeshObject [] toFreshen );

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
            long             duration );

    /**
     * Set a XprisoMessageLogger for all incoming and outgoing XprisoMessages.
     *
     * @param newValue the new value
     */
    public abstract void setXprisoMessageLogger(
                XprisoMessageLogger newValue );

    /**
     * Obtain the currently active XprisoMessageLogger, if any.
     *
     * @return the currently active XprisoMessageLogger, if any
     */
    public abstract XprisoMessageLogger getXprisoMessageLogger();

    /**
     * The name of the bound property we use to express "the timeout for accessLocally has changed".
     */
    public static final String ACCESS_LOCALLY_TIMES_OUT_AFTER_PROPERTY = "AccessLocallyTimesOutAfter";
}
