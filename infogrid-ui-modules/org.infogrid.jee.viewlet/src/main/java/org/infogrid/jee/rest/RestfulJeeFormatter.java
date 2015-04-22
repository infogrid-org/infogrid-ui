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

package org.infogrid.jee.rest;

import java.lang.reflect.Method;
import java.text.ParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.infogrid.jee.JeeFormatter;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.servlet.TemplatesFilter;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.text.MeshStringRepresentationParameters;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshObjectIdentifierFactory;
import org.infogrid.model.primitives.DataType;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.text.ModelPrimitivesStringRepresentationParameters;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.model.traversal.TraversalTranslatorException;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.SimpleStringRepresentationParameters;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * Collection of utility methods that are useful with InfoGrid JEE applications
 * and aware of InfoGrid REST conventions.
 */
public class RestfulJeeFormatter
        extends
            JeeFormatter
{
    private static final Log log = Log.getLogInstance( RestfulJeeFormatter.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param defaultMeshBase the default MeshBase
     * @param stringRepDir the StringRepresentationDirectory to use
     * @return the created JeeFormatter
     */
    public static RestfulJeeFormatter create(
            MeshBase                      defaultMeshBase,
            StringRepresentationDirectory stringRepDir )
    {
        return new RestfulJeeFormatter( defaultMeshBase, stringRepDir );
    }

    /**
     * Private constructor for subclasses only, use factory method.
     *
     * @param defaultMeshBase the default MeshBase
     * @param stringRepDir the StringRepresentationDirectory to use
     */
    protected RestfulJeeFormatter(
            MeshBase                      defaultMeshBase,
            StringRepresentationDirectory stringRepDir )
    {
        super( stringRepDir );

        theDefaultMeshBase = defaultMeshBase;
    }

    /**
     * <p>Return the value of the (non-nested) property of the specified
     * name, for the specified bean, with no type conversions. If no such object is found,
     * return <code>null</code> instead.</p>
     *
     * @param obj object whose property is to be extracted
     * @param propertyName possibly nested name of the property to be extracted
     * @return the found value, or null
     * @throws NullPointerException if the given object was null
     * @throws IllegalArgumentException thrown if an illegal propertyName was given
     */
    @Override
    public Object getSimpleProperty(
            Object obj,
            String propertyName )
    {
        if( obj == null ) {
            throw new NullPointerException( "Object cannot be null" );
        }

        if( propertyName == null ) {
            throw new IllegalArgumentException( "Property name cannot be null" );
        }
        if( propertyName.length() == 0 ) {
            throw new IllegalArgumentException( "Property name cannot be empty" );
        }

        // try the getter method first. if that fails, try MeshObject-specific methods.

        String getterName = "get" + Character.toUpperCase( propertyName.charAt( 0 )) + propertyName.substring( 1 );

        try {
            Method getterMethod = obj.getClass().getMethod( getterName, (Class []) null );
            Object ret          = getterMethod.invoke( obj, (Object []) null );

            return ret;

        } catch( Throwable ex ) {
            // ignore
        }

        // If this is a MeshObject, attempt to do PropertyType simpleLookup
        if( obj instanceof MeshObject ) {
            MeshObject realObj = (MeshObject) obj;

            if( "timeCreated".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeCreated();
            } else if( "timeUpdated".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeUpdated();
            } else if( "timeRead".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeRead();
            } else if( "timeExpires".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeExpires();
            }

            try {
                Object ret = realObj.getPropertyValueByName( propertyName );
                return ret;

            } catch( MeshTypeNotFoundException ex ) {
                // this didn't work. Ignore and proceed as normal
            } catch( NotPermittedException ex ) {
                return "[access denied]";
            }
        }
        return null;
    }

    /**
     * <p>Return the value of the (non-nested) property of the specified
     * name, for the specified bean, with no type conversions. If no such object is found,
     * throw a <code>JspException</code>.</p>
     *
     * @param obj object whose property is to be extracted
     * @param propertyName possibly nested name of the property to be extracted
     * @return the found value
     * @throws NullPointerException if the given object was null
     * @throws IllegalArgumentException thrown if an illegal propertyName was given
     * @throws JspException thrown if the object's property could not be found
     */
    @Override
    public Object getSimplePropertyOrThrow(
            Object obj,
            String propertyName )
        throws
            JspException
    {
        if( obj == null ) {
            throw new NullPointerException( "Object cannot be null" );
        }

        if( propertyName == null ) {
            throw new IllegalArgumentException( "Property name cannot be null" );
        }
        if( propertyName.length() == 0 ) {
            throw new IllegalArgumentException( "Property name cannot be empty" );
        }

        // If this is a MeshObject, attempt to do PropertyType simpleLookup
        if( obj instanceof MeshObject ) {
            MeshObject realObj = (MeshObject) obj;

            if( "timeCreated".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeCreated();
            } else if( "timeUpdated".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeUpdated();
            } else if( "timeRead".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeRead();
            } else if( "timeExpires".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeExpires();
            }

            try {
                Object ret = realObj.getPropertyValueByName( propertyName );
                return ret;

            } catch( MeshTypeNotFoundException ex ) {
                // this didn't work. Ignore and proceed as normal
            } catch( NotPermittedException ex ) {
                return "[access denied]";
            }
        }

        // construct getter method name
        String getterName = "get" + Character.toUpperCase( propertyName.charAt( 0 )) + propertyName.substring( 1 );

        try {
            Method getterMethod = obj.getClass().getMethod( getterName, (Class []) null );
            Object ret          = getterMethod.invoke( obj, (Object []) null );

            return ret;

        } catch( Throwable ex ) {
            throw new JspException( "Cannot call getter method for property " + propertyName + " on object " + obj );
        }
    }

    /**
     * Find a MeshObject with the given identifier, or return null.

     * @param identifier the MeshObjectIdentifier in String form
     * @return the found MeshObject, or null
     * @throws JspException thrown if the identifier could not be parsed
     */
    public MeshObject findMeshObject(
            String identifier )
        throws
            JspException
    {
        try {
            MeshObject ret = theDefaultMeshBase.findMeshObjectByIdentifier(
                    theDefaultMeshBase.getMeshObjectIdentifierFactory().guessFromExternalForm( identifier ));
            return ret;

        } catch( ParseException ex ) {
            throw new JspException( "Could not parse identifier " + identifier );
        }
    }

    /**
     * Find a MeshObject with the given identifier, or return null.

     * @param identifier the MeshObjectIdentifier
     * @return the found MeshObject, or null
     * @throws JspException thrown if the identifier could not be parsed
     */
    public MeshObject findMeshObject(
            MeshObjectIdentifier identifier )
        throws
            JspException
    {
        MeshObject ret = theDefaultMeshBase.findMeshObjectByIdentifier( identifier );
        return ret;
    }

    /**
     * Find a MeshType by its identifier.
     *
     * @param identifier the MeshTypeIdentifier in String form
     * @return the found MeshType, or null
     * @throws JspException thrown if the identifier could not be parsed
     */
    public MeshType findMeshTypeByIdentifier(
            String identifier )
        throws
            JspException
    {
        ModelBase                 mb     = theDefaultMeshBase.getModelBase();
        MeshTypeIdentifierFactory idFact = mb.getMeshTypeIdentifierFactory();

        MeshTypeIdentifier id = idFact.fromExternalForm( identifier );

        try {
            MeshType ret = mb.findMeshTypeByIdentifier( id );
            return ret;
        } catch( MeshTypeWithIdentifierNotFoundException ex ) {
            // ignore
            return null;
        }
    }

    /**
     * Find a MeshType by its identifier, or throw an Exception
     *
     * @param identifier the MeshTypeIdentifier in String form
     * @return the found MeshType, or null
     * @throws JspException thrown if the identifier could not be parsed
     */
    public MeshType findMeshTypeByIdentifierOrThrow(
            String identifier )
        throws
            JspException
    {
        MeshType ret = findMeshTypeByIdentifier( identifier );
        if( ret == null ) {
            throw new JspException( "Could not find MeshType with identifier " + identifier );
        }
        return ret;
    }

    /**
     * Find a MeshObject with the given identifier, or throw an Exception.

     * @param identifier the MeshObjectIdentifier in String form
     * @return the found MeshObject
     * @throws JspException thrown if the identifier could not be parsed or the MeshObject could not be found
     */
    public MeshObject findMeshObjectOrThrow(
            String identifier )
        throws
            JspException
    {
        MeshObject ret = findMeshObject( identifier );
        if( ret == null ) {
            throw new JspException( "Could not find MeshObject with identifier " + identifier );
        }
        return ret;
    }

    /**
     * Find a MeshObject with the given identifier, or throw an Exception.

     * @param identifier the MeshObjectIdentifier
     * @return the found MeshObject
     * @throws JspException thrown if the identifier could not be parsed or the MeshObject could not be found
     */
    public MeshObject findMeshObjectOrThrow(
            MeshObjectIdentifier identifier )
        throws
            JspException
    {
        MeshObject ret = findMeshObject( identifier );
        if( ret == null ) {
            throw new JspException( "Could not find MeshObject with identifier " + identifier );
        }
        return ret;
    }

    /**
     * Find a PropertyType on a MeshObject by name.
     *
     * @param obj the MeshObject
     * @param name the name of the PropertyType
     * @return the found PropertyType
     * @throws NullPointerException thrown if the MeshObject or the name of the PropertyType were null
     */
    public PropertyType findPropertyType(
            MeshObject obj,
            String     name )
    {
        // tolerate lowercase first characters.

        if( name == null || name.length() == 0 ) {
            throw new NullPointerException( "PropertyType name cannot be empty" );
        }

        try {
            ModelBase mb;
            if( obj != null ) {
                mb = obj.getMeshBase().getModelBase();
            } else {
                mb = theDefaultMeshBase.getModelBase();
            }

            PropertyType ret = mb.findPropertyTypeByIdentifier( mb.getMeshTypeIdentifierFactory().fromExternalForm( name ));
            return ret;

        } catch( MeshTypeWithIdentifierNotFoundException ex ) {
            // nothing
        }

        if( obj == null ) {
            throw new NullPointerException( "Cannot find PropertyType named " + name + " (no MeshObject given)" );
        }
        PropertyType [] allTypes = obj.getAllPropertyTypes();
        for( PropertyType current : allTypes ) {
            if( current.getIdentifier().toExternalForm().equals( name )) {
                return current;
            }
        }

        // Now try by short name
        char firstChar = name.charAt( 0 );
        String capitalizedName;
        if( Character.isUpperCase( firstChar )) {
            capitalizedName = name;
        } else {
            capitalizedName = new StringBuilder( name.length() ).append( Character.toUpperCase( firstChar )).append( name.substring( 1 )).toString();
        }

        for( PropertyType current : allTypes ) {
            if( current.getName().equals( (Object) capitalizedName )) { // StringValue vs. String is correct
                return current;
            }
        }
        return null;
    }

    /**
     * Find a PropertyType on a MeshObject by name.
     *
     * @param obj the MeshObject
     * @param name the name of the PropertyType
     * @return the found PropertyType
     * @throws NullPointerException thrown if the MeshObject or the name of the PropertyType were null
     * @throws JspException thrown if the PropertyType could not be found
     */
    public PropertyType findPropertyTypeOrThrow(
            MeshObject obj,
            String     name )
        throws
            JspException
    {
        PropertyType ret = findPropertyType( obj, name );
        if( ret == null ) {
            throw new JspException( "Could not find property " + name + " on MeshObject " + obj );
        }
        return ret;
    }

    /**
     * Find a TraversalSpecification, or return null.
     *
     * @param startObject the start MeshObject
     * @param traversalTerm the serialized TraversalSpecification
     * @return the found TraversalSpecification, or null
     */
    public TraversalSpecification findTraversalSpecification(
            MeshObject startObject,
            String     traversalTerm )
    {
        TraversalSpecification ret = findRoleTypeByIdentifier( traversalTerm );
        if( ret != null ) {
            return ret;
        }

        Context             context = startObject.getMeshBase().getContext(); // not the best way of finding it, but should work
        TraversalTranslator dict    = context.findContextObject( TraversalTranslator.class );

        String [] traversalTerms = traversalTerm.split( "\\s" );
        try {
            ret = findTraversalSpecificationByTraversalTranslator( startObject, traversalTerms, dict );
        } catch( TraversalTranslatorException ex ) {
            // ignore
            if( log.isDebugEnabled() ) {
                log.debug( ex );
            }
        }
        return ret;
    }

    /**
     * Find a RoleType by its MeshTypeIdentifier.
     *
     * @param name the identifier
     * @return the RoleType, or null
     */
    public RoleType findRoleTypeByIdentifier(
            String name )
    {
        ModelBase                 mb     = theDefaultMeshBase.getModelBase();
        MeshTypeIdentifierFactory idFact = mb.getMeshTypeIdentifierFactory();

        MeshTypeIdentifier id = idFact.fromExternalForm( name );

        try {
            RoleType ret = mb.findRoleTypeByIdentifier( id );
            return ret;
        } catch( MeshTypeWithIdentifierNotFoundException ex ) {
            // ignore
            return null;
        }
    }

    /**
     * Find a TraversalSpecification by asking a TraversalTranslator.
     *
     * @param startObject the start MeshObject
     * @param traversalTerms the terms of the serialized TraversalSpecification
     * @param dict the TraversalTranslator
     * @return the TraversalSpecification, or null
     * @throws TraversalTranslatorException thrown if the traversalTerms could not be translated
     */
    public TraversalSpecification findTraversalSpecificationByTraversalTranslator(
            MeshObject          startObject,
            String []           traversalTerms,
            TraversalTranslator dict )
        throws
            TraversalTranslatorException
    {
        TraversalSpecification ret = dict.translateTraversalSpecification( startObject, traversalTerms );
        return ret;
    }

    /**
     * Find a TraversalSpecification, or throw an Exception.
     *
     * @param startObject the start MeshObject
     * @param traversalTerm the serialized TraversalSpecification
     * @return the found TraversalSpecification
     * @throws JspException thrown if the TraversalSpecification could not be found
     */
    public TraversalSpecification findTraversalSpecificationOrThrow(
            MeshObject startObject,
            String     traversalTerm )
        throws
            JspException
    {
        TraversalSpecification ret = findRoleTypeByIdentifier( traversalTerm );
        if( ret != null ) {
            return ret;
        }

        Context             context = startObject.getMeshBase().getContext(); // not the best way of finding it, but should work
        TraversalTranslator dict    = context.findContextObject( TraversalTranslator.class );

        if( dict == null ) {
            return null;
        }

        String [] traversalTerms = traversalTerm.split( "\\s" );
        try {
            ret = findTraversalSpecificationByTraversalTranslator( startObject, traversalTerms, dict );

        } catch( TraversalTranslatorException ex ) {
            throw new JspException( "Could not find TraversalSpecification " + traversalTerm, ex );
        }
        return ret;
    }

    /**
     * Find a sequence of TraversalSpecifications, or return null.
     *
     * @param startObject the start MeshObject
     * @param traversalTerm the serialized TraversalSpecifications
     * @return the found sequence of TraversalSpecifications, or null
     */
    public TraversalSpecification [] findTraversalSpecificationSequence(
            MeshObject startObject,
            String     traversalTerm )
    {
        Context             context = startObject.getMeshBase().getContext(); // not the best way of finding it, but should work
        TraversalTranslator dict    = context.findContextObject( TraversalTranslator.class );

        if( dict == null ) {
            return null;
        }

        String [] traversalTerms = traversalTerm.split( "\\s" );
        try {
            TraversalSpecification [] ret = dict.translateTraversalSpecifications( startObject, traversalTerms );
            return ret;
        } catch( TraversalTranslatorException ex ) {
            // ignore
            if( log.isDebugEnabled() ) {
                log.debug( ex );
            }
            return null;
        }
    }

    /**
     * Find a sequence of TraversalSpecifications, or throw an Exception.
     *
     * @param startObject the start MeshObject
     * @param traversalTerm the serialized TraversalSpecifications
     * @return the found sequence of TraversalSpecifications
     * @throws JspException thrown if the TraversalSpecification could not be found
     */
    public TraversalSpecification [] findTraversalSpecificationSequenceOrThrow(
            MeshObject startObject,
            String     traversalTerm )
        throws
            JspException
    {
        Context             context = startObject.getMeshBase().getContext(); // not the best way of finding it, but should work
        TraversalTranslator dict    = context.findContextObject( TraversalTranslator.class );

        if( dict == null ) {
            return null;
        }

        String [] traversalTerms = traversalTerm.split( "\\s" );
        try {
            TraversalSpecification [] ret = dict.translateTraversalSpecifications( startObject, traversalTerms );
            return ret;

        } catch( TraversalTranslatorException ex ) {
            throw new JspException( "Could not find TraversalSpecification " + traversalTerm, ex );
        }
    }

    /**
     * Format a PropertyValue.
     *
     * @param pageContext the PageContext object for this page
     * @param value the PropertyValue
     * @param nullString the String to display of the value is null
     * @param stringRepresentation the StringRepresentation for PropertyValues
     * @param overrideFormatString if given, use this String as format String instead of the default
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatPropertyValue(
            PageContext   pageContext,
            PropertyValue value,
            String        nullString,
            String        stringRepresentation,
            String        overrideFormatString,
            int           maxLength,
            boolean       colloquial )
        throws
            StringifierException
    {
        String ret;
        if( value == null ) {
            ret = nullString;
        } else {
            SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

            StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
            SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
            pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, saneRequest.getAbsoluteContextUri() );
            pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, saneRequest.getContextPath() );
            if( maxLength >= 0 ) {
                pars.put( StringRepresentationParameters.MAX_LENGTH, maxLength );
            }
            pars.put( StringRepresentationParameters.COLLOQUIAL, colloquial );

            if( nullString != null ) {
                pars.put( StringRepresentationParameters.NULL_STRING, nullString );
            }
            if( overrideFormatString != null ) {
                pars.put( StringRepresentationParameters.FORMAT_STRING, overrideFormatString );
            }
            ret = value.toStringRepresentation( rep, pars );
        }
        return ret;
    }

    /**
     * Format a Property.
     *
     * @param pageContext the PageContext object for this page
     * @param owningMeshObject the MeshObject that owns this Property
     * @param propertyType the PropertyType of the Property
     * @param editVar name of the HTML form elements to use
     * @param nullString the String to display of the value is null
     * @param stringRepresentation the StringRepresentation for PropertyValues
     * @param overrideFormatString if given, use this String as format String instead of the default
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @param allowNull if applicable, allow null values to be entered in edit mode
     * @param defaultValue if given, use this as the default value instead what is specified in the model
     * @param addText any text to add
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify\
     * @throws IllegalPropertyTypeException thrown if the PropertyType does not exist on this MeshObject
     *         because the MeshObject has not been blessed with a MeshType that provides this PropertyType
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public String formatProperty(
            PageContext   pageContext,
            MeshObject    owningMeshObject,
            PropertyType  propertyType,
            String        editVar,
            int           editIndex,
            String        nullString,
            String        stringRepresentation,
            String        overrideFormatString,
            int           maxLength,
            boolean       colloquial,
            boolean       allowNull,
            PropertyValue defaultValue,
            String        addText )
        throws
            StringifierException,
            IllegalPropertyTypeException,
            NotPermittedException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, saneRequest.getContextPath() );
        if( maxLength >= 0 ) {
            pars.put( StringRepresentationParameters.MAX_LENGTH, maxLength );
        }
        pars.put( StringRepresentationParameters.COLLOQUIAL, colloquial );
        pars.put( ModelPrimitivesStringRepresentationParameters.ALLOW_NULL, allowNull );
        if( owningMeshObject != null ) {
            pars.put( ModelPrimitivesStringRepresentationParameters.MESH_OBJECT, owningMeshObject );
        }
        if( propertyType != null ) {
            pars.put( ModelPrimitivesStringRepresentationParameters.PROPERTY_TYPE, propertyType );
        }
        if( nullString != null ) {
            pars.put( StringRepresentationParameters.NULL_STRING, nullString );
        }
        if( overrideFormatString != null ) {
            pars.put( StringRepresentationParameters.FORMAT_STRING, overrideFormatString );
        }
        if( editVar != null ) {
            pars.put( StringRepresentationParameters.EDIT_VARIABLE, editVar );
        }
        if( defaultValue != null ) {
            pars.put( ModelPrimitivesStringRepresentationParameters.DEFAULT_VALUE, defaultValue );
        }
        if( addText != null ) {
            pars.put( StringRepresentationParameters.ADD_TEXT, addText );
        }
        pars.put( StringRepresentationParameters.EDIT_INDEX, editIndex );

        String ret = propertyType.getDataType().formatProperty(
                owningMeshObject,
                propertyType,
                rep,
                pars );

        return ret;
    }

    /**
     * Format a MeshType.
     *
     * @param pageContext the PageContext object for this page
     * @param type the MeshType to format
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshType(
            PageContext pageContext,
            MeshType    type,
            String      stringRepresentation,
            int         maxLength,
            boolean     colloquial )
        throws
            StringifierException
    {
        // FIXME?

        StringValue name = type.getUserVisibleName();
        if( name == null ) {
            name = type.getName();
        }
        return name.value();
    }

    /**
     * Format a MeshTypeIdentifier.
     *
     * @param pageContext the PageContext object for this page
     * @param identifier the Identifier to format
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshTypeIdentifier(
            PageContext        pageContext,
            MeshTypeIdentifier identifier,
            String             stringRepresentation,
            int                maxLength )
        throws
            StringifierException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.MAX_LENGTH,               maxLength );
        pars.put( StringRepresentationParameters.COLLOQUIAL,               false );
        pars.put( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY, theDefaultMeshBase );
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, saneRequest.getContextPath() );

        String ret = identifier.toStringRepresentation( rep, pars );

        return ret;
    }

    /**
     * Format a DataType.
     *
     * @param pageContext the PageContext in which to format the Property
     * @param type the DataType to format
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatDataType(
            PageContext pageContext,
            DataType    type,
            String      stringRepresentation,
            int         maxLength )
        throws
            StringifierException
    {
        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.MAX_LENGTH,               maxLength );
        pars.put( StringRepresentationParameters.COLLOQUIAL,               false );

        String ret = type.toStringRepresentation( rep, pars );
        return ret;
    }

    /**
     * Format a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject that is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial should the value be emitted in colloquial form
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObject(
            PageContext pageContext,
            MeshObject  mesh,
            String      stringRepresentation,
            int         maxLength,
            boolean     colloquial )
        throws
            StringifierException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.MAX_LENGTH,               maxLength );
        pars.put( StringRepresentationParameters.COLLOQUIAL,               colloquial );
        pars.put( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY, theDefaultMeshBase );
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, saneRequest.getContextPath() );

        String ret = mesh.toStringRepresentation( rep, pars );
        return ret;
    }

    /**
     * Format the identifier of a MeshObject for a viewlet.
     *
     * @param saneRequest the incoming request
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObjectIdentifier(
            SaneRequest saneRequest,
            MeshObject         mesh,
            String             stringRepresentation,
            int                maxLength )
        throws
            StringifierException
    {
        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.MAX_LENGTH,               maxLength );
        pars.put( StringRepresentationParameters.COLLOQUIAL,               false );
        pars.put( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY, theDefaultMeshBase );
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, saneRequest.getContextPath() );

        String ret = mesh.getIdentifier().toStringRepresentation( rep, pars );
        return ret;
    }

    /**
     * Format the identifier of a MeshObject for a jsp tag.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObjectIdentifier(
            PageContext        pageContext,
            MeshObject         mesh,
            String             stringRepresentation,
            int                maxLength )
        throws
            StringifierException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );
        return formatMeshObjectIdentifier( saneRequest, mesh, stringRepresentation, maxLength );
    }

    /**
     * Format the start of the link to a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param addArguments additional arguments to the URL, if any
     * @param target the HTML target, if any
     * @param title the HTML title attribute, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObjectLinkStart(
            PageContext        pageContext,
            MeshObject         mesh,
            String             addArguments,
            String             target,
            String             title,
            String             stringRepresentation )
        throws
            StringifierException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

        addArguments = potentiallyAddAppContext( (HttpServletRequest) pageContext.getRequest(), addArguments );

        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.COLLOQUIAL,                    false );
        pars.put( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY,      theDefaultMeshBase );
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY,      saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY,      saneRequest.getContextPath() );
        pars.put( StringRepresentationParameters.LINK_TARGET_KEY,               target );
        pars.put( StringRepresentationParameters.LINK_TITLE_KEY,                title );
        pars.put( StringRepresentationParameters.HTML_URL_ADDITIONAL_ARGUMENTS, addArguments );

        String ret = mesh.toStringRepresentationLinkStart( rep, pars );
        return ret;
    }

    /**
     * Format the end of the link to a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObjectLinkEnd(
            PageContext        pageContext,
            MeshObject         mesh,
            String             stringRepresentation )
        throws
            StringifierException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.COLLOQUIAL,               false );
        pars.put( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY, theDefaultMeshBase );
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, saneRequest.getContextPath() );

        String ret = mesh.toStringRepresentationLinkEnd( rep, pars );
        return ret;
}

    /**
     * Recreate an MeshObjectIdentifier from a String.
     *
     * @param factory the MeshObjectIdentifierFactory to use
     * @param representation the StringRepresentation of the to-be-parsed String
     * @param s the String
     * @return the MeshObjectIdentifier
     * @throws ParseException thrown if a syntax error occurred
     */
    public MeshObjectIdentifier fromMeshObjectIdentifier(
            MeshObjectIdentifierFactory    factory,
            StringRepresentation           representation,
            StringRepresentationParameters pars,
            String                         s )
        throws
            ParseException
    {
        if( s == null ) {
            return null;
        }
        MeshObjectIdentifier ret = factory.fromStringRepresentation( representation, pars, s );

        return ret;
    }

    /**
     * Format the identifier of a MeshBase.
     *
     * @param pageContext the PageContext object for this page
     * @param base the MeshBase whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial should the value be emitted in colloquial form
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshBaseIdentifier(
            PageContext        pageContext,
            MeshBase           base,
            String             stringRepresentation,
            int                maxLength,
            boolean            colloquial )
        throws
            StringifierException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.MAX_LENGTH,               maxLength );
        pars.put( StringRepresentationParameters.COLLOQUIAL,               colloquial );
        pars.put( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY, theDefaultMeshBase );
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, saneRequest.getContextPath() );

        String ret = base.getIdentifier().toStringRepresentation( rep, pars );
        return ret;
    }

    /**
     * Format the start of a link to a MeshBase.
     *
     * @param pageContext the PageContext object for this page
     * @param base the MeshBase whose identifier is to be formatted
     * @param addArguments additional arguments to the URL, if any
     * @param target the HTML target, if any
     * @param title title of the HTML link, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshBaseLinkStart(
            PageContext        pageContext,
            MeshBase           base,
            String             addArguments,
            String             target,
            String             title,
            String             stringRepresentation )
        throws
            StringifierException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

        addArguments = potentiallyAddAppContext( (HttpServletRequest) pageContext.getRequest(), addArguments );

        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY,      theDefaultMeshBase );
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY,      saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY,      saneRequest.getContextPath() );
        pars.put( StringRepresentationParameters.LINK_TARGET_KEY,               target );
        pars.put( StringRepresentationParameters.LINK_TITLE_KEY,                title );
        pars.put( StringRepresentationParameters.HTML_URL_ADDITIONAL_ARGUMENTS, addArguments );

        String ret = base.toStringRepresentationLinkStart( rep, pars );
        return ret;
    }

    /**
     * Format the end of a link to a MeshBase.
     *
     * @param pageContext the PageContext object for this page
     * @param base the MeshBase whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshBaseLinkEnd(
            PageContext        pageContext,
            MeshBase           base,
            String             stringRepresentation )
        throws
            StringifierException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY, theDefaultMeshBase );
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, saneRequest.getContextPath() );

        String ret = base.toStringRepresentationLinkEnd( rep, pars );
        return ret;
    }

    /**
     * Determine whether the fully-qualified context path that cane be determined from a
     * PageContext identifies this MeshBase as the default MeshBase.
     *
     * @param base the MeshBase whose identifier is to be formatted
     * @return true if this MeshBase is the default MeshBase
     */
    public boolean isDefaultMeshBase(
            MeshBase base )
    {
        // FIXME this can be simpler right?
        String mbIdentifier = base.getIdentifier().toExternalForm();

        String   defaultMbIdentifier = theDefaultMeshBase.getIdentifier().toExternalForm();

        if( defaultMbIdentifier.equals( mbIdentifier )) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper method to potentially add the application context in link specifications.
     *
     * @param request the incoming request
     * @param addArguments the existing addArguments parameter
     * @return the new addArguments parameter
     */
    public static String potentiallyAddAppContext(
            HttpServletRequest request,
            String             addArguments )
    {
        SaneRequest sane       = SaneServletRequest.create( request );
        String      appContext = sane.getUrlArgument( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME );

        String ret = addArguments; // by default, do nothing
        if( appContext != null ) {
            // don't override existing parameter, only if not given
            boolean haveAlready = true;
            if( addArguments == null ) {
                haveAlready = false;
            } else {
                String pattern = TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME + "=";
                if( !addArguments.startsWith( pattern ) && addArguments.indexOf( "&" + pattern ) < 0 ) {
                    haveAlready = false;
                }
            }

            if( !haveAlready ) {
                StringBuilder toAdd = new StringBuilder();
                if( addArguments != null && addArguments.length() > 0 ) {
                    toAdd.append( addArguments );
                    toAdd.append( '&' );
                }
                toAdd.append( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME );
                toAdd.append( '=' );
                toAdd.append( HTTP.encodeToValidUrlArgument( appContext ));

                ret = toAdd.toString();
            }
        }
        return ret;
    }

    /**
     * The default MeshBase, if any.
     */
    protected MeshBase theDefaultMeshBase;
}
