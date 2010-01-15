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
import org.infogrid.mesh.set.ByRelatedMeshObjectSelector;
import org.infogrid.mesh.set.ByTypeMeshObjectSelector;
import org.infogrid.mesh.set.MeshObjectSelector;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.mesh.set.MeshObjectSorter;
import org.infogrid.mesh.set.OrderedMeshObjectSet;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.traversal.SequentialCompoundTraversalSpecification;
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
     * Given a web resource, return the associated XrdsServiceCollection, if any.
     *
     * @param resource the web resource
     * @return the XrdsServiceCollection, if any
     */
    public static MeshObject determineXrdsServiceCollection(
            MeshObject resource )
    {
        if( resource.isBlessedBy( YadisSubjectArea.XRDSSERVICECOLLECTION )) {
            return resource;
        }
        MeshObjectSet xrdsServiceCollectionSet = resource.traverse( YadisSubjectArea.WEBRESOURCE_HASXRDSLINKTO_WEBRESOURCE.getSource() );
        return xrdsServiceCollectionSet.getSingleMember();
    }

    /**
     * Given a web resource, return the set of associated services, in order of priority.
     *
     * @param resource the web resource
     * @return the set of services, in order of priority
     */
    public static OrderedMeshObjectSet determineServicesFor(
            MeshObject resource )
    {
        return determineServicesFor( resource, null );
    }

    /**
     * Given a web resource, return the set of associated services of a particular type, in order of priority.
     *
     * @param resource the web resource
     * @param type the type of services to look for
     * @return the set of services, in order of priority
     */
    public static OrderedMeshObjectSet determineServicesFor(
            MeshObject resource,
            EntityType type )
    {
        MeshObjectSetFactory factory = resource.getMeshBase().getMeshObjectSetFactory();

        MeshObjectSelector servicesSelector = ( type != null ) ? ByTypeMeshObjectSelector.create( type ) : null;

    // Resource may either be blessed with XrdsCollection itself, or reference one
        MeshObjectSet directAuthServices = resource.traverse( YadisSubjectArea.XRDSSERVICECOLLECTION_COLLECTS_XRDSSERVICE.getSource() );
        if( servicesSelector != null ) {
            directAuthServices = factory.createImmutableMeshObjectSet( directAuthServices, servicesSelector );
        }

        MeshObjectSet indirectAuthServices = resource.traverse( YadisSubjectArea.WEBRESOURCE_HASXRDSLINKTO_WEBRESOURCE.getSource() );
        indirectAuthServices = indirectAuthServices.traverse( YadisSubjectArea.XRDSSERVICECOLLECTION_COLLECTS_XRDSSERVICE.getSource() );
        if( servicesSelector != null ) {
            indirectAuthServices = factory.createImmutableMeshObjectSet( indirectAuthServices, servicesSelector );
        }

        MeshObjectSet allAuthServices = factory.createImmutableMeshObjectSetUnification( directAuthServices, indirectAuthServices );
        allAuthServices = factory.createImmutableMeshObjectSet( allAuthServices, theHasEndpointSelector );

        OrderedMeshObjectSet ret = factory.createOrderedImmutableMeshObjectSet( allAuthServices, theXrdsServiceByPrioritySorter );

        return ret;
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
        OrderedMeshObjectSet orderedEndpoints = factory.createOrderedImmutableMeshObjectSet( endpoints, theEndpointByPrioritySorter );

        return orderedEndpoints;
    }

    /**
     * Determine the web resources that act as endpoints for a given service, in order of
     * the endpoints' priorities.
     *
     * @param service the YadisService
     * @return the set of web resource endpoints, in order
     */
    public static OrderedMeshObjectSet determineOrderedEndpointWebResources(
            MeshObject service )
    {
        MeshObjectSetFactory factory = service.getMeshBase().getMeshObjectSetFactory();
        OrderedMeshObjectSet orderedEndpoints = determineOrderedEndpoints( service );

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
     * Sorts by XRDS service priority.
     */
    public static final MeshObjectSorter theXrdsServiceByPrioritySorter
            = ByPropertyValueSorter.create( YadisSubjectArea.XRDSSERVICE_PRIORITY );

    /**
     * Sorts by endpoint priority.
     */
    public static final MeshObjectSorter theEndpointByPrioritySorter
            = ByPropertyValueSorter.create( YadisSubjectArea.ENDPOINT_PRIORITY );

    /**
     * The selector for authentication services that have at least one endpoint.
     */
    protected static final MeshObjectSelector theHasEndpointSelector
            = ByRelatedMeshObjectSelector.createOneOrMore(
                    SequentialCompoundTraversalSpecification.create(
                            YadisSubjectArea.XRDSSERVICE_ISPROVIDEDAT_ENDPOINT.getSource(),
                            YadisSubjectArea.ENDPOINT_ISOPERATEDBY_WEBRESOURCE.getSource() ));
}
