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

import java.util.List;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.ReturnSynchronizer;
import org.infogrid.util.ReturnSynchronizerException;
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
     * @param synchronizer the ReturnSynchronizer to delegate to
     * @param messageEndpoint the BidirectionalMessageEndpoint to use as communications endpoint
     * @return the created ReturnSynchronizerEndpoint
     * @param <T> the message type
     */
    public static <T extends CarriesInvocationId> ReturnSynchronizerEndpoint<T> create(
            ReturnSynchronizer<Long,T>      synchronizer,
            BidirectionalMessageEndpoint<T> messageEndpoint )
    {
        ReturnSynchronizerEndpoint<T> ret = new ReturnSynchronizerEndpoint<T>( synchronizer, messageEndpoint );
        messageEndpoint.addWeakMessageEndpointListener( ret );

        return ret;
    }

    /**
     * Constructor.
     *
     * @param synchronizer the ReturnSynchronizer to delegate to
     * @param messageEndpoint the BidirectionalMessageEndpoint to use as communications endpoint
     */
    protected ReturnSynchronizerEndpoint(
            ReturnSynchronizer<Long,T>      synchronizer,
            BidirectionalMessageEndpoint<T> messageEndpoint )
    {
        super( messageEndpoint );

        theSynchronizer = synchronizer;
    }

    /**
     * Obtain the ReturnSynchronizer
     *
     * @return the ReturnSynchronizer
     */
    public final ReturnSynchronizer<Long,T> getReturnSynchronizer()
    {
        return theSynchronizer;
    }

    /**
     * Invoke the front leg of the remote procedure call.
     *
     * @param message the message that represents the argument to the call
     * @throws ReturnSynchronizerException.NoTransactionOpen thrown if a ReturnSynchronizer transaction had not been opened previously
     */
    public void call(
            T message )
        throws
            ReturnSynchronizerException.NoTransactionOpen
    {
        call( message, null, defaultTimeout );
    }

    /**
     * Invoke the front leg of the remote procedure call.
     *
     * @param message the message that represents the argument to the call
     * @param existingKeyForQuery non-null if this call is made to fulfill an existing query only
     *        partially performed so far. This is the key for that existig query.
     * @param timeout the timeout, in milliseconds, until the call times out
     * @throws ReturnSynchronizerException.NoTransactionOpen thrown if a ReturnSynchronizer transaction had not been opened previously
     */
    public void call(
            T    message,
            Long existingKeyForQuery,
            long timeout )
        throws
            ReturnSynchronizerException.NoTransactionOpen
    {
        long invocationId = createInvocationId();

        message.setRequestId( invocationId );

        if( log.isTraceEnabled() ) { // better here because here we have the invocation id set
            log.traceMethodCallEntry( this, "invoke", message, timeout );
        }

        try {
            if( existingKeyForQuery != null ) {
                theSynchronizer.addFurtherOpenQueryToOpenQuery( existingKeyForQuery, invocationId );
            } else {
                theSynchronizer.addOpenQuery( invocationId );
            }
            theMessageEndpoint.sendMessageAsap( message );

        } catch( ReturnSynchronizerException.DuplicateKey ex ) {
            log.error( ex );
        }
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
        boolean ret = theSynchronizer.hasOpenQuery( responseId );
        return ret;
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

        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "messageReceived", msg );
        }

        try {
            if( theSynchronizer.depositQueryResult( responseId, msg )) {
                otherMessageReceived( endpoint, msg );
            }
        } catch( ReturnSynchronizerException.DuplicateResult ex ) {
            log.error( ex );
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
        theSynchronizer.disablingError( t );
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
                    "theSynchronizer",
                    "theMessageEndpoint"
                },
                new Object[] {
                    theSynchronizer,
                    theMessageEndpoint
                });
    }

    /**
     * The ReturnSynchronizer to notify when a response has been received.
     */
    protected ReturnSynchronizer<Long,T> theSynchronizer;

    /**
     * The default timeout.
     */
    protected static long defaultTimeout = ResourceHelper.getInstance( ReturnSynchronizerEndpoint.class ).getResourceLongOrDefault( "DefaultTimeout", 5000L  );
}