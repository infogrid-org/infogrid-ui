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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.shell.http;

import java.util.Map;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.transaction.OnDemandTransaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.SmartFactory;
import org.infogrid.util.http.SaneRequest;

/**
 * Implemented by classes that can be invoked after the HttpShell is done with its processing.
 */
public abstract class AbstractHttpShellHandler
        implements
            HttpShellHandler
{
    /**
     * Obtain the name of the handler.
     *
     * @return the name1
     */
    public String getName()
    {
        return getClass().getName();
    }

    /**
     * Invoked by the HttpShell before any other processing takes place.
     *
     * @param request the incoming request
     * @param defaultMeshBase the default MeshBase to use
     * @throws HttpShellException a problem occurred, check cause for details
     */
    public void beforeTransactionStart(
            SaneRequest                                     request,
            MeshBase                                        defaultMeshBase )
        throws
            HttpShellException
    {
        // nothing
    }

    /**
     * Invoked by the HttpShell after any Transaction was created, but before any
     * processing within the transaction takes place.
     *
     * @param request the incoming request
     * @param txs the Transactions used by this invocation of the HttpShell
     *            so far at the time of invocation of this method
     * @param defaultMeshBase the default MeshBase to use
     * @throws HttpShellException a problem occurred, check cause for details
     */
    public void afterTransactionStart(
            SaneRequest                                     request,
            SmartFactory<MeshBase,OnDemandTransaction,Void> txs,
            MeshBase                                        defaultMeshBase )
        throws
            TransactionException
    {
        // nothing
    }

    /**
     * Invoked by the HttpShell before Transactions are closed.
     *
     * @param request the incoming request
     * @param vars the variables set by the HttpShell
     * @param txs the Transactions used by this invocation of the HttpShell
     * @param defaultMeshBase the default MeshBase to use
     */
    public void beforeTransactionEnd(
            SaneRequest                                     request,
            Map<String,MeshObject>                          vars,
            SmartFactory<MeshBase,OnDemandTransaction,Void> txs,
            MeshBase                                        defaultMeshBase )
        throws
            TransactionException
    {
        // nothing
    }

    /**
     * Invoked by the HttpShell after the Transactions have been closed.
     *
     * @param request the incoming request
     * @param vars the variables set by the HttpShell
     * @param txs the (now closed) Transactions used by this invocation of the HttpShell
     * @param defaultMeshBase the default MeshBase to use
     * @param maybeThrown if a Throwable was thrown, it is passed here
     * @throws HttpShellException a problem occurred, check cause for details
     */
    public void afterTransactionEnd(
            SaneRequest                                     request,
            Map<String,MeshObject>                          vars,
            SmartFactory<MeshBase,OnDemandTransaction,Void> txs,
            MeshBase                                        defaultMeshBase,
            Throwable                                       maybeThrown )
        throws
            HttpShellException
    {
        // nothing
    }

    /**
     * Helper method to get the value of a HTTP shell variable, or
     * throw an exception if not given.
     *
     * @param request the incoming request
     * @param varName name of the shell variable
     * @return value the value of the shell variable
     * @throws HttpShellException thrown if the argument is missing or empty
     */
    protected String getArgumentOrThrow(
            SaneRequest request,
            String      varName )
        throws
            HttpShellException
    {
        String argName = HttpShellKeywords.PREFIX + varName;
        String ret     = request.getPostedArgument( argName );
        if( ret == null || ret.length() == 0 ) {
            throw new HttpShellException( new EmptyArgumentValueException( argName ) );
        }
        return ret;
    }
}
