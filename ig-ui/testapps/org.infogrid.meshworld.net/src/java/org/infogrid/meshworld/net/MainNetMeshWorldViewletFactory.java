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

package org.infogrid.meshworld.net;

import java.util.ArrayList;
import org.infogrid.jee.viewlet.DefaultJspViewlet;
import org.infogrid.jee.viewlet.blob.BlobViewlet;
import org.infogrid.jee.viewlet.bulk.BulkLoaderViewlet;
import org.infogrid.jee.viewlet.log4j.Log4jConfigurationViewlet;
import org.infogrid.jee.viewlet.meshbase.net.AllNetMeshObjectsViewlet;
import org.infogrid.jee.viewlet.meshbase.net.ProxiesViewlet;
import org.infogrid.jee.viewlet.meshbase.net.ProxyViewlet;
import org.infogrid.jee.viewlet.modelbase.AllMeshTypesViewlet;
import org.infogrid.jee.viewlet.module.ModuleDirectoryViewlet;
import org.infogrid.jee.viewlet.net.JeeNetMeshObjectsToView;
import org.infogrid.jee.viewlet.probe.shadow.ShadowAwareAllMeshBasesViewlet;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.model.Wiki.WikiSubjectArea;
import org.infogrid.model.primitives.BlobDataType;
import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.AbstractViewletFactory;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * ViewletFactory for the NetMeshWorld application's main screen.
 */
public class MainNetMeshWorldViewletFactory
        extends
            AbstractViewletFactory
{
    private static final Log log = Log.getLogInstance( MainNetMeshWorldViewletFactory.class ); // our own, private logger

    /**
     * Constructor.
     */
    public MainNetMeshWorldViewletFactory()
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
        if( subject.getMeshBase().getHomeObject() == subject ) {
            ret.add( AllNetMeshObjectsViewlet.choice(       realToView, ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
            ret.add( AllMeshTypesViewlet.choice(            realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            ret.add( Log4jConfigurationViewlet.choice(      realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            ret.add( ModuleDirectoryViewlet.choice(         realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            ret.add( BulkLoaderViewlet.choice(              realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            ret.add( ProxiesViewlet.choice(                 realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            ret.add( ShadowAwareAllMeshBasesViewlet.choice( realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));

            Proxy p = realToView.getRequestedProxy();
            if( p != null ) {
                ret.add( ProxyViewlet.choice( realToView, ViewletFactoryChoice.PERFECT_MATCH_QUALITY+1.d )); // not quite perfect
            }
        }
        if( subject.isBlessedBy( WikiSubjectArea.WIKIOBJECT )) {
            ret.add( DefaultJspViewlet.choice( realToView, "org.infogrid.jee.viewlet.wikiobject.WikiObjectDisplayViewlet", ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
            ret.add( DefaultJspViewlet.choice( realToView, "org.infogrid.jee.viewlet.wikiobject.WikiObjectEditViewlet", ViewletFactoryChoice.GOOD_MATCH_QUALITY+1.0f ));
        }
        for( PropertyType type : subject.getAllPropertyTypes()) {
            if( type.getDataType() instanceof BlobDataType ) {
                try {
                    BlobValue value = (BlobValue) subject.getPropertyValue( type );
                    if( value != null && BlobDataType.theJdkSupportedBitmapType.isAllowedMimeType( value.getMimeType() )) {
                        ret.add( BlobViewlet.choice( realToView, ViewletFactoryChoice.BAD_MATCH_QUALITY ));
                        break;
                    }
                } catch( IllegalPropertyTypeException ex ) {
                    log.error( ex );
                } catch( NotPermittedException ex ) {
                    // ignore: then we'll do without this Viewlet
                }
            }
        }
        ret.add( DefaultJspViewlet.choice( realToView, "org.infogrid.jee.viewlet.graphtree.GraphTreeViewlet",                ViewletFactoryChoice.BAD_MATCH_QUALITY ));
        ret.add( DefaultJspViewlet.choice( realToView, "org.infogrid.jee.viewlet.propertysheet.PropertySheetViewlet",        ViewletFactoryChoice.BAD_MATCH_QUALITY ));
        ret.add( DefaultJspViewlet.choice( realToView, "org.infogrid.jee.viewlet.propertysheet.net.NetPropertySheetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY - 1.0 )); // slightly better
        ret.add( DefaultJspViewlet.choice( realToView, "org.infogrid.jee.viewlet.objectset.ObjectSetViewlet",                ViewletFactoryChoice.BAD_MATCH_QUALITY ));

        return ArrayHelper.copyIntoNewArray( ret, ViewletFactoryChoice.class );
    }
}
