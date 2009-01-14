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

package org.infogrid.comm;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.infogrid.util.AbstractListenerSet;
import org.infogrid.util.FlexibleListenerSet;
import org.infogrid.util.logging.Log;

/**
 * Factors out functionality common to many SendingMessageEndpoint implementations.
 * 
 * @param <T> the message type
 */
public abstract class AbstractSendingMessageEndpoint<T>
        implements
            SendingMessageEndpoint<T>
{
    private static final Log log = Log.getLogInstance( AbstractSendingMessageEndpoint.class ); // our own, private logger

    /**
     * Constructor for subclasses only.
     * 
     * @param name the name of the MessageEndpoint (for debugging only)
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @param messagesToBeSent outgoing message queue (may or may not be empty)
     */
    protected AbstractSendingMessageEndpoint(
            String                   name,
            double                   randomVariation,
            ScheduledExecutorService exec,
            List<T>                  messagesToBeSent )
    {
        theName             = name;
        theRandomVariation  = randomVariation;
        theExecutorService  = exec;
        theMessagesToBeSent = messagesToBeSent;

        if( messagesToBeSent == null ) {
            throw new NullPointerException( "Must not provide null messagesToBeSent" );
        }
    }

    /**
     * Send a message via the next ping or pong.
     *
     * @param msg the Message to send.
     */
    public void enqueueMessageForSend(
            T msg )
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".enqueueMessageForSend( " + msg + " )" );
        }
        
        synchronized( theMessagesToBeSent ) {
            theMessagesToBeSent.add( msg );
        }
        theListeners.fireEvent( msg, MESSAGE_ENQUEUED );
    }

    /**
     * Obtain the Messages still to be sent.
     *
     * @return the messages
     */
    public List<T> messagesToBeSent()
    {
        ArrayList<T> ret = new ArrayList<T>( theMessagesToBeSent.size());
        ret.addAll( theMessagesToBeSent );

        return ret;
    }
    
    /**
     * Calculate the next time of something, given a base and the random variation.
     *
     * @param base the base value of the property, e.g. theDeltaRespond
     * @return the next time
     */
    protected long calculateRandomizedFuture(
            long base )
    {
        // this also works for base=0

        double almost = ( ( Math.random() - 0.5f ) * theRandomVariation + 1. ) * base;
        long   ret    = (long) almost;
        
        return ret;
    }

    /**
     * Schedule a future task.
     *
     * @param task the TimedTask to schedule
     * @param base the base value of the time delay, e.g. theDeltaRespond
     */
    protected void schedule(
            TimedTask task,
            long      base )
    {
        long actual = calculateRandomizedFuture( base );

        if( log.isDebugEnabled() ) {
            log.debug( this + ".schedule( " + task + ", " + base + " ) in " + actual + " msec" );
        }

        try {
            theFuture = theExecutorService.schedule( task, actual, TimeUnit.MILLISECONDS );

        } catch( RejectedExecutionException ex ) {
            if( !theExecutorService.isShutdown() ) {
                log.warn(
                        this    + ": could not schedule task "
                                + task
                                + " with ExecutorService "
                                + theExecutorService,
                        ex );
            }
        }
    }
    
    /**
     * Invoked when the timer triggers.
     *
     * @param task the TimedTask that invokes this method
     */
    protected abstract void doAction(
            TimedTask task );

    /**
     * Add a MessageEndpointListener.
     *
     * @param newListener the listener to add
     */
    public void addDirectMessageEndpointListener(
            MessageEndpointListener<T> newListener )
    {
        theListeners.addDirect( newListener );
    }
    
    /**
     * Add a MessageEndpointListener.
     *
     * @param newListener the listener to add
     */
    public void addWeakMessageEndpointListener(
            MessageEndpointListener<T> newListener )
    {
        theListeners.addWeak( newListener );
    }
    
    /**
     * Add a MessageEndpointListener.
     *
     * @param newListener the listener to add
     */
    public void addSoftMessageEndpointListener(
            MessageEndpointListener<T> newListener )
    {
        theListeners.addSoft( newListener );
    }
    
    /**
     * Remove a MessageEndpointListener.
     *
     * @param oldListener the listener to remove
     */
    public void removeMessageEndpointListener(
            MessageEndpointListener<T> oldListener )
    {
        theListeners.remove( oldListener );
    }

    /**
     * Name of the endpoint (for debugging).
     */
    protected String theName;
    
    /**
     * The outgoing queue of Messages to send.
     */
    protected final List<T> theMessagesToBeSent;
    
    /**
     * The means by which to execute tasks.
     */
    protected ScheduledExecutorService theExecutorService;
    
    /**
     * The Future representing the TimedTask.
     */
    protected ScheduledFuture<?> theFuture;
    
    /**
     * Multiplier of random variation on theDeltaResponse and theDeltaRecover, in order to avoid
     * having repeated "collisions".
     */
    protected double theRandomVariation;
    
    /**
     * Captures the types of events that can be sent to the listeners.
     * This looks like it should be an enum type, but that would mean
     * subclasses can't define new event types; so it is not.
     * 
     * @param <T> the message type
     */
    protected static abstract class EventType<T>
    {
        /**
         * Send the event.
         * 
         * @param sender the endpoint that sent the event
         * @param listener the listener to which the event should be sent
         * @param event the event Object itself
         */
        public abstract void fireEvent(
                MessageEndpoint<T>         sender,
                MessageEndpointListener<T> listener,
                Object                     event );
    }

    /**
     * Indicates that a message was sent successfully.
     */   
    protected final EventType<T> MESSAGE_SENT = new EventType<T>() {
            @SuppressWarnings( "unchecked" )
            public void fireEvent(
                    MessageEndpoint<T>         sender,
                    MessageEndpointListener<T> listener,
                    Object                     event )
            {
                SendingMessageEndpoint<T> realSender =(SendingMessageEndpoint<T>) sender;
                listener.messageSent( realSender, (T) event );
            }
    };

    /**
     * Indicates that a message was received.
     */
    protected final EventType<T> MESSAGE_RECEIVED = new EventType<T>() {
            @SuppressWarnings( "unchecked" )
            public void fireEvent(
                    MessageEndpoint<T>         sender,
                    MessageEndpointListener<T> listener,
                    Object                     event )
            {
                ReceivingMessageEndpoint<T> realSender =(ReceivingMessageEndpoint<T>) sender;
                listener.messageReceived( realSender, (T) event );
            }
    };
    
    /**
     * Indicates that a message was enqueued, but not sent yet.
     */
    protected final EventType<T> MESSAGE_ENQUEUED = new EventType<T>() {
            @SuppressWarnings( "unchecked" )
            public void fireEvent(
                    MessageEndpoint<T>         sender,
                    MessageEndpointListener<T> listener,
                    Object                     event )
            {
                SendingMessageEndpoint<T> realSender =(SendingMessageEndpoint<T>) sender;
                listener.messageEnqueued( realSender, (T) event );
            }
    };
    
    /**
     * Indicates that sending a message failed.
     */
    protected final EventType<T> MESSAGE_SENDING_FAILED = new EventType<T>() {
            @SuppressWarnings( "unchecked" )
            public void fireEvent(
                    MessageEndpoint<T>         sender,
                    MessageEndpointListener<T> listener,
                    Object                     event )
            {
                SendingMessageEndpoint<T> realSender =(SendingMessageEndpoint<T>) sender;
                listener.messageSendingFailed( realSender, (T) event );
            }
    };

    /**
     * The current set of MessageEndpointListeners.
     */
    protected AbstractListenerSet<MessageEndpointListener<T>,Object,Object> theListeners
            = new FlexibleListenerSet<MessageEndpointListener<T>,Object,Object>()
    {
        /**
         * Fire the event to one contained object.
         *
         * @param listener the receiver of this event
         * @param event the sent event
         * @param parameter dispatch parameter
         */
        @SuppressWarnings(value={"unchecked"})
        protected void fireEventToListener(
                MessageEndpointListener<T> listener,
                Object                     event,
                Object                     parameter )
        {
            try {
                if( parameter instanceof EventType ) {
                    EventType realParameter = (EventType) parameter;
                    
                    realParameter.fireEvent( AbstractSendingMessageEndpoint.this, listener, event );

                } else if( parameter == null || parameter instanceof Throwable ) {
                    listener.disablingError( AbstractSendingMessageEndpoint.this, (List<T>) event, (Throwable) parameter );

                } else {
                    log.error( "unknown parameter: " + parameter );
                }
            } catch( Throwable t ) {
                log.error( t );
            }
        }
    };

    /**
     * The task to perform actions later, such as the resending of messages that could not be delivered.
     */    
    protected static abstract class TimedTask
            implements
                Runnable
    {
        /**
         * Constructor.
         *
         * @param ep the endpoint that is supposed to respond. This is kept internally as a WeakReference
         */
        public TimedTask(
                AbstractSendingMessageEndpoint ep )
        {
            theEndpointRef = new WeakReference<AbstractSendingMessageEndpoint>( ep );
        }

        /**
         * Run the task.
         */
        public void run()
        {
            AbstractSendingMessageEndpoint ep = theEndpointRef.get();
            
            if( ep != null ) {
                if( log.isDebugEnabled() ) {
                    log.debug( "TimedTask about to invoke " + ep );
                }
                try {
                    ep.doAction( this );

                } catch( Throwable t ) {
                    log.error( t );
                }
            } else {
                log.debug( "TimedTask cannot execute, AbstractSendingMessageEndpoint has gone away" );
            }
        }
        
        /**
         * Reference to the endpoint. This is a WeakReference as we don't want to get in
         * the way of the endpoint being garbage collected.
         */
        protected WeakReference<AbstractSendingMessageEndpoint> theEndpointRef;
    }
}
