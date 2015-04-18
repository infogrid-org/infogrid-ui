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

import org.infogrid.meshbase.MeshBase;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * A Viewlet Class that can impersonate any other JSP-based Viewlet, as long as the Viewlet
 * does not override or add any methods.
 */
public class DefaultJspViewlet
        extends
            AbstractJspViewlet
{
    /**
     * Factory method.
     *
     * @param pseudoClassName the fully-qualified class name of the class that will be impersonated
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param c the application context
     * @return the created Viewlet
     */
    public static DefaultJspViewlet create(
            String   pseudoClassName,
            MeshBase mb,
            Context  c )
    {
        DefaultJeeViewedMeshObjects viewed = new DefaultJeeViewedMeshObjects( mb );
        DefaultJspViewlet           ret    = new DefaultJspViewlet( pseudoClassName, viewed, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param toView the JeeMeshObjectsToView for which this is a choice
     * @param pseudoClassName the fully-qualified class name of the class that will be impersonated
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            JeeMeshObjectsToView toView,
            final String         pseudoClassName,
            double               matchQuality )
    {
        return new DefaultJspViewletFactoryChoice( toView, pseudoClassName, matchQuality ) {
                public DefaultJspViewlet instantiateViewlet()
                    throws
                        CannotViewException
                {
                    return create( pseudoClassName, getMeshObjectsToView().getMeshBase(), getMeshObjectsToView().getContext() );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param pseudoClassName the fully-qualified class name of the class that will be impersonated
     * @param viewed the JeeViewedMeshObjects to use
     * @param c the application context
     */
    protected DefaultJspViewlet(
            String               pseudoClassName,
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( pseudoClassName, viewed, c );
    }
}
