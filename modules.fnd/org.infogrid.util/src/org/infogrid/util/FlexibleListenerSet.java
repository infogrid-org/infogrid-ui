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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>A container for listener objects based on direct containment or references.
 * This needs to be subclassed to add the actual listener invocation method.</p>
 *
 * <p>Experiences shows that if listeners throw (unexpected) RuntimeExceptions,
 * often the whole rest of a piece of software gets confused as events are
 * sent only to some and not all of the listeners. Per default, this
 * class catches all RuntimeExceptions thrown by its listeners. This
 * behavior can be configured through a parameter in the constructor.</p>
 * 
 * @param <T> the type of listener
 * @param <E> the type of event
 * @param <P> the type of parameter
 */
public abstract class FlexibleListenerSet<T,E,P>
        extends
            AbstractListenerSet<T,E,P>
{
    /**
     * Constructor with default initial size.
     */
    protected FlexibleListenerSet()
    {
        this( true );
    }

    /**
     * Constructor with specified initial size.
     *
     * @param size initial size of the collection
     */
    protected FlexibleListenerSet(
            int size )
    {
        this( size, true );
    }

    /**
     * Create one with default initial size, specifying whether or not we want to catch
     * all RuntimeExceptions.
     *
     * @param catchRuntimeExceptions if set to false, firing events will be stopped as soon as an Exception occurs.
     */
    protected FlexibleListenerSet(
            boolean catchRuntimeExceptions )
    {
        super( catchRuntimeExceptions );

        theContent = new ArrayList<Object>();
    }

    /**
     * Create one with specified initial size, specifying whether or not we want to catch
     * all RuntimeExceptions.
     *
     * @param size initial size of the collection
     * @param catchRuntimeExceptions if set to false, firing events will be stopped as soon as an Exception occurs.
     */
    protected FlexibleListenerSet(
            int     size,
            boolean catchRuntimeExceptions )
    {
        super( catchRuntimeExceptions );

        theContent = new ArrayList<Object>( size );
    }

    /**
     * Obtain an iterator over this set. This iterator is guaranteed to skip
     * garbage-collected objects.
     *
     * @return the iterator over this set
     */
    public Iterator<T> iterator()
    {
        return new MyIterator<T>();
    }

    /**
     * Add a new listener object to this set using a WeakReference.
     *
     * @param newListener the listener to be added to this set
     * @see #addSoft
     * @see #addDirect
     * @see #remove
     */
    public void addWeak(
            T newListener )
    {
        internalAdd( new WeakReference<T>( newListener ), newListener );
    }
    
    /**
     * Add a new listener object to this set using a SoftReference.
     *
     * @param newListener the listener to be added to this set
     * @see #addWeak
     * @see #addDirect
     * @see #remove
     */
    public void addSoft(
            T newListener )
    {
        internalAdd( new SoftReference<T>( newListener ), newListener );
    }
    
    /**
     * Add a new listener object to this set directly, i.e. without using References.
     *
     * @param newListener the listener to be added to this set
     * @see #addSoft
     * @see #addWeak
     * @see #remove
     */
    public void addDirect(
            T newListener )
    {
        internalAdd( newListener, newListener );
    }
    
    /**
     * Internal helper to add a new listener.
     * 
     * @param newListenerOrReference the Reference to the listener, or the listener directly
     * @param newListener the new listener
     */
    @SuppressWarnings(value={"unchecked"})
    protected void internalAdd(
            Object newListenerOrReference,
            Object newListener )
    {
        // set this to false for production build if you like
        if( true ) {
            for( int i=theContent.size()-1 ; i>=0 ; --i ) {
                Object found = theContent.get( i );
                if( found instanceof Reference ) {
                    Reference<T> current = (Reference<T>) found;
                    if( current.get() == newListener ) {
                        throw new IllegalArgumentException( "have listener already: " + newListener );
                    }
                } else {
                    if( found == newListener ) {
                        throw new IllegalArgumentException( "have listener already: " + newListener );
                    }
                }
            }
        }
        theContent.add( newListenerOrReference );
    }

    /**
     * Remove a listener object member of this set.
     *
     * @param oldListener the listener to be removed from this set
     * @see #addSoft
     * @see #addDirect
     * @see #addWeak
     */
    @SuppressWarnings(value={"unchecked"})
    public synchronized void remove(
            T oldListener )
    {
        for( int i=theContent.size()-1 ; i>=0 ; --i ) {
            Object found = theContent.get( i );
            if( found instanceof Reference ) {
                Reference<T> current = (Reference<T>) found;
                if( current.get() == oldListener ) {
                    theContent.remove( i );
                    break;
                }
            } else {
                if( found == oldListener ) {
                    break;
                }
            }
        }
    }

    /**
     * Obtain the nth object contained in this set. This may return null if
     * the object has been garbage-collected.
     *
     * @param n specifies the position of the element we want to obtain
     * @return the found listener object at this position, or null.
     */
    @SuppressWarnings(value={"unchecked"})
    public T get(
            int n )
    {
        Object found = theContent.get( n );
        if( found instanceof Reference ) {
            Reference<T> current = (Reference<T>) found;
            return current.get();
        } else {
            return (T) found;
        }
    }

    /**
     * Determine whether this set is empty.
     *
     * @return the set is empty if this returns true
     */
    @SuppressWarnings(value={"unchecked"})
    public synchronized boolean isEmpty()
    {
        if( theContent.isEmpty() ) {
            return true;
        }
        int size = theContent.size();
        int firstNull = -1;
        for( int i=size-1 ; i>=0 ; --i ) {
            Object found = theContent.get( i );
            if( found instanceof Reference ) {
                Reference<T> current = (Reference<T>) found;
                if( current.get() == null ) {
                    firstNull = i;
                    break;
                }
            }
        }
        if( firstNull < 0 ) {
            return false;
        }

        ArrayList<Object> newContent = new ArrayList<Object>( size-1 ); // fits all elements, maybe more
        for( int i=0 ; i<size ; ++i ) {
            Object found = theContent.get( i );
            if( found instanceof Reference ) {
                Reference<T> current = (Reference<T>) found;
                if( current.get() != null ) {
                    newContent.add( current );
                }
            } else {
                newContent.add( found );
            }
        }
        theContent = newContent;
        return theContent.isEmpty();
    }

    /**
     * Obtain the number of elements in this set. Because of garbage collection, the actual
     * number of non-null elements in this set may be lower that the returned value.
     *
     * @return the number of elements in this set (includes recently garbage-collected ones)
     */
    public int size()
    {
        return theContent.size();
    }

    /**
     * Clear all members of this set.
     */
    public void clear()
    {
        theContent.clear();
    }

    /**
     * Fire an event to all members of the set. The parameter is helpful for dispatching
     * to the right method in the listener interfaces for subclasses of this class.
     *
     * @param event the event, such as PropertyChangeEvent
     * @param parameter a user-specific paramater that can be used to dispatch to the right method in the fireEventToListener method
     */
    @SuppressWarnings(value={"unchecked"})
    public void fireEvent(
            E event,
            P parameter )
    {
        // build real listener list
        T realList [] = (T []) new Object[ theContent.size() ];
        int realIndex   = realList.length;

        synchronized( this ) {
            // by going backwards, we avoid index confusion
            for( int i=theContent.size()-1 ; i>=0 ; --i ) {
                Object found    = theContent.get( i );
                Object listener;

                if( found instanceof Reference ) {
                    Reference<T> current  = (Reference<T>) found;
                    listener = current.get();
                } else {
                    listener = found;
                }

                if( listener != null ) {
                    realList[--realIndex] = (T) listener;
                } else {
                    theContent.remove( i );
                }
            }
        }

        internalFireEvent( realList, realIndex, event, parameter );
    }
 
    /**
     * Contains the references to the listeners.
     */
    protected ArrayList<Object> theContent;

    /**
     * Iterator implementation for this set.
     * 
     * @param <T> the type of element to iterate over
     */
    class MyIterator<T>
        implements
            Iterator<T>
    {
        /**
         * Construct one. We copy the non-empty content of the set in order to
         * avoid concurrent modifications.
         */
        public MyIterator()
        {
            listeners    = new Object[ size() ];
            currentIndex = listeners.length;
            for( int i=theContent.size()-1 ; i>=0 ; --i ) {
                Object current = get(i);
                if( current != null ) {
                    listeners[ --currentIndex ] = current;
                }
            }
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements.
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext()
        {
            return currentIndex < listeners.length;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         */
        @SuppressWarnings(value={"unchecked"})
        public T next()
        {
            return (T) listeners[currentIndex++];
        }

        /**
         * Remove element -- not supported.
         *
         * @throws UnsupportedOperationException always throws this
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * Index of the next object to be returned.
         */
        protected int currentIndex;

        /**
         * Copy of the content of the set.
         */
        protected Object [] listeners;
    }
}
