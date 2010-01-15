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
import org.infogrid.comm.BidirectionalMessageEndpoint;
import org.infogrid.comm.MessageEndpoint;
import org.infogrid.comm.RpcClientEndpoint;
import org.infogrid.comm.SendingMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpointListener;
import org.infogrid.testharness.AbstractTest;

/**
 * Factors out functionality common to PingPongRpcTests.
 */
public abstract class AbstractPingPongRpcTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     */
    protected AbstractPingPongRpcTest()
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
     * A client-side endpoint for the PingPongRpcTests.
     */
    class PingPongRpcClientEndpoint
            extends
                RpcClientEndpoint<Long,Long,TestMessage>
    {
        /**
         * Constructor.
         * 
         * @param ep the underlying MessageEndPoint
         */
        public PingPongRpcClientEndpoint(
                BidirectionalMessageEndpoint<TestMessage> ep )
        {
            super( ep );
        }
        
        /**
         * Marshal an RPC argument into an outgoing message.
         * 
         * @param arg the argument
         * @return the marshalled argument
         */
        protected TestMessage marshal(
                Long arg )
        {
            getLog().debug( "Marshaling " + arg );
            TestMessage ret = new TestMessage( arg );
            return ret;
        }

        /**
         * Unmarshal an incoming message into an RPC result.
         * 
         * @param msg the marshalled result
         * @return the result
         */
        protected Long unmarshal(
                TestMessage msg )
        {
            long ret = msg.getPayload();
            getLog().debug( "Unmarshaling " + ret );
            return ret;
        }
    }
}
