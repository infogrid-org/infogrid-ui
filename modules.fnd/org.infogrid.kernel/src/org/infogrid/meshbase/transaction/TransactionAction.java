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

package org.infogrid.meshbase.transaction;

/**
 * An action that is performed within Transaction boundaries.
 *
 * @param <T> the return type of the action
 */
public interface TransactionAction<T>
{
    /**
     * Execute the action. This will be invoked within valid Transaction
     * boundaries.
     *
     * @param tx the Transaction within which the code is invoked
     * @return a return object, if any
     * @throws TransactionActionException a problem occurred during execution of the TransactionAction
     * @throws TransactionException should never be thrown
     */
    public abstract T execute(
            Transaction tx )
        throws
            TransactionActionException,
            TransactionException;
}
