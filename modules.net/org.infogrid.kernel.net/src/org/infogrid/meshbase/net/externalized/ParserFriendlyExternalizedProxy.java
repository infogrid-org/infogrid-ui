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

import java.util.List;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;

/**
 * A temporary buffer for a to-be-deserialized Proxy.
 */
public class ParserFriendlyExternalizedProxy
        extends
            AbstractExternalizedProxy
{
    /**
     * Set the time at which this Proxy was created.
     *
     * @param newValue the new value
     */
    public void setTimeCreated(
            long newValue )
    {
        theTimeCreated = newValue;
    }

    /**
     * Set the time at which this Proxy was last updated.
     *
     * @param newValue the new value
     */
    public void setTimeUpdated(
            long newValue )
    {
        theTimeUpdated = newValue;
    }

    /**
     * Set the time at which this Proxy was last read.
     *
     * @param newValue the new value
     */
    public void setTimeRead(
            long newValue )
    {
        theTimeRead = newValue;
    }

    /**
     * Set the time at which this Proxy will expire.
     *
     * @param newValue the new value
     */
    public void setTimeExpires(
            long newValue )
    {
        theTimeExpires = newValue;
    }

    /**
     * Set the NetMeshBaseIdentifier of the NetMeshBase to which this Proxy belongs.
     * 
     * @param newValue the new value
     */
    public void setNetworkIdentifier(
            NetMeshBaseIdentifier newValue )
    {
        theNetworkIdentifier = newValue;
    }

    /**
     * Set the NetMeshBaseIdentifier of the NetMeshBase with which this Proxy communicates.
     * 
     * @param newValue the new value
     */
    public void setNetworkIdentifierOfPartner(
            NetMeshBaseIdentifier newValue )
    {
        theNetworkIdentifierOfPartner = newValue;
    }

    /**
     * Set the CoherenceSpecification used by this Proxy.
     * 
     * @param newValue the new value
     */
    public void setCoherenceSpecification(
            CoherenceSpecification newValue )
    {
        theCoherenceSpecification = newValue;
    }

    /**
     * Set the last sent token.
     *
     * @param newValue the new value
     */
    public void setLastSentToken(
            long newValue )
    {
        theLastSentToken = newValue;
    }

    /**
     * Set the last received token.
     *
     * @param newValue the new value
     */
    public void setLastReceivedToken(
            long newValue )
    {
        theLastReceivedToken = newValue;
    }
    
    /**
     * Set the Messages to be sent.
     *
     * @param newValue the new value
     */
    public void setMessagesToSend(
            List<XprisoMessage> newValue )
    {
        theMessagesToSend = newValue;
    }
    
    /**
     * Set the last-sent Messages.
     *
     * @param newValue the new value
     */
    public void setMessagesLastSent(
            List<XprisoMessage> newValue )
    {
        theMessagesLastSent = newValue;
    }
}
