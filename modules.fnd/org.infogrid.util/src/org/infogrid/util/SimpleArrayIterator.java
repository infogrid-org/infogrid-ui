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
  * of an array. It supports both the Enumeration and the Iterator interfaces.
 * 
 * @param T the type of element to iterate over
  */
public class SimpleArrayIterator<T>
        implements
            Enumeration<T>,
            Iterator<T>
{
    /**
      * Constructor.
      *
      * @param array the array to iterate over
      */
    public SimpleArrayIterator(
            T [] array )
    {
        this( array, 0, array.length );
    }

    /**
      * Constructor.
      *
      * @param array the array to iterate over
      * @param start the start index
      */
    public SimpleArrayIterator(
            T [] array,
            int  start )
    {
        this( array, start, array.length );
    }

    /**
      * Constructor.
      *
      * @param array the array to iterate over
      * @param start the start index
      * @param end the end index (exclusive)
      */
    public SimpleArrayIterator(
            T [] array,
            int  start,
            int  end )
    {
        this.theArray = array;
        index  = start;
        theEnd = end;

        if( theEnd > array.length ) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
      * Do we have more elements?
      *
      * @return true if we have more elements
      */
    public boolean hasNext()
    {
        return index < theEnd;
    }

    /**
      * Do we have more elements?
      *
      * @return true if we have more elements
      */
    public final boolean hasMoreElements()
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
        return theArray[ index++ ];
    }

    /**
      * Return next element and iterate.
      *
      * @return the next element
      */
    public final T nextElement()
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
        index = 0;
    }

    /**
      * Where are we in the array.
      */
    protected int index;

    /**
     * The maximum index to return.
     */
    protected int theEnd;

    /**
      * The array that we are iterating over.
      */
    protected T [] theArray;
}
