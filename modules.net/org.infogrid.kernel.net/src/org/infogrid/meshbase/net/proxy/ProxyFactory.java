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

import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.util.Factory;
import org.infogrid.util.FactoryException;

/**
 * A factory that knows how to construct Proxies.
 */
public interface ProxyFactory
        extends
            Factory<NetMeshBaseIdentifier,Proxy,CoherenceSpecification>
{
    /**
     * Set the NetMeshBase to be used with new Proxies.
     *
     * @param base the NetMeshBase to use
     */
    public void setNetMeshBase(
            NetMeshBase base );
    
    /**
     * Obtain the NetMeshBase to be used with new Proxies.
     * 
     * @return the NetMeshBase
     */
    public NetMeshBase getNetMeshBase();

    /**
     * Recreate a Proxy from an ExternalizedProxy.
     *
     * @param externalized the ExternalizedProxy
     * @return the recreated Proxy
     * @throws FactoryException thrown if the Proxy restore failed
     */
    public Proxy restoreProxy(
            ExternalizedProxy externalized )
        throws
            FactoryException;
}