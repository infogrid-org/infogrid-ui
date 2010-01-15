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

package org.infogrid.scene;

import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.MeshBaseLifecycleManager;

/**
 * <p>A SceneTemplate defines the structure of a Scene. Just like a type object with respect
 * to an instance object, a SceneTemplate defines certain placeholders for MeshObjects, which
 * are bound to actual MeshObjects when the SceneTemplate is instantiate into a Scene.</p>
 * 
 * <p>There can be zero to many Scenes per SceneTemplate.</p>
 */
public abstract class SceneTemplate
{
    /**
     * Construct a SceneTemplate from a collection of SceneTemplateRoles. Most of the actual construction
     * takes place in subclasses.
     *
     * @param directory the SceneTemplateDirectory to which this SceneTemplate will belong
     * @param rootRole  the root SceneTemplateRole, played by one mandatory MeshObject
     * @param entityRoles the non-root SceneTemplateRoles that are played by zero or more MeshObjects
     * @param sceneRoles the non-root SceneTemplateRoles that are played by zero or more other Scenes
     */
    protected SceneTemplate(
            SceneTemplateDirectory                  directory,
            SceneTemplateRole.Root                  rootRole,
            SceneTemplateRole.PlayedByMeshObject [] entityRoles,
            SceneTemplateRole.PlayedByScene      [] sceneRoles )
    {
        if( directory == null ) {
            throw new IllegalArgumentException();
        }
        if( rootRole == null ) {
            throw new IllegalArgumentException();
        }

        theDirectory   = directory;
        theRootRole    = rootRole;
        if( entityRoles != null ) { // makes life easier
            theEntityRoles = entityRoles;
        } else {
            theEntityRoles = new SceneTemplateRole.PlayedByMeshObject[0];
        }
        if( sceneRoles != null ) {
            theSceneRoles = sceneRoles;
        } else {
            theSceneRoles = new SceneTemplateRole.PlayedByScene[0];
        }
    }

    /**
     * <p>Allows subclasses to set the SceneTemplateAdditionCandidates.</p>
     *
     * <p>I would like to have this in the constructor, but we can't because SceneTemplateAdditionCandidates
     * usually require ModelBase lookups, and that can't really be put into a constructor. This array
     * must be specified in the same sequence as the array of SceneTemplateRoles specified in the constructor.</p>
     *
     * @param entityRoleAdditionCandidates the array of SceneTemplateAdditionCandidates per SceneTemplateRole
     * @see #getPotentialAdditionCandidates
     */
    protected void setPotentialAdditionCandidates(
            SceneTemplateAdditionCandidate [][] entityRoleAdditionCandidates )
    {
        if( entityRoleAdditionCandidates == null ) {
            thePotentialAdditionCandidates = new SceneTemplateAdditionCandidate[theEntityRoles.length][];
        } else if( entityRoleAdditionCandidates.length != theEntityRoles.length ) {
            throw new IllegalArgumentException();
        } else {
            thePotentialAdditionCandidates = entityRoleAdditionCandidates;
        }
    }

    /**
     * Obtain this SceneTemplate's root SceneTemplateRole.
     *
     * @return the SceneTemplate's root SceneTemplateRole
     */
    public final SceneTemplateRole.Root getTemplateRootRole()
    {
        return theRootRole;
    }

    /**
     * Obtain this SceneTemplate's SceneTemplateRoles that are played by MeshObjects
     * and that are not root.
     *
     * @return the SceneTemplate's entity SceneTemplateRoles
     */
    public final SceneTemplateRole.PlayedByMeshObject [] getTemplatePlayedByMeshObjectRoles()
    {
        return theEntityRoles;
    }

    /**
     * Obtain this SceneTemplate's SceneTemplateRoles that are played by Scenes
     * and that are not root.
     *
     * @return the SceneTemplate's scene SceneTemplateRoles
     */
    public final SceneTemplateRole.PlayedByScene [] getTemplatePlayedBySceneRoles()
    {
        return theSceneRoles;
    }

    /**
     * Obtain all of this SceneTemplate's SceneTemplateRoles.
     *
     * @return all SceneTemplateRoles of the SceneTemplate
     */
    public final SceneTemplateRole [] getAllTemplateRoles()
    {
        int count = 1;
        if( theEntityRoles != null ) {
            count += theEntityRoles.length;
        }
        if( theSceneRoles != null ) {
            count += theSceneRoles.length;
        }
        SceneTemplateRole [] ret = new SceneTemplateRole[ count ];

        if( theSceneRoles != null ) {
            for( int i=theSceneRoles.length-1 ; i>=0 ; --i ) {
                ret[--count] = theSceneRoles[i];
            }
        }
        if( theEntityRoles != null ) {
            for( int i=theEntityRoles.length-1 ; i>=0 ; --i ) {
                ret[--count] = theEntityRoles[i];
            }
        }
        ret[0] = theRootRole;
        return ret;
    }

    /**
     * Obtain the SceneTemplateAdditionCandidates that go with a particular SceneTemplateRole.
     *
     * @param role the SceneTemplateRole for which the SceneTemplateAdditionCandidates shall be determined
     * @return the SceneTemplateAdditionCandidates that go with this SceneTemplateRole
     * @see #setPotentialAdditionCandidates
     */
    public final SceneTemplateAdditionCandidate [] getPotentialAdditionCandidates(
            SceneTemplateRole role )
    {
        if( thePotentialAdditionCandidates == null ) { // not initialized
            throw new IllegalStateException();
        }

        for( int i=0 ; i<theEntityRoles.length ; ++i ) {
            if( role == theEntityRoles[i] ) {
                return thePotentialAdditionCandidates[i];
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * <p>Attempt to match a set of matched SceneRoles against this SceneTemplate. If successful,
     * it instantiates and returns the actual Scene. If the match is unsuccessful, returns null.
     * If the match is under-specified and a MeshBaseLifecycleManager is given, it may attempt
     * to complete the Scene by creating new MeshObjects. If such a completion is ambiguous,
     * it will throw an Exception.</p>
     *
     * <p>This is declared as protected, as it is only supposed to be invoked by the framework, not the
     * application developer. The application developer uses the methods provided on SceneDirectory.</p>
     *
     * @param matchedRootRole the root SceneRole
     * @param matchedEntityRolesSubset the specified subset of entity SceneRoles
     * @param matchedSceneRolesSubset the specified subset of scene SceneRoles
     * @param life the MeshBaseLifecycleManager used by the Scene to complete itself if necessary
     * @param directory the SceneDirectory in which other Scenes can be found
     * @return the created Scene, or null
     * @throws MatchAmbiguousException thrown if the match is ambiguous
     */
    protected abstract Scene match(
            SceneRole.Root                  matchedRootRole,
            SceneRole.PlayedByMeshObject [] matchedEntityRolesSubset,
            SceneRole.PlayedByScene []      matchedSceneRolesSubset,
            MeshBaseLifecycleManager        life,
            SceneDirectory                  directory )
        throws
            MatchAmbiguousException;

    /**
     * Obtain the SceneTemplateDirectory that we use.
     *
     * @return the SceneTemplateDirectory that we use
     */
    public final SceneTemplateDirectory getSceneTemplateDirectory()
    {
        return theDirectory;
    }

    /**
     * This internal helper constructs a name String that can be handed over to
     * the factory methods of our ActiveMeshObjectSets and ActiveTraversalPathSets.
     * This name is used for debugging only.
     *
     * @param root the root MeshObject
     * @param role the SceneTemplateRole of the root MeshObject
     * @return the created name
     */
    protected String constructSceneWithRoleName(
            MeshObject        root,
            SceneTemplateRole role )
    {
        return getClass().getName() + ": root: " + root.getIdentifier() + ", role: " + role;
    }

    /**
     * This internal helper constructs a name String that can be handed over to
     * the factory methods of our ActiveMeshObjectSets and ActiveTraversalPathSets.
     * This name is used for debugging only.
     *
     * @param root the root MeshObject
     * @param role the SceneTemplateRole of the root RootEntity
     * @param postfix a string to append
     * @return the created name
     */
    protected String constructSceneWithRoleName(
            MeshObject        root,
            SceneTemplateRole role,
            String            postfix )
    {
        return getClass().getName() + ": root: " + root.getIdentifier() + ", role: " + role + postfix;
    }

    /**
     * This internal helper constructs a name string that can be handed over to
     * the factory methods of our ActiveMeshObjectSets and ActiveTraversalPathSets.
     * This name is used for debugging only.
     *
     * @param root the root MeshObject
     * @param notQuiteRole a string to append
     * @return the created name
     */
    protected String constructSceneWithRoleName(
            MeshObject  root,
            String      notQuiteRole )
    {
        return getClass().getName() + ": root: " + root.getIdentifier() + ", " + notQuiteRole;
    }

    /**
     * The root SceneTemplateRole in this SceneTemplate.
     */
    protected SceneTemplateRole.Root theRootRole;

    /**
     * The SceneTemplateRoles in this SceneTemplate that are played by MeshObjects and that are not root SceneTemplateRoles.
     */
    protected SceneTemplateRole.PlayedByMeshObject [] theEntityRoles;

    /**
     * The SceneTemplateRoles in this SceneTemplate that are played by Scenes.
     */
    protected SceneTemplateRole.PlayedByScene [] theSceneRoles;

    /**
     * The types of addition candidates for this SceneTemplate, ordered in the same sequence
     * as theEntityRoles. Initialize with null to indicate that none have been set.
     */
    private SceneTemplateAdditionCandidate [][] thePotentialAdditionCandidates = null;

    /**
     * The SceneTemplateDirectory that we use.
     */
    protected SceneTemplateDirectory theDirectory;
}
