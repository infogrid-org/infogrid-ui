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

import java.util.concurrent.ScheduledExecutorService;
import java.util.List;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseNameServer;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpoint;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.ResourceHelper;

/**
 * Manufactures MessageEndpoints that do not persist their data themselves, and that
 * communicate via the ping-pon protocol.
 */
public class MPingPongNetMessageEndpointFactory
        extends
            AbstractFactory<NetMeshBaseIdentifier,ProxyMessageEndpoint,NetMeshBaseIdentifier>
        implements
            ProxyMessageEndpointFactory
{
    /**
     * Factory method.
     *
     * @param exec the ScheduledExecutorService to schedule communication-related events
     * @return the created MPingPongNetMessageEndpointFactory
     */
    public static MPingPongNetMessageEndpointFactory create(
            ScheduledExecutorService exec )
    {
        long   deltaRespondNoMessage   = theResourceHelper.getResourceLongOrDefault(   "DeltaRespondNoMessage",   60L * 1000L ); // 1 minute
        long   deltaRespondWithMessage = theResourceHelper.getResourceLongOrDefault(   "DeltaRespondWithMessage", 10L ); // quickly but still deterministic
        long   deltaResend             = theResourceHelper.getResourceLongOrDefault(   "DeltaResend",             500L );
        long   deltaRecover            = theResourceHelper.getResourceLongOrDefault(   "DeltaRecover",            deltaRespondNoMessage * 5L ); // 5 times longer
        double randomVariation         = theResourceHelper.getResourceDoubleOrDefault( "RandomVariation",         0.02 ); // 2%

        return new MPingPongNetMessageEndpointFactory(
                deltaRespondNoMessage,
                deltaRespondWithMessage,
                deltaResend,
                deltaRecover,
                randomVariation,
                exec );
    }

    /**
     * Factory method.
     *
     * @param deltaRespondNoMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if no message is in the queue
     * @param deltaRespondWithMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if a message is in the queue
     * @param deltaResend  the number of milliseconds until this PingPongMessageEndpoint resends the token if sending the token failed
     * @param deltaRecover the number of milliseconds until this PingPongMessageEndpoint decides that the token
     *                     was not received by the partner PingPongMessageEndpoint, and resends
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule communication-related events
     * @return the created MPingPongNetMessageEndpointFactory
     */
    public static MPingPongNetMessageEndpointFactory create(
            long                     deltaRespondNoMessage,
            long                     deltaRespondWithMessage,
            long                     deltaResend,
            long                     deltaRecover,
            double                   randomVariation,
            ScheduledExecutorService exec )
    {
        return new MPingPongNetMessageEndpointFactory(
                deltaRespondNoMessage,
                deltaRespondWithMessage,
                deltaResend,
                deltaRecover,
                randomVariation,
                exec );
    }

    /**
     * Constructor.
     *
     * @param deltaRespondNoMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if no message is in the queue
     * @param deltaRespondWithMessage the number of milliseconds until this PingPongMessageEndpoint returns the token if a message is in the queue
     * @param deltaResend  the number of milliseconds until this PingPongMessageEndpoint resends the token if sending the token failed
     * @param deltaRecover the number of milliseconds until this PingPongMessageEndpoint decides that the token
     *                     was not received by the partner PingPongMessageEndpoint, and resends
     * @param randomVariation the random component to add to the various times
     * @param exec the ScheduledExecutorService to schedule communication-related events
     */
    protected MPingPongNetMessageEndpointFactory(
            long                     deltaRespondNoMessage,
            long                     deltaRespondWithMessage,
            long                     deltaResend,
            long                     deltaRecover,
            double                   randomVariation,
            ScheduledExecutorService exec )
    {
        theDeltaRespondNoMessage   = deltaRespondNoMessage;
        theDeltaRespondWithMessage = deltaRespondWithMessage;
        theDeltaResend             = deltaResend;
        theDeltaRecover            = deltaRecover;
        theRandomVariation         = randomVariation;

        theExecService = exec;
    }

    /**
     * Set the NameServer to use. This is a separate method and not in the constructor because
     * there are situations where the NameServer is not available at the time of construction
     * of an instance.
     *
     * @param nameServer the NameServer
     */
    public void setNameServer(
            NetMeshBaseNameServer<NetMeshBaseIdentifier,NetMeshBase> nameServer )
    {
        theNameServer = nameServer;
    }

    /**
     * Factory method.
     *
     * @param partnerIdentifier the key information required for object creation
     * @param myIdentifier any argument-style information required for object creation
     * @return the created object
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public MPingPongNetMessageEndpoint obtainFor(
            NetMeshBaseIdentifier partnerIdentifier,
            NetMeshBaseIdentifier myIdentifier )
        throws
            FactoryException
    {
        MPingPongNetMessageEndpoint ret = MPingPongNetMessageEndpoint.create(
                "Endpoint " + myIdentifier.toExternalForm() + " -> " + partnerIdentifier.toExternalForm(),
                partnerIdentifier,
                myIdentifier,
                theNameServer,
                theDeltaRespondNoMessage,
                theDeltaRespondWithMessage,
                theDeltaResend,
                theDeltaRecover,
                theRandomVariation,
                theExecService );

        return ret;
    }
    
    /**
     * Restore a ProxyMessageEndpoint from storage.
     * 
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public ProxyMessageEndpoint restoreNetMessageEndpoint(
            NetMeshBaseIdentifier partnerIdentifier,
            NetMeshBaseIdentifier myIdentifier,
            long                  lastTokenSent,
            long                  lastTokenReceived,
            List<XprisoMessage>   lastMessagesSent,
            List<XprisoMessage>   messagesToBeSent )
        throws
            FactoryException
    {
        MPingPongNetMessageEndpoint ret = MPingPongNetMessageEndpoint.restore(
                "Endpoint " + myIdentifier.toExternalForm() + " -> " + partnerIdentifier.toExternalForm(),
                partnerIdentifier,
                myIdentifier,
                theNameServer,
                theDeltaRespondNoMessage,
                theDeltaRespondWithMessage,
                theDeltaResend,
                theDeltaRecover,
                theRandomVariation,
                theExecService,
                lastTokenSent,
                lastTokenReceived,
                lastMessagesSent,
                messagesToBeSent );

        return ret;
    }
    
    /**
     * The ScheduledExecutorService to use.
     */
    protected ScheduledExecutorService theExecService;

    /**
     * The NameServer to find Proxies to communicate with.
     */
    protected NetMeshBaseNameServer<NetMeshBaseIdentifier,NetMeshBase> theNameServer;

    /**
     * Our ResourceHelper.
     */
    protected static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( MPingPongNetMessageEndpointFactory.class );
    
    /**
     * Milliseconds from ping to pong if no message is in the queue.
     */
    protected long theDeltaRespondNoMessage;

    /**
     * Milliseconds from ping to pong if a message is in the queue.
     */
    protected long theDeltaRespondWithMessage;

    /**
     * Millisecond until we attempt to resend failed messages.
     */
    protected long theDeltaResend;
    
    /**
     * Millisecond until we attempt to recover.
     */
    protected long theDeltaRecover;
    
    /**
     * Random variation, as percentage, of the respond and recover times.
     */
    protected double theRandomVariation;
}
