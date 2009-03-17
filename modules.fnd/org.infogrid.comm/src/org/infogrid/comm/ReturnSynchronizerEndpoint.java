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

package org.infogrid.comm;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.ReturnSynchronizer;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * An communication endpoint that notifies a ReturnSynchronizer when a received
 * response to a sent message has arrived. This is useful to implement RPC-style communications
 * on top of the ping-pong framework without blocking the calling thread.
 *
 * @param <T> the message type
 */
public class ReturnSynchronizerEndpoint<T extends CarriesInvocationId>
        extends
            AbstractWaitingEndpoint<T>
        implements
            CanBeDumped
{
    private static final Log log = Log.getLogInstance( ReturnSynchronizerEndpoint.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param messageEndpoint the BidirectionalMessageEndpoint to use as communications endpoint
     * @return the created ReturnSynchronizerEndpoint
     * @param <T> the message type
     */
    public static <T extends CarriesInvocationId> ReturnSynchronizerEndpoint<T> create(
            BidirectionalMessageEndpoint<T> messageEndpoint )
    {
        ReturnSynchronizerEndpoint<T> ret = new ReturnSynchronizerEndpoint<T>( messageEndpoint );
        messageEndpoint.addWeakMessageEndpointListener( ret );

        return ret;
    }

    /**
     * Constructor.
     *
     * @param messageEndpoint the BidirectionalMessageEndpoint to use as communications endpoint
     */
    protected ReturnSynchronizerEndpoint(
            BidirectionalMessageEndpoint<T> messageEndpoint )
    {
        super( messageEndpoint );
    }

    /**
     * Obtain the ReturnSynchronizers currently known
     *
     * @return the ReturnSynchronizers
     */
    public final synchronized Collection<ReturnSynchronizer<Long,T>> getReturnSynchronizers()
    {
        ArrayList<ReturnSynchronizer<Long,T>> ret = new ArrayList<ReturnSynchronizer<Long,T>>( theSynchronizers.size() );
        for( Reference<ReturnSynchronizer<Long,T>> ref : theSynchronizers.values() ) {
            ReturnSynchronizer<Long,T> current = ref.get();
            if( current != null ) {
                ret.add( current );
            }
        }
        return ret;
    }

    /**
     * Invoke the remote procedure call.
     *
     * @param message the message that represents the argument to the call
     * @param synchronizer the ReturnSynchronizer to notify when a response has been received
     * @return the synchronization object so it becomes possible to retrieve the correct result later
     * @throws InvocationTargetException thrown if the invocation produced an Exception
     */
    public Object call(
            T                          message,
            ReturnSynchronizer<Long,T> synchronizer )
        throws
            InvocationTargetException
    {
        return call( message, defaultTimeout, synchronizer );
    }

    /**
     * Invoke the remote procedure call.
     *
     * @param message the message that represents the argument to the call
     * @param timeout the timeout, in milliseconds, until the call times out
     * @param synchronizer the ReturnSynchronizer to notify when a response has been received
     * @return the synchronization object so it becomes possible to retrieve the correct result later
     * @throws InvocationTargetException thrown if the invocation produced an Exception
     */
    public Object call(
            T                          message,
            long                       timeout,
            ReturnSynchronizer<Long,T> synchronizer )
        throws
            InvocationTargetException
    {
        long invocationId = createInvocationId();

        message.setRequestId( invocationId );

        if( log.isTraceCallEnabled() ) { // better here because here we have the invocation id set
            log.traceMethodCallEntry( this, "invoke", message, timeout );
        }

        Reference<ReturnSynchronizer<Long,T>> ref = new WeakReference<ReturnSynchronizer<Long,T>>( synchronizer );
        synchronized( this ) {
            theSynchronizers.put( invocationId, ref );
        }
        Object ret = synchronizer.addOpenQuery( invocationId );
        theMessageEndpoint.sendMessageAsap( message );

        return ret;
    }

    /**
     * Determine whether a call is waiting for a response with the provided responseId.
     *
     * @param responseId the responseId
     * @return true a call is waiting for this responseId
     */
    public boolean isCallWaitingFor(
            long responseId )
    {
        Reference<ReturnSynchronizer<Long,T>> ref;
        synchronized( this ) {
            ref = theSynchronizers.get( responseId );
        }
        ReturnSynchronizer<Long,T> sync = ref != null ? ref.get() : null;
        if( sync == null ) {
            return false;
        }
        boolean almost = sync.isQueryComplete( responseId );
        return !almost;
    }

    /**
     * Called when an incoming message has arrived.
     *
     * @param endpoint the BidirectionalMessageEndpoint that received the message
     * @param msg the received message
     */
    public void messageReceived(
            ReceivingMessageEndpoint<T> endpoint,
            T                           msg )
    {
        long responseId = msg.getResponseId();

        if( log.isTraceCallEnabled() ) {
            log.traceMethodCallEntry( this, "messageReceived", msg );
        }

        Reference<ReturnSynchronizer<Long,T>> ref;
        synchronized( this ) {
            ref = theSynchronizers.get( responseId );
        }
        ReturnSynchronizer<Long,T> sync = ref != null ? ref.get() : null;

        if( sync == null || !sync.queryHasCompleted( responseId, msg )) {
            otherMessageReceived( endpoint, msg );
        }
    }

    /**
     * Called when the receiving endpoint threw the EndpointIsDeadException.
     *
     * @param endpoint the BidirectionalMessageEndpoint that sent this event
     * @param msg the status of the outgoing queue
     * @param t the Throwable that caused this error, if any
     */
    public void disablingError(
            MessageEndpoint<T> endpoint,
            List<T>            msg,
            Throwable          t )
    {
        for( Reference<ReturnSynchronizer<Long,T>> ref : theSynchronizers.values() ) {
            ReturnSynchronizer<Long,T> current = ref.get();
            if( current != null ) {
                current.disablingError( t );
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
                    "theSynchronizers",
                    "theMessageEndpoint"
                },
                new Object[] {
                    theSynchronizers,
                    theMessageEndpoint
                });
    }

    /**
     * The ReturnSynchronizers to notify when a response has been received. This maps from
     * invocationId to ReturnSynchronizer.
     */
    protected Map<Long,Reference<ReturnSynchronizer<Long,T>>> theSynchronizers
            = new HashMap<Long,Reference<ReturnSynchronizer<Long,T>>>();

    /**
     * The default timeout.
     */
    protected static long defaultTimeout = ResourceHelper.getInstance( ReturnSynchronizerEndpoint.class ).getResourceLongOrDefault( "DefaultTimeout", 5000L  );
}