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

package org.infogrid.meshbase.transaction;

/**
 * <p>We abuse the Exception class slightly to have a convenient method of
 *    storing the place and time where a Transaction was created. This is
 *    often invaluable during debugging.</p>
 * <p>Please do not throw this Exception.</p>
 */
public class TransactionConstructionMarker
        extends
            Exception
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    TransactionConstructionMarker()
    {
        now = System.currentTimeMillis();
    }
    
    /**
     * Convert to String, for debugging purpose.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        StringBuilder ret = new StringBuilder();
        ret.append( "Transaction constructor marker created at " );
        ret.append( now );
        return ret.toString();
    }

    /**
     * The time at which the Transaction was created.
     */
    protected long now;
}
