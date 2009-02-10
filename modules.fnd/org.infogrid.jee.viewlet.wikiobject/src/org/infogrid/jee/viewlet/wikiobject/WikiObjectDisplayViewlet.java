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

package org.infogrid.jee.viewlet.wikiobject;

import org.infogrid.jee.viewlet.SimpleJeeViewlet;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.Wiki.WikiSubjectArea;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewedMeshObjects;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Viewlet that can display a WikiObject.
 */
public class WikiObjectDisplayViewlet
        extends
            SimpleJeeViewlet
{
    /**
     * Factory method.
     *
     * @param c the application context
     * @return the created Viewlet
     */
    public static WikiObjectDisplayViewlet create(
            Context c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects();
        WikiObjectDisplayViewlet ret    = new WikiObjectDisplayViewlet( viewed, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            double matchQuality )
    {
        return new DefaultViewletFactoryChoice( WikiObjectDisplayViewlet.class, matchQuality ) {
                public Viewlet instantiateViewlet(
                        MeshObjectsToView        toView,
                        Context                  c )
                    throws
                        CannotViewException
                {
                    return create( c );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected WikiObjectDisplayViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );
    }

    /**
     * Obtain the current content of the WikiObject.
     *
     * @return the current content
     * @throws IllegalPropertyTypeException thrown if the current subject does not have a WikiSubjectArea.WIKIOBJECT_CONTENT property
     * @throws NotPermittedException thrown if the caller is not authorized to access the WikiSubjectArea.WIKIOBJECT_CONTENT property
     */
    public String getContent()
        throws
            IllegalPropertyTypeException,
            NotPermittedException
    {
        BlobValue oldValue = (BlobValue) getSubject().getPropertyValue( WikiSubjectArea.WIKIOBJECT_CONTENT );
        String    ret;
        if( oldValue != null ) {
            ret = oldValue.getAsString();
        } else {
            ret = "";
        }
        return ret;
    }
}
