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

package org.infogrid.jee.rest.defaultapp.store;

import java.io.IOException;
import javax.naming.NamingException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.defaultapp.AbstractRestfulAppInitializationFilter;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.m.MFormTokenService;
import org.infogrid.jee.security.store.StoreFormTokenService;
import org.infogrid.jee.templates.defaultapp.AppInitializationException;
import org.infogrid.meshbase.DefaultMeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.meshbase.m.MMeshBaseNameServer;
import org.infogrid.meshbase.security.AccessManager;
import org.infogrid.meshbase.store.IterableStoreMeshBase;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.store.IterableStore;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * Common functionality of application initialization filters that are REST-ful and use a Store for MeshBase persistence.
 */
public abstract class AbstractStoreRestfulAppInitializationFilter
        extends
            AbstractRestfulAppInitializationFilter
{
    /**
     * Constructor.
     */
    protected AbstractStoreRestfulAppInitializationFilter()
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
        
        InfoGridWebApp app        = InfoGridWebApp.getSingleton();
        Context        appContext = app.getApplicationContext();

        // ModelBase
        ModelBase modelBase = ModelBaseSingleton.getSingleton();
        appContext.addContextObject( modelBase );

        // MeshBaseIdentifierFactory
        MeshBaseIdentifierFactory meshBaseIdentifierFactory = DefaultMeshBaseIdentifierFactory.create();
        appContext.addContextObject( meshBaseIdentifierFactory );

        // Main MeshBase
        MeshBaseIdentifier mbId = determineMainMeshBaseIdentifier( saneRequest, meshBaseIdentifierFactory );

        Throwable thrown = null; // set if data source initialization failed
        try {
            initializeDataSources();

        } catch( Throwable t ) {
            thrown = t;
            throw thrown;

        } finally {

            if( theMeshStore != null ) {
                AccessManager accessMgr = createAccessManager();

                IterableStoreMeshBase meshBase = IterableStoreMeshBase.create( mbId, modelBase, accessMgr, theMeshStore, appContext );
                populateMeshBase( saneRequest, meshBase );
                appContext.addContextObject( meshBase );
                // MeshBase adds itself to QuitManager

                // Name Server
                MMeshBaseNameServer<MeshBaseIdentifier,MeshBase> nameServer = MMeshBaseNameServer.create();
                nameServer.put( mbId, meshBase );
                appContext.addContextObject( nameServer );
            }

            if( theFormTokenStore != null ) {
                // FormTokenService
                StoreFormTokenService formTokenService = StoreFormTokenService.create( theFormTokenStore );
                appContext.addContextObject( formTokenService );

            } else {
                MFormTokenService formTokenService = MFormTokenService.create();
                appContext.addContextObject( formTokenService );
            }

            if( thrown == null ) {
                initializeContextObjects( saneRequest, appContext );
            } else {
                try {
                    initializeContextObjects( saneRequest, appContext );
                } catch( Throwable t ) {
                    // ignore
                }
            }
        }
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
     * Overridable method to create the AccessManager to use.
     *
     * @return the created AccessManager, or null
     */
    protected AccessManager createAccessManager()
    {
        return null;
    }

    /**
     * The Store for MeshObjects. This must be set by a subclass.
     */
    protected IterableStore theMeshStore;
    
    /**
     * The Store for form tokens. This must be set by a subclass.
     */
    protected IterableStore theFormTokenStore;
}
