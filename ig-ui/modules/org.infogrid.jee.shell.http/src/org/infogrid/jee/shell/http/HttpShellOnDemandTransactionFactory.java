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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.shell.http;

import java.util.List;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.transaction.OnDemandTransaction;
import org.infogrid.meshbase.transaction.OnDemandTransactionFactory;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.FactoryException;
import org.infogrid.util.SmartFactory;
import org.infogrid.util.http.SaneRequest;

/**
 * Extends OnDemandTransactionFactory for the purposes of the HttpShellHandler.
 */
public class HttpShellOnDemandTransactionFactory
        extends
            OnDemandTransactionFactory
{
    /**
     * Constructor.
     *
     * @param request the incoming request
     * @param handlers the HttpShellHandlers to invoke when a Transaction is created
     * @param mainMeshBase the main MeshBase
     */
    public HttpShellOnDemandTransactionFactory(
            SaneRequest            request,
            List<HttpShellHandler> handlers,
            MeshBase               mainMeshBase )
    {
        super();

        theRequest      = request;
        theHandlers     = handlers;
        theMainMeshBase = mainMeshBase;
    }

    /**
     * Factory method.
     *
     * @param key the key information required for object creation, if any
     * @param argument any argument-style information required for object creation, if any
     * @return the created object
     * @throws FactoryException catch-all Exception, consider its cause
     */
    @Override
    public OnDemandTransaction obtainFor(
            MeshBase key,
            Void     argument )
        throws
            FactoryException
    {
        return new OnDemandTransaction( key ) {
                @Override
                protected Transaction instantiate()
                    throws
                        TransactionException
                {
                    Transaction ret = super.instantiate();

                    try {
                        for( HttpShellHandler handler : theHandlers ) {
                            handler.afterTransactionStart( theRequest, theTransactions, theMainMeshBase );
                        }
                    } catch( HttpShellException ex ) {
                        throw new RuntimeException( ex ); // not sure that's the best
                    }
                    return ret;
                }
        };
    }

    /**
     * Set the transactions.
     * 
     * @param transactions the transactions
     */
    public void setTransactions(
            SmartFactory<MeshBase,OnDemandTransaction,Void> transactions )
    {
        theTransactions = transactions;
    }

    /**
     * The incoming request.
     */
    protected SaneRequest theRequest;

    /**
     * The main MeshBase.
     */
    protected MeshBase theMainMeshBase;

    /**
     * The on-demand Transactions.
     */
    protected SmartFactory<MeshBase,OnDemandTransaction,Void> theTransactions;
    
    /**
     * The handlers to invoke prior to creating an
     */
    protected List<HttpShellHandler> theHandlers;
}
