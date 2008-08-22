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
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.util.Factory;
import org.infogrid.util.FactoryException;

/**
 * Manufactures ProxyMessageEndpoints for a given ordered pair of local NetMeshBaseIdentifier
 * and remote NetMeshBaseIdentifier.
 */
public interface ProxyMessageEndpointFactory
        extends
            Factory<NetMeshBaseIdentifier,ProxyMessageEndpoint,NetMeshBaseIdentifier>
{
    /**
     * Restore a ProxyMessageEndpoint from storage.
     * 
     * @param partnerIdentifier the NetMeshBaseIdentifier of the NetMeshBase to communicate with
     * @param myIdentifier      the NetMeshBaseIdentifier of the NetMeshBase on whose behalf the restored ProxyMessageEndpoint will communicate
     * @param lastTokenSent     the last token sent by this ProxyMessageEndpoint prior to being saved to storage
     * @param lastTokenReceived the last token received by this ProxyMessageEndpoint prior to being saved to storage
     * @param lastMessagesSent  the last messages sent whose receipt had not been acknowledged yet at the time of being saved to storage
     * @param messagesToBeSent  the messages still to be sent at the time of being saved to storage
     * @return the restured ProxyMessageEndpoint
     * @throws FactoryException thrown if the ProxyMessageEndpoint could not be recreated
     */
    public ProxyMessageEndpoint restoreNetMessageEndpoint(
            NetMeshBaseIdentifier partnerIdentifier,
            NetMeshBaseIdentifier myIdentifier,
            long                  lastTokenSent,
            long                  lastTokenReceived,
            List<XprisoMessage>   lastMessagesSent,
            List<XprisoMessage>   messagesToBeSent )
        throws
            FactoryException;
}
