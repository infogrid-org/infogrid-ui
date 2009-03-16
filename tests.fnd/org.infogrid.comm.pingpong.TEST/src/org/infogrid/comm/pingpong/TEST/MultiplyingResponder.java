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

package org.infogrid.comm.pingpong.TEST;

import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.logging.Log;

/**
 * A listener that responds to incoming messages with the square of the payload.
 */
public class MultiplyingResponder
        extends
            AbstractPingPongListener
{
    private static final Log log = Log.getLogInstance( MultiplyingResponder.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param end the endpoint where this listener listens
     * @param test the test currently being run
     */
    public MultiplyingResponder(
            PingPongMessageEndpoint<TestMessage> end,
            AbstractTest                         test )
    {
        super( end, test );
    }

    /**
     * Called when an incoming message has arrived.
     *
     * @param endpoint the PingPongMessageEndpoint that sent this event
     * @param msg the received message
     */
    public void messageReceived(
            ReceivingMessageEndpoint<TestMessage> endpoint,
            TestMessage                           msg )
    {
        long ret = msg.getPayload();

        log.traceMethodCallEntry( this, "messageReceived", endpoint, msg );

        ret = ret * ret;

        TestMessage returnMessage = new TestMessage( ret );
        returnMessage.setResponseId( msg.getRequestId() );

        theEndpoint.enqueueMessageForSend( returnMessage );
    }

    /**
     * Called when the token has been received.
     *
     * @param endpoint the PingPongMessageEndpoint that sent this event
     * @param token the received token
     */
    public void tokenReceived(
            PingPongMessageEndpoint<TestMessage> endpoint,
            long                                 token )
    {
        // ignore
    }

    /**
      * Tell this object that we don't need it any more.
      */
    public void die()
    {
        // nothing
    }
}