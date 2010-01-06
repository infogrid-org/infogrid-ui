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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.probe.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
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
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.BlobDataType;
import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.primitives.BooleanValue;
import org.infogrid.model.primitives.ColorValue;
import org.infogrid.model.primitives.EnumeratedDataType;
import org.infogrid.model.primitives.ExtentValue;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
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
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.util.Base64;
import org.infogrid.util.XmlUtils;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * The InfoGrid Xml Probe in SAX. FIXME. This is work in progress which hopefully can
 * replace the DomMeshObjectSetProbe some day, as it is more resource-efficient.
 * But so far it can't.
 */
public class SaxMeshObjectSetProbe
        extends
            DefaultHandler
        implements
            MeshObjectSetProbeTags
{
    private static Log log = Log.getLogInstance( SaxMeshObjectSetProbe.class ); // our own, private logger

    /**
     * Constructor.
     */
    public SaxMeshObjectSetProbe()
    {
        // no op
    }

    /**
     * <p>Parse an incoming XML stream and instantiate corresponding MeshObjects.</p>
     * <p>This method declares
     * many different types of Exceptions; that enables the Probe Framework to handle many
     * possible error conditions out of the box, thereby making Probe programming easier.
     * Note that many of the declared Exceptions, if actually thrown, indicate a programming
     * error in the Probe implementation (e.g. IsAbstractException).</p>
     * <p>The Probe framework invokes this method with an open Transaction on the current Thread;
     * the Probe developer does not have to worry about Transactions.</p>
     * 
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param coherenceSpecification the type of data coherence that is requested by the application. Probe
     *         implementors may ignore this parameter, letting the Probe framework choose its own policy.
     *         If the Probe chooses to define its own policy (considering or ignoring this parameter), the
     *         Probe must bless the Probe's HomeObject with a subtype of <code>ProbeUpdateSpecification</code> (defined
     *         in the <code>org.infogrid.model.Probe</code> Subject Area) and suitable Property
     *         values that reflect the policy.
     * @param stream the InputStream to read from
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
     * @throws TransactionException a Transaction problem occurred. Throwing this typically indicates a programming error.
     * @throws URISyntaxException thrown if a URI was constructed in an invalid way
     */    
    public void parseDocument(
            NetMeshBaseIdentifier  dataSourceIdentifier,
            CoherenceSpecification coherenceSpecification,
            InputStream            stream,
            StagingMeshBase        freshMeshBase )
        throws
            IsAbstractException,
            EntityBlessedAlreadyException,
            EntityNotBlessedException,
            RelatedAlreadyException,
            NotRelatedException,
            MeshObjectIdentifierNotUniqueException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException,
            TransactionException,
            NotPermittedException,
            ProbeException,
            IOException,
            ModuleException,
            URISyntaxException
    {
        MeshBaseLifecycleManager life = freshMeshBase.getMeshBaseLifecycleManager();

        theMeshBase  = freshMeshBase;
        theModelBase = freshMeshBase.getModelBase();

        try {
            theBufferedObjects = new ArrayList<ExternalizedMeshObject>();
            
            // first parse
            theParser.parse( stream, this );

            // then instantiate MeshObjects
            
            for( ExternalizedMeshObject currentObject : theBufferedObjects ) {
                MeshObject realCurrentObject = life.createMeshObject(
                        freshMeshBase.getMeshObjectIdentifierFactory().fromExternalForm( currentObject.getIdentifier() ),
                        MeshObjectSetProbeUtils.lookupEntityTypes( currentObject.getMeshTypes(), freshMeshBase.getModelBase() ),
                        currentObject.getTimeCreated(),
                        currentObject.getTimeUpdated(),
                        currentObject.getTimeRead(),
                        currentObject.getTimeAutoDeletes() );

                for( int i=currentObject.thePropertyTypes.size()-1 ; i>=0 ; --i ) {
                    PropertyType  type  = MeshObjectSetProbeUtils.lookupPropertyType( currentObject.thePropertyTypes.get( i ), freshMeshBase.getModelBase() );
                    PropertyValue value = currentObject.thePropertyValues.get( i );
                    realCurrentObject.setPropertyValue( type, value );
                }
            }

            // finally relate MeshObjects

            for( ExternalizedMeshObject currentObject : theBufferedObjects ) {
                MeshObject realCurrentObject = freshMeshBase.findMeshObjectByIdentifier(
                        freshMeshBase.getMeshObjectIdentifierFactory().fromExternalForm( currentObject.getIdentifier() ));

                for( ExternalizedMeshObject.ExternalizedRelationship currentRelationship : currentObject.theRelationships ) {
                    MeshObject otherSide = freshMeshBase.findMeshObjectByIdentifier(
                            freshMeshBase.getMeshObjectIdentifierFactory().fromExternalForm( currentRelationship.getIdentifier() ));

                    try {
                        realCurrentObject.relate( otherSide );
                    } catch( RelatedAlreadyException ex ) {
                        // this must be the other side of what we related already
                    }
                    try {
                        realCurrentObject.blessRelationship( MeshObjectSetProbeUtils.lookupRoleTypes( currentRelationship.theRoleTypes, freshMeshBase.getModelBase() ), otherSide );
                    } catch( BlessedAlreadyException ex ) {
                        // this must be the other side of what we related already
                    }
                }
            }

        } catch( SAXException ex ) {
            log.error( ex );

        } catch( IOException ex ) {
            log.error( ex );

        } catch( NotPermittedException ex ) {
            throw new ProbeException.Other( dataSourceIdentifier, ex );

        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, ex );

        } catch( MeshTypeNotFoundException ex ) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, ex );

        } catch( StringRepresentationParseException ex ) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, ex );

        } finally {
            theBufferedObjects = null; // don't use memory when we don't need to
        }
    }
    
    /**
     * SAX found some characters.
     *
     * @param ch the character array
     * @param start the start index
     * @param length the number of characters
     */
    @Override
    public void characters(
            char [] ch,
            int     start,
            int     length )
    {
        try {
            if( length > 0 ) {
                if( theCharacters == null ) {
                    theCharacters = new StringBuffer();
                }
                theCharacters.append( ch, start, length );
            }
        } catch( RuntimeException ex ) {
            log.error( ex ); // internal error, no need to throw SAXParseException
        }
    }

    /**
     * SAX says a new element starts.
     *
     * @param namespaceURI URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @param attrs the Attributes at this element
     */
    @Override
    public void startElement(
            String     namespaceURI,
            String     localName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {
        log.info( "Starting element " + qName );
        
        theCharacters = null; // if we haven't processed them so far, we never will

        if( MESHOBJECT_SET_TAG.equals( qName )) {
            // no op
        } else if( MESHOBJECT_TAG.equals( qName )) {
            String identifier      = attrs.getValue( IDENTIFIER_TAG );
            String timeCreated     = attrs.getValue( TIME_CREATED_TAG );
            String timeUpdated     = attrs.getValue( TIME_UPDATED_TAG );
            String timeRead        = attrs.getValue( TIME_READ_TAG );
            String timeAutoDeletes = attrs.getValue( TIME_AUTO_DELETES_TAG );

            theObjectBeingParsed = new ExternalizedMeshObject();
            
            if( identifier != null && identifier.length() > 0 ) {
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

        } else if( MESH_TYPE_TAG.equals( qName )) {
            // no op
        } else if( PROPERTY_TYPE_TAG.equals( qName )) {
            String type = attrs.getValue( TYPE_TAG );
            
            if( type != null && type.length() > 0 ) {
                theObjectBeingParsed.addPropertyType( theModelBase.getMeshTypeIdentifierFactory().fromExternalForm( type ));
            } else {
                log.error( "empty '" + TYPE_TAG + "' on '" + PROPERTY_TYPE_TAG + "'" );
            }
        } else if( ROLE_TYPE_TAG.equals( qName )) {
            String type = attrs.getValue( TYPE_TAG );
            
            if( type != null && type.length() > 0 ) {
                theObjectBeingParsed.getCurrentRelationship().addRoleType( theModelBase.getMeshTypeIdentifierFactory().fromExternalForm( type ));
            } else {
                log.error( "empty '" + TYPE_TAG + "'" );
            }
        } else if( RELATIONSHIP_TAG.equals( qName )) {
            String identifier = attrs.getValue( IDENTIFIER_TAG );

            if( identifier != null && identifier.length() > 0 ) {
                theObjectBeingParsed.addRelationship( identifier );
            } else {
                log.error( "empty '" + IDENTIFIER_TAG + "' on '" + RELATIONSHIP_TAG + "'" );
            }

        } else if( BLOB_VALUE_TAG.equals( qName )) {
            String mime      = attrs.getValue( BLOB_VALUE_MIME_TAG );
            String loadFrom  = attrs.getValue( BLOB_VALUE_LOAD_TAG );

            if( mime != null && mime.length() > 0 ) {
                BlobValue propertyValue;
                if( loadFrom != null && loadFrom.length() > 0 ) {
                    propertyValue = BlobDataType.theAnyType.createBlobValueByLoadingFrom( loadFrom, mime );
                } else {
                    propertyValue = BlobDataType.theAnyType.createBlobValue( "placeholder", mime ); // that's a big ugly, but why not
                }
                theObjectBeingParsed.setCurrentPropertyValue( propertyValue );
            } else {
                log.error( "empty '" + BLOB_VALUE_MIME_TAG + "' on '" + BLOB_VALUE_TAG + "'" );
            }
            
        } else if( BOOLEAN_VALUE_TAG.equals( qName )) {
            // no op
        } else if( COLOR_VALUE_TAG.equals( qName )) {
            String red   = attrs.getValue( COLOR_VALUE_RED_TAG );
            String green = attrs.getValue( COLOR_VALUE_GREEN_TAG );
            String blue  = attrs.getValue( COLOR_VALUE_BLUE_TAG );
            String alpha = attrs.getValue( COLOR_VALUE_ALPHA_TAG );
            
            theObjectBeingParsed.setCurrentPropertyValue( ColorValue.create(
                    Float.parseFloat( red ),
                    Float.parseFloat( green ),
                    Float.parseFloat( blue ),
                    Float.parseFloat( alpha )));
        } else if( ENUMERATED_VALUE_TAG.equals( qName )) {
            // no op
        } else if( EXTENT_VALUE_TAG.equals( qName )) {
            String width  = attrs.getValue( EXTENT_VALUE_WIDTH_TAG );
            String height = attrs.getValue( EXTENT_VALUE_HEIGHT_TAG );

            if( width == null || width.length() == 0 ) {
                log.error( "empty '" + EXTENT_VALUE_WIDTH_TAG + "' on '" + EXTENT_VALUE_TAG + "'" );
            }            
            if( height == null || height.length() == 0 ) {
                log.error( "empty '" + EXTENT_VALUE_HEIGHT_TAG + "' on '" + EXTENT_VALUE_TAG + "'" );
            }
            theObjectBeingParsed.setCurrentPropertyValue( ExtentValue.create( Double.parseDouble( width ), Double.parseDouble( height )));

        } else if( INTEGER_VALUE_TAG.equals( qName )) {
            // no op
        } else if( FLOAT_VALUE_TAG.equals( qName )) {
            // no op
        } else if( MULTIPLICITY_VALUE_TAG.equals( qName )) {
            String min = attrs.getValue( MULTIPLICITY_VALUE_MIN_TAG );
            String max = attrs.getValue( MULTIPLICITY_VALUE_MAX_TAG );

            theObjectBeingParsed.setCurrentPropertyValue( MultiplicityValue.create(
                    ( min != null && min.length() > 0 ) ? Integer.parseInt( min ) : MultiplicityValue.N,
                    ( max != null && max.length() > 0 ) ? Integer.parseInt( max ) : MultiplicityValue.N ));

        } else if( POINT_VALUE_TAG.equals( qName )) {
            String x = attrs.getValue( POINT_VALUE_X_TAG );
            String y = attrs.getValue( POINT_VALUE_Y_TAG );

            if( x == null || x.length() == 0 ) {
                log.error( "empty '" + POINT_VALUE_X_TAG + "' on '" + POINT_VALUE_TAG + "'" );
            }
            if( y == null || y.length() == 0 ) {
                log.error( "empty '" + POINT_VALUE_Y_TAG + "' on '" + POINT_VALUE_TAG + "'" );
            }
            theObjectBeingParsed.setCurrentPropertyValue( PointValue.create( Double.parseDouble( x ), Double.parseDouble( y )));
                    
        } else if( STRING_VALUE_TAG.equals( qName )) {
            // no op
        } else if( TIME_PERIOD_TAG.equals( qName )) {
            String yr  = attrs.getValue( TIME_PERIOD_YEAR_TAG );
            String mon = attrs.getValue( TIME_PERIOD_MONTH_TAG );
            String day = attrs.getValue( TIME_PERIOD_DAY_TAG );
            String hr  = attrs.getValue( TIME_PERIOD_HOUR_TAG );
            String min = attrs.getValue( TIME_PERIOD_MINUTE_TAG );
            String sec = attrs.getValue( TIME_PERIOD_SECOND_TAG );

            if( yr == null || yr.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_YEAR_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            if( mon == null || mon.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_MONTH_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            if( day == null || day.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_DAY_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            if( hr == null || hr.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_HOUR_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            if( min == null || min.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_MINUTE_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            if( sec == null || sec.length() == 0 ) {
                log.error( "empty '" + TIME_PERIOD_SECOND_TAG + "' on '" + TIME_PERIOD_TAG + "'" );
            }
            
            theObjectBeingParsed.setCurrentPropertyValue( TimePeriodValue.create(
                    Short.parseShort( yr ),
                    Short.parseShort( mon ),
                    Short.parseShort( day ),
                    Short.parseShort( hr ),
                    Short.parseShort( min ),
                    Float.parseFloat( sec )));

        } else if( TIME_STAMP_TAG.equals( qName )) {
            String yr  = attrs.getValue( TIME_STAMP_YEAR_TAG );
            String mon = attrs.getValue( TIME_STAMP_MONTH_TAG );
            String day = attrs.getValue( TIME_STAMP_DAY_TAG );
            String hr  = attrs.getValue( TIME_STAMP_HOUR_TAG );
            String min = attrs.getValue( TIME_STAMP_MINUTE_TAG );
            String sec = attrs.getValue( TIME_STAMP_SECOND_TAG );

            if(    yr  != null && yr.length()  > 0
                && mon != null && mon.length() > 0
                && day != null && day.length() > 0
                && hr  != null && hr.length()  > 0
                && min != null && min.length() > 0
                && sec != null && sec.length() > 0 )
            {
                theObjectBeingParsed.setCurrentPropertyValue( TimeStampValue.create(
                        Short.parseShort( yr ),
                        Short.parseShort( mon ),
                        Short.parseShort( day ),
                        Short.parseShort( hr ),
                        Short.parseShort( min ),
                        Float.parseFloat( sec )));
            } else{
                theObjectBeingParsed.setCurrentPropertyValue(  null ); // flag for endElement that the String needs to be parsed
            }

        } else {
            throw new SAXParseException( "Unknown qname " + qName, theLocator );
        }
    }

    /**
     * SAX says an element ends.
     *
     * @param namespaceURI the URI of the namespace
     * @param localName the local name
     * @param qName the qName
     */
    @Override
    public void endElement(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        log.info( "Ending element " + qName );

        if( MESHOBJECT_SET_TAG.equals( qName )) {
            // no op
        } else if( MESHOBJECT_TAG.equals( qName )) {
            theBufferedObjects.add( theObjectBeingParsed );
        } else if( MESH_TYPE_TAG.equals( qName )) {
            // no op
            theObjectBeingParsed.addMeshType( theModelBase.getMeshTypeIdentifierFactory().fromExternalForm( theCharacters.toString().trim() ));
        
        } else if( PROPERTY_TYPE_TAG.equals( qName )) {
            theObjectBeingParsed.setCurrentPropertyValue( null );

        } else if( ROLE_TYPE_TAG.equals( qName )) {
            // no op
        } else if( RELATIONSHIP_TAG.equals( qName )) {
            // no op
        } else if( BLOB_VALUE_TAG.equals( qName )) {
            
            PropertyValue propValue = theObjectBeingParsed.getCurrentPropertyValue();

            if( ( propValue instanceof BlobValue ) && ((BlobValue)propValue).delayedLoadingFrom() == null ) {
                if( ((BlobValue)propValue).getMimeType().startsWith( "text/" ) ) {
                    propValue = BlobDataType.theAnyType.createBlobValue(
                            theCharacters != null ? XmlUtils.cdataDescape( theCharacters.toString().trim()) : "",
                            ((BlobValue)propValue).getMimeType() );
                } else {
                    propValue = BlobDataType.theAnyType.createBlobValue( Base64.base64decode( theCharacters.toString().trim() ), ((BlobValue)propValue).getMimeType() );
                    // This needs to be patched later once we have the instance of BlobDataType
                }
            }
            theObjectBeingParsed.addPropertyValue( propValue );
            
        } else if( BOOLEAN_VALUE_TAG.equals( qName )) {
            theObjectBeingParsed.addPropertyValue( BooleanValue.create( BOOLEAN_VALUE_TRUE_TAG.equals( theCharacters.toString().trim() )));

        } else if( COLOR_VALUE_TAG.equals( qName )) {
            theObjectBeingParsed.addPropertyValue( theObjectBeingParsed.getCurrentPropertyValue() );
            
        } else if( ENUMERATED_VALUE_TAG.equals( qName )) {
            MeshTypeIdentifier propertyTypeName = theObjectBeingParsed.getPropertyTypes()[ theObjectBeingParsed.getPropertyTypes().length-1 ];
            try {
                MeshType mt = theModelBase.findMeshTypeByIdentifier( propertyTypeName );
                if( ( mt instanceof PropertyType ) && ((PropertyType)mt).getDataType() instanceof EnumeratedDataType ) {
                    EnumeratedDataType realPt = (EnumeratedDataType) ((PropertyType)mt).getDataType();
                    theObjectBeingParsed.addPropertyValue( realPt.select( theCharacters.toString().trim() )); // FIXME?
                } else {
                    log.error( "MeshType with " + propertyTypeName + " is not a PropertyType" );
                }
            } catch( MeshTypeWithIdentifierNotFoundException ex) {
                log.error( "Cannot find PropertyType with " + propertyTypeName, ex );
            } catch( UnknownEnumeratedValueException ex ) {
                log.error( "Invalid key " + theCharacters.toString().trim() + " for EnumeratedDataType on PropertyType " + propertyTypeName, ex );
            }

        } else if( EXTENT_VALUE_TAG.equals( qName )) {
            theObjectBeingParsed.addPropertyValue( theObjectBeingParsed.getCurrentPropertyValue() );

        } else if( INTEGER_VALUE_TAG.equals( qName )) {
            theObjectBeingParsed.addPropertyValue( IntegerValue.parseIntegerValue( theCharacters.toString() ));

        } else if( FLOAT_VALUE_TAG.equals( qName )) {
            theObjectBeingParsed.addPropertyValue( FloatValue.parseFloatValue( theCharacters.toString() ));

        } else if( MULTIPLICITY_VALUE_TAG.equals( qName )) {
            theObjectBeingParsed.addPropertyValue( theObjectBeingParsed.getCurrentPropertyValue() );

        } else if( POINT_VALUE_TAG.equals( qName )) {
            theObjectBeingParsed.addPropertyValue( theObjectBeingParsed.getCurrentPropertyValue() );

        } else if( STRING_VALUE_TAG.equals( qName )) {
            theObjectBeingParsed.addPropertyValue( StringValue.create( theCharacters.toString() )); // let's not strip() here

        } else if( TIME_PERIOD_TAG.equals( qName )) {
            theObjectBeingParsed.addPropertyValue( theObjectBeingParsed.getCurrentPropertyValue() );

        } else if( TIME_STAMP_TAG.equals( qName )) {
            if( theObjectBeingParsed.getCurrentPropertyValue() != null ) {
                theObjectBeingParsed.addPropertyValue( theObjectBeingParsed.getCurrentPropertyValue() );
            } else {
                try {
                    theObjectBeingParsed.addPropertyValue( TimeStampValue.create( theCharacters.toString().trim() ) );
                } catch( ParseException ex ) {
                    throw new SAXParseException( "Cannot parse RFC 3339 String", theLocator, ex );
                }
            }

        } else {
            throw new SAXParseException( "Unknown qname " + qName, theLocator );
        }
    }
    
    /**
     * Receive a Locator object for document events.
     *
     * @param l A locator for all SAX document events.
     * @see org.xml.sax.ContentHandler#setDocumentLocator
     * @see org.xml.sax.Locator
     */
    @Override
    public void setDocumentLocator(
            Locator l )
    {
        theLocator = l;
    }

    /**
     * The ExternalizedMeshObject that is currently being parsed, if any.
     */
    protected ExternalizedMeshObject theObjectBeingParsed = null;

    /**
     * The MeshBase being parsed into.
     */
    protected MeshBase theMeshBase;
    
    /**
     * Our ModelBase.
     */
    protected ModelBase theModelBase;
    
    /**
     * The character String that is currently being parsed, if any.
     */
    protected StringBuffer theCharacters = null;

    /**
     * The set of MeshObjects being read in.
     */
    protected ArrayList<ExternalizedMeshObject> theBufferedObjects;

    /**
     * The Locator.
     */
    protected Locator theLocator;

    /**
     * For conversion speed, a character table.
     */
    protected static char [] charTable = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * The character encoding that we are using.
     */
    public static final String ENCODING = "UTF-8";
    
    /**
     * Our SAX parser.
     */
    protected static SAXParser theParser;
    
    static {
        try {
            SAXParserFactory fact = SAXParserFactory.newInstance(); // FIXME? new SAXParserFactoryImpl();
            fact.setValidating( true );

            theParser = fact.newSAXParser();
        } catch( Exception ex ) {
            log.error( ex );
        }
    }
}
