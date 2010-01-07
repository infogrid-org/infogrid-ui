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

package org.infogrid.meshbase.net.proxy;

import java.util.ArrayList;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.util.CachingMap;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.FactoryCreatedObject;
import org.infogrid.util.PatientSmartFactory;

/**
 * Knows how to manage Proxies.
 */
public class ProxyManager
        extends
            PatientSmartFactory<NetMeshBaseIdentifier,Proxy,CoherenceSpecification>
{
    /**
     * Factory method.
     * 
     * @param proxyFactory the underlying factory of Proxies
     * @param storage the storage for the Proxies managed by this ProxyManager
     * @return the created ProxyManager
     */
    public static ProxyManager create(
            ProxyFactory                            proxyFactory,
            CachingMap<NetMeshBaseIdentifier,Proxy> storage )
    {
        return new ProxyManager( proxyFactory, storage );
    }
    
    /**
     * Constructor.
     *
     * @param proxyFactory the underlying factory of Proxies
     * @param storage the storage for the Proxies managed by this ProxyManager
     */
    protected ProxyManager(
            ProxyFactory                            proxyFactory,
            CachingMap<NetMeshBaseIdentifier,Proxy> storage )
    {
        super( proxyFactory, storage );
    }
    
    /**
     * Obtain the set of current Proxies.
     *
     * @return the set of Proxies
     */
    public CursorIterator<Proxy> proxies()
    {
        CursorIterator<Proxy> ret = theKeyValueMap.valuesIterator( NetMeshBaseIdentifier.class, Proxy.class );

        return ret;
    }
    
    /**
      * Invoked to tell this ProxyManager it is not needed any more.
      * 
      * @param isPermanent if true, this MeshBase will go away permanmently; if false, it may come alive again some time later
      */
    public void die(
            boolean isPermanent )
    {
        // Need to copy, otherwise we get ConcurrentModificationExceptions
        ArrayList<Proxy> temp = new ArrayList<Proxy>(); // don't specify size() as an argument, it may be MAX_VALUE because it is unknown
        
        synchronized( this ) {
            for( NetMeshBaseIdentifier current : keySet() ) {
                Proxy p = get( current );
                temp.add( p );
            }
        }
        for( Proxy p : temp ) {
            p.die( isPermanent );
            factoryCreatedObjectUpdated( p );
            if( isPermanent ) {
                remove( p.getPartnerMeshBaseIdentifier() );
            }
        }
    }
    
    /**
     * Method invoked by a Proxy created by this ProxyManager that the Proxy has been updated. This
     * allows the ProxyManager to write changes back to disk, for example. This is overridden here
     * to allow for easier debugging.
     * 
     * @param updatedProxy the Proxy that was updated
     */
    @Override
    public void factoryCreatedObjectUpdated(
            FactoryCreatedObject<NetMeshBaseIdentifier,Proxy,CoherenceSpecification> updatedProxy )
    {
        super.factoryCreatedObjectUpdated( updatedProxy );
    }

    /**
     * Obtain the Proxies held by this ProxyManager in externalized form.
     *
     * @return the Proxies in externalized form
     */
    public synchronized ExternalizedProxy [] externalizedProxies()
    {
        ExternalizedProxy [] ret = new ExternalizedProxy[ size() ];
        
        int i=0;
        for( NetMeshBaseIdentifier current : keySet() ) {
            Proxy p = get( current );
            ret[i++] = p.asExternalized();
        }
        return ret;
    }
}
