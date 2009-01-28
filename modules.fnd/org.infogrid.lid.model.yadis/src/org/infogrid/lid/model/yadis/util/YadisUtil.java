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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.lid.model.yadis.util;

import java.util.ArrayList;
import org.infogrid.lid.model.yadis.YadisSubjectArea;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.ByPropertyValueSorter;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.mesh.set.MeshObjectSorter;
import org.infogrid.mesh.set.OrderedMeshObjectSet;
import org.infogrid.util.ArrayHelper;

/**
 * A few Yadis-related utilities.
 */
public abstract class YadisUtil
{
    /**
     * Private constructor to keep this class abstract.
     */
    private YadisUtil()
    {
        // nothing
    }

    /**
     * Determine the set of service endpoints from a service, in order of priority.
     *
     * @param service the YadisService
     * @return the set of endpoints, in order
     */
    public static OrderedMeshObjectSet determineOrderedEndpoints(
            MeshObject service )
    {
        MeshObjectSetFactory factory = service.getMeshBase().getMeshObjectSetFactory();

        MeshObjectSet        endpoints        = service.traverse( YadisSubjectArea.XRDSSERVICE_ISPROVIDEDAT_ENDPOINT.getSource() );
        OrderedMeshObjectSet orderedEndpoints = factory.createOrderedImmutableMeshObjectSet( endpoints, theByPrioritySorter );

        ArrayList<MeshObject> almost = new ArrayList<MeshObject>( orderedEndpoints.size() );

        for( MeshObject current : orderedEndpoints ) {
            MeshObjectSet resources = current.traverse( YadisSubjectArea.ENDPOINT_ISOPERATEDBY_WEBRESOURCE.getSource() );
            for( MeshObject current2 : resources ) {
                almost.add( current2 );
            }
        }
        OrderedMeshObjectSet ret = factory.createOrderedImmutableMeshObjectSet(
                ArrayHelper.copyIntoNewArray( almost, MeshObject.class ));

        return ret;
    }

    /**
     * Sorts by priority.
     */
    public static final MeshObjectSorter theByPrioritySorter
            = ByPropertyValueSorter.create( YadisSubjectArea.XRDSSERVICE_PRIORITY );
}
