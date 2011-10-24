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

import java.util.Map;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.transaction.OnDemandTransaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.TimeStampValue;
import org.infogrid.util.SmartFactory;
import org.infogrid.util.http.SaneRequest;

/**
 * Implemented by classes that would like to take action before, during, or after
 * regular HttpShell processing.
 */
public interface HttpShellHandler
{
    /**
     * Obtain the name of the handler.
     *
     * @return the name1
     */
    public String getName();

    /**
     * Invoked by the HttpShell before any other processing takes place.
     *
     * @param request the incoming request
     * @param defaultMeshBase the default MeshBase to use
     * @param now the time at which the HttpShell was invoked
     * @throws HttpShellException a problem occurred, check cause for details
     */
    public void beforeTransactionStart(
            SaneRequest                                     request,
            MeshBase                                        defaultMeshBase,
            TimeStampValue                                  now )
        throws
            HttpShellException;

    /**
     * Invoked by the HttpShell after any Transaction was created, but before any
     * processing within the transaction takes place.
     *
     * @param request the incoming request
     * @param txs the Transactions used by this invocation of the HttpShell
     *            so far at the time of invocation of this method
     * @param defaultMeshBase the default MeshBase to use
     * @param now the time at which the HttpShell was invoked
     * @throws HttpShellException a problem occurred, check cause for details
     * @throws TransactionException a problem with the Transaction occurred
     */
    public void afterTransactionStart(
            SaneRequest                                     request,
            SmartFactory<MeshBase,OnDemandTransaction,Void> txs,
            MeshBase                                        defaultMeshBase,
            TimeStampValue                                  now )
        throws
            HttpShellException,
            TransactionException;

    /**
     * Invoked by the HttpShell after MeshObjects were accessed, but before
     * any property setting or relationship management takes place.
     * 
     * @param request the incoming request
     * @param vars the variables set by the HttpShell
     * @param txs the Transactions used by this invocation of the HttpShell
     * @param defaultMeshBase the default MeshBase to use
     * @param now the time at which the HttpShell was invoked
     * @throws HttpShellException a problem occurred, check cause for details
     * @throws TransactionException a problem with the Transaction occurred
     */
    public void afterAccess(
            SaneRequest                                     request,
            Map<String,MeshObject>                          vars,
            SmartFactory<MeshBase,OnDemandTransaction,Void> txs,
            MeshBase                                        defaultMeshBase,
            TimeStampValue                                  now )
        throws
            HttpShellException,
            TransactionException;

    /**
     * Invoked by the HttpShell before Transactions are closed.
     *
     * @param request the incoming request
     * @param vars the variables set by the HttpShell
     * @param txs the Transactions used by this invocation of the HttpShell
     * @param defaultMeshBase the default MeshBase to use
     * @param now the time at which the HttpShell was invoked
     * @throws HttpShellException a problem occurred, check cause for details
     * @throws TransactionException a problem with the Transaction occurred
     */
    public void beforeTransactionEnd(
            SaneRequest                                     request,
            Map<String,MeshObject>                          vars,
            SmartFactory<MeshBase,OnDemandTransaction,Void> txs,
            MeshBase                                        defaultMeshBase,
            TimeStampValue                                  now )
        throws
            HttpShellException,
            TransactionException;

    /**
     * Invoked by the HttpShell after the Transactions have been closed.
     *
     * @param request the incoming request
     * @param vars the variables set by the HttpShell
     * @param txs the (now closed) Transactions used by this invocation of the HttpShell
     * @param defaultMeshBase the default MeshBase to use
     * @param now the time at which the HttpShell was invoked
     * @param maybeThrown if a Throwable was thrown, it is passed here
     * @return a URL where to redirect to, or null
     * @throws HttpShellException a problem occurred, check cause for details
     */
    public String afterTransactionEnd(
            SaneRequest                                     request,
            Map<String,MeshObject>                          vars,
            SmartFactory<MeshBase,OnDemandTransaction,Void> txs,
            MeshBase                                        defaultMeshBase,
            TimeStampValue                                  now,
            Throwable                                       maybeThrown )
        throws
            HttpShellException;
}
