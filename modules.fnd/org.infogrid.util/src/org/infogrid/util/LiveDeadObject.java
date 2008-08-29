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
 * This interface is implemented by certain Objects that need an explicit <code>die()</code>
 * invocation to get rid of allocated resources, subscriptions, etc. It supports a
 * listener mechanism to provide callbacks if/when that happens.
 */
public interface LiveDeadObject
{
    /**
     * Tell this object that we don't need it any more.
     *
     * @throws IsDeadException thrown if this object is dead already
     */
    public void die()
        throws
            IsDeadException;

    /**
     * Determine whether this object is dead already.
     *
     * @return true if this object is dead already
     */
    public boolean isDead();

    /**
     * Add a listener to be notified when this object dies.
     *
     * @param newListener the new listener
     * @see #removeLiveDeadListener
     */
    public void addLiveDeadListener(
            LiveDeadListener newListener );

    /**
     * Remove a listener to be notified when this object dies.
     *
     * @param oldListener the to-be-removed listener
     * @see #addLiveDeadListener
     */
    public void removeLiveDeadListener(
            LiveDeadListener oldListener );
}
