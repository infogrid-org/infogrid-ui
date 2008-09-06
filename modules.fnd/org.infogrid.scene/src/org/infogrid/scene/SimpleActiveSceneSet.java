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
 * A very simple ActiveSceneSet implementation.
 */
public class SimpleActiveSceneSet
    extends
        AbstractActiveSceneSet
{
    /**
     * Factory method.
     *
     * @param initialContent the initial content of the SceneSet
     * @return the created SimpleActiveSceneSet
     */
    public static SimpleActiveSceneSet create(
            Scene [] initialContent )
    {
        return new SimpleActiveSceneSet( initialContent );
    }

    /**
     * Factory method.
     *
     * @param contentIsOneScene the one Scene that is the initial content of the SceneSet
     * @return the created SimpleActiveSceneSet
     */
    public static SimpleActiveSceneSet create(
            Scene contentIsOneScene )
    {
        return new SimpleActiveSceneSet( new Scene[] { contentIsOneScene } );
    }

    /**
     * Factory method creating an empty set.
     *
     * @return the created SimpleActiveSceneSet
     */
    public static SimpleActiveSceneSet createEmpty()
    {
        return new SimpleActiveSceneSet( new Scene[0] );
    }

    /**
     * Constructor.
     *
     * @param initialContent the initial content of the SceneSet
     */
    protected SimpleActiveSceneSet(
            Scene [] initialContent )
    {
        super( initialContent );
    }
}
