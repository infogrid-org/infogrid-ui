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

package org.infogrid.meshbase.net.proxy;

import java.util.List;
import org.infogrid.comm.BidirectionalMessageEndpoint;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;

/**
 * Captures the requirements for BidirectionalMessageEndpoints that can be used for
 * Proxy communications.
 */
public interface ProxyMessageEndpoint
        extends
            BidirectionalMessageEndpoint<XprisoMessage>
{
    /**
     * Determine the NetMeshBaseIdentifier of the partner MeshBase.
     * 
     * @return the NetMeshBaseIdentifier of the partner MeshBase
     */
    public abstract NetMeshBaseIdentifier getNetworkIdentifierOfPartner();

    /**
     * Obtain the token that was last sent.
     *
     * @return the token
     */
    public abstract long getLastSentToken();

    /**
     * Obtain the token that was last received.
     *
     * @return the token
     */
    public abstract long getLastReceivedToken();
    
    /**
     * Obtain the XprisoMessages still to be sent.
     *
     * @return the messages
     */
    public abstract List<XprisoMessage> messagesToBeSent();
    
    /**
     * Obtain the XprisoMessages that were sent most recently.
     *
     * @return the messages
     */
    public abstract List<XprisoMessage> messagesLastSent();

    /**
     * Initiate communications with the partner MeshBase. If communications
     * were initiated earlier and are ongoing, this call does nothing.
     */
    public abstract void startCommunicating();

    /**
     * Stop communications with the partner MeshBase.
     */
    public abstract void stopCommunicating();

    /**
     * Attempt to send the outgoing messages, but stop receiving incoming messages.
     */
    public abstract void gracefulDie();
}
