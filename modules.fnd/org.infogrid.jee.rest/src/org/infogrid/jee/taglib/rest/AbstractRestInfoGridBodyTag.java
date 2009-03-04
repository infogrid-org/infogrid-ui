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

package org.infogrid.jee.taglib.rest;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;

/**
 * Adds REST awareness to the AbstractInfoGridTag.
 */
public abstract class AbstractRestInfoGridBodyTag
        extends
            AbstractInfoGridBodyTag
{
    /**
     * Constructor for subclasses only.
     */
    protected AbstractRestInfoGridBodyTag()
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
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the String to display
     */
    protected final String formatValue(
            PageContext   pageContext,
            PropertyValue value,
            String        nullString,
            String        stringRepresentation,
            int           maxLength )
    {
        return ((RestfulJeeFormatter)theFormatter).formatPropertyValue(
                pageContext,
                value,
                nullString,
                stringRepresentation,
                maxLength );
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
     * Find a RoleType, or throw an Exception.
     *
     * @param name name of the RoleType
     * @return the found RoleType
     * @throws JspException thrown if the RoleType could not be found
     */
    protected RoleType findRoleTypeOrThrow(
            String name )
        throws
            JspException
    {
        return ((RestfulJeeFormatter)theFormatter).findRoleTypeOrThrow( name );
    }

    /**
     * Find a RoleType, or return null.
     *
     * @param name name of the RoleType
     * @return the found RoleType, or null
     */
    protected RoleType findRoleType(
            String name )
    {
        return ((RestfulJeeFormatter)theFormatter).findRoleType( name );
    }
}
