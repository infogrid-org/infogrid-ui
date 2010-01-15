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

package org.infogrid.util;

/**
 * A RuntimeException that collects several other Throwables. This RuntimeException can be thrown
 * if more than one problem occurred simultaneously and all of them should be passed on.
 * @author jernst
 */
public class CompoundRuntimeException
    extends
        RuntimeException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param ts the Throwables
     */
    public CompoundRuntimeException(
            Throwable [] ts )
    {
        theThrowables = ts;
    }

    /**
     * Obtain the collected Throwables.
     *
     * @return the Exceptions
     */
    public Throwable [] getThrowables()
    {
        return theThrowables;
    }

    /**
     * The collected Throwables.
     */
    protected Throwable [] theThrowables;
}
