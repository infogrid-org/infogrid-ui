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

/**
 * An abstract implementation of <code>IterableStore</code>.
 */
public abstract class AbstractIterableStore
        extends
            AbstractStore
        implements
            IterableStore
{
    /**
     * Obtain an Iterator over the content of this Store.
     *
     * @return the Iterator
     */
    public IterableStoreCursor getIterator()
    {
        return iterator();
    }

    /**
     * Determine the number of StoreValues in this Store.
     *
     * @return the number of StoreValues in this Store
     * @throws IOException thrown if an I/O error occurred
     */
    public int size()
        throws
            IOException
    {
        return size( "" );
    }

    /**
     * Determine whether this Store is empty.
     *
     * @return true if this Store is empty
     * @throws IOException thrown if an I/O error occurred
     */
    public boolean isEmpty()
        throws
            IOException
    {
        return size() == 0;
    }
}
