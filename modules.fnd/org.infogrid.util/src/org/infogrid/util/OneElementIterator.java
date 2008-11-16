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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * A very simple Iterator that returns only one element and then stops.
 * It is surprising how often this is useful.
 * 
 * @param <T> the type of element to iterate over
 */
public class OneElementIterator<T>
    implements
        Iterator<T>,
        Enumeration<T>
{
    /**
     * Factory method.
     *
     * @param onlyObject the one Object that we iterate over.
     * @return the created OneElementIterator
     * @param <T> the type of element to iterate over
     */
    public static <T> OneElementIterator<T> create(
            T onlyObject )
    {
        return new OneElementIterator<T>( onlyObject );
    }

    /**
     * Construct one.
     *
     * @param onlyObject the one Object that we iterate over.
     */
    protected OneElementIterator(
            T onlyObject )
    {
        theOnlyObject = onlyObject;
        current       = theOnlyObject;
    }

    /**
     * Do we have another element.
     *
     * @return true if we have another element
     */
    public boolean hasNext()
    {
        return current != null;
    }

    /**
     * Obtain next element.
     *
     * @return the next element
     */
    public T next()
    {
        T ret;

        if( current != null ) {
            ret     = current;
            current = null;
        } else {
            ret = null;
        }

        return ret;
    }

    /**
     * We cannot remove.
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return  <code>true</code> if and only if this enumeration object
     *           contains at least one more element to provide;
     *          <code>false</code> otherwise.
     */
    public boolean hasMoreElements()
    {
        return hasNext();
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return the next element of this enumeration.
     */
    public T nextElement()
    {
        return next();
    }

    /**
     * The only Object that we have.
     */
    protected T theOnlyObject;

    /**
     * The current Object that we will return next time.
     */
    protected T current;
}
