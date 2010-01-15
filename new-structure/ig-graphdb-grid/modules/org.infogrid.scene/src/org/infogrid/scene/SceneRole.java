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
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.active.ActiveMeshObjectSet;
import org.infogrid.mesh.set.active.ActiveMeshObjectSetListener;
import org.infogrid.mesh.set.active.ActiveTraversalPathSet;
import org.infogrid.mesh.set.active.MeshObjectAddedEvent;
import org.infogrid.mesh.set.active.MeshObjectRemovedEvent;
import org.infogrid.mesh.set.active.OrderedActiveMeshObjectSetReorderedEvent;
import org.infogrid.meshbase.transaction.MeshObjectStateEvent;
import org.infogrid.util.FlexiblePropertyChangeListenerSet;

/**
 * A pair of a SceneTemplateRole, and a TraversalPath that leads to the
 * MeshObject playing the SceneTemplateRole in this Scene. It is the "instance",
 * so to speak, of a SceneTemplateRole for an instantiated Scene.
 */
public abstract class SceneRole
{
    /**
     * Factory method for the root SceneRole.
     *
     * @param templateRole the SceneTemplateRole.Root that we "instantiate"
     * @param player the MeshObject playing this SceneTemplateRole
     * @return the created SceneRole
     */
    public static final Root createRootRole(
            SceneTemplateRole.Root templateRole,
            MeshObject             player )
    {
        return new Root( templateRole, player );
    }

    /**
     * Factory method if we have an ActiveTraversalPathSet for an entity SceneTemplateRole.
     *
     * @param templateRole the SceneTemplateRole.PlayedByMeshObject that we "instantiate"
     * @param players the ActiveTraversalPathSet playing this SceneTemplateRole
     * @return the created SceneRole
     */
    public static final PlayedByMeshObject createEntityRole(
            SceneTemplateRole.PlayedByMeshObject templateRole,
            ActiveTraversalPathSet               players )
    {
        return new UseTraversalPathSet( templateRole, players );
    }

    /**
     * Factory method if we have an ActiveMeshObjectSet for a entity SceneTemplateRole.
     *
     * @param templateRole the SceneTemplateRole.PlayedByMeshObject that we "instantiate"
     * @param players the ActiveMeshObjectSet playing this SceneTemplateRole
     * @return the created SceneRole
     */
    public static final PlayedByMeshObject createEntityRole(
            SceneTemplateRole.PlayedByMeshObject templateRole,
            ActiveMeshObjectSet                  players )
    {
        return new UseMeshObjectSet( templateRole, players );
    }

    /**
     * Factory method if we have an ActiveSceneSet for a scene SceneTemplateRole.
     *
     * @param templateRole the SceneTemplateRole.PlayedByScene that we "instantiate"
     * @param players the ActiveSceneSet playing this SceneTemplateRole
     * @return the created SceneRole
     */
    public static final PlayedByScene createSceneRole(
            SceneTemplateRole.PlayedByScene templateRole,
            ActiveSceneSet                  players )
    {
        return new PlayedByScene( templateRole, players );
    }

    /**
     * Private constructor, use factory methods.
     *
     * @param templateRole the SceneTemplateRole that we "instantiate"
     */
    protected SceneRole(
            SceneTemplateRole templateRole )
    {
        theTemplateRole = templateRole;
    }

    /**
     * Obtain the SceneTemplateRole that we match.
     *
     * @return the SceneTemplateRole that we match
     */
    public final SceneTemplateRole getTemplateRole()
    {
        return theTemplateRole;
    }

    /**
     * Determine whether this SceneRole is currently matched correctly (ie min and
     * max multiplicities).
     *
     * @return true if this SceneRole is currently matched correctly
     */
    public final boolean isMatched()
    {
        return theIsMatched;
    }

    /**
     * Internal helper to update the isMatched bound property and generate events.
     *
     * @param newValue the new value for the IsMatched property
     */
    protected final void setIsMatched(
            boolean newValue )
    {
        if( newValue == theIsMatched ) {
            return;
        }

        theIsMatched = newValue;

        PropertyChangeEvent theEvent = new PropertyChangeEvent(
                this,
                IS_MATCHED_PROPERTY,
                theIsMatched ? Boolean.FALSE : Boolean.TRUE,
                theIsMatched ? Boolean.TRUE : Boolean.FALSE );

        thePropertyChangeListeners.fireEvent( theEvent );
    }

    /**
     * Add a PropertyChangeListener.
     *
     * @param newListener the new listener
     * @see #removePropertyChangeListener
     */
    public void addPropertyChangeListener(
            PropertyChangeListener newListener )
    {
        thePropertyChangeListeners.addWeak( newListener );
    }

    /**
     * Remove a PropertyChangeListener.
     *
     * @param oldListener the to-be-removed listener
     * @see #addPropertyChangeListener
     */
    public void removePropertyChangeListener(
            PropertyChangeListener oldListener )
    {
        thePropertyChangeListeners.remove( oldListener );
    }

    /**
     * The SceneTemplateRole that we match.
     */
    protected SceneTemplateRole theTemplateRole;

    /**
     * Is this SceneRole currently matched correctly with respect to its multiplicities.
     */
    protected boolean theIsMatched;

    /**
     * Our PropertyChangeListeners, allocated as needed.
     */
    private FlexiblePropertyChangeListenerSet thePropertyChangeListeners = new FlexiblePropertyChangeListenerSet();

    /**
     * The Name of the IsMatched property.
     */
    public static final String IS_MATCHED_PROPERTY = "IsMatched";

    /**
     * Subclass for a root SceneRole.
     */
    static public class Root
            extends
                SceneRole
            implements
                PropertyChangeListener
    {
        /**
         * Private constructor, use factory method.
         *
         * @param templateRole the SceneTemplateRole that we "instantiate"
         * @param player the MeshObject playing this SceneTemplateRole
         */
        protected Root(
                SceneTemplateRole.Root templateRole,
                MeshObject             player )
        {
            super( templateRole );
            theRoot = player;

            super.theIsMatched = true;

            theRoot.addWeakPropertyChangeListener( this );
        }

        /**
         * Obtain the single player of this root SceneRole.
         *
         * @return the player of the root SceneTemplateRole
         */
        public MeshObject getRootPlayer()
        {
            return theRoot;
        }

        /**
         * Determine equality.
         *
         * @param other the Object to compare against
         * @return true if the objects equal
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( other instanceof Root ) {
                Root realOther = (Root) other;

                if( getTemplateRole() != realOther.getTemplateRole() ) {
                    return false;
                }

                return theRoot == realOther.theRoot;
            }
            return false;
        }

        /**
         * Hash code. This is here to make the IDE happy that otherwise indicates a warning.
         *
         * @return hash code
         */
        @Override
        public int hashCode()
        {
            int ret = theRoot.hashCode();

            ret ^= theTemplateRole.hashCode();

            return ret;
        }

        /**
         * Callback from the player indicating a change in a property.
         *
         * @param theEvent the event
         */
        public void propertyChange(
                PropertyChangeEvent theEvent )
        {
            if( theEvent instanceof MeshObjectStateEvent ) {
                MeshObject sender = (MeshObject) theEvent.getSource();
                if( sender.getIsDead() ) {
                    super.setIsMatched( false );
                }
            }
        }

        /**
         * The MeshObject playing the root SceneTemplateRole.
         */
        protected MeshObject theRoot;
    }

    /**
     * Subclass for a SceneRole that is not a root SceneRole and that is played by MeshObjects.
     * This is an abstract class; we have implementations that work from MeshObjectSets and
     * TraversalPathSets.
     */
    public static abstract class PlayedByMeshObject
            extends
                SceneRole
            implements
                ActiveMeshObjectSetListener
    {
        /**
         * Pass-through constructor.
         *
         * @param templateRole the SceneTemplateRole that we "instantiate"
         */
        protected PlayedByMeshObject(
                SceneTemplateRole.PlayedByMeshObject templateRole )
        {
            super( templateRole );

            // our subclasses are responsible for subscriptions. However, because the
            // code is the same, we provide the event handling code.
        }

        /**
         * Determine which representation we have here.
         *
         * @return true if this is the TraversalPathSet implementation; RootEntitySet implementation otherwise
         */
        public abstract boolean isTraversalPathSet();

        /**
         * Obtain the set of players that play this SceneTemplateRole.
         *
         * @return the set of players that play this SceneTemplateRole
         */
        public abstract ActiveMeshObjectSet getPlayers();

        /**
         * Obtain the set of TraversalPaths leading to the set of players that play this SceneTemplateRole.
         *
         * @return the set of TraversalPaths leading to the set of players that play this SceneTemplateRole
         */
        public abstract ActiveTraversalPathSet getTraversalPathsToPlayers();

        /**
         * Callback from the players set, it changed.
         *
         * @param e the event
         */
        public void meshObjectAdded(
                MeshObjectAddedEvent e )
        {
            MeshObjectSet set = (MeshObjectSet) e.getSource();
            int size = set.size();

            setIsMatched( size >= theTemplateRole.getMin() && size <= theTemplateRole.getMax() );
        }

        /**
         * Callback from the players set, it changed.
         *
         * @param e the event
         */
        public void meshObjectRemoved(
                MeshObjectRemovedEvent e )
        {
            MeshObjectSet set = (MeshObjectSet) e.getSource();
            int size = set.size();

            setIsMatched( size >= theTemplateRole.getMin() && size <= theTemplateRole.getMax() );
        }

        /**
         * Callback from the players set, it changed.
         *
         * @param e the event
         */
        public void orderedMeshObjectSetReordered(
                OrderedActiveMeshObjectSetReorderedEvent e )
        {
            // noop
        }

    }

    /**
     * Subclass of PlayedByMeshObject that uses a TraversalPathSet internally.
     */
    static class UseTraversalPathSet
            extends
                PlayedByMeshObject
    {
        /**
         * Private constructor, use factory method.
         *
         * @param templateRole the SceneTemplateRole that we "instantiate"
         * @param pathToPlayers the ActiveTraversalPathSet leading to the players of this SceneTemplateRole
         */
        protected UseTraversalPathSet(
                SceneTemplateRole.PlayedByMeshObject templateRole,
                ActiveTraversalPathSet               pathToPlayers )
        {
            super( templateRole );

            thePaths   = pathToPlayers;
            thePlayers = thePaths.getDestinationsAsActiveSet();

            thePlayers.addWeakActiveMeshObjectSetListener( this );

            int size = thePlayers.size();
            if( size < super.theTemplateRole.getMin() ) {
                super.theIsMatched = false;
            } else if( size > super.theTemplateRole.getMax() ) {
                super.theIsMatched = false;
            } else {
                super.theIsMatched = true;
            }
        }

        /**
         * Obtain the set of players of this SceneRole.
         *
         * @return the set of players of this SceneRole
         */
        public ActiveMeshObjectSet getPlayers()
        {
            return thePlayers;
        }

        /**
         * Obtain the set of TraversalPaths leading to the players of this SceneRole.
         *
         * @return the set of TraversalPaths leading to the players of this SceneRole
         */
        public ActiveTraversalPathSet getTraversalPathsToPlayers()
        {
            return thePaths;
        }

        /**
         * Determine which representation we have here.
         *
         * @return true if this is the TraversalPathSet implementation; RootEntitySet implementation otherwise
         */
        public boolean isTraversalPathSet()
        {
            return true;
        }

        /**
         * Determine equality.
         *
         * @param other the Object to compare against
         * @return true if the objects equal
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( other instanceof UseTraversalPathSet ) {
                UseTraversalPathSet realOther = (UseTraversalPathSet) other;

                if( getTemplateRole() != realOther.getTemplateRole() ) {
                    return false;
                }

                return thePaths.equals( realOther.thePaths );
            }
            return false;
        }

        /**
         * Hash code. This is here to make the IDE happy that otherwise indicates a warning.
         *
         * @return hash code
         */
        @Override
        public int hashCode()
        {
            int ret = thePaths.hashCode();

            ret ^= theTemplateRole.hashCode();

            return ret;
        }

        /**
         * The TraversalPaths to the players in this SceneRole.
         */
        protected ActiveTraversalPathSet thePaths;

        /**
         * The destinations of the TraversalPaths as a set.
         */
        protected ActiveMeshObjectSet thePlayers;
    }

    /**
     * Subclass of PlayedByMeshObject that uses a MeshObjectSet internally.
     */
    static class UseMeshObjectSet
            extends
                PlayedByMeshObject
    {
        /**
         * Private constructor, use factory method.
         *
         * @param templateRole the SceneTemplateRole that we "instantiate"
         * @param players the players of this SceneTemplateRole
         */
        protected UseMeshObjectSet(
                SceneTemplateRole.PlayedByMeshObject templateRole,
                ActiveMeshObjectSet                  players )
        {
            super( templateRole );
            thePlayers = players;

            thePlayers.addWeakActiveMeshObjectSetListener( this );

            int size = thePlayers.size();
            if( size < super.theTemplateRole.getMin() ) {
                super.theIsMatched = false;
            } else if( size > super.theTemplateRole.getMax() ) {
                super.theIsMatched = false;
            } else {
                super.theIsMatched = true;
            }
        }

        /**
         * Obtain the set of players of this SceneRole.
         *
         * @return the set of players of this SceneRole
         */
        public ActiveMeshObjectSet getPlayers()
        {
            return thePlayers;
        }

        /**
         * Obtain the set of TraversalPaths leading to the players of this SceneRole.
         *
         * @return this always returns null in this implementation
         */
        public ActiveTraversalPathSet getTraversalPathsToPlayers()
        {
            return null;
        }

        /**
         * Determine which representation we have here.
         *
         * @return true if this is the TraversalPathSet implementation; RootEntitySet implementation otherwise
         */
        public boolean isTraversalPathSet()
        {
            return false;
        }

        /**
         * Determine equality.
         *
         * @param other the Object to compare against
         * @return true if the objects equal
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( other instanceof UseMeshObjectSet ) {
                UseMeshObjectSet realOther = (UseMeshObjectSet) other;

                if( getTemplateRole() != realOther.getTemplateRole() ) {
                    return false;
                }

                return thePlayers.equals( realOther.thePlayers );
            }
            return false;
        }

        /**
         * Hash code. This is here to make the IDE happy that otherwise indicates a warning.
         *
         * @return hash code
         */
        @Override
        public int hashCode()
        {
            int ret = thePlayers.hashCode();

            ret ^= theTemplateRole.hashCode();

            return ret;
        }

        /**
         * The players playing this SceneTemplateRole in this SceneRole.
         */
        protected ActiveMeshObjectSet thePlayers;
    }

    /**
     * Subclass for a SceneTemplateRole that is not a root SceneTemplateRole and played by Scenes.
     */
    public static class PlayedByScene
            extends
                SceneRole
            implements
                ActiveSceneSetListener
    {
        /**
         * Private constructor, use factory method.
         *
         * @param templateRole the SceneTemplateRole that we "instantiate"
         * @param players the players of this SceneTemplateRole
         */
        protected PlayedByScene(
                SceneTemplateRole.PlayedByScene templateRole,
                ActiveSceneSet                  players )
        {
            super( templateRole );
            thePlayers = players;

            thePlayers.addWeakActiveSceneSetListener( this );
        }

        /**
         * Obtain the Scenes (ie players) in this SceneRole.
         *
         * @return the Scenes that are players
         */
        public ActiveSceneSet getScenes()
        {
            return thePlayers;
        }

        /**
         * Determine equality.
         *
         * @param other the Object to compare against
         * @return true if the objects equal
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( other instanceof PlayedByScene ) {
                PlayedByScene realOther = (PlayedByScene) other;

                if( getTemplateRole() != realOther.getTemplateRole() ) {
                    return false;
                }

                return thePlayers.equals( realOther.thePlayers );
            }
            return false;
        }

        /**
         * Hash code. This is here to make the IDE happy that otherwise indicates a warning.
         *
         * @return hash code
         */
        @Override
        public int hashCode()
        {
            int ret = thePlayers.hashCode();

            ret ^= theTemplateRole.hashCode();

            return ret;
        }

        /**
         * Callback from our set of players, the set changed.
         *
         * @param e the event
         */
        public void sceneAdded(
                SceneAddedEvent e )
        {
            ActiveSceneSet set = (ActiveSceneSet) e.getSource();
            int size = set.size();

            setIsMatched( size >= theTemplateRole.getMin() && size <= theTemplateRole.getMax() );
        }

        /**
         * Callback from our set of players, the set changed.
         *
         * @param e the event
         */
        public void sceneRemoved(
                SceneRemovedEvent e )
        {
            ActiveSceneSet set = (ActiveSceneSet) e.getSource();
            int size = set.size();

            setIsMatched( size >= theTemplateRole.getMin() && size <= theTemplateRole.getMax() );
        }

        /**
         * The players playing this SceneTemplateRole in this SceneRole.
         */
        protected ActiveSceneSet thePlayers;
    }
}
