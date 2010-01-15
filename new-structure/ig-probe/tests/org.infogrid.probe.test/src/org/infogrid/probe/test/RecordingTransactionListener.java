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

package org.infogrid.probe.test;

import java.util.ArrayList;
import java.util.List;
import org.infogrid.meshbase.transaction.AbstractTransactionListener;
import org.infogrid.meshbase.transaction.Transaction;

/**
 * Listens to a MeshBase's Transactions and records them. For testing and debugging.
 */
public class RecordingTransactionListener
    extends
        AbstractTransactionListener
{
    /**
      * Indicates that a Transaction has been committed.
      *
      * @param tx the Transaction that was committed
      */
    @Override
    public void transactionCommitted(
            Transaction tx )
    {
        theCommittedTransactions.add( tx );
    }

    /**
     * Obtain the Transactions committed so far.
     *
     * @return the Transactions
     */
    public List<Transaction> getCommittedTransactions()
    {
        return theCommittedTransactions;
    }

    /**
     * Clear the recorded Transactions.
     */
    public void clear()
    {
        theCommittedTransactions.clear();
    }

    /**
     * The Transactions that have been committed so far.
     */
    protected ArrayList<Transaction> theCommittedTransactions = new ArrayList<Transaction>();
}
