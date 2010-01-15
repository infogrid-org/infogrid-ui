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

package org.infogrid.util;

/**
 * This Iterator returns only a single element.
 *
 * @param <T> the type of element to iterate over
 */
public final class SingleElementCursorIterator<T>
        extends
            ArrayCursorIterator<T>
{
    /**
     * Factory method.
     *
     * @param single the single element to iterate over
     * @return the created SingleElementCursorIterator
     * @param <T> the type of element to iterate over
     */
    @SuppressWarnings("unchecked")
    public static <T> SingleElementCursorIterator<T> create(
            T single )
    {
        T [] array = (T []) ArrayHelper.createArray( single.getClass(), 1 );
        array[0] = single;

        return new SingleElementCursorIterator<T>( array, (Class<T>) single.getClass(), 0 );
    }

    /**
     * Constructor.
     * 
     * @param array the array to iterate over
     * @param arrayComponentType the <code>Class</code> that should be used to create arrays for return values
     * @param startPosition the start position
     */
    protected SingleElementCursorIterator(
            T []     array,
            Class<T> arrayComponentType,
            int      startPosition )
    {
        super( array, arrayComponentType, startPosition, 0, 1 );
    }
}
