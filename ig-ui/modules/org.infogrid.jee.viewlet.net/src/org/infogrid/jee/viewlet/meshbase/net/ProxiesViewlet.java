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

package org.infogrid.jee.viewlet.meshbase.net;

import org.infogrid.jee.viewlet.AbstractJeeViewlet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.DefaultViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * A Viewlet that shows a NetMeshBase's Proxies.
 */ 
public class ProxiesViewlet
        extends
            AbstractJeeViewlet
{
    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param parent the parent Viewlet, if any
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static ProxiesViewlet create(
            MeshBase mb,
            Viewlet  parent,
            Context  c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects( mb );
        ProxiesViewlet           ret    = new ProxiesViewlet( viewed, parent, c );

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
        return new DefaultViewletFactoryChoice( ProxiesViewlet.class, matchQuality ) {
                public Viewlet instantiateViewlet(
                        MeshObjectsToView        toView,
                        Viewlet                  parent,
                        Context                  c )
                    throws
                        CannotViewException
                {
                    return create( toView.getMeshBase(), parent, c );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param parent the parent Viewlet, if any
     * @param c the application context
     */
    protected ProxiesViewlet(
            AbstractViewedMeshObjects viewed,
            Viewlet                   parent,
            Context                   c )
    {
        super( viewed, parent, c );
    }
}
