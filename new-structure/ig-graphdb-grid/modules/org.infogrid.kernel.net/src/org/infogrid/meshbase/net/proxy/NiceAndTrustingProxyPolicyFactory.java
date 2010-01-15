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
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.FactoryException;

/**
 * A ProxyPolicyFactory that generates nice and trusting ProxyPolicies. This may not
 * be too wise in a production environment that isn't otherwise tightly controlled
 * or secured.
 */
public class NiceAndTrustingProxyPolicyFactory
        extends
            AbstractFactory<NetMeshBaseIdentifier,ProxyPolicy,CoherenceSpecification>
        implements
            ProxyPolicyFactory
{
    /**
     * Factory method to create the factory.
     * 
     * @return the created NiceAndTrustingProxyPolicyFactory
     */
    public static NiceAndTrustingProxyPolicyFactory create()
    {
        NiceAndTrustingProxyPolicyFactory ret = new NiceAndTrustingProxyPolicyFactory( true );
        return ret;
    }
    
    /**
     * Factory method to create the factory.
     * 
     * @param pointsReplicasToItself if true, new Replicas will be created by a branch from the local Replica
     * @return the created NiceAndTrustingProxyPolicyFactory
     */
    public static NiceAndTrustingProxyPolicyFactory create(
            boolean pointsReplicasToItself )
    {
        NiceAndTrustingProxyPolicyFactory ret = new NiceAndTrustingProxyPolicyFactory( pointsReplicasToItself );
        return ret;
    }
    
    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param pointsReplicasToItself if true, new Replicas will be created by a branch from the local Replica
     */
    protected NiceAndTrustingProxyPolicyFactory(
            boolean pointsReplicasToItself )
    {
        thePointsReplicasToItself = pointsReplicasToItself;
    }

    /**
     * Factory method.
     *
     * @param key the key information required for object creation, if any
     * @param argument any argument-style information required for object creation, if any
     * @return the created object
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public NiceAndTrustingProxyPolicy obtainFor(
            NetMeshBaseIdentifier  key,
            CoherenceSpecification argument )
        throws
            FactoryException
    {
        NiceAndTrustingProxyPolicy ret = NiceAndTrustingProxyPolicy.create( argument, thePointsReplicasToItself );
        return ret;
    }
    
    /**
     * If true, new Replicas will be created by a branch from the local Replica.
     */
    protected boolean thePointsReplicasToItself;
}