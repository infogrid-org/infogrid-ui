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

package org.infogrid.util;

/**
 * Wraps a Java array, so we can use it as a proper class, such as a parameter
 * for a generic class.
 * @param <T> the array's component type
 */
public class ArrayFacade<T>
{
    /**
     * Factory method.
     *
     * @param array the array
     * @return the created ArrayFacade
     * @param <T> the array's component type
     */
    public static <T> ArrayFacade<T> create(
            T [] array )
    {
        return new ArrayFacade<T>( array );
    }

    /**
     * Factory method.
     *
     * @param n the desired length of the array
     * @return the created ArrayFacade
     * @param <T> the array's component type
     */
    @SuppressWarnings(value={"unchecked"})
    public static <T> ArrayFacade<T> create(
            int n )
    {
        T [] array = (T []) new Object[ n ];
        return new ArrayFacade<T>( array );
    }

    /**
     * Constructor.
     *
     * @param array the array
     */
    public ArrayFacade(
            T [] array )
    {
        theArray = array;
    }
    
    /**
     * Obtain the array.
     *
     * @return the array
     */
    public T [] getArray()
    {
        return theArray;
    }
    
    /**
     * Put something into the array.
     *
     * @param index the location in the array
     * @param value the value to put
     * @return the old value previously held there
     */
    public T put(
            int index,
            T   value )
    {
        T ret = theArray[index];
        theArray[index] = value;
        return ret;
    }
    /**
     * The array.
     */
    protected T [] theArray;
}
