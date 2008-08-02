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
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.infogrid.context.SimpleContext;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.security.StoreFormTokenService;
import org.infogrid.jee.viewlet.templates.DefaultStructuredResponseTemplateFactory;
import org.infogrid.jee.viewlet.templates.StructuredResponseTemplateFactory;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.store.IterableLocalNetStoreMeshBase;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.ModuleRequirement;
import org.infogrid.module.SoftwareInstallation;
import org.infogrid.module.servlet.ServletBootLoader;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.store.sql.SqlStore;
import org.infogrid.util.NameServer;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.logging.log4j.Log4jLog;
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
     * @throws NamingException thrown if a JNDI problem occurred
     * @throws URISyntaxException thrown if the defaultMeshBaseIdentifier could not be parsed
     */
    public static NetMeshWorldApp create(
            String defaultMeshBaseIdentifier )
        throws
            NamingException,
            URISyntaxException
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

            try {
                theThisModule = registry.resolve( registry.determineResolutionCandidates( req )[0] ); // we know it is there

            } catch( Exception ex ) {
                System.err.println( "Unexpected Exception attempting to re-resolve module with " + req );
                ex.printStackTrace( System.err );
                throw new RuntimeException( ex );
            }
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

        SqlStore meshStore        = SqlStore.create( theDataSource, theResourceHelper.getResourceString( "MeshObjectTable" ) );
        SqlStore proxyStore       = SqlStore.create( theDataSource, theResourceHelper.getResourceString( "ProxyStoreTable" ));
        SqlStore shadowStore      = SqlStore.create( theDataSource, theResourceHelper.getResourceString( "ShadowTable" ) );
        SqlStore shadowProxyStore = SqlStore.create( theDataSource, theResourceHelper.getResourceString( "ShadowProxyTable" ));
        SqlStore formTokenStore   = SqlStore.create( theDataSource, theResourceHelper.getResourceString( "FormTokenTable" ) );

        meshStore.initializeIfNecessary();
        proxyStore.initializeIfNecessary();
        shadowStore.initializeIfNecessary();
        shadowProxyStore.initializeIfNecessary();
        formTokenStore.initializeIfNecessary();

        // NetMeshBaseIdentifier
        NetMeshBaseIdentifier mbId = NetMeshBaseIdentifier.create( defaultMeshBaseIdentifier );

        // AccessManager
        NetAccessManager accessMgr = null; // NetMeshWorldAccessManager.create();

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
                theResourceHelper.getResourceLongOrDefault( "TimeShadowNotNeededTillExpires", 120000L ), // 2 min 
                rootContext );
        rootContext.addContextObject( meshBase );

        NameServer<NetMeshBaseIdentifier,NetMeshBase> nameServer = meshBase.getLocalNameServer();
        rootContext.addContextObject( nameServer );
        
        // FormTokenService
        StoreFormTokenService formTokenService = StoreFormTokenService.create( formTokenStore );
        rootContext.addContextObject( formTokenService );

        // ViewletFactory
        ViewletFactory vlFact = new NetMeshWorldViewletFactory();
        rootContext.addContextObject( vlFact );
        
        // StructuredResponseTemplateFactory
        StructuredResponseTemplateFactory tmplFact = DefaultStructuredResponseTemplateFactory.create();
        rootContext.addContextObject( tmplFact );
        
        // finally, create the application
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
        NetMeshBaseIdentifier ret = NetMeshBaseIdentifier.create( stringForm );
        return ret;
    }
}
