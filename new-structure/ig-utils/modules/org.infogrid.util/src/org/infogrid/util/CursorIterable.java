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
 * This interface is supported by those <code>java.lang.Iterable</code>s that return a
 * {@link CursorIterator CursorIterator} instead of just a plain <code>Iterator</code>.
 * 
 * @param <T> the type of element to iterate over
 */
public interface CursorIterable<T>
        extends
            Iterable<T>
{
    /**
     * Obtain a @{link CursorIterable} instead of a <code>java.util.Iterator</code>.
     *
     * @return the <code>CursorIterable</code>
     */
    public CursorIterator<T> iterator();

    /**
     * Obtain a @{link CursorIterable}. This performs the exact same operation as
     * {@link #iterator iterator}, but is friendlier towards JSPs and other software
     * that likes to use JavaBeans conventions.
     *
     * @return the <code>CursorIterable</code>
     */
    public CursorIterator<T> getIterator();
}
