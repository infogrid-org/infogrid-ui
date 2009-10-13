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

package org.infogrid.meshbase.net.transaction;

import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.transaction.CannotApplyChangeException;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.CreateWhenNeeded;
import org.infogrid.util.logging.Log;

/**
 * A NetMeshObjectCreatedEvent that is used for ForwardReferences.
 */
public class ForwardReferenceCreatedEvent
        extends
            NetMeshObjectCreatedEvent
{
    private static final Log  log              = Log.getLogInstance( ForwardReferenceCreatedEvent.class ); // our own, private logger
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param pathToObject identifies the data source where the MeshObject can be found
     * @param createdObject the NetMeshObject that was created
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     */
    public ForwardReferenceCreatedEvent(
            NetMeshBase                      source,
            NetMeshBaseIdentifier            sourceIdentifier,
            NetMeshObjectAccessSpecification pathToObject,
            NetMeshObject                    createdObject,
            NetMeshBaseIdentifier            originIdentifier )
    {
        super( source, sourceIdentifier, createdObject, originIdentifier );

        thePathToObject = pathToObject;
    }

    /**
     * Constructor.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param pathToObject identifies the data source where the MeshObject can be found
     * @param createdObject the NetMeshObject that was created, in externalized form
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     */
    public ForwardReferenceCreatedEvent(
            NetMeshBase                      source,
            NetMeshBaseIdentifier            sourceIdentifier,
            NetMeshObjectAccessSpecification pathToObject,
            ExternalizedNetMeshObject        createdObject,
            NetMeshBaseIdentifier            originIdentifier )
    {
        super( source, sourceIdentifier, createdObject, originIdentifier );

        thePathToObject = pathToObject;
    }

    /**
     * Constructor.
     *
     * @param source the NetMeshBase that is the source of the event
     * @param sourceIdentifier the NetMeshBaseIdentifier representing the source of the event
     * @param pathToObject identifies the data source where the MeshObject can be found
     * @param createdObject the NetMeshObject that was created
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public ForwardReferenceCreatedEvent(
            NetMeshBase                      source,
            NetMeshBaseIdentifier            sourceIdentifier,
            NetMeshObjectAccessSpecification pathToObject,
            NetMeshObject                    createdObject,
            NetMeshBaseIdentifier            originIdentifier,
            long                             timeEventOccurred )
    {
        super( source, sourceIdentifier, createdObject, originIdentifier, timeEventOccurred );

        thePathToObject = pathToObject;
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
    @Override
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

            NetMeshObject newObject = base.getMeshBaseLifecycleManager().createForwardReference(
                        thePathToObject,
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
     * Overridable helper method to actually instantiate the MeshObject.
     *
     * @param base the MeshBase in which to apply the Change
     * @return the MeshObject to which the Change was applied
     * @throws CannotApplyChangeException thrown if the Change could not be applied, e.g because
     *         the affected MeshObject did not exist in MeshBase base
     * @throws IsAbstractException thrown if at least one of the EntityTypes was abstract
     * @throws MeshObjectIdentifierNotUniqueException thrown if a MeshObject with this identifier exists already
     * @throws NotPermittedException thrown if the caller did not have sufficient permissions for this operation
     * @throws TransactionException thrown if a Transaction didn't exist on this Thread and
     *         could not be created
     */
    @Override
    protected MeshObject instantiateMeshObject(
            MeshBase base )
        throws
            CannotApplyChangeException,
            IsAbstractException,
            MeshObjectIdentifierNotUniqueException,
            NotPermittedException,
            TransactionException
    {
        MeshObject ret = ((NetMeshBase)base).getMeshBaseLifecycleManager().createForwardReference(
                        thePathToObject,
                        getEntityTypes(),
                        theExternalizedMeshObject.getTimeCreated(),
                        theExternalizedMeshObject.getTimeUpdated(),
                        theExternalizedMeshObject.getTimeRead(),
                        theExternalizedMeshObject.getTimeExpires() );

        return ret;
    }

    /**
     * The location of the NetMeshObject into which the ForwardReference resolves.
     */
    protected NetMeshObjectAccessSpecification thePathToObject;
}
