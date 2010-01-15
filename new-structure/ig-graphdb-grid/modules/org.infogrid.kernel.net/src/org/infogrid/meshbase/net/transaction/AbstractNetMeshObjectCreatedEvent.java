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

package org.infogrid.meshbase.net.transaction;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.transaction.CannotApplyChangeException;
import org.infogrid.meshbase.transaction.MeshObjectCreatedEvent;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.CreateWhenNeeded;
import org.infogrid.util.logging.Log;

/**
 * Abstract superclass to hold the common behavior of NetMeshObjectCreatedEvent
 * and ReplicaCreatedEvent.
 */
public abstract class AbstractNetMeshObjectCreatedEvent
        extends
            MeshObjectCreatedEvent
        implements
            NetChange<MeshBase,MeshBaseIdentifier,MeshObject,MeshObjectIdentifier>
{
    private static final Log log = Log.getLogInstance( AbstractNetMeshObjectCreatedEvent.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param createdObject the NetMeshObject that was created
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     */
    public AbstractNetMeshObjectCreatedEvent(
            NetMeshBase           source,
            NetMeshBaseIdentifier sourceIdentifier,
            NetMeshObject         createdObject,
            NetMeshBaseIdentifier originIdentifier )
    {
        super( source, sourceIdentifier, createdObject, createdObject.getTimeCreated() );
        
        theOriginNetworkIdentifier = originIdentifier;
    }

    /**
     * Constructor.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param createdObject the NetMeshObject that was created, in externalized form
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     */
    public AbstractNetMeshObjectCreatedEvent(
            NetMeshBase               source,
            NetMeshBaseIdentifier     sourceIdentifier,
            ExternalizedNetMeshObject createdObject,
            NetMeshBaseIdentifier     originIdentifier )
    {
        super( source, sourceIdentifier, createdObject, createdObject.getTimeCreated() );
        
        theOriginNetworkIdentifier = originIdentifier;
    }

    /**
     * Constructor for subclasses only.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param createdObject the NetMeshObject that was created
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    protected AbstractNetMeshObjectCreatedEvent(
            NetMeshBase           source,
            NetMeshBaseIdentifier sourceIdentifier,
            NetMeshObject         createdObject,
            NetMeshBaseIdentifier originIdentifier,
            long                  timeEventOccurred )
    {
        super( source, sourceIdentifier, createdObject, timeEventOccurred );
        
        theOriginNetworkIdentifier = originIdentifier;
    }

    /**
     * Obtain the MeshObject affected by this Change.
     *
     * @return obtain the MeshObject affected by this Change
     */
    @Override
    public NetMeshObject getAffectedMeshObject()
    {
        return (NetMeshObject) super.getAffectedMeshObject();
    }

    /**
     * Obtain the MeshObjectIdentifier of the MeshObject affected by this Change.
     *
     * @return the MeshObjectIdentifier of the NetMeshObject affected by this Change
     */
    @Override
    public NetMeshObjectIdentifier getAffectedMeshObjectIdentifier()
    {
        return (NetMeshObjectIdentifier) super.getAffectedMeshObjectIdentifier();
    }

    /**
     * Obtain the ExternalizedMeshObject that captures the newly created MeshObject.
     *
     * @return the ExternalizedMeshObject
     */
    @Override
    public ExternalizedNetMeshObject getExternalizedMeshObject()
    {
        return (ExternalizedNetMeshObject) theExternalizedMeshObject;
    }

    /**
     * <p>Apply this NetChange to a NetMeshObject in this MeshBase. This method
     *    is intended to make it easy to replicate NetChanges that were made to a
     *    replica of one NetMeshObject in one NetMeshBase to another replica
     *    of the NetMeshObject in another NetMeshBase.</p>
     *
     * <p>This method will attempt to create a Transaction if none is present on the
     * current Thread.</p>
     *
     * @param base the NetMeshBase in which to apply the NetChange
     * @param perhapsTx the Transaction to use, if any
     * @param incomingProxy the Proxy through which this NetChange was received
     * @return the NetMeshObject to which the NetChange was applied
     * @throws CannotApplyChangeException thrown if the NetChange could not be applied, e.g because
     *         the affected NetMeshObject did not exist in MeshBase base
     * @throws TransactionException thrown if a Transaction didn't exist on this Thread and
     *         could not be created
     */
    public NetMeshObject potentiallyApplyToReplicaIn(
            NetMeshBase                   base,
            CreateWhenNeeded<Transaction> perhapsTx,
            Proxy                         incomingProxy )
        throws
            CannotApplyChangeException,
            TransactionException
    {
        setResolver( base );

        ModelBase modelBase = base.getModelBase();
        resolveEntityTypes( this, theExternalizedMeshObject, theResolver );
        
        PropertyType [] thePropertyTypes = new PropertyType[ theExternalizedMeshObject.getPropertyTypes().length ];
        for( int i=0 ; i<thePropertyTypes.length ; ++i ) {
            try {
                thePropertyTypes[i] = modelBase.findPropertyTypeByIdentifier( theExternalizedMeshObject.getPropertyTypes()[i] );

            } catch( MeshTypeWithIdentifierNotFoundException ex ) {
                log.error( ex );
            }
        }
            
        try {
            perhapsTx.obtain(); // can ignore return value

            NetMeshObject newObject = base.getMeshBaseLifecycleManager().createMeshObject(
                        getAffectedMeshObjectIdentifier(),
                        theEntityTypes,
                        theExternalizedMeshObject.getTimeCreated(),
                        theExternalizedMeshObject.getTimeUpdated(),
                        theExternalizedMeshObject.getTimeRead(),
                        theExternalizedMeshObject.getTimeExpires() );

            newObject.rippleSetPropertyValues( thePropertyTypes, theExternalizedMeshObject.getPropertyValues(), getTimeEventOccurred() );

            return newObject;

        } catch( TransactionException ex ) {
            throw ex;

        } catch( Throwable ex ) {
            throw new CannotApplyChangeException.ExceptionOccurred( base, ex );
        }
    }

    /**
     * Determine whether this NetChange should be forwarded through the given, outgoing Proxy.
     * If specified, {@link #getOriginNetworkIdentifier} specifies where the NetChange came from.
     *
     * @param outgoingProxy the potential outgoing Proxy
     * @return true if the NetChange should be forwarded torwards the outgoingProxy
     */
    public boolean shouldBeSent(
            Proxy outgoingProxy )
    {
        return false;
    }

    /**
     * Obtain the NetMeshBaseIdentifier of the NetMeshBase from where this NetChange arrived.
     * This may or may not be the NetMeshBase where the Change originated, as it might be
     * passed through several NetMeshBases until it arrived here. This may be null if
     * the Change originated locally.
     *
     * @return the NetMeshBaseIdentifier, if any
     */
    public final NetMeshBaseIdentifier getOriginNetworkIdentifier()
    {
        return theOriginNetworkIdentifier;
    }

    /**
     * The NetMeshBaseIdentifier of the NetMeshBase from where this NetChange arrived.
     * This may or may not be the NetMeshBase where the Change originated, as it might be
     * passed through several NetMeshBases until it arrived here. This may be null if
     * the Change originated locally.
     */
    protected NetMeshBaseIdentifier theOriginNetworkIdentifier;
}
