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

import java.net.Inet4Address;
import org.infogrid.lid.openid.OpenIdIdpSideAssociation;
import org.infogrid.lid.openid.AbstractOpenIdIdpSideAssociationManager;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.CachingMap;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;

/**
 * An OpenIdIdpSideAssociationManager implemented using Store.
 */
public class StoreOpenIdIdpSideAssociationManager
        extends
            AbstractOpenIdIdpSideAssociationManager
{
    private static final Log log = Log.getLogInstance( StoreOpenIdIdpSideAssociationManager.class ); // our own, private logger

    /**
     * Factory method to create a stateful association manager.
     * 
     * @param store the Store to use
     * @return the created StoreOpenIdIdpSideAssociationManager
     */
    public static StoreOpenIdIdpSideAssociationManager createSmart(
            Store store )
    {
        String prefix = theResourceHelper.getResourceStringOrNull( "SmartAssociationHandlePrefix" );
        if( prefix == null ) {
            prefix = getDefaultPrefix( "s" );
        }
        long associationDuration = theResourceHelper.getResourceLongOrDefault( "SmartAssociationDuration", 30L * 24L * 60L * 60L * 1000L ); // one month

        return create( prefix, associationDuration, store );
    }

    /**
     * Factory method to create a stateless association manager.
     *
     * @param store the Store to use
     * @return the created StoreOpenIdIdpSideAssociationManager
     */
    public static StoreOpenIdIdpSideAssociationManager createDumb(
            Store store )
    {
        String prefix = theResourceHelper.getResourceStringOrNull( "DumbAssociationHandlePrefix" );
        if( prefix == null ) {
            prefix = getDefaultPrefix( "s" );
        }
        long associationDuration = theResourceHelper.getResourceLongOrDefault( "DumbAssociationDuration", 30L * 24L * 60L * 60L * 1000L ); // one month
        return create( prefix, associationDuration, store );
    }

    /**
     * Factory method to create an association manager.
     *
     * @param associationHandlePrefix prefix for the generated association handles
     * @param associationDuration validity period for new associations
     * @param store the Store to use
     * @return the created StoreOpenIdIdpSideAssociationManager
     */
    public static StoreOpenIdIdpSideAssociationManager create(
            String associationHandlePrefix,
            long   associationDuration,
            Store  store )
    {
        OpenIdIdpSideAssociationMapper                              mapper  = OpenIdIdpSideAssociationMapper.create();
        StoreBackedSwappingHashMap<String,OpenIdIdpSideAssociation> storage = StoreBackedSwappingHashMap.createWeak( mapper, store );

        return new StoreOpenIdIdpSideAssociationManager( associationHandlePrefix, associationDuration, storage );
    }

    /**
     * Constructor.
     * 
     * @param associationHandlePrefix prefix for the generated association handles
     * @param associationDuration validity period for new associations
     * @param storage storage for the associations
     */
    protected StoreOpenIdIdpSideAssociationManager(
            String                                      associationHandlePrefix,
            long                                        associationDuration,
            CachingMap<String,OpenIdIdpSideAssociation> storage )
    {
        super( associationHandlePrefix, associationDuration, storage );
    }

    /**
     * Obtain a default prefix.
     *
     * @param modifier modifier to distinguish smart from dumb associations
     * @return default prefix
     */
    protected static String getDefaultPrefix(
            String modifier )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "infogrid.org-" );
        try {
            buf.append( Inet4Address.getLocalHost().getHostAddress() );
            buf.append( "-" );

        } catch( Exception ex ) {
            log.error( ex );
        }
        buf.append( modifier );
        buf.append( "-" );
        return buf.toString();
    }

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( StoreOpenIdIdpSideAssociationManager.class );
}
