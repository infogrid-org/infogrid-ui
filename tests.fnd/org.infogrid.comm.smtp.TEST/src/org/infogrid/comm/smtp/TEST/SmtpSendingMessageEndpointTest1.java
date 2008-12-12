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

package org.infogrid.comm.smtp.TEST;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.comm.smtp.SmtpSendingMessageEndpoint;
import org.infogrid.comm.smtp.SimpleSmtpSendableMessage;
import org.infogrid.util.logging.Log;
import org.infogrid.testharness.AbstractTest;

/**
 * Tests the exchange of the token back and forth between the endpoints.
 */
public class SmtpSendingMessageEndpointTest1
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
        String MAILHOST = "smtp"; // WARNING: This will only work if you have a mailhost called smtp (in the default domain)
                
        SimpleSmtpSendableMessage [] testMessages = {
                SimpleSmtpSendableMessage.create(
                        "testuser1@infogrid.org",
                        "testuser2@infogrid.org",
                        "Test Message 1 Subject",
                        "first line\n  second line\n\tthird line" )
        };
        
        SmtpSendingMessageEndpoint<SimpleSmtpSendableMessage> endpoint
                = SmtpSendingMessageEndpoint.create( exec, MAILHOST );
        
        for( int i=0 ; i<testMessages.length ; ++i ) {
            log.info( "About to send message " + i );
            
            endpoint.enqueueMessageForSend( testMessages[i] );
        }
        
        Thread.sleep( 5000L );
        
        List<SimpleSmtpSendableMessage> leftover = endpoint.messagesToBeSent();
        checkEquals( leftover.size(), 0, "still messages left to send" );
    }

    /**
      * Main program.
      *
      * @param args command-line arguments
      */
    public static void main(
             String [] args )
    {
        SmtpSendingMessageEndpointTest1 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }
            test = new SmtpSendingMessageEndpointTest1( args );
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
    public SmtpSendingMessageEndpointTest1(
            String [] args )
        throws
            Exception
    {
        super();

        log = Log.getLogInstance( getClass() );
    }

    // Our Logger
    private static Log log;

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );
}
