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

package org.infogrid.comm.pingpong.TEST;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.comm.MessageEndpoint;
import org.infogrid.comm.MessageEndpointListener;
import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.comm.SendingMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;
import org.infogrid.comm.pingpong.m.MPingPongMessageEndpoint;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.logging.Log;

/**
 * Tests what happens if one side is very slow in processing.
 */
public class PingPongTest3
        extends
            AbstractTest
{
    /**
     * Test run.
     *
     * @throws Exception this code may throw any Exception
     */
    public void run()
            throws
                Exception
    {
        MPingPongMessageEndpoint<String> ep1 = MPingPongMessageEndpoint.create( "ep1", 1000L, 500L, 5000L, 0.f, exec1 );
        MPingPongMessageEndpoint<String> ep2 = MPingPongMessageEndpoint.create( "ep2", 1000L, 500L, 5000L, 0.f, exec2 );
        
        MyListener l1 = new MyListener( ep1, "one ",   10L );
        MyListener l2 = new MyListener( ep2, "two ", 8000L );
        ep1.addDirectMessageEndpointListener( l1 );
        ep2.addDirectMessageEndpointListener( l2 );

        log.info( "Starting to ping-pong" );
        log.debug( "Note that the events seem to be a bit out of order as we only print the event after it was successfully sent (and received)" );

        ep1.setPartnerAndInitiateCommunications( ep2 );
        
        // only after we initiate communications, so the first message send does not happen on the main thread
        
        ep1.enqueueMessageForSend( "seed" );

        Thread.sleep( 15000L ); // four ping and five pongs

        ep1.stopCommunicating();
        ep2.stopCommunicating();

        log.debug( "Checking received messages" );
        // make sure we received every message only once
        for( String msg : l2.receivedMessages.keySet() ) {
            Integer value = l2.receivedMessages.get( msg );
            
            log.debug( "Received message '" + msg + "' times " + value );
            
            checkEquals( 1, (int) value, "wrong value for message '" + msg + "'" );
        }        
    }

    /**
      * Main program.
      *
      * @param args command-line arguments
      */
    public static void main(
             String [] args )
    {
        PingPongTest3 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }
            test = new PingPongTest3( args );
            test.run();

        } catch( Throwable ex ) {
            log.error( ex );
            ++errorCount;
        }
        if( test != null ) {
            test.cleanup();
        }

        if( errorCount == 0 ) {
            log.info( "PASS" );
        } else {
            log.error( "FAIL (" + errorCount + " errors)" );
        }

        System.exit( errorCount );
    }

    /**
     * Setup.
     *
     * @param args not used
     * @throws Exception any kind of exception
     */
    public PingPongTest3(
            String [] args )
        throws
            Exception
    {
        super( thisPackage( PingPongTest3.class, "Log.properties" ));

        log = Log.getLogInstance( getClass() );
    }

    /**
     * Cleanup.
     */
    @Override
    public void cleanup()
    {
        done = true;
        
        exec1.shutdown();
        exec2.shutdown();
    }

    // Our Logger
    private static Log log = Log.getLogInstance( PingPongTest3.class );

    /**
     * ThreadPool.
     */
    protected ScheduledExecutorService exec1 = createThreadPool( getClass().getName() + "-exec1", 2 );

    /**
     * ThreadPool.
     */
    protected ScheduledExecutorService exec2 = createThreadPool( getClass().getName() + "-exec2", 2 );
        
    /**
     * Set to true if the test is done, so listeners won't report an error.
     */
    protected boolean done = false;
    
    /**
     * Listener.
     */
    class MyListener
            implements
                MessageEndpointListener<String>
    {
        public MyListener(
                PingPongMessageEndpoint<String> end,
                String                          prefix,
                long                            delay )
        {
            theEndpoint = end;
            thePrefix   = prefix;
            theDelay    = delay;
        }

        /**
         * Called when an incoming message has arrived.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the received message
         */
        public void messageReceived(
                ReceivingMessageEndpoint<String> endpoint,
                String                           msg )
        {
            log.debug( this + " received message " + msg );
            
            Integer current = receivedMessages.get( msg );
            if( current == null ) {
                receivedMessages.put( msg, 1 );
            } else {
                receivedMessages.put( msg, current+1 );
            }
            
            // only sleep the first time a non-null message was received
            
            if( firstNonNullMessageReceived == null && msg != null ) {
                firstNonNullMessageReceived = msg;
                if( theDelay > 0L ) {
                    log.info( "Now sleeping for " + theDelay );
                    try {
                        Thread.sleep( theDelay );
                    } catch( Exception ex ) {
                        log.error( ex );
                    }
                    log.info( "Woken up" );
                }
            }
            
            theEndpoint.enqueueMessageForSend( thePrefix + msg );
        }

        /**
         * Called when an outgoing message has been sent.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the sent message
         */
        public void messageSent(
                SendingMessageEndpoint<String> endpoint,
                String                         msg )
        {
            log.debug( this + " sent message " + msg );
        }

        /**
         * Called when an outgoing message has enqueued for sending.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the enqueued message
         */
        public void messageEnqueued(
                SendingMessageEndpoint<String> endpoint,
                String                         msg )
        {
            log.debug( this + " enqueued message " + msg );
        }
    
        /**
         * Called when an outoing message failed to be sent.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the outgoing message
         */
        public void messageSendingFailed(
                SendingMessageEndpoint<String> endpoint,
                String                         msg )
        {
            reportError( "Message sending failed: " + msg );
        }

        /**
         * Called when the receiving endpoint threw the EndpointIsDeadException.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the status of the outgoing queue
         * @param t the error
         */
        public void disablingError(
                MessageEndpoint<String> endpoint,
                List<String>            msg,
                Throwable               t )
        {
            if( !done ) {
                reportError( "Receiving endpoint is dead: " + msg );
            }
        }

        public void clear()
        {
            firstNonNullMessageReceived = null;
            receivedMessages.clear();
        }
        
        @Override
        public String toString()
        {
            return "Listener (prefix: " + thePrefix + "): ";
        }

        PingPongMessageEndpoint<String> theEndpoint;
        String                          thePrefix;
        String                          firstNonNullMessageReceived;
        
        HashMap<String,Integer> receivedMessages = new HashMap<String,Integer>();
        
        long theDelay;
    }
}

