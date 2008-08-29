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

import java.io.IOException;
import org.infogrid.util.CursorIterable;

/**
 * A {@link Store} over whose contained data elements clients can iterate. Because some
 * <code>Stores</code> may be large and distributed, not all <code>Stores</code> are
 * necessarily <code>IterableStores</code>.
 */
public interface IterableStore
        extends
            Store,
            CursorIterable<StoreValue>
{
    /**
     * Return a more specific subtype of CursorIterable as an iterator.
     *
     * @return the Iterator
     */
    public abstract IterableStoreCursor iterator();

    /**
     * Return a more specific subtype of CursorIterable as an iterator.
     *
     * @return the Iterator
     */
    public abstract IterableStoreCursor getIterator();

    /**
     * Determine the number of data elements in this Store. Some classes implementing
     * this interface may only return an approximation.
     *
     * @return the number of data elements in this Store
     * @throws IOException thrown if an I/O error occurred
     */
    public abstract int size()
            throws
                IOException;

    /**
     * Determine the number of StoreValues in this Store whose key starts with this String
     *
     * @param startsWith the String the key starts with
     * @return the number of StoreValues in this Store whose key starts with this String
     * @throws IOException thrown if an I/O error occurred
     */
    public abstract int size(
            String startsWith )
        throws
            IOException;

    /**
     * Determine whether this Store is empty.
     *
     * @return true if this Store is empty
     * @throws IOException thrown if an I/O error occurred
     */
    public abstract boolean isEmpty()
        throws
            IOException;
}
