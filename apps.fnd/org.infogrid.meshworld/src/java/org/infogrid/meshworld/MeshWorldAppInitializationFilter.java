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

package org.infogrid.meshworld;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.infogrid.jee.rest.defaultapp.store.AbstractStoreRestfulAppInitializationFilter;
import org.infogrid.store.sql.mysql.MysqlStore;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.naming.NamingReportingException;
import org.infogrid.viewlet.ViewletFactory;

/**
 * Initializes application-level functionality.
 */
public class MeshWorldAppInitializationFilter
        extends
            AbstractStoreRestfulAppInitializationFilter
{
    /**
     * Constructor.
     */
    public MeshWorldAppInitializationFilter()
    {
        // nothing
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
        String         name = "java:comp/env/jdbc/meshworldDB";
        InitialContext ctx  = null;
        try {
            // Database access via JNDI
            ResourceHelper rh = ResourceHelper.getInstance( MeshWorldAppInitializationFilter.class );

            ctx                      = new InitialContext();
            DataSource theDataSource = (DataSource) ctx.lookup( name );

            theMeshStore      = MysqlStore.create( theDataSource, rh.getResourceStringOrDefault( "MeshObjectTable", "MeshObjects" ));
            theFormTokenStore = MysqlStore.create( theDataSource, rh.getResourceStringOrDefault( "FormTokenTable",  "FormTokens"  ));

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
        ViewletFactory vlFact = new MeshWorldViewletFactory();
        context.addContextObject( vlFact );
    }
}

