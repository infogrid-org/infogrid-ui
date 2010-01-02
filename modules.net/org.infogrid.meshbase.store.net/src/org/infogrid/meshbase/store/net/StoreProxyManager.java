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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.meshbase.store.net;

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyFactory;
import org.infogrid.meshbase.net.proxy.ProxyManager;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.AbstractSwappingHashMapListener;
import org.infogrid.util.SwappingHashMap;

/**
 * Adds a listener to the ProxyManager that "patches" the Proxies recreated from
 * Storage by telling them about the StoreProxyManager.
 */
public class StoreProxyManager
    extends
        ProxyManager
{
    /**
     * Factory method.
     * 
     * @param proxyFactory the underlying factory of Proxies
     * @param storage the storage for the Proxies managed by this ProxyManager
     * @return the created StoreProxyManager
     */
    public static StoreProxyManager create(
            ProxyFactory                                proxyFactory,
            StoreBackedSwappingHashMap<NetMeshBaseIdentifier,Proxy> storage )
    {
        return new StoreProxyManager( proxyFactory, storage );
    }

    /**
     * Constructor.
     *
     * @param proxyFactory the underlying factory of Proxies
     * @param storage the storage for the Proxies managed by this ProxyManager
     */
    protected StoreProxyManager(
            ProxyFactory                                proxyFactory,
            StoreBackedSwappingHashMap<NetMeshBaseIdentifier,Proxy> storage )
    {
        super( proxyFactory, storage );

        theMapListener = new MyMapListener();
        storage.addWeakSwappingHashMapListener( theMapListener ); // this must be weak 
    }
    
    /**
     * Keep a reference to the listener to avoid it being garbage-collected.
     */
    protected MyMapListener theMapListener;

    /**
     * Patches Proxies restored from disk before they are returned.
     */
    protected class MyMapListener
            extends
                AbstractSwappingHashMapListener<NetMeshBaseIdentifier,Proxy>
    {
        /**
         * A key-value pair has been loaded from storage.
         *
         * @param source the SwappingHashMap that emitted this event
         * @param key the key of the key-value pair that was loaded
         * @param value the value of the key-value pair that was saved
         */
        @Override
        public void loadedFromStorage(
                SwappingHashMap<NetMeshBaseIdentifier,Proxy> source,
                NetMeshBaseIdentifier                        key,
                Proxy                                        value )
        {
            if( value != null ) {
                value.setFactory( StoreProxyManager.this ); // patch this back in after restore from disk
            }
        }        
    }
}
