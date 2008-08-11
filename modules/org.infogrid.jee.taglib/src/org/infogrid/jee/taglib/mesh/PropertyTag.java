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

package org.infogrid.jee.taglib.mesh;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;

/**
 * Tag that renders a property of a <code>MeshObject</code>.
 */
public class PropertyTag
        extends
            AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public PropertyTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectName       = null;
        thePropertyTypeName     = null;
        thePropertyType         = null;
        theNullString           = "";
        theStringRepresentation = null;
        theMaxLength            = -1;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public final String getMeshObjectName()
    {
        return theMeshObjectName;
    }

    /**
     * Set value of the meshObjectName property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public final void setMeshObjectName(
            String newValue )
    {
        theMeshObjectName = newValue;
    }

    /**
     * Obtain value of the propertyTypeName property.
     *
     * @return value of the propertyTypeName property
     * @see #setPropertyTypeName
     */
    public final String getPropertyTypeName()
    {
        return thePropertyTypeName;
    }

    /**
     * Set value of the propertyTypeName property.
     *
     * @param newValue new value of the propertyTypeName property
     * @see #getPropertyTypeName
     */
    public final void setPropertyTypeName(
            String newValue )
    {
        thePropertyTypeName = newValue;
    }
    
    /**
     * Obtain value of the propertyType property.
     *
     * @return value of the propertyType property
     * @see #setPropertyType
     */
    public final String getPropertyType()
    {
        return thePropertyType;
    }

    /**
     * Set value of the propertyType property.
     *
     * @param newValue new value of the propertyType property
     * @see #getPropertyType
     */
    public final void setPropertyType(
            String newValue )
    {
        thePropertyType = newValue;
    }

    /**
     * Obtain value of the nullString property.
     *
     * @return value of the nullString property
     * @see #setNullString
     */
    public String getNullString()
    {
        return theNullString;
    }

    /**
     * Set value of the nullString property.
     *
     * @param newValue new value of the nullString property
     * @see #getNullString
     */
    public void setNullString(
            String newValue )
    {
        theNullString = newValue;
    }

    /**
     * Obtain value of the stringRepresentation property.
     *
     * @return value of the stringRepresentation property
     * @see #setStringRepresentation
     */
    public String getStringRepresentation()
    {
        return theStringRepresentation;
    }

    /**
     * Set value of the stringRepresentation property.
     *
     * @param newValue new value of the stringRepresentation property
     * @see #getStringRepresentation
     */
    public void setStringRepresentation(
            String newValue )
    {
        theStringRepresentation = newValue;
    }

    /**
     * Obtain value of the maxLength property.
     *
     * @return value of the maxLength property
     * @see #setMaxLength
     */
    public int getMaxLength()
    {
        return theMaxLength;
    }

    /**
     * Set value of the maxLength property.
     *
     * @param newValue new value of the maxLength property
     * @see #getNullString
     */
    public void setMaxLength(
            int newValue )
    {
        theMaxLength = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        MeshObject obj  = (MeshObject) lookupOrThrow( theMeshObjectName );

        if( obj == null ) {
            // ignore == true
            return SKIP_BODY;
        }
        PropertyType  type  = null;
        PropertyValue value = null;
        
        if( thePropertyType != null ) {
            if( thePropertyTypeName != null ) {
                throw new JspException( "Must not specify both propertyTypeName and propertyType" );
            }
            
            type = findPropertyTypeOrThrow( obj, thePropertyType );

        } else if( thePropertyTypeName == null ) {
            throw new JspException( "Must specify either propertyTypeName or propertyType" );
        }

        if( value == null ) {
            if( type == null ) {
                type = (PropertyType) lookupOrThrow( thePropertyTypeName );
            }

            try {
                value = obj.getPropertyValue( type );

            } catch( Exception ex ) {
                throw new JspException( ex );
            }        
        }
        String text = formatValue( pageContext, value, theNullString, theStringRepresentation );
               text = theFormatter.potentiallyShorten( text, theMaxLength );
        
        print( text );
        
        return SKIP_BODY;
    }

    /**
     * String containing the name of the bean that is the MeshObject whose property we render.
     */
    protected String theMeshObjectName;

    /**
     * String containing the name of the bean that is the PropertyType.
     */
    protected String thePropertyTypeName;

    /**
     * Identifier of the PropertyType. This is mutually exclusive with thePropertyTypeName.
     */
    protected String thePropertyType;
    
    /**
     * The String that is shown if a value is null.
     */
    protected String theNullString;
    
    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
    
    /**
     * The maximum length of an emitted String.
     */
    protected int theMaxLength;
}
