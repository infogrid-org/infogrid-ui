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

package org.infogrid.probe.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.mesh.BlessedAlreadyException;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.BlobDataType;
import org.infogrid.model.primitives.BooleanValue;
import org.infogrid.model.primitives.ColorValue;
import org.infogrid.model.primitives.EnumeratedDataType;
import org.infogrid.model.primitives.ExtentValue;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.MultiplicityValue;
import org.infogrid.model.primitives.PointValue;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.TimePeriodValue;
import org.infogrid.model.primitives.TimeStampValue;
import org.infogrid.model.primitives.UnknownEnumeratedValueException;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.probe.Probe;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.StagingMeshBaseLifecycleManager;
import org.infogrid.util.Base64;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The NetMesh Xml Probe.
 */
public class DomMeshObjectSetProbe
        implements
            Probe,
            MeshObjectSetProbeTags
{
    private static final Log log = Log.getLogInstance( DomMeshObjectSetProbe.class ); // our own, private logger

    /**
     * Constructor.
     */
    public DomMeshObjectSetProbe()
    {
        // no op
    }
    
    /**
     * Parse a DOM Document and instantiate corresponding MeshObjects.
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
     * @throws IOException an input/output error occurred during execution of the Probe
     * @throws MeshObjectIdentifierNotUniqueException thrown if the Probe developer incorrectly
     *         assigned duplicate MeshObjectsIdentifiers to created MeshObjects.
     *         Throwing this typically indicates a programming error.
     * @throws NotPermittedException thrown if an operation performed by the Probe was not permitted
     * @throws ProbeException a Probe error occurred per the possible subclasses defined in ProbeException
     * @throws TransactionException a Transaction problem occurred. Throwing this typically indicates a programming error.
     */
    public void parseDocument(
            NetMeshBaseIdentifier  dataSourceIdentifier,
            CoherenceSpecification coherenceSpecification,
            Document               theDocument,
            StagingMeshBase        freshMeshBase )
        throws
            IOException,
            MeshObjectIdentifierNotUniqueException,
            NotPermittedException,
            ProbeException,
            TransactionException
    {
        ModelBase                       theModelBase = freshMeshBase.getModelBase();
        StagingMeshBaseLifecycleManager life         = freshMeshBase.getMeshBaseLifecycleManager();
        
        ArrayList<ExternalizedMeshObject> theBufferedObjects = new ArrayList<ExternalizedMeshObject>();
        
        Node     topNode = null;
        NodeList meshObjectList = theDocument.getChildNodes();
        for( int i=0 ; i<meshObjectList.getLength() ; ++i ) {
            Node meshObjectNode = meshObjectList.item( i );
            if( meshObjectNode.getNodeType() == Node.ELEMENT_NODE && MESHOBJECT_SET_TAG.equals( meshObjectNode.getNodeName() )) {
                topNode = meshObjectNode;
                break;
            }
        }
        if( topNode == null ) {
            throw new ProbeException.EmptyDataSource( dataSourceIdentifier );
        }
        
        meshObjectList = topNode.getChildNodes();
        for( int i=0 ; i<meshObjectList.getLength() ; ++i ) {
            Node meshObjectNode = meshObjectList.item( i );
            
            if( meshObjectNode.getNodeType() != Node.ELEMENT_NODE ) {
                continue;
            }
            NamedNodeMap attrs = meshObjectNode.getAttributes();
            
            String identifier       = MeshObjectSetProbeUtils.getTextContent( attrs, IDENTIFIER_TAG );
            String timeCreated      = MeshObjectSetProbeUtils.getTextContent( attrs, TIME_CREATED_TAG );
            String timeUpdated      = MeshObjectSetProbeUtils.getTextContent( attrs, TIME_UPDATED_TAG );
            String timeRead         = MeshObjectSetProbeUtils.getTextContent( attrs, TIME_READ_TAG );
            String timeAutoDeletes  = MeshObjectSetProbeUtils.getTextContent( attrs, TIME_AUTO_DELETES_TAG );
            String giveUpLock       = MeshObjectSetProbeUtils.getTextContent( attrs, GIVE_UP_LOCK_TAG );
            String giveUpHome       = MeshObjectSetProbeUtils.getTextContent( attrs, GIVE_UP_HOME_TAG );
            String proxyTowardsHome = MeshObjectSetProbeUtils.getTextContent( attrs, PROXY_TOWARDS_HOME_TAG );

            ExternalizedMeshObject theObjectBeingParsed = new ExternalizedMeshObject();
            
            if( identifier != null ) { // need to support "" for Home Object
                theObjectBeingParsed.setIdentifier( identifier );
            }
            if( timeCreated != null && timeCreated.length() > 0 ) {
                theObjectBeingParsed.setTimeCreated( MeshObjectSetProbeUtils.parseTime( timeCreated ));
            }
            if( timeUpdated != null && timeUpdated.length() > 0 ) {
                theObjectBeingParsed.setTimeUpdated( MeshObjectSetProbeUtils.parseTime( timeUpdated ));
            }
            if( timeRead != null && timeRead.length() > 0 ) {
                theObjectBeingParsed.setTimeRead( MeshObjectSetProbeUtils.parseTime( timeRead ));
            }
            if( timeAutoDeletes != null && timeAutoDeletes.length() > 0 ) {
                theObjectBeingParsed.setTimeAutoDeletes( MeshObjectSetProbeUtils.parseTime( timeAutoDeletes ));
            }
            if( YES_TAG.equals( giveUpLock )) {
                theObjectBeingParsed.setGiveUpLock( true );
            }
            if( YES_TAG.equals( giveUpHome )) {
                theObjectBeingParsed.setGiveUpHome( true );
            }
            if( proxyTowardsHome != null && proxyTowardsHome.length() > 0 ) {
                theObjectBeingParsed.setProxyTowardsHome(
                        constructNetworkIdentifier(
                                dataSourceIdentifier,
                                proxyTowardsHome,
                                freshMeshBase.getMeshBaseIdentifierFactory() ));
            }
            
            NodeList childNodeList = meshObjectNode.getChildNodes();
            for( int j=0 ; j<childNodeList.getLength() ; ++j ) {
                Node   childNode     = childNodeList.item( j );
                String childNodeName = childNode.getNodeName();
                
                if( childNode.getNodeType() != Node.ELEMENT_NODE ) {
                    continue;
                }
                if( MESH_TYPE_TAG.equals( childNodeName )) {
                    String typeIdentifier = childNode.getTextContent();
                    theObjectBeingParsed.addMeshType( theModelBase.getMeshTypeIdentifierFactory().fromExternalForm( typeIdentifier ));

                } else if( PROPERTY_TYPE_TAG.equals( childNodeName )) {
                    String propertyIdentifier = MeshObjectSetProbeUtils.getTextContent( childNode.getAttributes(), TYPE_TAG );
                    theObjectBeingParsed.addPropertyType( theModelBase.getMeshTypeIdentifierFactory().fromExternalForm( propertyIdentifier ));
                    
                    NodeList grandNodeList = childNode.getChildNodes();
                    for( int k=0 ; k<grandNodeList.getLength() ; ++k ) {
                        Node   grandNode     = grandNodeList.item( k );
                        String grandNodeName = grandNode.getNodeName();
                        String content       = grandNode.getTextContent();

                        if( grandNode.getNodeType() != Node.ELEMENT_NODE ) {
                            continue;
                        }

                        NamedNodeMap  grandAttrs = grandNode.getAttributes();
                        PropertyValue propValue  = null;
                        
                        if( BLOB_VALUE_TAG.equals( grandNodeName )) {
                            String mime      = MeshObjectSetProbeUtils.getTextContent( grandAttrs, BLOB_VALUE_MIME_TAG );
                            String loadFrom  = MeshObjectSetProbeUtils.getTextContent( grandAttrs, BLOB_VALUE_LOAD_TAG );

                            if( mime != null && mime.length() > 0 ) {
                                if( loadFrom != null && loadFrom.length() > 0 ) {
                                    propValue = BlobDataType.theAnyType.createBlobValueByLoadingFrom( loadFrom, mime );
                                } else if( mime.startsWith( "text/" )) {
                                    propValue = BlobDataType.theAnyType.createBlobValue( Base64.base64decode( content.trim() ), mime );
                                } else {
                                    propValue = BlobDataType.theAnyType.createBlobValue( content.trim(), mime );
                                }
                            } else {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier, "empty '" + BLOB_VALUE_MIME_TAG + "' on '" + BLOB_VALUE_TAG + "'", null );
                            }
                           
                        } else if( BOOLEAN_VALUE_TAG.equals( grandNodeName )) {
                            propValue = BooleanValue.create( BOOLEAN_VALUE_TRUE_TAG.equals( content.trim() ));
                            
                        } else if( COLOR_VALUE_TAG.equals( grandNodeName )) {
                            String red   = MeshObjectSetProbeUtils.getTextContent( grandAttrs, COLOR_VALUE_RED_TAG );
                            String green = MeshObjectSetProbeUtils.getTextContent( grandAttrs, COLOR_VALUE_GREEN_TAG );
                            String blue  = MeshObjectSetProbeUtils.getTextContent( grandAttrs, COLOR_VALUE_BLUE_TAG );
                            String alpha = MeshObjectSetProbeUtils.getTextContent( grandAttrs, COLOR_VALUE_ALPHA_TAG );

                            propValue = ColorValue.create(
                                    Float.parseFloat( red ),
                                    Float.parseFloat( green ),
                                    Float.parseFloat( blue ),
                                    Float.parseFloat( alpha ));
                            
                        } else if( ENUMERATED_VALUE_TAG.equals( grandNodeName )) {
                            try {
                                MeshType mt = theModelBase.findMeshTypeByIdentifier( theModelBase.getMeshTypeIdentifierFactory().fromExternalForm( propertyIdentifier ));
                                if( ( mt instanceof PropertyType ) && ((PropertyType)mt).getDataType() instanceof EnumeratedDataType ) {
                                    EnumeratedDataType realPt = (EnumeratedDataType) ((PropertyType)mt).getDataType();
                                    theObjectBeingParsed.addPropertyValue( realPt.select( content.trim() )); // FIXME?
                                } else {
                                    throw new ProbeException.SyntaxError( dataSourceIdentifier, "MeshType with " + propertyIdentifier + " is not a PropertyType", null );
                                }
                            } catch( MeshTypeWithIdentifierNotFoundException ex ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier, "Cannot find PropertyType with " + propertyIdentifier, ex );
                            } catch( UnknownEnumeratedValueException ex ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier, "Invalid key " + content.trim() + " for EnumeratedDataType on PropertyType " + propertyIdentifier, ex );
                            }
                            
                        } else if( EXTENT_VALUE_TAG.equals( grandNodeName )) {
                            String width  = MeshObjectSetProbeUtils.getTextContent( grandAttrs, EXTENT_VALUE_WIDTH_TAG );
                            String height = MeshObjectSetProbeUtils.getTextContent( grandAttrs, EXTENT_VALUE_HEIGHT_TAG );

                            if( width == null || width.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + EXTENT_VALUE_WIDTH_TAG + "' on '" + EXTENT_VALUE_TAG + "'", null );
                            }            
                            if( height == null && height.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + EXTENT_VALUE_HEIGHT_TAG + "' on '" + EXTENT_VALUE_TAG + "'", null );
                            }
                            propValue = ExtentValue.create( Double.parseDouble( width ), Double.parseDouble( height ));

                        } else if( INTEGER_VALUE_TAG.equals( grandNodeName )) {
                            propValue = IntegerValue.parseIntegerValue( content );
                            
                        } else if( FLOAT_VALUE_TAG.equals( grandNodeName )) {
                            propValue = FloatValue.parseFloatValue( content );
                            
                        } else if( MULTIPLICITY_VALUE_TAG.equals( grandNodeName )) {
                            String min = MeshObjectSetProbeUtils.getTextContent( grandAttrs, MULTIPLICITY_VALUE_MIN_TAG );
                            String max = MeshObjectSetProbeUtils.getTextContent( grandAttrs, MULTIPLICITY_VALUE_MAX_TAG );

                            propValue = MultiplicityValue.create(
                                    ( min != null && min.length() > 0 ) ? Integer.parseInt( min ) : MultiplicityValue.N,
                                    ( max != null && max.length() > 0 ) ? Integer.parseInt( max ) : MultiplicityValue.N );
                            
                        } else if( POINT_VALUE_TAG.equals( grandNodeName )) {
                            String x = MeshObjectSetProbeUtils.getTextContent( grandAttrs, POINT_VALUE_X_TAG );
                            String y = MeshObjectSetProbeUtils.getTextContent( grandAttrs, POINT_VALUE_Y_TAG );

                            if( x == null || x.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + POINT_VALUE_X_TAG + "' on '" + POINT_VALUE_TAG + "'", null );
                            }
                            if( y != null || y.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + POINT_VALUE_Y_TAG + "' on '" + POINT_VALUE_TAG + "'", null );
                            }
                            propValue = PointValue.create( Double.parseDouble( x ), Double.parseDouble( y ));

                        } else if( STRING_VALUE_TAG.equals( grandNodeName )) {
                            propValue = StringValue.create( content );

                        } else if( TIME_PERIOD_TAG.equals( grandNodeName )) {
                            String yr  = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_PERIOD_YEAR_TAG );
                            String mon = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_PERIOD_MONTH_TAG );
                            String day = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_PERIOD_DAY_TAG );
                            String hr  = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_PERIOD_HOUR_TAG );
                            String min = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_PERIOD_MINUTE_TAG );
                            String sec = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_PERIOD_SECOND_TAG );

                            if( yr == null || yr.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_PERIOD_YEAR_TAG + "' on '" + TIME_PERIOD_TAG + "'", null );
                            }
                            if( mon == null || mon.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_PERIOD_MONTH_TAG + "' on '" + TIME_PERIOD_TAG + "'", null );
                            }
                            if( day == null || day.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_PERIOD_DAY_TAG + "' on '" + TIME_PERIOD_TAG + "'", null );
                            }
                            if( hr == null || hr.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_PERIOD_HOUR_TAG + "' on '" + TIME_PERIOD_TAG + "'", null );
                            }
                            if( min == null || min.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_PERIOD_MINUTE_TAG + "' on '" + TIME_PERIOD_TAG + "'", null );
                            }
                            if( sec == null || sec.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_PERIOD_SECOND_TAG + "' on '" + TIME_PERIOD_TAG + "'", null );
                            }

                            propValue = TimePeriodValue.create(
                                    Short.parseShort( yr ),
                                    Short.parseShort( mon ),
                                    Short.parseShort( day ),
                                    Short.parseShort( hr ),
                                    Short.parseShort( min ),
                                    Float.parseFloat( sec ));
                            
                        } else if( TIME_STAMP_TAG.equals( grandNodeName )) {
                            String yr  = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_STAMP_YEAR_TAG );
                            String mon = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_STAMP_MONTH_TAG );
                            String day = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_STAMP_DAY_TAG );
                            String hr  = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_STAMP_HOUR_TAG );
                            String min = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_STAMP_MINUTE_TAG );
                            String sec = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TIME_STAMP_SECOND_TAG );

                            if( yr == null || yr.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_STAMP_YEAR_TAG + "' on '" + TIME_STAMP_TAG + "'", null );
                            }
                            if( mon == null || mon.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_STAMP_MONTH_TAG + "' on '" + TIME_STAMP_TAG + "'", null );
                            }
                            if( day == null || day.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_STAMP_DAY_TAG + "' on '" + TIME_STAMP_TAG + "'", null );
                            }
                            if( hr == null || hr.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_STAMP_HOUR_TAG + "' on '" + TIME_STAMP_TAG + "'", null );
                            }
                            if( min == null || min.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_STAMP_MINUTE_TAG + "' on '" + TIME_STAMP_TAG + "'", null );
                            }
                            if( sec == null || sec.length() == 0 ) {
                                throw new ProbeException.SyntaxError( dataSourceIdentifier,  "empty '" + TIME_STAMP_SECOND_TAG + "' on '" + TIME_STAMP_TAG + "'", null );
                            }
                            propValue = TimeStampValue.create(
                                    Short.parseShort( yr ),
                                    Short.parseShort( mon ),
                                    Short.parseShort( day ),
                                    Short.parseShort( hr ),
                                    Short.parseShort( min ),
                                    Float.parseFloat( sec ));

            
                        } else {
                            throw new ProbeException.SyntaxError( dataSourceIdentifier,  "Unknown XML tag: " + grandNodeName, null );
                        }
                    
                        theObjectBeingParsed.addPropertyValue( propValue );
                    }
                } else if( RELATIONSHIP_TAG.equals( childNodeName )) {
                    String otherSideIdentifier = MeshObjectSetProbeUtils.getTextContent( childNode.getAttributes(), IDENTIFIER_TAG );

                    theObjectBeingParsed.addRelationship( otherSideIdentifier );
                    
                    NodeList grandNodeList = childNode.getChildNodes();

                    for( int k=0 ; k<grandNodeList.getLength() ; ++k ) {
                        Node   grandNode     = grandNodeList.item( k );

                        if( grandNode.getNodeType() != Node.ELEMENT_NODE ) {
                            continue;
                        }

                        NamedNodeMap grandAttrs     = grandNode.getAttributes();
                        String       typeIdentifier = MeshObjectSetProbeUtils.getTextContent( grandAttrs, TYPE_TAG );

                        theObjectBeingParsed.getCurrentRelationship().addRoleType( theModelBase.getMeshTypeIdentifierFactory().fromExternalForm( typeIdentifier ));
                    }

                } else {
                    throw new ProbeException.SyntaxError( dataSourceIdentifier, "Unknown XML tag: " + childNodeName, null );
                }
            }
            theBufferedObjects.add( theObjectBeingParsed );
        }
        
        try {
            // then instantiate MeshObjects

            for( ExternalizedMeshObject currentObject : theBufferedObjects ) {

                try {
                    MeshObject            realCurrentObject;
                    NetMeshBaseIdentifier proxy             = currentObject.getProxyTowardsHome();
                    String                currentIdentifier = currentObject.getIdentifier();

                    if( currentIdentifier.indexOf( '#' ) < 0 && proxy == null ) {
                        realCurrentObject = freshMeshBase.getHomeObject();

                        realCurrentObject.bless( MeshObjectSetProbeUtils.lookupEntityTypes( currentObject.getMeshTypes(), theModelBase ));

                    } else if( proxy == null ) {
                        realCurrentObject = life.createMeshObject(
                                constructIdentifier(
                                        dataSourceIdentifier,
                                        currentObject.getIdentifier(),
                                        freshMeshBase.getMeshObjectIdentifierFactory() ),
                                MeshObjectSetProbeUtils.lookupEntityTypes( currentObject.getMeshTypes(), theModelBase ),
                                currentObject.getTimeCreated(),
                                currentObject.getTimeUpdated(),
                                currentObject.getTimeRead(),
                                currentObject.getTimeAutoDeletes());

                    } else {
                        // ForwardReference

                        NetMeshObjectIdentifier fwdRefName = constructIdentifier(
                                dataSourceIdentifier,
                                currentObject.getIdentifier(),
                                freshMeshBase.getMeshObjectIdentifierFactory() );

                        realCurrentObject = life.createForwardReference(
                                proxy,
                                fwdRefName,
                                MeshObjectSetProbeUtils.lookupEntityTypes( currentObject.getMeshTypes(), theModelBase ));
                    }

                    for( int i=currentObject.thePropertyTypes.size()-1 ; i>=0 ; --i ) {
                        PropertyType  type  = MeshObjectSetProbeUtils.lookupPropertyType( currentObject.thePropertyTypes.get( i ), theModelBase );
                        PropertyValue value = currentObject.thePropertyValues.get( i );
                        realCurrentObject.setPropertyValue( type, value );
                    }
                } catch( IsAbstractException ex ) {
                    log.error( ex );
                } catch( EntityBlessedAlreadyException ex ) {
                    log.error( ex );
                } catch( IllegalPropertyTypeException ex ) {
                    log.error( ex );
                } catch( IllegalPropertyValueException ex ) {
                    log.error( ex );
                }
            }

            // finally relate MeshObjects

            for( ExternalizedMeshObject currentObject : theBufferedObjects ) {
                NetMeshObjectIdentifier currentObjectName = constructIdentifier(
                        dataSourceIdentifier,
                        currentObject.getIdentifier(),
                        freshMeshBase.getMeshObjectIdentifierFactory());
                MeshObject realCurrentObject = freshMeshBase.findMeshObjectByIdentifier( currentObjectName );

                for( ExternalizedMeshObject.ExternalizedRelationship currentRelationship : currentObject.theRelationships ) {
                    NetMeshObjectIdentifier otherSideName = constructIdentifier(
                            dataSourceIdentifier,
                            currentRelationship.getIdentifier(),
                            freshMeshBase.getMeshObjectIdentifierFactory());
                    MeshObject otherSide = freshMeshBase.findMeshObjectByIdentifier( otherSideName );

                    if( otherSide == null ) {
                        throw new ProbeException.SyntaxError( dataSourceIdentifier, "Referenced MeshObject could not be found: " + otherSide, null );
                    }
                    try {
                        realCurrentObject.relate( otherSide );
                    } catch( RelatedAlreadyException ex ) {
                        // this must be the other side of what we related already
                    }
                    try {
                        realCurrentObject.blessRelationship( MeshObjectSetProbeUtils.lookupRoleTypes( currentRelationship.theRoleTypes, theModelBase ), otherSide );
                    } catch( BlessedAlreadyException ex ) {
                        // this must be the other side of what we related already
                    } catch( EntityNotBlessedException ex ) {
                        log.error( ex );
                    } catch( NotRelatedException ex ) {
                        log.error( ex );
                    } catch( IsAbstractException ex ) {
                        log.error( ex );
                    }
                }
            }

        
        } catch( NotPermittedException ex ) {
            throw new ProbeException.Other( dataSourceIdentifier, ex );

        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, ex );

        } catch( MeshTypeNotFoundException ex ) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, ex );
        }
    }

    /**
     * Helper method to construct a fully-qualified NetMeshObjectIdentifier, given a String
     * in the XML file that represents the Identifier, and a NetMeshBaseIdentifier as context.
     * 
     * @param dataSourceIdentifier identifies the data source
     * @param externalForm the external form of the NetNeshBaseIdentifier
     * @param meshObjectIdentifierFactory factory for MeshObjectIdentifiers
     * @return the created NetMeshObjectIdentifier
     * @throws org.infogrid.probe.ProbeException.SyntaxError thrown if a syntax error was discovered
     */
    protected NetMeshObjectIdentifier constructIdentifier(
            NetMeshBaseIdentifier          dataSourceIdentifier,
            String                         externalForm,
            NetMeshObjectIdentifierFactory meshObjectIdentifierFactory )
        throws
            ProbeException.SyntaxError
    {
        try {
            NetMeshObjectIdentifier ret;

            Matcher m = VARIABLE_PATTERN.matcher( externalForm );
            if( m.find() ) {
                String variable = m.group( 1 );
                String replacement = theVariableReplacements.get( variable );
                if( replacement == null ) {
                    throw new ProbeException.SyntaxError( dataSourceIdentifier, "Cannot resolve variable " + variable, null );
                }
                String newExternalForm = externalForm.replaceAll( VARIABLE_PATTERN.pattern(), replacement );
                ret = meshObjectIdentifierFactory.fromExternalForm( dataSourceIdentifier, newExternalForm );
            } else {
                ret = meshObjectIdentifierFactory.fromExternalForm( dataSourceIdentifier, externalForm );
            }
            return ret;

        } catch( StringRepresentationParseException ex ) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, ex );
        }
    }
    
    /**
     * Helper method to construct a fully-qualified NetNeshBaseIdentifier, given a String
     * in the XML file that represents the Identifier, and a NetMeshBaseIdentifier as context.
     * 
     * @param dataSourceIdentifier identifies the data source
     * @param externalForm the external form of the NetNeshBaseIdentifier
     * @param meshBaseIdentifierFactory factory for MeshBaseIdentifiers
     * @return the created NetNeshBaseIdentifier
     * @throws org.infogrid.probe.ProbeException.SyntaxError thrown if a syntax error was discovered
     */
    protected NetMeshBaseIdentifier constructNetworkIdentifier(
            NetMeshBaseIdentifier        dataSourceIdentifier,
            String                       externalForm,
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
        throws
            ProbeException.SyntaxError
    {
        try {
            NetMeshBaseIdentifier ret = meshBaseIdentifierFactory.guessFromExternalForm( dataSourceIdentifier, externalForm );

            theVariableReplacements.put( externalForm, ret.toExternalForm() );
            
            return ret;

        } catch( StringRepresentationParseException ex ) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, ex );
        }
    }

    /**
     * Pattern indicating a variable.
     */
    public static final Pattern VARIABLE_PATTERN = Pattern.compile( "\\$\\{(.*)\\}" );

    /**
     * The table of variables and their replacements.
     */
    protected HashMap<String,String> theVariableReplacements = new HashMap<String,String>();
}
