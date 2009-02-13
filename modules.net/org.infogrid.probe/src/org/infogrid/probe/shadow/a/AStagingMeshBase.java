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

package org.infogrid.probe.shadow.a;

import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.meshbase.net.IterableNetMeshBaseDifferencer;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.proxy.ProxyManager;
import org.infogrid.meshbase.net.a.AnetMeshBase;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.util.CachingMap;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.context.Context;

/**
 * Factors out common functionality of ShadowMeshBases and StagingMeshBase.
 */
public abstract class AStagingMeshBase
        extends
            AnetMeshBase
        implements
            StagingMeshBase
{
    /**
     * Constructor for subclasses only. This does not initialize content.
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
    protected AStagingMeshBase(
            NetMeshBaseIdentifier                       identifier,
            NetMeshObjectIdentifierFactory              identifierFactory,
            NetMeshBaseIdentifierFactory                meshBaseIdentifierFactory,
            NetMeshObjectAccessSpecificationFactory     netMeshObjectAccessSpecificationFactory,
            MeshObjectSetFactory                        setFactory,
            ModelBase                                   modelBase,
            AStagingMeshBaseLifecycleManager            life,
            NetAccessManager                            accessMgr,
            CachingMap<MeshObjectIdentifier,MeshObject> cache,
            ProxyManager                                proxyManager,
            Context                                     context )
    {
        super(  identifier,
                identifierFactory,
                meshBaseIdentifierFactory,
                netMeshObjectAccessSpecificationFactory,
                setFactory,
                modelBase,
                life,
                accessMgr,
                cache,
                proxyManager,
                context );

        setDefaultWillGiveUpLock( false ); // a shadow, after all
    }

    /**
     * <p>Obtain a manager for object lifecycles.</p>
     * 
     * @return a MeshBaseLifecycleManager that works on this MeshBase with the specified parameters
     */
    @Override
    public AStagingMeshBaseLifecycleManager getMeshBaseLifecycleManager()
    {
        return (AStagingMeshBaseLifecycleManager) theMeshBaseLifecycleManager;
    }
    
    /**
     * Enable the ProbeDispatcher to obtain the home object in the StagingMeshBase.
     *
     * @param timeCreated the creation date for the home object
     */
    public void initializeHomeObject(
            long timeCreated )
    {
        super.initializeHomeObject( timeCreated, timeCreated, timeCreated );
    }

    /**
     * Map iterator.
     *
     * @return the iterator
     */
    public final CursorIterator<MeshObject> getIterator()
    {
        return iterator();
    }

    /**
     * Update the lastUpdated property. This is delegated to here so ShadowMeshBases
     * and do this differently than regular NetMeshBases.
     *
     * @param timeUpdated the time to set to, or -1L to indicate the current time
     * @param lastTimeUpdated the time this MeshObject was updated the last time
     * @return the time to set to
     */
    @Override
    public long calculateLastUpdated(
            long timeUpdated,
            long lastTimeUpdated )
    {
        return lastTimeUpdated;
    }

    /**
     * Update the lastRead property. This does not trigger an event generation -- not necessary.
     * This may be overridden.
     *
     * @param timeRead the time to set to, or -1L to indicate the current time
     * @param lastTimeRead the time this MeshObject was read the last time
     * @return the time to set to
     */
    @Override
    public long calculateLastRead(
            long timeRead,
            long lastTimeRead )
    {
        return lastTimeRead;
    }

    /**
     * Determine the number of MeshObjects in this MeshBase.
     *
     * @return the number of MeshObjects in this MeshBase
     */
    public int size()
    {
        return theCache.size();
    }
   
    /**
     * Determine the number of MeshObjects in this MeshBase. This redundant method
     * is provided to make life easier for JavaBeans-aware software.
     *
     * @return the number of MeshObjects in this MeshBase
     * @see #size()
     */
    public final int getSize()
    {
        return size();
    }

    /**
     * Factory method for a IterableMeshBaseDifferencer, with this IterableMeshBase
     * being the comparison base.
     *
     * @return the IterableMeshBaseDifferencer
     * @throws UnsupportedOperationException always thrown because this should not be invoked on ShadowMeshBases
     */
    public IterableNetMeshBaseDifferencer getDifferencer()
    {
        return null;
    }

    /**
     * Continually sweep this IterableMeshBase in the background, according to
     * the configured Sweeper. For ShadowMeshBases, this does nothing.
     *
     * @param scheduleVia the ScheduledExecutorService to use for scheduling
     */
    public void startBackgroundSweeping(
            ScheduledExecutorService scheduleVia )
    {
        // no op
    }
    
    /**
     * Stop the background sweeping. For ShadowMeshBases, this does nothing.
     */
    public void stopBackgroundSweeping()
    {
        // no op
    }
    
    /**
     * Perform a sweep on every single MeshObject in this InterableMeshBase.
     * This may take a long time; using background sweeping is almost always
     * a better alternative. For ShadowMeshBases, this does nothing.
     */
    public void sweepAllNow()
    {
        // no op
    }

    /**
     * Invoked by the SweepStep, schedule the next SweepStep. For ShadowMeshBases, this does nothing.
     */
    public void scheduleSweepStep()
    {
        // no op
    }
}
