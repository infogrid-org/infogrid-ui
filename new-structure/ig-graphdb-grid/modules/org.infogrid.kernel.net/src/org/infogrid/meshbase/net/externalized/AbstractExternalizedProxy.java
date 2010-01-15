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

package org.infogrid.meshbase.net.externalized;

import java.util.ArrayList;
import java.util.List;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;

/**
 * Factors out functionality common to ExternalizedProxy implementations.
 */
public abstract class AbstractExternalizedProxy
        implements
            ExternalizedProxy
{
    /**
     * Constructor.
     */
    protected AbstractExternalizedProxy()
    {
        // no op
    }

    /**
     * Obtain the time at which the Proxy was created.
     *
     * @return the time, in System.currentMillis() format
     */
    public long getTimeCreated()
    {
        return theTimeCreated;
    }

    /**
     * Obtain the time at which the Proxy was last updated.
     *
     * @return the time, in System.currentMillis() format
     */
    public long getTimeUpdated()
    {
        return theTimeUpdated;
    }

    /**
     * Obtain the time at which the Proxy was read.
     *
     * @return the time, in System.currentMillis() format
     */
    public long getTimeRead()
    {
        return theTimeRead;
    }

    /**
     * Obtain the time at which the Proxy will expire.
     *
     * @return the time, in System.currentMillis() format
     */
    public long getTimeExpires()
    {
        return theTimeExpires;
    }

    /**
     * Obtain the NetMeshBaseIdentifier of the NetMeshBase to which this Proxy belongs.
     * 
     * @return the NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getNetworkIdentifier()
    {
        return theNetworkIdentifier;
    }

    /**
     * Obtain the NetMeshBaseIdentifier of the NetMeshBase to which this Proxy talks to.
     * 
     * @return the NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getNetworkIdentifierOfPartner()
    {
        return theNetworkIdentifierOfPartner;
    }

    /**
     * Obtain the CoherenceSpecification used by this Proxy.
     * 
     * @return the CoherenceSpecification
     */
    public CoherenceSpecification getCoherenceSpecification()
    {
        return theCoherenceSpecification;
    }

    /**
     * Obtain the token that was last sent by this Proxy.
     *
     * @return the token
     */
    public long getLastSentToken()
    {
        return theLastSentToken;
    }

    /**
     * Obtain the token that was last received by this Proxy.
     *
     * @return the token
     */
    public long getLastReceivedToken()
    {
        return theLastReceivedToken;
    }

    /**
     * Obtain the XprisoMessages still to be sent.
     *
     * @return the messages
     */
    public List<XprisoMessage> messagesToBeSent()
    {
        return theMessagesToSend;
    }

    /**
     * Obtain the XprisoMessages last sent.
     *
     * @return the messages
     */
    public List<XprisoMessage> messagesLastSent()
    {
        return theMessagesLastSent;
    }
    
    /**
     * The time when the Proxy was created.
     */
    protected long theTimeCreated = -1L;
    
    /**
     * The time when the Proxy was last updated.
     */
    protected long theTimeUpdated = -1L;
    
    /**
     * The time when the Proxy was last read.
     */
    protected long theTimeRead = -1L;
    
    /**
     * The time when the Proxy will expires.
     */
    protected long theTimeExpires = -1L;
    
    /**
     * The NetMeshBaseIdentifier of the local MeshBase.
     */
    protected NetMeshBaseIdentifier theNetworkIdentifier;
    
    /**
     * The NetMeshBaseIdentifier of the partner MeshBase.
     */
    protected NetMeshBaseIdentifier theNetworkIdentifierOfPartner;
    
    /**
     * The CoherenceSpecification used by the Proxy.
     */
    protected CoherenceSpecification theCoherenceSpecification;

    /**
     * The token last sent by this Proxy.
     */
    protected long theLastSentToken = -1L;
    
    /**
     * The token last received by this Proxy.
     */
    protected long theLastReceivedToken = -1L;
    
    /**
     * THe Messages still to be sent.
     */
    protected List<XprisoMessage> theMessagesToSend = new ArrayList<XprisoMessage>();
    
    /**
     * The Messages last sent.
     */
    protected List<XprisoMessage> theMessagesLastSent = new ArrayList<XprisoMessage>();
}
