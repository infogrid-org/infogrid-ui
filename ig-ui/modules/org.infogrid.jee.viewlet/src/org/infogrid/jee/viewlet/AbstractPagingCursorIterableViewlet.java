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
import org.infogrid.util.CursorIterator;
import org.infogrid.util.PagingCursorIterator;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.Viewlet;

/**
 * Factors out common functionality for Viewlets that display sets in
 * pages through use of a CursorIterator.
 */
public abstract class AbstractPagingCursorIterableViewlet
        extends
            AbstractCursorIterableViewlet
{
    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param parent the parent Viewlet, if any
     * @param defaultPageLength the default page length
     * @param c the application context
     */
    protected AbstractPagingCursorIterableViewlet(
            AbstractViewedMeshObjects viewed,
            Viewlet                   parent,
            int                       defaultPageLength,
            Context                   c )
    {
        super( viewed, parent, c );

        theDefaultPageLength = defaultPageLength;
    }

    /**
     * Ensure that the iterator is initialized.
     *
     * @throws CannotViewException.InvalidParameter thrown if a windowing parameter was invalid
     */
    @Override
    protected void ensureInitialized()
        throws
            CannotViewException.InvalidParameter
    {
        super.ensureInitialized();

        if( thePageIterator == null ) {
            thePageIterator = pageCursorIterator( theIterator );
        }
    }

    /**
     * Obtain the CursorIterator.
     *
     * @return the CursorIterator.
     * @throws CannotViewException.InvalidParameter thrown if a windowing parameter was invalid
     */
    @Override
    public CursorIterator<MeshObject> getCursorIterator()
        throws
            CannotViewException.InvalidParameter
    {
        ensureInitialized();
        return thePageIterator; // return the local one
    }

    /**
     * Page the Iterator.
     *
     * @param setIter the Iterator over the entire set
     * @return the paging Iterator, or null
     * @throws CannotViewException.InvalidParameter thrown if a subsetting parameter was invalid
     */
    protected PagingCursorIterator<MeshObject> pageCursorIterator(
            CursorIterator<MeshObject> setIter )
        throws
            CannotViewException.InvalidParameter
    {
        String pageStart  = (String) getViewedObjects().getViewletParameter( PAGE_START_NAME );
        String pageLength = (String) getViewedObjects().getViewletParameter( PAGE_LENGTH_NAME );
        
        MeshObject start;
        int        page;
        if( pageStart != null ) {
            try {
                start = resolveMeshObject( pageStart );
            } catch( Throwable t ) {
                throw new CannotViewException.InvalidParameter( this, PAGE_START_NAME, pageStart, null );
            }
        } else {
            setIter.moveToBeforeFirst();
            start = setIter.peekNext();
        }

        if( pageLength != null ) {
            page = Integer.parseInt( pageLength );
        } else {
            page = theDefaultPageLength;
        }

        PagingCursorIterator<MeshObject> ret = PagingCursorIterator.create( start, page, setIter, MeshObject.class );
        return ret;
    }

    /**
     * Obtain the MeshObject that is the minimum MeshObject on the first page of the results, or null.
     *
     * @return the MeshObject, if any
     */
    public MeshObject getNavigationStartMeshObject()
    {
        if( !theNavigationStartMeshObjectCalculated ) {
            try {
                ensureInitialized();
            } catch( CannotViewException.InvalidParameter ex ) {
                // ignore
            }
            theNavigationStartMeshObjectCalculated = true;

            CursorIterator<MeshObject> setIterCopy  = theIterator.createCopy();
            CursorIterator<MeshObject> pageIterCopy = thePageIterator.createCopy();

            setIterCopy.moveToBeforeFirst();
            pageIterCopy.moveToBeforeFirst();

            MeshObject firstInSet  = setIterCopy.peekNext();
            MeshObject firstInPage = pageIterCopy.peekNext();

            if( firstInSet.equals( firstInPage )) {
                // we are at the beginning
                theNavigationStartMeshObject = null;
            } else {
                theNavigationStartMeshObject = firstInSet;
            }
        }
        return theNavigationStartMeshObject;
    }

    /**
     * Obtain the MeshObject that is the minimum MeshObject on the previous page of the results, or null.
     *
     * @return the MeshObject, if any
     */
    public MeshObject getNavigationBackMeshObject()
    {
        if( !theNavigationBackMeshObjectCalculated ) {
            try {
                ensureInitialized();
            } catch( CannotViewException.InvalidParameter ex ) {
                // ignore
            }
            theNavigationBackMeshObjectCalculated = true;

            CursorIterator<MeshObject> setIterCopy  = theIterator.createCopy();
            CursorIterator<MeshObject> pageIterCopy = thePageIterator.createCopy();

            setIterCopy.moveToBeforeFirst();
            pageIterCopy.moveToBeforeFirst();

            MeshObject firstInSet  = setIterCopy.peekNext();
            MeshObject firstInPage = pageIterCopy.peekNext();

            if( firstInSet.equals( firstInPage )) {
                // we are at the beginning
                theNavigationBackMeshObject = null;

            } else {
                CursorIterator<MeshObject> setIterCopy2 = theIterator.createCopy();
                if( setIterCopy2.hasPrevious( thePageIterator.getPageLength() )) {
                    setIterCopy2.moveBy( -thePageIterator.getPageLength() );
                } else {
                    setIterCopy2.moveToBeforeFirst();
                }
                theNavigationBackMeshObject = setIterCopy2.next();
            }
        }
        return theNavigationBackMeshObject;
    }

    /**
     * Obtain the MeshObject that is the minimum MeshObject on the next page of the results, or null.
     *
     * @return the MeshObject, if any
     */
    public MeshObject getNavigationForwardMeshObject()
    {
        if( !theNavigationForwardMeshObjectCalculated ) {
            try {
                ensureInitialized();
            } catch( CannotViewException.InvalidParameter ex ) {
                // ignore
            }
            theNavigationForwardMeshObjectCalculated = true;

            CursorIterator<MeshObject> setIterCopy  = theIterator.createCopy();
            CursorIterator<MeshObject> pageIterCopy = thePageIterator.createCopy();

            setIterCopy.moveToAfterLast();
            pageIterCopy.moveToAfterLast();

            MeshObject lastInSet  = setIterCopy.peekPrevious();
            MeshObject lastInPage = pageIterCopy.peekPrevious();

            if( lastInSet.equals( lastInPage )) {
                // we are at the beginning
                theNavigationForwardMeshObject = null;

            } else {
                CursorIterator<MeshObject> setIterCopy2 = theIterator.createCopy();

                if( setIterCopy2.hasNext( thePageIterator.getPageLength() )) {
                    setIterCopy2.moveBy( thePageIterator.getPageLength() );
                } else {
                    setIterCopy2.moveToAfterLast();
                    if( setIterCopy2.hasPrevious( thePageIterator.getPageLength() )) {
                        setIterCopy2.moveBy( -thePageIterator.getPageLength() );
                    } else {
                        setIterCopy2.moveToBeforeFirst();
                    }
                }
                theNavigationForwardMeshObject = setIterCopy2.next();
            }
        }
        return theNavigationForwardMeshObject;
    }

    /**
     * Obtain the MeshObject that is the minimum MeshObject on the last page of the results, or null.
     *
     * @return the MeshObject, if any
     */
    public MeshObject getNavigationEndMeshObject()
    {
        if( !theNavigationEndMeshObjectCalculated ) {
            try {
                ensureInitialized();
            } catch( CannotViewException.InvalidParameter ex ) {
                // ignore
            }
            theNavigationEndMeshObjectCalculated = true;

            CursorIterator<MeshObject> setIterCopy  = theIterator.createCopy();
            CursorIterator<MeshObject> pageIterCopy = thePageIterator.createCopy();

            setIterCopy.moveToAfterLast();
            pageIterCopy.moveToAfterLast();

            MeshObject lastInSet  = setIterCopy.peekPrevious();
            MeshObject lastInPage = pageIterCopy.peekPrevious();

            if( lastInSet.equals( lastInPage )) {
                // we are at the beginning
                theNavigationEndMeshObject = null;

            } else {
                CursorIterator<MeshObject> setIterCopy2 = theIterator.createCopy();
                setIterCopy2.moveToAfterLast();

                if( setIterCopy2.hasPrevious( thePageIterator.getPageLength() )) {
                    setIterCopy2.moveBy( -thePageIterator.getPageLength() );
                } else {
                    setIterCopy2.moveToBeforeFirst();
                }
                theNavigationEndMeshObject = setIterCopy2.next();
            }
        }
        return theNavigationEndMeshObject;
    }

    /**
     * Determine the current page length.
     *
     * @return the page length
     */
    public int getPageLength()
    {
        try {
            ensureInitialized();
        } catch( CannotViewException.InvalidParameter ex ) {
            // ignore
        }

        return thePageIterator.getPageLength();
    }

    /**
     * The PagingCursorIterator that iterates over the page.
     */
    protected PagingCursorIterator<MeshObject> thePageIterator;

    /**
     * Name of the Viewlet parameter indicating the MeshObject with which the current page starts.
     */
    public static final String PAGE_START_NAME = "page-start";

    /**
     * Name of the Viewlet parameter indicating the page length.
     */
    public static final String PAGE_LENGTH_NAME = "page-length";

    /**
     * Default page length.
     */
    protected int theDefaultPageLength;

    /**
     * The navigation start object, once calculated.
     */
    protected MeshObject theNavigationStartMeshObject;

    /**
     * Flag whether the navigation start object has been calculated.
     */
    protected boolean theNavigationStartMeshObjectCalculated = false;

    /**
     * The navigation back object, once calculated.
     */
    protected MeshObject theNavigationBackMeshObject;

    /**
     * Flag whether the navigation back object has been calculated.
     */
    protected boolean theNavigationBackMeshObjectCalculated = false;

    /**
     * The navigation forward object, once calculated.
     */
    protected MeshObject theNavigationForwardMeshObject;

    /**
     * Flag whether the navigation forward object has been calculated.
     */
    protected boolean theNavigationForwardMeshObjectCalculated = false;

    /**
     * The navigation end object, once calculated.
     */
    protected MeshObject theNavigationEndMeshObject;

    /**
     * Flag whether the navigation end object has been calculated.
     */
    protected boolean theNavigationEndMeshObjectCalculated = false;
}
