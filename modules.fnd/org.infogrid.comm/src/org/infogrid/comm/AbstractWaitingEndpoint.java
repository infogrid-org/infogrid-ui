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

import org.infogrid.util.ResourceHelper;
import org.infogrid.util.SimpleTimeBasedUniqueLongGenerator;

/**
 * Factors out functionality common to communications endpoint that wait for responses.
 *
 * @param <T> the message type
 */
public abstract class AbstractWaitingEndpoint<T extends CarriesInvocationId>
        implements
            MessageEndpointListener<T>
{
    /**
     * Constructor.
     *
     * @param messageEndpoint the BidirectionalMessageEndpoint to use as communications endpoint
     */
    protected AbstractWaitingEndpoint(
            BidirectionalMessageEndpoint<T> messageEndpoint )
    {
        theMessageEndpoint = messageEndpoint;
    }

    /**
     * Obtain the BidirectionalMessageEndpoint through which the RPC Messages are sent.
     *
     * @return the BidirectionalMessageEndpoint
     */
    public final BidirectionalMessageEndpoint<T> getMessageEndpoint()
    {
        return theMessageEndpoint;
    }

    /**
     * Called when an outgoing message has been sent.
     *
     * @param endpoint the BidirectionalMessageEndpoint that sent this event
     * @param msg the sent message
     */
    public void messageSent(
            SendingMessageEndpoint<T> endpoint,
            T                         msg )
    {
        // do nothing
    }

    /**
     * Called when an outgoing message has enqueued for sending.
     *
     * @param endpoint the BidirectionalMessageEndpoint that sent this event
     * @param msg the enqueued message
     */
    public void messageEnqueued(
            SendingMessageEndpoint<T> endpoint,
            T                         msg )
    {
        // do nothing
    }

    /**
     * Invoked only for those messages that are not processed as a response.
     *
     * @param endpoint the BidirectionalMessageEndpoint that sent this event
     * @param msg the received message that was not processed before
     */
    protected void otherMessageReceived(
            ReceivingMessageEndpoint<T> endpoint,
            T                           msg )
    {
        // noop on this level
    }

    /**
     * Called when an outoing message failed to be sent.
     *
     * @param endpoint the BidirectionalMessageEndpoint that sent this event
     * @param msg the outgoing message
     */
    public void messageSendingFailed(
            SendingMessageEndpoint<T> endpoint,
            T                         msg )
    {
        // no op
    }

    /**
     * Overridable helper to create unique invocation IDs. Note that these invocation
     * IDs must be unique, even if the endpoint is temporarily suspended, saved on
     * disk etc. because ongoing communications with the partner may otherwise become
     * ambiguous. By default, we use the current time as an invocation ID.
     *
     * @return the invocation identifier
     */
    protected long createInvocationId()
    {
        long ret = theDelegate.createUniqueToken();
        return ret;
    }

    /**
     * The underlying BidirectionalMessageEndpoint.
     */
    protected BidirectionalMessageEndpoint<T> theMessageEndpoint;

    /**
     * The internally used UniqueTokenCreator.
     */
    protected static SimpleTimeBasedUniqueLongGenerator theDelegate = SimpleTimeBasedUniqueLongGenerator.create();
}
