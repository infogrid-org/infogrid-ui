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

package org.infogrid.util.instrument;

import org.infogrid.util.logging.Log;

/**
 * This subclasses Thread to provide instrumentation with which we can control
 * the behavior of this Thread better, specifically for debugging and testing
 * purposes.
 */
public class InstrumentedThread
    extends
        Thread
{
    private static final Log log = Log.getLogInstance( InstrumentedThread.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param delegate the code to be run by this thread
     * @param name a name, for debugging
     */
    public InstrumentedThread(
            Runnable delegate,
            String   name )
    {
        super( name );

        theDelegate = delegate;
    }

    /**
     * Run this Runnable. This is declared final, specify Runnable through
     * the constructor.
     */
    @Override
    public final void run()
    {
        if( log.isInfoEnabled() ) {
            log.info( "Thread " + getName() + " running " + theDelegate );
        }

        try {
            theDelegate.run();
        } catch( Throwable ex ) {
            log.error( "Unexpected exception in Runnable: ", ex );
        }

        if( log.isInfoEnabled() ) {
            log.info( "End running Thread " + getName() );
        }

        synchronized( this ) {
            isDead = true;
            this.notifyAll();
        }
    }

    /**
     * Advance this InstrumentedThread to a provided {@link Breakpoint}, and suspend it there.
     *
     * @param bp the Breakpoint to which the InstrumentedThread will advance
     * @throws java.lang.InterruptedException thrown if the Thread got interrupted
     */
    public void advanceTo(
            Breakpoint bp )
        throws
            InterruptedException
    {
        synchronized( bp ) {
            synchronized( this ) {
                if( ! isAlive() ) {
                    start();
                }
                // FIXME? Should this say here: if( runToBreakpoint != null ) runToBreakPoint.notifyAll()

                runToBreakpoint = bp;
            }
            bp.wait();
        }
    }

    /**
     * Advance this thread normally. Don't stop at Breakpoints.
     */
    public void advanceNormally()
    {
        synchronized( this ) {
            if( ! isAlive() ) {
                start();
            }
        }
        if( runToBreakpoint != null ) {
            synchronized( runToBreakpoint ) {
                runToBreakpoint.notifyAll();
                runToBreakpoint = null;
            }
        }
    }

    /**
     * Calling this causes this thread to complete its run, and only after this InstrumentedThread has
     * completed, the calling thread will return from the call. This is different from
     * {@link #advanceNormally} in that it waits until this thread has completed, and does not
     * immediately return as advanceNormally would. Note that if this thread does not
     * finish, the calling thread will be suspended indefinitely.
     *
     * @throws InterruptedException thrown if the calling thread is interrupted while waiting for this thread to complete
     */
    public void completeThreadAndWait()
        throws
            InterruptedException
    {
        advanceNormally();

        synchronized( this ) {
            if( isDead ) {
                return;
            }
            this.wait();
        }
    }

    /**
     * Determine which {@link Breakpoint} this thread is supposed to stop at next.
     *
     * @return the BreakPoint at which we stop next, or null if we don't
     */
    public Breakpoint getNextBreakpoint()
    {
        return runToBreakpoint;
    }

    /**
     * The Runnable that we delegate to.
     */
    protected Runnable theDelegate;

    /**
     * This is true once this Thread has run its course and died.
     */
    private boolean isDead = false;

    /**
     * The Breakpoint to which we run currently, if any.
     */
    private Breakpoint runToBreakpoint = null;
}
