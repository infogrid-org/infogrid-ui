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

package org.infogrid.meshworld.net;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.infogrid.jee.rest.net.local.defaultapp.store.AbstractStoreNetLocalRestfulAppInitializationFilter;
import org.infogrid.store.sql.mysql.MysqlStore;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.naming.NamingReportingException;
import org.infogrid.viewlet.ViewletFactory;

/**
 * Initializes application-level functionality.
 */
public class NetMeshWorldAppInitializationFilter
        extends
            AbstractStoreNetLocalRestfulAppInitializationFilter
{
    /**
     * Constructor for subclasses only, use factory method.
     */
    public NetMeshWorldAppInitializationFilter()
    {
        // nothing right now
    }

    /**
     * Initialize the data sources.
     * 
     * @throws NamingReportingException thrown if a data source could not be found or accessed
     */
    @Override
    protected void initializeDataSources()
            throws
                NamingReportingException            
    {
        String         name = "java:comp/env/jdbc/netmeshworldDB";
        InitialContext ctx  = null;
        try {
            // Database access via JNDI
            ResourceHelper rh = ResourceHelper.getInstance( NetMeshWorldAppInitializationFilter.class );

            ctx                      = new InitialContext();
            DataSource theDataSource = (DataSource) ctx.lookup( name );

            theMeshStore        = MysqlStore.create( theDataSource, rh.getResourceStringOrDefault( "MeshObjectTable",  "MeshObjects" ));
            theProxyStore       = MysqlStore.create( theDataSource, rh.getResourceStringOrDefault( "ProxyStoreTable",  "Proxies"       ));
            theShadowStore      = MysqlStore.create( theDataSource, rh.getResourceStringOrDefault( "ShadowTable",      "Shadows"       ));
            theShadowProxyStore = MysqlStore.create( theDataSource, rh.getResourceStringOrDefault( "ShadowProxyTable", "ShadowProxies" ));
            theFormTokenStore   = MysqlStore.create( theDataSource, rh.getResourceStringOrDefault( "FormTokenTable",   "FormTokens"  ));

        } catch( NamingException ex ) {
            throw new NamingReportingException( name, ctx, ex );
        }
    }

    /**
     * Initialize context objects.
     * 
     * @param context the Context
     */
    @Override
    protected void initializeContextObjects(
            Context context )
    {
        super.initializeContextObjects( context );

        ViewletFactory vlFact = new NetMeshWorldViewletFactory();
        context.addContextObject( vlFact );
    }
}
