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

import org.infogrid.lid.LidSession;
import org.infogrid.lid.LidSessionManager;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.Factory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.MSmartFactory;

/**
 * Implements LidSessionManager using the Store abstraction.
 */
public class StoreLidSessionManager
        extends
            MSmartFactory<String,LidSession,String>
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
        StoreLidSessionMapper mapper = new StoreLidSessionMapper();
        
        StoreBackedSwappingHashMap<String,LidSession> storage = StoreBackedSwappingHashMap.createWeak( 
                mapper,
                store );
        
        Factory<String,LidSession,String> delegateFactory = new AbstractFactory<String,LidSession,String>() {
            public LidSession obtainFor(
                    String lid,
                    String clientIp )
                throws
                    FactoryException
            {
                LidSession ret = LidSession.create( lid, clientIp );
                
                return ret;
            }

        };

        StoreLidSessionManager ret = new StoreLidSessionManager( delegateFactory, storage );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param delegateFactory the underlying factory for LidSessions
     * @param storage the storage to use
     */
    protected StoreLidSessionManager(
            Factory<String,LidSession,String>             delegateFactory,
            StoreBackedSwappingHashMap<String,LidSession> storage )
    {
        super( delegateFactory, storage );
    }
}
