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
 * A listener for events created by {@link CachingMap}s.
 */
public interface CachingMapListener
{
    /**
     * An element was added to the CachingMap.
     *
     * @param event the event
     */
    public void mapElementAdded(
            CachingMapEvent.Added event );

    /**
     * An element was removed from the CachingMap.
     *
     * @param event the event
     */
    public void mapElementRemoved(
            CachingMapEvent.Removed event );
    
    /**
     * An element expired from the cache, but can still be retrieved from storage.
     *
     * @param event the event
     */
    public void mapElementExpired(
            CachingMapEvent.Expired event );
}
