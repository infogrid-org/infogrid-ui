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

package org.infogrid.jee.net.TESTAPP;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.net.NetRestfulJeeFormatter;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.FormTokenService;
import org.infogrid.jee.security.m.MFormTokenService;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.jee.templates.DefaultStructuredResponseTemplateFactory;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.text.MeshStringRepresentationContext;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.NetMeshObjectAccessException;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.WritableProbeDirectory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.viewlet.ViewletFactory;

/**
 * Initializes application-level functionality
 */
public class TESTAPPInitializationFilter
        implements
            Filter
{
    private Log log = Log.getLogInstance( TESTAPPInitializationFilter.class );
    
    /**
     * Constructor for subclasses only, use factory method.
     */
    public TESTAPPInitializationFilter()
    {
        // nothing right now
    }

    /**
     * Execute the filter.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    public void doFilter(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        synchronized( TESTAPPInitializationFilter.class ) {
            if( !isInitialized ) {
                initialize( request, response );
                isInitialized = true;
            }
        }
        StringRepresentationContext stringRepContext
                = (StringRepresentationContext) request.getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );
        MeshBase mb
                = InfoGridWebApp.getSingleton().getApplicationContext().findContextObject( MeshBase.class );
        
        if( stringRepContext != null && mb != null ) {
            stringRepContext.put( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY, mb );
        }
            
        chain.doFilter( request, response );
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
        InfoGridWebApp     app        = InfoGridWebApp.getSingleton();
        Context            appContext = app.getApplicationContext();
        
        if( theDefaultMeshBaseIdentifier == null ) {
            SaneServletRequest lidRequest = SaneServletRequest.create( (HttpServletRequest) request );
            
            theDefaultMeshBaseIdentifier = lidRequest.getAbsoluteContextUri();
        }
        
        try {
            // ModelBase
            ModelBase modelBase = ModelBaseSingleton.getSingleton();

            // In-memory MeshBases only.

            // NetMeshBaseIdentifier
            NetMeshBaseIdentifier mbId = NetMeshBaseIdentifier.create( theDefaultMeshBaseIdentifier );

            // AccessManager
            NetAccessManager accessMgr = null;

            WritableProbeDirectory probeDirectory = MProbeDirectory.create();
            populateProbeDirectory( probeDirectory );
            
            ScheduledExecutorService exec = Executors.newScheduledThreadPool( 2 );

            // MeshBase
            LocalNetMMeshBase meshBase = LocalNetMMeshBase.create(
                    mbId,
                    modelBase,
                    accessMgr,
                    probeDirectory,
                    exec,
                    120000L, // 2 min
                    appContext );
            appContext.addContextObject( meshBase );

            populateMeshBase( meshBase );
            
            MeshBaseNameServer nameServer = meshBase.getLocalNameServer();
            appContext.addContextObject( nameServer );

            // FormTokenService
            FormTokenService formTokenService = MFormTokenService.create();
            appContext.addContextObject( formTokenService );

            // ViewletFactory and utils
            ViewletFactory vlFact = new TESTAPPViewletFactory();
            appContext.addContextObject( vlFact );

            NetRestfulJeeFormatter formatter = NetRestfulJeeFormatter.create();
            appContext.addContextObject( formatter );

            // StructuredResponseTemplateFactory
            StructuredResponseTemplateFactory tmplFact = DefaultStructuredResponseTemplateFactory.create();
            appContext.addContextObject( tmplFact );
        
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
     * Initialize the Filter.
     *
     * @param filterConfig the Filter configuration object
     * @throws ServletException thrown if misconfigured
     */
    public void init(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        theFilterConfig  = filterConfig;
        
        theDefaultMeshBaseIdentifier = theFilterConfig.getInitParameter( DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER );
        if( theDefaultMeshBaseIdentifier != null && theDefaultMeshBaseIdentifier.length() == 0 ) {
            throw new ServletException( "Filter configuration in web.xml must specify non-empty " + DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER );
        }
    }

    /**
     * Destroy method for this Filter.
     */
    public void destroy()
    {
        // noop
    }
    
    /**
     * Put some test data into the MeshBase.
     * 
     * @param mb the MeshBase
     * @throws MeshObjectIdentifierNotUniqueException coding error
     * @throws NetMeshObjectAccessException coding error
     * @throws RelatedAlreadyException coding error
     * @throws NotPermittedException coding error
     * @throws URISyntaxException coding error
     */
    protected void populateMeshBase(
            NetMeshBase mb )
        throws
            URISyntaxException,
            MeshObjectIdentifierNotUniqueException,
            NetMeshObjectAccessException,
            RelatedAlreadyException,
            NotPermittedException
    {
        NetMeshBaseLifecycleManager    life = mb.getMeshBaseLifecycleManager();
        NetMeshObjectIdentifierFactory fact = mb.getMeshObjectIdentifierFactory();

        NetMeshObject [] resolved = new NetMeshObject[ toAccess.length ];
        for( int i=0 ; i<resolved.length ; ++i ) {
            resolved[i] = mb.accessLocally( toAccess[i] );
            resolved[i].traverseToNeighborMeshObjects(); // get the related objects into the main MeshBase, too
        }
        
        Transaction tx = null;
        try {
            NetMeshObject a = life.createMeshObject( fact.fromExternalForm( "#a" ));
            NetMeshObject b = life.createMeshObject( fact.fromExternalForm( "#b" ));
            NetMeshObject c = life.createMeshObject( fact.fromExternalForm( "#c" ));
            
            a.relate( b );
            c.relate( mb.getHomeObject() );
            
            for( int i=0 ; i<resolved.length ; ++i ) {
                if( resolved[i] != null ) {
                    c.relate( resolved[i] );
                }
            }
            
            
        } catch( TransactionException ex ) {
            log.error( ex );
        } finally {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }
    }
    
    /**
     * Put the right Probes into the ProbeDirectory.
     * 
     * @param dir the ProbeDirectory
     * @throws URISyntaxException coding error
     */
    protected void populateProbeDirectory(
            WritableProbeDirectory dir )
        throws
            URISyntaxException
    {
        toAccess = new NetMeshBaseIdentifier[] {
            NetMeshBaseIdentifier.fromExternalForm( "custom://example.com/" ),
            NetMeshBaseIdentifier.fromExternalForm( "custom://example.org/a/?foo=bar&argl=brgl" ),
        };

        for( NetMeshBaseIdentifier current : toAccess ) {
            dir.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor( current.toExternalForm(), TestProbe1.class ));
        }
    }
    
    /**
     * The filter configuration object this Filter is associated with.
     */
    protected FilterConfig theFilterConfig = null;    

    /**
     * Have the Stores been successfully initialized.
     */
    protected boolean isInitialized = false;
    
    /**
     * The default MeshBaseIdentifier, in String form,
     */
    protected String theDefaultMeshBaseIdentifier;

    /**
     * THe identifiers of the Probe home objects to be accessLocally'd.
     */
    protected NetMeshBaseIdentifier [] toAccess;

    /**
     * Name of the String in the RequestContext that contains the identifier of the default
     * MeshBase.
     */
    public static final String DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER = "DefaultMeshBaseIdentifier";
}
