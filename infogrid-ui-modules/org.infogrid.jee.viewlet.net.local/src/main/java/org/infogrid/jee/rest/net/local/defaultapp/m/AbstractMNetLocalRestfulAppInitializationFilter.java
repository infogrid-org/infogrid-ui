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

package org.infogrid.jee.rest.net.local.defaultapp.m;

import java.text.ParseException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.defaultapp.AbstractRestfulAppInitializationFilter;
import org.infogrid.jee.rest.net.NetRestfulJeeFormatter;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.DefaultNetMeshObjectAccessSpecificationFactory;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.m.MProbeDirectory;
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
public abstract class AbstractMNetLocalRestfulAppInitializationFilter
        extends
            AbstractRestfulAppInitializationFilter
{
    /**
     * Constructor.
     */
    protected AbstractMNetLocalRestfulAppInitializationFilter()
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

        InfoGridWebApp app        = getInfoGridWebApp();
        Context        appContext = app.getApplicationContext();
        QuitManager    qm         = appContext.findContextObject( QuitManager.class );

        try {
            // ModelBase
            ModelBase modelBase = ModelBaseSingleton.getSingleton();
            appContext.addContextObject( modelBase );

            // NetMeshBaseIdentifierFactory
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory = createNetMeshBaseIdentifierFactory();
            appContext.addContextObject( meshBaseIdentifierFactory );

            // Main MeshBase
            NetMeshBaseIdentifier mbId = (NetMeshBaseIdentifier) determineMainMeshBaseIdentifier( saneRequest, meshBaseIdentifierFactory );

            // AccessManager
            NetAccessManager accessMgr = createAccessManager();

            ProbeDirectory probeDirectory = createAndPopulateProbeDirectory(
                    meshBaseIdentifierFactory );

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

            // MeshBase
            LocalNetMMeshBase meshBase = LocalNetMMeshBase.create(
                    mbId,
                    DefaultNetMeshObjectAccessSpecificationFactory.create(
                            mbId,
                            meshBaseIdentifierFactory ),
                    modelBase,
                    accessMgr,
                    probeDirectory,
                    exec,
                    appContext );

            populateMeshBase( saneRequest, meshBase );
            appContext.addContextObject( meshBase );
            // MeshBase adds itself to QuitManager

            MeshBaseNameServer nameServer = meshBase.getLocalNameServer();
            appContext.addContextObject( nameServer );

            // ViewletFactory and utils

            initializeContextObjects( saneRequest, appContext );

        } catch( Throwable t ) {

            StructuredResponse structured = (StructuredResponse) request.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
            if( structured != null ) {
                structured.reportProblem( t );
            } else {
                throw new ServletException( t );
            }
        }
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
     * Overridable method to create and populate a ProbeDirectory apporpriate for this
     * application.
     *
     * @param meshBaseIdentifierFactory the NetMeshBaseIdentifierFactory to us
     * @return the created and populated ProbeDirectory
     * @throws ParseException thrown if an identifier could not be parsed
     */
    protected ProbeDirectory createAndPopulateProbeDirectory(
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
        throws
            ParseException
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
        populateNetMeshBase( incomingRequest, (NetMeshBase) mb );
    }

    /**
     * Initialize the initial content of the NetMeshBase.
     *
     * @param incomingRequest the incoming request
     * @param mb the NetMeshBase to initialize
     */
    protected void populateNetMeshBase(
            SaneRequest incomingRequest,
            NetMeshBase mb )
    {
        // nothing on this level
    }

    /**
     * Overridable method to create the AccessManager to use.
     *
     * @return the created AccessManager, or null
     */
    protected NetAccessManager createAccessManager()
    {
        return null;
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
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( AbstractMNetLocalRestfulAppInitializationFilter.class );

    /**
     * The default number of threads to use.
     */
    protected static final int nThreads = theResourceHelper.getResourceIntegerOrDefault( "nThreads", 3 );
}
