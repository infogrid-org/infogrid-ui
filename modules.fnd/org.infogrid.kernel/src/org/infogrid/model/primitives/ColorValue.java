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

import org.infogrid.util.ResourceHelper;
import org.infogrid.util.text.StringRepresentation;

import java.awt.Color;

/**
  * This is a color value for PropertyValue.
  */
public final class ColorValue
        extends
            PropertyValue
{
    private final static long serialVersionUID = 1L; // helps with serialization

    /**
     * Factory method.
     *
     * @param value the JDK's definition of Color
     * @return the created ColorValue
     * @throws IllegalArgumentException if null was provided
     */
    public static ColorValue create(
            Color value )
    {
        if( value == null ) {
            throw new IllegalArgumentException( "null value" );
        }
        return new ColorValue( value );
    }

    /**
     * Factory method, or return null if the argument is null.
     *
     * @param value the JDK's definition of Color
     * @return the created ColorValue, or null
     * @throws IllegalArgumentException if null was provided
     */
    public static ColorValue createOrNull(
    		Color value )
    {
        if( value == null ) {
            return null;
        }
        return new ColorValue( value );
    }

    /**
     * Factory method.
     *
     * @param value the value as RGB value
     * @return the created ColorValue
     */
    public static ColorValue create(
            int value )
    {
        // We go through Java's Color because they are doing messy stuff there
        // with alpha channel defaults, and we don't want to hard code any dependency
        // on this into our code ...
        return new ColorValue( new Color( value ));
    }

    /**
      * Initialize to specified value.
      *
      * @param value the JDK's definition of Color
      */
    private ColorValue(
            Color value )
    {
        this.theValue = value.getRGB();
    }

    /**
      * Convert back to the JDK's definition of this Color.
      *
      * @return the JDK's definition of this Color.
      */
    public Color value()
    {
        return new Color( theValue );
    }

    /**
     * Obtain the value of the alpha component.
     *
     * @return value of the alpha component
     */
    public int getAlpha()
    {
        return ( theValue >> 24 ) & 0xFF;
    }

    /**
      * Obtain value of the red component.
      *
      * @return value of the red component
      */
    public int getRed()
    {
        return ( theValue >> 16 ) & 0xFF;
    }

    /**
      * Obtain value of the blue component.
      *
      * @return value of the blue component
      */
    public int getBlue()
    {
        return ( theValue >> 0 ) & 0xFF;
    }

    /**
      * Obtain value of the green component.
      *
      * @return value of the green component
      */
    public int getGreen()
    {
        return ( theValue >> 8 ) & 0xFF;
    }

    /**
      * Obtain as RGB value.
      *
      * @return the RGB value
      */
    public int getRGB()
    {
        return theValue;
    }

    /**
      * Determine equality of two objects.
      *
      * @param otherValue the object to test against
      * @return if true, the objects are equal
      */
    @Override
    public boolean equals(
            Object otherValue )
    {
        if( otherValue instanceof ColorValue ) {
            return theValue == ((ColorValue)otherValue).theValue;
        }
        return false;
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
        StringBuffer buf = new StringBuffer( 128 );
        buf.append( getClass().getName());
        buf.append( DataType.CREATE_STRING );
        buf.append( theValue );
        buf.append( DataType.CLOSE_PAREN_STRING );
        return buf.toString();
    }

    /**
      * Obtain as string representation, for debugging.
      *
      * @return string representation of this object
      */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "(" );
        buf.append( getRed() );
        buf.append( ";" );
        buf.append( getGreen() );
        buf.append( ";" );
        buf.append( getBlue() );
        buf.append( ";" );
        buf.append( getAlpha() );
        buf.append( ")" );
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
        ColorValue realValue = (ColorValue) o;

        if( theValue == realValue.theValue ) {
            return 0;
        } else {
            return +2; // not comparable convention: +2
        }
    }

    /**
      * This attempts to parse a string and turn it into a integer value similarly
      * to Integer.parseInt().
      *
      * @param theString the string that shall be parsed
      * @return the created IntegerValue
      * @throws NumberFormatException thrown if theString does not follow the correct syntax
      */
    public static ColorValue parseColorValue(
            String theString )
        throws
            NumberFormatException
    {
        return ColorValue.create( Integer.parseInt( theString ));
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
        return representation.formatEntry( RESOURCEHELPER, DEFAULT_ENTRY, getRed(), getGreen(), getBlue(), getAlpha(), getRGB() );
    }

    /**
      * The value encoded the same way as java.awt.Color.
      */
    protected int theValue;
    
    /**
     * Our ResourceHelper.
     */
    static final ResourceHelper RESOURCEHELPER = ResourceHelper.getInstance( ColorValue.class );
}
