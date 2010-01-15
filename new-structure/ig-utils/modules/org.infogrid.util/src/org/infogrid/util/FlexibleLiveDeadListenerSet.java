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
 * A {@link FlexibleListenerSet} for events raised by {@link LiveDeadObject}s.
 */
public class FlexibleLiveDeadListenerSet
    extends
        FlexibleListenerSet<LiveDeadListener,LiveDeadObject,Object>
{
    /**
     * Construct one.
     */
    public FlexibleLiveDeadListenerSet()
    {
        super();
    }

    /**
     * Construct one with an initial size.
     *
     * @param size the initial size
     */
    public FlexibleLiveDeadListenerSet(
            int size )
    {
        super( size );
    }

    /**
     * Fire the event.
     *
     * @param listener the listener to which we send the event
     * @param obj the LiveDeadObject that died
     * @param parameter a parameter (here: ignored)
     */
    protected void fireEventToListener(
            LiveDeadListener listener,
            LiveDeadObject   obj,
            Object           parameter )
    {
        listener.objectDied( obj );
    }
}
