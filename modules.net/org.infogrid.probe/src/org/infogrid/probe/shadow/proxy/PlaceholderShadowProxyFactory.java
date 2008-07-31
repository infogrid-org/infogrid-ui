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
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.meshbase.net.proxy.AbstractProxyFactory;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpoint;
import org.infogrid.probe.shadow.externalized.ExternalizedShadowProxy;
import org.infogrid.util.FactoryException;

/**
 * Factory of passive, non-communicating Proxies for the purposes of the StagingMeshBase only.
 */
public class PlaceholderShadowProxyFactory
        extends
            AbstractProxyFactory
{
    /** 
     * Factory method.
     *
     * @return the created PlaceholderShadowProxyFactory.
     */
    public static PlaceholderShadowProxyFactory create()
    {
        PlaceholderShadowProxyFactory ret = new PlaceholderShadowProxyFactory();
        
        return ret;
    }

    /**
     * Constructor.
     */
    protected PlaceholderShadowProxyFactory()
    {
        super( null, null );
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
        Proxy ret = DefaultShadowProxy.create( null, theNetMeshBase );
        ret.setFactory( this );

        return ret;
    } 

    /**
     * Recreate a Proxy from an ExternalizedProxy.
     *
     * @param externalized the ExternalizedProxy
     * @return the recreated Proxy
     * @throws FactoryException thrown if the Proxy could not be restored
     */
    public Proxy restoreProxy(
            ExternalizedProxy externalized )
        throws
            FactoryException
    {
        ExternalizedShadowProxy realExternalized = (ExternalizedShadowProxy) externalized;

        Proxy ret;
        if( realExternalized.getIsPlaceholder() ) {
            ret = DefaultShadowProxy.restoreProxy(
                    null,
                    theNetMeshBase,
                    true,
                    externalized.getTimeCreated(),
                    externalized.getTimeUpdated(),
                    externalized.getTimeRead(),
                    externalized.getTimeExpires() );

        } else {        
            ProxyMessageEndpoint ep = theEndpointFactory.restoreNetMessageEndpoint(
                    externalized.getNetworkIdentifierOfPartner(),
                    externalized.getNetworkIdentifier(),
                    externalized.getLastSentToken(),
                    externalized.getLastReceivedToken(),
                    externalized.messagesLastSent(),
                    externalized.messagesToBeSent() );

            ret = DefaultShadowProxy.restoreProxy(
                    ep,
                    theNetMeshBase,
                    false,
                    externalized.getTimeCreated(),
                    externalized.getTimeUpdated(),
                    externalized.getTimeRead(),
                    externalized.getTimeExpires() );
        }  
        return ret;
    }
}
