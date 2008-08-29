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

import java.util.*;

/**
 * This Iterator is always past the last element and never returns anything.
 * It is surprising how useful this class can be.
 * 
 * @param T the type of element to iterate over
 */
public final class ZeroElementIterator<T>
        implements
            Enumeration<T>,
            Iterator<T>
{
    /**
     * Factory method.
     * 
     * @return the created ZeroElementIterator
     */
    public static <T> ZeroElementIterator<T> create()
    {
        return new ZeroElementIterator<T>();
    }
    
    /**
     * Constructor.
     */
    protected ZeroElementIterator()
    {
        // noop
    }

    /**
      * We do not have more elements.
      *
      * @return false
      */
    public boolean hasMoreElements()
    {
        return false;
    }

    /**
      * We do not have more elements.
      *
      * @return false
      */
    public boolean hasNext()
    {
        return false;
    }

    /**
      * No next element.
      *
      * @return null
      */
    public T nextElement()
    {
        return null;
    }

    /**
      * No next element.
      *
      * @return null
      */
    public T next()
    {
        return null;
    }

    /**
     * We cannot remove anything.
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
