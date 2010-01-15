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

/**
 * A message endpoint for sending messages. The method 
 * enqueueMessageForSend is used for asynchronous sending of messages.
 * 
 * @param <T> the message type
 */
public interface SendingMessageEndpoint<T>
        extends
            MessageEndpoint<T>
{
    /**
     * Send a message on the regular schedule.
     *
     * @param msg the Message to send.
     */
    public void enqueueMessageForSend(
            T msg );

    /**
     * Send a message as quickly as possible.
     *
     * @param msg the Message to send.
     */
    public void sendMessageAsap(
            T msg );

    /**
     * Obtain the Messages still to be sent.
     *
     * @return the messages
     */
    public abstract List<T> messagesToBeSent();    
}
