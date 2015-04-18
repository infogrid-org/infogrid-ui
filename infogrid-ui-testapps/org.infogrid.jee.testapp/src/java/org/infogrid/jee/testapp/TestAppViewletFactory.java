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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.testapp;

import java.util.ArrayList;
import org.infogrid.jee.viewlet.DefaultJspViewlet;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.jee.viewlet.blob.BlobViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.Blob.BlobSubjectArea;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.util.ArrayHelper;
import org.infogrid.viewlet.AbstractViewletFactory;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * ViewletFactory for the TestApp application.
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
        JeeMeshObjectsToView realToView = (JeeMeshObjectsToView) toView;

        ArrayList<ViewletFactoryChoice> ret = new ArrayList<ViewletFactoryChoice>();
        
        MeshObject subject = toView.getSubject();

        if( subject.isBlessedBy( BlobSubjectArea.IMAGE )) {
            ret.add( BlobViewlet.choice( realToView, ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
        }
        if( subject.isBlessedBy( TestSubjectArea.OPTIONALPROPERTIES )) {
            ret.add( DefaultJspViewlet.choice( realToView, "org.infogrid.jee.viewlet.alternatedefaultpropertysheet.AlternateDefaultPropertySheetViewlet", ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
        }
        ret.add( DefaultJspViewlet.choice( realToView, "org.infogrid.jee.viewlet.propertysheet.PropertySheetViewlet", ViewletFactoryChoice.GOOD_MATCH_QUALITY ));

        return ArrayHelper.copyIntoNewArray( ret, ViewletFactoryChoice.class );
    }
}
