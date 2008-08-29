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

import org.infogrid.util.text.StringRepresentation;

import java.io.Serializable;

/**
  * This represents a data type for properties. This is an abstract class;
  * a more suitable subclass shall be instantiated.
  * A DataType can refine another DataType and usually does.
  *
  * Note: This is different from a value for properties:
  * Instances of (a subtype of) DataType represent the notion of a type,
  * such as "a positive integer", while instances of (a subtype of)
  * PropertyValue represent a value, such as "7".
  */
public abstract class DataType
        implements
            Serializable
{
    /**
      * Constructor for subclasses only.
      *
      * @param supertype the DataType that we refine, if any
      */
    protected DataType(
            DataType supertype )
    {
        theSupertype = supertype;
    }

    /**
      * Obtain the name of the DataType.
      *
      * @return the name of the DataType
      */
    public final String getName()
    {
        return this.getClass().getName();
    }

    /**
      * Test for equality.
      *
      * @param other object to test against
      * @return true if the two objects are equal
      */
    @Override
    public abstract boolean equals(
            Object other );

    /**
      * Test whether this DataType is a superset of or equals the argument
      * DataType. This is useful to test whether an assigment of
      * values is legal prior to attempting to do it. By default, this delegates
      * to the equals method.
      *
      * @param other the DataType we test against
      * @return if true, this DataType is a superset or equals the argument DataType
      */
    public boolean isSupersetOfOrEquals(
            DataType other )
    {
        return equals( other );
    }

    /**
     * Determine whether this PropertyValue conforms to this DataType.
     *
     * @param value the candidate PropertyValue
     * @return true if the candidate PropertyValue conforms to this type
     */
    public abstract boolean conforms(
            PropertyValue value );

    /**
      * Determine whether a domain check shall be performed on
      * assignments. Default to false.
      *
      * @return if true, a domain check shall be performed prior to performing assignments
      */
    public boolean getPerformDomainCheck()
    {
        return false;
    }

    /**
      * Return a boolean expression in the Java language that uses
      * varName as an argument and that evaluates whether the content
      * of variable varName is assignable to a value of this data type.
      * For example, on an integer data type with minimum and maximum
      * values, one would call it with argument "theValue", and it could
      * return: "( theValue > 2 && theValue < 7 )", where "2" and "7" are
      * the values of the min and max attributes of the integer data type.
      *
      * This is used primarily for code-generation purposes.
      *
      * @param varName the name of the variable containing the value
      * @return the Java language  expression
      */
    public String getJavaDomainCheckExpression(
            String varName )
    {
        return "true";
    }

    /**
      * Obtain the Java class that can hold values of this data type.
      *
      * @return the Java class that can hold values of this data type
      */
    public abstract Class getCorrespondingJavaClass();

    /**
     * Obtain a value expression in the Java language that invokes the constructor
     * of factory method of the underlying concrete class, thereby creating or
     * reusing an instance of the underlying concrete class that is identical
     * to the instance on which this method was invoked.
     *
     * This is used primarily for code-generation purposes.
     *
     * @param classLoaderVar name of a variable containing the class loader to be used to initialize this value
     * @return the Java language expression
     */
    public abstract String getJavaConstructorString(
            String classLoaderVar );

    /**
      * Instantiate this data type into a PropertyValue with a
      * reasonable default value.
      *
      * @return a PropertyValue with a reasonable default value that is an instance of this DataType
      */
    public abstract PropertyValue instantiate();

    /**
     * If this DataType is a refinement of another, find which DataType it refines.
     *
     * @return the refined DataType, if any
     */
    public final DataType getSupertype()
    {
        return theSupertype;
    }

    /**
     * Convert this PropertyValue to its String representation, using the representation scheme.
     *
     * @param representation the representation scheme
     * @return the String representation
     */
    public abstract String toStringRepresentation(
            StringRepresentation representation );

//
//    /**
//     * Consistent way of finding numbers in Strings for the purpose of parsing them.
//     *
//     * @param s the String
//     * @return array of float, representing the distinct numbers in the String, treating all others as white space, or null
//     */
//    protected float [] findFloatsIn(
//            String s )
//    {
//        String [] numberStrings = s.split( "[^0-9.-+]+" ); // FIXME: no exponential representation supported
//        
//        if( numberStrings.length == 1 && numberStrings[0].length() == 0 ) {
//            return null;
//        }
//        
//        float [] ret = new float[ numberStrings.length ];
//        
//        for( int i=0 ; i<numberStrings.length ; ++i ) {
//            ret[i] = Float.parseFloat( numberStrings[i] );
//        }
//        return ret;
//    }
//
//    /**
//     * Consistent way of finding numbers in Strings for the purpose of parsing them.
//     *
//     * @param s the String
//     * @return array of double, representing the distinct numbers in the String, treating all others as white space, or null
//     */
//    protected double [] findDoublesIn(
//            String s )
//    {
//        String [] numberStrings = s.split( "[^0-9.-+]+" ); // FIXME: no exponential representation supported
//
//        if( numberStrings.length == 1 && numberStrings[0].length() == 0 ) {
//            return null;
//        }
//        
//        double [] ret = new double[ numberStrings.length ];
//        
//        for( int i=0 ; i<numberStrings.length ; ++i ) {
//            ret[i] = Double.parseDouble( numberStrings[i] );
//        }
//        return ret;
//    }
//
//    /**
//     * Consistent way of finding numbers in Strings for the purpose of parsing them.
//     *
//     * @param s the String
//     * @return array of int, representing the distinct numbers in the String, treating all others as white space, or null
//     */
//    protected int [] findIntegersIn(
//            String s )
//    {
//        String [] numberStrings = s.split( "[^0-9-+]+" );
//
//        if( numberStrings.length == 1 && numberStrings[0].length() == 0 ) {
//            return null;
//        }
//        
//        int [] ret = new int[ numberStrings.length ];
//        
//        for( int i=0 ; i<numberStrings.length ; ++i ) {
//            ret[i] = Integer.parseInt( numberStrings[i] );
//        }
//        return ret;
//    }

    /**
     * Obtain a PropertyValue that corresponds to this PropertyType, based on the String representation
     * of the PropertyValue.
     * 
     * @param representation the StringRepresentation in which the String s is given
     * @param s the String
     * @return the PropertyValue
     * @throws PropertyValueParsingException thrown if the String representation could not be parsed successfully
     */
    public abstract PropertyValue fromStringRepresentation(
            StringRepresentation representation,
            String                      s )
        throws
            PropertyValueParsingException;

    /**
     * The supertype of this DataType (if any).
     */
    protected DataType theSupertype;

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String CREATE_STRING = ".create( ";

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String COMMA_STRING = ", ";

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String NULL_STRING = "null";

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String QUOTE_STRING = "\"";

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String DEFAULT_STRING = ".theDefault";

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String CLOSE_PAREN_STRING = " )";

    /**
     * The default entry in the resouce files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_ENTRY = "Type";
}
