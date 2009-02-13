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

import java.util.Iterator;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.viewlet.AbstractJeeViewlet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.util.NameServer;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.DefaultViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
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
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static AllMeshBasesViewlet create(
            Context c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects();
        AllMeshBasesViewlet      ret    = new AllMeshBasesViewlet( viewed, c );

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
        return new DefaultViewletFactoryChoice( AllMeshBasesViewlet.class, matchQuality ) {
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
    protected AllMeshBasesViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
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
        InfoGridWebApp app = InfoGridWebApp.getSingleton();

        // Context c = app.getApplicationContext();
        Context c = getContext();

        @SuppressWarnings( "unchecked" )
        NameServer<MeshBaseIdentifier,MeshBase> ns  = c.findContextObjectOrThrow( NameServer.class );
        
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
