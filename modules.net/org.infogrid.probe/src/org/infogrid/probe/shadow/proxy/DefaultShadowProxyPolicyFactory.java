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

package org.infogrid.probe.shadow.proxy;

import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.ProxyPolicy;
import org.infogrid.meshbase.net.proxy.ProxyPolicyFactory;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.FactoryException;

/**
 * A ProxyPolicyFactory that generates default ProxyPolicies for ShadowMeshBases.
 */
public class DefaultShadowProxyPolicyFactory
        extends
            AbstractFactory<NetMeshBaseIdentifier,ProxyPolicy,CoherenceSpecification>
        implements
            ProxyPolicyFactory
{
    /**
     * Factory method to create the factory.
     * 
     * @return the created DefaultShadowProxyPolicyFactory
     */
    public static DefaultShadowProxyPolicyFactory create()
    {
        DefaultShadowProxyPolicyFactory ret = new DefaultShadowProxyPolicyFactory();
        return ret;
    }
    
    /**
     * Constructor for subclasses only, use factory method.
     */
    protected DefaultShadowProxyPolicyFactory()
    {
        // no op
    }

    /**
     * Factory method.
     *
     * @param key the key information required for object creation, if any
     * @param argument any argument-style information required for object creation, if any
     * @return the created object
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public DefaultShadowProxyPolicy obtainFor(
            NetMeshBaseIdentifier  key,
            CoherenceSpecification argument )
        throws
            FactoryException
    {
        DefaultShadowProxyPolicy ret = DefaultShadowProxyPolicy.create();
        return ret;
    }
}