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

package org.infogrid.jee.net.TESTAPP;

import java.util.ArrayList;
import org.infogrid.jee.viewlet.meshbase.AllMeshObjectsViewlet;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.util.ArrayHelper;
import org.infogrid.viewlet.AbstractViewletFactory;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * ViewletFactory for the TESTAPP application.
 */
public class TESTAPPViewletFactory
        extends
            AbstractViewletFactory
{
    /**
     * Constructor.
     */
    public TESTAPPViewletFactory()
    {
        super( "org.infogrid.jee.viewlet.JeeViewlet" );
    }

    /**
     * Find the ViewletFactoryChoices that apply to these MeshObjectsToView, but ignore the specified
     * viewlet type. If none are found, return an emtpy array.
     *
     * @param theObjectsToView the MeshObjectsToView
     * @return the found ViewletFactoryChoices, if any
     */
    public ViewletFactoryChoice [] determineFactoryChoicesIgnoringType(
            MeshObjectsToView theObjectsToView )
    {
        ArrayList<ViewletFactoryChoice> ret = new ArrayList<ViewletFactoryChoice>();
        
        NetMeshObject subject = (NetMeshObject) theObjectsToView.getSubject();
        NetMeshBase   base    = subject.getMeshBase();

        // NetMeshBase's Home Object
        if( base.getHomeObject() == subject ) {
            ret.add( DefaultViewletFactoryChoice.create( AllMeshObjectsViewlet.class, ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
//            ret.add( DefaultViewletFactoryChoice.create( AllMeshTypesViewlet.class,   ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
//            ret.add( DefaultViewletFactoryChoice.create( BulkLoaderViewlet.class,     ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
//            ret.add( DefaultViewletFactoryChoice.create( ProxiesViewlet.class,        ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
//            ret.add( DefaultViewletFactoryChoice.create( AllMeshBasesViewlet.class,   ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
//
//            Proxy p = theObjectsToView.getViewletParameters() != null ? (Proxy) theObjectsToView.getViewletParameters().get( NetViewletDispatcherServlet.PROXY_NAME ) : null;
//            if( p != null ) {
//                ret.add( DefaultViewletFactoryChoice.create( ProxyViewlet.class, ViewletFactoryChoice.PERFECT_MATCH_QUALITY+1.d )); // not quite perfect
//            }
        }
        
//        ret.add( PseudoJspViewletFactoryChoice.create( "org.infogrid.jee.viewlet.propertysheet.PropertySheetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY ));
//        ret.add( PseudoJspViewletFactoryChoice.create( "org.infogrid.jee.viewlet.propertysheet.net.NetPropertySheetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY - 1.0 )); // slightly better
//        ret.add( PseudoJspViewletFactoryChoice.create( "org.infogrid.jee.viewlet.objectset.ObjectSetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY ));

        return ArrayHelper.copyIntoNewArray( ret, ViewletFactoryChoice.class );
    }
}
