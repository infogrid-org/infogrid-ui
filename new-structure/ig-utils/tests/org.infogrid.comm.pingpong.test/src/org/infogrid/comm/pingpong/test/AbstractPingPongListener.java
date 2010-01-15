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
import org.infogrid.comm.BidirectionalMessageEndpoint;
import org.infogrid.comm.MessageEndpoint;
import org.infogrid.comm.SendingMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpointListener;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.AbstractLiveDeadObject;
import org.infogrid.util.logging.Log;

    /**
     * Factors out common functionality for listeners in PingPongTests.
     */
public abstract class AbstractPingPongListener
        extends
            AbstractLiveDeadObject
        implements
            PingPongMessageEndpointListener<TestMessage>
{
    /**
     * Constructor.
     *
     * @param end the endpoint where this listener listens
     * @param test the test currently being run
     */
    public AbstractPingPongListener(
            BidirectionalMessageEndpoint<TestMessage> end,
            AbstractTest                              test )
    {
        theEndpoint = end;
        theTest     = test;
    }

    /**
     * Called when an outgoing message has been sent.
     *
     * @param endpoint the MessageEndpoint that sent this event
     * @param msg the sent message
     */
    public void messageSent(
            SendingMessageEndpoint<TestMessage> endpoint,
            TestMessage                         msg )
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
            SendingMessageEndpoint<TestMessage> endpoint,
            TestMessage                         msg )
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
            SendingMessageEndpoint<TestMessage> endpoint,
            TestMessage                         msg )
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
            MessageEndpoint<TestMessage> endpoint,
            List<TestMessage>            msg,
            Throwable                    t )
    {
        if( !isDead() ) {
            theTest.reportError( "Receiving endpoint is dead", msg );
        }
    }

    /**
     * Called when the token has been sent.
     *
     * @param endpoint the MessageEndpoint that sent this event
     * @param token the sent token
     */
    public void tokenSent(
            PingPongMessageEndpoint<TestMessage> endpoint,
            long                                 token )
    {
        // ignore
    }

    /**
     * Helper to get the log.
     *
     * @return the log
     */
    protected Log getLog()
    {
        return Log.getLogInstance( theTest.getClass() );
    }

    /**
     * The endpoint to which this listener listens.
     */
    protected BidirectionalMessageEndpoint<TestMessage> theEndpoint;

    /**
     * The test that's being run.
     */
    protected AbstractTest theTest;
}
