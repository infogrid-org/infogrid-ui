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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is an Iterator that returns all content of a delegate Iterator, except that
 * it skips all duplicates after they were returned once.
 * 
 * @param T the type of element to iterate over
 */
public class UniqueIterator<T>
        implements
            Iterator<T>
{
    /**
     * Construct one.
     *
     * @param delegate the delegate Iterator
     * @param useEquals if true, we use the equals method to determine equality, == otherwise
     */
    public UniqueIterator(
            Iterator<T> delegate,
            boolean     useEquals )
    {
        theDelegate  = delegate;
        theUseEquals = useEquals;

        if( theDelegate.hasNext() ) {
            nextObject = theDelegate.next();
            foundAlready.add( nextObject );

        } else {
            nextObject = null;
        }
    }

    /**
     * Do we have a next object to return?
     *
     * @return true if we have a next object to return
     */
    public boolean hasNext()
    {
        return nextObject != null;
    }

    /**
     * Return the next object and iterate.
     *
     * @return the next object
     */
    public T next()
    {
        T ret = nextObject;

        while( true ) {
            if( theDelegate.hasNext() ) {
                nextObject = theDelegate.next();
            
            } else {
                nextObject = null;
                break;
            }

            boolean found;
            if( theUseEquals ) {
                found = foundAlready.contains( nextObject );

            } else {
                found = false;
                for( int i = foundAlready.size()-1 ; i>=0 ; --i ) {
                    if( nextObject == foundAlready.get( i )) {

                        found = true;
                        break;
                    }
                }
            }
            if( !found ) {
                break;
            }
        }

        foundAlready.add( nextObject );
        return ret;
    }

    /**
     * We don't know how to remove.
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * The delegate iterator.
     */
    protected Iterator<T> theDelegate;

    /**
     * Do we use the equals method to determine object equality.
     */
    protected boolean theUseEquals;

    /**
     * The next Object to return.
     */
    protected T nextObject;

    /**
     * The container of all Objects we have found already.
     */
    protected ArrayList<T> foundAlready = new ArrayList<T>();
}
