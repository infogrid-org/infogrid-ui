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

package org.infogrid.model.primitives;

import org.infogrid.util.DoubleDimension;
import org.infogrid.util.ResourceHelper;

import org.infogrid.util.text.StringRepresentation;

import java.awt.geom.Dimension2D;

/**
  * This is a graphical extent value for PropertyValues.
  */
public final class ExtentValue
        extends
            PropertyValue
{
    private final static long serialVersionUID = 1L; // helps with serialization

    /**
     * Factory method.
     *
     * @param value the Dimension2D version of this value
     * @return the created ExtentValue
     */
    public static ExtentValue create(
            Dimension2D value )
    {
        if( value == null ) {
            throw new IllegalArgumentException( "null value" );
        }
        return new ExtentValue( value.getWidth(), value.getHeight() );
    }

    /**
     * Factory method, or return null if the argument is null.
     *
     * @param value the Dimension2D version of this value
     * @return the created ExtentValue, or null
     */
    public static ExtentValue createOrNull(
            Dimension2D value )
    {
        if( value == null ) {
            return null;
        }
        return new ExtentValue( value.getWidth(), value.getHeight() );
    }

    /**
     * Factory method.
     *
     * @param width the width of the extent
     * @param height the height of the extent
     * @return the created ExtentValue
     */
    public static ExtentValue create(
            double width,
            double height )
    {
        return new ExtentValue( width, height );
    }

    /**
      * Private constructor, use factory method.
      *
      * @param width the width of the extent
      * @param height the height of the extent
      */
    private ExtentValue(
            double width,
            double height )
    {
        this.width  = width;
        this.height = height;
    }

    /**
      * Obtain value.
      *
      * @return the value as Dimension2D
      */
    public Dimension2D value()
    {
        return new DoubleDimension( width, height ); // FIXME as soon as Sun provides this
    }

    /**
      * Determine width of the extent.
      *
      * @return the width of the extent
      */
    public double getWidth()
    {
        return width;
    }

    /**
      * Determine height of the extent.
      *
      * @return the height of the extent
      */
    public double getHeight()
    {
        return height;
    }

    /**
      * Determine equality of two objects.
      *
      * @param otherValue the object to test against
      * @return true if the objects are equal
      */
    @Override
    public boolean equals(
            Object otherValue )
    {
        if( otherValue instanceof ExtentValue ) {
            return (height == ((ExtentValue)otherValue).height)
                && (width  == ((ExtentValue)otherValue).width);
        }
        return false;
    }

    /**
      * Obtain as string representation, for debugging.
      *
      * @return string representation of this object
      */
    @Override
    public String toString()
    {
        return "[" + width + ";" + height + "]";
    }

    /**
     * Obtain a string which is the Java-language constructor expression reflecting this value.
     *
     * @param classLoaderVar name of a variable containing the class loader to be used to initialize this value
     * @param typeVar  name of the variable containing the DatatType that goes with the to-be-created instance.
     * @return the Java-language constructor expression
     */
    public String getJavaConstructorString(
            String classLoaderVar,
            String typeVar )
    {
        StringBuffer buf = new StringBuffer( 64 );
        buf.append( getClass().getName());
        buf.append( DataType.CREATE_STRING );
        buf.append( width );
        buf.append( ", " );
        buf.append( height );
        buf.append( DataType.CLOSE_PAREN_STRING );
        return buf.toString();
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param   o the PropertyValue to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this Object.
     */
    public int compareTo(
            PropertyValue o )
    {
        ExtentValue realOther = (ExtentValue) o;

        if( width < realOther.width ) {
            if( height < realOther.height ) {
                return -1;
            } else {
                return +2; // not comparable convention: +2
            }
        } else if( width == realOther.width ) {
            if( height == realOther.height ) {
                return 0;
            } else {
               return +2; // not comparable convention: +2
            }
        } else {
            if( height > realOther.height ) {
                return +1;
            } else {
                return +2; // not comparable convention: +2
            }
        }
    }

    /**
     * Convert this PropertyValue to its String representation, using the representation scheme.
     *
     * @param representation the representation scheme
     * @return the String representation
     */
    public String toStringRepresentation(
            StringRepresentation representation )
    {
        return representation.formatEntry( RESOURCEHELPER, DEFAULT_ENTRY, width, height );
    }

    /**
      * The actual value for the width.
      */
    protected double width;

    /**
      * The actual value for the height.
      */
    protected double height;
    
    /**
     * Our ResourceHelper.
     */
    static final ResourceHelper RESOURCEHELPER = ResourceHelper.getInstance( ExtentValue.class );    
}
