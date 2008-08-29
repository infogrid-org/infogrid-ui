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

import org.infogrid.lid.AbstractLidNonceManager;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;

/**
 * Store implementation of LidNonceManager. THis works the opposite way one could
 * think: nonces are generated, but not stored. When presented and not in the Store,
 * everything is fine. If in the store, we have a playback attack. This depends on
 * cleaning the Store out from time to time.
 */
public class StoreLidNonceManager
        extends
            AbstractLidNonceManager
{
    /**
     * Factory method.
     *
     * @param store the Store to use
     * @return the created StoreLidNonceManager
     */
    public static StoreLidNonceManager create(
            Store store )
    {
        StoreLidNonceMapper mapper = new StoreLidNonceMapper();
        
        StoreBackedSwappingHashMap<String,String> storage = StoreBackedSwappingHashMap.createWeak( 
                mapper,
                store );
        StoreLidNonceManager ret = new StoreLidNonceManager( storage );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param storage the storage to use
     */
    protected StoreLidNonceManager(
            StoreBackedSwappingHashMap<String,String> storage )
    {
        theStorage = storage;
    }

    /**
     * Generate a new nonce.
     * 
     * @return the newly generated nonce
     */
    @Override
    public String generateNewNonce()
    {
        String ret = super.generateNewNonce();

        // do not put into storage
        
        return ret;
    }

    /**
     * Validate a presented nonce.
     * 
     * @param nonce the presented nonce
     * @return true if the nonce is valid, false if it is not valid or unknown.
     */
    public boolean validateNonce(
            String nonce )
    {
        if( !validateNonceTimeRange( nonce )) {
            return false;
        }
        
        String found = theStorage.put( nonce, nonce );
        if( found != null ) {
            return false;
        } else {
            return true;
        }
        
    }

    /**
     * The storage to use.
     */
    protected StoreBackedSwappingHashMap<String,String> theStorage;
}
