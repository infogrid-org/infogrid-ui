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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.meshworld;

import java.io.IOException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.defaultapp.store.AbstractStoreRestfulAppInitializationFilter;
import org.infogrid.jee.templates.defaultapp.AppInitializationException;
import org.infogrid.jee.viewlet.DefaultJeeMeshObjectsToViewFactory;
import org.infogrid.jee.viewlet.JeeMeshObjectsToViewFactory;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.security.AccessManager;
import org.infogrid.meshbase.store.IterableStoreMeshBase;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.model.traversal.xpath.XpathTraversalTranslator;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.module.SharedSpace;
import org.infogrid.store.m.MStore;
import org.infogrid.store.sql.mysql.MysqlStore;
import org.infogrid.util.CompoundException;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.naming.NamingReportingException;
import org.infogrid.viewlet.ViewletFactory;

/**
 * Initializes application-level functionality.
 */
public class AppInitializationFilter
        extends
            AbstractStoreRestfulAppInitializationFilter
{
    /**
     * Constructor.
     */
    public AppInitializationFilter()
    {
        // nothing
    }

    /**
     * This overrides the default implementation with one in which the created MeshBase is being
     * registered in the SharedSpace, using its identifier as a name
     *
     * @param saneRequest the incoming request, so it is possible to determine hostname, context etc.
     * @param mbId the MeshBaseIdentifier for the MeshBase
     * @param modelBase the ModelBase for the MeshBase
     * @param app the InfoGridWebApp being created
     * @return the set up MeshBase
     */
    @Override
    protected MeshBase setupMeshBase(
            final SaneRequest        saneRequest,
            final MeshBaseIdentifier mbId,
            final ModelBase          modelBase,
            final InfoGridWebApp     app )
    {
        MeshBase mb = (MeshBase) SharedSpace.checkoutObject(
                app,
                mbId.toExternalForm(),
                new SharedSpace.Factory() {
                        public Object create()
                                throws Throwable
                        {
                            IterableStoreMeshBase meshBase = null;

                            try {
                                initializeDataSources();

                            } finally {

                                if( theMeshStore != null ) {
                                    AccessManager accessMgr = createAccessManager();

                                    meshBase = IterableStoreMeshBase.create( mbId, modelBase, accessMgr, theMeshStore, app.getApplicationContext() );
                                    populateMeshBase( saneRequest, meshBase );
                                }
                            }
                            return meshBase;
                        }
                },
                new SharedSpace.LastReturned() {
                        public void lastReturned(
                                String name,
                                Object sharedObject )
                        {
                            IterableStoreMeshBase meshBase = (IterableStoreMeshBase) sharedObject;
                            if( meshBase != null ) {
                                meshBase.die();
                            }
                        }
                } );
        return mb;
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
        String         name    = "java:comp/env/jdbc/org.infogrid.meshworld";
        InitialContext ctx     = null;
        Throwable      toThrow = null;

        try {
            // Database access via JNDI
            ResourceHelper rh = ResourceHelper.getInstance( AppInitializationFilter.class );

            ctx                      = new InitialContext();
            DataSource theDataSource = (DataSource) ctx.lookup( name );

            theMeshStore = MysqlStore.create( theDataSource, rh.getResourceStringOrDefault( "MeshObjectTable", "MeshObjects" ));
            theMeshStore.initializeIfNecessary();

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

        MeshBaseIdentifierFactory mbIdentifierFact = rootContext.findContextObject( MeshBaseIdentifierFactory.class );
        MeshBaseNameServer        mbNameServer     = rootContext.findContextObject( MeshBaseNameServer.class );

        TraversalTranslator translator = XpathTraversalTranslator.create( mb );
        rootContext.addContextObject( translator );

        ViewletFactory mainVlFact = new MainViewletFactory();
        rootContext.addContextObject( mainVlFact );

        @SuppressWarnings("unchecked")
        JeeMeshObjectsToViewFactory toViewFact = DefaultJeeMeshObjectsToViewFactory.create(
                mb.getIdentifier(),
                mbIdentifierFact,
                mbNameServer,
                translator,
                incomingRequest.getContextPath(),
                incomingRequest.getAbsoluteContextUri(),
                rootContext );
        rootContext.addContextObject( toViewFact );
    }
}
