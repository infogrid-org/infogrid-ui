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

package org.infogrid.comm.pingpong;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.comm.AbstractSendingMessageEndpoint;
import org.infogrid.comm.BidirectionalMessageEndpoint;
import org.infogrid.comm.MessageEndpoint;
import org.infogrid.comm.MessageEndpointIsDeadException;
import org.infogrid.comm.MessageEndpointListener;
import org.infogrid.comm.MessageSendException;
import org.infogrid.util.StringHelper;
import org.infogrid.util.logging.Log;

/**
 * <p>Endpoint for bidirectional communications using the ping-pong protocol.
 *    This is abstract: subclasses need to implement the atual message transfer mechanism.</p>
 * <p>In order to avoid difficult timing conditions, all time constants are continually modified with
 *    a slight, random delta.</p>
 * <p>This class supports a regular and a low-level logger, which reflect application-developer
 *    vs. protocol-developer-centric views of logging.</p>
 * 
 * @param <T> the message type
 */
public abstract class PingPongMessageEndpoint<T>
        extends
            AbstractSendingMessageEndpoint<T>
        implements
            BidirectionalMessageEndpoint<T>
{
    private static final Log logHigh = Log.getLogInstance( PingPongMessageEndpoint.class ); // our own, private logger for high-level events
    private static final Log logLow  = Log.getLogInstance( PingPongMessageEndpoint.class.getName() + "-lowlevel" ); // our own, private logger for low-level events

    /**
     * Constructor for subclasses only.
     *
     * @param name the name of the MessageEndpoint (for debugging only)
     * @param deltaRespondNoMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if no message is in the queue
     * @param deltaRespondWithMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if a message is in the queue
     * @param deltaResend  the number of milliseconds until this PingPongMessageEndpoint resends the token if sending the token failed
     * @param deltaRecover the number of milliseconds until this PingPongMessageEndpoint decides that the token
     *                     was not received by the partner PingPongMessageEndpoint, and resends
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @param lastSentToken the last token sent in a previous instantiation of this BidirectionalMessageEndpoint
     * @param lastReceivedToken the last token received in a previous instantiation of this BidirectionalMessageEndpoint
     * @param messagesSentLast the last set of Messages sent in a previous instantiation of this BidirectionalMessageEndpoint
     * @param messagesToBeSent outgoing message queue (may or may not be empty)
     */
    protected PingPongMessageEndpoint(
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
        super( name, randomVariation, exec, messagesToBeSent );

        theDeltaRespondNoMessage   = deltaRespondNoMessage;
        theDeltaRespondWithMessage = deltaRespondWithMessage;
        theDeltaResend             = deltaResend;
        theDeltaRecover            = deltaRecover;

        theLastSentToken     = lastSentToken;
        theLastReceivedToken = lastReceivedToken;
        theMessagesSentLast  = messagesSentLast;
    }

    /**
     * Start the ping-pong.
     */
    public void startCommunicating()
    {
        if( theFutureTask == null ) {
            doAction( null ); // null indicates startCommunicating()
        }
    }

    /**
     * Stop the ping-pong.
     */
    public void stopCommunicating()
    {
        TimedTask future = theFutureTask;
        if( future != null ) {
            future.cancel();
            theFutureTask = null;
        }
    }

    /**
     * Invoked when the timer triggers.
     *
     * @param task the TimedTask that invokes this handler
     */
    protected synchronized void doAction(
            TimedTask task )
    {
        if( logLow.isDebugEnabled() ) {
            logLow.debug( this + ".doAction( " + task + " ): queue has length " + theMessagesToBeSent.size() );
        }        

        // determine whether this is a regular response, a resend, or a recover. Resend and regular
        // response are treated by the same code, the only difference is when it is invoked by the timer.
        
        long    tokenToSend = -1L;
        List<T> toBeSent    = null; // send nothing unless something is set

        synchronized( this ) {
            if( theLastReceivedToken < 0 ) {
                // never received anything
                if( theLastReceivedToken < 0 ) {
                    // never sent anything either
                    tokenToSend = 1; // very first token
                } else {
                    tokenToSend = theLastSentToken; // resend
                }

            } else if( ( theLastSentToken < 0 ) || ( theLastReceivedToken == theLastSentToken + 1 )) {
                // regular response
                tokenToSend = theLastReceivedToken + 1;

            } else if( theLastReceivedToken + 1 == theLastSentToken ) {
                // resend
                tokenToSend = theLastSentToken;
            } else {
                logLow.error( "No idea how we got here: " + this );
                return;
            }

            if( tokenToSend == theLastSentToken ) {
                // resend
                toBeSent = theMessagesSentLast;

            } else {
                // regular response
                synchronized( theMessagesToBeSent ) {
                    if( !theMessagesToBeSent.isEmpty() ) {
                        toBeSent = new LinkedList<T>();
                        toBeSent.addAll( theMessagesToBeSent );
                        theMessagesToBeSent.clear(); // we do this so the synchronized statement always works on the same object
                    }
                }
            }

            theLastSentToken    = tokenToSend;
            theMessagesSentLast = toBeSent;
        }

        // make sure we use local variables here, not member variables, in order to support concurrency
        if( logLow.isDebugEnabled() ) {
            logLow.debug( this + " doRespond: about to send message( " + toBeSent + " )" );
        }        
        try {
            schedule( new RecoverTask( this ), theDeltaRecover );
                // schedule a recover event prior to sending and firing events to listeners:
                // if the sending takes a long time, we don't want to block

        } catch( RejectedExecutionException ex ) {
            // underlying ThreadPool does not like this task
            if( logLow.isInfoEnabled() ) {
                logLow.info( this + " cannot schedule (" + tokenToSend + "): " + ( toBeSent != null ? toBeSent : "<empty>" ), ex );
            }
            
            theListeners.fireEvent( toBeSent, new MessageEndpointIsDeadException() );

            if( theFutureTask != null ) {
                // this is the recover future, we don't want to recover if message send failed, but resend
                if( logLow.isDebugEnabled() ) {
                    logLow.debug( this + " canceling future (RejectedExecutionException)" );
                }
                theFutureTask.cancel();
            }
        }
        
        try {
            // do not reschedule the future, we stop here

            sendMessage( tokenToSend, toBeSent );

            if( logHigh.isDebugEnabled() ) {
                logHigh.debug( this + " sent message (" + tokenToSend + ") successfully: " + ( toBeSent != null ? toBeSent : "<empty>" ));
            }

            theListeners.fireEvent( tokenToSend, TOKEN_SENT );
            if( toBeSent != null ) {
                for( T current : toBeSent ) {
                    theListeners.fireEvent( current, MESSAGE_SENT );
                }
            }
            
        } catch( MessageEndpointIsDeadException ex ) {
            if( logHigh.isInfoEnabled() ) {
                logHigh.info( this + " Endpoint is dead (" + tokenToSend + "): " + ( toBeSent != null ? toBeSent : "<empty>" ), ex );
            }
            
            theListeners.fireEvent( toBeSent, ex );

            if( theFutureTask != null ) {
                // this is the recover future, we don't want to recover if message send failed, but resend
                if( logLow.isDebugEnabled() ) {
                    logLow.debug( this + " canceling future (MessageEndpointIsDeadException)" );
                }
                theFutureTask.cancel();
            }
            
            // do not reschedule the future, we stop here
            
        } catch( MessageSendException ex ) {

            if( logHigh.isInfoEnabled() ) {
                logHigh.info( this + " failed to send message (" + tokenToSend + "): " + ( toBeSent != null ? toBeSent : "<empty>" ), ex );
            }

            if( theFutureTask != null ) {
                // this is the recover future, we don't want to recover if message send failed, but resend
                if( logLow.isDebugEnabled() ) {
                    logLow.debug( this + " canceling future (MessageSendException)" );
                }
                theFutureTask.cancel();
            }

            schedule( new ResendTask( this ), theDeltaResend );
                // schedule a resend event prior to firing events to listeners

            if( toBeSent != null ) { // can happen when endpoint is killed off
                for( T t : toBeSent ) {
                    theListeners.fireEvent( t, MESSAGE_SENDING_FAILED );
                }
            }

        } catch( Throwable t ) {
            // catch-all
            logHigh.error( "Unexpected exception", t );
        }
    }

    /**
     * Implemented by subclasses, this performs the actual message send.
     *
     * @param token the integer representing the token
     * @param content the payload, if any
     * @throws MessageSendException thrown if the message could not be sent
     */
    protected abstract void sendMessage(
            long    token,
            List<T> content )
        throws
            MessageSendException;

    /**
     * Invoked by subclasses to provide the content of a received message.
     *
     * @param token the integer representing the token
     * @param content the content of a received message
     * @throws MessageEndpointIsDeadException thrown if the MessageEndpoint is dead
     * @throws MessageSendException thrown if the message could not be sent
     */
    protected void incomingMessage(
            long    token,
            List<T> content )
        throws
            MessageEndpointIsDeadException,
            MessageSendException
    {
        boolean fireEvents = false;

        try {
            if( content != null && ! content.isEmpty() ) {
                if( logHigh.isInfoEnabled() ) {
                    logHigh.info( this + ".incomingMessage( " + token + ", " + content + " )" );
                }
            } else {
                if( logHigh.isDebugEnabled() ) {
                    logHigh.debug( this + ".incomingMessage( " + token + ", " + content + " )" );
                }
            }

            // ignore if we received this one already
            if( theLastReceivedToken != token ) {
                if( theFutureTask != null ) {
                    if( logLow.isDebugEnabled() ) {
                        logLow.debug( this + " canceling future (regular response required)" );
                    }
                    theFutureTask.cancel();
                }
                theLastReceivedToken = token;
                fireEvents = true;

            } else {
                logLow.warn( this + " ignoring duplicate incoming message(" + token + "): " + content );
            }

        } catch( Throwable t ) {
            logHigh.error( t );

        } finally {
            boolean slow;
            if( !theMessagesToBeSent.isEmpty() ) {
                schedule( new RespondTask( this ), theDeltaRespondWithMessage );
                slow = false;

            } else {
                schedule( new RespondTask( this ), theDeltaRespondNoMessage );
                slow = true;
            }

            if( fireEvents ) {
                theListeners.fireEvent( token, TOKEN_RECEIVED );
                if( content != null ) {
                    for( T current : content ) {
                        theListeners.fireEvent( current, MESSAGE_RECEIVED );
                    }
                }
            }

            if( slow && !theMessagesToBeSent.isEmpty() ) {
                // if our listeners have entered messages into the queue during callback ("response")
                TimedTask t = theFutureTask;
                if( t != null ) {
                    t.cancel();
                }
                schedule( new RespondTask( this ), theDeltaRespondWithMessage );
            }
        }
    }

    /**
     * Send a message as quickly as possible.
     *
     * @param msg the Message to send.
     */
    @Override
    public void sendMessageAsap(
            T msg )
    {
        enqueueMessageForSend( msg );
        if( !hasToken() ) {
            sendGrabTokenMessage();
        }
    }

    /**
     * Send a message to the partner indicating that we'd like to have the token back as quickly as possible.
     */
    protected abstract void sendGrabTokenMessage();

    /**
     * Obtain the token that was last sent.
     *
     * @return the token
     */
    public long getLastSentToken()
    {
        return theLastSentToken;
    }

    /**
     * Obtain the token that was last received.
     *
     * @return the token
     */
    public long getLastReceivedToken()
    {
        return theLastReceivedToken;
    }

    /**
     * Determine whether this PingPongMessageEndpoint currently has the token, or
     * whether it is waiting for the token.
     *
     * @return true if the PingPongMessageEndpoint currently has the token
     */
    public boolean hasToken()
    {
        TimedTask t = theFutureTask;
        if( t == null ) {
            return true;
        }
        if( t instanceof RespondTask ) {
            return true;
        }
        return false;
    }

    /**
     * Convert to String form, for debugging.
     *
     * @return String form
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "theName",
                    "theLastReceivedToken",
                    "theLastSentToken",
                    "theFutureTask",
                    "theMessagesToBeSent"
                },
                new Object[] {
                    theName,
                    theLastReceivedToken,
                    theLastSentToken,
                    theFutureTask,
                    theMessagesToBeSent
                });
    }

    /**
     * The time until we respond with a ping to an incoming pong, assumning no
     * message has been queued.
     */
    protected long theDeltaRespondNoMessage;

    /**
     * The time until we respond with a ping to an incoming pong, assumning that
     * at least one message has been queued.
     */
    protected long theDeltaRespondWithMessage;

    /**
     * The time until we retry to send the token if sending the token failed.
     */
    protected long theDeltaResend;
    
    /**
     * The time util we attempt to recover after having sent a ping whose pong did not arrive.
     */
    protected long theDeltaRecover;
    
    /**
     * Cached content of the last sent message, in order to be able to resend it upon
     * recover.
     */
    protected List<T> theMessagesSentLast;
    
    /**
     * The last token that was sent, in order to be able to resend it upon recover.
     */
    protected long theLastSentToken;
    
    /**
     * The last token that was received.
     */
    protected long theLastReceivedToken;

    /**
     * Indicates that a token was sent.
     */
    protected final EventType<T> TOKEN_SENT = new EventType<T>() {
            @SuppressWarnings( "unchecked" )
            public void fireEvent(
                    MessageEndpoint<T>         sender,
                    MessageEndpointListener<T> listener,
                    Object                     event )
            {
                if( listener instanceof PingPongMessageEndpointListener ) {
                    PingPongMessageEndpointListener realListener = (PingPongMessageEndpointListener) listener;
                    PingPongMessageEndpoint<T>      realSender   = (PingPongMessageEndpoint<T>) sender;
                    realListener.tokenSent( realSender, (Long) event );
                }
            }
    };
    
    /**
     * Indicates that a token was received.
     */
    protected final EventType<T> TOKEN_RECEIVED = new EventType<T>() {
            @SuppressWarnings( "unchecked" )
            public void fireEvent(
                    MessageEndpoint<T>         sender,
                    MessageEndpointListener<T> listener,
                    Object                     event )
            {
                if( listener instanceof PingPongMessageEndpointListener ) {
                    PingPongMessageEndpointListener realListener = (PingPongMessageEndpointListener) listener;
                    PingPongMessageEndpoint<T>      realSender   = (PingPongMessageEndpoint<T>) sender;
                    realListener.tokenReceived( realSender, (Long) event );
                }
            }
    };
    
    /**
     * The recover task.
     */
    protected static class RecoverTask
            extends
                TimedTask
    {
        /**
         * Constructor.
         *
         * @param ep the endpoint that is supposed to respond
         */
        public RecoverTask(
                PingPongMessageEndpoint ep )
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
                PingPongMessageEndpoint ep )
        {
            super( ep );
        }        
    }
    
    /**
     * The respond task.
     */
    protected static class RespondTask
            extends
                TimedTask
    {
        /**
         * Constructor.
         *
         * @param ep the endpoint that is supposed to respond
         */
        public RespondTask(
                PingPongMessageEndpoint ep )
        {
            super( ep );
        }        
    }
}
