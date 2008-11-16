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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import org.infogrid.mesh.AbstractMeshObject;
import org.infogrid.mesh.CannotRelateToItselfException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.EquivalentAlreadyException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.mesh.RoleTypeNotBlessedException;
import org.infogrid.mesh.TypedMeshObjectFacade;
import org.infogrid.mesh.externalized.SimpleExternalizedMeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.text.MeshStringRepresentationContext;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.WrongMeshBaseException;
import org.infogrid.meshbase.a.AMeshBase;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.AttributableMeshType;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.Role;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;

/**
 * One particular implementation of MeshObject.
 */
public class AMeshObject
        extends
            AbstractMeshObject
{
    private static final Log log = Log.getLogInstance( AMeshObject.class ); // our own, private logger

    /**
     * Constructor for regular instantiation.
     * 
     * @param identifier the MeshObjectIdentifier of the MeshObject
     * @param meshBase the MeshBase that this MeshObject belongs to
     * @param created the time this MeshObject was created
     * @param updated the time this MeshObject was last updated
     * @param read the time this MeshObject was last read
     * @param expires the time this MeshObject will expire
     */
    public AMeshObject(
            MeshObjectIdentifier identifier,
            AMeshBase            meshBase,
            long                 created,
            long                 updated,
            long                 read,
            long                 expires )
    {
        super( identifier, meshBase, created, updated, read, expires );
    }

    /**
     * Constructor for re-instantiation from external storage.
     * 
     * @param identifier the MeshObjectIdentifier of the MeshObject
     * @param meshBase the MeshBase that this MeshObject belongs to
     * @param created the time this MeshObject was created
     * @param updated the time this MeshObject was last updated
     * @param read the time this MeshObject was last read
     * @param expires the time this MeshObject will expire
     * @param properties the properties with their values of the MeshObject, if any
     * @param meshTypes the MeshTypes and facdes of the MeshObject, if any
     * @param equivalents either an array of length 2, or null. If given, contains the left and right equivalence pointers.
     * @param otherSides the current neighbors of the MeshObject, given as Identifiers
     * @param roleTypes the RoleTypes of the relationships with the various neighbors, in sequence
     */
    public AMeshObject(
            MeshObjectIdentifier                identifier,
            AMeshBase                           meshBase,
            long                                created,
            long                                updated,
            long                                read,
            long                                expires,
            HashMap<PropertyType,PropertyValue> properties,
            EntityType []                       meshTypes,
            MeshObjectIdentifier []             equivalents,
            MeshObjectIdentifier []             otherSides,
            RoleType [][]                       roleTypes )
    {
        super( identifier, meshBase, created, updated, read, expires );
        
        theProperties = properties;
        theOtherSides = otherSides;
        theRoleTypes  = roleTypes;
       
        if( equivalents != null && equivalents.length != 2 ) {
            throw new IllegalArgumentException( "Equivalents must be of length 2" );
        }
        theEquivalenceSetPointers = equivalents;
        
        if( meshTypes != null && meshTypes.length > 0 ) {
            theMeshTypes = new HashMap<EntityType,WeakReference<TypedMeshObjectFacade>>();
            for( int i=0 ; i<meshTypes.length ; ++i ) {
                theMeshTypes.put( meshTypes[i], null );
            }
        }
    }

    /**
     * Update the lastUpdated property. This does not trigger an event generation -- not necessary.
     * This may be overridden.
     *
     * @param timeUpdated the time to set to. -1 means "don't update" and 0 means "current time".
     * @param lastTimeUpdated the time this MeshObject was updated last before
     */
    protected void updateLastUpdated(
            long timeUpdated,
            long lastTimeUpdated )
    {
        AMeshBase realBase = (AMeshBase) theMeshBase;
        theTimeUpdated = realBase.calculateLastUpdated( timeUpdated, lastTimeUpdated );
    }

    /**
     * Update the lastRead property. This does not trigger an event generation -- not necessary.
     * This may be overridden.
     *
     * @param timeRead the time to set to. -1 means "don't update" and 0 means "current time".
     * @param lastTimeRead the time this MeshObject was read last before
     */
    protected void updateLastRead(
            long timeRead,
            long lastTimeRead )
    {
        AMeshBase realBase = (AMeshBase) theMeshBase;
        theTimeRead = realBase.calculateLastRead( timeRead, lastTimeRead );
    }
    
    /**
     * Traverse from this MeshObject to all directly related MeshObjects. Directly
     * related MeshObjects are those MeshObjects that are participating in a
     * relationship with this MeshObject. Specify whether to consider equivalents
     * as well.
     *
     * @param considerEquivalents if true, all equivalent MeshObjects are considered as well
     * @return the set of MeshObjects that are directly related to this MeshObject
     */
    public MeshObjectSet traverseToNeighborMeshObjects(
            boolean considerEquivalents )
    {
        checkAlive();

        MeshObject [] starts;
        if( considerEquivalents ) {
            starts = getEquivalents().getMeshObjects();
        } else {
            starts = new MeshObject[] { this };
        }

        AMeshBase                 realBase   = (AMeshBase) theMeshBase;
        MeshObjectIdentifier [][] otherSides = new MeshObjectIdentifier[ starts.length ][];
        int n=0;
        for( int s=0 ; s<starts.length ; ++s ) {
            AMeshObject current = (AMeshObject) starts[s];
            otherSides[s] = current.theOtherSides;
            if( current.theOtherSides != null ) {
                n += current.theOtherSides.length;
            }
        }
        if( n == 0 ) {
            return realBase.getMeshObjectSetFactory().obtainEmptyImmutableMeshObjectSet();
            
        } else {
            AMeshObject [] ret = new AMeshObject[ n ];
            
            int max = 0;
            for( int s=0 ; s<otherSides.length ; ++s ) {
                if( otherSides[s] == null ) {
                    continue;
                }
                MeshObject [] add = findRelatedMeshObjects( theMeshBase, otherSides[s] );

                for( int i=0 ; i<add.length ; ++i ) {
                    if( add[i] == null ) {
                        // This happens when a MeshObject auto-expires. FIXME: This might introduce more problems
                        // than it solves (e.g. it masks genuine errors)
                        if( log.isDebugEnabled() ) {
                            log.debug( "Could not find related object " + otherSides[s][i] );
                        }

                    } else if( !ArrayHelper.isIn( add[i], ret, 0, max, false )) {
                        ret[ max++ ] = (AMeshObject) add[i];
                    }
                }
            }
            if( max < n ) {
                ret = ArrayHelper.copyIntoNewArray( ret, 0, max, AMeshObject.class );
            }

            updateLastRead();

            return realBase.getMeshObjectSetFactory().createImmutableMeshObjectSet( ret );
        }
    }

    /**
     * Find neighbor MeshObjects of this MeshObject that are known by their
     * MeshObjectIdentifiers.
     * We pass in the MeshBase to use because this may be invoked when a MeshObject's member
     * variable has been zero'd out already.
     *
     * @param mb the MeshBase to use
     * @param identifiers the MeshObjectIdentifiers of the MeshObjects we are looking for
     * @return the MeshObjects that we found
     */
    protected MeshObject [] findRelatedMeshObjects(
            MeshBase                mb,
            MeshObjectIdentifier [] identifiers )
    {
        MeshObject [] ret;
        try {
            ret = mb.accessLocally( identifiers );

        } catch( MeshObjectAccessException ex ) {
            log.error( ex );            
            ret = ex.getBestEffortResult();

        } catch( NotPermittedException ex ) {
            ret = new MeshObject[0];
        }
        return ret;
    }

    /**
     * Find a single neighbor MeshObject of this MeshObject that is known by its
     * MeshObjectIdentifier.
     * We pass in the MeshBase to use because this may be invoked when a MeshObject's member
     * variable has been zero'd out already.
     * This internal helper method may be overridden by subclasses.
     *
     * @param mb the MeshBase to use
     * @param identifier the MeshObjectIdentifier of the MeshObject we are looking for
     * @return the MeshObject that we found
     */
    protected MeshObject findRelatedMeshObject(
            MeshBase             mb,
            MeshObjectIdentifier identifier )
    {
        MeshObject [] ret = findRelatedMeshObjects( mb, new MeshObjectIdentifier[] { identifier } );
        return ret[0];
    }

    /**
     * For clients that know we are an AMeshObject, we can also return our internal representation.
     * Please do not modify the content of this array, bad things may happen.
     *
     * @return the set of other sides
     */
    public MeshObjectIdentifier [] getInternalNeighborList()
    {
        return theOtherSides;
    }

    /**
     * For clients that know we are an AMeshObject, we can also return our internal representation.
     * Please do not modify the content of this array, bad things may happen.
     *
     * @return the RoleTypes played by the other sides
     */
    public RoleType [][] getInternalNeighborRoleTypes()
    {
        return theRoleTypes;
    }

    /**
     * Relate this MeshObject to another MeshObject. This does not bless the relationship.
     *
     * @param otherObject the MeshObject to relate to
     * @throws RelatedAlreadyException thrown to indicate that this MeshObject is already related
     *         to the otherObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @see #unrelate
     * @see #relateAndBless
     */
    public void relate(
            MeshObject otherObject )
        throws
            RelatedAlreadyException,
            TransactionException
    {
        internalRelate( otherObject, true, false );
    }
    
    /**
     * Internal helper to implement a method. While on this level, it does not appear that factoring out
     * this method makes any sense, subclasses may appreciate it.
     *
     * @param otherObject the MeshObject to relate to
     * @param isMaster true if this is the master
     * @param forgiving true if the problems should be tolerated to the maximum extent possible
     * @throws RelatedAlreadyException thrown to indicate that this MeshObject is already related
     *         to the otherObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     */
    protected void internalRelate(
            MeshObject otherObject,
            boolean    isMaster,
            boolean    forgiving )
        throws
            RelatedAlreadyException,
            TransactionException
    {
        checkAlive();
        otherObject.checkAlive();

        if( otherObject == null ) {
            throw new NullPointerException( "otherObject is null" );
        }
        if( otherObject == this ) {
            throw new CannotRelateToItselfException( this );
        }
        if( getMeshBase() != otherObject.getMeshBase() ) {
            throw new WrongMeshBaseException( getMeshBase(), otherObject.getMeshBase() );
        }
        MeshObjectIdentifier here            = getIdentifier();
        MeshObjectIdentifier other           = otherObject.getIdentifier();
        AMeshObject    realOtherObject = (AMeshObject) otherObject;

        if( theMeshBase != realOtherObject.theMeshBase ) {
            throw new IllegalArgumentException( "Cannot relate MeshObjects held in different MeshBases" );
        }

        MeshObjectIdentifier [] oldOtherSides;
        MeshObjectIdentifier [] oldHereSides;

        synchronized( this ) {
            synchronized( otherObject ) {
                
                checkTransaction();
                
                boolean hereAlready  = false;
                boolean thereAlready = false;

                // do what can throw exceptions first
                if( theOtherSides != null ) {
                    for( int i=0 ; i<theOtherSides.length ; ++i ) {
                        if( other.equals( theOtherSides[i] )) {
                            if( forgiving ) {
                                hereAlready = true;
                            } else {
                                throw new RelatedAlreadyException( this, otherObject );
                            }
                        }
                    }
                }
                if( realOtherObject.theOtherSides != null ) {
                    for( int i=0 ; i<realOtherObject.theOtherSides.length ; ++i ) {
                        if( here.equals( realOtherObject.theOtherSides[i] )) {
                            if( forgiving ) {
                                thereAlready = true;
                            } else {
                                throw new RelatedAlreadyException( otherObject, this );
                            }
                        }
                    }
                }

                oldOtherSides = theOtherSides;
                oldHereSides  = realOtherObject.theOtherSides;

                if( !hereAlready ) {
                    if( theOtherSides == null ) {
                        theOtherSides = new MeshObjectIdentifier[]{ other };
                        theRoleTypes  = new RoleType[][] { null };
                    } else {
                        theOtherSides = ArrayHelper.append( theOtherSides, other,              MeshObjectIdentifier.class );
                        theRoleTypes  = ArrayHelper.append( theRoleTypes,  (RoleType []) null, RoleType[].class );
                    }
                }
                if( !thereAlready ) {
                    if( realOtherObject.theOtherSides == null ) {
                        realOtherObject.theOtherSides = new MeshObjectIdentifier[]{ here };
                        realOtherObject.theRoleTypes  = new RoleType[][] { null };
                    } else {
                        realOtherObject.theOtherSides = ArrayHelper.append( realOtherObject.theOtherSides, here,               MeshObjectIdentifier.class );
                        realOtherObject.theRoleTypes  = ArrayHelper.append( realOtherObject.theRoleTypes,  (RoleType []) null, RoleType[].class );
                    }
                }
                if( !hereAlready ) {
                    this.fireNeighborAdded( null, oldOtherSides, other, theOtherSides, theMeshBase );
                }
                if( !thereAlready ) {
                    realOtherObject.fireNeighborAdded( null, oldHereSides, here, realOtherObject.theOtherSides, theMeshBase );
                }
            }
        }
        updateLastUpdated();
    }

    /**
     * Unrelate this MeshObject from another MeshObject. This will also remove all blessings from the relationship.
     *
     * @param otherObject the MeshObject to unrelate from
     * @throws NotRelatedException thrown if this MeshObject is not already related to the otherObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     * @see #relate
     */
    public void unrelate(
            MeshObject otherObject )
        throws
            NotRelatedException,
            TransactionException,
            NotPermittedException
    {
        internalUnrelate( otherObject, theMeshBase, true, 0L );
    }
    
    /**
     * Internal helper to unrelate that also works for already-dead MeshObjects.
     *
     * @param otherObject the MeshObject to unrelate from
     * @param mb the MeshBase that this MeshObject does or used to belong to
     * @param isMaster true if this is the master replica
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws NotRelatedException thrown if this MeshObject is not already related to the otherObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the operation is not permitted
     */
    protected void internalUnrelate(
            MeshObject otherObject,
            MeshBase   mb,
            boolean    isMaster,
            long       timeUpdated )
        throws
            NotRelatedException,
            TransactionException,
            NotPermittedException
    {
        if( otherObject == null ) {
            throw new NullPointerException( "otherObject is null" );
        }
        if( otherObject == this ) {
            throw new NotRelatedException( this, this );
        }
        if( mb != otherObject.getMeshBase() ) {
            throw new IllegalArgumentException( "cannot unrelate MeshObjects in different MeshBases" );
        }

        checkAlive();
        otherObject.checkAlive();
        
        AMeshObject realOtherObject = (AMeshObject) otherObject;
        MeshObjectIdentifier here  = getIdentifier();
        MeshObjectIdentifier other = otherObject.getIdentifier();

        MeshObjectIdentifier [] oldOtherSides;
        MeshObjectIdentifier [] oldHereSides;

        if( theMeshBase != realOtherObject.theMeshBase ) {
            throw new IllegalArgumentException( "Cannot unrelate MeshObjects held in different MeshBases" );
        }

        synchronized( this ) {
            synchronized( otherObject ) {

                internalCheckTransaction( mb );

                if( theOtherSides == null ) {
                    throw new NotRelatedException( this, otherObject );
                }
                if( realOtherObject.theOtherSides == null ) {
                    log.error( "found otherObject here, but not here in otherObject: " + this + " vs. " + otherObject );
                    throw new NotRelatedException( otherObject, this );
                }

                int foundHere = -1;
                for( int i=0 ; i<theOtherSides.length ; ++i ) {
                    if( other.equals( theOtherSides[i] )) {
                        foundHere = i;
                        break;
                    }
                }
                if( foundHere == -1 ) {
                    throw new NotRelatedException( this, otherObject );
                }

                int foundOther = -1;
                for( int i=0 ; i<realOtherObject.theOtherSides.length ; ++i ) {
                    if( here.equals( realOtherObject.theOtherSides[i] )) {
                        foundOther = i;
                        break;
                    }
                }
                if( foundOther == -1 ) {
                    throw new NotRelatedException( otherObject, this );
                }

                // check that the ByRoleType with the other side let us
                if( theRoleTypes[foundHere] != null ) {
                    checkPermittedUnbless( theRoleTypes[foundHere], otherObject );
                }
                if( realOtherObject.theRoleTypes[foundOther] != null ) {
                    realOtherObject.checkPermittedUnbless( realOtherObject.theRoleTypes[foundOther], this );
                }
                // check that all other Roles let us
                for( int i=0 ; i<theOtherSides.length ; ++i ) {
                    if( i != foundHere && theRoleTypes[i] != null ) {
                        MeshObject realOtherSide = findRelatedMeshObject( mb, theOtherSides[i] );
                        checkPermittedUnbless( theRoleTypes[foundHere], otherObject, theRoleTypes[i], realOtherSide );
                    }
                }
                for( int i=0 ; i<realOtherObject.theOtherSides.length ; ++i ) {
                    if( i != foundOther && realOtherObject.theRoleTypes[i] != null ) {
                        MeshObject realOtherSide = findRelatedMeshObject( mb, realOtherObject.theOtherSides[i] );
                        realOtherObject.checkPermittedUnbless( realOtherObject.theRoleTypes[foundOther], this, realOtherObject.theRoleTypes[i], realOtherSide );
                    }
                }
                
                // first remove all the RoleTypes
                if( theRoleTypes[foundHere] != null ) {

                    fireTypesRemoved(
                            theRoleTypes[foundHere],
                            theRoleTypes[foundHere],
                            new RoleType[0],
                            otherObject,
                            mb );
                    realOtherObject.fireTypesRemoved( 
                            realOtherObject.theRoleTypes[foundOther],
                            realOtherObject.theRoleTypes[foundOther],
                            new RoleType[0],
                            this,
                            mb );                    
                }
                theRoleTypes[foundHere] = null;
                realOtherObject.theRoleTypes[foundOther] = null;

                oldOtherSides = theOtherSides;
                oldHereSides  = realOtherObject.theOtherSides;

                theOtherSides = ArrayHelper.remove( theOtherSides, foundHere, MeshObjectIdentifier.class );
                theRoleTypes  = ArrayHelper.remove( theRoleTypes,  foundHere, RoleType[].class );
                
                realOtherObject.theOtherSides = ArrayHelper.remove( realOtherObject.theOtherSides, foundOther, MeshObjectIdentifier.class );
                realOtherObject.theRoleTypes  = ArrayHelper.remove( realOtherObject.theRoleTypes,  foundOther, RoleType[].class );                

                this.fireNeighborRemoved( oldOtherSides, other, theOtherSides, mb );
                realOtherObject.fireNeighborRemoved( oldHereSides, here, realOtherObject.theOtherSides, mb );
            }
        }
        updateLastUpdated( timeUpdated, theTimeUpdated );
    }

    /**
     * Determine whether this MeshObject is related to another MeshObject.
     *
     * @param otherObject the MeshObject to which this MeshObject may be related
     * @return true if this MeshObject is currently related to otherObject
     */
    public boolean isRelated(
            MeshObject otherObject )
    {
        checkAlive();
        otherObject.checkAlive();
        updateLastRead();

        if( theMeshBase != otherObject.getMeshBase() ) {
            throw new IllegalArgumentException( "Cannot relate MeshObjects held in different MeshBases" );
        }

        MeshObjectIdentifier [] otherSides = theOtherSides;
        if( otherSides == null || otherSides.length == 0 ) {
            return false;
        }
        MeshObjectIdentifier other = otherObject.getIdentifier();

        for( int i=0 ; i<otherSides.length ; ++i ) {
            if( other.equals( otherSides[i] )) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make a relationship of this MeshObject to another MeshObject support the provided RoleTypes.
     * As a result, this relationship will support either all RoleTypes or none.
     * 
     * @param thisEnds the RoleTypes of the RelationshipTypes that are instantiated at the end that this MeshObject is attached to
     * @param otherObject the MeshObject whose relationship to this MeshObject shall be blessed
     * @throws RoleTypeBlessedAlreadyException thrown if the relationship to the other MeshObject is blessed
     *         already with one ore more of the given RoleTypes
     * @throws NotRelatedException thrown if this MeshObject is not currently related to otherObject
     * @throws IsAbstractException thrown if one of the RoleTypes belong to an abstract RelationshipType
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     * @see #relate
     * @see #relateAndBless
     * @see #unrelate
     */
    public void blessRelationship(
            RoleType [] thisEnds,
            MeshObject  otherObject )
        throws
            RoleTypeBlessedAlreadyException,
            EntityNotBlessedException,
            NotRelatedException,
            IsAbstractException,
            TransactionException,
            NotPermittedException
    {
        internalBless( thisEnds, otherObject, true, false, 0L );
    }
    
    /**
     * Internal helper to implement a method. While on this level, it does not appear that
     * factoring out this method makes any sense, subclasses may appreciate it.
     * 
     * @param roleTypesToAddHere the RoleTypes of the RelationshipTypes that are instantiated at the end that this MeshObject is attached to
     * @param otherObject the MeshObject whose relationship to this MeshObject shall be blessed
     * @param isMaster if true, this is the master replica
     * @param forgiving if true, attempt to ignore errors
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws RoleTypeBlessedAlreadyException thrown if the relationship to the other MeshObject is blessed
     *         already with one ore more of the given RoleTypes
     * @throws EntityNotBlessedException thrown if the source or destination MeshObject was not blessed with a required EntityType
     * @throws NotRelatedException thrown if this MeshObject is not currently related to otherObject
     * @throws IsAbstractException thrown if one of the RoleTypes belong to an abstract RelationshipType
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    protected void internalBless(
            RoleType [] roleTypesToAddHere,
            MeshObject  otherObject,
            boolean     isMaster,
            boolean     forgiving,
            long        timeUpdated )
        throws
            RoleTypeBlessedAlreadyException,
            EntityNotBlessedException,
            NotRelatedException,
            IsAbstractException,
            TransactionException,
            NotPermittedException
    {
        checkAlive();
        otherObject.checkAlive();

        for( RoleType thisEnd : roleTypesToAddHere ) {
            if( thisEnd == null ) {
                throw new IllegalArgumentException( "null RoleType" );
            }
            if( thisEnd.getRelationshipType().getIsAbstract().value() ) {
                throw new IsAbstractException( this, thisEnd.getRelationshipType() );
            }
        }
        
        AMeshObject realOtherObject = (AMeshObject) otherObject;

        if( theMeshBase != realOtherObject.theMeshBase ) {
            throw new WrongMeshBaseException( theMeshBase, realOtherObject.theMeshBase );
        }

        synchronized( this ) {
            synchronized( otherObject ) {

                theMeshBase.checkTransaction();

                for( int i=0 ; i<roleTypesToAddHere.length ; ++i ) {
                    RoleType   otherEnd          = roleTypesToAddHere[i].getOtherRoleType();
                    EntityType requiredType      = roleTypesToAddHere[i].getEntityType();
                    EntityType requiredOtherType = otherEnd.getEntityType();
                    
                    if( requiredType != null ) {
                        boolean found = false;
                        if( theMeshTypes != null ) {
                            for( AttributableMeshType amt : theMeshTypes.keySet() ) {
                                if( amt.equalsOrIsSupertype( requiredType )) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if( !found ) {
                            throw new EntityNotBlessedException( this, requiredType );
                        }
                    }
                    if( requiredOtherType != null ) {
                        boolean found = false;
                        if( realOtherObject.theMeshTypes != null ) {
                            for( AttributableMeshType amt : realOtherObject.theMeshTypes.keySet() ) {
                                if( amt.equalsOrIsSupertype( requiredOtherType )) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if( !found ) {
                            throw new EntityNotBlessedException( realOtherObject, requiredOtherType );
                        }
                    }
                }

                if( theOtherSides == null ) {
                    throw new NotRelatedException( this, otherObject );
                }
                if( realOtherObject.theOtherSides == null ) {
                    log.error( "found otherObject here, but not here in otherObject: " + this + " vs. " + otherObject );
                    throw new NotRelatedException( otherObject, this );
                }

                MeshObjectIdentifier other     = otherObject.getIdentifier();
                int            foundHere = -1;
                for( int i=0 ; i<theOtherSides.length ; ++i ) {
                    if( other.equals( theOtherSides[i] )) {
                        foundHere = i;
                        break;
                    }
                }
                if( foundHere == -1 ) {
                    throw new NotRelatedException( this, otherObject );
                }

                MeshObjectIdentifier here       = getIdentifier();
                int            foundThere = -1;
                for( int i=0 ; i<realOtherObject.theOtherSides.length ; ++i ) {
                    if( here.equals( realOtherObject.theOtherSides[i] )) {
                        foundThere = i;
                        break;
                    }
                }
                if( foundThere == -1 ) {
                    log.error( "found otherObject here, but not here in otherObject: " + this + " vs. " + otherObject );
                    throw new NotRelatedException( otherObject, this );
                }

                RoleType [] oldRoleTypesHere  = theRoleTypes[foundHere];
                RoleType [] oldRoleTypesThere = realOtherObject.theRoleTypes[foundThere];
                
                boolean hereAlready  = false;
                boolean thereAlready = false;
                
                for( RoleType thisEnd : roleTypesToAddHere ) {
                    RoleType otherEnd = thisEnd.getOtherRoleType();

                    // make sure we do everything that might throw an exception first, and do assignments later
                    if( theRoleTypes[foundHere] != null ) {
                        for( int i=0 ; i<theRoleTypes[foundHere].length ; ++i ) {
                            if( theRoleTypes[foundHere][i].isSpecializationOfOrEquals( thisEnd )) {
                                if( forgiving ) {
                                    hereAlready = true;
                                } else {
                                    throw new RoleTypeBlessedAlreadyException( this, thisEnd, otherObject );
                                }

                            } else if( thisEnd.isSpecializationOfOrEquals( theRoleTypes[foundHere][i] )) {
                                theRoleTypes[foundHere][i] = thisEnd;
                                break;
                            }
                        }
                    }
                                
                    if( realOtherObject.theRoleTypes[foundThere] != null ) {
                        for( int i=0 ; i<realOtherObject.theRoleTypes[foundThere].length ; ++i ) {
                            if( realOtherObject.theRoleTypes[foundThere][i].isSpecializationOfOrEquals( otherEnd )) {
                                if( forgiving ) {
                                    thereAlready = true;
                                } else {
                                    throw new RoleTypeBlessedAlreadyException( otherObject, otherEnd, this );
                                }
                    
                            } else if( otherEnd.isSpecializationOfOrEquals( realOtherObject.theRoleTypes[foundThere][i] )) {
                                realOtherObject.theRoleTypes[foundThere][i] = otherEnd;
                                return;
                            }
                        }
                    }
                }

                checkPermittedBless( roleTypesToAddHere, otherObject ); // implementation does everything else
                for( int i=0 ; i<theOtherSides.length ; ++i ) {
                    if( i != foundHere && theRoleTypes[i] != null ) {
                        MeshObject realOtherSide = findRelatedMeshObject( theMeshBase, theOtherSides[i] );
                        checkPermittedBless( roleTypesToAddHere, otherObject, theRoleTypes[i], realOtherSide );
                    }
                }
                
                for( RoleType thisEnd : roleTypesToAddHere ) {
                    RoleType otherEnd = thisEnd.getOtherRoleType();

                    if( !hereAlready ) {
                        if( theRoleTypes[foundHere] == null ) {
                            theRoleTypes[foundHere] = new RoleType[] { thisEnd };
                        } else {
                            theRoleTypes[foundHere] = ArrayHelper.append( theRoleTypes[foundHere], thisEnd, RoleType.class );
                        }
                    }
                    if( !thereAlready ) {
                        if( realOtherObject.theRoleTypes[foundThere] == null ) {
                            realOtherObject.theRoleTypes[foundThere] = new RoleType[] { otherEnd };
                        } else {
                            realOtherObject.theRoleTypes[foundThere] = ArrayHelper.append( realOtherObject.theRoleTypes[foundThere], otherEnd, RoleType.class );
                        }
                    }
                }

                RoleType [] newRoleTypesHere  = theRoleTypes[foundHere];
                RoleType [] newRoleTypesThere = realOtherObject.theRoleTypes[foundThere];
                
                RoleType [] roleTypesToAddThere = new RoleType[ roleTypesToAddHere.length ];
                for( int i=0 ; i<roleTypesToAddHere.length ; ++i ) {
                    roleTypesToAddThere[i] = roleTypesToAddHere[i].getOtherRoleType();
                }

                if( !hereAlready ) {
                    fireTypesAdded( oldRoleTypesHere, roleTypesToAddHere, newRoleTypesHere, otherObject, theMeshBase );
                }
                if( !thereAlready ) {
                    realOtherObject.fireTypesAdded( oldRoleTypesThere, roleTypesToAddThere, newRoleTypesThere, this, theMeshBase );
                }
            }
        }
        updateLastUpdated( timeUpdated, theTimeUpdated );
    }

    /**
     * Make a relationship of this MeshObject to another MeshObject stop supporting the provided RoleType.
     * 
     * @param thisEnds the RoleType of the RelationshipType at the end that this MeshObject is attached to, and that shall be removed
     * @param otherObject the other MeshObject whose relationship to this MeshObject shall be unblessed
     * @throws RoleTypeNotBlessedException thrown if the relationship to the other MeshObject does not support the RoleType
     * @throws NotRelatedException thrown if this MeshObject is not currently related to otherObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void unblessRelationship(
            RoleType [] thisEnds,
            MeshObject  otherObject )
        throws
            RoleTypeNotBlessedException,
            NotRelatedException,
            TransactionException,
            NotPermittedException
    {
        internalUnbless( thisEnds, otherObject, true, 0L );
    }
    
    /**
     * Internal helper to implement a method. While on this level, it does not appear that
     * factoring out this method makes any sense, subclasses may appreciate it.
     * 
     * @param roleTypesToRemoveHere the RoleType of the RelationshipType at the end that this MeshObject is attached to, and that shall be removed
     * @param otherObject the other MeshObject whose relationship to this MeshObject shall be unblessed
     * @param isMaster if true, this is the master replica
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws RoleTypeNotBlessedException thrown if the relationship to the other MeshObject does not support the RoleType
     * @throws NotRelatedException thrown if this MeshObject is not currently related to otherObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    protected void internalUnbless(
            RoleType [] roleTypesToRemoveHere,
            MeshObject  otherObject,
            boolean     isMaster,
            long        timeUpdated )
        throws
            RoleTypeNotBlessedException,
            NotRelatedException,
            TransactionException,
            NotPermittedException
    {
        checkAlive();
        otherObject.checkAlive();

        for( RoleType thisEnd : roleTypesToRemoveHere ) {
            if( thisEnd == null ) {
                throw new IllegalArgumentException( "null RoleType" );
            }
        }

        AMeshObject realOtherObject = (AMeshObject) otherObject;

        if( theMeshBase != realOtherObject.theMeshBase ) {
            throw new IllegalArgumentException( "Cannot unrelate MeshObjects held in different MeshBases" );
        }

        synchronized( this ) {
            synchronized( otherObject ) {

                checkTransaction();
                
                if( theOtherSides == null || theOtherSides.length == 0 ) {
                    throw new NotRelatedException( this, otherObject );
                }
                if( realOtherObject.theOtherSides == null || realOtherObject.theOtherSides.length == 0 ) {
                    log.error( "found otherObject here, but not here in otherObject: " + this + " vs. " + otherObject );
                    throw new NotRelatedException( otherObject, this );
                }

                MeshObjectIdentifier other     = otherObject.getIdentifier();
                int            foundHere = -1;
                for( int i=0 ; i<theOtherSides.length ; ++i ) {
                    if( other.equals( theOtherSides[i] )) {
                        foundHere = i;
                        break;
                    }
                }
                if( foundHere == -1 ) {
                    throw new NotRelatedException( this, otherObject );
                }
                
                MeshObjectIdentifier here       = getIdentifier();
                int            foundThere = -1;
                for( int i=0 ; i<realOtherObject.theOtherSides.length ; ++i ) {
                    if( here.equals( realOtherObject.theOtherSides[i] )) {
                        foundThere = i;
                        break;
                    }
                }
                if( foundThere == -1 ) {
                    log.error( "found otherObject here, but not here in otherObject: " + this + " vs. " + otherObject );
                    throw new NotRelatedException( otherObject, this );
                }

                RoleType [] oldRoleTypesHere  = theRoleTypes[foundHere];
                RoleType [] oldRoleTypesThere = realOtherObject.theRoleTypes[foundThere];

                RoleType [] roleTypesToRemoveThere = new RoleType[ roleTypesToRemoveHere.length ];
                for( int i=0 ; i<roleTypesToRemoveHere.length ; ++i ) {
                    roleTypesToRemoveThere[i] = roleTypesToRemoveHere[i].getOtherRoleType();
                }
                
                for( RoleType thisEnd : roleTypesToRemoveHere ) {
                    RoleType otherEnd = thisEnd.getOtherRoleType();

                    if( theRoleTypes[foundHere] == null || !ArrayHelper.isIn( thisEnd, theRoleTypes[foundHere], false )) {
                        throw new RoleTypeNotBlessedException( this, thisEnd, otherObject );
                    }
                    if( realOtherObject.theRoleTypes[foundThere] == null || !ArrayHelper.isIn( otherEnd, realOtherObject.theRoleTypes[foundThere], false )) {
                        throw new RoleTypeNotBlessedException( otherObject, otherEnd, this );
                    }
                }

                           this.checkPermittedUnbless( roleTypesToRemoveHere,  otherObject );
                realOtherObject.checkPermittedUnbless( roleTypesToRemoveThere, this );

                for( int i=0 ; i<theOtherSides.length ; ++i ) {
                    if( i != foundHere && theRoleTypes[i] != null ) {
                        MeshObject realOtherSide = findRelatedMeshObject( theMeshBase, theOtherSides[i] );
                        checkPermittedUnbless( roleTypesToRemoveHere, otherObject, theRoleTypes[i], realOtherSide );
                    }
                }
                for( int i=0 ; i<realOtherObject.theOtherSides.length ; ++i ) {
                    if( i != foundThere && realOtherObject.theRoleTypes[i] != null ) {
                        MeshObject realOtherSide = findRelatedMeshObject( theMeshBase, realOtherObject.theOtherSides[i] );
                        realOtherObject.checkPermittedUnbless( roleTypesToRemoveThere, this, realOtherObject.theRoleTypes[i], realOtherSide );
                    }
                }
                
                for( RoleType thisEnd : roleTypesToRemoveHere ) {
                    RoleType otherEnd = thisEnd.getOtherRoleType();
                    
                    theRoleTypes[foundHere]                  = ArrayHelper.remove( theRoleTypes[foundHere],                  thisEnd,  false, RoleType.class );
                    realOtherObject.theRoleTypes[foundThere] = ArrayHelper.remove( realOtherObject.theRoleTypes[foundThere], otherEnd, false, RoleType.class );

                    if( theRoleTypes[foundHere].length == 0 ) {
                        theRoleTypes[foundHere] = null;
                    }
                    if( realOtherObject.theRoleTypes[foundThere].length == 0 ) {
                        realOtherObject.theRoleTypes[foundThere] = null;
                    }
                }
                RoleType [] newRoleTypesHere  = theRoleTypes[foundHere];
                RoleType [] newRoleTypesThere = realOtherObject.theRoleTypes[foundThere];

                fireTypesRemoved( oldRoleTypesHere, roleTypesToRemoveHere, newRoleTypesHere, otherObject, theMeshBase );
                realOtherObject.fireTypesRemoved( oldRoleTypesThere, roleTypesToRemoveThere, newRoleTypesThere, this, theMeshBase );
            }
        }
        updateLastUpdated( timeUpdated, theTimeUpdated );
    }

    /**
      * Traverse a TraversalSpecification from this MeshObject to obtain a set of MeshObjects.
      * Specify whether relationships of equivalent MeshObjects should be considered as well.
      *
      * @param theTraverseSpec the TraversalSpecification to traverse
      * @param considerEquivalents if true, all equivalent MeshObjects are considered as well;
      *        if false, only this MeshObject will be used as the start
      * @return the set of MeshObjects found as a result of the traversal
      */
    public MeshObjectSet traverse(
            TraversalSpecification theTraverseSpec,
            boolean                considerEquivalents )
    {
        checkAlive();

        if( theTraverseSpec == null ) {
            MeshObjectSet ret = traverseToNeighborMeshObjects( considerEquivalents );
            return ret;
        }

        if( !( theTraverseSpec instanceof RoleType )) {
            return theTraverseSpec.traverse( this, considerEquivalents );
        }
        RoleType type = (RoleType) theTraverseSpec;

        MeshObject [] starts;
        if( considerEquivalents ) {
            starts = getEquivalents().getMeshObjects();
        } else {
            starts = new MeshObject[] { this };
        }
        
        AMeshBase                 realBase   = (AMeshBase) theMeshBase;
        MeshObjectIdentifier [][] otherSides = new MeshObjectIdentifier[ starts.length ][];
        RoleType [][][]           roleTypes  = new RoleType[ starts.length ][][];

        int n = 0;
        for( int s=0 ; s<starts.length ; ++s ) {
            AMeshObject current = (AMeshObject) starts[s];
            synchronized( current ) {
                otherSides[s] = current.theOtherSides;
                roleTypes[s]  = current.theRoleTypes;
            }
            if( otherSides[s] != null ) {
                n += otherSides[s].length;
            }
        }

        if( n == 0 ) {
            return realBase.getMeshObjectSetFactory().obtainEmptyImmutableMeshObjectSet();
        }

        MeshObjectIdentifier [] almost = new MeshObjectIdentifier[ n ];
        int                     max    = 0;

        // it's more efficient to first assemble all possible neighbors, and then subset based on permissions
        for( int s=0 ; s<otherSides.length ; ++s ) {
            for( int i=0 ; i<otherSides[s].length ; ++i ) {
                if( roleTypes[s][i] != null ) {
                    for( int j=0 ; j<roleTypes[s][i].length ; ++j ) {
                        if( roleTypes[s][i][j].isSpecializationOfOrEquals( type ) ) {
                            if( !ArrayHelper.isIn( otherSides[s][i], almost, 0, max, true )) {
                                almost[max++] = otherSides[s][i];
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        if( max < almost.length ) {
            almost = ArrayHelper.copyIntoNewArray( almost, 0, max, MeshObjectIdentifier.class );
        }
        MeshObject [] almostRet = findRelatedMeshObjects( theMeshBase, almost );
        MeshObject [] ret       = new MeshObject[ almostRet.length ];

        int index = 0;
        for( MeshObject current : almostRet ) {
            try {
                checkPermittedTraversal( type, current );
                ret[ index++ ] = current;
            } catch( NotPermittedException ex ) {
                log.info( ex );
            }
        }
        if( index < ret.length ) {
            ret = ArrayHelper.copyIntoNewArray( ret, 0, index, MeshObject.class );
        }

        updateLastRead();

        return realBase.getMeshObjectSetFactory().createImmutableMeshObjectSet( ret );
    }

    /**
     * Obtain the RoleTypes that this MeshObject currently participates in. This will return only one
     * instance of the same RoleType object, even if the MeshObject participates in this RoleType
     * multiple times with different other MeshObjects. Specify whether equivalent MeshObjects
     * should be considered as well.
     * 
     * @param considerEquivalents if true, all equivalent MeshObjects are considered as well;
     *        if false, only this MeshObject will be used as the start
     * @return the RoleTypes that this MeshObject currently participates in.
     */
    public RoleType [] getRoleTypes(
            boolean considerEquivalents )
    {
        checkAlive();

        MeshObject [] starts;
        if( considerEquivalents ) {
            starts = getEquivalents().getMeshObjects();
        } else {
            starts = new MeshObject[] { this };
        }
        
        MeshObjectIdentifier [][] otherSides = new MeshObjectIdentifier[ starts.length ][];
        RoleType [][][]     roleTypes  = new RoleType[ starts.length ][][];

        int n=0;
        for( int s=0 ; s<starts.length ; ++s ) {
            AMeshObject current = (AMeshObject) starts[s];
            synchronized( current ) {
                otherSides[s] = current.theOtherSides;
                roleTypes[s]  = current.theRoleTypes;
            }
            if( otherSides[s] != null ) {
                n += otherSides[s].length;
            }
        }

        RoleType [] ret = new RoleType[ n ];
        int         max = 0;

        for( int s=0 ; s<starts.length ; ++s ) {
            if( otherSides[s] == null ) {
                continue;
            }
            MeshObject [] realOtherSides = findRelatedMeshObjects( theMeshBase, otherSides[s] );

            if( roleTypes[s] != null ) {
                for( int i=0 ; i<roleTypes[s].length ; ++i ) {
                    if( roleTypes[s][i] != null ) {
                        for( int j=0 ; j<roleTypes[s][i].length ; ++j ) {
                            try {
                                checkPermittedTraversal( roleTypes[s][i][j], realOtherSides[i] );

                                if( !ArrayHelper.isIn( roleTypes[s][i][j], ret, 0, max, true )) {
                                    ret[ max++ ] = roleTypes[s][i][j];
                                }
                            } catch( NotPermittedException ex ) {
                                log.info( ex );
                            }
                        }
                    }
                }
            }
        }
        if( max < n ) {
            ret = ArrayHelper.copyIntoNewArray( ret, 0, max, RoleType.class );
        }
        updateLastRead();

        return ret;
    }

    /**
     * Obtain the MeshTypeIdentifiers of the RoleTypes that this MeshObject plays with a
     * given neighbor MeshObject identified by its MeshObjectIdentifier.
     * 
     * @param neighborIdentifier the MeshObjectIdentifier of the neighbor MeshObject
     * @return the RoleTypes
     * @throws NotRelatedException thrown if the specified MeshObject is not actually a neighbor
     */
    public synchronized MeshTypeIdentifier [] getRoleTypeIdentifiers(
            MeshObjectIdentifier neighborIdentifier )
        throws
            NotRelatedException
    {
        if( theOtherSides != null ) {
            for( int i=0 ; i<theOtherSides.length ; ++i ) {
                if( neighborIdentifier.equals( theOtherSides[i] )) {
                    if( theRoleTypes[i] != null ) {
                        MeshTypeIdentifier [] ret = new MeshTypeIdentifier[ theRoleTypes[i].length ];
                        for( int j=0 ; j<ret.length ; ++j ) {
                            ret[j] = theRoleTypes[i][j].getIdentifier();
                        }
                        return ret;
                    } else {
                        break;
                    }
                }
            }
        }
        throw new NotRelatedException( theMeshBase, theMeshBase.getIdentifier(), this, theIdentifier, null, neighborIdentifier );
    }
    
    /**
     * Obtain the Roles that this MeshObject currently participates in.
     * Specify whether relationships of equivalent MeshObjects
     * should be considered as well.
     *
     * @param considerEquivalents if true, all equivalent MeshObjects are considered as well
     *        if false, only this MeshObject will be used as the start
     * @return the Roles that this MeshObject currently participates in.
     */
    public Role [] getRoles(
            boolean considerEquivalents )
    {
        checkAlive();

        MeshObject [] starts;
        if( considerEquivalents ) {
            starts = getEquivalents().getMeshObjects();
        } else {
            starts = new MeshObject[] { this };
        }
        
        MeshObjectIdentifier [][] otherSides = new MeshObjectIdentifier[ starts.length ][];
        RoleType [][][]     roleTypes  = new RoleType[ starts.length ][][];

        int n=0;
        for( int s=0 ; s<starts.length ; ++s ) {
            AMeshObject current = (AMeshObject) starts[s];
            synchronized( current ) {
                otherSides[s] = current.theOtherSides;
                roleTypes[s]  = current.theRoleTypes;
            }
            if( roleTypes[s] != null ) {
                for( int i=0 ; i<roleTypes[s].length ; ++i ) {
                    if( roleTypes[s][i] != null ) {
                        n += roleTypes[s][i].length;
                    }
                }
            }
        }

        Role [] ret = new Role[ n ];
        int     max = 0;

        for( int s=0 ; s<starts.length ; ++s ) {
            if( otherSides[s] != null ) {
                MeshObject [] realOtherSides = findRelatedMeshObjects( theMeshBase, otherSides[s] );

                for( int i=0 ; i<roleTypes[s].length ; ++i ) {
                    if( roleTypes[s][i] != null ) {
                        for( int j=0 ; j<roleTypes[s][i].length ; ++j ) {
                            try {
                                checkPermittedTraversal( roleTypes[s][i][j], realOtherSides[i] );
                                ret[ max++ ] = new Role( roleTypes[s][i][j], realOtherSides[i] );
                            } catch( NotPermittedException ex ) {
                                log.info( ex );
                            }
                        }
                    }
                }
            }
        }
        if( max < n ) {
            ret = ArrayHelper.copyIntoNewArray( ret, 0, max, Role.class );
        }
        updateLastRead();

        return ret;
    }

    /**
     * Internal helper to obtain all Roles that this MeshObject currently participates in.
     *
     * @param mb the MeshBase
     * @param considerEquivalents if true, all equivalent MeshObjects are considered as well;
     *        if false, only this MeshObject will be used as the start
     * @return the Roles that this MeshObject currently participates in.
     */
    protected Role [] _getAllRoles(
            MeshBase mb,
            boolean  considerEquivalents )
    {
        MeshObject [] starts;
        if( considerEquivalents ) {
            starts = getEquivalents().getMeshObjects();
        } else {
            starts = new MeshObject[] { this };
        }
        
        MeshObjectIdentifier [][] otherSides = new MeshObjectIdentifier[ starts.length ][];
        RoleType [][][]     roleTypes  = new RoleType[ starts.length ][][];

        int n=0;
        for( int s=0 ; s<starts.length ; ++s ) {
            AMeshObject current = (AMeshObject) starts[s];
            synchronized( current ) {
                otherSides[s] = current.theOtherSides;
                roleTypes[s]  = current.theRoleTypes;
            }
            if( roleTypes[s] != null ) {
                for( int i=0 ; i<roleTypes[s].length ; ++i ) {
                    if( roleTypes[s][i] != null ) {
                        n += roleTypes[s][i].length;
                    }
                }
            }
        }

        if( n == 0 ) {
            return new Role[0];
        }

        Role [] ret = new Role[ n ];
        int     max = 0;

        for( int s=0 ; s<starts.length ; ++s ) {
            if( roleTypes[s] == null ) {
                continue;
            }
            AMeshObject current = (AMeshObject) starts[s];

            MeshObject [] realOtherSides = findRelatedMeshObjects( mb, otherSides[s] );
        
            for( int i=0 ; i<roleTypes[s].length ; ++i ) {
                if( roleTypes[s][i] != null ) {
                    for( int j=0 ; j<roleTypes[s][i].length ; ++j ) {
                        ret[ max++ ] = new Role( roleTypes[s][i][j], realOtherSides[i] );
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Obtain the RoleTypes that this MeshObject currently participates in with the
     * specified other MeshObject.
     * Specify whether relationships of equivalent MeshObjects should be considered
     * as well.
     *
     * @param otherObject the other MeshObject
     * @param considerEquivalents if true, all equivalent MeshObjects are considered as well;
     *        if false, only this MeshObject will be used as the start
     * @return the RoleTypes that this MeshObject currently participates in.
     */
    public RoleType [] getRoleTypes(
            MeshObject otherObject,
            boolean    considerEquivalents )
    {
        checkAlive();
        otherObject.checkAlive();

        if( theMeshBase != otherObject.getMeshBase() ) {
            throw new IllegalArgumentException( "Cannot relate MeshObjects held in different MeshBases" );
        }
        
        MeshObject [] starts;
        if( considerEquivalents ) {
            starts = getEquivalents().getMeshObjects();
        } else {
            starts = new MeshObject[] { this };
        }
        
        MeshObjectIdentifier [][] otherSides = new MeshObjectIdentifier[ starts.length ][];
        RoleType [][][]     roleTypes  = new RoleType[ starts.length ][][];

        int n=0;
        for( int s=0 ; s<starts.length ; ++s ) {
            AMeshObject current = (AMeshObject) starts[s];
            synchronized( current ) {
                otherSides[s] = current.theOtherSides;
                roleTypes[s]  = current.theRoleTypes;
            }
            if( roleTypes[s] != null ) {
                for( int i=0 ; i<roleTypes[s].length ; ++i ) {
                    if( roleTypes[s][i] != null ) {
                        n += roleTypes[s][i].length;
                    }
                }
            }
        }

        updateLastRead();

        if( n==0 ) {
            return new RoleType[0];
        }

        MeshObjectIdentifier other = otherObject.getIdentifier();
        RoleType [] ret = new RoleType[ n ];
        int max = 0;

        for( int s=0 ; s<starts.length ; ++s ) {

            if( otherSides[s] == null ) {
                continue;
            }
            
            for( int i=0 ; i<otherSides[s].length ; ++i ) {
                if( other.equals( otherSides[s][i] )) {
                    if( roleTypes[s][i] != null ) {
                        RoleType [] temp = new RoleType[ roleTypes[s][i].length ];

                        for( int j=0 ; j<roleTypes[s][i].length ; ++j ) {
                            try {
                                checkPermittedTraversal( roleTypes[s][i][j], otherObject );

                                ret[max++] = roleTypes[s][i][j];
                            } catch( NotPermittedException ex ) {
                                log.info( ex );
                            }
                        }
                    }
                }
            }
        }
        if( max < ret.length ) {
            ret = ArrayHelper.copyIntoNewArray( ret, 0, max, RoleType.class );
        }
        return ret;
    }

    /**
     * Add another MeshObject as an equivalent. All MeshObjects that are already equivalent
     * to this MeshObject, and all MeshObjects that are already equivalent to the newly
     * added MeshObject, are now equivalent.
     *
     * @param equiv the new equivalent
     * @throws EquivalentAlreadyException thrown if the provided MeshObject is already an equivalent of this MeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void addAsEquivalent(
            MeshObject equiv )
        throws
            EquivalentAlreadyException,
            TransactionException,
            NotPermittedException
    {
        internalAddAsEquivalent( equiv, true, 0L );
    }

    /**
     * Internal helper to implement a method. While on this level, it does not appear that
     * factoring out this method makes any sense, subclasses may appreciate it.
     * 
     * @param equiv the new equivalent
     * @param isMaster is true, this is the master replica
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws EquivalentAlreadyException thrown if the provided MeshObject is already an equivalent of this MeshObject
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    protected synchronized void internalAddAsEquivalent(
            MeshObject equiv,
            boolean    isMaster,
            long       timeUpdated )
        throws
            EquivalentAlreadyException,
            TransactionException,
            NotPermittedException
    {
        checkAlive();
        equiv.checkAlive();
        checkTransaction();

        // first check whether we have it already;
        if( equiv == null ) {
            throw new NullPointerException();
        }
        if( this == equiv ) {
            throw new EquivalentAlreadyException( this, equiv );
        }
        
        if( theMeshBase != equiv.getMeshBase() ) {
            throw new IllegalArgumentException( "Cannot relate MeshObjects held in different MeshBases" );
        }

        AMeshObject temp = this;
        while( ( temp = temp.getLeftEquivalentObject( theMeshBase ) ) != null ) {
            if( temp == equiv ) {
                throw new EquivalentAlreadyException( this, equiv );
            }
        }
        temp = this;
        while( ( temp = temp.getRightEquivalentObject( theMeshBase ) ) != null ) {
            if( temp == equiv ) {
                throw new EquivalentAlreadyException( this, equiv );
            }
        }
        
        checkPermittedAddAsEquivalent( equiv );

        // now insert, being mindful that we might be joining to chains here
        
        AMeshObject leftMostHere = this;
        while( ( temp = leftMostHere.getLeftEquivalentObject( theMeshBase )) != null ) {
            leftMostHere = temp;
        }

        AMeshObject rightMostThere = (AMeshObject) equiv;
        while( ( temp = rightMostThere.getRightEquivalentObject( theMeshBase ) ) != null ) {
            rightMostThere = temp;
        }

        if( rightMostThere.theEquivalenceSetPointers == null ) {
            rightMostThere.theEquivalenceSetPointers = new MeshObjectIdentifier[2];
        }
        rightMostThere.theEquivalenceSetPointers[1] = leftMostHere.getIdentifier();

        if( leftMostHere.theEquivalenceSetPointers == null ) {
            leftMostHere.theEquivalenceSetPointers = new MeshObjectIdentifier[2];
        }
        leftMostHere.theEquivalenceSetPointers[0] = rightMostThere.getIdentifier();
        
        updateLastUpdated( timeUpdated, theTimeUpdated );
    }
    
    /**
     * Obtain the set of MeshObjects, including this one, that are equivalent.
     * This always returns at least this MeshObject.
     *
     * @return the set of MeshObjects that are equivalent
     */
    public synchronized MeshObjectSet getEquivalents()
    {
        checkAlive();

        AMeshBase realBase = (AMeshBase) theMeshBase;
        
        if( theEquivalenceSetPointers == null ) {
            return realBase.getMeshObjectSetFactory().createSingleMemberImmutableMeshObjectSet( this );
        }

        ArrayList<MeshObject> toTheLeft  = new ArrayList<MeshObject>();
        ArrayList<MeshObject> toTheRight = new ArrayList<MeshObject>();

        AMeshObject current = this;
        while( ( current = current.getLeftEquivalentObject( theMeshBase ) ) != null ) {
            toTheLeft.add( current );
        }
        current = this;
        toTheRight.add( current );
        while( ( current = current.getRightEquivalentObject( theMeshBase ) ) != null ) {
            toTheRight.add( current );
        }
        
        // we revert the direction of the toTheLeft, in order to make debugging easier
        ArrayList<MeshObject> allEquivalents = new ArrayList<MeshObject>( toTheLeft.size() );
        for( int i=toTheLeft.size()-1 ; i>=0 ; --i ) {
            allEquivalents.add( toTheLeft.get( i ));
        }
        for( MeshObject loop : toTheRight ) {
            allEquivalents.add( loop );
        }
        
        MeshObjectSet ret = realBase.getMeshObjectSetFactory().createImmutableMeshObjectSet(
                ArrayHelper.copyIntoNewArray( allEquivalents, MeshObject.class ));
        
        updateLastRead();
        
        return ret;
    }
    
    /**
     * Remove this MeshObject as an equivalent from the set of equivalents. If this MeshObject
     * is not currently equivalent to any other MeshObject, this does nothing.
     *
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void removeAsEquivalent()
        throws
            TransactionException,
            NotPermittedException
    {
        internalRemoveAsEquivalent( true, 0L );
    }
    
    /**
     * Internal helper to implement a method. While on this level, it does not appear that
     * factoring out this method makes any sense, subclasses may appreciate it.
     * 
     * @param isMaster if true, this is the master replica
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    protected void internalRemoveAsEquivalent(
            boolean isMaster,
            long    timeUpdated )
        throws
            TransactionException,
            NotPermittedException
    {
        checkAlive();
        checkTransaction();
        
        checkPermittedRemoveAsEquivalent();
        
        AMeshObject theLeft  = getLeftEquivalentObject( theMeshBase );
        AMeshObject theRight = getRightEquivalentObject( theMeshBase );

        if( theLeft != null ) {
            theLeft.theEquivalenceSetPointers[1] = ( theRight != null ) ? theRight.getIdentifier() : null;
        }
        if( theRight != null ) {
            theRight.theEquivalenceSetPointers[0] = ( theLeft != null ) ? theLeft.getIdentifier() : null;
        }
        
        theEquivalenceSetPointers = null;

        updateLastUpdated( timeUpdated, theTimeUpdated );
    }

    /**
     * For clients that know we are an AMeshObject, we can also return our internal representation.
     * Please do not modify the content of this array, bad things may happen.
     *
     * @return the MeshObjectIdentifiers of the left and right equivalent MeshObject. Either may be null. The
     *         return value may be null, too.
     */
    public MeshObjectIdentifier[] getInternalEquivalentList()
    {
        return theEquivalenceSetPointers;
    }

    /**
     * Delete this MeshObject. This must only be invoked by our MeshObjectLifecycleManager
     * and thus is defined down here, not higher up in the inheritance hierarchy.
     * 
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    public void delete()
        throws
            TransactionException,
            NotPermittedException
    {
        internalDelete( true, 0L );
    }
    
    /**
     * Internal helper to implement a method. While on this level, it does not appear that
     * factoring out this method makes any sense, subclasses may appreciate it.
     * 
     * @param isMaster true if this is the master replica
     * @param timeUpdated the value for the timeUpdated property after this operation. -1 indicates "don't change"
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     */
    protected void internalDelete(
            boolean isMaster,
            long    timeUpdated )
        throws
            TransactionException,
            NotPermittedException
    {
        if( theMeshBase == null ) {
            // this is a loop, do nothing
            return;
        }
        MeshBase oldMeshBase = theMeshBase;

        AMeshObject theLeft  = getLeftEquivalentObject( oldMeshBase );
        AMeshObject theRight = getRightEquivalentObject( oldMeshBase );
        if( theLeft != null ) {
            if( theRight != null ) {
                // we are in the middle
                theLeft.theEquivalenceSetPointers[1]  = theRight.theIdentifier;
                theRight.theEquivalenceSetPointers[0] = theLeft.theIdentifier;
            } else {
                if( theLeft.theEquivalenceSetPointers[0] == null ) {
                    theLeft.theEquivalenceSetPointers = null; // don't need them any more
                } else {
                    theLeft.theEquivalenceSetPointers[1] = null;
                }
            }
        } else if( theRight != null ) {
            if( theRight.theEquivalenceSetPointers[1] == null ) {
                theRight.theEquivalenceSetPointers = null; // don't need them any more
            } else {
                theRight.theEquivalenceSetPointers[0] = null;
            }
        }

        if( theOtherSides != null ) {
            // we got to copy this array, otherwise we change it under our own feet
            MeshObject [] realOtherSides = new MeshObject[ theOtherSides.length ];
            for( int i=0 ; i<theOtherSides.length ; ++i ) {
                realOtherSides[i] = oldMeshBase.findMeshObjectByIdentifier( theOtherSides[i] );
            }
            for( int i=0 ; i<realOtherSides.length ; ++i ) {
                if( realOtherSides[i] != null ) {
                    try {
                        internalUnrelate( realOtherSides[i], oldMeshBase, isMaster, timeUpdated );
                    } catch( NotRelatedException ex ) {
                        // that's fine, ignore
                    } catch( NotPermittedException ex ) {
                        log.error( ex );
                    }
                } else {
                    // that's fine, ignore -- may happen when a Probe drops both source and
                    // destination at the same time
                }
            }
        }

        MeshObjectIdentifier canonicalMeshObjectName = getIdentifier();
        
        theMeshBase = null; // this needs to happen rather late so the other code still works

        fireDeleted( oldMeshBase, canonicalMeshObjectName, System.currentTimeMillis() );
    }

    /**
     * Internal helper to obtain the left equivalent MeshObject, if any.
     * We pass in the MeshBase to use because this may be invoked when a MeshObject's member
     * variable has been zero'd out already.
     *
     * @param mb the MeshBase to use
     * @return the left equivalent MeshObject, if any
     */
    protected AMeshObject getLeftEquivalentObject(
            MeshBase mb )
    {
        if( theEquivalenceSetPointers == null ) {
            return null;
        }
        if( theEquivalenceSetPointers[0] == null ) {
            return null;
        }
        MeshObject ret = findRelatedMeshObject( mb, theEquivalenceSetPointers[0] );
        return (AMeshObject) ret;
    }

    /**
     * Internal helper to obtain the right equivalent MeshObject, if any.
     * We pass in the MeshBase to use because this may be invoked when a MeshObject's member
     * variable has been zero'd out already.
     *
     * @param mb the MeshBase to use
     * @return the left equivalent MeshObject, if any
     */
    protected AMeshObject getRightEquivalentObject(
            MeshBase mb )
    {
        if( theEquivalenceSetPointers == null ) {
            return null;
        }
        if( theEquivalenceSetPointers[1] == null ) {
            return null;
        }
        MeshObject ret = findRelatedMeshObject( mb, theEquivalenceSetPointers[1] );
        return (AMeshObject) ret;
    }

    /**
     * Obtain the same MeshObject as ExternalizedMeshObject so it can be easily serialized.
     * 
     * @return this MeshObject as ExternalizedMeshObject
     */
    public SimpleExternalizedMeshObject asExternalized()
    {
        MeshTypeIdentifier [] types;
        if( theMeshTypes != null && theMeshTypes.size() > 0 ) {
            types = new MeshTypeIdentifier[ theMeshTypes.size() ];

            int i=0;
            for( EntityType current : theMeshTypes.keySet() ) {
                types[i++] = current.getIdentifier();
            }
        } else {
            types = null;
        }
        
        MeshTypeIdentifier [] propertyTypes;
        PropertyValue      [] propertyValues;
        if( theProperties != null && theProperties.size() > 0 ) {
            propertyTypes  = new MeshTypeIdentifier[ theProperties.size() ];
            propertyValues = new PropertyValue[ propertyTypes.length ];

            int i=0;
            for( PropertyType current : theProperties.keySet() ) {
                propertyTypes[i]  = current.getIdentifier();
                propertyValues[i] = theProperties.get( current );
                ++i;
            }
        } else {
            propertyTypes  = null;
            propertyValues = null;
        }
        
        MeshTypeIdentifier [][] roleTypes;
        if( theOtherSides != null && theOtherSides.length > 0 ) {
            roleTypes = new MeshTypeIdentifier[ theOtherSides.length][];
            for( int i=0 ; i<theOtherSides.length ; ++i ) {
                if( theRoleTypes[i] != null && theRoleTypes[i].length > 0 ) {
                    roleTypes[i] = new MeshTypeIdentifier[ theRoleTypes[i].length ];
                    for( int j=0 ; j<roleTypes[i].length ; ++j ) {
                        roleTypes[i][j] = theRoleTypes[i][j].getIdentifier();
                    }
                }
            }
        } else {
            roleTypes = null;
        }
        
        MeshObjectIdentifier [] equivalents;
        if( theEquivalenceSetPointers == null ) {
            equivalents = null;
        } else {
            int count = 0;
            if( theEquivalenceSetPointers[0] != null ) {
                ++count;
            }
            if( theEquivalenceSetPointers[1] != null ) {
                ++count;
            }
            equivalents = new MeshObjectIdentifier[count];
            if( theEquivalenceSetPointers[0] != null ) {
                equivalents[0] = theEquivalenceSetPointers[0];
                if( theEquivalenceSetPointers[1] != null ) {
                    equivalents[1] = theEquivalenceSetPointers[1];
                }
            } else if( theEquivalenceSetPointers[1] != null ) {
                equivalents[0] = theEquivalenceSetPointers[1];
            }
        }
        
        SimpleExternalizedMeshObject ret = SimpleExternalizedMeshObject.create(
                getIdentifier(),
                types,
                theTimeCreated,
                theTimeUpdated,
                theTimeRead,
                theTimeExpires,
                propertyTypes,
                propertyValues,
                theOtherSides,
                roleTypes,
                equivalents );

        return ret;
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        boolean isDefaultMeshBase = context != null ? ( getMeshBase().equals( context.get( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY ))) : true;
        String  contextPath       = context != null ? (String) context.get(  StringRepresentationContext.WEB_CONTEXT_KEY ) : null;
        
        String key;
        if( isDefaultMeshBase ) {
            if( isHomeObject() ) {
                key = DEFAULT_MESH_BASE_HOME_ENTRY;
            } else {
                key = DEFAULT_MESH_BASE_ENTRY;
            }
        } else {
            if( isHomeObject() ) {
                key = NON_DEFAULT_MESH_BASE_HOME_ENTRY;
            } else {
                key = NON_DEFAULT_MESH_BASE_ENTRY;
            }
        }

        String meshObjectExternalForm = theIdentifier.toExternalForm();
        String meshBaseExternalForm   = theMeshBase.getIdentifier().toExternalForm();

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                key,
                meshObjectExternalForm,
                contextPath,
                meshBaseExternalForm );

        return ret;
    }

    /**
     * Obtain the start part of a String representation of this MeshBase that acts
     * as a link/hyperlink and can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkStart(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        boolean isDefaultMeshBase = context != null ? ( getMeshBase().equals( context.get( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY ))) : true;
        String  contextPath       = context != null ? (String) context.get(  StringRepresentationContext.WEB_CONTEXT_KEY ) : null;

        String key;
        if( isDefaultMeshBase ) {
            if( isHomeObject() ) {
                key = DEFAULT_MESH_BASE_HOME_LINK_START_ENTRY;
            } else {
                key = DEFAULT_MESH_BASE_LINK_START_ENTRY;
            }
        } else {
            if( isHomeObject() ) {
                key = NON_DEFAULT_MESH_BASE_HOME_LINK_START_ENTRY;
            } else {
                key = NON_DEFAULT_MESH_BASE_LINK_START_ENTRY;
            }
        }

        String meshObjectExternalForm = theIdentifier.toExternalForm();
        String meshBaseExternalForm = theMeshBase.getIdentifier().toExternalForm();
        
        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                key,
                meshObjectExternalForm,
                contextPath,
                meshBaseExternalForm );

        return ret;        
    }

    /**
     * Obtain the end part of a String representation of this MeshBase that acts
     * as a link/hyperlink and can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkEnd(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        boolean isDefaultMeshBase = context != null ? ( getMeshBase().equals( context.get( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY ))) : true;
        String  contextPath       = context != null ? (String) context.get(  StringRepresentationContext.WEB_CONTEXT_KEY ) : null;

        String key;
        if( isDefaultMeshBase ) {
            if( isHomeObject() ) {
                key = DEFAULT_MESH_BASE_HOME_LINK_END_ENTRY;
            } else {
                key = DEFAULT_MESH_BASE_LINK_END_ENTRY;
            }
        } else {
            if( isHomeObject() ) {
                key = NON_DEFAULT_MESH_BASE_HOME_LINK_END_ENTRY;
            } else {
                key = NON_DEFAULT_MESH_BASE_LINK_END_ENTRY;
            }
        }

        String meshObjectExternalForm = theIdentifier.toExternalForm();
        String meshBaseExternalForm   = theMeshBase.getIdentifier().toExternalForm();

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                key,
                meshObjectExternalForm,
                contextPath,
                meshBaseExternalForm );

        return ret;
    }
    
    /**
     * The set of MeshObjecs to which this MeshObject is directly related. This is
     * expressed as a set of MeshObjectIdentifiers in order to not prevent garbage collection.
     */
    protected MeshObjectIdentifier [] theOtherSides;

    /**
     * The set of sets of RoleTypes that goes with theOtherSides.
     */
    protected RoleType [][] theRoleTypes;
    
    /**
     * The left and right MeshObject in the equivalence set. This member is either null,
     * or MeshObjectIdentifier[2], which may have one or two entries, the first representing
     * the "left" side in the doubly-linked list, the second representing the "right"
     * side.
     */
    protected MeshObjectIdentifier [] theEquivalenceSetPointers;

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_ENTRY = "DefaultMeshBaseString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_HOME_ENTRY = "DefaultMeshBaseHomeString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_LINK_START_ENTRY = "DefaultMeshBaseLinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_HOME_LINK_START_ENTRY = "DefaultMeshBaseHomeLinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_LINK_END_ENTRY = "DefaultMeshBaseLinkEndString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_HOME_LINK_END_ENTRY = "DefaultMeshBaseHomeLinkEndString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_ENTRY = "NonDefaultMeshBaseString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_HOME_ENTRY = "NonDefaultMeshBaseHomeString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_LINK_START_ENTRY = "NonDefaultMeshBaseLinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_HOME_LINK_START_ENTRY = "NonDefaultMeshBaseHomeLinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_LINK_END_ENTRY = "NonDefaultMeshBaseLinkEndString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_HOME_LINK_END_ENTRY = "NonDefaultMeshBaseHomeLinkEndString";
}
