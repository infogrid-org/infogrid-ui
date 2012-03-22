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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.rest.net.local.defaultapp.store;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.naming.NamingException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.defaultapp.AbstractRestfulAppInitializationFilter;
import org.infogrid.jee.rest.net.NetRestfulJeeFormatter;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.defaultapp.AppInitializationException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.DefaultNetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.IterableNetMeshBase;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.local.store.IterableLocalNetStoreMeshBase;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.store.IterableStore;
import org.infogrid.util.AbstractQuitListener;
import org.infogrid.util.NamedThreadFactory;
import org.infogrid.util.QuitManager;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.text.StringRepresentationDirectory;

/**
 * Common functionality of application initialization filters that are net-enabled and REST-ful.
 */
public abstract class AbstractStoreNetLocalRestfulAppInitializationFilter
        extends
            AbstractRestfulAppInitializationFilter
{
    /**
     * Constructor.
     */
    protected AbstractStoreNetLocalRestfulAppInitializationFilter()
    {
        // nothing
    }

    /**
     * <p>Perform initialization.</p>
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @throws Throwable something bad happened that cannot be fixed by re-invoking this method
     */
    protected void initialize(
            ServletRequest  request,
            ServletResponse response )
        throws
            Throwable
    {
        HttpServletRequest realRequest = (HttpServletRequest) request;
        SaneRequest        saneRequest = SaneServletRequest.create( realRequest );
        
        InfoGridWebApp app        = getInfoGridWebApp();
        Context        appContext = app.getApplicationContext();

        // ModelBase
        ModelBase modelBase = ModelBaseSingleton.getSingleton();
        appContext.addContextObject( modelBase );

        // NetMeshBaseIdentifierFactory
        NetMeshBaseIdentifierFactory meshBaseIdentifierFactory = createNetMeshBaseIdentifierFactory();
        appContext.addContextObject( meshBaseIdentifierFactory );

        // Main MeshBase
        NetMeshBaseIdentifier mbId = (NetMeshBaseIdentifier) determineMainMeshBaseIdentifier( saneRequest, meshBaseIdentifierFactory );

        IterableLocalNetStoreMeshBase mb = setupMeshBase( saneRequest, mbId, modelBase, app );

        if( mb != null ) {
            appContext.addContextObject( mb );
            // MeshBase adds itself to QuitManager

            // Name Server
            MeshBaseNameServer nameServer = mb.getLocalNameServer();
            appContext.addContextObject( nameServer );
        }

        if( mb != null ) {
            initializeContextObjects( saneRequest, appContext );
        } else {
            try {
                initializeContextObjects( saneRequest, appContext );
            } catch( Throwable t ) {
                // ignore
            }
        }
    }

    /**
     * Overridable default implementation for how to setup a MeshBase.
     *
     * @param saneRequest the incoming request, so it is possible to determine hostname, context etc.
     * @param mbId the MeshBaseIdentifier for the MeshBase
     * @param modelBase the ModelBase for the MeshBase
     * @param app the InfoGridWebApp that is being set up
     * @return the set up MeshBase
     */
    protected IterableLocalNetStoreMeshBase setupMeshBase(
            SaneRequest           saneRequest,
            NetMeshBaseIdentifier mbId,
            ModelBase             modelBase,
            InfoGridWebApp        app )
        throws
            NamingException,
            IOException,
            URISyntaxException,
            AppInitializationException
    {
        IterableLocalNetStoreMeshBase meshBase = null;

        try {
            initializeDataSources();

        } finally {

            if( theMeshStore != null && theProxyStore != null && theShadowStore != null && theShadowProxyStore != null ) {
                QuitManager                  qm        = app.getApplicationContext().findContextObject( QuitManager.class );
                NetAccessManager             accessMgr = createAccessManager();
                NetMeshBaseIdentifierFactory idFact    = app.getApplicationContext().findContextObject( NetMeshBaseIdentifierFactory.class );

                ProbeDirectory probeDirectory = createAndPopulateProbeDirectory( idFact );

                final ScheduledExecutorService exec = createScheduledExecutorService();
                if( qm != null ) {
                    qm.addDirectQuitListener( new AbstractQuitListener() {
                        @Override
                        public void die()
                        {
                            exec.shutdown();
                        }
                    });
                }

                meshBase = IterableLocalNetStoreMeshBase.create(
                        mbId,
                        DefaultNetMeshObjectAccessSpecificationFactory.create(
                                mbId,
                                idFact ),
                        modelBase,
                        accessMgr,
                        theMeshStore,
                        theProxyStore,
                        theShadowStore,
                        theShadowProxyStore,
                        probeDirectory,
                        exec,
                        true,
                        app.getApplicationContext() );
                populateMeshBase( saneRequest, meshBase );
            }
        }
        return meshBase;
    }

    /**
     * Initialize the data sources.
     *
     * @throws NamingException thrown if a data source could not be found or accessed
     * @throws IOException thrown if an I/O problem occurred
     * @throws AppInitializationException thrown if the application could not be initialized
     */
    protected abstract void initializeDataSources()
            throws
                NamingException,
                IOException,
                AppInitializationException;

    /**
     * Overridable method to create the NetAccessManager to use.
     *
     * @return the created AccessManager, or null
     */
    protected NetAccessManager createAccessManager()
    {
        return null;
    }

    /**
     * Overridable method to create the NetMeshBaseIdentifierFactory appropriate for this
     * application.
     *
     * @return the created NetMeshBaseIdentifierFactory
     */
    protected NetMeshBaseIdentifierFactory createNetMeshBaseIdentifierFactory()
    {
        DefaultNetMeshBaseIdentifierFactory ret =
                DefaultNetMeshBaseIdentifierFactory.create();

        return ret;
    }

    /**
     * Overridable method to create and populate a ProbeDirectory appropriate for this
     * application.
     *
     * @param meshBaseIdentifierFactory the NetMeshBaseIdentifierFactory to us
     * @return the created and populated ProbeDirectory
     * @throws URISyntaxException thrown if an identifier could not be parsed
     */
    protected ProbeDirectory createAndPopulateProbeDirectory(
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
        throws
            URISyntaxException
    {
        ProbeDirectory ret = MProbeDirectory.create();
        return ret;
    }

    /**
     * Convenience method to avoid subclassing mistakes.
     *
     * @param incomingRequest the incoming request
     * @param mb the MeshBase to initialize
     */
    @Override
    protected void populateMeshBase(
            SaneRequest incomingRequest,
            MeshBase    mb )
    {
        populateNetMeshBase( incomingRequest, (IterableNetMeshBase) mb );
    }

    /**
     * Initialize the initial content of the NetMeshBase.
     *
     * @param incomingRequest the incoming request
     * @param mb the NetMeshBase to initialize
     */
    protected void populateNetMeshBase(
            SaneRequest         incomingRequest,
            IterableNetMeshBase mb )
    {
        // nothing on this level
    }

    /**
     * Obtain a ThreadPool. This can be overridden by subclasses.
     *
     * @return the ScheduledExecutorService
     */
    protected ScheduledExecutorService createScheduledExecutorService()
    {
        NamedThreadFactory factory = new NamedThreadFactory( getClass().getName() );

        ScheduledThreadPoolExecutor ret = new ScheduledThreadPoolExecutor( nThreads, factory );
        ret.setContinueExistingPeriodicTasksAfterShutdownPolicy( false );
        ret.setExecuteExistingDelayedTasksAfterShutdownPolicy( false );

        return ret;
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

        // Formatter
        NetMeshBase                   defaultNetMeshBase = rootContext.findContextObject( NetMeshBase.class );
        StringRepresentationDirectory srepdir            = rootContext.findContextObjectOrThrow( StringRepresentationDirectory.class );

        NetRestfulJeeFormatter formatter = NetRestfulJeeFormatter.create( defaultNetMeshBase, srepdir );
        rootContext.addContextObject( formatter );
    }

    /**
     * The Store for MeshObjects. This must be set by a subclass.
     */
    protected IterableStore theMeshStore;

    /**
     * The Store for Proxies. This must be set by a subclass.
     */
    protected IterableStore theProxyStore;

    /**
     * The Store for ShadowMeshBases. This must be set by a subclass.
     */
    protected IterableStore theShadowStore;

    /**
     * The Store for the ShadowMeshBases' Proxies. This must be set by a subclass.
     */
    protected IterableStore theShadowProxyStore;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( AbstractStoreNetLocalRestfulAppInitializationFilter.class );

    /**
     * The default number of threads to use.
     */
    protected static final int nThreads = theResourceHelper.getResourceIntegerOrDefault( "nThreads", 3 );
}
