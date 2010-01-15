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

package org.infogrid.meshbase.net.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshObjectAccessException;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.transaction.NetChange;
import org.infogrid.meshbase.net.transaction.NetMeshObjectBecameDeadStateEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectDeletedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectEquivalentsAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectEquivalentsChangeEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectEquivalentsRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectPropertyChangeEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRelationshipEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeRemovedEvent;
import org.infogrid.meshbase.net.transaction.ReplicaCreatedEvent;
import org.infogrid.meshbase.net.transaction.ReplicaPurgedEvent;
import org.infogrid.meshbase.net.transaction.Utils;
import org.infogrid.meshbase.net.xpriso.ParserFriendlyXprisoMessage;
import org.infogrid.meshbase.net.xpriso.SimpleXprisoMessage;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.net.a.AccessLocallySynchronizer;
import org.infogrid.meshbase.transaction.Change;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CreateWhenNeeded;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.ReturnSynchronizerException;
import org.infogrid.util.logging.Log;

/**
 * Captures common behaviors of ProxyPolicy implementations.
 */
public abstract class AbstractProxyPolicy
        implements
            ProxyPolicy
{
    private static final Log log = Log.getLogInstance( AbstractProxyPolicy.class ); // our own, private logger

    /**
     * Constructor.
     * 
     * @param coherence the CoherenceSpecification used by this ProxyPolicy
     * @param pointsReplicasToItself if true, new Replicas will be created by a branch from the local Replica
     */
    protected AbstractProxyPolicy(
            CoherenceSpecification coherence,
            boolean                pointsReplicasToItself )
    {
        theCoherenceSpecification = coherence;
        thePointsReplicasToItself = pointsReplicasToItself;
    }
    
    /**
     * Obtain the CoherenceSpecification used by this ProxyPolicy.
     * 
     * @return the CoherenceSpecification
     */
    public CoherenceSpecification getCoherenceSpecification()
    {
        return theCoherenceSpecification;
    }

    /**
     * If this returns true, new Replicas will be created by a branch from the local
     * Replica in the replication graph. If this returns false, this new Replicas
     * create a branch from the Replicas in the third NetMeshBase from which this
     * NetMeshBase has obtained its own Replicas (if it has)
     *
     * @return true if Replicas are supposed to become Replicas of locally held Replicas
     */
    public boolean getPointsReplicasToItself()
    {
        return thePointsReplicasToItself;
    }

    /**
     * Default factory method for ProcessingInstructions objects. This may be overridden
     * in subclasses.
     * 
     * @return the created ProxyProcessingInstructions object.
     */
    protected ProxyProcessingInstructions createInstructions()
    {
        return ProxyProcessingInstructions.create();
    }

    /**
     * Determine the ProxyProcessingInstructions for ceasing communications.
     * This is defined on AbstractPolicyProxy as it is likely the same for all
     * subclasses; if not, it can be overridden.
     * 
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForCeaseCommunications(
            Proxy proxy )
    {
        ProxyProcessingInstructions ret = createInstructions();
        
        ret.setStartCommunicating(  true );
        ret.setCeaseCommunications( true );

        return ret;
    }
    
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
            Proxy                               proxy )
    {
        ProxyProcessingInstructions ret = createInstructions();

        ret.setRequestedFirstTimePaths( paths );
        ret.setExpectectedObtainReplicasWait( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedFirstTimeObjects( paths );

        ret.setStartCommunicating( true );
        ret.setSendViaWaitEndpoint( outgoing );
        ret.setWaitEndpointTimeout( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));
        
        return ret;
    }

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
            Proxy            proxy )
    {
        NetMeshObjectIdentifier [] identifiers = new NetMeshObjectIdentifier[ localReplicas.length ];
        for( int i=0 ; i<localReplicas.length ; ++i ) {
            identifiers[i] = localReplicas[i].getIdentifier();
        }

        ProxyProcessingInstructions ret = createInstructions();

        // ret.setRequestedLockObjects( identifiers );

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedLockObjects( identifiers );

        ret.setStartCommunicating( true );
        ret.setSendViaWaitEndpoint( outgoing );
        ret.setWaitEndpointTimeout( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));
        
        return ret;
    }

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
            final Proxy      proxy )
    {
        ProxyProcessingInstructions ret = createInstructions();
        
        CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing = new CreateWhenNeeded<ParserFriendlyXprisoMessage>() {
                /**
                 * Instantiation method.
                 *
                 * @return the instantiated object
                 */
                protected ParserFriendlyXprisoMessage instantiate()
                {
                    ParserFriendlyXprisoMessage ret = ParserFriendlyXprisoMessage.create(
                            proxy.getNetMeshBase().getIdentifier(),
                            proxy.getPartnerMeshBaseIdentifier() );
                    return ret;
                }
        };

        for( int i=0 ; i<localReplicas.length ; ++i ) {
            if( addPotentiallyConvey( localReplicas[i], perhapsOutgoing, proxy, isNewProxy[i] )) {
                perhapsOutgoing.obtain().addPushLockObject( localReplicas[i].getIdentifier() );
                ret.addSurrenderLock( localReplicas[i] ); // this includes the addRegisterReplicationIfNotAlready functionality
            }
        }
        if( perhapsOutgoing.hasBeenCreated() ) {
            ret.setStartCommunicating( true );
            ret.setSendViaWaitEndpoint( perhapsOutgoing.obtain() );
            ret.setWaitEndpointTimeout( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));
        }

        return ret;
    }

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
            Proxy            proxy )
    {
        NetMeshObjectIdentifier [] identifiers = new NetMeshObjectIdentifier[ localReplicas.length ];
        for( int i=0 ; i<localReplicas.length ; ++i ) {
            identifiers[i] = localReplicas[i].getIdentifier();
        }
        
        ProxyProcessingInstructions ret = createInstructions();
        
        // ret.setRequestedHomeReplicas( identifiers );

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedHomeReplicas( identifiers );

        ret.setStartCommunicating( true );
        ret.setSendViaWaitEndpoint( outgoing );
        ret.setWaitEndpointTimeout( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));
        
        return ret;
    }

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
            final Proxy      proxy )
    {
        ProxyProcessingInstructions ret = createInstructions();
        
        CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing = new CreateWhenNeeded<ParserFriendlyXprisoMessage>() {
                /**
                 * Instantiation method.
                 *
                 * @return the instantiated object
                 */
                protected ParserFriendlyXprisoMessage instantiate()
                {
                    ParserFriendlyXprisoMessage ret = ParserFriendlyXprisoMessage.create(
                            proxy.getNetMeshBase().getIdentifier(),
                            proxy.getPartnerMeshBaseIdentifier() );
                    return ret;
                }
        };
        
        for( int i=0 ; i<localReplicas.length ; ++i ) {
            if( addPotentiallyConvey( localReplicas[i], perhapsOutgoing, proxy, isNewProxy[i] )) {
                perhapsOutgoing.obtain().addPushHomeReplica( localReplicas[i].getIdentifier() );
                ret.addSurrenderHome( localReplicas[i] ); // this includes the addRegisterReplicationIfNotAlready functionality
            }
        }

        if( perhapsOutgoing.hasBeenCreated() ) {
            ret.setStartCommunicating( true );
            ret.setSendViaEndpoint( perhapsOutgoing.obtain() );
            ret.setWaitEndpointTimeout( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));
        }
        return ret;
    }

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
            Proxy            proxy )
    {
        NetMeshObjectIdentifier [] identifiers = new NetMeshObjectIdentifier[ localReplicas.length ];
        for( int i=0 ; i<localReplicas.length ; ++i ) {
            identifiers[i] = localReplicas[i].getIdentifier();
        }
        
        ProxyProcessingInstructions ret = createInstructions();
        
        // ret.setReclaimedLockObjects( identifiers );

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setReclaimedLockObjects( identifiers );

        ret.setStartCommunicating( true );
        ret.setSendViaEndpoint( outgoing );
        // we send this without waiting for receipt. So we ignore the passed duration parameter.
        
        return ret;
    }
    
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
            Long                       accessLocallySynchronizerQueryKey )
    {
        ProxyProcessingInstructions ret = createInstructions();
        
        ret.setStartCommunicating( true );
        // ret.setReclaimedLockObjects( identifiers );

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedResynchronizeReplicas( identifiers );

        if( accessLocallySynchronizerQueryKey != null ) {
            ret.setSendViaWaitEndpoint( outgoing, accessLocallySynchronizerQueryKey );
        } else {
            ret.setSendViaEndpoint( outgoing );
        }
        
        return ret;
    }
    
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
            Proxy            proxy )
    {
        NetMeshObjectIdentifier [] identifiers = new NetMeshObjectIdentifier[ localReplicas.length ];
        for( int i=0 ; i<localReplicas.length ; ++i ) {
            identifiers[i] = localReplicas[i].getIdentifier();
        }

        ProxyProcessingInstructions ret = createInstructions();
        
        // ret.setReclaimedLockObjects( identifiers );

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedCanceledObjects( identifiers );

        ret.setStartCommunicating( true );
        ret.setSendViaEndpoint( outgoing );
        
        return ret;        
    }

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
            Proxy            proxy )
    {
        NetMeshObjectIdentifier [] identifiers = new NetMeshObjectIdentifier[ localReplicas.length ];
        for( int i=0 ; i<localReplicas.length ; ++i ) {
            identifiers[i] = localReplicas[i].getIdentifier();
        }

        ProxyProcessingInstructions ret = createInstructions();

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedFreshenReplicas( identifiers );

        ret.setStartCommunicating( true );
        ret.setSendViaWaitEndpoint( outgoing );
        ret.setWaitEndpointTimeout( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));

        return ret;
    }

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
            final Proxy proxy )
    {
        ProxyProcessingInstructions ret = createInstructions();
        
        // ret.setReclaimedLockObjects( identifiers );

        CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing = new CreateWhenNeeded<ParserFriendlyXprisoMessage>() {
                /**
                 * Instantiation method.
                 *
                 * @return the instantiated object
                 */
                protected ParserFriendlyXprisoMessage instantiate()
                {
                    ParserFriendlyXprisoMessage ret = ParserFriendlyXprisoMessage.create(
                            proxy.getNetMeshBase().getIdentifier(),
                            proxy.getPartnerMeshBaseIdentifier() );
                    return ret;
                }
        };

        Change [] changes = tx.getChangeSet().getChanges();

        ArrayList<NetMeshObject> potentiallyConvey = new ArrayList<NetMeshObject>();

        for( int i=0 ; i<changes.length ; ++i ) {
            NetChange current = (NetChange) changes[i];
            
            current.setResolver( proxy.getNetMeshBase() );

            NetMeshObjectIdentifier currentIdentifier = current.getAffectedMeshObjectIdentifier();
            // NetMeshObject affectedObject = current.getAffectedMeshObject(); // affected object may be null

            if( current.shouldBeSent( proxy )) {
                
                if( current instanceof ReplicaPurgedEvent ) {
                    // affectedObject is null
                    perhapsOutgoing.obtain().addRequestedCanceledObject( currentIdentifier );

                } else if( current instanceof NetMeshObjectBecameDeadStateEvent ) {
                    // ignore

                } else if( current instanceof NetMeshObjectDeletedEvent ) {
                    NetMeshObjectDeletedEvent realCurrent = (NetMeshObjectDeletedEvent) current;
                    perhapsOutgoing.obtain().addDeleteChange( realCurrent );

                } else if( current instanceof NetMeshObjectEquivalentsAddedEvent ) {
                    NetMeshObjectEquivalentsAddedEvent realCurrent = (NetMeshObjectEquivalentsAddedEvent) current;
                    perhapsOutgoing.obtain().addEquivalentAddition( realCurrent );

                } else if( current instanceof NetMeshObjectEquivalentsRemovedEvent ) {
                    NetMeshObjectEquivalentsRemovedEvent realCurrent = (NetMeshObjectEquivalentsRemovedEvent) current;
                    perhapsOutgoing.obtain().addEquivalentRemoval( realCurrent );

                } else if( current instanceof NetMeshObjectNeighborAddedEvent ) {
                    NetMeshObjectNeighborAddedEvent realCurrent = (NetMeshObjectNeighborAddedEvent) current;
                    perhapsOutgoing.obtain().addNeighborAddition( realCurrent );
                    
                    NetMeshObject neighbor = realCurrent.getNeighborMeshObject();
                    if( neighbor != null ) {
                        potentiallyConvey.add( neighbor );
                    }

                } else if( current instanceof NetMeshObjectNeighborRemovedEvent ) {
                    NetMeshObjectNeighborRemovedEvent realCurrent = (NetMeshObjectNeighborRemovedEvent) current;
                    perhapsOutgoing.obtain().addNeighborRemoval( realCurrent );

                } else if( current instanceof NetMeshObjectPropertyChangeEvent ) {
                    NetMeshObjectPropertyChangeEvent realCurrent = (NetMeshObjectPropertyChangeEvent) current;
                    perhapsOutgoing.obtain().addPropertyChange( realCurrent );

                } else if( current instanceof NetMeshObjectRoleAddedEvent ) {
                    NetMeshObjectRoleAddedEvent realCurrent = (NetMeshObjectRoleAddedEvent) current;
                    perhapsOutgoing.obtain().addRoleAddition( realCurrent );

                } else if( current instanceof NetMeshObjectRoleRemovedEvent ) {
                    NetMeshObjectRoleRemovedEvent realCurrent = (NetMeshObjectRoleRemovedEvent) current;
                    perhapsOutgoing.obtain().addRoleRemoval( realCurrent );

                } else if( current instanceof NetMeshObjectTypeAddedEvent ) {
                    NetMeshObjectTypeAddedEvent realCurrent = (NetMeshObjectTypeAddedEvent) current;
                    perhapsOutgoing.obtain().addTypeAddition( realCurrent );

                } else if( current instanceof NetMeshObjectTypeRemovedEvent ) {
                    NetMeshObjectTypeRemovedEvent realCurrent = (NetMeshObjectTypeRemovedEvent) current;
                    perhapsOutgoing.obtain().addTypeRemoval( realCurrent );

                } else if( current instanceof ReplicaCreatedEvent ) {
                    // skip

                } else {
                    log.error( "What is this: " + current );
                }
            }
        }
        
        for( NetMeshObject current : potentiallyConvey ) {
            if( addPotentiallyConvey( current, perhapsOutgoing, proxy )) {
                ret.addRegisterReplicationIfNotAlready( current );
            }
        }
        
        if( perhapsOutgoing.hasBeenCreated() && !perhapsOutgoing.obtain().isEmpty() ) {
            ret.setStartCommunicating( true );
            ret.setSendViaEndpoint( perhapsOutgoing.obtain() );
        }
        if( !ret.isEmpty() ) {
            return ret;
        } else {
            return null;
        }
    }
     
    /**
     * Determine the necessary operations that need to be performed to process
     * this incoming message according to this ProxyPolicy.
     * 
     * @param endpoint the MessageEndpoint through which the message arrived
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
            final Proxy                             proxy )
    {
        ProxyProcessingInstructions ret = createInstructions();

        ret.setIncomingXprisoMessageEndpoint( endpoint );
        ret.setIncomingXprisoMessage( incoming );

        CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing = new CreateWhenNeeded<ParserFriendlyXprisoMessage>() {
                /**
                 * Instantiation method.
                 *
                 * @return the instantiated object
                 */
                protected ParserFriendlyXprisoMessage instantiate()
                {
                    ParserFriendlyXprisoMessage ret = ParserFriendlyXprisoMessage.create(
                            proxy.getNetMeshBase().getIdentifier(),
                            proxy.getPartnerMeshBaseIdentifier() );
                    return ret;
                }
        };

        processIncomingRequestedFirstTimeObjects(      proxy, ret, perhapsOutgoing );
        processIncomingRequestedResynchronizeReplicas( proxy, ret, perhapsOutgoing );
        processIncomingRequestedFreshenReplicas(       proxy, ret, perhapsOutgoing );
        processIncomingRequestedHomeReplicas(          proxy, ret, perhapsOutgoing );
        processIncomingRequestedLockObjects(           proxy, ret, perhapsOutgoing );
        processIncomingReclaimedLockObjects(           proxy, ret, perhapsOutgoing );
        processIncomingCanceledObjects(                proxy, ret, perhapsOutgoing );
        processIncomingConveyedObjects(                proxy, ret, perhapsOutgoing, isResponseToOngoingQuery );
        processIncomingPushedLocks(                    proxy, ret, perhapsOutgoing );
        processIncomingPushedHomes(                    proxy, ret, perhapsOutgoing );
        processIncomingPropertyChanges(                proxy, ret, perhapsOutgoing );
        processIncomingTypeChanges(                    proxy, ret, perhapsOutgoing );
        processIncomingNeighborRoleChanges(            proxy, ret, perhapsOutgoing );
        processIncomingEquivalentChanges(              proxy, ret, perhapsOutgoing );
        processIncomingDeleteChanges(                  proxy, ret, perhapsOutgoing );

        if( incoming.getRequestId() != 0 ) {
            perhapsOutgoing.obtain().setResponseId( incoming.getRequestId() ); // make this message as a response
        }

        // send message
        if( perhapsOutgoing.hasBeenCreated() && !perhapsOutgoing.obtain().isEmpty() ) {
            ret.setStartCommunicating( true );
            ret.setSendViaEndpoint( perhapsOutgoing.obtain() );
        }

        return ret;
    }

    /**
     * Process the incoming request: first-time requested objects.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingRequestedFirstTimeObjects(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase   theMeshBase = incomingProxy.getNetMeshBase();

        // requested first-time objects
        if( ArrayHelper.arrayHasContent( incoming.getRequestedFirstTimeObjects() ) ) {
            NetMeshObject[] firstTimeObjects = null;
            try {
                firstTimeObjects = theMeshBase.accessLocally( incoming.getRequestedFirstTimeObjects() );
            } catch( NetMeshObjectAccessException ex ) {
                if( ex.isPartialResultAvailable() ) {
                    firstTimeObjects = ex.getBestEffortResult();
                }
            } catch( NotPermittedException ex ) {
                // FIXME?
                log.warn( ex );
            }

            if( firstTimeObjects != null ) {
                for( int i=0 ; i<firstTimeObjects.length ; ++i ) {
                    if( firstTimeObjects[i] != null ) {
                        if( addPotentiallyConvey( firstTimeObjects[i], perhapsOutgoing, incomingProxy ) ) {
                            ret.addRegisterReplicationIfNotAlready( firstTimeObjects[i] );
                        }
                    }
                }
            }
        }
    }

    /**
     * Process the incoming request: resynchronize replicas.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingRequestedResynchronizeReplicas(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase   theMeshBase = incomingProxy.getNetMeshBase();

        // requested resynchronized objects
        if( ArrayHelper.arrayHasContent( incoming.getRequestedResynchronizeReplicas())) {
            NetMeshObject [] resync = theMeshBase.findMeshObjectsByIdentifier( incoming.getRequestedResynchronizeReplicas() );
            
            for( int i=0 ; i<resync.length ; ++i ) {
                if( resync[i] != null ) {
                    if( addPotentiallyConvey( resync[i], perhapsOutgoing, incomingProxy )) {
                        ret.addRegisterReplicationIfNotAlready( resync[i] );
                    }
                }
            }
        }
    }

    /**
     * Process the incoming request: freshen replicas.
     *
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingRequestedFreshenReplicas(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        // Do nothing. We are already the most fresh we are ever going to be
    }

    /**
     * Process the incoming request: requested home replicas.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingRequestedHomeReplicas(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase   theMeshBase = incomingProxy.getNetMeshBase();

        // requested home replicas
        if( ArrayHelper.arrayHasContent( incoming.getRequestedHomeReplicas())) {
            NetMeshObject [] homes = theMeshBase.findMeshObjectsByIdentifier( incoming.getRequestedHomeReplicas() );

            ArrayList<NetMeshObject>                toSurrender = new ArrayList<NetMeshObject>();
            HashMap<Proxy,ArrayList<NetMeshObject>> toGet       = new HashMap<Proxy,ArrayList<NetMeshObject>>();
            for( int i=0 ; i<homes.length ; ++i ) {
                if( homes[i] == null ) {
                    // can't/won't do anything
                } else if( !homes[i].getWillGiveUpHomeReplica() ) {
                    // whether we have it or not, we won't surrender
                } else if( homes[i].isHomeReplica() ) {
                    // we'll surrender this one
                    toSurrender.add( homes[i] );
                } else {
                    ArrayList<NetMeshObject> list = toGet.get( homes[i].getProxyTowardsHomeReplica() );
                    if( list == null ) {
                        list = new ArrayList<NetMeshObject>();
                        toGet.put( homes[i].getProxyTowardsHomeReplica(), list );
                    }
                    list.add( homes[i] );
                }
            }

            AccessLocallySynchronizer synchronizer = theMeshBase.getAccessLocallySynchronizer();

            try {
                synchronizer.beginTransaction();

                long waitFor = 0;
                for( Proxy p : toGet.keySet() ) {
                    ArrayList<NetMeshObject> list = toGet.get( p );
                    long delta = p.tryToObtainHomeReplicas( ArrayHelper.copyIntoNewArray( list, NetMeshObject.class ), theDefaultRpcWaitDuration );
                    waitFor = Math.max( waitFor, delta );
                }
                synchronizer.join( waitFor );

                synchronizer.endTransaction();

            } catch( ReturnSynchronizerException ex ) {
                log.error( ex );

            } catch( InterruptedException ex ) {
                log.error( ex );
            }


            for( Proxy p : toGet.keySet() ) {
                ArrayList<NetMeshObject> list = toGet.get( p );
                for( NetMeshObject current : list ) {
                    if( current.isHomeReplica() ) {
                        toSurrender.add( current );
                    }
                }
            }
            for( NetMeshObject current : toSurrender ) {
                addPotentiallyConvey( current, perhapsOutgoing, incomingProxy );
                ret.addSurrenderHome( current ); // this includes the addRegisterReplicationIfNotAlready functionality

                perhapsOutgoing.obtain().addPushHomeReplica( current.getIdentifier() );
            }
        }
    }

    /**
     * Process the incoming request: requested locks.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingRequestedLockObjects(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase   theMeshBase = incomingProxy.getNetMeshBase();

        // requested locks
        if( ArrayHelper.arrayHasContent( incoming.getRequestedLockObjects())) {
            NetMeshObject [] locks = theMeshBase.findMeshObjectsByIdentifier( incoming.getRequestedLockObjects() );
            
            ArrayList<NetMeshObject>                toSurrender = new ArrayList<NetMeshObject>();
            HashMap<Proxy,ArrayList<NetMeshObject>> toGet       = new HashMap<Proxy,ArrayList<NetMeshObject>>();
            for( int i=0 ; i<locks.length ; ++i ) {
                if( locks[i] == null ) {
                    // can't/won't do anything
                } else if( !locks[i].getWillGiveUpLock() ) {
                    // whether we have it or not, we won't surrender
                } else if( locks[i].hasLock() ) {
                    // we'll surrender this one
                    toSurrender.add( locks[i] );
                } else {
                    ArrayList<NetMeshObject> list = toGet.get( locks[i].getProxyTowardsLockReplica() );
                    if( list == null ) {
                        list = new ArrayList<NetMeshObject>();
                        toGet.put( locks[i].getProxyTowardsLockReplica(), list );
                    }
                    list.add( locks[i] );
                }
            }

            AccessLocallySynchronizer synchronizer = theMeshBase.getAccessLocallySynchronizer();

            try {
                synchronizer.beginTransaction();

                long waitFor = 0;
                for( Proxy p : toGet.keySet() ) {
                    ArrayList<NetMeshObject> list = toGet.get( p );
                    long delta = p.tryToObtainLocks( ArrayHelper.copyIntoNewArray( list, NetMeshObject.class ), theDefaultRpcWaitDuration );
                    waitFor = Math.max( waitFor, delta );
                }
                synchronizer.join( waitFor );

                synchronizer.endTransaction();

            } catch( ReturnSynchronizerException ex ) {
                log.error( ex );

            } catch( InterruptedException ex ) {
                log.error( ex );
            }
            
            for( Proxy p : toGet.keySet() ) {
                ArrayList<NetMeshObject> list = toGet.get( p );
                for( NetMeshObject current : list ) {
                    if( current.hasLock() ) {
                        toSurrender.add( current );
                    }
                }
            }
            for( NetMeshObject current : toSurrender ) {
                addPotentiallyConvey( current, perhapsOutgoing, incomingProxy );
                ret.addSurrenderLock( current ); // this includes the addRegisterReplicationIfNotAlready functionality

                perhapsOutgoing.obtain().addPushLockObject( current.getIdentifier() );
            }
        }
    }
    
    /**
     * Process the incoming request: reclaimed locks.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingReclaimedLockObjects(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase   theMeshBase = incomingProxy.getNetMeshBase();

        // reclaimed locks
        if( ArrayHelper.arrayHasContent( incoming.getReclaimedLockObjects())) {
            NetMeshObject [] lost = theMeshBase.findMeshObjectsByIdentifier( incoming.getReclaimedLockObjects() );
            
            for( int i=0 ; i<lost.length ; ++i ) {
                ret.addSurrenderLock( lost[i] );
            }
        }
    }
    
    /**
     * Process the incoming request: objects to be canceled.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingCanceledObjects(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase   theMeshBase = incomingProxy.getNetMeshBase();
        
    // canceled objects
        if( ArrayHelper.arrayHasContent( incoming.getRequestedCanceledObjects())) {
            NetMeshObject [] cancel = theMeshBase.findMeshObjectsByIdentifier( incoming.getRequestedCanceledObjects() );

            for( int i=0 ; i<cancel.length ; ++i ) {
                if( cancel[i] != null ) {
                    ret.addCancel( cancel[i] );
                }
            }
        }
    }
    
    /**
     * Process the incoming request: conveyed objects.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     * @param isResponseToOngoingQuery if true, this message was sent in response to a query
     */
    protected void processIncomingConveyedObjects(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing,
            boolean                                       isResponseToOngoingQuery )
    {
        XprisoMessage incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase   theMeshBase = incomingProxy.getNetMeshBase();
    
    // conveyed objects
        if( ArrayHelper.arrayHasContent( incoming.getConveyedMeshObjects())) {
            for( ExternalizedNetMeshObject externalized : incoming.getConveyedMeshObjects() ) {

                RippleInstructions ripple = RippleInstructions.create( externalized );
                ripple.setProxies( new Proxy[] { incomingProxy } );
                ripple.setProxyTowardsHomeIndex( 0 );
                ripple.setProxyTowardsLockIndex( 0 );

                NetMeshObject found = theMeshBase.findMeshObjectByIdentifier( externalized.getIdentifier() );

                boolean externalizedHasHomeProxy // if true, this means "externalized is the home replica"
                        = externalized.getProxyTowardsHomeNetworkIdentifier() != null;
                boolean externalizedHasDifferentHomeProxy
                        =  externalizedHasHomeProxy && !incomingProxy.getPartnerMeshBaseIdentifier().equals( externalized.getProxyTowardsHomeNetworkIdentifier() );

                // This is a bit tricky. We distinguish three main dimensions:
                // 1. conveyed MeshObject does or does not already exist locally, and if so, whether its home proxy points in a different direction:
                //        code (3 alternatives):
                //            found == null                                                 -- we've never heard of this object before
                //            found != null && found.getProxyTowardsHomeReplica() == proxy  -- we get a message from the home replica
                //            found != null && found.getProxyTowardsHomeReplica() != proxy  -- a message from somebody less authoritative
                // 2. the conveyed MeshObject carries Proxy information that points to a
                //    different home object than that sent this conveyed MeshObject
                //        code (2 alternatives): differentHomeProxy
                // 3. the incoming message is or is not a response to a request that was originated locally
                //        code (2 alternatives): isResponseToOngoingQuery
                // These three dimensions are captured in the 3-level if/then/else with 3x2x2 different choices.
                //
                // In response, the choices are:
                // A: do nothing | rippleCreate | rippleResynchronize
                // B: do nothing | cancel lease from current proxyTorwardsHome
                // C: do nothing | create instruction to issue resynchronize message with home proxy specified in conveyed MeshObject
                // D: do nothing | cancel lease of offered conveyed object
 
                boolean doRippleCreate        = false;
                boolean doRippleResynchronize = false;
                
                boolean cancelCurrentLease = false;
                boolean cancelOfferedLease = false;
                
                boolean issueResyncMessage = false;
                
                if( found == null ) {
                    if( externalizedHasDifferentHomeProxy ) {
                        if( isResponseToOngoingQuery ) {
                            // 1.1.1 -- we don't have it yet, but asked for it, and its home is somewhere else
                            
                            doRippleCreate     = true;
                            issueResyncMessage = true;

                        } else { // !isResponseToOngoingQuery
                            // 1.1.2 -- we don't have it yet and didn't ask, and its home is somewhere else

                            // do nothing, we don't care
                            cancelOfferedLease = true;
                        }

                    } else { // !differentHomeProxy
                        if( isResponseToOngoingQuery ) {
                            // 1.2.1 -- we don't have it yet, but asked for it, and we have the right home
                            
                            doRippleCreate = true;

                        } else { // !isResponseToOngoingQuery
                            // 1.2.2 -- we don't have it yet and didn't ask, and we have the right home

                            // do nothing, we don't care
                            cancelOfferedLease = true;
                        }
                    }
                    
                } else if( found.getProxyTowardsHomeReplica() == incomingProxy ) { // found != null
                    if( externalizedHasDifferentHomeProxy ) {
                        if( isResponseToOngoingQuery ) {
                            // 2.1.1 -- we have it, but asked for it, message comes from the home, but its home is somewhere else
                            // e.g. an unresolved ForwardReference arrives in response to a request

                            doRippleResynchronize = true;
                            issueResyncMessage    = true;

                        } else { // !isResponseToOngoingQuery
                            // 2.1.2 -- we have it, but didn't ask, message comes from the home, but its home is somewhere else
                            
                            // do nothing, we don't care
                            // cancelOfferedLease = true;  // externalizedHasNoRelationshipsNotFoundLocally( found, externalized )
                            cancelOfferedLease = externalizedHasNoRelationshipsNotFoundLocally( found, externalized );
                        }

                    } else { // !differentHomeProxy
                        if( isResponseToOngoingQuery ) {
                            // 2.2.1 -- we have it, but asked for it, message comes from the home, and we have the right home
                            
                            // to be safe, do a resync because we just got a fresh copy of authoritative data
                            doRippleResynchronize = true;
                            
                        } else { // !isResponseToOngoingQuery
                            // 2.2.2 -- we have it, but didn't ask, message comes from the home, and we have the right home
                            
                            // to be safe, do a resync because we just got a fresh copy of authoritative data
                            doRippleResynchronize = true;
                        }                        
                    }
                    
                } else { // found != null && found.getProxyTowardsHomeReplica() != proxy
                    if( externalizedHasDifferentHomeProxy ) {
                        if( isResponseToOngoingQuery ) {
                            // 3.1.1 -- we have it, but asked for it, message comes from somewhere else, and its home is somewhere else

                            // only accept lead if it has unique information about relationships
                            cancelOfferedLease = externalizedHasNoRelationshipsNotFoundLocally( found, externalized );
                            
                        } else { // !isResponseToOngoingQuery
                            // 3.1.2 -- we have it, didn't ask for it, message comes from somewhere else, and its home is somewhere else
                            
                            // only accept lead if it has unique information about relationships
                            cancelOfferedLease = externalizedHasNoRelationshipsNotFoundLocally( found, externalized );
                        }

                    } else { // !differentHomeProxy
                        if( isResponseToOngoingQuery ) {
                            // 3.2.1 -- we have it, but asked for it, message comes from the home, and we thought we have the right home
                            // this is the response from the home object to resync requests

                            // cancelCurrentLease    = true;
                            cancelCurrentLease    = currentProxyHasNoRelationshipsNotFoundExternally( found, externalized );
                            doRippleResynchronize = true;
//                            cancelOfferedLease = externalizedHasNoRelationshipsNotFoundLocally( found, externalized );
                            
                        } else { // !isResponseToOngoingQuery
                            // 3.2.2 -- we have it, didn't ask for it, message comes from the home, and we thought we have the right home
                            
                            // cancelCurrentLease    = true;
                            cancelCurrentLease    = currentProxyHasNoRelationshipsNotFoundExternally( found, externalized );
                            doRippleResynchronize = true;
                        }
                    }
                }
                
                
                // A:
                if( doRippleCreate ) {
                    if( doRippleResynchronize ) {
                        log.error( "programming error: create or resync, not both: " + externalized );
                    } else {
                        ret.addRippleCreate( ripple );
                    }
                } else if( doRippleResynchronize ) {
                    ret.addRippleResynchronize( ripple );
                } // else do nothing

                // B:
                if( cancelCurrentLease ) {
                    ret.addToCancelInstructions( found, found.getProxyTowardsHomeReplica());
                }
                
                // C:
                if( cancelOfferedLease ) {
                    perhapsOutgoing.obtain().addRequestedCanceledObject( externalized.getIdentifier() );
                }
                
                // D:
                if( issueResyncMessage ) {
                    ret.addToResynchronizeInstructions(
                            externalized.getIdentifier(),
                            externalized.getProxyTowardsHomeNetworkIdentifier());
                }
            }
        }
    }

    /**
     * Helper method to determine whether an ExternalizedNetMeshObject carries relationships
     * that we don't know of locally.
     *
     * @param local the local NetMeshObject
     * @param externalized the externalized NetMeshObject
     * @return true if the ExternalizedNetMeshObject has at least one neighbor not known locally
     */
    protected boolean externalizedHasNoRelationshipsNotFoundLocally(
            NetMeshObject             local,
            ExternalizedNetMeshObject externalized )
    {
        NetMeshObjectIdentifier [] localIdentifiers = local.getNeighborMeshObjectIdentifiers();

        for( NetMeshObjectIdentifier current : externalized.getNeighbors() ) {
            if( !ArrayHelper.isIn( current, localIdentifiers, true )) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to determine whether the current home Proxy is a better or worse source
     * for relationships than an ExternalizedNetMeshObject.
     *
     * @param local the local NetMeshObject
     * @param externalized the externalized NetMeshObject
     * @return true if the NetMeshObject's home Proxy has no neighbor not known to the externalized NetMeshObject
     */
    protected boolean currentProxyHasNoRelationshipsNotFoundExternally(
            NetMeshObject             local,
            ExternalizedNetMeshObject externalized )
    {
        Proxy homeProxy = local.getProxyTowardsHomeReplica();

        NetMeshObjectIdentifier [] localIdentifiers = local.getNeighborMeshObjectIdentifiersAccordingTo( homeProxy );
        NetMeshObjectIdentifier [] externalIdentifiers = externalized.getNeighbors();

        for( NetMeshObjectIdentifier current : localIdentifiers ) {
            if( !ArrayHelper.isIn( current, externalIdentifiers, true )) {
                return false;
            }
        }
        return true;
    }

    /**
     * Process the incoming request: pushed locks.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingPushedLocks(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase   theMeshBase = incomingProxy.getNetMeshBase();
        
        if( ArrayHelper.arrayHasContent( incoming.getPushLockObjects())) {
            NetMeshObject [] locks = theMeshBase.findMeshObjectsByIdentifier( incoming.getPushLockObjects() );
            
            for( int i=0 ; i<locks.length ; ++i ) {
                if( locks[i] != null ) {
                    locks[i].proxyOnlyPushLock( incomingProxy );
                }
            }
        }
    }
    
    /**
     * Process the incoming request: pushed home replicas.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingPushedHomes(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase   theMeshBase = incomingProxy.getNetMeshBase();

        if( ArrayHelper.arrayHasContent( incoming.getPushHomeReplicas() )) {
            NetMeshObject [] homes = theMeshBase.findMeshObjectsByIdentifier( incoming.getPushHomeReplicas() );
            
            for( int i=0 ; i<homes.length ; ++i ) {
                if( homes[i] != null ) {
                    homes[i].proxyOnlyPushHomeReplica( incomingProxy );
                }
            }
        }
    }
    
    /**
     * Process the incoming request: property changes.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingPropertyChanges(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming = ret.getIncomingXprisoMessage();

        if( ArrayHelper.arrayHasContent( incoming.getPropertyChanges() )) {
            NetMeshObjectPropertyChangeEvent [] events = incoming.getPropertyChanges();
            
            ret.setPropertyChanges( events );
        }
    }
    
    /**
     * Process the incoming request: type changes.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingTypeChanges(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming = ret.getIncomingXprisoMessage();

        if( ArrayHelper.arrayHasContent( incoming.getTypeAdditions())) {
            NetMeshObjectTypeAddedEvent [] events = incoming.getTypeAdditions();
            
            ret.setTypeAdditions( events );
        }
        if( ArrayHelper.arrayHasContent( incoming.getTypeRemovals())) {
            NetMeshObjectTypeRemovedEvent [] events = incoming.getTypeRemovals();
            
            ret.setTypeRemovals( events );
        }
    }
    
    /**
     * Process the incoming request: neighbor and role changes.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingNeighborRoleChanges(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming = ret.getIncomingXprisoMessage();

        if( ArrayHelper.arrayHasContent( incoming.getNeighborAdditions())) {
            NetMeshObjectNeighborAddedEvent [] events = incoming.getNeighborAdditions();

            for( NetMeshObjectNeighborAddedEvent current : events ) {
                if( acceptRelationshipEvent( incomingProxy, current )) {
                    ret.addNeighborAddition( current );
                }
            }
        }
        if( ArrayHelper.arrayHasContent( incoming.getNeighborRemovals())) {
            NetMeshObjectNeighborRemovedEvent [] events = incoming.getNeighborRemovals();
            
            for( NetMeshObjectNeighborRemovedEvent current : events ) {
                if( acceptRelationshipEvent( incomingProxy, current )) {
                    ret.addNeighborRemoval( current );
                }
            }
        }
        
        if( ArrayHelper.arrayHasContent( incoming.getRoleAdditions())) {
            NetMeshObjectRoleAddedEvent [] events = incoming.getRoleAdditions();
            
            for( NetMeshObjectRoleAddedEvent current : events ) {
                if( acceptRelationshipEvent( incomingProxy, current )) {
                    ret.addRoleAddition( current );
                }
            }
        }
        if( ArrayHelper.arrayHasContent( incoming.getRoleRemovals())) {
            NetMeshObjectRoleRemovedEvent [] events = incoming.getRoleRemovals();
            
            for( NetMeshObjectRoleRemovedEvent current : events ) {
                if( acceptRelationshipEvent( incomingProxy, current )) {
                    ret.addRoleRemoval( current );
                }
            }
        }
    }

    /**
     * Process the incoming request: equivalence changes.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingEquivalentChanges(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming = ret.getIncomingXprisoMessage();

        if( ArrayHelper.arrayHasContent( incoming.getEquivalentsAdditions())) {
            NetMeshObjectEquivalentsAddedEvent [] events = incoming.getEquivalentsAdditions();
            
            for( NetMeshObjectEquivalentsAddedEvent current : events ) {
                if( acceptRelationshipEvent( incomingProxy, current )) {
                    ret.addEquivalentsAddition( current );
                }
            }
        }
        if( ArrayHelper.arrayHasContent( incoming.getEquivalentsRemovals())) {
            NetMeshObjectEquivalentsRemovedEvent [] events = incoming.getEquivalentsRemovals();
            
            for( NetMeshObjectEquivalentsRemovedEvent current : events ) {
                if( acceptRelationshipEvent( incomingProxy, current )) {
                    ret.addEquivalentsRemoval( current );
                }
            }
        }
    }

    /**
     * Process the incoming request: delete changes.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     */
    protected void processIncomingDeleteChanges(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        XprisoMessage incoming = ret.getIncomingXprisoMessage();

        if( ArrayHelper.arrayHasContent( incoming.getDeletions())) {
            NetMeshObjectDeletedEvent [] events = incoming.getDeletions();
            
            ret.setDeletions( events );
        }
    }      
    
    /**
     * Helper method to add a NetMeshObject to an outgoing XprisoMessage to be conveyed, if needed.
     * 
     * @param obj the potentially added NetMeshObject
     * @param perhapsOutgoing the outgoing message being assembled
     * @param proxy the Proxy via which the XprisoMessage will be sent
     * @param needsToBeSent if false, do not send. 
     * @return true if the NetMeshObject was added
     */
    protected boolean addPotentiallyConvey(
            NetMeshObject                                 obj,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing,
            Proxy                                         proxy,
            boolean                                       needsToBeSent )
    {
        // make sure we don't have it already
        if( perhapsOutgoing.hasBeenCreated() ) {
            for( ExternalizedNetMeshObject current : perhapsOutgoing.obtain().getConveyedMeshObjects() ) {
                if( current.getIdentifier().equals( obj.getIdentifier() )) {
                    return false;
                }
            }
        }
        
        // make sure we need to
        if( !needsToBeSent ) {
            return false;
        }
        ExternalizedNetMeshObject toAdd = obj.asExternalized( !thePointsReplicasToItself );
        perhapsOutgoing.obtain().addConveyedMeshObject( toAdd );

        return true;
    }

    /**
     * Helper method to add a NetMeshObject to an outgoing XprisoMessage to be conveyed, if needed.
     * 
     * @param obj the potentially added NetMeshObject
     * @param perhapsOutgoing the outgoing message being assembled
     * @param proxy the Proxy via which the XprisoMessage will be sent
     * @return true if the NetMeshObject was added
     */
    protected boolean addPotentiallyConvey(
            NetMeshObject                                 obj,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing,
            Proxy                                         proxy )
    {
        return addPotentiallyConvey( obj, perhapsOutgoing, proxy, !Utils.hasReplicaInDirection( obj, proxy ));
    }

    /**
     * Helper method to calculate a timeout.
     * 
     * @param callerRequestedDuration the timeout duration specified by the caller. This may be -1, indicating default.
     * @param defaultDuration the default duration as per this ProxyPolicy.
     * @return the calculated timeout
     */
    protected static long calculateTimeoutDuration(
            long callerRequestedDuration,
            long defaultDuration )
    {
        if( callerRequestedDuration == -1L ) {
            // return default
            return defaultDuration;
        } else {
            return callerRequestedDuration;
        }
    }

    /**
     * Helper method to determine whether to accept an incoming relationship-related event.
     * Can be overridden in subclasses.
     * 
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @param e the event
     * @return true if we accept the event
     */
    protected boolean acceptRelationshipEvent(
            Proxy                          proxy,
            NetMeshObjectRelationshipEvent e )
    {
        return true;
    }

    /**
     * Helper method to determine whether to accept an incoming relationship-related event.
     * Can be overridden in subclasses.
     * 
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @param e the event
     * @return true if we accept the event
     */
    protected boolean acceptRelationshipEvent(
            Proxy                               proxy,
            NetMeshObjectEquivalentsChangeEvent e )
    {
        return true;
    }

    /**
     * The CoherenceSpecification used by this ProxyPolicy.
     */
    protected CoherenceSpecification theCoherenceSpecification;
    
    /**
     * If true, new Replicas will be created by a branch from the local Replica.
     */
    protected boolean thePointsReplicasToItself;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( AbstractProxyPolicy.class );
    
    /**
     * The default duration, in milliseconds, that we are willing for remote Proxies
     * to communicate with us.
     */
    protected static final long theDefaultRpcWaitDuration = theResourceHelper.getResourceLongOrDefault( "DefaultRpcWaitDuration", 5000L );
}
