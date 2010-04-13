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

package org.infogrid.jee.taglib.rest;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.text.StringifierException;

/**
 * Adds REST awareness to the AbstractInfoGridTag.
 */
public abstract class AbstractRestInfoGridTag
        extends
            AbstractInfoGridTag
{
    /**
     * Constructor for subclasses only.
     */
    protected AbstractRestInfoGridTag()
    {
        // nothing
    }

    /**
     * Format a PropertyValue.
     *
     * @param pageContext the PageContext in which to format the PropertyValue
     * @param value the PropertyValue
     * @param nullString the String to display of the value is null
     * @param stringRepresentation the StringRepresentation for PropertyValues
     * @param theMaxLength the maximum length of an emitted String
     * @param colloquial if applicable, output in colloquial form
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    protected final String formatPropertyValue(
            PageContext   pageContext,
            PropertyValue value,
            String        nullString,
            String        stringRepresentation,
            int           theMaxLength,
            boolean       colloquial )
        throws
            StringifierException
    {
        String ret = ((RestfulJeeFormatter)theFormatter).formatPropertyValue(
                pageContext,
                value,
                nullString,
                stringRepresentation,
                theMaxLength,
                colloquial );
        return ret;
    }

    /**
     * Format a Property.
     *
     * @param pageContext the PageContext in which to format the Property
     * @param owningMeshObject the MeshObject that owns this Property
     * @param propertyType the PropertyType of the Property
     * @param editVar name of the HTML form elements to use
     * @param nullString the String to display of the value is null
     * @param stringRepresentation the StringRepresentation for PropertyValues
     * @param theMaxLength the maximum length of an emitted String
     * @param colloquial if applicable, output in colloquial form
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     * @throws IllegalPropertyTypeException thrown if the PropertyType does not exist on this MeshObject
     *         because the MeshObject has not been blessed with a MeshType that provides this PropertyType
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    protected final String formatProperty(
            PageContext   pageContext,
            MeshObject    owningMeshObject,
            PropertyType  propertyType,
            String        editVar,
            String        nullString,
            String        stringRepresentation,
            int           theMaxLength,
            boolean       colloquial )
        throws
            StringifierException,
            IllegalPropertyTypeException,
            NotPermittedException
    {
        String ret = ((RestfulJeeFormatter)theFormatter).formatProperty(
                pageContext,
                owningMeshObject,
                propertyType,
                editVar,
                nullString,
                stringRepresentation,
                theMaxLength,
                colloquial );
        return ret;
    }

    /**
     * Find a MeshObject with the given identifier, or return null.

     * @param identifier the MeshObjectIdentifier in String form
     * @return the found MeshObject, or null
     * @throws JspException thrown if the identifier could not be parsed
     */
    protected MeshObject findMeshObject(
            String identifier )
        throws
            JspException
    {
        return ((RestfulJeeFormatter)theFormatter).findMeshObject( identifier );
    }

    /**
     * Find a MeshObject with the given identifier, or throw an Exception.

     * @param identifier the MeshObjectIdentifier in String form
     * @return the found MeshObject
     * @throws JspException thrown if the identifier could not be parsed or the MeshObject could not be found
     */
    protected MeshObject findMeshObjectOrThrow(
            String identifier )
        throws
            JspException
    {
        return ((RestfulJeeFormatter)theFormatter).findMeshObjectOrThrow( identifier );
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
        return ((RestfulJeeFormatter)theFormatter).findMeshTypeByIdentifier( identifier );
    }

    /**
     * Find a PropertyType, or return null. This will consider the
     * EntityTypes that the MeshObject is currently blessed with, and look for
     * a PropertyType with the given name.
     *
     * @param obj the MeshObject
     * @param name name of the PropertyType
     * @return the found PropertyType, or null
     */
    protected PropertyType findPropertyType(
            MeshObject obj,
            String     name )
    {
        return ((RestfulJeeFormatter)theFormatter).findPropertyType( obj, name );
    }

    /**
     * Find a PropertyType, or throw an Exception. This will consider the
     * EntityTypes that the MeshObject is currently blessed with, and look for
     * a PropertyType with the given name.
     *
     * @param obj the MeshObject
     * @param name name of the PropertyType
     * @return the found PropertyType
     * @throws JspException thrown if the PropertyType could not be found
     */
    protected PropertyType findPropertyTypeOrThrow(
            MeshObject obj,
            String     name )
        throws
            JspException
    {
        return ((RestfulJeeFormatter)theFormatter).findPropertyTypeOrThrow( obj, name );
    }

    /**
     * Find a TraversalSpecification, or return null.
     *
     * @param startObject the start MeshObject
     * @param traversalTerm the serialized TraversalSpecification
     * @return the found TraversalSpecification, or null
     */
    protected TraversalSpecification findTraversalSpecification(
            MeshObject startObject,
            String     traversalTerm )
    {
        return ((RestfulJeeFormatter)theFormatter).findTraversalSpecification( startObject, traversalTerm );
    }

    /**
     * Find a TraversalSpecification, or throw an Exception.
     *
     * @param startObject the start MeshObject
     * @param traversalTerm the serialized TraversalSpecification
     * @return the found TraversalSpecification
     * @throws JspException thrown if the TraversalSpecification could not be found
     */
    protected TraversalSpecification findTraversalSpecificationOrThrow(
            MeshObject startObject,
            String     traversalTerm )
        throws
            JspException
    {
        return ((RestfulJeeFormatter)theFormatter).findTraversalSpecificationOrThrow( startObject, traversalTerm );
    }

    /**
     * Find a sequence of TraversalSpecifications, or return null.
     *
     * @param startObject the start MeshObject
     * @param traversalTerm the serialized TraversalSpecification
     * @return the found sequence of TraversalSpecifications, or null
     */
    protected TraversalSpecification [] findTraversalSpecificationSequence(
            MeshObject startObject,
            String     traversalTerm )
    {
        return ((RestfulJeeFormatter)theFormatter).findTraversalSpecificationSequence( startObject, traversalTerm );
    }

    /**
     * Find a sequence of TraversalSpecifications, or throw an Exception.
     *
     * @param startObject the start MeshObject
     * @param traversalTerm the serialized TraversalSpecification
     * @return the found sequence of TraversalSpecifications
     * @throws JspException thrown if the TraversalSpecification could not be found
     */
    protected TraversalSpecification [] findTraversalSpecificationSequenceOrThrow(
            MeshObject startObject,
            String     traversalTerm )
        throws
            JspException
    {
        return ((RestfulJeeFormatter)theFormatter).findTraversalSpecificationSequenceOrThrow( startObject, traversalTerm );
    }
}
