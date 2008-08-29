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

package org.infogrid.jee.viewlet.meshbase;

import org.infogrid.jee.viewlet.AbstractCursorIterableViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.IterableMeshBase;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewedMeshObjects;

/**
 * A Viewlet that shows all MeshObjects in a MeshBase.
 */ 
public class AllMeshObjectsViewlet
        extends
            AbstractCursorIterableViewlet<MeshObject>
{
    /**
     * Factory method.
     *
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static AllMeshObjectsViewlet create(
            Context c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects();
        AllMeshObjectsViewlet    ret    = new AllMeshObjectsViewlet( viewed, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected AllMeshObjectsViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );
    }

    /**
     * Obtain the MeshObjectSet to display
     *
     * @return the MeshObjectSet
     */
    @Override
    public CursorIterator<MeshObject> getCursorIterator()
    {
        IterableMeshBase meshBase = (IterableMeshBase) getSubject().getMeshBase(); // derive from the subject, so we can do any MeshBase
        
        return meshBase.iterator();
    }
}
