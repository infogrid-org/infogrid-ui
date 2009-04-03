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

import java.util.ArrayList;
import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
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
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * Instructions to a Proxy for processing of an incoming XprisoMessage or internal
 * event, as determined by the ProxyPolicy.
 */
public class ProxyProcessingInstructions
        implements
            CanBeDumped
{
    /**
     * Factory method.
     * 
     * @return the DefaultProxyProcessingInstructions
     */
    public static ProxyProcessingInstructions create()
    {
        ProxyProcessingInstructions ret = new ProxyProcessingInstructions();
        
        return ret;
    }
    
    /**
     * Private constructor, for subclasses only. Use factory method.
     */
    protected ProxyProcessingInstructions()
    {
        // no op
    }

    /**
     * Set whether the Proxy should cease communications.
     * 
     * @param newValue true if the Proxy should cease communications
     */
    public void setCeaseCommunications(
            boolean newValue )
    {
        theCeaseCommunications = newValue;
    }

    /**
     * Determine whether the Proxy should cease communications.
     * 
     * @return returns true if the Proxy should cease communications
     */
    public boolean getCeaseCommunications()
    {
        return theCeaseCommunications;
    }

    /**
     * Set whether the Proxy should start communicating.
     * 
     * @param newValue true if the Proxy should start communicating
     */
    public void setStartCommunicating(
            boolean newValue )
    {
        theStartCommunicating = newValue;
    }
    
    /**
     * Determine whether the Proxy should start communicating.
     * 
     * @return returns true of the Proxy should start communicating
     */
    public boolean getStartCommunicating()
    {
        return theStartCommunicating;
    }

//

    /**
     * Set the XprisoMessage to be sent via the WaitEndpoint.
     * 
     * @param outgoing the outgoing XprisoMessage
     */
    public void setSendViaWaitEndpoint(
            XprisoMessage outgoing )
    {
        setSendViaWaitEndpoint( outgoing, null );
    }

    /**
     * Set the XprisoMessage to be sent via the WaitEndpoint.
     *
     * @param outgoing the outgoing XprisoMessage
     * @param accessLocallySynchronizerQueryKey if given, identifies the ongoing AccessLocallySynchronizer transaction
     *        to which the synchronous message exchange operation shall be added
     */
    public void setSendViaWaitEndpoint(
            XprisoMessage outgoing,
            Long          accessLocallySynchronizerQueryKey )
    {
        if( theSendViaWaitEndpoint != null ) {
            throw new IllegalStateException( "Invoked a second time" );
        }
        theSendViaWaitEndpoint         = outgoing;
        theSendViaWaitEndpointQueryKey = accessLocallySynchronizerQueryKey;
    }
    
    /**
     * Obtain the XprisoMessage to be sent via the WaitEndpoint.
     * 
     * @return the outgoing XprisoMessage, if any
     */
    public XprisoMessage getSendViaWaitEndpoint()
    {
        return theSendViaWaitEndpoint;
    }

    /**
     * Obtain the query key identifying an ongoing AccessLocallySynchronizer transaction to which the
     * synchronous message exchange operation via the WaitEndpoint shall be added.
     *
     * @return the query key, if any
     */
    public Long getSendViaWaitEndpointQueryKey()
    {
        return theSendViaWaitEndpointQueryKey;
    }

    /**
     * Set the timeout for the WaitEndpoint in this operation.
     * 
     * @param newValue the timeout, in milliseconds
     */
    public void setWaitEndpointTimeout(
            long newValue )
    {
        theWaitEndpointTimeout = newValue;
    }
    
    /**
     * Obtain the timeout for the WaitEndpoint in this operation.
     * 
     * @return the timeout, in milliseconds
     */
    public long getWaitEndpointTimeout()
    {
        return theWaitEndpointTimeout;
    }
    
//
    
    /**
     * Set the XprisoMessage to be sent via the regular communications endpoint.
     * 
     * @param outgoing the outgoing XprisoMessage
     */
    public void setSendViaEndpoint(
            XprisoMessage outgoing )
    {
        if( theSendViaEndpoint != null ) {
            throw new IllegalStateException( "Invoked a second time" );
        }
        theSendViaEndpoint = outgoing;
    }
    
    /**
     * Obtain the XprisoMessage to be sent via the regular communications endpoint.
     * 
     * @return the outgoing XprisoMessage, if any
     */
    public XprisoMessage getSendViaEndpoint()
    {
        return theSendViaEndpoint;
    }

//
    
    /**
     * Set the paths of NetMeshObjects requested for the first time.
     * 
     * @param newValue the paths
     */
    public void setRequestedFirstTimePaths(
            NetMeshObjectAccessSpecification [] newValue )
    {
        if( theRequestedFirstTimePaths != null ) {
            throw new IllegalStateException( "Invoked a second time" );
        }
        theRequestedFirstTimePaths = newValue;
    }
    
    /**
     * Obtain the paths of NetMeshObjects requested for the first time.
     * 
     * @return the paths
     */
    public NetMeshObjectAccessSpecification [] getRequestedFirstTimePaths()
    {
        return theRequestedFirstTimePaths;
    }

//

    /**
     * Set the incoming XprisoMessage.
     * 
     * @param newValue the incoming XprisoMessage
     */
    public void setIncomingXprisoMessage(
            XprisoMessage newValue )
    {
        theIncomingXprisoMessage = newValue;
    }
    
    /**
     * Obtain the incoming XprisoMessage.
     * 
     * @return the incoming XprisoMessage
     */
    public XprisoMessage getIncomingXprisoMessage()
    {
        return theIncomingXprisoMessage;
    }
    
    /**
     * Set the MessageEndpoint through which the incoming XprisoMessage was obtained.
     * 
     * @param newValue the MessageEndpoint
     */
    public void setIncomingXprisoMessageEndpoint(
            ReceivingMessageEndpoint<XprisoMessage> newValue )
    {
        theIncomingXprisoMessageEndpoint = newValue;
    }
    
    /**
     * Obtain the MessageEndpoint through which the incoming XprisoMessage was obtained.
     * 
     * @return the MessageEndpoint
     */
    public ReceivingMessageEndpoint<XprisoMessage> getIncomingXprisoMessageEndpoint()
    {
        return theIncomingXprisoMessageEndpoint;
    }
    
    /**
     * Add a Proxy in the NetMeshObject to this Proxy because a replica of it was just issued
     * by this Proxy to its partner.
     * 
     * @param toAdd the NetMeshObject in which to add the Proxy
     */
    public void addRegisterReplicationIfNotAlready(
            NetMeshObject toAdd )
    {
        theRegisterReplicationsIfNotAlready.add( toAdd );
    }
    
    /**
     * Obtain the NetMeshObjects to which this Proxy shall be added because a replica
     * of each was just issued by this Proxy to this partner.
     * 
     * @return the NetMeshObjects in which to add the Proxy
     */
    public NetMeshObject [] getRegisterReplicationsIfNotAlready()
    {
        return ArrayHelper.copyIntoNewArray( theRegisterReplicationsIfNotAlready, NetMeshObject.class  );
    }

    /**
     * Add an instruction to ripple-create a NetMeshObject.
     * 
     * @param toAdd the instructions
     */
    public void addRippleCreate(
            RippleInstructions toAdd )
    {
        theRippleCreates.add( toAdd );
    }
    
    /**
     * Obtain the instructions to ripple-create a NetMeshObject.
     * 
     * @return the instructions
     */
    public RippleInstructions [] getRippleCreates()
    {
        return ArrayHelper.copyIntoNewArray( theRippleCreates, RippleInstructions.class );
    }
    
    /**
     * Add an instruction to ripple-resynchronize a NetMeshObject.
     * 
     * @param toAdd the instructions
     */
    public void addRippleResynchronize(
            RippleInstructions toAdd )
    {
        theRippleResynchronizes.add( toAdd );
    }
    
    /**
     * Obtain the instructions to ripple-resynchronize a NetMeshObject.
     * 
     * @return the instructions
     */
    public RippleInstructions [] getRippleResynchronizes()
    {
        return ArrayHelper.copyIntoNewArray( theRippleResynchronizes, RippleInstructions.class );
    }
    
    /**
     * Add resynchronize instructions.
     * 
     * @param identifierToAdd the identifier of the NetMeshObject to resynchronize
     * @param proxyIdentifierToAdd the identifier of the Proxy through which to resynchronize
     */
    public void addToResynchronizeInstructions(
            NetMeshObjectIdentifier identifierToAdd,
            NetMeshBaseIdentifier   proxyIdentifierToAdd )
    {
        ResynchronizeInstructions found = null;
        for( ResynchronizeInstructions current : theResynchronizeInstructions ) {
            if( current.getProxyIdentifier().equals( proxyIdentifierToAdd )) {
                found = current;
                break;
            }
        }
        if( found != null ) {
            found.addNetMeshObjectIdentifier( identifierToAdd );
        } else {
            ResynchronizeInstructions toAdd = ResynchronizeInstructions.create( proxyIdentifierToAdd );
            toAdd.addNetMeshObjectIdentifier( identifierToAdd );
            theResynchronizeInstructions.add( toAdd );
        }
    }
    
    /**
     * Obtain the resynchronize instructions.
     * 
     * @return the instructions
     */
    public ResynchronizeInstructions [] getResynchronizeInstructions()
    {
        return ArrayHelper.copyIntoNewArray( theResynchronizeInstructions, ResynchronizeInstructions.class );
    }
    
    /**
     * Add cancel instructions.
     * 
     * @param objectToAdd the NetMeshObject whose lease shall be canceled
     * @param proxyToAdd the Proxy through which the lease shall be canceled
     */
    public void addToCancelInstructions(
            NetMeshObject objectToAdd,
            Proxy         proxyToAdd )
    {
        CancelInstructions found = null;
        for( CancelInstructions current : theCancelInstructions ) {
            if( current.getProxy() == proxyToAdd ) {
                found = current;
                break;
            }
        }
        if( found != null ) {
            found.addNetMeshObject( objectToAdd );
        } else {
            CancelInstructions toAdd = CancelInstructions.create( proxyToAdd );
            toAdd.addNetMeshObject( objectToAdd );
            theCancelInstructions.add( toAdd );
        }
    }
    
    /**
     * Obtain the cancel instructions.
     * 
     * @return the instructions
     */
    public CancelInstructions [] getCancelInstructions()
    {
        return ArrayHelper.copyIntoNewArray( theCancelInstructions, CancelInstructions.class );
    }
    
    /**
     * Add a NetMeshObject whose lock shall be surrendered.
     * 
     * @param toAdd the NetMeshObject
     */
    public void addSurrenderLock(
            NetMeshObject toAdd )
    {
        theSurrenderLocks.add( toAdd );
    }
    
    /**
     * Obtain the NetMeshObjects whose locks shall be surrendered.
     * 
     * @return the NetMeshObjects
     */
    public NetMeshObject [] getSurrenderLocks()
    {
        return ArrayHelper.copyIntoNewArray(  theSurrenderLocks, NetMeshObject.class );
    }

    /**
     * Add a NetMeshObject whose home replica status shall be surrendered.
     * 
     * @param toAdd the NetMeshObject
     */
    public void addSurrenderHome(
            NetMeshObject toAdd )
    {
        theSurrenderHomes.add( toAdd );
    }
    
    /**
     * Obtain the NetMeshObjects whose home replica status shall be surrendered.
     * 
     * @return the NetMeshObjects
     */
    public NetMeshObject [] getSurrenderHomes()
    {
        return ArrayHelper.copyIntoNewArray(  theSurrenderHomes, NetMeshObject.class );
    }

    /**
     * Add a NetMeshObject whose lease shall be canceled.
     * 
     * @param toAdd the NetMeshObject
     */
    public void addCancel(
            NetMeshObject toAdd )
    {
        theCancels.add( toAdd );
    }
    
    /**
     * The NetMeshObjects whose lease shall be canceled.
     * 
     * @return the NetMeshObjects
     */
    public NetMeshObject [] getCancels()
    {
        return ArrayHelper.copyIntoNewArray( theCancels, NetMeshObject.class );
    }
    
    /**
     * Set the PropertyChangeEvents that shall be rippled.
     * 
     * @param newValue the PropertyChangeEvents
     */
    public void setPropertyChanges(
            NetMeshObjectPropertyChangeEvent [] newValue )
    {
        if( newValue != null ) {
            thePropertyChanges = newValue;
        } else {
            thePropertyChanges = new NetMeshObjectPropertyChangeEvent[0];
        }
    }
    
    /**
     * Obtain the PropertyChangeEvents that shall be rippled.
     * 
     * @return the PropertyChangeEvents
     */
    public NetMeshObjectPropertyChangeEvent [] getPropertyChanges()
    {
        return thePropertyChanges;
    }
    
    /**
     * Add a TypeAddedEvent that shall be ripplied.
     * 
     * @param newValue the TypeAddedEvents
     */
    public void setTypeAdditions(
            NetMeshObjectTypeAddedEvent [] newValue )
    {
        theTypeAdditions = newValue;
    }
    
    /**
     * Obtain the TypeAddedEvents that shall be rippled.
     * 
     * @return the TypeAddedEvents
     */
    public NetMeshObjectTypeAddedEvent [] getTypeAdditions()
    {
        return theTypeAdditions;
    }

    /**
     * Add a TypeRemovedEvent that shall be rippled.
     * 
     * @param newValue the TypeRemovedEvent
     */
    public void setTypeRemovals(
            NetMeshObjectTypeRemovedEvent [] newValue )
    {
        theTypeRemovals = newValue;
    }
    
    /**
     * Obtain the TypeRemovedEvents that shall be ripplied.
     * 
     * @return the TypeRemovedEvents
     */
    public NetMeshObjectTypeRemovedEvent [] getTypeRemovals()
    {
        return theTypeRemovals;
    }
    
    /**
     * Add an EquivalentsAddedEvent that shall be rippled.
     * 
     * @param toAdd the EquivalentsAddedEvent
     */
    public void addEquivalentsAddition(
            NetMeshObjectEquivalentsAddedEvent toAdd )
    {
        theEquivalentsAdditions.add( toAdd );
    }
    
    /**
     * Obtain the EquivalentsAddedEvents that shall be rippled.
     * 
     * @return the EquivalentsAddedEvents
     */
    public NetMeshObjectEquivalentsAddedEvent [] getEquivalentsAdditions()
    {
        return ArrayHelper.copyIntoNewArray( theEquivalentsAdditions, NetMeshObjectEquivalentsAddedEvent.class );
    }

    /**
     * Add an EquivalentsRemovedEvent that shall be rippled.
     * 
     * @param toAdd the EquivalentsRemovedEvent
     */
    public void addEquivalentsRemoval(
            NetMeshObjectEquivalentsRemovedEvent toAdd )
    {
        theEquivalentsRemovals.add( toAdd );
    }
    
    /**
     * Obtain the EquivalentsRemovedEvents that shall be rippled.
     * 
     * @return the EquivalentsRemovedEvents
     */
    public NetMeshObjectEquivalentsRemovedEvent [] getEquivalentsRemovals()
    {
        return ArrayHelper.copyIntoNewArray( theEquivalentsRemovals, NetMeshObjectEquivalentsRemovedEvent.class );
    }
    
    /**
     * Add a NeighborAddedEvent that shall be rippled.
     * 
     * @param toAdd the NeighborAddedEvent
     */
    public void addNeighborAddition(
            NetMeshObjectNeighborAddedEvent toAdd )
    {
        theNeighborAdditions.add( toAdd );
    }
    
    /**
     * Obtain the NeighborAddedEvents that shall be rippled.
     * 
     * @return the NeighborAddedEvents
     */
    public NetMeshObjectNeighborAddedEvent [] getNeighborAdditions()
    {
        return ArrayHelper.copyIntoNewArray( theNeighborAdditions, NetMeshObjectNeighborAddedEvent.class );
    }

    /**
     * Add a NeighborRemovedEvent that shall be rippled.
     * 
     * @param toAdd the NeighborRemovedEvent
     */
    public void addNeighborRemoval(
            NetMeshObjectNeighborRemovedEvent toAdd )
    {
        theNeighborRemovals.add( toAdd );
    }
    
    /**
     * Obtain the NeighborRemovedEvents that shall be rippled.
     * 
     * @return the NeighborRemovedEvents
     */
    public NetMeshObjectNeighborRemovedEvent [] getNeighborRemovals()
    {
        return ArrayHelper.copyIntoNewArray( theNeighborRemovals, NetMeshObjectNeighborRemovedEvent.class );
    }

    /**
     * Add a RoleAddedEvent that shall be rippled.
     * 
     * @param toAdd the RoleAddedEvent
     */
    public void addRoleAddition(
            NetMeshObjectRoleAddedEvent toAdd )
    {
        theRoleAdditions.add( toAdd );
    }
    
    /**
     * Obtain the RoleAddedEvents that shall be rippled.
     * 
     * @return the RoleAddedEvents
     */
    public NetMeshObjectRoleAddedEvent [] getRoleAdditions()
    {
        return ArrayHelper.copyIntoNewArray( theRoleAdditions, NetMeshObjectRoleAddedEvent.class );
    }
    
    /**
     * Add a RoleRemovedEvent that shall be rippled.
     * 
     * @param toAdd the RoleRemovedEvent
     */
    public void addRoleRemoval(
            NetMeshObjectRoleRemovedEvent toAdd )
    {
        theRoleRemovals.add( toAdd );
    }
    
    /**
     * Obtain the RoleRemovedEvents that shall be rippled.
     * 
     * @return the RoleRemovedEvents
     */
    public NetMeshObjectRoleRemovedEvent [] getRoleRemovals()
    {
        return ArrayHelper.copyIntoNewArray( theRoleRemovals, NetMeshObjectRoleRemovedEvent.class );
    }
    
    /**
     * Set the time we wait to obtain replicas.
     * 
     * @param newValue the wait, in milliseconds
     */
    public void setExpectectedObtainReplicasWait(
            long newValue )
    {
        theExpectectedObtainReplicasWait = newValue;
    }
    
    /**
     * Obtain the time to wait to obtain replicas.
     * 
     * @return the time, in milliseconds
     */
    public long getExpectedObtainReplicasWait()
    {
        return theExpectectedObtainReplicasWait;
    }

    /**
     * Set the time we wait to obtain locks.
     *
     * @param newValue the wait, in milliseconds
     */
    public void setExpectedObtainLocksWait(
            long newValue )
    {
        theExpectedObtainLocksWait = newValue;
    }

    /**
     * Obtain the time to wait to obtain locks.
     *
     * @return the time, in milliseconds
     */
    public long getExpectedObtainLocksWait()
    {
        return theExpectedObtainLocksWait;
    }

    /**
     * Set the time we wait to push update rights.
     *
     * @param newValue the wait, in milliseconds
     */
    public void setExpectedPushLocksWait(
            long newValue )
    {
        theExpectedPushLocksWait = newValue;
    }

    /**
     * Obtain the time to wait to push update rights.
     *
     * @return the time, in milliseconds
     */
    public long getExpectedPushLocksWait()
    {
        return theExpectedPushLocksWait;
    }

    /**
     * Set the time we wait to obtain home replica rights.
     *
     * @param newValue the wait, in milliseconds
     */
    public void setExpectedObtainHomeReplicasWait(
            long newValue )
    {
        theExpectedObtainHomeReplicasWait = newValue;
    }

    /**
     * Obtain the time to wait to obtain home replica rights.
     *
     * @return the time, in milliseconds
     */
    public long getExpectedObtainHomeReplicasWait()
    {
        return theExpectedObtainHomeReplicasWait;
    }

    /**
     * Set the time we wait to push home replica rights.
     *
     * @param newValue the wait, in milliseconds
     */
    public void setExpectedPushHomeReplicasWait(
            long newValue )
    {
        theExpectedPushHomeReplicasWait = newValue;
    }

    /**
     * Obtain the time to wait to push home replica rights
     *
     * @return the time, in milliseconds
     */
    public long getExpectedPushHomeReplicasWait()
    {
        return theExpectedPushHomeReplicasWait;
    }

    /**
     * Set the time we wait to force-obtain locks.
     *
     * @param newValue the wait, in milliseconds
     */
    public void setExpectedForceObtainLocksWait(
            long newValue )
    {
        theExpectedForceObtainLocksWait = newValue;
    }

    /**
     * Obtain the time to wait to force-obtain locks.
     *
     * @return the time, in milliseconds
     */
    public long getExpectedForceObtainLocksWait()
    {
        return theExpectedForceObtainLocksWait;
    }

    /**
     * Set the time we wait to resynchronize replicas.
     *
     * @param newValue the wait, in milliseconds
     */
    public void setExpectedResynchronizeWait(
            long newValue )
    {
        theExpectedResynchronizeWait = newValue;
    }

    /**
     * Obtain the time to wait to resynchronize replicas.
     *
     * @return the time, in milliseconds
     */
    public long getExpectedResynchronizeWait()
    {
        return theExpectedResynchronizeWait;
    }

    /**
     * Set the time we wait to cancel replicas.
     *
     * @param newValue the wait, in milliseconds
     */
    public void setExpectedCancelReplicasWait(
            long newValue )
    {
        theExpectedCancelReplicasWait = newValue;
    }

    /**
     * Obtain the time to wait to cancel replicas.
     *
     * @return the time, in milliseconds
     */
    public long getExpectedCancelReplicasWait()
    {
        return theExpectedCancelReplicasWait;
    }

    /**
     * Set the time we wait to freshen replicas.
     *
     * @param newValue the wait, in milliseconds
     */
    public void setExpectedFreshenReplicasWait(
            long newValue )
    {
        theExpectedFreshenReplicasWait = newValue;
    }

    /**
     * Obtain the time to wait to freshen replicas.
     *
     * @return the time, in milliseconds
     */
    public long getExpectedFreshenReplicasWait()
    {
        return theExpectedFreshenReplicasWait;
    }

    /**
     * Set the DeletedEvents that shall be rippled.
     * 
     * @param deletions the DeletedEvents
     */
    public void setDeletions(
            NetMeshObjectDeletedEvent [] deletions )
    {
        if( theDeletions != null && theDeletions.length > 0 ) {
            throw new IllegalStateException( "Invoked a second time" );
        }
        theDeletions = deletions;
    }
    
    /**
     * Obtain the DeletedEvents that shall be rippled.
     * 
     * @return the DeletedEvents
     */
    public NetMeshObjectDeletedEvent [] getDeletions()
    {
        return theDeletions;
    }
    
    /**
     * Determine whether these instructions contain no content.
     * 
     * @return true if they are empty
     */
    public boolean isEmpty()
    {
        if( theCeaseCommunications ) {
            return false;
        }
        if( theRequestedFirstTimePaths != null && theRequestedFirstTimePaths.length > 0 ) {
            return false;
        }
        if( theSendViaWaitEndpoint != null ) {
            return false;
        }
        if( theSendViaEndpoint != null ) {
            return false;
        }
        if( theRegisterReplicationsIfNotAlready != null && !theRegisterReplicationsIfNotAlready.isEmpty() ) {
            return false;
        }
    
        if( theRippleCreates != null && !theRippleCreates.isEmpty() ) {
            return false;
        }
        if( theRippleResynchronizes != null && !theRippleResynchronizes.isEmpty() ) {
            return false;
        }
        if( theResynchronizeInstructions != null && !theResynchronizeInstructions.isEmpty() ) {
            return false;
        }
        if( theSurrenderLocks != null && !theSurrenderLocks.isEmpty() ) {
            return false;
        }
        if( theSurrenderHomes != null && !theSurrenderHomes.isEmpty() ) {
            return false;
        }
        if( theCancels != null && !theCancels.isEmpty() ) {
            return false;
        }
        
        if( thePropertyChanges != null && thePropertyChanges.length > 0 ) {
            return false;
        }
        return true;
    }

    /**
     * Internal consistency check.
     * 
     * @throws IllegalStateException thrown if the instructions are internally inconsistent.
     */
    public void check()
            throws
                IllegalStateException
    {
        // FIXME: Make internal consistency check
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theStartCommunicating",
                    "theCeaseCommunications",
                    "theRequestedFirstTimePaths",
                    "theSendViaWaitEndpoint",
                    "theSendViaEndpoint",
                    "theRegisterReplications",
                    "theRippleCreates",
                    "theRippleResynchronizes",
                    "theResynchronizeInstructions",
                    "theCancelInstructions",
                    "theSurrenderHomes",
                    "theSurrenderLocks",
                    "theCancels",
                    "thePropertyChanges",
                    "theTypeAdditions",
                    "theTypeRemovals",
                    "theEquivalentsAdditions",
                    "theEquivalentsRemovals",
                    "theNeighborAdditions",
                    "theNeighborRemovals",
                    "theRoleAdditions",
                    "theRoleRemovals"
                },
                new Object[] {
                    theStartCommunicating,
                    theCeaseCommunications,
                    theRequestedFirstTimePaths,
                    theSendViaWaitEndpoint,
                    theSendViaEndpoint,
                    theRegisterReplicationsIfNotAlready,
                    theRippleCreates,
                    theRippleResynchronizes,
                    theResynchronizeInstructions,
                    theCancelInstructions,
                    theSurrenderHomes,
                    theSurrenderLocks,
                    theCancels,
                    thePropertyChanges,
                    theTypeAdditions,
                    theTypeRemovals,
                    theEquivalentsAdditions,
                    theEquivalentsRemovals,
                    theNeighborAdditions,
                    theNeighborRemovals,
                    theRoleAdditions,
                    theRoleRemovals
                
                });
    }
    
    /**
     * Should the Proxy start communicating.
     */
    protected boolean theStartCommunicating = false; // default

    /**
     * Should the Proxy cease communications.
     */
    protected boolean theCeaseCommunications = false; // default
    
    /**
     * Which NetMeshObjects are requested for the first time.
     */
    protected NetMeshObjectAccessSpecification [] theRequestedFirstTimePaths;

    /**
     * The incoming XprisoMessage, if any.
     */
    protected XprisoMessage theIncomingXprisoMessage;
    
    /**
     * The endpoint through which the incoming XprisoMessage arrived.
     */
    protected ReceivingMessageEndpoint<XprisoMessage> theIncomingXprisoMessageEndpoint;

    /**
     * The XprisoMessage, if any, that needs to be emitted through the
     * WaitEndpoint as a result of processing these instructions.
     */
    protected XprisoMessage theSendViaWaitEndpoint = null;

   /**
     * The query key identifying an ongoing AccessLocallySynchronizer transaction to which the
     * synchronous message exchange operation via the WaitEndpoint shall be added, if any.
     */
    protected Long theSendViaWaitEndpointQueryKey;

    /**
     * The XprisoMessage, if any, that needs to be emitted through the
     * regular communications endpoint as a result of processing these instructions.
     */
    protected XprisoMessage theSendViaEndpoint = null;

    /**
     * The timeout, in milliseconds, to use for the WaitEndpoint endpoint.
     */
    protected long theWaitEndpointTimeout;
    
    /**
     * NetMeshObjects that need to register an additional replication relationship.
     */
    protected ArrayList<NetMeshObject> theRegisterReplicationsIfNotAlready = new ArrayList<NetMeshObject>();
    
    /**
     * Instructions to ripple-create.
     */
    protected ArrayList<RippleInstructions> theRippleCreates = new ArrayList<RippleInstructions>();
    
    /**
     * Instructions to ripple-resynchronize.
     */
    protected ArrayList<RippleInstructions> theRippleResynchronizes = new ArrayList<RippleInstructions>();
    
    /**
     * Instructions to resynchronize NetMeshObjects.
     */
    protected ArrayList<ResynchronizeInstructions> theResynchronizeInstructions = new ArrayList<ResynchronizeInstructions>();
    
    /**
     * Instructions to cancel leases.
     */
    protected ArrayList<CancelInstructions> theCancelInstructions = new ArrayList<CancelInstructions>();
    
    /**
     * THe NetMeshObjects that are supposed to surrender their update rights.
     */
    protected ArrayList<NetMeshObject> theSurrenderLocks = new ArrayList<NetMeshObject>();
    
    /**
     * The NetMeshObjects that are supposed to surrender their home replica status.
     */
    protected ArrayList<NetMeshObject> theSurrenderHomes = new ArrayList<NetMeshObject>();

    /**
     * THe NetMeshObjects whose lease is to be canceled.
     */
    protected ArrayList<NetMeshObject> theCancels = new ArrayList<NetMeshObject>();
    
    /**
     * The PropertyChangeEvents to ripple.
     */
    protected NetMeshObjectPropertyChangeEvent [] thePropertyChanges = {};

    /**
     * The TypeAddedEvents to ripple.
     */
    protected NetMeshObjectTypeAddedEvent [] theTypeAdditions = {};
    
    /**
     * The TypeRemovedEvents to ripple.
     */
    protected NetMeshObjectTypeRemovedEvent [] theTypeRemovals = {};

    /**
     * The EquivalentsAddedEvents to ripple.
     */
    protected ArrayList<NetMeshObjectEquivalentsAddedEvent> theEquivalentsAdditions = new ArrayList<NetMeshObjectEquivalentsAddedEvent>();
    
    /**
     * The EquivalentsRemovedEvents to ripple.
     */
    protected ArrayList<NetMeshObjectEquivalentsRemovedEvent> theEquivalentsRemovals = new ArrayList<NetMeshObjectEquivalentsRemovedEvent>();
    
    /**
     * The NeighborAddedEvents to ripple.
     */
    protected ArrayList<NetMeshObjectNeighborAddedEvent> theNeighborAdditions = new ArrayList<NetMeshObjectNeighborAddedEvent>();
    
    /**
     * The NeighborRemovedEvents to ripple.
     */
    protected ArrayList<NetMeshObjectNeighborRemovedEvent> theNeighborRemovals = new ArrayList<NetMeshObjectNeighborRemovedEvent>();
    
    /**
     * The RoleAddedEvents to ripple.
     */
    protected ArrayList<NetMeshObjectRoleAddedEvent> theRoleAdditions = new ArrayList<NetMeshObjectRoleAddedEvent>();
    
    /**
     * The RoleRemovedEvents to ripple.
     */
    protected ArrayList<NetMeshObjectRoleRemovedEvent> theRoleRemovals = new ArrayList<NetMeshObjectRoleRemovedEvent>();
    
    /**
     * The object deletions to ripple.
     */
    protected NetMeshObjectDeletedEvent [] theDeletions = {};
    
    protected long theExpectectedObtainReplicasWait = 2000L; // default. FIXME?
    protected long theExpectedObtainLocksWait = 2000L; // default. FIXME?
    protected long theExpectedPushLocksWait = 2000L; // default. FIXME?
    protected long theExpectedObtainHomeReplicasWait = 2000L; // default. FIXME?
    protected long theExpectedPushHomeReplicasWait = 2000L; // default. FIXME?
    protected long theExpectedForceObtainLocksWait = 2000L; // default.FIXME?
    protected long theExpectedResynchronizeWait = 2000L; // default. FIXME?
    protected long theExpectedCancelReplicasWait = 2000L; // default.FIXME?
    protected long theExpectedFreshenReplicasWait = 2000L; // default.FIXME?
}
