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

package org.infogrid.store;

import org.infogrid.util.CursorIterator;

import java.util.NoSuchElementException;

/**
 * Adds a few methods to <code>CursorIterator&lt;StoreValue&gt;</code> that make life easier.
 */
public interface IterableStoreCursor
        extends
            CursorIterator<StoreValue>
{
    /**
     * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
     * right afterwards.
     *
     * @param key the key of the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    public int moveToBefore(
            String key )
        throws
            NoSuchElementException;

    /**
     * Move the cursor to this element, i.e. return this element when {@link #previous previous} is invoked
     * right afterwards.
     *
     * @param key the key of the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    public int moveToAfter(
            String key )
        throws
            NoSuchElementException;

    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public IterableStoreCursor createCopy();
}
