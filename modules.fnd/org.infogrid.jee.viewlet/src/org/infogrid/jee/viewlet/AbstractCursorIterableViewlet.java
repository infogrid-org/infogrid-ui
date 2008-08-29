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

package org.infogrid.jee.viewlet;

import org.infogrid.mesh.MeshObject;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.AbstractViewedMeshObjects;

/**
 * Factors out common functionality for Viewlets that display sets through
 * use of a CursorIterator.
 */
public abstract class AbstractCursorIterableViewlet<T>
        extends
            AbstractJeeViewlet
{
    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected AbstractCursorIterableViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );
    }

    /**
     * Obtain the CursorIterator.
     *
     * @return the CursorIterator.
     */
    public CursorIterator<MeshObject> getCursorIterator()
    {
        return getObjects().iterator();
    }
}
