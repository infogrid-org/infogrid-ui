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

package org.infogrid.util.instrument;

import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * This is pretty much the same thing as what a debugger calls a breakpoint, except that
 * this one can be run without the debugger. It collaborates with {@link InstrumentedThread},
 * and can only be used on Threads that are of class InstrumentedThread. The run-time overhead
 * is fairly small, so it can be incorporated in production code if needed.
 */
public class Breakpoint
        implements
            CanBeDumped
{
    private static final Log log = Log.getLogInstance( Breakpoint.class ); // our own, private logger

    /**
     * Constructor, not specifying a name for this Breakpoint.
     */
    public Breakpoint()
    {
        this( null, 0 );
    }

    /**
     * Constructor, with a name for this Breakpoint. The name is provided only for
     * identification purposes during debugging and/or logging.
     *
     * @param name a name for this Breakpoint
     * @param delay the maximum time to stop at the breakpoint, in milliseconds. 0 means forever
     */
    public Breakpoint(
            String name,
            long   delay )
    {
        theName  = name;
        theDelay = delay;
    }

    /**
     * To declare that running code has reached this Breakpoint, invoke this method.
     * Depending on whether this Breakpoint is active or not, the thread will either
     * return immediately, or be suspended.
     *
     * @throws java.lang.InterruptedException thrown if the Thread was interrupted while waiting at this Breakpoint
     */
    public final void reached()
        throws
            InterruptedException
    {
        logEnter();

        Thread current = Thread.currentThread();
        if( current instanceof InstrumentedThread ) {
            InstrumentedThread realCurrent = (InstrumentedThread) current;

            synchronized( this ) {
                notifyAll();

                logWait();

                if( realCurrent.getNextBreakpoint() == this ) {
                    wait( theDelay );
                }
            }
        }
        logExit();
    }

    /**
     * This overridable method provides a hook through which we can log having entered this Breakpoint.
     */
    protected void logEnter()
    {
        if( log.isDebugEnabled() ) {
            log.traceMethodCallEntry( this, "logEnter" );
        }
    }

    /**
     * This overridable method provides a hook through which we can log that we are now suspended at this Breakpoint.
     */
    protected void logWait()
    {
        if( log.isDebugEnabled() ) {
            log.traceMethodCallEntry( this, "logWait" );
        }
    }

    /**
     * This overridable method provides a hook through which we can log having exited this Breakpoint.
     */
    protected void logExit()
    {
        if( log.isDebugEnabled() ) {
            log.traceMethodCallEntry( this, "logExit" );
        }
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "name"
                },
                new Object[] {
                    theName
                });
    }

    /**
     * A name for this Breakpoint.
     */
    protected String theName;
    
    /**
     * The maximum length of time to wait at the Breakpoint, in milliseconds. 0 means forever.
     */
    protected long theDelay;
}
