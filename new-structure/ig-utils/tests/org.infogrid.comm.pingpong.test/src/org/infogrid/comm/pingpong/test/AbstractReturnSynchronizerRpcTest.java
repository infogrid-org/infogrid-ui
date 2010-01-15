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

package org.infogrid.comm.pingpong.test;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.comm.CarriesInvocationId;
import org.infogrid.comm.BidirectionalMessageEndpoint;
import org.infogrid.comm.MessageEndpoint;
import org.infogrid.comm.RpcClientEndpoint;
import org.infogrid.comm.SendingMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpointListener;
import org.infogrid.testharness.AbstractTest;

/**
 * Factors out functionality common to ReturnSynchronizerRpcTests.
 */
public abstract class AbstractReturnSynchronizerRpcTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     */
    protected AbstractReturnSynchronizerRpcTest()
    {
        super();
    }

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 2 );

    /**
     * Set to true if the test is done, so listeners won't report an error.
     */
    protected boolean done = false;

    /**
     * The Message for this test.
     */
    class PingPongRpcTestMessage
            implements
                CarriesInvocationId
    {
        /**
         * Constructor with payload.
         *
         * @param payload the payload
         */
        public PingPongRpcTestMessage(
                long payload )
        {
            getLog().debug( "Creating PingPongRpcTestMessage( " + payload + " )" );
            thePayload = payload;
        }

        /**
         * Obtain the payload.
         *
         * @return the payload
         */
        public long getPayload()
        {
            return thePayload;
        }

        /**
         * Set the request ID.
         *
         * @param id the request ID
         */
        public void setRequestId(
                long id )
        {
            theRequestId = id;
        }

        /**
         * Obtain the request ID.
         *
         * @return the request ID
         */
        public long getRequestId()
        {
            return theRequestId;
        }

        /**
         * Set the response ID.
         *
         * @param id the response ID
         */
        public void setResponseId(
                long id )
        {
            theResponseId = id;
        }

        /**
         * Obtain the response ID.
         *
         * @return the response ID
         */
        public long getResponseId()
        {
            return theResponseId;
        }

        /**
         * Convert to String, for debugging.
         *
         * @return String form
         */
        @Override
        public String toString()
        {
            return super.toString() + ": requestId: " + theRequestId + ", responseId: " + theResponseId + ", payload: " + thePayload;
        }

        /**
         * The Payload of the message.
         */
        protected long thePayload;

        /**
         * The request ID.
         */
        protected long theRequestId;

        /**
         * The response ID.
         */
        protected long theResponseId;
    }

    /**
     * Factors out common functionality for listeners in PingPongRpcTests.
     */
    abstract class AbstractPingPongRpcListener
            implements
                PingPongMessageEndpointListener<PingPongRpcTestMessage>
    {
        /**
         * Constructor.
         *
         * @param end the endpoint where this listener listens
         */
        public AbstractPingPongRpcListener(
                BidirectionalMessageEndpoint<PingPongRpcTestMessage> end )
        {
            theEndpoint = end;
        }

        /**
         * Called when an outgoing message has been sent.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the sent message
         */
        public void messageSent(
                SendingMessageEndpoint<PingPongRpcTestMessage> endpoint,
                PingPongRpcTestMessage                         msg )
        {
            getLog().traceMethodCallEntry( this, "messageSent", endpoint, msg );
        }

        /**
         * Called when an outgoing message has enqueued for sending.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the enqueued message
         */
        public void messageEnqueued(
                SendingMessageEndpoint<PingPongRpcTestMessage> endpoint,
                PingPongRpcTestMessage                         msg )
        {
            getLog().traceMethodCallEntry( this, "messageEnqueued", endpoint, msg );
        }

        /**
         * Called when an outoing message failed to be sent.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the outgoing message
         */
        public void messageSendingFailed(
                SendingMessageEndpoint<PingPongRpcTestMessage> endpoint,
                PingPongRpcTestMessage                         msg )
        {
            getLog().traceMethodCallEntry( this, "messageSendingFailed", endpoint, msg );
        }

        /**
         * Called when the receiving endpoint threw the EndpointIsDeadException.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the status of the outgoing queue
         * @param t the disabling error
         */
        public void disablingError(
                MessageEndpoint<PingPongRpcTestMessage> endpoint,
                List<PingPongRpcTestMessage>            msg,
                Throwable                               t )
        {
            if( !done ) {
                reportError( "Receiving endpoint is dead", msg );
            }
        }

        /**
         * Called when the token has been sent.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param token the sent token
         */
        public void tokenSent(
                PingPongMessageEndpoint<PingPongRpcTestMessage> endpoint,
                long                                            token )
        {
            // ignore
        }

        /**
         * The endpoint to which this listener listens.
         */
        BidirectionalMessageEndpoint<PingPongRpcTestMessage> theEndpoint;
    }

    /**
     * A client-side endpoint for the PingPongRpcTests.
     */
    class PingPongRpcClientEndpoint
            extends
                RpcClientEndpoint<Long,Long,PingPongRpcTestMessage>
    {
        /**
         * Constructor.
         *
         * @param ep the underlying MessageEndPoint
         */
        public PingPongRpcClientEndpoint(
                BidirectionalMessageEndpoint<PingPongRpcTestMessage> ep )
        {
            super( ep );
        }

        /**
         * Marshal an RPC argument into an outgoing message.
         *
         * @param arg the argument
         * @return the marshalled argument
         */
        protected PingPongRpcTestMessage marshal(
                Long arg )
        {
            getLog().debug( "Marshaling " + arg );
            PingPongRpcTestMessage ret = new PingPongRpcTestMessage( arg );
            return ret;
        }

        /**
         * Unmarshal an incoming message into an RPC result.
         *
         * @param msg the marshalled result
         * @return the result
         */
        protected Long unmarshal(
                PingPongRpcTestMessage msg )
        {
            long ret = msg.getPayload();
            getLog().debug( "Unmarshaling " + ret );
            return ret;
        }
    }
}
