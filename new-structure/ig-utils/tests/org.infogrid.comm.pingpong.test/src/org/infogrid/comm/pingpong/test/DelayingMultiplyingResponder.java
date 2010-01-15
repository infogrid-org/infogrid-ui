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

import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.logging.Log;

/**
 * Listener that responds only on the second message.
 */
public class DelayingMultiplyingResponder
        extends
            AbstractPingPongListener
{
    private static final Log log = Log.getLogInstance( DelayingMultiplyingResponder.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param end the endpoint where this listener listens
     * @param test the test currently being run
     */
    public DelayingMultiplyingResponder(
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
            final TestMessage                     msg )
    {
        log.traceMethodCallEntry( this, "messageReceived", endpoint, msg );

        if( theOldMessage != null ) {
            long ret = theOldMessage.getPayload();

            log.debug( "Received message, calculating return for old message " + ret );

            ret = ret * ret;

            TestMessage returnMessage = new TestMessage( ret );
            returnMessage.setResponseId( theOldMessage.getRequestId() );

            theEndpoint.enqueueMessageForSend( returnMessage );

            theOldMessage = null;

        } else {
            log.debug( "Received message, responding with unrelated message" );

            TestMessage returnMessage = new TestMessage( 4 ) {
                    @Override
                    public String toString()
                    {
                        return super.toString() + ", UNRELATED: sent when payload " + msg.getPayload() + " came in";
                    }
            };
            if( msg.getPayload() >= 2 ) {
                returnMessage.setResponseId( msg.getPayload() -2 );
                // send an unrelated, old response id
            }

            theEndpoint.enqueueMessageForSend( returnMessage );
        }

        theOldMessage = msg;
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
        if( theOldMessage != null ) {
            long ret = theOldMessage.getPayload();

            log.debug( "Received token, calculating return for old message " + ret );

            ret = ret * ret;

            TestMessage returnMessage = new TestMessage( ret );
            returnMessage.setResponseId( theOldMessage.getRequestId() );

            theEndpoint.enqueueMessageForSend( returnMessage );

            theOldMessage = null;
        }
    }


    /**
      * Tell this object that we don't need it any more.
      */
    public void die()
    {
        // nothing
    }

    /**
     * Store for the last incoming message, so we can respond to it later.
     */
    protected TestMessage theOldMessage = null;
}