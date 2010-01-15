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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A Collection that delegates to other Collection.
 *
 * @param <E> the content type
 */
public class DelegatingCollection<E>
    implements
        Collection<E>
{
    /**
     * Factory method for any number of delegates.
     *
     * @param delegates the Maps to which this Map delegates
     * @return the created DelegatingCollection
     * @param <E> the content type
     */
    public static <E> DelegatingCollection<E> create(
            List<Collection<E>> delegates )
    {
        return new DelegatingCollection<E>( delegates );
    }

    /**
     * Factory method for any number of delegates.
     *
     * @param delegates the Maps to which this Map delegates
     * @return the created DelegatingCollection
     * @param <E> the content type
     */
    public static <E> DelegatingCollection<E> create(
            Set<E> [] delegates )
    {
        List<Collection<E>> delegates2 = new ArrayList<Collection<E>>( delegates.length );
        for( int i=0 ; i<delegates.length ; ++i ) {
            delegates2.add( delegates[i] );
        }
        return new DelegatingCollection<E>( delegates2 );
    }

    /**
     * Factory method for two delegates.
     *
     * @param del1 the first delegate
     * @param del2 the second delegate
     * @return the created DelegatingCollection
     * @param <E> the content type
     */
    public static <E> DelegatingCollection<E> create(
            Collection<E> del1,
            Collection<E> del2 )
    {
        List<Collection<E>> delegates2 = new ArrayList<Collection<E>>( 2 );
        delegates2.add( del1 );
        delegates2.add( del2 );

        return new DelegatingCollection<E>( delegates2 );
    }

    /**
     * Constructor.
     *
     * @param delegates the Collections to which this Collection delegates
     */
    protected DelegatingCollection(
            List<Collection<E>> delegates )
    {
        theDelegates = delegates;
    }

    /**
     * Obtain the delegates.
     *
     * @return the delegates
     */
    public List<Collection<E>> getDelegates()
    {
        return theDelegates;
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality).
     */
    public int size()
    {
        int ret = 0;
        for( Collection<E> current : theDelegates ) {
            ret += current.size();
        }
        return ret;
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements.
     */
    public boolean isEmpty()
    {
        for( Collection<E> current : theDelegates ) {
            if( !current.isEmpty() ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return <tt>true</tt> if this set contains the specified element.
     */
    public boolean contains(
            Object o )
    {
        for( Collection<E> current : theDelegates ) {
            if( current.contains( o ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an iterator over the elements in this set.  The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).
     *
     * @return an iterator over the elements in this set.
     */
    public Iterator<E> iterator()
    {
        List<Iterator<E>> newDelegates = new ArrayList<Iterator<E>>( theDelegates.size() );

        for( Collection<E> current : theDelegates ) {
            newDelegates.add( current.iterator() );
        }

        CompositeIterator<E> ret = CompositeIterator.createFromIterators( newDelegates );
        return ret;
    }

    /**
     * Returns an array containing all of the elements in this set.
     *
     * @return an array containing all of the elements in this set.
     */
    public Object [] toArray()
    {
        return toArray( new Object[0] );
    }

    /**
     * Returns an array containing all of the elements in this set; the
     * runtime type of the returned array is that of the specified array.
     *
     * @param a the array into which the elements of this set are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return an array containing the elements of this set.
     * @param <T> type of the array
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(
            T [] a )
    {
        int               count = 0;
        ArrayList<Object> overflow = null;

        for( Collection<E> current : theDelegates ) {
            for( E current2 : current ) {
                if( count < a.length ) {
                    a[count++] = (T) current2;
                } else {
                    if( overflow == null ) {
                        overflow = new ArrayList<Object>();
                    }
                    overflow.add( current2 );
                }
            }
        }

        T [] ret;
        if( overflow == null || overflow.isEmpty() ) {
            ret = a;
        } else {
            ret = ArrayHelper.createArray( (Class<T>) a.getClass().getComponentType(), a.length + overflow.size() );
            System.arraycopy( a, 0, ret, 0, a.length );
            for( int i=0 ; i<overflow.size() ; ++i ) {
                ret[i + a.length] = (T) overflow.get( i );
            }
        }
        return ret;
    }

    // Modification Operations

    /**
     * Adds the specified element to this set if it is not already present.
     *
     * @param o element to be added to this set.
     * @return <tt>true</tt> if this set did not already contain the specified
     *         element.
     *
     * @throws UnsupportedOperationException if the <tt>add</tt> method is not
     * 	       supported by this set.
     */
    public boolean add(
            E o )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o object to be removed from this set, if present.
     * @return true if the set contained the specified element.
     * @throws ClassCastException if the type of the specified element
     * 	       is incompatible with this set (optional).
     */
    public boolean remove(
            Object o )
    {
        throw new UnsupportedOperationException();
    }

    // Bulk Operations

    /**
     * Returns <tt>true</tt> if this set contains all of the elements of the
     * specified collection.  If the specified collection is also a set, this
     * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
     *
     * @param  c collection to be checked for containment in this set.
     * @return <tt>true</tt> if this set contains all of the elements of the
     * 	       specified collection.
     */
    public boolean containsAll(
            Collection<?> c )
    {
        for( Object x : c ) {
            if( !contains( x )) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present.
     *
     * @param c collection whose elements are to be added to this set.
     * @return <tt>true</tt> if this set changed as a result of the call.
     *
     * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
     * 		  not supported by this set.
     */
    public boolean addAll(
            Collection<? extends E> c )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection.
     *
     * @param c collection that defines which elements this set will retain.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call.
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
     * 		  is not supported by this Collection.
     */
    public boolean retainAll(
            Collection<?> c )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection.
     *
     * @param  c collection that defines which elements will be removed from
     *           this set.
     * @return <tt>true</tt> if this set changed as a result of the call.
     *
     * @throws UnsupportedOperationException if the <tt>removeAll</tt>
     * 		  method is not supported by this Collection.
     */
    public boolean removeAll(
            Collection<?> c )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all of the elements from this set.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> method
     * 		  is not supported by this set.
     */
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * The Collections to which this Collection delegates.
     */
    protected List<Collection<E>> theDelegates;
}
