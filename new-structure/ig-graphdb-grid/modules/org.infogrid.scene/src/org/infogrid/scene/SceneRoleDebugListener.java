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

import org.infogrid.mesh.set.active.ActiveMeshObjectSetListener;
import org.infogrid.mesh.set.active.ActiveTraversalPathSetListener;
import org.infogrid.mesh.set.active.MeshObjectAddedEvent;
import org.infogrid.mesh.set.active.MeshObjectRemovedEvent;
import org.infogrid.mesh.set.active.OrderedActiveMeshObjectSetReorderedEvent;
import org.infogrid.mesh.set.active.OrderedTraversalPathSetReorderedEvent;
import org.infogrid.mesh.set.active.TraversalPathAddedEvent;
import org.infogrid.mesh.set.active.TraversalPathRemovedEvent;
import org.infogrid.util.logging.Log;

/**
 * This class helps with debugging, and that's its only purpose. It can be attached to the ActiveMeshObjectSets
 * and/or ActiveTraversalPathSets in SceneRoles in order to log what is going on.
 */
public class SceneRoleDebugListener
    implements
        ActiveMeshObjectSetListener,
        ActiveTraversalPathSetListener
{
    /**
     * Construct one.
     *
     * @param s the Scene that we watch
     * @param role the SceneTemplateRole that we watch on the Scene
     * @param l the Logger to log to
     */
    public SceneRoleDebugListener(
            Scene             s,
            SceneTemplateRole role,
            Log               l )
    {
        theScene = s;
        theRole  = role;
        theLog   = l;
    }

    /**
      * The ActiveMeshObjectSet sends a MeshObjectAddedEvent to this listener.
      *
      * @param event the event
      */
    public void meshObjectAdded(
            MeshObjectAddedEvent event )
    {
        theLog.debug(
                "SceneRoleDebugListener: Scene: "
                + theScene
                + ", Role: "
                + theRole
                + ": meshObjectAdded( "
                + event
                + " )" );
    }

    /**
      * The ActiveMeshObjectSet sends a MeshObjectRemovedEvent to this listener.
      *
      * @param event the event
      */
    public void meshObjectRemoved(
            MeshObjectRemovedEvent event )
    {
        theLog.debug(
                "SceneRoleDebugListener: Scene: "
                + theScene
                + ", Role: "
                + theRole
                + ": meshObjectRemoved( "
                + event
                + " )" );
    }

    /**
      * An OrderedActiveMeshObjectSet sends a OrderedActiveMeshObjectSetReorderedEvent to this
      * listener. This method will only be invoked by OrderedActiveMeshObjectSets.
      *
      * @param event the event
      */
    public void orderedMeshObjectSetReordered(
            OrderedActiveMeshObjectSetReorderedEvent event )
    {
        theLog.debug(
                "SceneRoleDebugListener: Scene: "
                + theScene
                + ", Role: "
                + theRole
                + ": meshObjectSetReordered( "
                + event
                + " )" );
    }

    /**
      * Indicates that a TraversalPath was added to this set.
      *
      * @param event the event
      */
    public void traversalPathAdded(
            TraversalPathAddedEvent event )
    {
        theLog.debug(
                "SceneRoleDebugListener: Scene: "
                + theScene
                + ", Role: "
                + theRole
                + ": traversalPathAdded( "
                + event
                + " )" );
    }

    /**
      * Indicates that a TraversalPath was removed from this set.
      *
      * @param event the event
      */
    public void traversalPathRemoved(
            TraversalPathRemovedEvent event )
    {
        theLog.debug(
                "SceneRoleDebugListener: Scene: "
                + theScene
                + ", Role: "
                + theRole
                + ": traversalPathRemoved( "
                + event
                + " )" );
    }

    /**
     * This indicates that the set was re-ordered. Only applies in case of
     * ordered sets.
     *
     * @param event the event
     */
    public void orderedTraversalPathSetReordered(
            OrderedTraversalPathSetReorderedEvent event )
    {
        theLog.debug(
                "SceneRoleDebugListener: Scene: "
                + theScene
                + ", Role: "
                + theRole
                + ": traversalPathSetReordered( "
                + event
                + " )" );
    }

    /**
     * The Scene that we help traceMethodCallEntry.
     */
    protected Scene theScene;

    /**
     * The SceneTemplateRole that we help traceMethodCallEntry.
     */
    protected SceneTemplateRole theRole;

    /**
     * The logger for debugging.
     */
    protected Log theLog;
}
