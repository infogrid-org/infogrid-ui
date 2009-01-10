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

package org.infogrid.meshbase.net.proxy;

import java.util.List;
import org.infogrid.comm.BidirectionalMessageEndpoint;
import org.infogrid.comm.MessageEndpoint;
import org.infogrid.comm.MessageEndpointIsDeadException;
import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.comm.SendingMessageEndpoint;
import org.infogrid.comm.WaitForResponseEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpointListener;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.security.NetAccessManager;
import org.infogrid.meshbase.net.transaction.NetMeshObjectDeletedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectEquivalentsAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectEquivalentsRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectPropertyChangeEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeRemovedEvent;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.security.AccessManager;
import org.infogrid.meshbase.security.IdentityChangeException;
import org.infogrid.meshbase.transaction.CannotApplyChangeException;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.FactoryException;
import org.infogrid.util.IsDeadException;
import org.infogrid.util.RemoteQueryTimeoutException;
import org.infogrid.util.SmartFactory;
import org.infogrid.util.logging.Log;

/**
 * <p>Factors out functionality common to many Proxy implementations.</>
 *
 * <p>Note that this class does not listen to incoming messages itself; only the created
 * WaitForResponseEndpoint does. It is also not a TransactionListener any more: the
 * NetMeshBase behaves as if it is, though. This makes sure that the Proxy gets the updates
 * even if temporarily not in memory and swapped out.
 */
public abstract class AbstractCommunicatingProxy
        extends
            AbstractProxy
        implements
            PingPongMessageEndpointListener<XprisoMessage>
{
    private static final Log log = Log.getLogInstance( AbstractCommunicatingProxy.class ); // our own, private logger
    
    /**
     * Constructor.
     *
     * @param ep the ProxyMessageEndpoint to use by this Proxy
     * @param mb the NetMeshBase that this Proxy belongs to
     * @param policy the ProxyPolicy to use
     * @param partnerIdentifier identifier of the partner NetMeshBase with which this Proxy communicates
     */
    protected AbstractCommunicatingProxy(
            ProxyMessageEndpoint  ep,
            NetMeshBase           mb,
            ProxyPolicy           policy,
            NetMeshBaseIdentifier partnerIdentifier )
    {
        super( mb, policy, partnerIdentifier );

        theEndpoint = ep;

        if( theEndpoint != null ) {
            theEndpoint.addDirectMessageEndpointListener( this );
                // this must be contained direct, otherwise the proxy may be swapped out and the endpoint "swallows"
                // the incoming message that will never make it to the Proxy
        }

        // Don't use the factory. We dispatch the incoming messages ourselves, so we can make
        // sure we process them in order.
        theWaitForLockResponseEndpoint    = new MyWaitForLockResponseEndpoint( theEndpoint );
        theWaitForHomeResponseEndpoint    = new MyWaitForHomeReplicaResponseEndpoint( theEndpoint );
        theWaitForReplicaResponseEndpoint = new MyWaitForReplicaResponseEndpoint( theEndpoint );
    }

    /**
     * Internal helper triggered when the Proxy is updated in some fashion. This can be
     * used to write Proxy data to disk, for example.
     */
    protected void proxyUpdated()
    {
        if( theFactory != null ) {
            theFactory.factoryCreatedObjectUpdated( this );
        } else {
            log.error( this + ".proxyUpdated() does not have theFactory set" );
        }
    }

    /**
     * Obtain the BidirectionalMessageEndpoint associated with this Proxy.
     *
     * @return the BidirectionalMessageEndpoint
     */
    public final ProxyMessageEndpoint getMessageEndpoint()
    {
        return theEndpoint;
    }

    /**
     * Ask this Proxy to obtain from its partner NetMeshBase replicas with the enclosed
     * specification. Do not acquire the lock; that would be a separate operation. 
     * 
     * @param paths the NetMeshObjectAccessSpecifications specifying which replicas should be obtained
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @return the duration, in milliseconds, that the Proxy believes this operation will take
     */
    public final long obtainReplicas(
            NetMeshObjectAccessSpecification [] paths,
            long                                duration )
    {
        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForObtainReplicas( paths, duration, this );
        performInstructions( instructions );

        return instructions.getExpectedObtainReplicasWait();
    }
    
    /**
     * Ask this Proxy to obtain the lock for one or more replicas from the
     * partner NetMeshBase. Unlike many of the other calls, this call is
     * synchronous over the network and either succeeds, fails, or times out.
     *
     * @param localReplicas the local replicas for which the lock should be obtained
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @throws RemoteQueryTimeoutException thrown if this call times out
     */
    public final void tryToObtainLocks(
            NetMeshObject [] localReplicas,
            long             duration )
        throws
            RemoteQueryTimeoutException
    {
        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForTryToObtainLocks( localReplicas, duration, this );
        performInstructions( instructions );
    }
    
    /**
     * Ask this Proxy to push the locks for one or more replicas to the partner
     * NetMeshBase. Unlike many of the other calls, this call is
     * synchronous over the network and either succeeds, fails, or times out.
     * 
     * @param localReplicas the local replicas for which the lock should be pushed
     * @param isNewProxy if true, the the NetMeshObject did not replicate via this Proxy prior to this call.
     *         The sequence in the array is the same sequence as in localReplicas.
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @throws RemoteQueryTimeoutException thrown if this call times out
     */
    public void tryToPushLocks(
            NetMeshObject [] localReplicas,
            boolean []       isNewProxy,
            long             duration )
        throws
            RemoteQueryTimeoutException
    {
        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForTryToPushLocks( localReplicas, isNewProxy, duration, this );
        performInstructions( instructions );
    }

    /**
     * Ask this Proxy to obtain the home replica status for one or more replicas from the
     * partner NetMeshBase. Unlike many of the other calls, this call is
     * synchronous over the network and either succeeds, fails, or times out.
     *
     * @param localReplicas the local replicas for which the home replica status should be obtained
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @throws RemoteQueryTimeoutException thrown if this call times out
     */
    public void tryToObtainHomeReplicas(
            NetMeshObject [] localReplicas,
            long             duration )
        throws
            RemoteQueryTimeoutException
    {
        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForTryToObtainHomeReplicas( localReplicas, duration, this );
        performInstructions( instructions );
    }

    /**
     * Ask this Proxy to push the home replica status for one or more replicas to the partner
     * NetMeshBase. Unlike many of the other calls, this call is
     * synchronous over the network and either succeeds, fails, or times out.
     * 
     * @param localReplicas the local replicas for which the home replica status should be pushed
     * @param isNewProxy if true, the the NetMeshObject did not replicate via this Proxy prior to this call.
     *         The sequence in the array is the same sequence as in localReplicas.
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @throws RemoteQueryTimeoutException thrown if this call times out
     */
    public void tryToPushHomeReplicas(
            NetMeshObject [] localReplicas,
            boolean []       isNewProxy,
            long             duration )
        throws
            RemoteQueryTimeoutException
    {
        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForTryToPushHomeReplicas( localReplicas, isNewProxy, duration, this );
        performInstructions( instructions );
    }

    /**
     * Send notification to the partner NetMeshBase that this MeshBase has forcibly taken the
     * lock back for the given NetMeshObjects.
     *
     * @param localReplicas the local replicas for which the lock has been forced back
     */
    public void forceObtainLocks(
            NetMeshObject [] localReplicas )
    {
        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForForceObtainLocks( localReplicas, this );
        performInstructions( instructions );
    }

    /**
     * Tell the partner NetMeshBase that one or more local replicas would like to be
     * resynchronized. This call uses NetMeshObjectIdentifier instead of NetMeshObject
     * as sometimes the NetMeshObjects have not been instantiated when this call is
     * most naturally made.
     *
     * @param identifiers the identifiers of the NetMeshObjects
     */
    public void tryResynchronizeReplicas(
            NetMeshObjectIdentifier [] identifiers )
    {
        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForTryResynchronizeReplicas( identifiers, this );
        performInstructions( instructions );
    }

    /**
     * Ask this Proxy to cancel the leases for the given replicas from its partner NetMeshBase.
     * 
     * @param localReplicas the local replicas for which the lease should be canceled
     */
    public void cancelReplicas(
            NetMeshObject [] localReplicas )
    {
        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForCancelReplicas( localReplicas, this );
        performInstructions( instructions );
    }

    /**
     * Invoked by the NetMeshBase that this Proxy belongs to,
     * it causes this Proxy to initiate the "ceasing communication" sequence with
     * the partner NetMeshBase, and then kill itself.
     */
    @SuppressWarnings(value={"unchecked"})
    public void initiateCeaseCommunications()
    {
        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForCeaseCommunications( this );
        performInstructions( instructions );
    }

    /**
     * Tell this Proxy that it is not needed any more. This will invoke
     * {@link #initiateCeaseCommunications} if and only if
     * isPermanent is true.
     * 
     * @param isPermanent if true, this Proxy will go away permanently; if false,
     *        it may come alive again some time later, e.g. after a reboot
     */
    public void die(
            boolean isPermanent )
    {
        if( isPermanent ) {
            initiateCeaseCommunications();
        }
        
        if( theEndpoint != null ) {
            theEndpoint.gracefulDie();
        }
    }

    /**
     * Indicates that a Transaction has been committed. This is invoked by the NetMeshBase
     * without needing a subscription.
     *
     * @param theTransaction the Transaction that was committed
     */
    public void transactionCommitted(
            Transaction theTransaction )
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".transactionCommitted( " + theTransaction + " )" );
        }

        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForTransactionCommitted( theTransaction, this );
        performInstructions( instructions );
    }

    /**
     * Called when an incoming message has arrived.
     *
     * @param endpoint the MessageEndpoint sending this event
     * @param incoming the incoming message
     */
    public final void messageReceived(
            ReceivingMessageEndpoint<XprisoMessage> endpoint,
            XprisoMessage                           incoming )
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".messageReceived( " + incoming + " )" );
        }

        try {
            theMeshBase.registerIncomingProxy( this );

        } catch( IsDeadException ex ) {
            if( log.isDebugEnabled() ) {
                log.debug( ex );
            }
            return;
        }

        try {
            internalMessageReceived( endpoint, incoming );

        } catch( RuntimeException ex ) {
            log.error( ex );

        } finally {
            theMeshBase.unregisterIncomingProxy();
        }
    }
    
    /**
     * Internal implementation method for messageReceived. This makes catching exceptions
     * easier.
     *
     * @param endpoint the MessageEndpoint sending this event
     * @param incoming the incoming message
     */
    protected void internalMessageReceived(
            ReceivingMessageEndpoint<XprisoMessage> endpoint,
            XprisoMessage                           incoming )
    {
        long    responseId    = incoming.getResponseId();
        boolean callIsWaiting = theWaitForHomeResponseEndpoint.isCallWaitingFor( responseId );

        if( !callIsWaiting ) {
            callIsWaiting = theWaitForLockResponseEndpoint.isCallWaitingFor( responseId );
        }
        if( !callIsWaiting ) {
            callIsWaiting = theWaitForReplicaResponseEndpoint.isCallWaitingFor( responseId );
        }

        ProxyProcessingInstructions instructions = theProxyPolicy.calculateForIncomingMessage( endpoint, incoming, callIsWaiting, this );
        performInstructions( instructions );
    }

    /**
     * Perform the instructions obtained from our ProxyPolicy. The provided instructions
     * may be null, in which case nothing is done.
     * 
     * @param instructions the ProxyProcessingInstructions
     */
    protected void performInstructions(
            ProxyProcessingInstructions instructions )
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".performInstructions( " + instructions + " )" );
        }
        long now = System.currentTimeMillis();

        if( instructions == null ) {
            return; // nothing to do
        }
        // don't do "if instructions.isEmpty() return" here because we still need to acknowledge return receipt
        
        instructions.check();

        XprisoMessage incoming = instructions.getIncomingXprisoMessage();
        
        if( incoming != null ) {
            theWaitForLockResponseEndpoint.messageReceived( instructions.getIncomingXprisoMessageEndpoint(), incoming );
            theWaitForHomeResponseEndpoint.messageReceived( instructions.getIncomingXprisoMessageEndpoint(), incoming );
        }

        
        NetMeshBaseLifecycleManager life   = theMeshBase.getMeshBaseLifecycleManager();
        NetAccessManager            access = theMeshBase.getAccessManager();
        
        for( NetMeshObject current : instructions.getRegisterReplicationsIfNotAlready()) {
            try {
                current.proxyOnlyRegisterReplicationTowards( this  );
            } catch( IllegalArgumentException ex ) {
                // we have it already
                if( log.isDebugEnabled() ) {
                    log.debug( ex );
                }
            } finally {
                meshObjectModifiedDuringMessageProcessing( current );
            }
        }

        Transaction tx = null;
        try {
            for( RippleInstructions current : instructions.getRippleCreates() ) {
                tx = ensureRights( access, tx );
                NetMeshObject obj = null;
                try {
                    obj = life.rippleCreate(
                            current.getExternalizedNetMeshObject(),
                            current.getProxies(),
                            current.getProxyTowardsHomeIndex(),
                            current.getProxyTowardsLockIndex() );
                } catch( MeshObjectIdentifierNotUniqueException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
            for( RippleInstructions current : instructions.getRippleResynchronizes() ) {
                tx = ensureRights( access, tx );
                NetMeshObject obj = null;
                try {
                    obj = life.rippleResynchronize(
                            current.getExternalizedNetMeshObject(),
                            current.getProxies(),
                            current.getProxyTowardsHomeIndex(),
                            current.getProxyTowardsLockIndex());
                } catch( NotPermittedException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }                    
            }
            for( NetMeshObjectTypeAddedEvent event : instructions.getTypeAdditions() ) {
                NetMeshObject obj = null;
                try {
                    obj = event.potentiallyApplyToReplicaIn( theMeshBase, this );
                } catch( CannotApplyChangeException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
            for( NetMeshObjectEquivalentsAddedEvent event : instructions.getEquivalentsAdditions() ) {
                NetMeshObject obj = null;
                try {
                    obj = event.potentiallyApplyToReplicaIn( theMeshBase, this );
                } catch( CannotApplyChangeException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
            for( NetMeshObjectNeighborAddedEvent event : instructions.getNeighborAdditions() ) {
                NetMeshObject obj = null;
                try {
                    obj = event.potentiallyApplyToReplicaIn( theMeshBase, this );
                } catch( CannotApplyChangeException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
            for( NetMeshObjectRoleAddedEvent event : instructions.getRoleAdditions() ) {
                NetMeshObject obj = null;
                try {
                    obj = event.potentiallyApplyToReplicaIn( theMeshBase, this );
                } catch( CannotApplyChangeException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
            for( NetMeshObjectPropertyChangeEvent event : instructions.getPropertyChanges() ) {
                NetMeshObject obj = null;
                try {
                    obj = event.potentiallyApplyToReplicaIn( theMeshBase, this );
                } catch( CannotApplyChangeException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
            for( NetMeshObjectRoleRemovedEvent event : instructions.getRoleRemovals() ) {
                NetMeshObject obj = null;
                try {
                    obj = event.potentiallyApplyToReplicaIn( theMeshBase, this );
                } catch( CannotApplyChangeException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
            for( NetMeshObjectNeighborRemovedEvent event : instructions.getNeighborRemovals() ) {
                NetMeshObject obj = null;
                try {
                    obj = event.potentiallyApplyToReplicaIn( theMeshBase, this );
                } catch( CannotApplyChangeException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
            for( NetMeshObjectEquivalentsRemovedEvent event : instructions.getEquivalentsRemovals() ) {
                NetMeshObject obj = null;
                try {
                    obj = event.potentiallyApplyToReplicaIn( theMeshBase, this );
                } catch( CannotApplyChangeException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
            for( NetMeshObjectTypeRemovedEvent event : instructions.getTypeRemovals() ) {
                NetMeshObject obj = null;
                try {
                    obj = event.potentiallyApplyToReplicaIn( theMeshBase, this );
                } catch( CannotApplyChangeException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
            for( NetMeshObjectDeletedEvent event : instructions.getDeletions() ) {
                NetMeshObject obj = null;
                try {
                    obj = event.potentiallyApplyToReplicaIn( theMeshBase, this );
                } catch( CannotApplyChangeException ex ) {
                    log.error( ex );
                } finally {
                    if( obj != null ) {
                        meshObjectModifiedDuringMessageProcessing( obj );
                    }
                }
            }
        } catch( TransactionException ex ) {
            log.error( ex );
        } finally {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }

        for( NetMeshObject current : instructions.getSurrenderLocks() ) {
            current.proxyOnlySurrenderLock( this );
            meshObjectModifiedDuringMessageProcessing( current );
        }
        for( NetMeshObject current : instructions.getSurrenderHomes() ) {
            current.proxyOnlySurrenderHomeReplica( this );
            meshObjectModifiedDuringMessageProcessing( current );
        }
        for( NetMeshObject current : instructions.getCancels() ) {
            current.proxyOnlyUnregisterReplicationTowards( this );
            meshObjectModifiedDuringMessageProcessing( current );
        }
        
        for( ResynchronizeInstructions current : instructions.getResynchronizeInstructions() ) {
            NetMeshObjectIdentifier [] toResync    = current.getNetMeshObjectIdentifiers();
            
            try {
                Proxy resyncProxy = theMeshBase.obtainProxyFor( current.getProxyIdentifier(), null );
                resyncProxy.tryResynchronizeReplicas( toResync );

            } catch( FactoryException ex ) {
                theProxyListeners.fireEvent( new InitiateResynchronizeFailedEvent(
                        this,
                        current.getProxyIdentifier(),
                        current.getNetMeshObjectIdentifiers(),
                        ex.getCause() ));
            }
        }
        for( CancelInstructions current : instructions.getCancelInstructions() ) {
            NetMeshObject [] toCancel = current.getNetMeshObjects();
            current.getProxy().cancelReplicas( toCancel );
        }
        
    // send messages
        
        if( instructions.getStartCommunicating() ) {
            theEndpoint.startCommunicating(); // this is no-op on subsequent calls
        }

        XprisoMessage outgoing = instructions.getSendViaWaitForReplicaResponseEndpoint();
        if( outgoing != null ) {
            XprisoMessage incoming2; // this is only here to make debugging easier
            try {
                incoming2 = theWaitForReplicaResponseEndpoint.call( outgoing, instructions.getWaitForReplicaResponseEndpointTimeout() );

            } catch( Throwable t ) {
                theProxyListeners.fireEvent( new SendViaWaitForReplicaResponseEndpointFailedEvent( this, outgoing, t ));
            }
        }

        outgoing = instructions.getSendViaWaitForHomeResponseEndpoint();
        if( outgoing != null ) {
            XprisoMessage incoming2; // this is only here to make debugging easier
            try {
                incoming2 = theWaitForHomeResponseEndpoint.call( outgoing, instructions.getWaitForHomeResponseEndpointTimeout() );

            } catch( Throwable t ) {
                theProxyListeners.fireEvent( new SendViaWaitForHomeResponseEndpointFailedEvent( this, outgoing, t ));
            }
        }
        
        outgoing = instructions.getSendViaWaitForLockResponseEndpoint();
        if( outgoing != null ) {
            XprisoMessage incoming2; // this is only here to make debugging easier
            try {
                incoming2 = theWaitForLockResponseEndpoint.call( outgoing, instructions.getWaitForLockResponseEndpointTimeout() );

            } catch( Throwable t ) {
                theProxyListeners.fireEvent( new SendViaWaitForLockResponseEndpointFailedEvent( this, outgoing, t ));
            }
        }
        
        outgoing = instructions.getSendViaEndpoint();
        if( outgoing != null ) {
            theEndpoint.enqueueMessageForSend( outgoing );
        }
        if( incoming != null ) {
            theWaitForReplicaResponseEndpoint.messageReceived( instructions.getIncomingXprisoMessageEndpoint(), incoming );
        }
        
        theTimeRead = now;

        if( instructions.getCeaseCommunications() ) {
            // it's all over
            ( ( SmartFactory<NetMeshBaseIdentifier, Proxy, CoherenceSpecification> ) theFactory ).remove( getPartnerMeshBaseIdentifier() );
        }
    }
        
    /**
     * Hook that enables subclasses to take note which MeshObjects in the MeshBase have
     * been modified in response to message processing.
     * 
     * @param modified the NetMeshObject that was modified
     */
    protected void meshObjectModifiedDuringMessageProcessing(
            NetMeshObject modified )
    {
        // do nothing on this level
    }

    /**
     * Called when an outgoing message has been sent.
     *
     * @param endpoint the MessageEndpoint sending this event
     * @param msg the sent message
     */
    public void messageSent(
            SendingMessageEndpoint<XprisoMessage> endpoint,
            XprisoMessage                         msg )
    {
        proxyUpdated();
    }

    /**
     * Called when an outgoing message has enqueued for sending.
     *
     * @param endpoint the MessageEndpoint sending this event
     * @param msg the enqueued message
     */
    public void messageEnqueued(
            SendingMessageEndpoint<XprisoMessage> endpoint,
            XprisoMessage                         msg )
    {
        proxyUpdated();
    }

    /**
     * Called when an outoing message failed to be sent.
     *
     * @param endpoint the MessageEndpoint sending this event
     * @param msg the outgoing message
     */
    public void messageSendingFailed(
            SendingMessageEndpoint<XprisoMessage> endpoint,
            XprisoMessage                         msg )
    {
        proxyUpdated();
    }

    /**
     * Called when the token has been received.
     *
     * @param endpoint the MessageEndpoint sending this event
     * @param token the received token
     */
    public final void tokenReceived(
            PingPongMessageEndpoint<XprisoMessage> endpoint,
            long                                   token )
    {
        proxyUpdated();
    }
    
    /**
     * Called when the token has been sent.
     *
     * @param endpoint the MessageEndpoint sending this event
     * @param token the sent token
     */
    public final void tokenSent(
            PingPongMessageEndpoint<XprisoMessage> endpoint,
            long                                   token )
    {
        proxyUpdated();
    }

    /**
     * Called when the receiving endpoint threw the EndpointIsDeadException.
     *
     * @param endpoint the MessageEndpoint sending this event
     * @param msg the status of the outgoing queue
     * @param t the error
     */
    public void disablingError(
            MessageEndpoint<XprisoMessage> endpoint,
            List<XprisoMessage>            msg,
            Throwable                      t )
    {
        // just logging right now (FIXME?)

        if( t instanceof MessageEndpointIsDeadException ) {
            if( log.isDebugEnabled() ) {
                log.debug( this + ".disablingError( " + endpoint + ", " + msg + " )", t );
            }
        } else {
            log.error( this + ".disablingError( " + endpoint + ", " + msg + " )", t );
        }
        theWaitForLockResponseEndpoint.disablingError( endpoint, msg, t );
        theWaitForHomeResponseEndpoint.disablingError( endpoint, msg, t );
        theWaitForReplicaResponseEndpoint.disablingError( endpoint, msg, t );
        
        proxyUpdated();
    }
    
    /**
     * Helper method to make sure the current Thread has been initialized right.
     * 
     * @param access the AccessManager to use
     * @param tx the current Transaction, if any
     * @return the current Transaction, if any
     * @throws TransactionException thrown if a Transaction should have been created, but could not
     */
    protected Transaction ensureRights(
            AccessManager access,
            Transaction tx )
        throws
            TransactionException
    {
        if( tx == null ) {
            tx = theMeshBase.createTransactionAsapIfNeeded();

            if( access != null && !access.isSu() ) {
                try {
                    access.sudo();
                } catch( IdentityChangeException ex ) {
                    log.error( ex );
                }
            }
        }
        return tx;
    }

    /**
     * The BidirectionalMessageEndpoint to use to talk to our partner NetMeshBase's Proxy.
     */
    protected ProxyMessageEndpoint theEndpoint;

    /**
     * The WaitForResponseEndpoint that makes waiting for responses to lock requests much easier.
     */
    protected WaitForResponseEndpoint<XprisoMessage> theWaitForLockResponseEndpoint;

    /**
     * The WaitForResponseEndpoint that makes waiting for responses to homeReplica requests much easier.
     */
    protected WaitForResponseEndpoint<XprisoMessage> theWaitForHomeResponseEndpoint;

    /**
     * The WaitForResponseEndpoint that makes waiting for responses to replica requests much easier.
     */
    protected WaitForResponseEndpoint<XprisoMessage> theWaitForReplicaResponseEndpoint;

    /**
     * Subclass WaitForResponseEndpoint for easier debugging, and to avoid automatic event subscription.
     */
    static class MyWaitForLockResponseEndpoint
            extends
                WaitForResponseEndpoint<XprisoMessage>
    {
        /**
         *  Constructor.
         * 
         * @param ep the BidirectionalMessageEndpoint to use
         */
        public MyWaitForLockResponseEndpoint(
                BidirectionalMessageEndpoint<XprisoMessage> ep )
        {
            super( ep );
        }
    } 

    /**
     * Subclass WaitForResponseEndpoint for easier debugging, and to avoid automatic event subscription.
     */
    static class MyWaitForHomeReplicaResponseEndpoint
            extends
                WaitForResponseEndpoint<XprisoMessage>
    {
        /**
         *  Constructor.
         * 
         * @param ep the BidirectionalMessageEndpoint to use
         */
        public MyWaitForHomeReplicaResponseEndpoint(
                BidirectionalMessageEndpoint<XprisoMessage> ep )
        {
            super( ep );
        }
    } 

    /**
     * Subclass WaitForResponseEndpoint for easier debugging, and to avoid automatic event subscription.
     */
    static class MyWaitForReplicaResponseEndpoint
            extends
                WaitForResponseEndpoint<XprisoMessage>
    {
        /**
         *  Constructor.
         * 
         * @param ep the BidirectionalMessageEndpoint to use
         */
        public MyWaitForReplicaResponseEndpoint(
                BidirectionalMessageEndpoint<XprisoMessage> ep )
        {
            super( ep );
        }
    }
}

