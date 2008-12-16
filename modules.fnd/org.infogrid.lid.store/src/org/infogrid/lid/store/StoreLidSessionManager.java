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

package org.infogrid.lid.store;

import org.infogrid.lid.AbstractLidSessionManager;
import org.infogrid.lid.LidSession;
import org.infogrid.lid.LidSessionManager;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.Factory;
import org.infogrid.util.Identifier;

/**
 * Implements LidSessionManager using the Store abstraction.
 */
public class StoreLidSessionManager
        extends
            AbstractLidSessionManager
        implements
            LidSessionManager
{
    /**
     * Factory method.
     * 
     * @param store the Store to use
     * @return the created StoreLidSessionManager
     */
    public static StoreLidSessionManager create(
            Store store )
    {
        return create( store, DEFAULT_SESSION_DURATION );
    }

    /**
     * Factory method.
     * 
     * @param store the Store to use
     * @param sessionDuration the duration of the session, in milliseconds
     * @return the created StoreLidSessionManager
     */
    public static StoreLidSessionManager create(
            Store store,
            long  sessionDuration )
    {
        StoreLidSessionMapper mapper = new StoreLidSessionMapper();
        
        StoreBackedSwappingHashMap<Identifier,LidSession> storage = StoreBackedSwappingHashMap.createWeak(
                mapper,
                store );
        
        MyDelegateFactory delegateFactory = new MyDelegateFactory();
        
        StoreLidSessionManager ret = new StoreLidSessionManager( delegateFactory, storage, sessionDuration );
        delegateFactory.setLidSessionManager( ret );
        
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param delegateFactory the underlying factory for LidSessions
     * @param storage the storage to use
     * @param sessionDuration the duration of new or renewed sessions in milli-seconds
     */
    protected StoreLidSessionManager(
            Factory<Identifier,LidSession,String>             delegateFactory,
            StoreBackedSwappingHashMap<Identifier,LidSession> storage,
            long                                              sessionDuration )
    {
        super( delegateFactory, storage, sessionDuration );
    }    
}

