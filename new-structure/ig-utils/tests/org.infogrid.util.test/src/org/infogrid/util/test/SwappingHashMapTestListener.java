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

package org.infogrid.util.test;

import org.infogrid.util.CachingMapEvent;
import org.infogrid.util.CachingMapListener;
import org.infogrid.util.logging.Log;

import java.util.ArrayList;

/**
 * A CachingMapListener that also collects SwapwingHashMap-specific events.
 */
public class SwappingHashMapTestListener
        implements
            CachingMapListener
{
    private static final Log log = Log.getLogInstance( SwappingHashMapTestListener.class ); // our own, private logger

    /**
     * Constructor.
     */
    public SwappingHashMapTestListener()
    {
    }
    
    /**
     * Receive an "added" event.
     * 
     * @param event the received event
     */
    public void mapElementAdded(
            CachingMapEvent.Added event )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "mapElementAdded", event );
        }
        theAddedEvents.add( event );
    }

    /**
     * Receive an "removed" event.
     * 
     * @param event the received event
     */
    public void mapElementRemoved(
            CachingMapEvent.Removed event )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "mapElementRemoved", event );
        }
        theRemovedEvents.add( event );
    }
    
    /**
     * Receive an "removed" event.
     * 
     * @param event the received event
     */
    public void mapElementExpired(
            CachingMapEvent.Expired event )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "mapElementExpired", event );
        }
        theExpiredEvents.add( event );
    }
    
    /**
     * Clear the buffer.
     */
    public void clear()
    {
        theAddedEvents.clear();
        theRemovedEvents.clear();
        theExpiredEvents.clear();
    }

    /**
     * Buffer of received "added" events.
     */
    public ArrayList<CachingMapEvent.Added> theAddedEvents = new ArrayList<CachingMapEvent.Added>();
    
    /**
     * Buffer of received "removed" events.
     */
    public ArrayList<CachingMapEvent.Removed> theRemovedEvents = new ArrayList<CachingMapEvent.Removed>();
    
    
    /**
     * Buffer of received "expired" events.
     */
    public ArrayList<CachingMapEvent.Expired> theExpiredEvents = new ArrayList<CachingMapEvent.Expired>();
}
