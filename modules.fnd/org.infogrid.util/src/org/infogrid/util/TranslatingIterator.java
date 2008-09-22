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
import java.util.NoSuchElementException;

/**
 * Iterator that translates the values from some other iterator. This little utility is
 * useful too often to not put it into its own class.
 * 
 * @param <E1> the type of returned elements by this iterator
 * @param <E2> the type of returned elements by the delegate iterator
 */
public abstract class TranslatingIterator<E1,E2>
        implements
            Iterator<E1>,
            Enumeration<E1>
{
    /**
     * Constructor for subclasses only.
     * 
     * @param delegate the delegate iterator
     */
    protected TranslatingIterator(
            Iterator<E2> delegate )
    {
        theDelegate = delegate;
    }
    
    /**
     * Subclasses must implement the translation operation.
     * 
     * @param org the object returned by the delegate
     * @return the object to be returned by this iterator
     */
    protected abstract E1 translate(
            E2 org );

    /**
     * Returns <tt>true</tt> if the iteration has more elements.
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext()
    {
        return theDelegate.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public E1 next()
        throws
            NoSuchElementException
    {
        E2 org = theDelegate.next();
        E1 ret = translate( org );
        return ret;
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt>
     *		  operation is not supported by this Iterator.
     
     * @throws IllegalStateException if the <tt>next</tt> method has not
     *		  yet been called, or the <tt>remove</tt> method has already
     *		  been called after the last call to the <tt>next</tt>
     *		  method.
     */
    public void remove()
    {
        theDelegate.remove();
    }

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return  <code>true</code> if and only if this enumeration object
     *           contains at least one more element to provide;
     *          <code>false</code> otherwise.
     */
    public final boolean hasMoreElements()
    {
        return theDelegate.hasNext();
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return     the next element of this enumeration.
     * @throws  NoSuchElementException  if no more elements exist.
     */
    public final E1 nextElement()
    {
        return next();
    }
    
    /**
     * The underlying delegate iterator.
     */
    protected Iterator<E2> theDelegate;
}
