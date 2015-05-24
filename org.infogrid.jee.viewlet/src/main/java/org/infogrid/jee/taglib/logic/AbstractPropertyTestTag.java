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

package org.infogrid.jee.taglib.logic;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;

/**
 * <p>Abstract superclass for all tags evaluating a <code>PropertyValue</code>.</p>
 */
public abstract class AbstractPropertyTestTag
    extends
        AbstractRestInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractPropertyTestTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObject       = null;
        theMeshObjectName   = null;
        thePropertyTypeName = null;
        thePropertyType     = null;
        
        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the meshObject property.
     *
     * @return value of the meshObject property
     * @see #setMeshObject
     */
    public final Object getMeshObject()
    {
        return theMeshObject;
    }

    /**
     * Set value of the meshObject property.
     *
     * @param newValue new value of the meshObject property
     * @see #getMeshObject
     */
    public final void setMeshObject(
            Object newValue )
    {
        theMeshObject = newValue;
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
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        if( evaluateTest() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Evaluate the condition. If it returns true, we include, in the output,
     * the content contained in this tag. This is abstract as concrete
     * subclasses of this class need to have the ability to determine what
     * their evaluation criteria are.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract boolean evaluateTest()
        throws
            JspException,
            IgnoreException;

    /**
     * Determine the PropertyValue to be used in the test.
     *
     * @return the PropertyValue
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected PropertyValue evaluate()
        throws
            JspException,
            IgnoreException
    {
        return determinePropertyValue( theMeshObject, theMeshObjectName, thePropertyTypeName, thePropertyType, "propertyTypeName", "propertyType" );
    }

    /**
     * Helper method to determine the value of a property.
     *
     * @param meshObject  the MeshObject
     * @param meshObjectName name of the bean holding the MeshObject
     * @param propertyTypeName name of the bean holding the PropertyType
     * @param propertyType name of the PropertyType
     * @param propertyTypeNameString name of the attribute corresponding to propertyTypeName
     * @param propertyTypeString name of the attribute corresponding to propertyType
     * @return the PropertyValue
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected PropertyValue determinePropertyValue(
            Object meshObject,
            String meshObjectName,
            String propertyTypeName,
            String propertyType,
            String propertyTypeNameString,
            String propertyTypeString )
        throws
            JspException,
            IgnoreException
    {
        MeshObject obj = lookupMeshObjectOrThrow( "meshObject", meshObject, "meshObjectName", meshObjectName );

        PropertyType  type  = null;
        PropertyValue value = null;

        if( propertyType != null ) {
            if( propertyTypeName != null ) {
                throw new JspException( "Must not specify both " + propertyTypeNameString + " and " + propertyTypeString );
            }

            type = findPropertyTypeOrThrow( obj, propertyType );

        } else if( propertyTypeName == null ) {
            throw new JspException( "Must specify either " + propertyTypeNameString + " or " + propertyTypeString );
        }

        if( value == null ) {
            if( type == null ) {
                type = (PropertyType) lookupOrThrow( propertyTypeName );
            }

            try {
                value = obj.getPropertyValue( type );

            } catch( Exception ex ) {
                throw new JspException( ex );
            }
        }
        return value;
    }

    /**
     * The MeshObject whose property is considered in the test.
     */
    protected Object theMeshObject;

    /**
     * String containing the name of the bean that is the MeshObject whose property is considered in the test.
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
}
