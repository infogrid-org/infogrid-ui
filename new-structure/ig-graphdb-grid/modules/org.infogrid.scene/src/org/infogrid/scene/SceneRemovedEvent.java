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

import java.util.EventObject;

/**
 * Indicates that a Scene was removed from an ActiveSceneSet.
 */
public class SceneRemovedEvent
    extends
        EventObject
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param source the ActiveSceneSet sending this event
     * @param removedScene the Scene that was removed
     */
    public SceneRemovedEvent(
            ActiveSceneSet source,
            Scene          removedScene )
    {
        super( source );

        theRemovedScene = removedScene;
    }

    /**
     * Obtain the Scene that was removed.
     *
     * @return the removed Scene
     */
    public final Scene getRemovedScene()
    {
        return theRemovedScene;
    }

    /**
     * The Scene that was removed.
     */
    protected Scene theRemovedScene;
}
