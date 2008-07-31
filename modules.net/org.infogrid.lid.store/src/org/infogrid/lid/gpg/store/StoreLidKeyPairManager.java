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

import org.infogrid.lid.gpg.LidGpgKeyPairFactory;
import org.infogrid.lid.gpg.LidKeyPair;
import org.infogrid.lid.gpg.LidKeyPairManager;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.CachingMap;
import org.infogrid.util.Factory;
import org.infogrid.util.PatientSmartFactory;

/**
 * A Store implementation of LidKeyPairManager. FIXME: this implementation
 * should not hold private keys in memory for security reasons, but currently it does.
 */
public class StoreLidKeyPairManager
        extends
            PatientSmartFactory<String,LidKeyPair,Void>
        implements
            LidKeyPairManager
{
    /**
     * Factory method.
     *
     * @param store the Store to use
     * @return the created StoreLidKeyPairManager
     */
    public static StoreLidKeyPairManager create(
            Store store )
    {
        StoreLidKeyPairMapper mapper = new StoreLidKeyPairMapper();
        
        LidGpgKeyPairFactory delegateFactory = new LidGpgKeyPairFactory();
        StoreBackedSwappingHashMap<String,LidKeyPair> storage = StoreBackedSwappingHashMap.createWeak( mapper, store );
        
        StoreLidKeyPairManager ret = new StoreLidKeyPairManager( delegateFactory, storage );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param delegateFactory the Factory that knows how to instantiate values
     * @param storage the storage to use
     */
    protected StoreLidKeyPairManager(
            Factory<String,LidKeyPair,Void> delegateFactory,
            CachingMap<String,LidKeyPair>   storage )
    {
        super( delegateFactory, storage );
    }
    
    
    /**
     * The Store to use.
     */
    protected Store theStore;
    
    /**
     * The encoding to use.
     */
    public static final String ENCODING = StoreLidKeyPairManager.class.getName();
    
    /**
     * THe characterset to use.
     */
    protected static final String CHARSET = "UTF-8";
}
