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

import java.util.Iterator;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyManager;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.meshbase.net.proxy.NiceAndTrustingProxyPolicyFactory;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.meshbase.transaction.ChangeSet;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.ProbeDispatcher;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.manager.ProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseListener;
import org.infogrid.probe.shadow.externalized.ExternalizedShadowMeshBase;
import org.infogrid.probe.shadow.externalized.SimpleExternalizedShadowMeshBase;
import org.infogrid.util.CachingMap;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.Factory;
import org.infogrid.util.Invocable;
import org.infogrid.util.IsDeadException;
import org.infogrid.util.MapCursorIterator;
import org.infogrid.util.RemoteQueryTimeoutException;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * A ShadowMeshBase in the A implementation.
 */
public abstract class AShadowMeshBase
        extends
            AStagingMeshBase
        implements
            ShadowMeshBase
{
    private static final Log log = Log.getLogInstance( AShadowMeshBase.class ); // our own, private logger

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
     * @param directory the ProbeDirectory to use
     * @param timeCreated the time at which the data source was created (if this is a recreate operation) or -1
     * @param timeNotNeededTillExpires the time, in milliseconds, that this MShadowMeshBase will continue operating
     *         even if none of its MeshObjects are replicated to another NetMeshBase. If this is negative, it means "forever".
     *         If this is 0, it will expire immediately after the first Probe run, before the caller returns, which is probably
     *         not very useful.
     * @param context the Context in which this NetMeshBase runs.
     */
    protected AShadowMeshBase(
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
            ProbeDirectory                              directory,
            long                                        timeCreated,
            long                                        timeNotNeededTillExpires,
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
        
        ModuleRegistry registry = context.findContextObject( ModuleRegistry.class );
        
        theDispatcher = new ProbeDispatcher( this, directory, timeCreated, timeNotNeededTillExpires, registry );
    }

    /**
     * Returns a CursorIterator over the content of this MeshBase.
     * 
     * @return a CursorIterator.
     */
    public CursorIterator<MeshObject> iterator()
    {
        // not sure why these type casts are needed, they should not be
        MapCursorIterator.Values<MeshObjectIdentifier,MeshObject> ret = MapCursorIterator.createForValues(
                theCache,
                MeshObjectIdentifier.class,
                MeshObject.class );
        return ret;
    }

    /**
     * Enable a Factory to indicate to the FactoryCreatedObject that it was
     * it that created it.
     *
     * @param factory the Factory that created the FactoryCreatedObject
     */
    public final void setFactory(
            Factory<NetMeshBaseIdentifier,ShadowMeshBase,CoherenceSpecification> factory )
    {
        if( factory instanceof ProbeManager ) {
            theProbeManager = (ProbeManager) factory;

            if( log.isDebugEnabled() ) {
                log.debug( this + ".setFactory( " + factory + " ) -- theProbeManager updated" );
            }
        } else {
            // This can happen because factories delegating to other factories invoke this method
            // repeatedly with the entire chain of factories, not all of which may be SmartFactories.

            theProbeManager = null;

            if( log.isDebugEnabled() ) {
                log.debug( this + ".setFactory( " + factory + " ) -- theProbeManager is still NULL" );
            }
        }
    }

    /**
     * Obtain the Factory that created this FactoryCreatedObject. In case of
     * chained factories that delegate to each other, this method is
     * supposed to return the outermost factory invoked by the application programmer.
     *
     * @return the Factory that created the FactoryCreatedObject
     */
    public final Factory<NetMeshBaseIdentifier,ShadowMeshBase,CoherenceSpecification> getFactory()
    {
        return theProbeManager;
    }

    /**
     * Obtain the key for Smart Factories.
     *
     * @return the key
     */
    public NetMeshBaseIdentifier getFactoryKey()
    {
        return getIdentifier();
    }

    /**
     * Obtain the ProbeManager that manages this ShadowMeshBase.
     *
     * @return the ProbeManager
     */
    public ProbeManager getProbeManager()
    {
        return theProbeManager;
    }

    /**
     * Determine whether at the last run, this ShadowMeshBase used a WritableProbe.
     * 
     * @return true if at the last run, this ShadowMeshBase used a WritableProbe
     */
    public boolean usesWritableProbe()
    {
        return theDispatcher.usesWritableProbe();
    }

    /**
     * Invoke a run now.
     *
     * @return desired time of the next update, in milliseconds. -1 indicates never.
     * @throws ProbeException thrown if the update was unsuccessful
     * @throws IsDeadException thrown if the ShadowMeshBase is dead already when this method is being invoked
     */
    public long doUpdateNow()
        throws
            ProbeException,
            IsDeadException
    {
        return doUpdateNow( null );
    }

    /**
     * Invoke a run now.
     *
     * @param coherence the requested CoherenceSpecification, if any
     * @return desired time of the next update, in milliseconds. -1 indicates never.
     * @throws ProbeException thrown if the update was unsuccessful
     * @throws IsDeadException thrown if the ShadowMeshBase is dead already when this method is being invoked
     */
    public long doUpdateNow(
            CoherenceSpecification coherence )
        throws
            ProbeException,
            IsDeadException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".doUpdateNow()" );
        }

        synchronized( theDispatcher ) { // we can't synchronize on the shadow, because incoming transactions need to be able to create threads
            checkDead();

            long nextTime = theDispatcher.doUpdateNow( coherence );

            if( theProbeManager != null ) {
                if( log.isDebugEnabled() ) {
                    log.debug( this + ".doUpdateNow() --- telling factory about it" );
                }

                // the first time this runs, as part of the factory method, this has not been set yet
                theProbeManager.factoryCreatedObjectUpdated( this );
            } else {
                if( log.isDebugEnabled() ) {
                    log.debug( this + ".doUpdateNow() --- CANNOT TELL factory about it" );
                }
            }

            if( !theDispatcher.mayBeDeleted() ) {
                return nextTime;

            } else {

                if( log.isDebugEnabled() ) {
                    log.debug( this + ": not needed any more" );
                }

                // got to do this trick with the callback, otherwise we get a race condition
                theProbeManager.remove( (NetMeshBaseIdentifier) theMeshBaseIdentifier, new Invocable<ShadowMeshBase,Void>() {
                        public Void invoke(
                                ShadowMeshBase toDelete )
                        {
                            Iterator<Proxy> iter = toDelete.proxies();
                            while( iter.hasNext() ) {
                                Proxy current = iter.next();

                                if( log.isDebugEnabled() ) {
                                    log.debug( AShadowMeshBase.this + ": removing proxy " + current );
                                }

                                try {
                                    current.initiateCeaseCommunications();

                                } catch( RemoteQueryTimeoutException ex ) {
                                    log.warn( ex );
                                }
                                theProxyManager.remove( current.getPartnerMeshBaseIdentifier() );
                            }
                            return null;
                        }
                });

                return -1L;
            }
        }
    }

    /**
     * Obtain the time at which this ShadowMeshBase was created.
     *
     * @return the time at which this ShadowMeshBase was created, in System.currentTimeMillis() format
     */
    public final long getTimeCreated()
    {
        return theDispatcher.getTimeCreated();
    }

    /**
     * Obtain the time at which the most recent successful update of this
     * ShadowMeshBase was started.
     *
     * @return the time at which the update started, in System.currentTimeMillis() format
     */
    public final long getLastSuccessfulUpdateStartedTime()
    {
        return theDispatcher.getLastSuccessfulUpdateStartedTime();
    }

    /**
     * Obtain the time at which the most recent update of this ShadowMeshBase
     * was started, regardless of whether it was successful or not.
     *
     * @return the time at which the update started, in System.currentTimeMillis() format
     */
    public final long getLastUpdateStartedTime()
    {
        return theDispatcher.getLastUpdateStartedTime();
    }

    /**
     * Obtain the time at which the next update is supposed to happen.
     *
     * @return the time at which the next update is supposed to happen, or -1 if none.
     */
    public final long getDelayUntilNextUpdate()
    {
        return theDispatcher.getDelayUntilNextUpdate();
    }

    /**
     * Obtain the current problem with updating this ShadowMeshBase, if any.
     * It is reset every time the Probe runs. For example,
     * it clears after a Probe runs successfully even if previous runs failed.
     *
     * Changes to this Property are sent to all ShadowMeshBaseListeners.
     *
     * @return the current problem with updating this ShadowMeshBase
     */
    public final Throwable getCurrentProblem()
    {
        return theDispatcher.getCurrentProblem();
    }

    /**
     * Update the cache when Transactions are committed.
     *
     * @param tx Transaction the Transaction that was committed
     */
    @Override
    protected void transactionCommittedHook(
            Transaction tx )
    {
        super.transactionCommittedHook( tx );
        
        theDispatcher.queueNewChanges( tx.getChangeSet() );
    }

    /**
     * Queue new changes for the Shadow. These changes will be written out by the Probe
     * prior to reading the data source again. The application programmer should have
     * no need to call this.
     *
     * @param newChangeSet the set of new Changes
     */
    public final void queueNewChanges(
            ChangeSet newChangeSet )
    {
        theDispatcher.queueNewChanges( newChangeSet );
    }

    /**
     * Determine whether this ShadowMeshBase is still needed. It is needed if at least
     * one its MeshObjects is replicated to/from another NetMeshBase.
     *
     * @return true if it is still needed
     */
    public final boolean isNeeded()
    {
        return theDispatcher.isNeeded();
    }

    /**
     * Obtain the ProxyPolicyFactory of this ShadowMeshBase.
     *
     * @return the ProxyPolicyFactory
     */
    public ProxyPolicyFactory getProxyPolicyFactory()
    {
        ProxyPolicyFactory ret = NiceAndTrustingProxyPolicyFactory.create();
        return ret;
    }

    /**
     * Enable the associated MeshBaseLifecycleManager to determine the start of the current update.
     * This is only invoked during an actual update.
     *
     * @return the start time of the current update
     */
    public final long getCurrentUpdateStartedTime()
    {
        return theDispatcher.getCurrentUpdateStartedTime();
    }

    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #addWeakShadowListener
     * @see #addSoftShadowListener
     * @see #removeShadowListener
     */
    public final void addDirectShadowListener(
            ShadowMeshBaseListener newListener )
    {
        theDispatcher.addDirectShadowListener( newListener );
    }

    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #addDirectShadowListener
     * @see #addSoftShadowListener
     * @see #removeShadowListener
     */
    public final void addWeakShadowListener(
            ShadowMeshBaseListener newListener )
    {
        theDispatcher.addWeakShadowListener( newListener );
    }

    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #addDirectShadowListener
     * @see #addWeakShadowListener
     * @see #removeShadowListener
     */
    public final void addSoftShadowListener(
            ShadowMeshBaseListener newListener )
    {
        theDispatcher.addSoftShadowListener( newListener );
    }

    /**
     * Remove a listener to listen to ShadowMeshBase-specific events.
     *
     * @param oldListener the listener to be removed
     * @see #addDirectShadowListener
     * @see #addWeakShadowListener
     * @see #addSoftShadowListener
     */
    public final void removeShadowListener(
            ShadowMeshBaseListener oldListener )
    {
        theDispatcher.removeShadowListener( oldListener );
    }

    /**
     * Obtain the same MeshBase as ExternalizedMeshBase so it can be easily serialized.
     * 
     * @return this MeshObject as ExternalizedMeshBase
     */
    public synchronized ExternalizedShadowMeshBase asExternalized()
    {
        ExternalizedProxy [] externalizedProxies = theProxyManager.externalizedProxies();
        
        ExternalizedNetMeshObject [] externalizedNetMeshObjects = new ExternalizedNetMeshObject[ super.size() ];
        Iterator<MeshObject> iter = iterator();
        
        for( int i=0 ; iter.hasNext() ; ++i ) {
            NetMeshObject current = (NetMeshObject) iter.next();
            
            externalizedNetMeshObjects[i] = current.asExternalized( true );
        }
        
        SimpleExternalizedShadowMeshBase ret = SimpleExternalizedShadowMeshBase.create(
                (NetMeshBaseIdentifier) theMeshBaseIdentifier,
                externalizedProxies,
                externalizedNetMeshObjects );
        
        return ret;
    }
    
    /**
     * The ProbeManager that this ShadowMeshBase belongs to.
     */
    protected ProbeManager theProbeManager;
    
    /**
     * The ProbeDispatcher to which we delegate most of the work.
     */
    protected final ProbeDispatcher theDispatcher;
}
