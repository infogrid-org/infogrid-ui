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

package org.infogrid.meshworld.net;

import java.io.BufferedInputStream;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.net.NetRestfulJeeFormatter;
import org.infogrid.jee.security.store.StoreFormTokenService;
import org.infogrid.jee.templates.DefaultStructuredResponseTemplateFactory;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.local.store.IterableLocalNetStoreMeshBase;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.model.primitives.text.SimpleModelPrimitivesStringRepresentationDirectory;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.ModuleRequirement;
import org.infogrid.module.SoftwareInstallation;
import org.infogrid.module.servlet.ServletBootLoader;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.store.IterableStore;
import org.infogrid.store.sql.mysql.MysqlStore;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.logging.Log;
import org.infogrid.util.logging.log4j.Log4jLog;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.viewlet.ViewletFactory;

/**
 * NetMeshWorldApp application class.
 */
public class NetMeshWorldApp
        extends
            InfoGridWebApp
{
    private static Log log; // do not initialize here ... in factory method later
    
    /**
     * Factory method.
     *
     * @param defaultMeshBaseIdentifier String form of tthe NetMeshBaseIdentifier of the default NetMeshBase
     * @return the created NetMeshWorldApp
     * @throws Exception something went wrong and initialization was not possible
     */
    public static NetMeshWorldApp create(
            String defaultMeshBaseIdentifier )
        throws
            Exception
    {
        final String ROOT_MODULE_NAME = "org.infogrid.meshworld.net";
        
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
        log = Log.getLogInstance( NetMeshWorldApp.class );            
        // first resource helper, then logger
        String nameOfResourceHelperFile = NetMeshWorldApp.class.getName();
        String nameOfLog4jConfigFile    = NetMeshWorldApp.class.getName().replace( '.', '/' ) + "Log.properties";
        try {
            ResourceHelper.setApplicationResourceBundle( ResourceBundle.getBundle(
                    nameOfResourceHelperFile,
                    Locale.getDefault(),
                    NetMeshWorldApp.class.getClassLoader() ));

        } catch( Exception ex ) {
            System.err.println( "Unexpected Exception attempting to load " + nameOfResourceHelperFile );
            ex.printStackTrace( System.err );
            throw new RuntimeException( ex );
        }

        try {
            Properties logProperties = new Properties();
            logProperties.load( new BufferedInputStream(
                    NetMeshWorldApp.class.getClassLoader().getResourceAsStream( nameOfLog4jConfigFile )));

            Log4jLog.configure( logProperties );
            // which logger is being used is defined in the module dependency declaration through parameters

        } catch( Exception ex ) {
            System.err.println( "Unexpected Exception attempting to load " + nameOfLog4jConfigFile );
            ex.printStackTrace( System.err );
            throw new RuntimeException( ex );
        }

        ResourceHelper.initializeLogging();

        log = Log.getLogInstance( NetMeshWorldApp.class );

        ResourceHelper theResourceHelper = ResourceHelper.getInstance( NetMeshWorldApp.class );

        // Context
        SimpleContext rootContext = SimpleContext.createRoot( ROOT_MODULE_NAME + " root context" );
        rootContext.addContextObject( theThisModule.getModuleRegistry() );

        // ModelBase
        ModelBase modelBase = ModelBaseSingleton.getSingleton();

        // Database access via JNDI
        
        InitialContext ctx           = new InitialContext();
        DataSource     theDataSource = (DataSource) ctx.lookup( "java:comp/env/jdbc/netmeshworldDB" );        

        IterableStore meshStore        = MysqlStore.create( theDataSource, theResourceHelper.getResourceString( "MeshObjectTable" ) );
        IterableStore proxyStore       = MysqlStore.create( theDataSource, theResourceHelper.getResourceString( "ProxyStoreTable" ));
        IterableStore shadowStore      = MysqlStore.create( theDataSource, theResourceHelper.getResourceString( "ShadowTable" ) );
        IterableStore shadowProxyStore = MysqlStore.create( theDataSource, theResourceHelper.getResourceString( "ShadowProxyTable" ));
        IterableStore formTokenStore   = MysqlStore.create( theDataSource, theResourceHelper.getResourceString( "FormTokenTable" ) );

        meshStore.initializeIfNecessary();
        proxyStore.initializeIfNecessary();
        shadowStore.initializeIfNecessary();
        shadowProxyStore.initializeIfNecessary();
        formTokenStore.initializeIfNecessary();

        // NetMeshBaseIdentifierFactory
        NetMeshBaseIdentifierFactory meshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.create();
        rootContext.addContextObject( meshBaseIdentifierFactory );
        
        // Only one MeshBase
        NetMeshBaseIdentifier mbId;
        try {
            mbId = meshBaseIdentifierFactory.fromExternalForm( defaultMeshBaseIdentifier );

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
                meshStore,
                proxyStore,
                shadowStore,
                shadowProxyStore,
                probeDirectory,
                exec,
                true,
                rootContext );
        rootContext.addContextObject( meshBase );

        MeshBaseNameServer nameServer = meshBase.getLocalNameServer();
        rootContext.addContextObject( nameServer );
        
        // FormTokenService
        StoreFormTokenService formTokenService = StoreFormTokenService.create( formTokenStore );
        rootContext.addContextObject( formTokenService );

        // ViewletFactory and utils
        StringRepresentationDirectory srepdir = SimpleModelPrimitivesStringRepresentationDirectory.create();
        rootContext.addContextObject( srepdir );

        ViewletFactory vlFact = new NetMeshWorldViewletFactory();
        rootContext.addContextObject( vlFact );
        
        NetRestfulJeeFormatter formatter = NetRestfulJeeFormatter.create();
        rootContext.addContextObject( formatter );

        // StructuredResponseTemplateFactory
        StructuredResponseTemplateFactory tmplFact = DefaultStructuredResponseTemplateFactory.create();
        rootContext.addContextObject( tmplFact );
        
        // finally, obtain the application
        NetMeshWorldApp ret = new NetMeshWorldApp( rootContext );
        return ret;
    }

    /**
     * Constructor, to be invoked by factory method only.
     *
     * @param applicationContext the main application Context
     */
    @SuppressWarnings(value={"unchecked"})
    protected NetMeshWorldApp(
            SimpleContext applicationContext )
    {
        super( applicationContext );
    }
}
