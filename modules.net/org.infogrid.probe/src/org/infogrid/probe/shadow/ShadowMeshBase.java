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

package org.infogrid.probe.shadow;

import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.IterableNetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.transaction.ChangeSet;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.shadow.externalized.ExternalizedShadowMeshBase;
import org.infogrid.util.FactoryCreatedObject;
import org.infogrid.util.IsDeadException;

/**
 * A NetMeshBase that shadows the content of a particular data source via the Probe framework.
 */
public interface ShadowMeshBase
        extends
            IterableNetMeshBase,
            StagingMeshBase,
            FactoryCreatedObject<NetMeshBaseIdentifier,ShadowMeshBase,CoherenceSpecification>
{
    /**
     * Invoke a run now.
     *
     * @return desired time of the next update, in milliseconds. -1 indicates never.
     * @throws ProbeException thrown if the update was unsuccessful
     * @throws IsDeadException thrown in this ShadowMeshBase is dead already
     */
    public abstract long doUpdateNow()
        throws
            ProbeException,
            IsDeadException;

    /**
     * Invoke a run now.
     *
     * @param coherence the requested CoherenceSpecification, if any
     * @return desired time of the next update, in milliseconds. -1 indicates never.
     * @throws ProbeException thrown if the update was unsuccessful
     * @throws IsDeadException thrown in this ShadowMeshBase is dead already
     */
    public abstract long doUpdateNow(
            CoherenceSpecification coherence )
        throws
            ProbeException,
            IsDeadException;

    /**
     * Determine whether at the last run, this ShadowMeshBase used a WritableProbe.
     * 
     * @return true if at the last run, this ShadowMeshBase used a WritableProbe
     */
    public boolean usesWritableProbe();

    /**
     * Obtain the time at which this ShadowMeshBase was created.
     *
     * @return the time at which this ShadowMeshBase was created, in System.currentTimeMillis() format
     */
    public abstract long getTimeCreated();

    /**
     * Obtain the time at which the most recent successful update of this
     * ShadowMeshBase was started.
     *
     * @return the time at which the update started, in System.currentTimeMillis() format
     */
    public abstract long getLastSuccessfulUpdateStartedTime();

    /**
     * Obtain the time at which the most recent update of this ShadowMeshBase
     * was started, regardless of whether it was successful or not.
     *
     * @return the time at which the update started, in System.currentTimeMillis() format
     */
    public abstract long getLastUpdateStartedTime();

    /**
     * Obtain the number of milliseconds from now when the next update is supposed to happen.
     *
     * @return the number of milliseconds from now when the next update is supposed to happen, or 0 if as soon as possible, and -1 if none.
     */
    public abstract long getDelayUntilNextUpdate();

    /**
     * Obtain the current problem with updating this ShadowMeshBase, if any.
     * It is reset every time the Probe runs. For example,
     * it clears after a Probe runs successfully even if previous runs failed.
     *
     * Changes to this Property are sent to all ShadowMeshBaseListeners.
     *
     * @return the current problem with updating this ShadowMeshBase
     */
    public abstract Throwable getCurrentProblem();

    /**
     * Queue new changes for the Shadow. These changes will be written out by the Probe
     * prior to reading the data source again. The application programmer should have
     * no need to call this.
     *
     * @param newChangeSet the set of new Changes
     */
    public abstract void queueNewChanges(
            ChangeSet newChangeSet );

    /**
     * Determine whether this ShadowMeshBase is still needed. It is needed if at least
     * one its MeshObjects is replicated to/from another NetMeshBase.
     *
     * @return true if it is still needed
     */
    public boolean isNeeded();

    /**
     * Obtain the ProxyPolicyFactory of this ShadowMeshBase.
     *
     * @return the ProxyPolicyFactory
     */
    public ProxyPolicyFactory getProxyPolicyFactory();

    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #addWeakShadowListener
     * @see #addSoftShadowListener
     * @see #removeShadowListener
     */
    public abstract void addDirectShadowListener(
            ShadowMeshBaseListener newListener );

    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #addDirectShadowListener
     * @see #addSoftShadowListener
     * @see #removeShadowListener
     */
    public abstract void addWeakShadowListener(
            ShadowMeshBaseListener newListener );

    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #addDirectShadowListener
     * @see #addWeakShadowListener
     * @see #removeShadowListener
     */
    public abstract void addSoftShadowListener(
            ShadowMeshBaseListener newListener );

    /**
     * Remove a listener to listen to ShadowMeshBase-specific events.
     *
     * @param oldListener the listener to be removed
     * @see #addDirectShadowListener
     * @see #addWeakShadowListener
     * @see #addSoftShadowListener
     */
    public abstract void removeShadowListener(
            ShadowMeshBaseListener oldListener );

    /**
     * Obtain the same MeshBase as ExternalizedMeshBase so it can be easily serialized.
     * 
     * @return this MeshObject as ExternalizedMeshBase
     */
    public abstract ExternalizedShadowMeshBase asExternalized();

    /**
     * The name of the CurrentProblem property.
     */
    public static final String CURRENT_PROBLEM_PROPERTY = "CurrentProblem";
}
