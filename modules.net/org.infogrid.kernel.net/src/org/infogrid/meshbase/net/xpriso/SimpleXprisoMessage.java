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

import java.io.Serializable;
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
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * This implementation of XprisoMessage is fully initialized in the
 * factory method.
 */
public class SimpleXprisoMessage
        extends
            AbstractXprisoMessage
        implements
            XprisoMessage,
            CanBeDumped,
            Serializable
{
    private static final Log  log              = Log.getLogInstance( SimpleXprisoMessage.class ); // our own, private logger
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Create a blank SimpleXprisoMessage.
     * 
     * @param sender identifies the sender of this message
     * @param receiver identifies the receiver of this message
     * @return the created SimpleXprisoMessage
     */
    public static SimpleXprisoMessage create(
            NetMeshBaseIdentifier sender,
            NetMeshBaseIdentifier receiver )
    {
        SimpleXprisoMessage ret = new SimpleXprisoMessage( sender, receiver );

        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( SimpleXprisoMessage.class, "create" );
        }
        return ret;
    }

    /**
     * Constructor.
     * 
     * @param sender identifies the sender of this message
     * @param receiver identifies the receiver of this message
     */
    protected SimpleXprisoMessage(
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
     * Set the externalized representation of the NetMeshObjects that are conveyed
     * by the sender to the receiver, e.g. in response to a first-time lease request.
     *
     * @param newValue the ExternalizedNetMeshObjects
     */
    public void setConveyedMeshObjects(
            ExternalizedNetMeshObject [] newValue )
    {
        theConveyedMeshObjects = newValue;
    }

    /**
     * Obtain the externalized representation of the NetMeshObjects that are conveyed
     * by the sender to the receiver, e.g. in response to a first-time lease request.
     *
     * @return the ExternalizedNetMeshObjects
     */
    public ExternalizedNetMeshObject[] getConveyedMeshObjects()
    {
        return theConveyedMeshObjects;
    }
    
    /**
     * Set the NetMeshObjectAccessSpecifications to the NetMeshObjects for which the sender requests
     * a lease for the first time.
     *
     * @param newValue the NetMeshObjectAccessSpecifications for the NetMeshObjects
     */
    public void setRequestedFirstTimeObjects(
            NetMeshObjectAccessSpecification [] newValue )
    {
        theRequestedFirstTimeObjects = newValue;
    }
    
    /**
     * Obtain the NetMeshObjectAccessSpecifications to the NetMeshObjects for which the sender requests
     * a lease for the first time.
     *
     * @return the NetMeshObjectAccessSpecifications for the NetMeshObjects
     */
    public NetMeshObjectAccessSpecification [] getRequestedFirstTimeObjects()
    {
        return theRequestedFirstTimeObjects;
    }

    /**
     * Set the identifiers for the NetMeshObjects for which the sender requests
     * that a currently valid lease be canceled.
     *
     * @param newValue the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public void setRequestedCanceledObjects(
            NetMeshObjectIdentifier [] newValue )
    {
        theRequestedCanceledObjects = newValue;
    }
    
    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * that a currently valid lease be canceled.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedCanceledObjects()
    {
        return theRequestedCanceledObjects;
    }

    /**
     * Set the identifiers for the NetMeshObjects for which the sender requests
     * a freshening.
     *
     * @param newValue the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public void setRequestedFreshenReplicas(
            NetMeshObjectIdentifier [] newValue )
    {
        theRequestedFreshenReplicas = newValue;
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * a freshening.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedFreshenReplicas()
    {
        return theRequestedFreshenReplicas;
    }

    /**
     * Set the identifiers for the NetMeshObjects for which the sender has a replica
     * that it wishes to resynchronize.
     *
     * @param newValue the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public void setRequestedResynchronizeReplicas(
            NetMeshObjectIdentifier [] newValue )
    {
        theRequestedResynchronizeReplicas = newValue;
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender has a replica
     * that it wishes to resynchronize.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedResynchronizeReplicas()
    {
        return theRequestedResynchronizeReplicas;
    }

    /**
     * Set the identifiers for the NetMeshObjects for which the sender requests
     * the lock from the receiver (i.e. update rights).
     *
     * @param newValue the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public void setRequestedLockObjects(
            NetMeshObjectIdentifier [] newValue )
    {
        theRequestedLockObjects = newValue;
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * the lock from the receiver (i.e. update rights).
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedLockObjects()
    {
        return theRequestedLockObjects;
    }

    /**
     * Set the identifiers for the NetMeshObjects for which the sender surrenders
     * the lock to the receiver (i.e. update rights).
     *
     * @param newValue the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public void setPushLockObjects(
            NetMeshObjectIdentifier [] newValue )
    {
        thePushLockObjects = newValue;
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender surrenders
     * the lock to the receiver (i.e. update rights).
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getPushLockObjects()
    {
        return thePushLockObjects;
    }
    
    /**
     * Set the identifiers for the NetMeshObjects for which the sender has forcefully
     * reclaimed the lock.
     *
     * @param newValue the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public void setReclaimedLockObjects(
            NetMeshObjectIdentifier [] newValue )
    {
        theReclaimedLockObjects = newValue;
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender has forcefully
     * reclaimed the lock.
     *
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getReclaimedLockObjects()
    {
        return theReclaimedLockObjects;
    }
    
    /**
     * Set the identifiers for the NetMeshObjects for which the sender requests
     * home replica status.
     * 
     * @param newValue the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public void setRequestedHomeReplicas(
            NetMeshObjectIdentifier [] newValue )
    {
        theRequestedHomeReplicas = newValue;
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender requests
     * home replica status.
     * 
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getRequestedHomeReplicas()
    {
        return theRequestedHomeReplicas;
    }

    /**
     * Set the identifiers for the NetMeshObjects for which the sender surrenders
     * the home replica status to the receiver.
     * 
     * @param newValue the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public void setPushHomeReplicas(
            NetMeshObjectIdentifier [] newValue )
    {
        thePushHomeReplicas = newValue;
    }

    /**
     * Obtain the identifiers for the NetMeshObjects for which the sender surrenders
     * the home replica status to the receiver.
     * 
     * @return the NetMeshObjectIdentifiers for the NetMeshObjects
     */
    public NetMeshObjectIdentifier [] getPushHomeReplicas()
    {
        return thePushHomeReplicas;
    }
    
    /**
     * Set the type addition events that the sender needs to convey to the
     * receiver.
     *
     * @param newValue the type addition events
     */
    public void setTypeAdditions(
            NetMeshObjectTypeAddedEvent [] newValue )
    {
        theTypeAdditions = newValue;
    }

    /**
     * Obtain the type addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the type addition events
     */
    public NetMeshObjectTypeAddedEvent [] getTypeAdditions()
    {
        return theTypeAdditions;
    }

    /**
     * Set the type removal events that the sender needs to convey to the
     * receiver.
     *
     * @param newValue the type removal events
     */
    public void setTypeRemovals(
            NetMeshObjectTypeRemovedEvent [] newValue )
    {
        theTypeRemovals = newValue;
    }

    /**
     * Obtain the type removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the type removal events
     */
    public NetMeshObjectTypeRemovedEvent [] getTypeRemovals()
    {
        return theTypeRemovals;
    }

    /**
     * Set the property change events that the sender needs to convey to the
     * received.
     *
     * @param newValue the property change events
     */
    public void setPropertyChanges(
            NetMeshObjectPropertyChangeEvent [] newValue )
    {
        thePropertyChanges = newValue;
    }

    /**
     * Obtain the property change events that the sender needs to convey to the
     * receiver.
     *
     * @return the property change events
     */
    public NetMeshObjectPropertyChangeEvent [] getPropertyChanges()
    {
        return thePropertyChanges;
    }

    /**
     * Set the neighbor addition events that the sender needs to convey to the
     * receiver.
     *
     * @param newValue the neighbor addition events
     */
    public void setNeighborAdditions(
            NetMeshObjectNeighborAddedEvent [] newValue )
    {
        theNeighborAdditions = newValue;
    }

    /**
     * Obtain the neighbor addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the neighbor addition events
     */
    public NetMeshObjectNeighborAddedEvent [] getNeighborAdditions()
    {
        return theNeighborAdditions;
    }

    /**
     * Set the neighbor removal events that the sender needs to convey to the
     * receiver.
     *
     * @param newValue the neighbor removal events
     */
    public void setNeighborRemovals(
            NetMeshObjectNeighborRemovedEvent [] newValue )
    {
        theNeighborRemovals = newValue;
    }

    /**
     * Obtain the neighbor removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the neighbor removal events
     */
    public NetMeshObjectNeighborRemovedEvent [] getNeighborRemovals()
    {
        return theNeighborRemovals;
    }

    /**
     * Set the equivalent addition events that the sender needs to convey to the
     * receiver.
     * 
     * @param newValue the equivalent addition events
     */
    public void setEquivalentAdditions(
            NetMeshObjectEquivalentsAddedEvent [] newValue )
    {
        theEquivalentAdditions = newValue;
    }

    /**
     * Obtain the equivalent addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the equivalent addition events
     */
    public NetMeshObjectEquivalentsAddedEvent [] getEquivalentsAdditions()
    {
        return theEquivalentAdditions;
    }

    /**
     * Set the equivalent removal events that the sender needs to convey to the
     * receiver.
     * 
     * @param newValue the equivalent removal events
     */
    public void setEquivalentRemovals(
            NetMeshObjectEquivalentsRemovedEvent [] newValue )
    {
        theEquivalentRemovals = newValue;
    }

    /**
     * Obtain the equivalent removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the equivalent removal events
     */
    public NetMeshObjectEquivalentsRemovedEvent [] getEquivalentsRemovals()
    {
        return theEquivalentRemovals;
    }

    /**
     * Set the role addition events that the sender needs to convey to the
     * receiver.
     *
     * @param newValue the role addition events
     */
    public void setRoleAdditions(
            NetMeshObjectRoleAddedEvent [] newValue )
    {
        theRoleAdditions = newValue;
    }

    /**
     * Obtain the role addition events that the sender needs to convey to the
     * receiver.
     *
     * @return the role addition events
     */
    public NetMeshObjectRoleAddedEvent [] getRoleAdditions()
    {
        return theRoleAdditions;
    }

    /**
     * Set the role removal events that the sender needs to convey to the
     * receiver.
     *
     * @param newValue the role removal events
     */
    public void setRoleRemovals(
            NetMeshObjectRoleRemovedEvent [] newValue )
    {
        theRoleRemovals = newValue;
    }

    /**
     * Obtain the role removal events that the sender needs to convey to the
     * receiver.
     *
     * @return the role removal events
     */
    public NetMeshObjectRoleRemovedEvent [] getRoleRemovals()
    {
        return theRoleRemovals;
    }

    /**
     * Set the deletion events for the NetMeshObjects that the sender has deleted
     * semantically and that the sender needs to convey to the receiver.
     *
     * @param newValue the deletion events
     */
    public void setDeleteChanges(
            NetMeshObjectDeletedEvent [] newValue )
    {
        theDeleteChanges = newValue;
    }

    /**
     * Obtain the deletion events for the NetMeshObjects that the sender has deleted
     * semantically and that the sender needs to convey to the receiver.
     *
     * @return the deletion events
     */
    public NetMeshObjectDeletedEvent [] getDeletions()
    {
        return theDeleteChanges;
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
        if( theConveyedMeshObjects != null && theConveyedMeshObjects.length > 0 ) {
            return false;
        }
        if( theDeleteChanges != null && theDeleteChanges.length > 0 ) {
            return false;
        }
        if( theEquivalentAdditions != null && theEquivalentAdditions.length > 0 ) {
            return false;
        }
        if( theEquivalentRemovals != null && theEquivalentRemovals.length > 0 ) {
            return false;
        }
        if( theNeighborAdditions != null && theNeighborAdditions.length > 0 ) {
            return false;
        }
        if( theNeighborRemovals != null && theNeighborRemovals.length > 0 ) {
            return false;
        }
        if( thePropertyChanges != null && thePropertyChanges.length > 0 ) {
            return false;
        }
        if( thePushHomeReplicas != null && thePushHomeReplicas.length > 0 ) {
            return false;
        }
        if( thePushLockObjects != null && thePushLockObjects.length > 0 ) {
            return false;
        }
        // ignore theReceiverIdentifier;
        if( theReclaimedLockObjects != null && theReclaimedLockObjects.length > 0 ) {
            return false;
        }
        // ignore theRequestId;
        if( theRequestedCanceledObjects != null && theRequestedCanceledObjects.length > 0 ) {
            return false;
        }
        if( theRequestedFirstTimeObjects != null && theRequestedFirstTimeObjects.length > 0 ) {
            return false;
        }
        if( theRequestedHomeReplicas != null && theRequestedHomeReplicas.length > 0 ) {
            return false;
        }
        if( theRequestedLockObjects != null && theRequestedLockObjects.length > 0 ) {
            return false;
        }
        if( theRequestedResynchronizeReplicas != null && theRequestedResynchronizeReplicas.length > 0 ) {
            return false;
        }
        if( theResponseId != 0 ) {
            return false;
        }
        if( theRoleAdditions != null && theRoleAdditions.length > 0 ) {
            return false;
        }
        if( theRoleRemovals != null && theRoleRemovals.length > 0 ) {
            return false;
        }
        // ignore theSenderIdentifier;
        if( theTypeAdditions != null && theTypeAdditions.length > 0 ) {
            return false;
        }
        if( theTypeRemovals != null && theTypeRemovals.length > 0 ) {
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
                    "theEquivalentAdditions",
                    "theEquivalentRemovals",
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
                    theEquivalentAdditions,
                    theEquivalentRemovals,
                    theRoleAdditions,
                    theRoleRemovals,
                    theDeleteChanges,
                });
    }

    /**
     * The set of MeshObjects that is being conveyed by the sender to the receiver,
     * e.g. in response to a first-time lease requested.
     */
    protected ExternalizedNetMeshObject[] theConveyedMeshObjects = {};

    /**
     * The set of MeshObjects, identified by a NetMeshObjectAccessSpecification, for which the
     * sender would like to obtain a first-time lease.
     */
    protected NetMeshObjectAccessSpecification[] theRequestedFirstTimeObjects = NetMeshObjectAccessSpecification.EMPTY_ARRAY;
    
    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifier, for which the
     * sender currently as a lease, but whose lease the sender would like to
     * cancel.
     */
    protected NetMeshObjectIdentifier [] theRequestedCanceledObjects = NetMeshObjectIdentifier.NET_EMPTY_ARRAY;

    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifier, for which the
     * sender requests a freshening.
     */
    protected NetMeshObjectIdentifier [] theRequestedFreshenReplicas = NetMeshObjectIdentifier.NET_EMPTY_ARRAY;

    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifier, that the sender
     * wishes to resynchronize as dependent replicas.
     */
    protected NetMeshObjectIdentifier [] theRequestedResynchronizeReplicas = NetMeshObjectIdentifier.NET_EMPTY_ARRAY;

    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifier, for which the
     * sender requests the lock.
     */
    protected NetMeshObjectIdentifier [] theRequestedLockObjects = NetMeshObjectIdentifier.NET_EMPTY_ARRAY;
    
    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifier, whose lock
     * the sender surrenders to the receiver.
     */
    protected NetMeshObjectIdentifier [] thePushLockObjects = NetMeshObjectIdentifier.NET_EMPTY_ARRAY;
    
    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifier, whose lock
     * the sender has forcefully reclaimed.
     */
    protected NetMeshObjectIdentifier [] theReclaimedLockObjects = NetMeshObjectIdentifier.NET_EMPTY_ARRAY;

    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifier, whose which the
     * sender requests the homeReplica status.
     */
    protected NetMeshObjectIdentifier [] theRequestedHomeReplicas = NetMeshObjectIdentifier.NET_EMPTY_ARRAY;

    /**
     * The set of MeshObjects, identified by the MeshObjectIdentifier, whose homeReplica status
     * the sender surrenders to the receiver.
     */
    protected NetMeshObjectIdentifier [] thePushHomeReplicas = NetMeshObjectIdentifier.NET_EMPTY_ARRAY;

    /**
     * The set of TypeAddedEvents that the sender needs to convey to the receiver.
     */
    protected NetMeshObjectTypeAddedEvent [] theTypeAdditions = {};
    
    /**
     * The set of TypeRemovedEvents that the sender needs to convey to the receiver.
     */
    protected NetMeshObjectTypeRemovedEvent [] theTypeRemovals = {};
    
    /**
     * The set of PropertyChanges that the sender needs to convey to the receiver.
     */
    protected NetMeshObjectPropertyChangeEvent [] thePropertyChanges = {};
    
    /**
     * The set of NeighborAddedEvents that the sender needs to convey to the receiver.
     */
    protected NetMeshObjectNeighborAddedEvent [] theNeighborAdditions = {};
    
    /**
     * The set of NeighborAddedEvents that the sender needs to convey to the receiver.
     */
    protected NetMeshObjectNeighborRemovedEvent [] theNeighborRemovals = {};
    
    /**
     * The set of EquivalentsAddedEvents that the sender needs to convey to the receiver.
     */
    protected NetMeshObjectEquivalentsAddedEvent [] theEquivalentAdditions = {};

    /**
     * The set of EquivalentsRemovedEvents that the sender needs to convey to the receiver.
     */
    protected NetMeshObjectEquivalentsRemovedEvent [] theEquivalentRemovals = {};

    /**
     * The set of RoleChanges that the sender needs to convey to the receiver.
     */
    protected NetMeshObjectRoleAddedEvent [] theRoleAdditions = {};
    
    /**
     * The set of RoleChanges that the sender needs to convey to the receiver.
     */
    protected NetMeshObjectRoleRemovedEvent [] theRoleRemovals = {};
    
    /**
     * The set of MeshObjects, identified by their MeshObjectIdentifier, that have been
     * deleted semantically by the sender, and of whose deletion the receiver
     * needs to be notified.
     */
    protected NetMeshObjectDeletedEvent [] theDeleteChanges = {};
    
}
