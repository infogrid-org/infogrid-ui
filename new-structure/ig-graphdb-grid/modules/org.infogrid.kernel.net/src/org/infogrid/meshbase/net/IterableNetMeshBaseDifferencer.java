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

package org.infogrid.meshbase.net;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.externalized.ExternalizedMeshObject;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.meshbase.IterableMeshBaseDifferencer;
import org.infogrid.meshbase.net.transaction.NetMeshObjectCreatedEvent;
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
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RoleType;

/**
 * This subclasses IterableMeshBaseDifferencer to take account of the specifics of
 * NetMeshBases.
 */
public class IterableNetMeshBaseDifferencer
        extends
            IterableMeshBaseDifferencer
{
    /**
     * Constructor.
     *
     * @param baseline the baseline IterableNetMeshBase against which we compare.
     */
    public IterableNetMeshBaseDifferencer(
            IterableNetMeshBase baseline )
    {
        super( baseline );
    }

    /**
     * Helper method to create an array of MeshObjectIdentifier from an array of
     * MeshObjects.
     * 
     * @param objs the MeshObjects
     * @return the MeshObjectIdentifiers
     */
    @Override
    protected MeshObjectIdentifier [] asIdentifiers(
            MeshObject [] objs )
    {
        NetMeshObjectIdentifier [] ret = new NetMeshObjectIdentifier[ objs.length ];
        for( int i=0 ; i<objs.length ; ++i ) {
            ret[i] = (NetMeshObjectIdentifier) objs[i].getIdentifier();
        }
        return ret;
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     * 
     * @param obj the MeshObject that was created
     * @param time the time at which the creation occurred
     * @return the MeshObjectCreatedEvent or subclass
     */
    @Override
    protected NetMeshObjectCreatedEvent createMeshObjectCreatedEvent(
            MeshObject             obj,
            long                   time )
    {
        NetMeshObjectCreatedEvent ret = new NetMeshObjectCreatedEvent(
                (NetMeshBase) obj.getMeshBase(),
                (NetMeshBaseIdentifier) obj.getMeshBase().getIdentifier(),
                (NetMeshObject) obj,
                null,
                time );

        return ret;
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     * 
     * @param canonicalIdentifier the canonical Identifier of the MeshObject that was deleted
     * @param obj the MeshObject that was deleted
     * @param externalized external form of the deleted MeshObject
     * @return the MeshObjectDeletedEvent or subclass
     */
    @Override
    protected NetMeshObjectDeletedEvent createMeshObjectDeletedEvent(
            MeshObject             obj,
            MeshObjectIdentifier   canonicalIdentifier,
            ExternalizedMeshObject externalized,
            long                   time )
    {
        NetMeshObjectDeletedEvent ret = new NetMeshObjectDeletedEvent(
                (NetMeshBase) obj.getMeshBase(),
                (NetMeshBaseIdentifier) obj.getMeshBase().getIdentifier(),
                (NetMeshObject) obj,
                canonicalIdentifier,
                (NetMeshBaseIdentifier) null,
                externalized,
                time );

        return ret;
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     *
     * @param meshObjectInBase the MeshObject in the baseline
     * @param meshObjectInComparison the MeshObject in the comparison
     * @param addedTypes the added EntityTypes
     * @return the MeshObjectTypeAddedEvent or subclass
     */
    @Override
    protected NetMeshObjectTypeAddedEvent createMeshObjectTypesAddedEvent(
            MeshObject    meshObjectInBase,
            MeshObject    meshObjectInComparison,
            EntityType [] oldTypes,
            EntityType [] addedTypes,
            EntityType [] newTypes )
    {
        NetMeshObjectTypeAddedEvent ret = new NetMeshObjectTypeAddedEvent(
                (NetMeshObject) meshObjectInBase,
                oldTypes,
                addedTypes,
                newTypes,
                (NetMeshBaseIdentifier) null,
                meshObjectInComparison.getTimeUpdated() );

        return ret;
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     *
     * @param meshObjectInBase the MeshObject in the baseline
     * @param meshObjectInComparison the MeshObject in the comparison
     * @param removedTypes the removed EntityTypes
     * @return the MeshObjectTypeRemovedEvent or subclass
     */
    @Override
    protected NetMeshObjectTypeRemovedEvent createMeshObjectTypesRemovedEvent(
            MeshObject    meshObjectInBase,
            MeshObject    meshObjectInComparison,
            EntityType [] oldTypes,
            EntityType [] removedTypes,
            EntityType [] newTypes )
    {
        NetMeshObjectTypeRemovedEvent ret = new NetMeshObjectTypeRemovedEvent(
                (NetMeshObject) meshObjectInBase,
                oldTypes,
                removedTypes,
                newTypes,
                (NetMeshBaseIdentifier) null,
                meshObjectInComparison.getTimeUpdated() );

        return ret;
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     *
     * @param meshObjectInBase the MeshObject in the baseline
     * @param meshObjectInComparison the MeshObject in the comparison
     * @param propertyType the PropertyType that was changed
     * @param oldValue the value before the change
     * @param newValue the value after the change
     * @return the MeshObjectPropertyChangeEvent or subclass
     */
    @Override
    protected NetMeshObjectPropertyChangeEvent createMeshObjectPropertyChangeEvent(
            MeshObject    meshObjectInBase,
            MeshObject    meshObjectInComparison,
            PropertyType  propertyType,
            PropertyValue oldValue,
            PropertyValue newValue )
    {
        NetMeshObjectPropertyChangeEvent ret = new NetMeshObjectPropertyChangeEvent(
                (NetMeshObject) meshObjectInBase,
                propertyType,
                oldValue,
                newValue,
                (NetMeshBaseIdentifier) null,
                meshObjectInComparison.getTimeUpdated() );

        return ret;
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     *
     * @param meshObjectInBase the MeshObject in the baseline
     * @param meshObjectInComparison the MeshObject in the comparison
     * @param otherSidesInBase the MeshObjectSet of "other sides" in the baseline
     * @param addedNeighborInComparison the MeshObject that was added as a neighbor
     * @param otherSidesInComparison the MeshObjectSet of "other sides" in the comparison
     * @param addedRoleTypes the RoleTypes that the new neighbor relationship was blessed with
     * @return the MeshObjectNeighborAddedEvent or subclass
     */
    @Override
    protected NetMeshObjectNeighborAddedEvent createMeshObjectNeighborAddedEvent(
            MeshObject    meshObjectInBase,
            MeshObject    meshObjectInComparison,
            MeshObjectSet otherSidesInBase,
            MeshObject    addedNeighborInComparison,
            MeshObjectSet otherSidesInComparison,
            RoleType []   addedRoleTypes )
    {
        NetMeshObjectNeighborAddedEvent ret = new NetMeshObjectNeighborAddedEvent(
                (NetMeshObject) meshObjectInBase,
                addedRoleTypes,
                (NetMeshObjectIdentifier []) otherSidesInBase.asIdentifiers(),
                (NetMeshObjectIdentifier)    addedNeighborInComparison.getIdentifier(),
                (NetMeshObjectIdentifier []) otherSidesInComparison.asIdentifiers(),
                (NetMeshBaseIdentifier)      null,
                meshObjectInComparison.getTimeUpdated() );

        return ret;
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     *
     * @param meshObjectInBase the MeshObject in the baseline
     * @param meshObjectInComparison the MeshObject in the comparison
     * @param otherSidesInBase the MeshObjectSet of "other sides" in the baseline
     * @param removedNeighborInBase the MeshObject that was removed as a neighbor
     * @param otherSidesInComparison the MeshObjectSet of "other sides" in the comparison
     * @return the MeshObjectNeighborRemovedEvent or subclass
     */
    @Override
    protected NetMeshObjectNeighborRemovedEvent createMeshObjectNeighborRemovedEvent(
            MeshObject    meshObjectInBase,
            MeshObject    meshObjectInComparison,
            MeshObjectSet otherSidesInBase,
            MeshObject    removedNeighborInBase,
            MeshObjectSet otherSidesInComparison )
    {
        NetMeshObjectNeighborRemovedEvent ret = new NetMeshObjectNeighborRemovedEvent(
                (NetMeshObject) meshObjectInBase,
                (NetMeshObjectIdentifier []) otherSidesInBase.asIdentifiers(),
                (NetMeshObjectIdentifier)    removedNeighborInBase.getIdentifier(),
                (NetMeshObjectIdentifier []) otherSidesInComparison.asIdentifiers(),
                (NetMeshBaseIdentifier)      null,
                meshObjectInComparison.getTimeUpdated() );

        return ret;
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     *
     * @param meshObjectInBase the MeshObject in the baseline
     * @param meshObjectInComparison the MeshObject in the comparison
     * @param addedRoleTypes the RoleTypes that were added
     * @param otherSideInComparison the "other side" MeshObject in the comparison
     * @return the MeshObjectRoleAddedEvent or subclass
     */
    @Override
    protected NetMeshObjectRoleAddedEvent createMeshObjectRoleAddedEvent(
            MeshObject    meshObjectInBase,
            MeshObject    meshObjectInComparison,
            RoleType []   oldRoleTypes,
            RoleType []   addedRoleTypes,
            RoleType []   newRoleTypes,
            MeshObject    otherSideInComparison )
    {
        NetMeshObjectRoleAddedEvent ret = new NetMeshObjectRoleAddedEvent(
                (NetMeshObject) meshObjectInComparison,
                oldRoleTypes,
                addedRoleTypes,
                newRoleTypes,
                (NetMeshObject) otherSideInComparison,
                (NetMeshBaseIdentifier) null,
                meshObjectInComparison.getTimeUpdated() );


        return ret;
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     *
     * @param meshObjectInBase the MeshObject in the baseline
     * @param meshObjectInComparison the MeshObject in the comparison
     * @param removedRoleTypes the RoleTypes that were removed
     * @param otherSideInBase the "other side" MeshObject in the baseline
     * @return the MeshObjectRoleRemovedEvent or subclass
     */
    @Override
    protected NetMeshObjectRoleRemovedEvent createMeshObjectRoleRemovedEvent(
            MeshObject    meshObjectInBase,
            MeshObject    meshObjectInComparison,
            RoleType []   oldRoleTypes,
            RoleType []   removedRoleTypes,
            RoleType []   newRoleTypes,
            MeshObject    otherSideInBase )
    {
        NetMeshObjectRoleRemovedEvent ret = new NetMeshObjectRoleRemovedEvent(
                (NetMeshObject) meshObjectInBase,
                oldRoleTypes,
                removedRoleTypes,
                newRoleTypes,
                (NetMeshObjectIdentifier) otherSideInBase.getIdentifier(),
                (NetMeshBaseIdentifier) null,
                meshObjectInComparison.getTimeUpdated() );

        return ret;
    }

    /**
     * Allow subclasses to instantiate a more specific event.
     *
     * @param meshObjectInBase the MeshObject in the baseline
     * @param meshObjectInComparison the MeshObject in the comparison
     * @param added the MeshObjects that were added as neighbors
     * @param newEquivalents the Identifiers of the resulting set of equivalents
     * @return the MeshObjectDeletedEvent or subclass
     */
    @Override
    protected NetMeshObjectEquivalentsAddedEvent createMeshObjectEquivalentsAddedEvent(
            MeshObject              meshObjectInBase,
            MeshObject              meshObjectInComparison,
            MeshObjectSet           oldEquivalents,
            MeshObjectIdentifier [] added,
            MeshObjectSet           newEquivalents )
    {
        NetMeshObjectEquivalentsAddedEvent ret = new NetMeshObjectEquivalentsAddedEvent(
                (NetMeshObject) meshObjectInBase,
                (NetMeshObjectIdentifier []) oldEquivalents.asIdentifiers(),
                (NetMeshObjectIdentifier []) added,
                (NetMeshObjectIdentifier []) newEquivalents.asIdentifiers(),
                (NetMeshBaseIdentifier) null,
                meshObjectInComparison.getTimeUpdated() );

        return ret;
    }
    
    /**
     * Allow subclasses to instantiate a more specific event.
     *
     * @param meshObjectInBase the MeshObject in the baseline
     * @param meshObjectInComparison the MeshObject in the comparison
     * @param removed the Identifiers of the MeshObjects that were removed as equivalents
     * @param newEquivalents the Identifiers of the resulting set of equivalents
     * @return the MeshObjectEquivalentsRemovedEvent or subclass
     */
    @Override
    protected NetMeshObjectEquivalentsRemovedEvent createMeshObjectEquivalentsRemovedEvent(
            MeshObject              meshObjectInBase,
            MeshObject              meshObjectInComparison,
            MeshObjectSet           oldEquivalents,
            MeshObjectIdentifier [] removed,
            MeshObjectSet           newEquivalents )
    {
        NetMeshObjectEquivalentsRemovedEvent ret = new NetMeshObjectEquivalentsRemovedEvent(
                (NetMeshObject) meshObjectInBase,
                (NetMeshObjectIdentifier []) oldEquivalents.asIdentifiers(),
                (NetMeshObjectIdentifier []) removed,
                (NetMeshObjectIdentifier []) newEquivalents.asIdentifiers(),
                (NetMeshBaseIdentifier) null,
                meshObjectInComparison.getTimeUpdated() );

        return ret;
    }

    /**
     * Helper method to get the type of array right. FIXME there must be a less inefficient
     * way of doing this without disrupting all the code in superclasses.\
     *
     * @param org the original array
     * @return the new array
     */
    protected NetMeshObject [] copyIntoNetMeshObjectArray(
            MeshObject [] org )
    {
        NetMeshObject [] ret = new NetMeshObject[ org.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = (NetMeshObject) org[i];
        }
        return ret;
    }
}
