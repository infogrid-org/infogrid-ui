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

package org.infogrid.mesh.a;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;

/**
 * Abstraction to managers neighbors and associated information
 * in an AMeshObject. Instances of this type do not actually store any
 * information; that is provided by the AMeshObject provided as argument
 * into each method call.
 */
public class AMeshObjectNeighborManager
{
    private static final Log log = Log.getLogInstance( AMeshObjectNeighborManager.class ); // our own, private logger

    /**
     * Constructor for subclasses only. Use SINGLETON.
     */
    protected AMeshObjectNeighborManager()
    {
        // no op
    }


    /**
     * Determine whether a MeshObject has neighbors.
     *
     * @param subject the MeshObject in question
     * @return true if it has at least one neighbor
     */
    public boolean hasNeighbors(
            AMeshObject subject )
    {
        MeshObjectIdentifier [] neighborIdentifiers = subject.theNeighborIdentifiers;

        if( neighborIdentifiers == null ) {
            return false;
        }
        if( neighborIdentifiers.length == 0 ) {
            return false;
        }
        return true;
    }

    /**
     * Determine the index of a relationship in a MeshObject's relationship table.
     * Returns -1 if not related.
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     * @return the index, or -1
     */
    protected int determineRelationshipIndex(
            AMeshObject          subject,
            MeshObjectIdentifier neighborIdentifier )
    {
        MeshObjectIdentifier [] neighborIdentifiers = subject.theNeighborIdentifiers;
        if( neighborIdentifiers == null || neighborIdentifiers.length == 0 ) {
            return -1;
        }
        for( int i=0 ; i<neighborIdentifiers.length ; ++i ) {
            if( neighborIdentifier.equals( neighborIdentifiers[i] )) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determine whether a MeshObject is related to another.
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     * @return true if they are related
     */
    public boolean isRelated(
            AMeshObject          subject,
            MeshObjectIdentifier neighborIdentifier )
    {
        int index = determineRelationshipIndex( subject, neighborIdentifier );
        return index >= 0;
    }

    /**
     * Obtain the set of identifiers of neighbor MeshObjects.
     *
     * @param subject the MeshObject in question
     * @return the set of identifiers of neighbor MeshObjects
     */
    public MeshObjectIdentifier [] getNeighborIdentifiers(
            AMeshObject subject )
    {
        MeshObjectIdentifier [] ret = subject.theNeighborIdentifiers;
        return ret;
    }

    /**
     * Obtain the set of RoleTypes for all neighbors.
     *
     * @param subject the MeshObject in question
     * @return the RoleTypes of neighbor MeshObjects, in the same sequence as getNeighborIdentifiers()
     */
    public RoleType[][] getRoleTypes(
            AMeshObject subject )
    {
        RoleType [][] ret = subject.theNeighborRoleTypes;
        return ret;
    }

    /**
     * Obtain the set of RoleTypes for a particular neighbor.
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     * @return the RoleTypes of the neighbor MeshObject
     * @throws NotRelatedException thrown if the specified MeshObject is not actually a neighbor
     */
    public RoleType[] getRoleTypesFor(
            AMeshObject          subject,
            MeshObjectIdentifier neighborIdentifier )
        throws
            NotRelatedException
    {
        MeshObjectIdentifier [] neighborIdentifiers;
        RoleType [][]           neighborRoleTypes;

        synchronized( subject ) {
            neighborIdentifiers = subject.theNeighborIdentifiers;
            neighborRoleTypes   = subject.theNeighborRoleTypes;
        }

        if( neighborIdentifiers != null && neighborIdentifiers.length > 0 ) {
            for( int i=0 ; i<neighborIdentifiers.length ; ++i ) {
                if( neighborIdentifier.equals( neighborIdentifiers[i] )) {
                    // found
                    return neighborRoleTypes[i];
                }
            }
        }
        throw new NotRelatedException(
                subject.getMeshBase(),
                subject.getMeshBase().getIdentifier(),
                subject,
                subject.getIdentifier(),
                null,
                neighborIdentifier );
    }

    /**
     * Append a new neighbor and associated RoleTypes to a MeshObject.
     * 
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     * @param neighborRoleTypes RoleTypes, or null, for the neighbor
     */
    public void appendNeighbor(
            AMeshObject          subject,
            MeshObjectIdentifier neighborIdentifier,
            RoleType []          neighborRoleTypes )
    {
        synchronized( subject ) {
            internalAppendNeighbor( subject, neighborIdentifier, neighborRoleTypes );
        }
    }

    /**
     * Helper method to append a new neighbor and associated RoleTypes.
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     * @param neighborRoleTypes RoleTypes, or null, for the neighbor
     */
    protected void internalAppendNeighbor(
            AMeshObject          subject,
            MeshObjectIdentifier neighborIdentifier,
            RoleType []          neighborRoleTypes )
    {
        if( determineRelationshipIndex( subject, neighborIdentifier ) != -1 ) {
            log.error( "Have relationship already between " + subject + " and " + neighborIdentifier );
        }

        if( subject.theNeighborIdentifiers == null || subject.theNeighborIdentifiers.length == 0 ) {
            subject.theNeighborIdentifiers = makeMeshObjectIdentifiers( neighborIdentifier );
            subject.theNeighborRoleTypes   = new RoleType[][] { neighborRoleTypes };
        } else {
            subject.theNeighborIdentifiers = makeMeshObjectIdentifiers( subject.theNeighborIdentifiers, neighborIdentifier );
            subject.theNeighborRoleTypes   = ArrayHelper.append( subject.theNeighborRoleTypes, neighborRoleTypes,  RoleType[].class );
        }
    }

    /**
     * Remove a neighbor.
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     */
    public void removeNeighbor(
            AMeshObject          subject,
            MeshObjectIdentifier neighborIdentifier )
    {
        synchronized( subject ) {
            int found = determineRelationshipIndex( subject, neighborIdentifier );
            if( found < 0 ) {
                log.error( subject + ": neighbor not found " + neighborIdentifier );
                return;
            }
            internalRemoveNeighbor( subject, found );
        }
    }

    /**
     * Helper method to remove a neighbor and associated RoleTypes, at a given index.
     *
     * @param subject the MeshObject in question
     * @param index index at which the neighbor is found
     */
    protected void internalRemoveNeighbor(
            AMeshObject subject,
            int         index )
    {
        subject.theNeighborIdentifiers = removeMeshObjectIdentifier( subject.theNeighborIdentifiers, index );
        subject.theNeighborRoleTypes   = ArrayHelper.remove( subject.theNeighborRoleTypes, index, RoleType[].class );
    }

    /**
     * Append a RoleType to an existing RoleType array.
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     * @param toAdd the RoleType to add
     */
    public void appendRoleType(
            AMeshObject          subject,
            MeshObjectIdentifier neighborIdentifier,
            RoleType             toAdd )
    {
        appendRoleTypes( subject, neighborIdentifier, new RoleType[] { toAdd } );
    }

    /**
     * Append several RoleTypes to an existing RoleType array.
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     * @param toAdd the RoleTypes to add
     */
    public void appendRoleTypes(
            AMeshObject          subject,
            MeshObjectIdentifier neighborIdentifier,
            RoleType []          toAdd )
    {
        synchronized( subject ) {
            int found = determineRelationshipIndex( subject, neighborIdentifier );
            if( found < 0 ) {
                log.error( subject + ": neighbor not found " + neighborIdentifier );
                return;
            }
            internalAppendRoleTypes( subject, found, toAdd );
        }
    }
    
    /**
     * Helper method to append RoleTypes, at a given index.
     * 
     * @param subject the MeshObject in question
     * @param index index at which the neighbor is found
     * @param toAdd the RoleTypes to add
     */
    protected void internalAppendRoleTypes(
            AMeshObject subject,
            int         index,
            RoleType [] toAdd )
    {
        if( subject.theNeighborRoleTypes[index] == null ) {
            subject.theNeighborRoleTypes[index] = toAdd;
        } else {
            subject.theNeighborRoleTypes[index] = ArrayHelper.append( subject.theNeighborRoleTypes[index], toAdd, RoleType.class );
        }
    }

    /**
     * Remove a RoleType from an existing RoleType array.
     *
     * @param subject the MeshObject in question
     * @param neighborIdentifier identifier of the neighbor
     * @param toRemove the RoleType to remove
     */
    public void removeRoleType(
            AMeshObject          subject,
            MeshObjectIdentifier neighborIdentifier,
            RoleType             toRemove )
    {
        synchronized( subject ) {
            int found = determineRelationshipIndex( subject, neighborIdentifier );
            if( found < 0 ) {
                log.error( subject + ": neighbor not found " + neighborIdentifier );
                return;
            }
            internalRemoveRoleTypes( subject, found, toRemove );
        }
    }
    
    /**
     * Helper method to remove RoleTypes, at a given index.
     * 
     * @param subject the MeshObject in question
     * @param index index at which the neighbor is found
     * @param toRemove the RoleTypes to remove
     */
    protected void internalRemoveRoleTypes(
            AMeshObject subject,
            int         index,
            RoleType    toRemove )
    {
        subject.theNeighborRoleTypes[index] = ArrayHelper.remove( subject.theNeighborRoleTypes[index], toRemove, false, RoleType.class );

        if( subject.theNeighborRoleTypes[index].length == 0 ) {
            subject.theNeighborRoleTypes[index] = null;
        }
    }

    /**
     * Overridable method to create a single-element MeshObjectIdentifier array.
     *
     * @param oneElement the single element
     * @return the created array
     */
    protected MeshObjectIdentifier [] makeMeshObjectIdentifiers(
            MeshObjectIdentifier oneElement )
    {
        return new MeshObjectIdentifier[] { oneElement };
    }

    /**
     * Overridable method to create a MeshObjectIdentifier array as a
     * concatenation between an existing array and a new element.
     *
     * @param existing the existing array
     * @param toAdd the single element to add
     * @return the created array
     */
    protected MeshObjectIdentifier [] makeMeshObjectIdentifiers(
            MeshObjectIdentifier [] existing,
            MeshObjectIdentifier    toAdd )
    {
        return ArrayHelper.append( existing, toAdd, MeshObjectIdentifier.class );
    }

    /**
     * Remove an element from an array of MeshObjectIdentifier.
     * This may be overridden by subclasses.
     *
     * @param content the array
     * @param indexToRemove index of the object to remove
     * @return the content without to the object to be removed
     */
    protected MeshObjectIdentifier [] removeMeshObjectIdentifier(
            MeshObjectIdentifier [] content,
            int                     indexToRemove )
    {
        MeshObjectIdentifier [] ret = ArrayHelper.remove( content, indexToRemove, MeshObjectIdentifier.class );
        return ret;
    }

    /**
     * Singleton instance of this class.
     */
    public static final AMeshObjectNeighborManager SINGLETON
            = new AMeshObjectNeighborManager();
}
