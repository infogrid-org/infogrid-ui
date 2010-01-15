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
 * Thrown if an accessor method expecting a single member of a set was used,
 * but the set contained a different number of elements.
 */
public class NotSingleMemberException
    extends
        IllegalStateException
{
    private static final long serialVersionUID = 1l; // helps with serialization

    /**
     * Constructor.
     *
     * @param n the number of elements in the set
     */
    public NotSingleMemberException(
            int n )
    {
        theN = n;
    }

    /**
     * Constructor.
     *
     * @param msg the message
     * @param n the number of elements in the set
     */
    public NotSingleMemberException(
            String msg,
            int    n )
    {
        super( msg != null ? ( msg + ": " + n ) : String.valueOf( n ));

        theN = n;
    }

    /**
     * Constructor.
     *
     * @param msg the message
     * @param n the number of elements in the set
     * @param cause the underlying cause
     */
    public NotSingleMemberException(
            String    msg,
            int       n,
            Throwable cause )
    {
        super( msg != null ? ( msg + ": " + n ) : String.valueOf( n ), cause );

        theN = n;
    }

    /**
     * Obtain the actual number of elements in the set.
     *
     * @return the actual number
     */
    public int getActualNumberInSet()
    {
        return theN;
    }

    /**
     * The number of elements in the set.
     */
    protected int theN;
}
