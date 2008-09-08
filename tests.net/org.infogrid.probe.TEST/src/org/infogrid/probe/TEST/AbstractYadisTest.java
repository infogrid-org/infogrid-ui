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

package org.infogrid.probe.TEST;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.httpd.server.HttpServer;
import org.infogrid.lid.model.lid.LidSubjectArea;
import org.infogrid.lid.model.openid.OpenidSubjectArea;
import org.infogrid.lid.model.yadis.Service;
import org.infogrid.lid.model.yadis.Site;
import org.infogrid.lid.model.yadis.YadisSubjectArea;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpointFactory;
import org.infogrid.meshbase.net.proxy.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.probe.manager.PassiveProbeManager;
import org.infogrid.probe.manager.m.MPassiveProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.probe.shadow.m.MShadowMeshBaseFactory;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;

/**
 * Factors out the commonalities of the Yadis tests.
 */
public abstract class AbstractYadisTest
        extends
            AbstractProbeTest
{
    private static final Log log = Log.getLogInstance( AbstractYadisTest.class ); // our own, private logger

    /**
     * Constructor.
     * 
     * @param testClass the class to test
     * @param factory the factory for HttpResponses
     * @throws IOException thrown if the HttpServer could not be started
     */
    protected AbstractYadisTest(
            Class               testClass,
            HttpResponseFactory factory )
        throws
            IOException
    {
        super( testClass );

        theServer = new HttpServer( SERVER_PORT, NUMBER_THREADS );
        theServer.setResponseFactory( factory );

        // start the server
        theServer.start();

        ProxyMessageEndpointFactory endpointsFactory = MPingPongNetMessageEndpointFactory.create( exec );
        
        ShadowMeshBaseFactory theShadowFactory
                = MShadowMeshBaseFactory.create( theModelBase, endpointsFactory, theProbeDirectory, -1L, rootContext );
        
        theProbeManager1 = MPassiveProbeManager.create( theShadowFactory );
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        if( theServer != null ) {
            theServer.stop();
        }
        exec.shutdown();

        theProbeManager1 = null;
    }

    /**
     * Common method to check for the correct results, regardless of which test was run.
     * 
     * @param home the home MeshObject corresponding to the accessed URL
     * @param nServices the number of XRDS services expected
     * @throws Exception thrown if an Exception occurred during the test
     */
    protected void checkYadisResults(
            MeshObject home,
            int        nServices )
        throws
            Exception
    {
        checkEqualsOutOfSequence(
                home.getTypes(),
                new MeshType[] { YadisSubjectArea.SITE, ProbeSubjectArea.ONETIMEONLYPROBEUPDATESPECIFICATION },
                "home object blessed" );
        
        MeshObjectSet services = home.traverseToNeighborMeshObjects();
        
        checkEquals( services.size(), nServices, "Wrong number of services found" );

        for( MeshObject service : services ) {
            
            getLog().debug( "Looking at Yadis service " + service );

            boolean found = false;
            for( EntityType type : service.getTypes() ) {
                if( type.isSubtypeOfOrEquals( Service._TYPE )) {
                    found = true;
                    break;
                }
            }
            if( !found ) {
                reportError( "Service is not a Service.TYPE" );
            }
            if( ArrayHelper.isIn( LidSubjectArea.MINIMUMLID2, service.getTypes(), false )) {
                // good
            } else if( ArrayHelper.isIn( OpenidSubjectArea.AUTHENTICATION1_0SERVICE, service.getTypes(), false )) {
                // good
            } else {
                // not good
                reportError( "Service is neither MinimumLid nor OpenID Auth" );
            }
            

            MeshObjectSet endpoints = service.traverse( Service._Service_IsProvidedAtEndpoint_Site_SOURCE );
            checkEquals( endpoints.size(), 1, "wrong number of endpoints" );
            
            for( MeshObject endpoint : endpoints ) {
                getLog().debug( "Looking at Endpoint " + endpoint );

                checkEqualsOutOfSequence( endpoint.getTypes(), new MeshType[] { Site._TYPE }, "endpoint not blessed" );
            }
        }
    }

    /**
     * Our HTTP Server.
     */
    protected HttpServer theServer;

    /**
     * The port on which we run our server.
     */
    protected static final int SERVER_PORT = 8081;

    /**
     * The number of threads to create in the server. FIXME? Should we run all tests
     * with different numbers of threads?
     */
    protected static final int NUMBER_THREADS = 0;

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

    /**
     * The NetMeshBaseIdentifier of the first test file.
     */
    protected static final NetMeshBaseIdentifier theNetworkIdentifier;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = NetMeshBaseIdentifier.create( "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + SERVER_PORT + "/" );
        } catch( Throwable t ) {
            log.error( t );
        }
        theNetworkIdentifier = temp;
    }

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    /**
     * The ProbeManager that we use for the first Probe.
     */
    protected PassiveProbeManager theProbeManager1;

    /**
     * The XRDS data to be returned.
     */
    protected static final String XRDS
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<XRDS xmlns=\"xri://$xrds\" xmlns:xrd=\"xri://$xrd*($v*2.0)\">\n"
            + " <xrd:XRD>\n"
            + "  <xrd:Service priority=\"1\">\n"
            + "   <xrd:Type>http://lid.netmesh.org/minimum-lid/2.0b10</xrd:Type>\n"
            + "   <xrd:URI>http://mylid.net/test</xrd:URI>\n"
            + "  </xrd:Service>\n"
            + "  <xrd:Service priority=\"9\">\n"
            + "   <xrd:Type>http://openid.net/signon/1.0</xrd:Type>\n"
            + "   <xrd:URI>http://mylid.net/test1</xrd:URI>\n"
            + "   <openid:Delegate xmlns:openid=\"http://openid.net/xmlns/1.0\">http://mylid.net/test2</openid:Delegate>\n"
            + "  </xrd:Service>\n"
            + " </xrd:XRD>\n"
            + "</XRDS>\n";
    
    /**
     * The HTML data to be returned.
     */
    protected static final String HTML
            = "<html>\n"
            + " <head>\n"
            + "  <title>Test file</title>\n"
            + " </head>\n"
            + " <body>\n"
            + "  <h1>Test file</h1>\n"
            + " </body>\n"
            + "</html>\n";

    /**
     * The HTML data to be returned containing the HTTP-EQUIV header.
     */
    protected static final String HTML_WITH_EQUIV
            = "<html>\n"
            + " <head>\n"
            + "  <title>Test file</title>\n"
            + "  <meta http-equiv='x-xrds-location' content='" + theNetworkIdentifier.toExternalForm() + "xrds'>\n"
            + " </head>\n"
            + " <body>\n"
            + "  <h1>Test file</h1>\n"
            + " </body>\n"
            + "</html>\n";

    /**
     * The HTML data to be returned containing the OpenID LINK REL tags.
     */
    protected static final String HTML_WITH_OPENID_LINK_REL
            = "<html>\n"
            + " <head>\n"
            + "  <title>Test file</title>\n"
            + "  <link rel=\"openid.server\" href=\"http://mylid.net/test1\"\n"
            + " </head>\n"
            + " <body>\n"
            + "  <h1>Test file</h1>\n"
            + " </body>\n"
            + "</html>\n";
}
