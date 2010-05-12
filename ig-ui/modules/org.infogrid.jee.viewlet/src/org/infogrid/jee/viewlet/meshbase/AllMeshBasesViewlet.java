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

package org.infogrid.jee.viewlet.meshbase;

import java.util.Iterator;
import org.infogrid.jee.viewlet.AbstractJeeViewlet;
import org.infogrid.jee.viewlet.DefaultJeeViewedMeshObjects;
import org.infogrid.jee.viewlet.DefaultJeeViewletFactoryChoice;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.jee.viewlet.JeeViewedMeshObjects;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * A Viewlet that shows all locally known MeshBases.
 */ 
public class AllMeshBasesViewlet
        extends
            AbstractJeeViewlet
        implements
            Iterable<MeshBase>
{
    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static AllMeshBasesViewlet create(
            MeshBase mb,
            Context  c )
    {
        DefaultJeeViewedMeshObjects viewed = new DefaultJeeViewedMeshObjects( mb );
        AllMeshBasesViewlet         ret    = new AllMeshBasesViewlet( viewed, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param toView the JeeMeshObjectsToView for which this is a choice
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            JeeMeshObjectsToView toView,
            double               matchQuality )
    {
        return new DefaultJeeViewletFactoryChoice( toView, AllMeshBasesViewlet.class, matchQuality ) {
                public Viewlet instantiateViewlet()
                    throws
                        CannotViewException
                {
                    return create( getMeshObjectsToView().getMeshBase(), getMeshObjectsToView().getContext() );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the JeeViewedMeshObjects to use
     * @param c the application context
     */
    protected AllMeshBasesViewlet(
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( viewed, c );
    }

    /**
     * Obtain the MeshBases to display.
     *
     * @return Iterator over the MeshBases
     */
    public Iterator<MeshBase> iterator()
    {
        Context c = getContext();

        @SuppressWarnings( "unchecked" )
        MeshBaseNameServer<MeshBaseIdentifier,MeshBase> ns = c.findContextObjectOrThrow( MeshBaseNameServer.class );
        
        Iterator<MeshBase> ret = ns.values().iterator();
        
        return ret;
    }
    
    /**
     * Obtain the MeshBases to display.
     *
     * @return Iterator over the MeshBases
     */
    public Iterator<MeshBase> getIterator()
    {
        return iterator();
    }
}
