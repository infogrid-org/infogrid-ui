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

package org.infogrid.probe.TEST;

import org.infogrid.probe.shadow.ShadowMeshBaseEvent;
import org.infogrid.probe.shadow.ShadowMeshBaseListener;
import org.infogrid.testharness.AbstractTest;

import java.util.ArrayList;

/**
 * Listen to Shadow events.
 */
public class ProbeTestShadowListener
        implements
            ShadowMeshBaseListener
{
    /**
     * Constructor.
     *
     * @param id identifying this particular listener
     */
    public ProbeTestShadowListener(
            String id )
    {
        identifier = id;
    }

    /**
      * An update of the information in the ShadowMeshBase is about to start.
      *
      * @param theEvent the event
      */
    public void updateStarting(
            ShadowMeshBaseEvent theEvent )
    {
        addEvent( "started : " + theEvent + AbstractTest.stackTraceToString() );
    }

    /**
      * An update of the information in the ShadowMeshBase was skipped as there
      * were good reasons to believe that no changes in the data source would be found.
      *
      * @param theEvent the event
      */
    public void updateSkipped(
            ShadowMeshBaseEvent theEvent )
    {
        addEvent( "skipped : " + theEvent + AbstractTest.stackTraceToString() );
    }

    /**
      * An update of the information in the ShadowMeshBase has just finished
      * successfully.
      *
      * @param theEvent the event
      */
    public void updateFinishedSuccessfully(
            ShadowMeshBaseEvent theEvent )
    {
        addEvent( "finished successfully : " + theEvent + AbstractTest.stackTraceToString() );
    }

    /**
      * An update of the information in the ShadowMeshBase has just finished
      * unsuccessfully. The event indicates the problem.
      *
      * @param theEvent the event
      */
    public void updateFinishedUnsuccessfully(
            ShadowMeshBaseEvent theEvent )
    {
        addEvent( "finished UNsuccessfully : " + theEvent + AbstractTest.stackTraceToString() );
    }

    /**
     * Add an event. This is factored out so we can easily set a breakpoint.
     */
    protected void addEvent(
            String s )
    {
        eventQueue.add( s );
    }

    /**
     * Determine the number of events we received.
     *
     * @return the number of events we received
     */
    public int size()
    {
        return eventQueue.size();
    }

    /**
     * Clear the queue.
     */
    public void clear()
    {
        eventQueue.clear();
    }
    
    /**
     * Convert to String, for debugging.
     *
     * @return as String
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "ProbeTestShadowListener: " );
        buf.append( identifier );
        for( String current : eventQueue ) {
            buf.append( "\n    " ).append( current );
        }
        return buf.toString();
    }

    /**
     * identifier for this particular listener
     */
    protected String identifier;
    
    /**
     * The event queue.
     */
    protected ArrayList<String> eventQueue = new ArrayList<String>();
}