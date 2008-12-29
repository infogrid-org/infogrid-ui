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

import java.util.concurrent.ThreadFactory;

/**
 * A ThreadFactory that names each Thread with a prefix name and a sequential suffix.
 * This can be very helpful for debugging to determine where a particular Thread
 * came from.
 */
public class NamedThreadFactory
        implements
            ThreadFactory
{
    /**
     * Constructor.
     *
     * @param prefix the prefix name
     */
    public NamedThreadFactory(
            String prefix )
    {
        thePrefix = prefix + "-";
    }

    /**
     * Constructs a new <tt>Thread</tt>.  Implementations may also initialize
     * priority, name, daemon status, <tt>ThreadGroup</tt>, etc.
     *
     * @param r a runnable to be executed by new thread instance
     * @return constructed thread
     */
    public Thread newThread(
            Runnable r )
    {
        Thread ret = new Thread( r, thePrefix + theCounter++ );
        return ret;
    }

    /**
     * The Prefix for the name of created Threads.
     */
    protected String thePrefix;

    /**
     * The current counter of created Threads.
     */
    protected int theCounter = 0;
}
