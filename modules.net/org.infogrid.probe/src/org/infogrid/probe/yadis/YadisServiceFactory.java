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

package org.infogrid.probe.yadis;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import org.infogrid.lid.model.openid.auth.AuthSubjectArea;
import org.infogrid.lid.model.yadis.YadisSubjectArea;
import org.infogrid.mesh.BlessedAlreadyException;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.Web.WebSubjectArea;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.StagingMeshBaseLifecycleManager;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class knows how to instantiate YadisService, and subclasses of YadisService, given
 * the information provided in a service node in a Yadis Capability Document DOM.
 */
public class YadisServiceFactory
    extends
        XrdsProbe
{
    private static final Log log = Log.getLogInstance( YadisServiceFactory.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param meshBaseIdentifierFactory the factory to use for MeshBaseIdentifiers
     * @param builder the DocumentBuilder to use
     */
    public YadisServiceFactory(
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory,
            DocumentBuilder              builder )
    {
        theMeshBaseIdentifierFactory = meshBaseIdentifierFactory;
        theDocumentBuilder           = builder;
    }

    /**
     * Instantiate the Services from a Yadis capability document.
     *
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param yadisCapabilityFile the content of the Yadis document
     * @param base the MeshBase in which to instantiate
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries. This should not happen.
     */
    public void addYadisServicesFromXml(
            NetMeshBaseIdentifier dataSourceIdentifier,
            String                yadisCapabilityFile,
            StagingMeshBase       base )
        throws
            TransactionException
    {
        try {
            InputSource     source  = new InputSource( new StringReader( yadisCapabilityFile ));
            Document        dom     = theDocumentBuilder.parse( source );

            addYadisServicesFromXml( dataSourceIdentifier, dom, base );

        } catch( NotPermittedException ex ) {
            log.error( ex );
        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            log.error( ex );
        } catch( IsAbstractException ex ) {
            log.error( ex );
        } catch( IllegalPropertyTypeException ex ) {
            log.error( ex );
        } catch( IllegalPropertyValueException ex ) {
            log.error( ex );
        } catch( IOException ex ) {
            log.warn( ex );
        } catch( SAXException ex ) {
            log.warn( ex );
        } catch( StringRepresentationParseException ex ) {
            log.warn( ex );
        }
    }

    /**
     * Instantiate the Services from an HTML document.
     *
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param content the content of the HTML document
     * @param base the MeshBase in which to instantiate
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries. This should not happen.
     */
    public void addYadisServicesFromHtml(
            NetMeshBaseIdentifier dataSourceIdentifier,
            String                content,
            StagingMeshBase       base )
        throws
            TransactionException
    {
        try {
            NetMeshObject subject = base.getHomeObject();
        
            addYadisServicesFromHtml( dataSourceIdentifier, content, subject, base );

        } catch( NotPermittedException ex ) {
            log.error( ex );
        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            log.error( ex );
        } catch( StringRepresentationParseException ex ) {
            log.warn( ex );
        }
    }
        
    /**
     * Instantiate the Services from an HTML document.
     *
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param content the content of the HTML document
     * @param subject the MeshObject that represents the resource being described by the HTML document
     * @param base the MeshBase in which to instantiate
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries. This should not happen.
     * @throws NotPermittedException an operation was not permitted. This should not happen.
     * @throws MeshObjectIdentifierNotUniqueException an identifier was not unique. This should not happen.
     * @throws StringRepresentationParseException a syntax error occurred
     */
    public void addYadisServicesFromHtml(
            NetMeshBaseIdentifier dataSourceIdentifier,
            String                content,
            NetMeshObject         subject,
            StagingMeshBase       base )
        throws
            TransactionException,
            NotPermittedException,
            MeshObjectIdentifierNotUniqueException,
            StringRepresentationParseException
    {
        Matcher startHeadMatcher       = startHeadPattern.matcher( content );
        Matcher endHeadMatcher         = endHeadPattern.matcher( content );
        Matcher yadisHttpEquivMatcher1 = yadisHttpEquivPattern1.matcher( content );
        Matcher yadisHttpEquivMatcher2 = yadisHttpEquivPattern2.matcher( content );
        Matcher yadisHttpEquivMatcher3 = yadisHttpEquivPattern3.matcher( content );
        Matcher yadisHttpEquivMatcher4 = yadisHttpEquivPattern4.matcher( content );

        if( startHeadMatcher.find() && endHeadMatcher.find()) {

            int startHeadStart    = startHeadMatcher.start( 1 );
            int endHeadStart      = endHeadMatcher.start( 1 );

            Matcher yadisHttpEquivMatcher = null;
            if( yadisHttpEquivMatcher == null && yadisHttpEquivMatcher1.find() ) {
                yadisHttpEquivMatcher = yadisHttpEquivMatcher1;
            }
            if( yadisHttpEquivMatcher == null && yadisHttpEquivMatcher2.find() ) {
                yadisHttpEquivMatcher = yadisHttpEquivMatcher2;
            }
            if( yadisHttpEquivMatcher == null && yadisHttpEquivMatcher3.find() ) {
                yadisHttpEquivMatcher = yadisHttpEquivMatcher3;
            }
            if( yadisHttpEquivMatcher == null && yadisHttpEquivMatcher4.find() ) {
                yadisHttpEquivMatcher = yadisHttpEquivMatcher4;
            }

            if( yadisHttpEquivMatcher != null ) {
                // don't do another 'find' -- we already have one
                try {
                    int yadisLocationStart = yadisHttpEquivMatcher.start( 1 );

                    String                yadisLocation          = yadisHttpEquivMatcher.group( 1 );
                    NetMeshBaseIdentifier yadisNetworkIdentifier = theMeshBaseIdentifierFactory.guessFromExternalForm( dataSourceIdentifier, yadisLocation );


                    yadisLocation = yadisNetworkIdentifier.getUriString();

                    if( startHeadStart < yadisLocationStart && yadisLocationStart < endHeadStart ) {

                        StagingMeshBaseLifecycleManager life = base.getMeshBaseLifecycleManager();

                        try {
                            NetMeshObject fwdRef = life.createForwardReference(
                                            yadisNetworkIdentifier,
                                            WebSubjectArea.WEBRESOURCE );

                            base.getHomeObject().relateAndBless(
                                    YadisSubjectArea.WEBRESOURCE_HASXRDSLINKTO_WEBRESOURCE.getSource(), fwdRef );

                        } catch( MeshObjectIdentifierNotUniqueException ex ) {
                            log.error( ex );
                        } catch( EntityNotBlessedException ex ) {
                            log.error( ex );
                        } catch( RelatedAlreadyException ex ) {
                            log.error( ex );
                        } catch( IsAbstractException ex ) {
                            log.error( ex );
                        } catch( NotPermittedException ex ) {
                            log.error( ex );
                        }
                    }
                } catch( StringRepresentationParseException ex ) {
                    log.warn( ex );
                }
            }

            addOpenIdYadisServicesFromHtml(
                    content,
                    startHeadStart,
                    endHeadStart,
                    subject,
                    base,
                    openId1ServerPattern1,
                    openId1ServerPattern2,
                    openId1DelegatePattern1,
                    openId1DelegatePattern2,
                    AuthSubjectArea.AUTHENTICATION1DOT0SERVICE,
                    OPENID1_0TYPEIDENTIFIER,
                    "openid1",
                    IntegerValue.create( 20 ));

            addOpenIdYadisServicesFromHtml(
                    content,
                    startHeadStart,
                    endHeadStart,
                    subject,
                    base,
                    openId2ProviderPattern1,
                    openId2ProviderPattern2,
                    openId2Local_idPattern1,
                    openId2Local_idPattern2,
                    AuthSubjectArea.AUTHENTICATION2DOT0SERVICE,
                    OPENID2_0TYPEIDENTIFIER,
                    "openid2",
                    IntegerValue.create( 10 ));
        }
    }

    /**
     * Add discovered OpenID services from Html for one version of OpenID.
     *
     * @param content the content of the HTML document
     * @param startHeadStart index into content from where the head section starts
     * @param endHeadStart index into content to where the head section ends
     * @param subject the MeshObject that represents the resource being described by the HTML document
     * @param base the MeshBase in which to instantiate
     * @param serverPattern1 the first pattern describing how to find the server tag
     * @param serverPattern2 the second pattern describing how to find the server tag
     * @param delegatePattern1 the first pattern describing how to find the delegate tag
     * @param delegatePattern2 the second pattern describing how to find the delegate tag
     * @param serviceType the EntityType to bless the XRDS service object with
     * @param serviceTypeIdentifier the PropertyValue for the XRDS service object
     * @param uniqueIdComponent unique identifier to use in creating the instances
     * @param priority the Xrds Service's priority
     * @return true if a discovery was made
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries. This should not happen.
     * @throws NotPermittedException an operation was not permitted. This should not happen.
     * @throws MeshObjectIdentifierNotUniqueException an identifier was not unique. This should not happen.
     * @throws StringRepresentationParseException a syntax error occurred
     */
    protected boolean addOpenIdYadisServicesFromHtml(
            String          content,
            int             startHeadStart,
            int             endHeadStart,
            NetMeshObject   subject,
            StagingMeshBase base,
            Pattern         serverPattern1,
            Pattern         serverPattern2,
            Pattern         delegatePattern1,
            Pattern         delegatePattern2,
            EntityType      serviceType,
            StringValue     serviceTypeIdentifier,
            String          uniqueIdComponent,
            IntegerValue    priority )
        throws
            TransactionException,
            NotPermittedException,
            MeshObjectIdentifierNotUniqueException,
            StringRepresentationParseException
    {
        // look for OpenID tags
        Matcher openIdServerMatcher1   = serverPattern1.matcher( content );
        Matcher openIdServerMatcher2   = serverPattern2.matcher( content );
        Matcher openIdDelegateMatcher1 = delegatePattern1.matcher( content );
        Matcher openIdDelegateMatcher2 = delegatePattern2.matcher( content );

        Matcher openIdServerMatcher = null;
        if( openIdServerMatcher == null && openIdServerMatcher1.find() ) {
            openIdServerMatcher = openIdServerMatcher1;
        }
        if( openIdServerMatcher == null && openIdServerMatcher2.find() ) {
            openIdServerMatcher = openIdServerMatcher2;
        }
        // make sure it is in the right place
        if( openIdServerMatcher != null ) {
            int openIdServerStart = openIdServerMatcher.start( 1 );

            // String identityServer = openIdServerMatcher.group( 1 );
            NetMeshBaseIdentifier identityServerIdentifier = theMeshBaseIdentifierFactory.guessFromExternalForm( openIdServerMatcher.group( 1 ) );
            NetMeshBaseIdentifier delegateIdentifier = null;

            try {
                if( startHeadStart < openIdServerStart && openIdServerStart < endHeadStart ) {

                    // look for optional delegate tag
                    Matcher openIdDelegateMatcher = null;
                    if( openIdServerMatcher == null && openIdDelegateMatcher1.find() ) {
                        openIdDelegateMatcher = openIdDelegateMatcher1;
                    }
                    if( openIdServerMatcher == null && openIdDelegateMatcher2.find() ) {
                        openIdDelegateMatcher = openIdDelegateMatcher2;
                    }
                    if( openIdDelegateMatcher != null && openIdDelegateMatcher.find() ) {
                        int openIdDelegateStart = openIdDelegateMatcher.start();
                        if( startHeadStart < openIdDelegateStart && openIdDelegateStart < endHeadStart ) {
                            String delegateUrl = openIdDelegateMatcher.group( 1 );

                            delegateIdentifier = theMeshBaseIdentifierFactory.guessFromExternalForm( delegateUrl );
                        }
                    }
                }
            } catch( StringRepresentationParseException ex ) {
                log.warn( ex );
            }

            try {
                subject.bless( YadisSubjectArea.XRDSSERVICECOLLECTION );

            } catch( BlessedAlreadyException ex ) {
                // happens if both OpenID V1 and V2 are supported
                
            } catch( IsAbstractException ex ) {
                log.error( ex );
            }

            try {
                NetMeshObject serviceMeshObject = base.getMeshBaseLifecycleManager().createMeshObject(
                        base.getMeshObjectIdentifierFactory().fromExternalForm( "YadisService-" + uniqueIdComponent ),
                        YadisSubjectArea.XRDSSERVICE );

                serviceMeshObject.bless( serviceType );
                serviceMeshObject.setPropertyValue( YadisSubjectArea.XRDSSERVICE_PRIORITY, priority );

                if( delegateIdentifier != null ) {
                    serviceMeshObject.setPropertyValue(
                            AuthSubjectArea.AUTHENTICATIONSERVICE_DELEGATE,
                            StringValue.create( delegateIdentifier.toExternalForm() ));
                }

                NetMeshObject serviceMeshObjectType = base.getMeshBaseLifecycleManager().createMeshObject(
                        base.getMeshObjectIdentifierFactory().fromExternalForm( "YadisService- " + uniqueIdComponent + "-type-0" ),
                        YadisSubjectArea.XRDSSERVICETYPE );
                serviceMeshObjectType.setPropertyValue( YadisSubjectArea.XRDSSERVICETYPE_SERVICETYPEIDENTIFIER, serviceTypeIdentifier );

                serviceMeshObject.relateAndBless( YadisSubjectArea.XRDSSERVICE_HASTYPE_XRDSSERVICETYPE.getSource(), serviceMeshObjectType );

                NetMeshObject endpoint = base.getMeshBaseLifecycleManager().createMeshObject(
                        base.getMeshObjectIdentifierFactory().fromExternalForm( "Endpoint-" + uniqueIdComponent ),
                        YadisSubjectArea.ENDPOINT );
                // endpoint.setPropertyValue( ServiceEndPoint.URI_PROPERTYTYPE, StringValue.obtain( identityServer ));

                serviceMeshObject.relateAndBless( YadisSubjectArea.XRDSSERVICE_ISPROVIDEDAT_ENDPOINT.getSource(), endpoint );

                NetMeshObject resource = findOrCreateForwardReferenceAndBless(
                        base.getMeshObjectIdentifierFactory().fromExternalForm( identityServerIdentifier, null ),
                        WebSubjectArea.WEBRESOURCE,
                        base );

                if( !endpoint.isRelated( resource )) {
                    endpoint.relate( resource );
                }
                endpoint.blessRelationship(
                        YadisSubjectArea.ENDPOINT_ISOPERATEDBY_WEBRESOURCE.getSource(),
                        resource );

                if( !serviceMeshObject.isRelated( subject )) {
                    serviceMeshObject.relate( subject );
                }
                serviceMeshObject.blessRelationship(
                        YadisSubjectArea.XRDSSERVICECOLLECTION_COLLECTS_XRDSSERVICE.getDestination(),
                        subject );

            } catch( IsAbstractException ex ) {
                log.error( ex );
            } catch( EntityBlessedAlreadyException ex ) {
                log.error( ex );
            } catch( EntityNotBlessedException ex ) {
                log.error( ex );
            } catch( IllegalPropertyTypeException ex ) {
                log.error( ex );
            } catch( IllegalPropertyValueException ex ) {
                log.error( ex );
            } catch( RelatedAlreadyException ex ) {
                log.error( ex );
            } catch( NotRelatedException ex ) {
                log.error( ex );
            } catch( RoleTypeBlessedAlreadyException ex ) {
                log.error( ex );
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * The XML DocumentBuilder to use.
     */
    protected DocumentBuilder theDocumentBuilder;

    /**
     * Factory for MeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory;

    /**
     * The pattern that helps us find the beginning the HTML head section.
     */
    private static final Pattern startHeadPattern = Pattern.compile(
            ".*(<head).*",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );

    /**
     * The pattern that helps us find the end of the HTML head section.
     */
    private static final Pattern endHeadPattern = Pattern.compile(
            ".*(</head).*",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );

    /**
     * The pattern that helps us find the openid.server tag for OpenID V1.
     */
    private static final Pattern openId1ServerPattern1 = Pattern.compile(
            "<link[^>]+rel=[\"']?openid.server[\"'\\s][^>]*href=[\"']?([^\\s\"']*)[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );
    private static final Pattern openId1ServerPattern2 = Pattern.compile(
            "<link[^>]+href=[\"']?([^\\s\"']*)[\"'\\s][^>]*rel=[\"']?openid.server[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );

    /**
     * The pattern that helps us find the openid2.provider tag for OpenID V2.
     */
    private static final Pattern openId2ProviderPattern1 = Pattern.compile(
            "<link[^>]+rel=[\"']?openid2.provider[\"'\\s][^>]*href=[\"']?([^\\s\"']*)[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );
    private static final Pattern openId2ProviderPattern2 = Pattern.compile(
            "<link[^>]+href=[\"']?([^\\s\"']*)[\"'\\s][^>]*rel=[\"']?openid2.provider[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );

    /**
     * The pattern that helps us find the openid.delegate tag for OpenID V1.
     */
    private static final Pattern openId1DelegatePattern1 = Pattern.compile(
            "<link[^>]+rel=[\"']?openid.delegate[\"'\\s][^>]*href=[\"']?([^\\s\"']*)[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );
    private static final Pattern openId1DelegatePattern2 = Pattern.compile(
            "<link[^>]+href=[\"']?([^\\s\"']*)[\"'\\s][^>]*rel=[\"']?openid.delegate[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );

    /**
     * The pattern that helps us find the openid2.local_id tag for OpenID V1.
     */
    private static final Pattern openId2Local_idPattern1 = Pattern.compile(
            "<link[^>]+rel=[\"']?openid2.local_id[\"'\\s][^>]*href=[\"']?([^\\s\"']*)[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );
    private static final Pattern openId2Local_idPattern2 = Pattern.compile(
            "<link[^>]+href=[\"']?([^\\s\"']*)[\"'\\s][^>]*rel=[\"']?openid2.local_id[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );

    /**
     * The pattern that helps us find the yadis location tag.
     */
    private static final Pattern yadisHttpEquivPattern1 = Pattern.compile(
            "<meta[^>]+http-equiv=[\"']?X-XRDS-Location[\"'\\s][^>]*content=[\"']?([^\\s\"']*)[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );
    private static final Pattern yadisHttpEquivPattern2 = Pattern.compile(
            "<meta[^>]+content=[\"']?([^\\s\"']*)[\"'\\s][^>]*http-equiv=[\"']?X-XRDS-Location[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );
    private static final Pattern yadisHttpEquivPattern3 = Pattern.compile(
            "<meta[^>]+http-equiv=[\"']?X-YADIS-Location[\"'\\s][^>]*content=[\"']?([^\\s\"']*)[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );
    private static final Pattern yadisHttpEquivPattern4 = Pattern.compile(
            "<meta[^>]+content=[\"']?([^\\s\"']*)[\"'\\s][^>]*http-equiv=[\"']?X-YADIS-Location[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );
}

