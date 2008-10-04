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

import org.infogrid.lid.openid.OpenIdRpSideAssociationNegotiationParameters;
import org.infogrid.lid.openid.OpenIdRpSideAssociation;
import org.infogrid.lid.openid.OpenIdRpSideAssociationManager;
import org.infogrid.lid.openid.OpenIdRpSideAssociationNegotiator;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.CachingMap;
import org.infogrid.util.Factory;
import org.infogrid.util.PatientSmartFactory;

/**
 * A OpenIdRpSideAssociationManager implemented using Store.
 */
public class StoreOpenIdRpSideAssociationManager
        extends
            PatientSmartFactory<String,OpenIdRpSideAssociation,OpenIdRpSideAssociationNegotiationParameters>
        implements
            OpenIdRpSideAssociationManager
{
    /**
     * Factory method.
     * 
     * @param store the Store to use
     * @return the created StoreOpenIdRpSideAssociationManager
     */
    public static StoreOpenIdRpSideAssociationManager create(
            Store store )
    {
        OpenIdRpSideAssociationMapper                              mapper          = OpenIdRpSideAssociationMapper.create();
        StoreBackedSwappingHashMap<String,OpenIdRpSideAssociation> storage         = StoreBackedSwappingHashMap.createWeak( mapper, store );
        OpenIdRpSideAssociationNegotiator                          delegateFactory = OpenIdRpSideAssociationNegotiator.create();
        
        return new StoreOpenIdRpSideAssociationManager( delegateFactory, storage );
    }

    /**
     * Constructor.
     * 
     * @param delegateFactory the factory for RelyingPartySideAssociations
     * @param storage the storage for the RelyingPartySideAssociations
     */
    protected StoreOpenIdRpSideAssociationManager(
            Factory<String,OpenIdRpSideAssociation,OpenIdRpSideAssociationNegotiationParameters> delegateFactory,
            CachingMap<String,OpenIdRpSideAssociation>                                           storage )
    {
        super( delegateFactory, storage );
    }
}
