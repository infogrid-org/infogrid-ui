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

import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import java.util.List;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpoint;

/**
 * This implementation of ExternalizedProxy is fully initialized in the
 * factory method.
 */
public class SimpleExternalizedProxy
        extends
            AbstractExternalizedProxy
{
    /**
     * Factory method.
     *
     * @param proxy the Proxy to be externalized
     * @return the SimpleExternalizedProxy
     */
    public static SimpleExternalizedProxy create(
            Proxy proxy )
    {
        long lastSentToken;
        long lastReceivedToken;
        List<XprisoMessage> messagesToSend;
        List<XprisoMessage> messagesLastSent;

        ProxyMessageEndpoint ep = proxy.getMessageEndpoint();
        if( ep != null ) {
            lastSentToken     = ep.getLastSentToken();
            lastReceivedToken = ep.getLastReceivedToken();
            messagesToSend    = ep.messagesToBeSent();
            messagesLastSent  = ep.messagesLastSent();
        } else {
            lastSentToken     = -1L;
            lastReceivedToken = -1L;
            messagesToSend    = null;
            messagesLastSent  = null;
        }

        return new SimpleExternalizedProxy(
                proxy.getTimeCreated(),
                proxy.getTimeUpdated(),
                proxy.getTimeRead(),
                proxy.getTimeExpires(),
                lastSentToken,
                lastReceivedToken,
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier(),
                proxy.getCoherenceSpecification(),
                messagesToSend,
                messagesLastSent );
    }

    /**
     * Constructor.
     * 
     * @param timeCreated the time at which this Proxy was created
     * @param timeUpdated the time at which this Proxy was last updated
     * @param timeRead the time at which this Proxy was last read
     * @param timeExpires the time at which this Proxy will expire
     * @param lastSentToken the token sent last
     * @param lastReceivedToken the token received last
     * @param networkIdentifier the NetMeshBaseIdentifier of the NetMeshBase to which this Proxy belongs
     * @param networkIdentifierOfPartner the NetMeshBaseIdentifier of the NetMeshBase with which this Proxy communicates
     * @param coherenceSpecification the CoherenceSpecification used by this Proxy
     * @param messagesToSend the messages to be sent
     * @param messagesLastSent the message sent most recently
     */
    protected SimpleExternalizedProxy(
            long                    timeCreated,
            long                    timeUpdated,
            long                    timeRead,
            long                    timeExpires,
            long                    lastSentToken,
            long                    lastReceivedToken,
            NetMeshBaseIdentifier   networkIdentifier,
            NetMeshBaseIdentifier   networkIdentifierOfPartner,
            CoherenceSpecification  coherenceSpecification,
            List<XprisoMessage>     messagesToSend,
            List<XprisoMessage>     messagesLastSent )
    {
        theTimeCreated = timeCreated;
        theTimeUpdated = timeUpdated;
        theTimeRead    = timeRead;
        theTimeExpires = timeExpires;
        
        theLastSentToken     = lastSentToken;
        theLastReceivedToken = lastReceivedToken;
        
        theNetworkIdentifier          = networkIdentifier;
        theNetworkIdentifierOfPartner = networkIdentifierOfPartner;
        theCoherenceSpecification     = coherenceSpecification;
        
        theMessagesToSend   = messagesToSend;
        theMessagesLastSent = messagesLastSent;
    }
}
