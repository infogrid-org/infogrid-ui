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

package org.infogrid.meshbase.net.xpriso;

import java.util.ArrayList;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
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
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * An XprisoMessage that is suitable for parsers that pick up one item at a time, instead of
 * all of them at the same time.
 */
public class ParserFriendlyXprisoMessage
        extends
            AbstractXprisoMessage
        implements
            CanBeDumped
{
    private static final Log log = Log.getLogInstance( ParserFriendlyXprisoMessage.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param sender identifies the sender of this message
     * @param receiver identifies the receiver of this message
     * @return the created ParserFriendlyXprisoMessage
     */
    public static ParserFriendlyXprisoMessage create(
            NetMeshBaseIdentifier sender,
            NetMeshBaseIdentifier receiver )
    {
        ParserFriendlyXprisoMessage ret = new ParserFriendlyXprisoMessage( sender, receiver );

        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( ret, "constructor" );
        }
        return ret;
    }

    /**
     * Constructor.
     * 
     * @param sender identifies the sender of this message
     * @param receiver identifies the receiver of this message
     */
    protected ParserFriendlyXprisoMessage(
            NetMeshBaseIdentifier sender,
            NetMeshBaseIdentifier receiver )
    {
        super( sender, receiver );
    }

    /**
     * Set the NetMeshBaseIdentifier of the sender.
     * 
     * @param newValue the NetMeshBaseIdentifier of the sender
     */
    public void setSenderIdentifier(
            NetMeshBaseIdentifier newValue )
    {
        theSenderIdentifier = newValue;
    }

    /**
     * Set the NetMeshBaseIdentifier of the receiver.
     * 
     * @param newValue the NetMeshBaseIdentifier of the receiver
     */
    public void setReceiverIdentifier(
            NetMeshBaseIdentifier newValue )
    {
        theReceiverIdentifier = newValue;
    }

    /**
     * Set the request ID.
     *
     * @param id the request ID
     */
    public void setRequestId(
            long id )
    {
        theRequestId = id;
    }
    
    /**
     * Set the response ID.
     *
     * @param id the response ID
     */
    public void setResponseId(
            long id )
    {
        theResponseId = id;
    }
    
    /**
     * Set whether or not to cease communications after this message.
     *
     * @param newValue the new value
     */
    public void setCeaseCommunications(
            boolean newValue )
    {
        theCeaseCommunications = newValue;
    }
    
    /**
     * Add an externalized representation of a NetMeshObject that is conveyed
     * by the sender to the receiver, e.g. in response to a first-time lease request.
     *
     * @param toAdd the ExternalizedNetMeshObject
     */
    public void addConveyedMeshObject(
            ExternalizedNetMeshObject toAdd )
    {
        theConveyedMeshObjects.add( toAdd );
    }

    /**
     * Obtain the externalized representation of the NetMeshObjects that are conveyed
     * by the sender to the receiver, e.g. in response to a first-time lease request.
     *
     * @return the ExternalizedNetMeshObjects
     */
    public ExternalizedNetMeshObject [] getConveyedMeshObjects()
    {
        ExternalizedNetMeshObject [] ret = ArrayHelper.copyIntoNewArray( theConveyedMeshObjects, ExternalizedNetMeshObject.class );
        return ret;
    }
    
    /**
     * Add a NetMeshObjectAccessSpecification to a NetMeshObject for which the sender requests
     * a lease for the first time.
     *
     * @param toAdd the NetMeshObjectAccessSpecification to the NetMeshObject
     */
    public void addRequestedFirstTimeObject(
            NetMeshObjectAccessSpecification toAdd )
    {
        theRequestedFirstTimeObjects.add( toAdd );
    }
    
    /**
     * Obtain the NetMeshObjectAccessSpecifications to the NetMeshObjects for which the sender requests
     * a lease for the first time.
     *
     * @return the NetMeshObjectAccessSpecifications for the NetMeshObjects
     */
    public NetMeshObjectAccessSpecification[] getRequestedFirstTimeObjects()
    {
        NetMeshObjectAccessSpecification [] ret = ArrayHelper.copyIntoNewArray( theRequestedFirstTimeObjects, NetMeshObjectAccessSpecification.class );
        return ret;
    }

    /**
     * Add an identifier for a NetMeshObject for which the sender requests
     * that a currently valid lease be chanceled.
     *
     * @param toAdd the NetMeshObjectIdentifier for the NetMeshObjects
     */
    public void addRequestedCanceledObject(
            NetMeshObjectIdentifier toAdd )
    {
        theRequestedCanceledObjects.add( toAdd );
    }
    
    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * that a currently valid lease be canceled.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedCanceledObjects()
    {
        NetMeshObjectIdentifier [] ret = ArrayHelper.copyIntoNewArray( theRequestedCanceledObjects, NetMeshObjectIdentifier.class );
        return ret;
    }

    /**
     * Add an identifier for the NetMeshObjects for which the sender requests
     * a freshening.
     *
     * @param toAdd the NetMeshObjectIdentifier for the NetMeshObjects
     */
    public void addRequestedFreshenReplica(
            NetMeshObjectIdentifier toAdd )
    {
        theRequestedFreshenReplicas.add( toAdd );
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * a freshening.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedFreshenReplicas()
    {
        NetMeshObjectIdentifier [] ret = ArrayHelper.copyIntoNewArray( theRequestedFreshenReplicas, NetMeshObjectIdentifier.class );
        return ret;
    }

    /**
     * Add an identifier for a NetMeshObject for which the sender has a replica
     * that it wishes to resynchronize.
     * 
     * @param toAdd the NetMeshObjectIdentifier for the NetMeshObject
     */
    public void addRequestedResynchronizeReplica(
            NetMeshObjectIdentifier toAdd )
    {
        theRequestedResynchronizeReplicas.add( toAdd );
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender has a replica
     * that it wishes to resynchronize.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedResynchronizeReplicas()
    {
        NetMeshObjectIdentifier [] ret = ArrayHelper.copyIntoNewArray( theRequestedResynchronizeReplicas, NetMeshObjectIdentifier.class  );
        return ret;
    }

    /**
     * Add an identifier for a NetMeshObject for which the sender requests
     * the lock from the receiver (i.e. update rights).
     * 
     * @param toAdd the NetMeshObjectIdentifier for the NetMeshObject
     */
    public void addRequestedLockObject(
            NetMeshObjectIdentifier toAdd )
    {
        theRequestedLockObjects.add( toAdd );
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * the lock from the receiver (i.e. update rights).
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedLockObjects()
    {
        NetMeshObjectIdentifier [] ret = ArrayHelper.copyIntoNewArray( theRequestedLockObjects, NetMeshObjectIdentifier.class );
        return ret;
    }

    /**
     * Add an identifier for a NetMeshObject for which the sender surrenders
     * the lock to the receiver (i.e. update rights).
     * 
     * @param toAdd the NetMeshObjectIdentifier for the NetMeshObject
     */
    public void addPushLockObject(
            NetMeshObjectIdentifier toAdd )
    {
        thePushLockObjects.add( toAdd );
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender surrenders
     * the lock to the receiver (i.e. update rights).
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getPushLockObjects()
    {
        NetMeshObjectIdentifier [] ret = ArrayHelper.copyIntoNewArray( thePushLockObjects, NetMeshObjectIdentifier.class );
        return ret;
    }
    
    /**
     * Add an identifier for a NetMeshObject for which the sender has forcefully
     * reclaimed the lock.
     * 
     * @param toAdd the NetMeshObjectIdentifier for the NetMeshObject
     */
    public void addReclaimedLockObject(
            NetMeshObjectIdentifier toAdd )
    {
        theReclaimedLockObjects.add( toAdd );
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender has forcefully
     * reclaimed the lock.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getReclaimedLockObjects()
    {
        NetMeshObjectIdentifier [] ret = ArrayHelper.copyIntoNewArray( theReclaimedLockObjects, NetMeshObjectIdentifier.class );
        return ret;
    }
    
    /**
     * Add an identifier for a NetMeshObject for which the sender requests
     * home replica status.
     * 
     * @param toAdd the NetMeshObjectIdentifier for the NetMeshObject
     */
    public void addRequestedHomeReplica(
            NetMeshObjectIdentifier toAdd )
    {
        theRequestedHomeReplicas.add( toAdd );
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * home replica status.
     * 
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedHomeReplicas()
    {
        NetMeshObjectIdentifier [] ret = ArrayHelper.copyIntoNewArray( theRequestedHomeReplicas, NetMeshObjectIdentifier.class );
        return ret;
    }

    /**
     * Add an identifier for a NetMeshObject for which the sender surrenders
     * the home replica status to the receiver.
     * 
     * @param toAdd the NetMeshObjectIdentifier for the NetMeshObject
     */
    public void addPushHomeReplica(
            NetMeshObjectIdentifier toAdd )
    {
        thePushHomeReplicas.add( toAdd );
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender surrenders
     * the home replica status to the receiver.
     * 
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getPushHomeReplicas()
    {
        NetMeshObjectIdentifier [] ret = ArrayHelper.copyIntoNewArray( thePushHomeReplicas, NetMeshObjectIdentifier.class );
        return ret;
    }
    
    /**
     * Add a type added event that the sender needs to convey to the
     * receiver.
     *
     * @param toAdd the type addition event
     */
    public void addTypeAddition(
            NetMeshObjectTypeAddedEvent toAdd )
    {
        theTypeAdditions.add( toAdd );
    }

    /**
     * Obtain the type addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the type addition events
     */
    public NetMeshObjectTypeAddedEvent [] getTypeAdditions()
    {
        NetMeshObjectTypeAddedEvent [] ret = ArrayHelper.copyIntoNewArray( theTypeAdditions, NetMeshObjectTypeAddedEvent.class );
        return ret;
    }

    /**
     * Add a type removed event that the sender needs to convey to the
     * receiver.
     *
     * @param toAdd the type removed event
     */
    public void addTypeRemoval(
            NetMeshObjectTypeRemovedEvent toAdd )
    {
        theTypeRemovals.add( toAdd );
    }

    /**
     * Obtain the type removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the type removal events
     */
    public NetMeshObjectTypeRemovedEvent [] getTypeRemovals()
    {
        NetMeshObjectTypeRemovedEvent [] ret = ArrayHelper.copyIntoNewArray( theTypeRemovals, NetMeshObjectTypeRemovedEvent.class );
        return ret;
    }

    /**
     * Add a property change event that the sender needs to convey to the
     * received.
     *
     * @param toAdd the property change event
     */
    public void addPropertyChange(
            NetMeshObjectPropertyChangeEvent toAdd )
    {
        thePropertyChanges.add( toAdd );
    }

    /**
     * Obtain the property change events that the sender needs to convey to the
     * receiver.
     *
     * @return the property change events
     */
    public NetMeshObjectPropertyChangeEvent [] getPropertyChanges()
    {
        NetMeshObjectPropertyChangeEvent [] ret = ArrayHelper.copyIntoNewArray( thePropertyChanges, NetMeshObjectPropertyChangeEvent.class );
        return ret;
    }

    /**
     * Add a neighbor addition event that the sender needs to convey to the
     * receiver.
     *
     * @param toAdd the neighbor addition event
     */
    public void addNeighborAddition(
            NetMeshObjectNeighborAddedEvent toAdd )
    {
        theNeighborAdditions.add( toAdd );
    }

    /**
     * Obtain the neighbor addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the neighbor addition events
     */
    public NetMeshObjectNeighborAddedEvent [] getNeighborAdditions()
    {
        NetMeshObjectNeighborAddedEvent [] ret = ArrayHelper.copyIntoNewArray( theNeighborAdditions, NetMeshObjectNeighborAddedEvent.class );
        return ret;
    }

    /**
     * Add a neighbor removal event that the sender needs to convey to the
     * receiver.
     *
     * @param toAdd the neighbor removal events
     */
    public void addNeighborRemoval(
            NetMeshObjectNeighborRemovedEvent toAdd )
    {
        theNeighborRemovals.add( toAdd );
    }

    /**
     * Obtain the neighbor removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the neighbor removal events
     */
    public NetMeshObjectNeighborRemovedEvent [] getNeighborRemovals()
    {
        NetMeshObjectNeighborRemovedEvent [] ret = ArrayHelper.copyIntoNewArray( theNeighborRemovals, NetMeshObjectNeighborRemovedEvent.class );
        return ret;
    }
    
    /**
     * Add an equivalent addition event that the sender needs to convey to the
     * receiver.
     * 
     * @param toAdd the equivalent addition event
     */
    public void addEquivalentAddition(
            NetMeshObjectEquivalentsAddedEvent toAdd )
    {
        theEquivalentsAdditions.add( toAdd );
    }

    /**
     * Obtain the equivalent addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the equivalent addition events
     */
    public NetMeshObjectEquivalentsAddedEvent [] getEquivalentsAdditions()
    {
        NetMeshObjectEquivalentsAddedEvent [] ret = ArrayHelper.copyIntoNewArray( theEquivalentsAdditions, NetMeshObjectEquivalentsAddedEvent.class );
        return ret;
    }

    /**
     * Add an equivalent removal event that the sender needs to convey to the
     * receiver.
     * 
     * @param toAdd the equivalent removal event
     */
    public void addEquivalentRemoval(
            NetMeshObjectEquivalentsRemovedEvent toAdd )
    {
        theEquivalentsRemovals.add( toAdd );
    }

    /**
     * Obtain the equivalent removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the equivalent removal events
     */
    public NetMeshObjectEquivalentsRemovedEvent [] getEquivalentsRemovals()
    {
        NetMeshObjectEquivalentsRemovedEvent [] ret = ArrayHelper.copyIntoNewArray( theEquivalentsRemovals, NetMeshObjectEquivalentsRemovedEvent.class );
        return ret;
    }    
    
    /**
     * Add a role addition event that the sender needs to convey to the
     * receiver.
     *
     * @param toAdd the role addition event
     */
    public void addRoleAddition(
            NetMeshObjectRoleAddedEvent toAdd )
    {
        theRoleAdditions.add( toAdd );
    }

    /**
     * Obtain the role addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the role addition events
     */
    public NetMeshObjectRoleAddedEvent [] getRoleAdditions()
    {
        NetMeshObjectRoleAddedEvent [] ret = ArrayHelper.copyIntoNewArray( theRoleAdditions, NetMeshObjectRoleAddedEvent.class );
        return ret;        
        
    }

    /**
     * Add a role removal event that the sender needs to convey to the
     * receiver.
     *
     * @param toAdd the role removal event
     */
    public void addRoleRemoval(
            NetMeshObjectRoleRemovedEvent toAdd )
    {
        theRoleRemovals.add( toAdd );
    }

    /**
     * Obtain the role removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the role removal events
     */
    public NetMeshObjectRoleRemovedEvent [] getRoleRemovals()
    {
        NetMeshObjectRoleRemovedEvent [] ret = ArrayHelper.copyIntoNewArray( theRoleRemovals, NetMeshObjectRoleRemovedEvent.class );
        return ret;
    }

    /**
     * Add a deletion event for the NetMeshObject that the sender has deleted
     * semantically and that the sender needs to convey to the receiver.
     *
     * @param toAdd the deletion event
     */
    public void addDeleteChange(
            NetMeshObjectDeletedEvent toAdd )
    {
        theDeleteChanges.add( toAdd );
    }

    /**
     * Obtain the deletion events for the NetMeshObjects that the sender has deleted
     * semantically and that the sender needs to convey to the receiver.
     *
     * @return the deletion events
     */
    public NetMeshObjectDeletedEvent [] getDeletions()
    {
        NetMeshObjectDeletedEvent [] ret = ArrayHelper.copyIntoNewArray( theDeleteChanges, NetMeshObjectDeletedEvent.class );
        return ret;
    }

    /**
     * Determine whether this message contains any valid payload or is empty.
     *
     * @return true if it is empty
     */
    public boolean isEmpty()
    {
        // ignore theSenderIdentifier;
        // ignore theReceiverIdentifier;
        // ignore theRequestId;
        // do NOT ignore responseID: may acknowledge receipt of incoming message
        
        // alphabetically, so we can make sure we have all of them by comparing with the IDE

        if( theCeaseCommunications ) {
            return false;
        }
        if( theConveyedMeshObjects.size() > 0 ) {
            return false;
        }
        if( theDeleteChanges.size() > 0 ) {
            return false;
        }
        if( theEquivalentsAdditions.size() > 0 ) {
            return false;
        }
        if( theEquivalentsRemovals.size() > 0 ) {
            return false;
        }
        if( theNeighborAdditions.size() > 0 ) {
            return false;
        }
        if( theNeighborRemovals.size() > 0 ) {
            return false;
        }
        if( thePropertyChanges.size() > 0 ) {
            return false;
        }
        if( thePushHomeReplicas.size() > 0 ) {
            return false;
        }
        if( thePushLockObjects.size() > 0 ) {
            return false;
        }
        // ignore theReceiverIdentifier;
        if( theReclaimedLockObjects.size() > 0 ) {
            return false;
        }
        // ignore theRequestId;
        if( theRequestedCanceledObjects.size() > 0 ) {
            return false;
        }
        if( theRequestedFirstTimeObjects.size() > 0 ) {
            return false;
        }
        if( theRequestedHomeReplicas.size() > 0 ) {
            return false;
        }
        if( theRequestedLockObjects.size() > 0 ) {
            return false;
        }
        if( theRequestedResynchronizeReplicas.size() > 0 ) {
            return false;
        }
        // do NOT ignore responseID: may acknowledge receipt of incoming message
        if( theResponseId != 0 ) {
            return false;
        }
        if( theRoleAdditions.size() > 0 ) {
            return false;
        }
        if( theRoleRemovals.size() > 0 ) {
            return false;
        }
        // ignore theSenderIdentifier;        
        if( theTypeAdditions.size() > 0 ) {
            return false;
        }
        if( theTypeRemovals.size() > 0 ) {
            return false;
        }
        return true;
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
                    "theSenderIdentifier",
                    "theReceiverIdentifier",
                    "theRequestId",
                    "theResponseId",
                    "theCeaseCommunications",
                    "theConveyedMeshObjects",
                    "theRequestedFirstTimeObjects",
                    "theRequestedCanceledObjects",
                    "theRequestedResynchronizeDependentReplicas",
                    "theRequestedLockObjects",
                    "thePushLockObjects",
                    "theReclaimedLockObjects",
                    "theRequestedHomeReplicas",
                    "thePushHomeReplicas",
                    "theTypeAdditions",
                    "theTypeRemovals",
                    "thePropertyChanges",
                    "theNeighborAdditions",
                    "theNeighborRemovals",
                    "theEquivalentsAdditions",
                    "theEquivalentsRemovals",
                    "theRoleAdditions",
                    "theRoleRemovals",
                    "theDeleteChanges",
                },
                new Object[] {
                    theSenderIdentifier,
                    theReceiverIdentifier,
                    theRequestId,
                    theResponseId,
                    theCeaseCommunications,
                    theConveyedMeshObjects,
                    theRequestedFirstTimeObjects,
                    theRequestedCanceledObjects,
                    theRequestedResynchronizeReplicas,
                    theRequestedLockObjects,
                    thePushLockObjects,
                    theReclaimedLockObjects,
                    theRequestedHomeReplicas,
                    thePushHomeReplicas,
                    theTypeAdditions,
                    theTypeRemovals,
                    thePropertyChanges,
                    theNeighborAdditions,
                    theNeighborRemovals,
                    theEquivalentsAdditions,
                    theEquivalentsRemovals,
                    theRoleAdditions,
                    theRoleRemovals,
                    theDeleteChanges,
                });
    }

    /**
     * The set of MeshObjects that is being conveyed by the sender to the receiver,
     * e.g. in response to a first-time lease requested.
     */
    protected ArrayList<ExternalizedNetMeshObject> theConveyedMeshObjects = new ArrayList<ExternalizedNetMeshObject>();

    /**
     * The set of MeshObjects, identified by their NetMeshObjectAccessSpecifications, for which the
     * sender would like to obtain a first-time lease.
     */
    protected ArrayList<NetMeshObjectAccessSpecification> theRequestedFirstTimeObjects = new ArrayList<NetMeshObjectAccessSpecification>();
    
    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifiers, for which the
     * sender currently has a lease, but whose lease the sender would like to
     * cancel.
     */
    protected ArrayList<NetMeshObjectIdentifier> theRequestedCanceledObjects = new ArrayList<NetMeshObjectIdentifier>();

    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifiers, for which the
     * sender requests a freshening.
     */
    protected ArrayList<NetMeshObjectIdentifier> theRequestedFreshenReplicas = new ArrayList<NetMeshObjectIdentifier>();

    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifiers, that the sender
     * wishes to resynchronize as dependent replicas.
     */
    protected ArrayList<NetMeshObjectIdentifier> theRequestedResynchronizeReplicas = new ArrayList<NetMeshObjectIdentifier>();
    
    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifiers, for which the
     * sender requests the lock.
     */
    protected ArrayList<NetMeshObjectIdentifier> theRequestedLockObjects = new ArrayList<NetMeshObjectIdentifier>();
    
    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifiers, whose lock
     * the sender surrenders to the receiver.
     */
    protected ArrayList<NetMeshObjectIdentifier> thePushLockObjects = new ArrayList<NetMeshObjectIdentifier>();
    
    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifiers, whose lock
     * the sender has forcefully reclaimed.
     */
    protected ArrayList<NetMeshObjectIdentifier> theReclaimedLockObjects = new ArrayList<NetMeshObjectIdentifier>();
    
    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifiers, whose which the
     * sender requests the homeReplica status.
     */
    protected ArrayList<NetMeshObjectIdentifier> theRequestedHomeReplicas = new ArrayList<NetMeshObjectIdentifier>();

    /**
     * The set of MeshObjects, identified by the MeshObjectIdentifiers, whose homeReplica status
     * the sender surrenders to the receiver.
     */
    protected ArrayList<NetMeshObjectIdentifier> thePushHomeReplicas = new ArrayList<NetMeshObjectIdentifier>();
    
    /**
     * The set of TypeAddedEvents that the sender needs to convey to the receiver.
     */
    protected ArrayList<NetMeshObjectTypeAddedEvent> theTypeAdditions = new ArrayList<NetMeshObjectTypeAddedEvent>();
    
    /**
     * The set of TypeRemovedEvents that the sender needs to convey to the receiver.
     */
    protected ArrayList<NetMeshObjectTypeRemovedEvent> theTypeRemovals = new ArrayList<NetMeshObjectTypeRemovedEvent>();

    /**
     * The set of PropertyChanges that the sender needs to convey to the receiver.
     */
    protected ArrayList<NetMeshObjectPropertyChangeEvent> thePropertyChanges = new ArrayList<NetMeshObjectPropertyChangeEvent>();

    /**
     * The set of NeighborAddedEvents that the sender needs to convey to the receiver.
     */
    protected ArrayList<NetMeshObjectNeighborAddedEvent> theNeighborAdditions = new ArrayList<NetMeshObjectNeighborAddedEvent>();
    
    /**
     * The set of NeighborAddedEvents that the sender needs to convey to the receiver.
     */
    protected ArrayList<NetMeshObjectNeighborRemovedEvent> theNeighborRemovals = new ArrayList<NetMeshObjectNeighborRemovedEvent>();
    
    /**
     * The set of EquivalentsAddedEvents that the sender needs to convey to the receiver.
     */
    protected ArrayList<NetMeshObjectEquivalentsAddedEvent> theEquivalentsAdditions = new ArrayList<NetMeshObjectEquivalentsAddedEvent>();
    
    /**
     * The set of EquivalentsRemovedEvents that the sender needs to convey to the receiver.
     */
    protected ArrayList<NetMeshObjectEquivalentsRemovedEvent> theEquivalentsRemovals = new ArrayList<NetMeshObjectEquivalentsRemovedEvent>();
    
    /**
     * The set of RoleChanges that the sender needs to convey to the receiver.
     */
    protected ArrayList<NetMeshObjectRoleAddedEvent> theRoleAdditions = new ArrayList<NetMeshObjectRoleAddedEvent>();
    
    /**
     * The set of RoleChanges that the sender needs to convey to the receiver.
     */
    protected ArrayList<NetMeshObjectRoleRemovedEvent> theRoleRemovals = new ArrayList<NetMeshObjectRoleRemovedEvent>();
    
    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifier, that have been
     * deleted semantically by the sender, and of whose deletion the receiver
     * needs to be notified.
     */
    protected ArrayList<NetMeshObjectDeletedEvent> theDeleteChanges = new ArrayList<NetMeshObjectDeletedEvent>();
}
