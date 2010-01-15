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

package org.infogrid.probe.store.test;

import org.infogrid.store.Store;
import org.infogrid.store.StoreListener;
import org.infogrid.store.StoreValue;
import org.infogrid.util.logging.Log;

/**
 * Listens to Store events and prints them out.
 */
public class TestStoreListener
    implements
        StoreListener
{
    private static final Log log = Log.getLogInstance( TestStoreListener.class ); // our own, private logger

    /**
     * A put operation was performed. This indicates either a
     * <code>Store.put</code> or a <code>Store.putOrUpdate</code> operation
     * in which an actual <code>put</code> was performed.
     *
     * @param store the Store that emitted this event
     * @param value the StoreValue that was put
     */
    public void putPerformed(
            Store      store,
            StoreValue value )
    {
        if( log.isTraceEnabled()) {
            log.traceMethodCallEntry( this, "putPerformed", store, value, new RuntimeException("marker") );
        }
    }

    /**
     * An update operation was performed. This indicates either a
     * <code>Store.update</code> or a <code>Store.putOrUpdate</code> operation
     * in which an actual <code>update</code> was performed.
     *
     * @param store the Store that emitted this event
     * @param value the StoreValue that was updated
     */
    public void updatePerformed(
            Store      store,
            StoreValue value )
    {
        if( log.isTraceEnabled()) {
            log.traceMethodCallEntry( this, "updatePerformed", store, value );
        }
    }

    /**
     * A get operation was performed.
     *
     * @param store the Store that emitted this event
     * @param value the StoreValue that was obtained
     */
    public void getPerformed(
            Store      store,
            StoreValue value )
    {
        if( log.isTraceEnabled()) {
            log.traceMethodCallEntry( this, "getPerformed", store, value );
        }
    }

    /**
     * A get operation was attempted but not value could be found.
     *
     * @param store the Store that emitted this event
     * @param key the key that was attempted
     */
    public void getFailed(
            Store  store,
            String key )
    {
        if( log.isTraceEnabled()) {
            log.traceMethodCallEntry( this, "getFailed", store, key );
        }
    }

    /**
     * A delete operation was performed.
     *
     * @param store the Store that emitted this event
     * @param key the key with which the data element was stored
     */
    public void deletePerformed(
            Store  store,
            String key )
    {
        if( log.isTraceEnabled()) {
            log.traceMethodCallEntry( this, "deletePerformed", store, key );
        }
    }

    /**
     * A delete-all operation was performed.
     *
     * @param store the Store that emitted this event
     * @param prefix if given, indicates the prefix of all keys that were deleted. If null, indicates &quot;all&quot;.
     */
    public void deleteAllPerformed(
            Store  store,
            String prefix )
    {
        if( log.isTraceEnabled()) {
            log.traceMethodCallEntry( this, "deleteAllPerformed", store, prefix );
        }
    }
}
