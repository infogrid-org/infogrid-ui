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

import java.io.IOException;
import java.net.URISyntaxException;
import javax.naming.InitialContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.store.StoreFormTokenService;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.mesh.text.MeshStringRepresentationContext;
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
import org.infogrid.store.Store;
import org.infogrid.store.sql.mysql.MysqlStore;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.viewlet.ViewletFactory;

/**
 * Initializes application-level functionality.
 */
public class MeshWorldAppInitializationFilter
        implements
            Filter
{
    /**
     * Constructor for subclasses only, use factory method.
     */
    public MeshWorldAppInitializationFilter()
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
        synchronized( MeshWorldAppInitializationFilter.class ) {
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
        HttpServletRequest realRequest = (HttpServletRequest) request;
        SaneRequest        saneRequest = SaneServletRequest.create( realRequest );
        
        InfoGridWebApp app        = InfoGridWebApp.getSingleton();
        Context        appContext = app.getApplicationContext();
        
        ResourceHelper theResourceHelper = ResourceHelper.getInstance( getClass() );
        try {
            // Database access via JNDI
            InitialContext ctx           = new InitialContext();
            DataSource     theDataSource = (DataSource) ctx.lookup( "java:comp/env/jdbc/meshworldDB" );        

            IterableStore meshStore      = MysqlStore.create( theDataSource, theResourceHelper.getResourceStringOrDefault( "MeshObjectTable", "MeshObjects" ));
            IterableStore formTokenStore = MysqlStore.create( theDataSource, theResourceHelper.getResourceStringOrDefault( "FormTokenTable",  "FormTokens"  ));

            meshStore.initializeIfNecessary();
            formTokenStore.initializeIfNecessary();

            // ModelBase
            ModelBase modelBase = ModelBaseSingleton.getSingleton();
            appContext.addContextObject( modelBase );

            // MeshBaseIdentifierFactory
            MeshBaseIdentifierFactory meshBaseIdentifierFactory = DefaultMeshBaseIdentifierFactory.create();
            appContext.addContextObject( meshBaseIdentifierFactory );

            if( theDefaultMeshBaseIdentifier == null ) {
                theDefaultMeshBaseIdentifier = saneRequest.getAbsoluteBaseUri();
            }
            
            // Only one MeshBase
            MeshBaseIdentifier mbId;
            try {
                mbId = meshBaseIdentifierFactory.fromExternalForm( theDefaultMeshBaseIdentifier );

            } catch( URISyntaxException ex ) {
                throw new RuntimeException( ex );
            }

            // AccessManager
            AccessManager accessMgr = null;

            IterableStoreMeshBase meshBase = IterableStoreMeshBase.create( mbId, modelBase, accessMgr, meshStore, appContext );
            appContext.addContextObject( meshBase );

            // Name Server
            MMeshBaseNameServer<MeshBaseIdentifier,MeshBase> nameServer = MMeshBaseNameServer.create();
            nameServer.put( mbId, meshBase );
            appContext.addContextObject( nameServer );

            // FormTokenService
            StoreFormTokenService formTokenService = StoreFormTokenService.create( formTokenStore );
            appContext.addContextObject( formTokenService );

//        // ViewletFactory and utils

            ViewletFactory vlFact = new MeshWorldViewletFactory();
            appContext.addContextObject( vlFact );

            RestfulJeeFormatter formatter = RestfulJeeFormatter.create();
            appContext.addContextObject( formatter );

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
        
        theDefaultMeshBaseIdentifier = filterConfig.getInitParameter( DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER_NAME );
    }
    
    /**
     * Destroy method for this Filter.
     */
    public void destroy()
    {
        // noop
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
     * The Store for OpenID Associations. This must be set by a subclass.
     */
    protected Store theOpenIdAssociationStore;
    
    /**
     * The Store for form tokens. This must be set by a subclass.
     */
    protected Store theFormTokenStore;
    
    /**
     * The Store for nonces. This must be set by a subclass.
     */
    protected Store theNonceStore;
    
    /**
     * The Store for sessions. This must be set by a subclass.
     */
    protected Store theSessionStore;
    
    /**
     * Identifier of the main MeshBase.
     */
    protected String theDefaultMeshBaseIdentifier;

    /**
     * Name of the Filter parameter in web.xml that contains the identifier of the main MeshBase.
     */
    public static final String DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER_NAME = "DefaultMeshBaseIdentifier";
}

