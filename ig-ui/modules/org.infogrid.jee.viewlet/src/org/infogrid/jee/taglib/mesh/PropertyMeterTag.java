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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.mesh;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.jee.taglib.util.InfoGridIterationTag;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;

/**
 * Iterates from a min value to a max value in a number of steps, while selecting
 * one of two possible contained tags depending on whether or not the current value
 * of the iteration is below or above a value given by a Property.
 *
 * In other words, makes it easy to implement a thermometer based on the value of
 * a Property or similar displays.
 */
public class PropertyMeterTag
    extends
        AbstractRestInfoGridBodyTag
    implements
        InfoGridIterationTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public PropertyMeterTag()
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
        theMin              = null;
        theMax              = null;
        theN                = null;

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
     * Obtain value of the min property.
     *
     * @return value of the min property
     * @see #setMin
     */
    public final String getMin()
    {
        return theMin;
    }

    /**
     * Set value of the min property.
     *
     * @param newValue new value of the min property
     * @see #getMin
     */
    public final void setMin(
            String newValue )
    {
        theMin = newValue;
    }

    /**
     * Obtain value of the max property.
     *
     * @return value of the max property
     * @see #setMax
     */
    public final String getMax()
    {
        return theMax;
    }

    /**
     * Set value of the max property.
     *
     * @param newValue new value of the max property
     * @see #getMax
     */
    public final void setMax(
            String newValue )
    {
        theMax = newValue;
    }

    /**
     * Obtain value of the n property.
     *
     * @return value of the n property
     * @see #setN
     */
    public final String getN()
    {
        return theN;
    }

    /**
     * Set value of the n property.
     *
     * @param newValue new value of the n property
     * @see #getN
     */
    public final void setN(
            String newValue )
    {
        theN = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        MeshObject   obj  = lookupMeshObjectOrThrow( "meshObject", theMeshObject, "meshObjectName", theMeshObjectName );
        PropertyType type = null;

        if( obj == null ) {
            // if we get here, ignore is necessarily true
            return SKIP_BODY;
        }

        if( thePropertyType != null ) {
            if( thePropertyTypeName != null ) {
                throw new JspException( "Must not specify both propertyTypeName and propertyType" );
            }

            type = findPropertyTypeOrThrow( obj, thePropertyType );

        } else if( thePropertyTypeName != null ) {
            type = (PropertyType) lookupOrThrow( thePropertyTypeName );

        } else {
            throw new JspException( "Must specify either propertyTypeName or propertyType" );
        }

        PropertyValue value;
        try {
            value = obj.getPropertyValue( type );

        } catch( IllegalPropertyTypeException ex ) {
            throw new JspException( ex );
        } catch( NotPermittedException ex ) {
            throw new JspException( ex );
        }

        if( value == null ) {
            if( theFormatter.isTrue( getIgnore() )) {
                return SKIP_BODY;
            } else {
                throw new JspException( "PropertyValue is null, cannot show" );
            }
        }
        double realValue;
        if( value instanceof IntegerValue ) {
            realValue = ((IntegerValue)value).doubleValue();
        } else if( value instanceof FloatValue ) {
            realValue = ((FloatValue)value).doubleValue();
        } else {
            throw new JspException( "Cannot show non-numeric PropertyValue in PropertyMeterTag: " + value );
        }

        theCurrent          = 0;
        theNumberIterations = Integer.parseInt( theN );

        if( theNumberIterations <= 0 ) {
            throw new JspException( "Number of iterations must be a positive number" );
        }
        double min = Double.parseDouble( theMin );
        double max = Double.parseDouble( theMax );
        
        theNumberIterationsBelow = (int) (( realValue - min ) / ( max - min ) * theNumberIterations + .5 );

        return EVAL_BODY_AGAIN;
    }

    /**
     * Invoked after the Body tag has been invoked.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an error occurred
     */
    @Override
    protected int realDoAfterBody()
        throws
            JspException
    {
        if( bodyContent != null ) {
            theFormatter.printPrevious( pageContext, theFormatter.isTrue( getFilter()), bodyContent.getString() );
            bodyContent.clearBody();
        }

        ++theCurrent;

        if( theCurrent < theNumberIterations ) {
            return EVAL_BODY_AGAIN;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Determine whether this iteration tag has a next element to be returned
     * in the iteration.
     *
     * @return true if there is a next element
     */
    public boolean hasNext()
    {
        return theCurrent < theNumberIterations;
    }

    /**
     * Allow enclosed tags to determine whether, during this iteration, the
     * PropertyMeterBelowTag should be displayed.
     *
     * @return true if the PropertyMeterBelowTag should be displayed
     */
    public boolean displayBelowTag()
    {
        return theCurrent < theNumberIterationsBelow;
    }

    /**
     * Allow enclosed tags to determine whether, during this iteration, the
     * PropertyMeterAboveTag should be displayed.
     *
     * @return true if the PropertyMeterAboveTag should be displayed
     */
    public boolean displayAboveTag()
    {
        return !displayBelowTag();
    }

    /**
     * The MeshObject.
     */
    protected Object theMeshObject;

    /**
     * String containing the name of the bean that is the MeshObject.
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
     * The minimum value.
     */
    protected String theMin;

    /**
     * The maximum value.
     */
    protected String theMax;

    /**
     * The number of steps.
     */
    protected String theN;

    /**
     * The number of iterations.
     */
    protected int theNumberIterations;

    /**
     * The numver of iterations we are "below".
     */
    protected int theNumberIterationsBelow;

    /**
     * The current value in the iteration.
     */
    protected int theCurrent;
}
