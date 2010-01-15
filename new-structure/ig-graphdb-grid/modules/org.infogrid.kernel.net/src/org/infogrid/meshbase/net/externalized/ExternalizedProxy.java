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
 * Representation of Proxy that can be easily serialized and deserialized.
 */
public interface ExternalizedProxy
{
    /**
     * Obtain the time at which the Proxy was created.
     *
     * @return the time, in System.currentMillis() format
     */
    public long getTimeCreated();

    /**
     * Obtain the time at which the Proxy was last updated.
     *
     * @return the time, in System.currentMillis() format
     */
    public long getTimeUpdated();

    /**
     * Obtain the time at which the Proxy was read.
     *
     * @return the time, in System.currentMillis() format
     */
    public long getTimeRead();

    /**
     * Obtain the time at which the Proxy will expire.
     *
     * @return the time, in System.currentMillis() format
     */
    public long getTimeExpires();

    /**
     * Obtain the NetMeshBaseIdentifier of the NetMeshBase to which this Proxy belongs.
     * 
     * @return the NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getNetworkIdentifier();

    /**
     * Obtain the NetMeshBaseIdentifier of the NetMeshBase to which this Proxy talks to.
     * 
     * @return the NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getNetworkIdentifierOfPartner();

    /**
     * Obtain the CoherenceSpecification used by this Proxy.
     * 
     * @return the CoherenceSpecification
     */
    public CoherenceSpecification getCoherenceSpecification();

    /**
     * Obtain the token that was last sent by this Proxy.
     *
     * @return the token
     */
    public long getLastSentToken();

    /**
     * Obtain the token that was last received by this Proxy.
     *
     * @return the token
     */
    public long getLastReceivedToken();

    /**
     * Obtain the XprisoMessages still to be sent.
     *
     * @return the Iterator
     */
    public List<XprisoMessage> messagesToBeSent();

    /**
     * Obtain the XprisoMessages last sent.
     *
     * @return the Iterator
     */
    public List<XprisoMessage> messagesLastSent();
}
