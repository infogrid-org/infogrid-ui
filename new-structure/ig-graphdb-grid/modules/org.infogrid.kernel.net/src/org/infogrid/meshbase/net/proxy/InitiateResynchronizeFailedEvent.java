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

import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.util.logging.Dumper;

/**
 * Indicates that the attempt to issue a resynchronization via a 3rd Proxy failed.
 */
public class InitiateResynchronizeFailedEvent
        extends
           ProxyEvent
{
    private static final long serialVersionUID = 1L; // helps with serialization
    
    /**
     * Constructor.
     *
     * @param sender the sending Proxy
     * @param resynchronizeProxyIdentifier the NetMeshBaseIdentifier of the Proxy through which resynchronization failed
     * @param toBeResynchronized the NetMeshObjectIdentifiers of the NetMeshObjects that were to be resynchronized
     * @param cause the underlying cause
     */
    public InitiateResynchronizeFailedEvent(
            Proxy                      sender,
            NetMeshBaseIdentifier      resynchronizeProxyIdentifier,
            NetMeshObjectIdentifier [] toBeResynchronized,
            Throwable                  cause )
    {
        super( sender, sender.getPartnerMeshBaseIdentifier(), null, resynchronizeProxyIdentifier, System.currentTimeMillis() );
        
        theToBeResynchronized = toBeResynchronized;
        theCause              = cause;
    }
    
    /**
     * Obtain the NetMeshObjectIdentifiers of the NetMeshObjects that were to be resynchronized.
     * 
     * @return NetMeshObjectIdentifiers of the NetMeshObjects that were to be resynchronized
     */
    public NetMeshObjectIdentifier [] getToBeResynchronized()
    {
        return theToBeResynchronized;
    }

    /**
     * The underlying cause.
     * 
     * @return the cause
     */
    public Throwable getCause()
    {
        return theCause;
    }
    
    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    @Override
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theSourceIdentifier",
                    "theDeltaValueIdentifier",
                    "theTimeEventOccurred",
                    "theToBeResynchronized",
                    "theCause"
                },
                new Object[] {
                    getSourceIdentifier(),
                    getDeltaValueIdentifier(),
                    getTimeEventOccurred(),
                    theToBeResynchronized,
                    theCause
                });
    }

    /**
     * The NetMeshObjectIdentifiers of the NetMeshObjects that were to be resynchronized.
     */
    protected NetMeshObjectIdentifier [] theToBeResynchronized;

    /**
     * The underlying cause.
     */
    protected Throwable theCause;
}
