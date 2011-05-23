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

package org.infogrid.jee.net.testapp;

import java.util.ArrayList;
import org.infogrid.jee.viewlet.DefaultJspViewlet;
import org.infogrid.jee.viewlet.bulk.BulkLoaderViewlet;
import org.infogrid.jee.viewlet.meshbase.AllMeshObjectsViewlet;
import org.infogrid.jee.viewlet.meshbase.net.ProxyViewlet;
import org.infogrid.jee.viewlet.net.JeeNetMeshObjectsToView;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.local.LocalNetMeshBase;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.util.ArrayHelper;
import org.infogrid.viewlet.AbstractViewletFactory;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * ViewletFactory for the testapp application.
 */
public class TestAppViewletFactory
        extends
            AbstractViewletFactory
{
    /**
     * Constructor.
     */
    public TestAppViewletFactory()
    {
        super( "org.infogrid.jee.viewlet.JeeViewlet" );
    }

    /**
     * Find the ViewletFactoryChoices that apply to these MeshObjectsToView, but ignore the specified
     * viewlet type. If none are found, return an emtpy array.
     *
     * @param toView the MeshObjectsToView
     * @return the found ViewletFactoryChoices, if any
     */
    public ViewletFactoryChoice [] determineFactoryChoicesIgnoringType(
            MeshObjectsToView toView )
    {
        JeeNetMeshObjectsToView realToView = (JeeNetMeshObjectsToView) toView;

        ArrayList<ViewletFactoryChoice> ret = new ArrayList<ViewletFactoryChoice>();
        
        NetMeshObject subject = (NetMeshObject) toView.getSubject();
        NetMeshBase   base    = subject.getMeshBase();

        // NetMeshBase's Home Object
        if( base.getHomeObject() == subject ) {
            ret.add( AllMeshObjectsViewlet.choice( realToView, ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
//            ret.add( AllMeshTypesViewlet.choice(   realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            ret.add( BulkLoaderViewlet.choice(     realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
//            ret.add( BulkExporterViewlet.choice(     realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
//            ret.add( ProxiesViewlet.choice(        realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
//            ret.add( AllMeshBasesViewlet.choice(   realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));

            Proxy p = realToView.getRequestedProxy();
            if( p != null ) {
                ret.add( ProxyViewlet.choice( realToView, ViewletFactoryChoice.PERFECT_MATCH_QUALITY+1.d )); // not quite perfect
            }
            
            if( base instanceof LocalNetMeshBase ) {
                ret.add( DefaultJspViewlet.choice( realToView, "org.infogrid.jee.viewlet.meshbase.net.AllShadowsViewlet", ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            }
        }

        ret.add( DefaultJspViewlet.choice( realToView, "org.infogrid.jee.viewlet.propertysheet.PropertySheetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY ));
//        ret.add( DefaultJspViewlet.choice( toView, "org.infogrid.jee.viewlet.propertysheet.net.NetPropertySheetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY - 1.0 )); // slightly better
//        ret.add( DefaultJspViewlet.choice( toView, "org.infogrid.jee.viewlet.objectset.ObjectSetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY ));

        return ArrayHelper.copyIntoNewArray( ret, ViewletFactoryChoice.class );
    }
}
