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
import org.infogrid.meshbase.transaction.Change;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.RemoteQueryTimeoutException;
import org.infogrid.util.ResourceHelper;
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

        ret.setStartCommunicating( true );
        ret.setRequestedFirstTimePaths( paths );
        ret.setExpectectedObtainReplicasWait( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedFirstTimeObjects( paths );

        ret.setSendViaWaitForReplicaResponseEndpoint( outgoing );
        
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

        ret.setStartCommunicating( true );
        // ret.setRequestedLockObjects( identifiers );

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedLockObjects( identifiers );

        ret.setSendViaWaitForLockResponseEndpoint( outgoing );
        ret.setWaitForLockResponseEndpointTimeout( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));
        
        return ret;
    }

    /**
     * Determine the ProxyProcessingInstructions for pushing one or more
     * locks via this Proxy.
     * 
     * @param localReplicas the local replicas for which the lock should be obtained
     * @param isNewProxy if true, the the NetMeshObject did not replicate via this Proxy prior to this call.
     *         The sequence in the array is the same sequence as in localReplicas.
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForTryToPushLocks(
            NetMeshObject [] localReplicas,
            boolean []       isNewProxy,
            long             duration,
            Proxy            proxy )
    {
        ProxyProcessingInstructions ret = createInstructions();
        
        ret.setStartCommunicating( true );

        ParserFriendlyXprisoMessage outgoing = ParserFriendlyXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );
        
        for( int i=0 ; i<localReplicas.length ; ++i ) {
            if( addPotentiallyConvey( localReplicas[i], outgoing, proxy, isNewProxy[i] )) {
                outgoing.addPushLockObject( localReplicas[i].getIdentifier() );
                ret.addSurrenderLock( localReplicas[i] ); // this includes the addRegisterReplicationIfNotAlready functionality
            }
        }

        ret.setSendViaWaitForLockResponseEndpoint( outgoing );
        ret.setWaitForLockResponseEndpointTimeout( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));
        
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
        
        ret.setStartCommunicating( true );
        // ret.setRequestedHomeReplicas( identifiers );

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedHomeReplicas( identifiers );

        ret.setSendViaWaitForReplicaResponseEndpoint( outgoing );
        ret.setWaitForReplicaResponseEndpointTimeout( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));
        
        return ret;
    }

    /**
     * Determine the ProxyProcessingInstructions for pushing one or more
     * home replica statuses via this Proxy.
     * 
     * @param localReplicas the local replicas for which the home replica statuses should be obtained
     * @param isNewProxy if true, the the NetMeshObject did not replicate via this Proxy prior to this call.
     *         The sequence in the array is the same sequence as in localReplicas.
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForTryToPushHomeReplicas(
            NetMeshObject [] localReplicas,
            boolean []       isNewProxy,
            long             duration,
            Proxy            proxy )
    {
        ProxyProcessingInstructions ret = createInstructions();
        
        ret.setStartCommunicating( true );

        ParserFriendlyXprisoMessage outgoing = ParserFriendlyXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );
        
        for( int i=0 ; i<localReplicas.length ; ++i ) {
            if( addPotentiallyConvey( localReplicas[i], outgoing, proxy, isNewProxy[i] )) {
                outgoing.addPushHomeReplica( localReplicas[i].getIdentifier() );
                ret.addSurrenderHome( localReplicas[i] ); // this includes the addRegisterReplicationIfNotAlready functionality
            }
        }

        ret.setSendViaWaitForHomeResponseEndpoint( outgoing );
        ret.setWaitForHomeResponseEndpointTimeout( calculateTimeoutDuration( duration, theDefaultRpcWaitDuration ));
        
        return ret;
    }

    /**
     * Determine the ProxyProcessingInstructions for forcefully re-acquiring one or more
     * locks via this Proxy.
     * 
     * @param localReplicas the local replicas for which the locks are forcefully re-acquired
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForForceObtainLocks(
            NetMeshObject [] localReplicas,
            Proxy            proxy )
    {
        NetMeshObjectIdentifier [] identifiers = new NetMeshObjectIdentifier[ localReplicas.length ];
        for( int i=0 ; i<localReplicas.length ; ++i ) {
            identifiers[i] = localReplicas[i].getIdentifier();
        }
        
        ProxyProcessingInstructions ret = createInstructions();
        
        ret.setStartCommunicating( true );
        // ret.setReclaimedLockObjects( identifiers );

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setReclaimedLockObjects( identifiers );

        ret.setSendViaEndpoint( outgoing );
        
        return ret;
    }
    
    /**
     * Determine the ProxyProcessingInstructions for attempting to resynchronize one or more
     * NetMeshObjects via this Proxy.
     * 
     * @param identifiers the identifiers of the local replicas which should be resynchronized
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForTryResynchronizeReplicas(
            NetMeshObjectIdentifier [] identifiers,
            Proxy                      proxy )
    {
        ProxyProcessingInstructions ret = createInstructions();
        
        ret.setStartCommunicating( true );
        // ret.setReclaimedLockObjects( identifiers );

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedResynchronizeReplicas( identifiers );

        ret.setSendViaEndpoint( outgoing );
        
        return ret;
    }
    
    /**
     * Determine the ProxyProcessingInstructions for canceling one or more 
     * NetMeshObject leases via this Proxy.
     * 
     * @param localReplicas the local replicas for which the the lease should be canceled
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    public ProxyProcessingInstructions calculateForCancelReplicas(
            NetMeshObject [] localReplicas,
            Proxy            proxy )
    {
        NetMeshObjectIdentifier [] identifiers = new NetMeshObjectIdentifier[ localReplicas.length ];
        for( int i=0 ; i<localReplicas.length ; ++i ) {
            identifiers[i] = localReplicas[i].getIdentifier();
        }

        ProxyProcessingInstructions ret = createInstructions();
        
        ret.setStartCommunicating( true );
        // ret.setReclaimedLockObjects( identifiers );

        SimpleXprisoMessage outgoing = SimpleXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        outgoing.setRequestedCanceledObjects( identifiers );

        ret.setSendViaEndpoint( outgoing );
        
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
            Proxy       proxy )
    {
        ProxyProcessingInstructions ret = createInstructions();
        
        ret.setStartCommunicating( true );
        // ret.setReclaimedLockObjects( identifiers );

        ParserFriendlyXprisoMessage outgoing = ParserFriendlyXprisoMessage.create(
                proxy.getNetMeshBase().getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

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
                    outgoing.addRequestedCanceledObject( currentIdentifier );

                } else if( current instanceof NetMeshObjectBecameDeadStateEvent ) {
                    // ignore

                } else if( current instanceof NetMeshObjectDeletedEvent ) {
                    NetMeshObjectDeletedEvent realCurrent = (NetMeshObjectDeletedEvent) current;
                    outgoing.addDeleteChange( realCurrent );

                } else if( current instanceof NetMeshObjectEquivalentsAddedEvent ) {
                    NetMeshObjectEquivalentsAddedEvent realCurrent = (NetMeshObjectEquivalentsAddedEvent) current;
                    outgoing.addEquivalentAddition( realCurrent );

                } else if( current instanceof NetMeshObjectEquivalentsRemovedEvent ) {
                    NetMeshObjectEquivalentsRemovedEvent realCurrent = (NetMeshObjectEquivalentsRemovedEvent) current;
                    outgoing.addEquivalentRemoval( realCurrent );

                } else if( current instanceof NetMeshObjectNeighborAddedEvent ) {
                    NetMeshObjectNeighborAddedEvent realCurrent = (NetMeshObjectNeighborAddedEvent) current;
                    outgoing.addNeighborAddition( realCurrent );
                    
                    NetMeshObject neighbor = realCurrent.getNeighborMeshObject();
                    potentiallyConvey.add( neighbor );

                } else if( current instanceof NetMeshObjectNeighborRemovedEvent ) {
                    NetMeshObjectNeighborRemovedEvent realCurrent = (NetMeshObjectNeighborRemovedEvent) current;
                    outgoing.addNeighborRemoval( realCurrent );

                } else if( current instanceof NetMeshObjectPropertyChangeEvent ) {
                    NetMeshObjectPropertyChangeEvent realCurrent = (NetMeshObjectPropertyChangeEvent) current;
                    outgoing.addPropertyChange( realCurrent );

                } else if( current instanceof NetMeshObjectRoleAddedEvent ) {
                    NetMeshObjectRoleAddedEvent realCurrent = (NetMeshObjectRoleAddedEvent) current;
                    outgoing.addRoleAddition( realCurrent );

                } else if( current instanceof NetMeshObjectRoleRemovedEvent ) {
                    NetMeshObjectRoleRemovedEvent realCurrent = (NetMeshObjectRoleRemovedEvent) current;
                    outgoing.addRoleRemoval( realCurrent );

                } else if( current instanceof NetMeshObjectTypeAddedEvent ) {
                    NetMeshObjectTypeAddedEvent realCurrent = (NetMeshObjectTypeAddedEvent) current;
                    outgoing.addTypeAddition( realCurrent );

                } else if( current instanceof NetMeshObjectTypeRemovedEvent ) {
                    NetMeshObjectTypeRemovedEvent realCurrent = (NetMeshObjectTypeRemovedEvent) current;
                    outgoing.addTypeRemoval( realCurrent );

                } else if( current instanceof ReplicaCreatedEvent ) {
                    // skip

                } else {
                    log.error( "What is this: " + current );
                }
            }
        }
        
        for( NetMeshObject current : potentiallyConvey ) {
            if( addPotentiallyConvey( current, outgoing, proxy )) {
                ret.addRegisterReplicationIfNotAlready( current );
            }
        }
        
        if( !outgoing.isEmpty() ) {
            ret.setStartCommunicating(  true );
            ret.setSendViaEndpoint( outgoing );
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
            Proxy                                   proxy )
    {
        NetMeshBase theMeshBase = proxy.getNetMeshBase();
        
        ProxyProcessingInstructions ret = createInstructions();

        ret.setIncomingXprisoMessageEndpoint( endpoint );
        ret.setIncomingXprisoMessage( incoming );

        ParserFriendlyXprisoMessage outgoing = ParserFriendlyXprisoMessage.create(
                theMeshBase.getIdentifier(),
                proxy.getPartnerMeshBaseIdentifier() );

        if( incoming.getRequestId() != 0 ) {
            outgoing.setResponseId( incoming.getRequestId() ); // make this message as a response
        }

        processIncomingRequestedFirstTimeObjects(      proxy, ret, outgoing );
        processIncomingRequestedResynchronizeReplicas( proxy, ret, outgoing );
        processIncomingRequestedHomeReplicas(          proxy, ret, outgoing );
        processIncomingRequestedLockObjects(           proxy, ret, outgoing );
        processIncomingReclaimedLockObjects(           proxy, ret, outgoing );
        processIncomingCanceledObjects(                proxy, ret, outgoing );
        processIncomingConveyedObjects(                proxy, ret, outgoing, isResponseToOngoingQuery );
        processIncomingPushedLocks(                    proxy, ret, outgoing );
        processIncomingPushedHomes(                    proxy, ret, outgoing );
        processIncomingPropertyChanges(                proxy, ret, outgoing );
        processIncomingTypeChanges(                    proxy, ret, outgoing );
        processIncomingNeighborRoleChanges(            proxy, ret, outgoing );
        processIncomingEquivalentChanges(              proxy, ret, outgoing );
        processIncomingDeleteChanges(                  proxy, ret, outgoing );

    // send message
        if( !outgoing.isEmpty() ) {
            ret.setSendViaEndpoint( outgoing );
        }

        return ret;
    }

    /**
     * Process the incoming request: first-time requested objects.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingRequestedFirstTimeObjects(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();

        // requested first-time objects
        if( arrayHasContent( incoming.getRequestedFirstTimeObjects() ) ) {
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

            for( int i=0 ; i<firstTimeObjects.length ; ++i ) {
                if( firstTimeObjects[i] != null ) {
                    if( addPotentiallyConvey( firstTimeObjects[i], outgoing, proxy ) ) {
                        ret.addRegisterReplicationIfNotAlready( firstTimeObjects[i] );
                    }
                }
            }
        }
    }

    /**
     * Process the incoming request: resynchronize replicas.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingRequestedResynchronizeReplicas(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();

        // requested resynchronized objects
        if( arrayHasContent( incoming.getRequestedResynchronizeReplicas())) {
            NetMeshObject [] resync = theMeshBase.findMeshObjectsByIdentifier( incoming.getRequestedResynchronizeReplicas() );
            
            for( int i=0 ; i<resync.length ; ++i ) {
                if( resync[i] != null ) {
                    if( addPotentiallyConvey( resync[i], outgoing, proxy )) {
                        ret.addRegisterReplicationIfNotAlready( resync[i] );
                    }
                }
            }
        }
    }

    /**
     * Process the incoming request: requested home replicas.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingRequestedHomeReplicas(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();

        // requested home replicas
        if( arrayHasContent( incoming.getRequestedHomeReplicas())) {
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
            for( Proxy p : toGet.keySet() ) {
                ArrayList<NetMeshObject> list = toGet.get( p );
                try {
                    p.tryToObtainHomeReplicas( ArrayHelper.copyIntoNewArray( list, NetMeshObject.class ), theDefaultRpcWaitDuration );
                } catch( RemoteQueryTimeoutException ex ) {
                    log.warn( ex );
                }
                for( NetMeshObject current : list ) {
                    if( current.isHomeReplica() ) {
                        toSurrender.add( current );
                    }
                }
            }
            for( NetMeshObject current : toSurrender ) {
                addPotentiallyConvey( current, outgoing, proxy );
                ret.addSurrenderHome( current ); // this includes the addRegisterReplicationIfNotAlready functionality
                outgoing.addPushHomeReplica( current.getIdentifier() );
            }
        }
    }

    /**
     * Process the incoming request: requested locks.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingRequestedLockObjects(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();

        // requested locks
        if( arrayHasContent( incoming.getRequestedLockObjects())) {
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
            for( Proxy p : toGet.keySet() ) {
                ArrayList<NetMeshObject> list = toGet.get( p );
                try {
                    p.tryToObtainLocks( ArrayHelper.copyIntoNewArray( list, NetMeshObject.class ), theDefaultRpcWaitDuration );
                } catch( RemoteQueryTimeoutException ex ) {
                    log.warn( ex );
                }
                for( NetMeshObject current : list ) {
                    if( current.hasLock() ) {
                        toSurrender.add( current );
                    }
                }
            }
            for( NetMeshObject current : toSurrender ) {
                addPotentiallyConvey( current, outgoing, proxy );
                ret.addSurrenderLock( current ); // this includes the addRegisterReplicationIfNotAlready functionality
                outgoing.addPushLockObject( current.getIdentifier() );
            }
        }
    }
    
    /**
     * Process the incoming request: reclaimed locks.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingReclaimedLockObjects(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();

        // reclaimed locks
        if( arrayHasContent( incoming.getReclaimedLockObjects())) {
            NetMeshObject [] lost = theMeshBase.findMeshObjectsByIdentifier( incoming.getReclaimedLockObjects() );
            
            for( int i=0 ; i<lost.length ; ++i ) {
                ret.addSurrenderLock( lost[i] );
            }
        }
    }
    
    /**
     * Process the incoming request: objects to be canceled.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingCanceledObjects(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();
        
    // canceled objects
        if( arrayHasContent( incoming.getRequestedCanceledObjects())) {
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
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     * @param isResponseToOngoingQuery if true, this message was sent in response to a query
     */
    protected void processIncomingConveyedObjects(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing,
            boolean                     isResponseToOngoingQuery )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();
    
    // conveyed objects
        if( arrayHasContent( incoming.getConveyedMeshObjects())) {
            for( ExternalizedNetMeshObject current : incoming.getConveyedMeshObjects() ) {

                RippleInstructions ripple = RippleInstructions.create( current );
                ripple.setProxies( new Proxy[] { proxy } );
                ripple.setProxyTowardsHomeIndex( 0 );
                ripple.setProxyTowardsLockIndex( 0 );

                NetMeshObject found = theMeshBase.findMeshObjectByIdentifier( current.getIdentifier() );

                boolean noHomeProxy
                        = current.getProxyTowardsHomeNetworkIdentifier() == null;
                boolean differentHomeProxy
                        =  !noHomeProxy && !proxy.getPartnerMeshBaseIdentifier().equals( current.getProxyTowardsHomeNetworkIdentifier());

                // This is a bit tricky. We distinguish three main dimensions:
                // 1. conveyed MeshObject does or does not already exist locally, and if so, whether its home proxy points in a different direction:
                //        code (3 alternatives):
                //            found == null
                //            found != null && found.getProxyTowardsHomeReplica() == proxy
                //            found != null && found.getProxyTowardsHomeReplica() != proxy
                // 2. the conveyed MeshObject carries Proxy information that points to a
                //    different home object than that sent this conveyed MeshObject
                //        code (2 alternatives): differentHomeProxy
                // 3. the incoming message is or is not a response to a request that was originated locally
                //        code (2 alternatives): isResponseToOngoingQuery
                //
                // In response, the choices are:
                // A: do nothing | rippleCreate | rippleResynchronize
                // B: do nothing | cancel lease from current proxyTorwardsHome
                // C: do nothing | create instruction to issue resynchronize message with home proxy specified in conveyed MeshObject
                // D: do nothing | cancel lease of offered conveyed object
                
                // This produces 3x2x2 different choices, reflected in the following code:

                boolean doRippleCreate        = false;
                boolean doRippleResynchronize = false;
                
                boolean cancelCurrentLease = false;
                boolean cancelOfferedLease = false;
                
                boolean issueResyncMessage = false;
                
                if( found == null ) {
                    if( differentHomeProxy ) {
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
                    
                } else if( found.getProxyTowardsHomeReplica() == proxy ) { // found != null
                    if( differentHomeProxy ) {
                        if( isResponseToOngoingQuery ) {
                            // 2.1.1 -- we have it, but asked for it, message comes from the home, but its home is somewhere else
                            
                            doRippleResynchronize = true;
                            issueResyncMessage    = true;

                        } else { // !isResponseToOngoingQuery
                            // 2.1.2 -- we have it, but didn't ask, message comes from the home, but its home is somewhere else
                            
                            doRippleResynchronize = true;
                            issueResyncMessage    = true;
                        }

                    } else { // !differentHomeProxy
                        if( isResponseToOngoingQuery ) {
                            // 2.2.1 -- we have it, but asked for it, message comes from the home, and we have the right home
                            
                            // do nothing, everything is great
                            
                        } else { // !isResponseToOngoingQuery
                            // 2.2.2 -- we have it, but didn't ask, message comes from the home, and we have the right home
                            
                            // do nothing, everything is great
                        }                        
                    }
                    
                } else { // found != null && found.getProxyTowardsHomeReplica() != proxy
                    if( differentHomeProxy ) {
                        if( isResponseToOngoingQuery ) {
                            // 3.1.1 -- we have it, but asked for it, message comes from somewhere else, and its home is somewhere else
                            
                            // ignore, not worth doing
                            cancelCurrentLease = true;
                            
                        } else { // !isResponseToOngoingQuery
                            // 3.1.2 -- we have it, didn't ask for it, message comes from somewhere else, and its home is somewhere else
                            
                            // ignore, not worth doing
                            cancelCurrentLease = true;
                        }

                    } else { // !differentHomeProxy
                        if( isResponseToOngoingQuery ) {
                            // 3.2.1 -- we have it, but asked for it, message comes from the home, and we thought we have the right home

                            // this is the response from the home object to resync requests
                            // FIXME: This section is currently not reached. See comment in 3.2.2
                            
                            cancelCurrentLease    = true;
                            doRippleResynchronize = true;
                            
                        } else { // !isResponseToOngoingQuery
                            // 3.2.2 -- we have it, didn't ask for it, message comes from the home, and we thought we have the right home
                            
                            // this is the response from the home object to resync requests

                            // FIXME: why should we believe that one? We should only do the previous case (3.2.1) but
                            // currently, responses to resync requests are not RPC-style calls, so we isResponseToOngoingQuery
                            // is false. Can we make them RPC-style synchronous calls to carry the requestID & responseID?

                            cancelCurrentLease    = true;
                            doRippleResynchronize = true;
                        }
                    }
                }
                
                
                // A:
                if( doRippleCreate ) {
                    if( doRippleResynchronize ) {
                        log.error( "programming error: create or resync, not both" );
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
                    outgoing.addRequestedCanceledObject( current.getIdentifier() );
                }
                
                // D:
                if( issueResyncMessage ) {
                    ret.addToResynchronizeInstructions(
                            current.getIdentifier(),
                            current.getProxyTowardsHomeNetworkIdentifier());
                }
            }
        }
    }

    /**
     * Process the incoming request: pushed locks.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingPushedLocks(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();
        
    // pushed locks
        if( arrayHasContent( incoming.getPushLockObjects())) {
            NetMeshObject [] locks = theMeshBase.findMeshObjectsByIdentifier( incoming.getPushLockObjects() );
            
            for( int i=0 ; i<locks.length ; ++i ) {
                if( locks[i] != null ) {
                    locks[i].proxyOnlyPushLock( proxy );
                }
            }
        }
    }
    
    /**
     * Process the incoming request: pushed home replicas.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingPushedHomes(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();
        
    // pushed homes
        if( arrayHasContent( incoming.getPushHomeReplicas() )) {
            NetMeshObject [] homes = theMeshBase.findMeshObjectsByIdentifier( incoming.getPushHomeReplicas() );
            
            for( int i=0 ; i<homes.length ; ++i ) {
                if( homes[i] != null ) {
                    homes[i].proxyOnlyPushHomeReplica( proxy );
                }
            }
        }
    }
    
    /**
     * Process the incoming request: property changes.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingPropertyChanges(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();
        
    // property changes
        if( arrayHasContent( incoming.getPropertyChanges() )) {
            NetMeshObjectPropertyChangeEvent [] events = incoming.getPropertyChanges();
            
            ret.setPropertyChanges( events );
        }    
    }
    
    /**
     * Process the incoming request: type changes.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingTypeChanges(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();
        
    // type changes
        if( arrayHasContent( incoming.getTypeAdditions())) {
            NetMeshObjectTypeAddedEvent [] events = incoming.getTypeAdditions();
            
            ret.setTypeAdditions( events );
        }
        if( arrayHasContent( incoming.getTypeRemovals())) {
            NetMeshObjectTypeRemovedEvent [] events = incoming.getTypeRemovals();
            
            ret.setTypeRemovals( events );
        }
    }
    
    /**
     * Process the incoming request: neighbor and role changes.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingNeighborRoleChanges(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();
        
    // neighbor and role changes
        if( arrayHasContent( incoming.getNeighborAdditions())) {
            NetMeshObjectNeighborAddedEvent [] events = incoming.getNeighborAdditions();

            for( NetMeshObjectNeighborAddedEvent current : events ) {
                if( acceptRelationshipEvent( proxy, current )) {
                    ret.addNeighborAddition( current );
                }
            }
        }
        if( arrayHasContent( incoming.getNeighborRemovals())) {
            NetMeshObjectNeighborRemovedEvent [] events = incoming.getNeighborRemovals();
            
            for( NetMeshObjectNeighborRemovedEvent current : events ) {
                if( acceptRelationshipEvent( proxy, current )) {
                    ret.addNeighborRemoval( current );
                }
            }
        }
        
        if( arrayHasContent( incoming.getRoleAdditions())) {
            NetMeshObjectRoleAddedEvent [] events = incoming.getRoleAdditions();
            
            for( NetMeshObjectRoleAddedEvent current : events ) {
                if( acceptRelationshipEvent( proxy, current )) {
                    ret.addRoleAddition( current );
                }
            }
        }
        if( arrayHasContent( incoming.getRoleRemovals())) {
            NetMeshObjectRoleRemovedEvent [] events = incoming.getRoleRemovals();
            
            for( NetMeshObjectRoleRemovedEvent current : events ) {
                if( acceptRelationshipEvent( proxy, current )) {
                    ret.addRoleRemoval( current );
                }
            }
        }
    }

    /**
     * Process the incoming request: equivalence changes.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingEquivalentChanges(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();
        
    // equivalent changes
        if( arrayHasContent( incoming.getEquivalentsAdditions())) {
            NetMeshObjectEquivalentsAddedEvent [] events = incoming.getEquivalentsAdditions();
            
            for( NetMeshObjectEquivalentsAddedEvent current : events ) {
                if( acceptRelationshipEvent( proxy, current )) {
                    ret.addEquivalentsAddition( current );
                }
            }
        }
        if( arrayHasContent( incoming.getEquivalentsRemovals())) {
            NetMeshObjectEquivalentsRemovedEvent [] events = incoming.getEquivalentsRemovals();
            
            for( NetMeshObjectEquivalentsRemovedEvent current : events ) {
                if( acceptRelationshipEvent( proxy, current )) {
                    ret.addEquivalentsRemoval( current );
                }
            }
        }
    }

    /**
     * Process the incoming request: delete changes.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    protected void processIncomingDeleteChanges(
            Proxy                       proxy,
            ProxyProcessingInstructions ret,
            ParserFriendlyXprisoMessage outgoing )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        NetMeshBase                 theMeshBase = proxy.getNetMeshBase();
        
    // deletions
        if( arrayHasContent( incoming.getDeletions())) {
            NetMeshObjectDeletedEvent [] events = incoming.getDeletions();
            
            ret.setDeletions( events );
        }
    }      
    
    /**
     * Helper method to determine whether the array has any content.
     * 
     * @param array the array
     * @return true if the array is non-null and has a length other than 0
     */
    protected boolean arrayHasContent(
            Object [] array )
    {
        if( array == null ) {
            return false;
        }
        if( array.length == 0 ) {
            return false;
        }
        return true;
    }

    /**
     * Helper method to add a NetMeshObject to an outgoing XprisoMessage to be conveyed, if needed.
     * 
     * @param obj the potentially added NetMeshObject
     * @param outgoing the XprisoMessage
     * @param proxy the Proxy via which the XprisoMessage will be sent
     * @param needsToBeSent if false, do not send. 
     * @return true if the NetMeshObject was added
     */
    protected boolean addPotentiallyConvey(
            NetMeshObject               obj,
            ParserFriendlyXprisoMessage outgoing,
            Proxy                       proxy,
            boolean                     needsToBeSent )
    {
        // make sure we don't have it already
        for( ExternalizedNetMeshObject current : outgoing.getConveyedMeshObjects() ) {
            if( current.getIdentifier().equals( obj.getIdentifier() )) {
                return false;
            }
        }
        
        // make sure we need to
        if( !needsToBeSent ) {
            return false;
        }
        ExternalizedNetMeshObject toAdd = obj.asExternalized( !thePointsReplicasToItself );
        outgoing.addConveyedMeshObject( toAdd );

        return true;
    }

    /**
     * Helper method to add a NetMeshObject to an outgoing XprisoMessage to be conveyed, if needed.
     * 
     * @param obj the potentially added NetMeshObject
     * @param outgoing the XprisoMessage
     * @param proxy the Proxy via which the XprisoMessage will be sent
     * @return true if the NetMeshObject was added
     */
    protected boolean addPotentiallyConvey(
            NetMeshObject               obj,
            ParserFriendlyXprisoMessage outgoing,
            Proxy                       proxy )
    {
        return addPotentiallyConvey( obj, outgoing, proxy, !Utils.hasReplicaInDirection( obj, proxy ));
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
