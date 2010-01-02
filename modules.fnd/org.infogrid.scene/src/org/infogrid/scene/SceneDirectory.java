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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.scene;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.util.ArrayHelper;

/**
 * A directory of currently active Scenes. This is similar to a MeshBase is for MeshObjects,
 * but for Scenes. It contains all matched Scenes that are still used (we use WeakReferences).
 */
public class SceneDirectory
{
    /**
     * Constructor, with the directory of all known SceneTemplates.
     *
     * @param templateDir the SceneTemplateDirectory containing all known SceneTemplates
     */
    public SceneDirectory(
            SceneTemplateDirectory templateDir )
    {
        theTemplateDirectory = templateDir;
    }

    /**
     * <p>Find or create a Scene, given these matched SceneRoles. This is a smart factory
     * method, i.e. it will create new Scene if possible but no instance exists yet with
     * the required matched SceneRoles.</p>
     * <p>If a MeshBaseLifecycleManager is given, we will attempt to complete
     * the Scene if no match is currently possible because key objects are missing.</p>
     *
     * <p>As a side effect, this implementation cleans up behind Scenes that have been
     * garbage collected or that have died since the last call.</p>
     *
     * @param template the SceneTemplate that we intend to instantiate
     * @param matchedRootRole the root SceneRole
     * @param matchedEntityRolesSubset the specified subset of entity SceneRoles
     * @param matchedSceneRolesSubset the specified subset of scene SceneRoles
     * @param life the MeshObjectLifecycleManager used by the Scene to complete itself if necessary
     * @return the created Scene, or null
     * @throws MatchAmbiguousException thrown if the match is ambiguous
     */
    @SuppressWarnings("unchecked")
    public synchronized Scene findOrCreateSceneFor(
            SceneTemplate                   template,
            SceneRole.Root                  matchedRootRole,
            SceneRole.PlayedByMeshObject [] matchedEntityRolesSubset,
            SceneRole.PlayedByScene []      matchedSceneRolesSubset,
            MeshBaseLifecycleManager        life )
        throws
            MatchAmbiguousException
    {
        if( template == null ) {
            throw new IllegalArgumentException( "SceneTemplate may not be null" );
        }

        WeakReference<Scene> [] sceneReferences = theScenes.get( template );

        // FIXME: this algorithm should also shrink the array, not just grow it.
        // When looking through the array, we should not only memorize the location
        // of the first hole, but automatically fill it as we look through the array
        // by moving later elements forward. If, once we are done looking through the
        // array, the array is more than half empty, we should shrink the array.

        int holeIndex = -1; // if we find a hole, memorize where it was
        if( sceneReferences != null ) {
            for( int i=0 ; i<sceneReferences.length ; ++i ) {
                if( sceneReferences[i] == null ) {
                    holeIndex = i;
                    continue;
                }

                Scene currentScene = sceneReferences[i].get();
                if( currentScene == null || currentScene.isDead() ) {
                    sceneReferences[i] = null;
                    holeIndex = i;
                    continue;
                }

                if( currentScene.hasRolesMatching( matchedRootRole, matchedEntityRolesSubset, matchedSceneRolesSubset )) {
                    return currentScene; // FIXME? cleanup holes if we found any?
                }
            }
        }

        // we did not find a suitable Scene
        Scene currentScene = template.match( matchedRootRole, matchedEntityRolesSubset, matchedSceneRolesSubset, life, this );
        if( currentScene != null ) {
            if( holeIndex >=0 ) {
                sceneReferences[holeIndex] = new WeakReference<Scene>( currentScene );

            } else {
                if( sceneReferences != null ) {
                    sceneReferences = (WeakReference<Scene>[]) ArrayHelper.append( sceneReferences, new WeakReference<Scene>( currentScene ), WeakReference.class );
                } else {
                    sceneReferences = (WeakReference<Scene>[]) new WeakReference[] { new WeakReference<Scene>( currentScene ) };
                }

                theScenes.put( template, sceneReferences );
            }
        }
        return currentScene;
    }

    /**
     * Convenience method to find or create a Scene, given these matched SceneRoles and the name of the
     * SceneTemplate to look up by name.
     *
     * @param nameOfSceneTemplateInDirectory the name of the SceneTemplate that we intend to instantiate
     * @param matchedRootRole the root SceneRole
     * @param matchedEntityRolesSubset the specified subset of entity SceneRoles
     * @param matchedSceneRolesSubset the specified subset of scene SceneRoles
     * @param life the ModelObjectLifecycleManager used by the Scene to complete itself if necessary
     * @return the created Scene, or null
     * @throws MatchAmbiguousException thrown if the match is ambiguous
     */
    public Scene findOrCreateSceneFor(
            String                          nameOfSceneTemplateInDirectory,
            SceneRole.Root                  matchedRootRole,
            SceneRole.PlayedByMeshObject [] matchedEntityRolesSubset,
            SceneRole.PlayedByScene []      matchedSceneRolesSubset,
            MeshBaseLifecycleManager        life )
        throws
            MatchAmbiguousException
    {
        SceneTemplate template = theTemplateDirectory.findByClassName( nameOfSceneTemplateInDirectory );
        if( template == null ) {
            throw new IllegalArgumentException( "Template not in SceneDirectory: " + nameOfSceneTemplateInDirectory );
        }

        return findOrCreateSceneFor( template, matchedRootRole, matchedEntityRolesSubset, matchedSceneRolesSubset, life );
    }

    /**
     * Obtain the SceneTemplateDirectory in which we find our SceneTemplates.
     *
     * @return the SceneTemplateDirectory
     */
    public final SceneTemplateDirectory getTemplateDirectory()
    {
        return theTemplateDirectory;
    }

    /**
     * The set of current Scenes. This is keyed by the SceneTemplate. The values
     * are arrays of WeakReferences pointing to the Scenes, in order to not consume resources
     * when we don't need to.
     */
    protected HashMap<SceneTemplate,WeakReference<Scene>[]> theScenes = new HashMap<SceneTemplate,WeakReference<Scene>[]>( 20 );

    /**
     * The directory of SceneTemplates.
     */
    protected SceneTemplateDirectory theTemplateDirectory;
}
