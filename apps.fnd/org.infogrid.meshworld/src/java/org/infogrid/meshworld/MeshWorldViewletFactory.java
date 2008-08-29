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

package org.infogrid.meshworld;

import java.util.ArrayList;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.jee.viewlet.PseudoJspViewletFactoryChoice;
import org.infogrid.jee.viewlet.bulk.BulkLoaderViewlet;
import org.infogrid.jee.viewlet.meshbase.AllMeshObjectsViewlet;
import org.infogrid.jee.viewlet.modelbase.AllMeshTypesViewlet;
import org.infogrid.jee.viewlet.wikiobject.WikiObjectDisplayViewlet;
import org.infogrid.jee.viewlet.wikiobject.WikiObjectEditViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.Wiki.WikiSubjectArea;
import org.infogrid.viewlet.AbstractViewletFactory;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactoryChoice;
import org.infogrid.util.ArrayHelper;

/**
 * ViewletFactory for the MeshWorld application
 */
public class MeshWorldViewletFactory
        extends
            AbstractViewletFactory
{
    /**
     * Constructor.
     */
    public MeshWorldViewletFactory()
    {
        super( JeeViewlet.class.getName() );
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
        
        MeshObject subject = theObjectsToView.getSubject();
        if( subject.getMeshBase().getHomeObject() == subject ) {
            ret.add( DefaultViewletFactoryChoice.create( AllMeshObjectsViewlet.class, ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
            ret.add( DefaultViewletFactoryChoice.create( AllMeshTypesViewlet.class,   ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            ret.add( DefaultViewletFactoryChoice.create( BulkLoaderViewlet.class,     ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
        }
        if( subject.isBlessedBy( WikiSubjectArea.WIKIOBJECT )) {
            ret.add( DefaultViewletFactoryChoice.create( WikiObjectDisplayViewlet.class, ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
            ret.add( DefaultViewletFactoryChoice.create( WikiObjectEditViewlet.class,    ViewletFactoryChoice.GOOD_MATCH_QUALITY+1.0f ));
        }
        ret.add( PseudoJspViewletFactoryChoice.create( "org.infogrid.jee.viewlet.propertysheet.PropertySheetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY ));
        ret.add( PseudoJspViewletFactoryChoice.create( "org.infogrid.jee.viewlet.objectset.ObjectSetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY ));

        return ArrayHelper.copyIntoNewArray( ret, ViewletFactoryChoice.class );
    }
}
