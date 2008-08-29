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

package org.infogrid.model.AclBasedSecurity.accessmanager;

import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;

import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.meshbase.security.AbstractAccessManager;
import org.infogrid.meshbase.security.IdentityChangeException;
import org.infogrid.meshbase.transaction.TransactionException;

import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.Role;
import org.infogrid.model.primitives.RoleType;

import org.infogrid.model.AclBasedSecurity.AclBasedSecuritySubjectArea;

import org.infogrid.util.logging.Log;

/**
 * A simple implementation of AccessManager using Access Control List. This simple
 * implementation should only be used in environments where arbitrary clients cannot
 * start calling arbitrary APIs.
 */
public class AclBasedAccessManager
        extends
            AbstractAccessManager
{
    private static final Log log = Log.getLogInstance( AclBasedAccessManager.class ); // our own, private logger
    
    /**
     * Factory method.
     *
     * @return the created SimpleAccessManager
     */
    public static AclBasedAccessManager create()
    {
        return new AclBasedAccessManager();
    }

    /**
     * Constructor for subclasses only, use factory method.
     */
    protected AclBasedAccessManager()
    {
        // noop
    }

    /**
     * Assign the second MeshObject to be the owner of the first MeshObject. This
     * must only be called if the current Thread has an open Transaction.
     *
     * @param toBeOwned the MeshObject to be owned by the new owner
     * @param newOwner the MeshObject that is the new owner.
     * @throws TransactionException thrown if this is invoked outside of proper transaction boundaries
     */
    public void assignOwner(
            MeshObject toBeOwned,
            MeshObject newOwner )
        throws
            TransactionException
    {
        try {
            sudo();
            toBeOwned.relateAndBless( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getSource(), newOwner );

        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } catch( EntityNotBlessedException ex ) {
            log.error( ex );

        } catch( RelatedAlreadyException ex ) {
            log.error( ex );

        } catch( IsAbstractException ex ) {
            log.error( ex );

        } catch( NotPermittedException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }
    }
    
    /**
     * Check whether it is permitted to semantically create a MeshObject with the provided
     * MeshObjectIdentifier.
     * 
     * @param identifier the MeshObjectIdentifier
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedCreate(
            MeshObjectIdentifier identifier )
        throws
            NotPermittedException
    {
        // always allowed -- FIXME?
    }
    
    /**
     * Check whether it is permitted to set a MeshObject's auto-delete time to the given value.
     * Subclasses may override this.
     *
     * @param obj the MeshObject
     * @param newValue the proposed new value for the auto-delete time
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedSetTimeExpires(
            MeshObject obj,
            long       newValue )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            for( Role current : obj.getRoles( false ) ) {
                current.getRoleType().checkPermittedSetTimeExpires( obj, newValue, caller );
            }
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }
    }

    /**
     * Check whether it is permitted to set a MeshObject's given property to the given
     * value. Subclasses may override this.
     *
     * @param obj the MeshObject
     * @param thePropertyType the PropertyType identifing the property to be modified
     * @param newValue the proposed new value for the property
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedSetProperty(
            MeshObject    obj,
            PropertyType  thePropertyType,
            PropertyValue newValue )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            thePropertyType.checkPermittedSetProperty( obj, newValue, caller );

        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }
    }

    
    /**
     * Check whether it is permitted to obtain a MeshObject's given property.
     *
     * @param obj the MeshObject
     * @param thePropertyType the PropertyType identifing the property to be read
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedGetProperty(
            MeshObject   obj,
            PropertyType thePropertyType )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            thePropertyType.checkPermittedGetProperty( obj, caller );
        
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }
    }

    /**
     * Check whether it is permitted to determine whether or not a MeshObject is blessed with
     * the given type.
     * 
     * @param obj the MeshObject
     * @param type the EntityType whose blessing we wish to check
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedBlessedBy(
            MeshObject obj,
            EntityType type )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            type.checkPermittedBlessedBy( obj, caller );
        
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }
    }

    /**
     * Check whether it is permitted to bless a MeshObject with the given EntityTypes.
     * 
     * @param obj the MeshObject
     * @param types the EntityTypes with which to bless
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedBless(
            MeshObject    obj,
            EntityType [] types )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            // we ask the new EntityTypes, and we ask the existing RoleTypes

            for( EntityType current : types ) {
                current.checkPermittedBless( obj, caller );
            }

            Role [] roles = obj.getRoles( false );
            for( Role current : roles ) {
                current.getRoleType().checkPermittedIncrementalBless( obj, current.getOtherSide(), types, caller );
            }
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }        
    }

    /**
     * Check whether it is permitted to unbless a MeshObject from the given EntityTypes.
     * 
     * @param obj the MeshObject
     * @param types the EntityTypes from which to unbless
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedUnbless(
            MeshObject    obj,
            EntityType [] types )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            for( EntityType current : types ) {
                current.checkPermittedUnbless( obj, caller );
            }

            Role [] roles = obj.getRoles( false );
            for( Role current : roles ) {
                current.getRoleType().checkPermittedIncrementalUnbless( obj, current.getOtherSide(), types, caller );
            }
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }                
    }

    /**
     * Check whether it is permitted to bless the relationship to the otherObject with the
     * provided RoleTypes.
     * 
     * @param obj the MeshObject
     * @param thisEnds the RoleTypes to bless the relationship with
     * @param otherObject the neighbor to which this MeshObject is related
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedBless(
            MeshObject  obj,
            RoleType [] thisEnds,
            MeshObject  otherObject )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();
        
            for( RoleType current : thisEnds ) {
                RoleType other = current.getOtherRoleType();
                current.checkPermittedBless( obj, otherObject, caller );
                  other.checkPermittedBless( otherObject, obj, caller );
            }

            Role [] roles = obj.getRoles( false );
            for( Role current : roles ) {
                current.getRoleType().checkPermittedIncrementalBless( obj, current.getOtherSide(), thisEnds, otherObject, caller );
                current.getRoleType().getOtherRoleType().checkPermittedIncrementalBless( current.getOtherSide(), obj, thisEnds, otherObject, caller );
            }
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }        
    }

    /**
     * Check whether it is permitted to unbless the relationship to the otherObject from the
     * provided RoleTypes.
     * 
     * @param obj the MeshObject
     * @param thisEnds the RoleTypes to unbless the relationship from
     * @param otherObject the neighbor to which this MeshObject is related
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedUnbless(
            MeshObject  obj,
            RoleType [] thisEnds,
            MeshObject  otherObject )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();
        
            for( RoleType current : thisEnds ) {
                RoleType other = current.getOtherRoleType();
                current.checkPermittedUnbless( obj,         otherObject, caller );
                  other.checkPermittedUnbless( otherObject, obj,         caller );
            }

            Role [] roles = obj.getRoles( false );
            for( Role current : roles ) {
                current.getRoleType().checkPermittedIncrementalUnbless( obj, current.getOtherSide(), thisEnds, otherObject, caller );
                current.getRoleType().getOtherRoleType().checkPermittedIncrementalUnbless( current.getOtherSide(), obj, thisEnds, otherObject, caller );
            }
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }        
    }
    
    /**
     * Check whether it is permitted to traverse the given ByRoleType from this MeshObject to the
     * given MeshObject.
     * 
     * @param obj the MeshObject
     * @param toTraverse the RoleType to traverse
     * @param otherObject the reached MeshObject in the traversal
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedTraversal(
            MeshObject obj,
            RoleType   toTraverse,
            MeshObject otherObject )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            toTraverse.checkPermittedTraversal( obj, otherObject, caller );

        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }        
    }

    /**
     * Check whether it is permitted to bless the relationship with the given otherObject with
     * the given thisEnds RoleTypes.
     * 
     * @param obj the MeshObject
     * @param thisEnds the RoleTypes to bless the relationship with
     * @param otherObject the neighbor to which this MeshObject is related
     * @param roleTypesToAsk the RoleTypes, of the relationship with RoleTypesToAskUsed, which to as
     * @param roleTypesToAskUsed the neighbor MeshObject whose rules may have an opinion on the blessing of the relationship with otherObject
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedBless(
            MeshObject  obj,
            RoleType [] thisEnds,
            MeshObject  otherObject,
            RoleType [] roleTypesToAsk,
            MeshObject  roleTypesToAskUsed )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            for( RoleType current : roleTypesToAsk ) {
                current.checkPermittedIncrementalBless( obj, roleTypesToAskUsed, thisEnds, otherObject, caller );
            }
        
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }        
    }

    /**
     * Check whether it is permitted to unbless the relationship from the given otherObject with
     * the given thisEnds RoleTypes. Subclasses
     * may override this.
     * 
     * @param obj the MeshObject
     * @param thisEnds the RoleTypes to unbless the relationship from
     * @param otherObject the neighbor to which this MeshObject is related
     * @param roleTypesToAsk the RoleTypes, of the relationship with RoleTypesToAskUsed, which to as
     * @param roleTypesToAskUsed the neighbor MeshObject whose rules may have an opinion on the blessing of the relationship with otherObject
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedUnbless(
            MeshObject  obj,
            RoleType [] thisEnds,
            MeshObject  otherObject,
            RoleType [] roleTypesToAsk,
            MeshObject  roleTypesToAskUsed )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            for( RoleType current : roleTypesToAsk ) {
                current.checkPermittedIncrementalUnbless( obj, roleTypesToAskUsed, thisEnds, otherObject, caller );
            }
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }        
    }

    /**
     * Check whether it is permitted to make one MeshObject equivalent to another.
     * 
     * @param one the first MeshObject
     * @param two the second MeshObject
     * @param roleTypesOneToAsk the RoleTypes, of MeshObject one, to ask
     * @param roleTypesTwoToAsk the RoleTypes, of MeshObject two, to ask
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedAddAsEquivalent(
            MeshObject  one,
            RoleType [] roleTypesOneToAsk,
            MeshObject  two,
            RoleType [] roleTypesTwoToAsk )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            for( RoleType current : roleTypesOneToAsk ) {
                current.checkPermittedAddAsEquivalent( one, two, caller );
            }
            for( RoleType current : roleTypesTwoToAsk ) {
                current.checkPermittedAddAsEquivalent( two, one, caller );
            }
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }        
    }

    /**
     * Check whether it is permitted to remove a MeshObject from the equivalence set
     * it is currently a member of.
     * Subclasses may override this.
     * 
     * @param obj the MeshObject to remove
     * @param roleTypesToAsk the RoleTypes to ask
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedRemoveAsEquivalent(
            MeshObject  obj,
            RoleType [] roleTypesToAsk )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            for( RoleType current : roleTypesToAsk ) {
                current.checkPermittedRemoveAsEquivalent( obj, caller );
            }
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }        
    }

    /**
     * Check whether it is permitted to delete this MeshObject. This checks both whether the
     * MeshObject itself may be deleted, and whether the relationships it participates in may
     * be deleted (which in turn depends on whether the relationships may be unblessed).
     *
     * @param obj the MeshObject
     * @throws NotPermittedException thrown if it is not permitted
     */
    public void checkPermittedDelete(
            MeshObject obj )
        throws
            NotPermittedException
    {
        try {
            sudo();
            
            MeshObject caller = getCaller();

            EntityType [] allTypes = obj.getTypes();
 
            checkPermittedUnbless( obj, allTypes );
        
            for( Role current : obj.getRoles( false ) ) {
                checkPermittedUnbless( obj,                    new RoleType[] { current.getRoleType() },                    current.getOtherSide() );
                checkPermittedUnbless( current.getOtherSide(), new RoleType[] { current.getRoleType().getOtherRoleType() }, obj );
            }
        } catch( IdentityChangeException ex ) {
            log.error( ex );

        } finally {
            sudone();
        }        
    }
}
