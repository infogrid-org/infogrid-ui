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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//
package org.infogrid.jee.viewlet.json;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.primitives.BlobDataType;
import org.infogrid.model.primitives.BooleanDataType;
import org.infogrid.model.primitives.DataType;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.EnumeratedDataType;
import org.infogrid.model.primitives.EnumeratedValue;
import org.infogrid.model.primitives.FloatDataType;
import org.infogrid.model.primitives.IntegerDataType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.model.primitives.StringDataType;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.TimeStampDataType;
import org.infogrid.model.primitives.TimeStampValue;
import org.infogrid.model.primitives.text.ModelPrimitivesStringRepresentationDirectorySingleton;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.SimpleStringRepresentationParameters;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * Encodes a graph of objects into Json on the output stream. Cycles in the
 * graph are broken by a dictionary. Objects encountered for the second time are
 * output as a reference to their external identifier.
 *
 */
public class MeshObjectJsonEncoder
        extends AbstractObjectInContext {

    /**
     * Factory method that constructs an instance.
     * 
     * @param structured     the Structured response
     * @param saneRequest    the sane request
     * @param ctxt           the context
     * @return               a MeshObjectJsonEncoder
     * @throws IOException   an input output exception
     */
    public static MeshObjectJsonEncoder create(
            StructuredResponse structured,
            SaneRequest saneRequest,
            Context ctxt)
            throws IOException {
        return new MeshObjectJsonEncoder(structured, saneRequest, ctxt);
    }

    /**
     * constr. - do not use, use static create
     * 
     * @param structured    the structured response.
     * @param saneRequest   the sane request.
     * @param ctxt          the context.
     * @throws IOException  an input output exception.
     */
    protected MeshObjectJsonEncoder(
            StructuredResponse structured,
            SaneRequest saneRequest,
            Context ctxt)
            throws IOException {
        super(ctxt);
        HttpServletResponse response = structured.getDelegate();
        OutputStream theOutputStream = response.getOutputStream();
        this.theSaneRequest = saneRequest;
        this.theJsonFactory = new JsonFactory();
        this.theJsonGenerator = theJsonFactory.createJsonGenerator(theOutputStream, JsonEncoding.UTF8);
        try {
            theIgnoredBlessings = new HashSet<String>();
            getAttributes();
            theVisitedMeshObjects = new HashSet<MeshObject>();
        } catch (AttributeValueException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errorOut(ex);
        }
    }

    /**
     * Parse the attributes from the URL into instance variables.
     * TODO - collect up all the errors and throw once.
     * 
     * @throws AttributeValueException   a checked exception to indicate errors in the URL attributes. 
     */
    private void getAttributes()
            throws AttributeValueException {
        theRootURI = theSaneRequest.getAbsoluteContextUriWithSlash();
        String value = theSaneRequest.getUrlArgument(LEVEL_ATTRIBUTE_NAME); // how deep
        if (value != null) {
            try {
                theTargetLevel = Integer.parseInt(value);
                if (theTargetLevel < 0) {
                    throw new AttributeValueException("The traversal level must be 0 or more", "4");
                }
            } catch (NumberFormatException ex) {
                throw new AttributeValueException("The traversal level must be 0 or more", "4");
            }
        } else {
            theTargetLevel = 0;
        }
        value = theSaneRequest.getUrlArgument(DATEENCODING_ATTRIBUTE_NAME); // pesky date encoding
        if (value != null) {
            if (DATEENCODING_VALUE_FN.equals(value)) {
                theDateEncoding = DATE_ENCODING_FN;
            } else if (DATEENCODING_VALUE_STR.equals(value)) {
                theDateEncoding = DATE_ENCODING_STR;
            } else {
                throw new AttributeValueException("The date encoding value must be either: " + DATEENCODING_VALUE_FN + ", or: " + DATEENCODING_VALUE_STR, "6");
            }
        } else {
            theDateEncoding = DATE_ENCODING_FN;
        }
        String[] values = theSaneRequest.getMultivaluedUrlArgument(IGNOREBLESSING_ATTRIBUTE_NAME); // unwanted blessings
        if (values != null && values.length != 0) {
            theIgnoredBlessings.addAll(Arrays.asList(values));
        }
        value = theSaneRequest.getUrlArgument(META_ATTRIBUTE_NAME); // output meta: only, include, no|false
        if (value != null) {
            if ("YES".equals(value.toUpperCase(Locale.US)) || "TRUE".equals(value.toUpperCase(Locale.US))) {
                theMeta = Meta.yes;
            } else if ("NO".equals(value.toUpperCase(Locale.US)) || "FALSE".equals(value.toUpperCase(Locale.US))) {
                theMeta = Meta.no;
            } else if ("ONLY".equals(value.toUpperCase(Locale.US))) {
                theMeta = Meta.only;
            }
        } else {
            theMeta = Meta.no; // default output meta with the entity
        }
        value = theSaneRequest.getUrlArgument(TRIMSA_ATTRIBUTE_NAME); // remove the SA prefix
        if (value != null) {
            if ("YES".equals(value.toUpperCase(Locale.US)) || "TRUE".equals(value.toUpperCase(Locale.US))) {
                theTrimSa = true;
            }
        } else {
            theTrimSa = true;
        }
    }

    /**
     * Write the error output.
     *
     * @param ex            the local checked exception which carries the first encountered error.
     * @throws IOException  an input output exception.
     */
    private void errorOut(AttributeValueException ex)
            throws IOException {
        theJsonGenerator.writeStartObject();
        theJsonGenerator.writeFieldName("id");
        theJsonGenerator.writeString(ex.getId());
        theJsonGenerator.writeFieldName("msg");
        theJsonGenerator.writeString(ex.getMessage());
        theJsonGenerator.writeEndObject();
        theJsonGenerator.flush();
        theJsonGenerator.close();
    }

    /**
     * Outputs the object graph as Json, to level n. Follows all relationships
     * away from the object, stops if it's visited the object before, or reaches
     * the targetLevel. A targetLevel of 0 means no limit - go to infinity.
     * There is a risk it could output the whole meshbase.
     *
     * @param theObject            the root MeshObject to be written as json.
     * @throws IOException         an input output exception.
     */
    public void write(MeshObject theObject)
            throws IOException {
        writeObject(theObject, 0); // start at top level 0
        theJsonGenerator.flush();
        theJsonGenerator.close();
    }

    /**
     * Write a graph traced from this object to the targetLevel
     *
     * @param theObject           a MeshObject to write.
     * @param thisLevel           the current level in the object graph.
     * @throws IOException
     */
    private void writeObject(MeshObject theObject,
            int thisLevel)
            throws IOException {
        String entityTypeID = theObject.getIdentifier().toExternalForm();
        if (theVisitedMeshObjects.contains(theObject)) { // here before? - leave
            if (theMeta == Meta.only) {
                return;
            } else {
                theJsonGenerator.writeStartObject();
                theJsonGenerator.writeFieldName("id");
                theJsonGenerator.writeString(entityTypeID);
                theJsonGenerator.writeEndObject();
                return;
            }
        }
        if (theTargetLevel != 0 && theTargetLevel == thisLevel++) { // graphed? - leave
            theJsonGenerator.writeEndObject();
            return;
        }
        theVisitedMeshObjects.add(theObject);
        theJsonGenerator.writeStartObject();
        if (theMeta != Meta.only) {
            theJsonGenerator.writeFieldName("id");
            theJsonGenerator.writeString(entityTypeID);
        }
        if (theMeta == Meta.only || theMeta == Meta.yes) {
            writeMeta(theObject);
        }
        if (theMeta != Meta.only) {
            writePropertyValues(theObject);
        }
        writeRelationships(theObject, thisLevel);
        theJsonGenerator.writeEndObject();
    }

    /**
     * Write the meta data this object.
     *
     * @param theObject       a mesh object for which this method writes its meta information.
     * @throws IOException    an input output exception.
     */
    private void writeMeta(MeshObject theObject)
            throws IOException {
        EntityType[] entityTypes = theObject.getTypes();
        theJsonGenerator.writeArrayFieldStart("types");
        String entityTypeId;
        for (EntityType entityType : entityTypes) {
            entityTypeId = entityType.getIdentifier().toExternalForm();
            if (ignoreTypeId(entityTypeId)) {
                continue;
            }
            theJsonGenerator.writeStartObject();
            theJsonGenerator.writeArrayFieldStart(entityTypeId);
            writePropertyTypes(theObject);
            theJsonGenerator.writeEndArray();
            theJsonGenerator.writeEndObject();
        }
        theJsonGenerator.writeEndArray();
    }

    /**
     * Write the property types as an object array
     *
     * @param theObject        the mesh object for which this method writes its property types.
     * @throws IOException     an input output exception.
     */
    private void writePropertyTypes(MeshObject theObject)
            throws IOException {
        PropertyType[] propertyTypes = theObject.getAllPropertyTypes();
        DataType theDataType;
        for (PropertyType propertyType : propertyTypes) {
            theDataType = propertyType.getDataType();
            theJsonGenerator.writeStartObject();
            writePropertyId(propertyType);
            theJsonGenerator.writeString(theDataType.getName());
            theJsonGenerator.writeEndObject();
        }
    }

    /**
     * Helper to ignore typeid regex's
     *
     * @param entityTypeId     the string id of an entity type. 
     * @return boolean         whether or not to ignore mesh objects with this id.
     */
    private boolean ignoreTypeId(String entityTypeId) {
        if (theIgnoredBlessings.contains(entityTypeId)) { // exact match
            return true;
        } else {
            for (String typeIdPattern : theIgnoredBlessings) {
                if (entityTypeId.matches(typeIdPattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Write a property id
     *
     * @param propertyType     a property type who's id is to be written by this method.
     * @throws IOException     an input output exception.
     */
    private void writePropertyId(PropertyType propertyType)
            throws IOException {
        String propertyId = propertyType.getIdentifier().toExternalForm();
        if (theTrimSa) {
            propertyId = propertyId.substring(propertyId.indexOf("/") + 1);
        }
        theJsonGenerator.writeFieldName(propertyId);
    }

    /**
     * Write the object's property names and values
     *
     * @param theObject        the mesh object for which this method writes its property values.
     * @throws IOException     an input output exception
     */
    private void writePropertyValues(MeshObject theObject)
            throws IOException {
        PropertyType[] propertyTypes = theObject.getAllPropertyTypes();
        PropertyValue thePropertyValue;
        DataType theDataType;
        EntityType entityType;
        for (PropertyType propertyType : propertyTypes) {
            theDataType = propertyType.getDataType();
            entityType = (EntityType) propertyType.getAttributableMeshType();
            if (!ignoreTypeId(entityType.getIdentifier().toExternalForm())) {
                writePropertyId(propertyType);
                try {
                    thePropertyValue = theObject.getPropertyValue(propertyType);
                    if (thePropertyValue == null) {
                        theJsonGenerator.writeNull();
                    } else if (theDataType instanceof BooleanDataType) {
                        theJsonGenerator.writeBoolean((Boolean) thePropertyValue.value());
                    } else if (theDataType instanceof FloatDataType) {
                        theJsonGenerator.writeNumber((Float) thePropertyValue.value());
                    } else if (theDataType instanceof IntegerDataType) {
                        theJsonGenerator.writeNumber((Long) thePropertyValue.value());
                    } else if (theDataType instanceof TimeStampDataType) {
                        if (theDateEncoding == DATE_ENCODING_FN) {
                            theJsonGenerator.writeRawValue("\"\\/Date(" + Long.toString(((TimeStampValue) thePropertyValue).getAsMillis()) + ")\\/\"");
                        } else if (theDateEncoding == DATE_ENCODING_STR) {
                            TimeStampValue realValue = (TimeStampValue) thePropertyValue;
                            theJsonGenerator.writeString(realValue.getAsRfc3339String());
                        }
                    } else if (theDataType instanceof BlobDataType) {
                        RestfulJeeFormatter theFormatter = getContext().findContextObjectOrThrow(RestfulJeeFormatter.class);
                        String objectId = theFormatter.formatMeshObjectIdentifier(theSaneRequest, theObject, "url", -1);
                        theJsonGenerator.writeString(theRootURI
                                + objectId
                                + "?lid-format=viewlet:org.infogrid.jee.viewlet.blob.BlobViewlet"
                                + "&propertytype="
                                + propertyType.getIdentifier().toExternalForm());
                    } else if (theDataType instanceof StringDataType) {
                        StringValue realValue = (StringValue) thePropertyValue;
                        theJsonGenerator.writeString(realValue.value());
                    } else if (theDataType instanceof EnumeratedDataType) {
                        EnumeratedValue realValue = (EnumeratedValue) thePropertyValue;
                        theJsonGenerator.writeString(realValue.value());
                    } else {
                        writeString(thePropertyValue);
                    }
                } catch (IllegalPropertyTypeException ex) {
                    log.error(ex);
                } catch (NotPermittedException ex) {
                    log.error(ex);
                } catch (StringifierException ex) {
                    log.error(ex);
                }
            }
        }
    }

    /**
     * Helper to write the property value as a string
     *
     * @param thePropertyValue          a property value which this method writes as json.
     * @throws IOException              an input output exception.
     * @throws StringifierException     an exception that is thrown if the property value can not be represented as a string.
     */
    private void writeString(PropertyValue thePropertyValue)
            throws IOException,
            StringifierException {
        StringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        ModelPrimitivesStringRepresentationDirectorySingleton.initialize();
        StringRepresentation sr = ModelPrimitivesStringRepresentationDirectorySingleton.getSingleton().get(StringRepresentationDirectory.TEXT_PLAIN_NAME);
        String value = PropertyValue.toStringRepresentationOrNull(thePropertyValue, sr, pars);
        theJsonGenerator.writeString(value);
    }

    /**
     * Write the object's relationships
     *
     * @param theObject        the mesh object for which this method writes its relationships.
     * @param thisLevel        the level of this object in the graph.
     * @throws IOException     an input output exception.
     */
    private void writeRelationships(MeshObject theObject,
            int thisLevel)
            throws IOException {
        RoleType[] roleTypes = theObject.getRoleTypes();
        MeshObjectSet meshObjects;
        CursorIterator<MeshObject> iter;
        for (RoleType roleType : roleTypes) {
            meshObjects = theObject.traverse(roleType);
            iter = meshObjects.iterator();
            theJsonGenerator.writeArrayFieldStart(roleType.getIdentifier().toExternalForm());
            while (iter.hasNext()) {
                writeObject(iter.next(), thisLevel);
            }
            theJsonGenerator.writeEndArray();
        }
    }
    /**
     * The default MeshBase, if any.
     */
    protected MeshBase theDefaultMeshBase;
    /**
     * Date encoding options
     */
    private static final int DATE_ENCODING_FN = 1;
    private static final int DATE_ENCODING_STR = 0;
    /**
     * URL arguments
     */
    private static final String LEVEL_ATTRIBUTE_NAME = "level";                    // how many levels to traverse
    private static final String DATEENCODING_ATTRIBUTE_NAME = "dateenc";           // how to encode dates
    private static final String IGNOREBLESSING_ATTRIBUTE_NAME = "ignoreblessing";  // skip blessings
    private static final String META_ATTRIBUTE_NAME = "meta";                      // how to treat meta info
    private static final String TRIMSA_ATTRIBUTE_NAME = "trimsa";                  // remove subject area prefix

    /**
     * constrains the meta URL argument
     */
    private enum Meta {

        only, no, yes
    }
    /**
     * Date encoding options
     */
    private static final String DATEENCODING_VALUE_FN = "func"; // default asp.net
    private static final String DATEENCODING_VALUE_STR = "string"; // return the date as a string
    /**
     * Blessings that we want to ignore
     */
    private HashSet<String> theIgnoredBlessings;
    /**
     * who sent me
     */
    private String theRootURI;
    /**
     * How deep to traverse, 0 = infinity
     */
    private int theTargetLevel;
    /**
     * Date encoding selection - defaults to readable
     */
    private int theDateEncoding;
    /**
     * Dump meta data
     */
    private Meta theMeta;
    /**
     * Trim subject area names from the front of property names
     */
    private boolean theTrimSa;
    /**
     * Json
     */
    private JsonFactory theJsonFactory;
    private JsonGenerator theJsonGenerator;
    /**
     * Cycle dictionary
     */
    private HashSet<MeshObject> theVisitedMeshObjects;
    /**
     * cached for formatting blobs
     */
    private SaneRequest theSaneRequest;
    
    /**
     * logging
     */
    private static final Log log = Log.getLogInstance(MeshObjectJsonEncoder.class); // our own, private logger
}
