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
 * This is a Collection "projecting" the unification (with duplicates)
 * of N other Collections. It is a facade, i.e. it does not actually
 * hold any data itself.
 * 
 * @param E the type of element to iterate over
 */
public class UnifiedCollectionFacade<E>
        extends
            AbstractCollection<E>
{
    /**
     * Construct one.
     *
     * @param coll the set of Collections to unify
     */
    public UnifiedCollectionFacade(
            Collection<E> [] coll )
    {
        theCollections = coll;
    }

    /**
     * Obtain the number of elements in this Collection.
     *
     * @return the number of elements in this Collection
     */
    public int size()
    {
        int ret = 0;
        for( int i=0 ; i<theCollections.length ; ++i ) {
            ret += theCollections[i].size();
        }
        return ret;
    }

    /**
     * Returns <tt>true</tt> if this Collection contains no elements.
     *
     * @return <tt>true</tt> if this Collection contains no elements
     */
    @Override
    public boolean isEmpty()
    {
        for( int i=0 ; i<theCollections.length ; ++i ) {
            if( !theCollections[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <tt>true</tt> if this Collection contains the specified
     * element.  More formally, returns <tt>true</tt> if and only if this
     * collection contains at least one element <tt>e</tt> such that
     * <tt>(o==null ? e==null : o.equals(e))</tt>.
     *
     * @param o element whose presence in this collection is to be tested.
     * @return <tt>true</tt> if this collection contains the specified
     *         element
     */
    @Override
    public boolean contains(
            Object o )
    {
        for( int i=0 ; i<theCollections.length ; ++i ) {
            if( theCollections[i].contains( o )) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an Iterator over the elements in this collection.  There are no
     * guarantees concerning the order in which the elements are returned
     * (unless this collection is an instance of some class that provides a
     * guarantee).
     *
     * @return an <tt>Iterator</tt> over the elements in this collection
     */
    @Override
    public Iterator<E> iterator()
    {
        return new MyIterator();
    }

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation).
     *
     * @param o element to be removed from this collection, if present.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * @throws UnsupportedOperationException remove is not supported by this
     *         collection.
     */
    @Override
    public boolean remove(
            Object o )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all of the elements from this collection.
     *
     * @throws UnsupportedOperationException always thrown
     */
    @Override
    public void clear()
    {
        for( int i=0 ; i<theCollections.length ; ++i ) {
            theCollections[i].clear();
        }
    }

    /**
     * The constituent Collections.
     */
    protected Collection<E> [] theCollections;

    /**
     * An Iterator appropriate for this Collection.
     */
    class MyIterator
            implements
                Iterator<E>
    {
        /**
         * Constructor.
         */
        public MyIterator()
        {
            goNext();
        }

        /**
         * Returns <tt>true</tt> if the Iterator has more elements.
         *
         * @return <tt>true</tt> if the Iterator has more elements.
         */
        public boolean hasNext()
        {
            return nextToReturn != null;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         */
        public E next()
        {
            E ret = nextToReturn;
            goNext();
            return ret;
        }

        /**
         * Removes from the underlying collection the last element returned by the
         * iterator.
         *
         * @throws UnsupportedOperationException always thrown
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * Internal helper to go to the next element.
         */
        protected void goNext()
        {
            if( theIndex == -1 ) {
                if( theCollections.length == 0 ) {
                    return;
                }

                theIndex = 0;
                theIterator = theCollections[theIndex].iterator();
            }

            while( true ) {
                if( !theIterator.hasNext() ) {
                    ++theIndex;
                    if( theIndex >= theCollections.length ) {
                        nextToReturn = null;
                        return;
                    }
                    theIterator = theCollections[theIndex].iterator();

                } else {
                    nextToReturn = theIterator.next();
                    return;
                }

            }
        }

        /**
         * Current index into the Collections array.
         */
        protected int theIndex = -1;

        /**
         * The next Object to return, if any.
         */
        protected E nextToReturn;

        /**
         * The current Iterator at the current index.
         */
        protected Iterator<E> theIterator;
    }
}
