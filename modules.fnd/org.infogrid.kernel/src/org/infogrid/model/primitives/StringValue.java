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

package org.infogrid.model.primitives;

import org.infogrid.model.primitives.text.ModelPrimitivesStringRepresentationParameters;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationParameters;

/**
  * This is a string value for PropertyValues. StringValues are arbitary-length.
  */
public final class StringValue
        extends
            PropertyValue
{
    private final static long serialVersionUID = 1L; // helps with serialization

    /**
     * Factory method.
     *
     * @param value the value
     * @return the created StringValue
     * @throws IllegalArgumentException if null is given as argument
     */
    public static StringValue create(
            String value )
    {
        if( value == null ) {
            throw new IllegalArgumentException( "null value" );
        }
        return new StringValue( value );
    }

    /**
     * Factory method, or return null if the argument is null.
     *
     * @param value the value
     * @return the created StringValue, or null
     * @throws IllegalArgumentException if null is given as argument
     */
    public static StringValue createOrNull(
            String value )
    {
        if( value == null ) {
            return null;
        }
        return new StringValue( value );
    }

    /**
     * Factory method.
     *
     * @param value the value
     * @return the created StringValue
     * @throws IllegalArgumentException if null is given as argument
     */
    public static StringValue create(
            StringBuffer value )
    {
        if( value == null ) {
            throw new IllegalArgumentException( "null value" );
        }
        return new StringValue( value.toString() );
    }

    /**
     * Factory method, or return null if the argument is null.
     *
     * @param value the value
     * @return the created StringValue, or null
     * @throws IllegalArgumentException if null is given as argument
     */
    public static StringValue createOrNull(
            StringBuffer value )
    {
    	if( value == null ) {
            return null;
        }
    	return new StringValue( value.toString() );
    }

    /**
     * Helper method to create an array of StringValues from Strings.
     *
     * @param raw the array of Strings, or null
     * @return the corresponding array of StringValue, or null
     */
    public static StringValue [] createMultiple(
            String [] raw )
    {
        if( raw == null ) {
            return null;
        }
        StringValue [] ret = new StringValue[ raw.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = StringValue.create( raw[i] );
        }
        return ret;
    }

    /**
      * Private constructor, use factory methods
      *
      * @param value the value
      */
    private StringValue(
            String value )
    {
        this.theValue = value;
    }

    /**
      * Obtain the value.
      *
      * @return the value
      */
    public String value()
    {
        return theValue;
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
        if( otherValue instanceof StringValue ) {
            return theValue.equals( ((StringValue) otherValue).theValue );
        }
        if( otherValue instanceof String ) {
            return theValue.equals( (String) otherValue );
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
        StringBuffer buf = new StringBuffer( theValue.length() + 2 );
        buf.append( "\'" );
        buf.append( theValue );
        buf.append( "\'" );
        return buf.toString();
    }

    /**
     * Obtain as String, mostly for JavaBeans-aware software.
     *
     * @return as String
     */
    public String getAsString()
    {
        return value();
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
        StringBuffer buf = new StringBuffer( theValue.length() + 2 );
        buf.append( getClass().getName() );
        buf.append( DataType.CREATE_STRING );
        buf.append( encodeAsJavaString( theValue ));
        buf.append( " )" );
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
        StringValue realOther = (StringValue) o;

        return theValue.compareTo( realOther.theValue );
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationContext    context,
            StringRepresentationParameters pars )
    {
        Object editVariable;
        Object meshObject;
        Object propertyType;
        if( pars != null ) {
            editVariable = pars.get( StringRepresentationParameters.EDIT_VARIABLE );
            meshObject   = pars.get( ModelPrimitivesStringRepresentationParameters.MESH_OBJECT );
            propertyType = pars.get( ModelPrimitivesStringRepresentationParameters.PROPERTY_TYPE );
        } else {
            editVariable = null;
            meshObject   = null;
            propertyType = null;
        }

        return rep.formatEntry(
                getClass(),
                DEFAULT_ENTRY,
                pars,
        /* 0 */ editVariable,
        /* 1 */ meshObject,
        /* 2 */ propertyType,
        /* 3 */ this,
        /* 4 */ theValue );
    }

    /**
     * Helper method to encode a String as a Java string.
     * 
     * @param s the String
     * @return the encoded String
     */
    public static String encodeAsJavaString(
            String s )
    {
        StringBuilder ret = new StringBuilder( s.length() * 4 / 3 ); // fudge
        ret.append( '"' );
        
        for( int i=0 ; i<s.length() ; ++i ) {
            char c = s.charAt( i );
            
            switch( c ) {
                case '"':
                    ret.append( "\\\"" );
                    break;
                default:
                    ret.append( c );
                    break;
            }
        }
        ret.append( '"' );
        return ret.toString();
    }

    /**
      * The real value.
      */
    protected String theValue;
}
