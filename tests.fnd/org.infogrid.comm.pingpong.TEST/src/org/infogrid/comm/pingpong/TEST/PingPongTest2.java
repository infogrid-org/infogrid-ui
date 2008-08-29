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
 * Tests the passing of messages between the endpoints.
 */
public class PingPongTest2
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
        MPingPongMessageEndpoint<String> ep1 = MPingPongMessageEndpoint.create( "ep1", 1000L, 500L, 10000L, 0.f, exec );
        MPingPongMessageEndpoint<String> ep2 = MPingPongMessageEndpoint.create( "ep2", 1000L, 500L, 10000L, 0.f, exec );
        
        MyListener l1 = new MyListener( ep1, "one " );
        MyListener l2 = new MyListener( ep2, "two " );
        ep1.addDirectMessageEndpointListener( l1 );
        ep2.addDirectMessageEndpointListener( l2 );

        log.info( "Starting to ping-pong" );
        log.debug( "Note that the events seem to be a bit out of order as we only print the event after it was successfully sent (and received)" );

        ep1.setPartnerAndInitiateCommunications( ep2 );
        
        ep2.enqueueMessageForSend( "seed" );
        
        Thread.sleep( 10000L ); // four ping and five pongs

        ep1.stopCommunicating();
        ep2.stopCommunicating();
        
        String lastMessage1 = l1.lastMessageReceived;
        String lastMessage2 = l2.lastMessageReceived;
        
        checkEquals( lastMessage1, "two one two one two one two one seed", "wrong last message" );
        checkEquals( lastMessage2, "one two one two one two one seed", "wrong last message" );
    }

    /**
      * Main program.
      *
      * @param args command-line arguments
      */
    public static void main(
             String [] args )
    {
        PingPongTest2 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }
            test = new PingPongTest2( args );
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
    public PingPongTest2(
            String [] args )
        throws
            Exception
    {
        super( thisPackage( PingPongTest2.class, "Log.properties" ));

        log = Log.getLogInstance( getClass() );
    }

    /**
     * Cleanup.
     */
    @Override
    public void cleanup()
    {
        done = true;
        
        exec.shutdown();
    }

    // Our Logger
    private static Log log = Log.getLogInstance( PingPongTest2.class );

    /**
     * Our ThreadPool
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );
        
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
                String                          prefix )
        {
            theEndpoint = end;
            thePrefix   = prefix;
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
            lastMessageReceived = msg;
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
            received = 0;
            sent     = 0;
        }

        PingPongMessageEndpoint<String> theEndpoint;
        String                          thePrefix;
        String                          lastMessageReceived;
        
        int received;
        int sent;
    }
}
