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

/**
 * An active (ie self-changing) set of Scenes. This concept is similar to ActiveMeshObjectSet,
 * except that this contains Scenes, not MeshObjects.
 */
public interface ActiveSceneSet
{
    /**
     * Obtain the current content of the set.
     *
     * @return the current content of the set
     */
    public abstract Scene [] getScenes();

    /**
     * Obtain the number of members currently in the set.
     *
     * @return the number of members currently in the set
     */
    public abstract int size();

    /**
     * Determine whether this set is empty.
     *
     * @return true if this set is empty
     */
    public abstract boolean isEmpty();

    /**
     * Add a listener.
     *
     * @param newListener the new listener
     * @see #addSoftActiveSceneSetListener
     * @see #addWeakActiveSceneSetListener
     * @see #removeActiveSceneSetListener
     */
    public abstract void addDirectActiveSceneSetListener(
            ActiveSceneSetListener newListener );

    /**
     * Add a listener.
     *
     * @param newListener the new listener
     * @see #addDirectActiveSceneSetListener
     * @see #addWeakActiveSceneSetListener
     * @see #removeActiveSceneSetListener
     */
    public abstract void addSoftActiveSceneSetListener(
            ActiveSceneSetListener newListener );

    /**
     * Add a listener.
     *
     * @param newListener the new listener
     * @see #addDirectActiveSceneSetListener
     * @see #addSoftActiveSceneSetListener
     * @see #removeActiveSceneSetListener
     */
    public abstract void addWeakActiveSceneSetListener(
            ActiveSceneSetListener newListener );

    /**
     * Remove a listener.
     *
     * @param oldListener the existing listener to be removed
     * @see #addDirectActiveSceneSetListener
     * @see #addSoftActiveSceneSetListener
     * @see #addWeakActiveSceneSetListener
     */
    public abstract void removeActiveSceneSetListener(
            ActiveSceneSetListener oldListener );
}
