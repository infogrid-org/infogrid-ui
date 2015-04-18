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
// Copyright 1998-2013 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

//
// This file has been generated AUTOMATICALLY. DO NOT MODIFY.
// on Sat, 2015-04-18 16:39:28 +0000
//
// DO NOT MODIFY -- re-generate!

package org.infogrid.model.Viewlet;

import org.infogrid.model.primitives.*;
import org.infogrid.mesh.*;
import org.infogrid.modelbase.ModelBaseSingleton;

/**
  * <p>Collects the MeshTypes in this Subject Area for simpler access.</p>
  */
public abstract class ViewletSubjectArea
{
    /**
      * The SubjectArea itself.
      */
    public static final SubjectArea _SA = ModelBaseSingleton.findSubjectArea( "org.infogrid.model.Viewlet" );

    /**
      * Enables any MeshObject to express its preference of Viewlet type for rendering.
      */
    public static final EntityType VIEWLETPREFERENCE = org.infogrid.model.Viewlet.ViewletPreference._TYPE;

    /**
      * Name of the Viewlet Type that should be used to render this MeshObject
      */
    public static final PropertyType VIEWLETPREFERENCE_VIEWLETTYPE = org.infogrid.model.Viewlet.ViewletPreference.VIEWLETTYPE;
    public static final org.infogrid.model.primitives.StringDataType VIEWLETPREFERENCE_VIEWLETTYPE_type = (org.infogrid.model.primitives.StringDataType) VIEWLETPREFERENCE_VIEWLETTYPE.getDataType();

}
