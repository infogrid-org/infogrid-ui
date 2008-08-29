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

import java.util.Iterator;
import java.util.Enumeration;

/**
 * A simple Iterator implementation which returns elements
 * of a two-dimensional array. It supports both the Enumeration
 * and the Iterator interfaces.
 * 
 * @param T the type of element to iterate over
 */
public class DoubleArrayEnumeration<T>
        implements
            Enumeration<T>,
            Iterator<T>
{
    /**
     * Construct one with an array of arrays to traverse. We will first
     * traverse array[0], then array[1] etc.
     *
     * @param array the array to iterate over
     */
    public DoubleArrayEnumeration(
            T [][] array )
    {
        this.theArray = array;
        outerIndex = 0;
        innerIndex = 0;
    }

    /**
      * Do we have more elements?
      *
      * @return true if we have more elements
      */
    public boolean hasNext()
    {
        if( outerIndex >= theArray.length ) {
            return false;
        }
        return innerIndex < theArray[outerIndex].length;
    }

    /**
      * Do we have more elements?
      *
      * @return true if we have more elements
      */
    public boolean hasMoreElements()
    {
        return hasNext();
    }

    /**
      * Return next element and iterate.
      *
      * @return the next element
      */
    public T next()
    {
        if( hasNext() ) {
            if( innerIndex >= theArray[outerIndex ].length ) {
                innerIndex = 0;
                ++outerIndex;
            }
            return theArray[ outerIndex ][ innerIndex++ ];
        }
        return null;
    }

    /**
      * Return next element and iterate.
      *
      * @return the next element
      */
    public T nextElement()
    {
        return next();
    }

    /**
     * We don't know how to remove.
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
      * Reset the Iterator.
      */
    public void reset()
    {
        innerIndex = 0;
        outerIndex = 0;
    }

    /**
      * Where are we in the outer array.
      */
    protected int outerIndex;

    /**
      * Where are we in the inner array.
      */
    protected int innerIndex;

    /**
      * The arrays that we are iterating over.
      */
    protected T [][] theArray;
}
