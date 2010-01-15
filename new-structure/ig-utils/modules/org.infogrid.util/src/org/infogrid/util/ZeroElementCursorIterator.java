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
 * This Iterator is always past the last element and never returns anything.
 * It is surprising how useful this class can be.
 * 
 * @param <T> the type of element to iterate over
 */
public final class ZeroElementCursorIterator<T>
        extends
            ArrayCursorIterator<T>
{
    /**
     * Factory method.
     * 
     * @return the created ZeroElementCursorIterator
     * @param <T> the type of element to iterate over
     */
    public static <T> ZeroElementCursorIterator<T> create()
    {
        return new ZeroElementCursorIterator<T>();
    }
    
    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    protected ZeroElementCursorIterator()
    {
        super( (T []) EMPTY_ARRAY, (Class<T>) Object.class, 0, 0, 0 );
    }

    /**
     * The empty array to iterate over.
     */
    protected static final Object [] EMPTY_ARRAY = {};
}
