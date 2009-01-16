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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.util.logging.Log;

/**
 * Factors out functionality common to many SendingMessageEndpoint implementations
 * that use a "fire and forget" model.
 * 
 * @param <T> the message type
 */
public abstract class AbstractFireAndForgetSendingMessageEndpoint<T>
        extends
            AbstractSendingMessageEndpoint<T>
{
    private static final Log log = Log.getLogInstance( AbstractFireAndForgetSendingMessageEndpoint.class ); // our own, private logger

    /**
     * Constructor for subclasses only.
     * 
     * @param name the name of the MessageEndpoint (for debugging only)
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @param messagesToBeSent outgoing message queue (may or may not be empty)
     */
    protected AbstractFireAndForgetSendingMessageEndpoint(
            String                   name,
            double                   randomVariation,
            ScheduledExecutorService exec,
            List<T>                  messagesToBeSent )
    {
        super( name, randomVariation, exec, messagesToBeSent );
    }

    /**
     * Send a message.
     *
     * @param msg the Message to send.
     */
    @Override
    public void enqueueMessageForSend(
            T msg )
    {
        super.enqueueMessageForSend( msg );
        
        synchronized( this ) {
            if( theFutureTask != null ) {
                theFutureTask.cancel(); // This is pessimistic if enqueueMessageForSend is invoked N times very rapidly
            }
            super.schedule( new SendTask( this ), 0 ); // immediately
        }
    }

    /**
     * Invoked when the timer triggers.
     *
     * @param task the TimedTask that invokes this method
     */
    protected void doAction(
            TimedTask task )
    {
        List<T> toSend = new ArrayList<T>();
        
        synchronized( this ) {
            toSend.addAll( theMessagesToBeSent );
            theMessagesToBeSent.clear();
        }

        // we can send messages out of order with SMTP
        List<T> failed = new ArrayList<T>();
        for( T current : toSend ) {

            try {
                attemptSend( current );

            } catch( IOException ex ) {
                failed.add( current );
                
                if( log.isInfoEnabled() ) {
                    log.info( "Could not send message " + current );
                }
            }
        }
        
        if( !failed.isEmpty() ) {
            synchronized( this ) {
                theMessagesToBeSent.addAll( 0, failed ); // prepending at the beginning
        
                if( theFutureTask == null || theFutureTask.isCancelled() ) {
                    schedule( new ResendTask( this ), 0 );
                }
            }
        }
    }

    /**
     * Attempt to send one message.
     * 
     * @param msg the Message to send.
     * @throws IOException the message send failed
     */
    protected abstract void attemptSend(
            T msg )
        throws
            IOException;

    /**
     * The send task.
     */
    protected static class SendTask
            extends
                TimedTask
    {
        /**
         * Constructor.
         *
         * @param ep the endpoint that is supposed to respond
         */
        public SendTask(
                AbstractSendingMessageEndpoint ep )
        {
            super( ep );
        }        
    }
    
    /**
     * The resend task.
     */
    protected static class ResendTask
            extends
                TimedTask
    {
        /**
         * Constructor.
         *
         * @param ep the endpoint that is supposed to respond
         */
        public ResendTask(
                AbstractSendingMessageEndpoint ep )
        {
            super( ep );
        }        
    }
}
