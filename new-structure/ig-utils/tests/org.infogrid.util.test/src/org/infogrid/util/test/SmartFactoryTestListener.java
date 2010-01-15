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

import org.infogrid.util.SmartFactoryEvent;
import org.infogrid.util.SmartFactoryListener;
import org.infogrid.util.logging.Log;

import java.util.ArrayList;

/**
 * Listener for SmartFactoryTests.
 */
public class SmartFactoryTestListener
        implements
            SmartFactoryListener
{
    private static final Log log = Log.getLogInstance( SmartFactoryTestListener.class ); // our own, private logger

    /**
     * Constructor.
     */
    public SmartFactoryTestListener()
    {
    }
    
    /**
     * Receive an "added" event.
     * 
     * @param event the received event
     */
    public void smartFactoryElementAdded(
            SmartFactoryEvent.Added event )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "smartFactoryElementAdded", event );
        }
        theAddedEvents.add( event );
    }

    /**
     * Receive an "removed" event.
     * 
     * @param event the received event
     */
    public void smartFactoryElementRemoved(
            SmartFactoryEvent.Removed event )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "smartFactoryElementRemoved", event );
        }
        theRemovedEvents.add( event );
    }
    
    /**
     * Clear the buffer.
     */
    public void clear()
    {
        theAddedEvents.clear();
        theRemovedEvents.clear();
    }

    /**
     * Buffer of received "added" events.
     */
    public ArrayList<SmartFactoryEvent.Added> theAddedEvents = new ArrayList<SmartFactoryEvent.Added>();
    
    /**
     * Buffer of received "removed" events.
     */
    public ArrayList<SmartFactoryEvent.Removed> theRemovedEvents = new ArrayList<SmartFactoryEvent.Removed>();
    
    
}
