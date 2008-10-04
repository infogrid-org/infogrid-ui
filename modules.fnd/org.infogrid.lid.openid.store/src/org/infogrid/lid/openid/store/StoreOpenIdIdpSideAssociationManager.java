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

import org.infogrid.lid.openid.OpenIdIdpSideAssociation;
import org.infogrid.lid.openid.AbstractOpenIdIdpSideAssociationManager;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.CachingMap;

/**
 * An OpenIdIdpSideAssociationManager implemented using Store.
 */
public class StoreOpenIdIdpSideAssociationManager
        extends
            AbstractOpenIdIdpSideAssociationManager
{
    /**
     * Factory method.
     * 
     * @param store the Store to use
     * @return the created StoreOpenIdIdpSideAssociationManager
     */
    public static StoreOpenIdIdpSideAssociationManager create(
            Store store )
    {
        OpenIdIdpSideAssociationMapper                              mapper  = OpenIdIdpSideAssociationMapper.create();
        StoreBackedSwappingHashMap<String,OpenIdIdpSideAssociation> storage = StoreBackedSwappingHashMap.createWeak( mapper, store );
        
        return new StoreOpenIdIdpSideAssociationManager( storage );
    }

    /**
     * Constructor.
     * 
     * @param storage /the storage for the OpenIdIdpSideAssociations
     */
    protected StoreOpenIdIdpSideAssociationManager(
            CachingMap<String,OpenIdIdpSideAssociation> storage )
    {
        super( storage );
    }
}
