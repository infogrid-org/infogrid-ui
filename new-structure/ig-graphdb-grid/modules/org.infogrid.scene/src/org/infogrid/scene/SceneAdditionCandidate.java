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
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.transaction.TransactionException;

/**
 * A potential addition to a Scene. Often this is subclassed.
 */
public class SceneAdditionCandidate
{
    /**
     * Constructor.
     *
     * @param match the Scene to which this SceneAdditionCandidate can be added
     * @param role the SceneTemplateRole affected by this SceneAdditionCandidate
     * @param type the SceneTemplateAdditionCandidate of which this is an "instance"
     */
    public SceneAdditionCandidate(
            Scene                          match,
            SceneTemplateRole              role,
            SceneTemplateAdditionCandidate type )
    {
        theScene    = match;
        theRole     = role;
        theType     = type;
        theUserName = null;
    }

    /**
     * Obtain the Scene that this SceneAdditionCandidate belongs to.
     *
     * @return the Scene that this SceneAdditionCandidate belongs to
     */
    public final Scene getScene()
    {
        return theScene;
    }

    /**
     * Obtain the SceneTemplateRole that this SceneAdditionCandidate will be playing when instantiated.
     *
     * @return the SceneTemplateRole that this SceneAdditionCandidate will be playing when instantiated
     */
    public final SceneTemplateRole getTemplateRole()
    {
        return theRole;
    }

    /**
     * Obtain the SceneTemplateAdditionCandidate that is the "type" of this SceneAdditionCandidate.
     *
     * @return the SceneTemplateAdditionCandidate that is the "type" of this SceneAdditionCandidate
     */
    public final SceneTemplateAdditionCandidate getTemplateAdditionCandidate()
    {
        return theType;
    }

    /**
     * Allow to set the user-visible string to a different value.
     *
     * @param newValue new value for the user-visible string
     * @see #getUserVisibleString
     */
    public final void setUserVisibleString(
            String newValue )
    {
        theUserName = newValue;
    }

    /**
     * Obtain a string that can be shown to the user that identifies this SceneAdditionCandidate.
     *
     * @return the string that can be shown to the user
     * @see #setUserVisibleString
     */
    public final String getUserVisibleString()
    {
        if( theUserName != null ) {
            return theUserName;
        }
        return theType.getUserVisibleString();
    }

    /**
     * Determine whether this SceneAdditionCandidate is sufficiently parameterized and thus can be instantiated.
     * Subclasses typically override this.
     *
     * @return this always returns false, but subtypes may (and should!) do that differently
     */
    public boolean isSufficientlyParameterized()
    {
        return true;
    }

    /**
     * The Scene to which this SceneAdditionCandidate belongs tells this SceneAdditionCandidate that it
     * should instantiate itself. The default implementation keeps delegating to the SceneTemplateAdditionCandidate,
     * but that behavior can be overridden by subclasses.
     *
     * @param life the MeshBaseLifecycleManager used to instantiate all required MeshObjects
     * @return the created MeshObjects
     * @throws UnknownHowException thrown if not enough information is available to perform this operation
     * @throws IllegalArgumentException indicates a wrong configuration
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws DoNotHaveLockException thrown if update rights could not be obtained for MeshObjects that needed to be updated
     */
    protected MeshObject instantiate(
            MeshBaseLifecycleManager life )
        throws
            UnknownHowException,
            IllegalArgumentException,
            TransactionException,
            NotPermittedException
    {
        return theType.instantiate( this, life );
    }

    /**
     * The Scene for which this SceneAdditionCandidate is a SceneAdditionCandidate.
     */
    protected Scene theScene;

    /**
     * The SceneTemplateRole that this SceneAdditionCandidate could be playing.
     */
    protected SceneTemplateRole theRole;

    /**
     * The "type".
     */
    protected SceneTemplateAdditionCandidate theType;

    /**
     * The user-visible name for this SceneAdditionCandidate, if given.
     */
    private String theUserName;
}
