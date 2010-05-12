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

package org.infogrid.jee.viewlet;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.SubsettingCursorIterator;
import org.infogrid.util.ZeroElementCursorIterator;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.CannotViewException;

/**
 * Factors out common functionality for Viewlets that display sets through
 * use of a CursorIterator.
 */
public abstract class AbstractCursorIterableViewlet
        extends
            AbstractJeeViewlet
{
    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the JeeViewedMeshObjects to use
     * @param c the application context
     */
    protected AbstractCursorIterableViewlet(
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( viewed, c );
    }

    /**
     * Ensure that the iterator is initialized.
     *
     * @throws CannotViewException.InvalidParameter thrown if a windowing parameter was invalid
     */
    protected void ensureInitialized()
        throws
            CannotViewException.InvalidParameter
    {
        if( theIterator == null ) {
            CursorIterator<MeshObject> found = determineCursorIterator();
            theIterator = subsetCursorIterator( found );
        }
    }

    /**
     * Obtain the CursorIterator.
     *
     * @return the CursorIterator
     * @throws CannotViewException.InvalidParameter thrown if a windowing parameter was invalid
     */
    public CursorIterator<MeshObject> getCursorIterator()
        throws
            CannotViewException.InvalidParameter
    {
        ensureInitialized();
        return theIterator;
    }

    /**
     * Detemine the correct CursorIterator. Default implementation can be
     * overridden by subclasses.
     *
     * @return the CursorIterator
     */
    protected CursorIterator<MeshObject> determineCursorIterator()
    {
        MeshObjectSet reached = getViewedMeshObjects().getReachedObjects();
        if( reached != null ) {
            return reached.iterator();
        } else {
            return ZeroElementCursorIterator.create();
        }
    }

    /**
     * Subset the CursorIterator, if needed.
     *
     * @param iter the to-be-clipped CursorIterator
     * @return the subsetted CursorIterator
     * @throws CannotViewException.InvalidParameter thrown if a subsetting parameter was invalid
     */
    protected CursorIterator<MeshObject> subsetCursorIterator(
            CursorIterator<MeshObject> iter )
        throws
            CannotViewException.InvalidParameter
    {
        String minName    = (String) getViewedMeshObjects().getViewletParameter( MIN_NAME );
        String maxName    = (String) getViewedMeshObjects().getViewletParameter( MAX_NAME );

        CursorIterator<MeshObject> ret = iter; // default

        if( minName != null || maxName != null ) {
            MeshObject min;
            MeshObject max;
            int        page;

            if( minName != null ) {
                try {
                    min = resolveMeshObject( minName );
                } catch( Throwable t ) {
                    throw new CannotViewException.InvalidParameter( this, MIN_NAME, minName, null );
                }
            } else {
                min = null;
            }
            if( maxName != null ) {
                try {
                    max = resolveMeshObject( maxName );
                } catch( Throwable t ) {
                    throw new CannotViewException.InvalidParameter( this, MAX_NAME, maxName, null );
                }
            } else {
                max = null;
            }

            if( min != null || max != null ) {
                ret = SubsettingCursorIterator.create( min, max, iter, MeshObject.class );
            }
        }
        return ret;
    }

    /**
     * The CursorIterator to be returned.
     */
    protected CursorIterator<MeshObject> theIterator;

    /**
     * Name of the Viewlet parameter indicating the start index.
     */
    public static final String MIN_NAME = "start-index";

    /**
     * Name of the Viewlet parameter indicating the end index.
     */
    public static final String MAX_NAME = "end-index";
}
