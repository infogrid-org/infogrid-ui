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

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator that returns the values obtained from an Iterator over references.
 * It also "clears up" expired references, but does not support remove().
 * 
 * @param <E> the type of element to iterate over
 */
public class DereferencingIterator<E>
        implements
            Iterator<E>
{
    /**
     * Constructor.
     *
     * @param delegate the delegate Iterator that iterates over
     *        <code>Reference&lt;E&gt;</code> instead of <code>E</code>
     */
    public DereferencingIterator(
            Iterator<? extends Reference<E>> delegate )
    {
        theDelegate = delegate;
        
        goNext();
    }
    
    /**
     * Returns <tt>true</tt> if the Iterator has more elements.
     *
     * @return <tt>true</tt> if the Iterator has more elements.
     */
    public boolean hasNext()
    {
        while( true ) {
            if( theNextElement == null ) {
                return false;
            }
            E nextElement = theNextElement;
            if( nextElement != null ) {
                return true;
            }
            goNext();
        }
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public E next()
    {
        while( true ) {
            if( theNextElement == null ) {
                return null;
            }
            E nextElement = theNextElement;

            goNext(); // first go next, then return what we found

            return nextElement;
        }
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * Iterator (optional operation).
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Iterate forward to the next value.
     */
    protected void goNext()
    {
        while( theDelegate.hasNext() ) {
            E found = theDelegate.next().get();
            
            if( found != null ) {
                theNextElement = found;
                return;
            }
        }
        theNextElement = null;
    }

    /**
     * The delegate Iterator.
     */
    protected Iterator<? extends Reference<E>> theDelegate;
    
    /**
     * The next element to return. We store this directly so it doesn't disappear on us
     * while we think it is still around.
     */
    protected E theNextElement;
}
