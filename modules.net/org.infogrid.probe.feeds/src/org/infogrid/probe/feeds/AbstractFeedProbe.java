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

package org.infogrid.probe.feeds;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;

import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;

import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.primitives.BooleanValue;
import org.infogrid.model.primitives.ColorValue;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.EnumeratedDataType;
import org.infogrid.model.primitives.ExtentValue;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.MultiplicityValue;
import org.infogrid.model.primitives.PointValue;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.TimePeriodValue;
import org.infogrid.model.primitives.TimeStampValue;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;

import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.xml.MeshObjectSetProbeTags;
import org.infogrid.probe.xml.XmlDOMProbe;

import org.infogrid.util.logging.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Color;
import java.net.URISyntaxException;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;

/**
 * Factors out common functionality for Atom and RSS feeds.
 */
public abstract class AbstractFeedProbe
        implements
            XmlDOMProbe
{
    private static final Log log = Log.getLogInstance( AbstractFeedProbe.class );

    /**
     * Constructor for subclasses only.
     */
    protected AbstractFeedProbe()
    {
    }
    
    /**
     * Invoked by subclasses to instantiate the InfoGrid-specific extensions on RSS/Atom feeds.
     *
     * @param top the top DOM node of the feed (channel in RSS, feed in Atom)
     */
    protected void handleInfoGridFeedExtensions(
            NetMeshBaseIdentifier networkId,
            Document          theDocument,
            Element           here,
            NetMeshObject     current )
        throws
            TransactionException,
            NotPermittedException,
            ProbeException,
            URISyntaxException,
            IsAbstractException,
            EntityBlessedAlreadyException,
            EntityNotBlessedException,
            RelatedAlreadyException,
            RoleTypeBlessedAlreadyException,
            NotRelatedException,
//            MeshObjectIdentifierNotUniqueException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException
//            TransactionException,
//            NotPermittedException,
//            ProbeException,
//            IOException,
//            ModuleException,
//            URISyntaxException;
    {
        if( here == null ) {
            return;
        }
        if( current == null ) {
            return;
        }
        
        ModelBase modelBase = current.getMeshBase().getModelBase();

        NodeList children = here.getChildNodes();
        for( int i=0 ; i<children.getLength() ; ++i ) {
            Node child = children.item( i );

            if( !( child instanceof Element )) {
                continue;
            }
            Element realChild = (Element) child;
            if( !MeshObjectSetProbeTags.INFOGRID_NAMESPACE.equals( realChild.getNamespaceURI() )) {
                continue;
            }
            
            if( MeshObjectSetProbeTags.MESH_TYPE_TAG.equals( realChild.getLocalName())) {
                String typeString = realChild.getTextContent();
                if( typeString != null ) {
                    typeString = typeString.trim();
                }
                if( typeString == null || typeString.length() == 0 ) {
                    log.warn( "Empty type given for " + current );
                } else {
                    try {
                        EntityType type = modelBase.findEntityTypeByIdentifier(
                                modelBase.getMeshTypeIdentifierFactory().fromExternalForm( typeString ));
                        current.bless( type );
                    } catch( MeshTypeWithIdentifierNotFoundException ex ) {
                        log.warn( ex );
                    }
                }

            } else if( MeshObjectSetProbeTags.PROPERTY_TYPE_TAG.equals( realChild.getLocalName())) {
                String typeString = realChild.getAttribute( MeshObjectSetProbeTags.TYPE_TAG );
                if( typeString != null ) {
                    typeString = typeString.trim();
                }
                if( typeString == null || typeString.length() == 0 ) {
                    log.warn( "Empty type given for property on " + current );
                } else {
                    try {
                        PropertyType  type  = modelBase.findPropertyTypeByIdentifier(
                                modelBase.getMeshTypeIdentifierFactory().fromExternalForm( typeString ));

                        PropertyValue value = determinePropertyValue( networkId, type, realChild );
                        
                        current.setPropertyValue( type, value );
                        
                    } catch( MeshTypeWithIdentifierNotFoundException ex ) {
                        throw new ProbeException.SyntaxError( networkId, ex );
                    }
                }

            } else if( MeshObjectSetProbeTags.RELATIONSHIP_TAG.equals( realChild.getLocalName())) {
                String idString = realChild.getAttribute( MeshObjectSetProbeTags.IDENTIFIER_TAG );
                if( idString != null ) {
                    idString = idString.trim();
                }
                if( idString == null) {
                    log.warn( "No ID given for relationship on " + current );
                } else {
                    establishRelationship( current, idString, realChild );
                }
            } else {
                log.warn( "unexpected tag: " + realChild.getLocalName() );
            }
        }
    }
    
    /**
     * Invoked by subclasses to instantiate the InfoGrid-specific extensions on RSS/Atom feeds.
     */
    protected NetMeshObject createExtendedInfoGridFeedEntryObject(
            NetMeshBaseIdentifier networkId,
            Document          theDocument,
            Element           here,
            MeshObjectIdentifier   identifier,
            EntityType        type,
            StagingMeshBase   mb )
        throws
            TransactionException,
            EntityNotBlessedException,
            RelatedAlreadyException,
            RoleTypeBlessedAlreadyException,
            NotRelatedException,
            NotPermittedException,
            MeshObjectIdentifierNotUniqueException,
            ProbeException,
            URISyntaxException,
            IsAbstractException,
            EntityBlessedAlreadyException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException
    {
        NetMeshObject ret = mb.getMeshBaseLifecycleManager().createMeshObject( identifier, type );
        
        handleInfoGridFeedExtensions( networkId, theDocument, here, ret );
        
        return ret;
    }

    /**
     * Helper method to determine a PropertyValue encoded in the InfoGrid XML.
     *
     * @param current the NetMeshObject
     * @param here the enclosing DOM element
     */
    protected PropertyValue determinePropertyValue(
            NetMeshBaseIdentifier networkId,
            PropertyType      type,
            Element           here )
        throws
            ProbeException
    {
        
        NodeList children = here.getChildNodes();
        for( int i=0 ; i<children.getLength() ; ++i ) {
            Node child = children.item( i );

            if( !( child instanceof Element )) {
                continue;
            }
            Element realChild = (Element) child;
            if( !MeshObjectSetProbeTags.INFOGRID_NAMESPACE.equals( realChild.getNamespaceURI() )) {
                continue;
            }
            
            String localName = realChild.getLocalName();
            if( MeshObjectSetProbeTags.BLOB_VALUE_TAG.equals( localName )) {
                String mime     = realChild.getAttribute( MeshObjectSetProbeTags.BLOB_VALUE_MIME_TAG );
                String loadFrom = realChild.getAttribute( MeshObjectSetProbeTags.BLOB_VALUE_LOAD_TAG );
                
                if( loadFrom != null ) {
                    PropertyValue ret = BlobValue.createByLoadingFrom( loadFrom, mime );
                } else {                
                    String content = realChild.getTextContent();

                    PropertyValue ret;
                    if( mime.startsWith( "text/" )) {

                        ret = BlobValue.create( content, mime );
                    } else {
                        if( !content.startsWith( "x\'" ) || !content.endsWith( "\'" )) {
                            throw new ProbeException.SyntaxError( networkId, "hex-encoded binary BlobValue must be encapsulated in x'...'", null );
                        }
                        content = content.substring( 2, content.length()-1 );
                        ret = BlobValue.create( BlobValue.decodeHex( content ), mime );
                    }                    
                }
                
            } else if( MeshObjectSetProbeTags.BOOLEAN_VALUE_TAG.equals( localName )) {
                String content = realChild.getTextContent();

                if( MeshObjectSetProbeTags.BOOLEAN_VALUE_TRUE_TAG.equals( content )) {
                    PropertyValue ret = BooleanValue.TRUE;
                    return ret;

                } else if( MeshObjectSetProbeTags.BOOLEAN_VALUE_FALSE_TAG.equals( content )) {
                    PropertyValue ret = BooleanValue.TRUE;
                    return ret;
                } else {
                    log.error( "Wrong value for tag " + localName );
                }
                
            } else if( MeshObjectSetProbeTags.COLOR_VALUE_TAG.equals( localName )) {
                String red   = realChild.getAttribute( MeshObjectSetProbeTags.COLOR_VALUE_RED_TAG );
                String green = realChild.getAttribute( MeshObjectSetProbeTags.COLOR_VALUE_GREEN_TAG );
                String blue  = realChild.getAttribute( MeshObjectSetProbeTags.COLOR_VALUE_BLUE_TAG );
                String alpha = realChild.getAttribute( MeshObjectSetProbeTags.COLOR_VALUE_ALPHA_TAG );

                Color col = new Color( Integer.parseInt( red ), Integer.parseInt( green ), Integer.parseInt( blue ), Integer.parseInt( alpha ));
                PropertyValue ret = ColorValue.createOrNull( col );
                return ret;
                
            } else if( MeshObjectSetProbeTags.ENUMERATED_VALUE_TAG.equals( localName )) {
                if( !( type.getDataType() instanceof EnumeratedDataType )) {
                    throw new ProbeException.SyntaxError( networkId, "Data type not an EnumeratedDataType: " + type, null );
                }
                EnumeratedDataType realType = (EnumeratedDataType) type.getDataType();
                
                String content = realChild.getTextContent();

                PropertyValue ret = realType.select( content );
                return ret;
                            
            } else if( MeshObjectSetProbeTags.EXTENT_VALUE_TAG.equals( localName )) {
                String w = realChild.getAttribute( MeshObjectSetProbeTags.EXTENT_VALUE_WIDTH_TAG );
                String h = realChild.getAttribute( MeshObjectSetProbeTags.EXTENT_VALUE_HEIGHT_TAG );

                PropertyValue ret = ExtentValue.create( Double.parseDouble( w ), Double.parseDouble( h ));
                return ret;
                
            } else if( MeshObjectSetProbeTags.INTEGER_VALUE_TAG.equals( localName )) {
                String content = realChild.getTextContent();
                
                PropertyValue ret = IntegerValue.create( Integer.parseInt( content ) );
                return ret;
    
            } else if( MeshObjectSetProbeTags.FLOAT_VALUE_TAG.equals( localName )) {
                String content = realChild.getTextContent();
                
                PropertyValue ret = FloatValue.create( Double.parseDouble( content ) );
                return ret;
    
            } else if( MeshObjectSetProbeTags.MULTIPLICITY_VALUE_TAG.equals( localName )) {
                String minString = realChild.getAttribute( MeshObjectSetProbeTags.MULTIPLICITY_VALUE_MIN_TAG );
                String maxString = realChild.getAttribute( MeshObjectSetProbeTags.MULTIPLICITY_VALUE_MAX_TAG );

                int min;
                int max;

                if( "*".equals( minString ) || "n".equalsIgnoreCase( minString )) {
                    min = MultiplicityValue.N;
                } else {
                    min = Integer.parseInt( minString );
                }
                if( "*".equals( maxString ) || "n".equalsIgnoreCase( maxString )) {
                    max = MultiplicityValue.N;
                } else {
                    max = Integer.parseInt( maxString );
                }
                
                PropertyValue ret = MultiplicityValue.create( min, max );
                return ret;
                
            } else if( MeshObjectSetProbeTags.POINT_VALUE_TAG.equals( localName )) {
                String x = realChild.getAttribute( MeshObjectSetProbeTags.POINT_VALUE_X_TAG );
                String y = realChild.getAttribute( MeshObjectSetProbeTags.POINT_VALUE_Y_TAG );

                PropertyValue ret = PointValue.create( Double.parseDouble( x ), Double.parseDouble( y ));
                return ret;
                
            } else if( MeshObjectSetProbeTags.STRING_VALUE_TAG.equals( localName )) {
                String content = realChild.getTextContent();
                
                PropertyValue ret = StringValue.create( content );
                return ret;
                
            } else if( MeshObjectSetProbeTags.TIME_PERIOD_TAG.equals( localName )) {
                String year   = realChild.getAttribute( MeshObjectSetProbeTags.TIME_PERIOD_YEAR_TAG );
                String month  = realChild.getAttribute( MeshObjectSetProbeTags.TIME_PERIOD_MONTH_TAG );
                String day    = realChild.getAttribute( MeshObjectSetProbeTags.TIME_PERIOD_DAY_TAG );
                String hour   = realChild.getAttribute( MeshObjectSetProbeTags.TIME_PERIOD_HOUR_TAG );
                String minute = realChild.getAttribute( MeshObjectSetProbeTags.TIME_PERIOD_MINUTE_TAG );
                String second = realChild.getAttribute( MeshObjectSetProbeTags.TIME_PERIOD_SECOND_TAG );

                PropertyValue ret = TimePeriodValue.create(
                        Short.parseShort( year ),
                        Short.parseShort( month ),
                        Short.parseShort( day ),
                        Short.parseShort( hour ),
                        Short.parseShort( minute ),
                        Float.parseFloat( second ));
                return ret;
                
            } else if( MeshObjectSetProbeTags.TIME_STAMP_TAG.equals( localName )) {
                String year   = realChild.getAttribute( MeshObjectSetProbeTags.TIME_STAMP_YEAR_TAG );
                String month  = realChild.getAttribute( MeshObjectSetProbeTags.TIME_STAMP_MONTH_TAG );
                String day    = realChild.getAttribute( MeshObjectSetProbeTags.TIME_STAMP_DAY_TAG );
                String hour   = realChild.getAttribute( MeshObjectSetProbeTags.TIME_STAMP_HOUR_TAG );
                String minute = realChild.getAttribute( MeshObjectSetProbeTags.TIME_STAMP_MINUTE_TAG );
                String second = realChild.getAttribute( MeshObjectSetProbeTags.TIME_STAMP_SECOND_TAG );

                PropertyValue ret = TimeStampValue.create(
                        Short.parseShort( year ),
                        Short.parseShort( month ),
                        Short.parseShort( day ),
                        Short.parseShort( hour ),
                        Short.parseShort( minute ),
                        Float.parseFloat( second ));
                return ret;

            } else {
                log.error( "Unexpected tag: " + localName );
            }
        }
        throw new IllegalArgumentException( "Invalid Property statement" );
    }

    protected void establishRelationship(
            NetMeshObject current,
            String        partnerId,
            Element       here )
        throws
            TransactionException,
            URISyntaxException,
            RelatedAlreadyException,
            RoleTypeBlessedAlreadyException,
            EntityNotBlessedException,
            NotRelatedException,
            IsAbstractException,
            NotPermittedException
    {
        NetMeshBase base = current.getMeshBase();
        
        NetMeshObject partner = base.findMeshObjectByIdentifier( base.getMeshObjectIdentifierFactory().fromExternalForm( partnerId ));
        if( partner == null ) {
            // don't have it (yet?), ignore
            return;
        }

        current.relate( partner );
        
        ModelBase modelBase = current.getMeshBase().getModelBase();

        NodeList children = here.getChildNodes();
        for( int i=0 ; i<children.getLength() ; ++i ) {
            Node child = children.item( i );
            
            if( !( child instanceof Element )) {
                continue;
            }
            
            Element realChild = (Element) child;
            if( !MeshObjectSetProbeTags.INFOGRID_NAMESPACE.equals( realChild.getNamespaceURI() )) {
                log.warn( "Infogrid XML data must only contain InfoGrid tags" );
                continue;
            }
            
            if( MeshObjectSetProbeTags.ROLE_TYPE_TAG.equals( realChild.getLocalName())) {
                String typeString = realChild.getAttribute( MeshObjectSetProbeTags.TYPE_TAG );
                if( typeString != null ) {
                    typeString = typeString.trim();
                }
                if( typeString == null || typeString.length() == 0 ) {
                    log.warn( "Empty type given for " + current );
                } else {
                    try {
                        RoleType type = modelBase.findRoleTypeByIdentifier(
                                modelBase.getMeshTypeIdentifierFactory().fromExternalForm( typeString ));
                        current.blessRelationship( type, partner );

                    } catch( MeshTypeWithIdentifierNotFoundException ex ) {
                        log.warn( ex );
                    }
                }
            } else {
                log.warn( "unexpected tag: " + realChild.getLocalName() );
            }
        }
        
    }

    /**
     * Helper method to obtain the text contained in a child tag with a particular tag name.
     *
     * @param node the DOM node
     * @param tag the tag name of the child node
     * @return the found String content, or null if none found
     */
    protected String getChildNodeValue(
            Element node,
            String  tag )
    {
        if( node == null ) {
            return null;
        }
        NodeList children = node.getChildNodes();
        for( int i=0 ; i<children.getLength() ; ++i ) {
            Node child = children.item( i );
            
            if( tag == null || tag.equals( child.getNodeName() )) {
                String ret = child.getTextContent();
                if( ret != null && ret.length() > 0 ) {
                    return ret;
                }
            }
        }
        return null;
    }
 
    /**
     * Helper method to obtain an attribute value in a child tag with a particular tag name.
     * This uses the same algorithm as getChildNodeValue to find the respective node.
     *
     * @param node the DOM node
     * @param tag the tag name of the child node
     * @param att the attribute name of the child node
     * @return the found String content, or null if none found
     * @see #getChildNodeValue
     */
    protected String getChildNodeAttribute(
            Element node,
            String  tag,
            String  att )
    {
        if( node == null ) {
            return null;
        }
        NodeList children = node.getChildNodes();
        for( int i=0 ; i<children.getLength() ; ++i ) {
            Node child = children.item( i );
            
            if( tag == null || tag.equals( child.getNodeName() )) {
                String found = child.getTextContent();
                if( found != null && found.length() > 0 ) {
                    Node foundAttNode = child.getAttributes().getNamedItem( att );
                    if( foundAttNode != null ) {
                        String ret = foundAttNode.getTextContent();
                        return ret;
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
