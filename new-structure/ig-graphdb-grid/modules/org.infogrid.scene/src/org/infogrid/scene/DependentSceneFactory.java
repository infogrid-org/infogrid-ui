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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.active.ActiveMeshObjectSet;
import org.infogrid.mesh.set.active.ActiveMeshObjectSetListener;
import org.infogrid.mesh.set.active.MeshObjectAddedEvent;
import org.infogrid.mesh.set.active.MeshObjectRemovedEvent;
import org.infogrid.mesh.set.active.OrderedActiveMeshObjectSetReorderedEvent;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.util.logging.Log;

/**
 * Abstract superclass for objects that create dependent Scenes based on some event. This
 * abstract class listens to events in Scenes that have SceneRoles played by other
 * Scenes.
 */
public abstract class DependentSceneFactory
    implements
        ActiveSceneSetListener, // we may listen to ActiveSceneSets of another DependentSceneFactory
        PropertyChangeListener  // we listen to matched / unmatched events of our Scenes
{
    private static final Log log = Log.getLogInstance( DependentSceneFactory.class ); // our own, private logger

    /**
     * Construct one.
     *
     * @param dependentSceneTemplate the SceneTemplate to instantiate (this is a factory, after all)
     * @param life the MeshObjectLifecycleManager to use to instantiate MeshObjects
     * @param directory the SceneDirectory that contains the Scenes that may be instantiated by this DependentSceneFactory
     */
    protected DependentSceneFactory(
            SceneTemplate            dependentSceneTemplate,
            MeshBaseLifecycleManager life,
            SceneDirectory           directory )
    {
        theDependentSceneTemplate = dependentSceneTemplate;
        theLifecycleManager       = life;
        theSceneDirectory         = directory;

        theMatchedActiveSceneSet.addWeakContentPropertyChangeListener( this );
        theUnmatchedActiveSceneSet.addWeakContentPropertyChangeListener( this );
    }

    /**
     * Invoked to tell this factory that the passed-in set is the set it is dependent upon.
     * The passed-in SceneTemplateRole specifies which SceneRole this DependentSceneFactory subscribes to
     * in the Scenes contained in the fromSet: when such a SceneRole changes its content, this
     * DependentSceneFactory takes action with respect to its dependent Scenes.
     *
     * @param fromSet the set of Scenes that this DependentSceneFactory depends on
     * @param roleToWatchInScenesInFromSet the SceneTemplateRole that this DependentSceneFactory watchs in the set of Scenes that it depends on
     */
    public void initializeWith(
            ActiveSceneSet                       fromSet,
            SceneTemplateRole.PlayedByMeshObject roleToWatchInScenesInFromSet )
    {
        if( roleToWatchInScenesInFromSet == null ) {
            throw new NullPointerException();
        }
        if( theRoleToWatch != null ) {
            throw new IllegalStateException( "re-initialized" );
        }
        theRoleToWatch = roleToWatchInScenesInFromSet;

        fromSet.addWeakActiveSceneSetListener( this );

        Scene [] initial = fromSet.getScenes();
        for( int i=0 ; i<initial.length ; ++i ) {
            Scene               currentScene          = initial[i];
            ActiveMeshObjectSet currentSceneEntitySet = currentScene.getMatchFor( roleToWatchInScenesInFromSet ).getPlayers();
            MeshObject []       currentSceneEntities  = currentSceneEntitySet.getMeshObjects();

            for( int j=0 ; j<currentSceneEntities.length ; ++j ) {
                Scene addedScene = findOrCreateSceneFor(
                        currentScene,
                        currentSceneEntities[j],
                        theDependentSceneTemplate,
                        theSceneDirectory,
                        theLifecycleManager );

                if( addedScene == null ) {
                    continue;
                }

                addToEntityToScenePlusCounterMap( currentSceneEntities[j], addedScene );

                if( addedScene.isMatched() ) {
                    theMatchedActiveSceneSet.potentiallyAdd( addedScene );
                } else {
                    theUnmatchedActiveSceneSet.potentiallyAdd( addedScene );
                }
            }

            InputSceneListener newListener = new InputSceneListener( currentScene );
            currentSceneEntitySet.addWeakActiveMeshObjectSetListener( newListener );

            inputSceneToListenerMap.put( currentScene, newListener );
        }
    }

    /**
     * Initialize with what is essentially an ActiveSceneSet with only one (un-changing) member.
     *
     * @param fromScene the Scene that we depend on
     * @param roleToWatchInFromScene the SceneTemplateRole that this DependentSceneFactory watches in the Scene that it depends on
     */
    public void initializeWith(
            Scene                                fromScene,
            SceneTemplateRole.PlayedByMeshObject roleToWatchInFromScene )
    {
        initializeWith( SimpleActiveSceneSet.create( fromScene ), roleToWatchInFromScene );
    }

    /**
     * Obtain the active set of currently matched Scenes that this DependentSceneFactory manages.
     *
     * @return the active set of currently matched Scenes that this DependentSceneFactory manages
     */
    public ActiveSceneSet getScenes()
    {
        return theMatchedActiveSceneSet;
    }

    /**
     * This method exists so it is easy to set breakpoints in the debugger, either right
     * here or in subclasses that override this.
     */
    protected void breakpointHook()
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "breakpointHook" );
        }
    }

    /**
     * This needs to be implemented by subclasses to instantiate the right Scene when a new MeshObject
     * has been added to a SceneRole that this DependentSceneFactory monitors through being subscribed as a listener.
     * 
     * @param triggeringScene the Scene that triggered this call
     * @param addedMeshObjectInTriggeringScene the MeshObject that was added to the Scene that triggered this call
     * @param theSceneTemplate the SceneTemplate to be instantiated
     * @param theSceneDirectory the SceneDirectory within which we instantiate the Scene
     * @param theLifecycleManager the MeshBaseLifecycleManager we use to instantiate any needed MeshObjects
     * @return the newly created Scene
     */
    protected abstract Scene findOrCreateSceneFor(
            Scene                    triggeringScene,
            MeshObject               addedMeshObjectInTriggeringScene,
            SceneTemplate            theSceneTemplate,
            SceneDirectory           theSceneDirectory,
            MeshBaseLifecycleManager theLifecycleManager );

    /**
     * Callback indicating that a Acene has been added to this DependentSceneFactory's input Scene set.
     *
     * @param theEvent the event
     */
    public synchronized void sceneAdded(
            SceneAddedEvent theEvent )
    {
        Scene addedParentScene = theEvent.getAddedScene();

        ActiveMeshObjectSet addedSceneEntitySet = addedParentScene.getMatchFor( theRoleToWatch ).getPlayers();
        MeshObject []       addedSceneEntities  = addedSceneEntitySet.getMeshObjects();

        for( int j=0 ; j<addedSceneEntities.length ; ++j ) {
            Scene addedChildScene = findOrCreateSceneFor(
                    addedParentScene,
                    addedSceneEntities[j],
                    theDependentSceneTemplate,
                    theSceneDirectory,
                    theLifecycleManager );

            if( addedChildScene == null ) {
                continue;
            }

            addToEntityToScenePlusCounterMap( addedSceneEntities[j], addedChildScene );

            if( addedChildScene.isMatched() ) {
                theMatchedActiveSceneSet.potentiallyAdd( addedChildScene );
            } else {
                theUnmatchedActiveSceneSet.potentiallyAdd( addedChildScene );
            }
        }

        InputSceneListener newListener = new InputSceneListener( addedParentScene );
        addedSceneEntitySet.addWeakActiveMeshObjectSetListener( newListener );

        inputSceneToListenerMap.put( addedParentScene, newListener );
    }

    /**
     * Callback indicating that a Acene has been removed from this DependentSceneFactory's input Scene set.
     *
     * @param theEvent the event
     */
    public synchronized void sceneRemoved(
            SceneRemovedEvent theEvent )
    {
        Scene removedParentScene = theEvent.getRemovedScene();

        ActiveMeshObjectSet removedSceneEntitySet = removedParentScene.getMatchFor( theRoleToWatch ).getPlayers();
        MeshObject []       removedSceneEntities  = removedSceneEntitySet.getMeshObjects();

        breakpointHook();

        for( int i=0 ; i<removedSceneEntities.length ; ++i ) {
            // we don't know whether we should remove the scene because it may be contained
            // multiple -- so we first get it, and them remove it if it turns out the counters are down to zero.

            Scene potentiallyRemovedScene = removeFromEntityToScenePlusCounterMap( removedSceneEntities[i] );
            if( potentiallyRemovedScene == null ) {
                return;
            }

            boolean b1 = theMatchedActiveSceneSet.potentiallyRemove( potentiallyRemovedScene );
            boolean b2 = theUnmatchedActiveSceneSet.potentiallyRemove( potentiallyRemovedScene );
        }
        InputSceneListener oldListener = inputSceneToListenerMap.remove( removedParentScene );
        removedSceneEntitySet.removeActiveMeshObjectSetListener( oldListener );
    }

    /**
     * PropertyChangeEvent callback from one of this DependentSceneFactory's dependent Scenes.
     *
     * @param theEvent the event
     */
    public void propertyChange(
            PropertyChangeEvent theEvent )
    {
        if( Scene.IS_MATCHED_PROPERTY.equals( theEvent.getPropertyName() )) {
            Scene changedScene = (Scene) theEvent.getSource();

            if( changedScene.isMatched() ) {
                int nTimes = theUnmatchedActiveSceneSet.certainlyRemove( changedScene );
                log.assertLog( nTimes > 0, "not contained" );
                theMatchedActiveSceneSet.certainlyAdd( nTimes, changedScene );

            } else {
                int nTimes = theMatchedActiveSceneSet.certainlyRemove( changedScene );
                log.assertLog( nTimes > 0, "not contained" );
                theUnmatchedActiveSceneSet.certainlyAdd( nTimes, changedScene );
            }
        }
    }

    /**
     * Helper method to add to the entityToScenePlusCounterMap.
     *
     * @param theEntity the MeshObject to add to the entityToScenePlusCounterMap
     * @param theScene the Scene
     */
    protected void addToEntityToScenePlusCounterMap(
            MeshObject theEntity,
            Scene      theScene )
    {
        ScenePlusCounter record = entityToScenePlusCounterMap.get( theEntity );
        if( record != null ) {
            record.theCounter++;
        } else {
            record = new ScenePlusCounter();
            record.theScene = theScene;
            record.theCounter = 1;
            entityToScenePlusCounterMap.put( theEntity, record );
        }
    }

    /**
     * Helper method to remove from the entityToScenePlusCounterMap.
     *
     * @param theEntity the RootEntity to remove from the entityToScenePlusCounterMap
     * @return the Scene
     */
    protected Scene removeFromEntityToScenePlusCounterMap(
            MeshObject theEntity )
    {
        ScenePlusCounter record = entityToScenePlusCounterMap.get( theEntity );
        if( record != null ) {
            record.theCounter--;
            if( record.theCounter == 0 ) {
                entityToScenePlusCounterMap.remove( theEntity );
                return record.theScene;

            } else {
                return null;
            }
        }
        return null;
        // it is allowed that we don't find anything -- could be that our Scene factory method decided
        // not to create a dependent Scene for this entity
    }

    /**
     * The SceneTemplate that we instantiate.
     */
    private SceneTemplate theDependentSceneTemplate;

    /**
     * The MeshBaseLifecycleManager to use for instantiation.
     */
    private MeshBaseLifecycleManager theLifecycleManager;

    /**
     * The SceneDirectory in which we instantiate the SceneTemplate.
     */
    private SceneDirectory theSceneDirectory;

    /**
     * The SceneTemplateRole that we apply to the Scenes in our input set.
     */
    private SceneTemplateRole.PlayedByMeshObject theRoleToWatch;

    /**
     * This maps the RootEntity playing root role of a dependent Scene to the
     * Scene in which it playes that root SceneTemplateRole. Unfortunately, the same RootEntity
     * may come from different parent Scenes, and thus we need to keep track of
     * both the dependent Scene, and a counter -- how many times the scene is
     * held here. So the value is ScenePlusCounter.
     */
    private HashMap<MeshObject,ScenePlusCounter> entityToScenePlusCounterMap
            = new HashMap<MeshObject,ScenePlusCounter>(); // FIXME? Should this be a map with weak keys?

    /**
     * This maps an input Scene to the listener of its events. This is there because otherwise,
     * the garbage collector would collect the listeners.
     */
    private HashMap<Scene,InputSceneListener> inputSceneToListenerMap = new HashMap<Scene,InputSceneListener>();

    /**
     * The set of Scenes that we manage and that are currently matched.
     */
    private SimpleActiveSceneSet theMatchedActiveSceneSet = SimpleActiveSceneSet.createEmpty();

    /**
     * The set of Scenes that we manage and that are currently unmatched.
     */
    private SimpleActiveSceneSet theUnmatchedActiveSceneSet = SimpleActiveSceneSet.createEmpty();

    /**
     * A listener of MeshObjectAdded and MeshObjectRemoved events.
     * One instance of this per input Scene.
     */
    class InputSceneListener
        implements
            ActiveMeshObjectSetListener
    {
        /**
         * Construct one.
         *
         * @param inputScene the Scene that we listen to
         */
        public InputSceneListener(
                Scene inputScene )
        {
            theInputScene = inputScene;
        }

        /**
         * Callback indicating that one of the MeshObjects that one of our Scenes depends on has been added.
         *
         * @param theEvent the event
         */
        public void meshObjectAdded(
                MeshObjectAddedEvent theEvent )
        {
            MeshObject addedMeshObject = theEvent.getDeltaValue();

            Scene addedScene = findOrCreateSceneFor(
                    theInputScene,
                    addedMeshObject,
                    theDependentSceneTemplate,
                    theSceneDirectory,
                    theLifecycleManager );

            breakpointHook();

            if( addedScene == null ) {
                return;
            }

            addToEntityToScenePlusCounterMap( addedMeshObject, addedScene );

            if( addedScene.isMatched() ) {
                theMatchedActiveSceneSet.potentiallyAdd( addedScene );
            } else {
                theUnmatchedActiveSceneSet.potentiallyAdd( addedScene );
            }
        }

        /**
         * Callback indicating that one of the MeshObjects that one of our Scenes depends on has been removed.
         *
         * @param theEvent the event
         */
        public void meshObjectRemoved(
                MeshObjectRemovedEvent theEvent )
        {
            MeshObject removedMeshObject = theEvent.getDeltaValue();

            breakpointHook();

            // we don't know whether we should remove the scene because it may be contained
            // multiple -- so we first get it, and them remove it if it turns out the counters are down to zero.

            Scene potentiallyRemovedScene = removeFromEntityToScenePlusCounterMap( removedMeshObject );
            if( potentiallyRemovedScene == null ) {
                return;
            }

            boolean b1 = theMatchedActiveSceneSet.potentiallyRemove( potentiallyRemovedScene );
            boolean b2 = theUnmatchedActiveSceneSet.potentiallyRemove( potentiallyRemovedScene );
        }

        /**
         * Callback indicating that the set has been reordered. Ignored.
         *
         * @param theEvent the event
         */
        public void orderedMeshObjectSetReordered(
                OrderedActiveMeshObjectSetReorderedEvent theEvent )
        {
            // noop
        }

        /**
         * The input Scene that we watch.
         */
        private Scene theInputScene;
    }

    /**
     * Auxiliary struct that contains a Scene, plus a counter for how many times the
     * Scene has been reached.
     */
    private static class ScenePlusCounter
    {
        Scene theScene;
        int   theCounter;
    }
}
