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
 * Simplifies {@link CachingMapListener} implementations that only wish to
 * listen to some of the possible events.
 */
public abstract class AbstractCachingMapListener
        implements
            CachingMapListener
{
    /**
     * An element was added to the SwappingHashMap.
     *
     * @param event the event
     */
    public void mapElementAdded(
            CachingMapEvent.Added event )
    {
        // no op
    }

    /**
     * An element was removed from the SwappingHashMap.
     *
     * @param event the event
     */
    public void mapElementRemoved(
            CachingMapEvent.Removed event )
    {
        // no op
    }
    
    /**
     * An element expired from the cache, but can still be retrieved from storage.
     *
     * @param event the event
     */
    public void mapElementExpired(
            CachingMapEvent.Expired event )
    {
        // no op
    }
}
