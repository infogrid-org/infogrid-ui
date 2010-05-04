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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.meshworld;

import java.io.IOException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.defaultapp.store.AbstractStoreRestfulAppInitializationFilter;
import org.infogrid.jee.templates.DefaultStructuredResponseTemplateFactory;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.jee.templates.defaultapp.AppInitializationException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.traversal.xpath.XpathTraversalTranslator;
import org.infogrid.store.m.MStore;
import org.infogrid.store.sql.mysql.MysqlStore;
import org.infogrid.util.CompoundException;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.http.SaneRequest;
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
     * @throws NamingException thrown if a data source could not be found or accessed
     * @throws IOException thrown if an I/O problem occurred
     * @throws AppInitializationException thrown if the application could not be initialized
     */
    protected void initializeDataSources()
            throws
                NamingException,
                IOException,
                AppInitializationException
    {
        String         name    = "java:comp/env/jdbc/meshworlddb";
        InitialContext ctx     = null;
        Throwable      toThrow = null;

        try {
            // Database access via JNDI
            ResourceHelper rh = ResourceHelper.getInstance( MeshWorldAppInitializationFilter.class );

            ctx                      = new InitialContext();
            DataSource theDataSource = (DataSource) ctx.lookup( name );

            theMeshStore      = MysqlStore.create( theDataSource, rh.getResourceStringOrDefault( "MeshObjectTable", "MeshObjects" ));
            theFormTokenStore = MysqlStore.create( theDataSource, rh.getResourceStringOrDefault( "FormTokenTable",  "FormTokens"  ));

            theMeshStore.initializeIfNecessary();
            theFormTokenStore.initializeIfNecessary();

        } catch( NamingException ex ) {
            toThrow = new NamingReportingException( name, ctx, ex );

        } catch( IOException ex ) {
            toThrow = ex;

        } catch( Throwable ex ) {
            toThrow = ex;
        }

        if( toThrow != null ) {
            theMeshStore = MStore.create();
            theMeshStore.initializeIfNecessary();

            theFormTokenStore = MStore.create();
            theFormTokenStore.initializeIfNecessary();

            throw new AppInitializationException(
                    new CompoundException(
                            new InMemoryOnlyException(),
                            toThrow ));
        }
    }

    /**
     * Initialize the context objects. This may be overridden by subclasses.
     *
     * @param incomingRequest the incoming request
     * @param rootContext the root Context
     * @throws Exception initialization may fail
     */
    @Override
    protected void initializeContextObjects(
            SaneRequest incomingRequest,
            Context     rootContext )
        throws
            Exception
    {
        super.initializeContextObjects( incomingRequest, rootContext );

        MeshBase mb = rootContext.findContextObjectOrThrow( MeshBase.class );
        rootContext.addContextObject( XpathTraversalTranslator.create( mb ));

        SimpleContext iframeContext = SimpleContext.create( rootContext, "iframe" ); // making rootContext a parent allows us to delegate automatically if not found locally
        InfoGridWebApp.getSingleton().getContextDirectory().addContext( iframeContext );

        ViewletFactory mainVlFact   = new MainMeshWorldViewletFactory();
        ViewletFactory iframeVlFact = new IframeMeshWorldViewletFactory();

        rootContext.addContextObject( mainVlFact );
        iframeContext.addContextObject( iframeVlFact );

        StructuredResponseTemplateFactory iframeRtFact
                = DefaultStructuredResponseTemplateFactory.create(
                        "default-iframe",
                        DefaultStructuredResponseTemplateFactory.DEFAULT_MIME_TYPE );
        iframeContext.addContextObject( iframeRtFact );
    }
}
