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

package org.infogrid.probe.yadis;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import org.infogrid.lid.model.openid.Authentication1_0Service;
import org.infogrid.lid.model.yadis.Service;
import org.infogrid.lid.model.yadis.Site;
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
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class knows how to instantiate YadisService, and subclasses of YadisService, given
 * the information provided in a service node in a Yadis Capability Document DOM.
 */
public class YadisServiceFactory
    implements
        XmlConstants
{
    private static final Log log = Log.getLogInstance( YadisServiceFactory.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param builder the DocumentBuilder to use
     */
    public YadisServiceFactory(
            DocumentBuilder builder )
    {
        theDocumentBuilder = builder;
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

            addYadisServicesFromXml( dataSourceIdentifier, dom, base.getHomeObject(), base );

        } catch( NotPermittedException ex ) {
            log.error( ex );
        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            log.error( ex );
        } catch( IOException ex ) {
            log.warn( ex );
        } catch( SAXException ex ) {
            log.warn( ex );
        } catch( URISyntaxException ex ) {
            log.warn( ex );
        }
    }
    
    /**
     * Create all the services that are defined in this Yadis document.
     *
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param dom the Yadis document's DOM
     * @param subject the MeshObject that represents the resource being described by the Yadis document
     * @param base the MeshBase in which to instantiate
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries. This should not happen.
     * @throws NotPermittedException an operation was not permitted. This should not happen.
     * @throws MeshObjectIdentifierNotUniqueException an identifier was not unique. This should not happen.
     * @throws URISyntaxException a syntax error occurred
     */
    public void addYadisServicesFromXml(
            NetMeshBaseIdentifier dataSourceIdentifier,
            Document              dom,
            NetMeshObject         subject,
            StagingMeshBase       base )
        throws
            TransactionException,
            NotPermittedException,
            MeshObjectIdentifierNotUniqueException,
            URISyntaxException
    {
        try {
            subject.bless( YadisSubjectArea.SITE );

        } catch( BlessedAlreadyException ex ) {
            log.warn( ex );
        } catch( IsAbstractException ex ) {
            log.error( ex );
            return;
        }

        NodeList rootList = dom.getChildNodes();
        for( int h=0 ; h<rootList.getLength() ; ++h ) {
            Node rootNode = rootList.item( h );
            if( !XRDS_XML_NAMESPACE.equals( rootNode.getNamespaceURI()) || !"XRDS".equals( rootNode.getLocalName() )) {
                continue;
            }
            NodeList xrdList     = rootNode.getChildNodes();
            Node     lastXrdNode = null;
            for( int i=0 ; i<xrdList.getLength() ; ++i ) {
                Node xrdNode = xrdList.item( i );
                if( !XRD_XML_NAMESPACE.equals( xrdNode.getNamespaceURI()) || !"XRD".equals( xrdNode.getLocalName() )) {
                    continue;
                }
                lastXrdNode = xrdNode;
            }
            if( lastXrdNode != null ) {
                NodeList serviceList = lastXrdNode.getChildNodes();
                int serviceCount = 0;
                for( int j=0 ; j<serviceList.getLength() ; ++j ) {
                    Node serviceNode = serviceList.item( j );
                    if( !XRD_XML_NAMESPACE.equals( serviceNode.getNamespaceURI()) || !"Service".equals( serviceNode.getLocalName() )) {
                        continue;
                    }
                    
                    String prefix = "YadisService-" + String.valueOf( serviceCount++ );
                    NetMeshObject serviceMeshObject;
                    try {
                        serviceMeshObject = base.getMeshBaseLifecycleManager().createMeshObject(
                                base.getMeshObjectIdentifierFactory().fromExternalForm( prefix ),
                                Service._TYPE );
                    } catch( IsAbstractException ex ) {
                        log.error( ex );
                        continue;
                    }
                    createService( dataSourceIdentifier, (Element) serviceNode, serviceMeshObject, prefix, base );
                    
                    try {
                        serviceMeshObject.relate( subject );
                    } catch( RelatedAlreadyException ex ) {
                        // ignore
                    }
                    try {
                        serviceMeshObject.blessRelationship( Service._Site_MakesUseOf_Service_DESTINATION, subject );
                    } catch( RoleTypeBlessedAlreadyException ex ) {
                        // ignore
                    } catch( EntityNotBlessedException ex ) {
                        log.error( ex );
                    } catch( NotRelatedException ex ) {
                        log.error( ex );
                    } catch( IsAbstractException ex ) {
                        log.error( ex );
                    }
                }
            }
        }
    }

    /**
     * Factory method to instantiate the Services found at this serviceNode.
     *
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param serviceNode the Element in the DOM that we are currently working on
     * @param serviceMeshObject the MeshObject that represents the serviceNode
     * @param prefix the prefix to use for uniquely naming elements
     * @param base the MeshBase in which to instantiate
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries. This should not happen.
     * @throws NotPermittedException an operation was not permitted. This should not happen.
     * @throws MeshObjectIdentifierNotUniqueException an identifier was not unique. This should not happen.
     * @throws URISyntaxException a syntax error occurred
     */
    public void createService(
            NetMeshBaseIdentifier dataSourceIdentifier,
            Element               serviceNode,
            NetMeshObject         serviceMeshObject,
            String                prefix,
            StagingMeshBase       base )
        throws
            TransactionException,
            NotPermittedException,
            MeshObjectIdentifierNotUniqueException,
            URISyntaxException
    {
        try {
            serviceMeshObject.setPropertyValue( Service.PRIORITY, decodePriorityValue( serviceNode ));

        } catch( IllegalPropertyTypeException ex ) {
            log.error( ex );
            return;
        } catch( IllegalPropertyValueException ex ) {
            log.error( ex );
            return;
        }

        NodeList infoList = serviceNode.getChildNodes();
        for( int k=0 ; k<infoList.getLength() ; ++k ) {
            Node infoNode = infoList.item( k );
            if( XRD_XML_NAMESPACE.equals( infoNode.getNamespaceURI() ) && "Type".equals( infoNode.getLocalName() )) {
                NodeList     childList = infoNode.getChildNodes();
                StringBuffer found     = new StringBuffer();
                for( int l=0 ; l<childList.getLength() ; ++l ) {
                    Node child = childList.item( l );
                    if( child instanceof Text ) {
                        found.append( ((Text)child).getData() );
                    }
                }
                String realFound = found.toString().trim();
                if( realFound.indexOf( "://" ) > 0 ) {
                    
                    EntityType type = findEntityType( base.getModelBase(), realFound );
                    if( type != null ) {
                        try {
                            serviceMeshObject.bless( type );

                        } catch( EntityBlessedAlreadyException ex ) {
                            log.error( ex );
                        } catch( IsAbstractException ex ) {
                            log.error( ex );
                        }
                    }

                }
            } else if( XRD_XML_NAMESPACE.equals( infoNode.getNamespaceURI() ) && "URI".equals( infoNode.getLocalName() )) {
                NodeList     childList = infoNode.getChildNodes();
                StringBuffer found     = new StringBuffer();
                for( int l=0 ; l<childList.getLength() ; ++l ) {
                    Node child = childList.item( l );
                    if( child instanceof Text ) {
                        found.append( ((Text)child).getData() );
                    }
                }

                String realFound = found.toString().trim();
                NetMeshObjectIdentifier endpointIdentifier = base.getMeshObjectIdentifierFactory().fromExternalForm( realFound );

                NetMeshObject endpoint = findOrCreateAndBless( endpointIdentifier, YadisSubjectArea.SITE, base );

                // FIXME? endpoint.setPropertyValue( ServiceEndPoint.Priority_PROPERTYTYPE, decodePriorityValue( infoNode ));
                // endpoint.setPropertyValue( ServiceEndPoint.URI_PROPERTYTYPE, StringValue.create( realFound ));
                try {
                    serviceMeshObject.relate( endpoint );
                } catch( RelatedAlreadyException ex ) {
                    // ignore
                }
                try {
                    serviceMeshObject.blessRelationship( Service._Service_IsProvidedAtEndpoint_Site_SOURCE, endpoint );

                } catch( BlessedAlreadyException ex ) {
                    // ignore
                } catch( EntityNotBlessedException ex ) {
                    log.error( ex );
                } catch( NotRelatedException ex ) {
                    log.error( ex );
                } catch( IsAbstractException ex ) {
                    log.error( ex );
                }
            }
        }
    }

    /**
     * Find a priority attribute on a node, and obtain the value as an IntegerValue.
     *
     * @param nodeWithPriority the XML node that holds the priority attribute
     * @return the corresponding IntegerValue
     */
    protected PropertyValue decodePriorityValue(
            Node nodeWithPriority )
    {
        Node priorityNode = nodeWithPriority.getAttributes().getNamedItemNS( XRD_XML_NAMESPACE, "priority" );

        if( priorityNode == null ) {
            priorityNode = nodeWithPriority.getAttributes().getNamedItem( "priority" );
            // seems like a good compromise? FIXME?
        }
        int priorityValue = Integer.MAX_VALUE;
        if( priorityNode != null ) {
            String tmp = ((Attr)priorityNode).getValue();
            try {
                priorityValue = Integer.parseInt( tmp );
            } catch( Exception ex ) {
                log.warn( ex );
            }
        }
        return IntegerValue.create( priorityValue );
    }

    /**
     * Find an EntityType that goes with the provided type tag in the XRDS file.
     * 
     * @param modelBase the ModelBase to use
     * @param externalForm the external form of the EntityType's identifier
     * @return the found EntityType, or null
     */
    protected EntityType findEntityType(
            ModelBase modelBase,
            String    externalForm )
    {
        MeshTypeIdentifier identifier;
        MeshType ret;
        try {
            // first look in the mapping table
            identifier = theTypeMappingTable.get( externalForm );
            if( identifier != null ) {
                ret = modelBase.findMeshTypeByIdentifier( identifier );
                return (EntityType) ret;
            }
            
            // then try directly
            identifier = modelBase.getMeshTypeIdentifierFactory().fromExternalForm( externalForm );
            ret = modelBase.findMeshTypeByIdentifier( identifier );
            if( ret != null ) {
                return (EntityType) ret;
            }
        } catch( MeshTypeNotFoundException ex ) {
            // ignore
            if( log.isDebugEnabled() ) {
                log.debug( ex );
            }
        }

        // finally try to strip of the beta version
        Matcher m = betaPattern.matcher( externalForm );
        if( !m.find() ) {
            return null;
        }
        externalForm = m.group( 1 );
        identifier = modelBase.getMeshTypeIdentifierFactory().fromExternalForm( externalForm );

        // try directly
        try {
            ret = modelBase.findMeshTypeByIdentifier( identifier );
            return (EntityType) ret;

        } catch( MeshTypeNotFoundException ex ) {
            // ignore
            if( log.isDebugEnabled() ) {
                log.debug( ex );
            }
        }
        
        identifier = theTypeMappingTable.get( externalForm );
        if( identifier == null ) {
            return null;
        }

        try {
            ret = modelBase.findMeshTypeByIdentifier( identifier );
            return (EntityType) ret;

        } catch( MeshTypeNotFoundException ex ) {
            // ignore, we just don't know
            if( log.isInfoEnabled() ) {
                log.info( ex );
            }
        }
        return null;
    }

    /**
     * Find a MeshObject with a certain identifier; if not found, instantiate
     * a new one and bless it with an EntityType.
     * 
     * @param identifier the identifier of the MeshObject to be found or created
     * @param type the EntityType with which to bless a newly created MeshObject
     * @param base the MeshBase to use
     * @return the found or newly created MeshObject
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries. This should not happen.
     * @throws NotPermittedException an operation was not permitted. This should not happen.
     * @throws MeshObjectIdentifierNotUniqueException an identifier was not unique. This should not happen.
     */
    protected NetMeshObject findOrCreateAndBless(
            NetMeshObjectIdentifier identifier,
            EntityType              type,
            StagingMeshBase         base )
        throws
            TransactionException,
            NotPermittedException,
            MeshObjectIdentifierNotUniqueException
    {
        NetMeshObject ret = base.findMeshObjectByIdentifier( identifier );
        if( ret == null ) {
            ret = base.getMeshBaseLifecycleManager().createForwardReference( identifier.getNetMeshBaseIdentifier(), identifier );
        }
        try {
            ret.bless( type );
        } catch( BlessedAlreadyException ex ) {
            // ignore
        } catch( IsAbstractException ex ) {
            log.error( ex );
        }
        return ret;
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
        } catch( URISyntaxException ex ) {
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
     * @throws URISyntaxException a syntax error occurred
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
            URISyntaxException
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
                    NetMeshBaseIdentifier yadisNetworkIdentifier = NetMeshBaseIdentifier.guessAndCreate( dataSourceIdentifier, yadisLocation );

                    yadisLocation = yadisNetworkIdentifier.getUriString();

                    if( startHeadStart < yadisLocationStart && yadisLocationStart < endHeadStart ) {
                        HTTP.Response yadisResponse = HTTP.http_get( yadisLocation );
                        if( yadisResponse.isSuccess() ) {
                            addYadisServicesFromXml( yadisNetworkIdentifier, yadisResponse.getContentAsString(), base );
                            return;
                        }
                    }
                } catch( URISyntaxException ex ) {
                    log.warn( ex );
                } catch( IOException ex ) {
                    log.warn( ex );
                }
            }
            
            // look for OpenID tags
            Matcher openIdServerMatcher1   = openIdServerPattern1.matcher( content );
            Matcher openIdServerMatcher2   = openIdServerPattern2.matcher( content );
            Matcher openIdDelegateMatcher1 = openIdDelegatePattern1.matcher( content );
            Matcher openIdDelegateMatcher2 = openIdDelegatePattern2.matcher( content );

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
                NetMeshBaseIdentifier identityServerIdentifier = NetMeshBaseIdentifier.guessAndCreate( openIdServerMatcher.group( 1 ) );
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

                                delegateIdentifier = NetMeshBaseIdentifier.guessAndCreate( delegateUrl );
                            }
                        }
                    }
                } catch( URISyntaxException ex ) {
                    log.warn( ex );
                }

                try {
                    subject.bless( YadisSubjectArea.SITE );

                } catch( BlessedAlreadyException ex ) {
                    log.warn( ex );
                } catch( IsAbstractException ex ) {
                    log.error( ex );
                }

                String prefix = "YadisService-0";
                try {
                    NetMeshObject serviceMeshObject = base.getMeshBaseLifecycleManager().createMeshObject(
                            base.getMeshObjectIdentifierFactory().fromExternalForm( prefix ),
                            Service._TYPE );

                    serviceMeshObject.bless( Authentication1_0Service._TYPE ); // FIXME? OpenIDAuthentication.TYPE );
                    if( delegateIdentifier != null ) {
                        serviceMeshObject.setPropertyValue( org.infogrid.lid.model.openid.AuthenticationService.DELEGATE, StringValue.create( delegateIdentifier.toExternalForm() ));
                    }
                    
                    NetMeshObject endpoint = base.getMeshBaseLifecycleManager().createForwardReference(
                            identityServerIdentifier,
                            Site._TYPE );

                    // endpoint.setPropertyValue( ServiceEndPoint.URI_PROPERTYTYPE, StringValue.create( identityServer ));

                    serviceMeshObject.relateAndBless( Service._Service_IsProvidedAtEndpoint_Site_SOURCE, endpoint );
                    serviceMeshObject.relateAndBless( Service._Site_MakesUseOf_Service_DESTINATION, subject );

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
                }
            }
        }
    }

    /**
     * The XML DocumentBuilder to use.
     */
    protected DocumentBuilder theDocumentBuilder;

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
     * The pattern that helps us find the openid.server tag.
     */
    private static final Pattern openIdServerPattern1 = Pattern.compile(
            "<link[^>]+rel=[\"']?openid.server[\"'\\s][^>]*href=[\"']?([^\\s\"']*)[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );
    private static final Pattern openIdServerPattern2 = Pattern.compile(
            "<link[^>]+href=[\"']?([^\\s\"']*)[\"'\\s][^>]*rel=[\"']?openid.server[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );

    /**
     * The pattern that helps us find the openid.delegate tag.
     */
    private static final Pattern openIdDelegatePattern1 = Pattern.compile(
            "<link[^>]+rel=[\"']?openid.delegate[\"'\\s][^>]*href=[\"']?([^\\s\"']*)[\"'\\s>]",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL );
    private static final Pattern openIdDelegatePattern2 = Pattern.compile(
            "<link[^>]+href=[\"']?([^\\s\"']*)[\"'\\s][^>]*rel=[\"']?openid.delegate[\"'\\s>]",
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
    
    /**
     * Table that maps Yadis type tags to Identifiers in our model.
     */
    protected static final HashMap<String,MeshTypeIdentifier> theTypeMappingTable = new HashMap<String,MeshTypeIdentifier>();
    static {
        // none of the following lines should be instantiated as long as we haven't defined a subtype of the Yadis generic Service
        // theTypeMappingTable.put( "http://lid.netmesh.org/minimum-lid/2.0",        MeshTypeIdentifier.create( "org.Yadis", "Service" ));
        // theTypeMappingTable.put( "http://lid.netmesh.org/sso/2.0",                MeshTypeIdentifier.create( "org.Yadis", "Service" ));
        // theTypeMappingTable.put( "http://lid.netmesh.org/relying-party/2.0",      MeshTypeIdentifier.create( "org.Yadis", "Service" ));
        // theTypeMappingTable.put( "http://lid.netmesh.org/traversal/2.0",          MeshTypeIdentifier.create( "org.Yadis", "Service" ));
        // theTypeMappingTable.put( "http://lid.netmesh.org/format-negotiation/2.0", MeshTypeIdentifier.create( "org.Yadis", "Service" ));
        // theTypeMappingTable.put( "http://lid.netmesh.org/post/sender/2.0",        MeshTypeIdentifier.create( "org.Yadis", "Service" ));
        // theTypeMappingTable.put( "http://lid.netmesh.org/post/receiver/2.0",      MeshTypeIdentifier.create( "org.Yadis", "Service" ));
    }
    
    /**
     * The "beta" pattern.
     */
    private static final Pattern betaPattern = Pattern.compile(
            "^(.*)b([0-9]+)$",
            Pattern.CASE_INSENSITIVE );
}

