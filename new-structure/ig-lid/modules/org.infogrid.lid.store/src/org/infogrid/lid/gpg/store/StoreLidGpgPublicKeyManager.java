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

package org.infogrid.lid.gpg.store;

import org.infogrid.lid.gpg.LidGpgPublicKeyManager;
import org.infogrid.lid.gpg.LidGpgPublicKeyNegotiator;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.CachingMap;
import org.infogrid.util.Factory;
import org.infogrid.util.Identifier;
import org.infogrid.util.IdentifierFactory;
import org.infogrid.util.PatientSmartFactory;

/**
 * A Store implementation of LidGpgPublicKeyManager.
 */
public class StoreLidGpgPublicKeyManager
        extends
            PatientSmartFactory<Identifier,String,Void>
        implements
            LidGpgPublicKeyManager
{
    /**
     * Factory method.
     *
     * @param identifierFactory the factory for identifiers to use
     * @param store the Store to use
     * @return the created StoreLidGpgPublicKeyManager
     */
    public static StoreLidGpgPublicKeyManager create(
            IdentifierFactory identifierFactory,
            Store             store )
    {
        StoreLidGpgPublicKeyMapper mapper = new StoreLidGpgPublicKeyMapper( identifierFactory );
        
        LidGpgPublicKeyNegotiator delegateFactory = new LidGpgPublicKeyNegotiator();
        StoreBackedSwappingHashMap<Identifier,String> storage = StoreBackedSwappingHashMap.createWeak( mapper, store );
        
        StoreLidGpgPublicKeyManager ret = new StoreLidGpgPublicKeyManager( delegateFactory, storage );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param delegateFactory the Factory that knows how to instantiate values
     * @param storage the storage to use
     */
    protected StoreLidGpgPublicKeyManager(
            Factory<Identifier,String,Void> delegateFactory,
            CachingMap<Identifier,String>   storage )
    {
        super( delegateFactory, storage );
    }
}
