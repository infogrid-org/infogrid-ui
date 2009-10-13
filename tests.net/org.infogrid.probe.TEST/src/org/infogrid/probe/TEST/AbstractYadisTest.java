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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.probe.TEST;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.httpd.server.HttpServer;
import org.infogrid.lid.model.lid.LidSubjectArea;
import org.infogrid.lid.model.openid.auth.AuthSubjectArea;
import org.infogrid.lid.model.yadis.YadisSubjectArea;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.Web.WebSubjectArea;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.blob.BlobProbe;
import org.infogrid.probe.m.MProbeDirectory;
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

        // Probe directory has Probe for plain text needed in YadisTest6

        theProbeDirectory.addStreamProbe( new ProbeDirectory.StreamProbeDescriptor( "text/plain", BlobProbe.class ));

        // MeshBase
        exec = createThreadPool( 1 );

        theMeshBase1 = LocalNetMMeshBase.create( here1, theModelBase, null, theProbeDirectory, exec, rootContext );
        theMeshBase2 = LocalNetMMeshBase.create( here2, theModelBase, null, theProbeDirectory, exec, rootContext );
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        try {
            Thread.sleep( 200L ); // just a bit, so ripple effects can have died down
        } catch( Throwable t ) {
            log.error( t );
        }
        
        theMeshBase1.die();
        theMeshBase1 = null;

        if( theServer != null ) {
            theServer.stop();
        }
        exec.shutdown();
    }

    /**
     * Check Yadis results if the home object of the main Probe is also an XrdsServiceCollection.
     * Common method to check for the correct results, regardless of which test was run.
     * 
     * @param home the home MeshObject corresponding to the accessed URL
     * @param nServices the number of XRDS services expected
     * @throws Exception all sorts of things may go wrong in tests
     */
    protected void checkYadisResultsDirect(
            MeshObject home,
            int        nServices )
        throws
            Exception
    {
        checkEqualsOutOfSequence(
                home.getTypes(),
                new MeshType[] {
                        WebSubjectArea.WEBRESOURCE,
                        YadisSubjectArea.XRDSSERVICECOLLECTION,
                        ProbeSubjectArea.ONETIMEONLYPROBEUPDATESPECIFICATION },
                "home object not blessed right" );

        checkYadisResultsServices( home, nServices );
    }

    /**
     * Check Yadis results if the home object of the main Probe links to a ForwardReference
     * that is also an XrdsServiceCollection.
     * Common method to check for the correct results, regardless of which test was run.
     *
     * @param home the home MeshObject corresponding to the accessed URL
     * @param nServices the number of XRDS services expected
     * @throws Exception all sorts of things may go wrong in tests
     */
    protected void checkYadisResultsIndirect(
            MeshObject home,
            int        nServices )
        throws
            Exception
    {
        checkEqualsOutOfSequence(
                home.getTypes(),
                new MeshType[] {
                        WebSubjectArea.WEBRESOURCE,
                        ProbeSubjectArea.ONETIMEONLYPROBEUPDATESPECIFICATION },
                "home object not blessed right" );

        MeshObjectSet xrdsCollection = home.traverse( YadisSubjectArea.WEBRESOURCE_HASXRDSLINKTO_WEBRESOURCE.getSource() );

        checkEquals( xrdsCollection.size(), 1, "Wrong set of link destinations" );

        checkEqualsOutOfSequence(
                xrdsCollection.get( 0 ).getTypes(),
                new MeshType[] {
                        WebSubjectArea.WEBRESOURCE,
                        ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION,
                        YadisSubjectArea.XRDSSERVICECOLLECTION },
                "xrds collection not blessed" );

        checkYadisResultsServices( xrdsCollection.get( 0 ), nServices );
    }

    /**
     * Check the content of the XrdsCollection for the right Yadis services.
     *
     * @param xrdsCollection the XrdsCollection collection object
     * @param nServices the number of XRDS services expected
     */
    protected void checkYadisResultsServices(
            MeshObject xrdsCollection,
            int        nServices )
    {
        MeshObjectSet services = xrdsCollection.traverse( YadisSubjectArea.XRDSSERVICECOLLECTION_COLLECTS_XRDSSERVICE.getSource() );
        
        checkEquals( services.size(), nServices, "Wrong number of services found" );

        for( MeshObject service : services ) {
            
            getLog().debug( "Looking at Yadis service " + service );

            boolean found = false;
            for( EntityType type : service.getTypes() ) {
                if( type.isSubtypeOfOrEquals( YadisSubjectArea.XRDSSERVICE )) {
                    found = true;
                    break;
                }
            }
            if( !found ) {
                reportError( "Service is not a XRDSSERVICE" );
            }
            if( ArrayHelper.isIn( LidSubjectArea.MINIMUMLID2, service.getTypes(), false )) {
                // good
            } else if( ArrayHelper.isIn( AuthSubjectArea.AUTHENTICATION1DOT0SERVICE, service.getTypes(), false )) {
                // good
            } else {
                // not good
                reportError( "Service is neither MinimumLid nor OpenID Auth", service );
            }
            
            MeshObjectSet endpoints = service.traverse( YadisSubjectArea.XRDSSERVICE_ISPROVIDEDAT_ENDPOINT.getSource() );
            checkEquals( endpoints.size(), 1, "wrong number of endpoints" );
            
            for( MeshObject endpoint : endpoints ) {
                getLog().debug( "Looking at Endpoint " + endpoint );

                checkCondition( endpoint.isBlessedBy( YadisSubjectArea.ENDPOINT ), "endpoint not blessed" );

                MeshObjectSet reverseServices = endpoint.traverse( YadisSubjectArea.XRDSSERVICE_ISPROVIDEDAT_ENDPOINT.getDestination() );
                checkEquals( reverseServices.size(), 1, "Wrong number of reverse services" );

                MeshObjectSet resources = endpoint.traverse( YadisSubjectArea.ENDPOINT_ISOPERATEDBY_WEBRESOURCE.getSource() );
                checkEquals( resources.size(), 1, "wrong number of resources" );

                for( MeshObject resource : resources ) {
                    getLog().debug( "Looking at Resource " + resource );

                    checkCondition( resource.isBlessedBy( WebSubjectArea.WEBRESOURCE ), "endpoint not blessed" );

                    // needs more sophisticated test for reverse -- they only get replicated in over time (FIXME?)
                    // MeshObjectSet reverseEndpoints = resource.traverse( YadisSubjectArea.ENDPOINT_ISOPERATEDBY_WEBRESOURCE.getDestination() );
                    // checkEquals( reverseEndpoints.size(), nServices, "Wrong number of reverse endpoints" );
                }
            }
        }
    }

    /**
     * Check that there are no Yadis services.
     *
     * @param home the home MeshObject corresponding to the accessed URL
     * @throws Exception all sorts of things may go wrong in tests
     */
    protected void checkNoYadisResults(
            MeshObject home )
        throws
            Exception
    {
        checkCondition( !home.isBlessedBy( YadisSubjectArea.XRDSSERVICECOLLECTION ), "unexpectedly is a XrdsServiceCollection" );

        MeshObjectSet xrdsCollection = home.traverse( YadisSubjectArea.WEBRESOURCE_HASXRDSLINKTO_WEBRESOURCE.getSource() );
        checkEquals( xrdsCollection.size(), 0, "Unexpected associated Xrds WebResource" );
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
    protected ScheduledExecutorService exec;

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    /**
     * The first test NetMeshBase.
     */
    protected LocalNetMMeshBase theMeshBase1;

    /**
     * The second test NetMeshBase.
     */
    protected LocalNetMMeshBase theMeshBase2;

    /**
     * The identifier of the first test NetMeshBase.
     */
    protected static final NetMeshBaseIdentifier here1;

    /**
     * The identifier of the second test NetMeshBase.
     */
    protected static final NetMeshBaseIdentifier here2;
    static {
        NetMeshBaseIdentifier temp1;
        NetMeshBaseIdentifier temp2;
        try {
            temp1 = theMeshBaseIdentifierFactory.fromExternalForm( "http://here1.local/" ); // this is not going to work for communications
            temp2 = theMeshBaseIdentifierFactory.fromExternalForm( "http://here2.local/" ); // this is not going to work for communications
        } catch( Exception ex ) {
            log.error( ex );
            temp1 = null; // make compiler happy
            temp2 = null; // make compiler happy
        }
        here1 = temp1;
        here2 = temp2;
    }

    /**
     * Flag that enables us to switch between expecting Yadis information and not.
     */
    protected static boolean theWithYadis;
    
    /**
     * Root identifier of the web server.
     */
    protected static final String WEB_SERVER_IDENTIFIER = "http://localhost:" + SERVER_PORT + "/";

    /**
     * Identifier of the identity.
     */
    protected static final String IDENTITY_LOCAL_IDENTIFIER = "identity";

    /**
     * Identifier of the XRDS file.
     */
    protected static final String XRDS_LOCAL_IDENTIFIER = "xrds";

    /**
     * Identifier of the identity.
     */
    protected static final String IDENTITY_IDENTIFIER = WEB_SERVER_IDENTIFIER + IDENTITY_LOCAL_IDENTIFIER;

    /**
     * Identifier of the XRDS file.
     */
    protected static final String XRDS_IDENTIFIER = WEB_SERVER_IDENTIFIER + XRDS_LOCAL_IDENTIFIER;

    /**
     * The NetMeshBaseIdentifier of the first test file.
     */
    protected static final NetMeshBaseIdentifier theIdentityIdentifier;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = theMeshBaseIdentifierFactory.fromExternalForm( IDENTITY_IDENTIFIER );
        } catch( Throwable t ) {
            log.error( t );
        }
        theIdentityIdentifier = temp;
    }

    /**
     * The XRDS data to be returned.
     */
    protected static final String XRDS
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<XRDS xmlns=\"xri://$xrds\" xmlns:xrd=\"xri://$xrd*($v*2.0)\">\n"
            + " <xrd:XRD>\n"
            + "  <xrd:Service priority=\"1\">\n"
            + "   <xrd:Type>http://lid.netmesh.org/minimum-lid/2.0b10</xrd:Type>\n"
            + "   <xrd:URI>" + IDENTITY_IDENTIFIER + "</xrd:URI>\n"
            + "  </xrd:Service>\n"
            + "  <xrd:Service priority=\"9\">\n"
            + "   <xrd:Type>http://openid.net/signon/1.0</xrd:Type>\n"
            + "   <xrd:URI>" + IDENTITY_IDENTIFIER + "</xrd:URI>\n"
            + "   <openid:Delegate xmlns:openid=\"http://openid.net/xmlns/1.0\">" + IDENTITY_IDENTIFIER + "</openid:Delegate>\n"
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
            + "  <meta http-equiv='x-xrds-location' content='" + XRDS_IDENTIFIER + "'>\n"
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
            + "  <link rel=\"openid.server\" href=\"" + IDENTITY_IDENTIFIER + "\"\n"
            + " </head>\n"
            + " <body>\n"
            + "  <h1>Test file</h1>\n"
            + " </body>\n"
            + "</html>\n";
}
