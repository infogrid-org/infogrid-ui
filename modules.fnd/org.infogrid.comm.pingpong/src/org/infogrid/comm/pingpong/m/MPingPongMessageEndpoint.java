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

package org.infogrid.comm.pingpong.m;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.comm.MessageEndpointIsDeadException;
import org.infogrid.comm.MessageSendException;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * In-memory implementation of PingPongMessageEndpoint. This implementation supports
 * the on-disk storage of a snapshot of this PingPongMessageEndpoint, and its recovery
 * later, with a special factory method for that purpose.
 * 
 * @param <T> the message type
 */
public class MPingPongMessageEndpoint<T>
        extends
            PingPongMessageEndpoint<T>
        implements
            CanBeDumped
{
    private static final Log log = Log.getLogInstance( MPingPongMessageEndpoint.class ); // our own, private logger

    /**
     * Factory method.
     * 
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @return the created MPingPongMessageEndpoint
     * @param <T> the message type
     */
    public static <T> MPingPongMessageEndpoint<T> create(
            ScheduledExecutorService exec )
    {
        String name                    = "MPingPongMessageEndpoint";
        long   deltaRespondNoMessage   = theResourceHelper.getResourceLongOrDefault(   "DeltaRespondNoMessage",   1000L );
        long   deltaRespondWithMessage = theResourceHelper.getResourceLongOrDefault(   "DeltaRespondWithMessage",   10L ); // quickly but still deterministic
        long   deltaResend             = theResourceHelper.getResourceLongOrDefault(   "DeltaResend",              500L );
        long   deltaRecover            = theResourceHelper.getResourceLongOrDefault(   "DeltaRecover",            5000L );
        double randomVariation         = theResourceHelper.getResourceDoubleOrDefault( "RandomVariation",          0.02 ); // 2%

        // it is advantageous if the recover time is larger than 4 times the respond time: that way, a
        // second RPC call can be successfully completed before returning to the parent RPC caller.
        
        MPingPongMessageEndpoint<T> ret = new MPingPongMessageEndpoint<T>(
                name,
                deltaRespondNoMessage,
                deltaRespondWithMessage,
                deltaResend,
                deltaRecover,
                randomVariation,
                exec );

        if( log.isTraceEnabled() ) {
            log.traceConstructor( ret );
        }
        return ret;
    }

    /**
     * Factory method.
     *
     * @param name the name of the PingPongMessageEndpoint (for debugging only)
     * @param deltaRespondNoMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if no message is in the queue
     * @param deltaRespondWithMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if a message is in the queue
     * @param deltaResend  the number of milliseconds until this PingPongMessageEndpoint resends the token if sending the token failed
     * @param deltaRecover the number of milliseconds until this PingPongMessageEndpoint decides that the token
     *                     was not received by the partner PingPongMessageEndpoint, and resends
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @return the created MPingPongMessageEndpoint
     * @param <T> the message type
     */
    public static <T> MPingPongMessageEndpoint<T> create(
            String                   name,
            long                     deltaRespondNoMessage,
            long                     deltaRespondWithMessage,
            long                     deltaResend,
            long                     deltaRecover,
            double                   randomVariation,
            ScheduledExecutorService exec )
    {
        MPingPongMessageEndpoint<T> ret = new MPingPongMessageEndpoint<T>(
                name,
                deltaRespondNoMessage,
                deltaRespondWithMessage,
                deltaResend,
                deltaRecover,
                randomVariation,
                exec );

        if( log.isTraceEnabled() ) {
            log.traceConstructor( ret );
        }
        return ret;
    }

    /**
     * Factory method.
     *
     * @param name the name of the PingPongMessageEndpoint (for debugging only)
     * @param deltaRespondNoMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if no message is in the queue
     * @param deltaRespondWithMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if a message is in the queue
     * @param deltaResend  the number of milliseconds until this PingPongMessageEndpoint resends the token if sending the token failed
     * @param deltaRecover the number of milliseconds until this PingPongMessageEndpoint decides that the token
     *                     was not received by the partner PingPongMessageEndpoint, and resends
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @param lastSentToken the last token sent in a previous instantiation of this MessageEndpoint
     * @param lastReceivedToken the last token received in a previous instantiation of this MessageEndpoint
     * @param messagesSentLast the last set of Messages sent in a previous instantiation of this MessageEndpoint
     * @param messagesToBeSent the Messages to be sent from a previous instantiation of this MessageEndpoint
     * @return the created MPingPongMessageEndpoint
     * @param <T> the message type
     */
    public static <T> MPingPongMessageEndpoint<T> restore(
            String                   name,
            long                     deltaRespondNoMessage,
            long                     deltaRespondWithMessage,
            long                     deltaResend,
            long                     deltaRecover,
            double                   randomVariation,
            ScheduledExecutorService exec,
            long                     lastSentToken,
            long                     lastReceivedToken,
            List<T>                  messagesSentLast,
            List<T>                  messagesToBeSent )
    {
        MPingPongMessageEndpoint<T> ret = new MPingPongMessageEndpoint<T>(
                name,
                deltaRespondNoMessage,
                deltaRespondWithMessage,
                deltaResend,
                deltaRecover,
                randomVariation,
                exec,
                lastSentToken,
                lastReceivedToken,
                messagesSentLast,
                messagesToBeSent );

        if( log.isTraceEnabled() ) {
            log.traceConstructor( ret );
        }
        return ret;
    }

    /**
     * Constructor.
     *
     * @param name the name of the PingPongMessageEndpoint (for debugging only)
     * @param deltaRespondNoMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if no message is in the queue
     * @param deltaRespondWithMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if a message is in the queue
     * @param deltaResend  the number of milliseconds until this PingPongMessageEndpoint resends the token if sending the token failed
     * @param deltaRecover the number of milliseconds until this PingPongMessageEndpoint decides that the token
     *                     was not received by the partner PingPongMessageEndpoint, and resends
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule timed tasks
     */
    protected MPingPongMessageEndpoint(
            String                   name,
            long                     deltaRespondNoMessage,
            long                     deltaRespondWithMessage,
            long                     deltaResend,
            long                     deltaRecover,
            double                   randomVariation,
            ScheduledExecutorService exec )
    {
        this(   name,
                deltaRespondNoMessage,
                deltaRespondWithMessage,
                deltaResend,
                deltaRecover,
                randomVariation,
                exec,
                -1,
                -1,
                null,
                new ArrayList<T>() );
    }

    /**
     * Constructor.
     *
     * @param name the name of the PingPongMessageEndpoint (for debugging only)
     * @param deltaRespondNoMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if no message is in the queue
     * @param deltaRespondWithMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if a message is in the queue
     * @param deltaResend  the number of milliseconds until this PingPongMessageEndpoint resends the token if sending the token failed
     * @param deltaRecover the number of milliseconds until this PingPongMessageEndpoint decides that the token
     *                     was not received by the partner PingPongMessageEndpoint, and resends
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @param lastSentToken the last token sent in a previous instantiation of this MessageEndpoint
     * @param lastReceivedToken the last token received in a previous instantiation of this MessageEndpoint
     * @param messagesSentLast the last set of Messages sent in a previous instantiation of this MessageEndpoint
     * @param messagesToBeSent outgoing message queue (may or may not be empty)
     */
    protected MPingPongMessageEndpoint(
            String                   name,
            long                     deltaRespondNoMessage,
            long                     deltaRespondWithMessage,
            long                     deltaResend,
            long                     deltaRecover,
            double                   randomVariation,
            ScheduledExecutorService exec,
            long                     lastSentToken,
            long                     lastReceivedToken,
            List<T>                  messagesSentLast,
            List<T>                  messagesToBeSent )
    {
        super(  name,
                deltaRespondNoMessage,
                deltaRespondWithMessage,
                deltaResend, 
                deltaRecover,
                randomVariation,
                exec,
                lastSentToken,
                lastReceivedToken,
                messagesSentLast,
                messagesToBeSent );
    }

    /**
     * Set the partner endpoint and start communicating.
     *
     * @param partner the partner
     */
    public void setPartnerAndInitiateCommunications(
            MPingPongMessageEndpoint<T> partner )
    {
        setPartner( partner );
        
        startCommunicating();
    }
    
    /**
     * Set the partner endpoint.
     *
     * @param partner the partner
     */
    public void setPartner(
            MPingPongMessageEndpoint<T> partner )
    {
        if( thePartner != null ) {
            throw new IllegalStateException();
        }
        if( thePartner == this ) {
            throw new IllegalArgumentException( "Cannot communicate with myself" );
        }
        thePartner = partner;

        thePartner.thePartner = this; // point back to us
    }

    /**
     * Send a message via the next ping or pong.
     *
     * @param msg the Message to send.
     */
    @Override
    public void enqueueMessageForSend(
            T msg )
    {
        if( isGracefullyDead ) {
            throw new IllegalStateException( this + " is dead" );
        }
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "enqueueMessageForSend", msg );
        }

        synchronized( theMessagesToBeSent ) {
            theMessagesToBeSent.add( msg );

            if( hasToken() ) {
                // if our listeners have entered messages into the queue during callback ("response")
                TimedTask t = theFutureTask;
                if( t != null ) {
                    t.cancel();
                }
                schedule( new RespondTask( this ), theDeltaRespondWithMessage );
            }
        }

        theListeners.fireEvent( msg, MESSAGE_ENQUEUED );
    }

    /**
     * Send a message to the partner indicating that we'd like to have the token back as quickly as possible.
     */
    protected void sendGrabTokenMessage()
    {
        MPingPongMessageEndpoint p = thePartner;
        if( p == null ) {
            return;
        }
        TimedTask t = p.theFutureTask;
        if( t instanceof RespondTask ) {
            t.cancel();
            p.schedule( t, theDeltaRespondWithMessage );
        }
    }

    /**
     * Do the message send.
     *
     * @param token the token of the message
     * @param content the content to send.
     * @throws MessageSendException thrown if the message could not be sent
     */
    protected void sendMessage(
            long    token,
            List<T> content )
        throws
            MessageSendException
    {
        if( content != null && !content.isEmpty() && log.isInfoEnabled() ) {
            log.info( this, "sendMessage", token, content );
        } else if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "sendMessage", token, content );
        }

        MPingPongMessageEndpoint<T> partner = thePartner;
        if( partner != null ) {
            partner.incomingMessage( token, content );
        } else {
            throw new MessageSendException( content, "No partner MPingPongMessageEndpoint has been set" );
        }
    }

    /**
     * Invoked by subclasses to provide the content of a received message.
     *
     * @param token the integer representing the token
     * @param content the content of a received message
     * @throws MessageEndpointIsDeadException thrown if the MessageEndpoint is dead
     * @throws MessageSendException thrown if the message could not be sent
     */
    @Override
    protected void incomingMessage(
            long    token,
            List<T> content )
        throws
            MessageEndpointIsDeadException,
            MessageSendException
    {
        if( isGracefullyDead ) {
            throw new MessageEndpointIsDeadException();
        } else {

            try {
                super.incomingMessage( token, content );

            } catch( RejectedExecutionException ex ) {
                throw new MessageEndpointIsDeadException( ex );
            }
        }
    }

    /**
     * Obtain the messages that were sent most recently.
     *
     * @return the List
     */
    @SuppressWarnings(value={"unchecked"})
    public List<T> messagesLastSent()
    {
        List<T>      found = theMessagesSentLast; // this avoids a warning about synchronizing on a non-final member variable
        ArrayList<T> ret   = new ArrayList<T>( found != null ? found.size() : 1 );

        if( found != null ) {
            synchronized( found ) {
                if( !found.isEmpty() ) {
                    ret.addAll( found );
                }
                return ret;
            }
        }
        return ret;
    }

    /**
     * Attempt to send the outgoing messages, but stop receiving incoming messages.
     */
    public void gracefulDie()
    {
        isGracefullyDead = true;
        
        if( theFutureTask != null ) {
            if( log.isDebugEnabled() ) {
                log.debug( this + " canceling future" );
            }
            theFutureTask.cancel();
            theFutureTask = null;
        }
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    @Override
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theName",
                    "isGracefullyDead",
                    "theLastReceivedToken",
                    "theLastSentToken",
                    "theFutureTask",
                    "theMessagesToBeSent"
                },
                new Object[] {
                    theName,
                    isGracefullyDead,
                    theLastReceivedToken,
                    theLastSentToken,
                    theFutureTask,
                    theMessagesToBeSent
                });
    }

    /**
     * The partner PingPongMessageEndpoint.
     */
    protected MPingPongMessageEndpoint<T> thePartner;
    
    /**
     * If this is true, the MessageEndpoint is dead, but still attempts to send queued
     * messages.
     */
    protected boolean isGracefullyDead = false;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( MPingPongMessageEndpoint.class );
}
