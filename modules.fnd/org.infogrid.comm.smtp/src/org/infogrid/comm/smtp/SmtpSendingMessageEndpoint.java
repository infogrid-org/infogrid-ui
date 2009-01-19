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

package org.infogrid.comm.smtp;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.comm.AbstractFireAndForgetSendingMessageEndpoint;
import org.infogrid.util.ResourceHelper;
import sun.net.smtp.SmtpClient;

/**
 * A message endpoint for sending messages via SMTP.
 * 
 * @param <T> the message type
 */
public class SmtpSendingMessageEndpoint<T extends SmtpSendableMessage>
        extends
            AbstractFireAndForgetSendingMessageEndpoint<T>
{
    /**
     * Factory method.
     *
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @return the created SmtpSendingMessageEndpoint
     * @param <T> the message type
     */
    public static <T extends SmtpSendableMessage> SmtpSendingMessageEndpoint<T> create(
            ScheduledExecutorService exec )
    {
        String name            = null;
        double randomVariation = theResourceHelper.getResourceDoubleOrDefault( "RandomVariation", 0.02 ); // 2%
        String mailHost        = theResourceHelper.getResourceStringOrNull( "MailHost" );
        
        if( mailHost == null ) {
            throw new IllegalArgumentException( "No mailhost specified in " + theResourceHelper );
        }

        List<T> messagesToBeSent = new ArrayList<T>();
        
        SmtpSendingMessageEndpoint<T> ret = new SmtpSendingMessageEndpoint<T>(
                name,
                randomVariation,
                exec,
                messagesToBeSent,
                mailHost );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @return the created SmtpSendingMessageEndpoint
     * @param mailHost host that runs the SMTP server
     * @param <T> the message type
     */
    public static <T extends SmtpSendableMessage> SmtpSendingMessageEndpoint<T> create(
            ScheduledExecutorService exec,
            String                   mailHost )
    {
        String name            = null;
        double randomVariation = theResourceHelper.getResourceDoubleOrDefault( "RandomVariation", 0.02 ); // 2%
        
        if( mailHost == null ) {
            throw new IllegalArgumentException( "No mailhost specified in " + theResourceHelper );
        }

        List<T> messagesToBeSent = new ArrayList<T>();
        
        SmtpSendingMessageEndpoint<T> ret = new SmtpSendingMessageEndpoint<T>(
                name,
                randomVariation,
                exec,
                messagesToBeSent,
                mailHost );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param name the name of the MessageEndpoint (for debugging only)
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @param messagesToBeSent outgoing message queue (may or may not be empty)
     * @param mailHost host that runs the SMTP server
     * @return the created SmtpSendingMessageEndpoint
     * @param <T> the message type
     */
    public static <T extends SmtpSendableMessage> SmtpSendingMessageEndpoint<T> create(
            String                   name,
            double                   randomVariation,
            ScheduledExecutorService exec,
            List<T>                  messagesToBeSent,
            String                   mailHost )
    {
        SmtpSendingMessageEndpoint<T> ret = new SmtpSendingMessageEndpoint<T>(
                name,
                randomVariation,
                exec,
                messagesToBeSent,
                mailHost );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param name the name of the MessageEndpoint (for debugging only)
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @param messagesToBeSent outgoing message queue (may or may not be empty)
     * @param mailHost host that runs the SMTP server
     */
    protected SmtpSendingMessageEndpoint(
            String                   name,
            double                   randomVariation,
            ScheduledExecutorService exec,
            List<T>                  messagesToBeSent,
            String                   mailHost )
    {
        super( name, randomVariation, exec, messagesToBeSent );
        
        theMailHost = mailHost;
    }

    /**
     * Attempt to send one message.
     * 
     * @param msg the Message to send.
     * @throws IOException the message send failed
     */
    protected void attemptSend(
            T msg )
        throws
            IOException
    {
        SmtpClient smtp = new SmtpClient( theMailHost );

        if( msg.getSenderString() != null ) {
            smtp.from( msg.getSenderString() );
        }
        if( msg.getReceiverString() != null ) {
            smtp.to( msg.getReceiverString() );
        }

        PrintStream stream = smtp.startMessage();

        if( msg.getReceiverString() != null ) {
            stream.println( "To: "      + msg.getReceiverString() );
        }
        if( msg.getSenderString() != null ) {
            stream.println( "From: "    + msg.getSenderString() );
        }
        if( msg.getSubject() != null ) {
            stream.println( "Subject: " + msg.getSubject() );
        }
        // stream.println( "Reply-To: " + $from );
        // stream.println( "X-org.netmesh.lid.lid: " + identityUrlString );
        // if( target != null ) {
        //     stream.println( "X-org.netmesh.lid.target: " + target );
        // }
        stream.println();

        if( msg.getPayload() != null ) {
            stream.print( msg.getPayload() );
        }
        stream.flush();
        smtp.closeServer();
    }
    
    /**
     * IP of the mail host on which SMTP is running.
     */
    protected String theMailHost;
    
    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( SmtpSendingMessageEndpoint.class );
}
