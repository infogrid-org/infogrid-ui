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

package org.infogrid.probe.manager;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionAction;
import org.infogrid.meshbase.transaction.TransactionActionException;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.Probe.ProbeUpdateSpecification;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.util.CachingMap;
import org.infogrid.util.IsDeadException;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * A ProbeManager implementation that uses a ScheduledExecutorService to schedule
 * updates.
 */
public abstract class ScheduledExecutorProbeManager
        extends
            AbstractProbeManager
        implements
            ActiveProbeManager
{
    private static final Log log = Log.getLogInstance( ScheduledExecutorProbeManager.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param delegateFactory the delegate ShadowMeshBaseFactory that knows how to instantiate ShadowMeshBases
     * @param storage the storage to use
     */
    protected ScheduledExecutorProbeManager(
            ShadowMeshBaseFactory                            delegateFactory,
            CachingMap<NetMeshBaseIdentifier,ShadowMeshBase> storage )
    {
        super( delegateFactory, storage );

        theExecutorService = null; // must invoke start() to start
    }
    
    /**
     * Obtain the ScheduledExecutorService that is used by this ProbeManager.
     * 
     * @return the ScheduledExecutorService
     */
    public ScheduledExecutorService getScheduledExecutorService()
    {
        return theExecutorService;
    }
    
    /**
     * Start this ScheduledExecutorProbeManager.
     * 
     * @param exec the ScheduledExecutorService to use
     */
    public synchronized void start(
            ScheduledExecutorService exec )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "start", exec );
        }
        if( theExecutorService != null ) {
            throw new IllegalStateException( "Already started" );
        }
        theExecutorService = exec;

        // re-initialize
        Iterator<NetMeshBaseIdentifier> keyIter = theKeyValueMap.keysIterator( NetMeshBaseIdentifier.class, ShadowMeshBase.class );
        while( keyIter.hasNext() ) {
            NetMeshBaseIdentifier key   = keyIter.next();
            ShadowMeshBase        value = theKeyValueMap.get( key );
            
            long nextTime = value.getDelayUntilNextUpdate();
            if( nextTime >= 0 ) {  // allow 0 for immediate execution
                ScheduledFuture<Long> newFuture = theExecutorService.schedule(
                        new ExecutorAdapter( new WeakReference<ScheduledExecutorProbeManager>( this ), key, nextTime ),
                        nextTime,
                        TimeUnit.MILLISECONDS );
                theFutures.put( key, newFuture );
            }
        }
    }

    /**
     * Stop this ScheduledExecutorProbeManager.
     */
    public synchronized void stop()
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "stop" );
        }
        if( theExecutorService == null ) {
            throw new IllegalStateException( "Already stopped" );
        }

        for( ScheduledFuture<Long> current : theFutures.values() ) {
            current.cancel( false );
        }
        theFutures.clear();
        
        theExecutorService = null;
    }

    /**
     * Invoke a ShadowMeshBase update now
     *
     * @param shadow the ShadowMeshBase to update
     * @throws ProbeException thrown if the update was unsuccessful
     * @throws IsDeadException thrown in this ShadowMeshBase is dead already
     */
    public void doUpdateNow(
            ShadowMeshBase shadow )
        throws
            ProbeException,
            IsDeadException
    {
        Future<Long> f = theFutures.remove( shadow.getIdentifier() );
        if( f != null && !f.isCancelled() ) {
            f.cancel( false );
        }

        long nextTime = shadow.doUpdateNow();

        if( nextTime >= 0 ) {  // allow 0 for immediate execution
            ScheduledFuture<Long> newFuture = theExecutorService.schedule(
                    new ExecutorAdapter( new WeakReference<ScheduledExecutorProbeManager>( this ), shadow.getIdentifier(), nextTime ),
                    nextTime,
                    TimeUnit.MILLISECONDS );
            theFutures.put( shadow.getIdentifier(), newFuture );
        }
    }

    /**
     * Stop future automatic updates for this ShadowMeshBase.
     *
     * @param shadow the ShadowMeshBase
     */
    public void disableFutureUpdates(
            final ShadowMeshBase shadow )
    {
        Future<Long> f = theFutures.remove( shadow.getIdentifier() );
        if( f != null && !f.isCancelled() ) {
            f.cancel( false );
        }

        try {
            shadow.executeAsap( new TransactionAction<ProbeUpdateSpecification>() {
                    public ProbeUpdateSpecification execute(
                            Transaction tx )
                        throws
                            Throwable
                    {
                        MeshObject               home = shadow.getHomeObject();
                        ProbeUpdateSpecification spec = (ProbeUpdateSpecification) home.getTypedFacadeFor( ProbeSubjectArea.PROBEUPDATESPECIFICATION );

                        spec.stopUpdating();
                        return spec;
                    }
            });

        } catch( TransactionActionException ex ) {
            log.error( ex );
        }
    }

    /**
     * This overridable method allows our subclasses to invoke particular functionality
     * every time this SmartFactory created a new value by invoking the delegate Factory.
     * It is not invoked for those returned values that are merely retrieved from
     * the storage in the smart factory.
     * 
     * @param key the key of the newly created value
     * @param value the newly created value
     * @param argument the argument into the creation of the newly created value
     */
    @Override
    protected void createdHook(
            NetMeshBaseIdentifier  key,
            ShadowMeshBase         value,
            CoherenceSpecification argument )
    {
        long nextTime = value.getDelayUntilNextUpdate();
        if( nextTime >= 0 && theExecutorService != null ) { // allow 0 for immediate execution
            ScheduledFuture<Long> newFuture = theExecutorService.schedule(
                    new ExecutorAdapter( new WeakReference<ScheduledExecutorProbeManager>( this ), key, nextTime ),
                    nextTime,
                    TimeUnit.MILLISECONDS );
            theFutures.put( key, newFuture );
        }
        super.createdHook( key, value, argument );
    }

    /**
     * We are not needed any more.
     * 
     * @param isPermanent if true, this MeshBase will go away permanently; if false, it may come alive again some time later
     */
    public synchronized void die(
            boolean isPermanent )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "die" );
        }
        for( ShadowMeshBase shadow : theKeyValueMap.values() ) {
            // attempt to be as successful as possible
            try {
                shadow.die();
            } catch( Throwable t ) {
                log.error( t );
            }
        }
        if( theExecutorService != null ) {
            stop(); // cleans up nicely
        }
    }

    /**
     * The ScheduledExecutorService that executes our Probe runs.
     */
    protected ScheduledExecutorService theExecutorService;
    
    /**
     * The Futures currently waiting to be executed on behalf of this ScheduledExecutorProbeManager.
     * This maps from the ShadowMeshBase's identifier to the Future.
     */
    protected Map<NetMeshBaseIdentifier,ScheduledFuture<Long>> theFutures
            = new HashMap<NetMeshBaseIdentifier,ScheduledFuture<Long>>()
    {
        @Override
        public ScheduledFuture<Long> remove( Object id ) {
            if( log.isTraceEnabled() ) {
                log.traceMethodCallEntry( this, "remove", id );
            }
            return super.remove( id );
        }
    };

    /**
     * The default thread-pool size.
     */
    protected static int DEFAULT_THREAD_POOL_SIZE = 1;
    
    /**
     * Helper class to be able to reschedule the ShadowMeshBase. This is a static class, so the ProbeManager
     * can be garbage-collected, even while some ScheduledExecutorService still thinks it has a future call to make.
     */
    protected static class ExecutorAdapter
            implements
                Callable<Long>,
                CanBeDumped
    {
        /**
         * Constructor.
         *
         * @param belongsTo reference to the ScheduledExecutorProbeManager to which this ExecutorAdaptor belongs
         * @param shadowIdentifier the Identifier of the ShadowMeshBase to be updated
         * @param nextTime the relative time, from now, when this ExecutablerAdapter will be called. This is only
         *        provided for debugging purposes
         */
        public ExecutorAdapter(
                Reference<ScheduledExecutorProbeManager> belongsTo,
                NetMeshBaseIdentifier                    shadowIdentifier,
                long                                     nextTime )
        {
            theBelongsTo        = belongsTo;
            theShadowIdentifier = shadowIdentifier;
            theWillBeCalledAt   = new Date( System.currentTimeMillis() + nextTime );

            if( log.isTraceEnabled() ) {
                log.traceMethodCallEntry( this, "constructor" );
            }
        }

        /**
         * The main call when invoked by a background Thread.
         * 
         * @throws Exception catch-all Exception
         * @return desired time of the next update, in milliseconds. -1 indicates never.
         */
        public Long call()
            throws
                Exception
        {
            return call( null );
        }

        /**
         * The main call when invoked on the thread of the application programmer.
         * 
         * @param coherence optional CoherenceSpecification
         * @return desired time of the next update, in milliseconds. -1 indicates never.
         * @throws Exception catch-all Exception
         */
        public Long call(
                CoherenceSpecification coherence )
            throws
                Exception
        {
            if( log.isInfoEnabled() ) {
                log.info( this + ".call( " + coherence + " )" );
            }
            
            ScheduledExecutorProbeManager belongsTo = theBelongsTo.get();
            boolean                       removeOld = true;
            try {
                Long nextTime= -1L;

                if( belongsTo == null ) {
                    // we are done
                    return nextTime;
                }

                ShadowMeshBase shadow = belongsTo.get( theShadowIdentifier );
                if( shadow == null ) {
                    return nextTime;
                }

                try {
                    nextTime = shadow.doUpdateNow( coherence );

                    if( nextTime != null && nextTime.longValue() >= 0 ) { // allow 0 for immediate execution
                        if( log.isDebugEnabled() ) {
                            log.debug( this + ".call ... schedule in " + nextTime.longValue() );
                        }
                        ScheduledFuture<Long> f = belongsTo.theExecutorService.schedule( this, nextTime.longValue(), TimeUnit.MILLISECONDS );
                        belongsTo.theFutures.put( theShadowIdentifier, f );

                        removeOld = false; // otherwise we remove what we just added, the old one was removed as a side effect of put
                    }

                } catch( IsDeadException ex ) {
                    log.info( ex );
                    // simply don't reschedule
                }
                return nextTime;

            } finally {
                if( belongsTo != null && removeOld ) {
                    belongsTo.theFutures.remove( theShadowIdentifier );
                }
            }
        }

        /**
         * Dump this object.
         *
         * @param d the Dumper to dump to
         */
        public void dump(
                Dumper d )
        {
            d.dump( this,
                    new String[] {
                        "shadowIdentifier",
                        "theWillBeCalledAt"
                    },
                    new Object[] {
                        theShadowIdentifier,
                        theWillBeCalledAt
                    });
        }

        /**
         * The ScheduledExecutorProbeManager that this instance belogs to. This is a static
         * class with a Reference in order to not get in the way of garbage collection.
         */
        protected Reference<ScheduledExecutorProbeManager> theBelongsTo;

        /**
         * The NetMeshBaseIdentifier of the ShadowMeshBase to call. This allows the garbage collection
         * for the ShadowMeshBase to proceed even if future runs are scheduled.
         */
        protected NetMeshBaseIdentifier theShadowIdentifier;
        
        /**
         * The time at which this Callable will be called. This is only here for debugging purposes.
         */
        protected Date theWillBeCalledAt;
    }
}
