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

/**
 * This Iterator iterates over a subset of the Objects returned by a delegate Iterator.
 * The subset is determined by a {@link Filter}, which may or may not select an
 * Object to be returned This iterator always reads one object ahead to know whether
 * we have more elements or not.
 * 
 * @param T the type of element to iterate over
 */
public class FilteringIterator<T>
        implements
            Iterator<T>
{
    /**
     * Construct one.
     *
     * @param delegate the Iterator from whom we are taking the candidate objects
     * @param filt the filter algorithm
     */
    public FilteringIterator(
            Iterator<T> delegate,
            Filter<T>   filt )
    {
        theDelegate = delegate;
        theFilter   = filt;

        goNext();
    }

    /**
     * Do we have a next element?
     *
     * @return true if we have a next element
     */
    public boolean hasNext()
    {
        return theNextObject != null;
    }

    /**
     * Obtain the next Object in this iteration.
     *
     * @return the new Object in this iteration
     */
    public synchronized T next()
    {
        T ret = theNextObject;
        if( ret == null ) {
            return null;
        }
        goNext();

        return ret;
    }

    /**
     * Remove current Object. We don't do that.
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Helper method to go to the next Object we want to return.
     */
    protected void goNext()
    {
        while( theDelegate.hasNext() ) {
            T candidate = theDelegate.next();
            if( theFilter.accept( candidate )) {
                theNextObject = candidate;
                return;
            }
        }
        theNextObject = null;
    }

    /**
     * The delegate Iterator from which we take the candidate Objects that we filter.
     */
    protected Iterator<T> theDelegate;

    /**
     * The specific filter for this instance of FilteringIterator.
     */
    protected Filter<T> theFilter;

    /**
     * The next Object to return, if any. This is the read-ahead buffer.
     */
    protected T theNextObject;

    /**
     * This inner interface is implemented by users of FilteringIterator to determine whether
     * or not we should accept a certain Object.
     * 
     * @param T the type of element to iterate over
     */
    public static interface Filter<T>
    {
        /**
          * Determine whether or not to accept a candidate Object.
          *
          * @param candidate the candidate Object
          * @return true if this Object shall be accepted according to this Filter
          */
        public boolean accept(
                T candidate );
    }
}
