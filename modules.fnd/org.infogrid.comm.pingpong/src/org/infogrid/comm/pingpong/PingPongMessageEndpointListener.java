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

package org.infogrid.comm.pingpong;

import org.infogrid.comm.MessageEndpointListener;

/**
 * Adds more detail to MessageEndpointListener if it is known that
 * the ping-pong protocol is being used.
 * 
 * @param <T> the message type
 */
public interface PingPongMessageEndpointListener<T>
        extends
            MessageEndpointListener<T>
{
    /**
     * Called when the token has been received.
     *
     * @param endpoint the PingPongMessageEndpoint that sent this event
     * @param token the received token
     */
    public void tokenReceived(
            PingPongMessageEndpoint<T> endpoint,
            long                       token );
    
    /**
     * Called when the token has been sent.
     *
     * @param endpoint the PingPongMessageEndpoint that sent this event
     * @param token the sent token
     */
    public void tokenSent(
            PingPongMessageEndpoint<T> endpoint,
            long                       token );
}
