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

package org.infogrid.meshbase.net.proxy;

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.util.event.AbstractExternalizableEvent;
import org.infogrid.util.event.SourceUnresolvedException;
import org.infogrid.util.event.ValueUnresolvedException;

/**
 * Abstract superclass for events emitted by a Proxy. This uses AbstractExternalizableEvent
 * in order to avoid EventObject's serialization problems, but does not attempt to
 * resolve Proxies.
 */
public abstract class ProxyEvent
        extends
            AbstractExternalizableEvent<Proxy,NetMeshBaseIdentifier,Proxy,NetMeshBaseIdentifier>
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param sender the sending Proxy
     * @param senderIdentifier the identifier representing the sending Proxy
     * @param affectedProxy the affected Proxy, if available
     * @param affectedProxyIdentifier the identifier of the affected Proxy (may be null)
     * @param timeEventOccurred the time the event occurred
     */
    public ProxyEvent(
            Proxy                 sender,
            NetMeshBaseIdentifier senderIdentifier,
            Proxy                 affectedProxy,
            NetMeshBaseIdentifier affectedProxyIdentifier,
            long                  timeEventOccurred )
    {
        super( sender, sender.getPartnerMeshBaseIdentifier(), null, null, timeEventOccurred );
    }

    /**
     * Resolve the source of the event.
     *
     * @return the source of the event
     * @throws SourceUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the source failed
     */
    protected Proxy resolveSource()
            throws
                SourceUnresolvedException
    {
        throw new SourceUnresolvedException( this );
    }
    
    /**
     * Resolve a value of the event.
     *
     * @param vid the identifier of the value of the event
     * @return a value of the event
     * @throws ValueUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    protected Proxy resolveValue(
            NetMeshBaseIdentifier vid )
       throws
           ValueUnresolvedException
    {
        throw new ValueUnresolvedException( this, vid );
    }
}
