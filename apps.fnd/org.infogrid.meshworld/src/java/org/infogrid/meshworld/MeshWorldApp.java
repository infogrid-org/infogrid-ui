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

import java.io.BufferedInputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.security.store.StoreFormTokenService;
import org.infogrid.jee.templates.DefaultStructuredResponseTemplateFactory;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.mesh.text.MeshStringRepresentationContext;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.security.AccessManager;
import org.infogrid.meshbase.store.IterableStoreMeshBase;
import org.infogrid.model.primitives.text.SimpleModelPrimitivesStringRepresentationDirectory;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.ModuleRequirement;
import org.infogrid.module.SoftwareInstallation;
import org.infogrid.module.servlet.ServletBootLoader;
import org.infogrid.store.sql.AbstractSqlStore;
import org.infogrid.store.sql.mysql.MysqlStore;
import org.infogrid.util.MNameServer;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.logging.Log;
import org.infogrid.util.logging.log4j.Log4jLog;
import org.infogrid.util.text.SimpleStringRepresentationContext;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.viewlet.ViewletFactory;

/**
 * MeshWorldApp application class.
 */
public class MeshWorldApp
        extends
            InfoGridWebApp
{
    private static Log log; // do not initialize here ... in factory method later
    
    /**
     * Factory method.
     *
     * @param defaultMeshBaseIdentifier String form of tthe MeshBaseIdentifier of the default MeshBase
     * @return the created MeshWorldApp
     * @throws Exception something went wrong and initialization was not possible
     */
    public static MeshWorldApp create(
            String defaultMeshBaseIdentifier )
        throws
            Exception
    {
        final String ROOT_MODULE_NAME = "org.infogrid.meshworld";
        
        Properties properties = new Properties();
        properties.put( "rootmodule", ROOT_MODULE_NAME );

        Module theThisModule;
        if( SoftwareInstallation.getSoftwareInstallation() == null ) {
            theThisModule = ServletBootLoader.initialize( properties );
        } else {
            ModuleRequirement req      = ModuleRequirement.create1( ROOT_MODULE_NAME );
            ModuleRegistry    registry = ServletBootLoader.getModuleRegistry();

           theThisModule = registry.resolve( registry.determineSingleResolutionCandidate( req )); // we know it is there
         }
        log = Log.getLogInstance( MeshWorldApp.class );            
        // first resource helper, then logger
        String nameOfResourceHelperFile = MeshWorldApp.class.getName();
        String nameOfLog4jConfigFile    = MeshWorldApp.class.getName().replace( '.', '/' ) + "Log.properties";
        try {
            ResourceHelper.setApplicationResourceBundle( ResourceBundle.getBundle(
                    nameOfResourceHelperFile,
                    Locale.getDefault(),
                    MeshWorldApp.class.getClassLoader() ));

        } catch( Exception ex ) {
            System.err.println( "Unexpected Exception attempting to load " + nameOfResourceHelperFile );
            ex.printStackTrace( System.err );
            throw new RuntimeException( ex );
        }

        try {
            Properties logProperties = new Properties();
            logProperties.load( new BufferedInputStream(
                    MeshWorldApp.class.getClassLoader().getResourceAsStream( nameOfLog4jConfigFile )));

            Log4jLog.configure( logProperties );
            // which logger is being used is defined in the module dependency declaration through parameters

        } catch( Exception ex ) {
            System.err.println( "Unexpected Exception attempting to load " + nameOfLog4jConfigFile );
            ex.printStackTrace( System.err );
            throw new RuntimeException( ex );
        }

        ResourceHelper.initializeLogging();

        log = Log.getLogInstance( MeshWorldApp.class );

        ResourceHelper theResourceHelper = ResourceHelper.getInstance( MeshWorldApp.class );

        // Context
        SimpleContext rootContext = SimpleContext.createRoot( ROOT_MODULE_NAME + " root context" );
        rootContext.addContextObject( theThisModule.getModuleRegistry() );

        // Database access via JNDI

        InitialContext ctx           = new InitialContext();
        DataSource     theDataSource = (DataSource) ctx.lookup( "java:comp/env/jdbc/meshworldDB" );        

        AbstractSqlStore meshStore      = MysqlStore.create( theDataSource, theResourceHelper.getResourceString( "MeshObjectTable" ) );
        AbstractSqlStore formTokenStore = MysqlStore.create( theDataSource, theResourceHelper.getResourceString( "FormTokenTable" ) );

        meshStore.initializeIfNecessary();
        formTokenStore.initializeIfNecessary();

        // ModelBase
        ModelBase modelBase = ModelBaseSingleton.getSingleton();
        rootContext.addContextObject( modelBase );
        
        // Only one MeshBase
        MeshBaseIdentifier mbId;
        try {
            mbId = MeshBaseIdentifier.create( defaultMeshBaseIdentifier );

        } catch( URISyntaxException ex ) {
            throw new RuntimeException( ex );
        }
        
        // AccessManager
        AccessManager accessMgr = null;

        IterableStoreMeshBase meshBase = IterableStoreMeshBase.create( mbId, modelBase, accessMgr, meshStore, rootContext    );
        rootContext.addContextObject( meshBase );

        // Name Server
        MNameServer<MeshBaseIdentifier,MeshBase> nameServer = MNameServer.create();
        nameServer.put( mbId, meshBase );
        rootContext.addContextObject( nameServer );

        // FormTokenService
        StoreFormTokenService formTokenService = StoreFormTokenService.create( formTokenStore );
        rootContext.addContextObject( formTokenService );
        
        // ViewletFactory and utils
        
        StringRepresentationDirectory srepdir = SimpleModelPrimitivesStringRepresentationDirectory.create();
        rootContext.addContextObject( srepdir );

        ViewletFactory vlFact = new MeshWorldViewletFactory();
        rootContext.addContextObject( vlFact );
        
        RestfulJeeFormatter formatter = RestfulJeeFormatter.create();
        rootContext.addContextObject( formatter );
        
        // StructuredResponseTemplateFactory
        StructuredResponseTemplateFactory tmplFact = DefaultStructuredResponseTemplateFactory.create();
        rootContext.addContextObject( tmplFact );
        
        // finally, create the application
        MeshWorldApp ret = new MeshWorldApp( rootContext );
        return ret;
    }

    /**
     * Constructor, to be invoked by factory method only.
     *
     * @param applicationContext the main application Context
     */
    protected MeshWorldApp(
            SimpleContext applicationContext )
    {
        super( applicationContext );
    }

    /**
     *  Factory method to create the right subtype MeshBaseIdentifier.
     * 
     * @param stringForm the String representation of the MeshBaseIdentifier
     * @return suitable subtype of MeshBaseIdentifier
     * @throws URISyntaxException thrown if a syntax error occurred
     */
    public MeshBaseIdentifier createMeshBaseIdentifier(
            String stringForm )
        throws
            URISyntaxException
    {
        MeshBaseIdentifier ret = MeshBaseIdentifier.create( stringForm );
        return ret;
    }

    /**
     * Construct a StringRepresentationContext appropriate for this application. This may
     * be overridden by subclasses.
     * 
     * @param request the incoming request
     * @return the created StringRepresentationContext
     */
    @Override
    public StringRepresentationContext constructStringRepresentationContext(
            HttpServletRequest request )
    {
        MeshBase meshBase = theApplicationContext.findContextObjectOrThrow( MeshBase.class );
        
        HashMap<String,Object> contextObjects = new HashMap<String,Object>();
        contextObjects.put( StringRepresentationContext.WEB_CONTEXT_KEY, request.getContextPath() );
 
        contextObjects.put( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY, meshBase );
        StringRepresentationContext ret = SimpleStringRepresentationContext.create( contextObjects );
        return ret;
    }

}
