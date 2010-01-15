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
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.util.FactoryException;

/**
 * ProxyFactory that manufactures DefaultProxies.
 */
public class DefaultProxyFactory
        extends
            AbstractProxyFactory
{
    /** 
     * Factory method.
     *
     * @param endpointFactory the ProxyMessageEndpointFactory to use to communicate
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     * @return the created DefaultProxyFactory.
     */
    public static DefaultProxyFactory create(
            ProxyMessageEndpointFactory endpointFactory,
            ProxyPolicyFactory          proxyPolicyFactory )
    {
        DefaultProxyFactory ret = new DefaultProxyFactory( endpointFactory, proxyPolicyFactory );
        
        return ret;
    }

    /**
     * Constructor.
     * 
     * @param endpointFactory the ProxyMessageEndpointFactory to use to communicate
     * @param proxyPolicyFactory the factory for ProxyPolicies for communications with other NetMeshBases
     */
    protected DefaultProxyFactory(
            ProxyMessageEndpointFactory endpointFactory,
            ProxyPolicyFactory        proxyPolicyFactory )
    {
        super( endpointFactory, proxyPolicyFactory );
    }

    /**
     * Create a Proxy.
     *
     * @param partnerMeshBaseIdentifier the NetMeshBaseIdentifier of the NetMeshBase to talk to
     * @param arg the CoherenceSpecification to use
     * @return the created Proxy
     * @throws FactoryException thrown if the Proxy could not be created
     */
    public Proxy obtainFor(
            NetMeshBaseIdentifier  partnerMeshBaseIdentifier,
            CoherenceSpecification arg )
        throws
            FactoryException
    {
        ProxyMessageEndpoint endpoint = theEndpointFactory.obtainFor( partnerMeshBaseIdentifier, theNetMeshBase.getIdentifier() );
        ProxyPolicy          policy   = theProxyPolicyFactory.obtainFor( partnerMeshBaseIdentifier, arg );// in the future, this should become configurable

        Proxy ret = DefaultProxy.create( endpoint, theNetMeshBase, policy, partnerMeshBaseIdentifier );
        ret.setFactory( this );

        // we don't need to start communicating here yet -- it suffices that we start
        // when the first message is handed to the ProxyMessageEndpoint

        return ret;
    } 

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
            FactoryException
    {
        ProxyMessageEndpoint ep = theEndpointFactory.restoreNetMessageEndpoint(
                externalized.getNetworkIdentifierOfPartner(),
                externalized.getNetworkIdentifier(),
                externalized.getLastSentToken(),
                externalized.getLastReceivedToken(),
                externalized.messagesLastSent(),
                externalized.messagesToBeSent() );

        NiceAndTrustingProxyPolicy policy = NiceAndTrustingProxyPolicy.create( // in the future, this should become configurable -- FIXME
                externalized.getCoherenceSpecification() );

        DefaultProxy ret = DefaultProxy.restoreProxy(
                ep,
                theNetMeshBase,
                policy,
                externalized.getNetworkIdentifierOfPartner(),
                externalized.getTimeCreated(),
                externalized.getTimeUpdated(),
                externalized.getTimeRead(),
                externalized.getTimeExpires() );
  
        return ret;
    }
}
