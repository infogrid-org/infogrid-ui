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

package org.infogrid.meshbase.net.proxy;

import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.transaction.Transaction;

/**
 * <p>Factors out the communications policies of a Proxy. When faced with a request
 *    or an event, a Proxy asks its ProxyPolicy for ProxyProcessingInstructions,
 *    which it then performs.</p>
 * <p>Through this design, the communications policies of a Proxy become pluggable.</p>
 */
public interface ProxyPolicy
{
    /**
     * Obtain the CoherenceSpecification used by this ProxyPolicy.
     * 
     * @return the CoherenceSpecification
     */
    public CoherenceSpecification getCoherenceSpecification();

    /**
     * Determine the ProxyProcessingInstructions for obtaining one or more
     * replicas via this Proxy.
     * 
     * @param paths the NetMeshObjectAccessSpecification for finding the NetMeshObjects to be replicated
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForObtainReplicas(
            NetMeshObjectAccessSpecification [] paths,
            long                                duration,
            Proxy                               proxy );

    /**
     * Determine the ProxyProcessingInstructions for obtaining one or more
     * locks via this Proxy.
     * 
     * @param localReplicas the local replicas for which the lock should be obtained
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForTryToObtainLocks(
            NetMeshObject [] localReplicas,
            long             duration,
            Proxy            proxy );

    /**
     * Determine the ProxyProcessingInstructions for pushing one or more
     * locks via this Proxy.
     * 
     * @param localReplicas the local replicas for which the lock should be obtained
     * @param isNewProxy if true, the NetMeshObject did not replicate via this Proxy prior to this call.
     *         The sequence in the array is the same sequence as in localReplicas.
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForTryToPushLocks(
            NetMeshObject [] localReplicas,
            boolean []       isNewProxy,
            long             duration,
            Proxy            proxy );

    /**
     * Determine the ProxyProcessingInstructions for obtaining one or more
     * home replica statuses via this Proxy.
     * 
     * @param localReplicas the local replicas for which the home replica statuses should be obtained
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForTryToObtainHomeReplicas(
            NetMeshObject [] localReplicas,
            long             duration,
            Proxy            proxy );

    /**
     * Determine the ProxyProcessingInstructions for pushing one or more
     * home replica statuses via this Proxy.
     * 
     * @param localReplicas the local replicas for which the home replica statuses should be obtained
     * @param isNewProxy if true, the NetMeshObject did not replicate via this Proxy prior to this call.
     *         The sequence in the array is the same sequence as in localReplicas.
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForTryToPushHomeReplicas(
            NetMeshObject [] localReplicas,
            boolean []       isNewProxy,
            long             duration,
            Proxy            proxy );

    /**
     * Determine the ProxyProcessingInstructions for forcefully re-acquiring one or more
     * locks via this Proxy.
     * 
     * @param localReplicas the local replicas for which the locks are forcefully re-acquired
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForForceObtainLocks(
            NetMeshObject [] localReplicas,
            long             duration,
            Proxy            proxy );
    
    /**
     * Determine the ProxyProcessingInstructions for attempting to resynchronize one or more
     * NetMeshObjects via this Proxy.
     *
     * @param identifiers the identifiers of the local replicas which should be resynchronized
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @param accessLocallySynchronizerQueryKey if given, add all to-be-opened queries within this operation to the existing transaction
     *         with this query key. If not given, add all to-be-opened queries within this operation to this thread's transaction. This
     *         enables resynchronization to be performed on another thread while an accessLocally operation is still waiting
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForTryResynchronizeReplicas(
            NetMeshObjectIdentifier [] identifiers,
            Proxy                      proxy,
            Long                       accessLocallySynchronizerQueryKey );

    /**
     * Determine the ProxyProcessingInstructions for canceling one or more 
     * NetMeshObject leases via this Proxy.
     * 
     * @param localReplicas the local replicas for which the lease should be canceled
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForCancelReplicas(
            NetMeshObject [] localReplicas,
            long             duration,
            Proxy            proxy );

    /**
     * Determine the ProxyProcessingInstructions for freshening one or more
     * NetMeshObject leases via this Proxy.
     *
     * @param localReplicas the local replicas that should be freshened
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForFreshenReplicas(
            NetMeshObject [] localReplicas,
            long             duration,
            Proxy            proxy );

    /**
     * Determine the ProxyProcessingInstructions for ceasing communications.
     * 
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForCeaseCommunications(
            Proxy            proxy );
    
    /**
     * Given a committed Transaction, determine the ProxyProcessingInstructions for notifying
     * our partner Proxy.
     * 
     * @param tx the Transaction
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForTransactionCommitted(
            Transaction tx,
            Proxy       proxy );

    /**
     * Determine the necessary operations that need to be performed to process
     * this incoming message according to this ProxyPolicy.
     * 
     * @param endpoint the MessageEndpoint sending this event
     * @param incoming the incoming XprisoMessage
     * @param isResponseToOngoingQuery if true, this message is known to be a response to a still-ongoing
     *        query
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForIncomingMessage(
            ReceivingMessageEndpoint<XprisoMessage> endpoint,
            XprisoMessage                           incoming,
            boolean                                 isResponseToOngoingQuery,
            Proxy                                   proxy );
}
