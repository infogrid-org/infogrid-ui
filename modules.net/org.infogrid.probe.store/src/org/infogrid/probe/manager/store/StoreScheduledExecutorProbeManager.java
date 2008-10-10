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

package org.infogrid.probe.manager.store;

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.probe.manager.ScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.store.StoreShadowMeshBaseFactory;
import org.infogrid.store.IterableStore;
import org.infogrid.store.util.IterableStoreBackedSwappingHashMap;
import org.infogrid.util.AbstractSwappingHashMapListener;
import org.infogrid.util.SwappingHashMap;

/**
 * A ScheduledExecutorProbeManager that stores ShadowMeshBases in a Store.
 */
public class StoreScheduledExecutorProbeManager
        extends
            ScheduledExecutorProbeManager
{
    /**
     * Factory method.
     *
     * @param delegate the underlying factory for StoreShadowMeshBases
     * @param shadowStore the Store in which serialized ShadowMeshBases are kept
     * @return the created StoreScheduledExecutorProbeManager
     */
    public static StoreScheduledExecutorProbeManager create(
            StoreShadowMeshBaseFactory delegate,
            IterableStore              shadowStore )
    {
        StoreProbeManagerMapper theMapper = new StoreProbeManagerMapper( delegate );

        IterableStoreBackedSwappingHashMap<NetMeshBaseIdentifier,ShadowMeshBase> storage = IterableStoreBackedSwappingHashMap.createWeak( theMapper, shadowStore );

        StoreScheduledExecutorProbeManager ret = new StoreScheduledExecutorProbeManager( delegate, storage );

        return ret;
    }

    /**
     * Private constructor, use factory method.
     * 
     * @param delegate the underlying factory for StoreShadowMeshBases
     * @param storage the storage to use
     */
    protected StoreScheduledExecutorProbeManager(
            StoreShadowMeshBaseFactory                                               delegate,
            IterableStoreBackedSwappingHashMap<NetMeshBaseIdentifier,ShadowMeshBase> storage )
    {
        super( delegate, storage );

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
                AbstractSwappingHashMapListener<NetMeshBaseIdentifier,ShadowMeshBase>
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
                SwappingHashMap<NetMeshBaseIdentifier,ShadowMeshBase> source,
                NetMeshBaseIdentifier                                 key,
                ShadowMeshBase                                        value )
        {
            if( value != null ) {
                value.setFactory( StoreScheduledExecutorProbeManager.this ); // patch this back in after restore from disk
            }
        }        
    }
}
