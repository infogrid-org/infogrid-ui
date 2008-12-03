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

package org.infogrid.jee.rest.net.local.defaultapp.store;

import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.defaultapp.AbstractRestfulAppInitializationFilter;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.FormTokenService;
import org.infogrid.jee.security.m.MFormTokenService;
import org.infogrid.jee.security.store.StoreFormTokenService;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.local.store.IterableLocalNetStoreMeshBase;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.store.IterableStore;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Common functionality of application initialization filters that are net-enabled and REST-ful.
 */
public abstract class AbstractStoreNetLocalRestfulAppInitializationFilter
        extends
            AbstractRestfulAppInitializationFilter
{
    private static final Log log = Log.getLogInstance( AbstractStoreNetLocalRestfulAppInitializationFilter.class  ); // our own, private logger

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
     * @throws ServletException something bad happened that cannot be fixed by re-invoking this method
     */
    protected void initialize(
            ServletRequest  request,
            ServletResponse response )
        throws
            ServletException
    {
        HttpServletRequest realRequest = (HttpServletRequest) request;
        SaneRequest        saneRequest = SaneServletRequest.create( realRequest );
        
        InfoGridWebApp app        = InfoGridWebApp.getSingleton();
        Context        appContext = app.getApplicationContext();
        
        ResourceHelper theResourceHelper = ResourceHelper.getInstance( getClass() );
        try {
            initializeDataSources();

            theMeshStore.initializeIfNecessary();
            theProxyStore.initializeIfNecessary();
            theShadowStore.initializeIfNecessary();
            theShadowProxyStore.initializeIfNecessary();
            theFormTokenStore.initializeIfNecessary();

            // ModelBase
            ModelBase modelBase = ModelBaseSingleton.getSingleton();
            appContext.addContextObject( modelBase );

            // NetMeshBaseIdentifierFactory
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.create();
            appContext.addContextObject( meshBaseIdentifierFactory );

            if( theDefaultMeshBaseIdentifier == null ) {
                theDefaultMeshBaseIdentifier = saneRequest.getAbsoluteBaseUri();
            }

            // Only one MeshBase
            NetMeshBaseIdentifier mbId;
            try {
                mbId = meshBaseIdentifierFactory.fromExternalForm( theDefaultMeshBaseIdentifier );

            } catch( URISyntaxException ex ) {
                throw new RuntimeException( ex );
            }
            
            // AccessManager
            NetAccessManager accessMgr = null; // NetMeshWorldAccessManager.obtain();


            ProbeDirectory probeDirectory = MProbeDirectory.create();
            ScheduledExecutorService exec = Executors.newScheduledThreadPool( 2 );

            // MeshBase
            IterableLocalNetStoreMeshBase meshBase = IterableLocalNetStoreMeshBase.create(
                    mbId,
                    modelBase,
                    accessMgr,
                    theMeshStore,
                    theProxyStore,
                    theShadowStore,
                    theShadowProxyStore,
                    probeDirectory,
                    exec,
                    true,
                    appContext );
            populateMeshBase( meshBase );
            appContext.addContextObject( meshBase );

            MeshBaseNameServer nameServer = meshBase.getLocalNameServer();
            appContext.addContextObject( nameServer );

            // FormTokenService
            StoreFormTokenService formTokenService = StoreFormTokenService.create( theFormTokenStore );
            appContext.addContextObject( formTokenService );

            // ViewletFactory and utils
            
            initializeContextObjects( appContext );

        } catch( Throwable t ) {

            StructuredResponse structured = (StructuredResponse) request.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
            if( structured != null ) {
                structured.reportProblem( t );
            } else {
                throw new ServletException( t );
            }
        }
        
        // want some kind of FormTokenService even if initialization failed
        if( appContext.findContextObject( FormTokenService.class ) == null ) {
            MFormTokenService formTokenService = MFormTokenService.create();
            appContext.addContextObject( formTokenService );
        }
    }

    /**
     * Initialize the data sources.
     * 
     * @throws NamingException thrown if a data source could not be found or accessed
     */
    protected abstract void initializeDataSources()
            throws
                NamingException;

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
     * The Store for form tokens. This must be set by a subclass.
     */
    protected IterableStore theFormTokenStore;
}
