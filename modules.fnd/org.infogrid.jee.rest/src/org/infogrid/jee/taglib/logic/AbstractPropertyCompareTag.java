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

package org.infogrid.jee.taglib.logic;

import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.model.primitives.PropertyValue;

import javax.servlet.jsp.JspException;

/**
 * <p>Abstract superclass for all tags performing a comparison of
 *    <code>PropertyValues</code>.</p>
 */
public abstract class AbstractPropertyCompareTag
    extends
        AbstractPropertyTestTag
{
    /**
     * Constructor.
     */
    protected AbstractPropertyCompareTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theValue             = null;
        theValueName         = null;
        theMeshObject2Name   = null;
        thePropertyType2Name = null;
        thePropertyType2     = null;
        
        super.initializeToDefaults();
    }

    /**
     * Obtain the value of the value property.
     *
     * @return the value of the value property
     * @see #setValue
     */
    public String getValue()
    {
        return theValue;
    }

    /**
     * Set the value of the value property.
     *
     * @param newValue the new value of the value property
     * @see #getValue
     */
    public void setValue(
            String newValue )
    {
        theValue = newValue;
    }

    /**
     * Obtain the value of the valueName property.
     *
     * @return the value of the valueName property
     * @see #setValueName
     */
    public String getValueName()
    {
        return theValueName;
    }

    /**
     * Set the value of the valueName property.
     *
     * @param newValue the new value of the valueName property
     * @see #getValueName
     */
    public void setValueName(
            String newValue )
    {
        theValueName = newValue;
    }

    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public final String getMeshObject2Name()
    {
        return theMeshObject2Name;
    }

    /**
     * Set value of the meshObjectName property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public final void setMeshObject2Name(
            String newValue )
    {
        theMeshObject2Name = newValue;
    }

    /**
     * Obtain value of the propertyTypeName property.
     *
     * @return value of the propertyTypeName property
     * @see #setPropertyTypeName
     */
    public final String getPropertyType2Name()
    {
        return thePropertyType2Name;
    }

    /**
     * Set value of the propertyTypeName property.
     *
     * @param newValue new value of the propertyTypeName property
     * @see #getPropertyTypeName
     */
    public final void setPropertyType2Name(
            String newValue )
    {
        thePropertyType2Name = newValue;
    }

    /**
     * Obtain value of the propertyType property.
     *
     * @return value of the propertyType property
     * @see #setPropertyType
     */
    public final String getPropertyType2()
    {
        return thePropertyType2;
    }

    /**
     * Set value of the propertyType property.
     *
     * @param newValue new value of the propertyType property
     * @see #getPropertyType
     */
    public final void setPropertyType2(
            String newValue )
    {
        thePropertyType2 = newValue;
    }

    /**
     * Determine the relative relationship between the comparison operators.
     * This returns values in analogy to the values returned by <code>String.compareTo</code>.
     *
     * @return -1, 0 or +1 depending on the result of the comparison;
     *         2 in case of non-comparable values.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int compare()
        throws
            JspException,
            IgnoreException
    {
        PropertyValue found = evaluate();

        if( theValueName != null && theValue != null ) {
            throw new JspException( "Must not specify both valueName and value" );
        }
        if( thePropertyType2Name != null && thePropertyType2 != null ) {
            throw new JspException( "Must not specify both propertyType2Name and propertyType2" );
        }
        if(    ( theValueName != null || theValue != null )
            && ( theMeshObject2Name != null || thePropertyType2Name != null || thePropertyType2 != null ))
        {
            throw new JspException( "Must not specify both a second property and a value" );
        }

        if( theValueName != null ) {
            PropertyValue comparison = (PropertyValue) lookupOrThrow( theValueName );
            
            return PropertyValue.compare( found, comparison );

        } else if( theMeshObject2Name != null || thePropertyType2Name != null || thePropertyType2 != null ) {

            // default to first set of attributes if not given here
            String meshObject2Name   = theMeshObject2Name != null ? theMeshObject2Name : theMeshObjectName;
            String propertyType2Name;
            String propertyType2;
            
            if( thePropertyType2Name != null || thePropertyType2 != null ) {
                propertyType2Name = thePropertyType2Name;
                propertyType2     = thePropertyType2;
            } else {
                propertyType2Name = thePropertyTypeName;
                propertyType2     = thePropertyType;
            }

            PropertyValue value2 = determinePropertyValue( meshObject2Name, propertyType2Name, propertyType2, "propertyType2Name", "propertyType2" );

            return PropertyValue.compare( found, value2 );

        } else {
            if( found == null ) {
                if( theValue == null || theValue.equals( "null" )) {
                    return 0;
                } else {
                    return +2; // non-comparable convention: +2
                }
            }

            String stringified = found.toString();
            return stringified.compareTo( theValue );
        }
    }

    /**
     * The value property.
     */
    protected String theValue;
    
    /**
     * The valueName property.
     */
    protected String theValueName;

    /**
     * String containing the name of the bean that is the MeshObject whose property is considered in the test.
     */
    protected String theMeshObject2Name;

    /**
     * String containing the name of the bean that is the PropertyType.
     */
    protected String thePropertyType2Name;

    /**
     * Identifier of the PropertyType. This is mutually exclusive with thePropertyTypeName.
     */
    protected String thePropertyType2;
}
