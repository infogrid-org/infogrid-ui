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

import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;

/**
 * An instruction that keeps information about a to-be-locally-instantiated or to-be-resynchronized
 * NetMeshObject together. It is used for both, as the instruction is structurally the same.
 */
public class InstantiateOrResynchronizeInstruction
{
    /**
     * Factory method.
     * 
     * @param externalizedNetMeshObject the external representation of the NetMeshObject
     * @param proxyTowardsHome the correct proxyTowardsHome for the NetMeshObject
     * @param proxyTowardsLock the correct proxyTowardsLock for the NetMeshObject
     * @param giveUpHome if true, the NetMeshObject is supposed to give up home replica status when asked
     * @param giveUpLock if true, the NetMeshObject is supposed to give up its update rights when asked
     * @return the created InstantiateOrResynchronizeInstruction
     */
    public static InstantiateOrResynchronizeInstruction create(
            ExternalizedNetMeshObject externalizedNetMeshObject,
            Proxy                     proxyTowardsHome,
            Proxy                     proxyTowardsLock,
            boolean                   giveUpHome,
            boolean                   giveUpLock )
    {
        return new InstantiateOrResynchronizeInstruction(
                externalizedNetMeshObject,
                proxyTowardsHome,
                proxyTowardsLock,
                giveUpHome,
                giveUpLock );
    }

    /**
     * Constructor.
     * 
     * @param externalizedNetMeshObject the external representation of the NetMeshObject
     * @param proxyTowardsHome the correct proxyTowardsHome for the NetMeshObject
     * @param proxyTowardsLock the correct proxyTowardsLock for the NetMeshObject
     * @param giveUpHome if true, the NetMeshObject is supposed to give up home replica status when asked
     * @param giveUpLock if true, the NetMeshObject is supposed to give up its update rights when asked
     */
    protected InstantiateOrResynchronizeInstruction(
            ExternalizedNetMeshObject externalizedNetMeshObject,
            Proxy                     proxyTowardsHome,
            Proxy                     proxyTowardsLock,
            boolean                   giveUpHome,
            boolean                   giveUpLock )
    {
        theExternalizedNetMeshObject = externalizedNetMeshObject;
        theProxyTowardsHome          = proxyTowardsHome;
        theProxyTowardsLock          = proxyTowardsLock;
        theGiveUpHome                = giveUpHome;
        theGiveUpLock                = giveUpLock;
    }

    /**
     * Obtain the ExternalizedNetMeshObject.
     * 
     * @return the ExternalizedNetMeshObject
     */
    public ExternalizedNetMeshObject getExternalizedNetMeshObject()
    {
        return theExternalizedNetMeshObject;
    }

    /**
     * Obtain the Proxy towards the home replica for this NetMeshObject, if any.
     * 
     * @return the Proxy
     */
    public Proxy getProxyTowardsHome()
    {
        return theProxyTowardsHome;
    }
    
    /**
     * Obtain the Proxy towards the replica with the lock for this NetMeshObject, if any.
     * 
     * @return the Proxy
     */
    public Proxy getProxyTowardsLock()
    {
        return theProxyTowardsLock;
    }

    /**
     * Obtain whether this NetMeshObject will surrender home replica status when
     * asked.
     * 
     * @return true if it surrenders the home replica status
     */
    public boolean getGiveUpHome()
    {
        return theGiveUpHome;
    }

    /**
     * Obtain whether this NetMeshObject will surrender update rights when
     * asked.
     * 
     * @return true if it surrenders update rights
     */
    public boolean getGiveUpLock()
    {
        return theGiveUpLock;
    }
    
    /**
     * Externalized representation of the correct version of the NetMeshObject.
     */
    protected ExternalizedNetMeshObject theExternalizedNetMeshObject;
    
    /**
     * Correct ProxyTowardsHome, if any.
     */
    protected Proxy theProxyTowardsHome;
    
    /**
     * Correct ProxyTowardsLock, if any.
     */
    protected Proxy theProxyTowardsLock;
    
    /**
     * If true, will surrender home replica status when asked.
     */
    protected boolean theGiveUpHome;
    
    /**
     * If true, will surrender update rights when asked.
     */
    protected boolean theGiveUpLock;
}