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
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Web.WebSubjectArea;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.MeshTypeSynonymDictionary;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.xml.XmlDOMProbe;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * <p>Probe for XRDS files.</p>
 */
public class XrdsProbe
        implements
            XmlDOMProbe,
            XmlConstants
{
    private static final Log log = Log.getLogInstance( XrdsProbe.class ); // our own, private logger

    /**
     * <p>Read from the DOM and instantiate corresponding MeshObjects.</p>
     *
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param coherenceSpecification the type of data coherence that is requested by the application. Probe
     *         implementors may ignore this parameter, letting the Probe framework choose its own policy.
     *         If the Probe chooses to define its own policy (considering or ignoring this parameter), the
     *         Probe must bless the Probe's HomeObject with a subtype of <code>ProbeUpdateSpecification</code> (defined
     *         in the <code>org.infogrid.model.Probe</code> Subject Area) and suitable Property
     *         values that reflect the policy.
     * @param theDocument the DOM document to be interpreted
     * @param freshMeshBase the StagingMeshBase in which the corresponding MeshObjects are to be instantiated by the Probe.
     *         This StagingMeshBase is empty when passed into this call, except for the home object which always exists
     * @throws EntityBlessedAlreadyException thrown if a MeshObject was incorrectly blessed twice with the same
     *         EntityType. Throwing this typically indicates a programming error.
     * @throws EntityNotBlessedException thrown if a MeshObject was not blessed with a required EntityType.
     *         Throwing this typically indicates a programming error.
     * @throws IllegalPropertyTypeException thrown if a MeshObject did not carry a PropertyType that it needed
     *         to carry. Throwing this typically indicates a programming error.
     * @throws IllegalPropertyValueException thrown if a PropertyValue was assigned to a property that was
     *         outside of the allowed range. Throwing this typically indicates a programming error.
     * @throws IOException an input/output error occurred during execution of the Probe
     * @throws IsAbstractException thrown if an EntityType or a Relationship could not be instantiated because
     *         it was abstract. Throwing this typically indicates a programming error.
     * @throws MeshObjectIdentifierNotUniqueException thrown if the Probe developer incorrectly
     *         assigned duplicate MeshObjectsIdentifiers to created MeshObjects.
     *         Throwing this typically indicates a programming error.
     * @throws ModuleException thrown if a Module required by the Probe could not be loaded
     * @throws NotPermittedException thrown if an operation performed by the Probe was not permitted
     * @throws NotRelatedException thrown if a relationship was supposed to become blessed, but the relationship
     *         did not exist. Throwing this typically indicates a programming error.
     * @throws ProbeException a Probe error occurred per the possible subclasses defined in ProbeException
     * @throws RelatedAlreadyException thrown if the Probe developer incorrectly attempted to
     *         relate two already-related MeshObjects. Throwing this typically indicates a programming error.
     * @throws RoleTypeBlessedAlreadyException thrown if a relationship was incorrectly blessed twice with the same
     *         RelationshipType, in the same direction. Throwing this typically indicates a programming error.
     * @throws TransactionException a Transaction problem occurred. Throwing this typically indicates a programming error.
     * @throws URISyntaxException thrown if a URI was constructed in an invalid way
     * @throws StringRepresentationParseException thrown if a StringRepresentation could not be parsed
     */
    public void parseDocument(
            NetMeshBaseIdentifier  dataSourceIdentifier,
            CoherenceSpecification coherenceSpecification,
            Document               theDocument,
            StagingMeshBase        freshMeshBase )
        throws
            EntityBlessedAlreadyException,
            EntityNotBlessedException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException,
            IOException,
            IsAbstractException,
            MeshObjectIdentifierNotUniqueException,
            ModuleException,
            NotPermittedException,
            NotRelatedException,
            ProbeException,
            RelatedAlreadyException,
            RoleTypeBlessedAlreadyException,
            TransactionException,
            URISyntaxException,
            StringRepresentationParseException
    {
        addYadisServicesFromXml( dataSourceIdentifier, theDocument, freshMeshBase );
    }

    /**
     * Create all the services that are defined in this Yadis document.
     *
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param dom the Yadis document's DOM
     * @param base the MeshBase in which to instantiate
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries. This should not happen.
     * @throws NotPermittedException an operation was not permitted. This should not happen.
     * @throws IsAbstractException thrown if a MeshType was abstract. This should not happen.
     * @throws IllegalPropertyTypeException thrown if a MeshObject did not carry a specified PropertyType. This should not happen.
     * @throws IllegalPropertyValueException thrown if a property could not be set to the specified PropertyValue. This should not happen
     * @throws MeshObjectIdentifierNotUniqueException an identifier was not unique. This should not happen.
     * @throws StringRepresentationParseException a syntax error occurred
     */
    public void addYadisServicesFromXml(
            NetMeshBaseIdentifier dataSourceIdentifier,
            Document              dom,
            StagingMeshBase       base )
        throws
            TransactionException,
            NotPermittedException,
            IsAbstractException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException,
            MeshObjectIdentifierNotUniqueException,
            StringRepresentationParseException
    {
        NetMeshObject subject = base.getHomeObject();

        try {
            subject.bless( YadisSubjectArea.XRDSSERVICECOLLECTION );

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
                                YadisSubjectArea.XRDSSERVICE );
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
                        subject.blessRelationship(
                                YadisSubjectArea.XRDSSERVICECOLLECTION_COLLECTS_XRDSSERVICE.getSource(),
                                serviceMeshObject );
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
     * @throws IsAbstractException thrown if a MeshType was abstract. This should not happen.
     * @throws IllegalPropertyTypeException thrown if a MeshObject did not carry a specified PropertyType. This should not happen.
     * @throws IllegalPropertyValueException thrown if a property could not be set to the specified PropertyValue. This should not happen
     * @throws MeshObjectIdentifierNotUniqueException an identifier was not unique. This should not happen.
     * @throws StringRepresentationParseException a syntax error occurred
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
            IsAbstractException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException,
            MeshObjectIdentifierNotUniqueException,
            StringRepresentationParseException
    {
        try {
            serviceMeshObject.setPropertyValue(
                    YadisSubjectArea.XRDSSERVICE_PRIORITY,
                    decodePriorityValue( serviceNode ));

        } catch( IllegalPropertyTypeException ex ) {
            log.error( ex );
            return;
        } catch( IllegalPropertyValueException ex ) {
            log.error( ex );
            return;
        }

        int epCounter   = 0;
        int typeCounter = 0;

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

                    try {
                        NetMeshObject serviceMeshObjectType = base.getMeshBaseLifecycleManager().createMeshObject(
                                base.getMeshObjectIdentifierFactory().fromExternalForm( serviceMeshObject.getIdentifier().toExternalForm() + "-type-" + typeCounter ),
                                YadisSubjectArea.XRDSSERVICETYPE );
                        serviceMeshObjectType.setPropertyValue( YadisSubjectArea.XRDSSERVICETYPE_SERVICETYPEIDENTIFIER, StringValue.create( realFound ));

                        serviceMeshObject.relateAndBless( YadisSubjectArea.XRDSSERVICE_HASTYPE_XRDSSERVICETYPE.getSource(), serviceMeshObjectType );
                        ++typeCounter;

                    } catch( EntityNotBlessedException ex ) {
                        log.error( ex );
                    } catch( RelatedAlreadyException ex ) {
                        log.error( ex );
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

                NetMeshObject endpoint = base.getMeshBaseLifecycleManager().createMeshObject(
                        base.getMeshObjectIdentifierFactory().fromExternalForm( serviceMeshObject.getIdentifier().toExternalForm() + "-endpoint-" + epCounter ),
                        YadisSubjectArea.ENDPOINT );
                endpoint.setPropertyValue( YadisSubjectArea.ENDPOINT_PRIORITY, decodePriorityValue( infoNode ));
                ++epCounter;

                String realFound = found.toString().trim();
                NetMeshObjectIdentifier resourceIdentifier = base.getMeshObjectIdentifierFactory().fromExternalForm( realFound );

                NetMeshObject resource = findOrCreateForwardReferenceAndBless(
                        resourceIdentifier,
                        WebSubjectArea.WEBRESOURCE,
                        base );

                // endpoint.setPropertyValue( ServiceEndPoint.URI_PROPERTYTYPE, StringValue.obtain( realFound ));
                try {
                    serviceMeshObject.relate( endpoint );
                } catch( RelatedAlreadyException ex ) {
                    // ignore
                }
                try {
                    serviceMeshObject.blessRelationship(
                            YadisSubjectArea.XRDSSERVICE_ISPROVIDEDAT_ENDPOINT.getSource(),
                            endpoint );

                } catch( BlessedAlreadyException ex ) {
                    // ignore
                } catch( EntityNotBlessedException ex ) {
                    log.error( ex );
                } catch( NotRelatedException ex ) {
                    log.error( ex );
                }

                try {
                    endpoint.relate( resource );
                } catch( RelatedAlreadyException ex ) {
                    // ignore
                }
                try {
                    endpoint.blessRelationship(
                            YadisSubjectArea.ENDPOINT_ISOPERATEDBY_WEBRESOURCE.getSource(),
                            resource );

                } catch( BlessedAlreadyException ex ) {
                    // ignore
                } catch( EntityNotBlessedException ex ) {
                    log.error( ex );
                } catch( NotRelatedException ex ) {
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

        IntegerValue ret = null;
        if( priorityNode != null ) {
            String tmp = ((Attr)priorityNode).getValue();
            try {
                int found = Integer.parseInt( tmp );
                ret = IntegerValue.create( found );
            } catch( Exception ex ) {
                log.warn( ex );
            }
        }
        
        return ret;
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
        MeshTypeSynonymDictionary dictionary = modelBase.getSynonymDictionary();
        MeshTypeIdentifier        identifier = modelBase.getMeshTypeIdentifierFactory().fromExternalForm( externalForm );

        MeshType ret = null;
        if( ret == null && dictionary != null ) {
            try {
                ret = dictionary.findMeshTypeByIdentifier( identifier );
            } catch( MeshTypeNotFoundException ex ) {
                // ignore
            }
        }

        if( ret == null ) {
            try {
                ret = modelBase.findMeshTypeByIdentifier( identifier );
            } catch( MeshTypeNotFoundException ex ) {
                // ignore
            }
        }

        MeshTypeIdentifier withoutBetaIdentifier = null;
        if( ret == null ) {
            // finally try to strip of the beta version
            Matcher m = betaPattern.matcher( externalForm );
            if( m.find() ) {
                externalForm = m.group( 1 );
                withoutBetaIdentifier = modelBase.getMeshTypeIdentifierFactory().fromExternalForm( externalForm );
            }
        }

        if( withoutBetaIdentifier != null ) {
            if( ret == null && dictionary != null ) {
                try {
                    ret = dictionary.findMeshTypeByIdentifier( withoutBetaIdentifier );
                } catch( MeshTypeNotFoundException ex ) {
                    // ignore
                }
            }
            if( ret == null ) {
                try {
                    ret = modelBase.findMeshTypeByIdentifier( withoutBetaIdentifier );
                } catch( MeshTypeNotFoundException ex ) {
                    // ignore
                }
            }
        }

        try {
            return (EntityType) ret;

        } catch( ClassCastException ex ) {
            log.error( ex );
        }
        return null;
    }

    /**
     * Find a ForwardReference MeshObject with a certain identifier; if not found, instantiate
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
    protected NetMeshObject findOrCreateForwardReferenceAndBless(
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
     * The "beta" pattern.
     */
    private static final Pattern betaPattern = Pattern.compile(
            "^(.*)b([0-9]+)$",
            Pattern.CASE_INSENSITIVE );
}
