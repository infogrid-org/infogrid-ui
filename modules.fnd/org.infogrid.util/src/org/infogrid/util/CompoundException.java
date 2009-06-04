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
 * An Exception that collects several other Throwables. This Exception can be thrown
 * if more than one problem occurred simultaneously and all of them should be passed on.
 * @author jernst
 */
public class CompoundException
    extends
        Exception
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param ts the Throwable
     */
    public CompoundException(
            Throwable [] ts )
    {
        theThrowables = ts;
    }

    /**
     * Convenience constructor.
     *
     * @param t1 first Throwable
     * @param t2 second Throwable
     */
    public CompoundException(
            Throwable t1,
            Throwable t2 )
    {
        this( new Throwable[] { t1, t2 } );
    }

    /**
     * Obtain the collected Exceptions.
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
