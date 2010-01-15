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

package org.infogrid.meshbase.net.proxy.m;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.comm.MessageSendException;
import org.infogrid.comm.pingpong.m.MPingPongMessageEndpoint;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpoint;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.util.FactoryException;
import org.infogrid.util.NameServer;
import org.infogrid.util.logging.Log;

/**
 * Subclass of MPingPongMessageEndpoint to be used for Proxy communication that
 * does not persist its own data and communicates via the ping-pong protocol.
 */
public class MPingPongNetMessageEndpoint
        extends
            MPingPongMessageEndpoint<XprisoMessage>
        implements
            ProxyMessageEndpoint
{
    private static final Log log = Log.getLogInstance( MPingPongNetMessageEndpoint.class ); // our own, private logger

    /**
     * Factory method.
     * 
     * @param name the name of the PingPongMessageEndpoint (for debugging only)
     * @param partnerIdentifier identifier of the partner NetMeshBase
     * @param myIdentifier identifier of the NetMeshBase on whose behalf this endpoint communicates
     * @param nameServer the NameServer to use to to resolve identifiers
     * @param deltaRespondNoMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if no message is in the queue
     * @param deltaRespondWithMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if a message is in the queue
     * @param deltaResend  the number of milliseconds until this PingPongMessageEndpoint resends the token if sending the token failed
     * @param deltaRecover the number of milliseconds until this PingPongMessageEndpoint decides that the token
     *                     was not received by the partner PingPongMessageEndpoint, and resends
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule timed tasks
     * @return the created MPingPongNetMessageEndpoint
     */
    public static MPingPongNetMessageEndpoint create(
            String                                                  name,
            NetMeshBaseIdentifier                                   partnerIdentifier,
            NetMeshBaseIdentifier                                   myIdentifier,
            NameServer<NetMeshBaseIdentifier,? extends NetMeshBase> nameServer,
            long                                                    deltaRespondNoMessage,
            long                                                    deltaRespondWithMessage,
            long                                                    deltaResend,
            long                                                    deltaRecover,
            double                                                  randomVariation,
            ScheduledExecutorService                                exec )
    {
        MPingPongNetMessageEndpoint ret = new MPingPongNetMessageEndpoint(
                name,
                partnerIdentifier,
                myIdentifier,
                nameServer,
                deltaRespondNoMessage,
                deltaRespondWithMessage,
                deltaResend,
                deltaRecover,
                randomVariation,
                exec,
                -1,
                -1,
                null,
                new ArrayList<XprisoMessage>() );
    
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( MPingPongNetMessageEndpoint.class, "create" );
        }
        return ret;
    }
    
    /**
     * Factory method.
     * 
     * @param name the name of the PingPongMessageEndpoint (for debugging only)
     * @param partnerIdentifier identifier of the partner NetMeshBase
     * @param myIdentifier identifier of the NetMeshBase on whose behalf this endpoint communicates
     * @param nameServer the NameServer to use to to resolve identifiers
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
     * @return the created MPingPongNetMessageEndpoint
     */
    public static MPingPongNetMessageEndpoint restore(
            String                                                  name,
            NetMeshBaseIdentifier                                   partnerIdentifier,
            NetMeshBaseIdentifier                                   myIdentifier,
            NameServer<NetMeshBaseIdentifier,? extends NetMeshBase> nameServer,
            long                                                    deltaRespondNoMessage,
            long                                                    deltaRespondWithMessage,
            long                                                    deltaResend,
            long                                                    deltaRecover,
            double                                                  randomVariation,
            ScheduledExecutorService                                exec,
            long                                                    lastSentToken,
            long                                                    lastReceivedToken,
            List<XprisoMessage>                                     messagesSentLast,
            List<XprisoMessage>                                     messagesToBeSent )
    {
        MPingPongNetMessageEndpoint ret = new MPingPongNetMessageEndpoint(
                name,
                partnerIdentifier,
                myIdentifier,
                nameServer,
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
            log.traceMethodCallEntry( MPingPongNetMessageEndpoint.class, "create" );
        }
        return ret;
    }
    
    /**
     * Constructor.
     *
     * @param name the name of the PingPongMessageEndpoint (for debugging only)
     * @param partnerIdentifier identifier of the partner NetMeshBase
     * @param myIdentifier identifier of the NetMeshBase on whose behalf this endpoint communicates
     * @param nameServer the NameServer to use to to resolve identifiers
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
    protected MPingPongNetMessageEndpoint(
            String                                                  name,
            NetMeshBaseIdentifier                                   partnerIdentifier,
            NetMeshBaseIdentifier                                   myIdentifier,
            NameServer<NetMeshBaseIdentifier,? extends NetMeshBase> nameServer,
            long                                                    deltaRespondNoMessage,
            long                                                    deltaRespondWithMessage,
            long                                                    deltaResend,
            long                                                    deltaRecover,
            double                                                  randomVariation,
            ScheduledExecutorService                                exec,
            long                                                    lastSentToken,
            long                                                    lastReceivedToken,
            List<XprisoMessage>                                     messagesSentLast,
            List<XprisoMessage>                                     messagesToBeSent )
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
        
        if( partnerIdentifier.equals( myIdentifier )) {
            throw new IllegalArgumentException( "Cannot talk to myself" );
        }
        
        thePartnerIdentifier = partnerIdentifier;
        theMyIdentifier      = myIdentifier;
        theNameServerRef     = new WeakReference<NameServer<NetMeshBaseIdentifier,? extends NetMeshBase>>( nameServer );
    }

    /**
     * Determine the NetMeshBaseIdentifier of the partner MeshBase.
     * 
     * @return the NetMeshBaseIdentifier of the partner MeshBase
     */
    public NetMeshBaseIdentifier getNetworkIdentifierOfPartner()
    {
        return thePartnerIdentifier;
    }

    /**
     * Do the message send.
     *
     * @param token the token of the message
     * @param content the content to send.
     * @throws MessageSendException thrown if the message could not be sent
     */
    @Override
    protected void sendMessage(
            long                token,
            List<XprisoMessage> content )
        throws
            MessageSendException
    {
        if( thePartner == null ) {
            NameServer<NetMeshBaseIdentifier, ? extends NetMeshBase> nameServer = theNameServerRef.get();
            if( nameServer == null ) {
                // has been garbage collected, we are done
                return;
            }
            try {
                NetMeshBase partnerBase = nameServer.get( thePartnerIdentifier );
                if( partnerBase == null ) {
                    throw new MessageSendException( content, "Could not find NetMeshBase with identifier " + thePartnerIdentifier );
                }

                Proxy partnerProxy = partnerBase.obtainProxyFor( theMyIdentifier, null ); // FIXME? What is the right CoherenceSpecification here?
                if( partnerProxy == null ) {
                    throw new MessageSendException( content, "Could not obtain proxy for " + theMyIdentifier + " from NetMeshBase with identifier " + thePartnerIdentifier );
                }

                thePartner = (MPingPongNetMessageEndpoint) partnerProxy.getMessageEndpoint();

            } catch( FactoryException ex ) {
                throw new MessageSendException( content, ex );
            }
        }
        super.sendMessage( token, content );
    }

    /**
     * Convert to String, for debugging only.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append( super.toString() );
        buf.append( "{ " );
        buf.append( theMyIdentifier.toExternalForm() );
        buf.append( " -> " );
        buf.append( thePartnerIdentifier.toExternalForm() );
        buf.append( " {" );
        return buf.toString();
    }
    
    /**
     * Identifier of the local MeshBase.
     */
    protected NetMeshBaseIdentifier theMyIdentifier;

    /**
     * Identifier of the partner MeshBase.
     */
    protected NetMeshBaseIdentifier thePartnerIdentifier;

    /**
     * The NameServer to use to find the partner NetMeshBase. This is a WeakReference so garbage collection
     * is not impeded.
     */
    protected WeakReference<NameServer<NetMeshBaseIdentifier, ? extends NetMeshBase>> theNameServerRef;
}
