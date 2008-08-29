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
 * Represents a set of listeners.
 * 
 * @param T the type of listener
 * @param E the type of event
 * @param P the type of parameter
 */
public interface ListenerSet<T,E,P>
{
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
}
