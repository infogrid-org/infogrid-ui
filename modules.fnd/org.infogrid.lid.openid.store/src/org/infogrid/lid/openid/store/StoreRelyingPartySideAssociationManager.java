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

package org.infogrid.lid.openid.store;

import org.infogrid.lid.openid.OpenIdAssociationNegotiationParameters;
import org.infogrid.lid.openid.OpenIdRelyingPartySideAssociation;
import org.infogrid.lid.openid.OpenIdRelyingPartySideAssociationManager;
import org.infogrid.lid.openid.OpenIdRelyingPartySideAssociationNegotiator;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.CachingMap;
import org.infogrid.util.Factory;
import org.infogrid.util.PatientSmartFactory;

/**
 * A OpenIdRelyingPartySideAssociationManager implemented using Store.
 */
public class StoreRelyingPartySideAssociationManager
        extends
            PatientSmartFactory<String,OpenIdRelyingPartySideAssociation,OpenIdAssociationNegotiationParameters>
        implements
            OpenIdRelyingPartySideAssociationManager
{
    /**
     * Factory method.
     * 
     * @param store the Store to use
     * @return the created StoreRelyingPartySideAssociationManager
     */
    public static StoreRelyingPartySideAssociationManager create(
            Store store )
    {
        RelyingPartySideAssociationMapper                  mapper          = RelyingPartySideAssociationMapper.create();
        StoreBackedSwappingHashMap<String,OpenIdRelyingPartySideAssociation> storage         = StoreBackedSwappingHashMap.createWeak( mapper, store );
        OpenIdRelyingPartySideAssociationNegotiator              delegateFactory = OpenIdRelyingPartySideAssociationNegotiator.create();
        
        return new StoreRelyingPartySideAssociationManager( delegateFactory, storage );
    }

    /**
     * Constructor.
     * 
     * @param delegateFactory the factory for RelyingPartySideAssociations
     * @param storage the storage for the RelyingPartySideAssociations
     */
    protected StoreRelyingPartySideAssociationManager(
            Factory<String,OpenIdRelyingPartySideAssociation,OpenIdAssociationNegotiationParameters> delegateFactory,
            CachingMap<String,OpenIdRelyingPartySideAssociation>                               storage )
    {
        super( delegateFactory, storage );
    }
    
    /**
     * This overridable method allows our subclasses to judge whether a value retrieved
     * from cache is still good. If not, it will be discarded and the factory proceeeds
     * as if no value had been found in the cache in the first place. This method should
     * return quickly.
     *
     * @param key the key that was passed into the obtainFor method
     * @param value the found value, which is being looked at
     * @param argument the argument that was passed into the obtainFor method
     * @return true if this value is still good
     */
    protected boolean isStillGood(
            String                      key,
            OpenIdRelyingPartySideAssociation value,
            Object                      argument )
    {
        boolean ret = value.isCurrentlyValid();
        return ret;
    }
}
