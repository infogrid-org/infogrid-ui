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
import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.comm.SendingMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpointListener;
import org.infogrid.comm.pingpong.m.MPingPongMessageEndpoint;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.logging.Log;

/**
 * Tests the exchange of the token back and forth between the endpoints.
 */
public class PingPongTest1
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
        
        MyListener l1 = new MyListener( "1", "ping" );
        MyListener l2 = new MyListener( "2", "pong" );
        ep1.addDirectMessageEndpointListener( l1 );
        ep2.addDirectMessageEndpointListener( l2 );

        ep1.setPartnerAndInitiateCommunications( ep2 );

        log.info( "Starting to ping-pong" );
        log.debug( "Note that the events seem to be a bit out of order as we only print the event after it was successfully sent (and received)" );

        Thread.sleep( 9500L );

        ep1.stopCommunicating();
        ep2.stopCommunicating();
        
        checkEquals( l1.received, 5, "Wrong number of received tokens in listener 1" );
        checkEquals( l1.sent,     5, "Wrong number of sent tokens in listener 1" );
        checkEquals( l2.received, 5, "Wrong number of received tokens in listener 2" );
        checkEquals( l2.sent,     5, "Wrong number of sent tokens in listener 2" );
    }

    /**
      * Main program.
      *
      * @param args command-line arguments
      */
    public static void main(
             String [] args )
    {
        PingPongTest1 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }
            test = new PingPongTest1( args );
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
    public PingPongTest1(
            String [] args )
        throws
            Exception
    {
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
    private static Log log = Log.getLogInstance( PingPongTest1.class );

    /**
     * The ThreadPool to use.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

    /**
     * Set to true if the test is done, so listeners won't report an error.
     */
    protected boolean done = false;
    
    /**
     * This listener memorizes incoming events.
     */
    class MyListener
            implements
                PingPongMessageEndpointListener<String>
    {
        /**
         * Constructor.
         * 
         * @param name 
         * @param p
         */
        public MyListener(
                String name,
                String p )
        {
            theName = name;
            theP    = p;
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
            log.debug( this + " failed to send message " + msg );
        }

        /**
         * Called when the token has been received.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param token the received token
         */
        public void tokenReceived(
                PingPongMessageEndpoint<String> endpoint,
                long                            token )
        {
            log.debug( "Listener " + theName + " received token: " + token );
            log.info( theP );
            ++received;
        }

        /**
         * Called when the token has been sent.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param token the sent token
         */
        public void tokenSent(
                PingPongMessageEndpoint<String> endpoint,
                long                            token )
        {
            log.debug( "Listener " + theName + " sent token: " + token );
            ++sent;
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

        String theName;
        String theP;
        int received;
        int sent;
        
    }
}
