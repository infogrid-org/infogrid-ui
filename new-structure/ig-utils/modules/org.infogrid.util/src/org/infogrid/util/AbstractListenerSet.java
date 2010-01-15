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

import org.infogrid.util.logging.Log;

/**
 * Factors out common functionality of sets of listeners.
 * @param <T> the type of the listener
 * @param <E> the type of the event
 * @param <P> the type of parameter that allows this AbstractListenerSet to invoke the right listener method
 */
public abstract class AbstractListenerSet<T,E,P>
        implements
            ListenerSet<T,E,P>
{
    private static final Log log = Log.getLogInstance( AbstractListenerSet.class ); // our own, private logger

    /**
     * Constructor.
     * 
     * @param catchRuntimeExceptions if true, any event will be fired to all listeners, even if the listeners throw RuntimeExceptions
     */
    protected AbstractListenerSet(
            boolean catchRuntimeExceptions )
    {
        theCatchRuntimeExceptions = catchRuntimeExceptions;
    }

    /**
     * Add a new listener object to this set using a WeakReference.
     *
     * @param newListener the listener to be added to this set
     */
    public abstract void addWeak(
            T newListener );
    
    /**
     * Add a new listener object to this set using a SoftReference.
     *
     * @param newListener the listener to be added to this set
     */
    public abstract void addSoft(
            T newListener );
    
    /**
     * Add a new listener object to this set directly, i.e. without using References.
     *
     * @param newListener the listener to be added to this set
     */
    public abstract void addDirect(
            T newListener );

    /**
     * Remove a listener object member of this set.
     *
     * @param oldListener the listener to be removed from this set
     */
    public abstract void remove(
            T oldListener );
    
    /**
     * Determine whether this set is empty.
     *
     * @return the set is empty if this returns true
     */
    public abstract boolean isEmpty();

    /**
     * Clear all members of this set.
     */
    public abstract void clear();
    
    /**
     * Fire an event to all members of this set.
     *
     * @param event the event, such as PropertyChangeEvent
     */
    public void fireEvent(
            E event )
    {
        fireEvent( event, null );
    }

    /**
     * Fire an event to all members of the set. The parameter is helpful for dispatching
     * to the right method in the listener interfaces for subclasses of this class.
     *
     * @param event the event, such as PropertyChangeEvent
     * @param parameter a user-specific paramater that can be used to dispatch to the right method in the fireEventToListener method
     */
    public abstract void fireEvent(
            E event,
            P parameter );

    /**
     * Fire the provided event and parameter to the set of objects in this array.
     *
     * @param realList the real list of listeners
     * @param startIndex the start index in the real list of listeners
     * @param event the event, such as PropertyChangeEvent
     * @param parameter a user-specific paramater that can be used to dispatch to the right method in the fireEventToListener method
     */
    protected void internalFireEvent(
            T [] realList,
            int  startIndex,
            E    event,
            P    parameter )
    {
        // now fire outside of the synchronized block
        if( theCatchRuntimeExceptions ) {
            if( realList.length > 0 ) {
                for( int i=startIndex ; i<realList.length ; ++i ) {
                    try {
                        fireEventToListener( realList[i], event, parameter );
                    } catch( RuntimeException ex ) {
                        log.error( ex );
                    }
                }
            } else {
                try {
                    fireEventIfNoListeners( event, parameter );
                } catch( RuntimeException ex ) {
                    log.error( ex );
                }
            }
        } else {
            if( realList.length > 0 ) {
                for( int i=startIndex ; i<realList.length ; ++i ) {
                    fireEventToListener( realList[i], event, parameter );
                }
            } else {
                fireEventIfNoListeners( event, parameter );
            }
        }
    }
    
    /**
     * If this ListenerSet does not currently have any listeners, invoke this
     * overridable method that can handle that case. By default, this method
     * does nothing.
     * 
     * @param event the event, such as PropertyChangeEvent
     * @param parameter a user-specific paramater that can be used to dispatch to the right method in the fireEventToListener method
     */
    protected void fireEventIfNoListeners(
            E event,
            P parameter )
    {
        // do nothing
    }

    /**
     * Fire the event to one contained object.
     *
     * @param listener the receiver of this event
     * @param event the sent event
     * @param parameter dispatch parameter
     */
    protected abstract void fireEventToListener(
            T listener,
            E event,
            P parameter );

    /**
     * If this is true, we catch all RuntimeExceptions from our listeners.
     */
    protected boolean theCatchRuntimeExceptions;
}
