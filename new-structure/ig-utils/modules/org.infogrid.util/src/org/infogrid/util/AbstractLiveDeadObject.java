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
import java.util.Iterator;

/**
 * This is a simple, abstract implementation of {@link LiveDeadObject}
 * that can be used as "mixin".
 */
public abstract class AbstractLiveDeadObject
    implements
        LiveDeadObject
{
    /**
      * Tell this object that we don't need it any more.
      *
      * @throws IsDeadException if called on an object that is dead already
      */
    public abstract void die()
        throws
            IsDeadException;

    /**
     * Determine whether this object is dead already.
     *
     * @return true if this object is dead
     */
    public final boolean isDead()
    {
        return isDead;
    }

    /**
      * Simple checking method to create IsDeadExceptions when necessary.
      *
      * @throws IsDeadException thrown if this LiveDeadObject is dead already; do nothing otherwise
      */
    public final void checkDead()
        throws
            IsDeadException
    {
        if( isDead ) {
            throw new IsDeadException( this );
        }
    }

    /**
     * This convenience method for our subclasses will set the isDead flag to false,
     * and continue, or throw an exception if we are dead already.
     *
     * @throws IsDeadException thrown if this object is dead already
     */
    protected synchronized void makeDead()
        throws
            IsDeadException
    {
        checkDead();

        isDead = true;
    }

    /**
     * Register as a listener to be notified when this object dies.
     *
     * @param newListener the new listener
     * @see #removeLiveDeadListener
     */
    public synchronized void addLiveDeadListener(
            LiveDeadListener newListener )
    {
        if( theLiveDeadListeners == null ) {
            theLiveDeadListeners = new ArrayList<LiveDeadListener>();
        }
        theLiveDeadListeners.add( newListener );
    }

    /**
     * Unregister as a listener to be notified when this object dies.
     *
     * @param oldListener the to-be-removed listener
     * @see #addLiveDeadListener
     */
    public synchronized void removeLiveDeadListener(
            LiveDeadListener oldListener )
    {
        theLiveDeadListeners.remove( oldListener );
    }

    /**
     * This allows subclasses to fire off an event "we died".
     */
    protected final void fireObjectDied()
    {
        Iterator<LiveDeadListener> theIter;
        synchronized( this ) {
            if( theLiveDeadListeners == null || theLiveDeadListeners.isEmpty() ) {
                return;
            }

            theIter = new ArrayList<LiveDeadListener>( theLiveDeadListeners ).iterator();
        }

        while( theIter.hasNext() ) {
            LiveDeadListener current = theIter.next();
            current.objectDied( this );
        }
    }

    /**
     * Flag indicating whether we are dead already.
     */
    private boolean isDead;

    /**
     * The current list of listeners to our live dead state, allocated as needed.
     */
    private ArrayList<LiveDeadListener> theLiveDeadListeners = null;
}
